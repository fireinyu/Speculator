package engine.PriceData;

import java.time.ZonedDateTime;

public class CostPosition extends Position{
    public static CostPosition makeEmpty() {
        return new CostPosition(0, 0);
    }

    public static CostPosition from(Position position, double price) {
        return new CostPosition(position.getUnits(), price * position.getUnits());
    }
    public static CostPosition makeLong(double units, double price){
        return new CostPosition(units, price*units);
    }
    public static CostPosition makeShort(double units, double price) {
        return new CostPosition(-units, -price*units);
    }
    private double cost;

   CostPosition(double units, double cost) {
        super(units);
        this.cost = cost;
    }

    public double getTotalCost () {
        return cost;
    }

    public double getAvgCost() {
        return cost/getUnits();
    }
    public CostPosition apply(CostPosition delta) {
        return new CostPosition(getUnits() + delta.getUnits(), cost + delta.cost);
    }

    public NAVPosition evaluate(ZonedDateTime at, double price) {
       return new NAVPosition(getUnits(), cost, price, at);
    }

}
