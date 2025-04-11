import tools.Debug;

import java.util.Date;
import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class Strategy {
    private final RequestTable requestTable;

    public Strategy(RequestTable requestTable) {
        this.requestTable = requestTable;
    }

    public Advice getAdvice(int curFloor, int curPersonNums, int direction, int speed,
        CopyOnWriteArrayList<Person> personInElevator, boolean simulate,
                            int TopFloor, int BottomFloor, int TFloor) {
//        synchronized (requestTable) {
        if (requestTable.hasSche()) {
            return Advice.SCHE;
        }
        if (requestTable.hasUpdate()) {
            return Advice.UPDATE;
        }
        if (reverseByCantGo(curFloor, direction, TopFloor, BottomFloor)) {
            return Advice.REVERSE;
        }
        if (personOut(curFloor, personInElevator)
            || personIn(curFloor, curPersonNums, direction)) {
            return Advice.OPEN;
        }
        if (curPersonNums != 0) {
            return Advice.MOVE;
        } else {
            // 电梯里没人
            if (requestTable.noRequests()) {
                // 请求队列为空
                if (requestTable.isEnd()) {
                    return Advice.KILL;
                } else {
                    return Advice.WAIT;
                }
            }
            // 请求队列不为空
            if (simulate) {
                if (reqAhead(curFloor, direction, TopFloor, BottomFloor)) {
                    return Advice.MOVE;
                } else {
                    if (Debug.getDebug()) {
                        System.out.println(requestTable);
                    }
                    return Advice.REVERSE;
                }
            }
            else {
                if (!reqAhead(curFloor, direction, TopFloor, BottomFloor)) {
                    return Advice.REVERSE;
                }
                if (reverseBySimulate(curFloor, curPersonNums,
                    direction, speed, TopFloor, BottomFloor, TFloor,requestTable, personInElevator)) {
                    return Advice.REVERSE;
                } else {
                    return Advice.MOVE;
                }
            }
        }
//        }
    }

    private boolean personOut(int curFloor,
        CopyOnWriteArrayList<Person> personInElevator) {
        for (Person person : personInElevator) {
            if (person.getToFloor() == curFloor) {
                return true;
            }
        }
        return false;
    }

    private boolean personIn(int curFloor, int curPersonNums, int direction) {
        if (curPersonNums == 6) {
            return false;
        }
        return !requestTable.getFloorRequests(curFloor, direction).isEmpty();
    }

    private boolean reqAhead(int curFloor, int direction, int TopFloor, int BottomFloor) {
        synchronized (requestTable) {
            for (int i = curFloor + direction; i >= BottomFloor && i <= TopFloor; i += direction) {
                if (!requestTable.getFloorRequests(i, 1).isEmpty()
                    || !requestTable.getFloorRequests(i, -1).isEmpty()) {
                    return true;
                }
            }
            return false;
        }
    }

    private boolean reverseByCantGo(int curFloor, int direction, int TopFloor, int BottomFloor) {
        return ((curFloor == TopFloor && direction == 1) || (curFloor == BottomFloor && direction == -1));
    }

    public boolean reverseBySimulate(int curFloor, int curPersonNums, int direction, int speed, int TopFloor, int BottomFloor, int TFloor,
        RequestTable requestTable, CopyOnWriteArrayList<Person> personInElevator) {
        CopyOnWriteArrayList<Person> personInElevatorCopy1 = new CopyOnWriteArrayList<>();
        for (Person person : personInElevator) {
            personInElevatorCopy1.add(person.clone());
        }
        Elevator same = new Elevator(0, curFloor, curPersonNums, direction, speed, TopFloor, BottomFloor, TFloor, requestTable.clone(),
            personInElevatorCopy1, null);
        CopyOnWriteArrayList<Person> personInElevatorCopy2 = new CopyOnWriteArrayList<>();
        for (Person person : personInElevator) {
            personInElevatorCopy2.add(person.clone());
        }
        Elevator revert = new Elevator(0, curFloor, curPersonNums, -direction, speed, TopFloor, BottomFloor, TFloor, requestTable.clone(),
                personInElevatorCopy2, null);
        long sameTime = same.simulate(0) + same.getSimulateSumTime();
        long revTime = revert.simulate(0) + revert.getSimulateSumTime();
        return sameTime > revTime;
    }
}
