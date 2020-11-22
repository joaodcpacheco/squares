package map.mapClasses;

import java.util.ArrayList;
import java.util.List;

public class River
{
	public int id;
	public String name;
	public List<RiverElement> elems;

	public River(Square source)
	{
		elems = new ArrayList<RiverElement>();
		addSquare(source, -1);
	}

	public void addSquare(Square square, int direction)
	{
		elems.add(new RiverElement(square, direction));
	}

	public class RiverElement
	{
		public Square square;
		public int direction;

		RiverElement(Square square, int direction)
		{
			this.square = square;
			this.direction = direction;
		}

		@Override
		public String toString()
		{
			return square.toString();
		}
	}

	@Override
	public String toString()
	{
		return String.format("%d: River %s", id, name);
		// return elems.toString();
	}
}
