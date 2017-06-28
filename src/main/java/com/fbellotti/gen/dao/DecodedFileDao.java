package com.fbellotti.gen.dao;

import com.fbellotti.api_ws_spring.dao.QueryStringDao;
import com.fbellotti.api_ws_spring.model.DaoResponse;
import com.fbellotti.gen.model.DecodedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

  private static final Logger LOG = LoggerFactory.getLogger(DecodedFileDao.class);
  private DataSource dataSource;

  @Autowired
  public DecodedFileDao(DataSource dataSource){
    this.dataSource = dataSource;
  }

  /**
   * Add a decoded file in the database
   * @param decodedFile The decoded file to create in database
   */
  public void create(DecodedFile decodedFile){
    Connection conn = null;
    String query = "INSERT INTO decodedFile (fileName, decodeKey, md5, firstWord, secret) VALUES (?,?,?,?,?)";

    try {
      // create connection and prepare the query
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, decodedFile.getFileName());
      ps.setString(2, decodedFile.getDecodeKey());
      ps.setString(3, decodedFile.getMd5());
      ps.setString(4, decodedFile.getFirstWorld());
      ps.setString(5, decodedFile.getSecret());

      // execute the query and close connection
      ps.executeUpdate();
      LOG.info("DecodedFile added : " + decodedFile.toString());
      ps.close();
    } catch (SQLException e) {
      LOG.error("Error when try to add decodedFile : " + decodedFile.toString() + e);
    } finally {
      closeConnection(conn);
    }
  }

  /**
   * Find all decoded files
   * @param multivaluedMap It's a MultivaluedMap that contains an list of filters (asc, fields...)
   * @return Return an object DaoResponse<DecodedFile>
   */
  public DaoResponse<DecodedFile> find(MultivaluedMap<String, String> multivaluedMap){
    String query = createQuery(multivaluedMap, false);
    Connection conn = null;
    List<DecodedFile> decodedFiles = new ArrayList<>();
    Set<String> fields = (multivaluedMap.get("fields") == null) ? null : new HashSet<>(multivaluedMap.get("fields"));

    try {
      // create connection and prepare the query
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(query);

      // execute query and close connection
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
      LOG.error("Error when try to find decodedFiles" + e);
    } finally {
      closeConnection(conn);
    }

    return null;
  }

  /**
   * Count the number of decoded files
   * @param multivaluedMap It's a MultivaluedMap that contains an list of filters (asc, fields...)
   * @return Return a long that contains the count of decoded files
   */
  @Override
  public long count(MultivaluedMap<String, String> multivaluedMap){
    String query = createQuery(multivaluedMap, true);
    Connection conn = null;
    int total = 0;

    try {
      // create connection and prepare query
      conn = dataSource.getConnection();
      PreparedStatement ps = conn.prepareStatement(query);

      // execute query
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        total = rs.getInt("total");
      }

      // close connections
      rs.close();
      ps.close();
      return (long) total;
    } catch (SQLException e) {
      LOG.error("Error when try to count decodedFiles" + e);
    } finally {
      closeConnection(conn);
    }

    return 0;
  }

  /**
   * This method is not implemented
   */
  @Override
  public DaoResponse<DecodedFile> first(MultivaluedMap<String, String> multivaluedMap) {
    return null;
  }

  /**
   * This method say if a decoded file exist in the database
   * @param fileName The file name of the decoded file
   * @param md5 The md5 of the decoded file
   * @return Return true is the file exist in the database
   */
  public Boolean isAlreadyExist(String fileName, String md5){
    String sql =
      "SELECT distinct fileName, firstWord FROM decodedFile " +
      "WHERE fileName = ? AND md5 = ?";
    Connection conn = null;

    try {
      // create the connection
      conn = dataSource.getConnection();

      // prepare the query & execute
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, fileName);
      ps.setString(2, md5);
      ResultSet rs = ps.executeQuery();
      Boolean exist = rs.next();

      // close
      rs.close();
      ps.close();
      return exist;
    } catch (SQLException e) {
      LOG.error("Error when try to say if file " + fileName  + " and md5 " + md5 + " exist in the database");
    } finally {
      closeConnection(conn);
    }

    return false;
  }

  /**
   * This method allow to create a query to find decoded files
   * in the database. There is some filters (ex : range, fields)
   * @param filters The filters
   * @param count True if the query is a count and not a basic select
   * @return Return the query in a String
   */
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

  /**
   * This method allow to close the database's connection
   * @param conn The connection to close
   */
  private void closeConnection(Connection conn) {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException e) {
        LOG.error("Error when try to close database connection" + e);
      }
    }
  }
}
