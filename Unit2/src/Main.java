import com.oocourse.elevator1.TimableOutput;

public class Main {
    public static void main(String[] args) {
        TimableOutput.initStartTimestamp();

        InputHandler inputHandler = new InputHandler();
        Thread inputThread = new Thread(inputHandler);
        inputThread.start();
        Scheduler scheduler = new Scheduler(inputHandler.inputRequests, 6);
        Thread schedulerThread = new Thread(scheduler);
        schedulerThread.start();
        for (int i = 1; i <= 6; i++) {
            Elevator elevator = new Elevator(i, scheduler.getRequestTables().get(i));
            Thread elevatorThread = new Thread(elevator);
            elevatorThread.setName("Elevator " + i);
            elevatorThread.start();
        }
    }
}