package hu.megharaplak.te.csalo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {

	private static final String DELIMITER = ";";
	private static final String BASEFOLDER = "valasztas.hu";
	private static final String OUTPUTFOLDER = "output";

	public static void main(String[] args) throws IOException {
		List<Path> paths = Files.walk(Paths.get(BASEFOLDER))
        	.filter(Files::isRegularFile)
        	.collect(Collectors.toList());
		
		List<File> files = new ArrayList<>();
		
		paths.forEach(path -> {
			System.out.println(path.toString());
			files.add(path.toFile());
		});
		
		File outputFolder = new File(OUTPUTFOLDER);
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}

		FileUtils.cleanDirectory(outputFolder);
		for (File file : files) {
			
			String html = FileUtils.readFileToString(file, "UTF-8");
			System.out.println("------------------" + file.getAbsolutePath() + "------------------");
			Document doc = Jsoup.parse(html);
			
			// TODO find out what types of tables do we have, and how we can distinct them from the others
			String parsedContent = parse(doc);
			System.out.println(parsedContent);

			File resultFile = new File(outputFolder + "\\" +  FilenameUtils.removeExtension(file.getPath()).concat(".csv"));
			FileUtils.touch(resultFile);
			FileUtils.writeStringToFile(resultFile, parsedContent);
			System.out.println("----------------END OF" + file.getAbsolutePath() + "--------------");
			System.out.println("");
			System.out.println("");
			System.out.println("");
		}
	}

	private static String parse(Document doc) {
		StringBuilder result = new StringBuilder();
		Elements table = doc.select("table");
		for (int i = 0; i < table.size(); i++) {
			Elements rows = table.get(i).select("tr");
			StringBuilder headerContent = new StringBuilder();
			StringBuilder content = new StringBuilder();
			
			for (int j = 0; j < rows.size(); j++) {
			    Element row = rows.get(j);
			    Elements header = row.select("th");
			    Elements cols = row.select("td");

			    headerContent.append(getTableCols(header));
			    content.append(getTableCols(cols));
			}
			result.append(headerContent);
			result.append(content);
			result.append(System.getProperty("line.separator"));
		}
		return result.toString();
	}

	private static String getTableCols(Elements cols) {
		StringBuilder result = new StringBuilder();
		for (int k = 0; k < cols.size(); k++) {
			result.append(cols.get(k).text());
		    if (k + 1 != cols.size()) {
		    	result.append(DELIMITER);
		    }
		}
		if (!cols.isEmpty()) {
			result.append(System.getProperty("line.separator"));
		}
		return result.toString();
	}

}
