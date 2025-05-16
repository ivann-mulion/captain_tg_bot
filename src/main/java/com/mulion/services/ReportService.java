package com.mulion.services;

import com.mulion.constants.ErrorsMessages;
import com.mulion.models.Record;
import com.mulion.models.Report;
import com.mulion.models.User;
import com.mulion.models.impl.ReportImpl;

import javax.naming.AuthenticationException;
import javax.security.auth.login.FailedLoginException;
import java.time.LocalDate;
import java.util.List;

public class ReportService {
    public static Report getReport(User user, LocalDate date) {
        List<Record> records = tryGetRecords(user, date);
        if (records != null) {
            return new ReportImpl(user, date, records);
        }

        if (!authorize(user)) {
            return null;
        }

        records = tryGetRecords(user, date);
        if (records != null) {
            return new ReportImpl(user, date, records);
        }
        return null;
    }

    public static String getReportMessage(User user, LocalDate date) {
        Report report = getReport(user, date);
        if (report == null) {
            return ErrorsMessages.AUTH_ERROR;
        }
        return report.getReportMessage();
    }

    private static List<Record> tryGetRecords(User user, LocalDate date) {
        List<Record> records;
        try {
            records = RecordService.getRecords(user, date);
        } catch (AuthenticationException _) {
            return null;
        }
        return records;
    }

    private static boolean authorize(User user) {
        try {
            UserService.authorization(user);
        } catch (FailedLoginException _) {
            return false;
        }
        return true;
    }

    private ReportService() {
    }
}
