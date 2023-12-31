package com.github.mkorman9.todo;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestPath;
import org.jboss.resteasy.reactive.RestResponse;

import java.util.Optional;
import java.util.UUID;

@Path("/api/todo")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({})
@RunOnVirtualThread
public class TodoResource {
    private static final int MIN_PAGE_SIZE = 1;
    private static final int MAX_PAGE_SIZE = 100;

    @Inject
    TodoService todoService;

    @GET
    public TodoItemsPage getTodoItems(
        @QueryParam("pageSize") @DefaultValue("10") int pageSize,
        @QueryParam("pageToken") Optional<UUID> pageToken
    ) {
        pageSize = Math.clamp(pageSize, MIN_PAGE_SIZE, MAX_PAGE_SIZE);
        return pageToken.isEmpty() ?
            todoService.getFirstItemsPage(pageSize) :
            todoService.getNextItemsPage(pageSize, pageToken.get());
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public TodoItemAddResponse addTodoItem(@NotNull @Valid TodoItemAddPayload payload) {
        var id = todoService.addItem(payload.content());
        return new TodoItemAddResponse(id);
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
