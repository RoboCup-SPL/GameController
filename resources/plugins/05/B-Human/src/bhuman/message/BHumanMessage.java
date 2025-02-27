package bhuman.message;

import bhuman.message.data.Angle;
import bhuman.message.data.BitStream;
import bhuman.message.data.Eigen;
import bhuman.message.data.Timestamp;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import teamcomm.data.AdvancedMessage;
import util.Unsigned;

/**
 * This class was generated automatically. DO NOT EDIT!
 */
public class BHumanMessage extends AdvancedMessage {
    public static final short BHUMAN_MESSAGE_STRUCT_VERSION = 72;

    public static class IndirectKick {
        public Timestamp lastKickTimestamp;

        public void read(final BitStream __bitStream, final long __timestampBase) {
            lastKickTimestamp = __bitStream.readTimestamp(__timestampBase, 24, 8, -1, true);
        }
    }

    public enum Role__Type {
        none,
        playBall,
        freeKickWall,
        closestToTeamBall,
        startSetPlay,
        goalkeeper,
        attackingGoalkeeper,
        defender,
        midfielder,
        forward,
        UNKNOWN
    }

    public enum Tactic__Position__Type {
        none,
        goalkeeper,
        attackingGoalkeeper,
        defender,
        defenderL,
        defenderR,
        midfielder,
        midfielderM,
        midfielderL,
        midfielderR,
        forward,
        forwardM,
        forwardL,
        forwardR,
        UNKNOWN
    }

    public enum SetPlay__Type {
        none,
        directKickOff,
        directKickOff5v5,
        kiteKickOff,
        kiteKickOffensive,
        diamondKickOff5v5,
        arrowKickOff5v5,
        theOneTrueOwnPenaltyKick,
        theOneTrueOwnPenaltyKickAttacking,
        ownPenaltyKick5v5,
        theOneTrueOpponentPenaltyKick,
        opponentPenaltyKick5v5,
        ownCornerKick,
        ownCornerKickAttacking,
        ownGoalKick,
        ownKickInOwnHalf,
        ownKickInOpponentHalf,
        passFreeKick5v5,
        cornerKick5v5,
        opponentCornerKick,
        placeholder5v5,
        UNKNOWN
    }

    public enum Tactic__Type {
        none,
        t123,
        t222,
        t033,
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

        public void read(final BitStream __bitStream, final long __timestampBase) {
            proposedTactic = Tactic__Type.values()[Math.min((int) __bitStream.readBits(3), Tactic__Type.values().length - 1)];
            acceptedTactic = Tactic__Type.values()[Math.min((int) __bitStream.readBits(3), Tactic__Type.values().length - 1)];
            proposedMirror = __bitStream.readBoolean();
            acceptedMirror = __bitStream.readBoolean();
            proposedSetPlay = SetPlay__Type.values()[Math.min((int) __bitStream.readBits(5), SetPlay__Type.values().length - 1)];
            acceptedSetPlay = SetPlay__Type.values()[Math.min((int) __bitStream.readBits(5), SetPlay__Type.values().length - 1)];
            position = Tactic__Position__Type.values()[Math.min((int) __bitStream.readBits(4), Tactic__Position__Type.values().length - 1)];
            role = Role__Type.values()[Math.min((int) __bitStream.readBits(4), Role__Type.values().length - 1)];
        }
    }

    public static class RecentWhistle {
        public float confidenceOfLastWhistleDetection;
        public Timestamp lastTimeWhistleDetected;

        public void read(final BitStream __bitStream, final long __timestampBase) {
            confidenceOfLastWhistleDetection = __bitStream.readFloat(0, 2.55, 8);
            lastTimeWhistleDetected = __bitStream.readTimestamp(__timestampBase, 8, 4, -1, true);
        }
    }

    public static class WhistleCompact {
        public boolean listening;
        public List<RecentWhistle> recentWhistle;

        public void read(final BitStream __bitStream, final long __timestampBase) {
            listening = __bitStream.readBoolean();
            final int _recentWhistleSize = (int) (__bitStream.readBits(1));
            recentWhistle = new ArrayList<>(_recentWhistleSize);
            for (int i = 0; i < _recentWhistleSize; ++i) {
                RecentWhistle _recentWhistle = new RecentWhistle();
                _recentWhistle.read(__bitStream, __timestampBase);
                recentWhistle.add(_recentWhistle);
            }
        }
    }

    public static class BehaviorStatus {
        public int passTarget;
        public Eigen.Vector2f walkingTo = new Eigen.Vector2f();
        public float speed;
        public List<Eigen.Vector2f> shootingTo;

        public void read(final BitStream __bitStream, final long __timestampBase) {
            passTarget = __bitStream.readInt(-1, 14, 4);
            walkingTo.x = __bitStream.readFloat(-12800, 12800, 11);
            walkingTo.y = __bitStream.readFloat(-12800, 12800, 11);
            speed = __bitStream.readFloat(0, 310, 5);
            final int _shootingToSize = (int) (__bitStream.readBits(1));
            shootingTo = new ArrayList<>(_shootingToSize);
            for (int i = 0; i < _shootingToSize; ++i) {
                Eigen.Vector2f _shootingTo = new Eigen.Vector2f();
                _shootingTo.x = __bitStream.readFloat(-12800, 12800, 11);
                _shootingTo.y = __bitStream.readFloat(-12800, 12800, 11);
                shootingTo.add(_shootingTo);
            }
        }
    }

    public static class BallState {
        public Eigen.Vector2f position = new Eigen.Vector2f();
        public Eigen.Vector2f velocity = new Eigen.Vector2f();

        public void read(final BitStream __bitStream, final long __timestampBase) {
            position.x = __bitStream.readFloat(-12800, 12800, 11);
            position.y = __bitStream.readFloat(-12800, 12800, 11);
            velocity.x = __bitStream.readFloat(-8000, 8000, 11);
            velocity.y = __bitStream.readFloat(-8000, 8000, 11);
        }
    }

    public static class BallModel {
        public BallState estimate = new BallState();
        public Timestamp timeWhenLastSeen;
        public Timestamp timeWhenDisappeared;

        public void read(final BitStream __bitStream, final long __timestampBase) {
            estimate.read(__bitStream, __timestampBase);
            timeWhenLastSeen = __bitStream.readTimestamp(__timestampBase, 11, 4, -1, true);
            timeWhenDisappeared = __bitStream.readTimestamp(__timestampBase, 9, 6, -1, true);
        }
    }

    public static class FrameInfo {
        public Timestamp time;

        public void read(final BitStream __bitStream, final long __timestampBase) {
            time = __bitStream.readTimestamp(__timestampBase, 0, 0, -1, false);
        }
    }

    public enum RefereeGesture__Gesture {
        none,
        kickInLeft,
        kickInRight,
        goalKickLeft,
        goalKickRight,
        cornerKickLeft,
        cornerKickRight,
        goalLeft,
        goalRight,
        pushingFreeKickLeft,
        pushingFreeKickRight,
        fullTime,
        substitution,
        ready,
        UNKNOWN
    }

    public static class RefereeSignal {
        public RefereeGesture__Gesture signal;
        public Timestamp timeWhenDetected;

        public void read(final BitStream __bitStream, final long __timestampBase) {
            signal = RefereeGesture__Gesture.values()[Math.min((int) __bitStream.readBits(4), RefereeGesture__Gesture.values().length - 1)];
            timeWhenDetected = __bitStream.readTimestamp(__timestampBase, 11, 4, -1, true);
        }
    }

    public static class RobotStatus {
        public boolean isUpright;
        public Timestamp timeWhenLastUpright;

        public void read(final BitStream __bitStream, final long __timestampBase) {
            isUpright = __bitStream.readBoolean();
            timeWhenLastUpright = __bitStream.readTimestamp(__timestampBase, 8, 6, -1, false);
        }
    }

    public enum RobotPose__LocalizationQuality {
        superb,
        okay,
        poor,
        UNKNOWN
    }

    public static class RobotPoseCompact {
        public Angle rotation;
        public Eigen.Vector2f translation = new Eigen.Vector2f();
        public RobotPose__LocalizationQuality quality;
        public Eigen.ColumnMatrix<Short> covariance = new Eigen.ColumnMatrix<Short>();
        public Timestamp timestampLastJump;

        @SuppressWarnings("unchecked")
        public void read(final BitStream __bitStream, final long __timestampBase) {
            rotation = __bitStream.readAngle(8);
            translation.x = __bitStream.readFloat(-5120, 5110, 10);
            translation.y = __bitStream.readFloat(-5120, 5110, 10);
            quality = RobotPose__LocalizationQuality.values()[Math.min((int) __bitStream.readBits(2), RobotPose__LocalizationQuality.values().length - 1)];
            covariance.cols = new Eigen.Vector[2];
            for (int i = 0; i < 2; ++i) {
                covariance.cols[i] = new Eigen.Vector<>();
                covariance.cols[i].elems = new Short[2];
                for (int j = 0; j < i; ++j) {
                    covariance.cols[i].elems[j] = covariance.cols[j].elems[i];
                }
                for (int j = i; j < 2; ++j) {
                    short _covariance;
                    _covariance = __bitStream.readShort(-32768, 32767, 16);
                    covariance.cols[i].elems[j] = _covariance;
                }
            }
            timestampLastJump = __bitStream.readTimestamp(__timestampBase, 1, 10, -1, true);
        }
    }

    public short magicNumber;
    public short playerNumber;
    public byte maxJointTemperatureStatus;
    public long timestamp;
    public int referenceGameControllerPacketTimestampOffset;
    public short referenceGameControllerPacketNumber;

    public RobotPoseCompact theRobotPose = new RobotPoseCompact();
    public RobotStatus theRobotStatus = new RobotStatus();
    public FrameInfo theFrameInfo = new FrameInfo();
    public BallModel theBallModel = new BallModel();
    public WhistleCompact theWhistle = new WhistleCompact();
    public BehaviorStatus theBehaviorStatus = new BehaviorStatus();
    public StrategyStatus theStrategyStatus = new StrategyStatus();
    public IndirectKick theIndirectKick = new IndirectKick();
    public RefereeSignal theRefereeSignal = new RefereeSignal();

    @Override
    public String[] display() {
        return new String[]{};
    }

    @Override
    public void init() {
        ByteBuffer __stream = ByteBuffer.wrap(data);
        __stream.rewind();
        __stream.order(ByteOrder.LITTLE_ENDIAN);
        magicNumber = (short) (Unsigned.toUnsigned(__stream.get()) ^ BHUMAN_MESSAGE_STRUCT_VERSION);
        playerNumber = Unsigned.toUnsigned(__stream.get());
        maxJointTemperatureStatus = (byte) (playerNumber >> 4 & 3);
        playerNumber &= 15;
        timestamp = Unsigned.toUnsigned(__stream.getShort());
        timestamp |= Unsigned.toUnsigned(__stream.get()) << 16;
        referenceGameControllerPacketTimestampOffset = Unsigned.toUnsigned(__stream.getShort());
        referenceGameControllerPacketNumber = Unsigned.toUnsigned(__stream.get());
        final BitStream __bitStream = new BitStream(__stream);
        final long __timestampBase = timestamp;
        theRobotPose.read(__bitStream, __timestampBase);
        theRobotStatus.read(__bitStream, __timestampBase);
        theFrameInfo.read(__bitStream, __timestampBase);
        theBallModel.read(__bitStream, __timestampBase);
        theWhistle.read(__bitStream, __timestampBase);
        theBehaviorStatus.read(__bitStream, __timestampBase);
        theStrategyStatus.read(__bitStream, __timestampBase);
        theIndirectKick.read(__bitStream, __timestampBase);
        theRefereeSignal.read(__bitStream, __timestampBase);
    }
}
