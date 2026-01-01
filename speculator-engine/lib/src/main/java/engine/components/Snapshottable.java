package engine.components;

import engine.PriceData.State;

import java.time.ZonedDateTime;

public interface Snapshottable{

    <V extends Number> State<V> snapshot (ZonedDateTime at);
    <V extends Number> State<V> verify (ZonedDateTime from);


    //TODO: finish simulator class

}
