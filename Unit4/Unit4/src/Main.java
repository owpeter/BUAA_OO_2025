import com.oocourse.library2.LibraryBookId;
import com.oocourse.library2.LibraryBookIsbn;
import com.oocourse.library2.LibraryCloseCmd;
import com.oocourse.library2.LibraryCommand;
import com.oocourse.library2.LibraryOpenCmd;
import com.oocourse.library2.LibraryReqCmd;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import static com.oocourse.library2.LibraryIO.PRINTER;
import static com.oocourse.library2.LibraryIO.SCANNER;

public class Main {
    public static void main(String[] args) {
        Map<LibraryBookIsbn, Integer> bookList = SCANNER.getInventory();
        Library library = new Library();
        library.initBook(bookList);
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) { break; }
            LocalDate today = command.getDate();
            if (command instanceof LibraryOpenCmd) {
                PRINTER.move(today, library.arrangeBook(today));
            } else if (command instanceof LibraryCloseCmd) {
                PRINTER.move(today, new ArrayList<>());
                library.endOpenDayActions();
            } else {
                handleReq(library, today, command);
            }
        }
    }

    private static void handleReq(Library library, LocalDate today, LibraryCommand command) {
        LibraryReqCmd req = (LibraryReqCmd) command;
        LibraryReqCmd.Type type = req.getType();
        LibraryBookIsbn bookIsbn = req.getBookIsbn();
        String studentId = req.getStudentId();
        switch (type) {
            case QUERIED:
                LibraryBookId queryBookId = req.getBookId();
                PRINTER.info(today, queryBookId, library.queryBook(queryBookId));
                break;
            case BORROWED:
                LibraryBookId borrowId = library.borrowBook(today, studentId, bookIsbn);
                if (borrowId != null) {
                    PRINTER.accept(req, borrowId);
                } else {
                    PRINTER.reject(req);
                }
                break;
            case ORDERED:
                if (library.orderBook(studentId, bookIsbn)) {
                    PRINTER.accept(req);
                } else {
                    PRINTER.reject(req);
                }
                break;
            case PICKED:
                LibraryBookId apBookId = library.getApBook(today, studentId, bookIsbn);
                if (apBookId != null) {
                    PRINTER.accept(req, apBookId);
                } else {
                    PRINTER.reject(req);
                }
                break;
            case RETURNED:
                LibraryBookId  returnedBookId = req.getBookId();
                library.returnBook(today, studentId, returnedBookId);
                PRINTER.accept(req);
                break;
            case READ:
                LibraryBookId readBookId = library.readBook(today, studentId, bookIsbn);
                if (readBookId != null) {
                    PRINTER.accept(req, readBookId);
                } else {
                    PRINTER.reject(req);
                }
                break;
            case RESTORED:
                LibraryBookId bookId = req.getBookId();
                library.restoreBook(today, studentId, bookId);
                PRINTER.accept(req);
                break;
            default:
                break;
        }
    }

}