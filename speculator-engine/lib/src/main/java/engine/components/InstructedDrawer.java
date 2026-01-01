package engine.components;

import java.util.List;

public abstract class InstructedDrawer {

    public abstract void draw(List<DrawInstruction.Point> points, DrawInstruction.Color color, DrawInstruction.Style style, String label);

    public abstract void undraw();
}
