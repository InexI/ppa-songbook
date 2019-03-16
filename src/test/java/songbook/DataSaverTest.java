package songbook;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.Assert;
import org.junit.Test;
import songbook.DataSaver.DataException;
import songbook.data.Album;
import songbook.data.Artist;
import songbook.data.Song;
import songbook.data.Songbook;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pwilkin on 28-Feb-19.
 */
public class DataSaverTest {

    @Test
    public void testReadSimpleSongbook() {
        String input = "###\nTestowy\n";
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
        try {
            Songbook sb = new DataSaver().readSongbook(bais);
            checkArtistsNotEmpty(sb);
            Assert.assertEquals(sb.getArtists().get(0).getName(), "Testowy");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkArtistsNotEmpty(Songbook sb) {
        Assert.assertNotNull(sb);
        Assert.assertNotNull(sb.getArtists());
        Assert.assertTrue(sb.getArtists().size() > 0);
        Assert.assertNotNull(sb.getArtists().get(0));
    }

    private void checkAlbumsNotEmpty(Artist art) {
        Assert.assertNotNull(art);
        Assert.assertNotNull(art.getAlbums());
        Assert.assertTrue(art.getAlbums().size() > 0);
        Assert.assertNotNull(art.getAlbums().get(0));
    }

    private void checkSongsNotEmpty(Album alb) {
        Assert.assertNotNull(alb);
        Assert.assertNotNull(alb.getSongs());
        Assert.assertTrue(alb.getSongs().size() > 0);
        Assert.assertNotNull(alb.getSongs().get(0));
    }

    @Test
    public void testReadASongbook() {
        String input = "###\nTestowy\n##\nTest album\n#\nPomidor#Jestem pomidorem#jejeeee\n";
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
        try {
            Songbook sb = new DataSaver().readSongbook(bais);
            checkArtistsNotEmpty(sb);
            Artist firstArtist = sb.getArtists().get(0);
            checkAlbumsNotEmpty(firstArtist);
            Album firstAlbum = firstArtist.getAlbums().get(0);
            checkSongsNotEmpty(firstAlbum);
            Song firstSong = firstAlbum.getSongs().get(0);
            Assert.assertEquals(firstSong.getTitle(), "Pomidor");
            Assert.assertTrue(firstSong.getLyrics().contains("jejeee"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Test
    public void testWriteASong() {
        // ...
//        ByteArrayOutputStream baos = null; // zmienić!
//        String fromBaos = baos.toString();

        //

        String songbookWroteToFile = "###\nTestowy\n##\nTest album\n#\nPomidor#Jestem pomidorem#jejeeee\n";
        Songbook songbook = readTestSongbook(songbookWroteToFile);
        Assert.assertNotNull(songbook);

        File selectedFile = getTestSelectedFile();
        Assert.assertNotNull(selectedFile);

        trySaveToFile(songbook, selectedFile);

        String songbookReadFromFile = readTestLine(selectedFile);
        Assert.assertNotNull(songbookReadFromFile);
        Assert.assertEquals(songbookWroteToFile,songbookReadFromFile);

    }

    private Songbook readTestSongbook(String testSongbook) {
        ByteArrayInputStream bais = new ByteArrayInputStream(testSongbook.getBytes());
        try {
            return new DataSaver().readSongbook(bais);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String readTestLine(File selectedFile) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader b = new BufferedReader(new FileReader(selectedFile));
            String readLine = null;
            while ((readLine = b.readLine()) != null) {
                stringBuilder.append(readLine);
                stringBuilder.append("\n");
            }
            return stringBuilder.toString();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void trySaveToFile(Songbook sb, File selectedFile) {
        try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
            new DataSaver().writeSongbook(sb, fos);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private File getTestSelectedFile() {
        String userHomeDir = System.getProperty("user.home");
        Path homeDir = Paths.get(userHomeDir);
        Path subdir = homeDir.resolve(".songbook");
        Path songsFile = subdir.resolve("songsTest.txt");
        try {
            if (!Files.isDirectory(subdir)) {
                Files.createDirectory(subdir);
            }
            if (!Files.exists(songsFile)) {
                Files.createFile(songsFile);
            }
            return songsFile.toFile();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Artist> getTestArtists(List<Album> albums) {
        List<Artist> artists = new ArrayList<>();
        Artist artist = new Artist();
        artist.setName("artistName");
        artist.setAlbums(albums);
        artists.add(artist);
        return artists;
    }

    private List<Album> getTestAlbums(List<Song> songs) {
        List<Album> albums = new ArrayList<>();
        Album album = new Album();
        album.setName("albumTitle");
        album.setSongs(songs);
        albums.add(album);
        return albums;
    }

    private List<Song> getTestSongs() {
        List<Song> songs = new ArrayList<>();
        Song song = new Song();
        song.setTitle("songTitle");
        song.setLyrics("songLyrics");
        songs.add(song);
        return songs;
    }


    @Test(expected = DataException.class)
    public void testDoNotReadASong() throws DataException {
        String input = "Testowy\n";
        ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
        try {
            Songbook sb = new DataSaver().readSongbook(bais);
        } catch (DataException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
