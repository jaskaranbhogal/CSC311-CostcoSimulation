# Costco Checkout Simulation

A Java producer-consumer lab based on Costco checkout lines. `Customer` threads finish shopping and join a shared `ArrayBlockingQueue`; `Cashier` threads remove customer IDs and simulate checkouts. The bounded queue handles thread coordination without busy-waiting.

## Team and individual contributions

- **Jaskaran Bhogal** — created the repository, implemented `Customer` and `Cashier`, and completed the separate understaffed/overstaffed experiment runner and measurements.
- **Dev Mondal** — implemented `CheckoutQueue`, helped integrate the main simulation, and documented the three original staffing-comparison runs.
- The README names the two confirmed contributors. If a third teammate participated, add their name and their actual contribution before submission.

## Build and run

From the project root, with Java installed:

```bash
mkdir -p out
javac -d out src/ex2/*.java
java -cp out ex2.CostcoSimulation
```

Enter the number of customers, cashiers, and queue capacity when prompted. The program reports customers served, average wait, total runtime, and hourly labor cost at $18/hour per cashier.

## Five lab experiments

The results report covers **five runs**, all with 20 customers:

| Experiment | Cashiers | Queue capacity |
| --- | ---: | ---: |
| Understaffed | 1 | 20 |
| Three-cashier staffing | 3 | 20 |
| Overstaffed | 10 | 20 |
| Smaller queue | 3 | 5 |
| Larger queue | 3 | 10 |

To repeat the three staffing scenarios, run the interactive `CostcoSimulation` three times with the table values. The other two can be run automatically:

```bash
# Individual experiments: 1 and 10 cashiers, queue capacity 5
java -cp out ex2.StaffingExperiments

# Queue-size experiments: 3 cashiers, queue capacities 5 and 10
java -cp out ex2.QueueSizeExperiments
```

The experiment programs each write their measurements to CSV in the current directory. Randomized shopping and checkout delays mean results vary between runs.

See [five experiment results and staffing recommendation](costco-staffing-test-results.md), [queue-size CSV](queue_size_results.csv), and [understaffed/overstaffed individual contribution](UNDERSTAFFED_OVERSTAFFED.md). The report recommends three cashiers as a starting point for 60 customers/hour **under a stated hypothetical two-minute checkout assumption**, not as a claim proven by this accelerated simulation.

## Thread synchronization

`CheckoutQueue` uses `ArrayBlockingQueue`: customers block when it is full and cashiers block when it is empty. After every customer has enqueued, the runner adds a stop marker per cashier. It then waits for all cashiers to exit before calculating the results. Customer-ready timestamps are recorded before queue insertion, so reported waiting includes time blocked by a full queue.

## Submission

Every group member should provide the URL of their own GitHub repository/fork in Brightspace, per the assignment instructions. The original repository owner can submit this repository URL; teammates using separate forks should submit their fork URLs. Confirm that each contributor's work is reflected accurately above.
