package com.epam.store.service;

import com.epam.store.entity.OrderCard;

import java.util.List;

public interface OrderCardService extends AbstractService<OrderCard, Long> {
    List<OrderCard> findAllSortByItemCountDesc();
    List<OrderCard> findAllByOrderId(Long id);
    List<OrderCard> findAllByPhoneId(Long id);
}
