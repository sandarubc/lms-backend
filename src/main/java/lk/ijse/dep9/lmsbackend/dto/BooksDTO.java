package lk.ijse.dep9.lmsbackend.dto;


import jakarta.json.bind.annotation.JsonbPropertyOrder;
import java.io.Serializable;

@JsonbPropertyOrder({"isbn","title","author","copies"})
public class BooksDTO implements Serializable {

    private String isbn;

    private String title;

    private String author;

    private int copies;

    public BooksDTO() {
    }

    public BooksDTO(String isbn, String title, String author, int copies) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.copies = copies;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public int getCopies() {
        return copies;
    }

    public void setCopies(int copies) {
        this.copies = copies;
    }

    @Override
    public String toString() {
        return "BooksDTO{" +
                "isbn='" + isbn + '\'' +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", copies=" + copies +
                '}';
    }
}
