package hu;

import java.util.ArrayList;
import java.util.List;

public class CandidateVotes {
	
	List<CandidateVote> candidateVoteList = new ArrayList<>();
	
	public void add(CandidateVote vote) {
		candidateVoteList.add(vote);
	}

	public List<CandidateVote> getCandidateVoteList() {
		return candidateVoteList;
	}	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (CandidateVote vote : candidateVoteList) {
			sb.append(vote.candidateName + ConstantHelper.DELIMITER + vote.partyOfCandidate + ConstantHelper.DELIMITER + vote.votes + ConstantHelper.LINESEPARATOR);
		}
		return sb.toString();
	}
	
}
