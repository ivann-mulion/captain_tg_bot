package com.mulion.models;

import com.mulion.enums.RegistrationStatus;
import com.mulion.services.ConfigService;
import com.mulion.yclients_models.services.YCUserService;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

@Getter
@Setter
@ToString
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
public class User {
    @Id
    private Long id;

    @Column(name = "user_name")
    private String tgUserName;
    private String name;
    private String userToken;
    private long staffId;
    private String login;
    private String password;
    private int cash;
    private RegistrationStatus registrationStatus;

    public User(Long id, String tgUserName, String name, String userToken, long staffId, String login, String password, int cash, RegistrationStatus registrationStatus) {
        this.id = id;
        this.tgUserName = tgUserName;
        this.name = name;
        this.staffId = staffId;
        this.login = login;
        this.password = password;
        this.cash = cash;
        this.userToken = userToken;
        this.registrationStatus = registrationStatus;
        if (userToken == null) {
            YCUserService.authorization(this);
        }
    }

    public String getLoginAndPassword() {
        return new ToStringBuilder(this, ToStringStyle.JSON_STYLE)
                .append("login", login)
                .append("password", password)
                .toString();
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
