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

    public String[] getTeamNames() {
        return teamNames;
    }

    public void setTeamNames(String[] teamNames) {
        this.teamNames = teamNames;
    }
}
