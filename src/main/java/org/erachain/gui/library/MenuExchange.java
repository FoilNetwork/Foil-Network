package org.erachain.gui.library;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.MainFrame;
import org.erachain.gui.items.accounts.*;
import org.erachain.gui.items.assets.DepositExchange;
import org.erachain.gui.items.assets.ExchangePanel;
import org.erachain.gui.items.assets.MyOrderTab;
import org.erachain.gui.items.assets.WithdrawExchange;
import org.erachain.gui.records.VouchRecordDialog;
import org.erachain.gui2.MainPanel;
import org.erachain.lang.Lang;
import org.erachain.settings.Settings;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuExchange extends JMenu {

    public MenuExchange() {

        if (Settings.EXCHANGE_IN_OUT) {
            /// DEPOSIT
            JMenuItem deposit = new JMenuItem(Lang.getInstance().translate("Deposit or Buy"));
            deposit.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Deposit funds to Exchange"));
            deposit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //
                    MainPanel.getInstance().addTab(DepositExchange.class.getSimpleName());
                }
            });
            add(deposit);

            addSeparator();
        }

        // TRADE
        JMenuItem trade = new JMenuItem(Lang.getInstance().translate("Trade on DEX"));
        trade.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Trade on Exchange"));
        trade.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                MainPanel.getInstance().addTab(ExchangePanel.class.getSimpleName());
            }
        });
        add(trade);

        // TRADE
        JMenuItem orders = new JMenuItem(Lang.getInstance().translate("My Orders"));
        orders.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("See My Orders"));
        orders.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                //
                MainPanel.getInstance().addTab(MyOrderTab.class.getSimpleName());
            }
        });
        add(orders);

        if (Settings.EXCHANGE_IN_OUT) {

            addSeparator();

            // WITHDRAW
            JMenuItem withdraw = new JMenuItem(Lang.getInstance().translate("Withdraw or Sell"));
            withdraw.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Withdraw funds from Exchange"));
            withdraw.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    //
                    MainPanel.getInstance().addTab(WithdrawExchange.class.getSimpleName());
                }
            });
            add(withdraw);
        }
    }
}
