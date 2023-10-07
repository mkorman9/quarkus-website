package com.github.mkorman9.todo;

import java.time.Instant;

public record TodoItem(
    String content,
    Instant createdAt
) {
}
