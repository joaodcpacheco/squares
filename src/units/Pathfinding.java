package units;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.stream.Collectors;

import map.mapClasses.Square;
import units.Path.PathElement;

public class Pathfinding
{
	private static final float RIVER_PENALTY = 2f;

	public Square[][] squares;

	public Pathfinding(Square[][] squares)
	{
		this.squares = squares;
	}

	// dijkstra
	public Path path(Unit unit, Square dst)
	{
		Square src = squares[unit.x][unit.y];
		
		if (src == dst || !Unit.canMoveTo(src, dst, squares))
			return new Path(); // empty

		Map<Square, Float> distances = new HashMap<Square, Float>();
		Map<Square, Square> froms = new HashMap<Square, Square>();

		Queue<Square> queue = new PriorityQueue<Square>(
				(s1, s2) -> Float.compare(distances.get(s1), distances.get(s2)));

		distances.put(src, 0f);
		froms.put(src, null);
		queue.add(src);

		while (!queue.isEmpty())
		{
			Square u = queue.poll();
			if (u == dst)
				break;

			for (Square v : validNeighbours(u))
			{
				Float prevDistance = distances.get(v);
				Float newDistance = distances.get(u) + distance(u, v);

				if (prevDistance == null || newDistance < prevDistance)
				{
					queue.remove(v);
					distances.put(v, newDistance);
					froms.put(v, u);
					queue.add(v);
				}
			}
		}

		Square u = dst;

		List<PathElement> elems = new ArrayList<PathElement>();
		while (u != null)
		{
			elems.add(0, new PathElement(u, distances.get(u)));
			u = froms.get(u);
		}

		Path path = new Path();

		float prevDistance = 0f;
		for (PathElement elem : elems)
		{
			path.add(elem.square, elem.distance - prevDistance);
			prevDistance = elem.distance;
		}

		return path;
	}

	// src and dst are presumed to be adjacent non-null squares on the same
	// earthmass
	public float distance(Square src, Square dst)
	{
		int dx = Math.abs(src.x - dst.x), dy = Math.abs(src.y - dst.y);
		boolean diagAdj = false, riverCrossing = false;

		if (dx + dy == 0) // same coordinates
			return 0;
		if (dx + dy == 1) // orthogonally adjacent
			if (dst.hasRiver())
				riverCrossing = true;

		if (dx == 1 && dy == 1) // diagonally adjacent
		{
			diagAdj = true;

			if (dst.hasRiver())
				riverCrossing = true;
			else
			{
				Square i1 = squares[src.x][dst.y], i2 = squares[dst.x][src.y];

				if (i1.hasRiver())
				{
					if (i2.hasRiver() || !i2.isLand())
						riverCrossing = true;
				}
				else
				{
					if (i2.hasRiver())
					{
						if (!i1.isLand())
							riverCrossing = true;
					}
				}
			}
		}

		float distance = diagAdj ? 1.414f : 1f;

		distance *= 1f / (0.5f * dst.terrain.settlementBonus + 1);

		if (src.isLand())
		{
			distance *= 1f + Math.pow(Math.max(0, (dst.elevation - src.elevation)), 2);
			distance *= 1f + Math.pow(dst.temperature - src.temperature, 2);
			distance *= 1f + Math.pow(dst.precipitation - src.precipitation, 2);

			if (riverCrossing)
				distance *= RIVER_PENALTY;
		}

		return distance;
	}

	private List<Square> validNeighbours(Square src)
	{
		if (src == null)
			return null;
		return Arrays.stream(src.neighbours).filter(n -> n != null && Unit.canMoveTo(src, n, squares))
				.collect(Collectors.toList());
	}
}
