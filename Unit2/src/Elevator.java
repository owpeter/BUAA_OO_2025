import com.oocourse.elevator1.TimableOutput;
import tools.FloorConverter;

import javax.management.timer.TimerNotification;
import java.util.*;

public class Elevator implements Runnable {
    private Integer id;
    private Integer curFloor;
    private Integer curPersonNums;
    private Integer direction;
    private final RequestTable requestTable;
    private HashMap<Integer, HashSet<Person>> personInElevator; // dest-floor -> person
    private Strategy strategy;
    private long last_time;

    public Elevator(Integer id, RequestTable requestTable) {
        this.id = id;
        this.curFloor = 5;
        this.curPersonNums = 0;
        this.direction = 1;
        this.requestTable = requestTable;
        personInElevator = new HashMap<>();
        for (int i = 1; i <= 11; i++) {
            HashSet<Person> persons = new HashSet<>();
            personInElevator.put(i, persons);
        }
        this.strategy = new Strategy(requestTable);
    }

    public void run() {
//        try{
//            synchronized (requestTable) {
            while (true) {
//                System.out.println("Elevator " + id + " is running");
                Advice advice = strategy.getAdvice(curFloor, curPersonNums, direction, personInElevator);
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
//            }
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    private void move() {
        last_time = System.currentTimeMillis();
        if (direction == 1) {
            curFloor++;
        } else {
            curFloor--;
        }
        long cur_time = System.currentTimeMillis();
        if (cur_time - last_time < 400) {
            try {
                Thread.sleep(400 - (cur_time - last_time));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        TimableOutput.println(String.format("ARRIVE-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        this.last_time = System.currentTimeMillis();
    }

    private void openAndClose() {
        TimableOutput.println(String.format("OPEN-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        last_time = System.currentTimeMillis();
        // 似乎不是在这里考虑人数问题？
        // out
        for (Person person: personInElevator.get(curFloor)) {
            TimableOutput.println(String.format("OUT-%d-%s-%d", person.getPersonId(),FloorConverter.convertNumberToFloor(curFloor), id));
            curPersonNums--;
        }
        personInElevator.get(curFloor).clear();
        // in
        // 谁该进来？？
        // 按person的priority大小顺序排列，优先让优先级高的进去
        List<Person> persons = requestTable.getSortedFloorRequests(curFloor);
        for (Person person: persons) {
            if ((direction > 0 && person.getToFloor() < curFloor)
                || (direction < 0 && person.getToFloor() > curFloor)) {
                continue;
            }
            if(curPersonNums <= 6) {
                TimableOutput.println(String.format("IN-%d-%s-%d", person.getPersonId(),FloorConverter.convertNumberToFloor(curFloor), id));
                personInElevator.get(person.getToFloor()).add(person);
                requestTable.removeRequest(person);
                curPersonNums++;
            } else {
                break;
            }
        }
        long cur_time = System.currentTimeMillis();
        if (cur_time - last_time < 400) {
            try {
                Thread.sleep(400 - (cur_time - last_time));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        TimableOutput.println(String.format("CLOSE-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
    }
}
