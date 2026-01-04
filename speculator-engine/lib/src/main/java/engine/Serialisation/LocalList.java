package engine.Serialisation;


import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class LocalList <T extends Serializable> {
    private static String metaTag = "_meta";
    private static String contentTag = "items";
    Path path;
    private List<LocalObject<T>> localObjects;
    private LocalObject<ArrayList<Integer>> indices;
    public LocalList (Path root, String... tags) {
        for (String tag : tags) {
            root = root.resolve(tag);
        }
        this.path = root;
        this.stage();
        this.indices = new LocalObject<>(this.path, LocalList.metaTag);
        this.localObjects = this.indices.get().orElse(new ArrayList<>()).stream()
                .map(idx -> new LocalObject<T>(this.path, LocalList.contentTag, String.valueOf(idx)))
                .collect(Collectors.toList());
    }

    private void stage() {
        File f = this.path.toFile();
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    public int size() {
        return this.localObjects.size();
    }

    public List<T> get() {
        return this.localObjects.stream()
                .map(LocalObject::get)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public T get(int index) {
        return this.localObjects.get(index).get().get();
    }

    public T set(int index, T element) {
        T prev = this.get(index);
        this.localObjects.get(index).put(element);
        return prev;
    }

    public void add(T element) {
        ArrayList<Integer> inds = indices.get().orElse(new ArrayList<>());
        int nextIndex;
        if (inds.size() == 0) {
            nextIndex = 0;
        } else {
            nextIndex = inds.get(inds.size()-1) + 1;
        }
        inds.add(nextIndex);
        this.indices.put(inds);
        String tag = String.valueOf(nextIndex);
        LocalObject<T> localObject = new LocalObject<>(this.path, LocalList.contentTag, tag);
        localObject.put(element);
        this.localObjects.add(localObject);
    }

    public void remove(int index) {
        ArrayList<Integer> inds = indices.get().orElse(new ArrayList<>());
        this.localObjects.remove(index).delete();
        inds.remove(index);
        indices.put(inds);
    }

    public void remove (List<Integer> indices) {
        indices.stream()
                .sorted(Comparator.reverseOrder())
                .mapToInt(x -> x)
                .forEach(this::remove);
    }

    public void remove(Object o) {
        List<Integer> indices = IntStream.range(0, this.size())
                .filter(i -> this.localObjects.get(i).get().get().equals(o))
                .boxed()
                .collect(Collectors.toList());
        this.remove(indices);
    }

}
