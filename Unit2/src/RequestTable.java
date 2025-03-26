

import java.util.*;
import java.util.stream.Collectors;

public class RequestTable {
    private boolean endFlag;
    private Integer requestNums;
    private HashMap<Integer, PriorityQueue<Person>> requests; // floor -> set(requests)

    public RequestTable() {
        endFlag = false;
        requestNums = 0;
        requests = new HashMap<>();
        for (int i = 1; i <= 11; i++) {
            requests.put(i, new PriorityQueue<>());
        }
    }

    public synchronized PriorityQueue<Person> getFloorRequests(int floorNum) {
        return requests.get(floorNum);
    }

    public synchronized boolean noRequests() {
        return requestNums == 0;
    }

    public synchronized void setEnd() {
        this.endFlag = true;
        notifyAll();
    }

    public synchronized boolean isEnd() {return this.endFlag;}

    public synchronized void AddRequest(Person person) {
        int fromFloor = person.getFromFloor();
        requests.get(fromFloor).add(person);
        requestNums++;
        notifyAll();
    }

    public synchronized Person getAndRemovePerson(int curFloor, int direction) {
        // for elevator's requests
        if (requestNums == 0) {
            return null;
        }
        PriorityQueue<Person> floorRequests = requests.get(curFloor);
        if (floorRequests == null || floorRequests.isEmpty()) {
            return null;
        }
        Person person = floorRequests.peek();
        if((person.getToFloor() - curFloor) * direction > 0) {
            requestNums--;
            floorRequests.poll();
//            System.out.println(Thread.currentThread().getName() + " remove person " + person.getPersonId() + " from floor " + curFloor);
            return person;
        }


        return null;
    }

    public synchronized Person getRandomPerson() {
        // for input requests
        if (requestNums == 0 && !isEnd()) {
            waitRequest();
        }

        if (requestNums == 0) {
            return null;
        }

        for (PriorityQueue<Person> personRequests : requests.values()) {
            if (!personRequests.isEmpty()) {
                Person person = personRequests.poll();
                requestNums--;
                return person;
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
}
