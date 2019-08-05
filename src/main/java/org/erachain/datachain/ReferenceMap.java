package org.erachain.datachain;

import com.google.common.primitives.UnsignedBytes;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import java.util.Map;
import java.util.TreeMap;


/**
 * TODO: Надо подумать может она лишняя??
 * seek reference to tx_Parent by address+timestamp
 * account.address -> <tx2.parentTimestamp>
 *
 */
public class ReferenceMap extends DCMap<byte[], Long> {

    public ReferenceMap(DCSet databaseSet, DB database) {
        super(databaseSet, database);
    }

    public ReferenceMap(ReferenceMap parent, DCSet dcSet) {
        super(parent, dcSet);
    }

    @Override
    protected Map<byte[], Long> getMap(DB database) {
        //OPEN MAP
        return database.createTreeMap("references")
                .keySerializer(BTreeKeySerializer.BASIC)
                .comparator(UnsignedBytes.lexicographicalComparator())
                .valuesOutsideNodesEnable()
                .counterEnable()
                .makeOrGet();
    }

    @Override
    protected Map<byte[], Long> getMemoryMap() {
        return new TreeMap<>(UnsignedBytes.lexicographicalComparator());
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected Long getDefaultValue() {
        // NEED for toByte for not referenced accounts
        return 0L;
    }

}