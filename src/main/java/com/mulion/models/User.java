package com.mulion.models;

import com.mulion.data_base.listeners.UserListener;
import com.mulion.models.enums.UserRole;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.proxy.HibernateProxy;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners({UserListener.class})
public class User {
    @Id
    private Long id;

    @Column(name = "user_name")
    private String tgUserName;
    @Column(name = "chat_id")
    private Long chatId;
    @Column(name = "bot_last_message_id")
    private Integer botLastMessageId;
    private String name;
    @Column(name = "user_token")
    private String userToken;
    private UserRole role;
    @Column(name = "staff_id")
    private Long staffId;
    @Column(name = "boats_count")
    private Integer boatsCount;
    private String login;
    private String password;
    private int cash;
    @Embedded
    private ActionSteps actionStep;
    @ManyToMany
    @ToString.Exclude
    @JoinTable(
            name = "users_to_boats",
            joinColumns = {
                    @JoinColumn(name = "user_id", referencedColumnName = "id")
            },
            inverseJoinColumns = {
                    @JoinColumn(name = "boat_id", referencedColumnName = "id")
            }
    )
    private final Set<Boat> boats = new HashSet<>();
    @OneToMany(mappedBy = "user")
    private List<Report> reports = new ArrayList<>();

    public String getLoginAndPassword() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("login", login)
                .append("password", password)
                .toString();
    }

    public void addBoat(Boat boat) {
        if (boat == null) return;
        if (staffId == null) {
            staffId = boat.getId();
        }
        boats.add(boat);
        boatsCount = boats.size();
    }

    public void removeBoat(Boat boat) {
        if (boat == null) return;
        boats.remove(boat);
        if (boat.getId().equals(staffId)) {
            if (!boats.isEmpty()) {
                staffId = boats.iterator().next().getId();
            } else {
                staffId = null;
            }
        }
        boatsCount = boats.size();
    }

    public void addCash(int cash) {
        this.cash += cash;
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        User user = (User) o;
        return getId() != null && Objects.equals(getId(), user.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
