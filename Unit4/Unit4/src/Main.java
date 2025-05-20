import com.oocourse.library1.LibraryBookId;
import com.oocourse.library1.LibraryBookIsbn;
import com.oocourse.library1.LibraryCloseCmd;
import com.oocourse.library1.LibraryCommand;
import com.oocourse.library1.LibraryOpenCmd;
import com.oocourse.library1.LibraryReqCmd;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Map;

import static com.oocourse.library1.LibraryIO.PRINTER;
import static com.oocourse.library1.LibraryIO.SCANNER;

public class Main {
    public static void main(String[] args) {
        Map<LibraryBookIsbn, Integer> bookList = SCANNER.getInventory();	// 获取图书馆内所有书籍ISBN号及相应副本数
        Library library = new Library();
        library.initBook(bookList);
        while (true) {
            LibraryCommand command = SCANNER.nextCommand();
            if (command == null) { break; }
            LocalDate today = command.getDate(); // 今天的日期
            if (command instanceof LibraryOpenCmd) {
                // 在开馆时做点什么
                 PRINTER.move(today, library.arrangeBook(today));
            } else if (command instanceof LibraryCloseCmd) {
                // 在闭馆时做点什么
                PRINTER.move(today, new ArrayList<>());
            } else {
                LibraryReqCmd req = (LibraryReqCmd) command;
                LibraryReqCmd.Type type = req.getType(); // 指令对应的类型（查询/阅读/借阅/预约/还书/取书/归还）
                LibraryBookIsbn bookIsbn = req.getBookIsbn(); // 指令对应的书籍ISBN号（type-uid）
//                LibraryBookId bookId = req.getBookId(); // 指令对应书籍编号（type-uid-copyId）
                String studentId = req.getStudentId(); // 指令对应的用户Id
                // 对指令进行处理
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
                    default:
                        break;
                }
            }
        }
    }

}