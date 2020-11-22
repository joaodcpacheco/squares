package units;

import map.mapClasses.Square;

public class LandUnit extends Unit
{
	public LandUnit(int xi, int yi, Square[][] squares)
	{
		super(xi, yi, squares);
	}

	@Override
	public boolean canMoveTo(int x, int y)
	{
		return super.canMoveTo(x, y) && squares[x][y].isLand();
	}
}
