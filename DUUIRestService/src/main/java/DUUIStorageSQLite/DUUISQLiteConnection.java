package DUUIStorageSQLite;

import java.sql.*;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONObject;

public class DUUISQLiteConnection {

  private static String getUUID() {
    return UUID.randomUUID().toString();
  }

  private static final String connectionString = "jdbc:sqlite:duui.db";

  public static void createPipelineDatabase() {
    String sql =
      """
                        CREATE TABLE IF NOT EXISTS pipelines (
                        	id text PRIMARY KEY NOT NULL,
                        	name text NOT NULL
                        );""";

    try (
      Connection conn = DriverManager.getConnection(connectionString);
      Statement stmt = conn.createStatement()
    ) {
      conn.setAutoCommit(false);
      // create a new table
      stmt.execute(sql);
      conn.commit();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  public static void createComponentDatabase() {
    String sql =
      """
                        CREATE TABLE IF NOT EXISTS components (
                            id text PRIMARY KEY NOT NULL,
                            name text NOT NULL,
                            driver text NOT NULL,
                            target text NOT NULL,
                            pipeline_id integer NOT NULL,
                            CONSTRAINT fk_pipeline_id FOREIGN KEY (pipeline_id)
                                REFERENCES pipelines(id)
                        );""";
    try (
      Connection conn = DriverManager.getConnection(connectionString);
      Statement stmt = conn.createStatement()
    ) {
      conn.setAutoCommit(false);
      stmt.execute(sql);
      conn.commit();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  public static void insertComponent(JSONObject component, String pipelineID) {
    createComponentDatabase();
    String componentID = getUUID();
    String sql =
      "INSERT INTO components(id, name, driver, target, pipeline_id) VALUES(?, ?, ?, ?, ?)";
    try (
      Connection conn = DriverManager.getConnection(connectionString);
      PreparedStatement stmt = conn.prepareStatement(sql)
    ) {
      stmt.setString(1, componentID);
      stmt.setString(2, component.getString("name"));
      stmt.setString(3, component.getString("driver"));
      stmt.setString(4, component.getString("target"));
      stmt.setString(5, pipelineID);

      stmt.executeUpdate();
      conn.commit();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  public static JSONObject getComponentByID(String componentID) {
    JSONObject componentObject = new JSONObject();
    String sql =
      "SELECT id, name, driver, target, pipeline_id FROM components WHERE id = ?";
    try (
      Connection conn = DriverManager.getConnection(connectionString);
      PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setString(1, componentID);

      ResultSet rs = pstmt.executeQuery();

      if (!rs.next()) {
        return componentObject;
      }

      componentObject.put("id", rs.getString("id"));
      componentObject.put("name", rs.getString("name"));
      componentObject.put("driver", rs.getString("driver"));
      componentObject.put("target", rs.getString("target"));
      componentObject.put("pipeline_id", rs.getString("pipeline_id"));
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    return componentObject;
  }

  public static JSONArray getComponentsForPipeline(String pipelineID) {
    JSONArray components = new JSONArray();

    String sql =
      """
                SELECT
                 c.id, c.name, c.driver, c.target, p.id AS pipeline_id
                FROM
                 components c
                JOIN
                 pipelines p ON c.pipeline_id = p.id
                WHERE p.id = ?
                """;
    try (
      Connection conn = DriverManager.getConnection(connectionString);
      PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setString(1, pipelineID);

      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        JSONObject componentObject = new JSONObject();

        componentObject.put("id", rs.getString("id"));
        componentObject.put("name", rs.getString("name"));
        componentObject.put("driver", rs.getString("driver"));
        componentObject.put("target", rs.getString("target"));
        componentObject.put("pipeline_id", rs.getString("pipeline_id"));
        components.put(componentObject);
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      return null;
    }
    return components;
  }

  public static void insertPipeline(JSONObject data) {
    createPipelineDatabase();

    String sql = "INSERT INTO pipelines(id, name) VALUES(?, ?)";
    final String uuid = getUUID();

    try (
      Connection conn = DriverManager.getConnection(connectionString);
      PreparedStatement stmt = conn.prepareStatement(sql)
    ) {
      stmt.setString(1, uuid);
      stmt.setString(2, data.getString("name"));
      stmt.executeUpdate();
      conn.commit();
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }

    data
      .getJSONArray("components")
      .forEach(component -> insertComponent((JSONObject) component, uuid));
  }

  public static JSONObject getPipelines() {
    JSONObject output = new JSONObject();
    output.put("pipelines", new JSONArray());

    String sql = "SELECT id, name FROM pipelines";

    try (
      Connection conn = DriverManager.getConnection(connectionString);
      PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      ResultSet rs = pstmt.executeQuery();

      while (rs.next()) {
        JSONObject pipelineObject = new JSONObject();

        pipelineObject.put("id", rs.getString("id"));
        pipelineObject.put("name", rs.getString("name"));
        pipelineObject.put(
          "components",
          getComponentsForPipeline(rs.getString("id"))
        );

        output.getJSONArray("pipelines").put(pipelineObject);
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      return null;
    }
    return output;
  }

  public static JSONObject getPipelines(int limit) {
    return getPipelines(limit, 0);
  }

  public static JSONObject getPipelines(int limit, int offset) {
    JSONObject output = new JSONObject();
    output.put("pipelines", new JSONArray());

    String sql = "SELECT id, name FROM pipelines LIMIT ? OFFSET ?";

    try (
      Connection conn = DriverManager.getConnection(connectionString);
      PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setInt(1, limit);
      pstmt.setInt(2, offset);

      ResultSet rs = pstmt.executeQuery();
      while (rs.next()) {
        JSONObject pipelineObject = new JSONObject();

        pipelineObject.put("id", rs.getString("id"));
        pipelineObject.put("name", rs.getString("name"));
        pipelineObject.put(
          "components",
          getComponentsForPipeline(rs.getString("id"))
        );

        output.getJSONArray("pipelines").put(pipelineObject);
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
      return null;
    }

    return output;
  }

  public static JSONObject getPipelineByID(String id) {
    String sql = "SELECT id, name FROM pipelines WHERE id = ?";

    JSONObject pipeline = new JSONObject();

    try (
      Connection conn = DriverManager.getConnection(connectionString);
      PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      pstmt.setString(1, id);
      ResultSet rs = pstmt.executeQuery();

      if (!rs.next()) {
        return new JSONObject();
      }

      pipeline.put("id", rs.getString("id"));
      pipeline.put("name", rs.getString("name"));
      pipeline.put("components", getComponentsForPipeline(rs.getString("id")));
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
    return pipeline;
  }

  public static void allComponents() {
    String sql = "SELECT * FROM components";

    try (
      Connection conn = DriverManager.getConnection(connectionString);
      PreparedStatement pstmt = conn.prepareStatement(sql)
    ) {
      ResultSet rs = pstmt.executeQuery();

      while (rs.next()) {
        JSONObject pipelineObject = new JSONObject();

        pipelineObject.put("id", rs.getString("id"));
        pipelineObject.put("name", rs.getString("name"));
        pipelineObject.put("driver", rs.getString("driver"));
        pipelineObject.put("target", rs.getString("target"));
        pipelineObject.put("pipeline_id", rs.getString("pipeline_id"));
        System.out.println(pipelineObject);
      }
    } catch (SQLException e) {
      System.out.println(e.getMessage());
    }
  }

  public static void main(String[] args) {
    System.out.println(getPipelines());
  }
}
