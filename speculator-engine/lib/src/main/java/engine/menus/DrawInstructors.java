package engine.menus;

import java.util.List;

import engine.Serialisation.Menu;
import engine.components.DrawInstructor;
import engine.components.Ticker;
import engine.drawInstructors.BoundPlotter;
import engine.drawInstructors.LinePlotter;

public class DrawInstructors {
    public static List<DrawInstructor> list = List.of(
            /// CONFIG
            new LinePlotter(0),
            new BoundPlotter(1)
    );
    public static Menu<DrawInstructor> menu = new Menu<>(list);

}
