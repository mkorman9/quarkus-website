package com.github.mkorman9.todo;

import com.github.f4b6a3.uuid.UuidCreator;
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
    @Inject
    DataSource dataSource;

    public List<TodoItem> getItems() {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()
        ) {
            var resultSet = statement.executeQuery("SELECT * FROM todo_items ORDER BY created_at DESC");
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
        var id = UuidCreator.getTimeOrderedEpoch();

        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(
                 "INSERT INTO todo_items (id, content, done, created_at) VALUES (?, ?, ?, ?)"
             )
        ) {
            statement.setObject(1, id);
            statement.setString(2, content);
            statement.setBoolean(3, false);
            statement.setTimestamp(4, Timestamp.from(Instant.now()));
            statement.execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return id;
    }

    public void markDone(UUID id) {
        findAndMark(id, true);
    }

    public void unmarkDone(UUID id) {
        findAndMark(id, false);
    }

    public void deleteAll() {
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()
        ) {
            statement.execute("DELETE FROM todo_items");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void findAndMark(UUID id, boolean done) {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(
                 "UPDATE todo_items SET done=? WHERE id=?"
             )
        ) {
            statement.setBoolean(1, done);
            statement.setObject(2, id);
            var updated = statement.executeUpdate();

            if (updated == 0) {
                throw new TodoItemNotFoundException();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
