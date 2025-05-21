package com.mulion.data_base.services;

import com.mulion.data_base.repositories.ReportRepository;
import com.mulion.models.Report;
import com.mulion.models.ReportId;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@RequiredArgsConstructor
public class DBReportService {
    private final ReportRepository repository;

    public Report getReport(Long boatId, LocalDate date) {
        return repository.findById(new ReportId(boatId, date)).orElse(null);
    }

    public void createReport(Report report) {
        report.getRecords().forEach(repository.getSession()::persist);
        repository.create(report);
    }
}
