import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PersonTable {
    private final HashSet<String> hasBset;
    private final HashMap<String, HashSet<LibraryBookIsbn>> hasCMap;
    private final HashSet<String> apSet;
    // Tracks personId -> bookId for books currently being read by the person
    private final HashMap<String, LibraryBookId> curReadMap;

    public PersonTable() {
        hasBset = new HashSet<>();
        hasCMap = new HashMap<>();
        apSet = new HashSet<>();
        curReadMap = new HashMap<>();
    }

    public void addBook(String personId, LibraryBookId bookId) {
        if (bookId.isTypeB()) {
            hasBset.add(personId);
        } else if (bookId.isTypeC()) {
            HashSet<LibraryBookIsbn> cbooks = hasCMap.getOrDefault(personId, new HashSet<>());
            cbooks.add(bookId.getBookIsbn());
            hasCMap.put(personId, cbooks);
        }
    }

    public void removeBook(String personId, LibraryBookId bookId) {
        if (bookId.isTypeB()) {
            hasBset.remove(personId);
        } else if (bookId.isTypeC()) {
            hasCMap.get(personId).remove(bookId.getBookIsbn());
            if (hasCMap.get(personId).isEmpty()) {
                hasCMap.remove(personId);
            }
        }
    }

    public boolean canHaveBook(String personId, LibraryBookIsbn bookId) {
        if (bookId.isTypeA()) {
            return false;
        }
        if (bookId.isTypeB()) {
            return !hasBset.contains(personId);
        }
        if (bookId.isTypeC()) {
            return !(hasCMap.containsKey(personId) && hasCMap.get(personId).contains(bookId));
        }
        return false;
    }

    public void makesAppointment(String personId) {
        apSet.add(personId);
    }

    public boolean hasApBook(String personId) {
        return apSet.contains(personId);
    }

    public void appointmentCancel(String personId) {
        apSet.remove(personId);
    }

    // Reading related methods
    public boolean canReadBook(String personId) {
        // "若该用户存在当日未阅读后归还的书籍，阅读失败"
        return !curReadMap.containsKey(personId);
    }

    public void userStartsReading(String personId, LibraryBookId bookId) {
        curReadMap.put(personId, bookId);
    }

    // Called when user restores book, or system moves book from RR
    public void userStopsReading(String personId, LibraryBookId bookId) {
        // Only remove if they were reading this specific book
        if (curReadMap.containsKey(personId)
            && curReadMap.get(personId).equals(bookId)) {
            curReadMap.remove(personId);
        }
    }

    public LibraryBookId getReadingBook(String personId) {
        return curReadMap.get(personId);
    }

    // Called when a book is moved from RR by system, not by user's "restore" action
    public void clearReadingSessionForBook(LibraryBookId bookId) {
        String personToRemove = null;
        for (Map.Entry<String, LibraryBookId> entry : curReadMap.entrySet()) {
            if (entry.getValue().equals(bookId)) {
                personToRemove = entry.getKey();
                break;
            }
        }
        if (personToRemove != null) {
            curReadMap.remove(personToRemove);
        }
    }

}
