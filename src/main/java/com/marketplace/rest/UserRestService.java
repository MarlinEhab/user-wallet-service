package com.marketplace.rest;

import com.marketplace.ejb.UserStatelessBean;
import com.marketplace.ejb.WalletStatefulBean;
import com.marketplace.entities.User;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONObject;
import java.util.List;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserRestService {

    @EJB
    private UserStatelessBean userBean;

    @EJB
    private WalletStatefulBean walletBean;

    // Register Customer
    @POST
    @Path("/register/customer")
    public Response registerCustomer(String body) {
        try {
            JSONObject json = new JSONObject(body);
            String username = json.getString("username");
            String password = json.getString("password");
            double balance = json.optDouble("balance", 0.0);

            User user = userBean.registerCustomer(username, password, balance);

            JSONObject response = new JSONObject();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("walletBalance", user.getWalletBalance());
            response.put("message", "Customer registered successfully");
            return Response.status(Response.Status.CREATED).entity(response.toString()).build();
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error.toString()).build();
        }
    }

    // Register Provider
    @POST
    @Path("/register/provider")
    public Response registerProvider(String body) {
        try {
            JSONObject json = new JSONObject(body);
            String username = json.getString("username");
            String password = json.getString("password");
            String professionType = json.getString("professionType");

            User user = userBean.registerProvider(username, password, professionType);

            JSONObject response = new JSONObject();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("professionType", user.getProfessionType());
            response.put("message", "Provider registered successfully");
            return Response.status(Response.Status.CREATED).entity(response.toString()).build();
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error.toString()).build();
        }
    }

    // Login
    @POST
    @Path("/login")
    public Response login(String body) {
        try {
            JSONObject json = new JSONObject(body);
            String username = json.getString("username");
            String password = json.getString("password");

            User user = userBean.login(username, password);

            JSONObject response = new JSONObject();
            response.put("id", user.getId());
            response.put("username", user.getUsername());
            response.put("role", user.getRole());
            response.put("professionType", user.getProfessionType() != null ? user.getProfessionType() : "");
            response.put("walletBalance", user.getWalletBalance() != null ? user.getWalletBalance() : 0.0);
            response.put("message", "Login successful");
            return Response.ok(response.toString()).build();
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.UNAUTHORIZED).entity(error.toString()).build();
        }
    }

    // Get all users (Admin)
    @GET
    @Path("/all")
    public Response getAllUsers() {
        try {
            List<User> users = userBean.getAllUsers();
            org.json.JSONArray array = new org.json.JSONArray();
            for (User u : users) {
                JSONObject obj = new JSONObject();
                obj.put("id", u.getId());
                obj.put("username", u.getUsername());
                obj.put("role", u.getRole());
                obj.put("professionType", u.getProfessionType() != null ? u.getProfessionType() : "");
                obj.put("walletBalance", u.getWalletBalance() != null ? u.getWalletBalance() : 0.0);
                array.put(obj);
            }
            return Response.ok(array.toString()).build();
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(error.toString()).build();
        }
    }

    // Get wallet balance
    @GET
    @Path("/{id}/wallet")
    public Response getWalletBalance(@PathParam("id") Long id) {
        try {
            walletBean.initSession(id);
            Double balance = walletBean.getBalance();
            walletBean.endSession();

            JSONObject response = new JSONObject();
            response.put("userId", id);
            response.put("balance", balance);
            return Response.ok(response.toString()).build();
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error.toString()).build();
        }
    }

    // Add funds to wallet
    @POST
    @Path("/{id}/wallet/add")
    public Response addFunds(@PathParam("id") Long id, String body) {
        try {
            JSONObject json = new JSONObject(body);
            double amount = json.getDouble("amount");

            walletBean.initSession(id);
            Double newBalance = walletBean.addFunds(amount);
            walletBean.endSession();

            JSONObject response = new JSONObject();
            response.put("userId", id);
            response.put("newBalance", newBalance);
            response.put("message", "Funds added successfully");
            return Response.ok(response.toString()).build();
        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(error.toString()).build();
        }
    }
}
