package com.github.mkorman9.todo;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@ApplicationScoped
public class TodoService {
    private final Map<UUID, TodoItem> items = new ConcurrentHashMap<>();

    public List<TodoItem> getItems() {
        return items.values()
            .stream()
            .sorted((item1, item2) -> {
                if (item1.createdAt().equals(item2.createdAt())) {
                    return 0;
                }

                return item1.createdAt().isBefore(item2.createdAt()) ? 1 : -1;
            })
            .toList();
    }

    public UUID addItem(String content) {
        var id = UuidCreator.getTimeOrderedEpoch();
        var now = Instant.now();

        var existingItemId = items.putIfAbsent(id,
            new TodoItem(
                id,
                content,
                false,
                now
            )
        );
        if (existingItemId != null) {
            throw new IllegalStateException("Item ID duplication");
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
        items.clear();
    }

    private void findAndMark(UUID id, boolean done) {
        items.compute(id, (idFound, item) -> {
            if (item == null) {
                throw new TodoItemNotFoundException();
            }

            return new TodoItem(
                item.id(),
                item.content(),
                done,
                item.createdAt()
            );
        });
    }
}
