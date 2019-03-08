package org.evansnet.common.security;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormAttachment;


/**
 * Prompt the user for vaultPwd to the key store.
 * These vaultPwd serve as the user's log in to the application. This helps to
 * keep the keystore secure. 
 * @author Dan Evans
 *
 */
public class VaultDialog extends Dialog {

	public static Logger javaLogger = Logger.getLogger(VaultDialog.class.getName());
	protected Shell shell;
	protected VaultComposite vaultComposite;
	private char[] vaultPwd; 
	
	private SelectionListener credentialsDlgBtnListener;

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public VaultDialog(Shell parent, int style) {
		super(parent, style);
		shell = new Shell(parent, SWT.NONE);
		createContents();
	}
	
	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		javaLogger.logp(Level.INFO, VaultDialog.class.getName(), "open()",
				"Opening vault dialog for log in.");
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
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
		shell.setText("Enter Vault Password");
		shell.setLayout(new FormLayout());

		credentialsDlgBtnListener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				widgetDefaultSelected(e);				
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				Button theBtn = (Button)e.getSource();
				String whichBtn = (String) theBtn.getData();
				if (whichBtn.equals("credentialBtnOk")) {
					vaultPwd = vaultComposite.getResult();
					if (vaultPwd.length == 0) return;
				} else if (whichBtn.equals("credentialBtnCancel")) {
					vaultPwd = null;
					return;
				}
				shell.dispose();	// close the dialog with the return value = vaultPwd				
			}
		};
		vaultComposite = new VaultComposite(shell , SWT.NONE);		
		vaultComposite.setBtnListener(credentialsDlgBtnListener);
		FormData fd_credentialsComposite = new FormData();
		fd_credentialsComposite.top = new FormAttachment(0);
		fd_credentialsComposite.bottom = new FormAttachment(0, 127);
		fd_credentialsComposite.left = new FormAttachment(0);
		fd_credentialsComposite.right = new FormAttachment(0, 323);
		vaultComposite.setLayoutData(fd_credentialsComposite);
		shell.pack();
		
	}
	
	public SelectionListener getCredentialsDlgBtnListener() {
		return credentialsDlgBtnListener;
	}
}
