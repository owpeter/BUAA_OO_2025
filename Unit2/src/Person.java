import tools.FloorConverter;

public class Person {
    private Integer fromFloor;
    private Integer toFloor;
    private Integer personId;
    private Integer priority;
    private Integer elevatorId;

    public Person(String fromFloor, String toFloor, Integer personId, Integer priority, Integer elevatorId) {
        this.fromFloor = FloorConverter.convertFloorToNumber(fromFloor);
        this.toFloor = FloorConverter.convertFloorToNumber(toFloor);
        this.personId = personId;
        this.priority = priority;
        this.elevatorId = elevatorId;
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
}

