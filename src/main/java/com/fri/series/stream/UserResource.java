package com.fri.series.stream;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.temporal.ChronoUnit;
import java.util.List;

import com.kumuluz.ee.configuration.utils.ConfigurationUtil;
import com.kumuluz.ee.fault.tolerance.annotations.CommandKey;
import com.kumuluz.ee.logs.cdi.Log;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import com.kumuluz.ee.discovery.annotations.DiscoverService;
import javax.inject.Inject;
import java.util.Optional;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.inject.Provider;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Timed;
import java.util.Arrays;
import com.kumuluz.ee.configuration.cdi.ConfigBundle;
import com.kumuluz.ee.configuration.cdi.ConfigValue;

@Produces(MediaType.APPLICATION_JSON)
@Path("users")
@Log
@ApplicationScoped
@ConfigBundle("rest-config")
public class UserResource {

    @Inject
    private UserBean userBean;

    @Inject
    private UserResource userResource;

    @ConfigValue(value = "login-required", watch = true)
    private Boolean loginRequired;

    @ConfigValue(value = "password-min-length", watch = true)
    private int passwordMinLength;

    public Boolean getLoginRequired() {
        return loginRequired;
    }

    public void setLoginRequired(Boolean loginRequired) {
        System.out.println("Spreminjam vrednost loginRequired na " + loginRequired);
        this.loginRequired = loginRequired;
    }

    public int getPasswordMinLength() {
        return passwordMinLength;
    }

    public void setPasswordMinLength(int passwordMinLength) {
        System.out.println("Spreminjam vrednost minLength na " + passwordMinLength);
        this.passwordMinLength = passwordMinLength;
    }

    private Logger log = LogManager.getLogger(UserResource.class.getName());

    @Inject
    @DiscoverService("series-stream-parcheese")
    private Provider<Optional<String>> parcheeseBaseProvider;
    //private String baseUrl;

    @Inject
    @DiscoverService("series-stream-rattings")
    private Provider<Optional<String>> raittingsBaseProvider;

    @GET
    public Response getAllUsers() {
        System.out.println("Show: " + this.loginRequired);
        System.out.println("PML: " + this.passwordMinLength);

        if(this.loginRequired != true) {
            List<User> users = UsersDatabase.getUsers();
            return Response.ok(users).build();
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
    }

    @GET
    @Timed(name = "get_episodes_long_lasting")
    @Path("{id}/parcheeses")
    public Response getUsersParcheeses(@PathParam("id") int id) {
        User user = UsersDatabase.getUser(id);
        if(user != null){
            List<Parcheese> returned = userBean.processParchesdEpisodes(user.getId());
            System.out.println("Vracam returned");
            return Response.ok(returned).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Timed(name = "get_rattings_long_lasting")
    @Path("{id}/rattings")
    public Response getUsersRattings(@PathParam("id") int id) {
        User user = UsersDatabase.getUser(id);
        if(user != null){
            return userBean.processRattedEpisodes(user.getId());
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @GET
    @Path("{id}")
    @Counted(name = "user_counter")
    public Response getUser(@PathParam("id") int id) {
        User user = UsersDatabase.getUser(id);
        if(user != null){
            return Response.ok(user).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @POST
    @Counted(name = "registration_counter")
    public Response addUser(User user) {
        if(this.passwordMinLength > user.getPassword().length()){
            return Response.ok("Geslo ni ustrezno").build();
        } else  {
            UsersDatabase.addUser(user);
            return Response.noContent().build();
        }
    }

    @POST
    @Path("login")
    @Counted(name = "login_counter")
    public Response login(User user) {
        return Response.ok("TOKEN").build();
    }

    @DELETE
    @Path("{id}")
    @Counted(name = "user_delete_counter")
    public Response deleteUser(@PathParam("id") int id) {
        UsersDatabase.deleteUser(id);
        return Response.noContent().build();
    }
}
