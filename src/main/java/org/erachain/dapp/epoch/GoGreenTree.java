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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Send public 20 GG to APPC3f7Sa6fABm7woHfiQPbQd38Wy9cJMJ
 * Set text: ["plant", 0, "7Mbik4Je6RXnsoE7dKhj6XXLcDU4WbPY9o"]
 * ["pour", 1048577]
 */
public class GoGreenTree extends EpochDAPPjson {


    static public final int ID = 99017;
    static public final String NAME = "GoGreenTree dApp";
    static public final String ASSET_NAME = "GGT";
    static public final long GO_GREEN_ASSET_KEY = BlockChain.TEST_MODE? 1048577L : 1048577L;
    static public final long O2_ASSET_KEY = BlockChain.TEST_MODE? 1156L : 1048577L;
    static public final int O2_START_BLOCK = BlockChain.TEST_MODE? 1 : 1048577;
    static public final BigDecimal MIN_VALUE = new BigDecimal("20");

    //
    // APPC3f7Sa6fABm7woHfiQPbQd38Wy9cJMJ
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
    //final static public String COMMAND_POUR = "pour";
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

    /**
     * Use: ["plant", type, "recipient address"] - ["plant", 0, "7Mbik4Je6RXnsoE7dKhj6XXLcDU4WbPY9o"]
     * @param dcSet
     * @param block
     * @param commandTX
     * @param asOrphan
     * @return
     */
    private boolean plant(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {

        //Long refDB = commandTX.getDBRef();
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
            else if (commandTX.getAbsKey() != GO_GREEN_ASSET_KEY)
                wrong = "wrong asset, need: " + GO_GREEN_ASSET_KEY;
            else if (commandTX.balancePosition() != Account.BALANCE_POS_OWN)
                wrong = "wrong balance position, need: " + Account.BALANCE_POS_OWN;
            else if (commandTX.getAmount().compareTo(MIN_VALUE) < 0)
                wrong = "wrong amount, need: >=" + MIN_VALUE.toPlainString();

            if (wrong != null) {
                fail(wrong + (commandTX.hasAmount() && commandTX.balancePosition() == Account.BALANCE_POS_OWN ?
                        ", thanks for donate project!" : "."));
                return false;
            }

            BigDecimal amount = commandTX.getAmount();
            String name;

            try {
                String description;
                JSONObject json = new JSONObject();
                json.put("v", amount.toPlainString());

                // ["plant", "type", 100, "7sadiuwyer7625346XXX"] - command, type, amount, recipient
                status = "Use: [\"plant\", \"type\", \"recipient address\"], wrong TYPE: ";
                String type = pars.get(1).toString();
                try {
                    int tt = Integer.parseInt(type);
                    if (tt < 0 || tt > 5) {
                        status = "wrong TYPE <> 0..5 ";
                        return false;
                    }

                    json.put("t", type);

                    switch (tt) {
                        case 0:
                            name = "The Mighty Oak";
                            description = "<h2 style=\"color: #2e6c80;\">Prize: Plant a tree in Madagascar</h2>" +
                                    "<p><a href=\"https://tree-nation.com/profile/gogreen\">https://tree-nation.com/profile/gogreen</a></p>" +
                                    "<p>The mighty oak tree, towering over the forest floor, is a symbol of strength and longevity. Its sturdy trunk and sprawling branches provide a home for countless creatures, while its acorns nourish a multitude of forest dwellers. With a lifespan of up to 1,000 years, the oak has stood the test of time, a testament to the resilience of nature.</p>";
                            break;
                        case 1:
                            name = "Hop";
                            description = "<h2 style=\"color: #2e6c80;\">Prize: Ambar Triple Zero beer</h2>" +
                                    "<p><span style=\"font-weight: 400;\">Discover the hop plant. With its winding vines and delicate cones, it is a vital component in the creation of beer. From the soil to the sun, every element of its growth is meticulously cultivated to produce the finest flavor. With its rich history and cultural significance, the hop plant is truly a botanical wonder.</span></p>";
                            break;
                        case 2: name = "Ulmus parvifolia";
                            description = "<h2 style=\"color: #2e6c80;\">Prize: Ticket to the FC Barcelona museum</h2>" +
                                    "<p><span style=\"font-weight: 400;\">Discover the Ulmus parvifolia bonzai, a tree whose small stature belies its beauty and complexity. Through the careful cultivation of its branches and roots, the Ulmus parvifolia is transformed into a breathtaking work of art, embodying the harmony and balance of nature. It is a symbol of resilience and adaptability as well as a living testament to the skill and dedication of its caretaker.</span></p>";
                            break;
                        case 3: name = "Wisteria";
                            description = "<h2 style=\"color: #2e6c80;\">Prize: Ticket to the Constel&middot;laci&oacute; gr&agrave;fica exhibition at CCCB</h2>" +
                                    "<p><span style=\"font-weight: 400;\">The wisteria blooms in a magnificent display of beauty beneath the warm sun of East Asia. With its cascading tendrils and delicate petals, it is a true marvel of nature. A beloved feature of gardens and parks around the world, the wisteria's sweet fragrance and vibrant colors attract a diverse array of pollinators, making it a vital member of the ecosystem. The wisteria is a true testament to the beauty and importance of biodiversity in our world.</span></p>";
                            break;
                        case 4: name = "Populus tremuloides";
                            description = "<h2 style=\"color: #2e6c80;\">Prize: &ldquo;Analyzing the crypto market&rdquo; course</h2>" +
                                    "<p>The populus tremuloides is native to North America, where it is commonly known as the quaking aspen. With its striking white bark and fluttering leaves, it is a true wonder of the natural world. As a keystone species, it provides food and shelter for countless animals, while its interconnected root system helps to maintain soil stability. The quaking aspen truly embodies the intricate and interconnected web of life in our forests.</p>";
                            break;
                        case 5: name = "Ginko";
                            description = "<h2 style=\"color: #2e6c80;\">Prize: Ticket to the MACBA museum</h2>" +
                                    "<p>Discover the ginkgo tree from the ancient forests of China. It stands as a living relic of a bygone era. With its fan-shaped leaves and golden hues, it is a true marvel of nature. Known for its medicinal properties and resilience in the face of adversity, the ginkgo tree has captured the hearts and minds of people around the world. As a symbol of longevity and endurance, the ginkgo tree has played an important role in the cultural and spiritual traditions of many societies.</p>";
                            break;
                        default:
                            fail("wrong type");
                            return false;
                    }

                    json.put("d", description);

                } catch (Exception e) {
                    fail(e.getMessage() + "\n" + e.getStackTrace().toString());
                    return false;
                }

                status = "Use: [\"plant\", \"type\", \"recipient address\"], wrong recipient address: ";
                Account recipient = new Account(pars.get(2).toString());

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

                AssetUnique treeAsset = new AssetUnique(AssetCls.makeAppData(
                        iconAsURL, iconType, imageAsURL, imageType, startDate, stopDate, tags, dexAwards, isUnTransferable, isAnonimDenied),
                        stock, name,
                        null, //("/dapps/gogreentree/" + type + "0_ico.svg").getBytes(StandardCharsets.UTF_8),
                        ("/dapps/gogreentree/tree_" + type + "_0.png").getBytes(StandardCharsets.UTF_8),
                        json.toString(), AssetCls.AS_NON_FUNGIBLE);
                treeAsset.setReference(commandTX.getSignature(), commandTX.getDBRef());

                //INSERT INTO BLOCKCHAIN DATABASE
                assetKey = assetMap.incrementPut(treeAsset);

                // SET AMOUNT
                stock.changeBalance(dcSet, false, false, assetKey,
                        BigDecimal.ONE, false, false, true, 0);

                // TRANSFER ASSET
                transfer(dcSet, block, commandTX, stock, recipient, BigDecimal.ONE, assetKey, false, null, "mint");

                // store results for orphan
                /// putState(dcSet, refDB, new Object[]{assetKey});

                status = "done " + assetKey;

            } catch (Exception e) {
                fail(status + "{" + e.getMessage() + "}" + (commandTX.hasAmount() && commandTX.balancePosition() == Account.BALANCE_POS_OWN ?
                        ", thanks for donate project!" : "."));
            }

        }

        return true;

    }

    /**
     * update some ggTree
     * Use: ["pour", GoGreen_Tree_key] - ["pour", 3108]
     * @param dcSet
     * @param block
     * @param commandTX
     * @param asOrphan
     * @return
     */
    private boolean care(DCSet dcSet, Block block, RSend commandTX, boolean asOrphan) {

        Long refDB = commandTX.getDBRef();
        ItemAssetMap assetMap = dcSet.getItemAssetMap();

        Long bonusKey;
        BigDecimal bonusAmount;

        if (asOrphan) {

            // RESTORE DATA
            Object[] result = removeState(dcSet, refDB);

            if (O2_START_BLOCK < block.heightBlock) {
                bonusKey = O2_ASSET_KEY;
                bonusAmount = (BigDecimal) result[3];

            } else {
                bonusKey = assetMap.getLastKey();
                bonusAmount = BigDecimal.ONE;
            }

            transfer(dcSet, null, commandTX, stock, commandTX.getCreator(), bonusAmount, bonusKey, true, null, null);

            // store results for orphan
            putState(dcSet, refDB, new Object[]{bonusAmount});


        } else {

            String wrong = null;
            if (!commandTX.hasAmount())
                wrong = "empty amount";
            else if (commandTX.getAbsKey() != GO_GREEN_ASSET_KEY)
                wrong = "wrong asset, need: " + GO_GREEN_ASSET_KEY;
            else if (commandTX.balancePosition() != Account.BALANCE_POS_OWN)
                wrong = "wrong balance position, need: " + Account.BALANCE_POS_OWN;
            else if (commandTX.getAmount().compareTo(MIN_VALUE) < 0)
                wrong = "wrong amount, need: >=" + MIN_VALUE.toPlainString();

            if (wrong != null) {
                fail(wrong + (commandTX.hasAmount() && commandTX.balancePosition() == Account.BALANCE_POS_OWN ?
                        ", thanks for donate project!" : "."));
                return false;
            }

            BigDecimal amount = commandTX.getAmount();

            try {
                // ["care", "GoGreen Tree key"] - ["care", "12032"]
                status = "Use: [\"care\", \"GoGreen Tree key\"], wrong Tree key: ";
                Long ggTreeKey = (Long) pars.get(1);

                AssetCls ggTree = dcSet.getItemAssetMap().get(ggTreeKey);
                if (!ggTree.getMaker().equals(MAKER)) {
                    fail("not GoGreen Tree asset");
                    return false;
                }

                JSONParser jsonParser = new JSONParser();
                JSONObject json = (JSONObject) jsonParser.parse(ggTree.getDescription());
                if (json == null) {
                    fail("ggTree desc JSON error");
                    return false;
                }

                BigDecimal vol = new BigDecimal(json.get("v").toString());

                vol = amount.add(vol);
                json.put("v", vol.toPlainString());

                String type = (String) json.get("t");

                byte[] image;
                int level;
                if (vol.compareTo(new BigDecimal("100")) < 0) {
                    level = 1;
                    image = ("/dapps/gogreentree/tree_" + type + "_0.png").getBytes(StandardCharsets.UTF_8);
                } else if (vol.compareTo(new BigDecimal("300")) < 0) {
                    level = 2;
                    image = ("/dapps/gogreentree/tree_" + type + "_1.png").getBytes(StandardCharsets.UTF_8);
                } else if (vol.compareTo(new BigDecimal("500")) < 0) {
                    level = 3;
                    image = ("/dapps/gogreentree/tree_" + type + "_2.png").getBytes(StandardCharsets.UTF_8);
                } else {
                    level = 4;
                    image = ("/dapps/gogreentree/tree_" + type + "_3.png").getBytes(StandardCharsets.UTF_8);
                }

                if (O2_START_BLOCK < block.heightBlock) {
                    bonusKey = O2_ASSET_KEY;
                    bonusAmount = amount.divide(BigDecimal.TEN, 8, BigDecimal.ROUND_UP).multiply(BigDecimal.valueOf(level));

                    // store results for orphan
                    putState(dcSet, refDB, new Object[]{ggTreeKey, ggTree.getImage(), ggTree.getDescription(), bonusAmount});

                } else {

                    AssetUnique treeAsset = new AssetUnique(ggTree.getAppData(),
                            stock, ggTree.getName(),
                            ggTree.getIcon(),
                            image,
                            json.toString(), AssetCls.AS_NON_FUNGIBLE);
                    treeAsset.setReference(ggTree.getReference(), ggTree.getDBref());
                    dcSet.getItemAssetMap().put(ggTreeKey, treeAsset);

                    bonusKey = 1L + GO_GREEN_ASSET_KEY + Long.parseLong(type);
                    bonusAmount = BigDecimal.ONE;

                    // store results for orphan
                    putState(dcSet, refDB, new Object[]{ggTreeKey, ggTree.getImage(), ggTree.getDescription(), bonusKey});

                }

                // TRANSFER ASSET
                transfer(dcSet, block, commandTX, stock, commandTX.getCreator(), bonusAmount,
                        bonusKey,
                        false, null, "care bonus");

                status = "done. New vol: " + vol.toPlainString();


            } catch (Exception e) {
                fail(status + "{" + e.getMessage() + "}" + (commandTX.hasAmount() && commandTX.balancePosition() == Account.BALANCE_POS_OWN ?
                        ", thanks for donate project!" : "."));
            }

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
        else if (COMMAND_POUR.equals(command))
            return care(dcSet, block, (RSend) commandTX, false);

            /// ADMIN COMMANDS
        else if ("init".equals(command))
            return init(dcSet, block, commandTX, false);

        else {
            RSend rSend = (RSend) commandTX;
        }

        fail("unknown command" + (commandTX instanceof RSend && ((RSend)commandTX).hasAmount()
                && ((RSend)commandTX).balancePosition() == Account.BALANCE_POS_OWN? ", thanks for donate project!" : "."));
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
        else if (COMMAND_POUR.equals(command))
            care(dcSet, null, (RSend) commandTX, true);

            /// ADMIN COMMANDS
        else if ("init".equals(command))
            init(dcSet, null, commandTX, true);

        else {
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
