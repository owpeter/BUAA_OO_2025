import tools.FloorConverter;

public class Person implements Comparable {
    private Integer fromFloor;
    private Integer toFloor;
    private Integer personId;
    private Integer priority;
    private Integer elevatorId;
    private Integer direction;
    private final Long inTime;

    public Person(String fromFloor, String toFloor,
        Integer personId, Integer priority, Integer elevatorId) {
        this.fromFloor = FloorConverter.convertFloorToNumber(fromFloor);
        this.toFloor = FloorConverter.convertFloorToNumber(toFloor);
        this.direction = this.toFloor > this.fromFloor ? 1 : -1;
        this.personId = personId;
        this.priority = priority;
        this.elevatorId = elevatorId;
        this.inTime = System.currentTimeMillis();
    }

    public Integer getToFloor() {
        return toFloor;
    }

    public Integer getFromFloor() {
        return fromFloor;
    }

    public Integer getElevatorId() {
        return elevatorId;
    }

    public Integer getPersonId() {
        return personId;
    }

    public Integer getPriority() {
        return priority;
    }

    public Integer getDirection() {
        return direction;
    }

    public Long getWaitTime() {
        return (System.currentTimeMillis() - inTime) / 10000;
    }

    @Override
    public int compareTo(Object o) {
        Person person = (Person) o;
        return this.priority.compareTo(person.priority);
    }
}

