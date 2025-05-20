import com.oocourse.library1.LibraryBookId;

public class Library {
    Bookshelf bookshelf;
    BorrowAndReturning borrowAndReturning;
    AppointmentOffice appointmentOffice;
    PersonTable personTable;

    public Library() {
        bookshelf = new Bookshelf();
        borrowAndReturning = new BorrowAndReturning();
        appointmentOffice = new AppointmentOffice();
        personTable = new PersonTable();
    }

    public boolean borrowBook(String personId, LibraryBookId bookId) {
        if (!bookshelf.hasBook(bookId)) {
            return false;
        }
        if (personTable.canHaveBook(personId, bookId)) {
            personTable.addBook(personId, bookId);
            bookshelf.removeBook(bookId);
            return true;
        } else {
            return false;
        }
    }

}
