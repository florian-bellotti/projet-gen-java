package com.fbellotti.gen.remote.decodedFile;

import com.fbellotti.api_ws_spring.dao.QueryStringDao;
import com.fbellotti.api_ws_spring.remote.QueryStringServiceImpl;
import com.fbellotti.gen.model.DecodedFile;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;


/**
 * @author <a href="http://fbellotti.com">Florian BELLOTTI</a>
 */
@Component
public class DecodedFileServiceImpl extends QueryStringServiceImpl<DecodedFile> implements DecodedFileService {

  public DecodedFileServiceImpl(QueryStringDao<DecodedFile> daoRef) {
    super(daoRef);
  }

  @Override
  public Response first(UriInfo info) {
    return Response.status(400).build();
  }
}
