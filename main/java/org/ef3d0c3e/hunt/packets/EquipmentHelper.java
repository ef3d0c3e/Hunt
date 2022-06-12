package org.ef3d0c3e.hunt.packets;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.Pair;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class EquipmentHelper
{
    final PacketContainer packet;
    final ArrayList<Pair<EnumWrappers.ItemSlot, ItemStack>> equipment;

    public EquipmentHelper()
    {
        packet = new PacketContainer(PacketType.Play.Server.ENTITY_EQUIPMENT);
        equipment = new ArrayList<>();
    }

    public PacketContainer getPacket(int entityId)
    {
        packet.getSlotStackPairLists().write(0, equipment);
        packet.getIntegers().write(0, entityId);
        return packet;
    }

    public PacketContainer getPacket(PacketContainer spawn)
    {
        packet.getSlotStackPairLists().write(0, equipment);
        packet.getIntegers().write(0, spawn.getIntegers().read(0));
        return packet;
    }

    public class Slot
    {
        public final static EnumWrappers.ItemSlot MAINHAND = EnumWrappers.ItemSlot.MAINHAND;
        public final static EnumWrappers.ItemSlot OFFHAND = EnumWrappers.ItemSlot.OFFHAND;
        public final static EnumWrappers.ItemSlot FEET = EnumWrappers.ItemSlot.FEET;
        public final static EnumWrappers.ItemSlot LEGS = EnumWrappers.ItemSlot.LEGS;
        public final static EnumWrappers.ItemSlot CHEST = EnumWrappers.ItemSlot.CHEST;
        public final static EnumWrappers.ItemSlot HEAD = EnumWrappers.ItemSlot.HEAD;
    }

    public void setItem(EnumWrappers.ItemSlot slot, ItemStack item)
    {
        equipment.add(new Pair(slot, item));
    }
}