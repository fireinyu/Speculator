package engine.control;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import engine.components.Authenticated;

public class AuthManager implements Serializable {
    private LinkedHashMap<String, String> creds = new LinkedHashMap<>();
    private LinkedHashMap<String, LinkedHashSet<String>> fields = new LinkedHashMap<>();
    private LinkedHashMap<String, LinkedHashSet<String>> targetsByField;
    private LinkedHashMap<String, Authenticated> targets;
//    public AuthManager(List<Authenticated> targets) {
//        this.targets = new LinkedHashMap<>();
//        targets.forEach(target -> this.targets.put(target.toString(), target));
//        targets.forEach(target -> this.creds.put(target.toString(), new HashMap<>()));
//        targets.forEach(target -> target.getFields()
//                .forEach(field -> this.creds.get(target.toString()).put(field,"")));
//    }
    public AuthManager(List<?>... objects) {
        this.targets = new LinkedHashMap<>();
        for (List<?> ls : objects) {
            List<Authenticated> targets =  ls.stream()
                .filter(x -> x instanceof Authenticated)
                .map(x -> (Authenticated)x)
                .collect(Collectors.toList());
            targets.forEach(target -> this.targets.put(target.toString(), target));
            targets.forEach(target -> this.fields.put(target.toString(), new LinkedHashSet<>(target.getFields())));
            targets.forEach(target -> target.getFields()
                    .stream()
                    .peek(field -> targetsByField.get(field).add(target.toString()))
                    .forEach(field -> this.creds.put(field,"")));

        }
    }
    public void auth(Map<String, String> creds) {
        this.creds.putAll(creds);
        creds.keySet().stream()
                .map(targetsByField::get)
                .flatMap(Collection::stream)
                .distinct()
                .forEach(target -> {
                    targets.get(target).authenticate(
                            fields.get(target).stream()
                                    .collect(Collectors.toMap(
                                            field -> field,
                                            creds::get
                                    ))
                    );
                });
    }

    public List<String> getTargets() {
        return this.targets.keySet().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }
    public List<String> getFields(String target) {
        return new ArrayList<>(this.fields.get(target));
    }
    public List<String> getFields() {
        return new ArrayList<>(this.fields.keySet());
    }

    public boolean isFilled(String field) {
        return !this.creds.get(field).isBlank();
    }

/// don't need below as target is already authed whenever creds have changed
//    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
//        // 1. Perform default restoration for non-transient fields
//        in.defaultReadObject();
//        this.targets.forEach((label,target)-> {
//            target.authenticate(this.creds.get(label));
//        });
//    }

}
