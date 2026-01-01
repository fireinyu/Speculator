package engine.PriceData;

import engine.Util;

public class ShortPosition <T extends Number> extends Position<T>{
    public ShortPosition(T units, T unitPrice) {
        super(units, Util.convertNumber((Double) unitPrice.doubleValue() * -1, unitPrice));
        if (unitPrice.doubleValue() < 0) {
            throw new IllegalArgumentException("price cannot be negative");
        }
    }

    @Override
    public T getTotalValue(T unitPrice) {
        return super.getTotalValue(Util.convertNumber((Double) unitPrice.doubleValue() * -1, unitPrice));
    }

}