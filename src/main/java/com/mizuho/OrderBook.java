package com.mizuho;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class OrderBook {
    private static final Logger log = LogManager.getLogger(OrderBook.class);

    // In reality the OrderBook would be per security Id (e.g. ISIN, CUSIP etc.) so we may
    // wish to store that in this object as well.
    public OrderBook() {}

    // Internal class allowing size to be changed
    private static class OrderHolder {
        private final long id; // id of order
        private final double price;
        private final char side; // B "Bid" or O "Offer"
        private final AtomicLong size;

        private OrderHolder(Order order) {
            this.id = order.getId();
            this.price = order.getPrice();
            this.side = order.getSide();
            this.size = new AtomicLong(order.getSize());
        }

        public long getId() {
            return id;
        }

        public double getPrice() {
            return price;
        }

        public char getSide() {
            return side;
        }

        public long getSize() {
            return size.get();
        }

        public void setSize(long s) {
            size.set(s);
        }
    }

    // Time complexity to insert or remove an element from a ConcurrentSkipListMap is O[log(n)]
    // Had we only wanted to see level 1 order book data (did not want to see depth of
    // market), then a PriorityBlockingQueue class (i.e. thread safe heap) would perhaps
    // be more appropriate, but as we need to see market depth too, I am using the
    // ConcurrentSkipListMap class.
    // The Map declares a <LinkedList> type in the generics (rather than just a List) as
    // we can then use the addLast() method which has O[1] in Java, as the LinkedList
    // class maintains a reference to its tail.
    private final Map<Double, LinkedList<OrderHolder> > bidQueue = new ConcurrentSkipListMap<>(Comparator.reverseOrder());
    private final Map<Double, LinkedList<OrderHolder> > offerQueue = new ConcurrentSkipListMap<>();

    // No need to sort keys by order here, so we can use a ConcurrentHashMap which offers O[1] performance
    private final Map<Long, OrderHolder> mapIdToOrder = new ConcurrentHashMap<>();

    public void addOrder(Order order) throws Exception {
        log.debug(() -> "addOrder() called for order id:" + order.getId());

        if ( order.getSize() <= 0 || order.getPrice() <= 0.0 )
            throw new Exception("Invalid size or price for order with id: " + order.getId());

        Map<Double, LinkedList<OrderHolder> > queue = getQueueFromSide(order.getSide());

        OrderHolder orderHolder = new OrderHolder(order);
        LinkedList<OrderHolder> orders = queue.computeIfAbsent(order.getPrice(), i -> new LinkedList<>());
        synchronized (orders) {
            // Java LinkedList offers good performance here as we are always adding to the tail.
            orders.addLast(orderHolder);

            // This next step is required as a concurrent remove() method may
            // have just removed the 'orders' list from the map. This is the
            // trade-off for the finer grained locking.
            queue.put(order.getPrice(), orders);
        }

        mapIdToOrder.put(order.getId(), orderHolder);
        log.debug(() -> "addOrder() exits for order id:" + order.getId());
    }

    public void removeOrder(long id) throws Exception {
        log.debug(() -> "removeOrder() called for order id:" + id);

        OrderHolder orderHolder = mapIdToOrder.get(id);
        if ( orderHolder == null ) {
            log.warn(() -> "removeOrder() did not find order with id:" + id);
            return;
        }

        Map<Double, LinkedList<OrderHolder> > queue = getQueueFromSide(orderHolder.getSide());
        List<OrderHolder> orders = queue.get(orderHolder.getPrice());
        if ( orders != null ) {
            synchronized (orders) {
                orders.remove(orderHolder);
                if (orders.size() == 0) {
                    // This call below is why the addOrder() method does a final put().
                    queue.remove(orderHolder.getPrice());
                }
            }
        }
        else {
            throw new Exception("removeOrder() did not find order with id:" + id);
        }

        mapIdToOrder.remove(id);
        log.debug(() -> "removeOrder() exits for order id:" + id);
    }

    public synchronized void modifyOrderSize(long id, long size) throws Exception {
        log.debug(() -> "modifyOrderSize() called for order id:" + id + " and size: " + size);
        OrderHolder orderHolder = mapIdToOrder.get(id);
        if ( orderHolder == null ) {
            throw new Exception("Could not find order with id: " + id); // Might have already been removed by another thread
        }

        orderHolder.setSize(size);
        log.debug(() -> "modifyOrderSize() exits for order id:" + id + " and size: " + size);
    }

    public double getPriceForSideAndLevel(char side, int level) throws Exception {
        log.debug(() -> "getPriceForSideAndLevel() called for side:" + side + " and level: " + level);
        Map<Double, LinkedList<OrderHolder> > queue = getQueueFromSide(side);

        // ConcurrentSkipListMap will give a consistent snapshot at the time the keySet()
        // is created.
        Double[] prices = queue.keySet().toArray(new Double[0]);
        if ( prices.length < level )
            throw new Exception("Level " + level + " does not exist");

        log.debug(() -> "getPriceForSideAndLevel() returns: " + prices[level-1]);
        return prices[level-1];
    }

    public long getSizeForSideAndLevel(char side, int level) throws Exception {
        log.debug(() -> "getSizeForSideAndLevel() called for side:" + side + " and level: " + level);
        Map<Double, LinkedList<OrderHolder> > queue = getQueueFromSide(side);

        double price = getPriceForSideAndLevel(side, level);
        LinkedList<OrderHolder> orders = queue.get(price);
        if ( orders == null )
            throw new Exception("Could not find size for side: " + side + " and level: " + level);

        long sum = orders.stream().mapToLong(OrderHolder::getSize).sum();
        log.debug(() -> "getSizeForSideAndLevel() returns: " + sum);
        return sum;
    }


    public List<Order> getOrdersForSide(char side) throws Exception {
        Map<Double, LinkedList<OrderHolder> > queue = getQueueFromSide(side);

        List<Order> rv = new LinkedList<>();

        // Our container classes will have done all the hard work for us....
        queue.forEach((key, value) -> rv.addAll(
                value.stream()
                        .map(o -> new Order(o.getId(), o.getPrice(), o.getSide(), o.getSize()))
                        .collect(Collectors.toList())
        ));

        return rv;
    }

    // Get the bid or the offer queue.
    private Map<Double, LinkedList<OrderHolder> > getQueueFromSide(char side) throws Exception {
        if ( side == 'B' ) {
            return bidQueue;
        }
        else if ( side == 'O' ) {
            return offerQueue;
        }
        else {
            throw new Exception("Unknown side: " + side);
        }
    }
}
