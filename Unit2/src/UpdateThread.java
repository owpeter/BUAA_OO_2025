import com.oocourse.elevator3.TimableOutput;
import com.oocourse.elevator3.UpdateRequest;

import java.util.HashMap;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class UpdateThread implements Runnable{
    private CyclicBarrier phase1End, phase2End;
    private CountDownLatch phase1Latch, phase2Latch;
    private HashMap<Integer, RequestTable> requestTables;
    private UpdateRequest updateRequest;
    private Elevator A, B;

    public UpdateThread(CyclicBarrier phase1End, CyclicBarrier phase2End, CountDownLatch phase1Latch, CountDownLatch phase2Latch,
                        HashMap<Integer, RequestTable> requestTables, UpdateRequest updateRequest,
                        Elevator A, Elevator B) {
        this.phase1End = phase1End;
        this.phase2End = phase2End;
        this.phase1Latch = phase1Latch;
        this.phase2Latch = phase2Latch;
        this.requestTables = requestTables;
        this.updateRequest = updateRequest;
        this.A = A;
        this.B = B;
    }

    public void run() {
        try {
            ReentrantLock lock = new ReentrantLock();
            A.setTfloorLock(lock);
            B.setTfloorLock(lock);
            A.setLatches(phase1Latch, phase2Latch, phase1End, phase2End);
            B.setLatches(phase1Latch, phase2Latch, phase1End, phase2End);
            int a = updateRequest.getElevatorAId();
            int b = updateRequest.getElevatorBId();
            requestTables.get(a).addUpdate(updateRequest);
            requestTables.get(b).addUpdate(updateRequest);
            phase1Latch.await(); // 等待两个电梯完成下人
            TimableOutput.println(String.format("UPDATE-BEGIN-%d-%d", a, b));
            phase1End.await(); // notify 2 elevator
            phase2Latch.await(); // 等待2个电梯完成update
            TimableOutput.println(String.format("UPDATE-END-%d-%d", a, b));
            phase2End.await(); // notify 2 elevator
        } catch (InterruptedException | BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }
}
