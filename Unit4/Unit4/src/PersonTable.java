import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;

import java.util.HashMap;
import java.util.HashSet;

public class PersonTable {
    private final HashSet<String> hasBset;
    private final HashMap<String, HashSet<LibraryBookIsbn>> hasCMap; // 精确到副本
    private final HashSet<String> apSet;

    public PersonTable() {
        hasBset = new HashSet<>();
        hasCMap = new HashMap<>();
        apSet = new HashSet<>();
    }

    public void addBook(String personId, LibraryBookId bookId) {
        if (bookId.isTypeB()) {
            hasBset.add(personId);
        } else if (bookId.isTypeC()) {
            HashSet<LibraryBookIsbn> set = hasCMap.getOrDefault(personId, new HashSet<>());
            set.add(bookId.getBookIsbn());
            hasCMap.put(personId, set);
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

    public void getApBook(String personId) {
        apSet.add(personId);
    }

    public boolean hasApBook(String personId) {
        return apSet.contains(personId);
    }

    public void removeApBook(String personId) {
        apSet.remove(personId);
    }

}
