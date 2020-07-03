package org.erachain.gui.items.link_hashes;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.PasswordPane;
import org.erachain.gui.library.AuxiliaryToolTip;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.erachain.utils.Pair;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.List;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * @author Саша
 */
public class IssueHashImprint extends javax.swing.JPanel {

    private AuxiliaryToolTip auxiliaryToolTip;
    private boolean wasChanged = false;

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    // Variables declaration - do not modify
    public javax.swing.JButton jButton;
    public javax.swing.JComboBox jComboBox_Account;
    public javax.swing.JComboBox<String> txtFeePow;
    public javax.swing.JLabel jLabel1;
    public javax.swing.JLabel jLabel_Account;
    public javax.swing.JLabel jLabel_Description;
    public javax.swing.JLabel jLabel_Table_Hash;
    public javax.swing.JLabel jLabel_Title;
    public javax.swing.JLabel jLabel_URL;
    public javax.swing.JScrollPane jScrollPane1;
    public javax.swing.JScrollPane jScrollPane2;
    public javax.swing.JTable jTable_Hash;
    public javax.swing.JTextArea jTextArea_Description;
    public javax.swing.JTextField jTextField_URL;
    TableModelIssueHashes table_Model;
    /**
     * Creates new form IssueHashImprint
     */
    public IssueHashImprint() {

        initComponents();
        this.jLabel_Title.setFont(new java.awt.Font("Tahoma", 0, 18));
        ;
        this.jLabel_Title.setText(Lang.getInstance().translate("Write Hashs to BlockChain"));
        table_Model = new TableModelIssueHashes(0);
        this.jTable_Hash.setModel(table_Model);
        this.jLabel_Account.setText(Lang.getInstance().translate("Account") + ":");
        this.jComboBox_Account.setModel(new AccountsComboBoxModel());
        String tipURL = Lang.getInstance().translate("Задайте внешнюю ссылку ввиде URL. \n Причем если ссвлка будет закачиваться на:\n / или = или № - то значение хеша будет добавлено к ссылке");
        this.jLabel_URL.setText(Lang.getInstance().translate("URL") + ":");
        this.jLabel_URL.setToolTipText(tipURL);
        this.jTextField_URL.setText("");
        this.jTextField_URL.setToolTipText(tipURL);


        this.jLabel_Description.setText(Lang.getInstance().translate("Description") + ":");
        this.jButton.setText(Lang.getInstance().translate("Write & Sign"));

  /*   this.jButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onIssueClick();
		    }
		});
 */
    }

    public void onIssueClick() {
        //DISABLE
        this.jButton.setEnabled(false);

        //CHECK IF NETWORK OK
        if (false && Controller.getInstance().getStatus() != Controller.STATUS_OK) {
            //NETWORK NOT OK
            JOptionPane.showMessageDialog(null, Lang.getInstance().translate("You are unable to send a transaction while synchronizing or while having no connections!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

            //ENABLE
            this.jButton.setEnabled(true);

            return;
        }

        //CHECK IF WALLET UNLOCKED
        if (!Controller.getInstance().isWalletUnlocked()) {
            //ASK FOR PASSWORD
            String password = PasswordPane.showUnlockWalletDialog(this);
            if (!Controller.getInstance().unlockWallet(password)) {
                //WRONG PASSWORD
                JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);

                //ENABLE
                this.jButton.setEnabled(true);

                return;
            }
        }

        //READ CREATOR
        Account sender = (Account) this.jComboBox_Account.getSelectedItem();

        long parse = 0;
        try {

            //READ FEE POW
            int feePow =  Integer.parseInt((String)this.txtFeePow.getSelectedItem());
            // READ AMOUNT
            //float amount = Float.parseFloat(this.txtAmount.getText());

            // NAME TOTAL
            String url = this.jTextField_URL.getText().trim();

            String description = this.jTextArea_Description.getText();

            List<String> hashes = this.table_Model.getValues(0);

            //CREATE IMPRINT
            PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress());
            if (creator == null) {
                JOptionPane.showMessageDialog(new JFrame(),
                        Lang.getInstance().translate(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                        Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
                return;
            }

            Pair<Transaction, Integer> result = Controller.getInstance().r_Hashes(creator, feePow, url, description,
                    String.join(" ", hashes));

            //CHECK VALIDATE MESSAGE
            if (result.getB() == Transaction.VALIDATE_OK) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Imprint issue has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
                //this.dispose();
            } else {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Unknown error")
                        + "[" + result.getB() + "]!", Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            }

        } catch (Exception e) {
            if (parse == 0) {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid quantity!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            }
        }

        //ENABLE
        this.jButton.setEnabled(true);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jLabel_Title = new javax.swing.JLabel();
        jLabel_Account = new javax.swing.JLabel();
        jComboBox_Account = new javax.swing.JComboBox<>();
        jLabel_URL = new javax.swing.JLabel();
        jTextField_URL = new javax.swing.JTextField();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea_Description = new javax.swing.JTextArea();
        jLabel_Description = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable_Hash = new javax.swing.JTable();
        jLabel_Table_Hash = new javax.swing.JLabel();
        jButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();

        setLayout(new java.awt.GridBagLayout());

        jLabel_Title.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel_Title.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_Title.setText("jLabel1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(10, 13, 11, 10);
        add(jLabel_Title, gridBagConstraints);

        jLabel_Account.setText("jLabel2");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(0, 13, 0, 0);
        add(jLabel_Account, gridBagConstraints);

        jComboBox_Account.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Item 1", "Item 2", "Item 3", "Item 4"}));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(jComboBox_Account, gridBagConstraints);

        jLabel_URL.setText("jLabel3");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, 13, 0, 0);
        add(jLabel_URL, gridBagConstraints);

        jTextField_URL.setText("jTextField1");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(jTextField_URL, gridBagConstraints);

        jTextArea_Description.setColumns(5);
        jTextArea_Description.setLineWrap(true);
        jTextArea_Description.setRows(2);
        jScrollPane1.setViewportView(jTextArea_Description);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        add(jScrollPane1, gridBagConstraints);

        jLabel_Description.setText("jLabel4");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new java.awt.Insets(0, 13, 0, 0);
        add(jLabel_Description, gridBagConstraints);

    /*    jTable_Hash.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));

        */
        //    jScrollPane2.setViewportView(jTable_Hash);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(0, 13, 11, 10);
        //      add(jScrollPane2, gridBagConstraints);

        jLabel_Table_Hash.setText("jLabel5");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(10, 13, 0, 0);
        //      add(jLabel_Table_Hash, gridBagConstraints);


        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 18, 9);
        add(jButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        add(jLabel1, gridBagConstraints);

        //LABEL FEE POW
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(0, 13, 0, 0);
        this.add(new JLabel(Lang.getInstance().translate("Fee Power") + ":"), gridBagConstraints);


        //TXT FEE
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, 10, 0, 10);
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;

        txtFeePow = new JComboBox<String>();
        txtFeePow.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8"}));
        txtFeePow.setSelectedIndex(0);


        this.add(this.txtFeePow, gridBagConstraints);

    }// </editor-fold>
    // End of variables declaration                   

    @Override
    public String getToolTipText() {
        String res = super.getToolTipText();
        return AuxiliaryToolTip.IGNORE_TOOLTIP.equals(res) ? null : res;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        if (auxiliaryToolTip == null) {
            auxiliaryToolTip = new AuxiliaryToolTip();
            auxiliaryToolTip.setComponent(this);
        }
        auxiliaryToolTip.setKey(null);
        auxiliaryToolTip.setStoredLocation(null);

        Point p = event.getPoint();
        Component component = event.getComponent();

        String rendererTip = null;
        if (component instanceof JComponent) {
            // Convert the event to the renderer's coordinate system
            MouseEvent newEvent = new MouseEvent(component, event.getID(),
                    event.getWhen(), event.getModifiers(),
                    p.x, p.y,
                    event.getXOnScreen(),
                    event.getYOnScreen(),
                    event.getClickCount(),
                    event.isPopupTrigger(),
                    MouseEvent.NOBUTTON);

            rendererTip = ((JComponent) component).getToolTipText(newEvent);
        }

        if (rendererTip != null) {
            return AuxiliaryToolTip.IGNORE_TOOLTIP.equals(rendererTip) ? null : rendererTip;
        }

        return auxiliaryToolTip.getKey() == null ? getToolTipText() : auxiliaryToolTip.getKey();
    }

    @Override
    public Point getToolTipLocation(MouseEvent event) {
        if ((auxiliaryToolTip == null) || (auxiliaryToolTip.getKey() == null)) {
            return null;
        } else {
            return auxiliaryToolTip.getStoredLocation();
        }
    }

    @Override
    public JToolTip createToolTip() {
        if ((auxiliaryToolTip == null) || (auxiliaryToolTip.getKey() == null)) {
            return super.createToolTip();
        } else {
            return auxiliaryToolTip;
        }
    }

}
