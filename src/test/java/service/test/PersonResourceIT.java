package service.test;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import service.Person;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.get;
import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(Arquillian.class)
public class PersonResourceIT {

    protected static URL url;

    @BeforeClass
    public static void verifyAndSetup() throws MalformedURLException {
        url = new URL("http://localhost:8080/service");

        await()
                .atMost(1, TimeUnit.MINUTES)
                .until(() -> {
                    try {
                        return get(url + "/person").statusCode() == 200;
                    } catch (Exception e) {
                        return false;
                    }
                });
        RestAssured.baseURI = url + "/person";

        // This will invoke the clearAll endpoint, clearing all entities without id of 1
        Response response = given().when().delete();

        System.out.println("Clear all attempt returned: " + response.statusCode());
    }

    @Test
    @InSequence(1)
    public void testGetPerson() {
        Response response =
                given()
                        .pathParam("person_id", 1)
                        .when()
                        .get("/{person_id}")
                        .then()
                        .statusCode(200)
                        .extract().response();

        String jsonAsString = response.asString();

        Person person = JsonPath.from(jsonAsString).getObject("", Person.class);

        assertThat(person.getId()).isEqualTo(1);
        assertThat(person.getName()).isEqualTo("JR");
        assertThat(person.getAge()).isEqualTo(20);
        assertThat(person.getEmail()).isEqualTo("jr_diehl@baylor.edu");
    }

    @Test
    @InSequence(2)
    public void testPostPerson() {
        Person person = new Person();
        person.setName("test");
        person.setAge(50);
        person.setEmail("test@example.com");

        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .body(person)
                        .when()
                        .post();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(201);

        Response getResp =
                given()
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .extract().response();

        String jsonAsString = getResp.asString();

        Collection<Person> people = JsonPath.from(jsonAsString).getObject("", Collection.class);

        assertThat(people).hasSize(2);
    }

    @Test
    @InSequence(3)
    public void testPutPerson() {
        Person person = new Person();
        person.setId(2);
        person.setName("testUser");
        person.setAge(30);
        person.setEmail("test_user@example.com");

        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .body(person)
                        .when()
                        .put("/" + person.getId().toString());

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(200);

        Response getResp =
                given()
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .extract().response();

        String jsonAsString = getResp.asString();

        Collection<Person> people = JsonPath.from(jsonAsString).getObject("", Collection.class);

        assertThat(people).hasSize(2);
    }

    @Test
    @InSequence(4)
    public void testDeletePerson() {
        Response response =
                given().when().delete("/2");

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(204);

        Response getResp =
                given()
                        .when()
                        .get()
                        .then()
                        .statusCode(200)
                        .extract().response();

        String jsonAsString = getResp.asString();

        Collection<Person> people = JsonPath.from(jsonAsString).getObject("", Collection.class);

        assertThat(people).hasSize(1);
    }

}
