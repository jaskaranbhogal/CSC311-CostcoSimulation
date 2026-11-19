# Costco Simulation: Five Experiments and Staffing Recommendation

The experiment results below use the `ex2` classes, including `Customer`, `Cashier`, and an `ArrayBlockingQueue`-backed `CheckoutQueue`. All tests use 20 customers. Each row is **one observed run**, not an average over multiple runs. Shopping durations are randomized from 0.3 to 1.0 seconds and checkout durations from 0.5 to 1.5 seconds, so rerunning can produce different values.

## Results

The first three results were previously recorded with queue capacity 20. The two additional queue-capacity runs were measured using `ex2.QueueSizeExperiments` with **three cashiers held constant**.

| Experiment | Customers | Cashiers | Queue capacity | Served | Avg. wait (sec) | Runtime (sec) | Cashier wages/hour |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| 1. Understaffed | 20 | 1 | 20 | 20/20 | 9.365 | 21.464 | $18 |
| 2. Three-cashier staffing | 20 | 3 | 20 | 20/20 | 2.555 | 7.348 | $54 |
| 3. Overstaffed | 20 | 10 | 20 | 20/20 | 0.405 | 3.312 | $180 |
| 4. Smaller checkout queue | 20 | 3 | 5 | 20/20 | 2.568 | 7.130 | $54 |
| 5. Larger checkout queue | 20 | 3 | 10 | 20/20 | 2.639 | 7.254 | $54 |

Recorded queue-size measurements are also in [queue_size_results.csv](queue_size_results.csv). A separate set of 1- and 10-cashier runs with queue capacity 5, completed as an individual contribution, is documented in [UNDERSTAFFED_OVERSTAFFED.md](UNDERSTAFFED_OVERSTAFFED.md) and [understaffed_overstaffed_results.csv](understaffed_overstaffed_results.csv).

## Observations and cost-service trade-offs

- **One cashier:** At $18/hour, this was the least expensive staffing scenario, but customers waited an average of 9.365 seconds in the recorded 20-customer burst.
- **Three cashiers:** At $54/hour, average wait dropped to 2.555 seconds in the staffing comparison. That is substantially faster than the one-cashier run at an additional $36/hour.
- **Ten cashiers:** At $180/hour, average wait dropped further to 0.405 seconds. Compared with three cashiers, wages increased by $126/hour for about 2.15 seconds less average waiting during this burst.
- **Queue capacity 5 versus 10:** With both scenarios using three cashiers and 20 customers, average waits were 2.568 and 2.639 seconds, respectively. The 0.071-second difference is small and may be caused by randomized shopping and service times; this single comparison does **not** show that a smaller queue is intrinsically faster. A smaller bounded queue can block customers from entering until space opens, while a larger queue can hold more waiting IDs. The customer ready timestamp is recorded *before* the blocking queue insertion, so the reported wait includes time blocked by a full queue.
- Queue capacity does not itself change cashier wages. Staffing changes the labor cost; capacity mainly changes how many customers can be waiting *inside* the bounded queue. Every recorded run served all 20 customers.

## Recommendation: 60 customers per hour at $18/hour per cashier

**Recommend starting with three cashiers ($54/hour) as a practical staffing choice**, then measure real arrival and checkout times before treating it as optimal. The burst simulation suggests three cashiers offer a useful wait/cost balance compared with one or ten, but the simulated checkout times (0.5–1.5 seconds) are much faster than real Costco checkouts. These burst-test results alone **cannot establish** the minimum staffing required for 60 evenly arriving customers per hour.

For an explicitly **hypothetical planning example**, suppose real checkouts average **two minutes per customer**. Each cashier could then serve about **30 customers/hour**. One cashier would not keep up with 60/hour; two would be fully utilized even before random slowdowns; three would provide around **90 checkouts/hour of nominal capacity** (60/90 = about **67% utilization**) at **$54/hour**. Ten cashiers would cost **$180/hour** and likely be excessive under that assumption. A different real checkout time can change the right recommendation.

## How to reproduce

From the repository root:

```bash
mkdir -p out
javac -d out src/ex2/*.java

# Interactive simulation: enter customers, cashiers, and queue size
java -cp out ex2.CostcoSimulation

# Two individual staffing-extremes tests (1 and 10 cashiers; capacity 5)
java -cp out ex2.StaffingExperiments

# Two controlled queue-size tests (3 cashiers; capacities 5 and 10)
java -cp out ex2.QueueSizeExperiments
```

`StaffingExperiments` overwrites `understaffed_overstaffed_results.csv` and `QueueSizeExperiments` overwrites `queue_size_results.csv` when rerun. The three staffing-comparison rows above were recorded with the interactive simulation at capacity 20. Use multiple trials and calibrated real-world arrival/service times for stronger conclusions.
