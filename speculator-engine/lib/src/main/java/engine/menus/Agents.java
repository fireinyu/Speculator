package engine.menus;

import java.util.List;

import engine.PriceData.Upstream;
import engine.Serialisation.EditMenu;
import engine.Serialisation.UserStateMachine;
import engine.agents.SimpleExpect;
import engine.components.Agent;

public class Agents {
    public static List<UserStateMachine.UserStateLoader<Agent>> list = List.of(
            /// CONFIG
            new SimpleExpect.SELoader()
    );
    public static EditMenu<Agent> menu = new EditMenu<>(list);
}
