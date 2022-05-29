package org.ef3d0c3e.hunt.stats;

import net.minecraft.stats.Stat;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.MessageFormat;

/**
 * Represents the value of a stat
 */
public abstract class StatValue
{
	StatValue() {}

	abstract public String format(final String fmt);
	abstract public String serialize();
	abstract public void deserialize(final String in);
}

