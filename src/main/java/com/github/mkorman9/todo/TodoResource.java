package com.github.mkorman9.todo;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;
import java.util.List;

@Path("/api/todo")
public class TodoResource {
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<TodoItem> getTodoItems() {
        return List.of(
            new TodoItem("Buy flowers", Instant.now()),
            new TodoItem("Boil pasta", Instant.now()),
            new TodoItem("Buy tickets", Instant.now())
        );
    }
}
