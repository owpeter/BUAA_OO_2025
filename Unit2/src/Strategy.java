import java.util.HashMap;
import java.util.HashSet;

public class Strategy {
    private final RequestTable requestTable;

    public Strategy(RequestTable requestTable) {
        this.requestTable = requestTable;
    }

    public Advice getAdvice(Integer curFloor, Integer curPersonNums, Integer direction,
                            HashMap<Integer, HashSet<Person>> personInElevator) {
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
                if (reqAhead(curFloor, direction)) {
                    return Advice.MOVE;
                } else {
                    return Advice.REVERSE;
                }
            }
        }
    }

    private boolean personOut(Integer curFloor, HashMap<Integer, HashSet<Person>> personInElevator) {
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

    private boolean sameDirection(Person person, Integer curFloor, Integer direction) {
        if (direction > 0) {
            return person.getToFloor() > curFloor;
        } else {
            return person.getToFloor() < curFloor;
        }
    }
}
