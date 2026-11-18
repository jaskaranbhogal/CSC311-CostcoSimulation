package ex2;

import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class Cashier implements Runnable {

    public static final int STOP_ID = -1;

    private final int cashierId;
    private final Buffer checkoutQueue;
    private final Map<Integer, Long> readyTimes;

    private int customersServed = 0;
    private long totalWaitNanos = 0;

    public Cashier(int cashierId, Buffer checkoutQueue,
                   Map<Integer, Long> readyTimes) {
        this.cashierId = cashierId;
        this.checkoutQueue = checkoutQueue;
        this.readyTimes = readyTimes;
    }

    @Override
    public void run() {
        try {
            while (true) {

                // Wait for the next customer in line
                int customerId = checkoutQueue.blockingGet();

                // Special ID tells the cashier to finish work
                if (customerId == STOP_ID) {
                    break;
                }

                // Calculate how long the customer waited
                Long readyTime = readyTimes.remove(customerId);

                long waitNanos = readyTime == null ? 0 :
                        System.nanoTime() - readyTime;

                totalWaitNanos += waitNanos;

                System.out.printf(
                        "Cashier %d serving Customer %d (wait: %.2f ms)%n",
                        cashierId, customerId,
                        waitNanos / 1_000_000.0);

                // Simulate checkout taking 0.5 to 1.5 seconds
                int checkoutTime = ThreadLocalRandom.current()
                        .nextInt(500, 1501);

                Thread.sleep(checkoutTime);

                customersServed++;

                System.out.printf(
                        "Cashier %d finished Customer %d.%n",
                        cashierId, customerId);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.printf(
                "Cashier %d finished. Served %d customers.%n",
                cashierId, customersServed);
    }

    public int getCustomersServed() {
        return customersServed;
    }

    public long getTotalWaitNanos() {
        return totalWaitNanos;
    }

    public double getAverageWaitMillis() {
        if (customersServed == 0) {
            return 0;
        }

        return totalWaitNanos /
                (customersServed * 1_000_000.0);
    }
}
