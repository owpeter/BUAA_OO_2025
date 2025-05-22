import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Bookshelf {
    private final HashMap<LibraryBookIsbn, LinkedList<LibraryBookId>> books;
    private final HashMap<LibraryBookId, ApBook> apMap;

    public Bookshelf() {
        books = new HashMap<>();
        apMap = new HashMap<>();
    }

    public boolean hasBook(LibraryBookIsbn bookId) {
        return !books.get(bookId).isEmpty();
    }

    public void addBook(LibraryBookId bookId) {
        LinkedList<LibraryBookId> list = books.getOrDefault(bookId.getBookIsbn(),
            new LinkedList<>());
        list.add(bookId);
        books.put(bookId.getBookIsbn(), list);
    }

    public LibraryBookId removeBook(LibraryBookIsbn bookIsbn) {
        LibraryBookId bookId = books.get(bookIsbn).remove();
        apMap.remove(bookId);
        return bookId;
    }

    public void removeBook(LibraryBookId bookId) {
        books.get(bookId.getBookIsbn()).remove(bookId);
    }

    public LibraryBookId orderBook(LibraryBookIsbn bookIsbn) {
        // 不删除
        return books.get(bookIsbn).getFirst();
    }

    public LibraryBookId addApBook(String personId,LibraryBookIsbn bookIsbn) {
        LibraryBookId bookId = orderBook(bookIsbn);
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
