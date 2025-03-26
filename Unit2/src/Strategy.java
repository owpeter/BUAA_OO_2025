import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;

public class Strategy {
    private final RequestTable requestTable;

    public Strategy(RequestTable requestTable) {
        this.requestTable = requestTable;
    }

    public Advice getAdvice(Integer curFloor, Integer curPersonNums, Integer direction,
                            HashMap<Integer, ArrayList<Person>> personInElevator) {
        synchronized (requestTable) {
            if (personOut(curFloor, personInElevator) || personIn(curFloor, curPersonNums, direction)) {
                return Advice.OPEN;
            }
            if (curPersonNums != 0) {
                return Advice.MOVE;
            } else {
                // 电梯里没人
                if(requestTable.noRequests()){
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
                if (reverseByWeight(curFloor, direction)) {
                    return Advice.REVERSE;
                } else {
//                    System.out.println(Thread.currentThread().getName() + " go ahead");
                    return Advice.MOVE;
                }
            }
        }
    }

    private boolean personOut(Integer curFloor, HashMap<Integer, ArrayList<Person>> personInElevator) {
//        boolean flag = !personInElevator.get(curFloor).isEmpty();
//        System.out.println("personOut: " + flag);
//        return flag;
        return !personInElevator.get(curFloor).isEmpty();
    }

    private boolean personIn(Integer curFloor, Integer curPersonNums, Integer direction) {
        if (curPersonNums == 6) {
            return false;
        }
        if (!requestTable.getFloorRequests(curFloor).isEmpty()){
            for (Person person : requestTable.getFloorRequests(curFloor)) {
                if (sameDirection(person, curFloor, direction)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean reqAhead(Integer curFloor, Integer direction) {
        if (direction > 0) {
            for(int i = curFloor + 1; i <= 11; i++) {
                if (!requestTable.getFloorRequests(i).isEmpty()){
                    return true;
                }
            }
        } else {
            for (int i = curFloor - 1; i >= 1; i--) {
                if (!requestTable.getFloorRequests(i).isEmpty()){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean reverseByWeight(int curFloor, int direction) {
        double gamma = 1.5;
        synchronized (requestTable) {
            double same_sum = 0, revert_sum = 0;

            if (direction > 0) {
                revert_sum += calculateSum(curFloor - 1, 0, true, gamma, curFloor);
                same_sum += calculateSum(curFloor + 1, 12, false, gamma, curFloor);
            } else {
                same_sum += calculateSum(curFloor - 1, 0, false, gamma, curFloor);
                revert_sum += calculateSum(curFloor + 1, 12, true, gamma, curFloor);
            }

            // TODO:
            System.out.println(Thread.currentThread().getName() +"same: " + same_sum + " revert: " + revert_sum);

            // 处理当前楼层的请求
            PriorityQueue<Person> currentFloorRequests = requestTable.getFloorRequests(curFloor);
            if (currentFloorRequests != null && !currentFloorRequests.isEmpty()) {
                for (Person person : currentFloorRequests) {


                    // TODO:
//                    System.out.println(Thread.currentThread().getName() + " curF: " + curFloor + "person: " + person.getToFloor() + " direction: " + direction);


                    if ((person.getToFloor() - curFloor) * direction > 0) {
                        same_sum += person.getWaitTime() + person.getPriority();
                    } else if ((person.getToFloor() - curFloor) * direction < 0){
                        revert_sum += person.getWaitTime() + person.getPriority() - gamma;
                    }
                }
            }

            // TODO:
//            System.out.println(Thread.currentThread().getName() + "same: " + same_sum + " revert: " + revert_sum);

            if (revert_sum > same_sum + gamma) {
                return true;
            }
            return false;
        }
    }

    private double calculateSum(int start, int end, boolean isRevert, double gamma, int curFloor) {
        double sum = 0;
        for (int i = start; i != end; i += (end > start ? 1 : -1)) {
            double distance = Math.abs(curFloor - i);
            PriorityQueue<Person> floorRequests = requestTable.getFloorRequests(i);
            if (floorRequests == null || floorRequests.isEmpty()) {
                continue; // 避免空指针异常
            }
            for (Person person : floorRequests) {
                if (isRevert) {
                    sum += person.getWaitTime() + person.getPriority() / distance - gamma;
                } else {
                    sum += person.getWaitTime() + person.getPriority() / distance;
                }
            }
        }
        return sum;
    }

    private boolean sameDirection(Person person, Integer curFloor, Integer direction) {
        if (direction > 0) {
            return person.getToFloor() > curFloor;
        } else {
            return person.getToFloor() < curFloor;
        }
    }
}
