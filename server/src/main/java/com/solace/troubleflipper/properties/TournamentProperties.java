package com.solace.troubleflipper.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@ConfigurationProperties("tournament")
@Component
public class TournamentProperties {
    private int playersPerTeam;
    private Set<String> teamNamesUsed = new HashSet<>();
    private int puzzleSize = 5;

    public int getPlayersPerTeam() {
        return playersPerTeam;
    }

    public void setPlayersPerTeam(int playersPerTeam) {
        this.playersPerTeam = playersPerTeam;
    }

    private String[] teamNamesPart_1 = {
        "Meandering", "Snippy", "Sliding", "Wavering", "Indecisive", "Messy",
        "Splattered", "Wide-eyed", "Burnt", "Toasted", "Spoiled", "Drooling",
        "Flipperdy", "Restless", "Left Over", "Sprinkled",
        "Slushy", "Muddy", "Oiled", "Buggy"
        };

    private String[] teamNamesPart_2 = {
        "Peaches", "Gang", "Flippers", "Toads",
        "Toadettes", "Koopalings", "Piranhas", "Warts", "Waluigis",
        "Kongs", "Birdos", "Lumas", "Diddys", "Magikoopas"
        };

    public void resetTeamNamesUsed() {
        this.teamNamesUsed.clear();
    }

    public String getNewTeamName() {
        Random randomGen = new Random();
        int    fNameCount = teamNamesPart_1.length;
        int    lNameCount = teamNamesPart_2.length;
        int totalNames = fNameCount * lNameCount;

        String newName = null;
        
        while (newName == null && this.teamNamesUsed.size() <= totalNames) {
            int fNameIndex = randomGen.nextInt(fNameCount);
            String fName = teamNamesPart_1[fNameIndex];

            int lNameIndex = randomGen.nextInt(lNameCount);
            String lName = teamNamesPart_2[lNameIndex];

            String tempName = fName + " " + lName;

            // checking for presence of an item in an array, this way, requires Java 8
//            if (!this.teamNamesUsed.contains(tempName)) {
                boolean checkAdd = this.teamNamesUsed.add(tempName);
                if (checkAdd) {
                    newName = tempName;
                    return newName;
                }
//            }
        }

        return newName;
    }

    //public String[] getTeamNames() {
    //    return teamNames;
    //}

    //public void setTeamNames(String[] teamNames) {
    //    this.teamNames = teamNames;
    //}


    public int getPuzzleSize() {
        return puzzleSize;
    }

    public void setPuzzleSize(int puzzleSize) {
        this.puzzleSize = puzzleSize;
    }
}
