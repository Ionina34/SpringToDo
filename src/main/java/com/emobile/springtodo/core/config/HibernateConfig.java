package com.emobile.springtodo.core.config;

import com.emobile.springtodo.core.entity.db.Task;
import com.emobile.springtodo.core.entity.db.User;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.orm.hibernate5.HibernateTransactionManager;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class HibernateConfig {

    @Value("${spring.datasource.url}")
    public String dbUrl;

    @Value("${spring.datasource.username}")
    public String dbUsername;

    @Value("${spring.datasource.password}")
    public String dbPassword;

    @Value("${spring.datasource.driver-class-name}")
    public String dbDriver;

    @Value("${hibernate.dialect}")
    public String hibernateDialect;

    @Value("${hibernate.show_sql}")
    public String showSql;

    @Value("${hibernate.format_sql}")
    public String formatSql;

    @Value("${hibernate.hbm2ddl.auto}")
    public String hdm2ddlAuto;

    @Bean
    public DataSource dataSource() {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(dbUrl);
        dataSource.setUsername(dbUsername);
        dataSource.setPassword(dbPassword);
        dataSource.setDriverClassName(dbDriver);
        dataSource.setMaximumPoolSize(10);
        dataSource.setConnectionTimeout(30000);
        return dataSource;
    }

    @Bean
    public SessionFactory sessionFactory(DataSource dataSource) {
        org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();
        configuration.setProperty("hibernate.dialect", hibernateDialect);
        configuration.setProperty("hibernate.show_sql", showSql);
        configuration.setProperty("hibernate.format_sql", formatSql);
        configuration.setProperty("hibernate.hbm2ddl.auto", hdm2ddlAuto);

        configuration.setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
        configuration.setProperty("hibernate.hikari.jdbcUrl", dbUrl);
        configuration.setProperty("hibernate.hikari.username", dbUsername);
        configuration.setProperty("hibernate.hikari.password", dbPassword);
        configuration.setProperty("hibernate.hikari.maximumPoolSize", "10");
        configuration.setProperty("hibernate.hikari.connectionTimeout", "30000");

        configuration.addAnnotatedClass(Task.class);
        configuration.addAnnotatedClass(User.class);

        return configuration.buildSessionFactory();
    }
}
