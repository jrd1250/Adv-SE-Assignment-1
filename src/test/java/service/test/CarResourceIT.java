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
import service.Car;
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
public class CarResourceIT {

    protected static URL url;

    @BeforeClass
    public static void verifyAndSetup() throws MalformedURLException {
        url = new URL("http://localhost:8080/service");

        await()
                .atMost(1, TimeUnit.MINUTES)
                .until(() -> {
                    try {
                        return get(url + "/car").statusCode() == 200;
                    } catch (Exception e) {
                        return false;
                    }
                });
        RestAssured.baseURI = url + "/car";

        // This will invoke the clearAll endpoint, clearing all entities without id of 1
        Response response = given().when().delete();

        System.out.println("Clear all attempt returned: " + response.statusCode());
    }

    @Test
    @InSequence(1)
    public void testGetCar() {
        Response response =
                given()
                        .pathParam("car_id", 1)
                        .when()
                        .get("/{car_id}")
                        .then()
                        .statusCode(200)
                        .extract().response();

        String jsonAsString = response.asString();

        Car car = JsonPath.from(jsonAsString).getObject("", Car.class);

        assertThat(car.getId()).isEqualTo(1);
        assertThat(car.getBrand()).isEqualTo("Ford");
        assertThat(car.getType()).isEqualTo("Fusion");
        assertThat(car.getLicensePlate()).isEqualTo("ABCDEFG");
        assertThat(car.getOwner().getId()).isEqualTo(1);
    }

    @Test
    @InSequence(2)
    public void testPostCar() {
        Car car = new Car();
        car.setBrand("Ford");
        car.setType("Expedition");
        car.setLicensePlate("1234567");

        Person owner = new Person();
        owner.setName("test");
        owner.setAge(50);
        owner.setEmail("test@example.com");
        car.setOwner(owner);

        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .body(car)
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

        Collection<Car> cars = JsonPath.from(jsonAsString).getObject("", Collection.class);

        assertThat(cars).hasSize(2);
    }

    @Test
    @InSequence(3)
    public void testPutCar() {
        Car car = new Car();
        car.setId(2);
        car.setBrand("Ford");
        car.setType("Expedition");
        car.setLicensePlate("1234567");

        Person owner = new Person();
        owner.setName("testUser");
        owner.setAge(30);
        owner.setEmail("test_user@example.com");
        car.setOwner(owner);

        Response response =
                given()
                        .contentType(ContentType.JSON)
                        .body(car)
                        .when()
                        .put("/" + car.getId().toString());

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

        Collection<Car> cars = JsonPath.from(jsonAsString).getObject("", Collection.class);

        assertThat(cars).hasSize(2);
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

        Collection<Car> cars = JsonPath.from(jsonAsString).getObject("", Collection.class);

        assertThat(cars).hasSize(1);
    }

}
