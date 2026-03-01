package engine.PriceData;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OffsetSeries<V extends Number>{

    List<? extends OffsetCandle<? extends V>> candles;
    public OffsetSeries (List<? extends OffsetCandle<? extends V>> candles) {
        this.candles = candles;
    }

    public TimeSeries<V> at(ZonedDateTime anchor) {
        return new TimeSeries<>(this.candles.stream().map(candle -> candle.at(anchor)).collect(Collectors.toList()));
    }
}
