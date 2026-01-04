package engine.Serialisation;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;

import java.util.Map;

public interface StateMachine<R> {
    /*
        StateMachine obj;
        \ASSERT obj.base().load(obj.save()) \EQUALS obj

     */
    StateLoader<? extends StateMachine<R>> getLoader();
    /*identity state
     */
    Map<String, String> save();

}
