import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;

import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class Bookshelf {
    private final HashMap<LibraryBookIsbn, ArrayList<LibraryBookId>> books;
    private final HashMap<LibraryBookId, ApBook> apMap;
    private final HashMap<LibraryBookIsbn, Deque<String>> waitingMap;

    public Bookshelf() {
        books = new HashMap<>();
        apMap = new HashMap<>();
        waitingMap = new HashMap<>();
    }

    public boolean hasBook(LibraryBookIsbn bookId) {
        return !books.get(bookId).isEmpty();
    }

    public void addBook(LibraryBookId bookId) {
        ArrayList<LibraryBookId> list = books.getOrDefault(bookId.getBookIsbn(),
            new ArrayList<>());
        list.add(bookId);
        books.put(bookId.getBookIsbn(), list);
    }

    public void checkWaitingList() {
        Iterator<LibraryBookIsbn> iterator = waitingMap.keySet().iterator();
        while (iterator.hasNext()) {
            LibraryBookIsbn bookIsbn = iterator.next(); // 首先获取元素
            if (hasBook(bookIsbn)) {
                LibraryBookId bookId = orderBook(bookIsbn);
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

    public LibraryBookId removeBook(LibraryBookIsbn bookIsbn) {
        LibraryBookId bookId = books.get(bookIsbn).remove(0);
        apMap.remove(bookId);
        return bookId;
    }

    public void removeBook(LibraryBookId bookId) {
        books.get(bookId.getBookIsbn()).remove(bookId);
    }

    public LibraryBookId orderBook(LibraryBookIsbn bookIsbn) {
        // 尽量给不在apMap中的副本
        for (int i = 0; i < books.get(bookIsbn).size(); i++) {
            if (!apMap.containsKey(books.get(bookIsbn).get(i))) {
                return books.get(bookIsbn).get(i);
            }
        }
        // 全在apMap中，则给第一个副本
        return books.get(bookIsbn).get(0);
    }

    public void addApBook(String personId, LibraryBookIsbn bookIsbn) {
        LibraryBookId bookId = orderBook(bookIsbn);
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
        List<ApBook> ret = new ArrayList<>();
        for (ApBook apBook : apMap.values()) {
            ret.add(apBook);
            removeBook(apBook.getBookId());
        }
        apMap.clear();
        return ret;
    }
}
