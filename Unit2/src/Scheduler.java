import java.util.HashMap;
import java.util.HashSet;

public class Scheduler implements Runnable{
    private final RequestTable inputRequest;
    private HashMap<Integer, RequestTable> requestTables; // elevator -> table

    public Scheduler(RequestTable inputRequest, Integer elevatorNums) {
        this.requestTables = new HashMap<>();
        for (int i = 1; i <= elevatorNums; i++) {
            RequestTable newRequestTable = new RequestTable();
            requestTables.put(i, newRequestTable);
        }
        this.inputRequest = inputRequest;
    }

    public HashMap<Integer, RequestTable> getRequestTables() {
        return requestTables;
    }

    public void run() {
        while (true) {
            if (inputRequest.isEnd()) {
                for (RequestTable requestTable : requestTables.values()) {
                    requestTable.setEnd();
                }
                return;
            }
            synchronized (inputRequest) {
                Person person = inputRequest.getRandomPerson();
                if (person == null) {
                    try {
                        inputRequest.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } else {
                    int elevatorId = properElevatorId(person);
                    requestTables.get(elevatorId).AddRequest(person);
                }
            }
        }
    }

    private Integer properElevatorId(Person person) {
        return person.getElevatorId();
    }


}
