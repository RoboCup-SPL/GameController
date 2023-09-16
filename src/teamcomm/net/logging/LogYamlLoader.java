package teamcomm.net.logging;

import common.net.GameControlReturnDataPackage;
import common.net.SPLTeamMessagePackage;
import data.GameControlData;
import static data.GameControlData.*;
import data.PlayerInfo;
import data.TeamInfo;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.Deque;
import java.util.List;
import java.util.Map;

/**
 * The class loads a GameController3 log file (.yaml) and adds all
 * relevant entries to the replay queue. Data from the states
 * INITIAL and FINISHED is skipped.
 */
class LogYamlLoader extends Constructor {
    /**
     * Helper class that is used to parse custom types. It basically
     * just parses the custom types just like regular types, but
     * adds an attribute "kind" that contains the name of the custom
     * type. Thus, the type can be queried later.
     */
    private class KindConstructor extends ConstructYamlMap {
        /** The name of the custom type that will be added. */
        private final String kind;

        /**
         * Constructor.
         * @param kind The name of the custom type that will be added.
         */
        public KindConstructor(final String kind) {
            this.kind = kind;
        }

        /**
         * Called to parse a node in the YAML tree and return it as a
         * map. The map will contain an attribute "kind" that stores
         * the custom type.
         * @param node The node in the YAML tree.
         * @return A map representing the node.
         */
        @Override public Object construct(Node node) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> map = (Map<String, Object>) super.construct(node);
            map.put("kind", kind);
            return map;
        }
    }

    /** Helper to map state names to the state number. "timeout" is later mapped to "initial". */
    private final List<String> states = Arrays.asList("initial", "ready", "set", "playing", "finished"); // timeout = 0

    /** Helper to map set play names to set player numbers. */
    private final List<String> setPlays = Arrays.asList(
            "noSetPlay", "goalKick","pushingFreeKick", "cornerKick", "kickIn", "penaltyKick");

    /** Helper to map color names to color numbers. */
    private final List<String> colors = Arrays.asList(
            "blue", "red", "yellow", "black", "white", "green", "orange", "purple", "brown", "gray");

    /** Helper to map penalty names to penalty numbers. "playingWithArmsHands" is later mapped to "ballHolding". */
    private final List<String> penalties = Arrays.asList(
            "noPenalty", "ballHolding", "playerPushing", "motionInSet", "fallenInactive", "illegalPosition",
            "leavingTheField", "pickedUp", "localGameStuck", "illegalPositionInSet", "playerStance",
            "_11", "_12", "_13", "substitute"); // playingWithArmsHands = 1

    /** The current state of the GameController packet. */
    private final GameControlData data = new GameControlData();

    /** The team information of the home team. */
    private final TeamInfo home = new TeamInfo();

    /** The team information of the away team. */
    private final TeamInfo away = new TeamInfo();

    /** Which side (left = 0, right = 1) has the initial kick-off? */
    private int kickOffSide;

    /**
     * Load the YAML log file.
     * Note the file size is currently limited to 64 mb.
     * @param file The file to load.
     * @param queue The queue that is filled with the data that was
     *              found in the file.
     * @throws IOException The file could not be opened or read.
     */
    static void load(final File file, final Deque<LogReplayTask.LoggedObject> queue) throws IOException {
        final LoaderOptions options = new LoaderOptions();
        options.setCodePointLimit(67108864);
        new LogYamlLoader(options).parse(file, queue);
    }

    /**
     * The constructor initializes the YAML parser. It also registers
     * all custom types.
     * @param options Options for the YAML parser.
     */
    private LogYamlLoader(final LoaderOptions options) {
        super(options);
        yamlConstructors.put(new Tag("!metadata"), new KindConstructor("metadata"));
        yamlConstructors.put(new Tag("!gameState"), new KindConstructor("gameState"));
        yamlConstructors.put(new Tag("!statusMessage"), new KindConstructor("statusMessage"));
        yamlConstructors.put(new Tag("!teamMessage"), new KindConstructor("teamMessage"));
        yamlConstructors.put(new Tag("!monitorRequest"), new ConstructYamlMap());
        yamlConstructors.put(new Tag("!action"), new ConstructYamlMap());
        yamlConstructors.put(new Tag("!started"), new ConstructYamlMap());
        yamlConstructors.put(new Tag("!expire"), new ConstructYamlSeq());
    }

    /**
     * Load the YAML log file.
     * @param file The file to load.
     * @param queue The queue that is filled with the data that was
     *              found in the file.
     * @throws IOException The file could not be opened or read.
     */
    private void parse(final File file, final Deque<LogReplayTask.LoggedObject> queue) throws IOException {
        data.isTrueData = true;
        data.packetNumber = -1;
        final Yaml yaml = new Yaml(this);
        long baseTime = 0; // The first timestamp in the current section.
        long recordedTime = 0; // The duration already recorded before the current section.
        boolean recording = false; // Are we currently recording data from the log file?

        final InputStream stream = Files.newInputStream(file.toPath());
        final List<Map<String, Object>> contents = yaml.load(stream);
        for (final Map<String, Object> map : contents) {
            if (map.get("timestamp") instanceof Map
                    && map.get("entry") instanceof Map) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> timestamp = (Map<String, Object>) map.get("timestamp");
                final Integer secs = (Integer) timestamp.get("secs");
                final Integer nanos = (Integer) timestamp.get("nanos");
                final long time = secs * 1000L + nanos / 1000000;
                @SuppressWarnings("unchecked")
                final Map<String, Object> entry = (Map<String, Object>) map.get("entry");
                if (entry.get("kind") instanceof String) {
                    Object message = null;
                    switch ((String) entry.get("kind")) {
                        case "metadata":
                            parseMetadata(entry);
                            continue;
                        case "gameState":
                            parseGameControlData(entry);

                            // Create a new instance for the queue.
                            final GameControlData copy = new GameControlData();
                            copy.fromByteArray(ByteBuffer.wrap(data.getTrueDataAsByteArray().array()));
                            message = copy;
                            break;
                        case "statusMessage":
                            if (!recording) {
                                continue;
                            }
                            message = new GameControlReturnDataPackage(
                                    (String) entry.get("host"),
                                    Base64.getDecoder().decode((String) entry.get("data")));
                            break;
                        case "teamMessage":
                            if (!recording) {
                                continue;
                            }
                            message = new SPLTeamMessagePackage(
                                    (String) entry.get("host"),
                                    (Integer) entry.get("team"),
                                    Base64.getDecoder().decode((String) entry.get("data")));
                            break;
                        default:
                            assert false;
                    }
                    if (data.gameState != STATE_INITIAL && data.gameState != STATE_FINISHED) {
                        if (!recording) { // Start a new section?
                            baseTime = time;
                            recording = true;
                        }
                        queue.addLast(new LogReplayTask.LoggedObject(time - baseTime + recordedTime, message));
                    } else if (recording) { // Ending the current section?
                        recordedTime += time - baseTime;
                        recording = false;
                    }
                }
            }
        }
        stream.close();
    }

    /**
     * Parses the metadata. Some general information about the game
     * can be found here.
     * @param entry The attributes under "!metadata".
     */
    private void parseMetadata(final Map<String, Object> entry) {
        if (entry.get("params") instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> params = (Map<String, Object>) entry.get("params");
            if (params.get("competition") instanceof Map) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> competition = (Map<String, Object>) params.get("competition");
                data.competitionType = competition.get("challengeMode") == null ? COMPETITION_TYPE_NORMAL : COMPETITION_TYPE_DYNAMIC_BALL_HANDLING;
                data.playersPerTeam = (byte)(int)(Integer) competition.get("playersPerTeam");
            }
            if (params.get("game") instanceof Map) {
                @SuppressWarnings("unchecked")
                final Map<String, Object> game = (Map<String, Object>) params.get("game");
                data.competitionPhase = (Boolean) game.get("long") ? COMPETITION_PHASE_PLAYOFF : COMPETITION_PHASE_ROUNDROBIN;
                kickOffSide = game.get("kickOffSide").equals("home") ? 0 : 1;
                if (game.get("teams") instanceof Map) {
                    @SuppressWarnings("unchecked")
                    final Map<String, Object> teams = (Map<String, Object>) game.get("teams");
                    if (teams.get("home") instanceof Map) {
                        @SuppressWarnings("unchecked")
                        final Map<String, Object> team = (Map<String, Object>) teams.get("home");
                        home.teamNumber = (byte)(int)(Integer) team.get("number");
                        home.fieldPlayerColor = (byte) colors.indexOf(team.get("fieldPlayerColor"));
                        home.goalkeeperColor = (byte) colors.indexOf(team.get("goalkeeperColor"));
                    }
                    if (teams.get("away") instanceof Map) {
                        @SuppressWarnings("unchecked")
                        final Map<String, Object> team = (Map<String, Object>) teams.get("away");
                        away.teamNumber = (byte)(int)(Integer) team.get("number");
                        away.fieldPlayerColor = (byte) colors.indexOf(team.get("fieldPlayerColor"));
                        away.goalkeeperColor = (byte) colors.indexOf(team.get("goalkeeperColor"));
                    }
                }
            }
        }
    }

    /**
     * Parses the data for a GameController packet.
     * @param entry The attributes under "!gameState".
     */
    private void parseGameControlData(final Map<String, ?> entry) {
        data.packetNumber = (byte) (data.packetNumber + 1);
        data.gamePhase = entry.get("phase").equals("PenaltyShootout") ? GAME_PHASE_PENALTYSHOOT : GAME_PHASE_NORMAL;
        data.gameState = (byte) Math.max(0, states.indexOf(entry.get("state")));
        data.setPlay = (byte) Math.max(0, setPlays.indexOf(entry.get("setPlay")));
        data.firstHalf = entry.get("phase").equals("firstHalf") ? C_TRUE : C_FALSE;
        data.kickingTeam = entry.get("kickingSide").equals("home") ? home.teamNumber : away.teamNumber;
        if (entry.get("primaryTimer") instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> timer = (Map<String, Object>) entry.get("primaryTimer");
            if (timer.get("remaining") instanceof List) {
                @SuppressWarnings("unchecked")
                final List<Integer> list = (List<Integer>) timer.get("remaining");
                data.secsRemaining = (short) (int) list.get(0);
            }
        }
        if (entry.get("secondaryTimer") instanceof String) {
            data.secondaryTime = 0;
        }
        else if (entry.get("secondaryTimer") instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> timer = (Map<String, Object>) entry.get("secondaryTimer");
            if (timer.get("remaining") instanceof List) {
                @SuppressWarnings("unchecked")
                final List<Integer> list = (List<Integer>) timer.get("remaining");
                data.secondaryTime = (short) (int) list.get(0);
            }
        }
        if (entry.get("teams") instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> teams = (Map<String, Object>) entry.get("teams");
            @SuppressWarnings("unchecked")
            final Map<String, Object> homeTeam = (Map<String, Object>) teams.get("home");
            parseTeamInfo(home, homeTeam);
            @SuppressWarnings("unchecked")
            final Map<String, Object> awayTeam = (Map<String, Object>) teams.get("away");
            parseTeamInfo(away, awayTeam);
            final int homeSide = data.firstHalf == C_TRUE ? kickOffSide : 1 - kickOffSide;
            data.team[homeSide] = home;
            data.team[1 - homeSide] = away;
        }
    }

    /**
     * Parses the data for information about a team in the GameController packet.
     * @param entry The attributes for a team.
     */
    private void parseTeamInfo(final TeamInfo info, final Map<String, ?> entry) {
        info.goalkeeper = (byte)(int)(Integer) entry.get("goalkeeper");
        info.score = (byte)(int)(Integer) entry.get("score");
        info.penaltyShot = (byte)(int)(Integer) entry.get("penaltyShot");
        info.singleShots = (short)(int)(Integer) entry.get("penaltyShotMask");
        info.messageBudget = (short)(int)(Integer) entry.get("messageBudget");
        @SuppressWarnings("unchecked")
        final List<Map<String, Object>> players = (List<Map<String, Object>>) entry.get("players");
        for (int i = 0; i < players.size(); ++i) {
            info.player[i] = parsePlayerInfo(players.get(i));
        }
    }

    /**
     * Parses the data for information about a player in the GameController packet.
     * @param entry The attributes for a player.
     */
    private PlayerInfo parsePlayerInfo(final Map<String, Object> entry) {
        final PlayerInfo info = new PlayerInfo();
        info.penalty = (byte) Math.abs(penalties.indexOf(entry.get("penalty")));
        if (entry.get("penaltyTimer") instanceof String) {
            info.secsTillUnpenalised = 0;
        }
        else if (entry.get("penaltyTimer") instanceof Map) {
            @SuppressWarnings("unchecked")
            final Map<String, Object> timer = (Map<String, Object>) entry.get("penaltyTimer");
            if (timer.get("remaining") instanceof List) {
                @SuppressWarnings("unchecked")
                final List<Integer> list = (List<Integer>) timer.get("remaining");
                info.secsTillUnpenalised = (byte) (int) list.get(0);
            }
        }
        return info;
    }
}
