package com.solace.troubleflipper.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties("tournament")
@Component
public class TournamentProperties {
    private int playersPerTeam;
    private String[] teamNames;

    public int getPlayersPerTeam() {
        return playersPerTeam;
    }

    public void setPlayersPerTeam(int playersPerTeam) {
        this.playersPerTeam = playersPerTeam;
    }

    @Value("${tournament.teamNamesPart1}")
    private String[] teamNamesPart_1;

    @Value("${tournament.teamNamesPart2}")
    private String[] teamNamesPart_2;

    public String getNewTeamName() {
        String newName = "None";

	System.out.println ("Number of names in part 1: " + teamNamesPart_1.length );
	/* 
	while ( ! newName.equals("None")) {
           String fName =  
	}
	*/
    newName = teamNamesPart_1;
	return newName;
    }

    public String[] getTeamNames() {
        return teamNames;
    }

    public void setTeamNames(String[] teamNames) {
        this.teamNames = teamNames;
    }
}
