package pkg.order;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.trader.*;
import pkg.market.api.*;

public class OrderBook{
	Market market;
	HashMap<String, ArrayList<Order>> buyOrders;
	HashMap<String, ArrayList<Order>> sellOrders;

	public HashMap<String, ArrayList<Order>> getBuyOrders() {
		return buyOrders;
	}

	public HashMap<String, ArrayList<Order>> getSellOrders() {
		return sellOrders;
	}

	public OrderBook(Market m) {
		this.market = m;
		buyOrders = new HashMap<String, ArrayList<Order>>();
		sellOrders = new HashMap<String, ArrayList<Order>>();
	}

	public void addToOrderBook(Order order) {
		
		String traderName = order.getTrader().getName();
		
		if (order instanceof SellOrder) {
				if (sellOrders.containsKey(traderName))
				{//Update existing trader entry
					sellOrders.get(traderName).add(order);
				}else{//Create New
					ArrayList<Order> orderArray = new ArrayList<Order>();
					orderArray.add(order);
					this.sellOrders.put(traderName, orderArray);
				}
			}else{
				if (buyOrders.containsKey(traderName))
				{//Update existing trader entry
					buyOrders.get(traderName).add(order);
				}else{//Create New
					ArrayList<Order> orderArray = new ArrayList<Order>();
					orderArray.add(order);
					this.buyOrders.put(traderName, orderArray);
				}
			}
	}

	public void trade() {
		
		double matchPrice = createOrderBookRep(buyOrders, sellOrders);
		
		PriceSetter ps = new PriceSetter();
		ps.registerObserver(new IObserver(){

			@Override
			public void update() {
				
			}

			@Override
			public void setSubject(ISubject subject) {
				
			}
			
		});
		
		for (ArrayList<Order> orderList : buyOrders.values()) {
			for(Order o: orderList)
			{
				ps.setNewPrice(market, o.getStockSymbol(), matchPrice);
				Trader trader = o.getTrader();
					double finalMatchPrice = matchPrice * o.size;
					try {
						trader.tradePerformed(o, finalMatchPrice);
					} catch (StockMarketExpection e) {
						e.printStackTrace();
					}
			}			
		}

		for (ArrayList<Order> orderList : sellOrders.values()) {
			for(Order o: orderList)
			{
				Trader trader = o.getTrader();
					double finalMatchPrice = matchPrice * o.size;
					try {
						trader.tradePerformed(o, finalMatchPrice);
					} catch (StockMarketExpection e) {
						e.printStackTrace();
					}
			}			
		}		
		
	}
	
	public double createOrderBookRep(HashMap<String, ArrayList<Order>> bl, HashMap<String, ArrayList<Order>> sl){
		
		ArrayList<Order> sortedBuyOrderBook = new ArrayList<Order>();
		ArrayList<Order> sortedSellOrderBook = new ArrayList<Order>();
		
		//Add Both Buy and Sell Orders to separate lists
		for (Map.Entry<String, ArrayList<Order>> entry : bl.entrySet())
		{
			for (Order o:entry.getValue()){
				sortedBuyOrderBook.add(o);
			}
		}
		
		for (Map.Entry<String, ArrayList<Order>> entry : sl.entrySet())
		{
			for (Order o:entry.getValue()){
				sortedSellOrderBook.add(o);
			}			
		}		
		
	
		ArrayList<Double> prices = new ArrayList<Double>();
		ArrayList<Integer> cumulativePerPriceBid = new ArrayList<Integer>();
		ArrayList<Integer> cumulativePerPriceAsk = new ArrayList<Integer>();
		ArrayList<Integer> cumulativeDemandBid = new ArrayList<Integer>();
		ArrayList<Integer> cumulativeDemandAsk = new ArrayList<Integer>();

		
		int marketSellOrders = 0;
		int marketBuyOrders = 0;
		
		//Add regular values to the prices list
		for (int i = 0; i < (sortedBuyOrderBook.size() > sortedSellOrderBook.size()?sortedBuyOrderBook.size():sortedSellOrderBook.size()); i++)
		{
			if (i < sortedBuyOrderBook.size() && 
					!(prices.contains((Double)sortedBuyOrderBook.get(i).getPrice())) && 
					(sortedBuyOrderBook.get(i).getPrice() != 0))
			{
				prices.add(sortedBuyOrderBook.get(i).getPrice());
			}
			
			if (i < sortedSellOrderBook.size() &&
					!(prices.contains((Double)sortedSellOrderBook.get(i).getPrice())) &&
					(sortedSellOrderBook.get(i).getPrice() != 0))
			{
				prices.add(sortedSellOrderBook.get(i).getPrice());
			}			
						
		}
		
		//Sort the price list		
		Collections.sort(prices);
		Collections.reverse(prices);
		
		//Add market values to price list		
		for (int i = 0; i < (sortedBuyOrderBook.size() > sortedSellOrderBook.size()?sortedBuyOrderBook.size():sortedSellOrderBook.size()); i++)
		{
			if (i < sortedBuyOrderBook.size() && 
					(sortedBuyOrderBook.get(i).getPrice() == 0))
			{
				prices.add(0, 0.0);
				marketBuyOrders += 1;
			}
			
			if (i < sortedSellOrderBook.size() &&
					(sortedSellOrderBook.get(i).getPrice() == 0))
			{
				prices.add(prices.size(), 0.0);
				marketSellOrders += 1;
			}			
		}		
		
		//Filling cumulative lists
		for (int i = 0; i < prices.size(); i++)
		{
			cumulativePerPriceAsk.add(0);
			cumulativePerPriceBid.add(0);
			cumulativeDemandAsk.add(0);
			cumulativeDemandBid.add(0);
		}		

		//Calculating cumulative per price
		for (int i = 0; i < prices.size(); i++)
		{
			for (int j = 0; j < sortedBuyOrderBook.size(); j++)
			{
				if (sortedBuyOrderBook.get(j).getPrice() == prices.get(i))
				{
					cumulativePerPriceBid.set(i, sortedBuyOrderBook.get(j).getSize()); 
				}
			}
			
			for (int j = 0; j < sortedSellOrderBook.size(); j++)
			{
				if (sortedSellOrderBook.get(j).getPrice() == prices.get(i))
				{
					cumulativePerPriceAsk.set(i, sortedSellOrderBook.get(j).getSize()); 
				}
			}			
		}

		//calculating cumulative demands
		for (int i = 0; i < prices.size(); i++)
		{
			if (marketBuyOrders > 0){
				cumulativePerPriceAsk.set(i, 0);
				marketBuyOrders -= 1;
			}
			
			if (i == 0)
			{
				cumulativeDemandBid.set(i, cumulativePerPriceBid.get(i));  
			}else{
				if (cumulativePerPriceBid.get(i) != 0)
				{
					cumulativeDemandBid.set(i, cumulativeDemandBid.get(i-1) + cumulativePerPriceBid.get(i));					
				}
			}
		}
		
		for (int i = prices.size() - 1; i >= 0 ; i--)
		{
			if (marketSellOrders > 0){
				cumulativePerPriceBid.set(i, 0);
				marketSellOrders -= 1;
			}
			
			if (i == prices.size() - 1)
			{
				cumulativeDemandAsk.set(i, cumulativePerPriceAsk.get(i));  
			}else{
				if (cumulativePerPriceAsk.get(i) != 0)
				{
					cumulativeDemandAsk.set(i, cumulativeDemandAsk.get(i+1) + cumulativePerPriceAsk.get(i));
				}
			}
		}

		return getMatchingPrice(sortedBuyOrderBook, sortedSellOrderBook);
	}

	private double getMatchingPrice(ArrayList<Order> sortedBuyOrderBook,
			ArrayList<Order> sortedSellOrderBook) 
	{
		
		ArrayList<Double> prices = new ArrayList<Double>();
		ArrayList<Integer> cumulativePerPriceBid = new ArrayList<Integer>();
		ArrayList<Integer> cumulativePerPriceAsk = new ArrayList<Integer>();
		ArrayList<Integer> cumulativeDemandBid = new ArrayList<Integer>();
		ArrayList<Integer> cumulativeDemandAsk = new ArrayList<Integer>();

		
		int marketSellOrders = 0;
		int marketBuyOrders = 0;
		
		//Add regular values to the prices list
		for (int i = 0; i < (sortedBuyOrderBook.size() > sortedSellOrderBook.size()?sortedBuyOrderBook.size():sortedSellOrderBook.size()); i++)
		{
			if (i < sortedBuyOrderBook.size() && 
					!(prices.contains((Double)sortedBuyOrderBook.get(i).getPrice())) && 
					(sortedBuyOrderBook.get(i).getPrice() != 0))
			{
				prices.add(sortedBuyOrderBook.get(i).getPrice());
			}
			
			if (i < sortedSellOrderBook.size() &&
					!(prices.contains((Double)sortedSellOrderBook.get(i).getPrice())) &&
					(sortedSellOrderBook.get(i).getPrice() != 0))
			{
				prices.add(sortedSellOrderBook.get(i).getPrice());
			}			
						
		}
		
		//Sort the price list		
		Collections.sort(prices);
		Collections.reverse(prices);
		
		//Add market values to price list		
		for (int i = 0; i < (sortedBuyOrderBook.size() > sortedSellOrderBook.size()?sortedBuyOrderBook.size():sortedSellOrderBook.size()); i++)
		{
			if (i < sortedBuyOrderBook.size() && 
					(sortedBuyOrderBook.get(i).getPrice() == 0))
			{
				prices.add(0, 0.0);
				marketBuyOrders += 1;
			}
			
			if (i < sortedSellOrderBook.size() &&
					(sortedSellOrderBook.get(i).getPrice() == 0))
			{
				prices.add(prices.size(), 0.0);
				marketSellOrders += 1;
			}			
		}		
		
		//Filling cumulative lists
		for (int i = 0; i < prices.size(); i++)
		{
			cumulativePerPriceAsk.add(0);
			cumulativePerPriceBid.add(0);
			cumulativeDemandAsk.add(0);
			cumulativeDemandBid.add(0);
		}		

		//Calculating cumulative per price
		for (int i = 0; i < prices.size(); i++)
		{
			for (int j = 0; j < sortedBuyOrderBook.size(); j++)
			{
				if (sortedBuyOrderBook.get(j).getPrice() == prices.get(i))
				{
					cumulativePerPriceBid.set(i, sortedBuyOrderBook.get(j).getSize()); 
				}
			}
			
			for (int j = 0; j < sortedSellOrderBook.size(); j++)
			{
				if (sortedSellOrderBook.get(j).getPrice() == prices.get(i))
				{
					cumulativePerPriceAsk.set(i, sortedSellOrderBook.get(j).getSize()); 
				}
			}			
		}

		//calculating cumulative demands
		for (int i = 0; i < prices.size(); i++)
		{
			if (marketBuyOrders > 0){
				cumulativePerPriceAsk.set(i, 0);
				marketBuyOrders -= 1;
			}
			
			if (i == 0)
			{
				cumulativeDemandBid.set(i, cumulativePerPriceBid.get(i));  
			}else{
				if (cumulativePerPriceBid.get(i) != 0)
				{
					cumulativeDemandBid.set(i, cumulativeDemandBid.get(i-1) + cumulativePerPriceBid.get(i));					
				}
			}
		}
		
		for (int i = prices.size() - 1; i >= 0 ; i--)
		{
			if (marketSellOrders > 0){
				cumulativePerPriceBid.set(i, 0);
				marketSellOrders -= 1;
			}
			
			if (i == prices.size() - 1)
			{
				cumulativeDemandAsk.set(i, cumulativePerPriceAsk.get(i));  
			}else{
				if (cumulativePerPriceAsk.get(i) != 0)
				{
					cumulativeDemandAsk.set(i, cumulativeDemandAsk.get(i+1) + cumulativePerPriceAsk.get(i));
				}
			}
		}		
		
		//calculating total demand for  prices
		double matchingPrice;
		int maxDemandIndex = 0;
		
		for (int i = 0; i < prices.size(); i++)
		{
			if ((int)cumulativeDemandBid.get(i) == (int)cumulativeDemandAsk.get(i))
			{
				maxDemandIndex = i;
			}
		}
		
		matchingPrice = prices.get(maxDemandIndex);
		return matchingPrice;
	}
}
