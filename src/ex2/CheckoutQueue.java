package ex2;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class CheckoutQueue {
    private final BlockingQueue<Integer> queue;

    public CheckoutQueue(int capacity) {
        queue = new ArrayBlockingQueue<>(capacity);
    }

    public void addCustomer(int customerId)
            throws InterruptedException {
        queue.put(customerId);
    }

    public int getCustomer() throws InterruptedException {
        return queue.take();
    }

    public int getSize() {
        return queue.size();
    }
}

