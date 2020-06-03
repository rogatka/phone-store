package com.epam.store.dao;


import com.epam.store.entity.Phone;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;

public class PhoneDAOImpl implements PhoneDAO {
    private EntityManagerFactory entityManagerFactory;

    public PhoneDAOImpl(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    @Override
    public List<Phone> findAll() {
        List<Phone> phones;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TypedQuery<Phone> query = entityManager.createQuery("from Phone", Phone.class);
        phones = query.getResultList();
        entityManager.close();
        return phones;
    }

    @Override
    public void saveAll(List<Phone> phoneList) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            for (Phone phone: phoneList) {
                entityManager.merge(phone);
            }
            transaction.commit();
        } catch (IllegalArgumentException e) {
            transaction.rollback();
            throw new IllegalArgumentException(e);
        } finally {
            entityManager.close();
        }
    }

    @Override
    public Optional<Phone> findByModelName(String modelName) {
        Optional<Phone> phone;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        TypedQuery<Phone> query = entityManager.createQuery("from Phone where model like :modelName", Phone.class)
                .setParameter("modelName", modelName.trim());
        List<Phone> phones = query.getResultList();
        if (phones.isEmpty()) {
            phone = Optional.empty();
        } else if (phones.size() > 1) {
            throw new RuntimeException("More than 1 phones with same model was found. Founded phones=" + phones.size() + ".Model name=" + modelName);
        } else {
            phone = Optional.of(phones.get(0));
        }
        entityManager.close();
        return phone;
    }

    @Override
    public Optional<Phone> findById(Long id) {
        Phone phone;
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        phone = entityManager.find(Phone.class, id);
        entityManager.close();
        return Optional.ofNullable(phone);
    }

    @Override
    public Phone save(Phone phone) {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        transaction.begin();
        try {
            if (phone.getId() == null) {
                entityManager.persist(phone);
            } else {
                phone = entityManager.merge(phone);
            }
            transaction.commit();
            return phone;
        } catch (EntityExistsException|IllegalArgumentException e) {
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
        Query query = entityManager.createQuery("delete from Phone where id=:id")
                .setParameter("id", id);
        transaction.begin();
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
