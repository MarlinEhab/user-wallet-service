package com.marketplace.rest;

import com.marketplace.ejb.UserStatelessBean;
import com.marketplace.ejb.WalletStatefulBean;
import com.marketplace.entities.User;
import jakarta.ejb.EJB;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.json.JSONArray;
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

    @POST
    @Path("/register/customer")
    public Response registerCustomer(String body) {
        try {
            JSONObject json = new JSONObject(body);

            String username = json.getString("username");
            String password = json.getString("password");
            double balance = json.optDouble("balance", 0.0);

            User user = userBean.registerCustomer(username, password, balance);

            JSONObject response = userToJson(user);
            response.put("message", "Customer registered successfully");

            return Response.status(Response.Status.CREATED)
                    .entity(response.toString())
                    .build();

        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @POST
    @Path("/register/provider")
    public Response registerProvider(String body) {
        try {
            JSONObject json = new JSONObject(body);

            String username = json.getString("username");
            String password = json.getString("password");
            String professionType = json.getString("professionType");

            User user = userBean.registerProvider(username, password, professionType);

            JSONObject response = userToJson(user);
            response.put("message", "Provider registered successfully");

            return Response.status(Response.Status.CREATED)
                    .entity(response.toString())
                    .build();

        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @POST
    @Path("/register/admin")
    public Response registerAdmin(String body) {
        try {
            JSONObject json = new JSONObject(body);

            String username = json.getString("username");
            String password = json.getString("password");

            User user = userBean.registerAdmin(username, password);

            JSONObject response = userToJson(user);
            response.put("message", "Admin registered successfully");

            return Response.status(Response.Status.CREATED)
                    .entity(response.toString())
                    .build();

        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @POST
    @Path("/login")
    public Response login(String body) {
        try {
            JSONObject json = new JSONObject(body);

            String username = json.getString("username");
            String password = json.getString("password");

            User user = userBean.login(username, password);

            JSONObject response = userToJson(user);
            response.put("message", "Login successful");

            return Response.ok(response.toString()).build();

        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(error.toString())
                    .build();
        }
    }

    @GET
    @Path("/all")
    public Response getAllUsers() {
        try {
            List<User> users = userBean.getAllUsers();

            JSONArray array = new JSONArray();

            for (User user : users) {
                array.put(userToJson(user));
            }

            return Response.ok(array.toString()).build();

        } catch (Exception e) {
            return serverError(e.getMessage());
        }
    }

    @GET
    @Path("/{id}")
    public Response getUserById(@PathParam("id") Long id) {
        try {
            User user = userBean.findById(id);

            if (user == null) {
                return badRequest("User not found");
            }

            return Response.ok(userToJson(user).toString()).build();

        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

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
            return badRequest(e.getMessage());
        }
    }

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
            response.put("addedAmount", amount);
            response.put("newBalance", newBalance);
            response.put("message", "Funds added successfully");

            return Response.ok(response.toString()).build();

        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @POST
    @Path("/{id}/wallet/check")
    public Response checkBalance(@PathParam("id") Long id, String body) {
        try {
            JSONObject json = new JSONObject(body);

            double amount = json.getDouble("amount");

            walletBean.initSession(id);

            boolean sufficient = walletBean.hasSufficientBalance(amount);
            Double currentBalance = walletBean.getBalance();

            walletBean.endSession();

            JSONObject response = new JSONObject();
            response.put("userId", id);
            response.put("requiredAmount", amount);
            response.put("currentBalance", currentBalance);
            response.put("sufficient", sufficient);

            return Response.ok(response.toString()).build();

        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    @POST
    @Path("/{id}/wallet/deduct")
    public Response deductFunds(@PathParam("id") Long id, String body) {
        try {
            JSONObject json = new JSONObject(body);

            double amount = json.getDouble("amount");

            walletBean.initSession(id);

            Double newBalance = walletBean.deductFunds(amount);

            walletBean.endSession();

            JSONObject response = new JSONObject();
            response.put("userId", id);
            response.put("deductedAmount", amount);
            response.put("newBalance", newBalance);
            response.put("message", "Funds deducted successfully");

            return Response.ok(response.toString()).build();

        } catch (Exception e) {
            JSONObject error = new JSONObject();
            error.put("error", e.getMessage());
            error.put("status", "PAYMENT_FAILED");

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error.toString())
                    .build();
        }
    }

    @POST
    @Path("/{id}/wallet/refund")
    public Response refundFunds(@PathParam("id") Long id, String body) {
        try {
            JSONObject json = new JSONObject(body);

            double amount = json.getDouble("amount");

            walletBean.initSession(id);

            Double newBalance = walletBean.refundFunds(amount);

            walletBean.endSession();

            JSONObject response = new JSONObject();
            response.put("userId", id);
            response.put("refundedAmount", amount);
            response.put("newBalance", newBalance);
            response.put("message", "Funds refunded successfully");

            return Response.ok(response.toString()).build();

        } catch (Exception e) {
            return badRequest(e.getMessage());
        }
    }

    private JSONObject userToJson(User user) {
        JSONObject obj = new JSONObject();

        obj.put("id", user.getId());
        obj.put("username", user.getUsername());
        obj.put("role", user.getRole());
        obj.put("professionType", user.getProfessionType() != null ? user.getProfessionType() : "");
        obj.put("walletBalance", user.getWalletBalance() != null ? user.getWalletBalance() : 0.0);

        return obj;
    }

    private Response badRequest(String message) {
        JSONObject error = new JSONObject();
        error.put("error", message);

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error.toString())
                .build();
    }

    private Response serverError(String message) {
        JSONObject error = new JSONObject();
        error.put("error", message);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error.toString())
                .build();
    }
}