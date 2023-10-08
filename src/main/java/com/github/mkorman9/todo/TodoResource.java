package com.github.mkorman9.todo;

import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestPath;

import java.util.List;
import java.util.UUID;

@Path("/api/todo")
@Produces(MediaType.APPLICATION_JSON)
public class TodoResource {
    @Inject
    TodoService todoService;

    @GET
    public List<TodoItem> getTodoItems() {
        return todoService.getItems();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public UUID addTodoItem(@NotNull @Valid TodoItemAddPayload payload) {
        return todoService.addItem(payload.content());
    }

    @PUT
    @Path("mark/{id}")
    public void markItemDone(@RestPath UUID id) {
        try {
            todoService.markDone(id);
        } catch (TodoItemNotFoundException e) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }
}
