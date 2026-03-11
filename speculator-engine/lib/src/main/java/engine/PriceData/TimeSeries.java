package engine.PriceData;

import engine.Util;

import java.sql.Time;
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

    public static <V extends Number> TimeSeries<V> empty() {
        return new EmptyTimeSeries<>();
    }
    private List<ZonedDateTime> times;

    public TimeSeries (List<ZonedDateTime> times, List<V> prices) {
//        this(List.of());
//        Log.d("debug_merge", "" + times.size() + ", " + prices.size());
        this(IntStream.range(0, times.size()).mapToObj(i -> new Candle<>(times.get(i), prices.get(i))).collect(Collectors.toList()));


    }

    public <D extends Datapoint<? extends V> & Timed> TimeSeries (List<D> datapoints) {
        super(datapoints);
        this.times = new ArrayList<>();
        for (D dp : datapoints) {
            this.times.add(dp.getTime());
        }
    }

    public TimeSeries (TimeSeries<V> src) {
        super(src);
        this.times = src.times;
    }

    private TimeSeries (Series<V> src, List<ZonedDateTime> times) {
        super(src);
        this.times = times;
    }

    @Override
    public TimeSeries<V> slice(int from, int to) {
        if (from >= to) {
            return new EmptyTimeSeries<>();
        }
        List<ZonedDateTime> slicedTimes = this.times.subList(from, to);
        if ((this.size()+super.excess)/(double)(from-to) > super.loadRatio) {
            slicedTimes = new ArrayList<>(slicedTimes);
        }
        Series<V> res = super.slice(from, to);
        System.out.println("slice result: " + from + to + res.size() );
        return new TimeSeries<>(res, slicedTimes);
    }


    public TimeSeries<V> merge (TimeSeries<V> src) {
        if (src.size() == 0) {
            return new TimeSeries<>(this);
        }
        ArrayList<Candle<V>> resultCandles = new ArrayList<>();
        List<ZonedDateTime> times = this.getTimes();
        List<V> prices = this.get();
        List<ZonedDateTime> newTimes = src.getTimes();
        List<V> newPrices = src.get();
        int thisIndex = 0;
        int srcIndex = 0;
        int thisSize = this.size();
        int srcSize = src.size();
        while (thisIndex < thisSize && srcIndex < srcSize) {
            ZonedDateTime thisTime = times.get(thisIndex);
            ZonedDateTime srcTime = newTimes.get(srcIndex);
            int compareResult = thisTime.compareTo(srcTime);
            Candle<V> newCandle = null;
            if (compareResult < 0) {
                newCandle = new Candle<>(thisTime, prices.get(thisIndex));
                thisIndex++;
            } else if (compareResult == 0){
                newCandle = new Candle<>(srcTime, newPrices.get(srcIndex));
                thisIndex++;
                srcIndex++;

            } else {
                newCandle = new Candle<>(srcTime, newPrices.get(srcIndex));
                srcIndex++;
            }
            resultCandles.add(newCandle);
        }
        if (thisIndex < thisSize) {
            IntStream.range(thisIndex, thisSize)
                    .mapToObj(i -> new Candle<>(times.get(i), prices.get(i)))
                    .forEach(resultCandles::add);
        } else if (srcIndex < srcSize) {
            IntStream.range(srcIndex, srcSize)
                    .mapToObj(i -> new Candle<>(newTimes.get(i), newPrices.get(i)))
                    .forEach(resultCandles::add);
        }
        return new TimeSeries<>(resultCandles);
    }

    public TimeSeries<V> extendLeft (TimeSeries<V> src){
        return src.extendRight(this);
    }

    public TimeSeries<V> extendRight (TimeSeries<V> src){
        System.out.println("TSeries::extendRight");
        System.out.println("TSeries::extendRight bug start");
        if (src instanceof EmptyTimeSeries) {
            return this;
        }
        assert src.from().isAfter(this.until());
        System.out.println("TSeries::extendRight bug end");
        if (!super.original) {
            this.times =  new ArrayList<>(this.times);
        }
        int size = this.size();
        this.times.addAll(src.times);
        TimeSeries<V> res = new TimeSeries<>(
                super.extendRight(src),
                this.times
        );
        this.times = this.times.subList(0, size);
        System.out.println("TSeries::extendRight end");
        return res;
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

    private int pointsBefore(ZonedDateTime anchor) {
        // number of datapoints strictly before anchor
        int anchorIndex = Collections.binarySearch(this.times, anchor);
        if (anchorIndex < 0) {
            return -(anchorIndex + 1);
        } else {
            return anchorIndex;
        }
    }

    public int pointsNotAfter(ZonedDateTime anchor) {
        // number of datapoints at or before anchor
        int anchorIndex = Collections.binarySearch(this.times, anchor);
        if (anchorIndex < 0) {
            return -(anchorIndex + 1);
        } else {
            return anchorIndex+1;
        }
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
        System.out.println("TimeSeries::getLast");
        System.out.println(this.size());
        return this.get(this.size()-1);
    }

    boolean isEmpty() {
        return this.data.isEmpty();
    }

    public static class EmptyTimeSeries <V extends Number> extends TimeSeries<V> {
        public EmptyTimeSeries() {
            super(List.of());
        }

        @Override
        public TimeSeries<V> slice(int from, int to) {
            return new EmptyTimeSeries<>();
        }

        @Override
        public TimeSeries<V> merge(TimeSeries<V> src) {
            return src;
        }

        @Override
        public TimeSeries<V> extendRight(TimeSeries<V> src) {
            return src;
        }

        @Override
        public TimeSeries<V> extendLeft(TimeSeries<V> src) {
            return src;
        }

        @Override
        boolean isEmpty() {
            return true;
        }
    }

}
