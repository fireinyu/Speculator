package engine.PriceData;

import engine.Util;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TimeSeries <V extends Number> extends Series<V>{

    public static <V extends Number> ZonedDateTime getAnchor(
            List<? extends TimeSeries<V>> seriesLs
            ) {
        return seriesLs.stream()
                .map(TimeSeries::until)
                .min(ZonedDateTime::compareTo)
                .orElse(null);
    }

    private List<ZonedDateTime> times;

    public TimeSeries (List<ZonedDateTime> times, List<V> prices) {
//        this(List.of());
//        Log.d("debug_merge", "" + times.size() + ", " + prices.size());
        this(IntStream.range(0, times.size()).mapToObj(i -> new Candle<>(times.get(i), prices.get(i))).collect(Collectors.toList()));


    }

    @Override
    public TimeSeries<V> slice(int from, int to) {
        return new TimeSeries<V>(this.times.subList(from, to), this.get().subList(from, to));
    }

    public <D extends Datapoint<? extends V> & Timed> TimeSeries (D[] datapoints) {
        super(datapoints);
        this.times = new ArrayList<>();
        for (D dp : datapoints) {
            this.times.add(dp.getTime());
        }
    }
    public <D extends Datapoint<? extends V> & Timed> TimeSeries (List<D> datapoints) {
        super(datapoints);
        this.times = new ArrayList<>();
        for (D dp : datapoints) {
            this.times.add(dp.getTime());
        }
    }


    public TimeSeries (Series<V> src, Duration interval, ZonedDateTime start) {
        super(src);
        this.times = new ArrayList<>();
        for (int i = 0; i < this.size(); i++) {
            times.add(start);
            start = start.plus(interval);
        }
    }

    public TimeSeries (TimeSeries<V> src) {
        super(src);
        this.times = List.copyOf(src.times);
    }

    public TimeSeries<V> merge (TimeSeries<? extends V> src) {
        if (src.size() == 0) {
            return new TimeSeries<>(this.getTimes(), this.get());
        }
        List<ZonedDateTime> times = this.getTimes();
        List<V> prices = new ArrayList<>(this.get());
        List<ZonedDateTime> newTimes = src.getTimes();
        List<? extends V> newPrices = src.get();
        for (int i = 0; i < src.size(); i++) {
            ZonedDateTime time = newTimes.get(i);
            int idx = Collections.binarySearch(times, time);
            if (idx >= 0) {
                prices.set(idx, newPrices.get(i));
            } else {
                idx = -idx - 1;
                prices.add(idx, newPrices.get(i));
                times.add(idx, time);
            }
        }


        TimeSeries<V> ts = new TimeSeries<>(times, prices);

        return ts;
    }

    public void extendLeft (TimeSeries<? extends V> src){
        assert src.until().isBefore(this.from());
        super.extendLeft(src);
        List<ZonedDateTime> combinedTimes = List.copyOf(src.times);
        combinedTimes.addAll(this.times);
        this.times = combinedTimes;
    }

    public void extendRight (TimeSeries<V> src){
        assert src.from().isAfter(this.until());
        super.extendRight(src);
        this.times.addAll(src.times);
    }

    public void extendLeft (Series<V> src, Duration interval) {
        ZonedDateTime start = this.from().minus(interval.multipliedBy(src.size()));
        this.extendLeft(new TimeSeries<>(src, interval, start));
    }

    public void extendRight (Series<V> src, Duration interval) {
        ZonedDateTime start = this.until().plus(interval);
        this.extendRight(new TimeSeries<>(src, interval, start));
    }

    public List<ZonedDateTime> getTimes () {
        return this.times;
    }

    public ZonedDateTime from () {
        return this.times.get(0);
    }

    public ZonedDateTime until () {
        return this.times.get(this.times.size()-1);
    }

    public <R extends Number> TimeSeries<R> map(Function<? super ZonedDateTime, ? extends ZonedDateTime> timeMapper, Function<? super V, ? extends R> priceMapper) {
        List<V> prices = super.get();
        return new TimeSeries<>(IntStream.range(0, this.times.size()).boxed()
                .map(i -> new Candle<>(timeMapper.apply(this.times.get(i)), priceMapper.apply(prices.get(i))))
                .collect(Collectors.toList())
        );

    }

    public <R> List<R> extract (BiFunction<? super ZonedDateTime, ? super V, ? extends R> extractor) {

        List<V> prices = super.get();
        return IntStream.range(0, this.times.size()).boxed()
                .map(i -> extractor.apply(this.times.get(i), prices.get(i)))
                .collect(Collectors.toList());

    }

    public V priceAt (ZonedDateTime anchor) {
        int anchorIndex = Collections.binarySearch(this.times, anchor);
        V anchorPrice;
        if (anchorIndex >= 0) {
            anchorPrice = this.get().get(anchorIndex);
        } else {
            anchorIndex = -(anchorIndex + 1);
            if (anchorIndex == 0) {
                anchorPrice = this.get().get(0);
            } else if (anchorIndex == this.size()) {
                anchorPrice = this.get().get(this.size()-1);
            } else {
                double leftPrice = this.get().get(anchorIndex - 1).doubleValue();
                double rightPrice = this.get().get(anchorIndex).doubleValue();
                double leftMs = this.getTimes().get(anchorIndex - 1).toEpochSecond();
                double rightMs = this.getTimes().get(anchorIndex).toEpochSecond();
                double res = leftPrice + (rightPrice-leftPrice) * (anchor.toEpochSecond()-leftMs)/(rightMs-leftMs);
                anchorPrice = Util.convertNumber(res, this.get().get(0));
            }
        }
        return anchorPrice;
    }

    public Candle<V> get(int index) {
        return new Candle<>( this.times.get(index),super.get(index));
    }

    public Candle<V> getLast() {
        return this.get(this.size()-1);
    }


}
