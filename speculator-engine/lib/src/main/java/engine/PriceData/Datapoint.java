package engine.PriceData;

public class Datapoint <T extends Number> {

    private T val;

    public Datapoint (T val) {
        this.val = val;
    }

    public T get () {
        return this.val;
    }

}
