
package com.solace.troubleflipper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.solace.troubleflipper.messages.AddUserMessage;
import com.solace.troubleflipper.messages.SwapPiecesMessage;
import com.solace.troubleflipper.messages.TournamentMessage;
import com.solace.troubleflipper.messages.UpdatePuzzleMessage;
import com.solace.troubleflipper.model.Game;
import com.solace.troubleflipper.model.PuzzlePiece;
import com.solace.troubleflipper.model.properties;
import com.solacesystems.jcsmp.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class PropertiesTests {
    public static void main() {
        TournamentProperties teamProp = new TournamentProperties();

        teamProp.getNewTeamName();
    }
}

