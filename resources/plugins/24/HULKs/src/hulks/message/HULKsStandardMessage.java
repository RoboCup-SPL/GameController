package hulks.message;

import common.Log;
import hulks.message.data.Eigen;
import hulks.message.data.NativeReaders;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

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

    public HULKsStandardMessage read(ByteBuffer stream) {
        if (stream.remaining() < NativeReaders.charReader.getStreamedSize()) {
            Log.error("Missing HULKs message.");
            return this;
        }
        version = NativeReaders.charReader.read(stream);
        if (version != CURRENT_VERSION) {
            Log.error("Invalid HULKs message without version.");
            return this;
        }
        if (stream.remaining() < NativeReaders.boolReader.getStreamedSize()) {
            Log.error("Incomplete HULKs message (no isPoseValid)");
            return this;
        }
        isPoseValid = NativeReaders.boolReader.read(stream);
        walkingPosition = new Eigen.Vector2f();
        if (stream.remaining() < walkingPosition.getStreamedSize()) {
            Log.error("Incomplete HULKs message (no walkingPosition)");
            return this;
        }
        walkingPosition.read(stream);
        if (stream.remaining() < NativeReaders.floatReader.getStreamedSize()) {
            Log.error("Incomplete HULKs message (no walkingOrientation)");
            return this;
        }
        walkingOrientation = NativeReaders.floatReader.read(stream);
        ballVelocity = new Eigen.Vector2f();
        if (stream.remaining() < ballVelocity.getStreamedSize()) {
            Log.error("Incomplete HULKs message (no ballVelocity)");
            return this;
        }
        ballVelocity.read(stream);
        currentSearchPosition = new Eigen.Vector2f();
        if (stream.remaining() < currentSearchPosition.getStreamedSize()) {
            Log.error("Incomplete HULKs message (no currentSearchPosition)");
            return this;
        }
        currentSearchPosition.read(stream);
        if (stream.remaining() < NativeReaders.ucharReader.getStreamedSize()) {
            Log.error("Incomplete HULKs message (no currentSearchPosition)");
            return this;
        }
        numberOfSuggestedPositions = NativeReaders.ucharReader.read(stream);
        positionSuggestions = new ArrayList<>(numberOfSuggestedPositions);
        for (int i = 0; i < numberOfSuggestedPositions; i++) {
            final Eigen.Vector2f position = new Eigen.Vector2f();
            if (stream.remaining() < position.getStreamedSize()) {
                Log.error(String.format("Incomplete HULKs message (no positionSuggestion (%s/%s))", i, numberOfSuggestedPositions));
                return this;
            }
            position.read(stream);
            positionSuggestions.add(position);
        }
        joinStates = new ArrayList<>(26); // 26 Joints
        for (int i = 0; i < 26; i++) {
            if (stream.remaining() < NativeReaders.charReader.getStreamedSize()) {
                Log.error(String.format("Incomplete HULKs message (no jointState (%s/%s))", i, 26));
                return this;
            }
            joinStates.add(Integer.valueOf(NativeReaders.charReader.read(stream)));
        }
        valid = true;
        return this;
    }
}
