import com.oocourse.library1.LibraryBookId;

import java.time.LocalDate;

public class ApBook {
    private LocalDate apDate;
    private LibraryBookId bookId;
    String personId;

    public ApBook(LocalDate apDate,LibraryBookId bookId, String personId) {
        this.bookId = bookId;
        this.personId = personId;
        this.apDate = apDate;
    }
}
