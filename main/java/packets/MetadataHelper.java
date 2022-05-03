package packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import org.bukkit.inventory.ItemStack;
import org.ef3d0c3e.hunt.game.Game;

public class MetadataHelper
{
    public enum Entity
    {
        // Entity
        STATUS((byte)0),
        AIR_TICKS((byte)1),
        CUSTOM_NAME((byte)2),
        CUSTOM_NAME_VISIBLE((byte)3),
        SILENT((byte)4),
        NO_GRAVITY((byte)5),
        POSE((byte)6),
        TICKS_FROZEN((byte)6);

        public enum LivingEntity
        {
            // Living Entity
            HAND_STATE((byte)8),
            HEALTH((byte)9),
            POTION_EFFECT_COLOR((byte)10),
            POTION_EFFECT_AMBIENT((byte)11),
            ARROW_NUMBER((byte)12),
            BEE_STRINGERS_NUMBER((byte)13),
            BED_LOCATION((byte)14);

            private LivingEntity(byte id)
            {
                this.id = id;
            }

            private byte id;


            // Armor Stand
            public enum ArmorStand
            {
                TYPE((byte)15),
                HEAD_ROTATION((byte)16);

                private ArmorStand(byte id)
                {
                    this.id = id;
                }

                private byte id;
            }

            // Shulker
            public enum Shulker
            {
                TYPE((byte)15),
                DIRECTION((byte)16);

                private Shulker(byte id) { this.id = id; }

                private byte id;
            }
        }

        private Entity(byte id)
        {
            this.id = id;
        }

        private byte id;
    }

    public enum EntityPose
    {
        // NOTE: Note sure about this enum...
        STANDING,
        SITTING,
        LYING,
        CRAWLING,
        WAVING,
        POINTING,
        CLAPPING,
        HANDSHAKING,
        PRAYING,
        SPINJUTSU,
        ;
    }

    public class Serializer
    {
        public final static WrappedDataWatcher.Serializer BYTE = WrappedDataWatcher.Registry.get(Byte.class);
        public final static WrappedDataWatcher.Serializer BOOL = WrappedDataWatcher.Registry.get(Boolean.class);
        public final static WrappedDataWatcher.Serializer INT = WrappedDataWatcher.Registry.get(Integer.class);
        public final static WrappedDataWatcher.Serializer FLOAT = WrappedDataWatcher.Registry.get(Float.class);
    }

    public MetadataHelper()
    {
        m_watcher = new WrappedDataWatcher();
    }

    public PacketContainer getPacket(int entityId, boolean writeDefaults)
    {
        PacketContainer packet = Game.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        if (writeDefaults)
            packet.getModifier().writeDefaults();

        packet.getIntegers().write(0, entityId);
        packet.getWatchableCollectionModifier().write(0, m_watcher.getWatchableObjects());
        return packet;
    }

    public PacketContainer getPacket(PacketContainer spawn, boolean writeDefaults)
    {
        PacketContainer packet = Game.getProtocolManager().createPacket(PacketType.Play.Server.ENTITY_METADATA);
        if (writeDefaults)
            packet.getModifier().writeDefaults();

        packet.getIntegers().write(0, spawn.getIntegers().read(0));
        packet.getWatchableCollectionModifier().write(0, m_watcher.getWatchableObjects());
        return packet;
    }

    private WrappedDataWatcher m_watcher;

    // * Entity
    public class Status
    {
        public final static byte Fire = 0x1;
        public final static byte Crouching = 0x2;
        public final static byte Sprinting = 0x4;
        public final static byte Swimming = 0x10;
        public final static byte Invisible = 0x20;
        public final static byte Glowing = 0x40;
        public final static byte ElytraFlying = (byte)0x80;
    }
    public void setStatus(byte v)
    { m_watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(Entity.STATUS.id, Serializer.BYTE), v); }
    public byte getStatus()
    { return (byte)m_watcher.getWatchableObject(Entity.STATUS.id).getValue(); }

    public void setAirTicks(int v)
    { m_watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(Entity.AIR_TICKS.id, Serializer.INT), v); }
    public int getAirTicks()
    { return (int)m_watcher.getWatchableObject(Entity.AIR_TICKS.id).getValue(); }

    // TODO: Custom name

    public void setCustomNameVisible(boolean v)
    { m_watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(Entity.CUSTOM_NAME_VISIBLE.id, Serializer.BOOL), v); }
    public boolean getCustomNameVisible()
    { return (boolean)m_watcher.getWatchableObject(Entity.CUSTOM_NAME_VISIBLE.id).getValue(); }

    public void setSilent(boolean v)
    { m_watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(Entity.SILENT.id, Serializer.BOOL), v); }
    public boolean getSilent()
    { return (boolean)m_watcher.getWatchableObject(Entity.SILENT.id).getValue(); }

    public void setNoGravity(boolean v)
    { m_watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(Entity.NO_GRAVITY.id, Serializer.BOOL), v); }
    public boolean getNoGravity()
    { return (boolean)m_watcher.getWatchableObject(Entity.NO_GRAVITY.id).getValue(); }

    // TODO: Pose

    public void setTicksFrozen(int v)
    { m_watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(Entity.TICKS_FROZEN.id, Serializer.INT), v); }
    public int getTicksFrozen()
    { return (int)m_watcher.getWatchableObject(Entity.TICKS_FROZEN.id).getValue(); }

    // * LivingEntity
    public class HandStates
    {
        public final static byte HandActive = 0x01;
        public final static byte OffHand = 0x02;
        public final static byte InRiptideAttack = 0x04;

    }
    public void setHandState(byte v)
    { m_watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(Entity.LivingEntity.HAND_STATE.id, Serializer.BYTE), v); }
    public byte getHandState()
    { return (byte)m_watcher.getWatchableObject(Entity.LivingEntity.HAND_STATE.id).getValue(); }

    public void setHealth(float v)
    { m_watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(Entity.LivingEntity.HEALTH.id, Serializer.FLOAT), v); }
    public float getHealth()
    { return (float)m_watcher.getWatchableObject(Entity.LivingEntity.HEALTH.id).getValue(); }

    public void setPotionEffectColot(int v)
    { m_watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(Entity.LivingEntity.POTION_EFFECT_COLOR.id, Serializer.INT), v); }
    public int getPotionEffectColor()
    { return (int)m_watcher.getWatchableObject(Entity.LivingEntity.POTION_EFFECT_COLOR.id).getValue(); }

    public void setPotionEffectAmbient(boolean v)
    { m_watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(Entity.LivingEntity.POTION_EFFECT_AMBIENT.id, Serializer.BOOL), v); }
    public boolean getPotionEffectAmbient()
    { return (boolean)m_watcher.getWatchableObject(Entity.LivingEntity.POTION_EFFECT_AMBIENT.id).getValue(); }

    public void setArrowNumber(int v)
    { m_watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(Entity.LivingEntity.ARROW_NUMBER.id, Serializer.INT), v); }
    public int getArrowNumber()
    { return (int)m_watcher.getWatchableObject(Entity.LivingEntity.ARROW_NUMBER.id).getValue(); }

    public void setBeeStingersNumber(int v)
    { m_watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(Entity.LivingEntity.BEE_STRINGERS_NUMBER.id, Serializer.INT), v); }
    public int getBeeStingersNumber()
    { return (int)m_watcher.getWatchableObject(Entity.LivingEntity.BEE_STRINGERS_NUMBER.id).getValue(); }

    // TODO: Bed location
    // TODO: Player

    // * Armor Stand
    public class ArmorStandTypes
    {
        public final static byte IsSmall = 0x01;
        public final static byte HasArms = 0x04;
        public final static byte HasNoBasePlate = 0x08;
        public final static byte IsMarker = 0x10;
    };
    public void setArmorStandType(byte v)
    { m_watcher.setObject(new WrappedDataWatcher.WrappedDataWatcherObject(Entity.LivingEntity.ArmorStand.TYPE.id, Serializer.BYTE), v); }
    public byte getArmorStandType()
    { return (byte)m_watcher.getWatchableObject(Entity.LivingEntity.ArmorStand.TYPE.id).getValue(); }

    // TODO: Rotations
}
