package org.GCremez.model;

import org.GCremez.timer.PomodoroTimer.SessionType;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.LocalDateTime;

class SessionTest {
    
    @Test
    void testSessionCreation() {
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(25);
        Session session = new Session(SessionType.WORK, duration, now);
        
        assertEquals(SessionType.WORK, session.getType());
        assertEquals(now, session.getStartTime());
        assertEquals(duration, session.getDuration());
    }
    
    @Test
    void testJsonSerialization() {
        LocalDateTime now = LocalDateTime.of(2024, 1, 1, 12, 0);
        Duration duration = Duration.ofMinutes(25);
        Session session = new Session(SessionType.WORK, duration, now);
        
        String json = session.toJson();
        Session deserialized = Session.fromJson(json);
        
        assertNotNull(deserialized);
        assertEquals(session.getType(), deserialized.getType());
        assertEquals(session.getStartTime(), deserialized.getStartTime());
        assertEquals(session.getDuration(), deserialized.getDuration());
    }
    
    @Test
    void testJsonEscaping() {
        Session session = new Session(SessionType.WORK, Duration.ofMinutes(25), LocalDateTime.now());
        String json = session.toJson();
        Session deserialized = Session.fromJson(json);
        
        assertNotNull(deserialized);
        assertEquals(SessionType.WORK, deserialized.getType());
    }
    
    @Test
    void testInvalidJson() {
        assertNull(Session.fromJson("invalid json"));
        assertNull(Session.fromJson("{invalid:json}"));
        assertNull(Session.fromJson(null));
    }
} 