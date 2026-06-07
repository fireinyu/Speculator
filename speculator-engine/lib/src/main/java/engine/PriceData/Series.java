package engine.PriceData;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class Series {

    float[] data;

    public Series (List<? extends Datapoint> datapoints) {
        this.data = new float[datapoints.size()];
        int index = 0;
        for (Datapoint dp : datapoints) {
            this.data[index++] = dp.get();
        }
    }

    public Series (float[] data) {
        this.data = data;
    }

    public Series (Series src) {
        this.data = src.data;
    }

    public float[] get () {
        return this.data;
    }

    public Datapoint get(int index) {
        return new Datapoint(this.data[index]);
    }

    public Series slice(int from, int to) {
        Series res = new Series(Arrays.copyOfRange(this.data, from, to));
        return res;
    }

    public int size () {
        return this.data.length;
    }

    public Series extendLeft (Series src) {
        return src.extendRight(this);
    }

    public Series extendRight (Series src) {
        float[] combined = Arrays.copyOf(this.data, this.size() + src.size());
        System.arraycopy(src.data, 0, combined, this.size(), src.size());
        Series result = new Series(combined);
        return result;
    }

    public Series map(Function<Float, Float> mapper) {
        float[] mapped = new float[this.data.length];
        for (int i = 0; i < this.data.length; i++) {
            mapped[i] = mapper.apply(this.data[i]);
        }
        return new Series(mapped);
    }

    public int getMinIndex() {
        if (this.data.length == 0) {
            return -1;
        }
        int minIndex = 0;
        for (int i = 1; i < this.data.length; i++) {
            if (this.data[i] < this.data[minIndex]) {
                minIndex = i;
            }
        }
        return minIndex;
    }

    public int getMaxIndex() {
        if (this.data.length == 0) {
            return -1;
        }
        int maxIndex = 0;
        for (int i = 1; i < this.data.length; i++) {
            if (this.data[i] > this.data[maxIndex]) {
                maxIndex = i;
            }
        }
        return maxIndex;
    }
}
