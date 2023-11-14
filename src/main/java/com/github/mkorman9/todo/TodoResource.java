package com.github.mkorman9.todo;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.List;
import java.util.UUID;

@Path("/api/todo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({})
@RunOnVirtualThread
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

    @POST
    @Path("/mark/{id}")
    public RestResponse<Void> markItemDone(@RestPath UUID id) {
        return todoService.markDone(id) ?
            RestResponse.ok() :
            RestResponse.status(Response.Status.BAD_REQUEST);
    }

    @POST
    @Path("/unmark/{id}")
    public RestResponse<Void> unmarkItemDone(@RestPath UUID id) {
        return todoService.unmarkDone(id) ?
            RestResponse.ok() :
            RestResponse.status(Response.Status.BAD_REQUEST);
    }
}
