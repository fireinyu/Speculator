package engine.PriceData;

import engine.Util;

public class NAVPosition extends Position{
    public static NAVPosition makeEmpty() {
        return new NAVPosition(0, 0);
    }
    public static NAVPosition makeLong(double units, double price){
        return new NAVPosition(units, price);
    }
    public static NAVPosition makeShort(double units, double price) {
        return new NAVPosition(-units, price);
    }
    private double avgCostPerUnit;

    public NAVPosition(double units, double avgCostPerUnit) {
        super(units);
        this.avgCostPerUnit = avgCostPerUnit;
    }

    public double getTotalCost () {
        Double rawCost = this.avgCostPerUnit * super.getUnits();
        return Util.convertNumber(rawCost, this.avgCostPerUnit);
    }

    public double getTotalValue (double price) {
        Double rawValue = price * super.getUnits();
        return Util.convertNumber(rawValue, this.avgCostPerUnit);
    }

    public double getNetValue (double price) {
        Double rawValue = this.getTotalValue(price) - this.getTotalCost();
        return Util.convertNumber(rawValue, this.avgCostPerUnit);
    }

}
