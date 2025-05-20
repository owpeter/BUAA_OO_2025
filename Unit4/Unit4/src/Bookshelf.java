import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryTrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class Bookshelf {
    private HashMap<LibraryBookIsbn, LinkedList<LibraryBookId>> books;
    private ArrayList<ApBook> apList;

    public Bookshelf() {
        books = new HashMap<>();
        apList = new ArrayList<>();
    }

    public boolean hasBook(LibraryBookIsbn bookId) {
        return !books.get(bookId).isEmpty();
    }

    public void addBook(LibraryBookId bookId) {
        LinkedList<LibraryBookId> set = books.getOrDefault(bookId.getBookIsbn(), new LinkedList<>());
        set.add(bookId);
        books.put(bookId.getBookIsbn(), set);
    }

    public LibraryBookId removeBook(LibraryBookIsbn bookId) {
        return books.get(bookId).remove();
    }

//    public List<LibraryTrace> queryBook(LibraryBookId bookId) {
//        return books.get(bookId);
//    }

    public LibraryBookId addApBook(String personId,LibraryBookIsbn bookIsbn) {
        LibraryBookId bookId = removeBook(bookIsbn);
        ApBook apBook = new ApBook(null, bookId, personId);
        apList.add(apBook);
        return bookId;
    }

    public List<ApBook> removeApBook() {
        List<ApBook> ret = new ArrayList<>(apList);
        apList.clear();
        return ret;
    }


}
