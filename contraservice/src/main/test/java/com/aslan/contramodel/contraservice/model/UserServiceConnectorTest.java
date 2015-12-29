package com.aslan.contramodel.contraservice.model;

import com.aslan.contra.dto.common.Person;
import com.aslan.contra.dto.ws.Message;
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
        person.setUserID("+94770780210");

        Message<Person> message = connector.create(person);

        assertEquals("Failed to create the person", true, message.isSuccess());
    }

    @Test
    public void testFindPerson() {
        UserServiceConnector connector = new UserServiceConnector();

        Person person = new Person();
        person.setUserID("+94770780210");
        person.setEmail("slgobinath@gmail.com");
        person.setName("Gobinath");

        connector.create(person);

        Message<Person> message = connector.find("+94770780210");
        assertEquals("Filed to create the person", "Gobinath", message.getEntity().getName());
    }
}
