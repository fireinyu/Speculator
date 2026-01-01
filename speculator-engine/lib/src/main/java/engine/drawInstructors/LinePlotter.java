package engine.drawInstructors;

import engine.components.DrawInstruction;
import engine.components.DrawInstructor;
import engine.components.InstructedDrawer;
import engine.PriceData.Ticker;
import engine.PriceData.TimeSeries;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LinePlotter <V extends Number> extends DrawInstructor<V> {
    private static List<DrawInstruction.Color> lineColors = List.of(
            DrawInstruction.Color.RED,
            DrawInstruction.Color.BLUE,
            DrawInstruction.Color.GREEN
    );
    private ZonedDateTime anchor;
    public LinePlotter (InstructedDrawer drawer) {
        this(drawer, ZonedDateTime.now());
    }

    public LinePlotter (InstructedDrawer drawer, ZonedDateTime anchor) {
        super(drawer);
        this.anchor = anchor;
    }
    @Override
    protected DrawInstruction singleDraw(TimeSeries<V> input, String label) {
        return new DrawInstruction(
                input.extract((dt, px) -> new DrawInstruction.Point(Duration.between(this.anchor, dt).getSeconds(), px)),
                lineColors.get(0),
                DrawInstruction.Style.SOLID,
                ""
        );
    }

    @Override
    protected List<DrawInstruction> drawAll(List<? extends Ticker> tickers, List<? extends TimeSeries<V>> featuresLs, List<? extends TimeSeries<V>> predsLs, List<? extends TimeSeries<V>> targetsLs) {
        ZonedDateTime anchor = TimeSeries.getAnchor(featuresLs);
        ArrayList<DrawInstruction> instructions = new ArrayList<>();
        if (tickers.size() == 1) {
            double anchorPrice = featuresLs.get(0).priceAt(anchor).doubleValue();
            featuresLs.forEach(series -> {
                instructions.add(new DrawInstruction(
                        series.map(ms -> ms, px -> px.doubleValue()/anchorPrice)
                                .extract((dt, px) -> new DrawInstruction.Point(Duration.between(this.anchor, dt).getSeconds(), px)),
                        lineColors.get(0),
                        DrawInstruction.Style.SOLID,
                        "features"

                ));
            });
            Iterator<DrawInstruction.Color> colorsP = LinePlotter.lineColors.iterator();
            predsLs.forEach(series -> {
                instructions.add(new DrawInstruction(
                        series.map(ms -> ms, px -> px.doubleValue()/anchorPrice)
                                .extract((dt, px) -> new DrawInstruction.Point(Duration.between(this.anchor, dt).getSeconds(), px)),
                        colorsP.next(),
                        DrawInstruction.Style.DASHED,
                        "prediction"
                ));
            });

            Iterator<DrawInstruction.Color> colorsT = LinePlotter.lineColors.iterator();
            targetsLs.forEach(series -> {
                instructions.add(new DrawInstruction(
                        series.map(ms -> ms, px -> px.doubleValue()/anchorPrice)
                                .extract((dt, px) -> new DrawInstruction.Point(Duration.between(this.anchor, dt).getSeconds(), px)),
                        colorsT.next(),
                        DrawInstruction.Style.DOTTED,
                        "targets"

                ));
            });
        } else {
            for (int i = 0; i < tickers.size(); i++) {
                DrawInstruction.Color color = lineColors.get(i);
                TimeSeries<V> f = featuresLs.get(i);
                TimeSeries<V> p =  predsLs.get(i);
                double anchorPrice = f.priceAt(anchor).doubleValue();
                instructions.add(new DrawInstruction(
                        f.map(ms -> ms, px -> px.doubleValue()/anchorPrice)
                                .extract((dt, px) -> new DrawInstruction.Point(Duration.between(this.anchor, dt).getSeconds(), px)),
                        color,
                        DrawInstruction.Style.SOLID,
                        "features"

                ));
                instructions.add(new DrawInstruction(
                        p.map(ms -> ms, px -> px.doubleValue()/anchorPrice)
                                .extract((dt, px) -> new DrawInstruction.Point(Duration.between(this.anchor, dt).getSeconds(), px)),
                        color,
                        DrawInstruction.Style.DASHED,
                        "prediction"

                ));
                if (i < targetsLs.size()) {
                    TimeSeries<V> t =  targetsLs.get(i);
                    instructions.add(new DrawInstruction(
                            t.map(ms -> ms, px -> px.doubleValue()/anchorPrice)
                                    .extract((dt, px) -> new DrawInstruction.Point(Duration.between(this.anchor, dt).getSeconds(), px)),
                            color,
                            DrawInstruction.Style.DOTTED,
                            "targets"

                    ));
                }
            }
        }
        return instructions;
    }
}
