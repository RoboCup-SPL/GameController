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
    public static final short BHUMAN_STANDARD_MESSAGE_STRUCT_VERSION = 15;

    public enum SetPlay__Type {
        none,
        backPassKickOff,
        directKickOff,
        passKickOff,
        diamondKickOff,
        arrowKickOff,
        yKickOff,
        theOneTrueOwnPenaltyKick,
        theOneTrueOpponentPenaltyKick,
        passFreeKick,
        cornerKick,
        placeholder,
        UNKNOWN
    }

    public enum Role__Type {
        none,
        playBall,
        freeKickWall,
        closestToTeammatesBall,
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
        public boolean isUpright;
        public Timestamp timeWhenLastUpright;
        public byte[] sequenceNumbers = new byte[7];

        public void read(final BitStream bitStream, final long __timestampBase) {
            isUpright = bitStream.readBoolean();
            timeWhenLastUpright = bitStream.readTimestamp(__timestampBase, 8, 6, -1, false);
            final int _sequenceNumbersSize = 7;
            for (int i = 0; i < _sequenceNumbersSize; ++i) {
                byte _sequenceNumbers;
                _sequenceNumbers = bitStream.readSignedChar(-1, 14, 4);
                sequenceNumbers[i] = _sequenceNumbers;
            }
        }
    }

    public short magicNumber;
    public long timestamp;
    public long referenceGameControllerPacketTimestamp;
    public short referenceGameControllerPacketNumber;

    public RobotStatus theRobotStatus = new RobotStatus();
    public RobotPose theRobotPose = new RobotPose();
    public FrameInfo theFrameInfo = new FrameInfo();
    public BallModel theBallModel = new BallModel();
    public ObstacleModel theObstacleModel = new ObstacleModel();
    public Whistle theWhistle = new Whistle();
    public BehaviorStatus theBehaviorStatus = new BehaviorStatus();
    public StrategyStatus theStrategyStatus = new StrategyStatus();

    public int getStreamedSize(final ByteBuffer stream) {
        int size = 1 + 4 + 4 + 1 + 2;
        if (stream.remaining() < size) {
            return size;
        }
        final int compressedSize = Unsigned.toUnsigned(stream.getShort(stream.position() + size - 2));
        return size + compressedSize;
    }

    public BHumanStandardMessage read(final ByteBuffer stream, byte playerNumber) {
        magicNumber = Unsigned.toUnsigned(stream.get());
        timestamp = Unsigned.toUnsigned(stream.getInt());
        referenceGameControllerPacketTimestamp = Unsigned.toUnsigned(stream.getInt());
        referenceGameControllerPacketNumber = Unsigned.toUnsigned(stream.get());
        final int containerSize = Unsigned.toUnsigned(stream.getShort());
        final int positionAfterCompressed = stream.position() + containerSize;
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
        assert stream.position() == positionAfterCompressed;
        return this;
    }
}
