package org.erachain.dapp.epoch.memeTheGathering;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.exdata.exLink.ExLinkAddress;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetUnique;
import org.erachain.core.transaction.RSend;
import org.erachain.core.transaction.Transaction;
import org.erachain.dapp.EpochDAPPjson;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.SmartContractValues;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class MemeTheGathering_02DAPP extends EpochDAPPjson {

    // TODO add command MakeSetByAssetsList:  pars: NA-NC,FG
    int WAIT_RAND = 3;

    static public final int ID = 10022;
    static public final String NAME = "Meme The Gathering 02";

    final public static HashSet<PublicKeyAccount> accounts = new HashSet<>();

    // APPBx3R6LCJUGHgatrLFagBPhD9LdtTW58
    final public static PublicKeyAccount MAKER = PublicKeyAccount.makeForDApp(crypto.digest(Longs.toByteArray(ID)));

    static {
        accounts.add(MAKER);
    }

    /**
     * admin account
     */
    final static public Account adminAddress = new Account("7NhZBb8Ce1H2S2MkPerrMnKLZNf9ryNYtP");

    final static public long BUSTER_1_KEY = BlockChain.TEST_MODE ? 1048655L : 9999L;
    final static public long BUSTER_2_KEY = BlockChain.TEST_MODE ? 1048656L : 9999L;
    final static public int BUSTER_2_SET_COUNT = 14;
    final static public long SET_1_KEY = BlockChain.TEST_MODE ? 1048657L : 9999L;
    final static public int SET_1_NO = 1;
    final static public long BUSTER_2_LAND_A_KEY = BlockChain.TEST_MODE ? 1048671L : 9999L;
    final static public long BUSTER_2_LAND_B_KEY = BlockChain.TEST_MODE ? 1048672L : 9999L;

    public static final int RARE_COMMON = 0;
    public static final int RARE_UNCOMMON = 1;
    public static final int RARE_RARE = 2;
    public static final int RARE_EPIC = 3;

    public MemeTheGathering_02DAPP(String data, String status) {
        super(ID, MAKER, data, status);
    }

    public String getName() {
        return NAME;
    }

    public static MemeTheGathering_02DAPP make(RSend txSend, String dataStr) {
        return new MemeTheGathering_02DAPP(dataStr, "");
    }

    /// PARSE / TO BYTES

    public static MemeTheGathering_02DAPP Parse(byte[] bytes, int pos, int forDeal) {

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

        return new MemeTheGathering_02DAPP(data, status);
    }

    ///////// COMMANDS

    /**
     * @param block
     * @param transaction
     * @param nonce
     * @return
     */
    public static byte[] getRandHash(Block block, Transaction transaction, int nonce) {

        byte[] hash = new byte[32];
        System.arraycopy(block.getSignature(), 0, hash, 0, 14);
        System.arraycopy(Ints.toByteArray(nonce), 0, hash, 14, 4);
        System.arraycopy(transaction.getSignature(), 0, hash, 18, 14);

        return crypto.digest(hash);

    }

    /// из какого числа карт выбираем в данном бустрере - номер бустреа уже знает свой Набор (Set)
    public static Integer openBuster_2_getBaseAssetKey(int rareLevel, int random) {
        int totalSetItems;
        switch (rareLevel) {
            case RARE_COMMON:
                totalSetItems = BUSTER_2_SET_COUNT;
                break;
            case RARE_UNCOMMON:
            case RARE_RARE:
            case RARE_EPIC:
            default:
                return null;
        }
        return totalSetItems * random / (2 * Short.MAX_VALUE);
    }

    /**
     * make pack by RARE
     *
     * @return
     */
    private void openBuster_1_getPack(DCSet dcSet, Block block, RSend commandTX, int nonce, List actions, String busterName) {

        String setName = "whiteList";
        int rareLevel = RARE_RARE;

        // по 2 карты из всего набора
        for (int i=0; i<BUSTER_2_SET_COUNT; i++) {
            actions.add(makeAsset(dcSet, block, commandTX, SET_1_KEY+i, rareLevel, setName, SET_1_NO, i, busterName));
            actions.add(makeAsset(dcSet, block, commandTX, SET_1_KEY+i, rareLevel, setName, SET_1_NO, i, busterName));
        }

        // по 5 карт земель
        for (int i=0; i<5; i++) {
            // LAND A
            actions.add(makeAsset(dcSet, block, commandTX, BUSTER_2_LAND_A_KEY, rareLevel, setName, SET_1_NO, (int) (BUSTER_2_LAND_A_KEY - SET_1_KEY), busterName));
            // LAND B
            actions.add(makeAsset(dcSet, block, commandTX, BUSTER_2_LAND_B_KEY, rareLevel, setName, SET_1_NO, (int) (BUSTER_2_LAND_B_KEY - SET_1_KEY), busterName));
        }

    }

    /**
     * make pack by RARE
     *
     * @return
     */
    private void openBuster_2_getPack(DCSet dcSet, Block block, RSend commandTX, int nonce, List actions, String busterName) {

        // GET RANDOM
        byte[] randomArray = getRandHash(block, commandTX, nonce);
        int index = 0;
        // 5,71% - Uncommon = 100% / 17,51
        // see in org.erachain.dapp.epoch.memeTheGathering.MemoCards_01DAPPTest.tt
        int rareVal = Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index++]);
        int rareRes = (int)((long)rareVal * 10000L / (long) (Short.MAX_VALUE * 2));

        String setName = "Start!";

        int rareLevel;
        Integer cardNo;
        if (true || // тут не градаций пока по редкости внутри бустреа
                rareRes > 571) {
            rareLevel = RARE_COMMON;
            cardNo = openBuster_2_getBaseAssetKey(rareLevel, Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index-1]));
            actions.add(makeAsset(dcSet, block, commandTX, SET_1_KEY + cardNo, rareLevel, setName, SET_1_NO, cardNo, busterName));
            cardNo = openBuster_2_getBaseAssetKey(rareLevel, Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index-1]));
            actions.add(makeAsset(dcSet, block, commandTX, SET_1_KEY + cardNo, rareLevel, setName, SET_1_NO, cardNo, busterName));
            cardNo = openBuster_2_getBaseAssetKey(rareLevel, Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index-1]));
            actions.add(makeAsset(dcSet, block, commandTX, SET_1_KEY + cardNo, rareLevel, setName, SET_1_NO, cardNo, busterName));
            cardNo = openBuster_2_getBaseAssetKey(rareLevel, Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index-1]));
            actions.add(makeAsset(dcSet, block, commandTX, SET_1_KEY + cardNo, rareLevel, setName, SET_1_NO, cardNo, busterName));

        } else {
            rareLevel = RARE_UNCOMMON;
            cardNo = openBuster_2_getBaseAssetKey(rareLevel, Ints.fromBytes((byte) 0, (byte) 0, randomArray[index++], randomArray[index-1]));
            actions.add(makeAsset(dcSet, block, commandTX, SET_1_KEY + cardNo, rareLevel, setName, SET_1_NO, cardNo, busterName));
        }

        // LAND A
        actions.add(makeAsset(dcSet, block, commandTX, BUSTER_2_LAND_A_KEY, rareLevel, setName, SET_1_NO, (int)(BUSTER_2_LAND_A_KEY - SET_1_KEY), busterName));
        // LAND B
        actions.add(makeAsset(dcSet, block, commandTX, BUSTER_2_LAND_B_KEY, rareLevel, setName, SET_1_NO, (int)(BUSTER_2_LAND_B_KEY - SET_1_KEY), busterName));

    }

    /**
     * Make new MEMO CARD by BASE ASSET KEY
     * @param dcSet
     * @param block
     * @param commandTX
     * @param assetBaseKey
     * @param description
     * @param setName
     * @return
     */
    private Long makeAssetByBaseAssetKey(DCSet dcSet, Block block, RSend commandTX, long assetBaseKey, String description,
                                         String setName, String busterName) {

        AssetCls assetBase = dcSet.getItemAssetMap().get(assetBaseKey);

        String name = assetBase.getName();

        boolean iconAsURL = true;
        int iconType = 0;
        boolean imageAsURL = true;
        int imageType = 0;
        Long startDate = null;
        Long stopDate = null;
        setName = "memocard, " + setName;
        ExLinkAddress[] dexAwards = assetBase.getDEXAwards();
        boolean isUnTransferable = false;
        boolean isAnonimDenied = false;

        AssetUnique randomAsset = new AssetUnique(AssetCls.makeAppData(
                iconAsURL, iconType, imageAsURL, imageType, startDate, stopDate, setName, dexAwards, isUnTransferable, isAnonimDenied),
                stock, name, ("/apiasset/icon/" + assetBaseKey).getBytes(StandardCharsets.UTF_8),
                ("/apiasset/image/" + assetBaseKey).getBytes(StandardCharsets.UTF_8),
                description, AssetCls.AS_NON_FUNGIBLE);
        randomAsset.setReference(commandTX.getSignature(), commandTX.getDBRef());

        //INSERT INTO BLOCKCHAIN DATABASE
        Long assetKey = dcSet.getItemAssetMap().incrementPut(randomAsset);

        // SET AMOUNT
        stock.changeBalance(dcSet, false, false, assetKey,
                BigDecimal.ONE, false, false, true, 0);

        // TRANSFER ASSET
        transfer(dcSet, block, commandTX, stock, commandTX.getCreator(), BigDecimal.ONE, assetKey, false, null, "buster " + busterName);

        return assetKey;

    }

    private Long makeAsset(DCSet dcSet, Block block, RSend commandTX, Long assetBaseKey, int rareLevel,
                           String setName, int setNo, int cardNo, String busterName) {

        if (assetBaseKey == null) {
            fail("makeAsset error 01");
            return null;
        }

        AssetCls assetBase = dcSet.getItemAssetMap().get(assetBaseKey);

        // make new MEMO CARD
        JSONObject json = new JSONObject();
        json.put("rare", rareLevel);
        json.put("set", setName);
        json.put("setNo", setNo);
        json.put("no", cardNo + 1); // start from 1
        json.put("type", "card");
        json.put("buster", busterName);
        String description = assetBase.getDescription() + "\n\n@" + json.toJSONString();
        return makeAssetByBaseAssetKey(dcSet, block, commandTX, assetBaseKey, description, setName, busterName);

    }


    /**
     * For that Buster and Amount it. Если номер актива не тот - ничего не сделает
     * @param dcSet
     * @param block
     * @param commandTX
     * @param busterKey
     * @param amount
     */
    private void openBusterRound(DCSet dcSet, Block block, RSend commandTX, List actions, Long busterKey, BigDecimal amount) {

        AssetCls buster = dcSet.getItemAssetMap().get(busterKey);
        String busterName = buster.getName();

        int count = amount.intValue();
        if (busterKey == BUSTER_1_KEY || busterKey == 1L ) {
            do {
                openBuster_1_getPack(dcSet, block, commandTX, count, actions, busterName);
            } while (--count > 0);
        } else if (busterKey == BUSTER_2_KEY || busterKey == 2L) {
            do {
                openBuster_2_getPack(dcSet, block, commandTX, count, actions, busterName);
            } while (--count > 0);
        }

    }
    /**
     * @param dcSet
     * @param commandTX
     * @param asOrphan
     */
    private boolean openBusters(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {
        // открываем бустер

        if (asOrphan) {

            SmartContractValues valuesMap = dcSet.getSmartContractValues();
            Object[] actions = removeState(dcSet, commandTX.getDBRef());

            int index = actions.length;
            Long assetKey;
            ItemCls asset;

            while (--index > 0) {
                assetKey = (Long) actions[index];

                // RESET AMOUNT
                stock.changeBalance(dcSet, true, false, assetKey,
                        BigDecimal.ONE, false, false, true, 0);

                transfer(dcSet, null, commandTX, stock, commandTX.getCreator(), BigDecimal.ONE, assetKey, true, null, null);

                // DELETE FROM BLOCKCHAIN DATABASE
                asset = dcSet.getItemAssetMap().decrementRemove(assetKey);

                // DELETE FROM CONTRACT DATABASE
                valuesMap.delete(new Tuple2(ID, asset.getName()));

            }

            status = "wait";

            return true;
        }

        if (!commandTX.hasAmount() || !commandTX.hasPacket() && commandTX.getAmount().signum() <= 0) {
            fail("Wrong amount. Need > 0");
            return false;
        } else if (commandTX.isBackward()) {
            fail("Wrong direction - backward");
            return false;
        } else if (commandTX.balancePosition() != Account.BALANCE_POS_OWN) {
            fail("Wrong balance position. Need OWN[1]");
            return false;
        }

        List actions = new ArrayList();
        if (commandTX.hasPacket()) {
            Object[][] packet = commandTX.getPacket();
            for (Object[] amount: packet) {
                Long key = (Long) amount[0];
                BigDecimal vol = (BigDecimal) amount[1];
                openBusterRound(dcSet, block, commandTX, actions, key, vol);
            }
        } else {
            openBusterRound(dcSet, block, commandTX, actions, commandTX.getAssetKey(), commandTX.getAmount());
        }

        putState(dcSet, commandTX.getDBRef(), actions.toArray());

        status = "done";

        return true;

    }

    @Override
    public boolean process(DCSet dcSet, Block block, Transaction commandTX) {

        if (commandTX instanceof RSend) {
            RSend rsend = (RSend) commandTX;

            if (!rsend.hasAmount() || !rsend.hasPacket() && commandTX.getAmount().signum() <= 0) {
                fail("Wrong amount. Need > 0");
                return false;
            } else if (rsend.isBackward()) {
                fail("Wrong direction - backward");
                return false;
            } else if (rsend.balancePosition() != Account.BALANCE_POS_OWN) {
                fail("Wrong balance position. Need OWN[1]");
                return false;
            } else if (block == null) {
                fail("null block");
                return false;
            }

            /// WAIT RANDOM FROM FUTURE
            dcSet.getTimeTXWaitMap().put(commandTX.getDBRef(), block.heightBlock + WAIT_RAND);
            status = "wait";
            return false;

        }

        fail("unknown command");
        return false;

    }

    @Override
    public boolean processByTime(DCSet dcSet, Block block, Transaction transaction) {

        try {
            return openBusters(dcSet, block, (RSend) transaction, false);
        } catch (Exception e) {
            fail(e.getMessage());
        }

        return false;

    }

    @Override
    public void orphanBody(DCSet dcSet, Transaction commandTX) {
    }

    @Override
    public void orphanByTime(DCSet dcSet, Block block, Transaction transaction) {
        if (status.startsWith("fail"))
            return;

        openBusters(dcSet, block, (RSend) transaction, true);

    }

    /**
     * add it to org.erachain.dapp.DAPPFactory
     * @param stocks
     */
    public static void setDAPPFactory(HashMap<Account, Integer> stocks) {
        for (Account account : accounts) {
            stocks.put(account, ID);
        }
    }

}
