/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package teamcomm.net;

import java.io.Serializable;

/**
 * Class for a message. Instances of this class are stored in the log files.
 */
public class SPLStandardMessagePackage implements Serializable {

    private static final long serialVersionUID = 758311663011901849L;
    public final String host;
    public final int team;
    public final byte[] message;

    public SPLStandardMessagePackage(final String host, final int team, final byte[] message) {
        this.host = host;
        this.team = team;
        this.message = message;
    }

}
