package com.epam.store.dao;

import com.epam.store.entity.Account;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;

public class AccountDAOImpl implements AccountDAO {

    private EntityManagerFactory entityManagerFactory;

    public AccountDAOImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public List<Account> findAll() {
        List<Account> accounts;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TypedQuery<Account> query = entityManager.createQuery("select u from Account u", Account.class);
        accounts = query.getResultList();
        entityManager.close();
        return accounts;
    }

    @Override
    public Optional<Account> findById(Long id) {
        Account account;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        account = entityManager.find(Account.class, id);
        entityManager.close();
        return Optional.ofNullable(account);
    }

    @Override
    public Optional<Account> findByUserId(Long userId) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TypedQuery<Account> query = entityManager.createQuery("from Account where user.id=:id", Account.class)
                .setParameter("id", userId);
        List<Account> accounts = query.getResultList();
        if (accounts.isEmpty()) {
            return Optional.empty();
        } else if (accounts.size() > 1) {
            throw new RuntimeException("More than 1 account with same user was found. Founded accounts=" + accounts + ".User Id=" + userId);
        }
        entityManager.close();
        return Optional.of(accounts.get(0));
    }

    @Override
    public Account save(Account account) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            if (account.getId() == null) {
                entityManager.persist(account);
            } else {
                account = entityManager.merge(account);
            }
            transaction.commit();
            return account;
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
        Query query = entityManager.createQuery("delete from Account where id=:id")
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
