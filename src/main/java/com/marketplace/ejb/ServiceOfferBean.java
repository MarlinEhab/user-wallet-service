package com.marketplace.ejb;

import com.marketplace.entities.ServiceCategory;
import com.marketplace.entities.ServiceOffer;
import com.marketplace.entities.User;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.time.LocalDate;
import java.util.List;

@Stateless
public class ServiceOfferBean {

    @PersistenceContext(unitName = "UserWalletPU")
    private EntityManager em;

    @EJB
    private UserStatelessBean userBean;

    // Admin: Add new service category
    public ServiceCategory addCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Category name is required");
        }

        ServiceCategory category = new ServiceCategory();
        category.setName(name);

        em.persist(category);
        return category;
    }

    // View all categories
    public List<ServiceCategory> getAllCategories() {
        return em.createQuery(
                "SELECT c FROM ServiceCategory c ORDER BY c.name",
                ServiceCategory.class
        ).getResultList();
    }

    // Provider: Create service offer
    public ServiceOffer createOffer(Long providerId, String category, String description,
                                    Double price, LocalDate availableDate) {

        User provider = userBean.findById(providerId);

        if (provider == null) {
            throw new RuntimeException("Provider not found");
        }

        if (!"PROVIDER".equalsIgnoreCase(provider.getRole())) {
            throw new RuntimeException("Only providers can create service offers");
        }

        if (price == null || price <= 0) {
            throw new RuntimeException("Price must be greater than zero");
        }

        ServiceOffer offer = new ServiceOffer();
        offer.setProviderId(providerId);
        offer.setProviderUsername(provider.getUsername());
        offer.setCategory(category);
        offer.setDescription(description);
        offer.setPrice(price);
        offer.setAvailableDate(availableDate);
        offer.setActive(true);

        em.persist(offer);
        return offer;
    }

    // View all active service offers
    public List<ServiceOffer> getActiveOffers() {
        return em.createQuery(
                "SELECT o FROM ServiceOffer o WHERE o.active = true ORDER BY o.availableDate",
                ServiceOffer.class
        ).getResultList();
    }

    // Customer: Browse services by category
    public List<ServiceOffer> getOffersByCategory(String category) {
        TypedQuery<ServiceOffer> query = em.createQuery(
                "SELECT o FROM ServiceOffer o WHERE LOWER(o.category) = LOWER(:category) AND o.active = true ORDER BY o.availableDate",
                ServiceOffer.class
        );

        query.setParameter("category", category);
        return query.getResultList();
    }

    // Provider: View their offers
    public List<ServiceOffer> getOffersByProvider(Long providerId) {
        TypedQuery<ServiceOffer> query = em.createQuery(
                "SELECT o FROM ServiceOffer o WHERE o.providerId = :providerId ORDER BY o.availableDate",
                ServiceOffer.class
        );

        query.setParameter("providerId", providerId);
        return query.getResultList();
    }

    // Provider: Update pricing and availability
    public ServiceOffer updateOffer(Long offerId, Double price, LocalDate availableDate, Boolean active) {
        ServiceOffer offer = em.find(ServiceOffer.class, offerId);

        if (offer == null) {
            throw new RuntimeException("Service offer not found");
        }

        if (price != null) {
            if (price <= 0) {
                throw new RuntimeException("Price must be greater than zero");
            }
            offer.setPrice(price);
        }

        if (availableDate != null) {
            offer.setAvailableDate(availableDate);
        }

        if (active != null) {
            offer.setActive(active);
        }

        em.merge(offer);
        return offer;
    }

    public ServiceOffer getOfferById(Long offerId) {
        ServiceOffer offer = em.find(ServiceOffer.class, offerId);

        if (offer == null) {
            throw new RuntimeException("Service offer not found");
        }

        return offer;
    }
}