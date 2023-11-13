package com.github.mkorman9.todo;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Produces;
import org.jdbi.v3.core.Jdbi;

import javax.sql.DataSource;

@ApplicationScoped
public class JdbiProvider {
    @Singleton
    @Produces
    public Jdbi jdbi(DataSource dataSource) {
        return Jdbi.create(dataSource);
    }
}
