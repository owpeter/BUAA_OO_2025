import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryBookState;
import com.oocourse.library2.annotation.Trigger;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Bookshelf {
    // ordinaryBooks are on LibraryBookState.BOOKSHELF
    private final HashMap<LibraryBookIsbn, ArrayList<LibraryBookId>> ordinaryBooks;
    // hotBooks are on LibraryBookState.HOT_BOOKSHELF
    private final HashMap<LibraryBookIsbn, ArrayList<LibraryBookId>> hotBooks;
    // To quickly find which shelf a specific book copy is on
    private final HashMap<LibraryBookId, LibraryBookState> bookLocationCache;
    private final HashMap<LibraryBookId, ApBook> apMap;
    private final HashMap<LibraryBookIsbn, Deque<String>> waitingMap;

    public Bookshelf() {
        ordinaryBooks = new HashMap<>();
        hotBooks = new HashMap<>();
        bookLocationCache = new HashMap<>();
        apMap = new HashMap<>();
        waitingMap = new HashMap<>();
    }

    public boolean hasBook(LibraryBookIsbn isbn) {
        return (hotBooks.containsKey(isbn) && !hotBooks.get(isbn).isEmpty()) ||
                (ordinaryBooks.containsKey(isbn) && !ordinaryBooks.get(isbn).isEmpty());
    }

    @Trigger(from = "nUser", to = "User")
    public void addBook(LibraryBookId bookId, LibraryBookState shelfType) {
        HashMap<LibraryBookIsbn, ArrayList<LibraryBookId>> targetShelfMap =
            shelfType == LibraryBookState.HOT_BOOKSHELF ? hotBooks : ordinaryBooks;
        ArrayList<LibraryBookId> list = targetShelfMap.getOrDefault(bookId.getBookIsbn(),
            new ArrayList<>());
        if (!list.contains(bookId)) { // Avoid duplicates if logic error elsewhere
            list.add(bookId);
            targetShelfMap.put(bookId.getBookIsbn(), list);
            bookLocationCache.put(bookId, shelfType);
        }
    }

    public void checkWaitingList() {
        Iterator<LibraryBookIsbn> iterator = waitingMap.keySet().iterator();
        while (iterator.hasNext()) {
            LibraryBookIsbn bookIsbn = iterator.next();
            if (hasBook(bookIsbn)) {
                LibraryBookId bookId = findBookForAppointment(bookIsbn);
                String personId = waitingMap.get(bookIsbn).remove(); // 从等待队列中移除一个人
                ApBook apBook = new ApBook(null, bookId, personId);
                apMap.put(bookId, apBook);
            }
            // 检查与该 bookIsbn 关联的等待队列是否为空
            if (waitingMap.get(bookIsbn) != null && waitingMap.get(bookIsbn).isEmpty()) {
                iterator.remove(); // 使用迭代器的 remove() 方法删除当前 bookIsbn 条目
            }
        }
    }

    // public LibraryBookId removeBook(LibraryBookIsbn bookIsbn) {
    //     LibraryBookId bookId = books.get(bookIsbn).remove(0);
    //     apMap.remove(bookId);
    //     return bookId;
    // }
    public BookAndSource removeBookForBorrowOrRead(LibraryBookIsbn isbn) {
        LibraryBookId bookId = removeFromShelfMap(isbn, hotBooks);
        if (bookId != null) {
            bookLocationCache.remove(bookId);
            apMap.remove(bookId);
            return new BookAndSource(bookId, LibraryBookState.HOT_BOOKSHELF);
        }
        bookId = removeFromShelfMap(isbn, ordinaryBooks);
        if (bookId != null) {
            bookLocationCache.remove(bookId);
            apMap.remove(bookId);
            return new BookAndSource(bookId, LibraryBookState.BOOKSHELF);
        }
        return null; // No book available
    }

    private LibraryBookId removeFromShelfMap(LibraryBookIsbn isbn, HashMap<LibraryBookIsbn,
        ArrayList<LibraryBookId>> shelfMap) {
        if (shelfMap.containsKey(isbn) && !shelfMap.get(isbn).isEmpty()) {
            LibraryBookId bookId = shelfMap.get(isbn).remove(0);
            if (shelfMap.get(isbn).isEmpty()) {
                shelfMap.remove(isbn);
            }
            return  bookId;
        }
        return null;
    }

    public LibraryBookState removeSpecificBookCopy(LibraryBookId bookId) {
        LibraryBookState sourceState = bookLocationCache.remove(bookId);
        if (sourceState == null) {
            return null; // Book not found on shelves
        }

        HashMap<LibraryBookIsbn, ArrayList<LibraryBookId>> sourceMap =
            (sourceState == LibraryBookState.HOT_BOOKSHELF) ? hotBooks : ordinaryBooks;

        if (sourceMap.containsKey(bookId.getBookIsbn())) {
            sourceMap.get(bookId.getBookIsbn()).remove(bookId);
            if (sourceMap.get(bookId.getBookIsbn()).isEmpty()) {
                sourceMap.remove(bookId.getBookIsbn());
            }
            return sourceState;
        }
        throw new RuntimeException("Book not found on sourceMap");
    }

    private LibraryBookId findBookForAppointment(LibraryBookIsbn bookIsbn) {
        // Try to give a copy not already in apMap from hot books
        if (hotBooks.containsKey(bookIsbn)) {
            for (LibraryBookId bookId : hotBooks.get(bookIsbn)) {
                if (!apMap.containsKey(bookId)) {
                    return bookId;
                }
            }
        }
        // Try to give a copy not already in apMap from ordinary books
        if (ordinaryBooks.containsKey(bookIsbn)) {
            for (LibraryBookId bookId : ordinaryBooks.get(bookIsbn)) {
                if (!apMap.containsKey(bookId)) {
                    return bookId;
                }
            }
        }
        if (hotBooks.containsKey(bookIsbn) && !hotBooks.get(bookIsbn).isEmpty()) {
            return hotBooks.get(bookIsbn).get(0);
        }
        if (ordinaryBooks.containsKey(bookIsbn) && !ordinaryBooks.get(bookIsbn).isEmpty()) {
            return ordinaryBooks.get(bookIsbn).get(0);
        }
        return null;
    }

    public void addApBook(String personId, LibraryBookIsbn bookIsbn) {
        if (!hasBook(bookIsbn)) {
            return;
        }
        LibraryBookId bookId = findBookForAppointment(bookIsbn);
        ApBook apBook = new ApBook(null, bookId, personId);
        if (apMap.containsKey(bookId)) {
            // 当前副本有人已经预定了，那么将当前人加入等待队列中
            goWaiting(personId, bookIsbn);
        } else {
            apMap.put(bookId, apBook);
        }
    }

    public void goWaiting(String personId, LibraryBookIsbn bookId) {
        Deque<String> deque = waitingMap.getOrDefault(bookId, new LinkedList<>());
        deque.add(personId);
        waitingMap.put(bookId, deque);
    }

    public List<ApBook> removeApBook() {
        List<ApBook> booksToMove = new ArrayList<>(apMap.values());
        apMap.clear();
        return booksToMove;
    }

    // for hw14

    public LibraryBookState getBookLocation(LibraryBookId bookId) {
        return bookLocationCache.get(bookId);
    }

    public List<LibraryBookId> getAllBooksOnShelves() {
        List<LibraryBookId> allBooks = new ArrayList<>();
        for (ArrayList<LibraryBookId> list : hotBooks.values()) {
            allBooks.addAll(list);
        }
        for (ArrayList<LibraryBookId> list : ordinaryBooks.values()) {
            allBooks.addAll(list);
        }
        return allBooks;
    }

    public void moveToHot(LibraryBookId bookId) {
        if (bookLocationCache.get(bookId) == LibraryBookState.BOOKSHELF) {
            ordinaryBooks.get(bookId.getBookIsbn()).remove(bookId);
            if (ordinaryBooks.get(bookId.getBookIsbn()).isEmpty()) {
                ordinaryBooks.remove(bookId.getBookIsbn());
            }
            addBook(bookId, LibraryBookState.HOT_BOOKSHELF);
            bookLocationCache.put(bookId, LibraryBookState.HOT_BOOKSHELF);
        }
    }

    public void moveToOrdinary(LibraryBookId bookId) {
        if (bookLocationCache.get(bookId) == LibraryBookState.HOT_BOOKSHELF) {
            hotBooks.get(bookId.getBookIsbn()).remove(bookId);
            if (hotBooks.get(bookId.getBookIsbn()).isEmpty()) {
                hotBooks.remove(bookId.getBookIsbn());
            }
            addBook(bookId, LibraryBookState.BOOKSHELF);
            bookLocationCache.put(bookId, LibraryBookState.BOOKSHELF);
        }
    }
}
