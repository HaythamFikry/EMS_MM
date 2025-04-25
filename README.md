# Event Management System

A Java Servlet-based web application for managing events, ticketing, and real-time attendee tracking. The system streamlines the process of creating, managing, and analyzing events for organizers, while providing a seamless experience for attendees.

## Features

### Event Management
- Create, update, and delete events.
- Add details like name, date, time, location, description, images, and videos.
- Customizable registration forms to collect attendee information.

### Ticketing & Registration
- Support for both free and paid tickets.
- Ticket types: General Admission, VIP, Early Bird, etc.
- Automated ticket purchasing, cancellations, and refunds.
- Dynamic pricing based on demand.
- Discount code management.

### Real-Time Attendee Management
- Live attendee check-ins and attendance tracking.
- Historical data storage for post-event analysis.
- Real-time updates using Observer design pattern.

### Venue Management
- Manage venue details, capacity, availability, and conflict resolution.
- Booking management for multiple venues and overlapping events.

### Feedback & Analysis
- Collect attendee feedback through ratings and comments.
- Generate post-event reports with aggregated feedback.

### Automated Notifications
- Email reminders, updates, and promotional messages.
- Notifications triggered by schedule changes or approaching events.

### Data Security & Compliance
- Secure password hashing.
- Follows best practices for data handling and user privacy.

## Technical Overview

### Tech Stack
- **Backend:** Java Servlets
- **Design Principles:** Object-Oriented Design, SOLID Principles
- **Design Patterns:**
  - Singleton: For services like database access, ticketing, and notifications.
  - Observer: For notifying attendees of schedule or event changes.

### Key Entities
- `Event`
- `User` (Organizer, Attendee)
- `Ticket`
- `Venue`
- `NotificationService`
- `Feedback`

## Installation

1. Setup your database and update the connection parameters in the project.

2. Build the project (WAR file) and deploy to Apache Tomcat.

## Running on Apache Tomcat

1. Download and extract [Apache Tomcat](https://tomcat.apache.org/download-90.cgi).

2. Place the generated `.war` file inside the `webapps/` directory of Tomcat.

3. Start Tomcat using the `startup.sh` (Linux/macOS) or `startup.bat` (Windows) script in the `bin/` folder.

4. Access your web application at `http://localhost:8080/your-app-name`.

## Usage

- Organizers can:
  - Create and manage events
  - Track attendees
  - Manage ticket sales
  - Analyze event feedback

- Attendees can:
  - Browse and register for events
  - Receive email notifications
  - Submit feedback post-event


**Database Script**
CREATE DATABASE IF NOT EXISTS event_management_system;
USE event_management_system;
-- Users table
CREATE TABLE users (
user_id INT AUTO_INCREMENT PRIMARY KEY,
username VARCHAR(50) NOT NULL UNIQUE,
password_hash VARCHAR(255) NOT NULL,
email VARCHAR(100) NOT NULL UNIQUE,
first_name VARCHAR(50) NOT NULL,
last_name VARCHAR(50) NOT NULL,
role ENUM('ORGANIZER', 'ATTENDEE', 'VENUE_MANAGER') NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE
CURRENT_TIMESTAMP
);
-- Venues table
CREATE TABLE venues (
venue_id INT AUTO_INCREMENT PRIMARY KEY,
name VARCHAR(100) NOT NULL,
address TEXT NOT NULL,
capacity INT NOT NULL,
contact_person VARCHAR(100),
contact_phone VARCHAR(20),
contact_email VARCHAR(100),
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE
CURRENT_TIMESTAMP
);
-- Events table
CREATE TABLE events (
event_id INT AUTO_INCREMENT PRIMARY KEY,
title VARCHAR(100) NOT NULL,
description TEXT,
start_datetime DATETIME NOT NULL,
end_datetime DATETIME NOT NULL,
venue_id INT,
organizer_id INT NOT NULL,
image_url VARCHAR(255),
status ENUM('DRAFT', 'PUBLISHED', 'CANCELLED', 'COMPLETED') NOT NULL
DEFAULT 'DRAFT',
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE
CURRENT_TIMESTAMP,
FOREIGN KEY (venue_id) REFERENCES venues(venue_id) ON DELETE SET NULL,
FOREIGN KEY (organizer_id) REFERENCES users(user_id) ON DELETE CASCADE
);
-- Tickets table
CREATE TABLE tickets (
ticket_id INT AUTO_INCREMENT PRIMARY KEY,
event_id INT NOT NULL,
ticket_type VARCHAR(50) NOT NULL,
price DECIMAL(10, 2) NOT NULL,
quantity_available INT NOT NULL,
sale_start_date DATETIME,
sale_end_date DATETIME,
description TEXT,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE
CURRENT_TIMESTAMP,
FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE
);
-- Discount codes table
CREATE TABLE discount_codes (
code_id INT AUTO_INCREMENT PRIMARY KEY,
code VARCHAR(50) NOT NULL UNIQUE,
discount_type ENUM('PERCENTAGE', 'FIXED') NOT NULL,
discount_value DECIMAL(10, 2) NOT NULL,
event_id INT,
valid_from DATETIME,
valid_until DATETIME,
max_uses INT,
current_uses INT DEFAULT 0,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE
CURRENT_TIMESTAMP,
FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE
);
-- Orders table
CREATE TABLE orders (
order_id INT AUTO_INCREMENT PRIMARY KEY,
attendee_id INT NOT NULL,
order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
total_amount DECIMAL(10, 2) NOT NULL,
status ENUM('PENDING', 'PAID', 'CANCELLED', 'REFUNDED') NOT NULL DEFAULT
'PENDING',
payment_method VARCHAR(50),
transaction_id VARCHAR(100),
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE
CURRENT_TIMESTAMP,
FOREIGN KEY (attendee_id) REFERENCES users(user_id) ON DELETE CASCADE
);
-- Order items table
CREATE TABLE order_items (
item_id INT AUTO_INCREMENT PRIMARY KEY,
order_id INT NOT NULL,
ticket_id INT NOT NULL,
quantity INT NOT NULL,
unit_price DECIMAL(10, 2) NOT NULL,
discount_applied DECIMAL(10, 2) DEFAULT 0,
final_price DECIMAL(10, 2) NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (order_id) REFERENCES orders(order_id) ON DELETE CASCADE,
FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id) ON DELETE CASCADE
);
-- Attendee check-ins table
CREATE TABLE attendee_checkins (
checkin_id INT AUTO_INCREMENT PRIMARY KEY,
order_item_id INT NOT NULL,
checkin_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
checked_in_by INT,
notes TEXT,
FOREIGN KEY (order_item_id) REFERENCES order_items(item_id) ON DELETE
CASCADE,
FOREIGN KEY (checked_in_by) REFERENCES users(user_id) ON DELETE SET NULL
);
-- Feedback table
CREATE TABLE feedback (
feedback_id INT AUTO_INCREMENT PRIMARY KEY,
event_id INT NOT NULL,
attendee_id INT NOT NULL,
rating INT NOT NULL CHECK (rating BETWEEN 1 AND 5),
comments TEXT,
submitted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
FOREIGN KEY (attendee_id) REFERENCES users(user_id) ON DELETE CASCADE
);
-- Notifications table
CREATE TABLE notifications (
notification_id INT AUTO_INCREMENT PRIMARY KEY,
user_id INT NOT NULL,
title VARCHAR(100) NOT NULL,
message TEXT NOT NULL,
is_read BOOLEAN DEFAULT FALSE,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
-- Event observers table (for Observer pattern implementation)
CREATE TABLE event_observers (
observer_id INT AUTO_INCREMENT PRIMARY KEY,
event_id INT NOT NULL,
user_id INT NOT NULL,
created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
FOREIGN KEY (event_id) REFERENCES events(event_id) ON DELETE CASCADE,
FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
UNIQUE KEY unique_observer (event_id, user_id)
);

