package songbook.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import songbook.DatabaseConnection;
import songbook.data.Album;
import songbook.data.Artist;

/**
 * Created by pwilkin on 28-Mar-19.
 */
public final class AlbumDAO {

    private static AlbumDAO singleton;

    private AlbumDAO() {}

    public synchronized static AlbumDAO getInstance() {
        if (singleton == null) {
            singleton = new AlbumDAO();
        }
        return singleton;
    }

    private Map<Integer, Album> loadedAlbums = new HashMap<>();

    public Album getOrLoadAlbum(Integer id) {
        if (loadedAlbums.containsKey(id)) {
            return loadedAlbums.get(id);
        } else {
            return loadAlbum(id);
        }
    }

    private Album loadAlbum(Integer id) {
        DatabaseConnection.getInstance().runInTransaction(con -> {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM ALBUMS WHERE ID=?")) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.first()) {
                    Album album = new Album();
                    album.setId(id);
                    album.setName(rs.getString("NAME"));
                    loadedAlbums.put(id, album);
                }
            }
        });
        return loadedAlbums.get(id);
    }

    public void saveAlbum(Album album, Artist artist) {
        DatabaseConnection.getInstance().runInTransaction(con -> {
            if (album.getId() == null) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO ALBUMS (NAME, ARTIST) VALUES (?, ?)")) {
                    ps.setString(1, album.getName());
                    ps.setInt(2, artist.getId());
                    ps.execute();
                    /* wyciągnij wartość kolumny ID dla nowo utworzonego wpisu bazodanowego */
                    ResultSet genKeys = ps.getGeneratedKeys();
                    album.setId(genKeys.getInt(1)); /* pierwszy i jedyny wygenerowany klucz, bo mamy tylko jedną kolumnę IDENTITY */
                    loadedAlbums.put(album.getId(), album);
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement("UPDATE ALBUMS SET NAME=?, ARTIST=? WHERE ID=?")) {
                    ps.setString(1, album.getName());
                    ps.setInt(2, artist.getId());
                    ps.setInt(3, album.getId());
                    ps.execute();
                }
            }
        });
    }

    public List<Album> loadAlbumsByArtist(Integer id) {
        List<Integer> albumIds = new ArrayList<>();
        DatabaseConnection.getInstance().runInTransaction(con -> {
            try (PreparedStatement ps = con.prepareStatement("SELECT ID FROM ALBUMS WHERE ARTIST=?")) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    albumIds.add(rs.getInt("ID"));
                }
            }
        });
        return albumIds.stream().map(this::getOrLoadAlbum).collect(Collectors.toList());
    }
}



