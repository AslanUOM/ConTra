package com.aslan.contramodel.contraservice.model;

import com.aslan.contra.dto.Person;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Before running this test, make sure that your Neo4j is up and running with contramodel plugin.
 * <p>
 * Created by gobinath on 12/17/15.
 */
public class UserServiceConnectorTest {
    @Test
    public void testCreatePerson() {
        UserServiceConnector connector = new UserServiceConnector();

        Person person = new Person();
        person.setPhoneNumber("+94770780210");
        person.setEmail("slgobinath@gmail.com");
        person.setName("Gobinath");

        boolean result = connector.create(person);

        assertEquals("Filed to create the person", true, result);
    }

    @Test
    public void testFindPerson() {
        UserServiceConnector connector = new UserServiceConnector();

        Person person = new Person();
        person.setPhoneNumber("+94770780210");
        person.setEmail("slgobinath@gmail.com");
        person.setName("Gobinath");

        connector.create(person);

        Person receivedPerson = connector.find("+94770780210");
        assertEquals("Filed to create the person", "Gobinath", receivedPerson.getName());
    }
}
