package map;

public enum MapSize
{
	SMALL(64, 36, 20),
	NORMAL(96, 54, 40),
	BIG(128, 72, 80),
	HUGE(192, 108, 160);
	
	public int squaresX, squaresY, nSettlements;
	
	MapSize(int squaresX, int squaresY, int nSettlements)
	{
		this.squaresX = squaresX;
		this.squaresY = squaresY;
		this.nSettlements = nSettlements;
	}
}
