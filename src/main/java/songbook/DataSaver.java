package songbook;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;

import songbook.data.*;

/**
 * Created by pwilkin on 13-Dec-18.
 */
public class DataSaver {

    public void exportSongbookToPdf(Songbook songbook, PdfDocument pdfDocument) throws IOException {
        PdfExport pdfExport = new PdfExport(songbook, pdfDocument);
        pdfExport.export();
    }

    public static class DataException extends Exception {
        public DataException(String reason) {
            super(reason);
        }
    }

    public void writeSongbook(Songbook songbook, OutputStream os) {
        StringBuilder sb = new StringBuilder();
        for (Artist artist : songbook.getArtists()) {
            sb.append("###\n").append(artist.getName()).append("\n");
            for (Album album : artist.getAlbums()) {
                sb.append("##\n").append(album.getName()).append("\n");
                for (Song song : album.getSongs()) {
                    sb.append("#\n").append(song.getTitle()).append("#").append(
                        Arrays.stream(song.getLyrics().split("\n")).map(x -> x.replaceAll("#", "\\#"))
                            .collect(Collectors.joining("#"))
                    ).append("\n");
                }
            }
        }
        PrintWriter pw = new PrintWriter(new OutputStreamWriter(os));
        pw.print(sb.toString());
        pw.flush();
    }

    public Songbook readSongbook(InputStream is) throws DataException {
        BufferedReader bf = new BufferedReader(new InputStreamReader(is));
        Songbook book = new Songbook();
        book.setArtists(new ArrayList<>());
        String line;
        boolean readingArtistName = false;
        boolean readingAlbumName = false;
        boolean readingSong = false;
        Artist currentArtist = null;
        Album currentAlbum = null;
        try {
            int i = 1;
            while ((line = bf.readLine()) != null) {
                // line = line.trim(); - naprawiamy linie ze spacjami na końcu
                if (readingArtistName) {
                    currentArtist = new Artist();
                    currentArtist.setName(line);
                    currentArtist.setAlbums(new ArrayList<>());
                    book.getArtists().add(currentArtist);
                    readingArtistName = false;
                } else if (readingAlbumName) {
                    currentAlbum = new Album();
                    currentAlbum.setName(line);
                    currentAlbum.setSongs(new ArrayList<>());
                    currentArtist.getAlbums().add(currentAlbum);
                    readingAlbumName = false;
                } else if (readingSong) {
                    currentAlbum.getSongs().add(readSong(line));
                    readingSong = false;
                } else {
                    if ("###".equals(line)) {
                        readingArtistName = true;
                    } else if ("##".equals(line)) {
                        readingAlbumName = true;
                    } else if ("#".equals(line)) {
                        readingSong = true;
                    } else {
                        throw new DataException("Error in line " + i + ", expected ###, ## or #, got \"" + line + "\"");
                    }
                }
                i++;
            }
        } catch (IOException e) {
            throw new DataException("Error reading from file: " + e.getMessage());
        }
        return book;
    }

    private Song readSong(String line) {
        boolean readingTitle = true;
        boolean inEscape = false;
        StringBuilder current = new StringBuilder();
        Song song = new Song();
        for (int i = 0; i < line.length(); i++) {
            String cur = line.substring(i, i + 1);
            if (inEscape) {
                current.append(cur);
                inEscape = false;
            } else {
                if ("\\".equals(cur)) {
                    inEscape = true;
                } else if ("#".equals(cur)) {
                    if (readingTitle) {
                        song.setTitle(current.toString());
                        song.setLyrics("");
                        readingTitle = false;
                    } else {
                        song.setLyrics(song.getLyrics() + current.toString() + "\n");
                    }
                    current = new StringBuilder();
                } else {
                    current.append(cur);
                }
            }
        }
        song.setLyrics(song.getLyrics() + current.toString());
        return song;
    }

}
