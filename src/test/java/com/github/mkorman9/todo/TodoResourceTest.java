package com.github.mkorman9.todo;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
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
        var item1 = addItem(content1)
            .then()
            .statusCode(200)
            .extract().body().as(TodoItemAddResponse.class);
        var item2 = addItem(content2)
            .then()
            .statusCode(200)
            .extract().body().as(TodoItemAddResponse.class);

        // when
        markItem(item1.id())
            .then()
            .statusCode(200);

        // then
        var todoItems = getAllItems()
            .then()
            .statusCode(200)
            .extract().body().as(TodoItemsPage.class)
            .items();

        assertThat(todoItems.size()).isEqualTo(2);
        assertThat(todoItems.get(0).id()).isEqualTo(item2.id());
        assertThat(todoItems.get(0).done()).isFalse();
        assertThat(todoItems.get(0).content()).isEqualTo(content2);
        assertThat(todoItems.get(1).id()).isEqualTo(item1.id());
        assertThat(todoItems.get(1).done()).isTrue();
        assertThat(todoItems.get(1).content()).isEqualTo(content1);
    }

    @Test
    public void shouldReturnSinglePageOfItems() {
        // given
        var content1 = "AAA";
        var content2 = "BBB";
        var content3 = "CCC";
        var content4 = "DDD";
        addItem(content1)
            .then()
            .statusCode(200);
        var item2 = addItem(content2)
            .then()
            .statusCode(200)
            .extract().body().as(TodoItemAddResponse.class);
        var item3 = addItem(content3)
            .then()
            .statusCode(200)
            .extract().body().as(TodoItemAddResponse.class);
        var item4 = addItem(content4)
            .then()
            .statusCode(200)
            .extract().body().as(TodoItemAddResponse.class);

        // when
        var todoItems = getItemsPage(2, item4.id())
            .then()
            .statusCode(200)
            .extract().body().as(TodoItemsPage.class)
            .items();

        // then
        assertThat(todoItems.size()).isEqualTo(2);
        assertThat(todoItems.get(0).id()).isEqualTo(item3.id());
        assertThat(todoItems.get(0).content()).isEqualTo(content3);
        assertThat(todoItems.get(1).id()).isEqualTo(item2.id());
        assertThat(todoItems.get(1).content()).isEqualTo(content2);
    }

    @Test
    public void shouldUnmarkItem() {
        // given
        var item = addItem("AAA")
            .then()
            .statusCode(200)
            .extract().body().as(TodoItemAddResponse.class);

        markItem(item.id())
            .then()
            .statusCode(200);

        // when
        unmarkItem(item.id())
            .then()
            .statusCode(200);

        // then
        var todoItems = getAllItems()
            .then()
            .statusCode(200)
            .extract().body().as(TodoItemsPage.class)
            .items();

        assertThat(todoItems.size()).isEqualTo(1);
        assertThat(todoItems.get(0).done()).isFalse();
    }

    @Test
    public void shouldFailOnEmptyItemContent() {
        addItem("")
            .then()
            .statusCode(400);
    }

    @Test
    public void shouldFailOnTooLongItemContent() {
        addItem("A".repeat(256))
            .then()
            .statusCode(400);
    }

    @Test
    public void shouldFailOnMarkingNonExistingItem() {
        markItem(UUID.randomUUID())
            .then()
            .statusCode(400);
    }

    @Test
    public void shouldFailOnUnmarkingNotMarkedItem() {
        // given
        var item = addItem("AAA")
            .then()
            .statusCode(200)
            .extract().body().as(TodoItemAddResponse.class);

        // when then
        unmarkItem(item.id())
            .then()
            .statusCode(400);
    }

    @Test
    public void shouldFailOnMarkingAlreadyMarkedItem() {
        // given
        var item = addItem("AAA")
            .then()
            .statusCode(200)
            .extract().body().as(TodoItemAddResponse.class);

        markItem(item.id())
            .then()
            .statusCode(200);

        // when then
        markItem(item.id())
            .then()
            .statusCode(400);
    }

    private Response getAllItems() {
        return given()
            .when()
            .get("/api/todo");
    }

    private Response getItemsPage(int pageSize, UUID pageToken) {
        return given()
            .when()
            .queryParam("pageSize", pageSize)
            .queryParam("pageToken", pageToken)
            .get("/api/todo");
    }

    private Response addItem(String content) {
        return given()
            .when()
            .contentType(MediaType.APPLICATION_JSON)
            .body(new TodoItemAddPayload(content))
            .post("/api/todo");
    }

    private Response markItem(UUID id) {
        return given()
            .when()
            .post("/api/todo/mark/" + id);
    }

    private Response unmarkItem(UUID id) {
        return given()
            .when()
            .post("/api/todo/unmark/" + id);
    }
}
