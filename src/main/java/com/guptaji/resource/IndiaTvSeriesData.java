package com.guptaji.resource;

import com.guptaji.proxyInterface.TvSeriesApiProxy;
import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logmanager.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.time.temporal.ChronoUnit;

@Path("/tvSeriesDataCount")
public class IndiaTvSeriesData {

    public static final Logger LOGGER =Logger.getLogger(String.valueOf(IndiaTvSeriesData.class));

    @RestClient
    TvSeriesApiProxy tvSeriesApiProxy;

    // important links for fault-tolerance https://quarkus.io/guides/smallrye-fault-tolerance

    // with @Retry what will happen when we want to call our API and if the response will not come then after that
    // our API will try to call other API three more time i.e. LOGGER inside our API will call 4 times ->
    // 1 normal execution + 3 Retries but if for all retries as well we will not get any output then in the
    // last i.e.after 3 retries fallback method will be hit.

    // in @timeout we've provided 1000 ms i.e. 1 sec wait time for our API so if the response will not come
    // in the given time then Retry will be there for 3 times then fallback. but if Retry will not be there then
    // after 1 sec. only fallback method will execute. but if fallback was also not there then

    @GET
    @Fallback(fallbackMethod = "getTvSeriesCountByCountryFallbackMethod")
    @Retry(maxRetries = 3)
    @Timeout(1000)
    @Path("/{countryName}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getTvSeriesCountByCountry(@PathParam("countryName") String countryName){
        LOGGER.info("TvSeriesDataByCountry API is called");
        
        Long startTime = System.currentTimeMillis();
        Response response = tvSeriesApiProxy.getTvSeriesCountByCountry(countryName);
//        try {
//            Response response = tvSeriesApiProxy.getTvSeriesCountByCountry(countryName);
//        } catch (Exception e){
//            LOGGER.info("Exceptions is handled that's why log get printed for the case" +
//                    "when retry and fallback has been removed only timeout is there");
//        }
//        LOGGER.info("this log is for testing timeout fault tolerance if both fallback and retry will " +
//                "be removed then will this log print??? so answer is - YES and some " +
//                "org.eclipse.microprofile.faulttolerance.exceptions.TimeoutException" +
//                "are also coming but i am not sure on that part yet.");
        Long endTime = System.currentTimeMillis();

        String resp = response.readEntity(String.class);

        return Response.ok(resp + " Time taken by API is "+ (endTime - startTime)/ 1000).build();
//        return null;
    }

    public Response getTvSeriesCountByCountryFallbackMethod(String countryName){
        LOGGER.info("TvSeriesDataByCountry API method's fallback is called");
        return Response.ok("Site is under maintenance so NIKALLE Yaha se Pehli Fursat mai").build();
    }

    // @CircuitBreaker demo

    // Here we have stopped our API that we are calling from our API to test our annotation, here what will
    // happen our code will 4 time hits the API and then if someone will try 5th time then instead of calling
    // our API, CircuitBreaker will simple call fallback method instead of calling the API. Now CircuitBreaker
    // will wait till 100 seconds once those 100 secs will be over then after that if someone will hit our
    // API then only circuitBreaker allow the API Calling. See Below logs for understanding.
    /*
2023-03-15 22:21:35,402 INFO  [cla.gup.res.IndiaTvSeriesData] (executor-thread-0) TvSeriesDataByCountry CKT Breaker API is called
2023-03-15 22:21:39,531 INFO  [cla.gup.res.IndiaTvSeriesData] (executor-thread-0) TvSeriesDataByCountry API method's fallback2 is called
2023-03-15 22:21:41,608 INFO  [cla.gup.res.IndiaTvSeriesData] (executor-thread-0) TvSeriesDataByCountry CKT Breaker API is called
2023-03-15 22:21:45,729 INFO  [cla.gup.res.IndiaTvSeriesData] (executor-thread-0) TvSeriesDataByCountry API method's fallback2 is called
2023-03-15 22:21:47,655 INFO  [cla.gup.res.IndiaTvSeriesData] (executor-thread-0) TvSeriesDataByCountry CKT Breaker API is called
2023-03-15 22:21:51,766 INFO  [cla.gup.res.IndiaTvSeriesData] (executor-thread-0) TvSeriesDataByCountry API method's fallback2 is called
2023-03-15 22:21:56,510 INFO  [cla.gup.res.IndiaTvSeriesData] (executor-thread-0) TvSeriesDataByCountry CKT Breaker API is called
2023-03-15 22:22:00,653 INFO  [cla.gup.res.IndiaTvSeriesData] (executor-thread-0) TvSeriesDataByCountry API method's fallback2 is called
2023-03-15 22:22:03,973 INFO  [cla.gup.res.IndiaTvSeriesData] (executor-thread-0) TvSeriesDataByCountry API method's fallback2 is called
2023-03-15 22:22:13,173 INFO  [cla.gup.res.IndiaTvSeriesData] (executor-thread-0) TvSeriesDataByCountry API method's fallback2 is called
2023-03-15 22:22:20,766 INFO  [cla.gup.res.IndiaTvSeriesData] (executor-thread-0) TvSeriesDataByCountry API method's fallback2 is called
2023-03-15 22:22:23,373 INFO  [cla.gup.res.IndiaTvSeriesData] (executor-thread-0) TvSeriesDataByCountry API method's fallback2 is called

     */

    @GET
    @Fallback(fallbackMethod = "getTvSeriesCountByCountryFallbackMethodCkt")
    @CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 100, delayUnit = ChronoUnit.SECONDS)
    @Path("cktBreaker/{countryName}")
    @Produces(MediaType.TEXT_PLAIN)
    public Response getTvSeriesCountByCountryByCkt(@PathParam("countryName") String countryName){
        LOGGER.info("TvSeriesDataByCountry CKT Breaker API is called");

        Response response = tvSeriesApiProxy.getTvSeriesCountByCountry(countryName);

        String resp = response.readEntity(String.class);

        return Response.ok(resp + " Time taken by API is ").build();
//        return null;
    }

    public Response getTvSeriesCountByCountryFallbackMethodCkt(String countryName){
        LOGGER.info("TvSeriesDataByCountry API method's fallback2 is called");
        return Response.ok("Site is under maintenance so NIKALLE Yaha se Pehli Fursat mai").build();
    }
}
