import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookState;

public class BookAndSource {
    private final LibraryBookId bookId;
    private final LibraryBookState sourceShelf;

    public BookAndSource(LibraryBookId bookId, LibraryBookState sourceShelf) {
        this.bookId = bookId;
        this.sourceShelf = sourceShelf;
    }

    public LibraryBookId getBookId() {
        return bookId;
    }

    public LibraryBookState getSourceShelf() {
        return sourceShelf;
    }
}