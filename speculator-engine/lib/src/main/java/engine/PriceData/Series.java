package engine.PriceData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.xml.crypto.Data;

public class Series <T extends Number> {

    List<T> data;
    boolean original = true; // whether ok to shallow copy
    int excess = 0; // number of unreachable elements
    int loadRatio = 1; // maximum number of unreachable elements as a fraction of size, in a slice

    public Series (List<? extends Datapoint<? extends T>> datapoints) {
        this.data = new ArrayList<>(datapoints.size());
        for (Datapoint<? extends T> dp : datapoints) {
            this.data.add(dp.get());
        }
    }

    public Series (Series<T> src) {
        this.data = src.data;
        this.loadRatio = src.loadRatio;
        this.excess = src.excess;
        this.original = src.original;
    }

    public List<T> get () {
        return this.data;
    }

    public Datapoint<T> get(int index) {
        return new Datapoint<>(this.data.get(index));
    }

    public Series<T> slice(int from, int to) {
        Series<T> res = new Series<>(List.of());
        res.data = this.data.subList(from, to);
        res.excess = this.excess + this.size() - (to-from);
        if (res.excess/(double)(to-from) > loadRatio) {
            res.data = new ArrayList<>(res.data);
            res.excess = 0;
            res.original = true;
        } else {
            res.original = false;
        }
        res.loadRatio = this.loadRatio;
        return res;
    }

    public int size () {
        return this.data.size();
    }

    public Series<T> extendLeft (Series<T> src) {
        return src.extendRight(this);
    }

    public Series<T> extendRight (Series<T> src) {
        int size = this.size();
        if (!this.original) {
            this.data = new ArrayList<>(this.data);
            this.excess = 0;
        }
        this.data.addAll(src.data);
        Series<T> combined = new Series<>(this);
        this.data = this.data.subList(0, size);
        this.excess += src.size();
        this.original = false;
        return combined;
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
