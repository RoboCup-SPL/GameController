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
import data.SPLCoachMessage;

public class SPLCoachMessageReceiver extends Thread {
	private static SPLCoachMessageReceiver instance;
	private final DatagramSocket datagramSocket;
	private boolean isBlueCoachPackageReceived = false;
	private boolean isRedCoachPackageReceived = false;
	private long timestampBlueCoachPackage;
	private long timestampRedCoachPackage;
	private ArrayList<SPLCoachMessage> splCoachMessagQueue = new ArrayList<>();

	private SPLCoachMessageReceiver() throws SocketException {
		datagramSocket = new DatagramSocket(null);
		datagramSocket.setReuseAddress(true);
		datagramSocket.bind(new InetSocketAddress(
				GameControlData.GAMECONTROLLER_PORT));
		timestampBlueCoachPackage = 0;
		timestampRedCoachPackage = 0;
	}

	public synchronized static SPLCoachMessageReceiver getInstance() {
		if (instance == null) {
			try {
				instance = new SPLCoachMessageReceiver();
			} catch (SocketException e) {
				throw new IllegalStateException(
						"fatal: Error while setting up Receiver.", e);
			}
		}
		return instance;
	}

	@Override
	public void run() {
		while (!isInterrupted()) {
			int i = 0;
			while (i < splCoachMessagQueue.size()) {
				if (splCoachMessagQueue.get(i).getRemainingTimeToSend() == 0) {
					EventHandler.getInstance().data.team[splCoachMessagQueue
							.get(i).team].coachMessage = splCoachMessagQueue
							.get(i).message;
					splCoachMessagQueue.remove(i);
				} else {
					i++;
				}
			}
			final ByteBuffer buffer = ByteBuffer
					.wrap(new byte[SPLCoachMessage.SIZE]);
			final SPLCoachMessage coach = new SPLCoachMessage();

			final DatagramPacket packet = new DatagramPacket(buffer.array(),
					buffer.array().length);

			if (isBlueCoachPackageReceived) {
				if ((System.currentTimeMillis() - timestampBlueCoachPackage) >= SPLCoachMessage.SPL_COACH_MESSAGE_RECEIVE_INTERVALL) {
					isBlueCoachPackageReceived = false;
				}
			}

			if (isRedCoachPackageReceived) {
				if ((System.currentTimeMillis() - timestampRedCoachPackage) >= SPLCoachMessage.SPL_COACH_MESSAGE_RECEIVE_INTERVALL) {
					isRedCoachPackageReceived = false;
				}
			}

			try {
				datagramSocket.receive(packet);
				buffer.rewind();

				if (coach.fromByteArray(buffer)) {
					if (coach.team == GameControlData.TEAM_BLUE) {
						if (!isBlueCoachPackageReceived) {
							isBlueCoachPackageReceived = true;
							timestampBlueCoachPackage = System
									.currentTimeMillis();
							splCoachMessagQueue.add(coach);
							System.out.println("GET MESSAGE : " + coach.getRemainingTimeToSend());
						}
					} else {
						if (!isRedCoachPackageReceived) {
							isRedCoachPackageReceived = true;
							timestampRedCoachPackage = System
									.currentTimeMillis();
							splCoachMessagQueue.add(coach);
						}
					}
				}
			} catch (IOException e) {
				Log.error("something went wrong while receiving the coach packages : "+ e.getMessage());
			}
		}

		datagramSocket.close();
	}
}
