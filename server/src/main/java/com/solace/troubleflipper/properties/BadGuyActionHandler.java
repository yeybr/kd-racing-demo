package com.solace.troubleflipper.properties;

import com.solace.troubleflipper.model.Bowser;
import com.solace.troubleflipper.model.Goomba;

public interface BadGuyActionHandler {

    void troubleFlipper(Bowser bowser);

    void greenShell(Goomba goomba);
}
