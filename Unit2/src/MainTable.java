import com.oocourse.elevator3.Request;

import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MainTable {
    private boolean endFlag = false;
    private Queue<Request> requests;
    private final Lock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();

    public MainTable() {
        requests = new java.util.LinkedList<>();
    }

    public boolean isEmpty() {
        lock.lock();
        try {
            return requests.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    public boolean isEnd() {
        lock.lock();
        try {
            return endFlag;
        } finally {
            lock.unlock();
        }
    }

    public void setEnd() {
        lock.lock();
        try {
            endFlag = true;
            notEmpty.signal();
            // 唤醒scheduler结束进程
        } finally {
            lock.unlock();
        }
    }

    public void checkKill() {
        lock.lock();
        try {
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public void addRequest(Request request) {
        // InputHandler
        lock.lock();
        try {
            requests.add(request);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    public Request getAndRemoveRequest() {
        // scheduler
        lock.lock();
        try {
            if (requests.isEmpty()) {
                try {
                    notEmpty.await();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (requests.isEmpty()) {
                return null;
            }
            return requests.poll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (Request request : requests) {
            sb.append(request.toString());
            sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
