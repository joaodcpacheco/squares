package map;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Queue;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import map.mapClasses.Earthmass;
import map.mapClasses.Earthmass.EarthmassType;
import map.mapClasses.Resource;
import map.mapClasses.River;
import map.mapClasses.River.RiverElement;
import map.mapClasses.Settlement;
import map.mapClasses.Square;
import map.mapClasses.Terrain;
import processing.core.PApplet;
import units.LandUnit;
import units.Unit;
import util.Names;

public class GameMap extends PApplet
{
	public final int N_SQUARES;
	public final int SQUARES_X, SQUARES_Y;
	public final int N_SETTLEMENTS;

	// map variables
	public Square[][] squares;
	public List<Earthmass> earthmasses;
	public List<Square> peaks;
	public List<River> rivers;
	public List<Settlement> settlements;
	public int landSquareCount;

	// map parameters
	public float HEIGHT_NOISE = 0.07f;
	public float HEIGHT_EXP = 5f;
	public float PEAK_ELEVATION_BOOST = 0.15f;
	public float PEAK_ELEVATION_BOOST_N8 = 0.075f;
	public float WATER_ELEVATION = 0.25f;
	public float CONTINENT_SIZE = 0.05f;
	public float SEA_SIZE = 0.025f;
	public float HILL_SLOPE = 0.125f;
	public float MOUNTAIN_SLOPE = 0.175f;
	public float MOUNTAIN_ELEVATION = 0.85f;
	
	public float SETTLEMENT_DISTANCE = 0.075f;

	//
	public List<Unit> units;

	// misc
	public final String INFO_INDENT = "\t\t\t\t\t";

	public GameMap(int squaresX, int squaresY, int nSettlements)
	{
		SQUARES_X = squaresX;
		SQUARES_Y = squaresY;
		N_SQUARES = squaresX * squaresY;
		N_SETTLEMENTS = nSettlements;
		
		System.out.printf(INFO_INDENT + "# of squares: %d\n", N_SQUARES);
		System.out.printf(INFO_INDENT + "map dimensions: (%d, %d)\n", squaresX, squaresY);
		
		initSquares();
		initElevation();
		initEarthmasses();
		fixLandDepressions();
		initElevationGradient();
		initDistanceFromSea();
		initRivers();
		initTemperature();
		initPrecipitation();
		initTerrain();
		initResources();
		initSettlements();
		initUnits();
	}
	
	/*
	 * INIT
	 */

	void initSquares()
	{
		System.out.println("initializing squares");

		squares = new Square[SQUARES_X][SQUARES_Y];

		// init squares
		for (int i = 0; i < SQUARES_X; i++)
			for (int j = 0; j < SQUARES_Y; j++)
				squares[i][j] = new Square(i, j);

		Square square;
		int x1, y1, x2, y2;

		// init neighbours
		for (int x = 0; x < SQUARES_X; x++)
		{
			for (int y = 0; y < SQUARES_Y; y++)
			{
				square = squares[x][y];

				x1 = (x - 1 >= 0) ? x - 1 : SQUARES_X - 1;
				y1 = (y - 1 >= 0) ? y - 1 : SQUARES_Y - 1;
				x2 = (x + 1 < SQUARES_X) ? x + 1 : 0;
				y2 = (y + 1 < SQUARES_Y) ? y + 1 : 0;

				if (y1 < y)
					square.neighbours[Square.N] = squares[x][y1];

				if (y2 > y)
					square.neighbours[Square.S] = squares[x][y2];

				if (x1 < x)
					square.neighbours[Square.E] = squares[x1][y];

				if (x2 > x)
					square.neighbours[Square.W] = squares[x2][y];

				if (y1 < y && x1 < x)
					square.neighbours[Square.NE] = squares[x1][y1];

				if (y2 > y && x1 < x)
					square.neighbours[Square.SE] = squares[x1][y2];

				if (y1 < y && x2 > x)
					square.neighbours[Square.NW] = squares[x2][y1];

				if (y2 > y && x2 > x)
					square.neighbours[Square.SW] = squares[x2][y2];
			}
		}
	}

	void initElevation()
	{
		System.out.println("initializing elevations");

		float minElevation = Float.MAX_VALUE, maxElevation = Float.MIN_VALUE;
		float noise, elevation;

		for (int i = 0; i < SQUARES_X; i++)
		{
			for (int j = 0; j < SQUARES_Y; j++)
			{
				noise = noise(i * HEIGHT_NOISE, j * HEIGHT_NOISE);

				// value = pow(noise, HEIGHT_EXP);

				elevation = pow(sin(noise * PI * 0.5f), HEIGHT_EXP);

				squares[i][j].elevation = elevation;

				minElevation = min(elevation, minElevation);
				maxElevation = max(elevation, maxElevation);
			}
		}

		System.out.println("normalizing elevation");

		float elevation_;
		int waterSquareCount = 0;

		for (int i = 0; i < SQUARES_X; i++)
		{
			for (int j = 0; j < SQUARES_Y; j++)
			{
				elevation_ = map(squares[i][j].elevation, minElevation, maxElevation, 0, 1);
				squares[i][j].elevation = elevation_;

				if (elevation_ < WATER_ELEVATION)
					waterSquareCount++;
			}
		}

		landSquareCount = N_SQUARES - waterSquareCount;

		System.out.printf(INFO_INDENT + "# of land squares: %d,  # of water squares: %d\n", landSquareCount,
				waterSquareCount);
		System.out.printf(INFO_INDENT + "%% of water squares: %f\n", 100f * waterSquareCount / N_SQUARES);
	}

	void initEarthmasses()
	{
		System.out.println("initializing earthmasses");

		earthmasses = new ArrayList<Earthmass>();

		boolean[][] reached = new boolean[SQUARES_X][SQUARES_Y];
		Queue<Square> queue = new LinkedList<Square>();
		boolean water = false;
		int earthmassId = 0;

		for (int i = 0; i < SQUARES_X; i++)
		{
			for (int j = 0; j < SQUARES_Y; j++)
			{
				Square firstInEarthmass = squares[i][j];

				if (!reached[i][j])
				{
					water = firstInEarthmass.elevation <= WATER_ELEVATION;

					queue.add(firstInEarthmass);

					Earthmass earthmass = new Earthmass();
					earthmass.id = earthmassId++;
					earthmass.name = Names.name();
					earthmass.type = water ? EarthmassType.LAKE : EarthmassType.ISLAND;
					earthmasses.add(earthmass);

					while (!queue.isEmpty())
					{
						Square beingVisited = queue.remove();

						if (!reached[beingVisited.x][beingVisited.y])
						{
							reached[beingVisited.x][beingVisited.y] = true;
							earthmass.add(beingVisited);
							beingVisited.earthmass = earthmass;

							for (Square neighbour : beingVisited.neighbours)
							{
								if (neighbour != null && !reached[neighbour.x][neighbour.y] &&
										(neighbour.elevation > WATER_ELEVATION) != water)
									queue.add(neighbour);
							}
						}
					}
				}
			}
		}

		System.out.println("assigning earthmass types");

		int maxLandSize = -1, maxWaterSize = -1;
		int landmassCount = 0, watermassCount = 0;
		int continentCount = 0, seaCount = 0;

		for (Earthmass earthmass : earthmasses)
		{
			int size = earthmass.size();

			if (earthmass.get(0).elevation < WATER_ELEVATION) // water
			{
				watermassCount++;

				if (size > maxWaterSize)
					maxWaterSize = size;

				if (size > N_SQUARES * SEA_SIZE)
				{
					earthmass.type = EarthmassType.SEA;
					seaCount++;
				}
			}

			else
			{
				landmassCount++;
				if (size > maxLandSize)
					maxLandSize = size;

				if (size > N_SQUARES * CONTINENT_SIZE)
				{
					earthmass.type = EarthmassType.CONTINENT;
					continentCount++;
				}
			}
		}

		System.out.printf(INFO_INDENT + "# of landmasses: %d,  # of watermasses: %d\n", landmassCount, watermassCount);
		System.out.printf(INFO_INDENT + "# of continents: %d,  # of seas: %d\n", continentCount, seaCount);
		System.out.printf(INFO_INDENT + "largest landmass: %d,  largest watermass: %d\n", maxLandSize, maxWaterSize);
	}

	void fixLandDepressions()
	{
		System.out.println("fixing land depressions");

		Square square;
		boolean noDepressions, noLowestNeighbour;

		while (true)
		{
			noDepressions = true;

			for (int i = 0; i < SQUARES_X; i++)
			{
				for (int j = 0; j < SQUARES_Y; j++)
				{
					square = squares[i][j];
					noLowestNeighbour = true;

					if (square.isLand())
					{
						for (Square neighbour : square.neighbours)
						{
							if (neighbour != null && neighbour.elevation < square.elevation)
							{
								noLowestNeighbour = false;
								break;
							}
						}

						if (noLowestNeighbour)
						{
							noDepressions = false;
							if (square.isLand())
								square.elevation = (float) Arrays.stream(square.neighbours).filter(Objects::nonNull)
										.mapToDouble(s -> s.elevation).average().getAsDouble();
						}
					}
				}
			}

			if (noDepressions)
				break;
		}
	}

	void initElevationGradient()
	{
		System.out.println("calculating elevation gradient");

		peaks = new ArrayList<Square>();

		Square square;
		int highestNeighbour, lowestNeighbour;

		for (int i = 0; i < SQUARES_X; i++)
		{
			for (int j = 0; j < SQUARES_Y; j++)
			{
				square = squares[i][j];

				highestNeighbour = -1;
				lowestNeighbour = -1;

				for (int k = 0; k < 8; k++)
				{
					Square neighbour = square.neighbours[k];

					if (neighbour != null && (highestNeighbour == -1 ||
							neighbour.elevation > square.neighbours[highestNeighbour].elevation))
						highestNeighbour = k;

					if (neighbour != null && (lowestNeighbour == -1 ||
							neighbour.elevation < square.neighbours[lowestNeighbour].elevation))
						lowestNeighbour = k;
				}

				if (square.neighbours[highestNeighbour].elevation > square.elevation)
					square.highestNeighbour = highestNeighbour;
				else
				{
					square.highestNeighbour = -1;
					if (square.isLand())
						peaks.add(square);
				}

				if (square.neighbours[lowestNeighbour].elevation < square.elevation)
					square.lowestNeighbour = lowestNeighbour;
				else
					square.lowestNeighbour = -1;
			}
		}

		for (Square peak : peaks)
		{
			if (peak.isLand())
			{
				peak.elevation += (1 - peak.elevation) * random(0, PEAK_ELEVATION_BOOST);
				for (Square neighbour : peak.neighbours)
				{
					if (neighbour != null && neighbour.earthmass == peak.earthmass)
						neighbour.elevation += (1 - neighbour.elevation) * random(0, PEAK_ELEVATION_BOOST_N8);
				}
			}
		}

		for (int i = 0; i < SQUARES_X; i++)
		{
			for (int j = 0; j < SQUARES_Y; j++)
			{
				Square square_ = squares[i][j];
				square_.maxSlope = Arrays.stream(square_.neighbours).filter(Objects::nonNull)
						.map((n -> square_.elevation - n.elevation)).max(Comparator.comparing(Float::valueOf)).get();
			}
		}
	}

	void initDistanceFromSea()
	{
		System.out.println("calculating distances from sea");

		Queue<Square> queue = new LinkedList<Square>();

		float maxDistanceFromSea = 0;

		for (Square square : landSquares())
		{
			if (square.isCoast())
			{
				queue.add(square);
				square.distanceFromSea = 0;
			}
		}

		while (!queue.isEmpty())
		{
			Square beingVisited = queue.remove();

			for (Square neighbour : beingVisited.neighbours)
			{
				if (neighbour != null && !neighbour.isSea() && neighbour.distanceFromSea == -1)
				{
					neighbour.distanceFromSea = beingVisited.distanceFromSea + 1;
					maxDistanceFromSea = max(maxDistanceFromSea, neighbour.distanceFromSea);
					queue.add(neighbour);
				}
			}
		}

		for (int i = 0; i < SQUARES_X; i++)
		{
			for (int j = 0; j < SQUARES_Y; j++)
			{
				Square square = squares[i][j];

				square.distanceFromSea = !square.isSea() ? map(square.distanceFromSea, 0, maxDistanceFromSea, 0, 1)
						: 0;
			}
		}
	}

	void initRivers()
	{
		System.out.println("initializing rivers");

		rivers = new ArrayList<River>();

		River river;
		int riverId = 0;
		int direction;
		Square next;

		for (Square peak : peaks)
		{
			river = new River(peak);

			direction = peak.lowestNeighbour;
			next = peak.lowestNeighbour();

			while (next != null)
			{
				River prevRiver = next.river;

				river.addSquare(next, direction);

				if (!next.isLand())
					break;
				if (prevRiver != null)
					break;

				direction = next.lowestNeighbour;
				next = next.lowestNeighbour();
			}

			if (river.elems.size() >= 3)
			{
				river.id = riverId++;
				river.name = Names.name();
				for (RiverElement elem : river.elems)
					elem.square.river = river;
				rivers.add(river);
			}
		}

		System.out.printf(INFO_INDENT + "# of rivers: %d\n", rivers.size());
	}

	void initTemperature()
	{
		System.out.println("initializing temperatures");

		Square square;
		float x, y, temperature;

		for (int i = 0; i < SQUARES_X; i++)
		{
			for (int j = 0; j < SQUARES_Y; j++)
			{
				square = squares[i][j];

				x = ((float) i / SQUARES_X);
				y = ((float) j / SQUARES_Y);

				temperature = 1 - abs(2 * y - 1);
				temperature += 0.1f * x;

				temperature -= 0.5f * constrain(square.elevation - WATER_ELEVATION, 0, 1);
				temperature += 0.4f * constrain(sq(sin(square.distanceFromSea * PI / 2)) - 0.1f, 0, 1);

				temperature += 1f * (noise(i * 0.03f, 1 + j * 0.03f) - 0.5f);
				temperature += 0.02f * (random(0, 1) - 0.5f);

				if (square.isCoast())
					temperature *= 0.95f;
				if (square.isLand() && square.lowestNeighbour == -1)
					temperature *= 1.1f;
				square.temperature = temperature;
			}
		}

		Square[] sorted = Arrays.stream(squares).flatMap(Arrays::stream)
				.sorted((s0, s1) -> Float.compare(s0.temperature, s1.temperature)).toArray(Square[]::new);
		for (int i = 0; i < sorted.length; i++)
			sorted[i].temperature = (float) i / sorted.length;
	}

	void initPrecipitation()
	{
		System.out.println("initializing precipitations");

		Square square;
		float x, y, precipitation;

		for (int i = 0; i < SQUARES_X; i++)
		{
			for (int j = 0; j < SQUARES_Y; j++)
			{
				square = squares[i][j];

				x = (1f * i / SQUARES_X);
				y = (1f * j / SQUARES_Y);

				precipitation = sq(sin(1.5f * (y + 0.5f) * PI));
				precipitation += 0.5f * x;

				precipitation -= 0.4f * sq(sin(square.distanceFromSea * PI / 2));

				precipitation += 0.8f * (noise(1_000_000 + i * 0.03f, 1_000_001 + j * 0.03f) - 0.5f);
				precipitation += 0.02f * (random(0, 1) - 0.5f);

				if (square.isCoast())
					precipitation *= 1.05f;
				if (square.river != null)
					precipitation *= 1.1f;
				square.precipitation = precipitation;
			}
		}

		Square[] sorted = Arrays.stream(squares).flatMap(Arrays::stream)
				.sorted((s0, s1) -> Float.compare(s0.precipitation, s1.precipitation)).toArray(Square[]::new);
		for (int i = 0; i < sorted.length; i++)
			sorted[i].precipitation = (float) i / sorted.length;
	}

	void initTerrain()
	{
		System.out.println("initializing terrain");

		final int terrainDistX = Terrain.terrainDist[0].length, terrainDistY = Terrain.terrainDist.length;

		Terrain terrain;
		int tx, ty;
		float elevation, maxSlope;

		for (Square square : waterSquares())
		{
			square.terrain = Terrain.OPEN_WATERS;

			for (Square neighbour : square.neighbours)
			{
				if (neighbour != null && neighbour.isLand())
				{
					square.terrain = Terrain.COASTAL_WATERS;
					break;
				}
			}
		}

		for (Square square : landSquares())
		{
			terrain = Terrain._DEFAULT;

			tx = terrainDistX - (int) (square.precipitation * terrainDistY);
			if (tx >= 10)
				tx = 9;
			ty = terrainDistY - (int) (square.temperature * terrainDistX);
			if (ty >= 10)
				ty = 9;

			elevation = square.elevation;
			maxSlope = square.maxSlope;

			terrain = Terrain.terrainDist[tx][ty];

			if (maxSlope < 0.05f && random(0, 1) < 0.3f && square.bordersWater())
				terrain = Terrain.WETLAND;
			if (maxSlope > HILL_SLOPE + (square.isCoast() ? 0.025f : 0))
				terrain = Terrain.HILLS;
			if (elevation >= MOUNTAIN_ELEVATION || maxSlope > MOUNTAIN_SLOPE + (square.isCoast() ? 0.025f : 0))
				terrain = Terrain.MOUNTAINS;

			square.terrain = terrain;
		}

		Map<Terrain, Integer> terrainCount = new TreeMap<Terrain, Integer>();

		for (Terrain tt : Terrain.values())
			terrainCount.put(tt, 0);

		for (Square square : landSquares())
			terrainCount.put(square.terrain, terrainCount.get(square.terrain) + 1);

		terrainCount.entrySet().stream().skip(3).forEach(s -> System.out.printf(INFO_INDENT + "%-20s\t%d (%.3f%%)\n",
				s.getKey(), s.getValue(), s.getValue() * 100f / landSquareCount));
	}

	void initResources()
	{
		System.out.println("initializing resources");

		Resource resource;
		SortedSet<Entry<Resource, Float>> scores;
		float value;

		for (Square square : landSquares())
		{
			resource = Resource._DEFAULT;

			scores = new TreeSet<Entry<Resource, Float>>((r1, r2) -> r1.getValue() < r2.getValue() ? 1 : -1);

			scores.add(new SimpleEntry<Resource, Float>(Resource.GRAIN, Resource.grain(square)));
			scores.add(new SimpleEntry<Resource, Float>(Resource.LIVESTOCK, Resource.livestock(square)));
			scores.add(new SimpleEntry<Resource, Float>(Resource.FISH, Resource.fish(square)));
			scores.add(new SimpleEntry<Resource, Float>(Resource.STONE, Resource.stone(square)));
			scores.add(new SimpleEntry<Resource, Float>(Resource.WOOD, Resource.wood(square)));
			scores.add(new SimpleEntry<Resource, Float>(Resource.IRON, Resource.iron(square)));
			scores.add(new SimpleEntry<Resource, Float>(Resource.HORSES, Resource.horses(square)));
			scores.add(new SimpleEntry<Resource, Float>(Resource.GOLD, Resource.gold(square)));
			scores.add(new SimpleEntry<Resource, Float>(Resource.SPICES, Resource.spices(square)));
			scores.add(new SimpleEntry<Resource, Float>(Resource.GEMS, Resource.gems(square)));

			value = random(0, scores.stream().map(r -> r.getValue()).reduce(0f, (total, s) -> total + s));

			for (Entry<Resource, Float> resource_ : scores)
			{
				if (value <= resource_.getValue())
				{
					resource = resource_.getKey();
					break;
				}
				value -= resource_.getValue();
			}

			square.resource = resource;
		}

		Map<Resource, Integer> resourceCount = new TreeMap<Resource, Integer>();

		for (Resource rr : Resource.values())
			resourceCount.put(rr, 0);

		for (Square square : landSquares())
			resourceCount.put(square.resource, resourceCount.get(square.resource) + 1);

		resourceCount.entrySet().stream().skip(1).forEach(s -> System.out.printf(INFO_INDENT + "%-20s\t%d (%.3f%%)\n",
				s.getKey(), s.getValue(), s.getValue() * 100f / landSquareCount));
	}

	void initSettlements()
	{
		System.out.println("initializing settlement scores");

		float score;
		int seaNeighbours, lakeNeighbours;

		for (Square square : landSquares())
		{
			score = 0;

			seaNeighbours = 0;
			lakeNeighbours = 0;

			for (Square neighbour : square.neighbours)
			{
				if (neighbour != null)
				{
					if (neighbour.isSea())
						seaNeighbours++;
					if (neighbour.isLake())
						lakeNeighbours++;
				}
			}

			if (seaNeighbours > 0)
				score += (1 / 7f) * (8 - seaNeighbours);
			score += (1 / 16f) * lakeNeighbours;

			score += 0.4f * square.terrain.settlementBonus;
			score += 0.25f * sqrt(sin(square.temperature * PI));
			score += 0.25f * sqrt(sin(square.precipitation * PI));

			// Earthmass earthmass = square.earthmass;
			// if (earthmass.type == EarthmassType.ISLAND)
			// score += 0.5f * sin(map(earthmass.size(), 0, N_SQUARES *
			// CONTINENT_SIZE, 0, 1) * PI);

			if (square.river != null)
				score *= 1.5f;
			score *= random(0.8f, 1.2f);
			square.settlementScore = score;
		}

		System.out.println("picking settlements");

		Comparator<Square> cmp = (s1, s2) -> Float.compare(s2.settlementScore, s1.settlementScore);
		List<Square> sorted = landSquares().stream().sorted(cmp).collect(Collectors.toList());
		for (int i = 0; i < sorted.size(); i++)
			sorted.get(i).settlementScore = pow((float) (sorted.size() - i) / sorted.size(), 1.3f);

		settlements = new ArrayList<Settlement>();

		float radius = sqrt(sq(SQUARES_X) + sq(SQUARES_Y)) * SETTLEMENT_DISTANCE;

		while (settlements.size() < N_SETTLEMENTS && radius >= 1)
		{
			radius /= 2;
			for (Square s0 : sorted)
			{
				if (!filterByRadius(squaresList$settlements(), s0, radius).isEmpty())
					continue;

				if (settlements.size() >= N_SETTLEMENTS)
					break;

				Settlement settlement = new Settlement(s0);
				settlement.name = Names.name();
				s0.settlement = settlement;
				settlements.add(settlement);
			}

			sorted.removeAll(settlements.stream().map(s -> s.square).collect(Collectors.toList()));
		}
	}

	void initUnits()
	{
		units = new ArrayList<Unit>();

		// Square water = waterSquares().get(0);
		//
		// Unit unit = new WaterUnit(water.x, water.y, squares);
		// units.add(unit);

		Square land = landSquares().get(0);

		Unit unit = new LandUnit(land.x, land.y, squares);
		units.add(unit);
	}

	/*
	 * AUX
	 */
	public List<Square> squaresList()
	{
		return Arrays.stream(squares).flatMap(e -> Arrays.stream(e)).collect(Collectors.toList());
	}

	public List<Square> squaresList$settlements()
	{
		return settlements.stream().map(s -> s.square).collect(Collectors.toList());
	}

	public List<Square> landSquares()
	{
		return squaresList().stream().filter(s -> s.isLand()).collect(Collectors.toList());
	}

	public List<Square> waterSquares()
	{
		return squaresList().stream().filter(s -> !s.isLand()).collect(Collectors.toList());
	}

	public List<Square> filterByRadius(List<Square> squares_, Square center, float radius)
	{
		return squares_.stream().filter(s -> dist(center.x, center.y, s.x, s.y) <= radius)
				.collect(Collectors.toList());
	}
}
