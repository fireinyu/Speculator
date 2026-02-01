package engine.Instances;

import java.util.List;

import engine.components.DrawInstructor;
import engine.drawInstructors.BoundPlotter;
import engine.drawInstructors.LinePlotter;

public class DrawInstructors {
    public static List<DrawInstructor<Float, Float>> list = List.of(
            // CONFIG
            new LinePlotter<>(),
            new BoundPlotter<>()
    );
}
