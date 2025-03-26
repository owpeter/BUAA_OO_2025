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
    private HashMap<Integer, ArrayList<Person>> personInElevator; // dest-floor -> person
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
            ArrayList<Person> persons = new ArrayList<>();
            personInElevator.put(i, persons);
        }
        this.strategy = new Strategy(requestTable);
    }

    public void run() {
//        try{
//            synchronized (requestTable) {
            while (true) {
//                System.out.println("Elevator " + id + " is running");
//                System.out.println(this.requestTable.noRequests());
                Advice advice = strategy.getAdvice(curFloor, curPersonNums, direction, personInElevator);
//                System.out.println(Thread.currentThread().getName() + ": curFloor " + curFloor);

//                System.out.println(Thread.currentThread().getName() +": advice " + advice);
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
        goOut();
        goIn();
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

    private void goOut() {
        Iterator<Person> iterator = personInElevator.get(curFloor).iterator();
        while (iterator.hasNext()) {
            Person person = iterator.next();
            TimableOutput.println(String.format("OUT-%d-%s-%d", person.getPersonId(),FloorConverter.convertNumberToFloor(curFloor), id));
            curPersonNums--;
            iterator.remove();
        }

        // TODO:
//        System.out.println("In curPersonNums: " + curPersonNums);
    }

    private void goIn() {
        while(curPersonNums < 6) {
            Person person = requestTable.getAndRemovePerson(curFloor, direction);

            if (person == null) {
                break;
            }
            personInElevator.get(person.getToFloor()).add(person);
            curPersonNums++;
            TimableOutput.println(String.format("IN-%d-%s-%d", person.getPersonId(),FloorConverter.convertNumberToFloor(curFloor), id));

            // TODO:
//            System.out.println("is removed");
//            System.out.println(requestTable.getFloorRequests(curFloor, direction).size());
//            System.out.println("In curPersonNums: " + curPersonNums);
        }
    }
}
