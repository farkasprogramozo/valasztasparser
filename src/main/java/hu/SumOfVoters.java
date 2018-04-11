package hu;

public class SumOfVoters extends SumOfSubstantiveVoters {

	Integer registratedVoters;
	Integer voted;
	
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
		return registratedVoters + ConstantHelper.DELIMITER + voted + ConstantHelper.DELIMITER 
				+ noStamper + ConstantHelper.DELIMITER + stamped + ConstantHelper.DELIMITER 
				+ differenceBetweenVotersAndStamped + ConstantHelper.DELIMITER + invalid + ConstantHelper.DELIMITER + valid;
	}
	
}
