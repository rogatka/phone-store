package com.epam.store.service;

import com.epam.store.config.ConfigService;
import com.epam.store.dao.*;
import com.epam.store.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ConfigService.class})
@ExtendWith(SpringExtension.class)
public class TestOrderService {

    @Mock
    private OrderDAO orderDAO;
    @Mock
    private PhoneDAO phoneDAO;

    @Mock
    private OrderCardDAO orderCardDAO;

    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void findByIdShouldThrowExceptionIfIdIsNull() {
        assertThatThrownBy(() ->
                orderService.findById(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Id must");
    }

    @Test
    void findByAccountIdShouldThrowExceptionIfAccountIdIsNull() {
        assertThatThrownBy(() ->
                orderService.findAllByAccountId(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Account id must");
    }

    @Test
    void deleteByIdShouldThrowExceptionIfIdIsNull() {
        assertThatThrownBy(() ->
                orderService.deleteById(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Id must");
    }

    @Test
    void deleteByIdShouldThrowExceptionIfThereAreProcessingOrders() {
        Order order = new Order();
        order.setStatus(OrderStatus.PROCESSING);
        Optional<Order> optionalOrder = Optional.of(order);
        when(orderDAO.findById(anyLong())).thenReturn(optionalOrder);
        assertThatThrownBy(() ->
                orderService.deleteById(anyLong()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Cannot delete order");
    }

    @Test
    void saveShouldThrowExceptionIfOrderIsNull() {
        assertThatThrownBy(() ->
                orderService.save(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Order must");
    }

    @Test
    void saveShouldThrowExceptionIfOrderCardsNotEmptyAndStatusIsNullAndIdIsNull() {
        Order order = new Order();
        when(orderCardDAO.findAllByOrderId(any())).thenReturn(Arrays.asList(new OrderCard()));
        assertThatThrownBy(() ->
                orderService.save(order))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Just created order");
    }

    @Test
    void saveShouldThrowExceptionIfOrderCardsNotEmptyAndStatusIsNullAndIdIsNotNull() {
        Order order = new Order();
        when(orderCardDAO.findAllByOrderId(any())).thenReturn(Arrays.asList(new OrderCard()));
        order.setId(1L);
        assertThatThrownBy(() ->
                orderService.save(order))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Order status");
    }

    @Test
    void saveShouldThrowExceptionIfOrderCardsIsEmptyAndStatusIsProcessingAndIdIsNotNull() {
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.PROCESSING);
        Account account = new Account();
        account.setUser(new User());
        account.setAmount(BigDecimal.ZERO);
        order.setAccount(account);
        when(orderCardDAO.findAllByOrderId(any())).thenReturn(Collections.emptyList());
        assertThatThrownBy(() ->
                orderService.save(order))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Started order");
    }

    @Test
    void addOrderCardShouldThrowExceptionIfPhoneCountLessThanItemCount() {
        Phone phone = new Phone();
        phone.setCount(10L);
        phone.setPrice(BigDecimal.valueOf(10));
        OrderCard orderCard = new OrderCard();
        orderCard.setPhone(phone);
        orderCard.setItemCount(1000L);
        when(orderCardDAO.findAllByOrderId(any())).thenReturn(Collections.singletonList(orderCard));
        when(phoneDAO.findById(any())).thenReturn(Optional.of(phone));
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.NOT_STARTED);
        orderCard.setOrder(order);
        when(orderDAO.findById(anyLong())).thenReturn(Optional.of(order));
        assertThatThrownBy(() ->
                orderService.saveOrderCard(order.getId(), orderCard))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Item count");
    }

    @Test
    void saveShouldThrowExceptionIfAccountAmountLessThanOrderTotalSum() {
        Phone phone = new Phone();
        phone.setCount(100L);
        phone.setPrice(BigDecimal.valueOf(10));
        OrderCard orderCard = new OrderCard();
        orderCard.setPhone(phone);
        orderCard.setItemCount(10L);
        when(orderCardDAO.findAllByOrderId(any())).thenReturn(Collections.singletonList(orderCard));
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.NOT_STARTED);
        Account account = new Account();
        account.setUser(new User());
        account.setAmount(BigDecimal.valueOf(10));
        order.setAccount(account);
        order.setTotalSum(phone.getPrice().add(BigDecimal.valueOf(orderCard.getItemCount())));
        when(orderDAO.findById(anyLong())).thenReturn(Optional.of(order));
        assertThatThrownBy(() ->
                orderService.save(order))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Order's total sum must be less or equal");
    }
}
