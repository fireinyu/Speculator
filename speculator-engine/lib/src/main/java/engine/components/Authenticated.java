package engine.components;

import java.util.List;
import java.util.Map;

import engine.control.AuthManager;

public interface Authenticated {
    List<String> getFields();
    void authenticate(Map<String, String> credentials);
}
