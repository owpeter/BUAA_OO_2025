
import com.oocourse.elevator2.ScheRequest;
import com.oocourse.elevator2.TimableOutput;
import tools.Debug;

import java.util.*;

public class RequestTable {
    private boolean endFlag;
    private int requestNums;
    private ArrayList<Person> buffer = new ArrayList<>(); //TODO: buffer还需要复制吗？？？
    private HashMap<Integer, Map<Integer,PriorityQueue<Person>>> requests = new HashMap<>();
    // floor -> (direction, persons)
    private ScheRequest scheRequest;

    public RequestTable() {
        endFlag = false;
        requestNums = 0;
        for (int i = 1; i <= 11; i++) {
            HashMap<Integer, PriorityQueue<Person>> hashMap = new HashMap<>();
            hashMap.put(1, new PriorityQueue<>());
            hashMap.put(-1, new PriorityQueue<>());
            requests.put(i, hashMap);
        }
        scheRequest = null;
    }

    public synchronized RequestTable clone() {
        RequestTable requestTable = new RequestTable();
        for (int i = 1; i <= 11; i++) {
            requestTable.requests.get(i).get(1).addAll(this.requests.get(i).get(1));
            requestTable.requests.get(i).get(-1).addAll(this.requests.get(i).get(-1));
        }
        requestTable.scheRequest = this.scheRequest;
        requestTable.requestNums = this.requestNums;
        requestTable.endFlag = this.endFlag;
        return requestTable;
    }

    public synchronized PriorityQueue<Person> getFloorRequests(int floorNum, int direction) {
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

    public synchronized void addPersonToBuffer(Person person) {
        buffer.add(person);
        notifyAll();
    }

    public synchronized void addPersonToRequest(Person person) {
        // for go in and out
        int fromFloor = person.getFromFloor();
        requests.get(fromFloor).get(person.getDirection()).add(person);
        requestNums++;
    }

    public synchronized void fromBufferToRequests(int id, boolean simulate) {
        for (Person person : buffer) {
            int fromFloor = person.getFromFloor();
            requests.get(fromFloor).get(person.getDirection()).add(person);
            requestNums++;
            if (!simulate) {
                TimableOutput.println(String.format("RECEIVE-%d-%d", person.getPersonId(), id));
            }
        }
        buffer.clear();
    }

    public synchronized void addSche(ScheRequest request) {
        this.scheRequest = request;
        notifyAll();
    }

    public synchronized ScheRequest getSche() {
        return this.scheRequest;
    }

    public synchronized void resetSche() {
        this.scheRequest = null;
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

    public synchronized boolean hasSche() {
        return scheRequest != null;
    }

    public synchronized void waitRequest(MainTable mainTable) {
        try {
//            System.out.println(Thread.currentThread().getName() + " is waiting");
            mainTable.checkKill();
            this.wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized int getRequestNums() {
        return requestNums;
    }

    public synchronized void moveToMainTable(int curFloor, MainTable mainTable, boolean simulate) {
        // for normal, clean transfer requests
        for (int direction : new int[]{1, -1}) {
            PriorityQueue<Person> floorRequests = requests.get(curFloor).get(direction);
            Iterator<Person> iterator = floorRequests.iterator();
            while (iterator.hasNext()) {
                Person person = iterator.next();
                if (person.getTransfer()) {
                    person.setTransfer(false);
                    if (!simulate) {
                        mainTable.addRequest(person);
                    }
                    iterator.remove();
                    requestNums--;
                }
            }
        }
    }

    public synchronized void scheMoveToMainTable(MainTable mainTable, boolean simulate) {
        // for  SCHE, clean all requests
        for (int floor = 1; floor <= 11; floor++) {
            for (int direction : new int[]{1, -1}) {
                PriorityQueue<Person> floorRequests = requests.get(floor).get(direction);
                while (!floorRequests.isEmpty()) {
                    Person person = floorRequests.poll();
                    person.setTransfer(false);
                    if (!simulate) {
                        mainTable.addRequest(person);
                    }
                    requestNums--;
                }
            }
        }
    }
}
