package map.mapClasses;

import java.util.Arrays;
import java.util.stream.IntStream;

public class Square
{
	public int x, y;
	public Square[] neighbours; // n s e w ne se nw sw
	public Earthmass earthmass;

	public float elevation;
	public float temperature;
	public float precipitation;
	public float settlementScore;
	public Settlement settlement;
	public int highestNeighbour, lowestNeighbour;
	public float maxSlope;
	public float distanceFromSea;
	public River river;
	public Terrain terrain;
	public Resource resource;

	public Square(int x, int y)
	{
		this.x = x;
		this.y = y;
		neighbours = new Square[8];
		elevation = -1;
		temperature = -1;
		precipitation = -1;
		settlementScore = -1;
		settlement = null;
		highestNeighbour = -1;
		lowestNeighbour = -1;
		maxSlope = -1;
		distanceFromSea = -1;
		river = null;

		terrain = Terrain._DEFAULT;
		resource = Resource._DEFAULT;
	}

	public Square n()
	{
		return neighbours[N];
	}

	public Square s()
	{
		return neighbours[S];
	}

	public Square e()
	{
		return neighbours[E];
	}

	public Square w()
	{
		return neighbours[W];
	}

	public Square ne()
	{
		return neighbours[NE];
	}

	public Square se()
	{
		return neighbours[SE];
	}

	public Square nw()
	{
		return neighbours[NW];
	}

	public Square sw()
	{
		return neighbours[SW];
	}

	public boolean isLand()
	{
		return earthmass.isLand();
	}

	public boolean isSea()
	{
		return earthmass.isSea();
	}

	public boolean isLake()
	{
		return earthmass.isLake();
	}

	public boolean isCoast()
	{
		return isLand() && bordersWater();
	}

	public boolean bordersWater()
	{
		return Arrays.stream(neighbours).anyMatch(n -> n != null && !n.isLand());
	}

	public boolean hasRiver()
	{
		return river != null;
	}

	public Square highestNeighbour()
	{
		return highestNeighbour != -1 ? neighbours[highestNeighbour] : null;
	}

	public Square lowestNeighbour()
	{
		return lowestNeighbour != -1 ? neighbours[lowestNeighbour] : null;
	}

	@Override
	public String toString()
	{
		// return String.format("(%d, %d) %s", x, y, isLand() ? "LAND" :
		// "WATER");
		return String.format("(%d, %d)", x, y);
	}

	public String info()
	{
		return String.format("%s%s\nEl: %.3f, Ms: %.3f\nTe: %.3f, Pr: %.3f\nTr: %s\nSs: %.3f\nRe: %s\nDs: %.3f\n%s\n%s",
				settlement != null ? settlement.name.toUpperCase() + " " : "", toString(), elevation, maxSlope,
				temperature, precipitation, terrain, settlementScore, resource, distanceFromSea, earthmass.toString(),
				river != null && isLand() ? river.toString() + "\n" : "");
	}

	public static final int N = 0, S = 7, E = 1, W = 6, NE = 2, SE = 4, NW = 3, SW = 5;

	public static Integer[] directions()
	{
		return IntStream.range(1, 8).boxed().toArray(Integer[]::new);
	}
}
