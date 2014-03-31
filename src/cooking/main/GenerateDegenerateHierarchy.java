package cooking.main;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class GenerateDegenerateHierarchy {

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		String filename = "data/ingredients_newFormat.csv";
		FileReader reader = new FileReader(filename);
		BufferedReader bReader = new BufferedReader(reader);
		
		StringBuffer input = new StringBuffer();
		
		
		while (bReader.ready()) {
			String line = bReader.readLine();
			
			if (!line.startsWith("*")) {
				String[] splitLine = line.split(",");
				input.append("*" + splitLine[0] + "\n**" + splitLine[0] + "\n" + line + "\n");
			}
		}
		
		String outputFilename = "data/ingredients_degeneratehierarchy.csv";
		FileWriter writer = new FileWriter(outputFilename);
		BufferedWriter bWriter = new BufferedWriter(writer);
		
		bWriter.write(input.toString());
		
		bReader.close();
		bWriter.close();
		
		System.out.println("Done***");
	}

}
