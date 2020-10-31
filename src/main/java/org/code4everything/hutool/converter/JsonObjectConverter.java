package org.code4everything.hutool.converter;

import com.alibaba.fastjson.JSONObject;
import org.code4everything.hutool.JsonConverter;

/**
 * @author pantao
 * @since 2020/10/31
 */
public class JsonObjectConverter extends JsonConverter<JSONObject> {

    @Override
    public Class<JSONObject> jsonType() {
        return JSONObject.class;
    }
}
