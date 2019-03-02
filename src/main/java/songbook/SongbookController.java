package songbook;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import songbook.data.Album;
import songbook.data.Artist;
import songbook.data.Song;

/**
 * Created by pwilkin on 10-Jan-19.
 */
public class SongbookController {

    @FXML
    protected TextField newArtist;
    @FXML
    protected Button addArtist;
    @FXML
    protected TextField newAlbum;
    @FXML
    protected Button addAlbum;
    @FXML
    protected TextField newSong;
    @FXML
    protected Button addSong;
    @FXML
    protected ListView<String> artists;
    @FXML
    protected ListView<String> albums;
    @FXML
    protected ListView<String> songs;
    @FXML
    protected TextArea songText;
    @FXML
    protected Button save;
    @FXML
    protected Button pdf;

    protected DataStorage dataStorage;
    protected Artist selectedArtist;
    protected Album selectedAlbum;
    protected Song selectedSong;

    public DataStorage getDataStorage() {
        return dataStorage;
    }

    public void setDataStorage(DataStorage dataStorage) {
        this.dataStorage = dataStorage;
        loadArtists();
    }

    private void loadArtists() {
        artists.getItems().clear();
        for (Artist artist : dataStorage.getSongbook().getArtists()) {
            artists.getItems().add(artist.getName());
        }
        toggleEditors(false, false);
        clearArtist();
    }

    private void clearArtist() {
        selectedArtist = null;
        albums.getItems().clear();
        clearAlbum();
    }

    private void loadAlbums(String selectedItem) {
        for (Artist artist : dataStorage.getSongbook().getArtists()) {
            if (Objects.equals(artist.getName(), selectedItem)) {
                selectedArtist = artist;
                albums.getItems().clear();
                for (Album album : selectedArtist.getAlbums()) {
                    albums.getItems().add(album.getName());
                }
            }
        }
        toggleEditors(true, false);
        clearAlbum();
    }

    private void clearAlbum() {
        selectedAlbum = null;
        songs.getItems().clear();
        clearSong();
    }

    private void loadSongs(String selectedItem) {
        for (Album album : selectedArtist.getAlbums()) {
            if (Objects.equals(album.getName(), selectedItem)) {
                selectedAlbum = album;
                songs.getItems().clear();
                for (Song song : selectedAlbum.getSongs()) {
                    songs.getItems().add(song.getTitle());
                }
            }
        }
        toggleEditors(true, true);
        clearSong();
    }

    private void toggleEditors(boolean allowAlbums, boolean allowSongs) {
        newAlbum.setDisable(!allowAlbums);
        addAlbum.setDisable(!allowAlbums);
        newSong.setDisable(!allowSongs);
        addSong.setDisable(!allowSongs);
        newArtist.setDisable(false);
        addArtist.setDisable(false);
    }

    private void clearSong() {
        selectedSong = null;
        songText.setText(null);
    }

    private void loadSong(String selectedItem) {
        for (Song song : selectedAlbum.getSongs()) {
            if (Objects.equals(song.getTitle(), selectedItem)) {
                selectedSong = song;
                songText.setText(song.getLyrics());
            }
        }
    }

    public void initialize() {
        artists.getSelectionModel().selectedItemProperty().addListener(c -> {
            if (artists.getSelectionModel().getSelectedItem() != null) {
                loadAlbums(artists.getSelectionModel().getSelectedItem());
            }
        });
        albums.getSelectionModel().selectedItemProperty().addListener(c -> {
            if (albums.getSelectionModel().getSelectedItem() != null) {
                loadSongs(albums.getSelectionModel().getSelectedItem());
            }
        });
        songs.getSelectionModel().selectedItemProperty().addListener(c -> {
            if (songs.getSelectionModel().getSelectedItem() != null) {
                loadSong(songs.getSelectionModel().getSelectedItem());
            }
        });
        songText.textProperty().addListener(c -> {
            if (selectedSong != null) {
                selectedSong.setLyrics(songText.getText());
            }
        });
    }

    public void saveSong(ActionEvent actionEvent) {
        try {
            getDataStorage().saveData();
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Data saved");
            alert.setContentText("The songbook data has been successfully saved!");
            alert.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Data save error");
            alert.setContentText("There was an error saving the data: " + e.getMessage());
            alert.showAndWait();
        }
    }

    public void addArtist(ActionEvent actionEvent) {
        if (newArtist.getText() == null || newArtist.getText().trim().length() == 0) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Invalid artist name");
            alert.setContentText("Artist name cannot be empty!");
            alert.showAndWait();
        } else if (dataStorage.getSongbook().getArtists().stream().anyMatch(x -> Objects.equals(x.getName(), newArtist.getText()))) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Duplicate artist");
            alert.setContentText("An artist with that name already exists!");
            alert.showAndWait();
        } else {
            Artist added = new Artist();
            added.setAlbums(new ArrayList<>());
            added.setName(newArtist.getText());
            newArtist.setText(null);
            dataStorage.getSongbook().getArtists().add(added);
            loadArtists();
            artists.getSelectionModel().select(added.getName());
        }
    }

    public void addAlbum(ActionEvent actionEvent) {
        if (newAlbum.getText() == null || newAlbum.getText().trim().length() == 0) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Invalid album name");
            alert.setContentText("Album name cannot be empty!");
            alert.showAndWait();
        } else if (selectedArtist.getAlbums().stream().anyMatch(x -> Objects.equals(x.getName(), newAlbum.getText()))) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Duplicate album");
            alert.setContentText("An album with that name already exists!");
            alert.showAndWait();
        } else {
            Album added = new Album();
            added.setSongs(new ArrayList<>());
            added.setName(newAlbum.getText());
            newAlbum.setText(null);
            selectedArtist.getAlbums().add(added);
            loadAlbums(selectedArtist.getName());
            albums.getSelectionModel().select(added.getName());
        }
    }

    public void addSong(ActionEvent actionEvent) {
        if (newSong.getText() == null || newSong.getText().trim().length() == 0) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Invalid song name");
            alert.setContentText("Song name cannot be empty!");
            alert.showAndWait();
        } else if (selectedAlbum.getSongs().stream().anyMatch(x -> Objects.equals(x.getTitle(), newSong.getText()))) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Duplicate song");
            alert.setContentText("An song with that name already exists!");
            alert.showAndWait();
        } else {
            Song added = new Song();
            added.setTitle(newSong.getText());
            added.setLyrics("");
            newSong.setText(null);
            selectedAlbum.getSongs().add(added);
            loadSongs(selectedAlbum.getName());
            songs.getSelectionModel().select(added.getTitle());
        }
    }

    public void saveToPdf(ActionEvent actionEvent) {
        try {
            Alert alert = new Alert(AlertType.INFORMATION);
            getDataStorage().saveToPdf(alert);
            alert.showAndWait();
        } catch (Exception e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Data save error");
            alert.setContentText("There was an error exporting the data: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
