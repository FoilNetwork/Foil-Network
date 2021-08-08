package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.crypto.Base58;
import org.erachain.core.exdata.exLink.ExLinkAddress;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.utils.GUIConstants;
import org.erachain.lang.Lang;

import javax.swing.*;

/**
 * @author Саша
 */
public class IssueAssetCopyPanel extends IssueAssetPanelCls {

    public static String NAME = "IssueAssetCopyPanel";
    public static String TITLE = "Issue Series";

    public JTextField assetRefField = new JTextField("");
    byte[] origAssetTXSign;

    public IssueAssetCopyPanel() {
        super(NAME, TITLE, "Asset series issue has been sent!", true, GUIConstants.WIDTH_IMAGE, GUIConstants.WIDTH_IMAGE,
                true, true);

        quantityJLabel.setText(Lang.T("Series Total") + ":");
        initComponents();

    }

    protected void initComponents() {

        super.initComponents();

        int gridy = initTopArea(true);

        JLabel signLabel = new JLabel(Lang.T("Original Asset Issue TX Signature") + ":");
        labelGBC.gridy = gridy;
        jPanelAdd.add(signLabel, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(assetRefField, fieldGBC);

        labelGBC.gridy = ++gridy;
        jPanelAdd.add(quantityJLabel, labelGBC);

        fieldGBC.gridy = gridy++;
        jPanelAdd.add(textQuantity, fieldGBC);

        // вывод подвала
        super.initBottom(gridy);
    }

    protected boolean checkValues() {

        assetType = AssetCls.AS_NON_FUNGIBLE;

        int parseStep = 0;
        try {

            // READ ORIG SIGNATURE
            origAssetTXSign = Base58.decode(assetRefField.getText());

            // READ QUANTITY
            parseStep++;
            quantity = Long.parseLong(textQuantity.getText());

        } catch (Exception e) {
            switch (parseStep) {
                case 0:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.T("Invalid original signature!"), Lang.T("Error"),
                            JOptionPane.ERROR_MESSAGE);
                    break;
                case 1:
                    JOptionPane.showMessageDialog(MainFrame.getInstance(),
                            Lang.T("Invalid quantity!"), Lang.T("Error"),
                            JOptionPane.ERROR_MESSAGE);
                    break;
            }
            return false;
        }

        return true;
    }

    @Override
    protected void makeTransaction() {

        // PROTOTYPE ASSET
        AssetVenture prototypeAsset = new AssetVenture(itemAppData, creator, nameField.getText(), addIconLabel.getMediaBytes(),
                addImageLabel.getMediaBytes(), textAreaDescription.getText(),
                assetType, 0, quantity);

        transaction = Controller.getInstance().issueAssetSeries(creator, exLink, feePow, origAssetTXSign, prototypeAsset);

    }

    @Override
    protected String makeBodyView() {

        String out = super.makeBodyView();
        AssetCls asset = (AssetCls) item;

        out += Lang.T("Asset Class") + ":&nbsp;"
                + Lang.T(asset.getItemSubType() + "") + "<br>"
                + Lang.T("Asset Type") + ":&nbsp;"
                + "<b>" + asset.charAssetType() + asset.viewAssetTypeAbbrev() + "</b>:" + Lang.T(asset.viewAssetTypeFull() + "") + "<br>"
                + Lang.T("Quantity") + ":&nbsp;" + asset.getQuantity() + ", "
                + Lang.T("Scale") + ":&nbsp;" + asset.getScale() + "<br>";

        if (asset.getDEXAwards() != null) {
            out += Lang.T("DEX Awards" + ":");
            for (ExLinkAddress award : asset.getDEXAwards()) {
                out += "<br>&nbsp;&nbsp;&nbsp;&nbsp;" + award.getAccount().getPersonAsString() + " <b>" + award.getValue1() * 0.001d + "%</b>"
                        + (award.getMemo() == null || award.getMemo().isEmpty() ? "" : " - " + award.getMemo());
            }
            out += "<br>";
        }

        return out;

    }

}
