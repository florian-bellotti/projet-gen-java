package com.fbellotti.gen.remote.dictionary;

import com.fbellotti.api_ws_spring.dao.CrudDao;
import com.fbellotti.api_ws_spring.model.ErrorRemoteResponse;
import com.fbellotti.api_ws_spring.remote.CrudServiceImpl;
import com.fbellotti.gen.dao.DictionaryDao;
import com.fbellotti.gen.model.Words;
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
  private Words words;

  @Autowired
  public DictionaryServiceImpl(CrudDao<Word> crudDao, DictionaryDao dictionaryDao, Words words) {
    super(crudDao);
    this.dictionaryDao = dictionaryDao;
    this.words = words;
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

  @Override
  public Response create(Word item) {
    if(item == null) {
      ErrorRemoteResponse error = new ErrorRemoteResponse("NULL_ITEM", "The item is null");
      GenericEntity<ErrorRemoteResponse> genericError = new GenericEntity<ErrorRemoteResponse>(error) {
      };
      return Response.status(400).entity(genericError).build();
    } else {
      try {
        item = this.dictionaryDao.create(item);
        words.setWords(this.dictionaryDao.findAll());
        return Response.status(201).entity(item).build();
      } catch (Exception var5) {
        LOG.error("Tried to insert item : " + item.toString() + var5);
        return Response.status(500).entity(this.executionError).build();
      }
    }
  }

  @Override
  public Response update(String id, Word item) {
    ErrorRemoteResponse error;
    GenericEntity genericError;
    if(id == null) {
      error = new ErrorRemoteResponse("NULL_ID", "The id is null");
      genericError = new GenericEntity<ErrorRemoteResponse>(error) {
      };
      return Response.status(400).entity(genericError).build();
    } else if(item == null) {
      error = new ErrorRemoteResponse("NULL_ITEM", "The item is null");
      genericError = new GenericEntity<ErrorRemoteResponse>(error) {
      };
      return Response.status(400).entity(genericError).build();
    } else {
      try {
        this.dictionaryDao.update(id, item);
        words.setWords(this.dictionaryDao.findAll());
        return Response.status(204).build();
      } catch (Exception var6) {
        return Response.status(500).entity(this.executionError).build();
      }
    }
  }

  @Override
  public Response delete(String id) {
    if(id == null) {
      ErrorRemoteResponse error = new ErrorRemoteResponse("NULL_ID", "The id is null");
      GenericEntity<ErrorRemoteResponse> genericError = new GenericEntity<ErrorRemoteResponse>(error) {
      };
      return Response.status(400).entity(genericError).build();
    } else {
      try {
        this.dictionaryDao.delete(id);
        words.setWords(this.dictionaryDao.findAll());
        return Response.status(204).header("Access-Control-Allow-Origin", "*").build();
      } catch (Exception var5) {
        return Response.status(500).entity(this.executionError).build();
      }
    }
  }
}
