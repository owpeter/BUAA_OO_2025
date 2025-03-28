import com.oocourse.elevator1.TimableOutput;
import tools.Debug;
import tools.FloorConverter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class Elevator implements Runnable {
    private Integer id;
    private Integer curFloor;
    private Integer curPersonNums;
    private Integer direction;
    private final RequestTable requestTable;
    private ArrayList<Person> personInElevator; // dest-floor -> person
    private Strategy strategy;
    private long lastTime;

    public Elevator(Integer id, RequestTable requestTable) {
        this.id = id;
        this.curFloor = 5;
        this.curPersonNums = 0;
        this.direction = 1;
        this.requestTable = requestTable;
        personInElevator = new ArrayList<>();
        this.strategy = new Strategy(requestTable);
    }

    public void run() {
        while (true) {
            if (Debug.getDebug()) {
                //System.out.println(Thread.currentThread().getName() + ": curFloor " + curFloor);
                System.out.println("request nums " + this.requestTable.getRequestNums());
            }

            Advice advice = strategy.getAdvice(curFloor, curPersonNums,
                direction, personInElevator);
            if (Debug.getDebug()) {
                //System.out.println(Thread.currentThread().getName() + ": advice " + advice);
            }
            if (advice == Advice.KILL) {
                break;
            } else if (advice == Advice.MOVE) {
                move();
            } else if (advice == Advice.OPEN) {
                openAndClose();
            } else if (advice == Advice.REVERSE) {
                direction = -direction;
            } else if (advice == Advice.WAIT) {
                requestTable.waitRequest();
            }
        }
//        try {
//            throw new Exception("Elevator " + id + " is dead!");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    private void move() {
        lastTime = System.currentTimeMillis();
        if (direction == 1) {
            curFloor++;
        } else {
            curFloor--;
        }
        long curTime = System.currentTimeMillis();
        if (curTime - lastTime < 400) {
            try {
                Thread.sleep(400 - (curTime - lastTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        TimableOutput.println(String.format(
            "ARRIVE-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        this.lastTime = System.currentTimeMillis();
    }

    private void openAndClose() {
        TimableOutput.println(String.format(
            "OPEN-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        lastTime = System.currentTimeMillis();
        goOut();
        goIn();
        long curTime = System.currentTimeMillis();
        if (curTime - lastTime < 400) {
            try {
                Thread.sleep(400 - (curTime - lastTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        TimableOutput.println(String.format(
            "CLOSE-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
    }

    private void goOut() {
        Iterator<Person> iterator = personInElevator.get(curFloor).iterator();
        while (iterator.hasNext()) {
            Person person = iterator.next();
            TimableOutput.println(String.format(
                "OUT-%d-%s-%d", person.getPersonId(),
                FloorConverter.convertNumberToFloor(curFloor), id));
            curPersonNums--;
            iterator.remove();
        }

        if (Debug.getDebug()) {
            System.out.println("In curPersonNums: " + curPersonNums);
        }
    }

    private void goIn() {
        while (curPersonNums < 6) {
            Person person = requestTable.getAndRemovePerson(curFloor, direction);

            if (person == null) {
                break;
            }
            personInElevator.get(person.getToFloor()).add(person);
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
}
