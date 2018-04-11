package hu;

import java.util.ArrayList;
import java.util.List;

public class PartyVotes {
	
	List<PartyVote> partyVoteList = new ArrayList<>();
	
	public void add(PartyVote vote) {
		partyVoteList.add(vote);
	}

	public List<PartyVote> getCandidateVoteList() {
		return partyVoteList;
	}	

}
