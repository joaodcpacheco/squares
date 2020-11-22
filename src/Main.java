import java.util.ArrayList;
import java.util.List;

import map.GameMap;
import map.MapSize;
import map.mapClasses.Earthmass;
import map.mapClasses.Resource;
import map.mapClasses.River;
import map.mapClasses.River.RiverElement;
import map.mapClasses.Settlement;
import map.mapClasses.Square;
import map.mapClasses.Terrain;
import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import units.Path;
import units.Pathfinding;
import units.Unit;

public class Main extends PApplet
{
	public static void main(String[] args)
	{
		System.out.println("starting applet");
		PApplet.main("Main");
	}

	public int W, H;

	public PGraphics pGraphics;

	public PVector selectionStart;

	public GameMap gameMap;

	// rendering
	public float XOFF, YOFF;
	public int BG_COLOUR;
	public final int ELEVATION_LAYER_OPACITY = 50;
	public PImage landElevationLayer, waterElevationLayer, blackLayer;
	public List<MapMode> maps;
	public int mapMode = 0;
	public boolean gridMode = false;

	// misc
	public final String INFO_INDENT = "\t\t\t\t\t";

	@Override
	public void settings()
	{
		W = displayWidth;
		H = displayHeight;

		fullScreen(P2D);

		//		W = 1152;
		//		H = 648;

		//		size(W, H, P2D);
	}

	@Override
	public void setup()
	{
		surface.setVisible(false);
		surface.setTitle("Squares");
		colorMode(HSB, 240, 100, 100, 100);
		textFont(createFont("MS Gothic", 24));
		frameRate(60);
		hint(ENABLE_KEY_REPEAT);

		long start = System.currentTimeMillis();

		gameMap = new GameMap(MapSize.NORMAL.squaresX, MapSize.NORMAL.squaresY, 50);
		for (Earthmass earthmass : gameMap.earthmasses)
			earthmass.colour = earthmass.isLake() ? color(140, 100, 40) : randomColour();

			for (Settlement settlement : gameMap.settlements)
				settlement.colour = randomColour();

			selectionStart = null;

			XOFF = W / gameMap.SQUARES_X;
			YOFF = H / gameMap.SQUARES_Y;

			pGraphics = createGraphics(gameMap.SQUARES_X * (int) XOFF, gameMap.SQUARES_Y * (int) YOFF);

			BG_COLOUR = color(160, 90, 15);

			generateMaps();

			System.out.println("setup done.");
			System.out.printf(INFO_INDENT + "elapsed time: %d ms\n", System.currentTimeMillis() - start);

			background(0);
			surface.setVisible(true);
	}

	@Override
	public void draw()
	{
		background(0);

		pGraphics.beginDraw();
		pGraphics.background(0);
		pGraphics.image(maps.get(mapMode).map, 0, 0, pGraphics.width, pGraphics.height);
		if (gridMode)
			drawGrid();

		drawUnits();
		drawPath();
		if (selectionStart == null)
			drawHighlight();
		else
			drawSelection();

		pGraphics.endDraw();
		image(pGraphics, 0, 0);
	}

	@Override
	public void mousePressed()
	{
		setSelection();

		Square target = squareUnderMouse();
		if (target != null)
			gameMap.units.get(0).moveTo(target.x, target.y);
	}

	@Override
	public void mouseReleased()
	{
		releaseSelection();
	}

	@Override
	public void keyPressed()
	{
		if (key == CODED)
		{
			switch (keyCode)
			{
				case LEFT:
					gameMap.units.get(0).moveLeft();
					break;
				case RIGHT:
					gameMap.units.get(0).moveRight();
					break;
				case UP:
					gameMap.units.get(0).moveUp();
					break;
				case DOWN:
					gameMap.units.get(0).moveDown();
					break;
				default:
					break;
			}
		}
		else
		{
			switch (Character.toLowerCase(key))
			{
				// case ESC:
				// key = 0;
				// break;

				case 'a':
					decrementMapMode();
					break;
				case 'd':
					incrementMapMode();
					break;
				case 'g':
					toggleGridMode();
					break;
				default:
					break;
			}
		}
	}

	public void generateMaps()
	{
		System.out.println("rendering map modes");

		pGraphics.beginDraw();
		pGraphics.colorMode(PApplet.HSB, 240, 100, 100, 100);
		pGraphics.strokeWeight(1);
		pGraphics.textSize(12);
		pGraphics.textAlign(PApplet.LEFT, PApplet.TOP);

		maps = new ArrayList<MapMode>();

		int colour;

		pGraphics.background(0);
		blackLayer = pGraphics.get();
		drawLandElevation();
		landElevationLayer = pGraphics.get();
		landElevationLayer.format = ARGB;

		for (int i = 0; i < landElevationLayer.pixels.length; i++)
		{
			colour = landElevationLayer.pixels[i];

			if (brightness(colour) == 0 && saturation(colour) == 0)
				landElevationLayer.pixels[i] = color(0, 0, 0, 0);
			else
				landElevationLayer.pixels[i] = color(0, 0, brightness(colour),
						ELEVATION_LAYER_OPACITY);
		}
		landElevationLayer.updatePixels();

		pGraphics.background(0);
		drawWaterElevation();
		waterElevationLayer = pGraphics.get();
		waterElevationLayer.format = ARGB;

		for (int i = 0; i < waterElevationLayer.pixels.length; i++)
		{
			colour = waterElevationLayer.pixels[i];

			if (brightness(colour) == 0 && saturation(colour) == 0)
				waterElevationLayer.pixels[i] = color(0, 0, 0, 0);
			else
				waterElevationLayer.pixels[i] = color(hue(colour),
						saturation(colour), brightness(colour), 100);
		}
		waterElevationLayer.updatePixels();

		// settlements
		pGraphics.background(70);
		pGraphics.image(waterElevationLayer, 0, 0);
		drawSettlements();
		maps.add(new MapMode("settlements", pGraphics.get()));

		// elevation
		pGraphics.background(BG_COLOUR);
		drawLandElevation();
		drawWaterElevation();
		// drawPeaks();
		maps.add(new MapMode("elevation", pGraphics.get()));

		// earthmasses
		pGraphics.background(BG_COLOUR);
		drawEarthmasses();
		maps.add(new MapMode("earthmasses", pGraphics.get()));

		// // earthmasses + elevation
		// background(BG_COLOUR);
		// pg.image(landElevationLayer, 0, 0);
		// maps.add(new MapMode("earthmasses + elevation", pg.get()));

		// terrain
		pGraphics.background(BG_COLOUR);
		drawTerrain();
		drawRivers();
		maps.add(new MapMode("terrain", pGraphics.get()));

		// terrain settlement bonus
		pGraphics.background(BG_COLOUR);
		drawTerrainSettlementBonus();
		pGraphics.image(waterElevationLayer, 0, 0);
		maps.add(new MapMode("terrain settlement bonus", pGraphics.get()));

		// resources
		pGraphics.background(BG_COLOUR);
		drawResources();
		pGraphics.image(waterElevationLayer, 0, 0);
		maps.add(new MapMode("resources", pGraphics.get()));

		// settlement score
		pGraphics.background(BG_COLOUR);
		pGraphics.image(waterElevationLayer, 0, 0);
		drawSettlementScore();
		maps.add(new MapMode("settlement score", pGraphics.get()));

		// // temperature
		// pGraphics.background(BG_COLOUR);
		// pGraphics.image(waterElevationLayer, 0, 0);
		// drawTemperatures();
		// maps.add(new MapMode("temperature", pGraphics.get()));

		// // precipitation
		// pGraphics.background(BG_COLOUR);
		// pGraphics.image(waterElevationLayer, 0, 0);
		// drawPrecipitation();
		// maps.add(new MapMode("precipitation", pGraphics.get()));

		pGraphics.endDraw();
	}

	/*
	 * DRAW
	 */
	void drawLandElevation()
	{
		int colour = 0;

		for (Square square : gameMap.landSquares())
		{
			colour = color(0, 0, 100 * square.elevation);

			pGraphics.fill(colour);
			drawSquare(square);
		}
	}

	void drawPeaks()
	{
		pGraphics.stroke(200, 100, 100);

		for (Square peak : gameMap.peaks)
			drawSquareOutline(peak);
	}

	void drawWaterElevation()
	{
		float elevation;
		int colour = 0;

		for (Square square : gameMap.waterSquares())
		{
			elevation = square.elevation;

			switch (square.earthmass.type)
			{
				case SEA:
					colour = color(
							map(elevation, gameMap.WATER_ELEVATION, 0, 140, 160), 100,
							map(elevation, gameMap.WATER_ELEVATION, 0, 20, 1));
					break;
				case LAKE:
					colour = color(
							map(elevation, gameMap.WATER_ELEVATION, 0, 140, 160), 100,
							map(elevation, gameMap.WATER_ELEVATION, 0, 35, 20));
					break;
			}

			pGraphics.fill(colour);
			drawSquare(square);
		}
	}

	void drawEarthmasses()
	{
		for (Earthmass earthmass : gameMap.earthmasses)
		{
			if (earthmass.isSea())
				continue;

			pGraphics.fill(earthmass.colour);
			for (Square square : earthmass.squares)
				drawSquare(square);
		}
	}

	void drawElevationGradient()
	{
		pGraphics.fill(240, 30, 100);

		char arrow = ' ';

		for (int i = 0; i < gameMap.SQUARES_X; i++)
		{
			for (int j = 0; j < gameMap.SQUARES_Y; j++)
			{
				switch (gameMap.squares[i][j].highestNeighbour)
				{
					case -1:
						arrow = '●';
						break;
					case Square.N:
						arrow = '↑';
						break;
					case Square.S:
						arrow = '↓';
						break;
					case Square.E:
						arrow = '←';
						break;
					case Square.W:
						arrow = '→';
						break;
					case Square.NE:
						arrow = '↖';
						break;
					case Square.SE:
						arrow = '↙';
						break;
					case Square.NW:
						arrow = '↗';
						break;
					case Square.SW:
						arrow = '↘';
						break;
				}

				pGraphics.text(arrow, i * XOFF + 5, j * YOFF);
			}
		}
	}

	void drawRivers()
	{
		pGraphics.strokeWeight(1);
		pGraphics.stroke(150, 90, 50);
		Square src, dst;
		PVector srcPoint, dstPoint;

		for (River river : gameMap.rivers)
		{
			src = river.elems.get(0).square;

			for (RiverElement elem : river.elems)
			{
				dst = elem.square;
				srcPoint = squareCenter(src);

				if (!dst.isLand())
				{
					switch (elem.direction)
					{
						case -1:
						default:
							dstPoint = srcPoint;
							break;

						case Square.N:
							dstPoint = new PVector(src.x * XOFF + XOFF / 2, src.y * YOFF);
							break;
						case Square.S:
							dstPoint = new PVector(src.x * XOFF + XOFF / 2, (src.y + 1) * YOFF);
							break;
						case Square.E:
							dstPoint = new PVector(src.x * XOFF, src.y * YOFF + YOFF / 2);
							break;
						case Square.W:
							dstPoint = new PVector((src.x + 1) * XOFF, src.y * YOFF + YOFF / 2);
							break;

						case Square.NE:
							dstPoint = new PVector(src.x * XOFF, src.y * YOFF);
							break;
						case Square.NW:
							dstPoint = new PVector((src.x + 1) * XOFF, src.y * YOFF);
							break;
						case Square.SE:
							dstPoint = new PVector(src.x * XOFF, (src.y + 1) * YOFF);
							break;
						case Square.SW:
							dstPoint = new PVector((src.x + 1) * XOFF, (src.y + 1) * YOFF);
							break;
					}
				}
				else
					dstPoint = squareCenter(dst);

				pGraphics.line(srcPoint.x, srcPoint.y, dstPoint.x, dstPoint.y);

				src = elem.square;
				srcPoint = squareCenter(src);
			}

			src = river.elems.get(0).square;
		}
	}

	void drawTemperatures()
	{
		final int aColour = color(5, 1, 90), bColour = color(5, 80, 20);

		Square square;
		int colour;

		for (int i = 0; i < gameMap.SQUARES_X; i++)
		{
			for (int j = 0; j < gameMap.SQUARES_Y; j++)
			{
				square = gameMap.squares[i][j];

				colour = lerpColor(aColour, bColour, square.temperature);

				pGraphics.fill(colour);
				drawSquare(square);
			}
		}
	}

	void drawPrecipitation()
	{
		final int aColour = color(150, 1, 90), bColour = color(150, 80, 20);

		Square square;
		int colour;

		for (int i = 0; i < gameMap.SQUARES_X; i++)
		{
			for (int j = 0; j < gameMap.SQUARES_Y; j++)
			{
				square = gameMap.squares[i][j];

				colour = lerpColor(aColour, bColour, square.precipitation);

				pGraphics.fill(colour);
				drawSquare(square);
			}
		}
	}

	void drawTerrain()
	{
		int colour;
		Terrain terrain;

		for (Square square : gameMap.squaresList())
		{
			colour = color(0);

			terrain = square.terrain;

			if (terrain != null)
				colour = color(terrain.h(), terrain.s(), terrain.b());

			pGraphics.fill(colour);
			drawSquare(square);
		}
	}

	void drawResources()
	{
		int colour;
		Resource resource;

		for (Square square : gameMap.landSquares())
		{
			colour = color(0);

			resource = square.resource;

			if (resource != null)
				colour = color(resource.h(), resource.s(), resource.b());

			pGraphics.fill(colour);
			drawSquare(square);
		}
	}

	void drawTerrainSettlementBonus()
	{
		int colour;
		Terrain terrain;

		for (Square square : gameMap.landSquares())
		{
			colour = color(0);

			terrain = square.terrain;
			if (terrain != null)
			{
				switch (terrain.settlementBonus)
				{
					case 0:
						colour = color(0, 85, 85);
						break;
					case 1:
						colour = color(20, 85, 85);
						break;
					case 2:
						colour = color(40, 85, 85);
						break;
					case 3:
						colour = color(60, 85, 85);
						break;
					case 4:
						colour = color(80, 85, 85);
						break;
					case 5:
						colour = color(120, 85, 85);
						break;
				}
			}

			pGraphics.fill(colour);
			drawSquare(square);
		}
	}

	void drawSettlementScore()
	{
		final int aColour = color(70, 1, 90), bColour = color(70, 80, 20);

		int colour;
		for (Square square : gameMap.landSquares())
		{
			colour = lerpColor(aColour, bColour, square.settlementScore);

			pGraphics.fill(colour);
			drawSquare(square);
		}
	}

	void drawSettlements()
	{
		for (Settlement settlement : gameMap.settlements)
		{
			pGraphics.fill(settlement.colour);
			drawSquare(settlement.square);
		}
	}

	void drawHighlight()
	{
		Square square = squareUnderMouse();

		if (square != null)
		{
			pGraphics.fill(0, 100, 100, 30);
			drawSquare(square);
			pGraphics.stroke(0, 100, 100, 60);
			drawSquareOutline(square);

			pGraphics.fill(0, 100, 100);
			pGraphics.text(square.info(), pGraphics.width / 2, pGraphics.height / 2);
		}
	}

	void drawHighestAndLowestNeighbour()
	{
		Square square = squareUnderMouse();

		if (square.highestNeighbour() != null)
		{
			pGraphics.fill(160, 100, 100, 40);
			drawSquare(square.highestNeighbour());
		}

		if (square.lowestNeighbour() != null)
		{
			pGraphics.fill(80, 100, 100, 40);
			drawSquare(square.lowestNeighbour());
		}
	}

	void drawPath()
	{
		pGraphics.rectMode(CENTER);
		Square src = squareUnderMouse();
		if (src != null)
		{
			Path path = new Pathfinding(gameMap.squares)
					.path(gameMap.units.get(0), src);
			Square dst = path.end();
			Square square_ = null;
			if (dst != null)
			{
				pGraphics.stroke(0, 100, 100);
				pGraphics.fill(0, 100, 100);
				pGraphics.strokeWeight(2);
				for (Square square : path.squares())
				{
					if (square_ != null)
					{
						PVector point = squareCenter(square), point_ = squareCenter(square_);
						pGraphics.line(point.x, point.y, point_.x, point_.y);
						pGraphics.square(point.x, point.y, 3);
					}

					square_ = square;
				}

				pGraphics.fill(80, 100, 100);
				pGraphics.text(String.format("%d -> %.3f\n", path.size(), path.totalDistance), (dst.x + 1) * XOFF,
						dst.y * YOFF);
			}
		}
		pGraphics.rectMode(CORNER);
	}

	void drawUnits()
	{
		for (Unit unit : gameMap.units)
		{
			Square square = gameMap.squares[unit.x][unit.y];

			if (square != null)
			{
				pGraphics.fill(25, 100, 100, 30);
				drawSquare(square);
				pGraphics.stroke(25, 100, 100, 60);
				drawSquareOutline(square);
			}
		}
	}

	void drawGrid()
	{
		pGraphics.strokeWeight(1);
		pGraphics.stroke(0, 0, 0);

		for (int i = 0; i < gameMap.SQUARES_X - 1; i++)
		{
			float lineX = XOFF * (i + 1);
			pGraphics.line(lineX, 0, lineX, pGraphics.height);
		}

		for (int i = 0; i < gameMap.SQUARES_Y - 1; i++)
		{
			float lineY = YOFF * (i + 1);
			pGraphics.line(0, lineY, pGraphics.width, lineY);
		}
	}

	public void drawSelection()
	{
		PVector selectionEnd = new PVector(constrain(mouseX, 0, pGraphics.width),
				constrain(mouseY, 0, pGraphics.height - 1));

		if (selectionStart != null && (selectionStart.x != selectionEnd.x || selectionStart.y != selectionEnd.y))
		{
			pGraphics.fill(0, 0, 80, 15);
			pGraphics.stroke(80);
			pGraphics.rectMode(CORNERS);
			pGraphics.rect(selectionStart.x, selectionStart.y, selectionEnd.x, selectionEnd.y);
			pGraphics.rectMode(CORNER);
		}
	}

	/*
	 * AUX
	 */

	public void setSelection()
	{
		selectionStart = new PVector(constrain(mouseX, 0, pGraphics.width),
				constrain(mouseY, 0, pGraphics.height));
	}

	public void releaseSelection()
	{
		selectionStart = null;
	}

	public Square squareUnderMouse()
	{
		int x = (int) (mouseX / XOFF), y = (int) (mouseY / YOFF);

		if (x >= 0 && x < gameMap.SQUARES_X && y >= 0 && y < gameMap.SQUARES_Y)
			return gameMap.squares[x][y];
		else
			return null;
	}

	PVector squareCenter(Square square)
	{
		return new PVector(square.x * XOFF + XOFF / 2, square.y * YOFF + YOFF / 2);
	}

	void drawSquare(Square square)
	{
		pGraphics.noStroke();
		pGraphics.rect(square.x * XOFF, square.y * YOFF, XOFF, YOFF);
	}

	void drawSquareOutline(Square square)
	{
		pGraphics.noFill();
		pGraphics.strokeWeight(1);
		pGraphics.rect(square.x * XOFF, square.y * YOFF, XOFF, YOFF);
	}

	public void decrementMapMode()
	{
		mapMode--;
		if (mapMode < 0)
			mapMode = maps.size() - 1;
	}

	public void incrementMapMode()
	{
		mapMode++;
		if (mapMode >= maps.size())
			mapMode = 0;
	}

	public void toggleGridMode()
	{
		gridMode = !gridMode;
	}

	public int randomColour()
	{
		float h = 240 * pow(random(0, 1), 2f);
		float s = 100 * random(0.3f, 0.7f);
		float b = 100 * random(0.2f, 0.9f);

		return color(h, s, b);
	}

	public class MapMode
	{
		String name;
		public PImage map;

		public MapMode(String name, PImage map)
		{
			this.name = name;
			this.map = map;
		}
	}

	public void setMapModeByName(String name)
	{
		for (int i = 0; i < maps.size(); i++)
		{
			if (maps.get(i).name.equals(name))
				mapMode = i;
		}
	}
}
