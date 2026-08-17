package com.summit.stp.common.codec.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.summit.stp.common.codec.IdCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PublicIdDeserializer extends JsonDeserializer<Long> {

    private static IdCodec idCodec;

    @Autowired
    public void setIdCodec(IdCodec idCodec) {
        PublicIdDeserializer.idCodec = idCodec;
    }

    @Override
    public Long deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String value = p.getValueAsString();
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        if (idCodec != null) {
            return idCodec.decode(value);
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
