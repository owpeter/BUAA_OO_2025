package tools;

public class FloorConverter {
    public static int convertFloorToNumber(String floor) {
        if (floor.startsWith("B")) {
            // 地下楼层：B4 是最低层，对应 1，B3 对应 2，依此类推
            int basementLevel = Integer.parseInt(floor.substring(1));
            return 5 - basementLevel;
        } else if (floor.startsWith("F")) {
            // 地上楼层：F1 是紧接在 B1 之上的第一层，对应 5，F7 对应 11
            int floorLevel = Integer.parseInt(floor.substring(1));
            return 4 + floorLevel;
        } else {
            // 非法输入
            return -1;
        }
    }

    public static String convertNumberToFloor(int floorNumber) {
        if (floorNumber < 5 && floorNumber >= 1) {
            return "B" + (5 - floorNumber);
        } else if (floorNumber <= 11 && floorNumber >= 5) {
            return "F" + (floorNumber - 4);
        } else if (floorNumber == -1 || floorNumber == 0) {
            return "None";
        } else {
            throw new IllegalArgumentException("Invalid floor number" + floorNumber);
        }
    }
}
