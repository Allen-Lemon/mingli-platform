package com.mingli.common;

import java.util.HashMap;
import java.util.Map;

/** 统一响应体 */
public class R extends HashMap<String, Object> {

    private static final long serialVersionUID = 1L;

    public R() {
        put("code", 0);
        put("msg", "ok");
    }

    public static R ok() {
        return new R();
    }

    public static R ok(Object data) {
        R r = new R();
        r.put("data", data);
        return r;
    }

    public static R ok(Map<String, Object> extra) {
        R r = new R();
        r.putAll(extra);
        return r;
    }

    public static R error(String msg) {
        R r = new R();
        r.put("code", 500);
        r.put("msg", msg);
        return r;
    }

    public static R error(int code, String msg) {
        R r = new R();
        r.put("code", code);
        r.put("msg", msg);
        return r;
    }

    public R put(String key, Object value) {
        super.put(key, value);
        return this;
    }
}
