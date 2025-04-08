import com.oocourse.elevator3.*;
import tools.Debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;

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
            if (mainTable.isEmpty() && mainTable.isEnd() &&
                allWaiting() && allRequestTableEmpty()) {
                for (RequestTable requestTable : requestTables.values()) {
                    requestTable.setEnd();
                }
                break;
            }
            Request request = mainTable.getAndRemoveRequest();
            if (request == null) {
                continue;
            }
//            TimableOutput.println(String.format("REQUEST-%s", request));
            if (request instanceof PersonRequest) {
                PersonRequest personRequest = (PersonRequest) request;
                Person person = new Person(personRequest);
                int elevatorId = properElevatorId(person);
                requestTables.get(elevatorId).addPersonToBuffer(person);
            } else if (request instanceof Person) {
                Person person = (Person) request;
                int elevatorId = properElevatorId(person);
                requestTables.get(elevatorId).addPersonToBuffer(person);
            } else if (request instanceof ScheRequest) {
                ScheRequest scheRequest = (ScheRequest) request;
                requestTables.get(scheRequest.getElevatorId()).addSche(scheRequest);
            } else if (request instanceof UpdateRequest) {
                Thread updateThreadThread = getThread((UpdateRequest) request);
                updateThreadThread.start();
            }
        }
        // throw new RuntimeException("scheduler is dead");
    }

    private Thread getThread(UpdateRequest request) {
        // for doing update
        UpdateRequest updateRequest = request;
        CyclicBarrier phase1End = new CyclicBarrier(3);
        CyclicBarrier phase2End = new CyclicBarrier(3);
        CountDownLatch phase1Latch = new CountDownLatch(2);
        CountDownLatch phase2Latch = new CountDownLatch(2);
        // run UpdateThread
        UpdateThread updateThread = new UpdateThread(phase1End, phase2End, phase1Latch, phase2Latch,
                requestTables, updateRequest,
                elevators.get(updateRequest.getElevatorAId() - 1),
                elevators.get(updateRequest.getElevatorBId() - 1));
        Thread updateThreadThread = new Thread(updateThread);
        return updateThreadThread;
    }

    private int properElevatorId(Person person) {
        if (Debug.getDebug()) {
            System.out.println("simulating: " + person);
        }
        long minWaitTime = Integer.MAX_VALUE;
        int minElevatorId = 0;
        ArrayList<Elevator> clonedElevators = new ArrayList<>();
        synchronized (elevators.get(0)) {
            synchronized (elevators.get(1)) {
                synchronized (elevators.get(2)) {
                    synchronized (elevators.get(3)) {
                        synchronized (elevators.get(4)) {
                            synchronized (elevators.get(5)) {
                                if (Debug.getDebug()) {
                                    TimableOutput.println("cloning....");
                                }
                                for (Elevator elevator : elevators) {
                                    clonedElevators.add(elevator.clone());
                                }
                                for (Elevator elevator : clonedElevators) {
                                    if (Debug.getDebug()) {
                                        System.out.println("simulating " + elevator.getId());
                                    }
                                    Person clonedPerson = person.clone();
                                    long cost;
                                    if (fitCondition(elevator, clonedPerson)) {
                                        elevator.addPersonToBuffer(clonedPerson);
                                        cost = elevator.simulate(0) + elevator.getSimulateSumTime();
                                        if (elevator.getStatus().equals(Advice.UPDATE)) {
                                            cost += 1000;
                                        }
                                    } else {
                                        cost = Integer.MAX_VALUE;
                                    }
                                    if (Debug.getDebug()) {
                                        System.out.println("elevator: " + elevator.getId() +
                                            " cost: " + cost);
                                    }
                                    if (cost < minWaitTime) {
                                        minWaitTime = (int) cost;
                                        minElevatorId = elevator.getId();
                                    }
                                }
                                if (minElevatorId == 0) {
                                    throw new RuntimeException("minElevatorId == 0");
                                }
                                if (Debug.getDebug()) {
                                    System.out.println("schedule elevator "
                                        + minElevatorId + " to receive this person");
                                }
                                return minElevatorId;
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean fitCondition(Elevator elevator, Person person) {
        if (elevator.getStatus().equals(Advice.UPDATE)) {
            elevator.updateFloor();
        }
        if (inRange(elevator, person.getFromFloor())) {
            if (person.getFromFloor() == elevator.getTFloor()) {
                return inRange(elevator, person.getToFloor());
            }
            return true;
        }
        return false;
    }

    private boolean inRange(Elevator elevator, int floor) {
        return !(floor < elevator.getBottomFloor() || floor > elevator.getTopFloor());
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
