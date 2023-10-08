package com.github.mkorman9.todo;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder(toBuilder = true)
public record TodoItem(
    UUID id,
    String content,
    boolean done,
    Instant createdAt
) {
}
