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

class StatLong extends StatValue
{
	Long value;

	StatLong()
	{
		super();
		value = 0L;
	}

	public String format(final String fmt)
	{
		return MessageFormat.format(fmt, value);
	}

	public String serialize()
	{
		return String.valueOf(value);
	}

	public void deserialize(final String in)
	{
		value = Long.valueOf(in);
	}
}

class StatDouble extends StatValue
{
	Double value;

	StatDouble()
	{
		super();
		value = 0.0;
	}

	public String format(final String fmt)
	{
		return MessageFormat.format(fmt, value);
	}

	public String serialize()
	{
		BigDecimal bd = BigDecimal.valueOf(value);
		bd = bd.setScale(6, RoundingMode.HALF_UP);
		return bd.toPlainString();
	}

	public void deserialize(final String in)
	{
		value = Double.valueOf(in);
	}
}
