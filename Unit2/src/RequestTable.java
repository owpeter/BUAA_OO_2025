
import tools.Debug;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

public class RequestTable {
    private boolean endFlag;
    private int requestNums;
    private HashMap<Integer, Map<Integer,PriorityQueue<Person>>> requests;
    // floor -> (direction, persons)

    public RequestTable() {
        endFlag = false;
        requestNums = 0;
        requests = new HashMap<>();
        for (int i = 1; i <= 11; i++) {
            HashMap<Integer, PriorityQueue<Person>> hashMap = new HashMap<>();
            hashMap.put(1, new PriorityQueue<>());
            hashMap.put(-1, new PriorityQueue<>());
            requests.put(i, hashMap);
        }
    }

    public synchronized PriorityQueue<Person> getFloorRequests(int floorNum, int direction) {
        if (Debug.getDebug()) {
            //System.out.println("get floor " + floorNum +
                //"size: " + requests.get(floorNum).get(direction).size());
        }

        return requests.get(floorNum).get(direction);
    }

    public synchronized boolean noRequests() {
        return requestNums == 0;
    }

    public synchronized void setEnd() {
        this.endFlag = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {
        return this.endFlag;
    }

    public synchronized void AddRequest(Person person) {
        int fromFloor = person.getFromFloor();
        requests.get(fromFloor).get(person.getDirection()).add(person);
        requestNums++;
        notifyAll();
    }

    public synchronized Person getAndRemovePerson(int curFloor, int direction) {
        // for elevator's requests
        if (requestNums == 0) {
            return null;
        }
        PriorityQueue<Person> floorRequests = requests.get(curFloor).get(direction);
        if (floorRequests == null || floorRequests.isEmpty()) {
            return null;
        }
        requestNums--;
        return floorRequests.poll();
    }

    public synchronized Person getRandomPerson() {
        // for input requests
        if (requestNums == 0 && !isEnd()) {
            waitRequest();
        }

        if (requestNums == 0) {
            return null;
        }

        for (Map<Integer, PriorityQueue<Person>> personRequests : requests.values()) {
            for (PriorityQueue<Person> request : personRequests.values()) {
                if (!request.isEmpty()) {
                    Person person = request.poll();
                    requestNums--;
                    notifyAll();
                    return person;
                }
            }
        }
        return null;
    }

    public synchronized void waitRequest() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized int getRequestNums() {
        return requestNums;
    }
}
