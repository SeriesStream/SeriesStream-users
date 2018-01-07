package com.fri.series.stream;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.temporal.ChronoUnit;
import java.util.List;

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

@Produces(MediaType.APPLICATION_JSON)
@Path("users")
@Log
@ApplicationScoped
@CircuitBreaker
public class UserResource {

    private Logger log = LogManager.getLogger(UserResource.class.getName());

    @Inject
    @DiscoverService("series-stream-parcheese")
    private Provider<Optional<String>> parcheeseBaseProvider;
    //private String baseUrl;

    @Inject
    @DiscoverService("series-stream-raittings")
    private Provider<Optional<String>> raittingsBaseProvider;

    private Client httpClient = ClientBuilder.newClient();

    @GET
    public Response getAllUsers() {
        List<User> users = UsersDatabase.getUsers();
        return Response.ok(users).build();
    }

    @GET
    @Timed(name = "get_episodes_long_lasting")
    @Path("{id}/parcheeses")
    public Response getUsersParcheeses(@PathParam("id") int id) {
        User user = UsersDatabase.getUser(id);
        if(user != null){
            return processParchesdEpisodes(user.getId());
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
            return processRattedEpisodes(user.getId());
        } else {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
    }

    @CommandKey("find-episodes")
    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 2)
    @Fallback(fallbackMethod = "getUserdEpisodesFallback")
    private Response processParchesdEpisodes(int id) {
        Optional<String> baseUrl = parcheeseBaseProvider.get();
        if (baseUrl.isPresent()) {
            try {
                String link = baseUrl.get();
                System.out.println(link);
                return httpClient
                        .target(link + "/v1/parcheeses/user/" + id)
                        .request().get();
            } catch (WebApplicationException | ProcessingException e) {
                System.out.println("Error se je zgodil");
                log.error(e);
                System.out.println("Error se je zgodil");
                throw new InternalServerErrorException(e);
            }
        }
        log.error("baseUrl ni prisoten");
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @CommandKey("find-rattings")
    @Timeout(value = 1, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 2)
    @Fallback(fallbackMethod = "getUserdRattingsFallback")
    private Response processRattedEpisodes(int id) {
        Optional<String> baseUrl = raittingsBaseProvider.get();
        if (baseUrl.isPresent()) {
            try {
                String link = baseUrl.get();
                System.out.println(link);
                return httpClient
                        .target(link + "/v1/rattings/user/" + id)
                        .request().get();
            } catch (WebApplicationException | ProcessingException e) {
                System.out.println("Error se je zgodil");
                log.error(e);
                System.out.println("Error se je zgodil");
                throw new InternalServerErrorException(e);
            }
        }
        log.error("baseUrl ni prisoten");
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    public Response getUserdEpisodesFallback(int id){
        System.out.println("Napaka pri poizvedbi za epizodo z idijem" + id);
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    public Response getUserdRattingsFallback(int id){
        System.out.println("Napaka pri poizvedbi za ratting z userjem" + id);
        return Response.ok(Arrays.asList()).build();
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
        UsersDatabase.addUser(user);
        return Response.noContent().build();
    }

    @DELETE
    @Path("{id}")
    @Counted(name = "user_delete_counter")
    public Response deleteUser(@PathParam("id") int id) {
        UsersDatabase.deleteUser(id);
        return Response.noContent().build();
    }
}
