package com.github.mkorman9.todo;

import com.github.f4b6a3.uuid.UuidCreator;
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
        var id = UuidCreator.getTimeOrderedEpoch();

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
            throw new RuntimeException(e);
        }

        return id;
    }

    @Transactional
    public void markDone(UUID id) {
        findAndMark(id, true);
    }

    @Transactional
    public void unmarkDone(UUID id) {
        findAndMark(id, false);
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

    private void findAndMark(UUID id, boolean done) {
        try (
            var connection = dataSource.getConnection();
            var statement = connection.prepareStatement(
                "update todo_items set done=? where id=?"
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
