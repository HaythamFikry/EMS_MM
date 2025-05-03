package com.ems.services;

import com.ems.config.DatabaseConnection;
import com.ems.dao.EventDAO;
import com.ems.exceptions.EventManagementException;
import com.ems.models.Event;
import com.ems.models.User;
import com.ems.models.Venue;
import com.ems.observers.EmailNotificationObserver;
import com.ems.observers.EventNotifier;
import com.ems.observers.InAppNotificationObserver;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 
 * Service class for event management operations.
 * Follows Single Responsibility Principle by focusing only on event-related operations.
 * 
 */
public class EventService {
    private EventDAO eventDAO;
    private Connection connection;

    public EventService() {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.eventDAO = new EventDAO(connection);
    }

    // Constructor For Mock
    public EventService(EventDAO eventDAO) {
        this.eventDAO = eventDAO; 
    }

    // Create a new event
    public Event createEvent(String title, String description, LocalDateTime startDateTime,
                             LocalDateTime endDateTime, User organizer, Venue venue) {

        // Validate title and organizer are not null
        if (title ==  null) {
            throw new NullPointerException("Title cannot be null");
        }
        if (organizer ==  null) {
            throw new NullPointerException("Organizer cannot be null");
        }

        // Validate date range
        if (startDateTime !=  null && endDateTime != null && endDateTime.isBefore(startDateTime)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }
        Event event =  new Event(0, title, description, startDateTime, endDateTime, venue, organizer, null);
        try {
            event  = eventDAO.createEvent(event);
            return event;
        } catch (SQLException e) {
            throw new EventManagementException("Failed to create event", e);
        }
    }




    // Update an existing event
    public Event updateEvent(Event event) {
        try {
            Event updatedEvent = eventDAO.updateEvent(event);

            // Notify observers about the update
            EventNotifier notifier = new EventNotifier(updatedEvent);
            List<User> observers = eventDAO.getEventObservers(updatedEvent.getEventId());
            NotificationService notificationService = new NotificationService();
            EmailService emailService = new EmailService();

            for (User observer : observers) {
                notifier.registerObserver(new EmailNotificationObserver(observer, emailService));
                notifier.registerObserver(new InAppNotificationObserver(observer, notificationService));
            }

            notifier.notifyObservers("Event details have been updated");

            return updatedEvent;
        } catch (SQLException e) {
            throw new EventManagementException("Failed to update event", e);
        }
    }



    // Cancel an event
    public void cancelEvent(int eventId) {
        try {
            Event event = eventDAO.getEventById(eventId);
            if (event == null) {
                throw new EventManagementException("Event not found with ID: " + eventId);
            }

            event.setStatus(Event.EventStatus.CANCELLED);
            eventDAO.updateEvent(event);

            // Notify observers about cancellation
            EventNotifier notifier = new EventNotifier(event);
            List<User> observers = eventDAO.getEventObservers(eventId);
            NotificationService notificationService = new NotificationService();
            EmailService emailService = new EmailService();

            for (User observer : observers) {
                notifier.registerObserver(new EmailNotificationObserver(observer, emailService));
                notifier.registerObserver(new InAppNotificationObserver(observer, notificationService));
            }

            notifier.notifyObservers("Event has been cancelled");

        } catch (SQLException e) {
            throw new EventManagementException("Failed to cancel event", e);
        }
    }

    // Get event by ID
    public Event getEventById(int eventId) {
        try {
            return eventDAO.getEventById(eventId);
        } catch (SQLException e) {
            throw new EventManagementException("Failed to retrieve event", e);
        }
    }

    // Get all events
    public List<Event> getAllEvents() {
        try {
            return eventDAO.getAllEvents();
        } catch (SQLException e) {
            throw new EventManagementException("Failed to retrieve events", e);
        }
    }
    public List<Event> getAllNotCancelledEvents() {
        try {
            return eventDAO.getAllNotCancelledEvents();
        } catch (SQLException e) {
            throw new EventManagementException("Failed to retrieve non-canceled events", e);
        }
    }

    public List<Event> getCanceledEvents() {
        try {
            return eventDAO.getCanceledEvents();
        } catch (SQLException e) {
            throw new EventManagementException("Failed to retrieve canceled events", e);
        }
    }

    // Get events organized by a specific user
    public List<Event> getEventsByOrganizer(int organizerId) {
        try {
            return eventDAO.getEventsByOrganizer(organizerId);
        } catch (SQLException e) {
            throw new EventManagementException("Failed to retrieve organizer's events", e);
        }
    }

    // Register an observer for an event
    public void registerEventObserver(int eventId, int userId) {
        try {
            eventDAO.addEventObserver(eventId, userId);
        } catch (SQLException e) {
            throw new EventManagementException("Failed to register event observer", e);
        }
    }

    // Remove an observer from an event
    public void removeEventObserver(int eventId, int userId) {
        try {
            eventDAO.removeEventObserver(eventId, userId);
        } catch (SQLException e) {
            throw new EventManagementException("Failed to remove event observer", e);
        }
    }



    public boolean isVenueAvailable(int venueId,int eventId, LocalDateTime start, LocalDateTime end) throws EventManagementException {
        try {
            return eventDAO.isVenueAvailable(venueId,eventId, start, end);
        } catch (SQLException e) {
            throw new EventManagementException("Error checking venue availability", e);
        }
    }
}
