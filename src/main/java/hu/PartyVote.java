package hu;

public class PartyVote {
	
	String party;
	Integer votes;
	
	public String getParty() {
		return party;
	}
	
	public void setParty(String party) {
		this.party = party;
	}
	
	public Integer getVotes() {
		return votes;
	}
	
	public void setVotes(Integer votes) {
		this.votes = votes;
	}	
	
	@Override
	public String toString() {
		return party + ConstantHelper.DELIMITER + votes;
	}
	
}
