package engine.Serialisation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Menu <T extends StateMachine<T>> implements Serializable {
    ArrayList<SavedStateMachine<T>> items;
    ArrayList<String> labels;
    LinkedHashSet<Integer> selected;
    int limit;

    public Menu(List<T> items, int limit) {
        this.items = new ArrayList<>();
        this.labels = new ArrayList<>();
        items.stream()
                .peek(item -> this.labels.add(item.toString()))
                .map(SavedStateMachine::new)
                .forEach(this.items::add);
        this.selected = new LinkedHashSet<>();
        this.limit = limit;
    }
    public Menu(List<T> items) {
        this(items, -1);
    }

    public int size() {
        return this.items.size();
    }

    public LinkedHashSet<Integer> getSelectedIndices() {
        return new LinkedHashSet<>(selected);
    }
    public List<T> getSelection() {
//        System.out.println("get selection: ");
        return selected.stream().map(items::get).map(SavedStateMachine::get).collect(Collectors.toList());
    }
    public List<String> getLabels() {
        return labels;
    }
    public void select(int i) {
        if (limit > -1 && this.selected.size() >= limit) {
            this.selected = new LinkedHashSet<>(new ArrayList<>(selected).subList(1, selected.size()));
        }
        selected.add(i);
    }
    public void unselect(int i) {
        selected.remove(i);
    }
    public void selectAll(Collection<Integer> indices) {
        indices.forEach(this::select);
    }
    public void unselectAll() {
        this.selected = new LinkedHashSet<>();
    }

}
