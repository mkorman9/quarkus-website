package com.github.mkorman9.todo;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

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
    public void shouldAddAndMarkAndRetrieveItemsInOrder() {
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
            .when().put("/api/todo/mark/" + id1)
            .then()
            .statusCode(204);

        // then
        var todoItems = given()
            .when().get("/api/todo")
            .then()
            .statusCode(200)
            .extract().body().jsonPath()
            .getList(".", TodoItem.class);

        assertThat(todoItems.size()).isEqualTo(2);
        assertThat(todoItems.get(0).id()).isEqualTo(id2);
        assertThat(todoItems.get(0).done()).isFalse();
        assertThat(todoItems.get(0).content()).isEqualTo(content2);
        assertThat(todoItems.get(1).id()).isEqualTo(id1);
        assertThat(todoItems.get(1).done()).isTrue();
        assertThat(todoItems.get(1).content()).isEqualTo(content1);
    }

    @Test
    public void shouldUnmarkItem() {
        // given
        var id = given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new TodoItemAddPayload("AAA"))
            .post("/api/todo")
            .then()
            .statusCode(200)
            .extract().body().as(UUID.class);

        given()
            .when().put("/api/todo/mark/" + id)
            .then()
            .statusCode(204);

        // when
        given()
            .when().put("/api/todo/unmark/" + id)
            .then()
            .statusCode(204);

        // then
        var todoItems = given()
            .when().get("/api/todo")
            .then()
            .statusCode(200)
            .extract().body().jsonPath()
            .getList(".", TodoItem.class);

        assertThat(todoItems.size()).isEqualTo(1);
        assertThat(todoItems.get(0).done()).isFalse();
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
    public void shouldFailOnTooLongItemContent() {
        given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new TodoItemAddPayload("A".repeat(256)))
            .post("/api/todo")
            .then()
            .statusCode(400);
    }

    @Test
    public void shouldFailOnMarkingNonExistingItem() {
        given()
            .when().put("/api/todo/mark/" + UUID.randomUUID())
            .then()
            .statusCode(404);
    }
}
