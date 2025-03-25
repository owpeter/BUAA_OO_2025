import java.util.*;
import java.util.stream.Collectors;

public class RequestTable {
    private boolean endFlag;
    private Integer requestNums;
    private HashMap<Integer, HashSet<Person>> requests; // floor -> set(requests)

    public RequestTable() {
        endFlag = false;
        requestNums = 0;
        requests = new HashMap<>();
        for (int i = 1; i <= 11; i++) {
            requests.put(i, new HashSet<>());
        }
    }

    public HashSet<Person> getFloorRequests(int floorNum) {
        return requests.get(floorNum);
    }

    public boolean noRequests() {
        return requestNums == 0;
    }

    public synchronized void setEnd() {
        this.endFlag = true;
        notifyAll();
    }

    public boolean isEnd() {return this.endFlag;}

    public synchronized void AddRequest(Person person) {
        int fromFloor = person.getFromFloor();
        HashSet hashSet = requests.get(fromFloor);
        if (hashSet == null) {
            hashSet = new HashSet();
        }
        hashSet.add(person);
        requests.put(fromFloor, hashSet);

        requestNums++;
        notifyAll();
    }

    public synchronized void removeRequest(Person person) {
        int fromFloor = person.getFromFloor();
        HashSet hashSet = requests.get(fromFloor);
        if (hashSet != null && !hashSet.isEmpty()) {
            hashSet.remove(person);
            requestNums--;
        }
    }

    public synchronized Person getRandomPerson() {
        if (requests.isEmpty()) {
            return null;
        }

        for (Set<Person> personRequests : requests.values()) {
            if (personRequests != null && !personRequests.isEmpty()) {
                Person person = personRequests.iterator().next();
                personRequests.remove(person);
                requestNums--;
                return person;
            }
        }
//        System.out.println("No more requests");
        return null;
    }

    public List<Person> getSortedFloorRequests(int floorNum) {
        HashSet<Person> floorRequests = requests.get(floorNum);
        if (floorRequests == null) {
            return Collections.emptyList();
        }
        return floorRequests.stream()
                .sorted(Comparator.comparingInt(Person::getPriority).reversed())
                .collect(Collectors.toList());
    }

    public synchronized void waitRequest() {
        try {
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
