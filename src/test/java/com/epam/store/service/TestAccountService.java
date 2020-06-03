package com.epam.store.service;

import com.epam.store.config.ConfigService;
import com.epam.store.config.WebJavaConfig;
import com.epam.store.dao.AccountDAO;
import com.epam.store.dao.OrderDAO;
import com.epam.store.entity.Account;
import com.epam.store.entity.Order;
import com.epam.store.entity.OrderStatus;
import com.epam.store.entity.User;
import com.epam.store.exception.OrderStatusException;
import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.validation.ConstraintViolation;
import javax.validation.Valid;
import javax.validation.Validator;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ConfigService.class})
@ExtendWith(SpringExtension.class)
//@WebAppConfiguration
public class TestAccountService {
    @Mock
    private OrderDAO orderDAO;

    @InjectMocks
    private AccountServiceImpl accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void findByIdShouldThrowExceptionIfIdIsNull() {
        assertThatThrownBy(() ->
                accountService.findById(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Id must");
    }

    @Test
    void findByUserIdShouldThrowExceptionIfUserIdIsNull() {
        assertThatThrownBy(() ->
                accountService.findByUserId(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("User id must");
    }

    @Test
    void deleteByIdShouldThrowExceptionIfIdIsNull() {
        assertThatThrownBy(() ->
                accountService.deleteById(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Id must");
    }

    @Test
    void deleteByIdShouldThrowExceptionIfThereAreProcessingOrders() {
        Order order = new Order();
        order.setStatus(OrderStatus.PROCESSING);
        when(orderDAO.findAllByAccountId(anyLong())).thenReturn(Collections.singletonList(order));
        assertThatThrownBy(() ->
                accountService.deleteById(anyLong()))
                .isInstanceOf(OrderStatusException.class)
                .hasMessageStartingWith("Cannot delete account");
    }

    @Test
    void saveShouldThrowExceptionIfAccountIsNull() {
        assertThatThrownBy(() ->
                accountService.save(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Account must");
    }
}
