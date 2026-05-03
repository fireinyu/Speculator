package engine.control;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import engine.components.Authenticated;

public class AuthManager implements Serializable {
    private LinkedHashMap<String, HashMap<String, String>> creds = new LinkedHashMap<>();
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
            targets.forEach(target -> this.creds.put(target.toString(), new HashMap<>()));
            targets.forEach(target -> target.getFields()
                    .forEach(field -> this.creds.get(target.toString()).put(field,"")));

        }
    }
    public void auth(String target, String field, String cred) {
        this.creds.get(target).put(field, cred);
        this.targets.get(target).authenticate(this.creds.get(target));
    }

    public List<String> getTargets() {
        return this.targets.keySet().stream()
                .map(Object::toString)
                .collect(Collectors.toList());
    }
    public List<String> getFields(String target) {
        return new ArrayList<>(this.creds.get(target).keySet());
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        // 1. Perform default restoration for non-transient fields
        in.defaultReadObject();
        this.targets.forEach((label,target)-> {
            target.authenticate(this.creds.get(label));
        });
    }

}
