import com.oocourse.elevator2.PersonRequest;
import com.oocourse.elevator2.Request;
import com.oocourse.elevator2.ScheRequest;
import com.oocourse.elevator2.TimableOutput;

import java.util.ArrayList;
import java.util.HashMap;

public class Scheduler implements Runnable {
    private final MainTable mainTable;
    private HashMap<Integer, RequestTable> requestTables; // elevator -> table
    private ArrayList<Elevator> elevators;

    public Scheduler(MainTable mainTable, Integer elevatorNums) {
        this.requestTables = new HashMap<>();
        for (int i = 1; i <= elevatorNums; i++) {
            RequestTable newRequestTable = new RequestTable();
            requestTables.put(i, newRequestTable);
        }
        this.mainTable = mainTable;
        this.elevators = new ArrayList<>();
    }

    public HashMap<Integer, RequestTable> getRequestTables() {
        return requestTables;
    }

    public void run() {

        while (true) {
//            System.out.println("scheduler is run");
//            System.out.println("is empty: " + mainTable.isEmpty() + " is end: " + mainTable.isEnd() + " allRequestTableEmpty: " + allRequestTableEmpty());
            if (mainTable.isEmpty() && mainTable.isEnd() && allWaiting() && allRequestTableEmpty()) {
//                System.out.println("scheduler is killing");
                for (RequestTable requestTable : requestTables.values()) {
                    requestTable.setEnd();
                }
                break;
            }
            Request request = mainTable.getAndRemoveRequest();
            if (request == null) {
                continue;
            }
            if (request instanceof PersonRequest) {
                PersonRequest personRequest = (PersonRequest) request;
                Person person = new Person(personRequest);
                int elevatorId = properElevatorId(person);
                TimableOutput.println(String.format("RECEIVE-%d-%d", person.getPersonId(), elevatorId));
//            System.out.println("elevator id: " + elevatorId + "person: " + person.getPersonId());
                requestTables.get(elevatorId).addPerson(person);
            } else if (request instanceof Person) {
                Person person = (Person) request;
                int elevatorId = properElevatorId(person);
                TimableOutput.println(String.format("RECEIVE-%d-%d", person.getPersonId(), elevatorId));
                requestTables.get(elevatorId).addPerson(person);
            } else if (request instanceof ScheRequest) {
                ScheRequest scheRequest = (ScheRequest) request;
                requestTables.get(scheRequest.getElevatorId()).addSche(scheRequest);
            }
        }
//        throw new RuntimeException("scheduler is dead");
    }

    private int properElevatorId(Person person) {
//        System.out.println("personId: " + person.getPersonId());
        long minWaitTime = Integer.MAX_VALUE;
        int minElevatorId = 0;
        ArrayList<Elevator> clonedElevators = new ArrayList<>();
        synchronized (elevators.get(0)) {
            synchronized (elevators.get(1)) {
                synchronized (elevators.get(2)) {
                    synchronized (elevators.get(3)) {
                        synchronized (elevators.get(4)) {
                            synchronized (elevators.get(5)) {
                                TimableOutput.println("cloning....");
                                for (Elevator elevator : elevators) {
                                    System.out.println("cloning " + elevator.getId());
                                    clonedElevators.add(elevator.clone());
                                }
                            }
                        }
                    }
                }
            }
        }
        for (Elevator elevator : clonedElevators) {
            System.out.println("simulating " + elevator.getId());
            elevator.addRequest(person);
            long cost = elevator.simulate(0);
            System.out.println("elevator: " + elevator.getId() + " cost: " + cost);
            if (cost < minWaitTime) {
                minWaitTime = (int) cost;
                minElevatorId = elevator.getId();
            }
        }
        if (minElevatorId == 0) {
            throw new RuntimeException("minElevatorId == 0");
        }
//        System.out.println("----------------");
        return minElevatorId;
    }

    public void addElevator(Elevator elevator) {
        elevators.add(elevator);
    }

    private boolean allRequestTableEmpty() {
        for (RequestTable requestTable : requestTables.values()) {
            if (!requestTable.noRequests()) {
                return false;
            }
            if (requestTable.hasSche()) {
                return false;
            }
        }
        return true;
    }

    private boolean allWaiting() {
        for (Elevator elevator : elevators) {
            if (!elevator.getStatus().equals(Advice.WAIT)) {
                return false;
            }
        }
        return true;
    }
}
