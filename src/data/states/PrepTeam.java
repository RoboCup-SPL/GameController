package data.states;

import data.communication.GameControlData;

/**
 * Created by rkessler on 2017-02-11.
 */
public class PrepTeam {

    private String teamName;
    private byte teamColor;
    private byte teamNumber;

    PrepTeam(String name, byte number, byte color){
        teamName = name;
        teamNumber = number;
        teamColor = color;
    }

    PrepTeam(String name, byte number){
        teamName = name;
        teamNumber = number;
        teamColor = GameControlData.TEAM_WHITE;
    }

    public byte getTeamColor() {
        return teamColor;
    }

    public void setTeamColor(String teamColorString) {
        this.teamColor = GameControlData.fromColorName(teamColorString);
    }

    public void setTeamColor(byte teamColorByte) {
        assert GameControlData.colorConstants.contains(teamColorByte) :
                String.format("Unknown team color byte %1s must be one of %s1",
                        teamColorByte, GameControlData.colorConstants);
        this.teamColor = teamColorByte;
    }

    public byte getTeamNumber() {
        return teamNumber;
    }

    public void setTeamNumber(int teamNumber) {
        this.teamNumber = (byte) teamNumber;
    }

    public PrepTeam clone(){
        return new PrepTeam(this.teamName, this.teamNumber, this.teamColor);
    }
}