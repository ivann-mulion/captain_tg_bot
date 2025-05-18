package com.mulion.data_base.repositories;

import com.mulion.models.Boat;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class BoatRepository implements Repository<Boat, Long> {
    private final SessionFactory sessionFactory;

    @Override
    public Optional<Boat> findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return Optional.ofNullable(session.get(Boat.class, id));
        }
    }

    @Override
    public List<Boat> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("from Boat", Boat.class).list();
        }
    }

    @Override
    public void create(Boat boat) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(boat);
            transaction.commit();
        }
    }

    @Override
    public void delete(Boat boat) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.remove(boat);
            transaction.commit();
        }
    }

    @Override
    public void deleteById(Long id) {
        Optional<Boat> boat = findById(id);
        if (boat.isEmpty()) return;
        delete(boat.get());
    }

    @Override
    public void update(Boat boat) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(boat);
            transaction.commit();
        }
    }

    @Override
    public Session openSession() {
        return sessionFactory.openSession();
    }
}
