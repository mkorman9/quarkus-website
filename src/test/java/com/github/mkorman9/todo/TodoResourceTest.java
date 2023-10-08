package com.github.mkorman9.todo;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class TodoResourceTest {
    @Inject
    TodoService todoService;

    @AfterEach
    public void tearDown() {
        todoService.deleteAll();
    }

    @Test
    public void shouldAddMarkAndRetrieveItems() {
        // given
        var content1 = "AAA";
        var content2 = "BBB";
        var id1 = given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new TodoItemAddPayload(content1))
            .post("/api/todo")
            .then()
            .statusCode(200)
            .extract().body().as(UUID.class);
        var id2 = given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new TodoItemAddPayload(content2))
            .post("/api/todo")
            .then()
            .statusCode(200)
            .extract().body().as(UUID.class);

        // when
        given()
            .when().put("/api/todo/mark/" + id1.toString())
            .then()
            .statusCode(204);

        // then
        var todoItems = given()
            .when().get("/api/todo")
            .then()
            .statusCode(200)
            .extract().body().jsonPath()
            .getList(".", TodoItem.class)
            .stream().collect(Collectors.toMap(TodoItem::id, Function.identity()));

        assertThat(todoItems.size()).isEqualTo(2);
        assertThat(todoItems.get(id1).done()).isTrue();
        assertThat(todoItems.get(id1).content()).isEqualTo(content1);
        assertThat(todoItems.get(id2).done()).isFalse();
        assertThat(todoItems.get(id2).content()).isEqualTo(content2);
    }

    @Test
    public void shouldFailOnEmptyItemContent() {
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new TodoItemAddPayload(""))
            .post("/api/todo")
            .then()
            .statusCode(400);
    }

    @Test
    public void shouldFailWhenMarkingNonExistingItem() {
        given()
            .when().put("/api/todo/mark/" + UUID.randomUUID())
            .then()
            .statusCode(404);
    }
}
