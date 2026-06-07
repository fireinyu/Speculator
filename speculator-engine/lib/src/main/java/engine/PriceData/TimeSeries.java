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

    public TimeSeries (List<ZonedDateTime> times, float[] prices) {
        this(IntStream.range(0, times.size()).mapToObj(i -> new Candle(times.get(i), prices[i])).collect(Collectors.toList()));
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
        List<ZonedDateTime> slicedTimes = new ArrayList<>(this.times.subList(from, to));
        Series res = super.slice(from, to);
        return new TimeSeries(res, slicedTimes);
    }


    public TimeSeries merge (TimeSeries src) {
        if (src.size() == 0) {
            return new TimeSeries(this);
        }
        ArrayList<Candle> resultCandles = new ArrayList<>();
        List<ZonedDateTime> times = this.getTimes();
        float[] prices = this.get();
        List<ZonedDateTime> newTimes = src.getTimes();
        float[] newPrices = src.get();
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
                newCandle = new Candle(thisTime, prices[thisIndex]);
                thisIndex++;
            } else if (compareResult == 0){
                newCandle = new Candle(srcTime, newPrices[srcIndex]);
                thisIndex++;
                srcIndex++;

            } else {
                newCandle = new Candle(srcTime, newPrices[srcIndex]);
                srcIndex++;
            }
            resultCandles.add(newCandle);
        }
        if (thisIndex < thisSize) {
            IntStream.range(thisIndex, thisSize)
                    .mapToObj(i -> new Candle(times.get(i), prices[i]))
                    .forEach(resultCandles::add);
        } else if (srcIndex < srcSize) {
            IntStream.range(srcIndex, srcSize)
                    .mapToObj(i -> new Candle(newTimes.get(i), newPrices[i]))
                    .forEach(resultCandles::add);
        }
        return new TimeSeries(resultCandles);
    }

    public TimeSeries extendLeft (TimeSeries src){
        return src.extendRight(this);
    }

    public TimeSeries extendRight (TimeSeries src){
        if (src instanceof EmptyTimeSeries) {
            return this;
        }
        assert src.from().isAfter(this.until());
        List<ZonedDateTime> combinedTimes = new ArrayList<>(this.times);
        combinedTimes.addAll(src.times);
        TimeSeries res = new TimeSeries(
                super.extendRight(src),
                combinedTimes
        );
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
        float[] prices = super.get();
        return new TimeSeries(IntStream.range(0, this.times.size()).boxed()
                .map(i -> new Candle(timeMapper.apply(this.times.get(i)), priceMapper.apply(prices[i])))
                .collect(Collectors.toList())
        );

    }

    public float[] extractFloats (BiFunction<ZonedDateTime, Float, Float> extractor) {
        float[] prices = super.get();
        float[] extracted = new float[this.times.size()];
        for (int i = 0; i < this.times.size(); i++) {
            extracted[i] = extractor.apply(this.times.get(i), prices[i]);
        }
        return extracted;
    }

    public <R> List<R> extract (BiFunction<ZonedDateTime, Float, R> extractor) {

        float[] prices = super.get();
        return IntStream.range(0, this.times.size()).boxed()
                .map(i -> extractor.apply(this.times.get(i), prices[i]))
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
        float[] prices = this.get();
        if (anchorIndex >= 0) {
            anchorPrice = prices[anchorIndex];
        } else {
            anchorIndex = -(anchorIndex + 1);
            if (anchorIndex == 0) {
                anchorPrice = prices[0];
            } else if (anchorIndex == this.size()) {
                anchorPrice = prices[this.size()-1];
            } else {
                float leftPrice = prices[anchorIndex - 1];
                float rightPrice = prices[anchorIndex];
                ZonedDateTime leftMs = this.getTimes().get(anchorIndex - 1);
                ZonedDateTime rightMs = this.getTimes().get(anchorIndex);
                double res = leftPrice + (rightPrice-leftPrice) * Duration.between(leftMs, anchor).toMillis()/Duration.between(leftMs, rightMs).toMillis();

                anchorPrice = Util.convertNumber(res, prices[0]);
            }
        }
        return anchorPrice;
    }

    public Candle get(int index) {
        return new Candle( this.times.get(index),super.get(index));
    }

    public Candle getLast() {
        // System.out.println("TimeSeries::getLast");
        // System.out.println(this.size());
        return this.get(this.size()-1);
    }

    boolean isEmpty() {
        return this.data.length == 0;
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
