package org.erachain.dbs.mapDB;

import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.crypto.Crypto;
import org.erachain.database.DBASet;
import org.mapdb.*;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple5;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Collection;

// TODO SOFT HARD TRUE

public class ItemAssetBalanceMapDBMap extends DBMapSuit<byte[], Tuple5<
        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> {


    static Logger logger = LoggerFactory.getLogger(TransactionMapDBMap.class.getSimpleName());

    @SuppressWarnings("rawtypes")
    private BTreeMap assetKeyMap;
    private BTreeMap addressKeyMap;

    public ItemAssetBalanceMapDBMap(DBASet databaseSet, DB database) {
        super(databaseSet, database);

    }

    @SuppressWarnings({"unchecked"})
    @Override
    protected void getMap() {
        //OPEN MAP
        BTreeMap<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> treeMap;
        HTreeMap<byte[], Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>> hashMap;

        if (true) {
            hashMap = database.createHashMap("balances")
                    .keySerializer(SerializerBase.BYTE_ARRAY)
                    .hasher(Hasher.BYTE_ARRAY)
                    .counterEnable()
                    .makeOrGet();
            map = hashMap;
        } else {

            treeMap = database.createTreeMap("balances")
                    //.keySerializer(BTreeKeySerializer.TUPLE2)
                    .keySerializer(BTreeKeySerializer.BASIC)
                    //.keySerializer(new BTreeKeySerializer.Tuple2KeySerializer(
                    //        UnsignedBytes.lexicographicalComparator(), // Fun.BYTE_ARRAY_COMPARATOR,
                    //        Serializer.BYTE_ARRAY,
                    //        Serializer.LONG))
                    //.comparator(Fun.TUPLE2_COMPARATOR)
                    .comparator(Fun.BYTE_ARRAY_COMPARATOR)
                    //.comparator(UnsignedBytes.lexicographicalComparator())
                    .counterEnable()
                    .makeOrGet();
            map = treeMap;
        }

        if (Controller.getInstance().onlyProtocolIndexing)
            // NOT USE SECONDARY INDEXES
            return;


        //BIND ASSET KEY
        /// так как основной Индекс не сравниваемы - byte[] то во Вторичном индексе делаем Строку
        // - иначе она не сработает так как тут дерево с поиском
        this.assetKeyMap = database.createTreeMap("balances_key_asset_bal_address")
                .comparator(Fun.COMPARATOR)
                //.valuesOutsideNodesEnable()
                .makeOrGet();

        Bind.secondaryKey(hashMap, this.assetKeyMap, new Fun.Function2<Tuple2<Long, BigDecimal>,
                byte[],
                Tuple5<
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
                () {
            @Override
            public Tuple2<Long, BigDecimal>
            run(byte[] key, Tuple5<
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {

                byte[] assetKeyBytes = new byte[8];
                System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                return new Tuple2<Long, BigDecimal>(
                        Longs.fromByteArray(assetKeyBytes), value.a.b.negate()
                    );
            }
        });

        this.addressKeyMap = database.createTreeMap("balances_address_asset_bal")
                .comparator(Fun.COMPARATOR)
                //.valuesOutsideNodesEnable()
                .makeOrGet();

        Bind.secondaryKey(hashMap, this.addressKeyMap, new Fun.Function2<Tuple2<String, Long>,
                byte[],
                Tuple5<
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                        Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>>
                () {
            @Override
            public Tuple2<String, Long>
            run(byte[] key, Tuple5<
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                    Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> value) {

                // Address
                byte[] shortAddress = new byte[20];
                System.arraycopy(key, 0, shortAddress, 0, 20);
                // ASSET KEY
                byte[] assetKeyBytes = new byte[8];
                System.arraycopy(key, 20, assetKeyBytes, 0, 8);

                return new Tuple2<String, Long>(
                        Crypto.getInstance().getAddressFromShort(shortAddress),
                        Longs.fromByteArray(assetKeyBytes)
                );
            }
        });

    }

    protected Tuple5<
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
            Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>> getDefaultValue() {
        return new Tuple5<
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>,
                Tuple2<BigDecimal, BigDecimal>, Tuple2<BigDecimal, BigDecimal>>
                (new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO),
                        new Tuple2<BigDecimal, BigDecimal>(BigDecimal.ZERO, BigDecimal.ZERO));
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void createIndexes() {
    }

    public Collection<byte[]> assetKeySubMap(long key) {
        return this.assetKeyMap.subMap(
                Fun.t2(key, null),
                Fun.t2(key, Fun.HI())).values();
    }

    public Collection<byte[]> addressKeySubMap(String address) {
        return this.addressKeyMap.subMap(
                Fun.t2(address, null),
                Fun.t2(address, Fun.HI())).values();
    }

}
