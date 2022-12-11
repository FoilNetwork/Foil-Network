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
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.dapp.EpochDAPPjson;
import org.erachain.datachain.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Send public mail to APPBNt7cZp89L5j47Ud62ZRSiKb1Y9hYjD
 * Set text: ["mint","DOGE", 100, "7Recipient"]
 */
public class Voucher extends EpochDAPPjson {


    static public final int ID = 99012;
    static public final String NAME = "Voucher dApp";
    static public final String ASSET_NAME = "Red envelope";

    // APPBNt7cZp89L5j47Ud62ZRSiKb1Y9hYjD
    final public static PublicKeyAccount MAKER = PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(ID)));

    /**
     * admin account
     */
    final static public Account adminAddress = new Account("7NhZBb8Ce1H2S2MkPerrMnKLZNf9ryNYtP");

    /**
     * ASSET KEY
     */
    static final private Tuple2 INIT_KEY = new Tuple2(ID, "i");
    final static public String COMMAND_MINT = "mint";

    public Voucher(String data, String status) {
        super(ID, MAKER, data, status);
    }

    public String getName() {
        return NAME;
    }

    private boolean isAdminCommand(Account txCreator) {
        return txCreator.equals(adminAddress);
    }


    ///////// COMMANDS
    private boolean mint(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {

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

            try {
                // ["mint", "DOGE", 100, "7sadiuwyer7625346XXX"] - command, coins, amount, recipient
                status = "";
                String coins = (String) pars.get(1);
                String amountStr = pars.get(2).toString();
                BigDecimal amount = new BigDecimal(amountStr);
                Account recipient = new Account(pars.get(3).toString());

                String name = ASSET_NAME;
                boolean iconAsURL = true;
                int iconType = 0;
                boolean imageAsURL = true;
                int imageType = 0;
                long protoAssetKey = 1050898;
                Long startDate = null;
                Long stopDate = null;
                String tags = "";
                ExLinkAddress[] dexAwards = null;
                boolean isUnTransferable = false;

                boolean isAnonimDenied = false;
                if (false && (coins.equals("DOGE") && amount.compareTo(new BigDecimal("500")) > 0
                        || coins.equals("USD") && amount.compareTo(new BigDecimal("1000")) > 0)) {
                    isAnonimDenied = true;
                }

                name += " " + amountStr;
                if (coins.equals("USD"))
                    name += "$";
                else if (coins.equals("DOGE"))
                    name += " " + coins;
                else {
                    fail("wrong coin");
                    return false;
                }

                //JSONArray array = new JSONArray();
                //array.add(coins); array.add(amount);
                String description = "<p>Stable <b>Voola</b> tokens exist as NFT tokens of the ERC-721 standard, built on several leading blockchains. The StableNFT architecture consists of open source smart contracts and the NFT PAY software package, which interacts with blockchains, allowing you to pay, store, send and receive funds using a non-fungible token as a payment method.\n" +
                        "Each NFT token is 100% backed by our reserves, which include traditional currency and cash equivalents, and may also include other assets and accounts receivable on loans provided by Voola to third parties.</p>"
                        + "<p>voola稳定令牌是建立在几个领先区块链上的ERC-721标准的NFT令牌。StableNFT架构由开源智能合约和几组与区块链交互的NFT支付软件组成，允许您使用不可替换的令牌作为付款方式进行支付、存储、发送和收款。\n" +
                        "每个NFT令牌100%由我们的储备金担保，包括传统的货币和现金等价物，以及由voola提供给第三方的其他资产和应收账款。</p>"
                        + "<p> Send to " + stock.getAddress() + " for withdraw + </p>";

                AssetUnique voucherAsset = new AssetUnique(AssetCls.makeAppData(
                        iconAsURL, iconType, imageAsURL, imageType, startDate, stopDate, tags, dexAwards, isUnTransferable, isAnonimDenied),
                        stock, name, ("/apiasset/icon/" + protoAssetKey).getBytes(StandardCharsets.UTF_8),
                        ("/apiasset/image/" + protoAssetKey).getBytes(StandardCharsets.UTF_8),
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
        if (COMMAND_MINT.equals(command)
                && (BlockChain.TEST_MODE || isAdminCommand(commandTX.getCreator())))
            return mint(dcSet, block, (RSend) commandTX, false);

            /// ADMIN COMMANDS
        else if ("init".equals(command))
            return init(dcSet, block, commandTX, false);

        else {
            RSend rSend = (RSend) commandTX;
            if (rSend.getAsset().getMaker().equals(stock))
                return withdraw(dcSet, block, rSend, false);
        }

        fail("unknown command");
        return false;

    }

    @Override
    public void orphanByTime(DCSet dcSet, Block block, Transaction transaction) {
    }

    @Override
    public void orphanBody(DCSet dcSet, Transaction commandTX) {

        /// COMMANDS
        if (COMMAND_MINT.equals(command)
                && (BlockChain.TEST_MODE || isAdminCommand(commandTX.getCreator())))
            mint(dcSet, null, (RSend) commandTX, true);

            /// ADMIN COMMANDS
        else if ("init".equals(command))
            init(dcSet, null, commandTX, true);

        else {
            RSend rSend = (RSend) commandTX;
            if (rSend.getAsset().getMaker().equals(stock))
                withdraw(dcSet, null, rSend, true);
        }

    }

    public static Voucher make(RSend txSend, String dataStr) {
        return new Voucher(dataStr, "");
    }

    /// PARSE / TOBYTES

    public static Voucher Parse(byte[] bytes, int pos, int forDeal) {

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

        return new Voucher(data, status);
    }

    public static void setDAPPFactory(HashMap<Account, Integer> stocks) {
        stocks.put(MAKER, ID);
    }

}
