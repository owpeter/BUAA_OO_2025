package tools;

public class FloorConverter {
//    public static void main(String[] args) {
//        String[] floors = {"B4", "B3", "B2", "B1", "F1", "F2", "F3", "F4", "F5", "F6", "F7"};
//
//        for (String floor : floors) {
//            int floorNumber = convertFloorToNumber(floor);
//            System.out.println(floor + " -> " + floorNumber);
//        }
//    }

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
        if (floorNumber < 5) {
            return "B" + (5 - floorNumber);
        } else if (floorNumber <= 11) {
            return "F" + (floorNumber - 4);
        } else {
            return "非法输入";
        }
    }
}
