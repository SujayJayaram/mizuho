# mizuho
This project is for my submission to the Mizuho eTrading Team (Sujit Nair).

---
### General Notes

- I have used lambdas in the logging messages so that strings are not needlessly generated when not logged out.

- As this code is latency sensitive, I am not explicitly validating parameters and throwing exceptions where not really necessary 
(e.g. if addOrder() is called with a null Order param, a NPE will be generated anyway, so I am not checking for null inputs upfront).
Similarly for non latency sensitive code, we would check that order id params are not zero or negative, but here, we rely on the caller
supplying valid values - its a matter of system policy.

In terms of synchronisation/locking policy, there is a simpler solution which would lock the respective bid or offer queue
but this would offer less throughput as the locking is more coarse grained. I have chosen to minimise locking to show my abilities.
However I tend to prefer a simpler solution where possible and then look to make optimisations like this only if absolutely necessary.

Within the OrderBook class, I have chosen to use the ConcurrentSkipListMap container. This class offers O[log(n)] performance as opposed to 
the near O[1] performance of the HashMap class, but I am using this container because we need to obtain the keys in sorted order.
Without knowing more about the frequency and latency requirements of the getPriceForSideAndLevel(), getSizeForSideAndLevel() and
getOrdersForSide() methods, the sizes of the bid and offer 'queues', and the data volumes and number of levels found in each side,
it's not easy to know which would be the best container to use. (HashMap would offer better insert/remove performance but the key set would
need to be manually sorted each time at a cost of O[klog(k)] where k is the number of keys/levels).

Had we only wanted to see level 1 order book data (i.e. top of book and did not want to see depth of market or organise into levels), 
then a PriorityBlockingQueue class (i.e. thread safe heap) would perhaps be a better container choice.

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
- Might want a setter for size. This would allow us to remove the inner OrderHolder class.
- Instead of using a char for side ("B" or "O") could use enums - this would lead to less character creation. Enums use the FlyWeight
design patter meaning there is only ever two (enum) instances created.

OrderBook Class
- Might want the functionality to match off orders where appropriate and create Trades

