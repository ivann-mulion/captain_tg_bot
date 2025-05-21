package com.mulion.models;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.mulion.constants.Config.reportDateFormatter;

@Entity
@Table(name = "reports")
@Getter
@Setter
@RequiredArgsConstructor
public class Report {
    public static final String DELIMITER = "-------------";

    @EmbeddedId
    private ReportId id;
    @ManyToOne
    @JoinColumn(name = "captain_id", nullable = false)
    private User user;
    @OneToMany
    private List<Record> records;
    @Column(columnDefinition = "TEXT")
    private String reportMessage;
    private double workHours;
    private int prepayment;
    private int acquiring;
    private int cash;
    @ManyToOne
    @MapsId("boatId")
    @JoinColumn(name = "boat_id", nullable = false)
    private Boat boat;

    public Report(User user, LocalDate date, List<Record> records, Boat boat) {
        id = new ReportId(boat.getId(), date);
        this.user = user;
        this.records = records;
        this.boat = boat;
        records.sort(Comparator.comparing(Record::getRecordDate)
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

    public LocalDate getDate() {
        return id != null ? id.getDate() : null;
    }

    private String getReportTextPrivate() {
        StringBuilder result = new StringBuilder();
        result.append(getDate().format(reportDateFormatter))
                .append(String.format(" (%s) - %s%n%s%n",
                        getDate().getDayOfWeek().getDisplayName(TextStyle.SHORT, Locale.of("ru")),
                        boat.getName(),
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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Report report = (Report) o;
        return getId() != null && Objects.equals(getId(), report.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
