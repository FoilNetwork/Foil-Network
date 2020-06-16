package org.erachain.core.transaction;

import com.google.common.primitives.Longs;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.unions.UnionCls;
import org.erachain.core.item.unions.UnionFactory;

import java.math.BigDecimal;
import java.util.Arrays;

//import java.util.Map;
// import org.slf4j.LoggerFactory;

public class IssueUnionRecord extends IssueItemRecord {
    private static final byte TYPE_ID = (byte) ISSUE_UNION_TRANSACTION;
    private static final String NAME_ID = "Issue Union";

    public IssueUnionRecord(byte[] typeBytes, PublicKeyAccount creator, UnionCls union, byte feePow, long timestamp, Long reference) {
        super(typeBytes, NAME_ID, creator, union, feePow, timestamp, reference);
    }

    public IssueUnionRecord(byte[] typeBytes, PublicKeyAccount creator, UnionCls union, byte feePow, long timestamp, Long reference, byte[] signature) {
        super(typeBytes, NAME_ID, creator, union, feePow, timestamp, reference, signature);
    }

    public IssueUnionRecord(byte[] typeBytes, PublicKeyAccount creator, UnionCls union, byte feePow, long timestamp,
                            Long reference, byte[] signature, long seqNo, long feeLong) {
        super(typeBytes, NAME_ID, creator, union, feePow, timestamp, reference, signature);
        if (seqNo > 0)
            this.setHeightSeq(seqNo);
        this.fee = BigDecimal.valueOf(feeLong, BlockChain.FEE_SCALE);
    }

    public IssueUnionRecord(byte[] typeBytes, PublicKeyAccount creator, UnionCls union, byte[] signature) {
        super(typeBytes, NAME_ID, creator, union, (byte) 0, 0l, null, signature);
    }

    public IssueUnionRecord(PublicKeyAccount creator, UnionCls union, byte feePow, long timestamp, Long reference, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, union, feePow, timestamp, reference, signature);
    }

    public IssueUnionRecord(PublicKeyAccount creator, UnionCls union, byte[] signature) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, union, (byte) 0, 0l, null, signature);
    }

    public IssueUnionRecord(PublicKeyAccount creator, UnionCls union, byte feePow, long timestamp, Long reference) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, union, feePow, timestamp, reference);
    }

    public IssueUnionRecord(PublicKeyAccount creator, UnionCls union) {
        this(new byte[]{TYPE_ID, 0, 0, 0}, creator, union, (byte) 0, 0l, null);
    }

    //GETTERS/SETTERS
    //public static String getName() { return "Issue Union"; }

    public static Transaction Parse(byte[] data, int asDeal) throws Exception {

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
        byte[] referenceBytes = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
        Long reference = Longs.fromByteArray(referenceBytes);
        position += REFERENCE_LENGTH;

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
        long seqNo = 0;
        if (asDeal == FOR_DB_RECORD) {
            //READ SEQ_NO
            byte[] seqNoBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
            seqNo = Longs.fromByteArray(seqNoBytes);
            position += TIMESTAMP_LENGTH;

            // READ FEE
            byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
            feeLong = Longs.fromByteArray(feeBytes);
            position += FEE_LENGTH;
        }

        //READ UNION
        // union parse without reference - if is = signature
        UnionCls union = UnionFactory.getInstance().parse(Arrays.copyOfRange(data, position, data.length), false);
        position += union.getDataLength(false);

        if (asDeal == FOR_DB_RECORD) {
            //READ KEY
            byte[] timestampBytes = Arrays.copyOfRange(data, position, position + KEY_LENGTH);
            long key = Longs.fromByteArray(timestampBytes);
            position += KEY_LENGTH;

            union.setKey(key);

        }

        if (asDeal > Transaction.FOR_MYPACK) {
            return new IssueUnionRecord(typeBytes, creator, union, feePow, timestamp, reference, signatureBytes, seqNo, feeLong);
        } else {
            return new IssueUnionRecord(typeBytes, creator, union, signatureBytes);
        }
    }

    //PARSE CONVERT

    //@Override
    public int isValid(int asDeal, long flags) {

        if (height < BlockChain.ALL_VALID_BEFORE) {
            return VALIDATE_OK;
        }

        int result = super.isValid(asDeal, flags);
        if (result != Transaction.VALIDATE_OK) return result;

		/*
		BigDecimal balERA = this.creator.getBalanceUSE(RIGHTS_KEY, db);
		if ( balERA.compareTo(BlockChain.MAJOR_ERA_BALANCE_BD)<0 )
		{
			return Transaction.NOT_ENOUGH_RIGHTS;
		}
		*/

        return Transaction.VALIDATE_OK;
    }

    //PROCESS/ORPHAN

}
