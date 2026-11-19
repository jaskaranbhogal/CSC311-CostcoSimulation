# Costco Checkout Simulation

This project turns the producer-consumer example from class into a simple Costco checkout simulation. Customer threads shop and join a shared line. Cashier threads take customers from that line and serve them. The queue uses Java's `ArrayBlockingQueue` so the threads can safely share customer IDs.

## Team and contributions

- **Dev Mondal** — worked on `CheckoutQueue` and connected the simulation pieces and results.
- **Jaskaran Bhogal** — set up the repository and worked on the `Customer` and `Cashier` classes.


## Run it

From the project folder, compile the Java files and start the simulation:

```bash
mkdir -p out
javac -d out src/ex2/*.java
java -cp out ex2.CostcoSimulation
```

Enter the number of cashiers, queue capacity, and customers when prompted. The program will print the results after all customers have been served.

## How the threads work

- A `Customer` thread simulates shopping, records when the customer is ready, and puts the customer's ID in the checkout queue.
- A `Cashier` thread waits for the next ID, measures the customer's wait, and simulates the checkout.
- `CheckoutQueue` is backed by an `ArrayBlockingQueue`. A customer waits if the bounded queue is full, and a cashier waits if the queue is empty.
- After the customers finish joining the line, the simulation adds a stop ID for each cashier so the cashier threads can finish after serving everyone.

## Experiments

Three scenarios were tested with different queue sizes and customer counts. The results are in [costco-staffing-test-results.md ](costco-staffing-test-results.md) and [UNDERSTAFFED_OVERSTAFFED.md](UNDERSTAFFED_OVERSTAFFED.md).