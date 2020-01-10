package org.erachain.database;
// 30/03 ++

import lombok.extern.slf4j.Slf4j;
import org.erachain.settings.Settings;
import org.erachain.utils.SimpleFileVisitorForRecursiveFolderDeletion;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import java.io.File;
import java.nio.file.Files;

@Slf4j
public class DLSet extends DBASet {

    private PeerMap peerMap;

    public DLSet(File dbFile, DB database, boolean withObserver, boolean dynamicGUI) {
        super(dbFile, database, withObserver, dynamicGUI);
        this.peerMap = new PeerMap(this, this.database);
    }

    static DB makeDB(File dbFile) {
        dbFile.getParentFile().mkdirs();
        return DBMaker.newFileDB(dbFile)

               //// иначе кеширует блок и если в нем удалить трнзакции или еще что то выдаст тут же такой блок с пустыми полями
               ///// добавил dcSet.clearCache(); --
               ///.cacheDisable()

               // это чистит сама память если соталось 25% от кучи - так что она безопасная
               //.cacheHardRefEnable()
               //.cacheLRUEnable()
               ///.cacheSoftRefEnable()
               //.cacheWeakRefEnable()

               // количество точек в таблице которые хранятся в HashMap как в КЭШе
               .cacheSize(1000)

               .checksumEnable()
               .mmapFileEnableIfSupported() // ++
               /// ICREATOR
               .commitFileSyncDisable() // ++

               // если при записи на диск блока процессор сильно нагружается - то уменьшить это
               .freeSpaceReclaimQ(7) // не нагружать процессор для поиска свободного места в базе данных

               //.compressionEnable()

               .transactionDisable()
               .make();
    }
    public static DLSet reCreateDB() {

        //OPEN DB
        File dbFile = new File(Settings.getInstance().getLocalDir(), "data.dat");

        DB database = null;
        try {
            database = makeDB(dbFile);
        } catch (Throwable e) {
            logger.error(e.getMessage(), e);
            try {
                Files.walkFileTree(dbFile.toPath(), new SimpleFileVisitorForRecursiveFolderDeletion());
            } catch (Throwable e1) {
                logger.error(e.getMessage(), e1);
            }
            database = makeDB(dbFile);
        }

        return new DLSet(dbFile, database, true, true);

    }

    public PeerMap getPeerMap() {
        return this.peerMap;
    }

}