package com.mulion.data_base.repositories;

import com.mulion.data_base.SessionProvider;
import com.mulion.models.Boat;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class BoatRepository implements Repository<Boat, Long> {
    private final SessionProvider provider;

    @Override
    public Optional<Boat> findById(Long id) {
        Session session = provider.getSession();
        return Optional.ofNullable(session.get(Boat.class, id));
    }

    @Override
    public List<Boat> findAll() {
        Session session = provider.getSession();
        return session.createQuery("from Boat", Boat.class).list();
    }

    @Override
    public void create(Boat boat) {
        Session session = provider.getSession();
        Transaction transaction = session.beginTransaction();
        session.persist(boat);
        transaction.commit();
    }

    @Override
    public void delete(Boat boat) {
        Session session = provider.getSession();
        Transaction transaction = session.beginTransaction();
        session.remove(boat);
        transaction.commit();
    }

    @Override
    public void update(Boat boat) {
        Session session = provider.getSession();
        Transaction transaction = session.beginTransaction();
        session.merge(boat);
        transaction.commit();
    }

    @Override
    public Session getSession() {
        return provider.getSession();
    }
}
