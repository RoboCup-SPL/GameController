"""
Library to perform conversion between binary udp data and a meaningful python object

Example of receiving data from teammate robots::

    from spl_standard_message import SPLStandardMessage
    import socket

    teamNum = 2  # Change this to your team's number

    # Setup UDP client
    client = socket.socket(
        socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP)  # UDP
    client.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEPORT, 1)
    client.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
    client.bind(('', 10000 + teamNum))  # Port number is specified in SPL rulebook

    # Receive data
    data, _ = client.recvfrom(1024)

    # Parse it
    parsed = SPLStandardMessage.parse(data)

    # Accessing data
    print('playerNum: ', parsed.playerNum)

Example of sending data to teammate robots::

    import socket
    from construct import Container
    from spl_standard_message import SPLStandardMessage

    teamNum = 2  # Change this to your team's number

    # Setup UDP client
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP) as client:
        client.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        client.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

        # Create sample binary data packet
        container = Container(
            teamNum=teamNum,
            playerNum=5,
        )
        data = SPLStandardMessage.build(container)

        # Broadcast data to team communication port
        client.sendto(data, ('', 10000 + teamNum))  # Port number is specified in SPL rulebook
"""

from construct import Array, Byte, Const, Default, Float32l, Int8ul, Struct

SPL_STANDARD_MESSAGE_STRUCT_HEADER = b'SPL '
SPL_STANDARD_MESSAGE_STRUCT_VERSION = 9

SPL_STANDARD_MESSAGE_DATA_SIZE = 128

# Important remarks about units:
#   For each parameter, the respective comments describe its unit.
#   The following units are used:
#     - Distances:  Millimeters (mm)
#     - Angles:     Radian
#     - Time:       Seconds (s)
SPLStandardMessage = Struct(
    'header' / Const(SPL_STANDARD_MESSAGE_STRUCT_HEADER),  # "SPL "
    'version' / Const(SPL_STANDARD_MESSAGE_STRUCT_VERSION, Byte),  # has to be set to SPL_STANDARD_MESSAGE_STRUCT_VERSION
    'playerNum' / Default(Byte, 0),  # [MANDATORY FIELD] 1-20
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
    'numOfDataBytes' / Default(Int8ul, 0),
    # buffer for arbitrary data, teams do not need to send more than specified in numOfDataBytes
    'data' / Default(Array(SPL_STANDARD_MESSAGE_DATA_SIZE, Byte), [0]*SPL_STANDARD_MESSAGE_DATA_SIZE)
)
