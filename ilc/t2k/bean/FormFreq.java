/**
 * $Id: FormFreq.java,v 1.3 2005/11/07 13:48:18 simone Exp $
 */
package ilc.t2k.bean;

public class FormFreq {

	protected String form;
	protected Integer freq;
	protected int df = 1;
	protected double measure = 0;
	/**
	 * @return Returns the form.
	 */
	public String getForm() {
		return form;
	}
	/**
	 * @param form The form to set.
	 */
	public void setForm(String form) {
		this.form = form;
	}
	/**
	 * @return Returns the freq.
	 */
	public Integer getFreq() {
		return freq;
	}
	/**
	 * @param freq The freq to set.
	 */
	public void setFreq(Integer freq) {
		this.freq = freq;
	}
	/**
	 * @return the df
	 */
	public Integer getDf() {
		return df;
	}
	/**
	 * @param df the df to set
	 */
	public void setDf(int df) {
		this.df = df;
	}
	/**
	 * @return the measure
	 */
	public double getMeasure() {
		return measure;
	}
	/**
	 * @param measure the measure to set
	 */
	public void setMeasure(double measure) {
		this.measure = measure;
	}

	
}
