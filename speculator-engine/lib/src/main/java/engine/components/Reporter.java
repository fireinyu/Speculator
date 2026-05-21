package engine.components;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

import engine.PriceData.Position;

public abstract class Reporter {

    public abstract void report(Executor.ExecutionResult result, String agent, ZonedDateTime at);

    public abstract void report(List<Simulator.SimResult> result);

}
