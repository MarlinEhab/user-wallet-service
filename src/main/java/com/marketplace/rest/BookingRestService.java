package com.marketplace.rest;

import com.marketplace.ejb.BookingBean;
import com.marketplace.entities.Booking;
import com.marketplace.entities.Notification;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.List;

@Path("/bookings")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookingRestService {

    @EJB
    private BookingBean bookingBean;

    // Create a booking
    @POST
    @Path("/create")
    public Response createBooking(String body) {
        try {
            JSONObject json = new JSONObject(body);
            Long customerId = json.getLong("customerId");
            Long serviceOfferId = json.getLong("serviceOfferId");
            Long providerId = json.getLong("providerId");
            double amount = json.getDouble("amount");

            Booking booking = bookingBean.createBooking(customerId, serviceOfferId, providerId, amount);

            JSONObject response = new JSONObject();
            response.put("bookingId", booking.getId());
            response.put("customerId", booking.getCustomerId());
            response.put("providerId", booking.getProviderId());
            response.put("serviceOfferId", booking.getServiceOfferId());
            response.put("amount", booking.getAmount());
            response.put("status", booking.getStatus());
            response.put("bookingDate", booking.getBookingDate().toString());
            response.put("customerUsername", booking.getCustomerUsername());
            response.put("providerUsername", booking.getProviderUsername());
            response.put("message", "Booking confirmed successfully");
            return Response.status(Response.Status.CREATED).entity(response.toString()).build();
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            error.put("status", "REJECTED");
            return Response.status(Response.Status.BAD_REQUEST).entity(error.toString()).build();
        }
    }

    // Get all bookings (Admin)
    @GET
    @Path("/all")
    public Response getAllBookings() {
        try {
            List<Booking> bookings = bookingBean.getAllBookings();
            JSONArray array = new JSONArray();
            for (Booking b : bookings) {
                JSONObject obj = new JSONObject();
                obj.put("bookingId", b.getId());
                obj.put("customerId", b.getCustomerId());
                obj.put("providerId", b.getProviderId());
                obj.put("serviceOfferId", b.getServiceOfferId());
                obj.put("amount", b.getAmount());
                obj.put("status", b.getStatus());
                obj.put("bookingDate", b.getBookingDate().toString());
                obj.put("customerUsername", b.getCustomerUsername());
                obj.put("providerUsername", b.getProviderUsername());
                array.put(obj);
            }
            return Response.ok(array.toString()).build();
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error.toString()).build();
        }
    }

    // Get bookings by customer
    @GET
    @Path("/customer/{customerId}")
    public Response getBookingsByCustomer(@PathParam("customerId") Long customerId) {
        try {
            List<Booking> bookings = bookingBean.getBookingsByCustomer(customerId);
            JSONArray array = new JSONArray();
            for (Booking b : bookings) {
                JSONObject obj = new JSONObject();
                obj.put("bookingId", b.getId());
                obj.put("serviceOfferId", b.getServiceOfferId());
                obj.put("providerId", b.getProviderId());
                obj.put("amount", b.getAmount());
                obj.put("status", b.getStatus());
                obj.put("bookingDate", b.getBookingDate().toString());
                obj.put("providerUsername", b.getProviderUsername());
                array.put(obj);
            }
            return Response.ok(array.toString()).build();
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error.toString()).build();
        }
    }

    // Get bookings by provider
    @GET
    @Path("/provider/{providerId}")
    public Response getBookingsByProvider(@PathParam("providerId") Long providerId) {
        try {
            List<Booking> bookings = bookingBean.getBookingsByProvider(providerId);
            JSONArray array = new JSONArray();
            for (Booking b : bookings) {
                JSONObject obj = new JSONObject();
                obj.put("bookingId", b.getId());
                obj.put("serviceOfferId", b.getServiceOfferId());
                obj.put("customerId", b.getCustomerId());
                obj.put("amount", b.getAmount());
                obj.put("status", b.getStatus());
                obj.put("bookingDate", b.getBookingDate().toString());
                obj.put("customerUsername", b.getCustomerUsername());
                array.put(obj);
            }
            return Response.ok(array.toString()).build();
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error.toString()).build();
        }
    }

    // Get notifications by user
    @GET
    @Path("/notifications/{userId}")
    public Response getNotifications(@PathParam("userId") Long userId) {
        try {
            List<Notification> notifications = bookingBean.getNotificationsByUser(userId);
            JSONArray array = new JSONArray();
            for (Notification n : notifications) {
                JSONObject obj = new JSONObject();
                obj.put("id", n.getId());
                obj.put("userId", n.getUserId());
                obj.put("message", n.getMessage());
                obj.put("type", n.getType());
                obj.put("createdAt", n.getCreatedAt().toString());
                obj.put("isRead", n.getIsRead());
                array.put(obj);
            }
            return Response.ok(array.toString()).build();
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error.toString()).build();
        }
    }
}
