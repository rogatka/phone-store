package com.epam.store.dao;

import com.epam.store.entity.Account;
import com.epam.store.entity.Order;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;

public class OrderDAOImpl implements OrderDAO {

    private EntityManagerFactory entityManagerFactory;

    public OrderDAOImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public List<Order> findAll() {
        List<Order> orders;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TypedQuery<Order> query = entityManager.createQuery("from Order", Order.class);
        orders = query.getResultList();
        entityManager.close();
        return orders;
    }

    @Override
    public List<Order> findAllByAccountId(Long id) {
        List<Order> orders;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TypedQuery<Order> query = entityManager.createQuery("select o from Order o where o.account.id=:id", Order.class)
                .setParameter("id", id);
        orders = query.getResultList();
        entityManager.close();
        return orders;
    }

    @Override
    public Optional<Order> findById(Long id) {
        List<Order> orders;
        Optional<Order> order;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query query = entityManager.createQuery("select o from Order o LEFT join fetch o.orderCards oc where o.id=:id")
                .setParameter("id", id);
        orders = query.getResultList();
        if (orders.isEmpty()) {
            order = Optional.empty();
        } else {
            order = Optional.of(orders.get(0));
        }
        entityManager.close();
        return order;
    }

    @Override
    public Order save(Order order) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            if (order.getId() == null) {
                entityManager.persist(order);
            } else {
                order = entityManager.merge(order);
            }
            transaction.commit();
            return order;
        } catch (EntityExistsException | IllegalArgumentException e) {
            transaction.rollback();
            throw new IllegalArgumentException(e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public void deleteById(Long id) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        Query query = entityManager.createQuery("delete from Order where id=:id")
                .setParameter("id", id);
        try {
            query.executeUpdate();
            transaction.commit();
        } catch (RuntimeException e) {
            transaction.rollback();
            throw new RuntimeException(e);
        } finally {
            entityManager.close();
        }
    }
}
