package org.erachain.dapp.epoch;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.exdata.exLink.ExLinkAddress;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetUnique;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.dapp.EpochDAPPjson;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemAssetMap;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Send public mail to APPBNt7cZp89L5j47Ud62ZRSiKb1Y9hYjD
 * Set text: ["mint","DOGE", 100, "7Recipient"]
 */
public class GoGreenTree extends EpochDAPPjson {


    static public final int ID = 99017;
    static public final String NAME = "GoGreenTree dApp";
    static public final String ASSET_NAME = "My Tree";
    static public final long GO_GREEN_ASSET_KEY = BlockChain.TEST_MODE? 1 : 1050898;
    static public final BigDecimal MIN_VALUE = new BigDecimal("20");

    // APPBNt7cZp89L5j47Ud62ZRSiKb1Y9hYjD
    final public static PublicKeyAccount MAKER = PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(ID)));

    /**
     * admin account
     */
    final static public Account adminAddress = new Account("796w8H3kfu9A76MWjpT8p4eP3WGyCxGKL3");

    /**
     * ASSET KEY
     */
    //static final private Tuple2 INIT_KEY = new Tuple2(ID, "i");
    final static public String COMMAND_PLANT = "plant";
    final static public String COMMAND_POUR = "pour";
    final static public String COMMAND_HUG = "hug";


    public GoGreenTree(String data, String status) {
        super(ID, MAKER, data, status);
    }

    public String getName() {
        return NAME;
    }

    private boolean isAdminCommand(Account txCreator) {
        return txCreator.equals(adminAddress);
    }


    ///////// COMMANDS
    private boolean plant(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {

        Long refDB = commandTX.getDBRef();
        Long assetKey;
        ItemAssetMap assetMap = dcSet.getItemAssetMap();

        if (asOrphan) {
            // Object[] result = removeState(dcSet, refDB);
            // assetKey = (Long)result[0];
            assetKey = assetMap.getLastKey();

            // RESET AMOUNT
            stock.changeBalance(dcSet, true, false, assetKey,
                    BigDecimal.ONE, false, false, true, 0);

            transfer(dcSet, null, commandTX, stock, commandTX.getCreator(), BigDecimal.ONE, assetKey, true, null, null);

            // DELETE FROM BLOCKCHAIN DATABASE
            dcSet.getItemAssetMap().decrementDelete(assetKey);

        } else {

            String wrong = null;
            if (!commandTX.hasAmount())
                wrong = "empty amount";
            else if (commandTX.getAssetKey() != GO_GREEN_ASSET_KEY)
                wrong = "wrong asset, need: " + GO_GREEN_ASSET_KEY;
            else if (commandTX.balancePosition() != Account.BALANCE_POS_OWN)
                wrong = "wrong balance position, need: " + Account.BALANCE_POS_OWN;
            else if (commandTX.getAmount().compareTo(MIN_VALUE) < 0)
                wrong = "wrong amount, need: >=" + MIN_VALUE.toPlainString();

            if (wrong != null) {
                fail(wrong + (commandTX.hasAmount() && commandTX.balancePosition() == Account.BALANCE_POS_OWN ?
                        " ,thanks for donate project!" : "."));
                return false;
            }

            try {
                // ["plant", "type", 100, "7sadiuwyer7625346XXX"] - command, type, amount, recipient
                status = "";
                String type = (String) pars.get(1);
                String amountStr = pars.get(2).toString();
                int amountInt = new Integer(amountStr);
                BigDecimal amount = new BigDecimal(amountInt);
                Account recipient = new Account(pars.get(3).toString());

                String name = ASSET_NAME;
                boolean iconAsURL = true;
                int iconType = 0;
                boolean imageAsURL = true;
                int imageType = 0;
                Long startDate = null;
                Long stopDate = null;
                String tags = "";
                ExLinkAddress[] dexAwards = null;
                boolean isUnTransferable = false;

                boolean isAnonimDenied = false;


                String level;
                if (amountInt <= 10)
                    level = "Bronze";
                else if (amountInt <= 50)
                    level = "Silver";
                else if (amountInt <= 250)
                    level = "Gold";
                else if (amountInt <= 1000)
                    level = "Platinum";
                else
                    level = "Blue";

                //JSONArray array = new JSONArray();
                //array.add(coins); array.add(amount);
                String description = "<p>GG TREE</p>"
                        + "<p> Send to " + stock.getAddress() + " for GG + </p>";

                // SVG
                AssetUnique voucherAsset = new AssetUnique(AssetCls.makeAppData(
                        iconAsURL, iconType, imageAsURL, imageType, startDate, stopDate, tags, dexAwards, isUnTransferable, isAnonimDenied),
                        stock, name,
                        ("/dapps/voola/" + level + "_ico.svg").getBytes(StandardCharsets.UTF_8),
                        ("/dapps/voola/" + level + ".svg").getBytes(StandardCharsets.UTF_8),
                        description, AssetCls.AS_NON_FUNGIBLE);
                voucherAsset.setReference(commandTX.getSignature(), commandTX.getDBRef());

                //INSERT INTO BLOCKCHAIN DATABASE
                assetKey = assetMap.incrementPut(voucherAsset);

                // SET AMOUNT
                stock.changeBalance(dcSet, false, false, assetKey,
                        BigDecimal.ONE, false, false, true, 0);

                // TRANSFER ASSET
                transfer(dcSet, block, commandTX, stock, recipient, BigDecimal.ONE, assetKey, false, null, "mint");

                // store results for orphan
                /// putState(dcSet, refDB, new Object[]{assetKey});

                status = "done " + assetKey;

            } catch (Exception e) {
                fail(e.getMessage());
            }

        }

        return true;

    }

    private boolean withdraw(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {

        AssetCls asset = commandTX.getAsset();
        String coinsAmo = asset.getName().substring(ASSET_NAME.length()).trim();
        String[] split = coinsAmo.split(" ");
        String coins;
        String amount;
        if (split.length == 2) {
            amount = split[0];
            coins = split[1];
        } else {
            amount = coinsAmo.substring(0, coinsAmo.length() - 1);
            coins = "USD";
        }

        Long assetKey;
        if (coins.equals("USD"))
            assetKey = 1840L;
        else if(coins.equals("DOGE"))
            assetKey = 18L;
        else {
            fail("wrong coin");
            return false;
        }


        if (asOrphan) {

            transfer(dcSet, null, commandTX, stock, commandTX.getCreator(), new BigDecimal(amount), assetKey, true, null, null);

        } else {

            // TRANSFER ASSET
            transfer(dcSet, block, commandTX, stock, commandTX.getCreator(), new BigDecimal(amount), assetKey, false, null, "withdraw");

            status = "done";

        }

        return true;

    }

    //////////////////// ADMIN PROCESS

    /// INIT
    private boolean init(DCSet dcSet, Block block, Transaction commandTX, boolean asOrphan) {
        return true;
    }

    @Override
    public boolean processByTime(DCSet dcSet, Block block, Transaction transaction) {
        fail("unknown command");
        return false;
    }

    @Override
    public boolean process(DCSet dcSet, Block block, Transaction commandTX) {

        /// COMMANDS
        if (COMMAND_PLANT.equals(command)
                && (BlockChain.TEST_MODE || isAdminCommand(commandTX.getCreator())))
            return plant(dcSet, block, (RSend) commandTX, false);

            /// ADMIN COMMANDS
        else if ("init".equals(command))
            return init(dcSet, block, commandTX, false);

        else {
            RSend rSend = (RSend) commandTX;
            if (rSend.getAsset().getMaker().equals(stock))
                return withdraw(dcSet, block, rSend, false);
        }

        fail("unknown command" + (commandTX instanceof RSend && ((RSend)commandTX).hasAmount()
                && ((RSend)commandTX).balancePosition() == Account.BALANCE_POS_OWN? " ,thanks for donate project!" : "."));
        return false;

    }

    @Override
    public void orphanByTime(DCSet dcSet, Block block, Transaction transaction) {
    }

    @Override
    public void orphanBody(DCSet dcSet, Transaction commandTX) {

        /// COMMANDS
        if (COMMAND_PLANT.equals(command)
                && (BlockChain.TEST_MODE || isAdminCommand(commandTX.getCreator())))
            plant(dcSet, null, (RSend) commandTX, true);

            /// ADMIN COMMANDS
        else if ("init".equals(command))
            init(dcSet, null, commandTX, true);

        else {
            RSend rSend = (RSend) commandTX;
            if (rSend.getAsset().getMaker().equals(stock))
                withdraw(dcSet, null, rSend, true);
        }

    }

    public static GoGreenTree make(RSend txSend, String dataStr) {
        return new GoGreenTree(dataStr, "");
    }

    /// PARSE / TOBYTES

    public static GoGreenTree Parse(byte[] bytes, int pos, int forDeal) {

        // skip ID
        pos += 4;

        String data;
        String status;
        if (forDeal == Transaction.FOR_DB_RECORD) {
            byte[] dataSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int dataSize = Ints.fromByteArray(dataSizeBytes);
            pos += 4;
            byte[] dataBytes = Arrays.copyOfRange(bytes, pos, pos + dataSize);
            pos += dataSize;
            data = new String(dataBytes, StandardCharsets.UTF_8);

            byte[] statusSizeBytes = Arrays.copyOfRange(bytes, pos, pos + 4);
            int statusLen = Ints.fromByteArray(statusSizeBytes);
            pos += 4;
            byte[] statusBytes = Arrays.copyOfRange(bytes, pos, pos + statusLen);
            pos += statusLen;
            status = new String(statusBytes, StandardCharsets.UTF_8);

        } else {
            data = "";
            status = "";
        }

        return new GoGreenTree(data, status);
    }

    public static void setDAPPFactory(HashMap<Account, Integer> stocks) {
        stocks.put(MAKER, ID);
    }

}
