package com.fbellotti.gen.remote.dictionary;

import com.fbellotti.api_ws_spring.remote.CrudService;
import com.fbellotti.gen.model.Word;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * @author <a href="http://fbellotti.com">Florian BELLOTTI</a>
 */
@Path("/words")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public interface DictionaryService extends CrudService<Word> {

  @GET
  @Path("/")
  Response advandedResearch(@QueryParam("contains") String contains);
}
