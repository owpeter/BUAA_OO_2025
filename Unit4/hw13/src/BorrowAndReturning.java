import com.oocourse.library1.LibraryBookId;

import java.util.ArrayList;
import java.util.List;

public class BorrowAndReturning {
    private ArrayList<LibraryBookId> books;

    public BorrowAndReturning() {
        books = new ArrayList<>();
    }

    public void returnBook(LibraryBookId bookId) {
        books.add(bookId);
    }

    public List<LibraryBookId> backToBookshelf() {
        List<LibraryBookId> result = new ArrayList<>(books);
        books.clear();
        return result;
    }
}
