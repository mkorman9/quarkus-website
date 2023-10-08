package com.github.mkorman9.todo;

import jakarta.validation.constraints.NotBlank;

public record TodoItemAddPayload(
    @NotBlank String content
) {
}
