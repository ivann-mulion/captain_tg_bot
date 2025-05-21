package com.mulion.models;

import com.mulion.yclients.models.responses.DataRecord;
import com.mulion.yclients.models.responses.PaymentTransactionRecord;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Objects;

import static com.mulion.constants.Config.yclientDateFormatter;

@Entity
@Table(name = "records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Record {
    public static final String PRE = "пред : ";
    public static final String BOAT = "борт : ";
    public static final String AQ = "экв  : ";
    public static final String FREE = "бюджет";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDate recordDate;
    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;
    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;
    private int length;
    private int prepayment;
    private int acquiring;
    private int cash;
    @ManyToOne
    @JoinColumns({
            @JoinColumn(name = "boat_id", referencedColumnName = "boat_id"),
            @JoinColumn(name = "date", referencedColumnName = "date")
    })
    private Report report;


    public Record(DataRecord dataRecord, List<PaymentTransactionRecord> transactions) {
        LocalDateTime dateTime = LocalDateTime.parse(dataRecord.getDate(), yclientDateFormatter);
        recordDate = dateTime.toLocalDate();
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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Record record = (Record) o;
        return getId() != null && Objects.equals(getId(), record.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
