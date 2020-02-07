package org.evansnet.common.ui;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;

public class CredentialsComposite extends Composite {
	private Text txtUserID;
	private Text txtPassword;
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public CredentialsComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));
		
		Label lblUserId = new Label(this, SWT.NONE);
		GridData gdLblUserId = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gdLblUserId.widthHint = 95;
		lblUserId.setLayoutData(gdLblUserId);
		lblUserId.setText("User ID:            ");
		
		txtUserID = new Text(this, SWT.BORDER);
		txtUserID.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblPassword = new Label(this, SWT.NONE);
		lblPassword.setText("Password: ");
		
		txtPassword = new Text(this, SWT.BORDER | SWT.PASSWORD);
		txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public String getTxtUserID() {
		return txtUserID.getText();
	}

	public void setTxtUserID(String uid) {
		txtUserID.setText(uid);
	}

	public char[] getTxtPassword() {
		return new String(txtPassword.getTextChars()).toCharArray();
	}

	public void setTxtPassword(String pwd) {
		txtPassword.setText(pwd);
	}

}
