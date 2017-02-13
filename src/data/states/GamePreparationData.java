package data.states;

import data.Helper;
import data.Rules;
import data.teams.TeamLoadInfo;
import data.teams.TeamsLoader;

import java.util.ArrayList;

/**
 * This complete class needs to be bound with the Observer pattern to the respective window
 */
public class GamePreparationData {

    private Rules _active_rules;

    private PrepTeam firstTeam;
    private PrepTeam secondTeam;
    private boolean autoColorChange;
    private boolean fullTimeGame;

    private boolean fullScreen;
    private ArrayList<TeamLoadInfo> availableTeams;

    public GamePreparationData(){
        // Chose default SPL rules
        _active_rules = Rules.LEAGUES[0];

        // Refresh the teams available under those rules
        refreshTeams();

        fullTimeGame = true;
        autoColorChange = false;
        fullScreen = false;
    }

    /**
     * Refreshes the teams that are available
     */
    private void refreshTeams(){
        TeamsLoader tl = TeamsLoader.getInstance();

        availableTeams = tl.getTeamLoadInfoList(_active_rules.leagueName);

        firstTeam = new PrepTeam(availableTeams.get(0));
        secondTeam = new PrepTeam(availableTeams.get(0));
    }

    public void switchRules(Rules _new_rules){
        assert Helper.isValidRule(_new_rules) : "Can not switch to this rules. Not active!";
        _active_rules = _new_rules;
        refreshTeams();
        System.out.println("Switching ruleset");
    }

    public String canStart(){
        /** Checks whether a game can start */

        // Cannot start if both teams are the same
        if (firstTeam.getTeamInfo() == secondTeam.getTeamInfo()){
            return "Cannot start with both teams being the same!";
        }

        if (firstTeam.getTeamColor().equals(secondTeam.getTeamColor())) {
            return "Cannot start when TeamColors are the same";
        }

        return null;
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


    public void chooseTeam(int team_index, TeamLoadInfo new_team) {
        assert 0 <= team_index && team_index <= 1 : "Team index must be 0 or 1";

        if (team_index == 0){
            firstTeam = new PrepTeam(new_team);
        } else {
            secondTeam = new PrepTeam(new_team);
        }
    }

    public PrepTeam getPrepTeam(int team_index) {
        if (team_index == 0){
            return firstTeam;
        } else {
            return secondTeam;
        }
    }

    public Rules getCurrentRules() {
        return _active_rules;
    }

    public ArrayList<TeamLoadInfo> getAvailableTeams() {
        return availableTeams;
    }

    public String toString(){
        return String.format("%s %s\n", this.firstTeam, this.secondTeam);
    }
}
