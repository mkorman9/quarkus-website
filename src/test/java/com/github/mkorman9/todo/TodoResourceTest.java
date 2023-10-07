package com.github.mkorman9.todo;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@QuarkusTest
class TodoResourceTest {
    @Test
    public void testGetTodoItems() {
        var todoItems = given()
            .when().get("/api/todo")
            .then()
            .statusCode(200)
            .extract().body().jsonPath()
            .getList(".", TodoItem.class);

        assertThat(todoItems.size()).isEqualTo(3);
    }
}
