package com.ems.services;

import com.ems.dao.TicketDAO;
import com.ems.exceptions.EventManagementException;
import com.ems.models.Ticket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ems.models.Event;
import java.time.LocalDateTime;
import java.util.Collections;

public class TicketServiceTest {

    private TicketService ticketService;
    private TicketDAO ticketDAOMock;

    private Event dummyEvent;

    @BeforeEach
    public void setup() {
        ticketDAOMock = mock(TicketDAO.class);
        ticketService = new TicketService(ticketDAOMock); // This assumes you refactor TicketService to accept a DAO
        dummyEvent = new Event(1, "Concert", "A cool concert",
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                null, null, null);
    }

    @Test
    public void testCreateTicket_Success() throws SQLException {
        Ticket inputTicket = new Ticket(0, dummyEvent, "VIP", 100.0, 50,
                LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(5),
                "Front row access");

        Ticket savedTicket = new Ticket(1, dummyEvent, "VIP", 100.0, 50,
                inputTicket.getSaleStartDate(), inputTicket.getSaleEndDate(), "Front row access");

        when(ticketDAOMock.createTicket(any(Ticket.class))).thenReturn(savedTicket);

        Ticket result = ticketService.createTicket(dummyEvent, "VIP", 100.0, 50,
                inputTicket.getSaleStartDate(), inputTicket.getSaleEndDate(), "Front row access");

        assertNotNull(result);
        assertEquals(1, result.getTicketId());
        assertEquals("VIP", result.getTicketType());
    }

    @Test
    public void testCreateTicket_InvalidDates_ShouldThrowException() {
        LocalDateTime start = LocalDateTime.now().plusDays(5);
        LocalDateTime end = LocalDateTime.now().plusDays(1);

        Exception exception = assertThrows(EventManagementException.class, () ->
                ticketService.createTicket(dummyEvent, "VIP", 100.0, 50, start, end, "Invalid dates")
        );

        assertTrue(exception.getMessage().contains("Ticket sale end date cannot be before the start date"));
    }

    @Test
    public void testGetTicketById_Success() throws SQLException {
        Ticket ticket = new Ticket(1, dummyEvent, "Standard", 50.0, 100,
                LocalDateTime.now(), LocalDateTime.now().plusDays(1), "Standard ticket");

        when(ticketDAOMock.getTicketById(1)).thenReturn(ticket);

        Ticket result = ticketService.getTicketById(1);
        assertNotNull(result);
        assertEquals("Standard", result.getTicketType());
    }

    @Test
    public void testDeleteTicket_CallsDAO() throws SQLException {
        doNothing().when(ticketDAOMock).deleteTicket(1);
        ticketService.deleteTicket(1);
        verify(ticketDAOMock, times(1)).deleteTicket(1);
    }

    @Test
    public void testGetTicketsByEvent_ReturnsList() throws SQLException {
        when(ticketDAOMock.getTicketsByEvent(1)).thenReturn(Collections.singletonList(
                new Ticket(1, dummyEvent, "Standard", 50.0, 20,
                        LocalDateTime.now(), LocalDateTime.now().plusDays(2), "Test ticket")
        ));

        List<Ticket> tickets = ticketService.getTicketsByEvent(1);
        assertEquals(1, tickets.size());
        assertEquals("Standard", tickets.get(0).getTicketType());
    }

    @Test
    public void testIsTicketAvailable_True() throws SQLException {
        Ticket ticket = new Ticket(1, dummyEvent, "General", 20.0, 5,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), "Available");

        when(ticketDAOMock.getTicketById(1)).thenReturn(ticket);

        boolean available = ticketService.isTicketAvailable(1, 3);
        assertTrue(available);
    }

    @Test
    public void testIsTicketAvailable_NotEnoughQuantity() throws SQLException {
        Ticket ticket = new Ticket(1, dummyEvent, "General", 20.0, 2,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1), "Limited");

        when(ticketDAOMock.getTicketById(1)).thenReturn(ticket);

        boolean available = ticketService.isTicketAvailable(1, 3);
        assertFalse(available);
    }

    @Test
    public void testUpdateTicketQuantity_CallsDAO() throws SQLException {
        doNothing().when(ticketDAOMock).updateTicketQuantity(1, -2);
        ticketService.updateTicketQuantity(1, -2);
        verify(ticketDAOMock).updateTicketQuantity(1, -2);
    }

    @Test
    public void testGetAllTickets_ReturnsList() throws SQLException {
        when(ticketDAOMock.getAllTickets()).thenReturn(Arrays.asList(
                new Ticket(1, dummyEvent, "VIP", 120.0, 5,
                        LocalDateTime.now(), LocalDateTime.now().plusDays(1), "First row")
        ));

        List<Ticket> tickets = ticketService.getAllTickets();
        assertEquals(1, tickets.size());
        assertEquals("VIP", tickets.get(0).getTicketType());
    }
}

