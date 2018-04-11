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
			
			String rawcontent = ele.toString().replace("&nbsp;", "");
			System.out.println(rawcontent);
			
			// TODO content parse, define Object
			// Object parsedContent = parse();
			// System.out.println(parsedContent.toString());
		}
	}

	private static Object parse() {
		// TODO Auto-generated method stub
		return null;
	}

	private static String getRowByKeyword(String line, String key) {
		if (line.contains(key)) {
			return line.replaceAll("\\<[^>]*>","").replace(key, "");
		}
		return null;
	}

}
