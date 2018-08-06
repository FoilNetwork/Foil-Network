package gui.library;
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import lang.Lang;

import javax.swing.*;

import core.transaction.Transaction;
import gui.transaction.TransactionDetailsFactory;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author Саша
 */
public class Issue_Confirm_Dialog extends javax.swing.JDialog {
    public boolean isConfirm = false;
    public javax.swing.JButton jButton1;
    public javax.swing.JButton jButton2;
    public javax.swing.JPanel jPanel1;
    public javax.swing.JScrollPane jScrollPane1;
    public MTextPane jTextPane1;
    int insest = 0;
    private JLabel jStatus_Label;
    private JLabel jTitle_Label;
    /**
     * Creates new form Issue_Asset_Confirm_Dialog
     *
     */
    public Issue_Confirm_Dialog(java.awt.Frame parent, boolean modal, Transaction transaction, String text,
                                int w, int h, String status_Text, String title_Text) {
        super(parent, modal);
        Init(parent, modal, transaction, text, w, h, status_Text, title_Text);
    }
    public Issue_Confirm_Dialog(java.awt.Frame parent, boolean modal, Transaction transaction, String text,
                                int w, int h, String status_Text) {
        super(parent, modal);
        Init(parent, modal, transaction, text, w, h, status_Text, "");
    }
    
    public Issue_Confirm_Dialog(java.awt.Frame parent, boolean modal, Transaction transaction,
                                int w, int h, String status_Text) {
        super(parent, modal);
        Init(parent, modal, transaction, w, h, status_Text, "");
    }

    public void Init(java.awt.Frame parent, boolean modal, Transaction transaction, String text,
                     int w, int h, String status_Text, String title_Text) {
        // setUndecorated(true);
        insest = UIManager.getFont("Label.font").getSize();
        if (insest <= 7) insest = 8;
        initComponents();
        jTitle_Label.setText(title_Text);
        jTextPane1.set_text(text);
        if (transaction != null) {
            String feeText = "" + Lang.getInstance().translate("Size") + ":&nbsp;"
                    + transaction.viewSize(false) + " Bytes, ";
            feeText += Lang.getInstance().translate("Fee") + ":&nbsp;<b>" + transaction.viewFee()
                    + "</b>";
            status_Text += feeText;
        }

        jStatus_Label.setText("<HTML>" + status_Text + "</HTML>");

        //  setMaximumSize(new Dimension(350,200));
        setSize(w, h);
        jButton1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                isConfirm = true;
                dispose();
            }
        });
        jButton2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                dispose();
            }
        });
    }

    public void Init(java.awt.Frame parent, boolean modal, Transaction transaction, int w, int h, String status_Text, String title_Text) {
        // setUndecorated(true);
        insest = UIManager.getFont("Label.font").getSize();
        if (insest <= 7) insest = 8;
        initComponents();
        jTitle_Label.setText(title_Text);
        JPanel pp = TransactionDetailsFactory.getInstance().createTransactionDetail(transaction);
        jScrollPane1.setViewportView(pp);
        jStatus_Label.setText(status_Text);
        //  setMaximumSize(new Dimension(350,200));
        setSize(w, h);
        jButton1.setVisible(false);
        jButton2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                dispose();
            }
        });
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new MTextPane();
        jPanel1 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jStatus_Label = new JLabel();
        jTitle_Label = new JLabel();


        setIconImage(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jTitle_Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.1;
        //  gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(insest, insest, insest, insest);
        getContentPane().add(jTitle_Label, gridBagConstraints);

        jScrollPane1.setBorder(null);
        jScrollPane1.setOpaque(false);

        jTextPane1.setOpaque(false);
        jScrollPane1.setViewportView(jTextPane1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.weighty = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(insest, insest, insest, insest);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jStatus_Label.setText(Lang.getInstance().translate("Status"));
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, insest, 0, 0);
        jPanel1.add(jStatus_Label, gridBagConstraints);


        jButton1.setText(Lang.getInstance().translate("Confirm"));
        gridBagConstraints.gridx = 1;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(0, insest, 0, 0);
        jPanel1.add(jButton1, gridBagConstraints);

        jButton2.setText(Lang.getInstance().translate("Cancel"));
        gridBagConstraints.gridx = 2;
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(0, insest, 0, 0);
        jPanel1.add(jButton2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new java.awt.Insets(0, insest, insest, insest);
        getContentPane().add(jPanel1, gridBagConstraints);

        pack();
    }// </editor-fold>

}
