package com.mulion.data_base;

import com.mulion.models.Boat;
import com.mulion.models.User;
import com.mulion.services.ConfigService;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

import java.util.Properties;

public class SessionProvider {
    public SessionFactory getSessionFactory() {
        Properties hibernateProperties = new Properties();

        hibernateProperties.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        hibernateProperties.setProperty("hibernate.connection.url", ConfigService.getProperty("db.connection_url"));
        hibernateProperties.setProperty("hibernate.connection.username", ConfigService.getProperty("db.user_name"));
        hibernateProperties.setProperty("hibernate.connection.password", ConfigService.getProperty("db.password"));
        hibernateProperties.setProperty("hibernate.hbm2ddl.auto", "update");
        hibernateProperties.setProperty("hibernate.show_sql", "true");

        return new Configuration()
                .addProperties(hibernateProperties)
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Boat.class)
                .buildSessionFactory();
    }
}
