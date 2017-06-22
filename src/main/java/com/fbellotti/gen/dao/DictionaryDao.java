package com.fbellotti.gen.dao;

import com.fbellotti.api_ws_spring.dao.CrudDao;
import com.fbellotti.gen.model.Word;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="http://fbellotti.com">Florian BELLOTTI</a>
 */
@Component
public class DictionaryDao implements CrudDao<Word> {

  private DataSource dataSource;

  @Autowired
  public DictionaryDao(DataSource dataSource){
    this.dataSource = dataSource;
  }

  public Word create(Word word) {
    Connection conn = null;
    String query = "INSERT INTO dictionary (label) VALUES (?)";

    try {
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

      // close connection
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

    return word;
  }

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

  public void update(String id, Word word) {
    Connection conn = null;
    String query = "UPDATE dictionary SET label = ? WHERE id = ?";

    try {
      conn = dataSource.getConnection();

      // prepare the query
      PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      ps.setString(1, word.getLabel());
      ps.setInt(2, Integer.parseInt(id));

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

  public void delete(String id) {
    Connection conn = null;
    String query = "DELETE FROM dictionary WHERE id = ?";

    try {
      conn = dataSource.getConnection();

      // prepare the query
      PreparedStatement ps = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
      ps.setInt(1, Integer.parseInt(id));

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

  public List<Word> labelContains(String label) {
    label = "%" + label + "%";
    String sql = "SELECT * FROM dictionary WHERE label LIKE ?";
    Connection conn = null;

    try {
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
      rs.close();
      ps.close();
      return customers;
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

  public List<String> findWords(List<String> words) {
    String parameter = String.join("', '", words).toLowerCase();
    String sql = "SELECT DISTINCT label FROM dictionary WHERE label IN ('"  + parameter + "')";
    Connection conn = null;
    words = new ArrayList<>();

    try {
      conn = dataSource.getConnection();

      // prepare the query & execute
      PreparedStatement ps = conn.prepareStatement(sql);
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        words.add(rs.getString("label"));
      }
      rs.close();
      ps.close();
      return words;
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
