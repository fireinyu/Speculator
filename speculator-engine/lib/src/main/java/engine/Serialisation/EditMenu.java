package engine.Serialisation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class EditMenu <T extends UserStateMachine<T>> extends Menu<T> {

    private List<UserStateMachine.UserStateLoader<T>> loaders;
    private int selectedLoader;

    public EditMenu(List<UserStateMachine.UserStateLoader<T>> loaders, int limit) {
        super(List.of(), limit);
        this.loaders = loaders;
        this.selectedLoader = 0;
    }
    public EditMenu(List<UserStateMachine.UserStateLoader<T>> loaders) {
        this(loaders, -1);
    }



    public void removeSelected() {
        ArrayList<SavedStateMachine<T>> newItems = new ArrayList<>();
        ArrayList<String> newLabels = new ArrayList<>();
        for (int i = 0; i<items.size(); i++) {
            if (!selected.contains(i)) {
                newItems.add(items.get(i));
                newLabels.add(labels.get(i));
            }
        }
        this.items = newItems;
        this.labels = newLabels;
        this.selected = new LinkedHashSet<>();
        this.seenBy = new HashSet<>();
    }
    public void add(Map<String, String> settings) {
        T item = loaders.get(selectedLoader).load(settings);
        this.items.add(new SavedStateMachine<>(item));
        this.labels.add(item.toString());
    }
    public String selectedLoader() {
        return this.loaders.get(selectedLoader).toString();
    }
    public Collection<String> getOptions() {
        return this.loaders.get(selectedLoader).getOptions();
    }
}
