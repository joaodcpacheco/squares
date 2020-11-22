package map.mapClasses;

import java.util.Arrays;

public enum Terrain
{
	_DEFAULT("0 0 0", 0),
	
	COASTAL_WATERS("150 90 50", 5), OPEN_WATERS("160 90 15", 3),
	
	DESERT("35 35 90", 0), TUNDRA("145 35 90", 0),
	JUNGLE("15 65 45", 1), TAIGA("130 65 45", 2), 
	FOREST("75 45 40", 3), WOODLAND("85 65 70", 4),
	SAVANNAH("10 50 75", 2), SHRUBLAND("65 50 75", 3), STEPPE("120 50 75", 2), 
	GRASSLAND("50 75 90", 5),
	WETLAND("165 40 70", 1),
	HILLS("0 0 35", 2), MOUNTAINS("0 0 15", 0);
	
	public int[] hsb;
	public int settlementBonus;

	public static final Terrain[][] terrainDist;

	static
	{
		final String dist = "JUNGLE	JUNGLE	JUNGLE	FOREST	FOREST	FOREST	FOREST	TAIGA	TAIGA	TAIGA;" +
				"JUNGLE	JUNGLE	JUNGLE	FOREST	FOREST	FOREST	FOREST	TAIGA	TAIGA	TAIGA;" +
				"JUNGLE	JUNGLE	FOREST	FOREST	FOREST	FOREST	FOREST	TAIGA	TAIGA	TAIGA;" +
				"SAVANNAH	SAVANNAH	WOODLAND	WOODLAND	WOODLAND	FOREST	FOREST	TAIGA	TAIGA	TAIGA;" +
				"SAVANNAH	SAVANNAH	WOODLAND	WOODLAND	WOODLAND	WOODLAND	WOODLAND	TAIGA	TAIGA	TAIGA;" +
				"DESERT	SAVANNAH	SAVANNAH	SHRUBLAND	GRASSLAND	GRASSLAND	WOODLAND	TAIGA	TAIGA	TUNDRA;" +
				"DESERT	DESERT	SAVANNAH	SHRUBLAND	SHRUBLAND	GRASSLAND	GRASSLAND	WOODLAND	TAIGA	TUNDRA;" +
				"DESERT	DESERT	DESERT	SAVANNAH	SHRUBLAND	SHRUBLAND	SHRUBLAND	STEPPE	STEPPE	TUNDRA;" +
				"DESERT	DESERT	DESERT	DESERT	SAVANNAH	SHRUBLAND	SHRUBLAND	STEPPE	STEPPE	TUNDRA;" +
				"DESERT	DESERT	DESERT	DESERT	DESERT	SAVANNAH	SHRUBLAND	STEPPE	STEPPE	TUNDRA";

		terrainDist = Arrays.stream(dist.split(";"))
				.map(row -> Arrays.stream(row.split("\t")).
						map(elem -> fromString(elem)).toArray(Terrain[]::new))
				.toArray(Terrain[][]::new);
	}

	Terrain(String hsb, int settlementScore)
	{
		this.hsb = Arrays.stream(hsb.split(" ")).mapToInt(s -> new Integer(s)).toArray();
		this.settlementBonus = settlementScore;
	}

	public int h()
	{
		return hsb[0];
	}

	public int s()
	{
		return hsb[1];
	}

	public int b()
	{
		return hsb[2];
	}

	public static Terrain fromString(String s)
	{
		try
		{
			return Terrain.valueOf(s.toUpperCase());
		}
		catch (Exception e)
		{
			return Terrain._DEFAULT;
		}
	}
}
