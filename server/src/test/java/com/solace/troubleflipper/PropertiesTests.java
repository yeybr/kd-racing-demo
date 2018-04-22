
package com.solace.troubleflipper;

import com.solace.troubleflipper.properties.TournamentProperties;
import org.junit.Test;

public class PropertiesTests {
    @Test
    public void testTeamNames() {
        TournamentProperties teamProp = new TournamentProperties();

        teamProp.getNewTeamName();
    }
}

