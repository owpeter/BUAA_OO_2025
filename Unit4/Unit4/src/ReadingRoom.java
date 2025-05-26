
import com.oocourse.library2.LibraryBookId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReadingRoom {
    // Stores bookId -> personId for books currently in the reading room
    private final HashMap<LibraryBookId, String> booksInRoom;

    public ReadingRoom() {
        booksInRoom = new HashMap<>();
    }

    public void addBook(LibraryBookId bookId, String personId) {
        booksInRoom.put(bookId, personId);
    }

    // Called when a user restores a book or when system clears room during arrange
    public String removeBook(LibraryBookId bookId) {
        return booksInRoom.remove(bookId); // Returns personId who was reading, or null
    }

    public void clearAllBooks() {
        booksInRoom.clear();
    }

    public boolean isBookInRoom(LibraryBookId bookId) {
        return booksInRoom.containsKey(bookId);
    }

    public String getReader(LibraryBookId bookId) {
        return booksInRoom.get(bookId);
    }

    // Used during arrange process to empty the reading room
    public List<Map.Entry<LibraryBookId, String>> clearAndGetBooks() {
        List<Map.Entry<LibraryBookId, String>> currentBooks = new ArrayList<>(booksInRoom.entrySet());
        booksInRoom.clear();
        return currentBooks;
    }

    public HashMap<LibraryBookId, String> getBooksInRoom() {
        return booksInRoom;
    }

    public boolean isEmpty() {
        return booksInRoom.isEmpty();
    }
}