package engine.Serialisation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class LocalStateMap <T extends StateMachine<T>> {

    private static Path registry = Path.of(".registry");
    private static Path items = Path.of(".items");

    private Map<String, LocalStateMachine<T>> map;
    private Path root;
    private T base;
    public LocalStateMap(T template, Path root) {
        this.base = template.base();
        this.map = new HashMap<>();
        this.root = root;
        this.stage();
    }

    public Optional<T> get(String tag) {
        return map.get(tag).get();
    }

    public Set<String> keySet() {
        return map.keySet();
    }

    public void put(String tag, T item) {
        this.stage(tag);
        map.get(tag).put(item);
    }

    public void remove(String tag) {
        this.map.get(tag).delete();
        this.map.remove(tag);
        try {
            new ObjectOutputStream(new FileOutputStream(this.root.resolve(LocalStateMap.registry).toFile())).writeObject(new ArrayList<String>(this.map.keySet()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void stage() {
        File f = this.root.resolve(LocalStateMap.registry).toFile();
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            this.root.resolve(LocalStateMap.items).toFile().mkdirs();
            try {
                f.createNewFile();
                new ObjectOutputStream(new FileOutputStream(f)).writeObject(new ArrayList<String>());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            ((ArrayList<String>) new ObjectInputStream(new FileInputStream(f)).readObject())
                    .forEach(tag -> this.map.put(tag, new LocalStateMachine<>(this.base, this.root.resolve(LocalStateMap.items), tag)));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void stage(String tag) {
        if (!this.keySet().contains(tag)) {
            File f = this.root.resolve(LocalStateMap.items).resolve(tag).toFile();
            try {
                f.createNewFile();
                this.map.put(tag, new LocalStateMachine<>(this.base, this.root.resolve(LocalStateMap.items), tag));
                new ObjectOutputStream(new FileOutputStream(this.root.resolve(LocalStateMap.registry).toFile())).writeObject(new ArrayList<String>(this.map.keySet()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
