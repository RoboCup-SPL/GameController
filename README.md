# RoboCup SPL GameController Support Tools

These are the GameController support tools developed by team B-Human for
the RoboCup SPL. Originally, this repository contained the GameController
as well. It was used in the RoboCup SPL from 2013 to 2022. Since 2023,
[a new version](https://github.com/RoboCup-SPL/GameController3) is being used.

Please note that the Humanoid League currently uses a
[fork](https://github.com/RoboCup-Humanoid-TC/GameController).

The sources mentioned in some sections of this document are available at
[https://github.com/RoboCup-SPL/GameController](https://github.com/RoboCup-SPL/GameController).


### Acknowledgement

The development was partially supported by the RoboCup Federation within the
calls for Support for Projects for League Developments for 2013, 2015, 2017,
and 2018.


## 1. Building from Source

To build it from the source code you may use Apache Ant.
Just call "ant" in the main directory.

Building the source code requires the JDK 1.8 or newer.


## 2. GameStateVisualizer

As of the 2017 RoboCup competitions, the GameStateVisualizer has been replaced
by the GameStateVisualizer mode of the TeamCommunicationMonitor.

To start it, run

`java -jar TeamCommunicationMonitor.jar --gsv -l <league>`.

The parameter `-l` (or `--league`) is optional; if it is not given, the GSV
assumes a game in the SPL.


## 3. TeamCommunicationMonitor

The TeamCommunicationMonitor (TCM) is a tool for visualizing the data
communicated by robots during SPL games.

It serves two main purposes:

1. offering diagnostic data such as which robots are communicating on which
   team ports.
2. visualizing the data that was sent via SPLStandardMessages in both textual
   and graphical form.

For more info see [TCM](TCM.md).
