import com.oocourse.elevator2.ScheRequest;
import com.oocourse.elevator2.TimableOutput;
import tools.Debug;
import tools.FloorConverter;

import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class Elevator implements Runnable {
    private int id;
    private int curFloor;
    private int curPersonNums;
    private int direction;
    private Advice status;
    private MainTable mainTable;
    private final RequestTable requestTable;
    private CopyOnWriteArrayList<Person> personInElevator = new CopyOnWriteArrayList<>();
    private Strategy strategy;
    private long lastTime = System.currentTimeMillis();
    private long simulateSumTime = 0;
    private final int speed = 400;
    private final int scheTime = 1000;

    public Elevator(int id, MainTable mainTable, RequestTable requestTable) {
        this.id = id;
        this.curFloor = 5;
        this.curPersonNums = 0;
        this.direction = 1;
        this.mainTable = mainTable;
        this.requestTable = requestTable;
        this.strategy = new Strategy(requestTable);
    }

    public Elevator(int curFloor, int curPersonNums, int direction,
        RequestTable requestTable, CopyOnWriteArrayList<Person> personInElevator) {
        this.curFloor = curFloor;
        this.curPersonNums = curPersonNums;
        this.direction = direction;
        this.requestTable = requestTable;
        this.personInElevator = personInElevator;
        this.strategy = new Strategy(requestTable);
    }

    public Elevator(int id, int curFloor, int curPersonNums, int direction,
                    RequestTable requestTable, CopyOnWriteArrayList<Person> personInElevator) {
        this.id = id;
        this.curFloor = curFloor;
        this.curPersonNums = curPersonNums;
        this.direction = direction;
        this.requestTable = requestTable;
        this.personInElevator = personInElevator;
        this.strategy = new Strategy(requestTable);
    }

    public Elevator clone() {
//        System.out.println("clone " + id);
        Elevator elevator = new Elevator(id, curFloor, curPersonNums, direction,
            requestTable.clone(), (CopyOnWriteArrayList<Person>) personInElevator.clone());
        return elevator;
    }

    public int getId() {
        return id;
    }

    public Advice getStatus() {
        return status;
    }

    public void run() {
        while (true) {
            Advice advice = strategy.getAdvice(curFloor, curPersonNums,
                direction, personInElevator, false);
            status = advice;
            System.out.println(Thread.currentThread().getName() + ": advice " + advice);
            if (advice == Advice.WAIT) {
                requestTable.waitRequest(mainTable);
            } else if (advice == Advice.MOVE) {
                trySleep(speed);
                Advice newAdvice = strategy.getAdvice(curFloor, curPersonNums,
                    direction, personInElevator, true);
                if (newAdvice == Advice.OPEN) {
                    openAndClose(speed);
                } else {
                    move();
                }
            } else if (advice == Advice.OPEN) {
                openAndClose(speed);
            } else if (advice == Advice.REVERSE) {
                direction = -direction;
            } else if (advice == Advice.KILL) {
                break;
            } else if (advice == Advice.SCHE) {
                schedule();
            }
        }

//        throw new RuntimeException("Elevator " + id + " is dead");
    }

    private void setLastTime() {
        this.lastTime = System.currentTimeMillis();
    }

    private void trySleep(int time) {
        long curTime = System.currentTimeMillis();
        if (curTime - lastTime < time) {
            try {
                Thread.sleep(time - (curTime - lastTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void schedule() {
        TimableOutput.println(String.format("SCHE-BEGIN-%d", id));
        ScheRequest scheRequest = requestTable.getSche();
        int toFloor = FloorConverter.convertFloorToNumber(scheRequest.getToFloor());
        if (curFloor < toFloor) {
            direction = 1;
        } else {
            direction = -1;
        }
        while (curFloor != toFloor) {
            trySleep((int) Math.round(scheRequest.getSpeed() * 1000));
            move();
        }
        scheOpenAndClose(scheTime);
        requestTable.resetSche();
        TimableOutput.println(String.format("SCHE-END-%d", id));
    }

    private long simulateSchedule() {
        long ret = 0;
        ScheRequest scheRequest = requestTable.getSche();
        int toFloor = FloorConverter.convertFloorToNumber(scheRequest.getToFloor());
        if (curFloor < toFloor) {
            direction = 1;
        } else {
            direction = -1;
        }
        while (curFloor != toFloor) {
            ret += (int) Math.round(scheRequest.getSpeed() * 1000);
            simulateMove();
        }
        ret += scheTime;
        simulateScheOpenAndClose();
        return ret;
    }

    private void move() {
        if (direction == 1) {
            curFloor++;
        } else {
            curFloor--;
        }
        TimableOutput.println(String.format(
            "ARRIVE-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        setLastTime();
    }

    private void simulateMove() {
        if (direction == 1) {
            curFloor++;
        } else {
            curFloor--;
        }
//        System.out.println("simulate move " + curFloor);
    }

    private void openAndClose(int time) {
        TimableOutput.println(String.format(
            "OPEN-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        setLastTime();
        goOut();
        goIn();
        requestTable.moveToMainTable(curFloor, mainTable, false);
        trySleep(time);
        TimableOutput.println(String.format(
            "CLOSE-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        setLastTime();
    }

    private void scheOpenAndClose(int time) {
        TimableOutput.println(String.format(
                "OPEN-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        setLastTime();
        goOut();
        requestTable.scheMoveToMainTable(mainTable, false);
        trySleep(time);
        TimableOutput.println(String.format(
                "CLOSE-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        setLastTime();
    }

    private void simulateOpenAndClose(long timeStamp) {
        simulateGoOut(timeStamp);
        simulateGoIn();
        requestTable.moveToMainTable(curFloor, mainTable, true);
    }

    private void simulateScheOpenAndClose() {
        personInElevator.clear();
    }

    private void goOut() {
        for (Person person : personInElevator) {
            if (person.getToFloor() == curFloor) {
                TimableOutput.println(String.format("OUT-S-%d-%s-%d", person.getPersonId(), FloorConverter.convertNumberToFloor(curFloor), id));
            } else {
                TimableOutput.println(String.format("OUT-F-%d-%s-%d", person.getPersonId(), FloorConverter.convertNumberToFloor(curFloor), id));
                person.setFromFloor(curFloor);
                person.setDirection();
                person.setTransfer(true);
                requestTable.addPerson(person);
            }
        }
        curPersonNums = 0;
        personInElevator.clear();

        if (Debug.getDebug()) {
            System.out.println("In curPersonNums: " + curPersonNums);
        }
    }

    private void simulateGoOut(long timeStamp) {
        for (Person person : personInElevator) {
            if (person.getToFloor() != curFloor) {
                person.setFromFloor(curFloor);
                person.setDirection();
                person.setTransfer(true);
                requestTable.addPerson(person);
            } else {
                simulateSumTime += person.getPriority() * timeStamp;
//                System.out.println("simulate person " + person.getPersonId() + " " + person.getPriority() + " " + timeStamp);
            }
        }
        curPersonNums = 0;
        personInElevator.clear();
    }

    private void goIn() {
        while (curPersonNums < 6) {
            Person person = requestTable.getAndRemovePerson(curFloor, direction);

            if (person == null) {
                break;
            }
            person.setTransfer(false);
            personInElevator.add(person);
            curPersonNums++;
            TimableOutput.println(String.format(
                "IN-%d-%s-%d", person.getPersonId(),
                FloorConverter.convertNumberToFloor(curFloor), id));

            if (Debug.getDebug()) {
                //System.out.println("is removed");
                //System.out.println(requestTable.getFloorRequests(curFloor, direction).size());
                //System.out.println("In curPersonNums: " + curPersonNums);
            }
        }
    }

    private void simulateGoIn() {
        while (curPersonNums < 6) {
            Person person = requestTable.getAndRemovePerson(curFloor, direction);

            if (person == null) {
                break;
            }
            person.setTransfer(false);
            personInElevator.add(person);
            curPersonNums++;
            if (Debug.getDebug()) {
                //System.out.println("is removed");
                //System.out.println(requestTable.getFloorRequests(curFloor,direction).size());
            }
        }
    }

    public long simulate(long startTime) {
        System.out.println(this);
//        System.out.println(requestTable.getRequestNums());
        long timeStamp = startTime;
        while(true) {
            Advice advice = strategy.getAdvice(curFloor, curPersonNums,
                    direction, personInElevator, true);

                System.out.println(Thread.currentThread().getName() + ": advice " + advice);
            if (advice == Advice.SCHE) {
                System.out.println("-----------simulate sche-----------");
                //TODO: unfinished
//                int toFloor = FloorConverter.convertFloorToNumber(requestTable.getSche().getToFloor());
//                requestTable.resetSche();
            } else if (advice == Advice.MOVE) {
                simulateMove();
                timeStamp += 400;
            } else if (advice == Advice.OPEN) {
                simulateOpenAndClose(timeStamp);
                timeStamp += 400;
            } else if (advice == Advice.REVERSE) {
                direction = -direction;
            } else if (advice == Advice.WAIT) {
                break;
            }
        }
//        System.out.println("-----------");
        return timeStamp;
    }

    public long getSimulateSumTime() {
        return simulateSumTime;
    }

    public void addRequest(Person person) {
        requestTable.addPerson(person);
    }

    @Override
    public String toString() {
        return "Elevator " + id + ", curF: " + curFloor + ", dir: " + direction + ", " + personInElevator.toString() + "hasSche: " + requestTable.hasSche();
    }
}
