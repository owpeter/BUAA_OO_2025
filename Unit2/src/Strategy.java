import java.util.PriorityQueue;
import java.util.concurrent.CopyOnWriteArrayList;

public class Strategy {
    private final RequestTable requestTable;

    public Strategy(RequestTable requestTable) {
        this.requestTable = requestTable;
    }

    public Advice getAdvice(int curFloor, int curPersonNums, int direction,
        CopyOnWriteArrayList<Person> personInElevator, boolean simulate) {
        synchronized (requestTable) {
            if (requestTable.hasSche()) {
                return Advice.SCHE;
            }
            if (reverseByCantGo(curFloor, direction)) {
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
                    if (reqAhead(curFloor, direction)) {
                        return Advice.MOVE;
                    } else {
                        return Advice.REVERSE;
                    }
                }
                else {
                    if (!reqAhead(curFloor, direction)) {
                        return Advice.REVERSE;
                    }
                    if (reverseBySimulate(curFloor, curPersonNums,
                        direction, requestTable, personInElevator)) {
                        return Advice.REVERSE;
                    } else {
                        return Advice.MOVE;
                    }
                }
            }
        }
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

    private boolean reqAhead(int curFloor, int direction) {
        synchronized (requestTable) {
            for (int i = curFloor + direction; i >= 1 && i <= 11; i += direction) {
                if (!requestTable.getFloorRequests(i, 1).isEmpty()
                    || !requestTable.getFloorRequests(i, -1).isEmpty()) {
                    return true;
                }
            }
            return false;
        }
    }

    private double calculateSum(int start, int end, int direction,
        boolean isRevert, double gamma, int curFloor) {
        double sum = 0;
        for (int i = start; i != end; i += (end > start ? 1 : -1)) {
            double distance = Math.abs(curFloor - i);
            sum += floorSum(i, direction, isRevert, distance, gamma);
            sum += floorSum(i, -direction, isRevert, distance, gamma);
        }
        return sum;
    }

    private double floorSum(int floor, int direction, boolean isRevert,
        double distance, double gamma) {
        // 计算一层中方向为direction的权重
        PriorityQueue<Person> floorRequests = requestTable.getFloorRequests(floor, direction);
        if (floorRequests == null || floorRequests.isEmpty()) {
            return 0;
        }
        double sum = 0;
        for (Person person : floorRequests) {
            if (isRevert) {
                sum += person.getWaitTime() + person.getPriority() / distance - gamma;
            } else {
                sum += person.getWaitTime() + person.getPriority() / distance;
            }
        }
        return sum;
    }

    private boolean reverseByCantGo(int curFloor, int direction) {
        return ((curFloor == 11 && direction == 1) || (curFloor == 1 && direction == -1));
    }

    public boolean reverseBySimulate(int curFloor, int curPersonNums, int direction,
        RequestTable requestTable, CopyOnWriteArrayList<Person> personInElevator) {
        CopyOnWriteArrayList<Person> personInElevatorCopy1 = new CopyOnWriteArrayList<>();
        for (Person person : personInElevator) {
            personInElevatorCopy1.add(person.clone());
        }
        Elevator same = new Elevator(curFloor, curPersonNums, direction, requestTable.clone(),
            personInElevatorCopy1);
        CopyOnWriteArrayList<Person> personInElevatorCopy2 = new CopyOnWriteArrayList<>();
        for (Person person : personInElevator) {
            personInElevatorCopy2.add(person.clone());
        }
        Elevator revert = new Elevator(curFloor, curPersonNums, -direction, requestTable.clone(),
            personInElevatorCopy2);
        long sameTime = same.simulate(0) + same.getSimulateSumTime();
        long revTime = revert.simulate(0) + revert.getSimulateSumTime();
        return sameTime > revTime;
    }
}
