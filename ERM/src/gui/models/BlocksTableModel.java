package gui.models;
import java.util.Observable;
import java.util.Observer;

import org.apache.log4j.Logger;

import utils.DateTimeFormat;
import utils.NumberAsString;
import utils.ObserverMessage;
import controller.Controller;
import core.block.Block;
import database.BlockMap;
import database.DBSet;
import database.SortableList;
import lang.Lang;

@SuppressWarnings("serial")
public class BlocksTableModel extends TableModelCls<byte[], Block> implements Observer{

	public static final int COLUMN_HEIGHT = 0;
	public static final int COLUMN_TIMESTAMP = 1;
	public static final int COLUMN_GENERATOR = 2;
	public static final int COLUMN_BASETARGET = 3;
	public static final int COLUMN_TRANSACTIONS = 4;
	public static final int COLUMN_FEE = 5;
	
	private SortableList<byte[], Block> blocks;
	private long winValue = 0l;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Height", "Timestamp", "Generator", "Generating Balance", "Transactions", "Fee"});
	
	static Logger LOGGER = Logger.getLogger(BlocksTableModel.class.getName());

	public BlocksTableModel()
	{
		Controller.getInstance().addObserver(this);
	}
	
	@Override
	public SortableList<byte[], Block> getSortableList() {
		return this.blocks;
	}
	
	@Override
	public int getColumnCount() 
	{
		return columnNames.length;
	}

	@Override
	public String getColumnName(int index) 
	{
		return columnNames[index];
	}

	@Override
	public int getRowCount() 
	{
		if(blocks == null)
		{
			return 0;
		}
		
		return blocks.size();
	}

	@Override
	public Object getValueAt(int row, int column)
	{
		try {
			
			if(blocks == null || blocks.size() - 1 < row)
			{
				return null;
			}
			
			Block block = blocks.get(row).getB();
			
			switch(column)
			{
			case COLUMN_HEIGHT:

				if (row == 0) {
					return block.getHeight(DBSet.getInstance())
							+ " " + Controller.getInstance().getBlockChain().getFullWeight();
					
				}
				
				return block.getHeight(DBSet.getInstance()) + " " + winValue;
				
			case COLUMN_TIMESTAMP:
				
				return DateTimeFormat.timestamptoString(block.getTimestamp(DBSet.getInstance())) + " " + block.getTimestamp(DBSet.getInstance())/ 1000;
				
			case COLUMN_GENERATOR:
				
				return block.getCreator().asPerson();
				
			case COLUMN_BASETARGET:
				
				return block.getGeneratingBalance() + " "
						+ block.calcWinValueTargeted(DBSet.getInstance());
				
			case COLUMN_TRANSACTIONS:
				
				return block.getTransactionCount();
				
			case COLUMN_FEE:	
				
				return NumberAsString.getInstance().numberAsString(block.getTotalFee());
				
			}
			
			return null;
		
		} catch (Exception e) {
			//LOGGER.error(e.getMessage(),e);
			return null;
		}
	}

	@Override
	public void update(Observable o, Object arg) 
	{	
		try
		{
			this.syncUpdate(o, arg);
		}
		catch(Exception e)
		{
			//GUI ERROR
		}
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_BLOCK_TYPE)
		{			
			if(this.blocks == null)
			{
				this.blocks = (SortableList<byte[], Block>) message.getValue();
				this.blocks.registerObserver();
				this.blocks.sort(BlockMap.HEIGHT_INDEX, true);
			}	
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_BLOCK_TYPE || message.getType() == ObserverMessage.REMOVE_BLOCK_TYPE)
		{
			this.fireTableDataChanged();
		}
	}

	public void removeObservers() 
	{
		this.blocks.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
