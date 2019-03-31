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
import songbook.data.Song;

/**
 * Created by pwilkin on 28-Mar-19.
 */
public final class SongDAO {

    private static SongDAO singleton;

    private SongDAO() {}

    public synchronized static SongDAO getInstance() {
        if (singleton == null) {
            singleton = new SongDAO();
        }
        return singleton;
    }

    private Map<Integer, Song> loadedSongs = new HashMap<>();

    public Song getOrLoadSong(Integer id) {
        if (loadedSongs.containsKey(id)) {
            return loadedSongs.get(id);
        } else {
            return loadSong(id);
        }
    }

    private Song loadSong(Integer id) {
        DatabaseConnection.getInstance().runInTransaction(con -> {
            try (PreparedStatement ps = con.prepareStatement("SELECT * FROM SONGS WHERE ID=?")) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                if (rs.first()) {
                    Song song = new Song();
                    song.setId(id);
                    song.setTitle(rs.getString("TITLE"));
                    song.setLyrics(rs.getString("LYRICS"));
                    loadedSongs.put(id, song);
                }
            }
        });
        return loadedSongs.get(id);
    }

    public void saveSong(Song song, Album album) {
        DatabaseConnection.getInstance().runInTransaction(con -> {
            if (song.getId() == null) {
                try (PreparedStatement ps = con.prepareStatement("INSERT INTO SONGS (TITLE, LYRICS, ALBUM) VALUES (?, ?, ?)")) {
                    ps.setString(1, song.getTitle());
                    ps.setString(2, song.getLyrics());
                    ps.setInt(3, album.getId());
                    ps.execute();
                    /* wyciągnij wartość kolumny ID dla nowo utworzonego wpisu bazodanowego */
                    ResultSet genKeys = ps.getGeneratedKeys();
                    song.setId(genKeys.getInt(1)); /* pierwszy i jedyny wygenerowany klucz, bo mamy tylko jedną kolumnę IDENTITY */
                    loadedSongs.put(song.getId(), song);
                }
            } else {
                try (PreparedStatement ps = con.prepareStatement("UPDATE SONGS SET TITLE=?, LYRICS=?, ALBUM=? WHERE ID=?")) {
                    ps.setString(1, song.getTitle());
                    ps.setString(2, song.getLyrics());
                    ps.setInt(3, album.getId());
                    ps.setInt(4, song.getId());
                    ps.execute();
                }
            }
        });
    }

    public List<Song> loadSongsByAlbum(Integer id) {
        List<Integer> songIds = new ArrayList<>();
        DatabaseConnection.getInstance().runInTransaction(con -> {
            try (PreparedStatement ps = con.prepareStatement("SELECT ID FROM SONGS WHERE ALBUM=?")) {
                ps.setInt(1, id);
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    songIds.add(rs.getInt("ID"));
                }
            }
        });
        return songIds.stream().map(this::getOrLoadSong).collect(Collectors.toList());
    }
}



