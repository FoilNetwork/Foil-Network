package gui.transaction;

import javax.swing.JFrame;

import core.transaction.ArbitraryTransaction;
import core.transaction.BuyNameTransaction;
import core.transaction.CancelOrderTransaction;
import core.transaction.CancelSellNameTransaction;
import core.transaction.CreateOrderTransaction;
import core.transaction.CreatePollTransaction;

import core.transaction.GenesisCertifyPersonRecord;
import core.transaction.GenesisIssueAssetTransaction;
import core.transaction.GenesisIssueNoteRecord;
import core.transaction.GenesisTransferAssetTransaction;

import core.transaction.IssueAssetTransaction;
import core.transaction.IssueImprintRecord;
import core.transaction.IssueNoteRecord;
import core.transaction.IssuePersonRecord;
import core.transaction.IssueStatusRecord;
import core.transaction.IssueUnionRecord;
import core.transaction.R_Send;
import core.transaction.MultiPaymentTransaction;
import core.transaction.R_SertifyPubKeys;
import core.transaction.R_SetStatusToItem;
import core.transaction.R_SignNote;
import core.transaction.R_Vouch;
import core.transaction.RegisterNameTransaction;
import core.transaction.SellNameTransaction;
import core.transaction.Transaction;
import core.transaction.UpdateNameTransaction;
import core.transaction.VoteOnPollTransaction;

public class TransactionDetailsFactory 
{
	private static TransactionDetailsFactory instance;

	public static TransactionDetailsFactory getInstance()
	{
		if(instance == null)
		{
			instance = new TransactionDetailsFactory();
		}
		
		return instance;
	}
	
	public JFrame createTransactionDetail(Transaction transaction)
	{
		switch(transaction.getType())
		{
		
		case Transaction.SIGN_NOTE_TRANSACTION:
			
			R_SignNote statement = (R_SignNote) transaction;
			return new RecStatementDetailsFrame(statement);
			
		case Transaction.REGISTER_NAME_TRANSACTION:
			
			RegisterNameTransaction nameRegistration = (RegisterNameTransaction) transaction;
			return new RegisterNameDetailsFrame(nameRegistration);
			
		case Transaction.UPDATE_NAME_TRANSACTION:
			
			UpdateNameTransaction nameUpdate = (UpdateNameTransaction) transaction;
			return new UpdateNameDetailsFrame(nameUpdate);	
			
		case Transaction.SELL_NAME_TRANSACTION:
			
			SellNameTransaction nameSale = (SellNameTransaction) transaction;
			return new SellNameDetailsFrame(nameSale);		
			
		case Transaction.CANCEL_SELL_NAME_TRANSACTION:
			
			CancelSellNameTransaction cancelNameSale = (CancelSellNameTransaction) transaction;
			return new CancelSellNameDetailsFrame(cancelNameSale);			
			
		case Transaction.BUY_NAME_TRANSACTION:
			
			BuyNameTransaction namePurchase = (BuyNameTransaction) transaction;
			return new BuyNameDetailsFrame(namePurchase);	
		
		case Transaction.CREATE_POLL_TRANSACTION:
			
			CreatePollTransaction pollCreation = (CreatePollTransaction) transaction;
			return new CreatePollDetailsFrame(pollCreation);			

		case Transaction.VOTE_ON_POLL_TRANSACTION:
			
			VoteOnPollTransaction pollVote = (VoteOnPollTransaction) transaction;
			return new VoteOnPollDetailsFrame(pollVote);
			
		case Transaction.ARBITRARY_TRANSACTION:
			
			ArbitraryTransaction arbitraryTransaction = (ArbitraryTransaction) transaction;
			return new ArbitraryTransactionDetailsFrame(arbitraryTransaction);	
			
		case Transaction.ISSUE_ASSET_TRANSACTION:
			
			IssueAssetTransaction issueAssetTransaction = (IssueAssetTransaction) transaction;
			return new IssueAssetDetailsFrame(issueAssetTransaction);	
						
		case Transaction.ISSUE_PERSON_TRANSACTION:
			
			IssuePersonRecord issuePerson = (IssuePersonRecord) transaction;
			return new IssuePersonDetailsFrame(issuePerson);	

		case Transaction.SET_STATUS_TO_ITEM_TRANSACTION:
			
			R_SetStatusToItem setStatusToItem = (R_SetStatusToItem) transaction;
			return new SetStatusToItemDetailsFrame(setStatusToItem);	

		case Transaction.CREATE_ORDER_TRANSACTION:
			
			CreateOrderTransaction createOrderTransaction = (CreateOrderTransaction) transaction;
			return new CreateOrderDetailsFrame(createOrderTransaction);	
		
		case Transaction.CANCEL_ORDER_TRANSACTION:
			
			CancelOrderTransaction cancelOrderTransaction = (CancelOrderTransaction) transaction;
			return new CancelOrderDetailsFrame(cancelOrderTransaction);		
			
		case Transaction.MULTI_PAYMENT_TRANSACTION:
			
			MultiPaymentTransaction MultiPaymentTransaction = (MultiPaymentTransaction) transaction;
			return new MultiPaymentDetailsFrame(MultiPaymentTransaction);

		case Transaction.SEND_ASSET_TRANSACTION:
			R_Send r_Send = (R_Send)transaction;
			return new Send_RecordDetailsFrame(r_Send);
			
		case Transaction.VOUCH_TRANSACTION:
			R_Vouch r_Vouch = (R_Vouch)transaction;
			return new VouchingDetailsFrame(r_Vouch);

		case Transaction.CERTIFY_PUB_KEYS_TRANSACTION:
			R_SertifyPubKeys sertifyPubKeysRecord = (R_SertifyPubKeys)transaction;
			return new SertifyPubKeysDetailsFrame(sertifyPubKeysRecord);

		case Transaction.ISSUE_IMPRINT_TRANSACTION:
			
			IssueImprintRecord issueImprint = (IssueImprintRecord) transaction;
			return new IssueImprintDetailsFrame(issueImprint);	

		case Transaction.ISSUE_NOTE_TRANSACTION:
			
			IssueNoteRecord issueNote = (IssueNoteRecord) transaction;
			return new IssueNoteDetailsFrame(issueNote);	

		case Transaction.ISSUE_UNION_TRANSACTION:
			
			IssueUnionRecord issueUnion = (IssueUnionRecord) transaction;
			return new IssueUnionDetailsFrame(issueUnion);	

		case Transaction.ISSUE_STATUS_TRANSACTION:
			
			IssueStatusRecord issueStatus = (IssueStatusRecord) transaction;
			return new IssueStatusDetailsFrame(issueStatus);	

		case Transaction.GENESIS_SEND_ASSET_TRANSACTION:
			
			GenesisTransferAssetTransaction genesisTransferAssetTransaction = (GenesisTransferAssetTransaction) transaction;
			return new GenesisTransferAssetDetailsFrame(genesisTransferAssetTransaction);		
			
		case Transaction.GENESIS_ISSUE_NOTE_TRANSACTION:
			
			GenesisIssueNoteRecord genesisIssueNoteRecord = (GenesisIssueNoteRecord) transaction;
			return new GenesisIssueNoteDetailsFrame(genesisIssueNoteRecord);	

		case Transaction.GENESIS_ISSUE_ASSET_TRANSACTION:
			
			GenesisIssueAssetTransaction genesisIssueAssetTransaction = (GenesisIssueAssetTransaction) transaction;
			return new GenesisIssueAssetDetailsFrame(genesisIssueAssetTransaction);	

		case Transaction.GENESIS_ISSUE_PERSON_TRANSACTION:
			
			GenesisCertifyPersonRecord record = (GenesisCertifyPersonRecord) transaction;
			return new GenesisCertifyPersonRecordFrame(record);
		}
		
		return null;
	}
}
