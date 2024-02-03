# mizuho
This project is for my submission to the Mizuho Team (Sujit Nair).

---
### General Notes

I have used lambdas in the logging messages so that strings are not needlessly generated when not logged out.

As this code is latency sensitive, I am not explicitly validating parameters and throwing exceptions where not really necessary 
(e.g. if addOrder() is called with a null Order param, a NPE will be generated anyway, so I am not checking for null inputs upfront).
Similarly for non latency sensitive code, we would check that order id params are not zero or negative, but here, we rely on the caller
supplying valid values - its a matter of system policy.

- I assume I cannot change the Order class hence creating an OrderHolder class
- I tend to wite equals() and hashCode() for all Pojos (and also make them 
implement Comparable so they can take advantage of Java 8 HashMap optimisations for large buckets)
- Order class should ideally have a timestamp attribute (MiFID2 requires microsecond timestamps)
