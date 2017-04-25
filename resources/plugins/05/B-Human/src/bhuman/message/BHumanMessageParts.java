package bhuman.message;

import bhuman.message.data.ListCountSize;
import bhuman.message.data.NativeReaders;
import bhuman.message.data.Primitive;
import bhuman.message.data.Reader;
import bhuman.message.data.StreamReader;
import bhuman.message.data.StreamedObject;
import common.Log;
import data.SPLStandardMessage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 *
 * @author Felix Thielke
 */
public class BHumanMessageParts {

    private static final String BHULKS_STANDARD_MESSAGE_STRUCT_HEADER = "BHLK";
    private static final short BHULKS_STANDARD_MESSAGE_STRUCT_VERSION = 7;

    private static final String BHUMAN_STANDARD_MESSAGE_STRUCT_HEADER = "BHUM";
    private static final short BHUMAN_STANDARD_MESSAGE_STRUCT_VERSION = 3;

    private static final String BHUMAN_ARBITRARY_MESSAGE_STRUCT_HEADER = "BHUA";
    private static final short BHUMAN_ARBITRARY_MESSAGE_STRUCT_VERSION = 0;

    /**
     * The B-HULKs information transferred in this message.
     */
    public final BHULKsStandardMessagePart bhulks;

    /**
     * The B-Human standard information transferred in this message.
     */
    public final BHumanStandardMessagePart bhuman;

    /**
     * The MessageQueue transferred in this message.
     */
    public final MessageQueue queue;

    public BHumanMessageParts(final SPLStandardMessage origin, final ByteBuffer data) {
        BHULKsStandardMessagePart bhlk = null;
        BHumanStandardMessagePart bhum = null;
        MessageQueue q = null;

        data.rewind();
        data.order(ByteOrder.LITTLE_ENDIAN);

        String header = new NativeReaders.SimpleStringReader(4).read(data);
        short version = NativeReaders.ucharReader.read(data);
        if (header.equals(BHULKS_STANDARD_MESSAGE_STRUCT_HEADER)) {
            if (version == BHULKS_STANDARD_MESSAGE_STRUCT_VERSION) {
                bhlk = new BHULKsStandardMessagePart();
                if (bhlk.getStreamedSize(data) <= data.remaining()) {
                    bhlk.read(data);
                } else {
                    Log.error("Wrong size of BHULKs message struct: was " + data.remaining() + ", expected " + bhlk.getStreamedSize(data));
                }
            } else {
                Log.error("Wrong BHULKs message struct version: was " + version + ", expected " + BHULKS_STANDARD_MESSAGE_STRUCT_VERSION);
            }
        } else {
            Log.error("Expected BHULKs message struct header, found: " + header);
        }

        header = new NativeReaders.SimpleStringReader(4).read(data);
        version = NativeReaders.ucharReader.read(data);
        if (header.equals(BHUMAN_STANDARD_MESSAGE_STRUCT_HEADER)) {
            if (version == BHUMAN_STANDARD_MESSAGE_STRUCT_VERSION) {
                bhum = new BHumanStandardMessagePart();
                if (bhum.getStreamedSize(data) <= data.remaining()) {
                    bhum.read(data);
                } else {
                    Log.error("Wrong size of B-Human standard message struct: was " + data.remaining() + ", expected " + bhum.getStreamedSize(data));
                }
            } else {
                Log.error("Wrong B-Human standard message struct version: was " + version + ", expected " + BHUMAN_STANDARD_MESSAGE_STRUCT_VERSION);
            }
        } else {
            data.position(data.position() - 5);
            Log.error("Expected B-Human standard message struct header, found: " + header);
        }

        header = new NativeReaders.SimpleStringReader(4).read(data);
        version = NativeReaders.ucharReader.read(data);
        if (header.equals(BHUMAN_ARBITRARY_MESSAGE_STRUCT_HEADER)) {
            if (version == BHUMAN_ARBITRARY_MESSAGE_STRUCT_VERSION) {
                q = new MessageQueue(origin, data);
            } else {
                Log.error("Wrong B-Human arbitrary message struct version: was " + version + ", expected " + BHUMAN_ARBITRARY_MESSAGE_STRUCT_VERSION);
            }
        } else {
            data.position(data.position() - 5);
            Log.error("Expected B-Human arbitrary message struct header, found: " + header);
        }

        this.bhulks = bhlk;
        this.bhuman = bhum;
        this.queue = q;
    }

    public static class BHULKsStandardMessagePart extends StreamedObject<BHULKsStandardMessagePart> {

        private static final int BHULKS_STANDARD_MESSAGE_MAX_NUM_OF_PLAYERS = 10;

        public static enum Team {
            HULKS,
            BHUMAN;
        }

        public static enum Role {
            // This is chess, because if it is chess than it is more exact! (Cheers Tim)
            King, //< Keeper         -> short range doing what he wants
            Rook, //< Defender       -> mainly covering with "horizontal" movement
            Queen, //< Striker        -> running wherever she wants
            Knight, //< Supporter      -> jumping alongside the Queen and helping her out
            Bishop, //< Pass Station   -> diagonal movement in front of the Queen
            beatenPieces; //< not perceived numbers -> robots staying outside the field carpet
        }

        public static enum HearingConfidence {
            iAmDeaf((byte) -1),
            heardOnOneEarButThinkingBothAreOk((byte) 33),
            oneEarIsBroken((byte) 66),
            allEarsAreOk((byte) 100);

            public final byte value;

            HearingConfidence(final byte value) {
                this.value = value;
            }

            public static HearingConfidence getHearingConfidence(final byte i) {
                for (final HearingConfidence v : values()) {
                    if (v.value == i) {
                        return v;
                    }
                }
                return null;
            }
        }

        public static class HearingConfidenceReader implements StreamReader<HearingConfidence> {

            @Override
            public HearingConfidence read(final ByteBuffer stream) {
                return HearingConfidence.getHearingConfidence(stream.get());
            }

        }

        public static class Obstacle extends StreamedObject<Obstacle> {

            public static enum ObstacleType {
                goalpost,
                unknown,
                someRobot,
                opponent,
                teammate,
                fallenSomeRobot,
                fallenOpponent,
                fallenTeammate;
            }

            // the obstacle center in robot (self) centered coordinates
            // - x goes to front
            // - y goes to left
            public float[] center = new float[2];

            @Primitive("uint")
            public long timestampLastSeen; //< the name says it
            public ObstacleType type;          //< the name says it
            public byte[] padding = new byte[3];

        }

        public static class BNTPMessage extends StreamedObject<BNTPMessage> {

            @Primitive("uint")
            public long requestOrigination;  //< The timestamp of the generation of the request
            @Primitive("uint")
            public long requestReceipt;      //< The timestamp of the receipt of the request
            @Primitive("uchar")
            public short receiver;             //< The robot, to which this message should be sent
            public byte[] padding = new byte[2];
        }

        public Team member;

        @Primitive("uint")
        public long timestamp;  //< the timestamp of this message

        public boolean isUpright;               //< The name says it all
        public boolean hasGroundContact;        //< The name says it all
        @Primitive("uint")
        public long timeOfLastGroundContact; //< The name says it all

        // is the robot penalized?
        // Theoretically the game controller say it too, but this is for better information
        // spreading in case of bad WLAN quality in combination with PENALTY_MANUAL.
        public boolean isPenalized;

        // GameControlData is omitted
        public byte[] gameControlData = new byte[9];

        // the current meassurement of head joint: HeadYaw
        public float headYawAngle;

        // the role this robot is currently performing
        public Role currentlyPerfomingRole;

        // the calculated role per robot
        // e.g. the role that is calculated for robot 2 is at position
        //      roleAssignments[<robot id> - 1] => roleAssignments[1]
        public Role[] roleAssignments = new Role[BHULKS_STANDARD_MESSAGE_MAX_NUM_OF_PLAYERS];

        // the King is playing the ball => the Queen must not play the ball
        // - !!! for safety reasons this should always be combined with
        //        (sPLStandardMessage.playerNum == 0)
        public boolean kingIsPlayingBall;

        // does/means what it says
        @Primitive("uint")
        public long timeWhenReachBall;
        @Primitive("uint")
        public long timeWhenReachBallQueen;

        // timestamp, when the ball was recognized
        // this is theoretically equal to SPLStandardMessage::ballAge, BUT it allows us to "ntp" it.
        @Primitive("uint")
        public long ballTimeWhenLastSeen;

        // the pass target's player number, filled by the current Queen
        //    robot if necessary
        // - normaly it is -1 (no target)
        // - for safety reasons this should always be combined with check on
        //     Queen role
        public byte passTarget;

        // timestamp of "last jumped"
        // - "last jumped" describes a situation, when the robots self localisation
        //   corrects for an bigger update than normal
        @Primitive("uint")
        public long timestampLastJumped;

        // whistle recognition stuff
        @Reader(HearingConfidenceReader.class)
        public HearingConfidence confidenceOfLastWhistleDetection; //< confidence based on hearing capability
        @Primitive("uint")
        public long lastTimeWhistleDetected; //< timestamp

        // the obstacles from the private obstacle model
        @ListCountSize(1)
        public List<Obstacle> obstacles;

        // is this robot requesting an ntp message?
        public boolean requestsNTPMessage;
        // all ntp-message this robot sends to his teammates in response to their requests
        @ListCountSize(1)
        public List<BNTPMessage> ntpMessages;
    }

    public static class BHumanStandardMessagePart extends StreamedObject<BHumanStandardMessagePart> {

        @Primitive("uchar")
        public short magicNumber;

        @Primitive("uint")
        public long ballTimeWhenDisappearedSeenPercentage;

        public short ballLastPerceptX;
        public short ballLastPerceptY;
        public float[] ballCovariance = new float[3];
        public float robotPoseDeviation;
        public float[] robotPoseCovariance = new float[6];
        @Primitive("uchar")
        public short robotPoseValidity;

    }
}
