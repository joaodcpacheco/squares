package units;

import map.mapClasses.Earthmass;
import map.mapClasses.Square;

public class Unit
{
	public int x, y;
	public final Square[][] squares;
	public final int X, Y;
	
	public Unit(int xi, int yi, Square[][] squares)
	{
		x = xi;
		y = yi;
		this.squares = squares;
		X = squares.length;
		Y = squares[0].length;
	}

	public void moveLeft()
	{
		if (x > 0 && canMoveTo(x - 1, y))
			x--;
	}

	public void moveRight()
	{
		if (x < X - 1 && canMoveTo(x + 1, y))
			x++;
	}

	public void moveUp()
	{
		if (y > 0 && canMoveTo(x, y - 1))
			y--;
	}

	public void moveDown()
	{
		if (y < Y - 1 && canMoveTo(x, y + 1))
			y++;
	}

	public void moveTo(int x, int y)
	{
		if (canMoveTo(x, y))
		{
			this.x = x;
			this.y = y;
		}
	}
	
	public boolean canMoveTo(int x, int y)
	{
		if (squares[x][y] != null && x >= 0 && y >= 0 && x < X && y < Y)
		{
			Earthmass targetEm = squares[x][y].earthmass;
			
			if (targetEm == thisSquare().earthmass)
				return true;
		}
		return false;
	}
	
	public static boolean canMoveTo(Square src, Square dst, Square[][] squares)
	{
		return new Unit(src.x, src.y, squares).canMoveTo(dst.x, dst.y);
	}
		
	public Square thisSquare()
	{
		return squares[x][y];
	}
}
