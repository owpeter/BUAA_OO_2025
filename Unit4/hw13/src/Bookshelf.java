import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryTrace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Bookshelf {
    private HashMap<LibraryBookId, ArrayList<LibraryTrace>> books;
    private ArrayList<LibraryBookId> apList;

    // TODO: no libtrace is added!

    public Bookshelf() {
        books = new HashMap<>();
        apList = new ArrayList<>();
    }

    public boolean hasBook(LibraryBookId bookId) {
        return books.containsKey(bookId);
    }

    public void addBook(LibraryBookId bookId) {
        books.put(bookId, new ArrayList<>());
    }

    public void removeBook(LibraryBookId bookId) {
        books.remove(bookId);
    }

    public List<LibraryTrace> queryBook(LibraryBookId bookId) {
        return books.get(bookId);
    }

    public void addApBook(LibraryBookId bookId) {
        apList.add(bookId);
    }

    public List<LibraryBookId> removeApBook() {
        List<LibraryBookId> ret = new ArrayList<>();
        for (LibraryBookId bookId : apList) {
            ret.add(bookId);
            removeBook(bookId);
        }
        apList.clear();
        return ret;
    }


}
