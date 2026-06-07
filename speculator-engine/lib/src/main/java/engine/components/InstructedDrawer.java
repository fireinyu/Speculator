package engine.components;

import java.util.List;

import engine.PriceData.CostPosition;
import engine.PriceData.NAVPosition;
import engine.Util;

public abstract class InstructedDrawer{

    public abstract void draw(List<DrawInstruction.Point> points, DrawInstruction.Color color, DrawInstruction.Style style, String label);
    public abstract void undraw();
    public abstract void legend(DrawInstruction.DrawMapping mapping);
    public abstract void showPositions(List<Util.Pair<Ticker, NAVPosition>> positions);

}
