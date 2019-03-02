package songbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;

import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import songbook.data.Songbook;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Created by pwilkin on 10-Jan-19.
 */
public class DataStorage {

    protected Songbook songbook;
    protected File selectedFile;
    protected File pdf;
    String pdfPath;

    public File getSelectedFile() {
        return selectedFile;
    }

    public void setSelectedFile(File selectedFile) {
        this.selectedFile = selectedFile;
    }

    public Songbook getSongbook() {
        return songbook;
    }

    public void setSongbook(Songbook songbook) {
        this.songbook = songbook;
    }

    public void saveData() throws IOException {
        try (FileOutputStream fos = new FileOutputStream(selectedFile)) {
            new DataSaver().writeSongbook(songbook, fos);
        }
    }

    public void saveToPdf(Alert alert) {
        JFrame parentFrame = new JFrame();
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Wybierz lokalizację");
        FileFilter filter = new FileNameExtensionFilter("PDF file", new String[] {"pdf"});
        fileChooser.setFileFilter(filter);
        if (pdf != null){
            fileChooser.setCurrentDirectory(new java.io.File(pdfPath));
        } else {
            fileChooser.setCurrentDirectory(new java.io.File(selectedFile.getAbsolutePath()));
        }
        int userSelection = fileChooser.showSaveDialog(parentFrame);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            pdf = fileChooser.getSelectedFile();
            pdfPath = pdf.getAbsolutePath();
            System.out.println("Save as file: " + pdfPath);
            if (!pdfPath.endsWith(".pdf")) {
                pdfPath = pdfPath + ".pdf";
            }
            alert.setTitle("Data saved");
            alert.setContentText("The songbook data has been successfully exported to the PDF file!");
            try {
                PdfDocument pdfDocument = new PdfDocument(new PdfWriter(pdfPath));
                new DataSaver().exportSongbookToPdf(songbook, pdfDocument);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            alert.setTitle("Data saving");
            alert.setContentText("Operation cancelled");
        }
    }
}