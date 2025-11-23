package bhuman.drawings;

import bhuman.message.BHumanMessage;
import com.jogamp.opengl.GL2;
import data.GameControlReturnData;
import data.PlayerInfo;
import data.Rules;
import data.SPL;
import java.io.File;
import java.io.IOException;
import javax.swing.JOptionPane;
import teamcomm.data.RobotState;
import teamcomm.gui.Camera;
import teamcomm.gui.drawings.Image;
import teamcomm.gui.drawings.PerPlayer;
import teamcomm.gui.drawings.TextureLoader;

/**
 * Custom drawing for visualizing when a referee signal was detected.
 *
 * @author Felix Thielke
 * @author Thomas Röfer
 */
public class RefereeDetected extends PerPlayer {

    @Override
    public void draw(final GL2 gl, final RobotState rs, final Camera camera) {
        if (rs.getLastTeamMessage() != null
                && rs.getLastTeamMessage().valid
                && rs.getLastTeamMessage() instanceof BHumanMessage
                && rs.getLastGCRDMessage() != null
                && rs.getLastGCRDMessage().valid) {
            final BHumanMessage bhMsg = (BHumanMessage) rs.getLastTeamMessage();
            final GameControlReturnData gcMsg = rs.getLastGCRDMessage();
            if (bhMsg.theRefereeSignal.signal == BHumanMessage.RefereeGesture__Gesture.ready
                    || bhMsg.theRefereeSignal.signal == BHumanMessage.RefereeGesture__Gesture.kickInLeft
                    || bhMsg.theRefereeSignal.signal == BHumanMessage.RefereeGesture__Gesture.kickInRight) {
                final long timeWhenDetected = rs.getLastTeamMessageTimestamp() -
                        bhMsg.theFrameInfo.time.getTimeSince(bhMsg.theRefereeSignal.timeWhenDetected.timestamp);
                if (Math.abs(System.currentTimeMillis() - timeWhenDetected) <= 2000) {
                    gl.glPushMatrix();

                    if (rs.getPenalty() != PlayerInfo.PENALTY_NONE && !(Rules.league instanceof SPL
                            && (rs.getPenalty() == PlayerInfo.PENALTY_SPL_ILLEGAL_MOTION_IN_STANDBY
                                || rs.getPenalty() == PlayerInfo.PENALTY_SPL_ILLEGAL_MOTION_IN_SET))) {
                        gl.glTranslatef(-bhMsg.playerNumber, -3.5f, 1.f);
                    } else {
                        gl.glTranslatef(gcMsg.pose[0] / 1000.f, gcMsg.pose[1] / 1000.f, 1.f);
                    }

                    camera.turnTowardsCamera(gl);
                    try {
                        BHumanMessage.RefereeGesture__Gesture signal = bhMsg.theRefereeSignal.signal;
                        if (camera.isFlipped()) {
                            switch (signal) {
                                case kickInLeft:
                                    signal = BHumanMessage.RefereeGesture__Gesture.kickInRight;
                                    break;
                                case kickInRight:
                                    signal = BHumanMessage.RefereeGesture__Gesture.kickInLeft;
                                    break;
                            }
                        }
                        final File f = new File("plugins/" + (rs.getTeamNumber() < 10 ? "0" + rs.getTeamNumber() : String.valueOf(rs.getTeamNumber()))
                                + "/resources/" + signal + ".png").getAbsoluteFile();
                        Image.drawImage(gl, TextureLoader.getInstance().loadTexture(gl, f), 0, 0, 0.3f);
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null,
                                "Error loading texture: " + ex.getMessage(),
                                ex.getClass().getSimpleName(),
                                JOptionPane.ERROR_MESSAGE);
                    }
                    gl.glPopMatrix();
                }
            }
        }
    }

    @Override
    public boolean hasAlpha() {
        return true;
    }

    @Override
    public int getPriority() {
        return 10;
    }

}
