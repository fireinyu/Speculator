package engine.PriceData;

import engine.Util;

public class Position <T extends Number> {

    public static <T extends Number> Position<T> empty() {
        return new Empty<>();
    }
    private T avgCostPerUnit;
    private T units;

    public Position (T units, T avgCostPerUnit) {
        this.units = units;
        this.avgCostPerUnit = avgCostPerUnit;
    }

    public T getTotalCost () {
        Double rawCost = this.avgCostPerUnit.doubleValue() * this.units.doubleValue();
        return Util.convertNumber(rawCost, this.avgCostPerUnit);
    }

    public T getTotalValue (T valuePerUnit) {
        Double rawValue = valuePerUnit.doubleValue() * this.units.doubleValue();
        return Util.convertNumber(rawValue, this.avgCostPerUnit);
    }

    public T getNetValue (T valuePerUnit) {
        Double rawValue = this.getTotalValue(valuePerUnit).doubleValue() - this.getTotalCost().doubleValue();
        return Util.convertNumber(rawValue, this.avgCostPerUnit);
    }

    private static class Empty <T extends Number> extends Position<T> {
        public Empty() {
            super(null, null);
        }

        @Override
        public T getTotalCost () {
            return Util.convertNumber(0, super.units);
        }

        @Override
        public T getTotalValue (T valuePerUnit) {
            return Util.convertNumber(0, super.units);
        }

        @Override
        public T getNetValue (T valuePerUnit) {
            return Util.convertNumber(0, super.units);
        }
    }

}
