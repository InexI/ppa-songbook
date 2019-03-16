package songbook.data;

import java.util.List;

/**
 * Created by pwilkin on 13-Dec-18.
 */
public class Artist {

    protected Integer id;
    protected String name;
    protected List<Album> albums;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(List<Album> albums) {
        this.albums = albums;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (albums != null) {
            sb.append("[").append(name).append("]\n");
            for (int i = 0; i < albums.size(); i++) {
                sb.append(albums.get(i));
                sb.append("\n--\n");
            }
        }
        return sb.toString();
    }
}
