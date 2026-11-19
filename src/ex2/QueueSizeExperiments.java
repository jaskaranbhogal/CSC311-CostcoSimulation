package ex2;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/** Compares two queue capacities while keeping customers and cashiers fixed. */
public class QueueSizeExperiments {
    private static final int CUSTOMERS = 20;
    private static final int CASHIERS = 3;
    private static final double HOURLY_WAGE = 18.0;
    private static final int[] CAPACITIES = {5, 10};
    private static final long TIMEOUT_SECONDS = 120;

    private static class Result {
        final int capacity;
        final int served;
        final double averageWaitSeconds;
        final double elapsedSeconds;

        Result(int capacity, int served, double averageWaitSeconds, double elapsedSeconds) {
            this.capacity = capacity;
            this.served = served;
            this.averageWaitSeconds = averageWaitSeconds;
            this.elapsedSeconds = elapsedSeconds;
        }

        String csvRow() {
            return String.format(Locale.US, "Queue capacity %d,%d,%d,%d,%d,%.3f,%.3f,%.2f",
                    capacity, CUSTOMERS, CASHIERS, capacity, served,
                    averageWaitSeconds, elapsedSeconds, CASHIERS * HOURLY_WAGE);
        }
    }

    private static Result runExperiment(int capacity) throws Exception {
        CheckoutQueue queue = new CheckoutQueue(capacity);
        Map<Integer, Long> readyTimes = new ConcurrentHashMap<>();
        List<Cashier> cashiers = new ArrayList<>();
        List<Future<?>> customerTasks = new ArrayList<>();
        List<Future<?>> cashierTasks = new ArrayList<>();
        ExecutorService customerPool = Executors.newFixedThreadPool(CUSTOMERS);
        ExecutorService cashierPool = Executors.newFixedThreadPool(CASHIERS);
        long start = System.nanoTime();

        try {
            for (int id = 1; id <= CASHIERS; id++) {
                Cashier cashier = new Cashier(id, queue, readyTimes);
                cashiers.add(cashier);
                cashierTasks.add(cashierPool.submit(cashier));
            }
            for (int id = 1; id <= CUSTOMERS; id++) {
                customerTasks.add(customerPool.submit(new Customer(id, queue, readyTimes)));
            }
            for (Future<?> task : customerTasks) {
                task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }
            for (int i = 0; i < CASHIERS; i++) {
                queue.blockingPut(Cashier.STOP_ID);
            }
            for (Future<?> task : cashierTasks) {
                task.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }

            int served = 0;
            long waitNanos = 0;
            for (Cashier cashier : cashiers) {
                served += cashier.getCustomersServed();
                waitNanos += cashier.getTotalWaitNanos();
            }
            if (served != CUSTOMERS || !readyTimes.isEmpty()) {
                throw new IllegalStateException("Expected " + CUSTOMERS + " checkouts; got " + served
                        + "; still waiting: " + readyTimes.keySet());
            }
            Result result = new Result(capacity, served,
                    waitNanos / (served * 1_000_000_000.0),
                    (System.nanoTime() - start) / 1_000_000_000.0);
            System.out.printf(Locale.US,
                    "Capacity %d: %d/%d served; average wait %.3f s; runtime %.3f s; labor $%.2f/hour%n",
                    capacity, served, CUSTOMERS, result.averageWaitSeconds,
                    result.elapsedSeconds, CASHIERS * HOURLY_WAGE);
            return result;
        } finally {
            customerPool.shutdownNow();
            cashierPool.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        List<String> csv = new ArrayList<>();
        csv.add("scenario,customers,cashiers,queue_capacity,served,average_wait_seconds,runtime_seconds,hourly_labor_cost_usd");
        for (int capacity : CAPACITIES) {
            System.out.printf("%n=== QUEUE CAPACITY %d: %d customers, %d cashiers ===%n",
                    capacity, CUSTOMERS, CASHIERS);
            csv.add(runExperiment(capacity).csvRow());
        }
        Path output = Path.of("queue_size_results.csv");
        Files.write(output, csv, StandardCharsets.UTF_8);
        System.out.println("\nSaved measurements to " + output.toAbsolutePath());
    }
}
