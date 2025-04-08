
import com.oocourse.elevator3.ScheRequest;
import com.oocourse.elevator3.TimableOutput;
import com.oocourse.elevator3.UpdateRequest;
import tools.Debug;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.util.Map;

public class RequestTable {
    private boolean endFlag;
    private int requestNums;
    private ArrayList<Person> buffer = new ArrayList<>();
    private HashMap<Integer, Map<Integer,PriorityQueue<Person>>> requests = new HashMap<>();
    // floor -> (direction, persons)
    private ScheRequest scheRequest = null;
    private UpdateRequest updateRequest = null;

    public RequestTable() {
        endFlag = false;
        requestNums = 0;
        for (int i = 1; i <= 11; i++) {
            HashMap<Integer, PriorityQueue<Person>> hashMap = new HashMap<>();
            hashMap.put(1, new PriorityQueue<>());
            hashMap.put(-1, new PriorityQueue<>());
            requests.put(i, hashMap);
        }
    }

    public synchronized RequestTable clone() {
        RequestTable requestTable = new RequestTable();
        for (int i = 1; i <= 11; i++) {
            PriorityQueue<Person> queue1 = this.requests.get(i).get(1);
            Iterator<Person> iterator1 = queue1.iterator();
            while (iterator1.hasNext()) {
                requestTable.requests.get(i).get(1).add(iterator1.next().clone());
            }
            PriorityQueue<Person> queue2 = this.requests.get(i).get(-1);
            Iterator<Person> iterator2 = queue2.iterator();
            while (iterator2.hasNext()) {
                requestTable.requests.get(i).get(-1).add(iterator2.next().clone());
            }
        }
        for (Person person : buffer) {
            requestTable.buffer.add(person.clone());
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

    public synchronized boolean hasSche() {
        return scheRequest != null;
    }

    public synchronized ScheRequest getSche() {
        return this.scheRequest;
    }

    public synchronized void resetSche() {
        this.scheRequest = null;
    }

    public synchronized void addUpdate(UpdateRequest request) {
        this.updateRequest = request;
        notifyAll();
    }

    public synchronized boolean hasUpdate() {
        return updateRequest != null;
    }

    public synchronized UpdateRequest getUpdate() {
        return this.updateRequest;
    }

    public synchronized void resetUpdate() {
        this.updateRequest = null;
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



    public synchronized void waitRequest(MainTable mainTable) {
        try {
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

    public synchronized void scheMoveToMainTable(MainTable mainTable) {
        if (Debug.getDebug()) {
            System.out.println("-----------scheMoveToMainTable----------------");
        }
        // for  SCHE and UPDATE, clean all requests
        for (int floor = 1; floor <= 11; floor++) {
            for (int direction : new int[]{1, -1}) {
                PriorityQueue<Person> floorRequests = requests.get(floor).get(direction);
                while (!floorRequests.isEmpty()) {
                    Person person = floorRequests.poll();
                    if (Debug.getDebug()) {
                        System.out.println(person);
                    }
                    person.setTransfer(false);
                    mainTable.addRequest(person);
                    requestNums--;
                }
            }
        }
    }

    public synchronized String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 1; i <= 11; i++) {
            for (int direction : new int[]{1, -1}) {
                if (requests.get(i).get(direction).isEmpty()) {
                    continue;
                }
                sb.append(requests.get(i).get(direction).toString());
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
