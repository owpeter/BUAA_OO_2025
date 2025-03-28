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

    public Person(String fromFloor, String toFloor,
        int personId, int priority, int elevatorId) {
        this.fromFloor = FloorConverter.convertFloorToNumber(fromFloor);
        this.toFloor = FloorConverter.convertFloorToNumber(toFloor);
        this.direction = this.toFloor > this.fromFloor ? 1 : -1;
        this.personId = personId;
        this.priority = priority;
        this.elevatorId = elevatorId;
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
        return (System.currentTimeMillis() - inTime) / 1000;
    }

    @Override
    public int compareTo(Object o) {
        Person person = (Person) o;
        return Integer.compare(person.priority, this.priority);
    }
}

