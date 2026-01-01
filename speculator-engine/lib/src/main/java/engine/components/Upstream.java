package engine.components;

import engine.PriceData.State;

import java.util.concurrent.CompletableFuture;

public abstract class Upstream {

    public abstract <V extends Number> State<V> update ();

    public <V extends Number> CompletableFuture<? extends State<V>> updateAsync () {
        return CompletableFuture.supplyAsync(this::update);
    }


}
