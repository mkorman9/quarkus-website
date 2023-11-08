package com.github.mkorman9.todo;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TodoService {
    private static final NoArgGenerator ID_GENERATOR = Generators.timeBasedEpochGenerator();

    @Inject
    DataSource dataSource;

    @Transactional
    public List<TodoItem> getItems() {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.createStatement()
        ) {
            var resultSet = statement.executeQuery("select * from todo_items order by created_at desc");
            var items = new ArrayList<TodoItem>();

            while (resultSet.next()) {
                var item = new TodoItem(
                    (UUID) resultSet.getObject("id"),
                    resultSet.getString("content"),
                    resultSet.getBoolean("done"),
                    resultSet.getTimestamp("created_at").toInstant()
                );
                items.add(item);
            }

            return items;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Transactional
    public UUID addItem(String content) {
        var id = ID_GENERATOR.generate();

        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(
                "insert into todo_items (id, content, done, created_at) values (?, ?, ?, ?)"
            )
        ) {
            statement.setObject(1, id);
            statement.setString(2, content);
            statement.setBoolean(3, false);
            statement.setTimestamp(4, Timestamp.from(Instant.now()));
            statement.execute();
        } catch (SQLException e) {
//            if (e instanceof PSQLException psqlException && psqlException.getServerErrorMessage() != null) {
//                if ("todo_items_pkey".equals(psqlException.getServerErrorMessage().getConstraint())) {
//                    // duplicate
//                }
//            }

            throw new RuntimeException(e);
        }

        return id;
    }

    @Transactional
    public boolean markDone(UUID id) {
        return findAndMark(id, true);
    }

    @Transactional
    public boolean unmarkDone(UUID id) {
        return findAndMark(id, false);
    }

    @Transactional
    public void deleteAll() {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.createStatement()
        ) {
            statement.execute("delete from todo_items");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean findAndMark(UUID id, boolean done) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(
                "update todo_items set done=? where id=?"
            )
        ) {
            statement.setBoolean(1, done);
            statement.setObject(2, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
