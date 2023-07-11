package de.greensurvivors.findme.dataObjects;

import de.greensurvivors.findme.GreenLogger;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Level;

public class Reflection {

    public static @Nullable Object getHandle(Object craftObject){
        Method handleMethod;
        try {
            GreenLogger.log(Level.INFO, "trying getting handle");
            handleMethod = craftObject.getClass().getMethod("getHandle");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }

        try {
            GreenLogger.log(Level.INFO, "trying invoke");
            return handleMethod.invoke(craftObject);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return null;
    }
}
