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
        List<Record> records;
        try {
            records = RecordService.getRecords(user, date);
        } catch (AuthenticationException _) {
            try {
                UserService.authorization(user);
            } catch (FailedLoginException _) {
                return null;
            }
            try {
                records = RecordService.getRecords(user, date);
            } catch (AuthenticationException _) {
                return null;
            }
        }
        return new ReportImpl(user, date, records);
    }

    public static String getReportMessage(User user, LocalDate date) {
        Report report = getReport(user, date);
        if (report == null) {
            return ErrorsMessages.AUTH_ERROR;
        }
        return report.getReportMessage();
    }

    private ReportService() {
    }
}
