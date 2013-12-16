package org.neo4j.example.activity;

import org.codehaus.jackson.map.ObjectMapper;
import org.neo4j.graphdb.GraphDatabaseService;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.OutputStream;

@Path("/activities")
public class ActivityResource {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final ActivityStream stream;

    public ActivityResource(@Context GraphDatabaseService db, @Context UriInfo uriInfo) {
        stream = new ActivityStream(db);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{userName}")
    public Response getActivityStream(@PathParam("userName") final String userName,
                                      @QueryParam("count") final Integer count) {
        return Response.ok().entity(new StreamingOutput() {
            public void write(OutputStream out) throws IOException, WebApplicationException {
                OBJECT_MAPPER.writeValue(out, stream.loadStream(userName,count));
            }
        }).build();
    }
}