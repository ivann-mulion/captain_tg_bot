package com.mulion.data_base.services;

import com.mulion.data_base.repository.Repository;
import com.mulion.models.Boat;
import com.mulion.models.User;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.hibernate.Session;

import java.util.List;

@RequiredArgsConstructor
public class DBBoatService {
    private final Repository<Boat, Long> repository;

    public Boat getBoat(Long boatId) {
        return repository.findById(boatId).orElse(null);
    }

    public List<Boat> getBoats() {
        return repository.findAll();
    }

    public Boat addBoat(Boat boat) {
        repository.create(boat);
        return boat;
    }

    public void updateBoat(Boat boat) {
        repository.update(boat);
    }
}
