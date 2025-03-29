import tools.Debug;

import java.util.ArrayList;
import java.util.PriorityQueue;

public class Strategy {
    private final RequestTable requestTable;

    public Strategy(RequestTable requestTable) {
        this.requestTable = requestTable;
    }

    public Advice getAdvice(int curFloor, int curPersonNums, int direction,
        ArrayList<Person> personInElevator) {
        synchronized (requestTable) {
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
                //                if (reqAhead(curFloor, direction)) {
                //                    return Advice.MOVE;
                //                } else {
                //                    return Advice.REVERSE;
                //                }
                if (reqAhead(curFloor, direction)) {
                    if (Debug.getDebug()) {
                        System.out.println(Thread.currentThread().getName() +
                            " reverse by reqAhead");
                    }
                    if (reverseByWeight(curFloor, direction)) {
                        return Advice.REVERSE;
                    }
                    return Advice.MOVE;
                } else {
                    if (Debug.getDebug()) {
                        System.out.println(Thread.currentThread().getName() + " go ahead");
                    }
                    return Advice.REVERSE;
                }
            }
        }
    }

    private boolean personOut(int curFloor,
        ArrayList<Person> personInElevator) {
        if (Debug.getDebug()) {
            boolean flag = !personInElevator.isEmpty();
            System.out.println("personOut: " + flag);
            return flag;
        }
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

    public boolean reverseByWeight(int curFloor, int direction) {
        double gamma = 15;
        synchronized (requestTable) {
            double sameSum = 0;
            double revertSum = 0;

            if (direction > 0) {
                revertSum += calculateSum(curFloor - 1, 0, direction, true, gamma, curFloor);
                sameSum += calculateSum(curFloor + 1, 12, direction, false, gamma, curFloor);
            } else {
                sameSum += calculateSum(curFloor - 1, 0, direction, false, gamma, curFloor);
                revertSum += calculateSum(curFloor + 1, 12, direction, true, gamma, curFloor);
            }

            if (Debug.getDebug()) {
                System.out.println(Thread.currentThread().getName()
                    + "same: " + sameSum + " revert: " + revertSum);
            }

            // 处理当前楼层的请求
            PriorityQueue<Person> sameDirectionRequests
                = requestTable.getFloorRequests(curFloor, direction);
            if (sameDirectionRequests != null && !sameDirectionRequests.isEmpty()) {
                for (Person person : sameDirectionRequests) {
                    sameSum += person.getWaitTime() + person.getPriority();
                }
            }
            PriorityQueue<Person> revertDirectionRequests
                = requestTable.getFloorRequests(curFloor, -direction);
            if (revertDirectionRequests != null && !revertDirectionRequests.isEmpty()) {
                for (Person person : revertDirectionRequests) {
                    revertSum += person.getWaitTime() + person.getPriority() - gamma;
                }
            }

            if (false) {
                System.out.println(Thread.currentThread().getName()
                    + "same: " + sameSum + " revert: " + revertSum);
            }

            return revertSum > sameSum + 100;
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
}
