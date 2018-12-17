package org.evansnet.common.security;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;

public class VaultComposite extends Composite {
	
	private final String BTN_OK = "credentialBtnOk";
	private final String BTN_CANCEL = "credentialBtnCancel";
	private Text txtPassword;

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public VaultComposite(Composite parent , int style) {
		super(parent, style);
		setLayout(new GridLayout(4, false));
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		// dummy label for spacing. Yeah, I know it's not pretty...
		Label label = new Label(this, SWT.NONE);
		label.setText("     ");
		
		Label lblPassword = new Label(this, SWT.NONE);
		lblPassword.setAlignment(SWT.CENTER);
		lblPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblPassword.setText("Password:  ");
		
		txtPassword = new Text(this, SWT.BORDER | SWT.PASSWORD);
		txtPassword.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNewLabel = new Label(this, SWT.NONE);
		lblNewLabel.setText("      ");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
		Button btnOk = new Button(this, SWT.CENTER);
		GridData gd_btnOk = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_btnOk.widthHint = 120;
		btnOk.setLayoutData(gd_btnOk);
		btnOk.setText("OK");
		btnOk.setData(BTN_OK);
		
		Button btnCancel = new Button(this, SWT.NONE);
		GridData gd_btnCancel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_btnCancel.widthHint = 120;
		btnCancel.setLayoutData(gd_btnCancel);
		btnCancel.setText("Cancel");
		btnCancel.setData(BTN_CANCEL);
		//Dummy label to provide spacing
		new Label(this, SWT.NONE);
	}
	
	public char[] getResult() {
		return txtPassword.getTextChars(); // Use a char[] to help protect the password.
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
