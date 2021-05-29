package com.wheatek.reflect;

import java.io.InputStream;
import java.lang.reflect.Method;

public class Streams {

    public static byte[] readFully(InputStream inputStream) {
        try {
            Class cls = Class.forName("libcore.io.Streams");
            Method m = cls.getMethod("readFully", InputStream.class);
            return (byte[]) m.invoke(null, inputStream);
        } catch (Throwable e) {
            e.printStackTrace();
            throw new NullPointerException(e.getMessage());
        }
    }
}
