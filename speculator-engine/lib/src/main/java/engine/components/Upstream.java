package engine.components;

import engine.PriceData.State;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.concurrent.CompletableFuture;

public abstract class Upstream {

    public abstract <V extends Number> State<V> update (Duration interval, int leftDependency);
    public abstract <V extends Number> State<V> snapshot (ZonedDateTime at, Duration interval, int leftDependency);
    public abstract <V extends Number> State<V> verify (ZonedDateTime from, Duration interval, int rightDependency);
//    public <V extends Number> CompletableFuture<? extends State<V>> updateAsync () {
//        return CompletableFuture.supplyAsync(this::update);
//    }


}
