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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {

	private static final String BASEFOLDER = "valasztas.hu";

	public static void main(String[] args) throws IOException {
		List<Path> paths = Files.walk(Paths.get(BASEFOLDER))
        	.filter(Files::isRegularFile)
        	.collect(Collectors.toList());
		
		List<File> files = new ArrayList<>();
		
		paths.forEach(path -> {
			System.out.println(path.toString());
			files.add(path.toFile());
		});
		
		for (File file : files) {
			
			String html = FileUtils.readFileToString(file, "UTF-8");
			System.out.println("--------------------" + file.getAbsolutePath() + "------------------");
			Document doc = Jsoup.parse(html);
			Element ele = doc.body();
			
			// TODO content parse, define Object
			Object parsedContent = parse(doc);
		}
	}

	private static Object parse(Document doc) {
		Elements table = doc.select("table");
		for (int i = 0; i < table.size(); i++) {
			Elements rows = table.get(i).select("tr");
			for (int j = 0; j < rows.size(); j++) {
			    Element row = rows.get(j);
			    Elements header = row.select("th");
			    Elements cols = row.select("td");
				for (int k = 0; k < cols.size(); k++) {
				    System.out.println(cols.get(k).text());
				}
				for (int k = 0; k < header.size(); k++) {
				    System.out.println(header.get(k).text());
				}
			}
		}
		return null;
	}

}
