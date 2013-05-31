package visualizer;

import common.Log;
import data.GameControlData;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

/**
 * @author: Michel Bartsch
 * 
 * This class receives the GameControlData from the GameController.
 */
public class Listener implements Runnable
{
    /** This is the port on which to listen. */
    private static final int PORT = 3838;
    
    /** The GUI to listen for, it´s update method will be called. */
    private GUI gui;
    /** Some attributes for receiving. */
    private DatagramSocket socket;
    private ByteBuffer buffer;
    private DatagramPacket packet;
    /** This will be set true by the method close to stop receiving. */
    private boolean closed = false;

    /**
     * Creates a new Listener.
     */
    public Listener(GUI gui)
    {
        this.gui = gui;
        try {
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.bind(new InetSocketAddress(PORT));
            buffer = ByteBuffer.wrap(new byte[GameControlData.SIZE]);
            packet = new DatagramPacket(buffer.array(), buffer.array().length);
        } catch (Exception e) {
            Log.error("Error on start listening to port "+PORT);
            System.exit(1);
        }
    }

    @Override
    public void run()
    {
        while (!closed) {
            try {
                socket.receive(packet);
                GameControlData data = new GameControlData();
                if(data.fromByteArray(buffer)) {
                    gui.update(data);
                }
            } catch(Exception e) {
                Log.error("Error while listening to port "+PORT);
            }
        }
    }
    
    /**
     * Closes the socket and stops receiving.
     */
    public void close()
    {
        closed = true;
        try {
            socket.close();
        } catch (Exception e) {
            Log.error("Error while closing port "+PORT);
        }
    }
}