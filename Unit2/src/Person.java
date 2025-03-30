import com.oocourse.elevator1.PersonRequest;
import tools.FloorConverter;

public class Person implements Comparable {
    private int fromFloor;
    private int toFloor;
    private int personId;
    private int priority;
    private int elevatorId;
    private int direction;
    private final Long inTime;
    private boolean transfer;

    public Person(PersonRequest request) {
        this.personId = request.getPersonId();
        this.fromFloor = FloorConverter.convertFloorToNumber(request.getFromFloor());
        this.toFloor = FloorConverter.convertFloorToNumber(request.getToFloor());
        this.direction = this.toFloor > this.fromFloor ? 1 : -1;
        this.priority = request.getPriority();
        this.elevatorId = request.getElevatorId();
        this.transfer = false;
        this.inTime = System.currentTimeMillis();
    }

    public int getToFloor() {
        return toFloor;
    }

    public int getFromFloor() {
        return fromFloor;
    }

    public int getElevatorId() {
        return elevatorId;
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
}

