package gui.library;

import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableColumn;

import controller.Controller;
import core.account.Account;
import core.account.PublicKeyAccount;
import core.item.persons.PersonCls;
import core.transaction.Transaction;
import gui.items.accounts.Account_Send_Dialog;
import gui.items.mails.Mail_Send_Dialog;
import gui.models.PersonAccountsModel;
import gui.models.Renderer_Left;
import lang.Lang;
import utils.TableMenuPopupUtil;

public class Accounts_Library_Panel extends JPanel {

	private JTable jTable_Accounts;
	private JScrollPane jScrollPane_Tab_Accounts;
	private GridBagConstraints gridBagConstraints;

	public Accounts_Library_Panel(PersonCls person) {

		this.setName(Lang.getInstance().translate("Accounts"));
		
		PersonAccountsModel person_Accounts_Model = new PersonAccountsModel(person.getKey());
		jTable_Accounts = new JTable(person_Accounts_Model);

		jTable_Accounts.setDefaultRenderer(String.class,
				new Renderer_Left(jTable_Accounts.getFontMetrics(jTable_Accounts.getFont()),
						person_Accounts_Model.get_Column_AutoHeight())); // set
																			// renderer
		
		TableColumn to_Date_Column = jTable_Accounts.getColumnModel().getColumn(PersonAccountsModel.COLUMN_TO_DATE);
		to_Date_Column.setMinWidth(50);
		to_Date_Column.setMaxWidth(200);
		to_Date_Column.setPreferredWidth(80);// .setWidth(30);
		
		jScrollPane_Tab_Accounts = new JScrollPane();
		jScrollPane_Tab_Accounts.setViewportView(jTable_Accounts);
		this.setLayout(new java.awt.GridBagLayout());

		gridBagConstraints = new java.awt.GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(5, 5, 5, 5);
		this.add(jScrollPane_Tab_Accounts, gridBagConstraints);

		JPopupMenu menu = new JPopupMenu();

		JMenuItem copyAddress = new JMenuItem(Lang.getInstance().translate("Copy Address"));
		copyAddress.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = jTable_Accounts.getSelectedRow();
				row = jTable_Accounts.convertRowIndexToModel(row);

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(person_Accounts_Model.getAccount_String(row));
				clipboard.setContents(value, null);
			}
		});
		menu.add(copyAddress);

		JMenuItem menu_copyPublicKey = new JMenuItem(Lang.getInstance().translate("Copy Public Key"));
		menu_copyPublicKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				// StringSelection value = new
				// StringSelection(person.getCreator().getAddress().toString());
				int row = jTable_Accounts.getSelectedRow();
				row = jTable_Accounts.convertRowIndexToModel(row);

				byte[] publick_Key = Controller.getInstance()
						.getPublicKeyByAddress(person_Accounts_Model.getAccount_String(row));
				PublicKeyAccount public_Account = new PublicKeyAccount(publick_Key);
				StringSelection value = new StringSelection(public_Account.getBase58());
				clipboard.setContents(value, null);
			}
		});
		menu.add(menu_copyPublicKey);

		JMenuItem copy_Creator_Address = new JMenuItem(Lang.getInstance().translate("Copy Creator Address"));
		copy_Creator_Address.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = jTable_Accounts.getSelectedRow();
				row = jTable_Accounts.convertRowIndexToModel(row);

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(person_Accounts_Model.get_Creator_Account(row));
				clipboard.setContents(value, null);
			}
		});
		menu.add(copy_Creator_Address);

		JMenuItem menu_copy_Creator_PublicKey = new JMenuItem(Lang.getInstance().translate("Copy Creator Public Key"));
		menu_copy_Creator_PublicKey.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				// StringSelection value = new
				// StringSelection(person.getCreator().getAddress().toString());
				int row = jTable_Accounts.getSelectedRow();
				row = jTable_Accounts.convertRowIndexToModel(row);

				byte[] publick_Key = Controller.getInstance()
						.getPublicKeyByAddress(person_Accounts_Model.get_Creator_Account(row));
				PublicKeyAccount public_Account = new PublicKeyAccount(publick_Key);
				StringSelection value = new StringSelection(public_Account.getBase58());
				clipboard.setContents(value, null);
			}
		});
		menu.add(menu_copy_Creator_PublicKey);

		JMenuItem Send_Coins_item_Menu = new JMenuItem(Lang.getInstance().translate("Send"));
		Send_Coins_item_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int row = jTable_Accounts.getSelectedRow();
				row = jTable_Accounts.convertRowIndexToModel(row);
				Account account = person_Accounts_Model.getAccount(row);

				new Account_Send_Dialog(null, null, account, null);

			}
		});
		menu.add(Send_Coins_item_Menu);

		JMenuItem Send_Mail_item_Menu = new JMenuItem(Lang.getInstance().translate("Send Mail"));
		Send_Mail_item_Menu.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				int row = jTable_Accounts.getSelectedRow();
				row = jTable_Accounts.convertRowIndexToModel(row);
				Account account = person_Accounts_Model.getAccount(row);

				new Mail_Send_Dialog(null, null, account, null);

			}
		});
		menu.add(Send_Mail_item_Menu);

		////////////////////
		TableMenuPopupUtil.installContextMenu(jTable_Accounts, menu); // SELECT
																		// ROW
																		// ON
																		// WHICH
																		// CLICKED
																		// RIGHT
																		// BUTTON

	}

}
