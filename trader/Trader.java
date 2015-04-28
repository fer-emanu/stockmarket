package pkg.trader;

import java.util.ArrayList;

import pkg.exception.StockMarketExpection;
import pkg.market.Market;
import pkg.order.*;
import pkg.stock.Stock;

public class Trader {
	// Name of the trader
	String name;
	// Cash left in the trader's hand
	double cashInHand;
	// Stocks owned by the trader
	ArrayList<Order> position;
	// Orders placed by the trader
	ArrayList<Order> ordersPlaced;
	
	public String getName()
	{
		return this.name;
	}
	
	public Trader(String name, double cashInHand) {
		super();
		this.name = name;
		this.cashInHand = cashInHand;
		this.position = new ArrayList<Order>();
		this.ordersPlaced = new ArrayList<Order>();
	}

	public void buyFromBank(Market m, String symbol, int volume)
			throws StockMarketExpection {
		// Buy stock straight from the bank DONE
		// Need not place the stock in the order list ?
		// Add it straight to the user's position DONE
		// If the stock's price is larger than the cash possessed, then an
		// exception is thrown DONE
		// Adjust cash possessed since the trader spent money to purchase a
		// stock. DONE
		
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
		// Place a new order and add to the orderlist
		// Also enter the order into the orderbook of the market.
		// Note that no trade has been made yet. The order is in suspension
		// until a trade is triggered.
		//
		// If the stock's price is larger than the cash possessed, then an
		// exception is thrown
		// A trader cannot place two orders for the same stock, throw an
		// exception if there are multiple orders for the same stock.
		// Also a person cannot place a sell order for a stock that he does not
		// own. Or he cannot sell more stocks than he possesses. Throw an
		// exception in these cases.
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

	public void tradePerformed(Order o, double matchPrice)
			throws StockMarketExpection {
		// Notification received that a trade has been made, the parameters are
		// the order corresponding to the trade, and the match price calculated
		// in the order book. Note than an order can sell some of the stocks he
		// bought, etc. Or add more stocks of a kind to his position. Handle
		// these situations.

		// Update the trader's orderPlaced, position, and cashInHand members
		// based on the notification.
		
		if (o instanceof BuyOrder)
		{
			if (matchPrice < this.cashInHand)
			{
				this.cashInHand -= matchPrice;
				this.position.add(o);
			}else{
				throw new StockMarketExpection("Cannot perform order for stock:" + o.getStockSymbol() + ", since there is not enough money. Trader: " + this.name);
			}
			
		}
		if (o instanceof SellOrder)
		{
			this.cashInHand += matchPrice;
			if (this.position.contains(o)){
				this.position.remove(this.position.indexOf(o));
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
