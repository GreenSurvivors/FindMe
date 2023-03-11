package de.greensurvivors.greenfinder.listener;

import org.bukkit.event.Listener;

public class SignListener  implements Listener {
    private static SignListener instance;

    public static SignListener inst() {
        if (instance == null) {
            instance = new SignListener();
        }
        return instance;
    }
}
