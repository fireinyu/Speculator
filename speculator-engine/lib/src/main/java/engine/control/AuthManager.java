package engine.control;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
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
    private LinkedHashMap<String, LinkedHashSet<String>> targetsByField = new LinkedHashMap<>();
    private transient LinkedHashMap<String, Authenticated> targets;
//    public AuthManager(List<Authenticated> targets) {
//        this.targets = new LinkedHashMap<>();
//        targets.forEach(target -> this.targets.put(target.toString(), target));
//        targets.forEach(target -> this.creds.put(target.toString(), new HashMap<>()));
//        targets.forEach(target -> target.getFields()
//                .forEach(field -> this.creds.get(target.toString()).put(field,"")));
//    }
    public AuthManager(List<?>... objects) {
        for (List<?> ls : objects) {
            List<Authenticated> targets =  ls.stream()
                .filter(x -> x instanceof Authenticated)
                .map(x -> (Authenticated)x)
                .collect(Collectors.toList());
            targets.forEach(target -> this.fields.put(target.toString(), new LinkedHashSet<>(target.getFields())));
            targets.forEach(target -> target.getFields()
                    .stream()
                    .peek(field -> {
                        if (!targetsByField.containsKey(field)) {
                            targetsByField.put(field, new LinkedHashSet<>());
                        }
                        targetsByField.get(field).add(target.toString());
                    })
                    .forEach(field -> this.creds.put(field,"")));

        }
        this.init(objects);
    }

    void init(Collection<?>... objects) {
        targets = new LinkedHashMap<>();
        Arrays.stream(objects)
                .flatMap(Collection::stream)
                .filter(x -> x instanceof Authenticated)
                .map(x -> (Authenticated)x)
                .peek(x -> x.authenticate(fields.get(x.toString()).stream().collect(Collectors.toMap(
                        field -> field,
                        field -> creds.get(field)
                ))))
                .forEach(x -> targets.put(x.toString(), x));
    }
    public void auth(Map<String, String> creds) {
        this.creds.putAll(creds);
        creds.keySet().stream()
                .map(targetsByField::get)
                .flatMap(Collection::stream)
                .distinct()
                .forEach(target -> {
                    // creds.forEach((k,v) -> System.out.println(k + "|-> " + v));
                    targets.get(target).authenticate(
                            fields.get(target).stream()
                                    //.peek(System.out::println)
                                    .collect(Collectors.toMap(
                                            field -> field,
                                            this.creds::get
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
        // this.creds.forEach((k, v) -> System.out.println(k + " |-> " + v));
        return new ArrayList<>(this.creds.keySet());
    }

    public boolean isFilled(String field) {
        // this.creds.keySet().forEach(System.out::println);
        return !this.creds.get(field).isBlank();
    }

/// need below as target is not authed after deserialised
//    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
//        // 1. Perform default restoration for non-transient fields
//        in.defaultReadObject();
//        this.fields.forEach((label,fields)-> {
//            System.out.println(label);
//            targets.keySet().forEach(System.out::println);
//            targets.get(label).authenticate(fields.stream().collect(Collectors.toMap(
//                    field -> field,
//                    field -> creds.get(field)
//            )));
//        });
//    }

}
