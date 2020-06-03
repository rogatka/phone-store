package com.epam.store.service;

import com.epam.store.config.ConfigService;
import com.epam.store.config.WebJavaConfig;
import com.epam.store.dao.OrderCardDAO;
import com.epam.store.dao.PhoneDAO;
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

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {ConfigService.class})
@ExtendWith(SpringExtension.class)
public class TestPhoneService {

    @Mock
    private PhoneDAO phoneDAO;
    @Mock
    private OrderCardDAO orderCardDAO;

    @InjectMocks
    private PhoneServiceImpl phoneService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void findByIdShouldThrowExceptionIfIdIsNull() {
        assertThatThrownBy(() ->
                phoneService.findById(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Id must");
    }

    @Test
    void findByModelNameShouldThrowExceptionIfModelNameIsNull() {
        assertThatThrownBy(() ->
                phoneService.findByModelName(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Model name must");
    }

    @Test
    void saveAllShouldThrowExceptionIfPhoneListIsNull() {
        assertThatThrownBy(() ->
                phoneService.saveAll(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Phone list must");
    }

    @Test
    void saveShouldThrowExceptionIfPhoneIsNull() {
        assertThatThrownBy(() ->
                phoneService.save(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Phone must");
    }

    @Test
    void deleteByIdShouldThrowExceptionIfIdIsNull() {
        assertThatThrownBy(() ->
                phoneService.deleteById(null))
                .isInstanceOf(RuntimeException.class)
                .hasMessageStartingWith("Id must");
    }

    @Test
    void deleteByIdShouldThrowExceptionIfThereAreProcessingOrders() {
        Order order = new Order();
        order.setStatus(OrderStatus.PROCESSING);
        OrderCard orderCard = new OrderCard();
        orderCard.setOrder(order);
        when(orderCardDAO.findAllByPhoneId(any())).thenReturn(Arrays.asList(orderCard));
        Phone phone = new Phone();
        Optional<Phone> phoneOptional = Optional.of(phone);
        when(phoneDAO.findById(any())).thenReturn(phoneOptional);
        assertThatThrownBy(() ->
                phoneService.deleteById(anyLong()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageStartingWith("Cannot delete phone");
    }
}
