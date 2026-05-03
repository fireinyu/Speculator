package engine.components;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import ai.djl.training.tracker.WarmUpTracker;
import engine.Util;

public abstract class InstructedDrawer implements Serializable {

    public abstract void draw(List<DrawInstruction.Point> points, DrawInstruction.Color color, DrawInstruction.Style style, String label);
    public abstract void legend(DrawInstruction.DrawMapping mapping);
    public abstract void undraw();

}
