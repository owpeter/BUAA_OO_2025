import com.oocourse.library1.LibraryBookId;

import java.time.LocalDate;
import java.util.ArrayList;

public class AppointmentOffice {
    private ArrayList<ApBook> apBooks;

    public AppointmentOffice() {
        apBooks = new ArrayList<ApBook>();
    }

    public void addApBook(LocalDate today, LibraryBookId bookId, String personId) {
        ApBook apBook = new ApBook(today, bookId, personId);
        apBooks.add(apBook);
    }
}
