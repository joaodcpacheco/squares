package map.mapClasses;

import java.util.ArrayList;
import java.util.List;

public class Earthmass
{
	public int id;
	public String name;
	public List<Square> squares;
	public EarthmassType type;
	public int colour;

	public Earthmass()
	{
		squares = new ArrayList<Square>();
		type = null;
		colour = -1;
	}

	public void add(Square square)
	{
		squares.add(square);
	}

	public Square get(int i)
	{
		return squares.get(i);
	}

	public int size()
	{
		return squares.size();
	}

	public boolean isLand()
	{
		return type == EarthmassType.CONTINENT || type == EarthmassType.ISLAND;
	}

	public boolean isSea()
	{
		return type == EarthmassType.SEA;
	}

	public boolean isLake()
	{
		return type == EarthmassType.LAKE;
	}

	@Override
	public String toString()
	{
		return String.format("%d: %s%s (%s)", id, name, type.desc, type.toString());
	}

	public enum EarthmassType
	{
		CONTINENT(""), ISLAND(" Isl."), SEA(" Sea"), LAKE(" Lake");

		String desc;

		EarthmassType(String desc)
		{
			this.desc = desc;
		}
	}
}
