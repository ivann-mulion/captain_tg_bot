package com.mulion.data_base.repositories;

import org.hibernate.Session;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    Optional<T> findById(ID id);

    List<T> findAll();

    void create(T t);

    void delete(T t);

    void deleteById(ID id);

    void update(T t);

    Session openSession();
}
