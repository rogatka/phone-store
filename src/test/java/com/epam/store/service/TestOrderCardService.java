package com.epam.store.service;

import com.epam.store.config.ConfigService;
import com.epam.store.config.WebJavaConfig;
import com.epam.store.dao.OrderCardDAO;
import com.epam.store.dao.OrderDAO;
import com.epam.store.dao.OrderStatusHistoryDAO;
import com.epam.store.entity.Order;
import com.epam.store.entity.OrderCard;
import com.epam.store.entity.OrderStatus;
import com.epam.store.entity.Phone;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ConfigService.class})
@ExtendWith(SpringExtension.class)
public class TestOrderCardService {

    @Mock
    private OrderCardDAO orderCardDAO;
    @Mock
    private OrderDAO orderDAO;

    @InjectMocks
    private OrderCardServiceImpl orderCardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void findByIdShouldThrowExceptionIfIdIsNull() {
        assertThatThrownBy(() ->
                orderCardService.findById(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Id must");
    }

    @Test
    void findByPhoneIdShouldThrowExceptionIfPhoneIdIsNull() {
        assertThatThrownBy(() ->
                orderCardService.findAllByPhoneId(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Phone id must");
    }

    @Test
    void findByOrderIdShouldThrowExceptionIfOrderIdIsNull() {
        assertThatThrownBy(() ->
                orderCardService.findAllByOrderId(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Order id must");
    }

    @Test
    void deleteByIdShouldThrowExceptionIfIdIsNull() {
        assertThatThrownBy(() ->
                orderCardService.deleteById(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Id must");
    }

    @Test
    void saveShouldThrowExceptionIfOrderCardIsNull() {
        assertThatThrownBy(() ->
                orderCardService.save(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Order card must");
    }
}
