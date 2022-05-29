package org.ef3d0c3e.hunt.stats;

import java.text.MessageFormat;

public class StatLong extends StatValue
{
	public Long value;

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
