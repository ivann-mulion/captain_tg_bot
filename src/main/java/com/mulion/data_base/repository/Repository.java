package com.mulion.data_base.repository;

import java.util.List;
import java.util.Optional;

public interface Repository<T, ID> {
    Optional<T> findById(ID id);

    List<T> findAll();

    void create(T t);

    void delete(T t);

    void deleteById(ID id);

    T update(T t);
}
