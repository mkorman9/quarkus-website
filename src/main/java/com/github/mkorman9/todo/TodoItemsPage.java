package com.github.mkorman9.todo;

import lombok.Builder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Builder
public record TodoItemsPage(
    List<TodoItem> items,
    int pageSize,
    Optional<UUID> nextPageToken
) {
}
