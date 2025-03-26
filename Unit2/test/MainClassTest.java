import org.junit.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class MainClassTest {

    @org.junit.Test
    public void testMain() throws IOException, InterruptedException {
        // 建立 Piped 流，用以将数据写入 System.in
        final PipedInputStream inPipe = new PipedInputStream();
        final PipedOutputStream outPipe = new PipedOutputStream(inPipe);
        // 重定向 System.in 为 piped 输入流
        System.setIn(inPipe);
//        System.setOut(inPipe);
        // 启动一个线程，根据 stdin.txt 中的时间延迟写入数据到 outPipe
        Thread inputSimulator = new Thread(() -> {
            try {
                // 读取测试文件（请确保文件存在于项目工作目录中）
                List<String> lines = Files.readAllLines(Paths.get(""), StandardCharsets.UTF_8);
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
        // 调用带有模拟输入的MainClass.main()方法
        Main.main(new String[0]);
        // 等待输入模拟线程执行结束
        inputSimulator.join();
    }

    @Test
    public void capturePipeContent() throws IOException {
        PipedInputStream inPipe = new PipedInputStream();
        PipedOutputStream outPipe = new PipedOutputStream(inPipe);

        // 写入测试数据
        outPipe.write("Test data 1\nTest data 2".getBytes());
        outPipe.flush();

        // 捕获内容
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inPipe.read(buffer)) != -1) {
            byteArrayOutputStream.write(buffer, 0, length);
        }

        String capturedContent = byteArrayOutputStream.toString();
        System.out.println("捕获的内容: " + capturedContent);

        outPipe.close();
        inPipe.close();
    }
}