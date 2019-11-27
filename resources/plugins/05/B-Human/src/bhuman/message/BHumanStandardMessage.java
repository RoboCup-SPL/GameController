package bhuman.message;

import bhuman.message.data.Angle;
import bhuman.message.data.BitStream;
import bhuman.message.data.ComplexStreamReader;
import bhuman.message.data.Eigen;
import bhuman.message.data.Timestamp;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import util.Unsigned;

/**
 * This class was generated automatically. DO NOT EDIT!
 */
public class BHumanStandardMessage implements ComplexStreamReader<BHumanStandardMessage> {
    public static final String BHUMAN_STANDARD_MESSAGE_STRUCT_HEADER = "BHUM";
    public static final short BHUMAN_STANDARD_MESSAGE_STRUCT_VERSION = 13;

    public static class BNTPMessage {
        public long requestOrigination;
        public long requestReceipt;
        public short receiver;
    }

    public static class ExternStrategyInput {
        public float priority;
        public long externStrategy;

        public void read(final BitStream bitStream, final long __timestampBase) {
            priority = bitStream.readFloat(0, 0, 0);
            externStrategy = bitStream.readUnsignedInt(0, 0, 0);
        }
    }

    public enum SideConfidence__ConfidenceState {
        CONFIDENT,
        ALMOST_CONFIDENT,
        UNSURE,
        CONFUSED,
        UNKNOWN
    }

    public static class SideConfidence {
        public boolean mirror;
        public SideConfidence__ConfidenceState confidenceState;
        public List<Integer> agreeMates;

        public void read(final BitStream bitStream, final long __timestampBase) {
            mirror = bitStream.readBoolean();
            confidenceState = SideConfidence__ConfidenceState.values()[Math.min((int) bitStream.readBits(2), SideConfidence__ConfidenceState.values().length - 1)];
            final int _agreeMatesSize = (int) (bitStream.readBits(3));
            agreeMates = new ArrayList<>(_agreeMatesSize);
            for (int i = 0; i < _agreeMatesSize; ++i) {
                int _agreeMates;
                _agreeMates = bitStream.readInt(1, 6, 3);
                agreeMates.add(_agreeMates);
            }
        }
    }

    public enum Obstacle__Type {
        goalpost,
        unknown,
        someRobot,
        opponent,
        teammate,
        fallenSomeRobot,
        fallenOpponent,
        fallenTeammate,
        UNKNOWN
    }

    public static class Obstacle {
        public Eigen.ColumnMatrix<Float> covariance = new Eigen.ColumnMatrix<Float>();
        public Eigen.Vector2f center = new Eigen.Vector2f();
        public Eigen.Vector2f left = new Eigen.Vector2f();
        public Eigen.Vector2f right = new Eigen.Vector2f();
        public Timestamp lastSeen;
        public Obstacle__Type type;

        @SuppressWarnings("unchecked")
        public void read(final BitStream bitStream, final long __timestampBase) {
            covariance.cols = new Eigen.Vector[2];
            for (int i = 0; i < 2; ++i) {
                covariance.cols[i] = new Eigen.Vector<>();
                covariance.cols[i].elems = new Float[2];
                for (int j = 0; j < i; ++j) {
                    covariance.cols[i].elems[j] = covariance.cols[j].elems[i];
                }
                for (int j = i; j < 2; ++j) {
                    float _covariance;
                    _covariance = bitStream.readFloat(0, 0, 0);
                    covariance.cols[i].elems[j] = _covariance;
                }
            }
            center.x = bitStream.readFloat(-32768, 32767, 16);
            center.y = bitStream.readFloat(-32768, 32767, 16);
            left.x = bitStream.readFloat(-32768, 32767, 14);
            left.y = bitStream.readFloat(-32768, 32767, 14);
            right.x = bitStream.readFloat(-32768, 32767, 14);
            right.y = bitStream.readFloat(-32768, 32767, 14);
            lastSeen = bitStream.readTimestamp(__timestampBase, 8, 6, -1, false);
            type = Obstacle__Type.values()[Math.min((int) bitStream.readBits(3), Obstacle__Type.values().length - 1)];
        }
    }

    public static class ObstacleModel {
        public List<Obstacle> obstacles;

        public void read(final BitStream bitStream, final long __timestampBase) {
            final int _obstaclesSize = (int) (bitStream.readBits(3));
            obstacles = new ArrayList<>(_obstaclesSize);
            for (int i = 0; i < _obstaclesSize; ++i) {
                Obstacle _obstacles = new Obstacle();
                _obstacles.read(bitStream, __timestampBase);
                obstacles.add(_obstacles);
            }
        }
    }

    public static class RobotPose {
        public Angle rotation;
        public Eigen.Vector2f translation = new Eigen.Vector2f();
        public float validity;
        public float deviation;
        public Timestamp timestampLastJump;

        public void read(final BitStream bitStream, final long __timestampBase) {
            rotation = bitStream.readAngle(8);
            translation.x = bitStream.readFloat(-32768, 32767, 16);
            translation.y = bitStream.readFloat(-32768, 32767, 16);
            validity = bitStream.readFloat(0, 1, 8);
            deviation = bitStream.readFloat(0, 0, 0);
            timestampLastJump = bitStream.readTimestamp(__timestampBase, 8, 7, -1, true);
        }
    }

    public static class BallState {
        public Eigen.Vector2f position = new Eigen.Vector2f();
        public Eigen.Vector2f velocity = new Eigen.Vector2f();

        public void read(final BitStream bitStream, final long __timestampBase) {
            position.x = bitStream.readFloat(-32768, 32767, 16);
            position.y = bitStream.readFloat(-32768, 32767, 16);
            velocity.x = bitStream.readFloat(-32768, 32767, 16);
            velocity.y = bitStream.readFloat(-32768, 32767, 16);
        }
    }

    public static class BallModel {
        public Eigen.Vector2f lastPerception = new Eigen.Vector2f();
        public BallState estimate = new BallState();
        public Timestamp timeWhenLastSeen;
        public Timestamp timeWhenDisappeared;
        public short seenPercentage;

        public void read(final BitStream bitStream, final long __timestampBase) {
            lastPerception.x = bitStream.readFloat(-32768, 32767, 16);
            lastPerception.y = bitStream.readFloat(-32768, 32767, 16);
            estimate.read(bitStream, __timestampBase);
            timeWhenLastSeen = bitStream.readTimestamp(__timestampBase, 0, 0, 0, false);
            timeWhenDisappeared = bitStream.readTimestamp(__timestampBase, 0, 0, 0, false);
            seenPercentage = bitStream.readUnsignedChar(0, 100, 7);
        }
    }

    public static class TeamTalk {
        public short say;
        public Timestamp timestamp;

        public void read(final BitStream bitStream, final long __timestampBase) {
            say = bitStream.readChar(0, 255, 8);
            timestamp = bitStream.readTimestamp(__timestampBase, 8, 6, 1, true);
        }
    }

    public static class Whistle {
        public float confidenceOfLastWhistleDetection;
        public short channelsUsedForWhistleDetection;
        public Timestamp lastTimeWhistleDetected;

        public void read(final BitStream bitStream, final long __timestampBase) {
            confidenceOfLastWhistleDetection = bitStream.readFloat(0, 2.55, 8);
            channelsUsedForWhistleDetection = bitStream.readUnsignedChar(0, 4, 3);
            lastTimeWhistleDetected = bitStream.readTimestamp(__timestampBase, 16, 0, -1, true);
        }
    }

    public enum BehaviorStatus__Activity {
        unknown,
        avoidCenterCircle,
        kickoffStriker,
        keeperKickoffPositioning,
        brokenKeeperKickoffPositioning,
        defenderKickoffPositioning,
        supporterKickoffPositioning,
        strikerKickoffPositioning,
        blockingOffenseBlocker,
        blockingOffenseStriker,
        passingOffenseTarget,
        passingOffenseStriker,
        blockRobot,
        markRobot,
        buildWall,
        cornerKickToOwnRobot,
        goalFreeKick,
        kickIn,
        pushingFreeKick,
        brokenKeeper,
        guardGoal,
        keeperCatchBall,
        keeperClearBall,
        keeperSearchForBall,
        returnToGoal,
        dribbleToGoal,
        duel,
        handleBallAtOwnGoalPost,
        kickAtGoal,
        passUpfield,
        blockAfterBallLost,
        defendGoal,
        waitForCornerBall,
        waitForGoalFreeKick,
        waitForKickInBall,
        waitUpfield,
        walkNextToKeeper,
        walkNextToStriker,
        afterInterceptBall,
        catchBall,
        continueToOwnBall,
        fallen,
        finished,
        initial,
        lookAroundAfterPenalty,
        nearFieldSearchForBall,
        searchForBallAtBallPosition,
        searchForBallAtDropInPosition,
        searchForBallFieldCoverage,
        searchForBall,
        searchForBallTurnAround,
        set,
        turnToBallClosest,
        penaltyStriker,
        penaltyKeeper,
        demo,
        UNKNOWN
    }

    public static class BehaviorStatus {
        public BehaviorStatus__Activity activity;
        public int passTarget;
        public Eigen.Vector2f walkingTo = new Eigen.Vector2f();
        public Eigen.Vector2f shootingTo = new Eigen.Vector2f();

        public void read(final BitStream bitStream, final long __timestampBase) {
            activity = BehaviorStatus__Activity.values()[Math.min((int) bitStream.readBits(6), BehaviorStatus__Activity.values().length - 1)];
            passTarget = bitStream.readInt(-1, 14, 4);
            walkingTo.x = bitStream.readFloat(-32768, 32767, 16);
            walkingTo.y = bitStream.readFloat(-32768, 32767, 16);
            shootingTo.x = bitStream.readFloat(-32768, 32767, 16);
            shootingTo.y = bitStream.readFloat(-32768, 32767, 16);
        }
    }

    public static class RobotStatus {
        public boolean isPenalized;
        public boolean isUpright;
        public boolean hasGroundContact;
        public Timestamp timeOfLastGroundContact;

        public void read(final BitStream bitStream, final long __timestampBase) {
            isPenalized = bitStream.readBoolean();
            isUpright = bitStream.readBoolean();
            hasGroundContact = bitStream.readBoolean();
            timeOfLastGroundContact = bitStream.readTimestamp(__timestampBase, 8, 6, -1, false);
        }
    }

    public static class Role {
        public boolean isGoalkeeper;
        public boolean playBall;
        public int supporterIndex;

        public void read(final BitStream bitStream, final long __timestampBase) {
            isGoalkeeper = bitStream.readBoolean();
            playBall = bitStream.readBoolean();
            supporterIndex = bitStream.readInt(-1, 14, 4);
        }
    }

    public static class TeammateRoles {
        public List<Integer> roles;
        public int captain;
        public Timestamp timestamp;

        public void read(final BitStream bitStream, final long __timestampBase) {
            final int _rolesSize = (int) (bitStream.readBits(3));
            roles = new ArrayList<>(_rolesSize);
            for (int i = 0; i < _rolesSize; ++i) {
                int _roles;
                _roles = bitStream.readInt(-1, 14, 4);
                roles.add(_roles);
            }
            captain = bitStream.readInt(-1, 14, 4);
            timestamp = bitStream.readTimestamp(__timestampBase, 13, 0, -1, false);
        }
    }

    public enum TeamBehaviorStatus__TeamActivity {
        noTeam,
        penaltyShootoutTeam,
        kickoffTeam,
        playingTeam,
        defendFreeKickNearGoalTeam,
        UNKNOWN
    }

    public static class TimeToReachBall {
        public Timestamp timeWhenReachBall;
        public Timestamp timeWhenReachBallStriker;

        public void read(final BitStream bitStream, final long __timestampBase) {
            timeWhenReachBall = bitStream.readTimestamp(__timestampBase, 16, 3, 1, false);
            timeWhenReachBallStriker = bitStream.readTimestamp(__timestampBase, 16, 3, 1, false);
        }
    }

    public static class TeamBehaviorStatus {
        public TeamBehaviorStatus__TeamActivity teamActivity;
        public TimeToReachBall timeToReachBall = new TimeToReachBall();
        public TeammateRoles teammateRoles = new TeammateRoles();
        public Role role = new Role();

        public void read(final BitStream bitStream, final long __timestampBase) {
            teamActivity = TeamBehaviorStatus__TeamActivity.values()[Math.min((int) bitStream.readBits(3), TeamBehaviorStatus__TeamActivity.values().length - 1)];
            timeToReachBall.read(bitStream, __timestampBase);
            teammateRoles.read(bitStream, __timestampBase);
            role.read(bitStream, __timestampBase);
        }
    }

    public short magicNumber;
    public long timestamp;
    public boolean requestsNTPMessage;
    public List<BNTPMessage> ntpMessages;

    public RobotStatus theRobotStatus = new RobotStatus();
    public RobotPose theRobotPose = new RobotPose();
    public BallModel theBallModel = new BallModel();
    public SideConfidence theSideConfidence = new SideConfidence();
    public ObstacleModel theObstacleModel = new ObstacleModel();
    public Whistle theWhistle = new Whistle();
    public BehaviorStatus theBehaviorStatus = new BehaviorStatus();
    public TeamBehaviorStatus theTeamBehaviorStatus = new TeamBehaviorStatus();
    public ExternStrategyInput theExternStrategyInput = new ExternStrategyInput();
    public TeamTalk theTeamTalk = new TeamTalk();

    @Override
    public int getStreamedSize(final ByteBuffer stream) {
        int size = 1 + 4 + 2;
        if (stream.remaining() < size) {
            return size;
        }
        final int container = Unsigned.toUnsigned(stream.getShort(stream.position() + size - 2));
        int ntpReceivers = container & 0x3F;
        int ntpCount = 0;
        while (ntpReceivers != 0) {
            if ((ntpReceivers & 1) == 1) {
                ++ntpCount;
            }
            ntpReceivers >>= 1;
        }
        final int compressedSize = container >> 7;
        return size + ntpCount * 5 + compressedSize;
    }

    @Override
    public BHumanStandardMessage read(final ByteBuffer stream) {
        magicNumber = Unsigned.toUnsigned(stream.get());
        timestamp = Unsigned.toUnsigned(stream.getInt());
        final int ntpAndSizeContainer = Unsigned.toUnsigned(stream.getShort());
        requestsNTPMessage = (ntpAndSizeContainer & (1 << 6)) != 0;
        ntpMessages = new LinkedList<>();
        long runner = 1 << 6;
        for (short i = 1; runner != 0; ++i) {
            if ((ntpAndSizeContainer & (runner >>= 1)) != 0) {
                final BNTPMessage message = new BNTPMessage();
                message.receiver = i;
                ntpMessages.add(message);
                final long timeStruct32 = Unsigned.toUnsigned(stream.getInt());
                final long timeStruct8 = (long) Unsigned.toUnsigned(stream.get());
                message.requestOrigination = timeStruct32 & 0xFFFFFFF;
                message.requestReceipt = timestamp - ((timeStruct32 >> 20) & 0xF00) | timeStruct8;
            }
        }
        final int positionAfterCompressed = stream.position() + (ntpAndSizeContainer >> 7);
        final BitStream bitStream = new BitStream(stream);
        final long __timestampBase = timestamp;
        theRobotStatus.read(bitStream, __timestampBase);
        theRobotPose.read(bitStream, __timestampBase);
        theBallModel.read(bitStream, __timestampBase);
        theSideConfidence.read(bitStream, __timestampBase);
        theObstacleModel.read(bitStream, __timestampBase);
        theWhistle.read(bitStream, __timestampBase);
        theBehaviorStatus.read(bitStream, __timestampBase);
        theTeamBehaviorStatus.read(bitStream, __timestampBase);
        theExternStrategyInput.read(bitStream, __timestampBase);
        theTeamTalk.read(bitStream, __timestampBase);
        assert stream.position() == positionAfterCompressed;
        return this;
    }
}
