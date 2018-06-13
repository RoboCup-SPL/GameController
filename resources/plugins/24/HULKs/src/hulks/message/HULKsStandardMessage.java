package hulks.message;

import common.Log;
import data.SPLStandardMessage;
import hulks.message.data.Eigen;
import hulks.message.data.NativeReaders;
import hulks.message.data.SearchPosition;
import hulks.message.data.Timestamp;

import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class HULKsStandardMessage {

    private static final int CURRENT_VERSION = 4;
    private static final int MAX_NUM_PLAYERS = 6;

    private boolean valid;
    private int version;
    private boolean isPoseValid;
    private Eigen.Vector2f walkingPosition;
    private float walkingOrientation;
    private Eigen.Vector2f ballVelocity;
    private List<Integer> joinStates;
    private Eigen.Vector2f currentSearchPosition;
    private List<SearchPosition> searchPositionSuggestions;
    private Timestamp timestampBallSearchMapUnreliable;
    private boolean availableForSearch;
    private int mostWisePlayerNumber;


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

    public List<Integer> getJoinStates() {
        return joinStates;
    }

    public List<SearchPosition> getSearchPositionSuggestions() {
        return searchPositionSuggestions;
    }

    public Timestamp getTimestampBallSearchMapUnreliable() {
        return timestampBallSearchMapUnreliable;
    }

    public boolean isAvailableForSearch() {
        return availableForSearch;
    }

    public int getMostWisePlayerNumber() {
        return mostWisePlayerNumber;
    }

    private static String getCurrentTime() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    public HULKsStandardMessage read(final SPLStandardMessage origin, ByteBuffer stream) {
        // Message Version
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

        // isPoseValid
        if (stream.remaining() < NativeReaders.boolReader.getStreamedSize()) {
            Log.error(String.format(
                    "[%s][%s] Incomplete HULKs message (no isPoseValid).",
                    origin.playerNum, currentTimeString
            ));
            return this;
        }
        isPoseValid = NativeReaders.boolReader.read(stream);

        // walkingPosition
        walkingPosition = new Eigen.Vector2f();
        if (stream.remaining() < walkingPosition.getStreamedSize()) {
            Log.error(String.format(
                    "[%s][%s] Incomplete HULKs message (no walkingPosition).",
                    origin.playerNum, currentTimeString
            ));
            return this;
        }
        walkingPosition.read(stream);

        // walkingOrientation
        if (stream.remaining() < NativeReaders.floatReader.getStreamedSize()) {
            Log.error(String.format(
                    "[%s][%s] Incomplete HULKs message (no walkingOrientation).",
                    origin.playerNum, currentTimeString
            ));
            return this;
        }
        walkingOrientation = NativeReaders.floatReader.read(stream);

        // ballVelocity
        ballVelocity = new Eigen.Vector2f();
        if (stream.remaining() < ballVelocity.getStreamedSize()) {
            Log.error(String.format(
                    "[%s][%s] Incomplete HULKs message (no ballVelocity).",
                    origin.playerNum, currentTimeString
            ));
            return this;
        }
        ballVelocity.read(stream);

        // currentSearchPosition
        currentSearchPosition = new Eigen.Vector2f();
        if (stream.remaining() < currentSearchPosition.getStreamedSize()) {
            Log.error(String.format(
                    "[%s][%s] Incomplete HULKs message (no currentSearchPosition).",
                    origin.playerNum, currentTimeString
            ));
            return this;
        }
        currentSearchPosition.read(stream);

        // positionSuggestions
        searchPositionSuggestions = new ArrayList<>(MAX_NUM_PLAYERS);
        if (stream.remaining() < NativeReaders.ucharReader.getStreamedSize()) {
            Log.error(String.format(
                    "[%s][%s] Incomplete HULKs message (no searchPositionsValid).",
                    origin.playerNum, currentTimeString
            ));
            return this;
        }
        short searchPositionsValid = NativeReaders.ucharReader.read(stream);
        for (int i = 0; i < MAX_NUM_PLAYERS; i++) {
            final Eigen.Vector2f position = new Eigen.Vector2f();
            if (stream.remaining() < position.getStreamedSize()) {
                Log.error(String.format(
                        "[%s][%s] Incomplete HULKs message (no searchPosition (%s/%s)).",
                        origin.playerNum, currentTimeString, i, MAX_NUM_PLAYERS
                ));
            }
            position.read(stream);
            if (getBit(searchPositionsValid, i)) {
                searchPositionSuggestions.add(new SearchPosition(i, position));
            }
        }

        // timestampBallSearchMapUnreliable
        timestampBallSearchMapUnreliable = new Timestamp();
        if (stream.remaining() < timestampBallSearchMapUnreliable.getStreamedSize()) {
            Log.error(String.format(
                    "[%s][%s] Incomplete HULKs message (no timestampBallSearchMapUnreliable).",
                    origin.playerNum, currentTimeString
            ));
        }
        timestampBallSearchMapUnreliable.read(stream);

        // availableForSearch
        if (stream.remaining() < NativeReaders.boolReader.getStreamedSize()) {
            Log.error(String.format(
                    "[%s][%s] Incomplete HULKs message (no availableForSearch).",
                    origin.playerNum, currentTimeString
            ));
            return this;
        }
        availableForSearch = NativeReaders.boolReader.read(stream);

        // mostWisePlayerNumber
        if (stream.remaining() < NativeReaders.ucharReader.getStreamedSize()) {
            Log.error(String.format(
                    "[%s][%s] Incomplete HULKs message (no mostWisePlayerNumber).",
                    origin.playerNum, currentTimeString
            ));
            return this;
        }
        mostWisePlayerNumber = NativeReaders.ucharReader.read(stream);

        //jointStates
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

        if (stream.remaining() > 0) {
            Log.error(String.format(
                    "[%s][%s] Message longer than expected. Remaining: %s.",
                    origin.playerNum, currentTimeString, stream.remaining()
            ));
        }

        valid = true;
        return this;
    }

    private static boolean getBit(short fromByte, int position) {
        return ((fromByte >> position) & 1) == 1;
    }

}
