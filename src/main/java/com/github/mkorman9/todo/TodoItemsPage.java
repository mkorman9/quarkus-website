package com.github.mkorman9.todo;

import java.util.List;
import java.util.UUID;

public record TodoItemsPage(
    List<TodoItem> items,
    UUID nextPageToken
) {
}
