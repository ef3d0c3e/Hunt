package packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.ChatColor;

import java.util.Collection;
import java.util.List;

public class TeamHelper
{
    final PacketContainer packet;

    public TeamHelper()
    {
        packet = new PacketContainer(PacketType.Play.Server.SCOREBOARD_TEAM);
    }

    public PacketContainer getPacket()
    { return packet; }

    public enum Mode
    {
        CREATE_TEAM(0),
        REMOVE_TEAM(1),
        UPDATE_TEAM(2),
        ADD_ENTITIES(3),
        REMOVE_ENTITIES(4);

        private Mode(int id)
        {
            this.id = id;
        }

        int id;
    }

    public void setName(String name)
    { packet.getStrings().write(0, name); }
    public String getName()
    { return packet.getStrings().read(0); }

    public void setMode(Mode mode)
    { packet.getIntegers().write(0, mode.id); }

    // Create Team
    // TODO: Display name

    public class FriendlyFlags
    {
        public final static byte FRIENDLY_FIRE = 0x01;
        public final static byte SEE_INVISIBLE = 0x02;
    }
    public void setFriendlyFlags(byte flags)
    { packet.getBytes().write(1, flags); }
    public byte getFriendlyFlags()
    { return packet.getBytes().read(1); }

    public class NametagVisbility
    {
        public final static String ALWAYS = "always";
        public final static String HIDE_FOR_OTHER_TEAMS = "hideForOtherTeams";
        public final static String HIDE_FOR_OWN_TEAM = "hideForOwnTeam";
        public final static String NEVER = "never";
    }
    public void setNametagVisibility(String vis)
    { packet.getStrings().write(1, vis); }
    public String getNametagVisibility()
    { return packet.getStrings().read(1); }

    public class CollisionRule
    {
        public final static String ALWAYS = "always";
        public final static String PUSH_OTHER_TEAMS = "pushOtherTeams";
        public final static String PUSH_FOR_OWN_TEAM = "pushOwnTeam";
        public final static String NEVER = "never";
    }
    public void setCollisionRule(String rule)
    { packet.getStrings().write(2, rule); }
    public String getCollisionRule()
    { return packet.getStrings().read(2); }

    public class TeamColor
    {
        public final static int BLACK = 0;
        public final static int DARK_BLUE = 1;
        public final static int DARK_GREEN = 2;
        public final static int CYAN = 3;
        public final static int DARK_RED = 4;
        public final static int DARK_PURPLE = 5;
        public final static int GOLD = 6;
        public final static int LIGHT_GREY = 7;
        public final static int DARK_GREY = 8;
        public final static int LIGHT_BLUE = 9;
        public final static int LIGHT_GREEN = 10;
        public final static int BABY_BLUE = 11;
        public final static int LIGHT_RED = 12;
        public final static int LIGHT_PURPLE = 13;
        public final static int YELLOW = 14;
        public final static int WHITE = 15;
        public final static int OBFUSCATED = 16;
        public final static int BOLD = 17;
        public final static int STRIKETHROUGH = 18;
        public final static int UNDERLINED = 19;
        public final static int ITALIC = 20;
        public final static int RESET = 21;
    }
    // NOTE : Currently protocolib is broken for this
    public void setTeamColor(ChatColor color) {
        /*
        PacketPlayOutScoreboardTeam t = (PacketPlayOutScoreboardTeam) packet.getHandle();
        try {
            t.getClass().getDeclaredField("g").set(t, color);
        } catch (Exception e) {
        }
        */
    }

    // TODO: prefix / suffix

    public void setEntitiesAtCreate(List<String> ents)
    {
        packet.getSpecificModifier(Collection.class).write(0, ents);
    }

    // TODO: Other modes

}
