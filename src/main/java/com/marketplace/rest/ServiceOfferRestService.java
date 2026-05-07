package com.marketplace.rest;

import com.marketplace.ejb.ServiceOfferBean;
import com.marketplace.entities.ServiceCategory;
import com.marketplace.entities.ServiceOffer;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.List;

@Path("/services")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ServiceOfferRestService {

    @EJB
    private ServiceOfferBean serviceOfferBean;

    // Admin: Add service category
    @POST
    @Path("/categories/add")
    public Response addCategory(String body) {
        try {
            JSONObject json = new JSONObject(body);
            String name = json.getString("name");

            ServiceCategory category = serviceOfferBean.addCategory(name);

            JSONObject response = new JSONObject();
            response.put("id", category.getId());
            response.put("name", category.getName());
            response.put("message", "Service category added successfully");

            return Response.status(Response.Status.CREATED).entity(response.toString()).build();

        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    // View all categories
    @GET
    @Path("/categories")
    public Response getAllCategories() {
        try {
            List<ServiceCategory> categories = serviceOfferBean.getAllCategories();
            JSONArray array = new JSONArray();

            for (ServiceCategory c : categories) {
                JSONObject obj = new JSONObject();
                obj.put("id", c.getId());
                obj.put("name", c.getName());
                array.put(obj);
            }

            return Response.ok(array.toString()).build();

        } catch (Exception e) {
            return serverError(e.getMessage());
        }
    }

    // Provider: Create service offer
    @POST
    @Path("/offers/create")
    public Response createOffer(String body) {
        try {
            JSONObject json = new JSONObject(body);

            Long providerId = json.getLong("providerId");
            String category = json.getString("category");
            String description = json.optString("description", "");
            Double price = json.getDouble("price");
            LocalDate availableDate = LocalDate.parse(json.getString("availableDate"));

            ServiceOffer offer = serviceOfferBean.createOffer(
                    providerId,
                    category,
                    description,
                    price,
                    availableDate
            );

            JSONObject response = offerToJson(offer);
            response.put("message", "Service offer created successfully");

            return Response.status(Response.Status.CREATED).entity(response.toString()).build();

        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    // View all active service offers
    @GET
    @Path("/offers/active")
    public Response getActiveOffers() {
        try {
            List<ServiceOffer> offers = serviceOfferBean.getActiveOffers();
            return Response.ok(offersToJsonArray(offers).toString()).build();

        } catch (Exception e) {
            return serverError(e.getMessage());
        }
    }

    // Browse offers by category
    @GET
    @Path("/offers/category/{category}")
    public Response getOffersByCategory(@PathParam("category") String category) {
        try {
            List<ServiceOffer> offers = serviceOfferBean.getOffersByCategory(category);
            return Response.ok(offersToJsonArray(offers).toString()).build();

        } catch (Exception e) {
            return serverError(e.getMessage());
        }
    }

    // Provider: View own offers
    @GET
    @Path("/offers/provider/{providerId}")
    public Response getOffersByProvider(@PathParam("providerId") Long providerId) {
        try {
            List<ServiceOffer> offers = serviceOfferBean.getOffersByProvider(providerId);
            return Response.ok(offersToJsonArray(offers).toString()).build();

        } catch (Exception e) {
            return serverError(e.getMessage());
        }
    }

    // Provider: Update price/date/active
    @PUT
    @Path("/offers/{offerId}/update")
    public Response updateOffer(@PathParam("offerId") Long offerId, String body) {
        try {
            JSONObject json = new JSONObject(body);

            Double price = json.has("price") ? json.getDouble("price") : null;
            LocalDate availableDate = json.has("availableDate")
                    ? LocalDate.parse(json.getString("availableDate"))
                    : null;
            Boolean active = json.has("active") ? json.getBoolean("active") : null;

            ServiceOffer offer = serviceOfferBean.updateOffer(offerId, price, availableDate, active);

            JSONObject response = offerToJson(offer);
            response.put("message", "Service offer updated successfully");

            return Response.ok(response.toString()).build();

        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    // View one offer
    @GET
    @Path("/offers/{offerId}")
    public Response getOfferById(@PathParam("offerId") Long offerId) {
        try {
            ServiceOffer offer = serviceOfferBean.getOfferById(offerId);
            return Response.ok(offerToJson(offer).toString()).build();

        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    private JSONArray offersToJsonArray(List<ServiceOffer> offers) {
        JSONArray array = new JSONArray();

        for (ServiceOffer offer : offers) {
            array.put(offerToJson(offer));
        }

        return array;
    }

    private JSONObject offerToJson(ServiceOffer offer) {
        JSONObject obj = new JSONObject();

        obj.put("id", offer.getId());
        obj.put("providerId", offer.getProviderId());
        obj.put("providerUsername", offer.getProviderUsername());
        obj.put("category", offer.getCategory());
        obj.put("description", offer.getDescription());
        obj.put("price", offer.getPrice());
        obj.put("availableDate", offer.getAvailableDate() != null ? offer.getAvailableDate().toString() : "");
        obj.put("active", offer.getActive());

        return obj;
    }

    private Response badRequest(String message) {
        JSONObject error = new JSONObject();
        error.put("error", message);

        return Response.status(Response.Status.BAD_REQUEST).entity(error.toString()).build();
    }

    private Response serverError(String message) {
        JSONObject error = new JSONObject();
        error.put("error", message);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error.toString()).build();
    }
}