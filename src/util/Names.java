package util;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Names
{
	private static final String NAMES_FILE = "assets/names.txt";

	private static final List<String> names;

	static
	{
		names = new ArrayList<String>(1_000);

		try
		{
			Scanner in = new Scanner(new File(NAMES_FILE));

			while (in.hasNextLine())
				names.add(in.nextLine());

			in.close();
		}
		catch (FileNotFoundException e)
		{

		}
	}

	public static String name()
	{
		if (names.size() <= 0)
			return "";
		int i = new Random().nextInt(names.size());
		return names.remove(i);
	}
}
