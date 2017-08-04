package bhuman.message;

import bhuman.message.data.Angle;
import bhuman.message.data.ComplexStreamReader;
import bhuman.message.data.NativeReaders;
import bhuman.message.data.Primitive;
import bhuman.message.data.SimpleStreamReader;
import bhuman.message.data.StreamedObject;
import bhuman.message.data.Timestamp;
import common.Log;
import data.SPLStandardMessage;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import util.Unsigned;

/**
 *
 * @author Felix Thielke
 */
public class BHumanMessageParts {

    private static final String BHULKS_STANDARD_MESSAGE_STRUCT_HEADER = "BHLK";
    private static final short BHULKS_STANDARD_MESSAGE_STRUCT_VERSION = 8;

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
            Log.error("Could not find BHULKs message struct header");
        }

        if (data.hasRemaining()) {
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
                Log.error("Wrong B-Human standard message struct header");
            }
        }

        if (data.hasRemaining()) {
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
                Log.error("Wrong B-Human arbitrary message struct header");
            }
        }

        this.bhulks = bhlk;
        this.bhuman = bhum;
        this.queue = q;
    }

    public static class BHULKsStandardMessagePart implements ComplexStreamReader<BHULKsStandardMessagePart> {

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
        }

        public static class Obstacle implements SimpleStreamReader<Obstacle> {

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

            public Timestamp timestampLastSeen; //< the name says it
            public ObstacleType type;          //< the name says it

            private final long baseTimestamp;

            public Obstacle(final long timestamp) {
                baseTimestamp = timestamp;
            }

            @Override
            public int getStreamedSize() {
                return 5;
            }

            @Override
            public Obstacle read(final ByteBuffer stream) {
                final int center0Struct = Unsigned.toUnsigned(stream.getShort());
                final int center1Struct = Unsigned.toUnsigned(stream.getShort());

                center[0] = (float) (short) (center0Struct << 2);
                center[1] = (float) (short) (center1Struct << 2);

                type = ObstacleType.values()[((center0Struct & 0xC000) >> 12) | ((center1Struct & 0xC000) >> 14)];

                timestampLastSeen = new Timestamp(baseTimestamp - (Unsigned.toUnsigned(stream.get()) << 6));

                return this;
            }
        }

        public static class BNTPMessage {

            public long requestOrigination;  //< The timestamp of the generation of the request
            public long requestReceipt;      //< The timestamp of the receipt of the request
            public short receiver;             //< The robot, to which this message should be sent
        }

        public Team member;

        public long timestamp;  //< the timestamp of this message

        public boolean isUpright;               //< The name says it all
        public boolean hasGroundContact;        //< The name says it all
        public Timestamp timeOfLastGroundContact; //< The name says it all

        // is the robot penalized?
        // Theoretically the game controller say it too, but this is for better information
        // spreading in case of bad WLAN quality in combination with PENALTY_MANUAL.
        public boolean isPenalized;

        // the current meassurement of head joint: HeadYaw
        public Angle headYawAngle;

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
        public Timestamp timeWhenReachBall;
        public Timestamp timeWhenReachBallQueen;

        // timestamp, when the ball was recognized
        // this is theoretically equal to SPLStandardMessage::ballAge, BUT it allows us to "ntp" it.
        public Timestamp ballTimeWhenLastSeen;

        // the pass target's player number, filled by the current Queen
        //    robot if necessary
        // - normaly it is -1 (no target)
        // - for safety reasons this should always be combined with check on
        //     Queen role
        public byte passTarget;

        // timestamp of "last jumped"
        // - "last jumped" describes a situation, when the robots self localisation
        //   corrects for an bigger update than normal
        public Timestamp timestampLastJumped;

        // whistle recognition stuff
        public HearingConfidence confidenceOfLastWhistleDetection; //< confidence based on hearing capability
        public Timestamp lastTimeWhistleDetected; //< timestamp

        // the obstacles from the private obstacle model
        public List<Obstacle> obstacles;

        // is this robot requesting an ntp message?
        public boolean requestsNTPMessage;
        // all ntp-message this robot sends to his teammates in response to their requests
        public List<BNTPMessage> ntpMessages;

        @Override
        public int getStreamedSize(final ByteBuffer stream) {
            int size = 4 // timestamp
                    + 1 // headYawAngle
                    + 1 // timeOfLastGroundContact
                    + 1 // timestampLastJumped
                    + 2 // timeWhenReachBall
                    + 2 // timeWhenReachBallQueen
                    + 4 // ballTimeWhenLastSeen
                    + 2 // whistle stuff
                    + 9 // gameControlData
                    + 4; // roleAssignments, currentlyPerfomingRole, passTarget

            if (stream.remaining() < size) {
                return size;
            }

            size += 1 + (Unsigned.toUnsigned(stream.get(stream.position() + size)) & 0x7) * new Obstacle(0).getStreamedSize(); // obstacles

            if (stream.remaining() < size) {
                return size;
            }

            int ntpReceivers = Unsigned.toUnsigned(stream.getShort(stream.position() + size)) & 0x3FF;
            int ntpCount = 0;
            while (ntpReceivers != 0) {
                if ((ntpReceivers & 1) == 1) {
                    ntpCount++;
                }
                ntpReceivers >>= 1;
            }
            return size
                    + 2 // member, isUpright, hasGroundContact, isPenalized, kingIsPlayingBall, requestsNTPMessage, \/
                    + ntpCount * 5;
        }

        @Override
        public BHULKsStandardMessagePart read(final ByteBuffer stream) {
            timestamp = Unsigned.toUnsigned(stream.getInt());

            headYawAngle = Angle.fromDegrees((double) stream.get());

            timeOfLastGroundContact = new Timestamp(timestamp - (((long) Unsigned.toUnsigned(stream.get())) << 6));
            timestampLastJumped = new Timestamp(timestamp - (((long) Unsigned.toUnsigned(stream.get())) << 7));

            timeWhenReachBall = new Timestamp(timestamp + (((long) Unsigned.toUnsigned(stream.getShort())) << 3));
            timeWhenReachBallQueen = new Timestamp(timestamp + (((long) Unsigned.toUnsigned(stream.getShort())) << 3));

            ballTimeWhenLastSeen = new Timestamp().read(stream);

            final int whistleDetectionContainer = Unsigned.toUnsigned(stream.getShort());
            confidenceOfLastWhistleDetection = HearingConfidence.values()[whistleDetectionContainer >> 14];
            lastTimeWhistleDetected = new Timestamp(timestamp - (long) (whistleDetectionContainer & 0x3FFF));

            // GameControlData is omitted
            stream.position(stream.position() + 9);

            final long roleContainer = Unsigned.toUnsigned(stream.getInt());
            long runner = 0x7 << ((BHULKS_STANDARD_MESSAGE_MAX_NUM_OF_PLAYERS - 1) * 3);
            for (int i = 0; i < BHULKS_STANDARD_MESSAGE_MAX_NUM_OF_PLAYERS; ++i, runner >>= 3) {
                roleAssignments[i] = Role.values()[(int) ((roleContainer & runner) >> ((BHULKS_STANDARD_MESSAGE_MAX_NUM_OF_PLAYERS - i - 1) * 3))];
            }

            final short numObsCurrPerforRolePassTargContainer = Unsigned.toUnsigned(stream.get());
            passTarget = (byte) (numObsCurrPerforRolePassTargContainer >> 4);
            currentlyPerfomingRole = Role.values()[(int) (((numObsCurrPerforRolePassTargContainer & 8) >> 1) | (roleContainer >> (BHULKS_STANDARD_MESSAGE_MAX_NUM_OF_PLAYERS * 3)))];

            final int numOfObstacles = numObsCurrPerforRolePassTargContainer & 0x7;
            obstacles = new ArrayList<>(numOfObstacles);
            for (int i = 0; i < numOfObstacles; i++) {
                obstacles.add(new Obstacle(timestamp).read(stream));
            }

            final int boolAndNTPReceiptContainer = Unsigned.toUnsigned(stream.getShort());
            runner = 1 << 15;
            member = (boolAndNTPReceiptContainer & runner) != 0 ? Team.BHUMAN : Team.HULKS;
            isUpright = (boolAndNTPReceiptContainer & (runner >>= 1)) != 0;
            hasGroundContact = (boolAndNTPReceiptContainer & (runner >>= 1)) != 0;
            isPenalized = (boolAndNTPReceiptContainer & (runner >>= 1)) != 0;
            kingIsPlayingBall = (boolAndNTPReceiptContainer & (runner >>= 1)) != 0;
            requestsNTPMessage = (boolAndNTPReceiptContainer & (runner >>= 1)) != 0;
            ntpMessages = new LinkedList<>();
            for (short i = 1; runner != 0; ++i) {
                if ((boolAndNTPReceiptContainer & (runner >>= 1)) != 0) {
                    final BNTPMessage message = new BNTPMessage();
                    message.receiver = i;
                    ntpMessages.add(message);
                    final long timeStruct32 = Unsigned.toUnsigned(stream.getInt());
                    final long timeStruct8 = (long) Unsigned.toUnsigned(stream.get());

                    message.requestOrigination = timeStruct32 & 0xFFFFFFF;
                    message.requestReceipt = timestamp - ((timeStruct32 >> 20) & 0xF00) | timeStruct8;
                }
            }

            return this;
        }
    }

    public static class BHumanStandardMessagePart extends StreamedObject<BHumanStandardMessagePart> {

        @Primitive("uchar")
        public short magicNumber;

        public Timestamp ballTimeWhenDisappearedSeenPercentage;

        public short ballLastPerceptX;
        public short ballLastPerceptY;
        public float[] ballCovariance = new float[3];
        public float robotPoseDeviation;
        public float[] robotPoseCovariance = new float[6];
        @Primitive("uchar")
        public short robotPoseValidity;

    }
}
