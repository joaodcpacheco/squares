package map.mapClasses;

public class Settlement
{
	public Square square;
	public String name;
	public int colour;
	
	public Settlement(Square square)
	{
		this.square = square;
		name = "";
		colour = 0;
	}
	
	@Override
	public String toString()
	{
		return String.format("%s %s\n", name, square.toString());
	}
}
