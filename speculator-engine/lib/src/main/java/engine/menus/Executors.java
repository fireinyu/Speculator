package engine.menus;

import java.util.List;

import engine.Serialisation.Menu;
import engine.components.Executor;

public class Executors {
    public static List<Executor> list = List.of(
            /// CONFIG
    );
    public static Menu<Executor> menu = new Menu<>(list, 1);
}
