import com.oocourse.spec2.main.PersonInterface;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class MainTest {

    private final String STDIN_FILE = "stdin.txt";
    private final String STDOUT_FILE = "stdout.txt";

    @Test
    public void mainTestWithFileInput() throws Exception {
        // Prepare the content for stdin.txt
        // String inputFileContent = "ap 1 Alice 20\n" +
        //         "ap 2 Bob 25\n" +
        //         "ar 1 2 10\n" +
        //         "qts\n" +
        //         "qcs\n"; // Example commands
        // createFile(STDIN_FILE, inputFileContent);

        // Store original System.in and System.out
        InputStream originalIn = System.in;
        PrintStream originalOut = System.out;

        try (FileInputStream fis = new FileInputStream(STDIN_FILE);
             FileOutputStream fos = new FileOutputStream(STDOUT_FILE);
             PrintStream ps = new PrintStream(fos)) {

            // Redirect System.in to read from stdin.txt
            System.setIn(fis);
            // Redirect System.out to write to stdout.txt
            System.setOut(ps);

            // Call the main method of your program
            // Ensure your Person, Network, and Tag classes are correctly implemented and accessible
            // If they are in a different package, adjust the imports in Main.java or here.
            Main.main(new String[]{});

        } finally {
            // Restore original System.in and System.out
            System.setIn(originalIn);
            System.setOut(originalOut);

            // You can optionally add assertions here to check the content of stdout.txt
            // For example:
            // String expectedOutput = "Ok\nOk\nOk\n0\n0\n"; // Adjust based on your program's logic
            // String actualOutput = readFile(STDOUT_FILE);
            // assertEquals(expectedOutput.trim().replaceAll("\\r\\n", "\n"), actualOutput.trim().replaceAll("\\r\\n", "\n"));

            // Clean up created files (optional)
            // deleteFile(STDIN_FILE);
            // deleteFile(STDOUT_FILE);
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