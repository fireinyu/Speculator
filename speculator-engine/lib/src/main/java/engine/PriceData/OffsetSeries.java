package engine.PriceData;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OffsetSeries{

    List<? extends OffsetCandle> candles;
    public OffsetSeries (List<? extends OffsetCandle> candles) {
        this.candles = candles;
    }

    public TimeSeries at(ZonedDateTime anchor) {
        return new TimeSeries(this.candles.stream().map(candle -> candle.at(anchor)).collect(Collectors.toList()));
    }
}
