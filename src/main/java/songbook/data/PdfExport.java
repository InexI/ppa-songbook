package songbook.data;

import com.itextpdf.io.IOException;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.VerticalAlignment;

public class PdfExport {
    private Songbook songbook;
    private PdfDocument pdf;
    private PdfFont bold;

    public PdfExport(Songbook songbook, PdfDocument pdfDocument) {
        this.songbook = songbook;
        this.pdf = pdfDocument;
    }

    public void export() {
        setBold();

        try (Document document = new Document(pdf)) {
            for (Artist artist : songbook.getArtists()) {
                newTitleParagraph(artist.getName(), document, 35);
                for (Album album : artist.getAlbums()) {
                    newTitleParagraph(album.getName(), document, 23);
                    for (Song song : album.getSongs()) {
                        Paragraph tpara = new Paragraph();
                        tpara.setFont(bold);
                        tpara.add(new Text(song.getTitle()).setFontSize(15.0f));
                        document.add(tpara);
                        document.add(new Paragraph(song.getLyrics()));
                        document.add(new AreaBreak());
                    }
                }
            }
        }
    }

    public void newTitleParagraph(String title, Document document, int font){
        Paragraph p = new Paragraph();
        p.setFont(bold);
        p.add(new Text(title));
        p.setFontSize(font);
        p.setTextAlignment(TextAlignment.CENTER);

        Cell c = new Cell();
        c.setWidth(200);
        c.setHeight(400);
        c.setVerticalAlignment(VerticalAlignment.MIDDLE);
        c.setHorizontalAlignment(HorizontalAlignment.CENTER);
        c.add(p);
        document.add(c);
        document.add(new AreaBreak());
    }

    public void setBold() {
        try {
            bold = PdfFontFactory.createFont(StandardFonts.HELVETICA_BOLD);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

}
