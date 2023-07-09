package org.erachain.dapp.epoch;

import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
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
    static public final long O2_ASSET_KEY = BlockChain.TEST_MODE? 1048578L : 1048578L;
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
                            name = "Oakzilla";
                            description = "In a small gardening shop, aspiring botanist Evgenii stumbled upon a magical flower pot tucked away in a corner. Little did he know that this humble pot would be the vessel for an extraordinary adventure. Placing an ordinary acorn inside, Harold watched in amazement as the acorn sprouted into Oakzilla, a mighty oak tree with dreams of grandeur.\n\n" +
                                    "Despite Oakzilla's ambitions of becoming the most powerful tree in the world, fate had a different plan in store. Instead of growing in a vast forest, Oakzilla found itself confined within the constraints of a flowerpot. But that didn't deter Oakzilla's determination. With each passing day, its branches stretched and twisted, trying to break free from its pot-sized prison. Though small in size, Oakzilla's spirit remained larger than life, and it became a legend among potted plants, inspiring them to dream big and reach for the sky.\n\n" +
                                    "Now, Oakzilla stands as a reminder that true strength and greatness can be found in the most unexpected places. Despite its unconventional home, Oakzilla's indomitable spirit and resilience have made it a symbol of perseverance and the power of dreams. And who knows, maybe one day, Oakzilla's ambitions will be realized, and it will become the mightiest potted tree in the world, forever etching its name in horticultural history.";
                            break;
                        case 1:
                            name = "HopsterInk";
                            description = "In a small tattoo parlour, renowned for its unique designs, an extraordinary event unfolded. One fateful day, as the tattoo artist was crafting a masterpiece, a tiny hop plant named HopsterInk magically sprouted from the ink bottle. To everyone's surprise, the plant's leaves and vines were inspired in tattoo designs, as if it had absorbed the artistry of the parlour. With its vibrant colors and creative patterns, HopsterInk quickly became the talk of the town, attracting beer enthusiasts and tattoo aficionados alike.\n\n" +
                                    "Little did they know that HopsterInk was not just an ordinary hop plant. Its origins were rooted in the rich history of brewing and the art of tattooing. As it grew, it shared the fascinating biological and historic data of its kind. From its ancient use in brewing traditions to its role in enhancing the flavors of beers, HopsterInk became a living embodiment of the perfect blend of art and hops. Its presence in the tattoo parlour brought laughter, inspiration, and a unique twist to the world of beer and body art";
                            break;
                        case 2: name = "Elmlet";
                            description = "In a stroke of ingenious eccentricity, a brilliant inventor named Joan created a digital controller that doubled as a caretaker for an Ulmus parvifolia bonsai named Elmlet. This tree, with its pint-sized grandeur and a mischievous sense of humour, quickly became the star of the gaming world. As players embarked on epic quests and intense battles, Elmlet would hilariously sway its branches in sync with the on-screen action, adding an extra level of immersion and amusement.\n\n" +
                                    "With each button press, the controller not only triggered virtual actions but also dispensed a fine mist of water and played soothing tunes for Elmlet's botanical enjoyment. It was a sight to behold—a gamer furiously pressing buttons while simultaneously watering and serenading a bonsai tree. Elmlet, with its witty antics and vibrant foliage, became a legend among gamers, reminding everyone that even in the realm of pixels and pixels, nature finds a way to bring laughter and delight.";
                            break;
                        case 3: name = "Mysteria";
                            description = "On a peaceful afternoon, Mysteria, a mischievous wisteria tree, surprised everyone by sprouting on top of a museum. It was as if nature had a whimsical sense of humour, adorning the stately building with its cascading vines and vibrant blooms. Mysteria's arrival brought laughter and wonder to all who encountered it, as visitors marvelled at the unexpected beauty flourishing in such an unconventional location. The museum staff affectionately nicknamed it \"Mysty,\" and its presence became a beloved symbol of creativity and the extraordinary wonders of the natural world.\n\n" +
                                    "The wisteria, known for its enchanting blossoms and alluring fragrance, originates from East Asia. Its delicate petals and cascading tendrils create a mesmerizing spectacle that has captured the hearts of garden enthusiasts worldwide. Beyond its aesthetic appeal, the wisteria plays a vital role in the ecosystem, attracting a diverse range of pollinators with its sweet nectar. Mysteria's presence atop the culture museum serves as a reminder of the beauty and importance of biodiversity, inviting visitors to appreciate the extraordinary and find inspiration in unexpected places.";
                            break;
                        case 4: name = "Pop-the-Pot";
                            description = "In a cozy backyard, Pop-the-Pot, a spirited populus tremuloides yearns for freedom beyond the confines of his flower pot. Inspired by the storied history of quaking aspens, with their resilience in forest fires and regenerative abilities, Pop-the-Pot hatches a daring plan to escape. With a team of loyal squirrel allies, they meticulously plot and strategize, exploring every possibility to ensure a foolproof escape. Pop-the-Pot's anticipation grows as he waits for the perfect moment to put their plan into action, determined to join the wild adventures of the majestic quaking aspens and be a part of the intricate web of life.\n\n" +
                                    "The populus tremuloides, or quaking aspen, is a native North American tree known for its striking white bark and fluttering leaves. As a keystone species, it provides vital resources and habitat for diverse wildlife, while its interconnected root system helps maintain soil stability. Inspired by the resilience and regenerative abilities of quaking aspens, Pop-the-Pot draws upon their storied history to fuel his ambition for freedom. With the assistance of his loyal squirrel allies, they embark on a daring and meticulously planned escape, eager to join the quaking aspens and contribute to the intricate tapestry of life in the wild.";
                            break;
                        case 5: name = "Chewinko";
                            description = "In a small town, bubble gum enthusiast Bruno stumbled upon a magical bubble gum dispenser at a flea market. Instead of gum, it sprouted a tiny ginkgo seedling, which he named Chewinko. To everyone's surprise, Chewinko grew into a magnificent tree right inside the dispenser, delighting the town with its mischievous bubble gum-themed antics. This enchanting ginkgo, with its roots intertwined with the spirit of bubble gum, has become a cherished symbol of joy and unexpected beauty, reminding us to embrace the extraordinary in the ordinary.\n\n" +
                                    "Discover Chewinko, the ginkgo tree growing inside a bubble gum dispenser. Its whimsical presence brings laughter and wonder to all who encounter it. Beyond its delightful origins, Chewinko is rooted in the fascinating biology and history of the ginkgo tree. With a lineage stretching back over 270 million years, the ginkgo is a living relic from prehistoric times. Renowned for its unique fan-shaped leaves, golden hues, and medicinal properties, the ginkgo tree holds a rich cultural and historical heritage. Chewinko embodies the resilience and endurance of its ancient ancestors, reminding us of the enduring legacy of the ginkgo tree and the captivating stories it holds.";
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

        // для всех новых новый протокол
        boolean newO2 = block == null || O2_START_BLOCK < block.heightBlock;

        Long bonusKey;
        BigDecimal bonusAmount;

        if (asOrphan) {

            // RESTORE DATA
            Object[] result = removeState(dcSet, refDB);

            if (newO2) {
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

                int addLevel = newO2? 300 : 200;
                byte[] image;
                int level;
                if (vol.compareTo(new BigDecimal(100)) < 0) {
                    level = 1;
                    image = ("/dapps/gogreentree/tree_" + type + "_0.png").getBytes(StandardCharsets.UTF_8);
                } else if (vol.compareTo(new BigDecimal(100 + addLevel)) < 0) {
                    level = 2;
                    image = ("/dapps/gogreentree/tree_" + type + "_1.png").getBytes(StandardCharsets.UTF_8);
                } else if (vol.compareTo(new BigDecimal(100 + 2 * addLevel)) < 0) {
                    level = 3;
                    image = ("/dapps/gogreentree/tree_" + type + "_2.png").getBytes(StandardCharsets.UTF_8);
                } else {
                    level = 4;
                    image = ("/dapps/gogreentree/tree_" + type + "_3.png").getBytes(StandardCharsets.UTF_8);
                }

                // обновим уровень и описание у дерева
                AssetUnique treeAsset = new AssetUnique(ggTree.getAppData(),
                        stock, ggTree.getName(),
                        ggTree.getIcon(),
                        image,
                        json.toString(), AssetCls.AS_NON_FUNGIBLE);
                treeAsset.setReference(ggTree.getReference(), ggTree.getDBref());
                dcSet.getItemAssetMap().put(ggTreeKey, treeAsset);

                if (newO2) {
                    bonusKey = O2_ASSET_KEY;
                    bonusAmount = amount.divide(BigDecimal.TEN, 8, BigDecimal.ROUND_UP).multiply(BigDecimal.valueOf(level));

                    // store results for orphan
                    putState(dcSet, refDB, new Object[]{ggTreeKey, ggTree.getImage(), ggTree.getDescription(), bonusAmount});

                } else {

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
