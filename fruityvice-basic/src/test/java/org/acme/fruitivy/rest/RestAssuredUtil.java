package org.acme.fruitivy.rest;

import io.restassured.response.ValidatableResponse;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;

public class RestAssuredUtil {

    public static ValidatableResponse post(Object body) {
        return post("", body);
    }

    public static ValidatableResponse post(String path, Object body) {
        return given().body(body).contentType(JSON)
                .when().post(path)
                .then();
    }

    public static ValidatableResponse patch(String path) {
        return given().contentType(JSON)
                .when().patch(path)
                .then();
    }

    public static <T> T get(Class<T> clazz) {
        return get("", clazz);
    }

    public static <T> T get(String path, Class<T> clazz) {
        return get(path).extract().as(clazz);
    }

    public static ValidatableResponse get(String path) {
        return given().contentType(JSON)
                .when().get(path)
                .then();
    }
}
