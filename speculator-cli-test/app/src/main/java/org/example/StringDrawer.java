package org.example;

import java.util.List;

import engine.components.InstructedDrawer;
import engine.components.DrawInstruction.Color;
import engine.components.DrawInstruction.DrawMapping;
import engine.components.DrawInstruction.Point;
import engine.components.DrawInstruction.Style;

public class StringDrawer extends InstructedDrawer{
    @Override
    public void undraw() {}
    @Override
    public void draw(List<Point> points, Color color, Style style, String label) {

        StringBuilder res = new StringBuilder();
        res.append(String.format("%s (%s %s):", label, color.name(), style.name()));
        points.forEach(pt -> res.append(String.format(" ,%.3g", pt.getY())));
        System.out.println(res.toString());
    }
    @Override
    public void legend(DrawMapping mapping) {
        
    }
}