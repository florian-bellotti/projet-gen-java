package com.fbellotti.gen.dao;

import com.fbellotti.api_ws_spring.dao.QueryStringDao;
import com.fbellotti.api_ws_spring.model.DaoResponse;
import com.fbellotti.gen.model.DecodedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import javax.ws.rs.core.MultivaluedMap;
import java.sql.*;
import java.util.*;

/**
 * @author <a href="http://fbellotti.com">Florian BELLOTTI</a>
 */
@Component
public class DecodedFileDao implements QueryStringDao<DecodedFile> {

  private DataSource dataSource;

  @Autowired
  public DecodedFileDao(DataSource dataSource){
    this.dataSource = dataSource;
  }

  public void create(DecodedFile decodedFile) {
    Connection conn = null;
    String query = "INSERT INTO decodedFile (fileName, decodeKey, md5, firstWord, secret) VALUES (?,?,?,?,?)";

    try {
      conn = dataSource.getConnection();

      // prepare the query
      PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, decodedFile.getFileName());
      ps.setString(2, decodedFile.getDecodeKey());
      ps.setString(3, decodedFile.getMd5());
      ps.setString(4, decodedFile.getFirstWorld());
      ps.setString(5, decodedFile.getSecret());

      // execute the query
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  @Override
  public DaoResponse<DecodedFile> find(MultivaluedMap<String, String> multivaluedMap) {
    String query = createQuery(multivaluedMap, false);
    Connection conn = null;
    List<DecodedFile> decodedFiles = new ArrayList<>();
    Set<String> fields = (multivaluedMap.get("fields") == null) ? null : new HashSet<>(multivaluedMap.get("fields"));

    try {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(query);

      // execute query
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        if (fields != null) {
          DecodedFile decodedFile = new DecodedFile();
          if (fields.contains("id")) {
            decodedFile.setId(rs.getInt("id"));
          }
          if (fields.contains("fileName")) {
            decodedFile.setFileName(rs.getString("fileName"));
          }
          if (fields.contains("decodeKey")) {
            decodedFile.setDecodeKey(rs.getString("decodeKey"));
          }
          if (fields.contains("md5")) {
            decodedFile.setMd5(rs.getString("md5"));
          }
          if (fields.contains("firstWord")) {
            decodedFile.setFirstWorld(rs.getString("firstWord"));
          }
          if (fields.contains("secret")) {
            decodedFile.setSecret(rs.getString("secret"));
          }
          decodedFiles.add(decodedFile);
        } else {
          decodedFiles.add(new DecodedFile(
            rs.getInt("id"),
            rs.getString("fileName"),
            rs.getString("decodeKey"),
            rs.getString("md5"),
            rs.getString("firstWord"),
            rs.getString("secret")
          ));
        }
      }
      rs.close();
      ps.close();
      return new DaoResponse<>(decodedFiles, 0, decodedFiles.size(), decodedFiles.size(), 1000);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }

  @Override
  public long count(MultivaluedMap<String, String> multivaluedMap) {
    String query = createQuery(multivaluedMap, true);
    Connection conn = null;
    int total = 0;

    try {
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(query);

      // execute query
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        total = rs.getInt("total");
      }
      rs.close();
      ps.close();
      return (long) total;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    }

  }

  @Override
  public DaoResponse<DecodedFile> first(MultivaluedMap<String, String> multivaluedMap) {
    return null;
  }

  private String createQuery(MultivaluedMap<String, String> filters, Boolean count) {
    String where = "";
    String fields = "";
    String orderBy = "";

    for (Map.Entry<String, List<String>> entry : filters.entrySet()) {
      switch (entry.getKey()) {
        case "range":
          where = ("".equals(where)) ? " WHERE " : " AND ";
          String[] range = entry.getValue().get(0).split("-");
          where += "id >= " + range[0] + " AND id <= " + range[1];
          break;
        case "fields":
          for (String field : entry.getValue()){
            fields = ("".equals(fields)) ? field : "," + field;
          }
          break;
        case "asc":
          orderBy = " ORDER BY " + entry.getValue() + " ASC";
          break;
        case "desc":
          orderBy = " ORDER BY " + entry.getValue() + " DESC";
          break;
        default:
          for (String condition : entry.getValue()) {
            where = ("".equals(where)) ? " WHERE " : " AND ";
            where += entry.getKey() + " = '" + condition + "'";
          }
      }
    }

    fields = ("".equals(fields)) ? "*" : fields;
    fields = count ? "(" + fields + ")" : fields;
    return "SELECT " + fields + " FROM decodedFile " + where + orderBy;
  }

  public Boolean isAlreadyExist(String fileName, String firstWord) {
    String sql =
      "SELECT distinct fileName, firstWord FROM decodedFile " +
      "WHERE fileName = ? AND firstWord = ?";
    Connection conn = null;
    Boolean exist = false;

    try {
      conn = dataSource.getConnection();

      // prepare the query & execute
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, fileName);
      ps.setString(2, firstWord);
      ResultSet rs = ps.executeQuery();
      exist = (rs.next()) ? true : false;

      // close
      rs.close();
      ps.close();

      return exist;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    } finally {
      if (conn != null) {
        try {
          conn.close();
        } catch (SQLException e) {
          throw new RuntimeException(e);
        }
      }
    }
  }
}
