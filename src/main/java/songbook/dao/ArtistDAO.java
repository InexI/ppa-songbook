package songbook.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import songbook.DatabaseConnection;
import songbook.data.Artist;

/**
 * Created by pwilkin on 28-Mar-19.
 */
public final class ArtistDAO {

    private static ArtistDAO singleton;

    private ArtistDAO() {}

    public synchronized static ArtistDAO getInstance() {
        if (singleton == null) {
            singleton = new ArtistDAO();
        }
        return singleton;
    }

    private Map<Integer, Artist> loadedArtists = new HashMap<>();

    public Artist getOrLoadArtist(Integer id) {
        if (loadedArtists.containsKey(id)) {
            return loadedArtists.get(id);
        } else {
            return loadArtist(id);
        }
    }

    private Artist loadArtist(Integer id) {
        DatabaseConnection.getInstance().runInTransaction(con -> {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM ARTISTS WHERE ID=?")) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.first()) {
                    Artist artist = new Artist();
                    artist.setId(id);
                    artist.setName(rs.getString("NAME"));
                    loadedArtists.put(id, artist);
                }
            }
        });
        return loadedArtists.get(id);
    }

    public void saveArtist(Artist artist) {
        DatabaseConnection.getInstance().runInTransaction(con -> {
            if (artist.getId() == null) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO ARTISTS (NAME) VALUES (?)")) {
                    ps.setString(1, artist.getName());
                    ps.execute();
                    /* wyciągnij wartość kolumny ID dla nowo utworzonego wpisu bazodanowego */
                    ResultSet genKeys = ps.getGeneratedKeys();
                    artist.setId(genKeys.getInt(1)); /* pierwszy i jedyny wygenerowany klucz, bo mamy tylko jedną kolumnę IDENTITY */
                    loadedArtists.put(artist.getId(), artist);
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement("UPDATE ARTISTS SET NAME=? WHERE ID=?")) {
                    ps.setString(1, artist.getName());
                    ps.setInt(2, artist.getId());
                    ps.execute();
                }
            }
        });
        artist.getAlbums().forEach(x -> AlbumDAO.getInstance().saveAlbum(x, artist));
    }
}



