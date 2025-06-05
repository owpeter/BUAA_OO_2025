import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryBookState;
import com.oocourse.library3.LibraryMoveInfo;
import com.oocourse.library3.LibraryTrace;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Library {
    private final Bookshelf bookshelf;
    private final BorrowAndReturning borrowAndReturning;
    private final AppointmentOffice appointmentOffice;
    private final PersonTable personTable;
    private final HashMap<LibraryBookId, ArrayList<LibraryTrace>> bookTrace;
    private final ReadingRoom readingRoom; // New

    // For hot book management
    private final HashSet<LibraryBookIsbn> hotIsbn;
    private final HashSet<LibraryBookIsbn> borrowedOrReadBooks;

    public Library() {
        bookshelf = new Bookshelf();
        borrowAndReturning = new BorrowAndReturning();
        appointmentOffice = new AppointmentOffice();
        personTable = new PersonTable();
        bookTrace = new HashMap<>();
        readingRoom = new ReadingRoom();

        hotIsbn = new HashSet<>();
        borrowedOrReadBooks = new HashSet<>();
    }

    public void initBook(Map<LibraryBookIsbn, Integer> bookList) {
        for (Map.Entry<LibraryBookIsbn, Integer> entry : bookList.entrySet()) {
            for (int i = 1; i <= entry.getValue(); i++) {
                String copyId = i < 10  ? "0" + i : String.valueOf(i);
                bookshelf.addBook(new LibraryBookId(entry.getKey().getType(),
                    entry.getKey().getUid(), copyId), LibraryBookState.BOOKSHELF);
            }
        }
    }

    private void addTrace(LibraryBookId bookId, LocalDate date,
        LibraryBookState from, LibraryBookState to) {
        ArrayList<LibraryTrace> traces = bookTrace.getOrDefault(bookId, new ArrayList<>());
        traces.add(new LibraryTrace(date, from, to));
        bookTrace.put(bookId, traces);
    }

    public LibraryBookId borrowBook(LocalDate today, String personId, LibraryBookIsbn isbn) {
        if (!bookshelf.hasBook(isbn)) {
            return null;
        }
        if (!personTable.canCreditBorrow(personId)) {
            return null;
        }
        if (personTable.canHaveBook(personId, isbn)) {
            BookAndSource borrowed = bookshelf.removeBookForBorrowOrRead(isbn);
            LibraryBookId bookId = borrowed.getBookId();
            personTable.addBook(personId, bookId, today);
            addTrace(bookId, today, borrowed.getSourceShelf(), LibraryBookState.USER);
            borrowedOrReadBooks.add(bookId.getBookIsbn());
            return bookId;
        } else {
            return null;
        }
    }

    public boolean returnBook(LocalDate today, String personId, LibraryBookId bookId) {
        boolean flag = personTable.removeBook(personId, bookId, today);
        borrowAndReturning.addBook(bookId);
        addTrace(bookId, today, LibraryBookState.USER, LibraryBookState.BORROW_RETURN_OFFICE);
        return flag;
    }

    public boolean orderBook(String personId, LibraryBookIsbn bookIsbn) {
        if (!personTable.canCreditAppointment(personId)) {
            return false;
        }
        if (personTable.canHaveBook(personId, bookIsbn) && !personTable.hasApBook(personId)) {
            // 有副本才借，没副本假装借，使得预约成功但永远不转运，该用户也永远不能再借书
            if (bookshelf.hasBook(bookIsbn)) {
                bookshelf.addApBook(personId, bookIsbn);
                personTable.makesAppointment(personId);
            } else {
                personTable.makesAppointment(personId);
                bookshelf.goWaiting(personId, bookIsbn);
            }
            return true;
        }
        return false;
    }

    public LibraryBookId getApBook(LocalDate today, String personId, LibraryBookIsbn bookIsbn) {
        if (personTable.canHaveBook(personId, bookIsbn)) {
            // appointment available
            LibraryBookId bookId = appointmentOffice.getApBook(today, personId, bookIsbn);
            if (bookId == null) {
                return null;
            }
            personTable.addBook(personId, bookId, today);
            personTable.appointmentCancel(personId);
            // update trace
            addTrace(bookId, today, LibraryBookState.APPOINTMENT_OFFICE, LibraryBookState.USER);
            return bookId;
        }
        return null;
    }

    public List<LibraryTrace> queryBook(LibraryBookId bookId) {
        return bookTrace.getOrDefault(bookId, new ArrayList<>());
    }

    public List<LibraryMoveInfo> arrangeBook(LocalDate today) {
        ArrayList<LibraryMoveInfo> moveInfos = new ArrayList<>();
        bo2bs(today, moveInfos);
        bs2ao(today, moveInfos);
        ao2bs(today, moveInfos);
        rr2bs(today, moveInfos);
        bsReorganization(today, moveInfos);
        // 检查新回到bs的书是否能够满足waitingMap的需求
        bookshelf.checkWaitingList();
        return moveInfos;
    }

    private void bo2bs(LocalDate today, ArrayList<LibraryMoveInfo> moveInfos) {
        List<LibraryBookId> books = borrowAndReturning.clearAndGetBooks();
        for (LibraryBookId bookId : books) {
            LibraryBookState targetShelf = hotIsbn.contains(bookId.getBookIsbn()) ?
                LibraryBookState.HOT_BOOKSHELF : LibraryBookState.BOOKSHELF;
            addTrace(bookId, today, LibraryBookState.BORROW_RETURN_OFFICE, targetShelf);
            moveInfos.add(new LibraryMoveInfo(bookId, LibraryBookState.BORROW_RETURN_OFFICE,
                targetShelf));
            bookshelf.addBook(bookId, targetShelf);
        }
    }

    private void bs2ao(LocalDate today, ArrayList<LibraryMoveInfo> moveInfos) {
        List<ApBook> apBooks = bookshelf.removeApBook();
        for (ApBook apBook : apBooks) {
            LibraryBookId bookId = apBook.getBookId();
            // Remove from actual shelf and get its source location
            LibraryBookState sourceShelf = bookshelf.removeSpecificBookCopy(bookId);
            addTrace(bookId, today, sourceShelf, LibraryBookState.APPOINTMENT_OFFICE);
            moveInfos.add(new LibraryMoveInfo(apBook.getBookId(), sourceShelf,
                LibraryBookState.APPOINTMENT_OFFICE, apBook.getPersonId()));
            appointmentOffice.addApBook(today, apBook);
        }
    }

    private void ao2bs(LocalDate today, ArrayList<LibraryMoveInfo> moveInfos) {
        List<ApBook> apBooks = appointmentOffice.getOutDatedApBooks(today);
        for (ApBook apBook : apBooks) {
            personTable.appointmentCancel(apBook.getPersonId());
            personTable.notPickBook(apBook.getPersonId());
            LibraryBookState targetShelf = hotIsbn.contains(apBook.getBookId().getBookIsbn()) ?
                LibraryBookState.HOT_BOOKSHELF : LibraryBookState.BOOKSHELF;
            addTrace(apBook.getBookId(), today, LibraryBookState.APPOINTMENT_OFFICE, targetShelf);
            moveInfos.add(new LibraryMoveInfo(apBook.getBookId(),
                LibraryBookState.APPOINTMENT_OFFICE, targetShelf));
            bookshelf.addBook(apBook.getBookId(), targetShelf);
        }
    }

    private void rr2bs(LocalDate today, ArrayList<LibraryMoveInfo> moveInfos) {
        List<Map.Entry<LibraryBookId, String>> fromRr = readingRoom.clearAndGetBooks();
        for (Map.Entry<LibraryBookId, String> entry : fromRr) {
            LibraryBookId bookId = entry.getKey();
            String personId = entry.getValue(); // Person who was reading
            personTable.passiveReturn(personId, bookId); // Update person state

            LibraryBookState targetShelf = hotIsbn.contains(bookId.getBookIsbn()) ?
                LibraryBookState.HOT_BOOKSHELF : LibraryBookState.BOOKSHELF;
            bookshelf.addBook(bookId, targetShelf);
            addTrace(bookId, today, LibraryBookState.READING_ROOM, targetShelf);
            moveInfos.add(new LibraryMoveInfo(bookId, LibraryBookState.READING_ROOM, targetShelf));
        }
    }

    private void bsReorganization(LocalDate today, ArrayList<LibraryMoveInfo> moveInfos) {
        List<LibraryBookId> allShelfBooks = bookshelf.getAllBooksOnShelves();
        for (LibraryBookId bookId : allShelfBooks) {
            boolean shouldBeHot = hotIsbn.contains(bookId.getBookIsbn());
            LibraryBookState currentShelf = bookshelf.getBookLocation(bookId);

            if (shouldBeHot && currentShelf == LibraryBookState.BOOKSHELF) {
                bookshelf.moveToHot(bookId);
                addTrace(bookId, today, LibraryBookState.BOOKSHELF, LibraryBookState.HOT_BOOKSHELF);
                moveInfos.add(new LibraryMoveInfo(bookId, LibraryBookState.BOOKSHELF,
                    LibraryBookState.HOT_BOOKSHELF));
            } else if (!shouldBeHot && currentShelf == LibraryBookState.HOT_BOOKSHELF) {
                bookshelf.moveToOrdinary(bookId);
                addTrace(bookId, today, LibraryBookState.HOT_BOOKSHELF, LibraryBookState.BOOKSHELF);
                moveInfos.add(new LibraryMoveInfo(bookId, LibraryBookState.HOT_BOOKSHELF,
                    LibraryBookState.BOOKSHELF));
            }
        }
    }

    // for hw14
    public LibraryBookId readBook(LocalDate today, String personId, LibraryBookIsbn isbn) {
        if (!personTable.canReadBook(personId, isbn)) {
            return null;
        }
        if (!bookshelf.hasBook(isbn)) {
            return null;
        }

        BookAndSource readFrom = bookshelf.removeBookForBorrowOrRead(isbn);
        if (readFrom == null) {
            return null;
        }

        readingRoom.addBook(readFrom.getBookId(), personId);
        personTable.userStartsReading(personId, readFrom.getBookId());
        addTrace(readFrom.getBookId(), today, readFrom.getSourceShelf(),
            LibraryBookState.READING_ROOM);

        borrowedOrReadBooks.add(readFrom.getBookId().getBookIsbn());
        return readFrom.getBookId();
    }

    public void restoreBook(LocalDate today, String personId, LibraryBookId bookId) {
        readingRoom.removeBook(bookId); // Remove from RR's direct tracking
        personTable.proactiveReturn(personId, bookId); // Update person's state
        borrowAndReturning.addBook(bookId); // Goes to BRO
        addTrace(bookId, today, LibraryBookState.READING_ROOM,
            LibraryBookState.BORROW_RETURN_OFFICE);
    }

    // Called at the END of a CLOSE command, after processing requests for the day
    public void endOpenDayActions() {
        // 更新hot book
        hotIsbn.clear();
        hotIsbn.addAll(borrowedOrReadBooks);
        borrowedOrReadBooks.clear();
    }

    // for hw15
    public int queryCreditScore(String personId, LocalDate today) {
        return personTable.calculateCreditScore(personId, today);
    }

}
