package engine.Serialisation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Optional;

public class LocalObject <T extends Serializable> {

    Path path;
    public LocalObject (Path root, String... tags) {
        for (String tag : tags) {
            root = root.resolve(tag);
        }
        this.path = root;
    }
    private void stage() {
        File f = this.path.toFile();
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            try {
                f.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    public Optional<T> get(){
        this.stage();
        try {
            return Optional.ofNullable((T) new ObjectInputStream(new FileInputStream(this.path.toFile())).readObject());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public void put(T item) {
        this.stage();
        try {
            new ObjectOutputStream(new FileOutputStream(this.path.toFile())).writeObject(item);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public void delete() {
        this.path.toFile().delete();
    }

    public static class Encrypted <T extends Serializable> extends LocalObject<T> {
        public Encrypted (Path root, String... tags) {
           super(root, tags);
        }

        @Override
        public Optional<T> get() {
            // TODO: decrypt
            return super.get();
        }

        @Override
        public void put(T item) {
            // TODO: encrypt
            super.put(item);
        }
    }


}
