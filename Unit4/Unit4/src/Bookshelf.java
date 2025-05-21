import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Bookshelf {
    private HashMap<LibraryBookIsbn, LinkedList<LibraryBookId>> books;
    private final HashMap<LibraryBookId, ApBook> apMap;

    public Bookshelf() {
        books = new HashMap<>();
        apMap = new HashMap<>();
    }

    public boolean hasBook(LibraryBookIsbn bookId) {
        return !books.get(bookId).isEmpty();
    }

    public void addBook(LibraryBookId bookId) {
        LinkedList<LibraryBookId> set = books.getOrDefault(bookId.getBookIsbn(),
            new LinkedList<>());
        set.add(bookId);
        books.put(bookId.getBookIsbn(), set);
    }

    public LibraryBookId removeBook(LibraryBookIsbn bookIsbn) {
        LibraryBookId bookId = books.get(bookIsbn).remove();
        apMap.remove(bookId);
        return bookId;
    }

    public void removeBook(LibraryBookId bookId) {
        books.get(bookId.getBookIsbn()).remove(bookId);
    }

    public LibraryBookId getBook(LibraryBookIsbn bookId) {
        try {
            return books.get(bookId).getFirst();
        } catch (Exception e) {
            return null;
        }
    }

    public LibraryBookId addApBook(String personId,LibraryBookIsbn bookIsbn) {
        // LibraryBookId bookId = removeBook(bookIsbn);
        LibraryBookId bookId = getBook(bookIsbn);
        if (bookId == null) {
            return null;
        }
        ApBook apBook = new ApBook(null, bookId, personId);
        apMap.put(bookId, apBook);
        return bookId;
    }

    public List<ApBook> removeApBook() {
        List<ApBook> ret = new ArrayList<>();
        for (ApBook apBook : apMap.values()) {
            ret.add(apBook);
            removeBook(apBook.getBookId());
        }
        apMap.clear();
        return ret;
    }
}
