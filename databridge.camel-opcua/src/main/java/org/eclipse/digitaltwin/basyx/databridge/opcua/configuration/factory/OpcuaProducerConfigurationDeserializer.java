/*******************************************************************************
 * Copyright (C) 2024 the Eclipse BaSyx Authors
 * 
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * SPDX-License-Identifier: MIT
 ******************************************************************************/
package org.eclipse.digitaltwin.basyx.databridge.opcua.configuration.factory;

import com.google.gson.*;

import org.eclipse.digitaltwin.basyx.databridge.opcua.configuration.OpcuaProducerConfiguration;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of OpcUa Producer configuration deserializer
 *
 * @author zielstor
 *
 */
public class OpcuaProducerConfigurationDeserializer implements JsonDeserializer<OpcuaProducerConfiguration> {
    @Override
    public OpcuaProducerConfiguration deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
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

        return new OpcuaProducerConfiguration(
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
