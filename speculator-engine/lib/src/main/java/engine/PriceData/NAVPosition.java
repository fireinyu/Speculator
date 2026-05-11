package engine.PriceData;

import engine.Util;

public class NAVPosition extends Position{
    public static NAVPosition makeEmpty() {
        return new NAVPosition(0, 0);
    }

    public static NAVPosition from(Position position, double price) {
        return new NAVPosition(position.getUnits(), price * position.getUnits());
    }
    public static NAVPosition makeLong(double units, double price){
        return new NAVPosition(units, price*units);
    }
    public static NAVPosition makeShort(double units, double price) {
        return new NAVPosition(-units, -price*units);
    }
    private double cost;

    private NAVPosition(double units, double cost) {
        super(units);
        this.cost = cost;
    }

    public double getTotalCost () {
        return cost;
    }

    public double getTotalValue (double price) {
        return price * getUnits();

    }

    public double getNetValue (double price) {
        return price * getUnits() - cost;
    }

    public NAVPosition apply(NAVPosition delta) {
        return new NAVPosition(getUnits() + delta.getUnits(), cost + delta.cost);
    }

}
