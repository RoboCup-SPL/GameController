package data.states;

import data.Helper;
import data.Rules;
import data.teams.TeamsLoader;

import java.util.ArrayList;

/**
 * This complete class needs to be bound with the Observer pattern to the respective window
 */
public class GamePreparationData {

    private Rules _active_rules;
    private ArrayList<PrepTeam> _available_teams;

    private PrepTeam firstTeam;
    private PrepTeam secondTeam;
    private boolean autoColorChange;
    private boolean fullTimeGame;

    private boolean fullScreen;

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
        _available_teams = new ArrayList<>();

        TeamsLoader tl = TeamsLoader.getInstance();

        ArrayList<String> teams = tl.getNames(_active_rules.leagueName, true);


        //TODO simplyfy by jsut usign the teamloadinfo directly
        for(String team: teams){
            if (team != null) {
                String[] components = team.split("\\(");
                String team_name = components[0];
                int team_number = Integer.valueOf(components[1].replace(")", ""));

                PrepTeam pt = new PrepTeam(team_name, (byte) team_number);

                _available_teams.add(pt);
            }
        }
        firstTeam = _available_teams.get(0).clone();
        secondTeam = _available_teams.get(0).clone();
    }

    public void switchRules(Rules _new_rules){
        assert Helper.isValidRule(_new_rules) : "Can not switch to this rules. Not active!";
        _active_rules = _new_rules;
        refreshTeams();
    }

    public boolean canStart(){
        /** Checks whether a game can start */

        // Cannot start if both teams are the same
        if (firstTeam.getTeamNumber() == secondTeam.getTeamNumber()){
            return false;
        }

        if (firstTeam.getTeamColor() == secondTeam.getTeamColor()) {
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

    public void replaceTeam(int team_index, PrepTeam new_team) {
        if (team_index == 0){
            firstTeam = new_team;
        } else {
            secondTeam = new_team;
        }
    }

    public PrepTeam getPrepTeam(int team_index) {
        if (team_index == 0){
            return firstTeam;
        } else {
            return secondTeam;
        }
    }

    public ArrayList<PrepTeam> getPreparedTeams() {
        return _available_teams;
    }
}
