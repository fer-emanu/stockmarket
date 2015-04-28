package pkg.trader;

import java.util.ArrayList;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.order.*;
import pkg.stock.Stock;

public class Trader {
	String name;
	double cashInHand;
	ArrayList<Order> position;
	ArrayList<Order> ordersPlaced;
	
	public String getName()
	{
		return this.name;
	}
	
	public Trader(String name, double cashInHand) {
		this.name = name;
		this.cashInHand = cashInHand;
		this.position = new ArrayList<Order>();
		this.ordersPlaced = new ArrayList<Order>();
	}

	public void buyFromBank(Market m, String symbol, int volume)
			throws StockMarketExpection {
		
		Stock stock = m.getStockForSymbol(symbol);
		
		if (stock.getPrice()*volume > this.cashInHand){
			throw new StockMarketExpection("The stock's price is larger than the cash possessed");
		}else{
			BuyOrder newBuyOrder = new BuyOrder(symbol, volume, stock.getPrice(), this);
			this.cashInHand -= stock.getPrice()*volume;
		}
	}

	public void placeNewOrder(Market m, String symbol, int volume,
			double price, OrderType orderType) throws StockMarketExpection {
			if (price > this.cashInHand){
				throw new StockMarketExpection("Cannot place order for stock: SBUX since there is not enough money. Trader: " + this.name);
			}
			
	        for (Order order : ordersPlaced) {
	            if (order.getStockSymbol() == symbol){
	            	throw new StockMarketExpection("A trader cannot place two orders for the same stock");
	            }
	            	
	        }
			
			switch (orderType)
			{
			case BUY:
				BuyOrder buyOrder = new BuyOrder(symbol, volume, price, this);
				ordersPlaced.add(buyOrder);
				m.addOrder(buyOrder);
				break;
			case SELL:
				SellOrder sellOrder = new SellOrder(symbol, volume, price, this);
				m.addOrder(sellOrder);
				ordersPlaced.add(sellOrder);
				break;
			}			
	}

	public void placeNewMarketOrder(Market m, String symbol, int volume,
			double price, OrderType orderType) throws StockMarketExpection {
			if (price > this.cashInHand){
				throw new StockMarketExpection("Cannot place order for stock:" + symbol  + "since there is not enough money. Trader: " + this.name);
			}
			
	        for (Order order : ordersPlaced) {
	            if (order.getStockSymbol() == symbol){
	            	throw new StockMarketExpection("A trader cannot place two orders for the same stock");
	            }
	            	
	        }
			
            switch (orderType)
            {
              case BUY:
              BuyOrder buyOrder = new BuyOrder(symbol, volume, true, this);
              ordersPlaced.add(buyOrder);
              m.addOrder(buyOrder);
              break;
              case SELL:
              SellOrder sellOrder = new SellOrder(symbol, volume, true, this);
              m.addOrder(sellOrder);
              ordersPlaced.add(sellOrder);
              break;
            }			
	}

	public void tradePerformed(Order order, double matchPrice)
			throws StockMarketExpection {
		
		if (order instanceof BuyOrder)
		{
			if (matchPrice < this.cashInHand)
			{
				this.cashInHand -= matchPrice;
				this.position.add(order);
			}else{
				throw new StockMarketExpection("Cannot perform order for stock:" + order.getStockSymbol() + ", since there is not enough money. Trader: " + this.name);
			}
			
		}
		if (order instanceof SellOrder)
		{
			this.cashInHand += matchPrice;
			if (this.position.contains(order)){
				this.position.remove(this.position.indexOf(order));
			}
			
		}
	}

	public void printTrader() {
		System.out.println("Trader Name: " + name);
		System.out.println("=====================");
		System.out.println("Cash: " + cashInHand);
		System.out.println("Stocks Owned: ");
		for (Order o : position) {
			o.printStockNameInOrder();
		}
		System.out.println("Stocks Desired: ");
		for (Order o : ordersPlaced) {
			o.printOrder();
		}
		System.out.println("+++++++++++++++++++++");
		System.out.println("+++++++++++++++++++++");
	}
}
