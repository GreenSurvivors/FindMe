package de.greensurvivors.findme.dataObjects;

import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Reflection {

    public static @Nullable Object getHandle(Object craftObject){
        Method handleMethod;
        try {
            handleMethod = craftObject.getClass().getMethod("getHandle");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }

        try {
            return handleMethod.invoke(craftObject);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }
}
