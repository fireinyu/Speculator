package engine.PriceData;

import java.time.Duration;
import java.time.ZonedDateTime;

public class OffsetCandle extends Datapoint{

    private Duration offset;
    public OffsetCandle (Duration offset, float price) {
        super(price);
        this.offset = offset;
    }

    public Candle at(ZonedDateTime anchor) {
        return new Candle(anchor.plus(this.offset), super.get());
    }
}
