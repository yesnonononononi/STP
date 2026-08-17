package com.summit.stp.common.codec.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.summit.stp.common.codec.IdCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PublicIdSerializer extends JsonSerializer<Long> {

    private static IdCodec idCodec;

    @Autowired
    public void setIdCodec(IdCodec idCodec) {
        PublicIdSerializer.idCodec = idCodec;
    }

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        if (idCodec != null) {
            gen.writeString(idCodec.encode(value));
        } else {
            gen.writeNumber(value);
        }
    }
}
