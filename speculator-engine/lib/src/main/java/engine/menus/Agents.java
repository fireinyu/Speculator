package engine.menus;

import java.util.List;

import engine.PriceData.Upstream;
import engine.Serialisation.EditMenu;
import engine.Serialisation.UserStateMachine;
import engine.components.Agent;

public class Agents {
    public static List<UserStateMachine.UserStateLoader<Agent>> list = List.of(
            /// CONFIG
    );
    public static EditMenu<Agent> menu = new EditMenu<>(list);
}
