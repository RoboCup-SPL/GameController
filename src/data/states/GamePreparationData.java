package data.states;

import data.communication.GameControlData;

/**
 * This complete class needs to be bound with the Observer pattern to the respective window
 */
public class GamePreparationData {


    private PrepTeam firstTeam;
    private PrepTeam secondTeam;
    private boolean autoColorChange;
    private boolean fullTimeGame;
    private boolean fullScreen;

    public GamePreparationData(){
        firstTeam = new PrepTeam();
        secondTeam = new PrepTeam();

        fullTimeGame = true;
        autoColorChange = false;
        fullScreen = false;
    }



    public boolean canStart(){
        /** Checks whether a game can start */

        // Cannot start if both teams are the same
        if (firstTeam.teamNumber == secondTeam.teamNumber){
            return false;
        }

        if (firstTeam.teamColor == secondTeam.teamColor) {
            return false;
        }

        return true;
    }

    public PrepTeam getSecondTeam() {
        return secondTeam;
    }

    public PrepTeam getFirstTeam() {
        return firstTeam;
    }

    public boolean isAutoColorChange() {
        return autoColorChange;
    }

    public boolean isFullTimeGame() {
        return fullTimeGame;
    }

    public void setAutoColorChange(boolean autoColorChange){
        this.autoColorChange = autoColorChange;
    }

    public void setFullTimeGame(boolean fullTimeGame){
        this.fullTimeGame = fullTimeGame;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public boolean getFullScreen() {
        return fullScreen;
    }

    public PrepTeam getPrepTeam(int team_index) {
        if (team_index == 0){
            return firstTeam;
        } else {
            return secondTeam;
        }
    }

    public class PrepTeam {
        private byte teamColor;
        private byte teamNumber;

        private PrepTeam(){
            teamNumber = 0;
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
    }

}
