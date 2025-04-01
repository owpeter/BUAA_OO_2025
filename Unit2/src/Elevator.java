import com.oocourse.elevator1.TimableOutput;
import tools.Debug;
import tools.FloorConverter;

import java.util.ArrayList;
import java.util.Iterator;

public class Elevator implements Runnable {
    private int id;
    private int curFloor;
    private int curPersonNums;
    private int direction;
    private final RequestTable requestTable;
    private ArrayList<Person> personInElevator; // dest-floor -> person
    private Strategy strategy;
    private long lastTime = System.currentTimeMillis();
    private long simulateSumTime = 0;

    public Elevator(int id, RequestTable requestTable) {
        this.id = id;
        this.curFloor = 5;
        this.curPersonNums = 0;
        this.direction = 1;
        this.requestTable = requestTable;
        personInElevator = new ArrayList<>();
        this.strategy = new Strategy(requestTable);
    }

    public Elevator(int curFloor, int curPersonNums, int direction,
        RequestTable requestTable, ArrayList<Person> personInElevator) {
        this.curFloor = curFloor;
        this.curPersonNums = curPersonNums;
        this.direction = direction;
        this.requestTable = requestTable;
        this.personInElevator = personInElevator;
        this.strategy = new Strategy(requestTable);
    }

    public void run() {
        while (true) {
            if (Debug.getDebug()) {
                //System.out.println(Thread.currentThread().getName() + ": curFloor " + curFloor);
                System.out.println("request nums " + this.requestTable.getRequestNums());
            }

            Advice advice = strategy.getAdvice(curFloor, curPersonNums,
                direction, personInElevator, false);
            if (Debug.getDebug()) {
                //System.out.println(Thread.currentThread().getName() + ": advice " + advice);
            }
            if (advice == Advice.KILL) {
                break;
            } else if (advice == Advice.MOVE) {
                trySleep();
                Advice newAdvice = strategy.getAdvice(curFloor, curPersonNums,
                    direction, personInElevator, true);
                if (newAdvice == Advice.OPEN) {
                    openAndClose();
                } else {
                    move();
                }
            } else if (advice == Advice.OPEN) {
                openAndClose();
            } else if (advice == Advice.REVERSE) {
                direction = -direction;
            } else if (advice == Advice.WAIT) {
                requestTable.waitRequest();
            }
        }
//        throw new RuntimeException("Elevator " + id + " is dead");
    }

    private void setLastTime() {
        this.lastTime = System.currentTimeMillis();
    }

    private void trySleep() {
        long curTime = System.currentTimeMillis();
        if (curTime - lastTime < 400) {
            try {
                Thread.sleep(400 - (curTime - lastTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
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

    private void openAndClose() {
        TimableOutput.println(String.format(
            "OPEN-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        setLastTime();
        goOut();
        goIn();
        trySleep();
        TimableOutput.println(String.format(
            "CLOSE-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        this.lastTime = System.currentTimeMillis();
    }

    private void simulateOpenAndClose(long timeStamp) {
        simulateGoOut(timeStamp);
        simulateGoIn();
    }

    private void goOut() {
        Iterator<Person> iterator = personInElevator.iterator();
        while (iterator.hasNext()) {
            Person person = iterator.next();
            TimableOutput.println(String.format(
                "OUT-%d-%s-%d", person.getPersonId(),
                FloorConverter.convertNumberToFloor(curFloor), id));
            curPersonNums--;
            // 将电梯内未到最终楼层的人加入到电梯请求表
            if (person.getToFloor() != curFloor) {
                person.setFromFloor(curFloor);
                person.setDirection();
                requestTable.AddRequest(person);
            }
            iterator.remove();
        }

        if (Debug.getDebug()) {
            System.out.println("In curPersonNums: " + curPersonNums);
        }
    }

    private void simulateGoOut(long timeStamp) {
        Iterator<Person> iterator = personInElevator.iterator();
        while (iterator.hasNext()) {
            Person person = iterator.next();
            curPersonNums--;
            // 将电梯内未到最终楼层的人加入到电梯请求表
            if (person.getToFloor() != curFloor) {
                person.setFromFloor(curFloor);
                person.setDirection();
                requestTable.AddRequest(person);
            }
            else {
                simulateSumTime += person.getPriority() * timeStamp;
//                System.out.println("simulate person " + person.getPersonId() + " " + person.getPriority() + " " + timeStamp);
            }
            iterator.remove();
       }
    }

    private void goIn() {
        while (curPersonNums < 6) {
            Person person = requestTable.getAndRemovePerson(curFloor, direction);

            if (person == null) {
                break;
            }
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
            personInElevator.add(person);
            curPersonNums++;
            if (Debug.getDebug()) {
                //System.out.println("is removed");
                //System.out.println(requestTable.getFloorRequests(curFloor,direction).size());
            }
        }
    }

    public long simulate(long startTime) {
        long timeStamp = startTime;
        while(!requestTable.noRequests() || !personInElevator.isEmpty()) {
            Advice advice = strategy.getAdvice(curFloor, curPersonNums,
                    direction, personInElevator, true);
//            if (Debug.getDebug()) {
//                System.out.println(Thread.currentThread().getName() + ": advice " + advice);
//            }
            if (advice == Advice.MOVE) {
                simulateMove();
                timeStamp += 400;
//                System.out.println("simulate move " + FloorConverter.convertNumberToFloor(curFloor));
            } else if (advice == Advice.OPEN) {
                simulateOpenAndClose(timeStamp);
                timeStamp += 400;
//                System.out.println("simulate open " + FloorConverter.convertNumberToFloor(curFloor));
            } else if (advice == Advice.REVERSE) {
                direction = -direction;
//                System.out.println("simulate reverse at " + FloorConverter.convertNumberToFloor(curFloor));
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
}
