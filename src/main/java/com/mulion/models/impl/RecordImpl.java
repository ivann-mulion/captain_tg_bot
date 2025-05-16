package com.mulion.models.impl;

import com.mulion.models.Record;
import com.mulion.models.responces.DataRecord;
import com.mulion.models.responces.PaymentTransactionRecord;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mulion.constants.Config.yclientDateFormatter;

@Data
public class RecordImpl implements Record {
    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int length;
    private String paymentInfo;
    private int prepayment;
    private int acquiring;
    private int cash;

    public RecordImpl(DataRecord dataRecord, List<PaymentTransactionRecord> transactions) {
        LocalDateTime dateTime = LocalDateTime.parse(dataRecord.getDate(), yclientDateFormatter);
        date = dateTime.toLocalDate();
        startTime = dateTime.toLocalTime();
        length = dataRecord.getLength();
        endTime = startTime.plusSeconds(length);
        paymentInfo = createPaymentInfo(transactions);
    }

    @Override
    public String toString() {
        return String.format("%s - %s, %s", startTime, endTime, paymentInfo);
    }

    @Override
    public boolean isDone() {
        return false;
    }

    private String createPaymentInfo(List<PaymentTransactionRecord> transactionRecords) {
        if (transactionRecords == null) return "";
        if (transactionRecords.isEmpty()) return "бюджет";
        Map<String, Integer> paymants = new HashMap<>();
        String pre = "пред : ";
        String boat = "борт : ";
        String aq = "экв  : ";
        if (!transactionRecords.getFirst().getAccount().isCash() && transactionRecords.size() != 1) {
            paymants.put(pre, transactionRecords.getFirst().getAmount());
            prepayment += transactionRecords.getFirst().getAmount();
            transactionRecords.removeFirst();
        }
        for (PaymentTransactionRecord transaction : transactionRecords) {
            if (transaction.getAccount().isCash()) {
                paymants.merge(boat, transaction.getAmount(), Integer::sum);
                cash += transaction.getAmount();
            } else {
                paymants.merge(aq, transaction.getAmount(), Integer::sum);
                acquiring += transaction.getAmount();

            }
        }
        StringBuilder result = new StringBuilder();
        Integer amount;
        if ((amount = paymants.get(pre)) != null) {
            result.append(pre).append(amount).append(' ');
        }
        if ((amount = paymants.get(aq)) != null) {
            result.append(aq).append(amount).append(' ');
        }
        if ((amount = paymants.get(boat)) != null) {
            result.append(boat).append(amount).append(' ');
        }
        return result.toString();
    }
}
