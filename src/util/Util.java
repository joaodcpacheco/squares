package util;
import java.util.ArrayList;
import java.util.List;

public class Util
{
	public static <E extends Comparable<E>> List<E> noDuplicates(List<E> ex)
	{
		List<E> ex_ = new ArrayList<E>();
		for (E e : ex)
		{
			boolean found = false;

			for (E e_ : ex_)
			{
				if (e.compareTo(e_) == 0)
				{
					found = true;
					break;
				}
			}
			if (!found)
				ex_.add(e);
		}
		return ex_;
	}
}
