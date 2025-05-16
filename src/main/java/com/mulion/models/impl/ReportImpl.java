package com.mulion.models.impl;

import com.mulion.models.User;
import com.mulion.yclients.models.Record;
import com.mulion.models.Report;
import lombok.Data;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static com.mulion.constants.Config.reportDateFormatter;

@Data
public class ReportImpl implements Report {
    public static final String DELIMITER = "-------------";

    private User user;
    private LocalDate date;
    private List<Record> records;
    private String reportMessage;
    private double workHours;
    private int prepayment;
    private int acquiring;
    private int cash;

    public ReportImpl(User user, LocalDate date, List<Record> records) {
        this.user = user;
        this.date = date;
        this.records = records;
        records.sort(Comparator.comparing(Record::getDate)
                .thenComparing(Record::getStartTime));
        records.forEach(re -> {
            prepayment += re.getPrepayment();
            acquiring += re.getAcquiring();
            cash += re.getCash();
        });
        user.addCash(cash);
        workHours = records
                .stream()
                .map(Record::getLength)
                .reduce(Integer::sum)
                .orElse(0) / 3600.;
        reportMessage = getReportTextPrivate();
    }

    private String getReportTextPrivate() {
        StringBuilder result = new StringBuilder();
        result.append(date.format(reportDateFormatter))
                .append(String.format(" (%s)%n%s%n",
                        date.getDayOfWeek().getDisplayName(TextStyle.SHORT, new Locale("ru")),
                        DELIMITER));
        records.forEach(re -> result.append(re).append('\n'));
        result.append(String.format("""
                %s
                пред : %d
                экв  : %d
                борт : %d
                """, DELIMITER, prepayment, acquiring, cash));
        result.append(DELIMITER)
                .append("\nчасов отработано : ")
                .append(workHours);
        return result.toString();
    }

    private String formatWithUnderscores(int number) {
        return String.format("%,d", number).replace(',', '_');
    }
}
