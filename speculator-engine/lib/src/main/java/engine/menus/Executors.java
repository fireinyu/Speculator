package engine.menus;

import java.util.List;

import engine.Serialisation.Menu;
import engine.components.Executor;
import engine.executors.DoNothing;

public class Executors {
    /// CONFIG
    public static Executor doNothing = new DoNothing(0);

    public static List<Executor> list = List.of(
            /// CONFIG
            doNothing
    );
    public static Menu<Executor> menu = new Menu<>(list);
}
