package com.mulion.services;

import com.mulion.data_base.services.DBReportService;
import com.mulion.models.Record;
import com.mulion.models.Report;
import com.mulion.constants.ErrorsMessages;
import com.mulion.data_base.services.DBBoatService;
import com.mulion.models.User;
import com.mulion.yclients.services.RecordService;
import com.mulion.yclients.services.YCUserService;
import lombok.RequiredArgsConstructor;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class ReportService {
    private final DBBoatService boatService;
    private final DBReportService dbReportService;

    public String getReportMessage(User user, LocalDate date) {
        Report report = getReport(user, date);
        if (report == null) {
            return ErrorsMessages.AUTH_ERROR;
        }
        return report.getReportMessage();
    }

    private Report getReport(User user, LocalDate date) {
        if (user.getStaffId() == null) {
            return null;
        }
        Report report = getReportFromDB(user, date);
        if (report != null) {
            return report;
        }
        List<Record> records = tryGetRecords(user, date);
        if (records != null) {
            report = new Report(user, date, records, boatService.getBoat(user.getStaffId()));
            dbReportService.createReport(report);
            return report;
        }
        if (user.getUserToken() == null && !YCUserService.authorization(user)) {
            return null;
        }
        records = tryGetRecords(user, date);
        if (records != null) {
            report = new Report(user, date, records, boatService.getBoat(user.getStaffId()));
            dbReportService.createReport(report);
            return report;
        }
        return null;
    }

    private Report getReportFromDB(User user, LocalDate date) {
        return dbReportService.getReport(user.getStaffId(), date);
    }

    private List<Record> tryGetRecords(User user, LocalDate date) {
        List<Record> records;
        try {
            records = RecordService.getRecords(user, date);
        } catch (AuthenticationException _) {
            return null;
        }
        return records;
    }
}
