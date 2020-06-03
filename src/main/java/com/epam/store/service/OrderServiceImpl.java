package com.epam.store.service;

import com.epam.store.dao.*;
import com.epam.store.entity.*;
import com.epam.store.exception.OrderStatusException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
public class OrderServiceImpl implements OrderService {

    private static final String ORDER_MUST_NOT_BE_NULL = "Order must not be null";
    private static final String ORDER_CARD_MUST_NOT_BE_NULL = "Order card must not be null";
    private static final String ORDER_STATUS_MUST_NOT_BE_NULL = "Order status must not be null";
    public static final String ACCOUNT_ID_MUST_NOT_BE_NULL = "Account id must not be null";
    public static final String ORDER_CARD_ID_MUST_NOT_BE_NULL = "Order card Id must not be null";
    public static final String STARTED_ORDER_CANNOT_HAVE_EMPTY_ORDER_CARDS = "Started order cannot have empty order cards";
    public static final String JUST_CREATED_ORDER_CANNOT_HAVE_ORDER_CARDS = "Just created order cannot have order cards";
    public static final String ACCOUNT_MUST_NOT_BE_NULL = "Account must not be null";
    public static final String NEW_ORDER_MUST_HAVE_NOT_STARTED_STATUS = "New order must have 'Not Started' order status";

    private OrderDAO orderDAO;
    private PhoneDAO phoneDAO;
    private OrderStatusHistoryDAO orderStatusHistoryDAO;
    private OrderCardDAO orderCardDAO;

    public OrderServiceImpl(OrderDAO orderDAO, PhoneDAO phoneDAO, OrderStatusHistoryDAO orderStatusHistoryDAO, OrderCardDAO orderCardDAO) {
        this.orderDAO = orderDAO;
        this.phoneDAO = phoneDAO;
        this.orderStatusHistoryDAO = orderStatusHistoryDAO;
        this.orderCardDAO = orderCardDAO;
    }

    @Override
    public List<Order> findAllByAccountId(Long accountId) {
        Objects.requireNonNull(accountId, ACCOUNT_ID_MUST_NOT_BE_NULL);
        return orderDAO.findAllByAccountId(accountId);
    }

    @Override
    public List<Order> findAll() {
        return orderDAO.findAll();
    }

    @Override
    public Optional<Order> findById(Long id) {
        Objects.requireNonNull(id, ID_MUST_NOT_BE_NULL);
        return orderDAO.findById(id);
    }

    @Override
    public Order save(Order order) {
        Objects.requireNonNull(order, ORDER_MUST_NOT_BE_NULL);
        List<OrderCard> orderCards = orderCardDAO.findAllByOrderId(order.getId());
        if (order.getId() == null) {
            if (order.getStatus() == null) {
                addOrderStatus(order, OrderStatus.NOT_STARTED);
            }
            if (order.getStatus() == OrderStatus.NOT_STARTED) {
                if (orderCards.isEmpty()) {
                    order.setTotalSum(BigDecimal.ZERO);
                    return orderDAO.save(order);
                } else {
                    throw new IllegalArgumentException(JUST_CREATED_ORDER_CANNOT_HAVE_ORDER_CARDS);
                }
            } else {
                throw new OrderStatusException(NEW_ORDER_MUST_HAVE_NOT_STARTED_STATUS);
            }
        }
        else {
            Objects.requireNonNull(order.getStatus(), ORDER_STATUS_MUST_NOT_BE_NULL);
            if (order.getStatus() != OrderStatus.NOT_STARTED && (orderCards.isEmpty())) {
                throw new IllegalArgumentException(STARTED_ORDER_CANNOT_HAVE_EMPTY_ORDER_CARDS);
            }
            Order orderFromDb = orderDAO.findById(order.getId()).get();
            if (orderFromDb.getStatus() != order.getStatus()) {
                addOrderStatus(order, order.getStatus());
            }
        }
        calculateAccountAmount(order, orderCards);
        return orderDAO.save(order);
    }

    private void addOrderStatus(Order order, OrderStatus status) {
        order.setStatus(status);
        OrderStatusHistory orderStatusHistory = new OrderStatusHistory();
        orderStatusHistory.setOrder(order);
        orderStatusHistory.setTimeStamp(LocalDateTime.now());
        orderStatusHistory.setOrderStatus(status);
        List<OrderStatusHistory> orderStatusHistoryList;
        if (order.getId() == null) {
            orderStatusHistoryList = new ArrayList<>();
        } else {
            orderStatusHistoryList = orderStatusHistoryDAO.findAllByOrderId(order.getId());
        }
        orderStatusHistoryList.add(orderStatusHistory);
        order.setOrderStatusHistoryList(orderStatusHistoryList);
    }

    private void calculateAccountAmount(Order order, List<OrderCard> orderCards) {
        Account account = order.getAccount();
        Objects.requireNonNull(account, ACCOUNT_MUST_NOT_BE_NULL);
        BigDecimal orderTotalSum = order.getTotalSum();
        BigDecimal accountAmount = account.getAmount();
        if (order.getStatus() == OrderStatus.CANCEL) {
            account.setAmount(accountAmount.add(orderTotalSum));
            calculatePhoneCount(orderCards);
        } else {
            if (accountAmount.compareTo(orderTotalSum) < 0) {
                throw new IllegalArgumentException("Order's total sum must be less or equal to account's amount");
            }
            account.setAmount(accountAmount.subtract(orderTotalSum));
        }
    }

    @Override
    public void deleteOrderCard(Long orderId, Long orderCardId) {
        Objects.requireNonNull(orderId, ID_MUST_NOT_BE_NULL);
        Objects.requireNonNull(orderCardId, ORDER_CARD_ID_MUST_NOT_BE_NULL);
        Order order = orderDAO.findById(orderId).get();
        if (order.getStatus() != OrderStatus.NOT_STARTED) {
            throw new IllegalArgumentException("You can't delete order card from active order. Order status: " + order.getStatus());
        }
        Optional<OrderCard> orderCardOptional = orderCardDAO.findById(orderCardId);
        if (orderCardOptional.isPresent()) {
            OrderCard orderCard = orderCardOptional.get();
            List<OrderCard> orderCards = order.getOrderCards();
            Phone phone = orderCard.getPhone();
            Long phoneCount = phone.getCount();
            phone.setCount(phoneCount + orderCard.getItemCount());
            orderCards.stream()
                    .filter((oc) -> oc.getPhone().getId().equals(phone.getId()))
                    .forEach((oc) -> oc.setPhone(phone));
            orderDAO.save(order);
            orderCards.removeIf((oc) -> oc.getId().equals(orderCardId));
            order.setTotalSum(calculateOrderCardsTotalSum(orderCards));
            orderDAO.save(order);
            orderCardDAO.deleteById(orderCardId);
        } else {
            throw new IllegalArgumentException("Order card with id =" + orderCardId + "is not found");
        }
    }

    @Override
    public void saveOrderCard(Long orderId, OrderCard orderCard) {
        Objects.requireNonNull(orderId, ID_MUST_NOT_BE_NULL);
        Objects.requireNonNull(orderCard, ORDER_CARD_MUST_NOT_BE_NULL);
        Order order = orderDAO.findById(orderId).get();
        Long orderCardId = orderCard.getId();
        checkOrderStatus(order, orderCardId);
        List<OrderCard> orderCards = order.getOrderCards();
        if (orderCardId == null) {
            addOrderCard(orderCard, order);
        } else {
            updateOrderCard(orderCard, order);
        }
    }

    private void updateOrderCard(OrderCard orderCard, Order order) {
        List<OrderCard> orderCards = order.getOrderCards();
        Optional<OrderCard> cardOptional = orderCards.stream()
                .filter((oc) -> oc.getId().equals(orderCard.getId()))
                .findFirst();
        if (cardOptional.isPresent()) {
            OrderCard card = cardOptional.get();
            long previousItemCount = card.getItemCount();
            Phone previousPhone = card.getPhone();
            long newItemCount = orderCard.getItemCount();
            Phone newPhone = orderCard.getPhone();
            Long phoneCount = newPhone.getCount();
            if (!previousPhone.getId().equals(newPhone.getId())) {
                Long oldPhoneCount = previousPhone.getCount();
                previousPhone.setCount(oldPhoneCount + previousItemCount);
                if (phoneCount < newItemCount) {
                    throw new IllegalArgumentException("Item count must be less or equal to phone count. Phone count: " + phoneCount + ". Item count: " + newItemCount);
                }
                newPhone.setCount(phoneCount - newItemCount);
            } else {
                if ((phoneCount + previousItemCount) < newItemCount) {
                    throw new IllegalArgumentException("Item count must be less or equal to phone count. Phone count: " + phoneCount + ". Item count: " + newItemCount);
                }
                newPhone.setCount(phoneCount - (newItemCount - previousItemCount));
            }
            orderDAO.save(order);
            card.setPhone(newPhone);
            card.setItemCount(newItemCount);
            orderCards.stream()
                    .filter((oc) -> oc.getPhone().getId().equals(newPhone.getId()))
                    .forEach((oc) -> oc.setPhone(newPhone));
            order.setTotalSum(calculateOrderCardsTotalSum(orderCards));
            orderDAO.save(order);
        } else {
            throw new IllegalArgumentException("Order card with id =" + orderCard.getId() + "is not found");
        }
    }

    private void addOrderCard(OrderCard orderCard, Order order) {
        List<OrderCard> orderCards = order.getOrderCards();
        Phone phone = orderCard.getPhone();
        Long itemCount = orderCard.getItemCount();
        Long phoneCount = phone.getCount();
        if (phoneCount < itemCount) {
            throw new IllegalArgumentException("Item count must be less or equal to phone count. Phone count: " + phoneCount + ". Item count: " + itemCount);
        }
        phone.setCount(phoneCount - itemCount);
        orderCards.stream()
                .filter((oc) -> oc.getPhone().getId().equals(phone.getId()))
                .forEach((oc) -> oc.setPhone(phone));
        orderCards.add(orderCard);
        order.setTotalSum(calculateOrderCardsTotalSum(orderCards));
        orderDAO.save(order);
    }

    private void checkOrderStatus(Order order, Long orderCardId) {
        if (order.getStatus() != OrderStatus.NOT_STARTED) {
            String errorMessage;
            if (orderCardId != null) {
                errorMessage = "You can't update order card in active or cancelled order. Order status: " + order.getStatus();
            } else {
                errorMessage = "You can't add order card to active or cancelled order. Order status: " + order.getStatus();
            }
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private BigDecimal calculateOrderCardsTotalSum(List<OrderCard> orderCards) {
        BigDecimal orderTotalSum = BigDecimal.ZERO;
        BigDecimal phonePrice;
        Long itemCount;
        for (OrderCard card : orderCards) {
            itemCount = card.getItemCount();
            phonePrice = card.getPhone().getPrice();
            orderTotalSum = orderTotalSum.add(phonePrice.multiply(BigDecimal.valueOf(itemCount)));
        }
        return orderTotalSum;
    }

    @Override
    public void deleteById(Long id) {
        Objects.requireNonNull(id, ID_MUST_NOT_BE_NULL);
        Optional<Order> orderOptional = findById(id);
        if (orderOptional.isPresent()) {
            checkOrderStatus(id, orderOptional);
        }
        orderDAO.deleteById(id);
    }

    private void checkOrderStatus(Long id, Optional<Order> orderOptional) {
        Order order = orderOptional.get();
        if (order.getStatus() == OrderStatus.PROCESSING) {
            throw new IllegalArgumentException("Cannot delete order because it has processing status, order id=" + order.getId());
        } else if (order.getStatus() == OrderStatus.NOT_STARTED) {
            List<OrderCard> orderCards = orderCardDAO.findAllByOrderId(id);
            calculatePhoneCount(orderCards);
        }
    }

    private void calculatePhoneCount(List<OrderCard> orderCards) {
        Map<Phone, Long> phonesCountMap = new HashMap<>();
        List<Phone> phones = phoneDAO.findAll();
        phones.forEach((p) -> phonesCountMap.put(p,p.getCount()));
        for (OrderCard orderCard: orderCards) {
            Phone phone = orderCard.getPhone();
            Long itemCount = orderCard.getItemCount();
            phonesCountMap.merge(phone, itemCount, (a,b) -> a + b);
        }
        phonesCountMap.forEach(Phone::setCount);
        phoneDAO.saveAll(new ArrayList<>(phonesCountMap.keySet()));
    }
}
