
SPL_STANDARD_MESSAGE_STRUCT_HEADER = "SPL "
SPL_STANDARD_MESSAGE_STRUCT_VERSION = 7

# Minimal MTU a network can set is 576 byte.
# We have to subtract the IP header of 60 bytes and the UDP data 8 bytes.
# So we have 576 - 60 - 8 = 508 safe size. From this we have to subtract the prefix to the data - 34 bytes.
# So we have in the end 508 - 34 = 474 bytes free payload.
# See also https://stackoverflow.com/a/23915324
SPL_STANDARD_MESSAGE_DATA_SIZE = 474

# Important remarks about units:
#   For each parameter, the respective comments describe its unit.
#   The following units are used:
#     - Distances:  Millimeters (mm)
#     - Angles:     Radian
#     - Time:       Seconds (s)
SPLStandardMessage = Struct(
    'header' / Const(SPL_STANDARD_MESSAGE_STRUCT_HEADER),  # "SPL "
    'version' / Const(SPL_STANDARD_MESSAGE_STRUCT_VERSION, Byte),  # has to be set to SPL_STANDARD_MESSAGE_STRUCT_VERSION
    'playerNum' / Default(Byte, 0),  # [MANDATORY FIELD] 1-7
    'teamNum' / Default(Byte, 0),  # [MANDATORY FIELD] the number of the team (as provided by the organizers)
    'fallen' / Default(Byte, 255),  # [MANDATORY FIELD] 1 means that the robot is fallen, 0 means that the robot can play
    # [MANDATORY FIELD]
    # position and orientation of robot
    # coordinates in millimeters
    # 0,0 is in center of field
    # +ve x-axis points towards the goal we are attempting to score on
    # +ve y-axis is 90 degrees counter clockwise from the +ve x-axis
    # angle in radians, 0 along the +x axis, increasing counter clockwise
    'pose' / Default(Array(3, Float32l), [0, 0, 0]),  # x,y,theta
    # ball information
    'ballAge' / Default(Float32l, -1),  # seconds since this robot last saw the ball. -1.f if we haven't seen it
    # position of ball relative to the robot
    # coordinates in millimeters
    # 0,0 is in center of the robot
    # +ve x-axis points forward from the robot
    # +ve y-axis is 90 degrees counter clockwise from the +ve x-axis
    'ball' / Default(Array(2, Float32l), [0, 0]),
    # number of bytes that is actually used by the data array
    'numOfDataBytes' / Default(Int16ul, 0),
    # buffer for arbitrary data, teams do not need to send more than specified in numOfDataBytes
    'data' / Default(Array(SPL_STANDARD_MESSAGE_DATA_SIZE, Byte), [0]*SPL_STANDARD_MESSAGE_DATA_SIZE)
)