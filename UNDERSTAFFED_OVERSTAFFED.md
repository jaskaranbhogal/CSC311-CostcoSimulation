# Understaffed and overstaffed experiments

These are the two assigned staffing-extremes experiments only. They use the existing ex2 Customer, Cashier, and CheckoutQueue classes.

## How to run

Run the main method in src/ex2/StaffingExperiments.java from IntelliJ (or compile src/ex2/*.java and run ex2.StaffingExperiments). It runs both scenarios and writes understaffed_overstaffed_results.csv in the current working directory.

## Configuration and one measured run

| Scenario | Customers | Cashiers | Queue capacity | Customers served | Avg. wait (seconds) | Runtime (seconds) | Cashier labor per hour |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Understaffed | 20 | 1 | 5 | 20 | 9.399 | 20.518 | $18 |
| Overstaffed | 20 | 10 | 5 | 20 | 0.276 | 2.750 | $180 |

Both runs completed successfully with 20 of 20 customers served. One cashier caused longer checkout waits. Ten cashiers reduced waiting substantially, but increased hourly labor cost by $162. Using 10 cashiers is costly for this 20-customer test.

## Methodology notes

- The experiments use 300-1000 ms randomized shopping times and 500-1500 ms randomized checkout times from the existing classes.
- The measured wait starts when a customer finishes shopping, so it includes delays while a full queue blocks entry.
- The runs simulate a burst of 20 customers, not evenly spaced arrivals at 60 customers per hour.
- Results vary on each run; the table and CSV represent one run each, not statistical averages across repeated trials.
- The $18/hour amount is a staffing wage assumption, not the actual expense of these seconds-long runs.
- This file covers only the two individual staffing-extremes experiments. The team-wide three-cashier comparison, both additional queue-capacity experiments, and the 60-customers/hour recommendation are documented in [costco-staffing-test-results.md](costco-staffing-test-results.md); the queue-size runner is [src/ex2/QueueSizeExperiments.java](src/ex2/QueueSizeExperiments.java).
