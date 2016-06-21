package teamcomm.gui.drawings.common;

import com.jogamp.opengl.GL2;
import data.GameControlData;
import data.PlayerInfo;
import data.SPLStandardMessage;
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
public class Player extends PerPlayer {

    private static String getModelName(final int color) {
        switch (color) {
            case GameControlData.TEAM_BLUE:
                return "robotBlue";
            case GameControlData.TEAM_RED:
                return "robotRed";
            case GameControlData.TEAM_BLACK:
                return "robotBlack";
            case GameControlData.TEAM_YELLOW:
                return "robotYellow";
            case GameControlData.TEAM_WHITE:
                return "robotWhite";
            case GameControlData.TEAM_GREEN:
                return "robotGreen";
            case GameControlData.TEAM_ORANGE:
                return "robotOrange";
            case GameControlData.TEAM_PURPLE:
                return "robotPurple";
            case GameControlData.TEAM_BROWN:
                return "robotBrown";
            case GameControlData.TEAM_GRAY:
                return "robotGray";
        }

        return "robotWhite";
    }

    @Override
    protected void init(GL2 gl) {
        RoSi2Loader.getInstance().cacheModels(gl, new String[]{"robotBlue", "robotRed", "robotBlack", "robotYellow", "robotWhite", "robotGreen", "robotOrange", "robotPurple", "robotBrown", "robotGray"});
    }

    @Override
    public void draw(final GL2 gl, final RobotState player, final Camera camera) {
        final SPLStandardMessage msg = player.getLastMessage();
        if (msg != null && msg.poseValid) {
            gl.glPushMatrix();

            if (player.getPenalty() != PlayerInfo.PENALTY_NONE) {
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

            gl.glCallList(RoSi2Loader.getInstance().loadModel(gl, getModelName(GameState.getInstance().getTeamColor(player.getTeamNumber()))));

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
