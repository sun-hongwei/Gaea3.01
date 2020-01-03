package com.wh.gaea.interfaces;

import com.wh.gaea.form.ChildForm;

public interface ISubForm {
	public void onStart(Object param);
	
	public void setParentForm(ChildForm form);

	public ChildForm getParentForm();

	public Object getResult();

}
