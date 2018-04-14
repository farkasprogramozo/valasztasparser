package hu;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
	
	private static final boolean WRITE_PARSED_RESULT_INTO_FILE = false;
	// TABLE 1 && 2 merged
	private static SortedMap<String, SumOfVoters> sumOfSubstantiveVotersTables = new TreeMap<>();
	private static SortedMap<String, SumOfVoters> sumOfSubstantiveVotersFromAnotherAreaTables = new TreeMap<>();	
	private static SortedMap<String, CandidateVotes> candidateVotesTables = new TreeMap<>();	
	private static SortedMap<String, List<SumOfVoters>> sumOfListVotersTables = new TreeMap<>();
	private static SortedMap<String, SumOfVoters> sumOfListVotersFromAnotherAreaTables = new TreeMap<>();
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
			
			if (WRITE_PARSED_RESULT_INTO_FILE) {
				File resultFile = new File(outputFolder + "\\" +  FilenameUtils.removeExtension(evkFile.getPath()).concat(".csv"));
				FileUtils.touch(resultFile);
				FileUtils.writeStringToFile(resultFile, parsedContent, ConstantHelper.ENCODING);
			}
		}

		System.out.println("Fixing differences...");

		for (Entry<String, SumOfVoters> entry : sumOfSubstantiveVotersTables.entrySet()) {
			SumOfVoters sumOfVoters = entry.getValue();
			sumOfVoters.setDifferenceBetweenVotersAndStamped(sumOfVoters.getStamped() - sumOfVoters.getVoted());
		}

		for (Entry<String, List<SumOfVoters>> entry : sumOfListVotersTables.entrySet()) {
			List<SumOfVoters> sumOfVotersList = entry.getValue();
			for (SumOfVoters sumOfVoters : sumOfVotersList) {
				sumOfVoters.setDifferenceBetweenVotersAndStamped(sumOfVoters.getStamped() - sumOfVoters.getVoted());
			}
		}

		for (Entry<String, SumOfVoters> entry : sumOfGentilitialListVotersTables.entrySet()) {
			SumOfVoters sumOfVoters = entry.getValue();
			sumOfVoters.setDifferenceBetweenVotersAndStamped(sumOfVoters.getStamped() - sumOfVoters.getVoted());
		}

		System.out.println("Generating reports...");
		
		// sum report of substantive votes
		File substantiveVotersFile = new File(outputFolder + "\\" + "egyeni_osszes.csv");
		FileUtils.touch(substantiveVotersFile);
		// header			
		FileUtils.writeStringToFile(substantiveVotersFile, "ID" + ConstantHelper.DELIMITER
				+ "A névjegyzékben és a mozgóurnát igénylõ választópolgárok jegyzékében lévõ választópolgárok száma" + ConstantHelper.DELIMITER
				+ "Szavazóként megjelent választópolgárok száma" + ConstantHelper.DELIMITER
				+ "Urnában lévõ, bélyegzõlenyomat nélküli szavazólapok száma" + ConstantHelper.DELIMITER
				+ "Urnában lévõ lebélyegzett szavazólapok száma" + ConstantHelper.DELIMITER
				+ "Eltérés a szavazóként megjelentek számától (többlet: + / hiányzó: -)" + ConstantHelper.DELIMITER
				+ "Érvénytelen szavazólapok száma" + ConstantHelper.DELIMITER
				+ "Érvényes szavazólapok száma"	+ ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		for (Entry<String, SumOfVoters> entry : sumOfSubstantiveVotersTables.entrySet()) {
			FileUtils.writeStringToFile(substantiveVotersFile, entry.getKey() + ";" + entry.getValue() + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		}
		
		
		// sum report of candidate votes
		File candidateVotesFile = new File(outputFolder + "\\" + "egyeni_szavazatok.csv");
		FileUtils.touch(candidateVotesFile);
		for (Entry<String, CandidateVotes> entry : candidateVotesTables.entrySet()) {
			FileUtils.writeStringToFile(candidateVotesFile, entry.getKey() + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
			// header			
			FileUtils.writeStringToFile(candidateVotesFile, "A jelölt neve" + ConstantHelper.DELIMITER 
					+ "Jelölõ szervezet(ek)" + ConstantHelper.DELIMITER
					+ "Kapott érvényes szavazat" + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
			FileUtils.writeStringToFile(candidateVotesFile, entry.getValue() + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		}
		
		// sum report of substantive votes
		File listVotersFile = new File(outputFolder + "\\" + "listas_osszes.csv");
		FileUtils.touch(listVotersFile);
		// header			
		FileUtils.writeStringToFile(listVotersFile, "ID" + ConstantHelper.DELIMITER
				+ "Tipus" + ConstantHelper.DELIMITER
				+ "A névjegyzékben és a mozgóurnát igénylõ választópolgárok jegyzékében lévõ választópolgárok száma" + ConstantHelper.DELIMITER
				+ "Szavazóként megjelent választópolgárok száma" + ConstantHelper.DELIMITER
				+ "Urnában lévõ, bélyegzõlenyomat nélküli szavazólapok száma" + ConstantHelper.DELIMITER
				+ "Urnában lévõ lebélyegzett szavazólapok száma" + ConstantHelper.DELIMITER
				+ "Eltérés a szavazóként megjelentek számától (többlet: + / hiányzó: -)" + ConstantHelper.DELIMITER
				+ "Érvénytelen szavazólapok száma" + ConstantHelper.DELIMITER
				+ "Érvényes szavazólapok száma"	+ ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		for (Entry<String, List<SumOfVoters>> entry : sumOfListVotersTables.entrySet()) {
			for (SumOfVoters sumOfVoters: entry.getValue()) {
				FileUtils.writeStringToFile(listVotersFile, entry.getKey() + ConstantHelper.DELIMITER + sumOfVoters.toString() + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
			}			
		}

		
		// sum report of party votes
		File partyVotesFile = new File(outputFolder + "\\" + "listas_szavazatok.csv");
		File partyVotesOrderedFile = new File(outputFolder + "\\" + "listas_szavazatok_rendezett.csv");
		FileUtils.touch(partyVotesFile);
		FileUtils.touch(partyVotesOrderedFile);
		// header			
		StringBuilder header = new StringBuilder("ID" + ConstantHelper.DELIMITER);
		for(int i = 0; i <= 23; i++) {
			header.append("A pártlista neve" + ConstantHelper.DELIMITER	+ "Szavazat");
			if (i == 23) {
				header.append(ConstantHelper.LINESEPARATOR);
			} else {
				header.append(ConstantHelper.DELIMITER);				
			}
		}
		FileUtils.writeStringToFile(partyVotesFile, header.toString(), ConstantHelper.ENCODING, true);		
		FileUtils.writeStringToFile(partyVotesOrderedFile, header.toString(), ConstantHelper.ENCODING, true);
		for (Entry<String, List<PartyVote>> entry : partyVotesTables.entrySet()) {
			StringBuilder sb = new StringBuilder();
			
			for (int i = 0; i < entry.getValue().size(); i++) {
				PartyVote partyVote = entry.getValue().get(i);
				sb.append(partyVote.toString());
				if (i + 1 != entry.getValue().size()) {
					sb.append(ConstantHelper.DELIMITER);
				}
			}
			FileUtils.writeStringToFile(partyVotesFile, entry.getKey() + ConstantHelper.DELIMITER + sb.toString() + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
			
			Collections.sort(entry.getValue(), new Comparator<PartyVote>() {
				@Override
				public int compare(PartyVote o1, PartyVote o2) {
					return o2.votes.compareTo(o1.votes);
				}
			});
			
			StringBuilder sbOfOrdered = new StringBuilder();
			for (int i = 0; i < entry.getValue().size(); i++) {
				PartyVote partyVote = entry.getValue().get(i);
				sbOfOrdered.append(partyVote.toString());
				if (i + 1 != entry.getValue().size()) {
					sbOfOrdered.append(ConstantHelper.DELIMITER);
				}
			}
			FileUtils.writeStringToFile(partyVotesOrderedFile, entry.getKey() + ConstantHelper.DELIMITER + sbOfOrdered.toString() + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		}
		
		
		// sum report of gentilitial party votes
		File gentilitialListVotersFile = new File(outputFolder + "\\" + "listas_osszes_nemzetisegi.csv");
		FileUtils.touch(gentilitialListVotersFile);
		// header			
		FileUtils.writeStringToFile(gentilitialListVotersFile, "ID" + ConstantHelper.DELIMITER
				+ "Tipus" + ConstantHelper.DELIMITER
				+ "A névjegyzékben és a mozgóurnát igénylõ választópolgárok jegyzékében lévõ választópolgárok száma" + ConstantHelper.DELIMITER
				+ "Szavazóként megjelent választópolgárok száma" + ConstantHelper.DELIMITER
				+ "Urnában lévõ, bélyegzõlenyomat nélküli szavazólapok száma" + ConstantHelper.DELIMITER
				+ "Urnában lévõ lebélyegzett szavazólapok száma" + ConstantHelper.DELIMITER
				+ "Eltérés a szavazóként megjelentek számától (többlet: + / hiányzó: -)" + ConstantHelper.DELIMITER
				+ "Érvénytelen szavazólapok száma" + ConstantHelper.DELIMITER
				+ "Érvényes szavazólapok száma"	+ ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		for (Entry<String, SumOfVoters> entry : sumOfGentilitialListVotersTables.entrySet()) {
			FileUtils.writeStringToFile(gentilitialListVotersFile, entry.getKey() + ConstantHelper.DELIMITER + entry.getValue() + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		}
		

		// sum report of candidate votes with voters another area
		File candidateVotersFromAnotherAreaFile = new File(outputFolder + "\\" + "egyeni_atjelentkezett.csv");
		FileUtils.touch(candidateVotersFromAnotherAreaFile);
		// header			
		FileUtils.writeStringToFile(candidateVotersFromAnotherAreaFile, "ID" + ConstantHelper.DELIMITER
				+ "Az átjelentkezett választópolgárok névjegyzékében és a mozgóurnát igénylõ átjelentkezett választópolgárok jegyzékében szereplõ választópolgárok száma" + ConstantHelper.DELIMITER
				+ "Átjelentkezéssel szavazóként megjelent választópolgárok száma"	+ ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		for (Entry<String, SumOfVoters> entry : sumOfSubstantiveVotersFromAnotherAreaTables.entrySet()) {
			FileUtils.writeStringToFile(candidateVotersFromAnotherAreaFile, entry.getKey() + ConstantHelper.DELIMITER + entry.getValue() + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		}
		
		
		// sum report of party votes with voters another area
		File partyVotersFromAnotherAreaFile = new File(outputFolder + "\\" + "egyeni_atjelentkezett.csv");
		FileUtils.touch(partyVotersFromAnotherAreaFile);
		// header			
		FileUtils.writeStringToFile(partyVotersFromAnotherAreaFile, "ID" + ConstantHelper.DELIMITER
				+ "Az átjelentkezett választópolgárok névjegyzékében és a mozgóurnát igénylõ átjelentkezett választópolgárok jegyzékében szereplõ választópolgárok száma" + ConstantHelper.DELIMITER
				+ "Átjelentkezéssel szavazóként megjelent választópolgárok száma"	+ ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		for (Entry<String, SumOfVoters> entry : sumOfListVotersFromAnotherAreaTables.entrySet()) {
			FileUtils.writeStringToFile(partyVotersFromAnotherAreaFile, entry.getKey() + ConstantHelper.DELIMITER + entry.getValue() + ConstantHelper.LINESEPARATOR, ConstantHelper.ENCODING, true);
		}

	}

	private static String parse(String filePath, Document doc) {
		boolean hasVotersFromAnotherArea = doc.toString().contains("Átjelentkezett választópolgárok");
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

			if (hasVotersFromAnotherArea) {
				parseTableWithVotersFromAnotherArea(filePath, i, headerContent, content);
			} else {
				parseTable(filePath, i, headerContent, content);
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

	private static void parseTableWithVotersFromAnotherArea(String filePath, int i, StringBuilder headerContent, StringBuilder content) {
		// TABLE 1: Jegyzékbe vett szavazók 
		if (i == 2 && headerContent.toString().split(ConstantHelper.DELIMITER)[0].contains("A névjegyzékben és a mozgóurnát igénylõ választópolgárok jegyzékében szereplõ választópolgárok száma")) {
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
		
		// voters from another area
		if (i == 3 && headerContent.toString().split(ConstantHelper.DELIMITER)[0].contains("Az átjelentkezett választópolgárok névjegyzékében és a mozgóurnát igénylõ átjelentkezett választópolgárok jegyzékében szereplõ választópolgárok száma")) {
			SumOfVoters registratedVoterTable = new SumOfVoters();
			String contentAsString = content.toString();
			registratedVoterTable.setRegistratedVoters(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[0].replaceAll(" ", "")));
			registratedVoterTable.setVoted(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[1].split(" ")[0]));

			sumOfSubstantiveVotersFromAnotherAreaTables.put(filePath, registratedVoterTable);
		}
		
		// merge with table 1 as it is the same as table 4 and 6
		// TABLE 2: Egyéni szavazólapok 
		if (i == 4 && headerContent.toString().split(ConstantHelper.DELIMITER)[0].contains("Urnában lévõ, bélyegzõlenyomat nélküli szavazólapok száma")) {
			SumOfVoters sumOfListVoters = new SumOfVoters();
			String contentAsString = content.toString();
			int noStamper = Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[0].replaceAll(" ", ""));
			int stamped = Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[1].replaceAll(" ", ""));
			int invalid = Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[3].replaceAll(" ", ""));
			int valid = Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[4].replaceAll(" ", "").replaceAll(System.getProperty("line.separator"), ""));
			
			if (!sumOfSubstantiveVotersTables.containsKey(filePath)) {				
				sumOfListVoters.setNoStamper(noStamper);
				sumOfListVoters.setStamped(stamped);
				sumOfListVoters.setInvalid(invalid);
				sumOfListVoters.setValid(valid);
				sumOfSubstantiveVotersTables.put(filePath, sumOfListVoters);
			} else {
				sumOfSubstantiveVotersTables.get(filePath).setNoStamper(noStamper);
				sumOfSubstantiveVotersTables.get(filePath).setStamped(stamped);
				sumOfSubstantiveVotersTables.get(filePath).setInvalid(invalid);
				sumOfSubstantiveVotersTables.get(filePath).setValid(valid);
			}
		}
		
		// TABLE 3: Egyéni szavazatok
		if (i == 5 && headerContent.toString().split(ConstantHelper.DELIMITER)[1].contains("A jelölt neve")) {
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
		
		// TABLE 4: Listás szavazólapok
		if (i == 7 && headerContent.toString().split(ConstantHelper.DELIMITER)[1].contains("A névjegyzékben és a mozgóurnát igénylõ választópolgárok jegyzékében lévõ választópolgárok száma")) {
			List<SumOfVoters> sumOfListVotersList = new ArrayList<>();
			String[] contentAsRows = content.toString().split("\\r?\\n");				
			for (String contentAsString : contentAsRows) {
				SumOfVoters sumOfListVoters = new SumOfVoters();
				sumOfListVoters.setVoteType(contentAsString.split(ConstantHelper.DELIMITER)[0].replaceAll(" ", ""));
				sumOfListVoters.setRegistratedVoters(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[1].replaceAll(" ", "")));
				sumOfListVoters.setVoted(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[2].replaceAll(" ", "")));
				sumOfListVoters.setNoStamper(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[3].replaceAll(" ", "")));
				sumOfListVoters.setStamped(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[4].replaceAll(" ", "")));
				sumOfListVoters.setInvalid(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[6].replaceAll(" ", "").replaceAll("-", "0")));
				sumOfListVoters.setValid(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[7].replaceAll(" ", "")));
				sumOfListVotersList.add(sumOfListVoters);
			}
			sumOfListVotersTables.put(filePath, sumOfListVotersList);
		}

		// voters from another area
		if (i == 8 && headerContent.toString().split(ConstantHelper.DELIMITER)[0].contains("Az átjelentkezett választópolgárok névjegyzékében és a mozgóurnát igénylõ átjelentkezett választópolgárok jegyzékében szereplõ választópolgárok száma")) {
			SumOfVoters registratedVoterTable = new SumOfVoters();
			String contentAsString = content.toString();
			registratedVoterTable.setRegistratedVoters(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[0].replaceAll(" ", "")));
			registratedVoterTable.setVoted(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[1].split(" ")[0]));
			sumOfListVotersFromAnotherAreaTables.put(filePath, registratedVoterTable);
		}
		
		// TABLE 5: Listás szavazatok
		if (i == 9 && headerContent.toString().split(ConstantHelper.DELIMITER)[1].contains("A pártlista neve")) {
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
		
		// TABLE 8: Nemzetiségi listák adatai
		if (i == 10 && headerContent.toString().split(ConstantHelper.DELIMITER)[1].contains("A névjegyzékben és a mozgóurnát igénylõ választópolgárok jegyzékében lévõ választópolgárok száma")) {
			SumOfVoters sumOfListVoters = new SumOfVoters();
			String[] contentAsRows = content.toString().split("\\r?\\n");
			for (String contentAsString : contentAsRows) {
				if (contentAsString != null && !"".equals(contentAsString)) {
					sumOfListVoters.setVoteType(contentAsString.split(ConstantHelper.DELIMITER)[0].replaceAll(" ", ""));
					sumOfListVoters.setRegistratedVoters(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[1].replaceAll(" ", "")));
					sumOfListVoters.setVoted(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[2].replaceAll(" ", "")));
					sumOfListVoters.setNoStamper(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[3].replaceAll(" ", "")));
					sumOfListVoters.setStamped(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[4].replaceAll(" ", "")));
					sumOfListVoters.setInvalid(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[6].replaceAll(" ", "").replaceAll("-", "0")));
					sumOfListVoters.setValid(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[7].replaceAll(" ", "").replaceAll("-", "0").replaceAll(System.getProperty("line.separator"), "")));
					sumOfGentilitialListVotersTables.put(filePath, sumOfListVoters);
				}
			}
		}
	}

	private static void parseTable(String filePath, int i, StringBuilder headerContent, StringBuilder content) {
		// TABLE 1: Jegyzékbe vett szavazók 
		if (i == 2 && headerContent.toString().split(ConstantHelper.DELIMITER)[0].contains("A névjegyzékben és a mozgóurnát igénylõ választópolgárok jegyzékében szereplõ választópolgárok száma")) {
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
		// TABLE 2: Egyéni szavazólapok 
		if (i == 3 && headerContent.toString().split(ConstantHelper.DELIMITER)[0].contains("Urnában lévõ, bélyegzõlenyomat nélküli szavazólapok száma")) {
			SumOfVoters sumOfListVoters = new SumOfVoters();
			String contentAsString = content.toString();
			int noStamper = Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[0].replaceAll(" ", ""));
			int stamped = Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[1].replaceAll(" ", ""));
			int invalid = Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[3].replaceAll(" ", ""));
			int valid = Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[4].replaceAll(" ", "").replaceAll(System.getProperty("line.separator"), ""));
			
			if (!sumOfSubstantiveVotersTables.containsKey(filePath)) {				
				sumOfListVoters.setNoStamper(noStamper);
				sumOfListVoters.setStamped(stamped);
				sumOfListVoters.setInvalid(invalid);
				sumOfListVoters.setValid(valid);
				sumOfSubstantiveVotersTables.put(filePath, sumOfListVoters);
			} else {
				sumOfSubstantiveVotersTables.get(filePath).setNoStamper(noStamper);
				sumOfSubstantiveVotersTables.get(filePath).setStamped(stamped);
				sumOfSubstantiveVotersTables.get(filePath).setInvalid(invalid);
				sumOfSubstantiveVotersTables.get(filePath).setValid(valid);
			}
		}
		
		// TABLE 3: Egyéni szavazatok
		if (i == 4 && headerContent.toString().split(ConstantHelper.DELIMITER)[1].contains("A jelölt neve")) {
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
		
		// TABLE 4: Listás szavazólapok
		if (i == 6 && headerContent.toString().split(ConstantHelper.DELIMITER)[1].contains("A névjegyzékben és a mozgóurnát igénylõ választópolgárok jegyzékében lévõ választópolgárok száma")) {
			List<SumOfVoters> sumOfListVotersList = new ArrayList<>();
			String[] contentAsRows = content.toString().split("\\r?\\n");				
			for (String contentAsString : contentAsRows) {
				SumOfVoters sumOfListVoters = new SumOfVoters();
				sumOfListVoters.setVoteType(contentAsString.split(ConstantHelper.DELIMITER)[0].replaceAll(" ", ""));
				sumOfListVoters.setRegistratedVoters(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[1].replaceAll(" ", "")));
				sumOfListVoters.setVoted(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[2].replaceAll(" ", "")));
				sumOfListVoters.setNoStamper(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[3].replaceAll(" ", "")));
				sumOfListVoters.setStamped(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[4].replaceAll(" ", "")));
				sumOfListVoters.setInvalid(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[6].replaceAll(" ", "").replaceAll("-", "0")));
				sumOfListVoters.setValid(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[7].replaceAll(" ", "")));
				sumOfListVotersList.add(sumOfListVoters);
			}
			sumOfListVotersTables.put(filePath, sumOfListVotersList);
		}
		
		// TABLE 5: Listás szavazatok
		if (i == 7 && headerContent.toString().split(ConstantHelper.DELIMITER)[1].contains("A pártlista neve")) {
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
		
		// TABLE 8: Nemzetiségi listák adatai
		if (i == 6 && headerContent.toString().split(ConstantHelper.DELIMITER)[1].contains("A névjegyzékben és a mozgóurnát igénylõ választópolgárok jegyzékében lévõ választópolgárok száma")) {
			SumOfVoters sumOfListVoters = new SumOfVoters();
			String[] contentAsRows = content.toString().split("\\r?\\n");
			for (String contentAsString : contentAsRows) {
				if (contentAsString != null && !"".equals(contentAsString)) {
					sumOfListVoters.setVoteType(contentAsString.split(ConstantHelper.DELIMITER)[0].replaceAll(" ", ""));
					sumOfListVoters.setRegistratedVoters(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[1].replaceAll(" ", "")));
					sumOfListVoters.setVoted(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[2].replaceAll(" ", "")));
					sumOfListVoters.setNoStamper(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[3].replaceAll(" ", "")));
					sumOfListVoters.setStamped(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[4].replaceAll(" ", "")));
					sumOfListVoters.setInvalid(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[6].replaceAll(" ", "").replaceAll("-", "0")));
					sumOfListVoters.setValid(Integer.parseInt(contentAsString.split(ConstantHelper.DELIMITER)[7].replaceAll(" ", "").replaceAll("-", "0").replaceAll(System.getProperty("line.separator"), "")));
					sumOfGentilitialListVotersTables.put(filePath, sumOfListVoters);
				}
			}
		}
	}

}
