package com.epam.store.service;

import com.epam.store.dao.AccountDAO;
import com.epam.store.dao.OrderDAO;
import com.epam.store.entity.Account;
import com.epam.store.entity.Order;
import com.epam.store.entity.OrderStatus;
import com.epam.store.exception.OrderStatusException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AccountServiceImpl implements AccountService {

    public static final String ACCOUNT_MUST_NOT_BE_NULL = "Account must not be null";
    public static final String USER_ID_MUST_NOT_BE_NULL = "User id must not be null";
    private AccountDAO accountDAO;
    private OrderDAO orderDAO;

    public AccountServiceImpl(AccountDAO accountDAO, OrderDAO orderDAO) {
        this.accountDAO = accountDAO;
        this.orderDAO = orderDAO;
    }

    @Override
    public List<Account> findAll() {
        return accountDAO.findAll();
    }

    @Override
    public Optional<Account> findById(Long id) {
        Objects.requireNonNull(id, ID_MUST_NOT_BE_NULL);
        return accountDAO.findById(id);
    }

    @Override
    public Optional<Account> findByUserId(Long userId) {
        Objects.requireNonNull(userId, USER_ID_MUST_NOT_BE_NULL);
        return accountDAO.findByUserId(userId);
    }

    @Override
    public Account save(Account account) {
        Objects.requireNonNull(account, ACCOUNT_MUST_NOT_BE_NULL);
        return accountDAO.save(account);
    }

    @Override
    public void deleteById(Long id) {
        Objects.requireNonNull(id, ID_MUST_NOT_BE_NULL);
        List<Order> orders = orderDAO.findAllByAccountId(id);
        if (!orders.isEmpty()) {
            checkOrders(id, orders);
        }
        accountDAO.deleteById(id);
    }

    private void checkOrders(Long accountId, List<Order> orders) {
        for (Order order: orders) {
            if (order.getStatus() == OrderStatus.PROCESSING) {
                throw new OrderStatusException(String.format("Cannot delete account with id=%d because there is processing order(id=%d) with that account", accountId, order.getId()));
            } else if (order.getStatus() == OrderStatus.NOT_STARTED) {
                throw new OrderStatusException(String.format("Cannot delete account with id=%d. Please first manually delete not started orders", accountId));
            }
        }
    }
}
