package engine.PriceData;

import java.time.ZonedDateTime;

public class Candle  extends Datapoint implements Timed{

    private ZonedDateTime time = ZonedDateTime.now();

    public Candle (ZonedDateTime time, float price) {
        super(price);
        this.time = time;
    }

    public Candle (ZonedDateTime time, Datapoint dp) {
        super(dp.get());
        this.time = time;
    }

    @Override
    public ZonedDateTime getTime () {
        return this.time;
    }


}
