package org.erachain.datachain;

import org.erachain.core.item.ItemCls;
import org.erachain.database.DBMap;
import org.mapdb.BTreeMap;
import org.mapdb.Fun;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.mapdb.Atomic;
import org.mapdb.DB;
import org.erachain.utils.Pair;

import java.util.*;

/**
 * Хранение сущностей
 * <p>
 * ключ: номер, с самоувеличением
 * Значение: Сущность
 */
public abstract class ItemMap extends DCMap<Long, ItemCls> {

    private static Logger logger = LoggerFactory.getLogger(ItemMap.class.getName());

    protected Atomic.Long atomicKey;
    protected long key;

    public ItemMap(DCSet databaseSet, DB database, String name) {
        super(databaseSet, database);

        atomicKey = database.getAtomicLong(name + "_key");
        key = atomicKey.get();
    }

    public ItemMap(DCSet databaseSet, DB database,
                   // int type,
                   String name, int observeReset, int observeAdd, int observeRemove, int observeList) {
        this(databaseSet, database, name);
        if (databaseSet.isWithObserver()) {
            if (observeReset > 0)
                this.observableData.put(DBMap.NOTIFY_RESET, observeReset);
            if (observeList > 0)
                this.observableData.put(DBMap.NOTIFY_LIST, observeList);
            if (observeAdd > 0) {
                observableData.put(DBMap.NOTIFY_ADD, observeAdd);
            }
            if (observeRemove > 0) {
                observableData.put(DBMap.NOTIFY_REMOVE, observeRemove);
            }
        }
    }

    public ItemMap(ItemMap parent) {
        super(parent, null);
        key = parent.getLastKey();
    }

    public long getLastKey() {
        return key;
    }

    public void setLastKey(long key) {
        // INCREMENT ATOMIC KEY IF EXISTS
        if (atomicKey != null) {
            atomicKey.set(key);
        }
        this.key = key;
    }

    protected void createIndexes(DB database) {
    }

    @Override
    protected Map<Long, ItemCls> getMemoryMap() {
        return new TreeMap<Long, ItemCls>();
    }

    @Override
    protected ItemCls getDefaultValue() {
        return null;
    }

    public ItemCls get(Long key) {
        ItemCls item = super.get(key);
        if (item == null) {
            return null;
        }

        item.setKey(key);
        return item;
    }

    public long add(ItemCls item) {
        // INCREMENT ATOMIC KEY IF EXISTS
        if (atomicKey != null) {
            atomicKey.incrementAndGet();
        }

        // INCREMENT KEY
        key++;
        item.setKey(key);

        // INSERT WITH NEW KEY
        set(key, item);

        // RETURN KEY
        return key;
    }

    public void remove(long key) {
        super.delete(key);

        if (this.key != key) {
            // it is not top of STACK (for UNIQUE items with short NUM)
            return;
        }
        // delete on top STACK

        if (atomicKey != null) {
            atomicKey.decrementAndGet();
        }

        // DECREMENT KEY
        --this.key;

    }

    // get list keys in name substring str
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<Long> findKeysByName(String str, boolean caseCharacter) {

        // TODO сделать поиск по ограничению  не перебором

        if (str == null || str.length() < 3){
            return null;
        }

        if (!caseCharacter) {
            str = str.toLowerCase();
        }

        List<Long> result = new ArrayList<>();

        Iterator<Long> iterator = this.getIterator(DEFAULT_INDEX, false);

        while (iterator.hasNext()) {

            Long itemKey = iterator.next();
            ItemCls item = get(itemKey);
            String s1 = item.getName();
            if (!caseCharacter) {
                s1 = s1.toLowerCase();
            }

            if (s1.contains(str))
                result.add(itemKey);
        }

        return result;
    }

    // get list items in name substring str
    @SuppressWarnings({"unchecked", "rawtypes"})
    public List<ItemCls> findByName(String str, boolean caseCharacter) {

        // TODO сделать поиск по ограничению  не перебором

        if (str == null || str.length() < 3){
            return null;
        }

        if (!caseCharacter) {
            str = str.toLowerCase();
        }

        List<ItemCls> result = new ArrayList<>();

        Iterator<Long> iterator = this.getIterator(DEFAULT_INDEX, false);

        while (iterator.hasNext()) {

            ItemCls item = get(iterator.next());
            String s1 = item.getName();
            if (!caseCharacter) {
                s1 = s1.toLowerCase();
            }

            if (s1.contains(str))
                result.add(item);
        }

        return result;
    }

    public Collection<Long> getFromToKeys(long fromKey, long toKey) {
        return ((BTreeMap)map).subMap(fromKey, toKey).values();
    }

}