package com.marketplace.ejb;

import com.marketplace.entities.Booking;
import com.marketplace.entities.Notification;
import com.marketplace.entities.User;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.time.LocalDateTime;
import java.util.List;

@Stateless
public class BookingBean {

    @PersistenceContext(unitName = "UserWalletPU")
    private EntityManager em;

    @EJB
    private UserStatelessBean userBean;

    // Create a booking
    public Booking createBooking(Long customerId, Long serviceOfferId,
                                 Long providerId, double amount) {
        User customer = userBean.findById(customerId);
        User provider = userBean.findById(providerId);

        if (customer == null || provider == null) {
            throw new RuntimeException("Customer or Provider not found");
        }

        if (customer.getWalletBalance() < amount) {
            // Save rejection notification
            saveNotification(customerId, "Booking rejected: Insufficient balance", "BOOKING_REJECTED");
            throw new RuntimeException("Insufficient balance");
        }

        // Deduct wallet
        customer.setWalletBalance(customer.getWalletBalance() - amount);
        em.merge(customer);

        // Create booking
        Booking booking = new Booking();
        booking.setCustomerId(customerId);
        booking.setServiceOfferId(serviceOfferId);
        booking.setProviderId(providerId);
        booking.setAmount(amount);
        booking.setStatus("CONFIRMED");
        booking.setBookingDate(LocalDateTime.now());
        booking.setCustomerUsername(customer.getUsername());
        booking.setProviderUsername(provider.getUsername());
        em.persist(booking);

        // Save notifications for both parties
        saveNotification(customerId,
                "Booking confirmed! Service booked for $" + amount, "BOOKING_CONFIRMED");
        saveNotification(providerId,
                "New booking received from " + customer.getUsername() +
                        " for $" + amount, "BOOKING_CONFIRMED");

        return booking;
    }

    // Get all bookings (admin)
    public List<Booking> getAllBookings() {
        return em.createQuery("SELECT b FROM Booking b", Booking.class).getResultList();
    }

    // Get bookings by customer
    public List<Booking> getBookingsByCustomer(Long customerId) {
        TypedQuery<Booking> query = em.createQuery(
                "SELECT b FROM Booking b WHERE b.customerId = :customerId", Booking.class);
        query.setParameter("customerId", customerId);
        return query.getResultList();
    }

    // Get bookings by provider
    public List<Booking> getBookingsByProvider(Long providerId) {
        TypedQuery<Booking> query = em.createQuery(
                "SELECT b FROM Booking b WHERE b.providerId = :providerId", Booking.class);
        query.setParameter("providerId", providerId);
        return query.getResultList();
    }

    // Save notification
    public void saveNotification(Long userId, String message, String type) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setMessage(message);
        notification.setType(type);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setIsRead(false);
        em.persist(notification);
    }

    // Get notifications by user
    public List<Notification> getNotificationsByUser(Long userId) {
        TypedQuery<Notification> query = em.createQuery(
                "SELECT n FROM Notification n WHERE n.userId = :userId", Notification.class);
        query.setParameter("userId", userId);
        return query.getResultList();
    }
}
