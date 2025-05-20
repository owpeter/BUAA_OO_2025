import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryBookState;
import com.oocourse.library1.LibraryMoveInfo;
import com.oocourse.library1.LibraryTrace;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Library {
    Bookshelf bookshelf;
    BorrowAndReturning borrowAndReturning;
    AppointmentOffice appointmentOffice;
    PersonTable personTable;
    HashMap<LibraryBookId, ArrayList<LibraryTrace>> bookTrace;

    public Library() {
        bookshelf = new Bookshelf();
        borrowAndReturning = new BorrowAndReturning();
        appointmentOffice = new AppointmentOffice();
        personTable = new PersonTable();
        bookTrace = new HashMap<>();
    }

    public void initBook(Map<LibraryBookIsbn, Integer> bookList) {
        for (Map.Entry<LibraryBookIsbn, Integer> entry : bookList.entrySet()) {
            for (int i = 1; i <= entry.getValue(); i++) {
                bookshelf.addBook(new LibraryBookId(entry.getKey().getType(), entry.getKey().getUid(), String.valueOf(i)));
            }
        }
    }

    public LibraryBookId borrowBook(LocalDate today, String personId, LibraryBookIsbn isbn) {
        if (!bookshelf.hasBook(isbn)) {
            return null;
        }
        if (personTable.canHaveBook(personId, isbn)) {

            LibraryBookId bookId = bookshelf.removeBook(isbn);
            personTable.addBook(personId, bookId);
            ArrayList<LibraryTrace> trace = bookTrace.getOrDefault(bookId, new ArrayList<>());
            trace.add(new LibraryTrace(today, LibraryBookState.BOOKSHELF, LibraryBookState.USER));
            bookTrace.put(bookId, trace);
            return bookId;
        } else {
            return null;
        }
    }

    public void returnBook(LocalDate today, String personId, LibraryBookId bookId) {
        personTable.removeBook(personId, bookId);
        borrowAndReturning.addBook(bookId);
        ArrayList<LibraryTrace> trace = bookTrace.getOrDefault(bookId, new ArrayList<>());
        trace.add(new LibraryTrace(today, LibraryBookState.USER, LibraryBookState.BORROW_RETURN_OFFICE));
        bookTrace.put(bookId, trace);
    }

    public boolean orderBook(String personId, LibraryBookIsbn bookIsbn) {
        if (personTable.canHaveBook(personId, bookIsbn) && !personTable.hasApBook(personId) && bookshelf.hasBook(bookIsbn)) {
            LibraryBookId bookId = bookshelf.addApBook(personId, bookIsbn);
            personTable.addApBook(personId, bookId);
            return true;
        }
        return false;
    }

    public LibraryBookId getApBook(LocalDate today, String personId, LibraryBookIsbn bookIsbn) {
        if (personTable.canHaveBook(personId, bookIsbn)) {
            // appointment available
            LibraryBookId bookId = appointmentOffice.getApBook(today, personId, bookIsbn);
            personTable.addBook(personId, bookId);
            // update trace
            ArrayList<LibraryTrace> trace = bookTrace.getOrDefault(bookId, new ArrayList<>());
            trace.add(new LibraryTrace(today, LibraryBookState.APPOINTMENT_OFFICE, LibraryBookState.USER));
            bookTrace.put(bookId, trace);
            return bookId;
        }
        return null;
    }

    public List<LibraryTrace> queryBook(LibraryBookId bookId) {
        return bookTrace.getOrDefault(bookId, new ArrayList<>());
    }

    public List<LibraryMoveInfo> arrangeBook(LocalDate today) {
        ArrayList<LibraryMoveInfo> moveInfos = new ArrayList<>();
        // bo -> bs
        List<LibraryBookId> books = borrowAndReturning.backToBookshelf();
        for (LibraryBookId bookId : books) {
            ArrayList<LibraryTrace> trace = bookTrace.getOrDefault(bookId, new ArrayList<>());
            trace.add(new LibraryTrace(today, LibraryBookState.BORROW_RETURN_OFFICE, LibraryBookState.BOOKSHELF));
            bookTrace.put(bookId, trace);
            moveInfos.add(new LibraryMoveInfo(bookId, LibraryBookState.BORROW_RETURN_OFFICE, LibraryBookState.BOOKSHELF));
            bookshelf.addBook(bookId);
        }
        // bs -> ao
        List<ApBook> apBooks = bookshelf.removeApBook();
        for (ApBook apBook : apBooks) {
            ArrayList<LibraryTrace> trace = bookTrace.getOrDefault(apBook.getBookId(), new ArrayList<>());
            trace.add(new LibraryTrace(today, LibraryBookState.BOOKSHELF, LibraryBookState.APPOINTMENT_OFFICE));
            bookTrace.put(apBook.getBookId(), trace);
            moveInfos.add(new LibraryMoveInfo(apBook.getBookId(), LibraryBookState.BOOKSHELF, LibraryBookState.APPOINTMENT_OFFICE));
            appointmentOffice.addApBook(today, apBook);
        }
        // ao -> bs
        List<ApBook> apBooks2 = appointmentOffice.getOutDatedApBooks(today);
        for (ApBook apBook : apBooks2) {
            personTable.removeApBook(apBook.getPersonId());
            ArrayList<LibraryTrace> trace = bookTrace.getOrDefault(apBook.getBookId(), new ArrayList<>());
            trace.add(new LibraryTrace(today, LibraryBookState.APPOINTMENT_OFFICE, LibraryBookState.BOOKSHELF));
            bookTrace.put(apBook.getBookId(), trace);
            moveInfos.add(new LibraryMoveInfo(apBook.getBookId(), LibraryBookState.APPOINTMENT_OFFICE, LibraryBookState.BOOKSHELF));
            bookshelf.addBook(apBook.getBookId());
        }
        return moveInfos;
    }

}
