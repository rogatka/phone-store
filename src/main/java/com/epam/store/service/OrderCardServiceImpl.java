package com.epam.store.service;

import com.epam.store.dao.OrderCardDAO;
import com.epam.store.dao.OrderDAO;
import com.epam.store.entity.OrderCard;
import com.epam.store.entity.OrderStatus;
import com.epam.store.exception.OrderStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class OrderCardServiceImpl implements OrderCardService {
    public static final String ORDER_CARD_MUST_NOT_BE_NULL = "Order card must not be null";
    public static final String ORDER_ID_MUST_NOT_BE_NULL = "Order id must not be null";
    public static final String PHONE_ID_MUST_NOT_BE_NULL = "Phone id must not be null";

    private OrderCardDAO orderCardDAO;
    private OrderDAO orderDAO;

    public OrderCardServiceImpl(OrderCardDAO orderCardDAO, OrderDAO orderDAO) {
        this.orderCardDAO = orderCardDAO;
        this.orderDAO = orderDAO;
    }

    @Override
    public List<OrderCard> findAllSortByItemCountDesc() {
        return orderCardDAO.findAllSortByItemCountDesc();
    }

    @Override
    public List<OrderCard> findAllByOrderId(Long orderId) {
        Objects.requireNonNull(orderId, ORDER_ID_MUST_NOT_BE_NULL);
        return orderCardDAO.findAllByOrderId(orderId);
    }

    @Override
    public List<OrderCard> findAllByPhoneId(Long phoneId) {
        Objects.requireNonNull(phoneId, PHONE_ID_MUST_NOT_BE_NULL);
        return orderCardDAO.findAllByPhoneId(phoneId);
    }

    @Override
    public List<OrderCard> findAll() {
        return orderCardDAO.findAll();
    }

    @Override
    public Optional<OrderCard> findById(Long id) {
        Objects.requireNonNull(id, ID_MUST_NOT_BE_NULL);
        return orderCardDAO.findById(id);
    }

    @Override
    public OrderCard save(OrderCard orderCard) {
        Objects.requireNonNull(orderCard, ORDER_CARD_MUST_NOT_BE_NULL);
        return orderCardDAO.save(orderCard);
    }

    @Override
    public void deleteById(Long id) {
        Objects.requireNonNull(id, ID_MUST_NOT_BE_NULL);
        Optional<OrderCard> orderCardOptional = findById(id);
        if (orderCardOptional.isPresent()) {
            OrderCard orderCard = orderCardOptional.get();
            if (orderCard.getOrder().getStatus() == OrderStatus.PROCESSING) {
                throw new OrderStatusException("Cannot delete orderCard because order has processing status, order id=" + orderCard.getOrder().getId());
            }
        }
        orderCardDAO.deleteById(id);
    }
}
