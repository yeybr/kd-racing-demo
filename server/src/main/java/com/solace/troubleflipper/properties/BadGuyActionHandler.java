package com.solace.troubleflipper.properties;

import com.solace.troubleflipper.model.Bowser;
import com.solace.troubleflipper.model.Goomba;
import com.solace.troubleflipper.model.Player;

public interface BadGuyActionHandler {

    void troubleFlipper(Player bowserPlayer);

    void greenShell(Player goombaPlayer);
}
