package com.fasterxml.jackson.datatype.joda.deser;

import com.fasterxml.jackson.core.*;
import com.fasterxml.jackson.databind.*;

import org.joda.time.*;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.util.TimeZone;

/**
 * Basic deserializer for {@link ReadableDateTime} and its subtypes.
 * Accepts JSON String and Number values and passes those to single-argument constructor.
 * Does not (yet?) support JSON object; support can be added if desired.
 */
public class DateTimeDeserializer
    extends JodaDeserializerBase<ReadableInstant>
{
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    public DateTimeDeserializer(Class<? extends ReadableInstant> cls) {
        super((Class<ReadableInstant>)cls);
    }

    @SuppressWarnings("unchecked")
    public static <T extends ReadableInstant> JsonDeserializer<T> forType(Class<T> cls)
    {
        return (JsonDeserializer<T>) new DateTimeDeserializer(cls);
    }

    @Override
    public ReadableDateTime deserialize(JsonParser jp, DeserializationContext ctxt)
        throws IOException
    {
        JsonToken t = jp.getCurrentToken();

        if (t == JsonToken.VALUE_NUMBER_INT) {
            TimeZone tz = ctxt.getTimeZone();
            DateTimeZone dtz = (tz == null) ? DateTimeZone.UTC : DateTimeZone.forTimeZone(tz);
            return new DateTime(jp.getLongValue(), dtz);
        }
        if (t == JsonToken.VALUE_STRING) {
            String str = jp.getText().trim();
            if (str.length() == 0) { // [JACKSON-360]
                return null;
            }
            if (ctxt.isEnabled(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE)) {
                TimeZone tz = ctxt.getTimeZone();
                DateTimeZone dtz = (tz == null) ? DateTimeZone.UTC : DateTimeZone.forTimeZone(tz);
                return ISODateTimeFormat.dateTimeParser().withZone(dtz).parseDateTime(str);
            }
            return ISODateTimeFormat.dateTimeParser().withOffsetParsed().parseDateTime(str);
        }
        throw ctxt.mappingException(handledType());
    }
}
