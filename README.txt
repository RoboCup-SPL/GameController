This is the GameController developed by team B-Human for the RoboCup SPL and Humanoid-League.

If there are any question, please contact yuzong@informatik.uni-bremen.de .


########## 1. Building from Source ##########

To build it from the source code you may use Apache Ant.
There are three ant targets:

- clean
	cleans up the project folder

- compile
	compiles the code and stores files in /build/classes
	
- distribute
	creates a jar package and stores it in /build/jar

	
########## 2. Executing the Jar ##########

Double-click GameController.jar or run 

java -jar GameController.jar <options>
  [-h | --help]                   display help
  [-b | --broadcast] <address>    changes the broadcast ip to the given one
  [-l | --league] <league-dir>    given league is preselected


########## 3. Usage ##########

## Start Dialog ##

Pick the two teams that are playing. They have to be different teams. If you are practicing alone, use the "Invisibles" as second team.

You also have to select whether you play a game in the preliminaries or a play-off game. In the preliminaries the clock will continue to run during game stoppages and there will be no penalties shootout in case of a draw.

You can select whether the GameController should run in fullscreen mode or in windowed mode. Note that the fullscreen mode does not work correctly on some Linux desktops, because although they report to Java that they would support this feature, they do not.


## Main Screen ##

The use of the main screen should be rather obvious in most cases. Therefore, we only focus on the specialties.

When ever you made a mistake, use the undo history at the bottom of the screen to correct it. You cannot correct individual decisions (except for the last one). Instead, you can only roll back to a certain state of the game. Click the oldest decision in the history you want to undo. After that all decisions that would be undone will be marked. Click the decision again to actually undo it together with all decisions that followed. 

To penalize a robot, first press the penalty button, then the robot button. For unpenalizing a robot, just press the robot button. A robot can only be unpenalized, when its penalty time is over or when the game state changes (SPL only). Five seconds before the penalty time is over, the robot's button starts flashing yellow. For regular penalties, it continues to flash until the button is pressed. Only buttons of robots that were requested for pickup stop flashing after five seconds and simply stay yellow until they are pressed, as a reminder that the robot can return as soon as it is ready.

Before unpenalizing a robot, please make sure that it was put back on the field by the assistant referees. For that reason, robots are never unpenalized automatically.

When pressing "+" (goal), "Timeout", "Kickoff Goal", or "Global Game Stuck", the other team gets the next kick-off. "Kickoff Goal" and "Global Game Stuck" share the same button.

########## 4. Shortcuts ##########

While the GameController is running, you may use the following keys on the keyboard instead of pushing buttons:

Esc		- press it twice to close the GameController2
Delete		- toggle test-mode (everything is legal, every button is visible and enabled)
Backspace	- undo last action

only SPL
B	- out by blue
R	- out by red

P	- pushing
L	- leaving the field
F	- fallen robot
I	- inactive robot
D	- illegal defender
O	- ball holding
H	- playing with hands
U	- request for pickup

only Humanoid-League
C	- out by cyan
M	- out by magenta

P	- pushing
D	- illegal defense
A	- illegal attack
B	- ball manipulation
R	- request for pickup


########## 5. Misc ##########

The format of the packets the GameController broadcasts and receives is defined in the file RoboCupGameControlData.h, which is identical to the one that was used in 2012.
