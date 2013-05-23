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

java -jar GameController.jar [-b <address> | --broadcast <address>]

If no broadcast address is specified, 255.255.255.255 is used.


########## 3. Shortcuts ##########

While the GameController is running, you may use the following keys on the keyboard instead of pushing buttons:

Esc		- press it twice to close the GameController2
Delete		- toggle test-mode (everything is legal, every button is visible and enabled)
Backspace	- undo last action

B	- out by blue
R	- out by red

only SPL
P	- pushing
L	- leaving the field
F	- fallen robot
I	- inactive robot
D	- illegal defender
O	- ball holding
H	- playing with hands
U	- request for pickup

only Humanoid-League
P	- pushing
D	- illegal defense
A	- illegal attack
M	- ball manipulation
U	- request for pickup


########## 4. Misc ##########

The format of the packets the GameController broadcasts and receives is defined in the file RoboCupGameControlData.h, which is identical to the one that was used in 2012.
