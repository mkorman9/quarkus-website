package com.github.mkorman9.todo;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class TodoService {
    private static final NoArgGenerator ID_GENERATOR = Generators.timeBasedEpochGenerator();

    @Inject
    Jdbi jdbi;

    public List<TodoItem> getItems() {
        return jdbi.withHandle(handle -> {
            return handle.createQuery(
                    "select id, content, done, created_at from todo_items order by created_at desc"
                )
                .map((rs, ctx) -> new TodoItem(
                    (UUID) rs.getObject("id"),
                    rs.getString("content"),
                    rs.getBoolean("done"),
                    rs.getTimestamp("created_at").toInstant()
                ))
                .list();
        });
    }

    public UUID addItem(String content) {
        var id = ID_GENERATOR.generate();

        try {
            jdbi.withHandle(handle -> {
                return handle.createUpdate(
                    "insert into todo_items (id, content, done, created_at) " +
                        "values (:id, :content, :done, :createdAt)"
                    )
                    .bind("id", id)
                    .bind("content", content)
                    .bind("done", false)
                    .bind("createdAt", Timestamp.from(Instant.now()))
                    .execute();
            });
        } catch (JdbiException e) {
//            if (e.getCause() instanceof PSQLException psqlException && psqlException.getServerErrorMessage() != null) {
//                if ("todo_items_pkey".equals(psqlException.getServerErrorMessage().getConstraint())) {
//                    // duplicate
//                }
//            }

            throw e;
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
        jdbi.withHandle(handle -> {
            return handle.createCall(
                    "delete from todo_items"
                )
                .invoke();
        });
    }

    private boolean findAndMark(UUID id, boolean done) {
        return jdbi.inTransaction(transaction -> {
            var updated = transaction.createUpdate("update todo_items set done=:done where id=:id")
                .bind("id", id)
                .bind("done", done)
                .execute();

            if (updated == 0) {
                return false;
            }

            transaction.createUpdate(
                "insert into todo_items_mark_actions (id, item_id, target_value, created_at) " +
                    "values (:id, :itemId, :value, :createdAt)"
                )
                .bind("id", ID_GENERATOR.generate())
                .bind("itemId", id)
                .bind("value", done)
                .bind("createdAt", Timestamp.from(Instant.now()))
                .execute();

            return true;
        });
    }
}
