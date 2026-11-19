package ex2;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class CostcoSimulation {

    public static void main(String[] args) throws InterruptedException {
        Scanner input = new Scanner(System.in);

        System.out.print("Customers: ");
        int customerCount = input.nextInt();

        System.out.print("Cashiers: ");
        int cashierCount = input.nextInt();

        System.out.print("Queue size: ");
        int queueSize = input.nextInt();

        if (customerCount <= 0 || cashierCount <= 0 || queueSize <= 0) {
            System.out.println("Customers, cashiers, and queue size must all be greater than zero.");
            return;
        }

        CheckoutQueue queue = new CheckoutQueue(queueSize);
        Map<Integer, Long> readyTimes = new ConcurrentHashMap<>();
        List<Cashier> cashiers = new ArrayList<>();
        List<Thread> cashierThreads = new ArrayList<>();
        List<Thread> customerThreads = new ArrayList<>();

        long startTime = System.nanoTime();

        for (int id = 1; id <= cashierCount; id++) {
            Cashier cashier = new Cashier(id, queue, readyTimes);
            cashiers.add(cashier);

            Thread thread = new Thread(cashier, "Cashier-" + id);
            cashierThreads.add(thread);
            thread.start();
        }

        for (int id = 1; id <= customerCount; id++) {
            Thread thread = new Thread(
                    new Customer(id, queue, readyTimes), "Customer-" + id);
            customerThreads.add(thread);
            thread.start();
        }

        for (Thread thread : customerThreads) {
            thread.join();
        }

        // All customers have joined the queue, so the stop IDs follow them.
        for (int i = 0; i < cashierCount; i++) {
            queue.blockingPut(Cashier.STOP_ID);
        }

        for (Thread thread : cashierThreads) {
            thread.join();
        }

        long totalWaitNanos = 0;
        int customersServed = 0;
        for (Cashier cashier : cashiers) {
            totalWaitNanos += cashier.getTotalWaitNanos();
            customersServed += cashier.getCustomersServed();
        }

        double averageWaitSeconds = customersServed == 0
                ? 0.0
                : totalWaitNanos / (customersServed * 1_000_000_000.0);
        double elapsedSeconds = (System.nanoTime() - startTime) / 1_000_000_000.0;

        System.out.println("\n--- Simulation Results ---");
        System.out.println("Customers served: " + customersServed + " / " + customerCount);
        System.out.printf("Average checkout-line wait: %.3f seconds%n", averageWaitSeconds);
        System.out.printf("Simulation elapsed time: %.3f seconds%n", elapsedSeconds);
        System.out.printf("Cashier labor cost per hour: $%.2f%n", cashierCount * 18.0);
    }
}
