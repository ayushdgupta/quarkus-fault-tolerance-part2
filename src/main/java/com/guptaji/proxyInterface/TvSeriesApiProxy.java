package com.guptaji.proxyInterface;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


@RegisterRestClient(baseUri = "http://localhost:8082/tvseries")
public interface TvSeriesApiProxy {

    @GET
    @Path("/faultToleranceTesting/{countryName}")
    @Produces(MediaType.TEXT_PLAIN)
    Response getTvSeriesCountByCountry(@PathParam("countryName") String countryName);
}
