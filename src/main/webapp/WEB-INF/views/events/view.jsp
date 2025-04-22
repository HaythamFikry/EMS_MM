<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<c:set var="pageTitle" value="${event.title}" scope="request" />
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/all.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/bootstrap.min.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/css/styles.css">
<jsp:include page="/WEB-INF/views/layouts/base.jsp" />

<div class="row">
    <!-- Main Content Column -->
    <div class="col-md-8">
        <% if (session.getAttribute("error") != null) { %>
        <div class="alert alert-danger">
            <%= session.getAttribute("error") %>
        </div>
        <% session.removeAttribute("error"); %>
        <% } %>
        <!-- Event Details Card -->
        <div class="card mb-4">
            <c:if test="${not empty event.imageUrl}">
                <img src="${pageContext.request.contextPath}/${event.imageUrl}" class="card-img-top" alt="${event.title}" style="height: 20rem; object-fit: cover;">
            </c:if>
            <div class="card-body">
                <h1 class="card-title">
                    <c:out value="${event.title}" />
                </h1>
                <div class="mb-4">
                    <span class="badge bg-${event.status == 'PUBLISHED' ? 'success' : event.status == 'CANCELLED' ? 'danger' : 'warning'}">
                        <c:out value="${event.status}" />
                    </span>
                    <c:if test="${event.status != 'CANCELLED'}">
                        <input type="hidden" id="eventId" value="${event.eventId}">
                    </c:if>
                </div>
                <div class="mb-4">
                    <h5>Event Details</h5>
                    <p>
                        <i class="far fa-calendar-alt"></i>
                        <fmt:formatDate value="${startDate}" pattern="EEE, MMM d, yyyy h:mm a" />
                        to
                        <fmt:formatDate value="${endDate}" pattern="EEE, MMM d, yyyy h:mm a" />
                    </p>
                    <c:if test="${event.venue != null}">
                        <p>
                            <i class="fas fa-map-marker-alt"></i>
                            <strong><c:out value="${event.venue.name}" /></strong><br>
                            <c:out value="${event.venue.address}" />
                        </p>
                    </c:if>
                </div>
                <div class="mb-4">
                    <h5>Description</h5>
                    <p class="card-text">
                        <c:out value="${event.description}" />
                    </p>
                </div>
                <c:if test="${sessionScope.user.userId == event.organizer.userId}">
                    <div class="mt-4">
                        <a href="${pageContext.request.contextPath}/events/${event.eventId}/edit" class="btn btn-primary me-2">Edit Event</a>
                        <c:if test="${event.status != 'CANCELLED'}">
                            <a href="${pageContext.request.contextPath}/events/${event.eventId}/cancel" class="btn btn-danger">Cancel Event</a>
                        </c:if>
                    </div>
                </c:if>
            </div>
        </div>





        <!-- Tickets List Card -->
        <div class="card mb-4">
            <div class="card-header d-flex justify-content-between align-items-center">
                <h5 class="mb-0">Tickets</h5>
                <c:if test="${sessionScope.user.role == 'ORGANIZER' && sessionScope.user.userId == event.organizer.userId}">
                    <button type="button" class="btn btn-success btn-sm" id="addTicketButton">Add Ticket</button>
                </c:if>
            </div>
            <div class="card-body">
                <c:choose>
                    <c:when test="${not empty tickets}">
                        <div class="table-responsive">
                            <table class="table table-hover">
                                <thead>
                                <tr>
                                    <th>Type</th>
                                    <th>Price</th>
                                    <th>Available</th>
                                    <th>Sale Period</th>
                                    <th>Actions</th>
                                </tr>
                                </thead>
                                <tbody>
                                <c:forEach var="ticket" items="${tickets}">
                                    <tr>
                                        <td>
                                            <strong><c:out value="${ticket.ticketType}" /></strong>
                                            <div class="small text-muted"><c:out value="${ticket.description}" /></div>
                                        </td>
                                        <td>$<fmt:formatNumber value="${ticket.price}" pattern="#,##0.00" /></td>
                                        <td class="quantity-cell">
                                            <span id="quantity-${ticket.ticketId}">${ticket.quantityAvailable}</span>
                                            <c:if test="${sessionScope.user.role == 'ORGANIZER' && sessionScope.user.userId == event.organizer.userId}">

                                            </c:if>
                                        </td>
                                        <td>
                                            <c:if test="${ticket.saleStartDate != null && ticket.saleEndDate != null}">
                                                <small>${ticket.getFormattedStartDateTime()} to<br>${ticket.getFormattedEndDateTime()}</small>
                                            </c:if>
                                            <c:if test="${ticket.saleStartDate == null || ticket.saleEndDate == null}">
                                                <small>Always available</small>
                                            </c:if>
                                        </td>
                                        <td>
                                            <c:if test="${sessionScope.user.role == 'ORGANIZER' && sessionScope.user.userId == event.organizer.userId}">
                                                <button type="button" class="btn btn-sm btn-outline-warning edit-ticket-btn"
                                                        data-ticket-id="${ticket.ticketId}"
                                                        data-ticket-type="${ticket.ticketType}"
                                                        data-price="${ticket.price}"
                                                        data-quantity="${ticket.quantityAvailable}"
                                                        data-description="${ticket.description}"
                                                        data-sale-start="${ticket.saleStartDate}"
                                                        data-sale-end="${ticket.saleEndDate}">
                                                    Update
                                                </button>
                                                <a href="${pageContext.request.contextPath}/tickets/${ticket.ticketId}/delete"
                                                   class="btn btn-sm btn-outline-danger"
                                                   onclick="return confirm('Are you sure you want to delete this ticket?')">Delete</a>
                                            </c:if>
                                            <c:if test="${ticket.isAvailable() && sessionScope.user != null && sessionScope.user.role == 'ATTENDEE'}">
                                                <button class="btn btn-primary btn-sm buy-ticket-btn"
                                                        data-ticket-id="${ticket.ticketId}"
                                                        data-ticket-type="${ticket.ticketType}"
                                                        data-price="${ticket.price}">
                                                    Buy Ticket
                                                </button>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <p>No tickets available for this event.</p>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>



    </div>

    <!-- Sidebar: Organizer Info and Event Statistics -->
    <div class="col-md-4">
        <!-- Organizer Info Card -->
        <div class="card mb-4">
            <div class="card-header">
                <h5 class="mb-0">Organizer</h5>
            </div>
            <div class="card-body">
                <div class="d-flex align-items-center">
                    <div class="me-3">
                        <div class="avatar bg-primary text-white rounded-circle d-flex align-items-center justify-content-center" style="width: 50px; height: 50px;">
                            ${fn:substring(event.organizer.firstName, 0, 1)}${fn:substring(event.organizer.lastName, 0, 1)}
                        </div>
                    </div>
                    <div>
                        <h6 class="mb-0">
                            <c:out value="${event.organizer.firstName} ${event.organizer.lastName}" />
                        </h6>
                        <small class="text-muted">Event Organizer</small>
                    </div>
                </div>
            </div>
        </div>

        <!-- Event Statistics Card -->
        <div class="card mb-4">
            <div class="card-header">
                <h5 class="mb-0">Event Statistics</h5>
            </div>
            <div class="card-body">
                <div class="mb-3">
                    <h6>Tickets Sold</h6>
                    <p class="mb-0">${ticketsSold} / ${totalCapacity}</p>
                </div>
                <div class="mb-3">
                    <h6>Check-ins</h6>
                    <p class="mb-0">${checkInsCount}</p>
                </div>
                <div>
                    <h6>Average Rating</h6>
                    <div class="d-flex align-items-center">
                        <div class="rating-stars me-2">
                            <c:forEach begin="1" end="5" var="i">
                                <c:choose>
                                    <c:when test="${i <= Math.floor(averageRating)}">
                                        <i class="fas fa-star"></i>
                                    </c:when>
                                    <c:when test="${i == Math.ceil(averageRating) && averageRating % 1 != 0}">
                                        <i class="fas fa-star-half-alt"></i>
                                    </c:when>
                                    <c:otherwise>
                                        <i class="far fa-star"></i>
                                    </c:otherwise>
                                </c:choose>
                            </c:forEach>
                        </div>
                        <span><fmt:formatNumber value="${averageRating}" pattern="#.#"/>/5 (${feedbackCount} reviews)</span>
                    </div>
                </div>
            </div>
        </div>


    </div>
</div>

<!-- Add Ticket Modal -->
<div class="modal fade" id="addTicketModal" tabindex="-1" aria-labelledby="addTicketModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form action="${pageContext.request.contextPath}/tickets/create" method="post">
                <div class="modal-header">
                    <h5 class="modal-title" id="addTicketModalLabel">Add Ticket</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" name="eventId" value="${event.eventId}" />
                    <div class="mb-3">
                        <label for="ticketTypeModal" class="form-label">Ticket Type</label>
                        <select class="form-select" id="ticketTypeModal" name="ticketType" required>
                            <option value="" selected disabled>Select ticket type</option>
                            <option value="Regular">Regular</option>
                            <option value="VIP">VIP</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label for="priceModal" class="form-label">Price</label>
                        <input type="number" step="0.01" class="form-control" id="priceModal" name="price" placeholder="e.g. 100.00" required>
                    </div>
                    <div class="mb-3">
                        <label for="quantityAvailableModal" class="form-label">Quantity Available</label>
                        <input type="number" class="form-control" id="quantityAvailableModal" name="quantityAvailable" required>
                    </div>
                    <div class="mb-3">
                        <label for="descriptionModal" class="form-label">Description</label>
                        <textarea class="form-control" id="descriptionModal" name="description" rows="3" placeholder="Ticket details"></textarea>
                    </div>
                    <div class="mb-3">
                        <label for="saleStartDateModal" class="form-label">Sale Start Date</label>
                        <input type="datetime-local" class="form-control" id="saleStartDateModal" name="saleStartDate" required>
                    </div>
                    <div class="mb-3">
                        <label for="saleEndDateModal" class="form-label">Sale End Date</label>
                        <input type="datetime-local" class="form-control" id="saleEndDateModal" name="saleEndDate" required>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">Add Ticket</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Edit Ticket Modal -->
<div class="modal fade" id="editTicketModal" tabindex="-1" aria-labelledby="editTicketModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <form id="editTicketForm" method="post">
                <div class="modal-header">
                    <h5 class="modal-title" id="editTicketModalLabel">Edit Ticket</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <input type="hidden" id="editTicketId" name="ticketId" />
                    <div class="mb-3">
                        <label for="editTicketType" class="form-label">Ticket Type</label>
                        <select class="form-select" id="editTicketType" name="ticketType" required>
                            <option value="Regular">Regular</option>
                            <option value="VIP">VIP</option>
                        </select>
                    </div>
                    <div class="mb-3">
                        <label for="editPrice" class="form-label">Price</label>
                        <input type="number" step="0.01" class="form-control" id="editPrice" name="price" required>
                    </div>
                    <div class="mb-3">
                        <label for="editQuantityAvailable" class="form-label">Quantity Available</label>
                        <input type="number" class="form-control" id="editQuantityAvailable" name="quantityAvailable" required>
                    </div>
                    <div class="mb-3">
                        <label for="editDescription" class="form-label">Description</label>
                        <textarea class="form-control" id="editDescription" name="description" rows="3"></textarea>
                    </div>
                    <div class="mb-3">
                        <label for="editSaleStartDate" class="form-label">Sale Start Date</label>
                        <input type="datetime-local" class="form-control" id="editSaleStartDate" name="saleStartDate" required>
                    </div>
                    <div class="mb-3">
                        <label for="editSaleEndDate" class="form-label">Sale End Date</label>
                        <input type="datetime-local" class="form-control" id="editSaleEndDate" name="saleEndDate" required>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                    <button type="submit" class="btn btn-primary">Update Ticket</button>
                </div>
            </form>
        </div>
    </div>
</div>

<!-- Buy Ticket Confirmation Modal -->
<div class="modal fade" id="buyConfirmModal" tabindex="-1" aria-labelledby="buyConfirmModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="buyConfirmModalLabel">Confirm Purchase</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <p>Are you sure you want to purchase <span id="ticketTypeSpan"></span>?</p>
                <p>Price: $<span id="ticketPriceSpan"></span></p>
                <form id="buyTicketForm" method="post" action="${pageContext.request.contextPath}/orders/purchase">
                    <input type="hidden" id="confirmTicketId" name="ticketId">
                    <div class="mb-3">
                        <label for="ticketQuantity" class="form-label">Quantity:</label>
                        <input type="number" class="form-control" id="ticketQuantity" name="quantity" min="1" value="1" required>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn btn-primary" id="confirmBuyBtn">Confirm Purchase</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>

<!-- Error Message Modal -->
<div class="modal fade" id="errorModal" tabindex="-1" aria-labelledby="errorModalLabel" aria-hidden="true">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h5 class="modal-title" id="errorModalLabel">Error</h5>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
            </div>
            <div class="modal-body">
                <p id="errorMessage"></p>
            </div>
            <div class="modal-footer">
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Close</button>
            </div>
        </div>
    </div>
</div>

<!-- Scripts -->
<script src="${pageContext.request.contextPath}/js/jquery-3.5.1.min.js"></script>
<script src="${pageContext.request.contextPath}/js/bootstrap.bundle.min.js"></script>
<script src="${pageContext.request.contextPath}/js/scripts.js"></script>
<script>
    $(document).ready(function () {
        // Add Ticket Button
        $("#addTicketButton").click(function (e) {
            e.preventDefault();
            $("#addTicketModal form")[0].reset();
            $("#addTicketModal").modal("show");
        });

// Edit Ticket Button
        $('.edit-ticket-btn').click(function () {
            const ticketId = $(this).data('ticket-id');
            const ticketType = $(this).data('ticket-type');
            const price = $(this).data('price');
            const quantity = $(this).data('quantity');
            const description = $(this).data('description');
            const saleStart = $(this).data('sale-start');
            const saleEnd = $(this).data('sale-end');

            $('#editTicketForm').attr('action', '${pageContext.request.contextPath}/tickets/' + ticketId + '/update');
            $('#editTicketId').val(ticketId);
            $('#editTicketType').val(ticketType); // This will select the appropriate option in the dropdown
            $('#editPrice').val(price);
            $('#editQuantityAvailable').val(quantity);
            $('#editDescription').val(description);
            if (saleStart) $('#editSaleStartDate').val(new Date(saleStart).toISOString().slice(0, 16));
            if (saleEnd) $('#editSaleEndDate').val(new Date(saleEnd).toISOString().slice(0, 16));

            $('#editTicketModal').modal('show');
        });

        // Buy Ticket Button
        $('.buy-ticket-btn').click(function () {
            const ticketId = $(this).data('ticket-id');
            const ticketType = $(this).data('ticket-type');
            const ticketPrice = $(this).data('price');

            $('#ticketTypeSpan').text(ticketType);
            $('#ticketPriceSpan').text(ticketPrice);
            $('#confirmTicketId').val(ticketId);
            $('#buyConfirmModal').modal('show');
        });

        // Ticket Quantity Buttons
        $('.increase-btn').click(function () {
            const ticketId = $(this).data('ticket-id');
            const quantityElement = $('#quantity-' + ticketId);
            let currentQuantity = parseInt(quantityElement.text());
            quantityElement.text(currentQuantity + 1);
            updateTicketQuantity(ticketId, 1);
        });

        $('.decrease-btn').click(function () {
            const ticketId = $(this).data('ticket-id');
            const quantityElement = $('#quantity-' + ticketId);
            let currentQuantity = parseInt(quantityElement.text());
            if (currentQuantity > 0) {
                quantityElement.text(currentQuantity - 1);
                updateTicketQuantity(ticketId, -1);
            }
        });

        function updateTicketQuantity(ticketId, change) {
            $.ajax({
                url: '${pageContext.request.contextPath}/tickets/' + ticketId + '/update-quantity',
                type: 'POST',
                data: { quantityChange: change },
                success: function (response) {
                    console.log('Quantity updated successfully');
                },
                error: function (xhr, status, error) {
                    console.error('Error updating quantity:', error);
                }
            });
        }

        // Watch/Unwatch Event
        $('#watchEventBtn').click(function () {
            const eventId = $('#eventId').val();
            const isWatching = $(this).text().trim() === 'Unwatch';
            $.ajax({
                url: '${pageContext.request.contextPath}/api/events/' + eventId + '/watch',
                type: isWatching ? 'DELETE' : 'POST',
                success: function () {
                    $('#watchEventBtn').text(isWatching ? 'Watch Event' : 'Unwatch Event');
                }
            });
        });

        // Validate Ticket Dates
        function validateTicketDates(startDateInput, endDateInput) {
            const startDate = new Date(startDateInput.value);
            const endDate = new Date(endDateInput.value);
            if (endDate < startDate) {
                showErrorModal('Error: Ticket sale end date cannot be before the start date.');
                return false;
            }
            return true;
        }

        // Add Ticket Form Validation
        $('#addTicketModal form').on('submit', function (e) {
            if (!validateTicketDates($('#saleStartDateModal'), $('#saleEndDateModal'))) {
                e.preventDefault();
            }
        });

        // Edit Ticket Form Validation
        $('#editTicketForm').on('submit', function (e) {
            if (!validateTicketDates($('#editSaleStartDate'), $('#editSaleEndDate'))) {
                e.preventDefault();
            }
        });

        // Show Error Modal
        function showErrorModal(message) {
            $('#errorMessage').text(message);
            $('#errorModal').modal('show');
        }
    });
</script>