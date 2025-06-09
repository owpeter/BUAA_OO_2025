import com.oocourse.library3.LibraryBookId;
import com.oocourse.library3.LibraryBookIsbn;
import com.oocourse.library3.LibraryCloseCmd;
import com.oocourse.library3.LibraryCommand;
import com.oocourse.library3.LibraryOpenCmd;
import com.oocourse.library3.LibraryQcsCmd;
import com.oocourse.library3.LibraryReqCmd;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import static com.oocourse.library3.LibraryIO.PRINTER;
import static com.oocourse.library3.LibraryIO.SCANNER;

public class Main {
    public static void main(String[] args) {
        Map<LibraryBookIsbn, Integer> bookList = SCANNER.getInventory();
        Library library = new Library();
        library.initBook(bookList);
        LocalDate lastOpenDay = null;
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) { break; }
            LocalDate today = command.getDate();
            if (command instanceof LibraryOpenCmd) {
                PRINTER.move(today, library.arrangeBook(today));
            } else if (command instanceof LibraryCloseCmd) {
                PRINTER.move(today, new ArrayList<>());
                library.endOpenDayActions();
                System.out.println("[debug]" + library.queryCreditScore("23370041", today));
            } else {
                handleReq(library, today, command);
            }

        }
    }

    private static void handleReq(Library library, LocalDate today, LibraryCommand command) {
        if (command instanceof LibraryQcsCmd) {
            PRINTER.info(command,
                library.queryCreditScore(((LibraryQcsCmd) command).getStudentId(), today));
            return;
        }
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
                if (library.orderBook(studentId, bookIsbn, today)) {
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
                handleReturn(library, today, req);
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

    private static void handleReturn(Library library, LocalDate today, LibraryReqCmd req) {
        LibraryBookId  returnedBookId = req.getBookId();
        boolean flag = library.returnBook(today, req.getStudentId(), returnedBookId);
        if (flag) {
            PRINTER.accept(req, "not overdue");
        } else {
            PRINTER.accept(req, "overdue");
        }
    }

}