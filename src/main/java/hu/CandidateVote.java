package hu;

public class CandidateVote {

	String candidateName;
	String partyOfCandidate;
	Integer votes;
	
	public String getCandidateName() {
		return candidateName;
	}
	
	public void setCandidateName(String candidateName) {
		this.candidateName = candidateName;
	}
	
	public String getPartyOfCandidate() {
		return partyOfCandidate;
	}
	
	public void setPartyOfCandidate(String partyOfCandidate) {
		this.partyOfCandidate = partyOfCandidate;
	}
	
	public Integer getVotes() {
		return votes;
	}
	
	public void setVotes(Integer votes) {
		this.votes = votes;
	}	
	
}
