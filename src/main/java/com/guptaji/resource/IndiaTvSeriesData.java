package com.guptaji.resource;

import com.guptaji.proxyInterface.TvSeriesApiProxy;
import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.logmanager.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/tvSeriesDataCount")
public class IndiaTvSeriesData {

    public static final Logger LOGGER =Logger.getLogger(String.valueOf(IndiaTvSeriesData.class));

    @RestClient
    TvSeriesApiProxy tvSeriesApiProxy;

    // with @Retry what will happen when we want to call our API and if the response will not come then after that
    // our API will try to call other API three more time i.e. LOGGER inside our API will call 4 times ->
    // 1 normal execution + 3 Retries but if for all retries as well we will not get any output then in the
    // last i.e.after 3 retries fallback method will be hit.

    // in @timeout we've provided 1000 ms i.e. 1 sec wait time for our API so if the response will not come
    // in the given time then Retry will be there for 3 times then fallback.

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
        Long endTime = System.currentTimeMillis();

        String resp = response.readEntity(String.class);

        return Response.ok(resp + " Time taken by API is "+ (endTime - startTime)/ 1000).build();
    }

    public Response getTvSeriesCountByCountryFallbackMethod(String countryName){
        LOGGER.info("TvSeriesDataByCountry API method's fallback is called");
        return Response.ok("Site is under maintenance so NIKALLE Yaha se Pehli Fursat mai").build();
    }
}
