package engine.PriceData;

import java.time.ZonedDateTime;

public class Candle <Y extends Number> extends Datapoint<Y> implements Timed{

    private ZonedDateTime time = ZonedDateTime.now();

    public Candle (ZonedDateTime time, Y price) {
        super(price);
        this.time = time;
    }

    public Candle (ZonedDateTime time, Datapoint<Y> dp) {
        super(dp.get());
        this.time = time;
    }

    @Override
    public ZonedDateTime getTime () {
        return this.time;
    }


}
