import com.oocourse.elevator3.ScheRequest;
import com.oocourse.elevator3.TimableOutput;
import tools.Debug;
import tools.FloorConverter;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.ReentrantLock;

public class Elevator implements Runnable {
    private int id;
    private int curFloor;
    private int curPersonNums;
    private int direction;
    private int TFloor = -1;
    private int topFloor = 11;
    private int bottomFloor = 1;
    private Advice status;
    private MainTable mainTable;
    private final RequestTable requestTable;
    private CopyOnWriteArrayList<Person> personInElevator = new CopyOnWriteArrayList<>();
    private Strategy strategy;
    private ReentrantLock TfloorLock;
    private CyclicBarrier phase1End, phase2End;
    private CountDownLatch phase1Latch, phase2Latch;
    private long lastTime = System.currentTimeMillis();
    private long simulateSumTime = 0;
    private int speed = 400;
    private int openCloseTime = 400;
    private final int scheTime = 1000;
    private final int updateTime = 1000;

    public Elevator(int id, MainTable mainTable, RequestTable requestTable) {
        this.id = id;
        this.curFloor = 5;
        this.curPersonNums = 0;
        this.direction = 1;
        this.mainTable = mainTable;
        this.requestTable = requestTable;
        this.strategy = new Strategy(requestTable);
    }

    public Elevator(int curFloor, int curPersonNums, int direction, int TopFloor, int BottomFloor,
        RequestTable requestTable, CopyOnWriteArrayList<Person> personInElevator) {
        this.curFloor = curFloor;
        this.curPersonNums = curPersonNums;
        this.direction = direction;
        this.topFloor = TopFloor;
        this.bottomFloor = BottomFloor;
        this.requestTable = requestTable;
        this.personInElevator = personInElevator;
        this.strategy = new Strategy(requestTable);
    }

    public Elevator(int id, int curFloor, int curPersonNums, int direction, int TopFloor, int BottomFloor, int TFloor,
        RequestTable requestTable, CopyOnWriteArrayList<Person> personInElevator, Advice status) {
        this.id = id;
        this.curFloor = curFloor;
        this.curPersonNums = curPersonNums;
        this.direction = direction;
        this.topFloor = TopFloor;
        this.bottomFloor = BottomFloor;
        this.TFloor = TFloor;
        this.requestTable = requestTable;
        this.personInElevator = personInElevator;
        this.strategy = new Strategy(requestTable);
        this.status = status;
    }

    public void run() {
        while (true) {
            //在这里将requestTable.buffer中的请求放入requests，同时输出receive
            requestTable.fromBufferToRequests(TFloor, topFloor, bottomFloor,id, false);
            Advice advice = strategy.getAdvice(curFloor, curPersonNums,
                    direction, personInElevator, false, topFloor, bottomFloor);
            status = advice;
            if (advice == Advice.WAIT) {
                requestTable.waitRequest(mainTable);
            } else if (advice == Advice.MOVE) {
                setLastTime();
                move(speed);
            } else if (advice == Advice.OPEN) {
                if (curFloor == TFloor) {
                    TFloorOpenAndClose(openCloseTime);
                } else {
                    openAndClose(openCloseTime);
                }
            } else if (advice == Advice.REVERSE) {
                direction = -direction;
            } else if (advice == Advice.KILL) {
                break;
            } else if (advice == Advice.SCHE) {
                schedule();
            } else if (advice == Advice.UPDATE) {
                update();
            }
        }
        // throw new RuntimeException("Elevator " + id + " is dead");
    }





    private void setLastTime() {
        this.lastTime = System.currentTimeMillis();
    }

    private void trySleep(int time) {
        long curTime = System.currentTimeMillis();
        if (curTime - lastTime < time) {
            try {
                Thread.sleep(time - (curTime - lastTime));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void schedule() {
        TimableOutput.println(String.format("SCHE-BEGIN-%d", id));
        setLastTime();
        ScheRequest scheRequest = requestTable.getSche();
        int toFloor = FloorConverter.convertFloorToNumber(scheRequest.getToFloor());
        if (curFloor < toFloor) {
            direction = 1;
        } else {
            direction = -1;
        }
        while (curFloor != toFloor) {
            move((int) Math.round(scheRequest.getSpeed() * 1000));
        }
        scheOpenAndClose(scheTime);
        requestTable.resetSche();
        TimableOutput.println(String.format("SCHE-END-%d", id));
    }

    private long simulateSchedule() {
        long ret = 0;
        ScheRequest scheRequest = requestTable.getSche();
        int toFloor = FloorConverter.convertFloorToNumber(scheRequest.getToFloor());
        if (curFloor < toFloor) {
            direction = 1;
        } else {
            direction = -1;
        }
        while (curFloor != toFloor) {
            ret += (int) Math.round(scheRequest.getSpeed() * 1000);
            simulateMove();
        }
        ret += scheTime;
        simulateScheOpenAndClose();
        requestTable.resetSche();
        return ret;
    }

    private void update(){
        try {
            if (curPersonNums != 0) {
                TimableOutput.println(String.format(
                        "OPEN-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
                setLastTime();
                goOut();
                trySleep(openCloseTime);
                TimableOutput.println(String.format(
                        "CLOSE-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
            }
            phase1Latch.countDown(); // notify scheduler
            phase1End.await(); // 等待scheduler输出update begin
            setLastTime();
            requestTable.scheMoveToMainTable(mainTable);
            //TODO: 其他更新相关设置？
            TFloor = FloorConverter.convertFloorToNumber(requestTable.getUpdate().getTransferFloor());
            speed = 200;
            updateFloor();
            trySleep(updateTime);
            requestTable.resetUpdate();
            phase2Latch.countDown(); // notify scheduler
            phase2End.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateFloor() {
        if (id == requestTable.getUpdate().getElevatorAId()) {
            bottomFloor = TFloor;
            curFloor = bottomFloor + 1;
        } else {
            topFloor = TFloor;
            curFloor = topFloor - 1;
        }
    }

    private void move(int time) {
        if (TFloor != -1) {
            if (curFloor + direction == TFloor) {
                // 将要到达TFloor，获得锁
                TfloorLock.lock();
                curFloor = TFloor;
                trySleep(time);
                TimableOutput.println(String.format(
                        "ARRIVE-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
                setLastTime();
                return;
            }
        }
        // normal condition
        if (direction == 1) {
            curFloor++;
        } else {
            curFloor--;
        }
        trySleep(time);
        TimableOutput.println(String.format(
            "ARRIVE-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        setLastTime();
    }

    private void simulateMove() {
        if (direction == 1) {
            curFloor++;
        } else {
            curFloor--;
        }
    }

    private void openAndClose(int time) {
        TimableOutput.println(String.format(
            "OPEN-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        setLastTime();
        goOut();
        goIn();
        requestTable.moveToMainTable(curFloor, mainTable, false);
        trySleep(time);
        TimableOutput.println(String.format(
            "CLOSE-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        setLastTime();
    }

    private void simulateOpenAndClose(long timeStamp) {
        simulateGoOut(timeStamp);
        simulateGoIn();
        requestTable.moveToMainTable(curFloor, mainTable, true);
    }

    private void scheOpenAndClose(int time) {

        TimableOutput.println(String.format(
            "OPEN-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        setLastTime();
        goOut();
        requestTable.scheMoveToMainTable(mainTable);
        trySleep(time);
        TimableOutput.println(String.format(
            "CLOSE-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        setLastTime();
    }

    private void simulateScheOpenAndClose() {
        personInElevator.clear();
        curPersonNums = 0;
    }

    private void TFloorOpenAndClose(int time) {
        TimableOutput.println(String.format(
                "OPEN-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        setLastTime();
        TFloorGoOut();
        if ((topFloor == TFloor && direction == 1) || (bottomFloor == TFloor && direction == -1)) {
            // TODO: maybe bug??
            // 考虑到空载过来已经掉过头了才会开门
            direction = -direction;
        }
        goIn();
        trySleep(time);
        TimableOutput.println(String.format(
                "CLOSE-%s-%d", FloorConverter.convertNumberToFloor(curFloor), id));
        setLastTime();
        move(speed);
        // 离开TFloor，释放锁
        TfloorLock.unlock();
        requestTable.moveToMainTable(TFloor, mainTable, false);
    }

    private void goOut() {
        for (Person person : personInElevator) {

            if (person.getToFloor() == curFloor) {
                TimableOutput.println(String.format(
                    "OUT-S-%d-%s-%d",
                    person.getPersonId(),
                    FloorConverter.convertNumberToFloor(curFloor), id));
            } else {
                TimableOutput.println(String.format(
                    "OUT-F-%d-%s-%d",
                    person.getPersonId(),
                    FloorConverter.convertNumberToFloor(curFloor), id));
                person.setFromFloor(curFloor);
                person.setDirection();
                person.setTransfer(true);
                requestTable.addPersonToRequest(person);
            }
        }
        curPersonNums = 0;
        personInElevator.clear();
    }

    private void simulateGoOut(long timeStamp) {
        for (Person person : personInElevator) {
            if (person.getToFloor() != curFloor) {
                person.setFromFloor(curFloor);
                person.setDirection();
                person.setTransfer(true);
                requestTable.addPersonToRequest(person);
            } else {
                if (person.getToFloor() == curFloor && person.getRealToFloor() != curFloor) {
                    simulateSumTime += person.getPriority() * (timeStamp + speed * 2L + (long) (person.getRealToFloor() - curFloor) * speed);
                } else {
                    simulateSumTime += person.getPriority() * timeStamp;
                }
            }
        }
        curPersonNums = 0;
        personInElevator.clear();
    }

    private void TFloorGoOut() {
        for (Person person : personInElevator) {
            if (person.getRealToFloor() == curFloor) {
                TimableOutput.println(String.format(
                    "OUT-S-%d-%s-%d",
                    person.getPersonId(),
                    FloorConverter.convertNumberToFloor(curFloor), id));
            } else {
                TimableOutput.println(String.format(
                        "OUT-F-%d-%s-%d",
                        person.getPersonId(),
                        FloorConverter.convertNumberToFloor(curFloor), id));
                person.setFromFloor(curFloor);
                person.setToFloor(person.getRealToFloor());
                person.setTransfer(true);
                requestTable.addPersonToRequest(person);
            }
        }
        curPersonNums = 0;
        personInElevator.clear();
    }

    private void goIn() {
        while (curPersonNums < 6) {
            Person person = requestTable.getAndRemovePerson(curFloor, direction);

            if (person == null) {
                break;
            }
            if (person.getTransfer()) {
                person.setTransfer(false);
                TimableOutput.println(String.format("RECEIVE-%d-%d", person.getPersonId(), id));
            }
            personInElevator.add(person);
            curPersonNums++;
            TimableOutput.println(String.format(
                "IN-%d-%s-%d", person.getPersonId(),
                FloorConverter.convertNumberToFloor(curFloor), id));

            if (Debug.getDebug()) {
                //System.out.println("is removed");
                //System.out.println(requestTable.getFloorRequests(curFloor, direction).size());
                //System.out.println("In curPersonNums: " + curPersonNums);
            }
        }
    }

    private void simulateGoIn() {
        while (curPersonNums < 6) {
            Person person = requestTable.getAndRemovePerson(curFloor, direction);

            if (person == null) {
                break;
            }
            person.setTransfer(false);
            personInElevator.add(person);
            curPersonNums++;
            if (Debug.getDebug()) {
                //System.out.println("is removed");
                //System.out.println(requestTable.getFloorRequests(curFloor,direction).size());
            }
        }
    }

    public long simulate(long startTime) {
        requestTable.fromBufferToRequests(TFloor, topFloor, bottomFloor,id, true);
        if (Debug.getDebug()) {
            System.out.println(this);
        }
        long timeStamp = startTime;
        while (true) {
            Advice advice = strategy.getAdvice(curFloor, curPersonNums,
                direction, personInElevator, true, topFloor, bottomFloor);
            if (Debug.getDebug()) {
                System.out.println(
                    Thread.currentThread().getName() +
                    ": advice " + advice + " , curTime: " + timeStamp);
            }

            if (advice == Advice.SCHE) {
                timeStamp += simulateSchedule();
                if (Debug.getDebug()) {
                    System.out.println("-----------simulate sche-----------");
                    System.out.println(this);
                    System.out.println("-----------simulate sche end-----------");
                }
            } else if (advice == Advice.MOVE) {
                simulateMove();
                timeStamp += speed;
            } else if (advice == Advice.OPEN) {
                //TODO: TFopen and close???
                simulateOpenAndClose(timeStamp);
                timeStamp += openCloseTime;
            } else if (advice == Advice.REVERSE) {
                direction = -direction;
            } else if (advice == Advice.WAIT) {
                break;
            }
        }
        return timeStamp;
    }

    public long getSimulateSumTime() {
        return simulateSumTime;
    }

    public void addPersonToBuffer(Person person) {
        requestTable.addPersonToBuffer(person);
    }

    public Elevator clone() {
        CopyOnWriteArrayList<Person> newPersonInElevator = new CopyOnWriteArrayList<>();
        for (Person person : personInElevator) {
            newPersonInElevator.add(person.clone());
        }
        Elevator elevator = new Elevator(id, curFloor, curPersonNums, direction, topFloor, bottomFloor, TFloor,
                requestTable.clone(), newPersonInElevator, status);
        return elevator;
    }

    public int getId() {
        return id;
    }

    public Advice getStatus() {
        return status;
    }

    public int getTopFloor() {
        return topFloor;
    }

    public int getBottomFloor() {
        return bottomFloor;
    }

    public int getTFloor() {
        return TFloor;
    }

    public void setLatches (CountDownLatch phase1Latch, CountDownLatch phase2Latch, CyclicBarrier phase1End, CyclicBarrier phase2End) {
        this.phase1End = phase1End;
        this.phase2End = phase2End;
        this.phase1Latch = phase1Latch;
        this.phase2Latch = phase2Latch;
    }

    public void setTfloorLock(ReentrantLock TfloorLock) {
        this.TfloorLock = TfloorLock;
    }

    @Override
    public String toString() {
        return "Elevator " + id + ", curF: " + FloorConverter.convertNumberToFloor(curFloor)
            + ", dir: " + direction + ", "
            + personInElevator.toString() + '\n' + requestTable.toString();
    }
}
