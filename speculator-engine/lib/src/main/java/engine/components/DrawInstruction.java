package engine.components;

import java.util.List;

public class DrawInstruction {
    public static enum Color {
        RED,
        BLUE,
        GREEN
    }

    public static enum Style {
        SOLID,
        DASHED,
        DOTTED,
        NOLINE
    }

    public static class Point {
        private Number x;
        private Number y;
        public Point(Number x, Number y) {
            this.x = x;
            this.y = y;
        }
        public Number getX() {
            return x;
        }
        public Number getY() {
            return y;
        }
    }

    private List<Point> points;
    private Color color;
    private Style style;
    private String label;
    public DrawInstruction(List<Point> points, Color color, Style style, String label) {
        this.points = points;
        this.color = color;
        this.style = style;
        this.label = label;
    }

    protected void drawBy(InstructedDrawer drawer) {
        if (this.points.isEmpty()) {
            return;
        }
        drawer.draw(this.points, this.color, this.style, this.label);
    }

}
