import com.oocourse.library1.LibraryBookId;

import java.time.LocalDate;

public class ApBook {
    private LocalDate startDate;
    private final LibraryBookId bookId;
    String personId;

    public ApBook(LocalDate startDate,LibraryBookId bookId, String personId) {
        this.bookId = bookId;
        this.personId = personId;
        this.startDate = startDate;
    }

    public String getPersonId() {
        return personId;
    }

    public LibraryBookId getBookId() {
        return bookId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setApDate(LocalDate startDate) {
        this.startDate = startDate;
    }
}
