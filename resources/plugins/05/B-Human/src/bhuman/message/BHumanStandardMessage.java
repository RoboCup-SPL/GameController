package bhuman.message;

import bhuman.message.data.Angle;
import bhuman.message.data.BitStream;
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
public class BHumanStandardMessage {
    public static final String BHUMAN_STANDARD_MESSAGE_STRUCT_HEADER = "BHUM";
    public static final short BHUMAN_STANDARD_MESSAGE_STRUCT_VERSION = 14;

    public static class BNTPMessage {
        public long requestOrigination;
        public long requestReceipt;
        public short receiver;
    }

    public enum PlayerRole__RoleType {
        none,
        goalkeeper,
        ballPlayer,
        supporter0,
        supporter1,
        supporter2,
        supporter3,
        supporter4,
        UNKNOWN
    }

    public static class PlayerRole {
        public PlayerRole__RoleType role;

        public void read(final BitStream bitStream, final long __timestampBase) {
            role = PlayerRole__RoleType.values()[Math.min((int) bitStream.readBits(3), PlayerRole__RoleType.values().length - 1)];
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

    public static class TimeToReachBall {
        public Timestamp timeWhenReachBall;
        public Timestamp timeWhenReachBallStriker;

        public void read(final BitStream bitStream, final long __timestampBase) {
            timeWhenReachBall = bitStream.readTimestamp(__timestampBase, 16, 3, 1, false);
            timeWhenReachBallStriker = bitStream.readTimestamp(__timestampBase, 16, 3, 1, false);
        }
    }

    public enum TeamBehaviorStatus__TeamActivity {
        noTeam,
        penaltyShootoutTeam,
        kickoffTeam,
        playingTeam,
        defendFreeKickNearGoalTeam,
        ownPenaltyKickTeam,
        opponentPenaltyKickTeam,
        UNKNOWN
    }

    public static class TeamBehaviorStatus {
        public TeamBehaviorStatus__TeamActivity teamActivity;
        public TimeToReachBall timeToReachBall = new TimeToReachBall();
        public TeammateRoles teammateRoles = new TeammateRoles();
        public PlayerRole role = new PlayerRole();

        public void read(final BitStream bitStream, final long __timestampBase) {
            teamActivity = TeamBehaviorStatus__TeamActivity.values()[Math.min((int) bitStream.readBits(3), TeamBehaviorStatus__TeamActivity.values().length - 1)];
            timeToReachBall.read(bitStream, __timestampBase);
            teammateRoles.read(bitStream, __timestampBase);
            role.read(bitStream, __timestampBase);
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

    public enum SetPlay__Type {
        none,
        directKickOff,
        passKickOff,
        diamondKickOff,
        theOneTrueOwnPenaltyKick,
        theOneTrueOpponentPenaltyKick,
        passFreeKick,
        cornerKick,
        placeholder,
        UNKNOWN
    }

    public enum Role__Type {
        none,
        active,
        goalkeeper,
        defender,
        midfielder,
        forward,
        UNKNOWN
    }

    public enum Tactic__Position__Type {
        none,
        goalkeeper,
        defender,
        defenderL,
        defenderR,
        midfielder,
        midfielderL,
        midfielderR,
        forward,
        forwardL,
        forwardR,
        UNKNOWN
    }

    public enum Tactic__Type {
        none,
        t211,
        t121,
        t112,
        UNKNOWN
    }

    public static class StrategyStatus {
        public Tactic__Type proposedTactic;
        public Tactic__Type acceptedTactic;
        public boolean proposedMirror;
        public boolean acceptedMirror;
        public SetPlay__Type proposedSetPlay;
        public SetPlay__Type acceptedSetPlay;
        public Tactic__Position__Type position;
        public Role__Type role;

        public void read(final BitStream bitStream, final long __timestampBase) {
            proposedTactic = Tactic__Type.values()[Math.min((int) bitStream.readBits(8), Tactic__Type.values().length - 1)];
            acceptedTactic = Tactic__Type.values()[Math.min((int) bitStream.readBits(8), Tactic__Type.values().length - 1)];
            proposedMirror = bitStream.readBoolean();
            acceptedMirror = bitStream.readBoolean();
            proposedSetPlay = SetPlay__Type.values()[Math.min((int) bitStream.readBits(8), SetPlay__Type.values().length - 1)];
            acceptedSetPlay = SetPlay__Type.values()[Math.min((int) bitStream.readBits(8), SetPlay__Type.values().length - 1)];
            position = Tactic__Position__Type.values()[Math.min((int) bitStream.readBits(4), Tactic__Position__Type.values().length - 1)];
            role = Role__Type.values()[Math.min((int) bitStream.readBits(3), Role__Type.values().length - 1)];
        }
    }

    public enum BehaviorStatus__Activity {
        unknown,
        avoidCenterCircle,
        kickoffStriker,
        keeperKickoffPositioning,
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
        distantKick,
        kickToOwnRobot,
        tapFreeKickLeft,
        tapFreeKickRight,
        guardGoal,
        keeperCatchBall,
        keeperSearchForBall,
        dribbleToGoal,
        duel,
        handleBallAtOwnGoalPost,
        kickAtGoal,
        passUpfield,
        clearBall,
        waitForFreeKick,
        waitForPass,
        blockAfterBallLost,
        defendGoal,
        waitUpfield,
        walkNextToStriker,
        catchBall,
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
        penaltyKickStriker,
        penaltyKickSupporter,
        penaltyKickDefender,
        penaltyKickKeeper,
        demo,
        calibrationFinished,
        UNKNOWN
    }

    public static class BehaviorStatus {
        public BehaviorStatus__Activity activity;
        public int passTarget;
        public Eigen.Vector2f walkingTo = new Eigen.Vector2f();
        public float speed;
        public Eigen.Vector2f shootingTo = new Eigen.Vector2f();

        public void read(final BitStream bitStream, final long __timestampBase) {
            activity = BehaviorStatus__Activity.values()[Math.min((int) bitStream.readBits(8), BehaviorStatus__Activity.values().length - 1)];
            passTarget = bitStream.readInt(-1, 14, 4);
            walkingTo.x = bitStream.readFloat(-32768, 32767, 16);
            walkingTo.y = bitStream.readFloat(-32768, 32767, 16);
            speed = bitStream.readFloat(-500, 500, 16);
            shootingTo.x = bitStream.readFloat(-32768, 32767, 16);
            shootingTo.y = bitStream.readFloat(-32768, 32767, 16);
        }
    }

    public static class BallState {
        public Eigen.Vector2f position = new Eigen.Vector2f();
        public Eigen.Vector2f velocity = new Eigen.Vector2f();
        public Eigen.ColumnMatrix<Float> covariance = new Eigen.ColumnMatrix<Float>();

        @SuppressWarnings("unchecked")
        public void read(final BitStream bitStream, final long __timestampBase) {
            position.x = bitStream.readFloat(-32768, 32767, 16);
            position.y = bitStream.readFloat(-32768, 32767, 16);
            velocity.x = bitStream.readFloat(-32768, 32767, 16);
            velocity.y = bitStream.readFloat(-32768, 32767, 16);
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

    public static class FrameInfo {
        public Timestamp time;

        public void read(final BitStream bitStream, final long __timestampBase) {
            time = bitStream.readTimestamp(__timestampBase, 8, 0, -1, false);
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

    public enum RobotPose__LocalizationQuality {
        superb,
        okay,
        poor,
        UNKNOWN
    }

    public static class RobotPose {
        public Angle rotation;
        public Eigen.Vector2f translation = new Eigen.Vector2f();
        public RobotPose__LocalizationQuality quality;
        public Eigen.ColumnMatrix<Float> covariance = new Eigen.ColumnMatrix<Float>();
        public Timestamp timestampLastJump;

        @SuppressWarnings("unchecked")
        public void read(final BitStream bitStream, final long __timestampBase) {
            rotation = bitStream.readAngle(8);
            translation.x = bitStream.readFloat(-32768, 32767, 16);
            translation.y = bitStream.readFloat(-32768, 32767, 16);
            quality = RobotPose__LocalizationQuality.values()[Math.min((int) bitStream.readBits(2), RobotPose__LocalizationQuality.values().length - 1)];
            covariance.cols = new Eigen.Vector[3];
            for (int i = 0; i < 3; ++i) {
                covariance.cols[i] = new Eigen.Vector<>();
                covariance.cols[i].elems = new Float[3];
                for (int j = 0; j < i; ++j) {
                    covariance.cols[i].elems[j] = covariance.cols[j].elems[i];
                }
                for (int j = i; j < 3; ++j) {
                    float _covariance;
                    _covariance = bitStream.readFloat(0, 0, 0);
                    covariance.cols[i].elems[j] = _covariance;
                }
            }
            timestampLastJump = bitStream.readTimestamp(__timestampBase, 8, 7, -1, true);
        }
    }

    public static class RobotStatus {
        public boolean isPenalized;
        public boolean isUpright;
        public boolean hasGroundContact;
        public Timestamp timeWhenLastUpright;
        public Timestamp timeOfLastGroundContact;
        public byte[] sequenceNumbers = new byte[6];

        public void read(final BitStream bitStream, final long __timestampBase) {
            isPenalized = bitStream.readBoolean();
            isUpright = bitStream.readBoolean();
            hasGroundContact = bitStream.readBoolean();
            timeWhenLastUpright = bitStream.readTimestamp(__timestampBase, 8, 6, -1, false);
            timeOfLastGroundContact = bitStream.readTimestamp(__timestampBase, 8, 6, -1, false);
            final int _sequenceNumbersSize = 6;
            for (int i = 0; i < _sequenceNumbersSize; ++i) {
                byte _sequenceNumbers;
                _sequenceNumbers = bitStream.readSignedChar(-1, 14, 4);
                sequenceNumbers[i] = _sequenceNumbers;
            }
        }
    }

    public short magicNumber;
    public long timestamp;
    public boolean requestsNTPMessage;
    public List<BNTPMessage> ntpMessages;

    public RobotStatus theRobotStatus = new RobotStatus();
    public RobotPose theRobotPose = new RobotPose();
    public FrameInfo theFrameInfo = new FrameInfo();
    public BallModel theBallModel = new BallModel();
    public ObstacleModel theObstacleModel = new ObstacleModel();
    public Whistle theWhistle = new Whistle();
    public BehaviorStatus theBehaviorStatus = new BehaviorStatus();
    public StrategyStatus theStrategyStatus = new StrategyStatus();
    public TeamBehaviorStatus theTeamBehaviorStatus = new TeamBehaviorStatus();
    public TeamTalk theTeamTalk = new TeamTalk();

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

    public BHumanStandardMessage read(final ByteBuffer stream, byte playerNumber) {
        magicNumber = Unsigned.toUnsigned(stream.get());
        timestamp = Unsigned.toUnsigned(stream.getInt());
        final int ntpAndSizeContainer = Unsigned.toUnsigned(stream.getShort());
        requestsNTPMessage = (ntpAndSizeContainer & (1 << 6)) != 0;
        ntpMessages = new LinkedList<>();
        long runner = 1 << 6;
        for (short i = 1; runner != 0; ++i) {
            if (i == playerNumber) {
                ++i;
            }
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
        theFrameInfo.read(bitStream, __timestampBase);
        theBallModel.read(bitStream, __timestampBase);
        theObstacleModel.read(bitStream, __timestampBase);
        theWhistle.read(bitStream, __timestampBase);
        theBehaviorStatus.read(bitStream, __timestampBase);
        theStrategyStatus.read(bitStream, __timestampBase);
        theTeamBehaviorStatus.read(bitStream, __timestampBase);
        theTeamTalk.read(bitStream, __timestampBase);
        assert stream.position() == positionAfterCompressed;
        return this;
    }
}
