package com.aslan.contramodel.contraservice.services;

import com.aslan.contra.dto.Person;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.Response;
import java.net.HttpURLConnection;

import static org.junit.Assert.assertEquals;

/**
 * Created by gobinath on 12/18/15.
 */
public class UserManagementServiceTest extends JerseyTest {

    @Override
    protected Application configure() {
        return new ResourceConfig(UserManagementService.class);
    }

    private Person createPerson(String name, String phoneNumber, String email) {
        Person person = new Person();
        person.setName(name);
        person.setUserID(phoneNumber);
        person.setEmail(email);

        return person;
    }

    @Test
    public void testCreateCountryInUpperCase() {
        Person person = createPerson("Gobinath", "0770780210", "slgobinath@gmail.com");
        String id = target("user/create").queryParam("country", "LK").request().post(Entity.json(person), String.class);
        assertEquals("Failed to create person.", "+94770780210", id);
    }

    @Test
    public void testCreateCountryInLowerCase() {
        Person person = createPerson("Alice", "0770780211", "alice@gmail.com");
        String id = target("user/create").queryParam("country", "lk").request().post(Entity.json(person), String.class);
        assertEquals("Failed to create person.", "+94770780211", id);
    }

    @Test
    public void testCreateWithoutCountry() {
        Person person = createPerson("Gobinath", "0770780210", "slgobinath@gmail.com");
        Response response = target("user/create").request().post(Entity.json(person));
        assertEquals("Empty country is accepted.", HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testCreateWithoutPerson() {
        Response response = target("user/create").queryParam("country", "lk").request().post(null);
        assertEquals("Null person is accepted.", HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testCreateInvalidCountry() {
        Person person = createPerson("Gobinath", "0770780210", "slgobinath@gmail.com");
        Response response = target("user/create").queryParam("country", "XX").request().post(Entity.json(person));
        assertEquals("Invalid country is accepted", HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }

    @Test
    public void testFindExistingPerson() {
        Person person = createPerson("Carol", "0776780124", "carol@gmail.com");
        String id = target("user/create").queryParam("country", "LK").request().post(Entity.json(person), String.class);
        assertEquals("Failed to create person.", "+94776780124", id);

        Person receivedPerson = target("user/find/+94776780124").request().get(Person.class);
        assertEquals("Failed to find the person.", "Carol", receivedPerson.getName());
    }

    @Test
    public void testFindInvalidUserId() {
        Response response = target("user/find/+94000").request().get();
        assertEquals("Accepting invalid phone number.", HttpURLConnection.HTTP_BAD_REQUEST, response.getStatus());
    }
}
