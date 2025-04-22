package com.ems.controllers;

import com.ems.exceptions.EventManagementException;
import com.ems.models.Event;
import com.ems.models.Feedback;
import com.ems.models.Order;
import com.ems.models.User;
import com.ems.services.EventService;
import com.ems.services.FeedbackService;
import com.ems.services.OrderService;
import com.ems.services.UserService;

import javax.servlet.*;
import javax.servlet.annotation.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@WebServlet(name = "UserServlet", urlPatterns = {"/register", "/login", "/logout", "/profile","/profile/change-password","/editProfile"})
public class UserServlet extends HttpServlet {
    private UserService userService;
    private EventService eventService;
    private OrderService orderService;
    private FeedbackService feedbackService;

    @Override
    public void init() throws ServletException {
        super.init();
        this.userService = new UserService();
        this.eventService = new EventService();
        this.orderService = new OrderService();
        this.feedbackService = new FeedbackService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        System.out.println(path);
        switch (path) {
            case "/register":
                showRegistrationForm(request, response);
                break;
            case "/login":
                showLoginForm(request, response);
                break;
            case "/logout":
                logoutUser(request, response);
                break;
            case "/profile":
                showUserProfile(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        System.out.println(path);
        switch (path) {
            case "/register":
                registerUser(request, response);
                break;
            case "/login":
                loginUser(request, response);
                break;
            case "/profile/change-password":
                changePassword(request, response);
                break;
            case "/editProfile":
                updateUser(request, response);
                break;
            default:
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showRegistrationForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/users/register.jsp").forward(request, response);
    }

    private void showLoginForm(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/users/login.jsp").forward(request, response);
    }

    private void showUserProfile(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if(user.getRole() == User.UserRole.ORGANIZER) {
            List<Event> events = eventService.getEventsByOrganizer(user.getUserId());
            request.setAttribute("events", events);


        } else {
            Event event = eventService.getEventById(user.getUserId()); // Single Event

            List<Event> events = new ArrayList<>();
            events.add(event);
            request.setAttribute("events", events); // Pass as a list to the JSP
        }

        List<Order> orders = orderService.getOrdersByUser(user.getUserId());
        if (orders == null) {
            orders = new ArrayList<>();
        }
        request.setAttribute("orders", orders);

        List<Feedback> feedbacks = feedbackService.getFeedbackByUser(user.getUserId());
        if (feedbacks == null) {
            feedbacks = new ArrayList<>();
        }
        request.setAttribute("feedbacks", feedbacks);

        Date createdAtDate = Date.from(user.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
        request.setAttribute("createdAtDate", createdAtDate);

        request.getRequestDispatcher("/WEB-INF/views/users/profile.jsp").forward(request, response);
    }

    private void registerUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String username = request.getParameter("username");
            String password = request.getParameter("password");
            String confirmPassword = request.getParameter("confirmPassword");
            String email = request.getParameter("email");
            String firstName = request.getParameter("firstName");
            String lastName = request.getParameter("lastName");
            User.UserRole role = User.UserRole.valueOf(request.getParameter("role"));

            // Check if password and confirm password match
            if (!password.equals(confirmPassword)) {
                request.setAttribute("error", "Passwords do not match");
                request.getRequestDispatcher("/WEB-INF/views/users/register.jsp").forward(request, response);
                return;
            }

            User user = userService.registerUser(username, password, email, firstName, lastName, role);

            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("username", user.getUsername());


            if (role == User.UserRole.ORGANIZER) {
                response.sendRedirect(request.getContextPath() + "/events/my-events");
            } else {
                response.sendRedirect(request.getContextPath() + "/events");
            }
        } catch (EventManagementException e) {
            request.setAttribute("error", "Registration failed: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/users/register.jsp").forward(request, response);
        }
    }

    private void loginUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            User user = userService.authenticateUser(username, password);

            HttpSession session = request.getSession();
            session.setAttribute("user", user);
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole().name());

            response.sendRedirect(request.getContextPath() + "/");
        } catch (EventManagementException e) {
            request.setAttribute("error", "Login failed: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/users/login.jsp").forward(request, response);
        }
    }

    private void logoutUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        session.invalidate();
//        response.sendRedirect(request.getContextPath() + "/login");
        request.getRequestDispatcher("/WEB-INF/views/users/login.jsp").forward(request, response);

    }

    private void changePassword(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmNewPassword");

        if (!newPassword.equals(confirmPassword)) {
            request.setAttribute("passwordMismatchError", "New password and confirmation do not match.");
            request.setAttribute("openChangePasswordModal", true);
            showUserProfile(request, response);
            return;
        }

        try {
            userService.changePassword(user.getUserId(), currentPassword, newPassword);
            session.invalidate();
            response.sendRedirect(request.getContextPath() + "/login?message=Password changed successfully.");
        } catch (EventManagementException e) {
            request.setAttribute("changePasswordError", e.getMessage());
            request.setAttribute("openChangePasswordModal", true);
            showUserProfile(request, response);
        }
    }

    public void updateUser(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {


        System.out.println("Try update");

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        System.out.println("Try update");

        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");

        try {

            System.out.println("Try update");
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEmail(email);

            userService.updateUser(user);
            request.setAttribute("success", "Profile updated successfully.");
            showUserProfile(request, response);
        } catch (EventManagementException e) {
            request.setAttribute("error", "Failed to update profile: " + e.getMessage());
            showUserProfile(request, response);
        }
    }
}