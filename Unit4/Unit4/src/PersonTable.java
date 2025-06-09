import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class PersonTable {
    private final HashMap<String, LocalDate> hasBMap;
    private final HashMap<String, HashMap<LibraryBookIsbn, LocalDate>> hasCMap;
    private final HashSet<String> apSet;
    // Tracks personId -> bookId for books currently being read by the person
    private final HashMap<String, LibraryBookId> curReadMap;
    private final HashMap<String, Integer> creditMap;

    public PersonTable() {
        hasBMap = new HashMap<>();
        hasCMap = new HashMap<>();
        apSet = new HashSet<>();
        curReadMap = new HashMap<>();
        creditMap = new HashMap<>();
    }

    public boolean isOverDue(LocalDate startDate, LocalDate endDate, LibraryBookId bookId) {
        LocalDate dueDate = startDate.plusDays(bookId.isTypeB() ? 30 : 60);
        return endDate.isAfter(dueDate);
    }

    public boolean isOverDue(LocalDate startDate, LocalDate endDate, String type) {
        LocalDate dueDate = startDate.plusDays(type.equals("B") ? 30 : 60);
        return endDate.isAfter(dueDate);
    }

    public Integer calOverDueDays(LocalDate startDate, LocalDate endDate, LibraryBookId bookId) {
        LocalDate dueDate = startDate.plusDays(bookId.isTypeB() ? 30 : 60);
        return endDate.isAfter(dueDate) ? (int) ChronoUnit.DAYS.between(dueDate, endDate)  : 0;
    }

    public Integer calOverDueDays(LocalDate startDate, LocalDate endDate, String type) {
        LocalDate dueDate = startDate.plusDays(type.equals("B") ? 30 : 60);
        return endDate.isAfter(dueDate) ? (int) ChronoUnit.DAYS.between(dueDate, endDate)  : 0;
    }

    public void setCredit(String personId, int delta) {
        int credit = creditMap.getOrDefault(personId, 100);
        if (delta >= 0) {
            credit = Math.min(credit + delta, 180);
            creditMap.put(personId, credit);
        } else {
            credit = Math.max(credit + delta, 0);
            creditMap.put(personId, credit);
        }
    }

    public void addBook(String personId, LibraryBookId bookId, LocalDate startDate) {
        if (bookId.isTypeB()) {
            hasBMap.put(personId, startDate);
        } else if (bookId.isTypeC()) {
            HashMap<LibraryBookIsbn,  LocalDate> cbooks = hasCMap.getOrDefault(personId,
                new HashMap<>());
            cbooks.put(bookId.getBookIsbn(), startDate);
            hasCMap.put(personId, cbooks);
        }
    }

    public boolean removeBook(String personId, LibraryBookId bookId, LocalDate endDate) {
        // 还书
        LocalDate startDate;
        if (bookId.isTypeB()) {
            startDate = hasBMap.get(personId);
            hasBMap.remove(personId);
        } else {
            startDate = hasCMap.get(personId).get(bookId.getBookIsbn());
            hasCMap.get(personId).remove(bookId.getBookIsbn());
            if (hasCMap.get(personId).isEmpty()) {
                hasCMap.remove(personId);
            }
        }

        if (!isOverDue(startDate, endDate, bookId)) {
            setCredit(personId, 10);
            return true;
        } else {
            int delta = -calOverDueDays(startDate, endDate, bookId);
            setCredit(personId, delta * 5);
            return false;
        }
    }

    public boolean canHaveBook(String personId, LibraryBookIsbn bookId) {
        if (bookId.isTypeA()) {
            return false;
        }
        if (bookId.isTypeB()) {
            return !hasBMap.containsKey(personId);
        }
        if (bookId.isTypeC()) {
            return !(hasCMap.containsKey(personId) && hasCMap.get(personId).containsKey(bookId));
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
    public boolean canReadBook(String personId, LibraryBookIsbn isbn, LocalDate today) {
        if (!canCreditRead(personId, isbn, today)) {
            return false;
        }
        return !curReadMap.containsKey(personId);
    }

    public void userStartsReading(String personId, LibraryBookId bookId) {
        curReadMap.put(personId, bookId);
    }

    public void proactiveReturn(String personId, LibraryBookId bookId) {
        if (curReadMap.containsKey(personId)
            && curReadMap.get(personId).equals(bookId)) {
            curReadMap.remove(personId);
            setCredit(personId, 10);
        }
    }

    public void passiveReturn(String personId, LibraryBookId bookId) {
        if (curReadMap.containsKey(personId)
            && curReadMap.get(personId).equals(bookId)) {
            curReadMap.remove(personId);
            setCredit(personId, -10);
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

    // hw15
    public Integer calculateCreditScore(String personId, LocalDate today) {
        int credit = creditMap.getOrDefault(personId, 100);
        if (hasBMap.containsKey(personId)) {
            if (isOverDue(hasBMap.get(personId), today, "B")) {
                int delta = calOverDueDays(hasBMap.get(personId), today, "B");
                credit = Math.max(credit - delta * 5, 0);
            }
        }
        for (Map.Entry<LibraryBookIsbn, LocalDate> entry : hasCMap.getOrDefault(personId,
            new HashMap<>()).entrySet()) {
            if (isOverDue(entry.getValue(), today, "C")) {
                int delta = calOverDueDays(entry.getValue(), today, "C");
                credit = Math.max(credit - delta * 5, 0);
            }
        }
        return credit;
    }

    public void notPickBook(String personId) {
        setCredit(personId, -15);
    }

    public boolean canCreditBorrow(String personId, LocalDate today) {
        return calculateCreditScore(personId, today) >= 60;
    }

    public boolean canCreditAppointment(String personId, LocalDate today) {
        return calculateCreditScore(personId, today) >= 100;
    }

    public boolean canCreditRead(String personId, LibraryBookIsbn isbn, LocalDate today) {
        int credit = calculateCreditScore(personId, today);
        if (isbn.isTypeA()) {
            return credit >= 40;
        } else {
            return credit > 0;
        }
    }

}
