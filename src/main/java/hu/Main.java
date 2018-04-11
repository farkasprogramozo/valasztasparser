package hu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Main {
	
	// TABLE 1 && 2 merged
	private static SortedMap<String, SumOfVoters> sumOfSubstantiveVotersTables = new TreeMap<>();
	
	private static SortedMap<String, CandidateVotes> candidateVotesTables = new TreeMap<>();	
	private static SortedMap<String, List<SumOfVoters>> sumOfListVotersTables = new TreeMap<>();
	private static SortedMap<String, List<PartyVote>> partyVotesTables = new TreeMap<>();
	private static SortedMap<String, SumOfVoters> sumOfGentilitialListVotersTables = new TreeMap<>();

	public static void main(String[] args) throws IOException {
		List<Path> paths = Files.walk(Paths.get(ConstantHelper.BASEFOLDER))
        	.filter(Files::isRegularFile)
        	.collect(Collectors.toList());
		
		List<File> evkFiles = new ArrayList<>();
		List<String> filePaths = new ArrayList<>();
		
		paths.forEach(path -> {
			String pathAsString = path.toString();
			String[] pathElements = pathAsString.split("\\\\");
			if (pathElements.length == 8
					&& pathElements[3].equals("szavossz") 
					&& pathElements[5].startsWith("M") 
					&& pathElements[6].startsWith("T") 
					&& pathElements[7].endsWith(".html") 
					&& pathElements[7].matches(".*[0-9].*")
					) {
				evkFiles.add(path.toFile());
				filePaths.add(path.toFile().getPath());
			}
		});
		
		File outputFolder = new File(ConstantHelper.OUTPUTFOLDER);
		if (!outputFolder.exists()) {
			outputFolder.mkdirs();
		}
		

		FileUtils.cleanDirectory(outputFolder);
		
		// separate csv reports
		for (File evkFile : evkFiles) {			
			String html = FileUtils.readFileToString(evkFile, ConstantHelper.ENCODING);
			System.out.println("------------------" + evkFile.getAbsolutePath() + "-------------------");
			Document doc = Jsoup.parse(html);			
			String parsedContent = parse(evkFile.getPath(), doc);		
			
			File resultFile = new File(outputFolder + "\\" +  FilenameUtils.removeExtension(evkFile.getPath()).concat(".csv"));
			FileUtils.touch(resultFile);
			FileUtils.writeStringToFile(resultFile, parsedContent, ConstantHelper.ENCODING);
		}

		
		// sum report of substantive votes
		File substantiveVotersFile = new File(outputFolder + "\\" + "egyeni_osszes.csv");
		FileUtils.touch(substantiveVotersFile);
		// header			
		FileUtils.writeStringToFile(substantiveVotersFile, "ID" + ConstantHelper.DELIMITER
				+ "A n�vjegyz�kben �s a mozg�urn�t ig�nyl� v�laszt�polg�rok jegyz�k�ben l�v� v�laszt�polg�rok sz�ma" + ConstantHelper.DELIMITER
				+ "Szavaz�k�nt megjelent v�laszt�polg�rok sz�ma" + ConstantHelper.DELIMITER
				+ "Urn�ban l�v�, b�lyegz�lenyomat n�lk�li szavaz�lapok sz�ma" + ConstantHelper.DELIMITER
				+ "Urn�ban l�v� leb�lyegzett szavaz�lapok sz�ma" + ConstantHelper.DELIMITER
				+ "Elt�r�s a szavaz�k�nt megjelentek sz�m�t�l (t�bblet: + / hi�nyz�: -)" + ConstantHelper.DELIMITER
				+ "�rv�nytelen szavaz�lapok sz�ma" + ConstantHelper.DELIMITER
				+ "�rv�nyes szavaz�lapok sz�ma"	+ ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		for (Entry<String, SumOfVoters> entry : sumOfSubstantiveVotersTables.entrySet()) {
			FileUtils.writeStringToFile(substantiveVotersFile, entry.getKey() + ";" + entry.getValue() + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		}
		
		
		// sum report of candidate votes
		File candidateVotesFile = new File(outputFolder + "\\" + "egyeni_szavazatok.csv");
		FileUtils.touch(candidateVotesFile);
		// header			
		FileUtils.writeStringToFile(candidateVotesFile, "ID" + ConstantHelper.DELIMITER
				+ "A jel�lt neve" + ConstantHelper.DELIMITER 
				+ "Jel�l� szervezet(ek)" + ConstantHelper.DELIMITER
				+ "Kapott �rv�nyes szavazat" + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		for (Entry<String, CandidateVotes> entry : candidateVotesTables.entrySet()) {
			FileUtils.writeStringToFile(candidateVotesFile, entry.getKey() + ConstantHelper.LINESEPARATOR + entry.getValue() + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		}
		
		// sum report of substantive votes
		File listVotersFile = new File(outputFolder + "\\" + "listas_osszes.csv");
		FileUtils.touch(listVotersFile);
		// header			
		FileUtils.writeStringToFile(listVotersFile, "ID" + ConstantHelper.DELIMITER
				+ "A n�vjegyz�kben �s a mozg�urn�t ig�nyl� v�laszt�polg�rok jegyz�k�ben l�v� v�laszt�polg�rok sz�ma" + ConstantHelper.DELIMITER
				+ "Szavaz�k�nt megjelent v�laszt�polg�rok sz�ma" + ConstantHelper.DELIMITER
				+ "Urn�ban l�v�, b�lyegz�lenyomat n�lk�li szavaz�lapok sz�ma" + ConstantHelper.DELIMITER
				+ "Urn�ban l�v� leb�lyegzett szavaz�lapok sz�ma" + ConstantHelper.DELIMITER
				+ "Elt�r�s a szavaz�k�nt megjelentek sz�m�t�l (t�bblet: + / hi�nyz�: -)" + ConstantHelper.DELIMITER
				+ "�rv�nytelen szavaz�lapok sz�ma" + ConstantHelper.DELIMITER
				+ "�rv�nyes szavaz�lapok sz�ma"	+ ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		for (Entry<String, List<SumOfVoters>> entry : sumOfListVotersTables.entrySet()) {
			FileUtils.writeStringToFile(listVotersFile, entry.getKey() + ConstantHelper.LINESEPARATOR + entry.getValue() + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		}

		
		// sum report of party votes
		File partyVotesFile = new File(outputFolder + "\\" + "listas_szavazatok.csv");
		FileUtils.touch(partyVotesFile);
		// header			
		FileUtils.writeStringToFile(partyVotesFile, "ID" + ConstantHelper.DELIMITER
				+ "A p�rtlista neve" + ConstantHelper.DELIMITER
				+ "Szavazat" + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		for (Entry<String, List<PartyVote>> entry : partyVotesTables.entrySet()) {
			FileUtils.writeStringToFile(partyVotesFile, entry.getKey() + ConstantHelper.LINESEPARATOR + entry.getValue() + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		}
		
		
		// sum report of gentilitial party votes
		File gentilitialListVotersFile = new File(outputFolder + "\\" + "listas_osszes_nemzetisegi.csv");
		FileUtils.touch(gentilitialListVotersFile);
		// header			
		FileUtils.writeStringToFile(gentilitialListVotersFile, "ID" + ConstantHelper.DELIMITER
				+ "A n�vjegyz�kben �s a mozg�urn�t ig�nyl� v�laszt�polg�rok jegyz�k�ben l�v� v�laszt�polg�rok sz�ma" + ConstantHelper.DELIMITER
				+ "Szavaz�k�nt megjelent v�laszt�polg�rok sz�ma" + ConstantHelper.DELIMITER
				+ "Urn�ban l�v�, b�lyegz�lenyomat n�lk�li szavaz�lapok sz�ma" + ConstantHelper.DELIMITER
				+ "Urn�ban l�v� leb�lyegzett szavaz�lapok sz�ma" + ConstantHelper.DELIMITER
				+ "Elt�r�s a szavaz�k�nt megjelentek sz�m�t�l (t�bblet: + / hi�nyz�: -)" + ConstantHelper.DELIMITER
				+ "�rv�nytelen szavaz�lapok sz�ma" + ConstantHelper.DELIMITER
				+ "�rv�nyes szavaz�lapok sz�ma"	+ ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		for (Entry<String, SumOfVoters> entry : sumOfGentilitialListVotersTables.entrySet()) {
			FileUtils.writeStringToFile(gentilitialListVotersFile, entry.getKey() + ConstantHelper.LINESEPARATOR + entry.getValue() + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		}


	}

	private static String parse(String filePath, Document doc) {
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

			
			// TABLE 1: Jegyz�kbe vett szavaz�k 
			if (i == 1 && headerContent.toString().split(ConstantHelper.DELIMITER)[0].contains("A n�vjegyz�kben �s a mozg�urn�t ig�nyl� v�laszt�polg�rok jegyz�k�ben szerepl� v�laszt�polg�rok sz�ma")) {
				SumOfVoters registratedVoterTable = new SumOfVoters();
				String contentAsString = content.toString();
				int registratedVoters = Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[0].replaceAll(" ", ""));
				int voted = Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[1].split(" ")[0]);

				if (!sumOfSubstantiveVotersTables.containsKey(filePath)) {				
					registratedVoterTable.setRegistratedVoters(registratedVoters);
					registratedVoterTable.setVoted(voted);
					sumOfSubstantiveVotersTables.put(filePath, registratedVoterTable);
				} else {
					sumOfSubstantiveVotersTables.get(filePath).setRegistratedVoters(registratedVoters);
					sumOfSubstantiveVotersTables.get(filePath).setVoted(voted);
				}
			}
			
			// merge with table 1 as it is the same as table 4 and 6
			// TABLE 2: Egy�ni szavaz�lapok 
			if (i == 2 && headerContent.toString().split(ConstantHelper.DELIMITER)[0].contains("Urn�ban l�v�, b�lyegz�lenyomat n�lk�li szavaz�lapok sz�ma")) {
				SumOfVoters sumOfListVoters = new SumOfVoters();
				String contentAsString = content.toString();
				int noStamper = Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[0].replaceAll(" ", ""));
				int stamped = Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[1].replaceAll(" ", ""));
				int differenceBetweenVotersAndStamped = Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[2].replaceAll(" ", ""));
				int invalid = Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[3].replaceAll(" ", ""));
				int valid = Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[4].replaceAll(" ", "").replaceAll(System.getProperty("line.separator"), ""));
				
				if (!sumOfSubstantiveVotersTables.containsKey(filePath)) {				
					sumOfListVoters.setNoStamper(noStamper);
					sumOfListVoters.setStamped(stamped);
					sumOfListVoters.setDifferenceBetweenVotersAndStamped(differenceBetweenVotersAndStamped);
					sumOfListVoters.setInvalid(invalid);
					sumOfListVoters.setValid(valid);
					sumOfSubstantiveVotersTables.put(filePath, sumOfListVoters);
				} else {
					sumOfSubstantiveVotersTables.get(filePath).setNoStamper(noStamper);
					sumOfSubstantiveVotersTables.get(filePath).setStamped(stamped);
					sumOfSubstantiveVotersTables.get(filePath).setDifferenceBetweenVotersAndStamped(differenceBetweenVotersAndStamped);
					sumOfSubstantiveVotersTables.get(filePath).setInvalid(invalid);
					sumOfSubstantiveVotersTables.get(filePath).setValid(valid);
				}
			}
			
			// TABLE 3: Egy�ni szavazatok
			if (i == 3 && headerContent.toString().split(ConstantHelper.DELIMITER)[1].contains("A jel�lt neve")) {
				CandidateVotes candidateVotes = new CandidateVotes();
				String[] contentAsRows = content.toString().split("\\r?\\n");
				for (String contentAsString : contentAsRows) {
					CandidateVote vote = new CandidateVote();
					vote.setCandidateName(contentAsString.split(ConstantHelper.DELIMITER)[1]);
					vote.setPartyOfCandidate(contentAsString.split(ConstantHelper.DELIMITER)[2]);
					vote.setVotes(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[3].replaceAll(" ", "").replaceAll(System.getProperty("line.separator"), "")));
					candidateVotes.add(vote);
				}
				candidateVotesTables.put(filePath, candidateVotes);
			}
			
			// TABLE 4: List�s szavaz�lapok
			if (i == 4 && headerContent.toString().split(ConstantHelper.DELIMITER)[1].contains("A n�vjegyz�kben �s a mozg�urn�t ig�nyl� v�laszt�polg�rok jegyz�k�ben l�v� v�laszt�polg�rok sz�ma")) {
				List<SumOfVoters> sumOfListVotersList = new ArrayList<>();
				String[] contentAsRows = content.toString().split("\\r?\\n");				
				for (String contentAsString : contentAsRows) {
					SumOfVoters sumOfListVoters = new SumOfVoters();
					sumOfListVoters.setRegistratedVoters(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[1].replaceAll(" ", "")));
					sumOfListVoters.setVoted(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[2].replaceAll(" ", "")));
					sumOfListVoters.setNoStamper(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[3].replaceAll(" ", "")));
					sumOfListVoters.setStamped(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[4].replaceAll(" ", "")));
					sumOfListVoters.setDifferenceBetweenVotersAndStamped(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[5].replaceAll(" ", "").replaceAll("-", "0")));
					sumOfListVoters.setInvalid(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[6].replaceAll(" ", "").replaceAll("-", "0")));
					sumOfListVoters.setValid(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[7].replaceAll(" ", "")));
					sumOfListVotersList.add(sumOfListVoters);
				}
				sumOfListVotersTables.put(filePath, sumOfListVotersList);
			}
			
			// TABLE 5: List�s szavazatok
			if (i == 5 && headerContent.toString().split(ConstantHelper.DELIMITER)[1].contains("A p�rtlista neve")) {
				List<PartyVote> partyVoteList = new ArrayList<>();
				String[] contentAsRows = content.toString().split("\\r?\\n");
				for (String contentAsString : contentAsRows) {
					PartyVote vote = new PartyVote();
					vote.setParty(contentAsString.split(ConstantHelper.DELIMITER)[1]);
					vote.setVotes(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[2].replaceAll(" ", "").replaceAll(System.getProperty("line.separator"), "")));
					partyVoteList.add(vote);
				}
				partyVotesTables.put(filePath, partyVoteList);
			}
			
			// TABLE 6: Nemzetis�gi list�k adatai
			if (i == 6 && headerContent.toString().split(ConstantHelper.DELIMITER)[1].contains("A n�vjegyz�kben �s a mozg�urn�t ig�nyl� v�laszt�polg�rok jegyz�k�ben l�v� v�laszt�polg�rok sz�ma")) {
				SumOfVoters sumOfListVoters = new SumOfVoters();
				String[] contentAsRows = content.toString().split("\\r?\\n");
				for (String contentAsString : contentAsRows) {
					if (contentAsString != null && !"".equals(contentAsString)) {
						sumOfListVoters.setRegistratedVoters(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[1].replaceAll(" ", "")));
						sumOfListVoters.setVoted(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[2].replaceAll(" ", "")));
						sumOfListVoters.setNoStamper(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[3].replaceAll(" ", "")));
						sumOfListVoters.setStamped(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[4].replaceAll(" ", "")));
						sumOfListVoters.setDifferenceBetweenVotersAndStamped(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[5].replaceAll(" ", "")));
						sumOfListVoters.setInvalid(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[6].replaceAll(" ", "")));
						sumOfListVoters.setValid(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[7].replaceAll(" ", "").replaceAll(System.getProperty("line.separator"), "")));
						sumOfGentilitialListVotersTables.put(filePath, sumOfListVoters);
					}
				}
			}
			
			
			
			result.append(headerContent);
			result.append(content);
			result.append(ConstantHelper.LINESEPARATOR);
		}
		return result.toString();
	}

	private static String getTableCols(Elements cols) {
		StringBuilder result = new StringBuilder();
		for (int k = 0; k < cols.size(); k++) {
			result.append(cols.get(k).text());
		    if (k + 1 != cols.size()) {
		    	result.append(ConstantHelper.DELIMITER);
		    }
		}
		if (!cols.isEmpty()) {
			result.append(ConstantHelper.LINESEPARATOR);
		}
		return result.toString();
	}

}
