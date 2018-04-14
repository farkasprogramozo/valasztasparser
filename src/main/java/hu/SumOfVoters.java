package hu;

public class SumOfVoters extends SumOfSubstantiveVoters {

	String voteType;
	Integer registratedVoters;
	Integer voted;
	
	
	public String getVoteType() {
		return voteType;
	}

	public void setVoteType(String voteType) {
		this.voteType = voteType;
	}

	public Integer getRegistratedVoters() {
		return registratedVoters;
	}
	
	public void setRegistratedVoters(Integer registratedVoters) {
		this.registratedVoters = registratedVoters;
	}
	
	public Integer getVoted() {
		return voted;
	}
	
	public void setVoted(Integer voted) {
		this.voted = voted;
	}
	
	@Override
	public String toString() {
		String result = "";
		if (voteType != null && !"".equals(voteType)) {
			result = result.concat(voteType + ConstantHelper.DELIMITER);
		}
		result = result.concat(registratedVoters + ConstantHelper.DELIMITER + voted + ConstantHelper.DELIMITER 
				+ noStamper + ConstantHelper.DELIMITER + stamped + ConstantHelper.DELIMITER 
				+ differenceBetweenVotersAndStamped + ConstantHelper.DELIMITER + invalid + ConstantHelper.DELIMITER + valid);
		return result;
	}
	
}
