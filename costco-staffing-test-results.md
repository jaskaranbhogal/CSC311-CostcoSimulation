# Costco Simulation Test Results

We compiled the program with `javac` and ran the three staffing experiments with 20 customers and a queue capacity of 20. The program chooses shopping and checkout times at random, so these numbers describe our runs; another run can come out differently. My partner handled the two queue-size experiments.

## Results

| Experiment | Cashiers | Queue spaces | Customers served | Average wait in line | Run time | Cashier cost per hour |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| Understaffed | 1 | 20 | 20 / 20 | 9.365 sec | 21.464 sec | $18 |
| Optimal | 3 | 20 | 20 / 20 | 2.555 sec | 7.348 sec | $54 |
| Overstaffed | 10 | 20 | 20 / 20 | 0.405 sec | 3.312 sec | $180 |

## Observations

Adding cashiers made the line move a lot faster it decreased the time by more than 3 times but it also increased the hourly cost. Ten cashier made it the waiting time almost none however, in real life the customer would most likely delay even if the cashier were fast so and also even those there were 10 cashiers the results i would say were diminishing.

## Recommendation for 60 customers per hour

For 60 customers per hour, I would recommend about 3 cahsierrs and after running the test it is at 9 seconds avg and 20 total time which is really considering the cost 54 dollars per hour so you can make a lot of money and still have efficiency without creating a infinite backlog.



