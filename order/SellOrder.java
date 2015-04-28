package pkg.order;

import pkg.exception.StockMarketExpection;
import pkg.trader.Trader;

public class SellOrder extends Order {
	public SellOrder(String stockSymbol, int size, double price, Trader trader) {
		// TODO:
		// Create a new sell order
		this.stockSymbol = stockSymbol;
		this.size = size;
		this.price = price;
		this.trader = trader;
	}

	public SellOrder(String stockSymbol, int size, boolean isMarketOrder,
			Trader trader) throws StockMarketExpection {
		
		// TODO:
		// Create a new sell order which is a market order DONE
		// Set the price to 0.0, Set isMarketOrder attribute to true DONE
		//
		// If this is called with isMarketOrder == false, throw an exception DONE
		// that an order has been placed without a valid price. DONE 
		
		if (!isMarketOrder)
		{
			throw new StockMarketExpection("Order has been placed without a valid price");
		}
		this.stockSymbol = stockSymbol;
		this.size = size;
		this.isMarketOrder = isMarketOrder;
		this.price = 0.0;
		this.trader = trader;			
}

	public void printOrder() {
		System.out.println("Stock: " + stockSymbol + " $" + price + " x "
				+ size + " (Sell)");
	}
}
