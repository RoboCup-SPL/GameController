package teamcomm.gui.drawings.common;

import com.jogamp.opengl.GL2;
import data.GameControlData;
import data.GameControlReturnData;
import data.PlayerInfo;
import data.Rules;
import data.SPL;
import teamcomm.data.GameState;
import teamcomm.data.RobotState;
import teamcomm.gui.Camera;
import teamcomm.gui.drawings.PerPlayer;
import teamcomm.gui.drawings.RoSi2Loader;

/**
 * Drawing for a robot.
 *
 * @author Felix Thielke
 */
public class K1 extends PerPlayer {

    private static String getModelName(final int color) {
        switch (color) {
            case GameControlData.TEAM_BLUE:
                return "k1Blue";
            case GameControlData.TEAM_RED:
                return "k1Red";
            case GameControlData.TEAM_BLACK:
                return "k1Black";
            case GameControlData.TEAM_YELLOW:
                return "k1Yellow";
            case GameControlData.TEAM_WHITE:
                return "k1White";
            case GameControlData.TEAM_GREEN:
                return "k1Green";
            case GameControlData.TEAM_ORANGE:
                return "k1Orange";
            case GameControlData.TEAM_PURPLE:
                return "k1Purple";
            case GameControlData.TEAM_BROWN:
                return "k1Brown";
            case GameControlData.TEAM_GRAY:
                return "k1Gray";
        }

        return "k1White";
    }

    @Override
    protected void init(GL2 gl) {
        RoSi2Loader.getInstance().cacheModels(gl, new String[]{"k1Blue", "k1Red", "k1Black", "k1Yellow", "k1White", "k1Green", "k1Orange", "k1Purple", "k1Brown", "k1Gray"});
    }

    @Override
    public void draw(final GL2 gl, final RobotState player, final Camera camera) {
        final GameControlReturnData msg = player.getLastGCRDMessage();
        if (msg != null && msg.poseValid) {
            gl.glPushMatrix();

            if (player.getPenalty() != PlayerInfo.PENALTY_NONE && !(Rules.league instanceof SPL
                    && (player.getPenalty() == PlayerInfo.PENALTY_SPL_ILLEGAL_MOTION_IN_STANDBY
                    || player.getPenalty() == PlayerInfo.PENALTY_SPL_ILLEGAL_MOTION_IN_SET))) {
                gl.glTranslatef(-msg.playerNum, -3.5f, 0);
                gl.glRotatef(-90, 0, 0, 1);
            } else {
                gl.glTranslatef(msg.pose[0] / 1000.f, msg.pose[1] / 1000.f, 0);
                gl.glRotatef((float) Math.toDegrees(msg.pose[2]), 0, 0, 1);

                if (msg.fallenValid && msg.fallen) {
                    gl.glTranslatef(0, 0, 0.05f);
                    gl.glRotatef(90, 0, 1, 0);
                }
            }

            gl.glCallList(RoSi2Loader.getInstance().loadModel(gl, getModelName(GameState.getInstance().getTeamColor(player.getTeamNumber(), player.getPlayerNumber()))));

            gl.glPopMatrix();
        }
    }

    @Override
    public boolean hasAlpha() {
        return false;
    }

    @Override
    public int getPriority() {
        return 0;
    }
}
