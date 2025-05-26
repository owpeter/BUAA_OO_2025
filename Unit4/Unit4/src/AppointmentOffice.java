import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AppointmentOffice {
    private final ArrayList<ApBook> apBooks;

    public AppointmentOffice() {
        apBooks = new ArrayList<>();
    }

    public void addApBook(LocalDate today, ApBook apBook) {
        apBook.setApDate(today);
        apBooks.add(apBook);
    }

    public LibraryBookId getApBook(LocalDate pickupAttemptDate, String userId,
        LibraryBookIsbn targetBookId) {
        int bestMatchIndex = -1;
        long largestQualifyingDaysDiff = -1;

        for (int i = 0; i < apBooks.size(); ++i) {
            ApBook currentAppointment = apBooks.get(i);
            boolean userMatches = currentAppointment.getPersonId().equals(userId);
            boolean bookMatches = currentAppointment.getBookId().getBookIsbn().equals(targetBookId);
            if (!userMatches || !bookMatches) {
                continue;
            }
            long daysDifference = Math.abs(ChronoUnit.DAYS.between(pickupAttemptDate,
                currentAppointment.getStartDate()));
            if (daysDifference < 5) {
                if (daysDifference > largestQualifyingDaysDiff) {
                    largestQualifyingDaysDiff = daysDifference;
                    bestMatchIndex = i;
                }
            }
        }
        if (bestMatchIndex != -1) {
            ApBook apBook = apBooks.remove(bestMatchIndex); // 从借还台删掉
            return apBook.getBookId();
        }
        return null;
    }

    public List<ApBook> getOutDatedApBooks(LocalDate today) {
        ArrayList<ApBook> outDatedBooks = new ArrayList<>();
        Iterator<ApBook> iterator = apBooks.iterator();
        while (iterator.hasNext()) {
            ApBook apBook = iterator.next();
            long daysDifference = ChronoUnit.DAYS.between(apBook.getStartDate(), today);
            if (daysDifference >= 5) {
                outDatedBooks.add(apBook);
                iterator.remove();
            }
        }
        return outDatedBooks;
    }

}
