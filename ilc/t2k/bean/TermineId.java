package ilc.t2k.bean;

public class TermineId {
	
	private String term;
	private Integer id;
	/**
	 * @return Returns the id.
	 */
	public  TermineId(String _term, Integer _id){
		term = _term;
		id = _id;
	}
	public Integer getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(Integer id) {
		this.id = id;
	}
	/**
	 * @return Returns the term.
	 */
	public String getTerm() {
		return term;
	}
	/**
	 * @param term The term to set.
	 */
	public void setTerm(String term) {
		this.term = term;
	}

}
