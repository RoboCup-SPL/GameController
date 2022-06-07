"""
Library to perform conversion between binary udp data and a meaningful python object.

Example of sending return data to the GameController::

    import socket
    from construct import Container
    from robocup_game_control_data import GAMECONTROLLER_DATA_PORT
    from robocup_game_control_return_data import GAMECONTROLLER_RETURN_PORT, RoboCupGameControlReturnData

    # Setup UDP client
    with socket.socket(socket.AF_INET, socket.SOCK_DGRAM, socket.IPPROTO_UDP) as client:
        # Listen to single GC packet to determine its address
        client.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        client.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
        client.bind(('', GAMECONTROLLER_DATA_PORT))
        _, (address, _) = client.recvfrom(1024)

        # Create sample binary data packet
        container = Container(
            teamNum=5,
            playerNum=5,
        )
        data = RoboCupGameControlReturnData.build(container)

        # Return data directly to the GameController's address and return port
        client.sendto(data, (address, GAMECONTROLLER_RETURN_PORT))
"""

from construct import Array, Byte, Const, Default, Float32l, Struct

GAMECONTROLLER_RETURN_PORT = 3939

GAMECONTROLLER_RETURN_STRUCT_HEADER = b'RGrt'
GAMECONTROLLER_RETURN_STRUCT_VERSION = 4

RoboCupGameControlReturnData = Struct(
    'header' / Const(GAMECONTROLLER_RETURN_STRUCT_HEADER),  # "RGrt"
    'version' / Const(GAMECONTROLLER_RETURN_STRUCT_VERSION, Byte),  # has to be set to GAMECONTROLLER_RETURN_STRUCT_VERSION
    'playerNum' / Default(Byte, 0),  # player number starts with 1
    'teamNum' / Default(Byte, 0),  # team number
    'fallen' / Default(Byte, 255),  # 1 means that the robot is fallen, 0 means that the robot can play
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
    'ball' / Default(Array(2, Float32l), [0, 0])
)
