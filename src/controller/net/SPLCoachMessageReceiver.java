package controller.net;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import common.Log;
import controller.EventHandler;
import data.GameControlData;
import data.PlayerInfo;
import data.Rules;
import data.SPLCoachMessage;

public class SPLCoachMessageReceiver extends Thread
{
    private static SPLCoachMessageReceiver instance;
    private final DatagramSocket datagramSocket;
    private boolean rejectCoachMessage[] = {false, false};
    private long timestampCoachPackage[] = {0, 0};
    private ArrayList<SPLCoachMessage> splCoachMessageQueue = new ArrayList<SPLCoachMessage>();

    private SPLCoachMessageReceiver() throws SocketException
    {
        datagramSocket = new DatagramSocket(null);
        datagramSocket.setReuseAddress(true);
        datagramSocket.bind(new InetSocketAddress(GameControlData.GAMECONTROLLER_PORT));
    }

    public synchronized static SPLCoachMessageReceiver getInstance()
    {
        if (instance == null) {
            try {
                instance = new SPLCoachMessageReceiver();
            } catch (SocketException e) {
                throw new IllegalStateException("fatal: Error while setting up Receiver.", e);
            }
        }
        return instance;
    }

    @Override
    public void run()
    {
        while (!isInterrupted()) {
            int i = 0;
            while (i < splCoachMessageQueue.size()) {
                if (splCoachMessageQueue.get(i).getRemainingTimeToSend() == 0) {
                    for (int j = 0; j < 2; j++) {
                        if (EventHandler.getInstance().data.team[j].teamColor == splCoachMessageQueue.get(i).team ) {
                            EventHandler.getInstance().data.team[j].coachMessage = splCoachMessageQueue.get(i).message;
                            splCoachMessageQueue.remove(i);
                            break;
                        }
                    }
                } else {
                    i++;
                }
            }

            for (i = 0; i < 2; i++) {
                if (System.currentTimeMillis() - timestampCoachPackage[i] >= SPLCoachMessage.SPL_COACH_MESSAGE_RECEIVE_INTERVALL) {
                    rejectCoachMessage[i] = false;
                }
            }
            
            try {
                final ByteBuffer buffer = ByteBuffer.wrap(new byte[SPLCoachMessage.SIZE]);
                final DatagramPacket packet = new DatagramPacket(buffer.array(), buffer.array().length);

                datagramSocket.receive(packet);
                buffer.rewind();

                final SPLCoachMessage coach = new SPLCoachMessage();

                if (coach.fromByteArray(buffer)) {
                    byte team = (EventHandler.getInstance().data.team[0].teamColor == coach.team)? (byte)0 : (byte)1;
                    RobotWatcher.updateCoach(team);
                    if (!rejectCoachMessage[coach.team] && (EventHandler.getInstance().data.team[team].coach.penalty != PlayerInfo.PENALTY_SPL_COACH_MOTION)) {
                        rejectCoachMessage[coach.team] = true;
                        timestampCoachPackage[coach.team] = System.currentTimeMillis();
                        splCoachMessageQueue.add(coach);
                    }
                }
            } catch (IOException e) {
                Log.error("something went wrong while receiving the coach packages : " + e.getMessage());
            }
        }

        datagramSocket.close();
    }
}
