import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class MainClassTest {

    @org.junit.Test
    public void testMain() throws IOException, InterruptedException {
        final Set<Thread> beforeThreads = new HashSet<>(Thread.getAllStackTraces().keySet());
        final String projectRoot = System.getProperty("user.dir");
        final Path testFilePath = Paths.get(projectRoot, "stdin.txt");
        // 建立 Piped 流，用以将数据写入 System.in
        final PipedInputStream inPipe = new PipedInputStream();
        final PipedOutputStream outPipe = new PipedOutputStream(inPipe);
        // 重定向 System.in 为 piped 输入流
        System.setIn(inPipe);

        // 启动一个线程，根据 stdin.txt 中的时间延迟写入数据到 outPipe
        Thread inputSimulator = new Thread(() -> {
            try {
                // 读取测试文件（请确保文件存在于项目工作目录中）
                List<String> lines = Files.readAllLines(testFilePath, StandardCharsets.UTF_8);
                long startTime = System.currentTimeMillis();
                for (String line : lines) {
                    if (line.trim().isEmpty()) {
                        break; // 遇到空行则结束模拟输入
                    }
                    // 解析行格式：假设格式为 [time] command
                    int endBracket = line.indexOf("]");
                    if (endBracket == -1) {
                        continue; // 如果无法解析则跳过
                    }
                    // 取出时间值，单位为秒
                    String timeString = line.substring(1, endBracket);
                    double commandTime = Double.parseDouble(timeString);
                    // 取出命令部分并去除多余空格
                    String command = line.substring(endBracket + 1).trim();

                    // 等待直到达到发送该命令的时间
                    long elapsed = System.currentTimeMillis() - startTime;
                    long targetTimeInMillis = (long) (commandTime * 1000);
                    if (elapsed < targetTimeInMillis) {
                        Thread.sleep(targetTimeInMillis - elapsed);
                    }
                    // 将命令写入 outPipe（需要加换行符，确保 MainClass.main() 能正确读取）
                    outPipe.write((command + "\n").getBytes(StandardCharsets.UTF_8));
                    outPipe.flush();
                }
            } catch (Exception e) {
                e.printStackTrace(System.err);
            } finally {
                try {
                    outPipe.close();
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        });
        inputSimulator.start();
        // 创建一个线程来执行MainClass.main方法
        Thread mainThread = new Thread(() -> Main.main(new String[0]));
        mainThread.start();

        // 等待输入模拟器和主线程完成
        mainThread.join();
        inputSimulator.join();

        Set<Thread> afterThreads = new HashSet<>(Thread.getAllStackTraces().keySet());
        afterThreads.removeAll(beforeThreads);
        List<Thread> userThreads = new ArrayList<>();
        for (Thread t : afterThreads) {
            if (t.isAlive() && !t.isDaemon()) {
                userThreads.add(t);
            }
        }
        // 循环等待这些线程结束
        while (!userThreads.isEmpty()) {
            Iterator<Thread> iterator = userThreads.iterator();
            while (iterator.hasNext()) {
                Thread t = iterator.next();
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                if (!t.isAlive()) {
                    iterator.remove();
                }
            }
            // 如果还有线程活着，则稍等一会儿
            if (!userThreads.isEmpty()) {
                TimeUnit.MILLISECONDS.sleep(50);
            }
        }

    }
}