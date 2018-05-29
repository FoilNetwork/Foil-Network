package gui.create;

import controller.Controller;
import core.item.templates.TemplateCls;
import datachain.DCSet;
import lang.Lang;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class License_JFrame extends JDialog {

    static Logger LOGGER = Logger.getLogger(License_JFrame.class.getName());
    boolean needAccept;
    NoWalletFrame parent;
    int goCreateWallet;
    TemplateCls template;
    // Variables declaration - do not modify
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    public License_JFrame(TemplateCls template, boolean needAccept, NoWalletFrame parent, int goCreateWallet) {
        this.template = template;
        this.needAccept = needAccept;
        this.parent = parent;
        this.goCreateWallet = goCreateWallet;
        initComponents();
    }
    public License_JFrame(TemplateCls template, boolean needAccept) {
        this.template = template;
        this.needAccept = needAccept;
        initComponents();
    }
    public License_JFrame(TemplateCls template) {
        this.template = template;
        needAccept = true;
        initComponents();
    }
    public License_JFrame() {
        this.template = (TemplateCls) DCSet.getInstance().getItemTemplateMap().get(Controller.getInstance().getWalletLicense());
        needAccept = false;
        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        List<Image> icons = new ArrayList<Image>();
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
        icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
        this.setIconImages(icons);
        this.setModal(true);

        java.awt.GridBagConstraints gridBagConstraints;

        jCheckBox1 = new javax.swing.JCheckBox();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();

        //      setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(Lang.getInstance().translate("License"));
        setMinimumSize(new java.awt.Dimension(800, 550));
        getContentPane().setLayout(new java.awt.GridBagLayout());

        jCheckBox1.setText(Lang.getInstance().translate("I accept"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(6, 8, 6, 0);
        if (needAccept)
            getContentPane().add(jCheckBox1, gridBagConstraints);

        jCheckBox1.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent arg0) {
                // TODO Auto-generated method stub
                jButton1.setEnabled(!jButton1.isEnabled());
            }
        });


        jButton1.setEnabled(false);
        jButton1.setText(Lang.getInstance().translate("Next"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 15, 8, 0);

        if (needAccept)
            getContentPane().add(jButton1, gridBagConstraints);

        jButton1.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                setVisible(false);

                if (parent != null)
                    parent.goAfterLicence(goCreateWallet);
            }

        });

        jButton2.setText(Lang.getInstance().translate(parent == null ? "Not Accept" : "Back"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 8, 8);
        if (needAccept)
            getContentPane().add(jButton2, gridBagConstraints);


        jButton2.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub

                if (parent != null) {
                    parent.setVisible(true);
                    dispose();
                } else {
                    Controller.getInstance().stopAll(0);
                    //      	System.exit(0);
                }
            }
        });
    /*

      jTextArea1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	if (!needAccept){

            	setVisible(false);
                dispose();
            	}
            }
        });

      jTextArea1.addKeyListener(new KeyAdapter() {
		    public void keyPressed(KeyEvent e) {
		    	if (!needAccept){

		    	setVisible(false);
                dispose();
		    	}
		    }
		});

     */


        //CLOSE NICELY
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {

                if (!needAccept)
                    return;

                Controller.getInstance().stopAll(0);
                // 	System.exit(0);

            }
        });

        // jTextArea1.setColumns(20);
        jTextArea1.setLineWrap(true);
        jTextArea1.setEditable(false);
        jTextArea1.setRows(5);
        jTextArea1.setText(template.getDescription());
        jScrollPane1.setViewportView(jTextArea1);

 /*
        this.jTextArea1.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            	if(parent != null) return;
            	setVisible(false);
                dispose();
            }
        });

        this.addKeyListener(new KeyAdapter() {
		    public void keyPressed(KeyEvent e) {
		    	if(parent != null) return;
		    	setVisible(false);
                dispose();
		    }
		});

   */

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.2;
        gridBagConstraints.weighty = 0.2;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        getContentPane().add(jScrollPane1, gridBagConstraints);

        jLabel1.setText(Lang.getInstance().translate("Read carefully") + "!");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.FIRST_LINE_START;
        gridBagConstraints.weightx = 0.1;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 0, 8);
        if (needAccept)
            getContentPane().add(jLabel1, gridBagConstraints);


        //     this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        //      if(!needAccept)
        this.setUndecorated(false);
        if (needAccept) this.setUndecorated(true);

        pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
    }// </editor-fold>
    // End of variables declaration                   


}
