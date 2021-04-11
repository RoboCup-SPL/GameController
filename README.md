# RoboCup SPL GameController

This is the GameController developed by team B-Human for the RoboCup SPL. Please note that the Humanoid League currently uses a
[fork](https://github.com/RoboCup-Humanoid-TC/GameController).


The sources mentioned in some sections of this document are available at
[https://github.com/RoboCup-SPL/GameController](https://github.com/RoboCup-SPL/GameController).


### Acknowledgement

The development was partially supported by the RoboCup Federation within the
calls for Support for Projects for League Developments for 2013, 2015, 2017, and 2018.


## 1. Building from Source

To build it from the source code you may use Apache Ant.
Just call "ant" in the main directory.

Building the source code requires the JDK 1.8 or newer.


## 2. GameController

### Executing the Jar

Double-click GameController.jar or run

Usage: `java -jar GameController.jar {options}`

    (-h | --help)                   display help
    (-t | --test)                   use test-mode - currently only disabling the
                                    delayed game state switches in the SPL
    (-i | --interface) <interface>  set network interface (default is a
                                    connected IPv4 interface)
    (-l | --league) (spl | spl_penalty_shootout | spl_general_penalty_kick_challenge)
                                    select league (default is spl)
    (-w | --window)                 select window mode (default is fullscreen)
    (-g | --game-type) (undefined | preliminary | playoff)
                                    select game type (default is undefined)
    (-b | --limited-broadcast)      use 255.255.255.255 as broadcast address
    --team1 <team name or number>   select first team (default is 0)
    --team2 <team name or number>   select second team (default is 0)


### Start Dialog

Select your league. The default can be specified as a command line parameter
(see above).

Pick the two teams that are playing. They have to be different teams. If you are
practicing alone, use the "Invisibles" as second team.

SPL: The GameController automatically selects the jersey colors as defined in
the file "teams.cfg". The left team's jersey color is picked first regardless if
it has a custom jersey or not. For both teams, the first jersey color that they
have and, if picking second, that does not conflict with the jersey color of the
opponent is selected in the following sequence:

* Left team: custom 1, custom 2, blue, red
* Right team: custom 1, custom 2, red, blue

The GameController operator can switch each teams color to their secondary color
if it is necessary to distinguish the jersey colors.

You also have to select whether you play a game in the preliminaries or a
play-off game. In the preliminaries the clock will continue to run during game
stoppages and there will be no penalty shootout in case of a draw. In play-off
games, the clock will be stopped and there may be penalty shootout.

You can select whether the GameController should run in fullscreen mode or in
windowed mode. Note that the fullscreen mode does not work correctly on some
Linux desktops, because although they report to Java that they would support
this feature, they do not.


### Main Screen

The use of the main screen should be rather obvious in most cases. Therefore, we
only focus on the specialties.

When ever you made a mistake, use the undo history at the bottom of the screen
to correct it. You cannot correct individual decisions (except for the last
one). Instead, you can only roll back to a certain state of the game. Click the
oldest decision in the history you want to undo. After that all decisions that
would be undone will be marked. Click the decision again to actually undo it
together with all decisions that followed.

To penalize a robot, first press the penalty button, then the robot button. The
only exception is the state "Set" in playoff games, in which the "Motion in Set"
penalty is preselected and robots can be penalized by simply clicking on them
(selecting other penalties is still possible). For unpenalizing a robot, just
press the robot button.  A robot can only be unpenalized, when its penalty time
is over or when the game state changes (SPL only). Ten seconds before the
penalty time is over, the robot's button starts flashing yellow. For regular
penalties, it continues to flash until the button is pressed. Only buttons of
robots that were requested for pickup stop flashing after ten seconds and simply
stay yellow until they are pressed, as a reminder that the robot can return as
soon as it is ready. Robots with a "Motion in Set" penalty stay on the field and
will be automatically unpenalized 15 seconds after pressing the button "Play".

Before unpenalizing a robot that was taken off the field, please make sure that
it was put back on the field by the assistant referees. For that reason, robots
are never unpenalized automatically, except for the "Illegal Motion in Set" penalty.

To substitute a robot, press "Substitute" and then the robot that should leave
the field.  Afterwards, any of the substitutes can be activated. If the robot
that is replaced is already penalized, its substitute inherits the penalty. If
it is not, the substitute can immediately enter the field in the HL, but gets a
"request for pickup" penalty before it can enter the field in the SPL.

When pressing the big "+" (goal), "Timeout", or "Global Game Stuck", the other
team gets the next kick-off. When pressing "Goal Kick", "Corner Kick" or
"Kick In", the same team gets the next kick-off.

SPL: When the referee decides that too much game time has been lost, use the
thin "+" next to the clock to increase the game time in one-minute steps. This
is only available during stoppages of play.


### Penalty Shootout

In the penalty shootout, press "Set" and place the robots in their correct
locations. Select the two robots that are actually performing the current
attempt as penalty taker and goalkeeper. All other robots are marked as
substitutes. Press "Play" to start a single shot. The penalty shot is ended by
either pressing "+" for the penalty taker or by pressing "Finish" if the shot
failed. In both cases, a penalty shot ends in the state "Finished". Press "Set"
again to start the next shot.


### Shortcuts

While the GameController is running, you may use the following keys on the
keyboard instead of pushing buttons:

    Esc       - press it twice to close the GameController
    Delete    - toggle test-mode (everything is legal, every button is visible
                and enabled)
    Backspace - undo last action

only SPL

    B    - kick in for blue
    R    - kick in for red
    Y    - kick in for yellow
    K    - kick in for black
         - the new team colors implemented for RoboCup 2016 do not yet have a key
           assigned

    P    - pushing
    L    - leaving the field
    I    - fallen / inactive
    D    - illegal position
    O    - illegal ball contact
    U    - request for pickup
    F    - foul


### Adding teams to the GameController

The teams registered in the GameController are determined using the config file
located at `config/<league name>/teams.cfg` relative the the GameController.jar.
To add teams to it, lines composed of `<team number>=<team name>` may be added
to this file.
Optionally, the primary and secondary team colors may be added behind the team
name, separated by commas.

Team logos lie in the same directory as the config file with the corresponding
team number as the file name.


## 3. GameStateVisualizer

As of the 2017 RoboCup competitions, the GameStateVisualizer has been replaced
by the GameStateVisualizer mode of the TeamCommunicationMonitor.

To start it, run

`java -jar TeamCommunicationMonitor.jar --gsv -l <league>`.

The parameter `-l` (or `--league`) is optional; if it is not given, the GSV
assumes a game in the SPL.


## 4. TeamCommunicationMonitor

The TeamCommunicationMonitor (TCM) is a tool for visualizing the data
communicated by robots during SPL games.

It serves two main purposes:

1. offering diagnostic data such as which robots are communicating on which
   team ports and whether the data that is sent by these conforms to the
   SPLStandardMessage, which is the standard communication protocol in the SPL
2. visualizing the data that was sent via SPLStandardMessages in both textual
   and graphical form.

For more info see [TCM](TCM.md).


## 5. Misc

The format of the packets the GameController broadcasts at port
GAMECONTROLLER\_DATA\_PORT and receives at port GAMECONTROLLER\_RETURN\_PORT
is defined in the file RoboCupGameControlData.h. It differs from the version used
in 2019 in the following ways:

- The macro `COMPETITION_TYPE_MIXEDTEAM` has been removed.
- The macro `COMPETITION_TYPE_GENERAL_PENALTY_KICK` has been added.
- The macro `SET_PLAY_GOAL_FREE_KICK` has been renamed to `SET_PLAY_GOAL_KICK`.
- The macro `SET_PLAY_PENALTY_KICK` has been added.
- The penalty macros have been reorganized:
  - `PENATLY_SPL_KICK_OFF_GOAL` has been removed.
  - `PENALTY_SPL_ILLEGAL_DEFENDER` has been renamed to `PENALTY_SPL_ILLEGAL_POSITION`.
  - `PENALTY_SPL_ILLEGAL_POSITIONING` has been renamed to `PENALTY_SPL_ILLEGAL_POSITION_IN_SET`.

Since 2015, after a change from Set to Playing in SPL games the GameController
does not send the correct game state and time for 15 seconds. This behaviour
became necessary so the robots have to listen for the whistle blown by the head
referee, but it is disruptive for applications such as the GameStateVisualizer
that display the current game state to the audience. Therefore, the
GameController now listens for TrueDataRequest messages
(see controller.data.TrueDataRequest) and sends the true game state to all
clients that requested it in this way.
The true game state is sent on the same port as the broadcasted GameControlData
and can be identified by its header. See teamcomm.net.GameControlDataReceiver
for a reference implementation of requesting the true game state and
distinguishing it from the normal broadcast message.
If a robot (identified through GameControlReturnData messages) sends a request
for receiving the true game state, the request is ignored and its network
address is written into the GameController's error log.


## 6. Known Issues

There are still a number of issues left:

- The alignment of button labels is bad if the buttons are small.

- Too many colors and buttons - too little keys (not enough shortcuts for all operations)

- On Windows, Swing windows are only displayed correctly scaled on HiDPI displays if
  Java 9 is used.
