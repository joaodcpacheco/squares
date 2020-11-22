package units;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import map.mapClasses.Square;

public class Path
{
	public List<PathElement> elems;
	public float totalDistance;

	public Path()
	{
		elems = new ArrayList<PathElement>();
	}

	public void add(Square square, float distance)
	{
		elems.add(new PathElement(square, distance));
		totalDistance += distance;
	}

	public Square start()
	{
		return elems.get(0).square;
	}

	public Square end()
	{
		return !elems.isEmpty() ? elems.get(elems.size() - 1).square : null;
	}
	
	public int size()
	{
		return elems.size();
	}
	
	public List<Square> squares()
	{
		return elems.stream().map(e -> e.square).collect(Collectors.toList());
	}
	
	@Override
	public String toString()
	{
		return elems.toString();
	}
	
	public static class PathElement
	{
		public Square square;
		public float distance;

		public PathElement(Square square, float distance)
		{
			this.square = square;
			this.distance = distance;
		}
		
		@Override
		public String toString()
		{
			return String.format("%s %f", square.toString(), distance);
		}
	}
}
