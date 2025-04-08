# 电梯改造

两部电梯必须在两次移动楼层操作内将所有乘客放出，并在**两部电梯都停下后**输出UPDATE-BEGIN就地开始改造动作

**初始状态**：B电梯位于换乘楼层的下面一层，A电梯位于换乘楼层的上面一层。两部轿厢的运行速度均提升至 0.2s/层

双轿厢电梯（A 电梯和 B 电梯）**可以不受 RECEIVE 约束而从目标楼层移动一层以离开目标楼层。**

电梯开始双轿厢升级后，其之前与两部电梯有关的RECEIVE全部自动取消。也即[时间戳]UPDATE-BEGIN-电梯ID输出后，之前仍有效的RECEIVE-乘客ID-电梯ID全部取消，相关乘客处于未分配状态，电梯也视为未RECEIVE到任何乘客。

保证改造为双轿厢的两部电梯不会再接收到改造请求或临时调度请求。

不会在临时调度请求执行期间收到改造请求。

# 升级的线程调度问题

Elevator 类:
持有两个 CountDownLatch (phase1Latch, phase2Latch) 和两个 CyclicBarrier (phase1EndBarrier, phase2EndBarrier) 的引用。
在完成阶段 1 工作后，调用 phase1Latch.countDown() 通知 Scheduler。
然后调用 phase1EndBarrier.await() 进入等待状态，直到 Scheduler 和另一个 Elevator 也到达此屏障。
被唤醒后，执行阶段 2 工作。
完成阶段 2 后，调用 phase2Latch.countDown() 通知 Scheduler。
然后调用 phase2EndBarrier.await() 再次等待。
最后结束。

Scheduler 类:
持有两个 Elevator 的 Runnable 实例、两个 Latch 和两个 Barrier。
run() 方法首先创建并启动两个 Elevator 线程。
调用 phase1Latch.await() 等待两个 Elevator 都完成阶段 1（即 phase1Latch 计数减到 0）。
被唤醒后，执行自己的中间操作。
然后调用 phase1EndBarrier.await()。因为 Barrier 的参与者是 3（2 个 Elevator + 1 个 Scheduler），当 Scheduler 调用 await() 时，如果两个 Elevator 已经在 await() 等待，那么屏障条件满足，所有 3 个线程（Scheduler 和两个 Elevator）将同时被唤醒并继续执行。这巧妙地实现了“Scheduler 唤醒两个 Elevator”。
接着，Scheduler 调用 phase2Latch.await() 等待两个 Elevator 完成阶段 2。
被唤醒后，执行最终操作。
最后，调用 phase2EndBarrier.await()，同样会释放等待的 Elevator 线程，允许它们最终结束。
Scheduler 自身任务完成并结束。