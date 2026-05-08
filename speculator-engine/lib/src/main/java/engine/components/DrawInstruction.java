package engine.components;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import ai.djl.training.tracker.WarmUpTracker;
import engine.Util;

public class DrawInstruction {
    public static enum Color {
        RED,
        GREEN,
        BLUE,
        YELLOW,
        PINK,
        ANY,
        NONE // do not draw
    }

    public static enum Style {
        SOLID,
        DASHED,
        DOTTED,
        ANY,
        NONE // do not draw
    }

    public static class Point {
        private float x;
        private float y;
        public Point(float x, float y) {
            this.x = x;
            this.y = y;
        }
        public float getX() {
            return x;
        }
        public float getY() {
            return y;
        }

        @Override
        public String toString() {
//            return "p";
            return String.format("(%.4g, %.4g)", x, y);
        }
    }

    public static class DrawMapping {
        private Map<Ticker, Map<ModelPredictor, Util.Pair<Color, Style>>> pMap;
        private Map<Ticker, Util.Pair<Color, Style>> fMap;
        private Map<Ticker, Util.Pair<Color, Style>> tMap;

        public DrawMapping(
                Map<Ticker, Util.Pair<Color, Style>> fMap,
                Map<Ticker, Map<ModelPredictor, Util.Pair<Color, Style>>> pMap,
                Map<Ticker, Util.Pair<Color, Style>> tMap
        ) {
            this.fMap = fMap;
            this.pMap = pMap;
            this.tMap = tMap;
        }
        public Color color(Ticker ticker) {
            return this.fMap.get(ticker).first;
        }
        public Style style(Ticker ticker) {
            return this.fMap.get(ticker).second;
        }
        public Color tColor(Ticker ticker) {
            return this.tMap.get(ticker).first;
        }
        public Style tStyle(Ticker ticker) {
            return this.fMap.get(ticker).second;
        }
        public Color color(Ticker ticker, ModelPredictor model) {
            return this.pMap.get(ticker).get(model).first;
        }
        public Style style(Ticker ticker, ModelPredictor model) {
            return this.pMap.get(ticker).get(model).second;
        }
        public Collection<Ticker> tickers() {
            return this.fMap.keySet();
        }
        public Collection<ModelPredictor> models() {
            return this.pMap.getOrDefault(pMap.keySet().stream().findAny().get(), Map.of()).keySet();
        }
    }

    public static Util.Cycle<Color> colors =  new Util.Cycle<>(List.of(
            Color.RED,
            Color.GREEN,
            Color.BLUE,
            Color.YELLOW,
            Color.PINK
    ));
    public static Util.Cycle<Style> styles = new Util.Cycle<>(List.of(
            Style.SOLID,
            Style.DOTTED,
            Style.DASHED
    ));

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

    public void drawBy(InstructedDrawer drawer) {
        if (this.points.isEmpty()) {
            return;
        }
        drawer.draw(this.points, this.color, this.style, this.label);
    }

}
