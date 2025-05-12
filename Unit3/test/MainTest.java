import com.oocourse.spec3.main.PersonInterface;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

public class MainTest {

    private final String STDIN_FILE = "stdin.txt";
    private final String STDOUT_FILE = "stdout.txt";

    @Test
    public void mainTestWithFileInput() throws Exception {
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        try (FileInputStream fis = new FileInputStream(STDIN_FILE);
             FileOutputStream fos = new FileOutputStream(STDOUT_FILE);
             PrintStream ps = new PrintStream(fos)) {
            System.setIn(fis);
            System.setOut(ps);
            Main.main(new String[]{});

        } finally {
            System.setIn(originalIn);
            System.setOut(originalOut);
        }
    }

    private void createFile(String fileName, String content) throws IOException {
        Files.write(Paths.get(fileName), content.getBytes());
    }

    private String readFile(String fileName) throws IOException {
        return new String(Files.readAllBytes(Paths.get(fileName)));
    }

    private void deleteFile(String fileName) throws IOException {
        Files.deleteIfExists(Paths.get(fileName));
    }
}