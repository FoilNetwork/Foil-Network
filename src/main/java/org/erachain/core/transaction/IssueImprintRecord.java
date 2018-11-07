package org.erachain.core.transaction;

import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.imprints.Imprint;
import org.erachain.core.item.imprints.ImprintCls;

import java.math.BigDecimal;
import java.util.Arrays;

// reference - as item.name
// TODO - reference NOT NEED - because it is unique record! - make it as new version protocol
public class IssueImprintRecord extends Issue_ItemRecord {
    //protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK;

    protected static final int BASE_LENGTH_AS_MYPACK = Transaction.BASE_LENGTH_AS_MYPACK - REFERENCE_LENGTH;
    protected static final int BASE_LENGTH_AS_PACK = Transaction.BASE_LENGTH_AS_PACK - REFERENCE_LENGTH;
    protected static final int BASE_LENGTH = Transaction.BASE_LENGTH - REFERENCE_LENGTH;
    protected static final int BASE_LENGTH_AS_DBRECORD = Transaction.BASE_LENGTH_AS_DBRECORD - REFERENCE_LENGTH;

    private static final byte TYPE_ID = (byte) ISSUE_IMPRINT_TRANSACTION;
    private static final String NAME_ID = "Issue Imprint";


    public IssueImprintRecord(byte[] typeBytes, PublicKeyAccount creator, ImprintCls imprint, byte feePow, long timestamp) {
        super(typeBytes, NAME_ID, creator, imprint, feePow, timestamp, null);
    }

    public IssueImprintRecord(byte[] typeBytes, PublicKeyAccount creator, ImprintCls imprint, byte feePow, long timestamp, byte[] signature) {
        super(typeBytes, NAME_ID, creator, imprint, feePow, timestamp, null, signature);
    }
    public IssueImprintRecord(byte[] typeBytes, PublicKeyAccount creator, ImprintCls imprint, byte feePow,
                              long timestamp, byte[] signature, long feeLong) {
        super(typeBytes, NAME_ID, creator, imprint, feePow, timestamp, null, signature);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.AMOUNT_DEDAULT_SCALE);
    }

    // asPack
    public IssueImprintRecord(byte[] typeBytes, PublicKeyAccount creator, ImprintCls imprint, byte[] signature) {
        super(typeBytes, NAME_ID, creator, imprint, (byte) 0, 0l, null, signature);
    }

    public IssueImprintRecord(PublicKeyAccount creator, ImprintCls imprint, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, imprint, (byte) 0, 0l, signature);
    }

    public IssueImprintRecord(PublicKeyAccount creator, ImprintCls imprint, byte feePow, long timestamp, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, imprint, feePow, timestamp, signature);
    }

    public IssueImprintRecord(PublicKeyAccount creator, ImprintCls imprint, byte feePow, long timestamp) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, imprint, feePow, timestamp, null);
    }

    public IssueImprintRecord(PublicKeyAccount creator, ImprintCls imprint) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, imprint, (byte) 0, 0l, null);
    }

    //GETTERS/SETTERS
    //public static String getName() { return "Issue Imprint"; }

    // RETURN START KEY in tot GEMESIS
    public long getStartKey(int height) {

        if (height < BlockChain.VERS_4_11) {
            return START_KEY;
        }

        return 0l;

    }

    public static Transaction Parse(byte[] data, int asDeal) throws Exception {

        //boolean asPack = releaserReference != null;

        //CHECK IF WE MATCH BLOCK LENGTH
        int test_len;
        if (asDeal == Transaction.FOR_MYPACK) {
            test_len = BASE_LENGTH_AS_MYPACK;
        } else if (asDeal == Transaction.FOR_PACK) {
            test_len = BASE_LENGTH_AS_PACK;
        } else if (asDeal == Transaction.FOR_DB_RECORD) {
            test_len = BASE_LENGTH_AS_DBRECORD;
        } else {
            test_len = BASE_LENGTH;
        }

        if (data.length < test_len) {
            throw new Exception("Data does not match block length " + data.length);
        }

        // READ TYPE
        byte[] typeBytes = Arrays.copyOfRange(data, 0, TYPE_LENGTH);
        int position = TYPE_LENGTH;

        long timestamp = 0;
        if (asDeal > Transaction.FOR_MYPACK) {
            //READ TIMESTAMP
            byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
            timestamp = Longs.fromByteArray(timestampBytes);
            position += TIMESTAMP_LENGTH;
        }

        //READ REFERENCE
        //byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
        //Long reference = Longs.fromByteArray(referenceBytes);
        //position += REFERENCE_LENGTH;

        //READ CREATOR
        byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
        PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
        position += CREATOR_LENGTH;

        byte feePow = 0;
        if (asDeal > Transaction.FOR_PACK) {
            //READ FEE POWER
            byte[] feePowBytes = Arrays.copyOfRange(data, position, position + 1);
            feePow = feePowBytes[0];
            position += 1;
        }

        //READ SIGNATURE
        byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
        position += SIGNATURE_LENGTH;

        long feeLong = 0;
        if (asDeal == FOR_DB_RECORD) {
            // READ FEE
            byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
            feeLong = Longs.fromByteArray(feeBytes);
            position += FEE_LENGTH;
        }

        //READ IMPRINT
        // imprint parse without reference - if is = signature
        ImprintCls imprint = Imprint.parse(Arrays.copyOfRange(data, position, data.length), false);
        position += imprint.getDataLength(false);

        if (asDeal > Transaction.FOR_MYPACK) {
            return new IssueImprintRecord(typeBytes, creator, imprint, feePow, timestamp, signatureBytes, feeLong);
        } else {
            return new IssueImprintRecord(typeBytes, creator, imprint, signatureBytes);
        }
    }

    @Override
    public boolean hasPublicText() {
        return false;
    }


    //PARSE CONVERT

    @Override
    public boolean isReferenced() {
        // reference not used - because all imprint is unique
        return false;
    }

    //VALIDATE
    //
    @Override
    public int isValid(int asDeal, long flags) {

        //CHECK NAME LENGTH
        ItemCls item = this.getItem();
        int nameLength = item.getName().getBytes().length;
        if (nameLength > 40 || nameLength < item.getMinNameLen()) {
            return INVALID_NAME_LENGTH;
        }

        int result = super.isValid(asDeal, flags);
        if (result != Transaction.VALIDATE_OK) return result;

        // CHECK reference in DB
        if (item.getDBIssueMap(this.dcSet).contains(item.getReference()))
            return Transaction.ITEM_DUPLICATE_KEY;

        return Transaction.VALIDATE_OK;

    }

    @Override
    public int getDataLength(int forDeal, boolean withSignature) {
        // not include item reference

        int base_len;
        if (forDeal == FOR_MYPACK)
            base_len = BASE_LENGTH_AS_MYPACK;
        else if (forDeal == FOR_PACK)
            base_len = BASE_LENGTH_AS_PACK;
        else if (forDeal == FOR_DB_RECORD)
            base_len = BASE_LENGTH_AS_DBRECORD;
        else
            base_len = BASE_LENGTH;

        if (!withSignature)
            base_len -= SIGNATURE_LENGTH;

        return base_len + this.getItem().getDataLength(false);

    }

    //PROCESS/ORPHAN

    @Override
    public long calcBaseFee() {
        if (this.height < BlockChain.VERS_4_11 && BlockChain.VERS_4_11_USE_OLD_FEE)
            return calcCommonFee() + BlockChain.FEE_PER_BYTE_4_10 * 128 * 4;

        return calcCommonFee();
    }

}
