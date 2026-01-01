package engine.PriceData;

import java.time.Duration;
import java.time.ZonedDateTime;

public class OffsetCandle<V extends Number> extends Datapoint<V>{

    private Duration offset;
    public OffsetCandle (Duration offset, V price) {
        super(price);
        this.offset = offset;
    }

    public Candle<V> at(ZonedDateTime anchor) {
        return new Candle<>(anchor.plus(this.offset), super.get());
    }
}
