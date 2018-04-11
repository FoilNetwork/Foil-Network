package datachain;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import core.transaction.Transaction;
import datachain.DCSet;
import datachain.Issue_ItemMap;

public class IssueImprintMap extends Issue_ItemMap 
{
	
	public IssueImprintMap(DCSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public IssueImprintMap(IssueImprintMap parent) 
	{
		super(parent);
	}
	
	@Override
	protected Map<byte[], Long> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("imprint_OrphanData")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.counterEnable()
				.makeOrGet();
	}

}
