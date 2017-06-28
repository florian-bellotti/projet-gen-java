package com.fbellotti.gen.dao;

import com.fbellotti.api_ws_spring.dao.CrudDao;
import com.fbellotti.gen.model.Word;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="http://fbellotti.com">Florian BELLOTTI</a>
 */
@Component
public class DictionaryDao implements CrudDao<Word> {

  private static final Logger LOG = LoggerFactory.getLogger(DictionaryDao.class);
  private DataSource dataSource;

  @Autowired
  public DictionaryDao(DataSource dataSource){
    this.dataSource = dataSource;
  }

  /**
   * Create an item
   * @param word The item
   * @return The created item
   */
  public Word create(Word word) {
    Connection conn = null;
    String query = "INSERT INTO dictionary (label) VALUES (?)";

    try {
      // create the connection and preprare the query
      conn = dataSource.getConnection();

      // prepare the query
      PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, word.getLabel());

      // execute the query
      int affectedRows = ps.executeUpdate();

      // get the id
      if (affectedRows == 0) {
        throw new SQLException("Creating user failed, no rows affected.");
      }
      try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          word.setId(generatedKeys.getInt(1));
        }
        else {
          throw new SQLException("Creating user failed, no ID obtained.");
        }
      }
      LOG.info("Word added : " + word.toString());

      // close connection
      ps.close();
    } catch (SQLException e) {
      LOG.error("Error when try to create word" + word.toString() + e);
    } finally {
      closeConnection(conn);
    }

    return word;
  }

  /**
   * Find an item by id
   * @param id The item's id
   * @return The item
   */
  public Word findById(String id) {
    String sql = "SELECT * FROM dictionary WHERE id = ?";
    Connection conn = null;

    try {
      conn = dataSource.getConnection();

      // prepare the query
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setInt(1, Integer.parseInt(id));
      Word customer = null;

      // execute query
      ResultSet rs = ps.executeQuery();
      if (rs.next()) {
        customer = new Word(
          rs.getInt("id"),
          rs.getString("label")
        );
      }
      rs.close();
      ps.close();
      return customer;
    } catch (SQLException e) {
      LOG.error("Error when try to find word with id " + id + e);
    } finally {
      closeConnection(conn);
    }

    return null;
  }

  /**
   * Update an item by id
   * @param id The item's id
   * @param word The item
   */
  public void update(String id, Word word) {
    Connection conn = null;
    String query = "UPDATE dictionary SET label = ? WHERE id = ?";

    try {
      conn = dataSource.getConnection();

      // prepare the query
      PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, word.getLabel());
      ps.setInt(2, Integer.parseInt(id));

      LOG.info("Word updated : " + word.toString());

      // execute the query
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      LOG.error("Error when try to update word with id " + id + e);
    } finally {
      closeConnection(conn);
    }
  }

  /**
   * Delete an item by id
   * @param id The item's id
   */
  public void delete(String id) {
    Connection conn = null;
    String query = "DELETE FROM dictionary WHERE id = ?";

    try {
      conn = dataSource.getConnection();

      // prepare the query
      PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      ps.setInt(1, Integer.parseInt(id));
      LOG.info("Word's id deleted : " + id);

      // execute the query
      ps.executeUpdate();
      ps.close();
    } catch (SQLException e) {
      LOG.error("Error when try to delete word with id " + id + e);
    } finally {
      closeConnection(conn);
    }
  }

  /**
   * Find all words in the database that contains a given label
   * @param label The given label
   * @return Return a List of words
   */
  public List<Word> labelContains(String label) {
    label = "%" + label + "%";
    String sql = "SELECT * FROM dictionary WHERE label LIKE ?";
    Connection conn = null;

    try {
      // create the connection
      conn = dataSource.getConnection();

      // prepare the query
      PreparedStatement ps = conn.prepareStatement(sql);
      ps.setString(1, label);
      List<Word> customers = new ArrayList<>();

      // execute query
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        customers.add(new Word(
          rs.getInt("id"),
          rs.getString("label")
        ));
      }

      // close the connection
      rs.close();
      ps.close();
      return customers;
    } catch (SQLException e) {
      LOG.error("Error when try to find contains words in database" + e);
    } finally {
      closeConnection(conn);
    }

    return null;
  }

  /**
   * Find all words in the database
   * @return Return all words in an HashSet
   */
  public Set<String> findAll() {
    String sql = "SELECT * FROM dictionary";
    Connection conn = null;
    Set<String> words = new HashSet<>();

    try {
      // create the connection
      conn = dataSource.getConnection();

      // prepare the query & execute
      PreparedStatement ps = conn.prepareStatement(sql);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        words.add(rs.getString("label"));
      }

      // close the connection
      rs.close();
      ps.close();
      return words;
    } catch (SQLException e) {
      LOG.error("Error when try to find allow words in database" + e);
    } finally {
      closeConnection(conn);
    }

    return null;
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
