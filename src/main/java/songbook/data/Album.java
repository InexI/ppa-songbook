package songbook.data;

import java.util.ArrayList;
import java.util.List;

import songbook.dao.SongDAO;

/**
 * Created by pwilkin on 13-Dec-18.
 */
public class Album {

    protected Integer id;
    protected List<Song> songs;
    protected String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<Song> getSongs() {
        if (songs == null) {
            if (id == null) {
                songs = new ArrayList<>();
            } else {
                songs = SongDAO.getInstance().loadSongsByAlbum(id);
            }
        }
        return songs;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (songs != null) {
            sb.append(">").append(name).append("<\n");
            for (int i = 0; i < songs.size(); i++) {
                sb.append(songs.get(i));
                sb.append("\n\n");
            }
        }
        return sb.toString();
    }
}
