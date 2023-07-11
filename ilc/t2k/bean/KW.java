/**
 * $Id: KW.java,v 1.2 2005/11/07 13:48:18 simone Exp $
 */
package ilc.t2k.bean;

public class KW {

	private String idDoc; 
	private String kwid; 
	private String irg;
	private String ird;
	
	public KW (String _idDoc, String _kwid, String _irg, String _ird){
		idDoc = _idDoc;
		kwid = _kwid;
		irg = _irg;
		ird = _ird;
	}

	/**
	 * @return Returns the idDoc.
	 */
	public String getIdDoc() {
		return idDoc;
	}

	/**
	 * @param idDoc The idDoc to set.
	 */
	public void setIdDoc(String idDoc) {
		this.idDoc = idDoc;
	}

	/**
	 * @return Returns the ird.
	 */
	public String getIrd() {
		return ird;
	}

	/**
	 * @param ird The ird to set.
	 */
	public void setIrd(String ird) {
		this.ird = ird;
	}

	/**
	 * @return Returns the irg.
	 */
	public String getIrg() {
		return irg;
	}

	/**
	 * @param irg The irg to set.
	 */
	public void setIrg(String irg) {
		this.irg = irg;
	}

	/**
	 * @return Returns the kwid.
	 */
	public String getKwid() {
		return kwid;
	}

	/**
	 * @param kwid The kwid to set.
	 */
	public void setKwid(String kwid) {
		this.kwid = kwid;
	}

}