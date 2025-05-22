package org.GCremez.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;

class SessionTest {
    
    @Test
    void testSessionCreation() {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(25);
        Session session = new Session("work", now, duration);
        
        assertEquals("work", session.getType());
        assertEquals(now, session.getStartTime());
        assertEquals(duration, session.getDuration());
    }
    
    @Test
    void testJsonSerialization() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0);
        Duration duration = Duration.ofMinutes(25);
        Session session = new Session("work", now, duration);
        
        String json = session.toJson();
        Session deserialized = Session.fromJson(json);
        
        assertNotNull(deserialized);
        assertEquals(session.getType(), deserialized.getType());
        assertEquals(session.getStartTime(), deserialized.getStartTime());
        assertEquals(session.getDuration(), deserialized.getDuration());
    }
    
    @Test
    void testJsonEscaping() {
        Session session = new Session("work\"test", LocalDateTime.now(), Duration.ofMinutes(25));
        String json = session.toJson();
        Session deserialized = Session.fromJson(json);
        
        assertNotNull(deserialized);
        assertEquals("work\"test", deserialized.getType());
    }
    
    @Test
    void testInvalidJson() {
        assertNull(Session.fromJson("invalid json"));
        assertNull(Session.fromJson("{invalid:json}"));
        assertNull(Session.fromJson(null));
    }
} 