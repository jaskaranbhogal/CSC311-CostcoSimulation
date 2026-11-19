package ex2;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CheckoutQueue implements Buffer {

    private final BlockingQueue<Integer> queue;

    public CheckoutQueue(int capacity) {
        queue = new ArrayBlockingQueue<>(capacity);
    }

    @Override
    public void blockingPut(int value)
            throws InterruptedException {
        queue.put(value);
    }

    @Override
    public int blockingGet()
            throws InterruptedException {
        return queue.take();
    }

    public void addCustomer(int customerId)
            throws InterruptedException {
        blockingPut(customerId);
    }

    public int getCustomer()
            throws InterruptedException {
        return blockingGet();
    }

    public int getSize() {
        return queue.size();
    }
}
