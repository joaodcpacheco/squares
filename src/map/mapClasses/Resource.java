package map.mapClasses;

import java.util.Arrays;

public enum Resource
{
	_DEFAULT("0 0 0"),

	GRAIN("40 40 95"), LIVESTOCK("225 40 70"), FISH("135 40 95"),

	STONE("0 0 65"), WOOD("80 40 25"), IRON("0 0 20"), HORSES("25 50 50"),

	GOLD("30 95 90"), SPICES("10 95 80"), GEMS("155 65 80");

	public int[] hsb;

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

	Resource(String hsb)
	{
		this.hsb = Arrays.stream(hsb.split(" ")).mapToInt(s -> new Integer(s)).toArray();
	}

	private static float initialScore()
	{
		return 0.75f + (0.25f * (float) Math.random());
	}

	public static float grain(Square square)
	{
		float score = initialScore();
		score *= 1.3f;

		switch (square.terrain)
		{
			case WOODLAND:
			case SAVANNAH:
			case SHRUBLAND:
			case STEPPE:
			case GRASSLAND:
				score *= 1f;
				break;
			case TAIGA:
			case FOREST:
			case HILLS:
				score *= 0.5f;
				break;
			case DESERT:
			case TUNDRA:
			case JUNGLE:
			case WETLAND:
			case MOUNTAINS:
				score *= 0f;
				break;
		}

		return score;
	}

	public static float livestock(Square square)
	{
		float score = initialScore();
		score *= 1.3f;

		switch (square.terrain)
		{
			case WOODLAND:
			case SAVANNAH:
			case SHRUBLAND:
			case STEPPE:
			case GRASSLAND:
				score *= 1f;
				break;
			case TAIGA:
			case FOREST:
			case HILLS:
				score *= 0.5f;
				break;
			case DESERT:
			case TUNDRA:
			case JUNGLE:
			case WETLAND:
			case MOUNTAINS:
				score *= 0f;
				break;
		}

		return score;
	}

	public static float fish(Square square)
	{
		float score = initialScore();
		score *= 1.3f;

		if (!square.bordersWater())
			score *= 0f;

		return score;
	}

	public static float stone(Square square)
	{
		float score = initialScore();
		score *= 0.3f;

		switch (square.terrain)
		{
			case DESERT:
			case TUNDRA:
			case TAIGA:
			case FOREST:
			case WOODLAND:
			case SAVANNAH:
			case SHRUBLAND:
			case STEPPE:
			case GRASSLAND:
			case HILLS:
			case MOUNTAINS:
				score *= 1f;
				break;
			case JUNGLE:
			case WETLAND:
				score *= 0f;
				break;
		}

		return score;
	}

	public static float wood(Square square)
	{
		float score = initialScore();
		score *= 1.0f;

		switch (square.terrain)
		{
			case TAIGA:
			case FOREST:
			case WOODLAND:
				score *= 1f;
				break;
			case JUNGLE:
			case SAVANNAH:
			case SHRUBLAND:
			case STEPPE:
			case GRASSLAND:
				score *= 0.5f;
				break;
			case DESERT:
			case TUNDRA:
			case WETLAND:
			case HILLS:
			case MOUNTAINS:
				score *= 0f;
				break;
		}

		return score;
	}

	public static float iron(Square square)
	{
		float score = initialScore();
		score *= 1.8f;

		switch (square.terrain)
		{
			case HILLS:
			case MOUNTAINS:
				score *= 1f;
				break;
			case DESERT:
			case TUNDRA:
			case JUNGLE:
			case TAIGA:
			case FOREST:
			case WOODLAND:
			case SAVANNAH:
			case SHRUBLAND:
			case STEPPE:
			case GRASSLAND:
			case WETLAND:
				score *= 0.25f;
				break;
		}
		
		score *= square.distanceFromSea;

		return score;
	}

	public static float horses(Square square)
	{
		float score = initialScore();
		score *= 0.6f;

		switch (square.terrain)
		{
			case SAVANNAH:
			case SHRUBLAND:
			case STEPPE:
			case GRASSLAND:
				score *= 1f;
				break;
			case DESERT:
			case WOODLAND:
			case HILLS:
				score *= 0.5f;
				break;
			case TUNDRA:
			case JUNGLE:
			case TAIGA:
			case FOREST:
			case WETLAND:
			case MOUNTAINS:
				score *= 0f;
				break;
		}

		return score;
	}

	public static float gold(Square square)
	{
		float score = initialScore();
		score *= 0.05f;

		switch (square.terrain)
		{
			case JUNGLE:
			case HILLS:
			case MOUNTAINS:
				score *= 1f;
				break;
			case DESERT:
			case TUNDRA:

			case TAIGA:
			case FOREST:
			case WOODLAND:
			case SAVANNAH:
			case SHRUBLAND:
			case STEPPE:
			case GRASSLAND:
			case WETLAND:
				score *= 0.5f;
				break;
		}
		
		if (square.hasRiver())
			score *= 1.25f;

		return score;
	}

	public static float spices(Square square)
	{
		float score = initialScore();
		score *= 0.10f;

		switch (square.terrain)
		{
			case JUNGLE:
			case WOODLAND:
			case SAVANNAH:
			case SHRUBLAND:
			case STEPPE:
			case GRASSLAND:
			case WETLAND:
				score *= 1f;
				break;
			case DESERT:
			case TUNDRA:
			case TAIGA:
			case FOREST:
			case HILLS:
			case MOUNTAINS:
				score *= 0f;
				break;
		}

		return score;
	}

	public static float gems(Square square)
	{
		float score = initialScore();
		score *= 0.2f;

		switch (square.terrain)
		{
			case JUNGLE:
			case HILLS:
			case MOUNTAINS:
				score *= 1f;
				break;
			case DESERT:
			case TUNDRA:

			case TAIGA:
			case FOREST:
			case WOODLAND:
			case SAVANNAH:
			case SHRUBLAND:
			case STEPPE:
			case GRASSLAND:
			case WETLAND:
				score *= 0.5f;
				break;
		}
		
		score *= square.distanceFromSea;

		return score;
	}
}
