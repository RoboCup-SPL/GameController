cmake_minimum_required(VERSION 2.8)
project(libgamectrl)

find_package(qibuild)

# Create a list of source files
set(_srcs
    GameCtrl.cpp
    UdpComm.h
    UdpComm.cpp)

# Create a plugin, that is a shared library, and make
# sure it is built in lib/naoqi
qi_create_lib(libgamectrl SHARED ${_srcs} SUBFOLDER naoqi)

# Tell CMake that mymodule depends on ALCOMMON.
# This will set the libraries to link mymodule with,
# the include paths, and so on
qi_use_lib(libgamectrl ALCOMMON BOOST)


# Add a simple test:
qi_use_lib(libgamectrl ALCOMMON)
