import com.oocourse.library1.LibraryBookId;

import java.util.HashMap;
import java.util.HashSet;

public class PersonTable {
    private HashSet<String> hasBset;
    private HashMap<String, HashSet<LibraryBookId>> hasCMap;
    private HashMap<String, LibraryBookId> apMap;

    public PersonTable() {
        hasBset = new HashSet<>();
        hasCMap = new HashMap<>();
        apMap = new HashMap<>();
    }

    public void addBook(String personId, LibraryBookId bookId) {
        if (bookId.isTypeB()) {
            hasBset.add(personId);
        } else if (bookId.isTypeC()) {
            HashSet<LibraryBookId> set = hasCMap.getOrDefault(personId, new HashSet<>());
            set.add(bookId);
        }
    }

    public void removeBook(String personId, LibraryBookId bookId) {
        if (bookId.isTypeB()) {
            hasBset.remove(personId);
        } else if (bookId.isTypeC()) {
            hasCMap.get(personId).remove(bookId);
            if (hasCMap.get(personId).isEmpty()) {
                hasCMap.remove(personId);
            }
        }
    }

    public boolean canHaveBook(String personId, LibraryBookId bookId) {
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

}
