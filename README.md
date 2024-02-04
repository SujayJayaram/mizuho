# mizuho
This project is for my submission to the Mizuho eTrading Team (Sujit Nair).

---
### General Notes

- I have used lambdas in the logging messages so that strings are not needlessly generated when not logged out.

- As this code is latency sensitive, I am not explicitly validating parameters and throwing exceptions where not really necessary 
(e.g. if addOrder() is called with a null Order param, a NPE will be generated anyway, so I am not checking for null inputs upfront).
Similarly for non latency sensitive code, we would check that order id params are not zero or negative, but here, we rely on the caller
supplying valid values - its a matter of system policy.

In terms of synchronisation/locking policy, there is a simple solution which would lock the respective bid or offer queue
but this would offer less throughput as the locking is corase grained. I have chosen to minimise locking to show my abilities.
However I tend to prefer a simpler solution where possible and then look to make optimisations like this only if absolutely necessary.

- I assume I cannot change the Order class hence creating an OrderHolder class
- I tend to write equals() and hashCode() for all Pojos (and also make them 
implement Comparable so they can take advantage of Java 8 HashMap optimisations for large buckets)
- Order class should ideally have a timestamp attribute (MiFID2 requires microsecond timestamps)

### Part B

Order Class
- Needs equals() and hashCode() methods in case they are used elsewhere in other containers. 
If they implement Comparable<Order> this will help if they are used in Java 8+ HashMaps with large
bucket sizes (HashMap uses a tree instead of a Linked List for large buckets)
- Should use BigDecimal for price.
- Should have a timestamp attribute (to microsec precision for MiFID2)
- Might want to contain an order type (e.g. "Good Till Day", "Good Till Cancel", "All Or Nothing","Immediate Or Cancel" etc)
- Might want to support Iceberg orders (e.g. there might be a "shownSize" and "fullSize" etc)
- Might want a setter for size.
- Instead of using a char for side ("B" or "O") could use enums - this would lead to less character creation. Enums use the FlyWeight
design patter meaning there is only ever two (enum) instances created.

OrderBook Class
- Might want the functionality for to match off orders and create Trades
- 

