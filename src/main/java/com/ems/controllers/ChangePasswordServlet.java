package com.ems.controllers;

import com.ems.config.DatabaseConnection;
import com.ems.dao.UserDAO;
import com.ems.models.User;
import com.ems.services.UserService;
import com.ems.utils.PasswordHasher;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;

@WebServlet("/profile/change-password")
public class ChangePasswordServlet extends HttpServlet {

    private UserService userService;
    private Connection connection;
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        this.connection = DatabaseConnection.getInstance().getConnection();
        this.userDAO = new UserDAO(connection);
        this.userService = new UserService(userDAO); // ✅ Important
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        System.out.println("🟢 ChangePasswordServlet triggered");
        User user = (User) request.getSession().getAttribute("user");

        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String confirmPassword = request.getParameter("confirmNewPassword");

        System.out.println("🔐 Current: " + currentPassword);
        System.out.println("🔐 New: " + newPassword);
        System.out.println("🔐 Confirm: " + confirmPassword);
        System.out.println("👤 User: " + (user != null ? user.getUsername() : "null"));



        try {


            if (!newPassword.equals(confirmPassword)) {
                request.setAttribute("passwordMismatchError", "New passwords do not match.");
                request.setAttribute("openChangePasswordModal", true);
                request.getRequestDispatcher("/WEB-INF/views/users/profile.jsp").forward(request, response);
                return;
            }
            if (!PasswordHasher.verifyPassword(currentPassword, user.getPasswordHash())) {
                request.setAttribute("changePasswordError", "Current password is incorrect.");
                request.setAttribute("openChangePasswordModal", true);
                request.getRequestDispatcher("/WEB-INF/views/users/profile.jsp").forward(request, response);
                return;
            }


            String hashedPassword = PasswordHasher.hashPassword(newPassword);
            System.out.println("🔐 Attempting to update password...");
            System.out.println("👤 User ID: " + user.getUserId());
            userService.updatePassword(user.getUserId(), hashedPassword);
            System.out.println("✅ Password update method completed.");

            // ✅ Logout the user
            request.getSession().invalidate();

            // ✅ Redirect to login page
            response.sendRedirect(request.getContextPath() + "/login?message=Password changed successfully.");
        } catch (Exception e) {
            request.setAttribute("error", "Failed to change password: " + e.getMessage());
            request.getRequestDispatcher("/WEB-INF/views/users/profile.jsp").forward(request, response);
        }
    }

}
