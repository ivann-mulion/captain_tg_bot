package com.mulion.data_base.repositories;

import com.mulion.data_base.SessionProvider;
import com.mulion.models.User;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class UserRepository implements Repository<User, Long> {
    private final SessionProvider provider;

    @Override
    public Optional<User> findById(Long id) {
        Session session = provider.getSession();
        return Optional.ofNullable(session.get(User.class, id));
    }

    @Override
    public List<User> findAll() {
        Session session = provider.getSession();
        return session.createQuery("from User", User.class).list();
    }

    @Override
    public void create(User user) {
        Session session = provider.getSession();
        Transaction transaction = session.beginTransaction();
        session.persist(user);
        transaction.commit();
    }

    @Override
    public void delete(User user) {
        Session session = provider.getSession();
        Transaction transaction = session.beginTransaction();
        session.remove(user);
        transaction.commit();
    }

    @Override
    public void update(User user) {
        Session session = provider.getSession();
        Transaction transaction = session.beginTransaction();
        session.merge(user);
        transaction.commit();
    }

    @Override
    public Session getSession() {
        return provider.getSession();
    }
}
