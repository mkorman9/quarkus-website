package com.github.mkorman9.todo;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TodoItemAddPayload(
    @NotBlank @Size(max = 255) String content
) {
}
