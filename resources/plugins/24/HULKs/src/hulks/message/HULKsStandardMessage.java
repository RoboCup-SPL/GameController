package hulks.message;

import common.Log;
import data.SPLStandardMessage;
import hulks.message.data.Eigen;
import hulks.message.data.NativeReaders;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class HULKsStandardMessage {

    private static final int CURRENT_VERSION = 3;

    private boolean valid;
    private int version;
    private boolean isPoseValid;
    private Eigen.Vector2f walkingPosition;
    private float walkingOrientation;
    private Eigen.Vector2f ballVelocity;
    private Eigen.Vector2f currentSearchPosition;
    private int numberOfSuggestedPositions;
    private List<Eigen.Vector2f> positionSuggestions;
    private List<Integer> joinStates;

    public HULKsStandardMessage() {
        this.valid = false;
    }

    public boolean isValid() {
        return valid;
    }

    public int getVersion() {
        return version;
    }

    public boolean isPoseValid() {
        return isPoseValid;
    }

    public Eigen.Vector2f getWalkingPosition() {
        return walkingPosition;
    }

    public float getWalkingOrientation() {
        return walkingOrientation;
    }

    public Eigen.Vector2f getBallVelocity() {
        return ballVelocity;
    }

    public Eigen.Vector2f getCurrentSearchPosition() {
        return currentSearchPosition;
    }

    public int getNumberOfSuggestedPositions() {
        return numberOfSuggestedPositions;
    }

    public List<Eigen.Vector2f> getPositionSuggestions() {
        return positionSuggestions;
    }

    public List<Integer> getJoinStates() {
        return joinStates;
    }

    private static String getCurrentTime() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    public HULKsStandardMessage read(final SPLStandardMessage origin, ByteBuffer stream) {
        final String currentTimeString = getCurrentTime();
        if (stream.remaining() < NativeReaders.ucharReader.getStreamedSize()) {
            Log.error(String.format(
                    "[%s][%s] Invalid HULKs message without version.",
                    origin.playerNum, currentTimeString
            ));
            return this;
        }
        version = NativeReaders.ucharReader.read(stream);
        if (version != CURRENT_VERSION) {
            Log.error(String.format(
                    "[%s][%s] Invalid HULKs message version '%s'.",
                    origin.playerNum, currentTimeString, version
            ));
            return this;
        }
        if (stream.remaining() < NativeReaders.boolReader.getStreamedSize()) {
            Log.error(String.format(
                    "[%s][%s] Incomplete HULKs message (no isPoseValid).",
                    origin.playerNum, currentTimeString
            ));
            return this;
        }
        isPoseValid = NativeReaders.boolReader.read(stream);
        walkingPosition = new Eigen.Vector2f();
        if (stream.remaining() < walkingPosition.getStreamedSize()) {
            Log.error(String.format(
                    "[%s][%s] Incomplete HULKs message (no walkingPosition).",
                    origin.playerNum, currentTimeString
            ));
            return this;
        }
        walkingPosition.read(stream);
        if (stream.remaining() < NativeReaders.floatReader.getStreamedSize()) {
            Log.error(String.format(
                    "[%s][%s] Incomplete HULKs message (no walkingOrientation).",
                    origin.playerNum, currentTimeString
            ));
            return this;
        }
        walkingOrientation = NativeReaders.floatReader.read(stream);
        ballVelocity = new Eigen.Vector2f();
        if (stream.remaining() < ballVelocity.getStreamedSize()) {
            Log.error(String.format(
                    "[%s][%s] Incomplete HULKs message (no ballVelocity).",
                    origin.playerNum, currentTimeString
            ));
            return this;
        }
        ballVelocity.read(stream);
        currentSearchPosition = new Eigen.Vector2f();
        if (stream.remaining() < currentSearchPosition.getStreamedSize()) {
            Log.error(String.format(
                    "[%s][%s] Incomplete HULKs message (no currentSearchPosition).",
                    origin.playerNum, currentTimeString
            ));
            return this;
        }
        currentSearchPosition.read(stream);
        if (stream.remaining() < NativeReaders.ucharReader.getStreamedSize()) {
            Log.error(String.format(
                    "[%s][%s] Incomplete HULKs message (no currentSearchPosition).",
                    origin.playerNum, currentTimeString
            ));
            return this;
        }
        numberOfSuggestedPositions = NativeReaders.ucharReader.read(stream);
        positionSuggestions = new ArrayList<>(numberOfSuggestedPositions);
        for (int i = 0; i < numberOfSuggestedPositions; i++) {
            final Eigen.Vector2f position = new Eigen.Vector2f();
            if (stream.remaining() < position.getStreamedSize()) {
                Log.error(String.format(
                        "[%s][%s] Incomplete HULKs message (no positionSuggestion (%s/%s)).",
                        origin.playerNum, currentTimeString, i, numberOfSuggestedPositions
                ));
                return this;
            }
            position.read(stream);
            positionSuggestions.add(position);
        }
        joinStates = new ArrayList<>(26); // 26 Joints
        for (int i = 0; i < 26; i++) {
            if (stream.remaining() < NativeReaders.ucharReader.getStreamedSize()) {
                Log.error(String.format(
                        "[%s][%s] Incomplete HULKs message (no jointState (%s/%s)).",
                        origin.playerNum, currentTimeString, i, 26
                ));
                return this;
            }
            joinStates.add(Integer.valueOf(NativeReaders.ucharReader.read(stream)));
        }
        valid = true;
        return this;
    }
}
