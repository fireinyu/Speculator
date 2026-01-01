package engine.components;

import engine.PriceData.Position;
import engine.PriceData.State;

public abstract class Agent <T extends Number> {

    public abstract Position<T> act (State s);

}
