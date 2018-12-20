package songbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import songbook.DataSaver.DataException;
import songbook.data.Songbook;

public class Controller {

    @FXML
    protected Button chooser;

    @FXML
    protected Button processor;

    @FXML
    protected Button def;

    protected File selectedFile;

    public void showChooseDialog(ActionEvent actionEvent) {
        FileChooser fc = new FileChooser();
        File file = fc.showOpenDialog(chooser.getScene().getWindow());
        if (file != null && file.exists()) {
            selectedFile = file;
            processor.setDisable(false);
            chooser.setText(file.getAbsolutePath());
        }
    }

    public void process(ActionEvent actionEvent) {
        try (FileInputStream fis = new FileInputStream(selectedFile)) {
            Songbook songbook = new DataSaver().readSongbook(fis);
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setContentText(songbook.toString());
            alert.show();
        } catch (FileNotFoundException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText("File not found: " + e.getMessage());
            alert.show();
        } catch (DataException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText("Data error: " + e.getMessage());
            alert.show();
        } catch (IOException e) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setContentText("Input/output exception: " + e.getMessage());
            alert.show();
        }
    }

    public void loadDefault(ActionEvent actionEvent) {
        String userHomeDir = System.getProperty("user.home");
        Path homeDir = Paths.get(userHomeDir);
        Path subdir = homeDir.resolve(".songbook");
        Path songsFile = subdir.resolve("songs.txt");
        try {
            if (!Files.isDirectory(subdir)) {
                Files.createDirectory(subdir);
            }
            if (!Files.exists(songsFile)) {
                Files.createFile(songsFile);
            }
            selectedFile = songsFile.toFile();
            processor.setDisable(false);
            chooser.setText(selectedFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
