package org.eclipse.digitaltwin.basyx.databridge.opcua.configuration.factory;

import com.google.gson.*;
import org.eclipse.digitaltwin.basyx.databridge.opcua.configuration.OpcuaConsumerConfiguration;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class OpcuaConsumerConfigurationDeserializer implements JsonDeserializer<OpcuaConsumerConfiguration> {
  
    @Override
    public OpcuaConsumerConfiguration deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        Map<String, String> ctx = new HashMap<>();

        if (object.has("requestedPublishingInterval"))
            ctx.put("requestedPublishingInterval", object.get("requestedPublishingInterval").getAsString());

        if (object.has("parameters")) {
            object.get("parameters").getAsJsonObject()
                    .entrySet()
                    .parallelStream()
                    .filter(o -> !o.getValue().isJsonNull() && !ctx.containsKey(o.getValue().getAsString()))
                    .forEach(o -> ctx.put(o.getKey(), o.getValue().getAsString()));
        }

        return new OpcuaConsumerConfiguration(
                object.get("uniqueId").getAsString(),
                object.get("serverUrl").getAsString(),
                object.get("serverPort").getAsInt(),
                object.get("pathToService").getAsString(),
                object.get("nodeInformation").getAsString(),
                (!object.has("username") || object.get("username").isJsonNull()) ? null :
                        object.get("username").getAsString(),
                (!object.has("password") || object.get("password").isJsonNull()) ? null :
                        object.get("password").getAsString(),
                ctx
        );
    }
}
