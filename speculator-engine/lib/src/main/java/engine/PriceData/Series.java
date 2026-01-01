package engine.PriceData;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.crypto.Data;

public class Series <T extends Number> {

    private List<T> data;

    public Series (Datapoint<? extends T>[] datapoints) {
        this.data = new ArrayList<>(datapoints.length);
        for (Datapoint<? extends T> dp : datapoints) {
            this.data.add(dp.get());
        }
    }

    public Series (List<? extends Datapoint<? extends T>> datapoints) {
        this.data = new ArrayList<>(datapoints.size());
        for (Datapoint<? extends T> dp : datapoints) {
            this.data.add(dp.get());
        }
    }

    public Series (Series<T> src) {
        this.data = List.copyOf(src.data);
    }

    public List<T> get () {
        return this.data;
    }

    public Datapoint<T> get(int index) {
        return new Datapoint<>(this.data.get(index));
    }

    public int size () {
        return this.data.size();
    }

    public void extendLeft (Series<? extends T> src) {
        List<T> combined = List.copyOf(src.data);
        combined.addAll(this.data);
        this.data = combined;
    }

    public void extendRight (Series<? extends T> src) {
        this.data.addAll(src.data);
    }

    public <R extends Number> Series<R> map(Function<? super T, ? extends R> mapper) {
        return new Series<>((this.data.stream()
                .map(mapper)
                .map(Datapoint::new)
                .collect(Collectors.toList()))
        );
    }

    public int getMinIndex() {
        return IntStream.range(0, this.size())
                .mapToObj(Integer::valueOf)
                .min(Comparator.comparing(i -> this.data.get(i).doubleValue())).orElse(-1);
    }

    public int getMaxIndex() {
        return IntStream.range(0, this.size())
                .mapToObj(Integer::valueOf)
                .max(Comparator.comparing(i -> this.data.get(i).doubleValue())).orElse(-1);
    }
}
