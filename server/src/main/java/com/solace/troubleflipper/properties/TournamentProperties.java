package com.solace.troubleflipper.properties;

//import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.Random;
//import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class TournamentProperties {
    private int playersPerTeam;
    private Set teamNamesUsed = new HashSet();

    public int getPlayersPerTeam() {
        return playersPerTeam;
    }

    public void setPlayersPerTeam(int playersPerTeam) {
        this.playersPerTeam = playersPerTeam;
    }

    private String[] teamNamesPart_1 = {
        "Meandering", "Snippy", "Sliding", "Wavering", "Indecisive", "Messy",
        "Splattered", "Wide-eyed", "Burnt", "Toasted", "Spoiled", "Drooling",
        "Flipperdy", "Restless", "Doki Doki", "Left Over", "Sprinkled",
        "Tase Target", "Slushy", "Muddy", "Oiled", "Buggy"
        };

    private String[] teamNamesPart_2 = {
        "Peaches", "Gang", "Trouble Makers", "Flippers", "Toads", "Koopa Kids",
        "Toadettes", "Koopalings", "Piranhas", "Warts", "Waluigis",
        "Boom Booms", "Kongs", "Birdos", "Lumas", "Diddys", "Magikoopas"
        };

    public String getNewTeamName() {
        Random randomGen = new Random();
        int    fNameCount = teamNamesPart_1.length;
        int    lNameCount = teamNamesPart_2.length;

        String newName = "None";
        
        while ( newName.equals("None")) {
            int fNameIndex = randomGen.nextInt(fNameCount);
            String fName = teamNamesPart_1[fNameIndex];

            int lNameIndex = randomGen.nextInt(lNameCount);
            String lName = teamNamesPart_2[lNameIndex];

            String tempName = fName + " " + lName;

            // checking for presence of an item in an array, this way, requires Java 8
            if (! this.teamNamesUsed.contains(tempName)) {
                newName = tempName;
                boolean checkAdd = this.teamNamesUsed.add(newName);
                if (! checkAdd) {
                    return tempName + ", failed to add to list";
                }
            }
        }

        return newName;
    }

    //public String[] getTeamNames() {
    //    return teamNames;
    //}

    //public void setTeamNames(String[] teamNames) {
    //    this.teamNames = teamNames;
    //}
}
