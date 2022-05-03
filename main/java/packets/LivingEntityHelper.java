package packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import org.ef3d0c3e.hunt.game.Game;

import java.util.UUID;

public class LivingEntityHelper
{
    public enum Mobs
    {
        ARMOR_STAND(1),
        AXOLOTL(3),
        BAT(4),
        BEE(5),
        BLAZE(6),
        CAT(8),
        CAVE_SPIDER(9),
        CHICKEN(10),
        COD(11),
        COW(12),
        CREEPER(13),
        DOLPHIN(14),
        DONKEY(15),
        DROWNED(17),
        ELDER_GUARDIAN(18),
        ENDER_DRAGON(20),
        ENDERMAN(21),
        ENDERMITE(22),
        EVOKER(23),
        FOX(29),
        GHAST(30),
        GIANT(31),
        GLOW_SQUID(33),
        GOAT(34),
        GUARDIAN(35),
        HOGLIN(36),
        HORSE(37),
        HUSK(38),
        ILLUSIONER(39),
        IRON_GOLEM(40),
        LLAMA(46),
        MAGMA_CUBE(48),
        MULE(57),
        MOOSHROOM(58),
        OCELOT(59),
        PANDA(61),
        PARROT(62),
        PHANTOM(63),
        PIG(64),
        PIGLIN(65),
        PIGLIN_BRUTE(66),
        PILLAGER(67),
        POLAR_BEAR(68),
        PUFFERFISH(70),
        RABBIT(71),
        RAVAGER(72),
        SALMON(73),
        SHEEP(74),
        SHULKER(75),
        SILVERFISH(77),
        SKELETON(78),
        SKELETON_HORSE(79),
        SLIME(80),
        SNOW_GOLEM(82),
        SPIDER(85),
        SQUID(86),
        STRAY(87),
        STRIDER(88),
        TRADER_LLAMA(94),
        TROPICAL_FISH(95),
        TURTLE(96),
        VEX(97),
        VILLAGER(98),
        VINDICATOR(99),
        WANDERING_TRADER(100),
        WITCH(101),
        WITHER(102),
        WITHER_SKELETON(103),
        WOLF(105),
        ZOGLIN(106),
        ZOMBIE(107),
        ZOMBIE_HORSE(108),
        ZOMBIE_VILLAGER(109),
        ZOMBIFIED_PIGLIN(110);

        private Mobs(int id)
        { this.id = id; }

        int id;
    }

    final PacketContainer packet;

    public LivingEntityHelper(int eid, UUID uuid)
    {
        packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        packet.getIntegers().write(0, eid);
        packet.getUUIDs().write(0, uuid);
    }

    public LivingEntityHelper()
    {
        packet = new PacketContainer(PacketType.Play.Server.SPAWN_ENTITY_LIVING);
        packet.getIntegers().write(0, Game.nextPosInt());
        packet.getUUIDs().write(0, UUID.randomUUID());
    }

    public PacketContainer getPacket()
    { return packet; }

    public void setEID(int eid)
    { packet.getIntegers().write(0, eid); }
    public int getEID()
    { return packet.getIntegers().read(0); }

    public void setUUID(UUID uuid)
    { packet.getUUIDs().write(0, uuid); }
    public UUID getUUID()
    { return packet.getUUIDs().read(0); }

    public void setType(Mobs type)
    { packet.getIntegers().write(1, type.id); }

    public void setPosition(double x, double y, double z)
    {
        packet.getDoubles()
                .write(0, x)
                .write(1, y)
                .write(2, z);
    }
    public void setX(double x)
    { packet.getDoubles().write(0, x); }
    public double getX()
    { return packet.getDoubles().read(0); }
    public void setY(double y)
    { packet.getDoubles().write(1, y); }
    public double getY()
    { return packet.getDoubles().read(1); }
    public void setZ(double z)
    { packet.getDoubles().write(2, z); }
    public double getZ()
    { return packet.getDoubles().read(2); }

    public void setYaw(byte yaw)
    { packet.getBytes().write(0, yaw); }
    public byte getYaw()
    { return packet.getBytes().read(0); }
    public void setPitch(byte pitch)
    { packet.getBytes().write(1, pitch); }
    public byte getPitch()
    { return packet.getBytes().read(1); }
    public void setHeadPitch(byte headPitch)
    { packet.getBytes().write(2, headPitch); }
    public byte getHeadPitch()
    { return packet.getBytes().read(2); }

    // TODO: Velocity
}
