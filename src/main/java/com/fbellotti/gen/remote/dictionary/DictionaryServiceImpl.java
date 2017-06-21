package com.fbellotti.gen.remote.dictionary;

import com.fbellotti.api_ws_spring.dao.CrudDao;
import com.fbellotti.api_ws_spring.model.ErrorRemoteResponse;
import com.fbellotti.api_ws_spring.remote.CrudServiceImpl;
import com.fbellotti.gen.dao.DictionaryDao;
import com.fbellotti.gen.model.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * @author <a href="http://fbellotti.com">Florian BELLOTTI</a>
 */
@Component
public class DictionaryServiceImpl extends CrudServiceImpl<Word> implements DictionaryService {

  private static final Logger LOG = LoggerFactory.getLogger(DictionaryServiceImpl.class);
  private DictionaryDao dictionaryDao;
  private ErrorRemoteResponse executionError;

  @Autowired
  public DictionaryServiceImpl(CrudDao<Word> crudDao, DictionaryDao dictionaryDao) {
    super(crudDao);
    this.dictionaryDao = dictionaryDao;
    this.executionError = new ErrorRemoteResponse("EXECUTION_ERROR", "Failed during the request execution");
  }

  public Response advandedResearch(String contains) {
    if(contains == null) {
      ErrorRemoteResponse error = new ErrorRemoteResponse("NULL_CONTAINS", "Contains is null");
      return Response.status(400).entity(error).build();
    }

    try {
      List<Word> words = this.dictionaryDao.labelContains(contains);
      GenericEntity<List<Word>> genericList = new GenericEntity<List<Word>>(words) {};
      return Response.status(200).entity(genericList).build();
    } catch (Exception e) {
      LOG.error("ERROR during advandedResearch execution : " + e);
      return Response.status(500).entity(this.executionError).build();
    }
  }
}
