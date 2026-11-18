package ex2;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Customer implements Runnable {

    private final int customerId;
    private final Buffer checkoutQueue;
    private final Map<Integer, Long> readyTimes;

    public Customer(int customerId, Buffer checkoutQueue,
                    Map<Integer, Long> readyTimes) {
        this.customerId = customerId;
        this.checkoutQueue = checkoutQueue;
        this.readyTimes = readyTimes;
    }

    @Override
    public void run() {
        try {
            // Simulate shopping for 0.3 to 1 second
            int shoppingTime = ThreadLocalRandom.current()
                    .nextInt(300, 1001);

            System.out.printf(
                    "Customer %d is shopping.%n", customerId);

            Thread.sleep(shoppingTime);

            // Record when the customer is ready to check out
            readyTimes.put(customerId, System.nanoTime());

            // Join the shared checkout queue
            checkoutQueue.blockingPut(customerId);

            System.out.printf(
                    "Customer %d joined the checkout line.%n",
                    customerId);

        } catch (InterruptedException e) {
            readyTimes.remove(customerId);
            Thread.currentThread().interrupt();
        }
    }
}
