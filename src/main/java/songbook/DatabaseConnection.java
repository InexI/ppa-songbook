package songbook;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by pwilkin on 28-Mar-19.
 */
public class DatabaseConnection {

    public static interface DatabaseRunnable {
        public void execute(Connection con) throws SQLException;
    }

    private static DatabaseConnection singleton;

    private DatabaseConnection() {}

    public synchronized static DatabaseConnection getInstance() {
        if (singleton == null) {
            singleton = new DatabaseConnection();
        }
        return singleton;
    }

    public void runInTransaction(DatabaseRunnable dr) {
        String userHomeDir = System.getProperty("user.home");
        Path homeDir = Paths.get(userHomeDir);
        Path subdir = homeDir.resolve(".songbook");
        Path songsFile = subdir.resolve("songs.db");
        try (Connection c = DriverManager.getConnection("jdbc:hsqldb:file:" + songsFile.toAbsolutePath().toString(), "SA", "")) {
            c.setAutoCommit(false); // POCZĄTEK TRANSAKCJI
            try {
                dr.execute(c);
                c.commit(); // ZATWIERDZENIE TRANSAKCJI; c.rollback() <= cofnięcie
            } catch (Exception e) {
                c.rollback();
                throw e; // WYSTĄPIŁ BŁĄD, WIEC COFAMY
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void initializeDatabaseIfNeeded() throws Exception {
        runInTransaction(con -> {
            ResultSet tbl = con.getMetaData().getTables(null, null, "ARTISTS", null);
            if (!tbl.first()) {
                con.createStatement().execute("CREATE TABLE ARTISTS (ID INT PRIMARY KEY IDENTITY, NAME VARCHAR(255))");
                con.createStatement().execute("CREATE TABLE ALBUMS (ID INT PRIMARY KEY IDENTITY, ARTIST INT, NAME VARCHAR(255))");
                con.createStatement().execute("CREATE TABLE SONGS (ID INT PRIMARY KEY IDENTITY, ALBUM INT, TITLE VARCHAR(255), LYRICS TEXT)");
            }
        });
    }

}
