package org.erachain.dapp;

import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.Order;
import org.erachain.core.transaction.CreateOrderTransaction;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.dapp.epoch.*;
import org.erachain.dapp.epoch.memeTheGathering.MemeTheGathering_01DAPP;
import org.erachain.dapp.epoch.memeTheGathering.MemeTheGatheringDAPP;
import org.erachain.dapp.epoch.shibaverse.ShibaVerseDAPP;
import org.erachain.utils.FileUtils;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public abstract class DAPPFactory {

    static protected Controller contr = Controller.getInstance();
    static protected Crypto crypto = Crypto.getInstance();
    static final HashMap<Account, Integer> stocks = new HashMap();
    public static JSONObject settingsJSON;

    static {
        try {
            settingsJSON = FileUtils.readCommentedJSONObject("settings_servers.json");
        } catch (IOException e) {
            settingsJSON = new JSONObject();
        }

        ShibaVerseDAPP.setDAPPFactory(stocks);
        MemeTheGatheringDAPP.setDAPPFactory(stocks);
        MemeTheGathering_01DAPP.setDAPPFactory(stocks);
        Refi.setDAPPFactory(stocks);
        Voucher.setDAPPFactory(stocks);
        GoGreenTree.setDAPPFactory(stocks);

    }


    /**
     * Делает смотр-контракт протокольный (на эпоху).
     *
     * @param transaction
     * @return
     */
    static public DAPP make(Transaction transaction) {

        if (BlockChain.DEMO_MODE && transaction.getBlockHeight() < 386460) {
            return null;
        }

        /////////// EVENTS
        if (false
                && transaction.getType() == Transaction.CREATE_ORDER_TRANSACTION) {
            CreateOrderTransaction createOrder = (CreateOrderTransaction) transaction;
            if (createOrder.getHaveKey() == AssetCls.ERA_KEY
                    && createOrder.getAmountHave().compareTo(new BigDecimal(100)) >= 0 //  && createOrder.getWantKey() == AssetCls.USD_KEY
                    || createOrder.getWantKey() == AssetCls.ERA_KEY
                    && createOrder.getAmountWant().compareTo(new BigDecimal(100)) >= 0 // && createOrder.getHaveKey() == AssetCls.USD_KEY
            ) {
                Order order = createOrder.getDCSet().getCompletedOrderMap().get(createOrder.getOrderId());
                if (false && order != null)
                    return new LeafFall();
            }
        }

        if (transaction.getType() != Transaction.SEND_ASSET_TRANSACTION) {
            return null;
        }

        RSend txSend = (RSend) transaction;

        if (BlockChain.TEST_MODE) {
            Refi dapp = Refi.tryMakeJob(txSend);
            if (dapp != null)
                return dapp;
        }

        if (!txSend.getRecipient().isDAppOwned()) {
            ///// OLD VERSION
            if (BlockChain.TEST_MODE &&
                    txSend.balancePosition() == TransactionAmount.ACTION_SPEND && txSend.hasAmount()
            ) {
                if (txSend.hasPacket()) {

                } else if (txSend.getAmount().signum() < 0) {
                    return new DogePlanet(Math.abs(transaction.getAmount().intValue()));
                }
            }
            //////

            return null;
        }

        /////// NEW VERSION

        ///////////////////// MAKE DAPPS HERE
        // TRY BY RECIPIENT
        Integer dappID = stocks.get(txSend.getRecipient());
        if (dappID == null) {
            // TODO сюда не дойдет из-за проверки isDAppOwned
            // TRY BY ASSET
            if (txSend.hasAmount()) {
                dappID = stocks.get(txSend.getAsset().getMaker());
            }

            if (dappID == null)
                return null;
        }

        String dataStr = txSend.isText() && !txSend.isEncrypted() ? new String(txSend.getData(), StandardCharsets.UTF_8) : null;

        switch (dappID) {
            case Refi.ID:
                if (true)
                    return null;
                return Refi.make(txSend, dataStr);
            case ShibaVerseDAPP.ID:
                return ShibaVerseDAPP.make(txSend, dataStr);
            case MemeTheGatheringDAPP.ID:
                return MemeTheGatheringDAPP.make(txSend, dataStr);
            case MemeTheGathering_01DAPP.ID:
                return MemeTheGathering_01DAPP.make(txSend, dataStr);
            case Voucher.ID:
                return Voucher.make(txSend, dataStr);
            case GoGreenTree.ID:
                return GoGreenTree.make(txSend, dataStr);
        }

        return null;

    }

    static public String getName(Account stock) {
        Integer dappID = stocks.get(stock);
        if (dappID == null)
            return null;

        switch (dappID) {
            case LeafFall.ID:
                return LeafFall.NAME;
            case DogePlanet.ID:
                return DogePlanet.NAME;
            case ShibaVerseDAPP.ID:
                return ShibaVerseDAPP.NAME;
            case MemeTheGatheringDAPP.ID:
                return MemeTheGatheringDAPP.NAME;
            case MemeTheGathering_01DAPP.ID:
                return MemeTheGathering_01DAPP.NAME;
            case Refi.ID:
                return Refi.NAME;
            case Voucher.ID:
                return Voucher.NAME;
            case GoGreenTree.ID:
                return GoGreenTree.NAME;
        }

        return null;
    }
}
