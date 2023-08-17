package exporter;

import common.net.GameControlReturnDataPackage;
import data.GameControlData;
import data.GameControlReturnData;
import data.TeamInfo;
import data.PlayerInfo;
import java.io.ObjectInputStream;
import java.io.IOException;
import java.io.EOFException;
import java.io.BufferedInputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
///...


public class LogExporter
{
    /**
     * A small exporter that converts TCM log files to a CSV output on the console
     *
     * @author Tim Laue
     */
    public static void main(String[] args)
    {
      if(args.length == 0) {
        System.out.println("No arguments specified!");
        System.out.println("Programs expects the file name of a TCM log file.");
        return;
      }
      String logfile = args[0];    // Name of TCM log to be opened
      GameControlData gcd = null;  // Pointer to last packet from GameController
      char currentGameState = 'X'; // Current game state

      try (ObjectInputStream stream = new ObjectInputStream(new BufferedInputStream(Files.newInputStream(Paths.get(logfile))))) {
        // Print header to console:
        System.out.println("Timestamp;Game State;Team;Player;Penalized;Pose X;Pose Y");
        while(true) {
          final long timestamp = stream.readLong();
          if (stream.readBoolean()) {
            final Object obj = stream.readObject();
            if (obj instanceof GameControlReturnDataPackage) {
              final GameControlReturnData msg = new GameControlReturnData();
              msg.fromByteArray(ByteBuffer.wrap(((GameControlReturnDataPackage) obj).message));
              // Event is a GameControlReturnData packet -> process message and prepare output
              if(gcd != null && currentGameState != 'X') {
                int teamNum = msg.teamNum;
                int playerNum = msg.playerNum;
                // Check, if player is currently penalized:
                int team01 = 0;
                if(gcd.team[1].teamNumber == teamNum)
                  team01 = 1;
                else if(gcd.team[0].teamNumber != teamNum) {
                  System.out.println("WTF??!? Strange team number found! Robot has " + teamNum + "!");
                  System.out.println("The GameController assumes teams " + gcd.team[0].teamNumber + " and " + gcd.team[1].teamNumber + ".");
                  return;
                }
                TeamInfo teamInfo = gcd.team[team01];
                char penaltyChar = teamInfo.player[playerNum-1].penalty != PlayerInfo.PENALTY_NONE ? 'P' : 'U';
                // Finally write data to console:
                float poseX = msg.pose[0];
                float poseY = msg.pose[1];
                System.out.println(timestamp + ";" + currentGameState + ";" + teamNum + ";" + playerNum + ";" + penaltyChar + ";" +
                                   poseX + ";" + poseY);
                // ADD MORE INTERESTING INFORMATION HERE ...
                // ...
                // ...
              }
            } else if (obj instanceof GameControlData) {
              // Set game state variable whenever a new packet from the GameController arrives
              gcd = (GameControlData)obj;
              switch(gcd.gameState) {
                case GameControlData.STATE_PLAYING: currentGameState = 'P'; break;
                case GameControlData.STATE_READY: currentGameState = 'R'; break;
                case GameControlData.STATE_INITIAL: currentGameState = 'I'; break;
                case GameControlData.STATE_FINISHED: currentGameState = 'F'; break;
                case GameControlData.STATE_SET: currentGameState = 'S'; break;
              }
            }
          } /*else if (stream.readInt() == 1) {
            current event is a timeout of the GameController connection
            nothing to do here now...
          }*/
        }
      }
      catch (EOFException e) {
        // Nothing to do here...
        // File just ended.
      }
      catch (IOException | ClassNotFoundException e) {
        System.out.println("Exception: " + e);
      }
  }
}
