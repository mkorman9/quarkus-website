package com.github.mkorman9.todo;

import java.time.Instant;
import java.util.UUID;

public record TodoItem(
    UUID id,
    String content,
    boolean done,
    Instant createdAt
) {
    public TodoItem withDone(boolean done) {
        return new TodoItem(
            id,
            content,
            done,
            createdAt
        );
    }
}
