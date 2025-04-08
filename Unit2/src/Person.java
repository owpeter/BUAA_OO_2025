import com.oocourse.elevator3.PersonRequest;
import com.oocourse.elevator3.Request;
import tools.FloorConverter;

public class Person extends Request implements Comparable {
    private int fromFloor;
    private int toFloor;
    private int realToFloor;
    private int personId;
    private int priority;
    private int direction;
    private final Long inTime;
    private boolean transfer;

    public Person(PersonRequest request) {
        this.personId = request.getPersonId();
        this.fromFloor = FloorConverter.convertFloorToNumber(request.getFromFloor());
        this.toFloor = FloorConverter.convertFloorToNumber(request.getToFloor());
        this.direction = this.toFloor > this.fromFloor ? 1 : -1;
        this.priority = request.getPriority();
        this.transfer = false;
        this.inTime = System.currentTimeMillis();
    }

    public Person(int id, int fromFloor, int toFloor, int priority,
        int direction, boolean transfer, long inTime) {
        this.personId = id;
        this.fromFloor = fromFloor;
        this.toFloor = toFloor;
        this.priority = priority;
        this.direction = direction;
        this.transfer = transfer;
        this.inTime = inTime;
    }

    public Person clone() {
        return new Person(this.personId, this.fromFloor, this.toFloor,
        this.priority, this.direction, this.transfer, this.inTime);
    }

    public int getToFloor() {
        return toFloor;
    }

    public int getRealToFloor() {
        return realToFloor;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public int getPersonId() {
        return personId;
    }

    public int getPriority() {
        return priority;
    }

    public int getDirection() {
        return direction;
    }

    public boolean getTransfer() {
        return transfer;
    }

    public void setFromFloor(int fromFloor) {
        this.fromFloor = fromFloor;
    }

    public void setToFloor(int toFloor) {
        this.toFloor = toFloor;
    }

    public void setRealToFloor(int realToFloor) {
        this.realToFloor = realToFloor;
    }

    public void setDirection() {
        direction = this.toFloor > this.fromFloor ? 1 : -1;
    }

    public void setTransfer(boolean transfer) {
        this.transfer = transfer;
    }

    public Long getWaitTime() {
        return (System.currentTimeMillis() - inTime) / 5000;
    }

    @Override
    public int compareTo(Object o) {
        Person person = (Person) o;
        return Integer.compare(person.priority, this.priority);
    }

    public String toString() {
        return "Person{" +
                "fromFloor=" + FloorConverter.convertNumberToFloor(fromFloor) +
                ", toFloor=" + FloorConverter.convertNumberToFloor(toFloor) +
                ", personId=" + personId +
                '}';
    }
}

