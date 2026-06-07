package engine.PriceData;

import java.time.ZonedDateTime;

public class NAVPosition extends CostPosition{

    private double price;
    private ZonedDateTime at;
    NAVPosition(double units, double cost, double price, ZonedDateTime at) {
        super(units, cost);
        this.price = price;
        this.at = at;
    }

    public double getTotalValue () {
        return price * getUnits();

    }

    public double getNetValue () {
        return price * getUnits() - getTotalCost();
    }

    public ZonedDateTime getDateTime() {
        return at;
    }

    public double getPrice() {
        return price;
    }
}
