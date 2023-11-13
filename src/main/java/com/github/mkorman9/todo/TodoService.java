package com.github.mkorman9.todo;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

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

    public List<TodoItem> getItems() {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.createStatement()
        ) {
            var resultSet = statement.executeQuery(
                "select id, content, done, created_at from todo_items order by created_at desc"
            );
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

    public boolean markDone(UUID id) {
        return findAndMark(id, true);
    }

    public boolean unmarkDone(UUID id) {
        return findAndMark(id, false);
    }

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
        try (var connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try (
                var updateItemStatement = connection.prepareStatement(
                    "update todo_items set done=? where id=?"
                );
                var insertActionStatement = connection.prepareStatement(
                    "insert into todo_items_mark_actions (id, item_id, target_value, created_at) values (?, ?, ?, ?)"
                )
            ) {
                updateItemStatement.setBoolean(1, done);
                updateItemStatement.setObject(2, id);
                if (updateItemStatement.executeUpdate() == 0) {
                    return false;
                }

                insertActionStatement.setObject(1, ID_GENERATOR.generate());
                insertActionStatement.setObject(2, id);
                insertActionStatement.setBoolean(3, done);
                insertActionStatement.setTimestamp(4, Timestamp.from(Instant.now()));
                insertActionStatement.execute();

                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
