package com.fri.series.stream;

import com.kumuluz.ee.discovery.annotations.DiscoverService;
import com.kumuluz.ee.fault.tolerance.annotations.CommandKey;
import com.kumuluz.ee.fault.tolerance.annotations.GroupKey;
import com.kumuluz.ee.logs.LogManager;
import com.kumuluz.ee.logs.Logger;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Timeout;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
@GroupKey("parcheeses")
public class UserBean {

    @Inject
    @DiscoverService("series-stream-parcheese")
    private Provider<Optional<String>> parcheeseBaseProvider;
    //private String baseUrl;

    @Inject
    @DiscoverService("series-stream-rattings")
    private Provider<Optional<String>> raittingsBaseProvider;

    private Client httpClient = ClientBuilder.newClient();

    private Logger log = LogManager.getLogger(UserResource.class.getName());

    @Timeout(value = 2, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 2)
    @Fallback(fallbackMethod = "getUserdEpisodesFallback")
    @CommandKey("find-episodes")
    public List<Parcheese> processParchesdEpisodes(int id) {
        Optional<String> baseUrl = parcheeseBaseProvider.get();
        if (baseUrl.isPresent()) {
            try {
                String link = baseUrl.get();
                System.out.println(link);
                return httpClient
                        .target(link + "/v1/parcheeses/user/" + id)
                        .request().get(new GenericType<List<Parcheese>>() {
                        });
            } catch (WebApplicationException | ProcessingException e) {
                System.out.println("Error se je zgodil");
                log.error(e);
                System.out.println("Error se je zgodil");
                throw new InternalServerErrorException(e);
            }
        }
        log.error("baseUrl ni prisoten");
        throw new InternalServerErrorException();
    }

    @CommandKey("find-rattings")
    @Timeout(value = 1, unit = ChronoUnit.SECONDS)
    @CircuitBreaker(requestVolumeThreshold = 2)
    @Fallback(fallbackMethod = "getUserdRattingsFallback")
    public Response processRattedEpisodes(int id) {
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

    public List<Parcheese> getUserdEpisodesFallback(int id){
        System.out.println("Napaka pri poizvedbi za epizodo z idijem" + id);
        return Arrays.asList();
    }

    public Response getUserdRattingsFallback(int id){
        System.out.println("Napaka pri poizvedbi za ratting z userjem" + id);
        return Response.ok(Arrays.asList()).build();
    }

}
