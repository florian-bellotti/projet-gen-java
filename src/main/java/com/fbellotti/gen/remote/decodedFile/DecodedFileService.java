package com.fbellotti.gen.remote.decodedFile;

import com.fbellotti.api_ws_spring.remote.QueryStringService;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author <a href="http://fbellotti.com">Florian BELLOTTI</a>
 */
@Path("/decodedFiles")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface DecodedFileService extends QueryStringService {
}
