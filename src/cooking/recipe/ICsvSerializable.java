/**
 * 
 */
package cooking.recipe;

import java.io.IOException;

/**
 * @author sburton
 *
 * Interface to define any objects in our system that can serialize / deserialize themselves to CSV format
 */
public interface ICsvSerializable {
	public String convertToCsvFormat();
	public void loadFromCsvFormat(String input) throws IOException;
}
