import com.oocourse.elevator3.ElevatorInput;
import com.oocourse.elevator3.Request;
import java.io.IOException;

public class InputHandler implements Runnable {
    private final MainTable mainTable;

    public InputHandler() {
        mainTable = new MainTable();
    }

    public MainTable getMainTable() {
        return mainTable;
    }

    public void run() {
        try {
            ElevatorInput input = new ElevatorInput(System.in);
            while (true) {
                Request request = input.nextRequest();
                if (request == null) {
                    mainTable.setEnd();
                    break;
                } else {
                    mainTable.addRequest(request);
                }
            }
            input.close();
            // throw new RuntimeException("input is dead");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
