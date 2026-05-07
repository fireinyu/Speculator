package engine.Serialisation;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import engine.control.App;

public class PresetMenu extends EditMenu<Preset> {
    private App app;
    @Override
    public void select(int i) {
        super.select(i);
        this.getSelection().get(0).apply(app);
    }

    @Override
    public List<String> getOptions() {
        return List.of("name");
    }

    @Override
    public void add(Map<String, String> settings) {
        settings = (new Preset(settings.get("name"), app).save());
        super.add(settings);
    }

    public PresetMenu(App app) {
        super(List.of(new Preset.PresetLoader()), 1);
        super.selectLoader(0);
        this.app = app;
    }
}
