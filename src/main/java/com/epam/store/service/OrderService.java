package com.epam.store.service;

import com.epam.store.entity.Order;
import com.epam.store.entity.OrderCard;

import java.util.List;

public interface OrderService extends AbstractService<Order, Long> {
    List<Order> findAllByAccountId(Long id);
    void saveOrderCard(Long orderId, OrderCard orderCard);
    void deleteOrderCard(Long orderId, Long orderCardId);
}
