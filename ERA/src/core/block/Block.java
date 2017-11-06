package core.block;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;

import ntp.NTP;
import settings.Settings;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;

import utils.Converter;
import utils.TransactionTimestampComparator;
import at.AT_API_Platform_Impl;
import at.AT_Block;
import at.AT_Constants;
import at.AT_Controller;
import at.AT_Exception;
import at.AT_Transaction;
import controller.Controller;
import core.BlockChain;
import core.account.Account;
import core.account.PrivateKeyAccount;
import core.account.PublicKeyAccount;
import core.crypto.Base58;
import core.crypto.Crypto;
import core.transaction.CreateOrderTransaction;
import core.transaction.DeployATTransaction;
import core.transaction.R_SertifyPubKeys;
import core.transaction.Transaction;
import core.transaction.TransactionAmount;
import core.transaction.TransactionFactory;
import database.DBSet;
import datachain.DCSet;
import datachain.TransactionFinalMap;
import datachain.TransactionMap;
import datachain.TransactionRef_BlockRef_Map;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;


public class Block {

	private boolean run = true;
	
	public static final int VERSION_LENGTH = 4;
	//public static final int TIMESTAMP_LENGTH = 8;
	public static final int GENERATING_BALANCE_LENGTH = 4;
	public static final int CREATOR_LENGTH = Crypto.HASH_LENGTH;
	public static final int SIGNATURE_LENGTH = Crypto.SIGNATURE_LENGTH;
	public static final int REFERENCE_LENGTH = SIGNATURE_LENGTH;
	public static final int TRANSACTIONS_HASH_LENGTH = Crypto.HASH_LENGTH;
	private static final int TRANSACTIONS_COUNT_LENGTH = 4;
	private static final int TRANSACTION_SIZE_LENGTH = 4;
	public static final int AT_BYTES_LENGTH = 4;
	private static final int BASE_LENGTH = VERSION_LENGTH + REFERENCE_LENGTH + CREATOR_LENGTH
			//+ GENERATING_BALANCE_LENGTH
			+ TRANSACTIONS_HASH_LENGTH + SIGNATURE_LENGTH + TRANSACTIONS_COUNT_LENGTH;
	//private static final int AT_FEES_LENGTH = 8;
	//private static final int AT_LENGTH = AT_FEES_LENGTH + AT_BYTES_LENGTH;
	private static final int AT_LENGTH = 0 + AT_BYTES_LENGTH;
	public static final int MAX_TRANSACTION_BYTES = BlockChain.MAX_BLOCK_BYTES - BASE_LENGTH;

	protected int version;
	protected byte[] reference;
	int heightBlock;
	Block parentBlock;
	//protected long timestamp;
	protected int generatingBalance; // only for DB MAP
	protected PublicKeyAccount creator;
	protected byte[] signature;

	private List<Transaction> transactions;	
	private int transactionCount;
	private byte[] rawTransactions;

	protected byte[] transactionsHash;

	protected byte[] atBytes;
	//protected Long atFees;

	static Logger LOGGER = Logger.getLogger(Block.class.getName());

	public Block(int version, byte[] reference, PublicKeyAccount creator, byte[] transactionsHash, byte[] atBytes)
	{
		this.version = version;
		this.reference = reference;
		this.creator = creator;

		this.transactionsHash = transactionsHash;

		this.transactionCount = 0;
		this.atBytes = atBytes;
		
		//this.setGeneratingBalance(dcSet);
		//BlockChain.getTarget();

	}

	// VERSION 2 AND 3 BLOCKS, WITH AT AND MESSAGE
	public Block(int version, byte[] reference, PublicKeyAccount creator, byte[] transactionsHash, byte[] atBytes, byte[] signature)
	{
		this(version, reference, creator, transactionsHash, atBytes);
		this.signature = signature;
	}

	
	//GETTERS/SETTERS

	public int getVersion()
	{
		return version;
	}

	public byte[] getSignature()
	{
		return this.signature;
	}

	public int getHeight(DCSet db)
	{

		if (this instanceof GenesisBlock
				|| Arrays.equals(this.signature,
						Controller.getInstance().getBlockChain().getGenesisBlock().getSignature()))
			return 1;
		
		if (heightBlock < 1)
			heightBlock = db.getBlockSignsMap().get(this.signature).a;

		return heightBlock;

	}
	
	/*
	protected long targetValue;
	public long getTargetValue() {
		return targetValue;
	}
	public void setTargetValue(long target) {
		if (targetValue == 0)
			targetValue = target;
	}
	*/

	public Block getParent(DCSet db)
	{
		if (parentBlock == null) {
			this.parentBlock = db.getBlockMap().get(this.reference);
		}
		return parentBlock;
	}

	public Block getChild(DCSet db)
	{
		return db.getBlockMap().getChildBlock(this);
	}

	public int getHeightByParent(DCSet db)
	{
		
		if (this instanceof GenesisBlock
				|| Arrays.equals(this.signature,
						Controller.getInstance().getBlockChain().getGenesisBlock().getSignature()))
			return 1;

		Block parent = this.getParent(db);
		if (parent == null)
			return -1;

		int height = parent.getHeight(db) + 1;
		return height;

	}

	public long getTimestamp(DCSet db)
	{

		int height = getHeightByParent(db);
		
		BlockChain blockChain = Controller.getInstance().getBlockChain();

		return blockChain.getTimestamp(height);
	}

	// balance on creator account when making this block
	public int getGeneratingBalance(DCSet dcSet)
	{
		return this.generatingBalance;
	}
	private void setGeneratingBalance(int generatingBalance)
	{
		this.generatingBalance = generatingBalance;
	}
		
	// IT IS RIGHTS ONLY WHEN BLOCK is MAKING
	// MABE used only in isValid and in Block Generator
	public static int calcGeneratingBalance(DCSet dcSet, Account creator, int height)
	{
		
		long incomed_amount = 0l;
		//long amount;
				
		int previousForgingHeight = getPreviousForgingHeightForCalcWin(dcSet, creator, height);
		if (previousForgingHeight == -1)
			return 0;
				
		previousForgingHeight++;
		if (previousForgingHeight < height) {
			
			// for recipient only
			List<Transaction> txs = dcSet.getTransactionFinalMap().findTransactions(null, null, creator.getAddress(),
					previousForgingHeight, height,
					0, 0, false, 0, 0);
			
			//amount = 0l;
			for(Transaction transaction: txs)
			{
				if ( transaction.getAbsKey() != Transaction.RIGHTS_KEY )
					continue;
				
				if (transaction instanceof TransactionAmount) {
					TransactionAmount recordAmount = (TransactionAmount) transaction;
					if (recordAmount.isBackward())
						continue;
					
					int amo_sign = recordAmount.getAmount().signum();
					if (amo_sign > 0) {
						// SEND or DEBT
						incomed_amount += recordAmount.getAmount().longValue();						
					} else {
						continue;
					}
				//} else if (transaction instanceof CreateOrderTransaction) {
				//	amount = transaction.getAmount().longValue();											
				} else {
					continue;
				}
				//incomed_amount += amount;
			}

			// for creator
			txs = dcSet.getTransactionFinalMap().findTransactions(null, creator.getAddress(), null,
					previousForgingHeight, height,
					0, 0, false, 0, 0);
			
			//amount = 0l;
			for(Transaction transaction: txs)
			{				
				if (false && transaction instanceof R_SertifyPubKeys) {
				//	amount = BlockChain.GIFTED_ERA_AMOUNT.intValue();
				//	incomed_amount += amount;

				} else if (transaction instanceof TransactionAmount) {

					if ( transaction.getAbsKey() != Transaction.RIGHTS_KEY )
						continue;
						
					TransactionAmount recordAmount = (TransactionAmount) transaction;
					// TODO: delete  on new CHAIN
					if (height > 45281 && recordAmount.isBackward()
							&& Account.actionType(recordAmount.getKey(), recordAmount.getAmount()) == 2) {
						// RE DEBT to me
						long amount = transaction.getAmount().abs().longValue();
						if (amount < 200) {
							continue;
						} else if (amount < 1000) {
							amount >>=2;
						} else {
							amount >>=1;			
						}
						incomed_amount += amount;						
					} else {
						continue;
					}
				} else {
					continue;
				}
				//incomed_amount += amount;
			}
		}
		
		// OWN + RENT balance - in USE
		long used_amount = creator.getBalanceUSE(Transaction.RIGHTS_KEY, dcSet).longValue();
		if (used_amount < BlockChain.MIN_GENERATING_BALANCE)
			return 0;
		
		if (used_amount - incomed_amount < BlockChain.MIN_GENERATING_BALANCE ) {
			return BlockChain.MIN_GENERATING_BALANCE;
		} else {
			return (int)(used_amount - incomed_amount);			
		}
	}

	// CALCULATE and SET
	public void setCalcGeneratingBalance(DCSet dcSet)
	{
		 this.generatingBalance = calcGeneratingBalance(dcSet, this.creator, this.getHeightByParent(dcSet));
	}

	public byte[] getReference()
	{
		return this.reference;
	}

	public PublicKeyAccount getCreator()
	{
		return this.creator;
	}

	public BigDecimal getTotalFee(DCSet db)
	{
		BigDecimal fee = this.getTotalFeeForProcess(db);

		// TODO calculate AT FEE
		// fee = fee.add(BigDecimal.valueOf(this.atFees, 8));
		int inDay30 = BlockChain.BLOCKS_PER_DAY*30;

		BigDecimal minFee = BlockChain.MIN_FEE_IN_BLOCK;
		int height = this.getHeightByParent(db);
		if (height < inDay30<<1)
			;
		else if (height < inDay30<<2)
			minFee = minFee.divide(new BigDecimal(2)).setScale(8);
		else if (height < inDay30<<3)
			minFee = minFee.divide(new BigDecimal(4)).setScale(8);
		else if (height < inDay30<<4)
			minFee = minFee.divide(new BigDecimal(8)).setScale(8);
		else if (height < inDay30<<5)
			minFee = minFee.divide(new BigDecimal(16)).setScale(8);
		else
			minFee = minFee.divide(new BigDecimal(64)).setScale(8);
			
		if ( fee.compareTo(minFee) < 0) 
			fee = minFee;

		return fee;
	}
	
	public BigDecimal getTotalFee() {
		return getTotalFee(DCSet.getInstance());
	}
	
	public BigDecimal getTotalFeeForProcess(DCSet db)
	{
		//BigDecimal fee = BigDecimal.ZERO;
		int fee = 0;

		for(Transaction transaction: this.getTransactions())
		{
			//fee = fee.add(transaction.getFee());
			fee += transaction.getForgedFee(db);
		}

		// TODO calculate AT FEE
		// fee = fee.add(BigDecimal.valueOf(this.atFees, 8));

		return BigDecimal.valueOf(fee, 8);
		
	}

	/*
	public BigDecimal getATfee()
	{
		return BigDecimal.valueOf(this.atFees, 8);
	}
	*/

	public void setTransactionData(int transactionCount, byte[] rawTransactions)
	{
		this.transactionCount = transactionCount;
		this.rawTransactions = rawTransactions;
	}

	public int getTransactionCount() 
	{	
		return this.transactionCount;		
	}

	public synchronized List<Transaction> getTransactions() 
	{
		if(this.transactions == null)
		{
			//LOAD TRANSACTIONS
			this.transactions = new ArrayList<Transaction>();

			try
			{
				int position = 0;
				for(int i=0; i<transactionCount; i++)
				{
					//GET TRANSACTION SIZE
					byte[] transactionLengthBytes = Arrays.copyOfRange(this.rawTransactions, position, position + TRANSACTION_SIZE_LENGTH);
					int transactionLength = Ints.fromByteArray(transactionLengthBytes);
					position += TRANSACTION_SIZE_LENGTH;
					
					//PARSE TRANSACTION
					byte[] transactionBytes = Arrays.copyOfRange(this.rawTransactions, position, position + transactionLength);
					Transaction transaction = TransactionFactory.getInstance().parse(transactionBytes, null);
					transaction.setBlock(this);

					//ADD TO TRANSACTIONS
					this.transactions.add(transaction);

					//ADD TO POSITION
					position += transactionLength;
				}
			}
			catch(Exception e)
			{
				//FAILED TO LOAD TRANSACTIONS
			}
		}

		return this.transactions;
	}

	/*
	public void addTransaction(Transaction transaction)
	{
		this.getTransactions().add(transaction);

		this.transactionCount++;
	}
	*/
	public void setTransactions(List<Transaction> transactions)
	{
		if (transactions == null)
			transactions = new ArrayList<Transaction>();
		
		this.transactions = transactions;
		this.transactionCount = transactions.size();
		this.atBytes = null;
		this.transactionsHash = makeTransactionsHash(this.creator.getPublicKey(), transactions, null);
	}
	
	public void setATBytes(byte[] atBytes)
	{
		this.atBytes = atBytes;
	}

	public int getTransactionSeq(byte[] signature)
	{
		int seq = 1;
		for(Transaction transaction: this.getTransactions())
		{
			if(Arrays.equals(transaction.getSignature(), signature))
			{
				return seq;
			}
			seq ++;
		}

		return -1;
	}
	/*
	public int getTransactionIndex(byte[] signature)
	{

		int i = 0;
		
		for(Transaction transaction: this.getTransactions())
		{
			if(Arrays.equals(transaction.getSignature(), signature))
			{
				return i;
			}
			i++;
		}

		return -1;
	}
	*/

	public Transaction getTransaction(byte[] signature)
	{

		for(Transaction transaction: this.getTransactions())
		{
			if(Arrays.equals(transaction.getSignature(), signature))
			{
				return transaction;
			}
		}

		return null;
	}

	public Transaction getTransaction(int index)
	{

		if (index < this.transactionCount)
			return getTransactions().get(index);
		else
			return null;
	}
	
	public byte[] getBlockATs()
	{
		return this.atBytes;
	}

	/*
	public void setTransactionsHash(byte[] transactionsHash) 
	{
		this.transactionsHash = transactionsHash;
	}
	*/
	
	public static byte[] makeTransactionsHash(byte[] creator, List<Transaction> transactions, byte[] atBytes) 
	{

		byte[] data = new byte[0];

		if (transactions == null || transactions.size() == 0) {
			data = Bytes.concat(data, creator);
			
		} else {
	
			//MAKE TRANSACTIONS HASH
			for(Transaction transaction: transactions)
			{
				data = Bytes.concat(data, transaction.getSignature());
			}
		}
		
		if (atBytes != null)
			data = Bytes.concat(data, atBytes);

		
		return Crypto.getInstance().digest(data);

	}
	public void makeTransactionsHash() 
	{
		this.transactionsHash = makeTransactionsHash(this.creator.getPublicKey(), this.getTransactions(), this.atBytes);
	}

	//PARSE/CONVERT

	public static Block parse(byte[] data, boolean forDB) throws Exception
	{
		if(data.length == 0)
		{
			return null;
		}
		
		//CHECK IF WE HAVE MINIMUM BLOCK LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data is less then minimum block length");
		}

		int position = 0;

		//READ VERSION
		byte[] versionBytes = Arrays.copyOfRange(data, position, position + VERSION_LENGTH);
		int version = Ints.fromByteArray(versionBytes);
		position += VERSION_LENGTH;

		/*
		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);
		position += TIMESTAMP_LENGTH;
		*/		

		//READ REFERENCE
		byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		position += REFERENCE_LENGTH;

		//READ GENERATOR
		byte[] generatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount generator = new PublicKeyAccount(generatorBytes);
		position += CREATOR_LENGTH;

		int generatingBalance = 0;
		if (forDB) {
			//READ GENERATING BALANCE
			byte[] generatingBalanceBytes = Arrays.copyOfRange(data, position, position + GENERATING_BALANCE_LENGTH);
			generatingBalance = Ints.fromByteArray(generatingBalanceBytes);
			if (generatingBalance < 0) {
				LOGGER.error("block.generatingBalance < 0:" + generatingBalance);
			}
			position += GENERATING_BALANCE_LENGTH;
		}

		//READ TRANSACTION SIGNATURE
		byte[] transactionsHash =  Arrays.copyOfRange(data, position, position + TRANSACTIONS_HASH_LENGTH);
		position += TRANSACTIONS_HASH_LENGTH;

		//READ GENERATOR SIGNATURE
		byte[] signature =  Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
		position += SIGNATURE_LENGTH;
 
		//CREATE BLOCK
		Block block;
		if(version > 1)
		{
			//ADD ATs BYTES
			byte[] atBytesCountBytes = Arrays.copyOfRange(data, position, position + AT_BYTES_LENGTH);
			int atBytesCount = Ints.fromByteArray(atBytesCountBytes);
			position += AT_BYTES_LENGTH;
	
			byte[] atBytes = Arrays.copyOfRange( data , position, position + atBytesCount);
			position += atBytesCount;
	
			//byte[] atFees = Arrays.copyOfRange( data , position , position + 8 );
			//position += 8;
	
			//long atFeesL = Longs.fromByteArray(atFees);

			block = new Block(version, reference, generator, transactionsHash, atBytes, signature); //, atFeesL);
		}
		else
		{
			// GENESIS BLOCK version = 0
			block = new Block(version, reference, generator, transactionsHash, new byte[0], signature);
		}
		
		if (forDB)
			block.setGeneratingBalance(generatingBalance);
		
		//READ TRANSACTIONS COUNT
		byte[] transactionCountBytes = Arrays.copyOfRange(data, position, position + TRANSACTIONS_COUNT_LENGTH);
		int transactionCount = Ints.fromByteArray(transactionCountBytes);
		position += TRANSACTIONS_COUNT_LENGTH;

		//SET TRANSACTIONDATA
		byte[] rawTransactions = Arrays.copyOfRange(data, position, data.length);
		block.setTransactionData(transactionCount, rawTransactions);

		//SET TRANSACTIONS SIGNATURE
		// transaction only in raw here - block.makeTransactionsHash();

		return block;
	}

	@SuppressWarnings("unchecked")
	public JSONObject toJson()
	{
		JSONObject block = new JSONObject();

		block.put("version", this.version);
		block.put("reference", Base58.encode(this.reference));
		block.put("timestamp", this.getTimestamp(DCSet.getInstance()));
		block.put("generatingBalance", this.generatingBalance);
		//block.put("winValue", this.calcWinValue(DBSet.getInstance()));
		block.put("winValueTargeted", this.calcWinValueTargeted(DCSet.getInstance()));
		block.put("creator", this.creator.getAddress());
		block.put("fee", this.getTotalFee().toPlainString());
		block.put("transactionsHash", Base58.encode(this.transactionsHash));
		block.put("signature", Base58.encode(this.signature));
		block.put("signature",  Base58.encode(this.getSignature()));
		block.put("height", this.getHeight(DCSet.getInstance()));

		//CREATE TRANSACTIONS
		JSONArray transactionsArray = new JSONArray();

		for(Transaction transaction: this.getTransactions())
		{
			transactionsArray.add(transaction.toJson());
		}

		//ADD TRANSACTIONS TO BLOCK
		block.put("transactions", transactionsArray);

		//ADD AT BYTES
		if ( atBytes != null )
		{
			block.put("blockATs", Converter.toHex( atBytes ));
			//block.put("atFees", this.atFees);
		}

		//RETURN
		return block;
	}

	public byte[] toBytes(boolean withSign, boolean forDB)
	{
		byte[] data = new byte[0];

		//WRITE VERSION
		byte[] versionBytes = Ints.toByteArray(this.version);
		versionBytes = Bytes.ensureCapacity(versionBytes, VERSION_LENGTH, 0);
		data = Bytes.concat(data, versionBytes);

		//WRITE REFERENCE
		byte[] referenceBytes = Bytes.ensureCapacity(this.reference, REFERENCE_LENGTH, 0);
		data = Bytes.concat(data, referenceBytes);

		//WRITE GENERATOR
		byte[] generatorBytes = Bytes.ensureCapacity(this.creator.getPublicKey(), CREATOR_LENGTH, 0);
		data = Bytes.concat(data, generatorBytes);

		if (forDB) {
			//WRITE GENERATING BALANCE
			byte[] generatingBalanceBytes = Ints.toByteArray(this.generatingBalance);
			generatingBalanceBytes = Bytes.ensureCapacity(generatingBalanceBytes, GENERATING_BALANCE_LENGTH, 0);
			data = Bytes.concat(data, generatingBalanceBytes);
		}

		//WRITE TRANSACTIONS HASH
		data = Bytes.concat(data, this.transactionsHash);

		if (!withSign) {
			// make HEAD data for signature
			return data;
		}

		//WRITE GENERATOR SIGNATURE
		data = Bytes.concat(data, this.signature);

		//ADD ATs BYTES
		if(this.version > 1)
		{
			if (atBytes!=null)
			{
				byte[] atBytesCount = Ints.toByteArray( atBytes.length );
				data = Bytes.concat(data, atBytesCount);

				data = Bytes.concat(data, atBytes);

				//byte[] atByteFees = Longs.toByteArray(atFees);
				//data = Bytes.concat(data,atByteFees);
			}
			else
			{
				byte[] atBytesCount = Ints.toByteArray( 0 );
				data = Bytes.concat(data, atBytesCount);
				
				//byte[] atByteFees = Longs.toByteArray(0L);
				//data = Bytes.concat(data,atByteFees);
			}
		}

		//WRITE TRANSACTION COUNT
		byte[] transactionCountBytes = Ints.toByteArray(this.getTransactionCount());
		transactionCountBytes = Bytes.ensureCapacity(transactionCountBytes, TRANSACTIONS_COUNT_LENGTH, 0);
		data = Bytes.concat(data, transactionCountBytes);

		for(Transaction transaction: this.getTransactions())
		{
			//WRITE TRANSACTION LENGTH
			int transactionLength = transaction.getDataLength(false);
			byte[] transactionLengthBytes = Ints.toByteArray(transactionLength);
			transactionLengthBytes = Bytes.ensureCapacity(transactionLengthBytes, TRANSACTION_SIZE_LENGTH, 0);
			data = Bytes.concat(data, transactionLengthBytes);

			//WRITE TRANSACTION
			data = Bytes.concat(data, transaction.toBytes(true, null));
		}

		return data;
	}

	public byte[] toBytesForSign()
	{
		byte[] data = new byte[0];

		//WRITE VERSION
		byte[] versionBytes = Ints.toByteArray(this.version);
		versionBytes = Bytes.ensureCapacity(versionBytes, VERSION_LENGTH, 0);
		data = Bytes.concat(data, versionBytes);

		//WRITE REFERENCE
		byte[] referenceBytes = Bytes.ensureCapacity(this.reference, REFERENCE_LENGTH, 0);
		data = Bytes.concat(data, referenceBytes);

		data = Bytes.concat(data, this.transactionsHash);

		return data;
	}
	
	public void sign(PrivateKeyAccount account) 
	{	
		byte[] data = toBytesForSign();
		this.signature = Crypto.getInstance().sign(account, data);
	}

	public int getDataLength(boolean forDB)
	{

		int length = BASE_LENGTH + (forDB?GENERATING_BALANCE_LENGTH:0);

		if(this.version > 1)
		{
			length += AT_LENGTH;
			if (this.atBytes!=null)
			{
				length+=atBytes.length;
			}
		}

		for(Transaction transaction: this.getTransactions())
		{
			length += TRANSACTION_SIZE_LENGTH + transaction.getDataLength(false);
		}

		return length;
	}

	public byte[] getProofHash()
	{
		//newSig = sha256(prevSig || pubKey)
		byte[] data = Bytes.concat(this.reference, creator.getPublicKey());

		return Crypto.getInstance().digest(data);
	}

	/*
	public static int getPreviousForgingHeightForIncomes(DBSet dcSet, Account creator, int height) {
		
		// IF BLOCK in the MAP
		int previousForgingHeight = creator.getForgingData(dcSet, height);
		if (previousForgingHeight == -1) {
			// IF BLOCK not inserted in MAP
			previousForgingHeight = creator.getLastForgingData(dcSet);
			if (previousForgingHeight == -1) {
				// if it is first payment to this account
				return height;
			}
		}
		
		return previousForgingHeight;

	}
	*/

	public static int getPreviousForgingHeightForCalcWin(DCSet dcSet, Account creator, int height) {
		
		// IF BLOCK in the MAP
		int previousForgingHeight = creator.getForgingData(dcSet, height);
		if (previousForgingHeight == -1) {
			// IF BLOCK not inserted in MAP
			previousForgingHeight = creator.getLastForgingData(dcSet);			
		}
		
		if (previousForgingHeight > height) {
			return height;
		}
		
		return previousForgingHeight;

	}

	private static long calcLenFoWinValue2(int heightThis, int heightStart, int generatingBalance)
	{
		int len = heightThis - heightStart;
		if (len < 1)
			return 1;
			
		if (generatingBalance == 0) {
			return 1;
		}
		
		int maxLen = (BlockChain.GENESIS_ERA_TOTAL<<1) / BlockChain.MIN_GENERATING_BALANCE;
		int pickBal = BlockChain.GENESIS_ERA_TOTAL / generatingBalance;
		int pickMin = BlockChain.MIN_GENERATING_BALANCE<<1;
		
		if (generatingBalance < BlockChain.MIN_GENERATING_BALANCE)
			return 1;
		else if (generatingBalance < pickMin)
			; // not change
		else if (generatingBalance < pickMin<<3) // 8
			maxLen >>=1;
		else if (generatingBalance < pickMin<<6) // 64
			maxLen >>=2;
		else if (generatingBalance < pickMin<<9) // 512
			maxLen >>=3;
		else if (generatingBalance < pickMin<<12) // 4k
			maxLen >>=4;
		else if (generatingBalance < pickMin<<15) // 32k
			maxLen >>=5;
		else if (generatingBalance < pickMin<<18) // 256k
			maxLen >>=6;
		else if (generatingBalance < pickMin<<21) // 2M
			maxLen >>=7;
		else			
			maxLen >>=8;
		
		if (len > maxLen)
			return maxLen;
		
		return len;
	}

	// may be calculated only for new BLOCK or last created BLOCK for this CREATOR
	// because: creator.getLastForgingData(dcSet);
	// see core.BlockChain.getMinTarget(int)
	public static long calcWinValue(int previousForgingHeight, int height, int generatingBalance)
	{
		
		long win_value = (long)generatingBalance * (height - previousForgingHeight);

		
		if (height < BlockChain.REPEAT_WIN)
			win_value >>= 4;
		else if (BlockChain.DEVELOP_USE)
			win_value >>= 4;
		else if (height < BlockChain.TARGET_COUNT)
			win_value = (win_value >>4) - (win_value >>6);
		else if (height < BlockChain.TARGET_COUNT<<2)
			win_value >>= 5;
		else if (height < BlockChain.TARGET_COUNT<<6)
			win_value = (win_value >>5) - (win_value >>7);
		else if (height < BlockChain.TARGET_COUNT<<10)
			win_value >>= 6;
		else
			win_value = (win_value >>7) - (win_value >>9);
		
		return win_value;

	}
	
	public long calcWinValue(DCSet dcSet)
	{
		if (this.version == 0) {
			// GENESIS
			return BlockChain.GENESIS_WIN_VALUE;
		}

		int height = this.getHeightByParent(dcSet);
		
		if (this.creator == null) {
			LOGGER.error("block.creator == null in BLOCK:" + height);
			return BlockChain.BASE_TARGET;
		}
		
		if (this.generatingBalance <= 0) {
			this.setCalcGeneratingBalance(dcSet);
		}
		
		int previousForgingHeight = getPreviousForgingHeightForCalcWin(dcSet, this.creator, height);
		if (previousForgingHeight == -1)
			return 0l;
		return calcWinValue(previousForgingHeight, height, this.generatingBalance);
	}


	public static int calcWinValueTargeted2(long win_value, long target)
	{

		if (target == 0l) {
			// in forked chain in may be = 0
			return -1;
		}

		int max_targ = BlockChain.BASE_TARGET * 15;
		int koeff = BlockChain.BASE_TARGET;
		int result = 0;
		while (koeff > 0 && result < max_targ && win_value > target<<1) {
			result += BlockChain.BASE_TARGET; 
			koeff >>=1;
			target <<=1;
		}
		
		result += (int)(koeff * win_value / target);
		if (result > max_targ)
			result = max_targ;
		
		return result;
		
	}
	
	public int calcWinValueTargeted(DCSet dcSet)
	{
		
		if (this.version == 0) {
			// GENESIS - getBlockChain = null
			return BlockChain.BASE_TARGET;
		}
		
		long win_value = this.calcWinValue(dcSet);
		long target = BlockChain.getTarget(dcSet, this);
		return calcWinValueTargeted2(win_value, target);
	}

	//VALIDATE

	public boolean isSignatureValid()
	{
		
		if (this.version == 0) {
			// genesis block
			GenesisBlock gb = (GenesisBlock)this;
			return gb.isSignatureValid();
		}
		//VALIDATE BLOCK SIGNATURE
		byte[] data = this.toBytesForSign();

		if(!Crypto.getInstance().verify(this.creator.getPublicKey(), this.signature, data))
		{
			LOGGER.error("Block signature not valid"
					+ ", Creator:" + this.creator.getAddress()
					+ ", SIGN: " + Base58.encode(this.signature));
			return false;
		}

		return true;
	}

	// canonical definition of block version release schedule
	public int getNextBlockVersion(DCSet db)
	{

		return 1;
		
		/*
		int height = getHeight(db);

		if(height < Transaction.getAT_BLOCK_HEIGHT_RELEASE())
		{
			return 1;
		}
		else if(getTimestamp() < Transaction.getPOWFIX_RELEASE())
		{
			return 2;
		}
		else
		{
			return 3;
		}
		*/
	}

	public static int isSoRapidly(int height, Account accountCreator, List<Block> lastBlocksForTarget) {
		
		int repeat_win = 0;
		if (height < BlockChain.REPEAT_WIN<<1) {
			repeat_win = BlockChain.REPEAT_WIN;
		}
		else if (height > 32400) {
		} else {
			return 0;
		}
		
		// NEED CHECK ONLY ON START
		// test repeated win account
		if (lastBlocksForTarget == null || lastBlocksForTarget.isEmpty()) {
			return 0;
		}
		// NEED CHECK ONLY ON START
		int i = 0;
		for (Block testBlock: lastBlocksForTarget) {
			i++;
			if (testBlock.getCreator().equals(accountCreator)) {
				return i;
			} else if ( i > repeat_win) {
				return 0;
			}
		}
	
	return 0;

	}
	public boolean isValid(DCSet db)
	{
		
		int height = this.getHeightByParent(db);
		Controller cnt = Controller.getInstance();

		/*
		// FOR PROBE START !!!
		if(height > 1000)
		{
			LOGGER.error("*** Block[" + this.getHeightByParent(db) + "] is PROBE");
			return false;
		}
		*/
		
		//CHECK IF PARENT EXISTS
		if(this.reference == null || this.getParent(db) == null)
		{
			LOGGER.debug("*** Block[" + height + "].reference invalid");
			return false;
		}

		// TODO - show it to USER
		if(this.getTimestamp(db) + (BlockChain.WIN_BLOCK_BROADCAST_WAIT>>2) > NTP.getTime()) {
			LOGGER.debug("*** Block[" + height + ":" + Base58.encode(this.signature).substring(0, 10) + "].timestamp invalid >NTP.getTime(): "
					+ NTP.getTime() + " + sec: " + (this.getTimestamp(db) - NTP.getTime())/1000);
			return false;			
		}
		
		//CHECK IF VERSION IS CORRECT
		if(this.version != this.getParent(db).getNextBlockVersion(db))
		{
			LOGGER.debug("*** Block[" + height + "].version invalid");
			return false;
		}
		if(this.version < 2 && this.atBytes != null && this.atBytes.length > 0) // || this.atFees != 0))
		{
			LOGGER.debug("*** Block[" + height + "].version AT invalid");
			return false;
		}
		
		
		// TEST STRONG of win Value
		int base = BlockChain.getMinTarget(height);
		int targetedWinValue = this.calcWinValueTargeted(db); 
		if (!cnt.isTestNet() && base > targetedWinValue) {
			targetedWinValue = this.calcWinValueTargeted(db);
			LOGGER.debug("*** Block[" + height + "] targeted WIN_VALUE < MINIMAL TARGET " + targetedWinValue + " < " + base);
			return false;
		}
		
		// STOP IF SO RAPIDLY			
		if (!cnt.isTestNet() && isSoRapidly(height, this.getCreator(),
				cnt.getBlockChain().getLastBlocksForTarget(db)) > 0) {
			LOGGER.debug("*** Block[" + height + "] REPEATED WIN invalid");
			return false;
		}

		if ( this.atBytes != null && this.atBytes.length > 0 )
		{
			try
			{

				AT_Block atBlock = AT_Controller.validateATs( this.getBlockATs() , db.getBlockMap().getLastBlock().getHeight(db)+1 , db);
				//this.atFees = atBlock.getTotalFees();
			}
			catch(NoSuchAlgorithmException | AT_Exception e)
			{
				LOGGER.error(e.getMessage(),e);
				return false;
			}
		}

		//CHECK TRANSACTIONS
		this.getTransactions(); // load from RAW transactions
		
		if (this.transactions == null || this.transactions.size() == 0) {
			// empty transactions
		} else {
			DCSet fork = db.fork();
			byte[] transactionsSignatures = new byte[0];
			
			long timestampEnd = this.getTimestamp(db);
			// because time filter used by parent block timestamp on core.BlockGenerator.run()
			long timestampBeg = this.getParent(fork).getTimestamp(fork);

			for(Transaction transaction: this.transactions)
			{
				if (cnt.isOnStopping())
					return false;

				if (!transaction.isWiped()) {
					// NOT WIPERD
	
					//CHECK IF NOT GENESISTRANSACTION
					if(transaction.getCreator() == null)
						 // ALL GENESIS transaction
						return false;
					
					if(!transaction.isSignatureValid()) {
						// 
						LOGGER.debug("*** Block[" + height
						+ "].Tx[" + this.getTransactionSeq(transaction.getSignature()) + " : "
						+ transaction.viewFullTypeName() + "]"
						+ "signature not valid!");
						return false;
					}
		
					transaction.setDB(fork, false);

					//CHECK IF VALID
					if ( transaction instanceof DeployATTransaction)
					{
						Integer min = 0;
						if ( db.getBlockMap().getParentList() != null )
						{
							min = AT_API_Platform_Impl.getForkHeight(db);
						}
		
						DeployATTransaction atTx = (DeployATTransaction)transaction;
						if ( atTx.isValid(fork, min) != Transaction.VALIDATE_OK )
						{
							LOGGER.debug("*** Block[" + height + "].atTx invalid");
							return false;
						}
					} else if(transaction.isValid(fork, null) != Transaction.VALIDATE_OK)
					{
						LOGGER.debug("*** Block[" + height
							+ "].Tx[" + this.getTransactionSeq(transaction.getSignature()) + " : "
							+ transaction.viewFullTypeName() + "]"
							+ "invalid code: " + transaction.isValid(fork, null));
						return false;
					}
		
					//CHECK TIMESTAMP AND DEADLINE
					long transactionTimestamp = transaction.getTimestamp();
					if( transactionTimestamp > timestampEnd
							|| transaction.getDeadline() <= timestampBeg)
					{
						LOGGER.debug("*** Block[" + height + "].TX.timestamp invalid");
						return false;
					}
		
					try{
						//PROCESS TRANSACTION IN MEMORYDB TO MAKE SURE OTHER TRANSACTIONS VALIDATE PROPERLY
						transaction.process(fork, this, false);
						
					} catch (Exception e) {
	                    LOGGER.error("*** Block[" + height + "].TX.process ERROR", e);
	                    return false;                    
					}
				}
				transactionsSignatures = Bytes.concat(transactionsSignatures, transaction.getSignature());
			}
			
			transactionsSignatures = Crypto.getInstance().digest(transactionsSignatures);
			if (!Arrays.equals(this.transactionsHash, transactionsSignatures)) {
				LOGGER.debug("*** Block[" + height + "].digest(transactionsSignatures) invalid");
				return false;
			}
		}

		//BLOCK IS VALID
		return true;
	}

	//PROCESS/ORPHAN

	// TODO - make it trownable
	public void process(DCSet dcSet) throws Exception
	{			
		
		Controller cnt = Controller.getInstance();
		if (cnt.isOnStopping())
			throw new Exception("on stoping");
		
		long start = System.currentTimeMillis();

		if (this.generatingBalance <= 0) {
			this.setCalcGeneratingBalance(dcSet);
			long tickets = System.currentTimeMillis() - start;
			//LOGGER.error("[" + this.heightBlock + "] setCalcGeneratingBalance time: " +  tickets*0.001 );
		}

		//ADD TO DB
		dcSet.getBlockMap().set(this);
		
		this.heightBlock = dcSet.getBlockSignsMap().getHeight(this.signature);

		//PROCESS TRANSACTIONS
		int seq = 1;
		this.getTransactions();
		//DBSet dbSet = Controller.getInstance().getDBSet();
		TransactionRef_BlockRef_Map refsMap = dcSet.getTransactionRef_BlockRef_Map();
		TransactionMap unconfirmedMap = dcSet.getTransactionMap();
		TransactionFinalMap finalMap = dcSet.getTransactionFinalMap();
		for(Transaction transaction: this.transactions)
		{
			
			if (cnt.isOnStopping())
				throw new Exception("on stoping");
			
			//PROCESS
			if (!transaction.isWiped()) {
				transaction.setDB(dcSet, false);
				transaction.process(dcSet, this, false);
			} else {
				//UPDATE REFERENCE OF SENDER
				if (transaction.isReferenced() )
					// IT IS REFERENCED RECORD?
					transaction.getCreator().setLastTimestamp(transaction.getTimestamp(), dcSet);
			}
			
			//SET PARENT
			refsMap.set(transaction, this);

			//REMOVE FROM UNCONFIRMED DATABASE
			unconfirmedMap.delete(transaction);

			Tuple2<Integer, Integer> key = new Tuple2<Integer, Integer>(this.heightBlock, seq);

			if (cnt.isOnStopping())
				throw new Exception("on stoping");
			
			finalMap.set( key, transaction);
			seq++;

		}

		//PROCESS FEE
		BigDecimal blockFee = this.getTotalFeeForProcess(dcSet);
		BigDecimal blockTotalFee = getTotalFee(dcSet);

		
		if (blockFee.compareTo(blockTotalFee) < 0) {
			
			// find rich account
			String rich = Account.getRich(Transaction.FEE_KEY);
			if (!rich.equals(this.creator.getAddress())) {
			
				BigDecimal bonus_fee = blockTotalFee.subtract(blockFee);
				blockFee = blockTotalFee;
				Account richAccount = new Account(rich);
			
				//richAccount.setBalance(Transaction.FEE_KEY, richAccount.getBalance(dcSet, Transaction.FEE_KEY).subtract(bonus_fee), dcSet);
				richAccount.changeBalance(dcSet, true, Transaction.FEE_KEY, bonus_fee.divide(new BigDecimal(2)));
				
			}
		}

		//UPDATE GENERATOR BALANCE WITH FEE
		//this.creator.setBalance(Transaction.FEE_KEY, this.creator.getBalance(dcSet, Transaction.FEE_KEY).add(blockFee), dcSet);
		if (cnt.isOnStopping())
			throw new Exception("on stoping");
		
		this.creator.changeBalance(dcSet, false, Transaction.FEE_KEY, blockFee);

		/*
		if (!dcSet.isFork()) {
			int lastHeight = dcSet.getBlockMap().getLastBlock().getHeight(dcSet);
			LOGGER.error("*** core.block.Block.process(DBSet)[" + (this.getParentHeight(dcSet) + 1)
					+ "] SET new last Height: " + lastHeight
					+ " getHeightMap().getHeight: " + this.height_process);
		}
		*/

		BlockChain blockChain = cnt.getBlockChain();
		if (blockChain != null) {
			cnt.getBlockChain().setCheckPoint(this.heightBlock - BlockChain.MAX_ORPHAN);
		}

		if(heightBlock % BlockChain.MAX_ORPHAN == 0) 
		{
			cnt.blockchainSyncStatusUpdate(heightBlock);
		}
		long tickets = System.currentTimeMillis() - start;
		if (tickets > 1000) {
			LOGGER.info("[" + this.heightBlock + "] processing time: " +  tickets*0.001
				+ " for records:" + this.getTransactionCount() + " millsec/record:" + tickets/(this.getTransactionCount()+1) );
		}
		
	}

	public void orphan(DCSet dcSet) throws Exception
	{

		Controller cnt = Controller.getInstance();
		if (cnt.isOnStopping())
			throw new Exception("on stoping");

		//LOGGER.debug("<<< core.block.Block.orphan(DBSet) #0");
		int height = this.getHeight(dcSet);
		if (height == 1) {
			// GENESIS BLOCK cannot be orphanED
			return;
		}
		
		long start = System.currentTimeMillis();
		

		//ORPHAN AT TRANSACTIONS
		if (BlockChain.USE_AT_ATX) {
			//LOGGER.debug("<<< core.block.Block.orphan(DBSet) #1 ORPHAN AT TRANSACTIONS");
	
			LinkedHashMap< Tuple2<Integer, Integer> , AT_Transaction > atTxs = dcSet.getATTransactionMap().getATTransactions(height);
	
			Iterator<AT_Transaction> iter = atTxs.values().iterator();
	
			while ( iter.hasNext() )
			{
				AT_Transaction key = iter.next();
				Long amount  = key.getAmount();
				if (key.getRecipientId() != null && !Arrays.equals(key.getRecipientId(), new byte[ AT_Constants.AT_ID_SIZE ]) && !key.getRecipient().equalsIgnoreCase("1") )
				{
					Account recipient = new Account( key.getRecipient() );
					//recipient.setBalance(Transaction.FEE_KEY,  recipient.getBalance(dcSet,  Transaction.FEE_KEY ).subtract( BigDecimal.valueOf( amount, 8 ) ) , dcSet );
					recipient.changeBalance(dcSet, true, Transaction.FEE_KEY, BigDecimal.valueOf( amount, 8 ));
				}
				Account sender = new Account( key.getSender() );
				//sender.setBalance(Transaction.FEE_KEY,  sender.getBalance(dcSet,  Transaction.FEE_KEY ).add( BigDecimal.valueOf( amount, 8 ) ) , dcSet );
				sender.changeBalance(dcSet, false, Transaction.FEE_KEY, BigDecimal.valueOf( amount, 8 ) );
	
			}
		}
		

		//ORPHAN TRANSACTIONS
		//LOGGER.debug("<<< core.block.Block.orphan(DBSet) #2 ORPHAN TRANSACTIONS");
		this.orphanTransactions(dcSet, height);

		//LOGGER.debug("<<< core.block.Block.orphan(DBSet) #2f FEE");

		//REMOVE FEE
		BigDecimal blockFee = this.getTotalFeeForProcess(dcSet);
		BigDecimal blockTotalFee = getTotalFee(dcSet); 

		if (blockFee.compareTo(blockTotalFee) < 0) {
			
			// find rich account
			String rich = Account.getRich(Transaction.FEE_KEY);

			if (!rich.equals(this.creator.getAddress())) {
				BigDecimal bonus_fee = blockTotalFee.subtract(blockFee);
				blockFee = blockTotalFee;

				Account richAccount = new Account(rich);
				//richAccount.setBalance(Transaction.FEE_KEY, richAccount.getBalance(dcSet, Transaction.FEE_KEY).add(bonus_fee), dcSet);
				richAccount.changeBalance(dcSet, false, Transaction.FEE_KEY, bonus_fee.divide(new BigDecimal(2)));
			}
		}

		//LOGGER.debug("<<< core.block.Block.orphan(DBSet) #3");

		//UPDATE GENERATOR BALANCE WITH FEE
		//this.creator.setBalance(Transaction.FEE_KEY, this.creator.getBalance(dcSet, Transaction.FEE_KEY).subtract(blockFee), dcSet);
		this.creator.changeBalance(dcSet, true, Transaction.FEE_KEY, blockFee);

		//DELETE AT TRANSACTIONS FROM DB
		if (BlockChain.USE_AT_ATX) dcSet.getATTransactionMap().delete(height);
		
		//// DELETE in orphanTransactions!
		//DELETE TRANSACTIONS FROM FINAL MAP
		/////dcSet.getTransactionFinalMap().delete(height);

		//DELETE BLOCK FROM DB
		dcSet.getBlockMap().delete(this);
		
		/*
		if (height > 1) {
			int lastHeightThis = dcSet.getBlockMap().getLastBlock().getHeight(dcSet);
			int lastHeight = dcSet.getBlockMap().getLastBlock().getHeight(dcSet);
			LOGGER.error("*** core.block.Block.orphan(DBSet)[" + height + ":" + lastHeightThis
					+ "] DELETE -> new last Height: " + lastHeight
					+ (dcSet.isFork()?" in FORK!": ""));
		}
		*/

		//LOGGER.debug("<<< core.block.Block.orphan(DBSet) #4");

		long tickets = System.currentTimeMillis() - start;
		LOGGER.error("[" + this.heightBlock + "] orphaning time: " +  (System.currentTimeMillis() - start)*0.001
				+ " for records:" + this.getTransactionCount() + " millsec/record:" + tickets/(this.getTransactionCount()+1) );

		this.heightBlock = -1;
		this.parentBlock = null;

	}

	private void orphanTransactions(DCSet dcSet, int height) throws Exception
	{
		
		Controller cnt = Controller.getInstance();
		//DBSet dbSet = Controller.getInstance().getDBSet();

		TransactionRef_BlockRef_Map refsMap = dcSet.getTransactionRef_BlockRef_Map();
		TransactionMap unconfirmedMap = dcSet.getTransactionMap();
		TransactionFinalMap finalMap = dcSet.getTransactionFinalMap();

		//ORPHAN ALL TRANSACTIONS IN DB BACK TO FRONT
		for(int i=this.getTransactions().size() -1; i>=0; i--)
		{
			if (cnt.isOnStopping())
				throw new Exception("on stoping");

			Transaction transaction = transactions.get(i);
			//LOGGER.debug("<<< core.block.Block.orphanTransactions\n" + transaction.toJson());

			if (!transaction.isWiped()) {
				transaction.setDB(dcSet, false);
				transaction.orphan(dcSet, false);
			} else {
				// IT IS REFERENCED RECORD?
				if (transaction.isReferenced() ) {
					//UPDATE REFERENCE OF SENDER
					transaction.getCreator().removeLastTimestamp(dcSet);
				}
			}
			
			//ADD ORPHANED TRANASCTIONS BACK TO DATABASE
			unconfirmedMap.add(transaction);
	
			Tuple2<Integer, Integer> key = new Tuple2<Integer, Integer>(height, i);
			finalMap.delete(key);
	
			//DELETE ORPHANED TRANASCTIONS FROM PARENT DATABASE
			refsMap.delete(transaction.getSignature());

		}
	}

	@Override 
	public boolean equals(Object otherObject)
	{
		if(otherObject instanceof Block)
		{
			Block otherBlock = (Block) otherObject;
			
			return Arrays.equals(this.getSignature(), otherBlock.getSignature());
		}
		
		return false;
	}

	public String toString(DCSet dcSet) {
		return "H:" + this.getHeightByParent(dcSet)
			+ " W: " + this.calcWinValue(dcSet)
			+ "WT: " + this.calcWinValueTargeted(dcSet)
			+ " C: " + this.getCreator().getPersonAsString();
	}
	
	public void stop() {		
		this.run = false;
	}

}
