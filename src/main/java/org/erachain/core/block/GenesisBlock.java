package org.erachain.core.block;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import org.apache.commons.net.util.Base64;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.item.assets.AssetVenture;
import org.erachain.core.item.statuses.Status;
import org.erachain.core.item.statuses.StatusCls;
import org.erachain.core.item.templates.Template;
import org.erachain.core.item.templates.TemplateCls;
import org.erachain.core.transaction.*;
import org.erachain.datachain.DCSet;
import org.erachain.settings.Settings;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GenesisBlock extends Block {

    public static final PublicKeyAccount CREATOR = new PublicKeyAccount(new byte[PublicKeyAccount.PUBLIC_KEY_LENGTH]);
    private static int genesisVersion = 0;
    private static byte[] genesisReference = Bytes.ensureCapacity(new byte[]{19, 66, 8, 21, 0, 0, 0, 0}, Crypto.SIGNATURE_LENGTH, 0);
    private static byte[] icon = new byte[0];
    private static byte[] image = new byte[0];
    private String testnetInfo;
    private long genesisTimestamp;
    private String sideSettingString;

    private static final byte[] itemAppData = null;

    public GenesisBlock() {

        super(genesisVersion, genesisReference, CREATOR);

        this.genesisTimestamp = Settings.getInstance().getGenesisStamp();

        Account recipient;
        BigDecimal bdAmount0;
        BigDecimal bdAmount1;

        // ISSUE ITEMS
        this.initItems();

        if (BlockChain.DEMO_MODE) {
            ;
        } else if (BlockChain.TEST_MODE) {
            this.testnetInfo = "";

            //ADD TESTNET GENESIS TRANSACTIONS
            this.testnetInfo += "\ngenesisStamp: " + String.valueOf(genesisTimestamp);

            byte[] seed = Crypto.getInstance().digest(Longs.toByteArray(genesisTimestamp));

            this.testnetInfo += "\ngenesisSeed: " + Base58.encode(seed);

            this.testnetInfo += "\nStart the other nodes with command" + ":";
            this.testnetInfo += "\njava -Xms512m -Xmx1024m -jar erachain.jar -testnet=" + genesisTimestamp;

        } else if (BlockChain.CLONE_MODE) {

            sideSettingString = "";
            sideSettingString += Settings.genesisJSON.get(0).toString();
            sideSettingString += Settings.genesisJSON.get(1).toString();

            Account leftRecipiend = null;
            BigDecimal totalSended = BigDecimal.ZERO;
            List<List<Object>> holders = (List) Settings.genesisJSON.get(2);

            if (!Settings.ERA_COMPU_ALL_UP) {
                for (int i = 0; i < holders.size(); i++) {
                    List holder = holders.get(i);

                    sideSettingString += holder.get(0).toString();
                    sideSettingString += holder.get(1).toString();

                    // SEND FONDs
                    Account founder = new Account(holder.get(0).toString());
                    if (leftRecipiend == null) {
                        leftRecipiend = founder;
                    }

                    BigDecimal fondAamount = new BigDecimal(holder.get(1).toString()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
                    transactions.add(new GenesisTransferAssetTransaction(founder,
                            AssetCls.ERA_KEY, fondAamount));

                    totalSended = totalSended.add(fondAamount);

                    if (holder.size() < 3)
                        continue;

                    String COMPUstr = holder.get(2).toString();
                    if (COMPUstr.length() > 0 && !COMPUstr.equals("0")) {
                        BigDecimal compu = new BigDecimal(COMPUstr).setScale(BlockChain.FEE_SCALE);
                        transactions.add(new GenesisTransferAssetTransaction(founder,
                                AssetCls.FEE_KEY, compu));
                        sideSettingString += compu.toString();
                    }

                    if (holder.size() < 4)
                        continue;

                    // DEBTORS
                    // AS List - for use Arrays.asList() in init debtors in holders

                    List debtors = (List) holder.get(3);
                    BigDecimal totalCredit = BigDecimal.ZERO;
                    for (int j = 0; j < debtors.size(); j++) {
                        List debtor = (List) debtors.get(j);

                        BigDecimal creditAmount = new BigDecimal(debtor.get(0).toString()).setScale(BlockChain.AMOUNT_DEDAULT_SCALE);
                        if (totalCredit.add(creditAmount).compareTo(fondAamount) > 0) {
                            break;
                        }

                        sideSettingString += creditAmount.toString();
                        sideSettingString += debtor.get(1).toString();

                        transactions.add(new GenesisTransferAssetTransaction(new Account(debtor.get(1).toString()),
                                -AssetCls.ERA_KEY,
                                creditAmount, founder));

                        totalCredit = totalCredit.add(creditAmount);
                    }
                }

                if (totalSended.compareTo(new BigDecimal(BlockChain.GENESIS_ERA_TOTAL)) < 0) {
                    // ADJUST end
                    transactions.add(new GenesisTransferAssetTransaction(
                            leftRecipiend, AssetCls.ERA_KEY,
                            new BigDecimal(BlockChain.GENESIS_ERA_TOTAL).subtract(totalSended).setScale(BlockChain.AMOUNT_DEDAULT_SCALE)));
                }
            }

        } else {

            // ERROR - MAIN MODE DENIED
            new Account("--");

        }

        //GENERATE AND VALIDATE TRANSACTIONS
        this.transactionCount = transactions.size();

        makeTransactionsRAWandHASH();

        // SIGN simple as HASH
        if (BlockChain.GENESIS_SIGNATURE == null) {
            this.signature = generateHeadHash();
        } else {
            this.signature = BlockChain.GENESIS_SIGNATURE;
        }

    }

    // make assets
    public static AssetVenture makeAsset(long key) {
        switch ((int) key) {
            case (int) AssetCls.ERA_KEY:
                return new AssetVenture(itemAppData, CREATOR, AssetCls.ERA_NAME, icon, image, AssetCls.ERA_DESCR, 0, 8, 0L);
            case (int) AssetCls.FEE_KEY:
                return new AssetVenture(itemAppData, CREATOR, AssetCls.FEE_NAME, icon, image, AssetCls.FEE_DESCR, 0, 8, 0L);
        }
        return null;
    }

    // make templates
    public static Template makeTemplate(int key) {
        return null;
    }

    // make statuses
    public static Status makeStatus(int key) {
        return null;
    }

    private static byte[] generateAccountSeed(byte[] seed, int nonce) {
        byte[] nonceBytes = Ints.toByteArray(nonce);
        byte[] accountSeed = Bytes.concat(nonceBytes, seed, nonceBytes);
        return Crypto.getInstance().doubleDigest(accountSeed);
    }

    private void initItems() {

        transactions = new ArrayList<Transaction>();
        ///// ASSETS
        //CREATE ERA ASSET
        //asset0 = makeAsset(AssetCls.ERA_KEY);
        //transactions.add(new GenesisIssueAssetTransaction(asset0));
        //CREATE JOB ASSET
        //asset1 = makeAsset(AssetCls.FEE_KEY);
        //transactions.add(new GenesisIssueAssetTransaction(asset1));
        // ASSET OTHER
        for (int i = 1; i <= BlockChain.SKIP_BASE_ASSETS_AFTER; i++) {
            AssetVenture asset = makeAsset(i);
            // MAKE OLD STYLE ASSET with DEVISIBLE:
            // PROP1 = 0 (unMOVABLE, SCALE = 8, assetTYPE = 1 (divisible)
            asset = new AssetVenture((byte) 0, itemAppData, asset.getMaker(), asset.getName(),
                    asset.getIcon(), asset.getImage(), asset.getDescription(), AssetCls.AS_INSIDE_ASSETS, 8, 0L);
            transactions.add(new GenesisIssueAssetTransaction(asset));
        }

        ///// TEMPLATES
        for (int i = 1; i < TemplateCls.EMPTY_KEY; i++)
            transactions.add(new GenesisIssueTemplateRecord(makeTemplate(i)));

        ///// STATUSES
        for (int i = 1; i < StatusCls.RIGHTS_KEY; i++)
            transactions.add(new GenesisIssueStatusRecord(makeStatus(i)));

        AssetVenture asset;

        asset = new AssetVenture((byte) 0, itemAppData, BlockChain.FEE_ASSET_EMITTER, "EXO",
                null, null, "", AssetCls.AS_INSIDE_ASSETS, 8, 0L);
        transactions.add(new GenesisIssueAssetTransaction(asset));

        asset = new AssetVenture((byte) 0, itemAppData, BlockChain.FEE_ASSET_EMITTER, "AS",
                null, null, "", AssetCls.AS_INSIDE_ASSETS, 5, 100000000L);
        transactions.add(new GenesisIssueAssetTransaction(asset));

        asset = new AssetVenture((byte) 0, itemAppData, BlockChain.FEE_ASSET_EMITTER, "BAL",
                null, null, "", AssetCls.AS_INSIDE_ASSETS, 2, 0L);
        transactions.add(new GenesisIssueAssetTransaction(asset));

        asset = new AssetVenture((byte) 0, itemAppData, BlockChain.FEE_ASSET_EMITTER, "BTC",
                null, null, "", AssetCls.AS_INSIDE_ASSETS, 8, 0L);
        transactions.add(new GenesisIssueAssetTransaction(asset));

        asset = new AssetVenture((byte) 0, itemAppData, BlockChain.FEE_ASSET_EMITTER, "GOLD",
                null, null, "", AssetCls.AS_INSIDE_ASSETS, 8, 0L);
        transactions.add(new GenesisIssueAssetTransaction(asset));

        asset = new AssetVenture((byte) 0, itemAppData, BlockChain.FEE_ASSET_EMITTER, "UAH",
                null, null, "", AssetCls.AS_INSIDE_ASSETS, 5, 0L);
        transactions.add(new GenesisIssueAssetTransaction(asset));
        asset = new AssetVenture((byte) 0, itemAppData, BlockChain.FEE_ASSET_EMITTER, "KZT",
                null, null, "", AssetCls.AS_INSIDE_ASSETS, 5, 0L);
        transactions.add(new GenesisIssueAssetTransaction(asset));
        asset = new AssetVenture((byte) 0, itemAppData, BlockChain.FEE_ASSET_EMITTER, "KGS",
                null, null, "", AssetCls.AS_INSIDE_ASSETS, 5, 0L);
        transactions.add(new GenesisIssueAssetTransaction(asset));
        asset = new AssetVenture((byte) 0, itemAppData, BlockChain.FEE_ASSET_EMITTER, "BYN",
                null, null, "", AssetCls.AS_INSIDE_ASSETS, 5, 0L);
        transactions.add(new GenesisIssueAssetTransaction(asset));
        asset = new AssetVenture((byte) 0, itemAppData, BlockChain.FEE_ASSET_EMITTER, "CNY",
                null, null, "", AssetCls.AS_INSIDE_ASSETS, 5, 0L);
        transactions.add(new GenesisIssueAssetTransaction(asset));

        asset = new AssetVenture((byte) 0, itemAppData, BlockChain.FEE_ASSET_EMITTER, "RUB",
                null, null, "", AssetCls.AS_INSIDE_ASSETS, 5, 0L);
        transactions.add(new GenesisIssueAssetTransaction(asset));

        asset = new AssetVenture((byte) 0, itemAppData, BlockChain.FEE_ASSET_EMITTER, "EUR",
                null, null, "", AssetCls.AS_INSIDE_ASSETS, 5, 0L);
        transactions.add(new GenesisIssueAssetTransaction(asset));

        asset = new AssetVenture((byte) 0, itemAppData, BlockChain.FEE_ASSET_EMITTER, "USD",
                null, null, "", AssetCls.AS_INSIDE_ASSETS, 5, 0L);
        transactions.add(new GenesisIssueAssetTransaction(asset));


        if (BlockChain.TEST_MODE) {
            for (String name : BlockChain.NOVA_ASSETS.keySet()) {
                asset = new AssetVenture((byte) 0, itemAppData, creator, name,
                        null, null, "", AssetCls.AS_INSIDE_ASSETS, 8, 0L);
                transactions.add(new GenesisIssueAssetTransaction(asset));
            }
        }

    }

    //GETTERS

    private void addDebt(String address, int val, List<List<Object>> genesisDebtors) {

        Account recipient;

        //int i = 0;
        for (int i = 0; i < genesisDebtors.size(); i++) {

            List<Object> item = genesisDebtors.get(i);
            String address_deb = (String) item.get(0);

            if (address_deb.length() > 36) {
                recipient = new PublicKeyAccount(address_deb);
            } else {
                recipient = new Account(address_deb);
            }

            if (recipient.equals(address)) {
                val += (int) item.get(1);
                genesisDebtors.set(i, Arrays.asList(address_deb, val));
                return;
            }
            i++;
        }
        genesisDebtors.add(Arrays.asList(address, val));
    }

    @Override
    public long getTimestamp() {
        return this.genesisTimestamp;
    }

    public String getTestNetInfo() {
        return this.testnetInfo;
    }

    @Override
    public Block getParent(DCSet db) {
        //PARENT DOES NOT EXIST
        return null;
    }

    //VALIDATE

    public byte[] generateHeadHash() {

        byte[] data = new byte[0];

        //WRITE VERSION
        byte[] versionBytes = Longs.toByteArray(genesisVersion);
        versionBytes = Bytes.ensureCapacity(versionBytes, 4, 0);
        data = Bytes.concat(data, versionBytes);

        //WRITE REFERENCE
        byte[] flagsBytes = Bytes.ensureCapacity(genesisReference, Crypto.SIGNATURE_LENGTH, 0);
        data = Bytes.concat(data, flagsBytes);

        //WRITE TIMESTAMP
        byte[] genesisTimestampBytes = Longs.toByteArray(this.genesisTimestamp);
        genesisTimestampBytes = Bytes.ensureCapacity(genesisTimestampBytes, 8, 0);
        data = Bytes.concat(data, genesisTimestampBytes);

        if (BlockChain.CLONE_MODE) {
            //WRITE SIDE SETTINGS
            byte[] genesisjsonCloneBytes = this.sideSettingString.getBytes(StandardCharsets.UTF_8);
            data = Bytes.concat(data, genesisjsonCloneBytes);
        }

        //DIGEST [32]
        byte[] digest = Crypto.getInstance().digest(data);

        //DIGEST + transactionsHash = byte[64]
        digest = Bytes.concat(digest, transactionsHash);

        return digest;
    }

    @Override
    public boolean isSignatureValid() {

        //VALIDATE BLOCK SIGNATURE
        byte[] digest = generateHeadHash();
        if (!Arrays.equals(digest,
                // TODO - как защитить свой оригинальныЙ? Если задан наш оригинальный - то его и берем
                BlockChain.GENESIS_SIGNATURE_TRUE == null ?
                        this.signature : BlockChain.GENESIS_SIGNATURE_TRUE)) {
            return false;
        }

        return true;
    }

    @Override
    public int isValid(DCSet db, boolean andProcess) {
        //CHECK IF NO OTHER BLOCK IN DB
        if (db.getBlockMap().last() != null) {
            return INVALID_BLOCK_VERSION;
        }

        //VALIDATE TRANSACTIONS
        byte[] transactionsSignatures = new byte[0];
        for (Transaction transaction : this.getTransactions()) {
            transaction.setDC(db);
            if (transaction.isValid(Transaction.FOR_NETWORK, 0L) != Transaction.VALIDATE_OK) {
                return INVALID_BLOCK_VERSION;
            }
            transactionsSignatures = Bytes.concat(transactionsSignatures, transaction.getSignature());

        }
        transactionsSignatures = Crypto.getInstance().digest(transactionsSignatures);
        if (!Arrays.equals(this.transactionsHash, transactionsSignatures)) {
            LOGGER.error("*** GenesisBlock.digest(transactionsSignatures) invalid");
            return INVALID_BLOCK_VERSION;
        }

        return INVALID_NONE;
    }

    @Override
    public void assetsFeeProcess(DCSet dcSet, boolean asOrphan) {
    }

    public void process(DCSet dcSet, boolean notLog) throws Exception {

        this.target = BlockChain.BASE_TARGET;

        this.blockHead = new BlockHead(this);

        super.process(dcSet, notLog);

    }

    public void orphan(DCSet dcSet) throws Exception {

        super.orphan(dcSet);

    }

}
