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

public class TimeSeries extends Series{

//    public static ZonedDateTime getAnchor(
//            List<? extends TimeSeries> seriesLs
//            ) {
//        return seriesLs.stream()
//                .map(TimeSeries::until)
//                .min(ZonedDateTime::compareTo)
//                .orElse(null);
//    }

    public static  TimeSeries empty() {
        return new EmptyTimeSeries();
    }
    private List<ZonedDateTime> times;

    public TimeSeries (List<ZonedDateTime> times, List<Float> prices) {
//        this(List.of());
//        Log.d("debug_merge", "" + times.size() + ", " + prices.size());
        this(IntStream.range(0, times.size()).mapToObj(i -> new Candle(times.get(i), prices.get(i))).collect(Collectors.toList()));


    }

    public <D extends Datapoint & Timed> TimeSeries (List<D> datapoints) {
        super(datapoints);
        this.times = new ArrayList<>();
        for (D dp : datapoints) {
            this.times.add(dp.getTime());
        }
    }

    public TimeSeries (TimeSeries src) {
        super(src);
        this.times = src.times;
    }

    private TimeSeries (Series src, List<ZonedDateTime> times) {
        super(src);
        this.times = times;
    }

    @Override
    public TimeSeries slice(int from, int to) {
        if (from >= to) {
            return new EmptyTimeSeries();
        }
        List<ZonedDateTime> slicedTimes = this.times.subList(from, to);
        if ((this.size()+super.excess)/(float)(from-to) > super.loadRatio) {
            slicedTimes = new ArrayList<>(slicedTimes);
        }
        Series res = super.slice(from, to);
        System.out.println("slice result: " + from + to + res.size() );
        return new TimeSeries(res, slicedTimes);
    }


    public TimeSeries merge (TimeSeries src) {
        if (src.size() == 0) {
            return new TimeSeries(this);
        }
        ArrayList<Candle> resultCandles = new ArrayList<>();
        List<ZonedDateTime> times = this.getTimes();
        List<Float> prices = this.get();
        List<ZonedDateTime> newTimes = src.getTimes();
        List<Float> newPrices = src.get();
        int thisIndex = 0;
        int srcIndex = 0;
        int thisSize = this.size();
        int srcSize = src.size();
        while (thisIndex < thisSize && srcIndex < srcSize) {
            ZonedDateTime thisTime = times.get(thisIndex);
            ZonedDateTime srcTime = newTimes.get(srcIndex);
            int compareResult = thisTime.compareTo(srcTime);
            Candle newCandle = null;
            if (compareResult < 0) {
                newCandle = new Candle(thisTime, prices.get(thisIndex));
                thisIndex++;
            } else if (compareResult == 0){
                newCandle = new Candle(srcTime, newPrices.get(srcIndex));
                thisIndex++;
                srcIndex++;

            } else {
                newCandle = new Candle(srcTime, newPrices.get(srcIndex));
                srcIndex++;
            }
            resultCandles.add(newCandle);
        }
        if (thisIndex < thisSize) {
            IntStream.range(thisIndex, thisSize)
                    .mapToObj(i -> new Candle(times.get(i), prices.get(i)))
                    .forEach(resultCandles::add);
        } else if (srcIndex < srcSize) {
            IntStream.range(srcIndex, srcSize)
                    .mapToObj(i -> new Candle(newTimes.get(i), newPrices.get(i)))
                    .forEach(resultCandles::add);
        }
        return new TimeSeries(resultCandles);
    }

    public TimeSeries extendLeft (TimeSeries src){
        return src.extendRight(this);
    }

    public TimeSeries extendRight (TimeSeries src){
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
        TimeSeries res = new TimeSeries(
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

    public TimeSeries map(Function<? super ZonedDateTime, ? extends ZonedDateTime> timeMapper, Function<Float, Float> priceMapper) {
        List<Float> prices = super.get();
        return new TimeSeries(IntStream.range(0, this.times.size()).boxed()
                .map(i -> new Candle(timeMapper.apply(this.times.get(i)), priceMapper.apply(prices.get(i))))
                .collect(Collectors.toList())
        );

    }

    public <R> List<R> extract (BiFunction<ZonedDateTime, Float, R> extractor) {

        List<Float> prices = super.get();
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

    public float priceAt (ZonedDateTime anchor) {
        int anchorIndex = Collections.binarySearch(this.times, anchor);
        float anchorPrice;
        if (anchorIndex >= 0) {
            anchorPrice = this.get().get(anchorIndex);
        } else {
            anchorIndex = -(anchorIndex + 1);
            if (anchorIndex == 0) {
                anchorPrice = this.get().get(0);
            } else if (anchorIndex == this.size()) {
                anchorPrice = this.get().get(this.size()-1);
            } else {
                float leftPrice = this.get().get(anchorIndex - 1);
                float rightPrice = this.get().get(anchorIndex);
                float leftMs = this.getTimes().get(anchorIndex - 1).toEpochSecond();
                float rightMs = this.getTimes().get(anchorIndex).toEpochSecond();
                float res = leftPrice + (rightPrice-leftPrice) * (anchor.toEpochSecond()-leftMs)/(rightMs-leftMs);
                anchorPrice = Util.convertNumber(res, this.get().get(0));
            }
        }
        return anchorPrice;
    }

    public Candle get(int index) {
        return new Candle( this.times.get(index),super.get(index));
    }

    public Candle getLast() {
        System.out.println("TimeSeries::getLast");
        System.out.println(this.size());
        return this.get(this.size()-1);
    }

    boolean isEmpty() {
        return this.data.isEmpty();
    }

    public static class EmptyTimeSeries  extends TimeSeries {
        public EmptyTimeSeries() {
            super(List.of());
        }

        @Override
        public TimeSeries slice(int from, int to) {
            return new EmptyTimeSeries();
        }

        @Override
        public TimeSeries merge(TimeSeries src) {
            return src;
        }

        @Override
        public TimeSeries extendRight(TimeSeries src) {
            return src;
        }

        @Override
        public TimeSeries extendLeft(TimeSeries src) {
            return src;
        }

        @Override
        boolean isEmpty() {
            return true;
        }
    }

}
