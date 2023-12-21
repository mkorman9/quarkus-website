package com.github.mkorman9.todo;

import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.NoArgGenerator;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.JdbiException;
import org.jdbi.v3.core.statement.Query;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class TodoService {
    private static final NoArgGenerator ID_GENERATOR = Generators.timeBasedEpochGenerator();

    @Inject
    Jdbi jdbi;

    public TodoItemsPage getItemsPage(int pageSize, @Nullable UUID pageToken) {
        var items = jdbi.withHandle(handle -> {
            var q = (pageToken == null)
                ? createItemsPageQuery(handle, pageSize)
                : createItemsPageQueryWithToken(handle, pageSize, pageToken);

            return q.map((rs, ctx) -> TodoItem.builder()
                    .id((UUID) rs.getObject("id"))
                    .content(rs.getString("content"))
                    .done(rs.getBoolean("done"))
                    .createdAt(rs.getTimestamp("created_at").toInstant())
                    .build()
                )
                .list();
        });

        return TodoItemsPage.builder()
            .items(items)
            .pageSize(pageSize)
            .nextPageToken(
                Optional.ofNullable(items.isEmpty() ? null : items.getLast().id())
            )
            .build();
    }

    private Query createItemsPageQuery(Handle handle, int pageSize) {
        return handle.createQuery(
                "select id, content, done, created_at from todo_items order by id desc limit :limit"
            )
            .bind("limit", pageSize);
    }

    private Query createItemsPageQueryWithToken(Handle handle, int pageSize, UUID pageToken) {
        return handle.createQuery(
                "select id, content, done, created_at from todo_items where :token > id " +
                    "order by id desc limit :limit"
            )
            .bind("token", pageToken)
            .bind("limit", pageSize);
    }

    public UUID addItem(String content) {
        var id = ID_GENERATOR.generate();
        var now = Instant.now();

        try {
            jdbi.useTransaction(transaction -> {
                transaction.createUpdate(
                        "insert into todo_items (id, content, done, created_at) " +
                            "values (:id, :content, :done, :createdAt)"
                    )
                    .bind("id", id)
                    .bind("content", content)
                    .bind("done", false)
                    .bind("createdAt", Timestamp.from(now))
                    .execute();

                transaction.createUpdate(
                        "insert into todo_items_actions (id, item_id, action_type, target_value, created_at) " +
                            "values (:id, :itemId, :actionType, :value, :createdAt)"
                    )
                    .bind("id", ID_GENERATOR.generate())
                    .bind("itemId", id)
                    .bind("actionType", "create")
                    .bind("value", false)
                    .bind("createdAt", Timestamp.from(now))
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

    private boolean findAndMark(UUID id, boolean done) {
        return jdbi.inTransaction(transaction -> {
            var updated = transaction.createUpdate(
                    "update todo_items set done=:value where id=:id and done != :value"
                )
                .bind("id", id)
                .bind("value", done)
                .execute();

            if (updated == 0) {
                return false;
            }

            transaction.createUpdate(
                    "insert into todo_items_actions (id, item_id, action_type, target_value, created_at) " +
                        "values (:id, :itemId, :actionType, :value, :createdAt)"
                )
                .bind("id", ID_GENERATOR.generate())
                .bind("itemId", id)
                .bind("actionType", "update")
                .bind("value", done)
                .bind("createdAt", Timestamp.from(Instant.now()))
                .execute();

            return true;
        });
    }

    public void deleteAll() {
        jdbi.withHandle(handle ->
            handle.createUpdate("delete from todo_items")
                .execute()
        );
    }
}
