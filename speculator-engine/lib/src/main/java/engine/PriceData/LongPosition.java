package engine.PriceData;

public class LongPosition <T extends Number> extends Position<T>{
    public LongPosition(T units, T unitPrice) {
        super(units, unitPrice);
        if (unitPrice.doubleValue() < 0) {
            throw new IllegalArgumentException("price cannot be negative");
        }
    }

}
