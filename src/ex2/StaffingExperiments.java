package ex2;

import java.io.IOException;
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

/** Runs only the understaffed and overstaffed Costco checkout experiments. */
public class StaffingExperiments {
    private static final int CUSTOMERS = 20;
    private static final int QUEUE_CAPACITY = 5;
    private static final double CASHIER_WAGE_PER_HOUR = 18.00;
    private static final long TASK_TIMEOUT_SECONDS = 120;

    private static class Result {
        final String scenario;
        final int cashierCount;
        final int customersServed;
        final double averageWaitSeconds;
        final double elapsedSeconds;

        Result(String scenario, int cashierCount, int customersServed,
               double averageWaitSeconds, double elapsedSeconds) {
            this.scenario = scenario;
            this.cashierCount = cashierCount;
            this.customersServed = customersServed;
            this.averageWaitSeconds = averageWaitSeconds;
            this.elapsedSeconds = elapsedSeconds;
        }

        double hourlyLaborCost() {
            return cashierCount * CASHIER_WAGE_PER_HOUR;
        }

        String csvRow() {
            return String.format(Locale.US, "%s,%d,%d,%d,%d,%.3f,%.3f,%.2f",
                    scenario, CUSTOMERS, cashierCount, QUEUE_CAPACITY,
                    customersServed, averageWaitSeconds, elapsedSeconds, hourlyLaborCost());
        }
    }

    private static Result runExperiment(String scenario, int cashierCount) throws Exception {
        CheckoutQueue queue = new CheckoutQueue(QUEUE_CAPACITY);
        Map<Integer, Long> readyTimes = new ConcurrentHashMap<>();
        List<Cashier> cashiers = new ArrayList<>();
        List<Future<?>> customerTasks = new ArrayList<>();
        List<Future<?>> cashierTasks = new ArrayList<>();

        // Separate pools prevent customers from starving the waiting cashiers.
        ExecutorService customerPool = Executors.newFixedThreadPool(CUSTOMERS);
        ExecutorService cashierPool = Executors.newFixedThreadPool(cashierCount);
        long start = System.nanoTime();

        try {
            for (int id = 1; id <= cashierCount; id++) {
                Cashier cashier = new Cashier(id, queue, readyTimes);
                cashiers.add(cashier);
                cashierTasks.add(cashierPool.submit(cashier));
            }
            for (int id = 1; id <= CUSTOMERS; id++) {
                customerTasks.add(customerPool.submit(new Customer(id, queue, readyTimes)));
            }

            // Wait until every customer has entered the checkout queue.
            for (Future<?> task : customerTasks) {
                task.get(TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }

            // One stop marker per cashier, after all real customer IDs.
            for (int i = 0; i < cashierCount; i++) {
                queue.blockingPut(Cashier.STOP_ID);
            }
            for (Future<?> task : cashierTasks) {
                task.get(TASK_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            }

            int served = 0;
            long totalWaitNanos = 0;
            for (Cashier cashier : cashiers) {
                served += cashier.getCustomersServed();
                totalWaitNanos += cashier.getTotalWaitNanos();
            }
            if (served != CUSTOMERS || !readyTimes.isEmpty()) {
                throw new IllegalStateException("Incomplete checkout: served " + served
                        + " of " + CUSTOMERS + ", unprocessed IDs: " + readyTimes.keySet());
            }

            double elapsedSeconds = (System.nanoTime() - start) / 1_000_000_000.0;
            double averageWaitSeconds = totalWaitNanos / (served * 1_000_000_000.0);
            Result result = new Result(scenario, cashierCount, served,
                    averageWaitSeconds, elapsedSeconds);
            System.out.printf(Locale.US,
                    "%n%s: %d/%d served; avg wait %.3f s; runtime %.3f s; labor $%.2f/hour%n",
                    scenario, served, CUSTOMERS, averageWaitSeconds,
                    elapsedSeconds, result.hourlyLaborCost());
            return result;
        } finally {
            customerPool.shutdownNow();
            cashierPool.shutdownNow();
        }
    }

    public static void main(String[] args) throws Exception {
        System.out.println("=== UNDERSTAFFED: 1 cashier, 20 customers, queue capacity 5 ===");
        Result understaffed = runExperiment("Understaffed", 1);
        System.out.println("\n=== OVERSTAFFED: 10 cashiers, 20 customers, queue capacity 5 ===");
        Result overstaffed = runExperiment("Overstaffed", 10);

        // Record measurements for the lab report. Results vary with every run.
        Path results = Path.of("understaffed_overstaffed_results.csv");
        List<String> lines = List.of(
                "scenario,customers,cashiers,queue_capacity,served,average_wait_seconds,runtime_seconds,hourly_labor_cost_usd",
                understaffed.csvRow(),
                overstaffed.csvRow());
        Files.write(results, lines, StandardCharsets.UTF_8);
        System.out.println("\nMeasurements saved to " + results.toAbsolutePath());
        System.out.printf(Locale.US, "Hourly labor cost difference: $%.2f%n",
                overstaffed.hourlyLaborCost() - understaffed.hourlyLaborCost());
    }
}
