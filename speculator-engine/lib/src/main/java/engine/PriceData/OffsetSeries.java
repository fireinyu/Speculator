package engine.PriceData;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OffsetSeries<V extends Number> extends Series<V>{

    List<? extends OffsetCandle<? extends V>> candles;
    public OffsetSeries (List<? extends OffsetCandle<? extends V>> candles) {
        super(candles);
        this.candles = candles;
    }

    public TimeSeries<V> at(ZonedDateTime anchor) {
        return new TimeSeries<>(this.candles.stream().map(candle -> candle.at(anchor)).collect(Collectors.toList()));
    }
}
