package com.mizuho;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.jupiter.api.Assertions.*;

class OrderBookTest {
    static final char OFFER = 'O';
    static final char BID = 'B';

    @Test
    public void testAddOrderForOfferWithLevels() {
        try {
            OrderBook orderBook = new OrderBook();
            orderBook.addOrder(new Order(1, 96.0, OFFER, 100L));
            orderBook.addOrder(new Order(2, 99.0, OFFER, 100L));
            orderBook.addOrder(new Order(3, 94.0, OFFER, 100L));
            orderBook.addOrder(new Order(4, 96.0, OFFER, 300L));

            List<Order> ordersForSide = orderBook.getOrdersForSide(OFFER);
            assertThat(ordersForSide.get(0).getPrice(), equalTo(94.0));
            assertThat(ordersForSide.get(1).getPrice(), equalTo(96.0));
            assertThat(ordersForSide.get(1).getSize(), equalTo(100L));
            assertThat(ordersForSide.get(2).getPrice(), equalTo(96.0));
            assertThat(ordersForSide.get(2).getSize(), equalTo(300L));
            assertThat(ordersForSide.get(3).getPrice(), equalTo(99.0));
        }
        catch(Exception e) {
            assert(false);
        }
    }

    @Test
    public void testAddOrderForBidWithLevels() {
        try {
            OrderBook orderBook = new OrderBook();
            orderBook.addOrder(new Order(1, 96.0, BID, 100L));
            orderBook.addOrder(new Order(2, 99.0, BID, 100L));
            orderBook.addOrder(new Order(3, 94.0, BID, 100L));
            orderBook.addOrder(new Order(4, 96.0, BID, 300L));

            List<Order> ordersForSide = orderBook.getOrdersForSide(BID);
            assertThat(ordersForSide.get(0).getPrice(), equalTo(99.0));
            assertThat(ordersForSide.get(1).getPrice(), equalTo(96.0));
            assertThat(ordersForSide.get(1).getSize(), equalTo(100L));
            assertThat(ordersForSide.get(2).getPrice(), equalTo(96.0));
            assertThat(ordersForSide.get(2).getSize(), equalTo(300L));
            assertThat(ordersForSide.get(3).getPrice(), equalTo(94.0));
        }
        catch(Exception e) {
            assert(false);
        }
    }

    @Test
    public void testErrorInSideChar() {
        OrderBook orderBook = new OrderBook();

        Exception exception = assertThrows(Exception.class, () -> orderBook.addOrder(new Order(1, 96.0, 'X', 100)));

        String actualMessage = exception.getMessage();
        assertThat(actualMessage, equalTo("Unknown side: X"));
    }

    @Test
    public void testRemoveOrder() {
        try {
            OrderBook orderBook = new OrderBook();
            orderBook.addOrder(new Order(1, 96.0, BID, 100L));
            orderBook.addOrder(new Order(2, 99.0, BID, 100L));
            orderBook.addOrder(new Order(3, 94.0, BID, 100L));

            orderBook.removeOrder(2);

            List<Order> ordersForSide = orderBook.getOrdersForSide(BID);
            assertThat(ordersForSide.size(), equalTo(2));
            assertThat(ordersForSide.get(0).getPrice(), equalTo(96.0));
            assertThat(ordersForSide.get(1).getPrice(), equalTo(94.0));
        }
        catch(Exception e) {
            assert(false);
        }
    }

    @Test
    public void testGetPriceForSideAndLevel(){
        try {
            OrderBook orderBook = new OrderBook();
            orderBook.addOrder(new Order(1, 96.0, BID, 100L));
            orderBook.addOrder(new Order(2, 99.0, BID, 100L));
            orderBook.addOrder(new Order(3, 96.0, BID, 300L));

            double priceForSideAndLevel = orderBook.getPriceForSideAndLevel(BID, 2);
            assertThat(priceForSideAndLevel, equalTo(96.0));
        }
        catch(Exception e) {
            assert(false);
        }
    }

    @Test
    public void testGetSizeForSideAndLevel() {
        try {
            OrderBook orderBook = new OrderBook();
            orderBook.addOrder(new Order(1, 96.0, BID, 100L));
            orderBook.addOrder(new Order(2, 99.0, BID, 100L));
            orderBook.addOrder(new Order(3, 96.0, BID, 300L));

            long sizeForSideAndLevel = orderBook.getSizeForSideAndLevel(BID, 2);
            assertThat(sizeForSideAndLevel, equalTo(400L));
        }
        catch(Exception e) {
            assert(false);
        }
    }


    @Test
    public void testModifyOrderSize() {
        try {
            OrderBook orderBook = new OrderBook();
            orderBook.addOrder(new Order(1, 96.0, BID, 100L));
            orderBook.addOrder(new Order(2, 99.0, BID, 100L));
            orderBook.addOrder(new Order(3, 96.0, BID, 300L));

            long sizeForSideAndLevel = orderBook.getSizeForSideAndLevel(BID, 2);
            assertThat(sizeForSideAndLevel, equalTo(400L));

            orderBook.modifyOrderSize(3, 500);
            long sizeForSideAndLevel2 = orderBook.getSizeForSideAndLevel(BID, 2);
            assertThat(sizeForSideAndLevel2, equalTo(600L));
        }
        catch(Exception e) {
            assert(false);
        }
    }
}