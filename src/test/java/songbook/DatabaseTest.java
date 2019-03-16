package songbook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.junit.Test;

/**
 * Created by pwilkin on 14-Mar-19.
 */
public class DatabaseTest {

    @Test
    public void testCreateTable() {
        try (Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "")) {
            c.createStatement().execute("CREATE TABLE TEXT (ID INT IDENTITY PRIMARY KEY, BLA VARCHAR(200))");
            for (int i = 0; i < 100; i++) {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO TEXT (BLA) VALUES (?)")) {
                    ps.setString(1, "test '" + i);
                    ps.execute();
                }
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM TEXT WHERE ID = ?")) {
                ps.setInt(1, 14);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    System.out.println(rs.getString("BLA"));
                }
                ps.setInt(1, 22);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    System.out.println(rs.getString("BLA"));
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testTransaction() {
        try (Connection c = DriverManager.getConnection("jdbc:hsqldb:mem:mymemdb", "SA", "")) {
            c.createStatement().execute("CREATE TABLE TEXT (ID INT IDENTITY PRIMARY KEY, BLA VARCHAR(200))");
            for (int i = 0; i < 100; i++) {
                try (PreparedStatement ps = c.prepareStatement("INSERT INTO TEXT (BLA) VALUES (?)")) {
                    ps.setString(1, "test '" + i);
                    ps.execute();
                }
            }
            c.setAutoCommit(false); // POCZĄTEK TRANSAKCJI
            try {
                try (PreparedStatement ps = c.prepareStatement("UPDATE TEXT SET BLA=? WHERE ID=?")) {
                    ps.setString(1, "HAHAHA");
                    ps.setInt(2, 15);
                    ps.execute();
                    ps.setInt(2, 30);
                    ps.execute();
                }
                try (PreparedStatement ps = c.prepareStatement("SELECT * FROM TEXT WHERE ID=?")) {
                    ps.setInt(1, 15);
                    try (ResultSet rs = ps.executeQuery()) {
                        rs.next();
                        System.out.println(rs.getString("BLA"));
                    }
                }
                c.commit(); // ZATWIERDZENIE TRANSAKCJI; c.rollback() <= cofnięcie
            } catch (Exception e) {
                c.rollback(); // WYSTĄPIŁ BŁĄD, WIEC COFAMY
            }
            try (PreparedStatement ps = c.prepareStatement("SELECT * FROM TEXT WHERE ID=?")) {
                ps.setInt(1, 15);
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    System.out.println(rs.getString("BLA"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
