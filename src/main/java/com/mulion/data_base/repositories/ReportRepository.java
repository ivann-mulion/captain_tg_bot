package com.mulion.data_base.repositories;

import com.mulion.data_base.SessionProvider;
import com.mulion.models.Report;
import com.mulion.models.ReportId;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class ReportRepository implements Repository<Report, ReportId> {
    private final SessionProvider provider;

    @Override
    public Optional<Report> findById(ReportId id) {
        Session session = provider.getSession();
        return Optional.ofNullable(session.get(Report.class, id));
    }

    @Override
    public List<Report> findAll() {
        Session session = provider.getSession();
        return session.createQuery("from Report", Report.class).list();
    }

    @Override
    public void create(Report report) {
        Session session = provider.getSession();
        Transaction transaction = session.beginTransaction();
        session.persist(report);
        transaction.commit();
    }

    @Override
    public void delete(Report report) {
        Session session = provider.getSession();
        Transaction transaction = session.beginTransaction();
        session.remove(report);
        transaction.commit();
    }

    @Override
    public void update(Report report) {
        Session session = provider.getSession();
            Transaction transaction = session.beginTransaction();
            session.merge(report);
            transaction.commit();
    }

    @Override
    public Session getSession() {
        return provider.getSession();
    }
}
