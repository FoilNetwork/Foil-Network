package org.erachain.gui.items.statuses;

import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.transaction.IssueStatusRecord;
import org.erachain.core.transaction.Transaction;
import org.erachain.gui.Gui;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.TypeOfImage;
import org.erachain.gui.library.AddImageLabel;
import org.erachain.gui.library.IssueConfirmDialog;
import org.erachain.gui.library.Library;
import org.erachain.gui.models.AccountsComboBoxModel;
import org.erachain.gui.transaction.OnDealClick;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;

import javax.swing.*;
import java.awt.*;

import static org.erachain.gui.items.utils.GUIConstants.*;
import static org.erachain.gui.items.utils.GUIUtils.checkWalletUnlock;

public class IssueStatusPanel extends JPanel  {

    private static String iconFile = Settings.getInstance().getPatnIcons() + "IssueStatusPanel.png";
    private JComboBox<Account> cbxFrom;
    private JComboBox<String> txtFeePow = new JComboBox<String>();
    private JTextField txtName = new JTextField();
    private JTextArea txtareaDescription = new JTextArea();
    private JButton issueButton;
    private JCheckBox jcheckUnique;
    private AddImageLabel addImageLabel;
    private AddImageLabel addLogoIconPanel;


    public IssueStatusPanel() {
        setLayout(new GridBagLayout());
        String colorText = "FF0000";

        addImageLabel = new AddImageLabel(
                Lang.getInstance().translate("Add image"),
                WIDTH_IMAGE, HEIGHT_IMAGE, TypeOfImage.JPEG,
                0, ItemCls.MAX_IMAGE_LENGTH, WIDTH_IMAGE_INITIAL, HEIGHT_IMAGE_INITIAL);

        addLogoIconPanel = new AddImageLabel(Lang.getInstance().translate("Add Logo"),
                WIDTH_LOGO, HEIGHT_LOGO, TypeOfImage.GIF,
                0, ItemCls.MAX_ICON_LENGTH, WIDTH_LOGO_INITIAL, HEIGHT_LOGO_INITIAL);

        JLabel labelCaption = new JLabel();
        labelCaption.setFont(FONT_TITLE);
        labelCaption.setText(Lang.getInstance().translate("Create Status"));
        labelCaption.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        add(labelCaption, gridBagConstraints);


        GridBagConstraints gbcAddImagePanel = new GridBagConstraints();
        gbcAddImagePanel.gridx = 0;
        gbcAddImagePanel.gridy = 1;
        gbcAddImagePanel.gridheight = 3;
        gbcAddImagePanel.insets = new Insets(0, 12, 8, 8);
        add(addImageLabel, gbcAddImagePanel);

        GridBagConstraints gbcAddLogoIconPanel = new GridBagConstraints();
        gbcAddLogoIconPanel.gridx = 0;
        gbcAddLogoIconPanel.gridy = 5;
        add(addLogoIconPanel, gbcAddLogoIconPanel);

        issueButton = new JButton(Lang.getInstance().translate("Issue"));
        issueButton.addActionListener(e -> onIssueClick());
        GridBagConstraints gbcIssueButton = new GridBagConstraints();
        gbcIssueButton.gridx = 0;
        gbcIssueButton.gridy = 8;
        gbcIssueButton.gridwidth = 5;
        gbcIssueButton.anchor = GridBagConstraints.CENTER;
        add(issueButton, gbcIssueButton);

        JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account") + ":");
        GridBagConstraints gbcFromLabel = new GridBagConstraints();
        gbcFromLabel.gridx = 1;
        gbcFromLabel.gridy = 1;
        gbcFromLabel.anchor = GridBagConstraints.NORTHEAST;
        add(fromLabel, gbcFromLabel);

        JLabel nameLabel = new JLabel(
                "<HTML><p style=':#" + colorText + "'>" + Lang.getInstance().translate("Name") + ": </p></html>");
        GridBagConstraints gbcNameLabel = new GridBagConstraints();
        gbcNameLabel.gridx = 1;
        gbcNameLabel.gridy = 2;
        gbcNameLabel.anchor = GridBagConstraints.NORTHEAST;
        add(nameLabel, gbcNameLabel);

        JLabel descriptionLabel = new JLabel(Lang.getInstance().translate("Description") + ":");
        GridBagConstraints gbcDescriptionLabel = new GridBagConstraints();
        gbcDescriptionLabel.gridx = 1;
        gbcDescriptionLabel.gridy = 3;
        gbcDescriptionLabel.anchor = GridBagConstraints.NORTHEAST;
        add(descriptionLabel, gbcDescriptionLabel);

        JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee Power") + ":");
        feeLabel.setVisible(Gui.SHOW_FEE_POWER);
        GridBagConstraints gbcFeeLabel = new GridBagConstraints();
        gbcFeeLabel.gridx = 1;
        gbcFeeLabel.gridy = 6;
        gbcFeeLabel.anchor = GridBagConstraints.NORTHEAST;
        add(feeLabel, gbcFeeLabel);

        JLabel singleLabel = new JLabel(Lang.getInstance().translate("Single") + ":");
        GridBagConstraints gbcSingleLabel = new GridBagConstraints();
        gbcSingleLabel.gridx = 1;
        gbcSingleLabel.gridy = 7;
        gbcSingleLabel.anchor = GridBagConstraints.NORTHEAST;
        add(singleLabel, gbcSingleLabel);


        cbxFrom = new JComboBox<>(new AccountsComboBoxModel());
        GridBagConstraints gbcCbxFrom = new GridBagConstraints();
        gbcCbxFrom.gridx = 2;
        gbcCbxFrom.gridy = 1;
        gbcCbxFrom.fill = GridBagConstraints.HORIZONTAL;
        gbcCbxFrom.gridwidth = 3;
        add(cbxFrom, gbcCbxFrom);


        GridBagConstraints gbcTxtName = new GridBagConstraints();
        gbcTxtName.gridx = 2;
        gbcTxtName.gridy = 2;
        gbcTxtName.fill = GridBagConstraints.HORIZONTAL;
        gbcTxtName.gridwidth = 3;
        add(txtName, gbcTxtName);


        JScrollPane scrollDescription = new JScrollPane();
        scrollDescription.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        scrollDescription.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
        GridBagConstraints gbcScrollDescription = new GridBagConstraints();
        gbcScrollDescription.gridx = 2;
        gbcScrollDescription.gridy = 3;
        gbcScrollDescription.fill = GridBagConstraints.BOTH;
        gbcScrollDescription.weightx = 0.1;
        gbcScrollDescription.weighty = 0.1;
        gbcScrollDescription.gridwidth = 3;
        gbcScrollDescription.gridheight = 3;
        scrollDescription.setViewportView(txtareaDescription);
        add(scrollDescription, gbcScrollDescription);


        txtFeePow.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8"}));
        txtFeePow.setSelectedIndex(0);
        txtFeePow.setVisible(Gui.SHOW_FEE_POWER);
        GridBagConstraints gbcTxtFeePow = new GridBagConstraints();
        gbcTxtFeePow.gridx = 2;
        gbcTxtFeePow.gridy = 6;
        gbcTxtFeePow.fill = GridBagConstraints.HORIZONTAL;
        gbcTxtFeePow.gridwidth = 3;
        add(txtFeePow, gbcTxtFeePow);


        jcheckUnique = new JCheckBox();
        GridBagConstraints gbcJCheckUnique = new GridBagConstraints();
        gbcJCheckUnique.gridx = 2;
        gbcJCheckUnique.gridy = 7;
        gbcJCheckUnique.anchor = GridBagConstraints.NORTHEAST;
        add(jcheckUnique, gbcJCheckUnique);


        setVisible(true);
    }

    public void onIssueClick() {
        // DISABLE
        issueButton.setEnabled(false);
        if (checkWalletUnlock(issueButton)) {
            return;
        }
        // READ CREATOR
        Account sender = (Account) cbxFrom.getSelectedItem();

        int feePow;
        try {
            feePow = Integer.parseInt((String) this.txtFeePow.getSelectedItem());
        } catch (Exception e) {
            String mess = "Invalid fee power 0..6";
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(e + mess),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            issueButton.setEnabled(true);
            return;
        }
        byte[] icon = addLogoIconPanel.getImgBytes();
        byte[] image = addImageLabel.getImgBytes();
        boolean unique = jcheckUnique.isSelected();
        PrivateKeyAccount creator = Controller.getInstance().getWalletPrivateKeyAccountByAddress(sender.getAddress());
        if (creator == null) {
            JOptionPane.showMessageDialog(new JFrame(),
                    Lang.getInstance().translate(OnDealClick.resultMess(Transaction.PRIVATE_KEY_NOT_FOUND)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
            return;
        }

        IssueStatusRecord issueStatus = (IssueStatusRecord) Controller.getInstance().issueStatus(creator,
                txtName.getText(), txtareaDescription.getText(), unique, icon, image, feePow);
        String text = "<HTML><body>";
        text += Lang.getInstance().translate("Confirmation Transaction") + ":&nbsp;"
                + Lang.getInstance().translate("Create Status") + "<br><br><br>";
        text += Lang.getInstance().translate("Creator") + ":&nbsp;" + issueStatus.getCreator() + "<br>";
        text += Lang.getInstance().translate("Name") + ":&nbsp;" + issueStatus.getItem().viewName() + "<br>";
        text += Lang.getInstance().translate("Description") + ":<br>"
                + Library.to_HTML(issueStatus.getItem().getDescription()) + "<br>";
        text += Lang.getInstance().translate("Unique") + ": " + ((StatusCls) issueStatus.getItem()).isUnique()
                + "<br>";
        String Status_text = "";
        IssueConfirmDialog issueConfirmDialog = new IssueConfirmDialog(MainFrame.getInstance(), true, issueStatus,
                text,
                (int) (getWidth() / 1.2), (int) (getHeight() / 1.2), Status_text,
                Lang.getInstance().translate("Confirmation Transaction"));
        issueConfirmDialog.setLocationRelativeTo(this);
        issueConfirmDialog.setVisible(true);
        if (!issueConfirmDialog.isConfirm) {
            issueButton.setEnabled(true);
            return;
        }
        // VALIDATE AND PROCESS
        int result = Controller.getInstance().getTransactionCreator().afterCreate(issueStatus, Transaction.FOR_NETWORK);
        // CHECK VALIDATE MESSAGE
        if (result == Transaction.VALIDATE_OK) {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Status issue has been sent") + "!",
                    Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
            clearPanel();
        } else {
            JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate(OnDealClick.resultMess(result)),
                    Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
        }
        // ENABLE
        issueButton.setEnabled(true);
    }

    private void clearPanel() {
        txtName.setText("");
        txtareaDescription.setText("");
        txtFeePow.setSelectedItem("0");
    }

    public static Image getIcon() {
        {
            try {
                return Toolkit.getDefaultToolkit().getImage(iconFile);
            } catch (Exception e) {
                return null;
            }
        }
    }
}
