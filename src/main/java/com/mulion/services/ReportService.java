package com.mulion.services;

import com.mulion.constants.ErrorsMessages;
import com.mulion.data_base.services.DBBoatService;
import com.mulion.models.User;
import com.mulion.models.Record;
import com.mulion.models.Report;
import com.mulion.models.impl.ReportImpl;
import com.mulion.yclients.services.RecordService;
import com.mulion.yclients.services.YCUserService;

import javax.naming.AuthenticationException;
import java.time.LocalDate;
import java.util.List;

public class ReportService {
    public static Report getReport(User user, LocalDate date, DBBoatService boatService) {
        List<Record> records = tryGetRecords(user, date);
        if (records != null) {
            return new ReportImpl(user, date, records, boatService.getBoat(user.getStaffId()));
        }

        if (!YCUserService.authorization(user)) {
            return null;
        }

        records = tryGetRecords(user, date);
        if (records != null) {
            return new ReportImpl(user, date, records, boatService.getBoat(user.getStaffId()));
        }
        return null;
    }

    public static String getReportMessage(User user, LocalDate date, DBBoatService boatService) {
        Report report = getReport(user, date, boatService);
        if (report == null) {
            return ErrorsMessages.AUTH_ERROR;
        }
        return report.getReportMessage();
    }

    private static List<Record> tryGetRecords(User user, LocalDate date) {
        List<Record> records;
        try {
            records = RecordService.getRecords(user, date);
        } catch (AuthenticationException e) {
            return null;
        }
        return records;
    }

    private ReportService() {
    }
}
