package org.evansnet.common.security;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;


/**
 * Prompt the user for vaultPwd to the key store.
 * These vaultPwd serve as the user's log in to the application. This helps to
 * keep the keystore secure. 
 * @author Dan Evans
 *
 */
public class VaultDialog extends Dialog {

	protected Shell shlCredentials;
	protected VaultComposite vaultComposite;
	private char[] vaultPwd; 
	
	@SuppressWarnings("unused")
	private SelectionListener credentialsDlgBtnListener;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public VaultDialog(Shell parent, int style) {
		super(parent, style);
		setText("Credentials Dialog");
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlCredentials.open();
		shlCredentials.layout();
		Display display = getParent().getDisplay();
		while (!shlCredentials.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return vaultPwd;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlCredentials = new Shell(getParent(), getStyle());
		shlCredentials.setSize(329, 188);
		shlCredentials.setText("Enter Vault Password");
		shlCredentials.setLayout(new FormLayout());
		vaultComposite = new VaultComposite(shlCredentials, SWT.NONE);
		FormData fd_credentialsComposite = new FormData();
		fd_credentialsComposite.top = new FormAttachment(0);
		fd_credentialsComposite.bottom = new FormAttachment(0, 127);
		fd_credentialsComposite.left = new FormAttachment(0);
		fd_credentialsComposite.right = new FormAttachment(0, 323);
		vaultComposite.setLayoutData(fd_credentialsComposite);
		
		credentialsDlgBtnListener = new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				Button theBtn = (Button)e.widget;
				String whichBtn = (String) theBtn.getData();
				if (whichBtn.equals("credentialBtnOk")) {
					vaultPwd = vaultComposite.getResult();
				} else if (whichBtn.equals("credentialBtnCancel")) {
					vaultPwd = null;
				}
				shlCredentials.dispose();	// close the dialog with the return value = vaultPwd
			}			
		};
	}
}
