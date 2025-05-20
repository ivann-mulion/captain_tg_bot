package com.mulion.models.impl;

import com.mulion.models.Record;
import com.mulion.yclients.models.responses.DataRecord;
import com.mulion.yclients.models.responses.PaymentTransactionRecord;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static com.mulion.constants.Config.yclientDateFormatter;

@Data
public class RecordImpl implements Record {
    public static final String PRE = "пред : ";
    public static final String BOAT = "борт : ";
    public static final String AQ = "экв  : ";
    public static final String FREE = "бюджет";
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int length;
    private int prepayment;
    private int acquiring;
    private int cash;

    public RecordImpl(DataRecord dataRecord, List<PaymentTransactionRecord> transactions) {
        LocalDateTime dateTime = LocalDateTime.parse(dataRecord.getDate(), yclientDateFormatter);
        date = dateTime.toLocalDate();
        startTime = dateTime.toLocalTime();
        length = dataRecord.getLength();
        endTime = startTime.plusSeconds(length);
        createPaymentInfo(transactions);
    }

    @Override
    public String toString() {
        return String.format("%s - %s, %s", startTime, endTime, paymentInfo());
    }

    private String paymentInfo() {
        StringBuilder result = new StringBuilder();
        if (prepayment > 0) {
            result.append(PRE).append(prepayment).append(' ');
        }
        if (acquiring > 0) {
            result.append(AQ).append(acquiring).append(' ');
        }
        if (cash > 0) {
            result.append(BOAT).append(cash).append(' ');
        }
        if (result.isEmpty()) {
            result.append(FREE);
        }
        return result.toString();
    }

    private void createPaymentInfo(List<PaymentTransactionRecord> transactionRecords) {
        if (transactionRecords == null || transactionRecords.isEmpty()) return;
        if (!transactionRecords.getFirst().getAccount().isCash() && transactionRecords.size() != 1) {
            prepayment += transactionRecords.getFirst().getAmount();
            transactionRecords.removeFirst();
        }
        for (PaymentTransactionRecord transaction : transactionRecords) {
            if (transaction.getAccount().isCash()) {
                cash += transaction.getAmount();
            } else {
                acquiring += transaction.getAmount();
            }
        }
    }
}
