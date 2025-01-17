package org.erachain.core;

import com.google.common.primitives.Longs;
import org.erachain.controller.Controller;
import org.erachain.core.account.Account;
import org.erachain.core.account.PrivateKeyAccount;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.block.GenesisBlock;
import org.erachain.core.crypto.Base58;
import org.erachain.core.crypto.Crypto;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.core.transaction.ArbitraryTransaction;
import org.erachain.core.transaction.Transaction;
import org.erachain.datachain.BlocksHeadsMap;
import org.erachain.datachain.DCSet;
import org.erachain.network.Peer;
import org.erachain.ntp.NTP;
import org.erachain.settings.Settings;
import org.erachain.utils.FileUtils;
import org.erachain.utils.Pair;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;

/**
 * для обработки цепочки блоков. Запоминает в себе генесиз-блок и базу данных.
 * Поидее именно тут должен быть метод FORK а не в базе данных - и отпочковывание новой цепочки.
 * А блоки должны добавляться в цепочку а не в базу данных напрямую. blockChain.add(BLOCK)
 */
public class BlockChain {

    public static final int TESTS_VERS = 0; // not use TESTs - or a11 (as version)

    public static String MESS_FORGING = "forging";

    /**
     * Задает потолок цепочки
     */
    public static final int BLOCK_COUNT = 0;
    /**
     * DATABASE benchmark test. use start options:<br>
     * erachain.jar -pass=1 -seed=5:new:1 -nogui -opi -nodatawallet -nocalculated -hardwork=[0..10] -dbschain=[rocksdb|mapdb] <br>
     * сколько транзакции в блоке - если больше 0 то запускает тест на старте
     */
    public static final int TEST_DB = Settings.TEST_DB_MODE;
    // запрет сборки своих блоков в ТЕСТЕ
    public static final boolean STOP_GENERATE_BLOCKS = false;

    /**
     * для каждого счета по времени создания транзакции сохраняется ссылка на транзакцию,
     * что требует создания длинных ключей 20 + 8. Это используется при откатах для восстановления последего значения
     */
    public static final boolean NOT_STORE_REFFS_HISTORY = TEST_DB > 0;

    /**
     * для каждого счета сохраняется последнее время транзакции и потом проверяется на повторы.
     * 0 - все сохранять в базе, N - создать в памяти стек размером N для числа блоков последних
     * и при этом ограничиваем время жизни неподтвержденных на N блоков,
     * -1 - не проверяем вообще - возможно стоит уже запрет на транзакции с одного счета в одном блоке,
     * и при этом разрешены транзакции только по времени за 1 блок.
     * Вдобавок если != 0 то проверки на коллизию ключа (подписи) в TransactionFinalMapSigns не проверяется,
     * что ускоряет работу но воявляется вероятность колллизии - поэтому можно там увеличить длинну ключа если тут != 0
     * ! Вдобавок нужно понимать что если мы проверяем по времени трнзакции то 100% они уже будут иметь уникальные подписи
     * и проверять на уникальность их в Финал не нужно (если нет слишком большой обрезки ключа, see Transaction.KEY_COLLISION)
     */
    public static final int CHECK_DOUBLE_SPEND_DEEP = TEST_DB > 0 ? -1 : 0;

    /**
     * Число счетов для теста базы данных - чем больше тем болше нагрузка и сложнее считать.
     * Если меньше чем TEST_DB то улучшается скорость за счет схлопыания повторнных изменений балансов счетов.
     */
    public static PrivateKeyAccount[] TEST_DB_ACCOUNTS = TEST_DB == 0 ? null : new PrivateKeyAccount[1000];
    public static final boolean NOT_CHECK_SIGNS = TEST_DB > 0 && false;

    static public int CHECK_BUGS = TEST_DB > 0 ? 0 : Settings.CHECK_BUGS;

    static public int SKIP_BASE_ASSETS_AFTER = 1;

    /**
     * если задан - первое подключение к нему
     */
    public static final byte[] START_PEER = null; //new byte[]{(byte)138, (byte)197, (byte)135, (byte)122};

    /**
     * Защита от платежей с удостоверенного на анонима
     */
    public static boolean PERSON_SEND_PROTECT = false;

    /**
     * Подмена реального на чужой - для синхронизации из старой ветки
     */
    public static byte[] GENESIS_SIGNATURE;
    /**
     * Мой реальный блок - что защиты от подмены
     */
    public static byte[] GENESIS_SIGNATURE_TRUE;


    public static final boolean CLONE_MODE = Settings.getInstance().isCloneNet();
    public static final boolean DEMO_MODE = Settings.getInstance().isDemoNet();
    public static final boolean TEST_MODE = Settings.getInstance().isTestNet();
    public static final boolean MAIN_MODE = false;

    /**
     * Счет на который начисляются %% для Эрачейн с сайдченов. If = NULL - not used
     */
    public static Account CLONE_ROYALTY_ERACHAIN_ACCOUNT = null; //new Account("7RYEVPZg7wbu2bmz3tWnzrhPavjpyQ4tnp");

    /**
     * default = 30 sec
     */
    private static int BLOCKS_PERIOD = 30; // [sec]

    /**
     * set uo all balances ERA to 10000 and COMPU to 100
     */
    public static final boolean ERA_COMPU_ALL_UP = TEST_MODE || TEST_DB > 0 || Settings.ERA_COMPU_ALL_UP;

    public static int NETWORK_PORT = TEST_DB > 0 ? 9006 : TEST_MODE ? 9086 : CLONE_MODE ? 9076 : 0;

    public static final int DEFAULT_WEB_PORT = NETWORK_PORT + 1;
    public static final int DEFAULT_RPC_PORT = NETWORK_PORT + 2;

    public static final String DEFAULT_EXPLORER = "http://explorer.foil.network:" + DEFAULT_WEB_PORT;

    //public static final String TIME_ZONE = "GMT+3";
    //
    public static final boolean ROBINHOOD_USE = false;

    /**
     * Аноним может создавать персон и удостоверять счета
     */
    public static final boolean ANONIM_SERT_USE = TEST_MODE || BlockChain.ERA_COMPU_ALL_UP;

    public static final int MAX_ORPHAN = CLONE_MODE? 300 : 999990; // max orphan blocks in chain for 30 sec
    public static final int SYNCHRONIZE_PACKET = 300; // when synchronize - get blocks packet by transactions

    /**
     * минимальное расстояние для сборки блоков
     */
    public static final int REPEAT_WIN = DEMO_MODE ? 10 : TEST_MODE ? 5 : ERA_COMPU_ALL_UP ? 10 : CLONE_MODE ? 30 : 40;

    // RIGHTs
    public static final int GENESIS_ERA_TOTAL = 100000000;
    public static final int GENERAL_ERA_BALANCE = GENESIS_ERA_TOTAL / 100;
    public static final int MAJOR_ERA_BALANCE = 500000;
    public static final int MINOR_ERA_BALANCE = 10000;

    /**
     * степень от 2 блоков для усреднения ЦЕЛи победы
     */
    public static final int TARGET_COUNT_SHIFT = 10;
    public static final int TARGET_COUNT = 1 << TARGET_COUNT_SHIFT;
    public static final int BASE_TARGET = 10000;

    // SERTIFY
    // need RIGHTS for non PERSON account
    public static final BigDecimal MAJOR_ERA_BALANCE_BD = BigDecimal.valueOf(MAJOR_ERA_BALANCE);
    // need RIGHTS for PERSON account
    public static final BigDecimal MINOR_ERA_BALANCE_BD = BigDecimal.valueOf(MINOR_ERA_BALANCE);

    public static final int MIN_GENERATING_BALANCE = 50000;
    public static final BigDecimal MIN_GENERATING_BALANCE_BD = new BigDecimal(MIN_GENERATING_BALANCE);

    public static final int MIN_REGISTERING_BALANCE_OWN = 0;
    public static final BigDecimal MIN_REGISTERING_BALANCE_OWN_BD = new BigDecimal(MIN_REGISTERING_BALANCE_OWN);
    public static final int MIN_REGISTERING_BALANCE_USE = 500000;
    public static final BigDecimal MIN_REGISTERING_BALANCE_USE_BD = new BigDecimal(MIN_REGISTERING_BALANCE_USE);

    public static final int MIN_REGISTERING_BALANCE_10 = 10;
    public static final BigDecimal MIN_REGISTERING_BALANCE_10_BD = new BigDecimal(MIN_REGISTERING_BALANCE_10);

    public static final int MIN_REGISTERING_BALANCE_100 = 100;
    public static final BigDecimal MIN_REGISTERING_BALANCE_100_BD = new BigDecimal(MIN_REGISTERING_BALANCE_100);

    public static final int MIN_REGISTERING_BALANCE_1000 = 1000;
    public static final BigDecimal MIN_REGISTERING_BALANCE_1000_BD = new BigDecimal(MIN_REGISTERING_BALANCE_1000);

    public static final int WIN_BLOCK_BROADCAST_WAIT_MS = 10000; //
    // задержка на включение в блок для хорошей сортировки

    /**
     * проверить цепочку по силе у соседей. Если поставить меньше 2 то будет проверять каждый блок, что иногда плохо
     * Наверно оптимально 2-4 блока. Так же было замечено что если 2 узла всего тоони войдя в режим проверки
     * начинали поочереди откатывать свои цепочки до бесконечности - то есть нельзя чтобы каждый блок это смотрелось
     */
    public static final int CHECK_PEERS_WEIGHT_AFTER_BLOCKS = 3;
    // хранить неподтвержденные долше чем то время когда мы делаем обзор цепочки по силе
    public static final int ON_CONNECT_SEND_UNCONFIRMED_NEED_COUNT = 10;

    //public static final int GENERATING_MAX_BLOCK_TIME = 1000;
    public static final int MAX_BLOCK_SIZE_BYTES = 20 * (1 << 20);
    public static final int MAX_BLOCK_SIZE = MAX_BLOCK_SIZE_BYTES >> 1024;
    public static final int MAX_REC_DATA_BYTES = 20 * (1 << 20);

    // переопределим размеры по HARD
    static private final int MAX_BLOCK_SIZE_GEN_TEMP = MAX_BLOCK_SIZE_BYTES / 100 * (10 * Controller.HARD_WORK + 10);
    public static final int MAX_BLOCK_SIZE_BYTES_GEN = MAX_BLOCK_SIZE_BYTES;
    public static final int MAX_BLOCK_SIZE_GEN = MAX_BLOCK_SIZE;

    public static final int MAX_UNCONFIGMED_MAP_SIZE = MAX_BLOCK_SIZE_GEN << 2;
    public static final int ON_CONNECT_SEND_UNCONFIRMED_UNTIL = MAX_UNCONFIGMED_MAP_SIZE;

    public static final int GENESIS_WIN_VALUE = TEST_MODE ? 3000 : ERA_COMPU_ALL_UP ? 10000 : 22000;

    public static final String[] GENESIS_ADMINS = !ERA_COMPU_ALL_UP && CLONE_MODE ? new String[]{
            (((List) ((List) Settings.genesisJSON.get(2)).get(0)).get(0)).toString()} // GET from chainPROTOCOL.json
            : new String[]{"78JFPWVVAVP3WW7S8HPgSkt24QF2vsGiS5",
            "7B3gTXXKB226bxTxEHi8cJNfnjSbuuDoMC"};

    public static final int VERS_4_11 = 0;

    //public static final int ORDER_FEE_DOWN = VERS_4_11;
    public static final int HOLD_VALID_START = VERS_4_11;

    /**
     * Если задан то это режим синхронизации со старым протоколом - значит нам нельзя генерить блоки и транзакции
     * и вести себя тихо - ничего не посылать никуда - чтобы не забанили
     */
    public static int ALL_VALID_BEFORE = DEMO_MODE ? 66817 : TEST_MODE? 0 : 788640; // see in sidePROTOCOL.json as 'allValidBefore'
    public static final int WIN_VAL_ALL_VALID = 0;
    public static final int ALL_BALANCES_OK_TO = DEMO_MODE? 0 : CLONE_MODE? 137687 : 0;
    public static final int CANCEL_ORDERS_ALL_VALID = DEMO_MODE? 0 : CLONE_MODE? 53495 : 0;
    public static final int AUTO_CANCEL_ORDERS_FROM = TEST_DB > 0? 0 : DEMO_MODE ? 154957 : CLONE_MODE? 226892 : 0;
    /**
     * Включает обработку заявок на бирже по цене рассчитанной по остаткам<bR>
     * !!! ВНИМАНИЕ !!! нельзя изменять походу собранной цепочки - так как съедут цены и индекс не удалится у некоторых ордеров - цена о другая.
     * см issue https://lab.erachain.org/erachain/Erachain/-/issues/1322
     */
    public static final int LEFT_PRICE_HEIGHT = 0;
    /**
     * {@link LEFT_PRICE_HEIGHT} as SeqNo
     */

    //public static final long LEFT_PRICE_HEIGHT_SEQ = Transaction.makeDBRef(LEFT_PRICE_HEIGHT, 0);

    public static final int SKIP_INVALID_SIGN_BEFORE = 0;

    public static final int VERS_4_12 = VERS_4_11;

    public static final int VERS_30SEC = 0;

    // TODO поидее отрицательное тоже работать будет как надо
    public static final long VERS_30SEC_TIME = 0L;

    public static final int VERS_4_23_01 = 0;

    public static final int VERS_5_01_01 = 0;

    public static final int VERS_5_3 = 0;

    public static final int CLEAR_OLD_ORDERS_HEIGHT = TEST_DB > 0 || !CLONE_MODE ? DEMO_MODE ? 9224733 : Integer.MAX_VALUE : 465000;
    public static final int CLEAR_OLD_ORDERS_PERIOD = 10 * 24 * 60 * 2;

    /**
     * Новый уровень начальных номеров для всех сущностей
     */
    public static int START_KEY_UP = 0;
    public static int START_KEY_UP_ITEMS = 1 << 20;

    public static final int USE_NEW_ISSUE_FEE = 0;
    public static final int MINIMAL_ISSUE_FEE = 100000;
    public static final int MINIMAL_ISSUE_FEE_ACCOUNTING_ASSET = BlockChain.MINIMAL_ISSUE_FEE / 5;
    public static final int MINIMAL_ISSUE_FEE_IMPRINT = BlockChain.MINIMAL_ISSUE_FEE / 20;

    public static final BigDecimal FORGE_BONUS = new BigDecimal("0.00001245");

    /**
     * Включает новые права на выпуск персон и на удостоверение публичных ключей и увеличение Бонуса персоне
     */
    public static final int START_ISSUE_RIGHTS = VERS_5_01_01;

    public static final int START_ITEM_DUPLICATE = VERS_5_01_01;

    public static final int START_ASSET_UNIQUE = 0;

    public static final int DEFAULT_DURATION = 365 * 5; // 5 years

    public static final int DEVELOP_FORGING_START = 100;

    public HashSet<String> trustedPeers = new HashSet<>();

    public static final HashSet<Integer> validBlocks = new HashSet<>();

    /**
     * Записи которые удалены
     */
    public static final HashSet<Long> WIPED_RECORDS = new HashSet<>();

    /*
     *  SEE in concrete TRANSACTIONS
     * public static final byte[][] VALID_RECORDS = new byte[][]{
     * };
     */

    public static final byte[][] VALID_ADDRESSES = TEST_DB > 0? new byte[][]{} : new byte[][]{};

    public static final byte[][] DISCREDIR_ADDRESSES = TEST_DB > 0? new byte[][]{} : new byte[][]{};

    public static final int SKIP_VALID_SIGN_BEFORE = !CLONE_MODE ? 0 : 78500;
    public static final byte[][] VALID_SIGN = !CLONE_MODE? new byte[][]{} : new byte[][]{
            // MAIL with AMOUNT = 0
            Base58.decode("3MDpK4MgZKafbaDegMDMgotdbL6eYjHuLRa3Si9oGdRvPiYiPBADbqBjQWcAMK9d4ZhUxobcEc8T5tVQ9raptix9")
    };

    public static final byte[][] INVALID_SIGN = !CLONE_MODE? new byte[][]{} : new byte[][]{
            // MAIL with AMOUNT = 0
            Base58.decode("39TctLK2x4Lo5WJCPnG5aYrXYH7H2ADQEeDGL5vohXuC3JN8qVmcfFUstNQKXuia6BRmajXDWTQE8MDv6ZVpJeKm"),
            Base58.decode("5BUDrUHkELpiNCu1r8nBRKBxsyLPDrGi6a1BAHKdWxnMX5Dv2b2MVPsvVxsmckZqffLzegfR9smDVSzPXTG98ZvK"),
            Base58.decode("3pH9KKx5EZxKh6Q9Wn7ryctAi1p8BK3PxHym4wHD6PcoCV8kRTzaYr8vQsKJQ6NroMboKSF12kvK68VqUVKn3x7A")
    };

    public static final byte[][] VALID_BAL = TEST_DB > 0 ? new byte[][]{} : CLONE_MODE || TEST_MODE ? new byte[][]{} :
            new byte[][]{};

    // DEX precision
    /**
     * Если после исполнения торговой сделки остается остаток у ордера-цели и
     * цена для остатка отклонится больше чем на эту величину то либо скидываем остаток в эту сделку либо ему возвращаем остаток
     * Инициатор - "ПО РЫНКУ" - у него точность хуже делаем
     */
    final public static BigDecimal MAX_TRADE_DEVIATION_HI = new BigDecimal("0.005");
    final public static BigDecimal MAX_ORDER_DEVIATION = new BigDecimal("0.002");
    final public static BigDecimal MAX_ORDER_DEVIATION_LOW = new BigDecimal("0.0005");
    final public static BigDecimal COMPARE_TRADE_DEVIATION = new BigDecimal("1.000001");


    public static final int ITEM_POLL_FROM = TEST_DB > 0 ? 0 : !MAIN_MODE ? 0 : VERS_4_11;

    public static final int AMOUNT_SCALE_FROM = 0;
    public static final int AMOUNT_DEDAULT_SCALE = 8;
    public static final int FREEZE_FROM = CLONE_MODE? 547392 : 0;

    // только на них можно замороженные средства вернуть из списка FOUNDATION_ADDRESSES (там же и замароженные из-за утраты)
    public static final String[] TRUE_ADDRESSES = TEST_DB > 0 || TEST_MODE? new String[]{} :
            new String[]{
                    (((List) ((List) Settings.genesisJSON.get(2)).get(0)).get(0)).toString()}; // GET from chainPROTOCOL.json

    // CHAIN
    public static final int CONFIRMS_HARD = 3; // for reference by signature
    // MAX orphan CHAIN
    public static final int CONFIRMS_TRUE = MAX_ORPHAN; // for reference by ITEM_KEY
    //public static final int FEE_MIN_BYTES = 200;
    public static final int FEE_PER_BYTE_4_10 = 64;
    public static final int FEE_PER_BYTE = 100;
    public static final long FEE_KEY = AssetCls.FEE_KEY;
    public static final int FEE_SCALE = 8;
    public static final BigDecimal FEE_RATE = BigDecimal.valueOf(1, FEE_SCALE);
    //public static final BigDecimal MIN_FEE_IN_BLOCK_4_10 = BigDecimal.valueOf(FEE_PER_BYTE_4_10 * 8 * 128, FEE_SCALE);
    public static final BigDecimal MIN_FEE_IN_BLOCK_4_10 = BigDecimal.valueOf(50000, FEE_SCALE);
    public static final BigDecimal MIN_FEE_IN_BLOCK = BigDecimal.valueOf(FEE_PER_BYTE * 6 * 128, FEE_SCALE);
    public static final float FEE_POW_BASE = (float) 1.5;
    public static final int FEE_POW_MAX = 6;
    public static final int FINANCIAL_FEE_LEVEL = 100;
    public static final int ISSUE_MULT_FEE = 1 << 10;
    public static final int ISSUE_ASSET_MULT_FEE = 1 << 8;
    public static final int TEST_FEE_ORPHAN = 0; //157000;

    public static final int FEE_FOR_ANONIMOUSE = 33;
    //

    /**
     * add to all TX for fee
     */

    public static final int ADD_FEE_BYTES_FOR_COMMON_TX = 200;

    public static final int FREE_FEE_LENGTH = ADD_FEE_BYTES_FOR_COMMON_TX + (1 << 13);
    public static final int FREE_FEE_TO_SEQNO = 1;
    public static final int FREE_FEE_FROM_HEIGHT = 1;


    /**
     * FEE_KEY used here
     */
    public static final int ACTION_ROYALTY_START = 0; // if - 0 - OFF
    public static final int ACTION_ROYALTY_PERCENT = 8400; // x0.001
    public static final BigDecimal ACTION_ROYALTY_MIN = new BigDecimal("0.0001"); // x0.001
    public static final int ACTION_ROYALTY_MAX_DAYS = 30; // x0.001
    public static final BigDecimal ACTION_ROYALTY_TO_HOLD_ROYALTY_PERCENT = new BigDecimal("0.10"); // сколько добавляем к награде
    public static final boolean ACTION_ROYALTY_PERSONS_ONLY = false;
    public static final long ACTION_ROYALTY_ASSET_2 = AssetCls.BAL_KEY;

    /**
     * какие проценты при переводе каких активов - Ключ : коэффициент комиссии
     * Это Доход форжера за минусом Сгорания. Обязательно задать ASSET_TRANSFER_PERCENTAGE_MIN_TAB - иначе игнор %%
     */
    public static final HashMap<Long, BigDecimal> ASSET_TRANSFER_PERCENTAGE_TAB = new HashMap<>();
    /// default = 0.1%
    public static final BigDecimal ASSET_TRANSFER_PERCENTAGE_DEFAULT = new BigDecimal("0.0025");

    /**
     * минимальная комиссия для актива - абсолютное значение. Если не задано, то и процент комиссии не берется!
     */
    public static final HashMap<Long, BigDecimal> ASSET_TRANSFER_PERCENTAGE_MIN_TAB = new HashMap<>();
    /**
     * какие проценты сжигаем при переводе активов - Ключ : процент
     */
    public static final HashMap<Long, BigDecimal> ASSET_BURN_PERCENTAGE_TAB = new HashMap<>();
    /**
     * 1 - all to asset owner; 0 - all to block forger
     */
    public static final BigDecimal ASSET_BURN_PERCENTAGE_DEFAULT = new BigDecimal("0.5");

    public static final int HOLD_ROYALTY_PERIOD_DAYS = 0; // как часто начисляем? Если = 0 - на начислять
    public static final BigDecimal HOLD_ROYALTY_MIN = new BigDecimal("0.00000001"); // если меньше то распределение не делаем

    /**
     * минимальная ставка на бирже для актива - абсолютное значение!
     */
    public static final HashMap<Long, BigDecimal> EXCHANGE_MIN_AMOUNT_TAB = new HashMap<>();

    /**
     * По какому активу считаем дивиденды
     */
    public static final long HOLD_ROYALTY_ASSET = AssetCls.AS_KEY;

    /**
     * Если не задан то будет взят счет из Генесиз-блока
     * Если есть начисления бонусов по ROYALTY то надо его задать
     */
    public static PublicKeyAccount FEE_ASSET_EMITTER = CLONE_MODE ?
            new PublicKeyAccount(TEST_MODE ?
                    "AnEbFWkPi9tG9ZPiqVmB4yAri9HBb5D7xUXYhRR58ye6" :
                    "2Mh5ntMBForLYqwdjugRAGyzepR252ca6ChjQL6EXtYU")
            : null;


    /**
     * Включает реферальную систему
     */
    public static int REFERRAL_BONUS_FOR_PERSON = 0;

    /**
     * Multi-level Referral System. Levels for deep
     */
    public static final int FEE_INVITED_DEEP = TEST_DB > 0 ? 0 : 3;
    /**
     * Stop referals system on this person Number. Причем рефералка которая должна упасть этим персонам
     * (с номером ниже заданного) по сути просто сжигается - то есть идет дефляция.
     */
    public static final long BONUS_STOP_PERSON_KEY = TEST_DB > 0 || !MAIN_MODE ? 0 : 0L;

    /**
     * Постаянная награда за байт транзакции
     */
    public static final int BONUS_REFERAL = 200 * FEE_PER_BYTE;
    /**
     * Какую долю отдавать на уровень ниже - как степень двойки. 1 - половину, 2 - четверть, 3 - восьмую часть
     */
    public static final int FEE_INVITED_SHIFT_IN_LEVEL = 1;

    public static final Tuple2<Integer, byte[]> CHECKPOINT = new Tuple2<Integer, byte[]>(
            !MAIN_MODE ? 0 : 0,
            Base58.decode(
                    !MAIN_MODE ? "" // sign
                            : ""));

    // issue PERSON
    //public static final BigDecimal PERSON_MIN_ERA_BALANCE = BigDecimal.valueOf(10000000);
    public static HashSet<String> TRUSTED_ANONYMOUS = new HashSet<String>();
    public static HashSet<String> ANONYMASERS = new HashSet<String>();
    public static HashSet<String> FOUNDATION_ADDRESSES = new HashSet<String>();

    /**
     * Used for freeze some amount on period list. [Height, freezed amount],...<br>
     * example:<br>
     * .put("7DMJcs8kw7EXUSeEFfNwznRKRLHLrcXJFm", new int[][]{{225655, 150000}, {333655, 100000}});
     */
    public static HashMap<String, int[][]> FREEZED_BALANCES = new HashMap<String, int[][]>();
    public static HashSet<String> FREEZED_FORGING = new HashSet<String>();

    public static HashMap<String, Tuple3<Long, Long, byte[]>> NOVA_ASSETS = new HashMap<String, Tuple3<Long, Long, byte[]>>();
    public static HashMap<String, Tuple3<Long, Long, byte[]>> NOVA_PERSONS = new HashMap<String, Tuple3<Long, Long, byte[]>>();

    public static HashMap<String, String> LOCKED__ADDRESSES = new HashMap<String, String>();
    public static HashMap<String, Tuple3<String, Integer, Integer>> LOCKED__ADDRESSES_PERIOD = new HashMap<String, Tuple3<String, Integer, Integer>>();
    public static HashMap<Long, PublicKeyAccount> ASSET_OWNERS = new HashMap<Long, PublicKeyAccount>();
    static Logger LOGGER = LoggerFactory.getLogger(BlockChain.class.getSimpleName());
    private GenesisBlock genesisBlock;
    private long genesisTimestamp;
    private Block waitWinBuffer;

    public static long[] startKeys = new long[10];

    //private int target = 0;
    //private byte[] lastBlockSignature;
    //private Tuple2<Integer, Long> HWeight;

    public long transactionWinnedTimingAverage;
    public long transactionWinnedTimingCounter;

    public long transactionValidateTimingAverage;
    public long transactionValidateTimingCounter;

    /**
     * Учитывает время очистки очереди неподтвержденных трнзакций и сброса на жесткий диск их памяти
     * И поэтому это число хуже чем в Логе по подсчету обработки транзакций в блоке
     */
    public long transactionProcessTimingAverage;
    public long transactionProcessTimingCounter;

    //private DLSet dcSet;

    // dcSet_in = db() - for test
    public BlockChain(DCSet dcSet_in) throws Exception {

        trustedPeers.addAll(Settings.getInstance().getTrustedPeers());
        if (FEE_ASSET_EMITTER == null) {
            // для учета эмиссии COMPU и других
            FEE_ASSET_EMITTER = GenesisBlock.CREATOR;
        }

        /// FOR SIDE and DEMO load PARAMS
        File file = new File(Settings.CLONE_OR_SIDE.toLowerCase() + "PROTOCOL.json");
        if (file.exists()) {
            LOGGER.info(Settings.CLONE_OR_SIDE.toLowerCase() + "PROTOCOL.json USED");
            // START SIDE CHAIN
            JSONObject chainParams = FileUtils.readCommentedJSONObject(file.getPath());
            if (chainParams == null) {
                throw new Exception("Wrong JSON or not UTF-8 encode in " + file.getName());
            }

            if (chainParams.containsKey("assets")) {
                JSONArray items = (JSONArray) chainParams.get("assets");
                for (Object item : items) {
                    JSONArray json = (JSONArray) item;
                    NOVA_ASSETS.put(json.get(1).toString(),
                            new Tuple3<>((Long) json.get(0), (Long) json.get(2),
                                    ERA_COMPU_ALL_UP? GenesisBlock.CREATOR.getShortAddressBytes()
                                     : Crypto.getInstance().getShortBytesFromAddress(json.get(3).toString())));
                }
            }

            if (chainParams.containsKey("persons")) {
                JSONArray items = (JSONArray) chainParams.get("persons");
                for (Object item : items) {
                    JSONArray json = (JSONArray) item;
                    NOVA_PERSONS.put(json.get(1).toString(),
                            new Tuple3<>((Long) json.get(0), (Long) json.get(2),
                                    ERA_COMPU_ALL_UP? GenesisBlock.CREATOR.getShortAddressBytes()
                                     : Crypto.getInstance().getShortBytesFromAddress(json.get(3).toString())));
                }
            }

            if (chainParams.containsKey("blockPeriod")) {
                BLOCKS_PERIOD = Integer.parseInt(chainParams.get("blockPeriod").toString());
                if (BLOCKS_PERIOD < 3) {
                    BLOCKS_PERIOD = 3;
                }
            }

            if (chainParams.containsKey("peersURL")) {
                Settings.peersURL = chainParams.get("peersURL").toString();
            }

            if (chainParams.containsKey(Settings.CLONE_OR_SIDE.toLowerCase() + "License")) {
                Settings.cloneLicense = chainParams.get(Settings.CLONE_OR_SIDE.toLowerCase() + "License").toString();
            }

            if (chainParams.containsKey("startKey")) {
                JSONObject startKeysJson = (JSONObject) chainParams.get("startKey");
                for (Object key : startKeysJson.keySet()) {
                    startKeys[ItemCls.getItemTypeByName((String) key)] = (long) startKeysJson.get(key);
                }
            }

            if (chainParams.containsKey("explorer")) {
                Settings.getInstance().explorerURL = chainParams.get("explorer").toString();
            }

            if (chainParams.containsKey("referalsOn")) {
                REFERRAL_BONUS_FOR_PERSON = (Boolean) chainParams.get("referalsOn") ? 0 : Integer.MAX_VALUE;
            }

            if (chainParams.containsKey("allValidBefore")) {
                ALL_VALID_BEFORE = Integer.parseInt(chainParams.get("allValidBefore").toString());
            }

            if (chainParams.containsKey("protectSendToAnonymous")) {
                PERSON_SEND_PROTECT = (Boolean) chainParams.get("protectSendToAnonymous");
            }

            if (chainParams.containsKey("genesisSignature")) {
                // нужно для синхронизации из другой ветки например если потокол сильно поменялся
                // используется совместно с ключем - ALL_VALID_BEFORE
                JSONArray array = (JSONArray) chainParams.get("genesisSignature");
                GENESIS_SIGNATURE = Base58.decode(array.get(0).toString());
                GENESIS_SIGNATURE_TRUE = Base58.decode(array.get(1).toString());
            }
        }

        if (CLONE_MODE || TEST_MODE) {

            if (false // for TEST_MODE see below
            ) {
                // лишком большая волатильность - и минимально граница - будет постоянно меняться?
                // BTC new BigDecimal("0.0025")
                ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(12L, new BigDecimal("0.000002"));

                ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(18L, new BigDecimal("0.5"));
                ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(20L, new BigDecimal("0.001"));
                ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(22L, new BigDecimal("0.001"));

                // GOLD
                ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(21L, new BigDecimal("0.00005"));

            }

            // FIAT CURRENCIES
            // UAH
            ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(82L, new BigDecimal("0.25"));
            // KZT
            ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(83L, new BigDecimal("0.25"));
            // KFS
            ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(84L, new BigDecimal("0.25"));
            // BYN
            ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(85L, new BigDecimal("0.25"));
            // TRY
            ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(86L, new BigDecimal("0.25"));
            // SGN
            ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(87L, new BigDecimal("0.25"));
            // AUD
            ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(88L, new BigDecimal("0.25"));
            // CFH
            ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(89L, new BigDecimal("0.25"));
            // GBP
            ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(90L, new BigDecimal("0.25"));
            // JPY
            ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(91L, new BigDecimal("0.25"));
            // RUB
            ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(92L, new BigDecimal("0.25"));
            // CNY
            ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(93L, new BigDecimal("1.5"));
            // EUR
            ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(94L, new BigDecimal("0.25"));
            // USD
            ASSET_TRANSFER_PERCENTAGE_MIN_TAB.put(85L, new BigDecimal("0.25"));

            // процент сжигания - если тут не задано то берется 1/2
            //ASSET_BURN_PERCENTAGE.put(AssetCls.ERA_KEY, BigDecimal.ZERO);
            //ASSET_BURN_PERCENTAGE.put(AssetCls.AS_KEY, BigDecimal.ZERO);

            if (CLONE_MODE) {
                // FREEZED_FORGING.add("");
                FOUNDATION_ADDRESSES.add("7JdaW3v6Jt89XibGpCB2q3zm4d7njvSBBM");

                FOUNDATION_ADDRESSES.add("7PP46kVHwqkQCqEUhBjSVevQsjT7XgrbLj");
                FOUNDATION_ADDRESSES.add("76uM1bjTqAV6SrZN5HMMKGrRCEognHWEWo");
                FOUNDATION_ADDRESSES.add("7GAhKrTv7k65TEbGHnVFN8ypgGJKhGg6fn");
                FOUNDATION_ADDRESSES.add("7KZy6rg87JyUCixGxeZs6obYkrn58bCMcV");
                FOUNDATION_ADDRESSES.add("7H7zPwte3qL5CkdnkvsfKjDeH9UJFHeutF");
                FOUNDATION_ADDRESSES.add("738s3y3M5QsdvhvVbGaY8jfzSoYyi2c25Q");
                FOUNDATION_ADDRESSES.add("7CP4eso63ZTG9uUeutBFs1N7Q8bP3RG1bq");
                FOUNDATION_ADDRESSES.add("7NmCBXnKGpb5ha5gBPmRAi8pUtVCFNhmSc");
                FOUNDATION_ADDRESSES.add("7EMYCpwS183oMDrTbekf2zD74N2AW4KoMy");
                FOUNDATION_ADDRESSES.add("7AATKxfZrFdT7Ksidvrvq6rJYP3LRizpzE");
                FOUNDATION_ADDRESSES.add("7Eh6raYUhwGKdV3BntvJZiSB5dvYGNNUqC");
                FOUNDATION_ADDRESSES.add("74zaQ65ZuDyP1HRzqbd1PVog3WbCLeVMN2");
                FOUNDATION_ADDRESSES.add("7CmtTjwgwUJk5wgyKfJXhaddQkXyh2UJA2");
                FOUNDATION_ADDRESSES.add("7NrwisgWgccbHJ7vdWyGq7ss4mKLDRpSQT");
                FOUNDATION_ADDRESSES.add("7BYWr39hcXtpfM3HgzvfQXW3mVwfCnoNNT");

                FOUNDATION_ADDRESSES.add("7EZ2wbj3Tbv7DQy6vmdvqno2Z5h97vwc4b");
                FOUNDATION_ADDRESSES.add("7GSCayNRM7PP1sqzZrsS7VePodTpdeVE1Q");

            }
        } else {
            // MAIN MODE

        }

        //CREATE GENESIS BLOCK
        genesisBlock = new GenesisBlock();
        genesisTimestamp = genesisBlock.getTimestamp();

        DCSet dcSet = dcSet_in;
        if (dcSet == null) {
            dcSet = DCSet.getInstance();
        }

        if (!MAIN_MODE) {
            LOGGER.info(genesisBlock.getTestNetInfo());
        }

        int height = dcSet.getBlockSignsMap().size();
        if (height <= 0)
        // process genesis block
        {
            if (dcSet_in == null && dcSet.getBlockMap().getLastBlockSignature() != null) {
                LOGGER.info("reCreateDB Database...");

                try {
                    dcSet.close();
                    dcSet = Controller.getInstance().reCreateDC(Controller.getInstance().inMemoryDC);
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    Controller.getInstance().stopAndExit(101);
                }
            }

            //PROCESS
            genesisBlock.process(dcSet, false);

        } else {

            // TRY compare GENESIS BLOCK SIGNATURE
            if (!Arrays.equals(dcSet.getBlockMap().getAndProcess(1).getSignature(),
                    genesisBlock.getSignature())) {

                throw new Exception("Wrong GENESIS BLOCK in datachain");
            }

        }

        ERA_ASSET = dcSet.getItemAssetMap().get(AssetCls.ERA_KEY);
        FEE_ASSET = dcSet.getItemAssetMap().get(AssetCls.FEE_KEY);

    }

    //
    public static int getHeight(DCSet dcSet) {
        //GET LAST BLOCK
        return dcSet.getBlockSignsMap().size();
    }

    public static int GENERATING_MIN_BLOCK_TIME(int height) {
        return !TEST_MODE? BLOCKS_PERIOD : 5;
    }

    public static boolean isWiped(byte[] signature) {
        return WIPED_RECORDS.contains(Longs.fromByteArray(signature));
    }

    public static int GENERATING_MIN_BLOCK_TIME_MS(int height) {
        return GENERATING_MIN_BLOCK_TIME(height) * 1000;
    }

    public static int GENERATING_MIN_BLOCK_TIME_MS(long timestamp) {
        int height = timestamp < VERS_30SEC_TIME? 1 : VERS_30SEC + 1;
        return GENERATING_MIN_BLOCK_TIME(height) * 1000;
    }

    public static int FLUSH_TIMEPOINT(int height) {
        return GENERATING_MIN_BLOCK_TIME_MS(height) - (GENERATING_MIN_BLOCK_TIME_MS(height) >> 3);
    }

    public static int UNCONFIRMED_SORT_WAIT_MS(int height) {
        if (VERS_30SEC > 0 && height <= VERS_30SEC) {
            return -GENERATING_MIN_BLOCK_TIME_MS(height);
        }
        return 0;
    }

    public static AssetCls ERA_ASSET;
    public static AssetCls FEE_ASSET;

    /**
     * Если счет админа и с него можно до бесконечности брать
     *
     * @param height
     * @param account
     * @return
     */
    public static boolean isFeeEnough(int height, Account account) {
        if (true) {
            // for MAIN NET
            return false;
        }

        // FOR CLONES
        if (FEE_ASSET == null)
            FEE_ASSET = Controller.getInstance().getDCSet().getItemAssetMap().get(AssetCls.FEE_KEY);

        return FEE_ASSET.getMaker().equals(account);
    }


    public static int BLOCKS_PER_DAY(int height) {
        return 24 * 60 * 60 / GENERATING_MIN_BLOCK_TIME(height); // 300 PER DAY
    }

    public static int WIN_TIMEPOINT(int height) {
        return GENERATING_MIN_BLOCK_TIME_MS(height) >> 2;
    }

    public static int UNCONFIRMED_DEADTIME_MS(long timestamp) {
        int height = timestamp < VERS_30SEC_TIME ? 1 : VERS_30SEC + 1;
        if (TEST_DB > 0) {
            return GENERATING_MIN_BLOCK_TIME_MS(height);
        } else {
            return TEST_MODE ? GENERATING_MIN_BLOCK_TIME_MS(height) << 4 : GENERATING_MIN_BLOCK_TIME_MS(height) << 3;
        }
    }

    public static int VALID_PERSON_REG_ERA(Transaction transaction, int height, BigDecimal totalERA, BigDecimal totalLIA) {

        if (!CLONE_MODE)
            return 0;

        if (MIN_REGISTERING_BALANCE_OWN > 0 && totalERA.compareTo(MIN_REGISTERING_BALANCE_OWN_BD) < 0) {
            transaction.setErrorValue("balance in OWN less then " + MIN_REGISTERING_BALANCE_OWN);
            return Transaction.NOT_ENOUGH_ERA_OWN;
        }
        if (MIN_REGISTERING_BALANCE_USE > 0 && totalERA.compareTo(MIN_REGISTERING_BALANCE_USE_BD) < 0) {
            transaction.setErrorValue("balance in USE less then " + MIN_REGISTERING_BALANCE_USE);
            return Transaction.NOT_ENOUGH_ERA_USE;
        }

        return 0;

    }

    public static int VALID_PERSON_CERT_ERA(Transaction transaction, int height, BigDecimal totalERA, BigDecimal totalLIA) {

        if (!CLONE_MODE)
            return 0;

        if (MIN_REGISTERING_BALANCE_OWN > 0 && totalERA.compareTo(MIN_REGISTERING_BALANCE_OWN_BD) < 0) {
            transaction.setErrorValue("balance in OWN less then " + MIN_REGISTERING_BALANCE_OWN);
            return Transaction.NOT_ENOUGH_ERA_OWN;
        }
        if (MIN_REGISTERING_BALANCE_USE > 0 && totalERA.compareTo(MIN_REGISTERING_BALANCE_USE_BD) < 0) {
            transaction.setErrorValue("balance in USE less then " + MIN_REGISTERING_BALANCE_USE);
            return Transaction.NOT_ENOUGH_ERA_USE;
        }

        return 0;

    }

    public static BigDecimal feeBG(long feeLong) {
        return BigDecimal.valueOf(feeLong * FEE_PER_BYTE, FEE_SCALE);
    }

    public static BigDecimal ASSET_TRANSFER_PERCENTAGE_MIN(int height, Long assetKey) {
        return ASSET_TRANSFER_PERCENTAGE_MIN_TAB.get(assetKey);
    }

    public static BigDecimal ASSET_TRANSFER_PERCENTAGE(int height, Long assetKey) {
        BigDecimal percentAsset = ASSET_TRANSFER_PERCENTAGE_TAB.get(assetKey);
        if (percentAsset == null) {
            percentAsset = ASSET_TRANSFER_PERCENTAGE_DEFAULT;
        }

        return percentAsset;

    }

    public static BigDecimal ASSET_BURN_PERCENTAGE(int height, Long assetKey) {
        if (ASSET_BURN_PERCENTAGE_TAB.isEmpty()
                || !ASSET_BURN_PERCENTAGE_TAB.containsKey(assetKey))
            return ASSET_BURN_PERCENTAGE_DEFAULT;

        return ASSET_BURN_PERCENTAGE_TAB.get(assetKey);

    }

    public static BigDecimal BONUS_FOR_PERSON(int height) {
        return feeBG(10000);
    }

    public static boolean REFERAL_BONUS_FOR_PERSON(int height) {
        return height > REFERRAL_BONUS_FOR_PERSON;
    }

    public static int getCheckPoint(DCSet dcSet, boolean useDynamic) {

        int heightCheckPoint = 1;
        if (CHECKPOINT.a > 1) {
            Integer item = dcSet.getBlockSignsMap().get(CHECKPOINT.b);
            if (item == null || item < 1)
                return 1;

            heightCheckPoint = item;
        }

        if (!useDynamic)
            return heightCheckPoint;

        int dynamicCheckPoint = getHeight(dcSet) - BlockChain.MAX_ORPHAN;

        if (dynamicCheckPoint > heightCheckPoint)
            return dynamicCheckPoint;
        return heightCheckPoint;
    }

    public Long getTimestampByDBRef(Long dbRef) {
        Tuple2<Integer, Integer> key = Transaction.parseDBRef(dbRef);
        return getTimestamp(key.a) + key.b;
    }

    public byte[] getMyHardCheckPointSign() {
        if (CHECKPOINT.a > 1) {
            return CHECKPOINT.b;
        } else {
            return genesisBlock.getSignature();
        }
    }

    public int getMyHardCheckPointHeight() {
        if (CHECKPOINT.a > 1) {
            return CHECKPOINT.a;
        } else {
            return 1;
        }
    }

    /**
     * @param peerHeight  Long чтобы преобразования JSON не делать лоя Нуля
     * @param peerSignStr
     * @return
     */
    public boolean validateHardCheckPointPeerSign(Long peerHeight, String peerSignStr) {

        byte[] peerSign = Base58.decode(peerSignStr);
        if (Arrays.equals(getMyHardCheckPointSign(), peerSign))
            return true;

        DCSet dcSet = DCSet.getInstance();
        if (dcSet.getBlockSignsMap().contains(peerSign))
            return true;

        if (peerHeight == null || // OLD version
                peerHeight > getHeight(dcSet))
            return true;

        return false;
    }

    public boolean isPeerTrusted(Peer peer) {
        return trustedPeers.contains(peer.getAddress().getHostAddress());
    }

    public static boolean isNovaAsset(Long key) {
        Iterator<Tuple3<Long, Long, byte[]>> iterator = NOVA_ASSETS.values().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().a.equals(key))
                return true;
        }
        return false;
    }

    public static boolean isNovaPerson(Long key) {
        Iterator<Tuple3<Long, Long, byte[]>> iterator = NOVA_PERSONS.values().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().a.equals(key))
                return true;
        }
        return false;
    }

    /**
     * Calculate Target (Average Win Value for 1024 last blocks) for this block
     *
     * @param height         - height of blockchain
     * @param targetPrevious - previous Target
     * @param winValue       - current Win Value
     * @return
     */
    public static long calcTarget(int height, long targetPrevious, long winValue) {

        if (height < TARGET_COUNT) {
            return targetPrevious - (targetPrevious / height) + (winValue / height);
        }

        // CUT GROWTH
        long cut1 = targetPrevious + (targetPrevious >> 1);
        if (height > TARGET_COUNT && winValue > cut1) {
            winValue = cut1;
        }

        // better accuracy
        long target = (((targetPrevious << TARGET_COUNT_SHIFT) - targetPrevious) + winValue) >> TARGET_COUNT_SHIFT;
        if (target < 1000 && (ERA_COMPU_ALL_UP))
            target = 1000;

        return target;
    }

    // GET MIN TARGET
    // TODO GENESIS_CHAIN
    // SEE core.block.Block.calcWinValue(DLSet, Account, int, int)
    public static int getTargetedMin(int height) {
        int base;
        if (height < BlockChain.REPEAT_WIN)
            // FOR not repeated WINS - not need check BASE_TARGET
            base = BlockChain.BASE_TARGET >> 4; // ONLY UP
        else if (ERA_COMPU_ALL_UP || BlockChain.TEST_MODE)
            base = 1;
        else
            base = BlockChain.BASE_TARGET >> 1 + 100;

        return base;

    }

    public static int calcWinValueTargeted(long win_value, long target) {

        if (target == 0) {
            // in forked chain in may be = 0
            return -1;
        }

        int result = (int) (BlockChain.BASE_TARGET * win_value / target);
        if (result < 1 || result > BlockChain.BASE_TARGET * 10)
            // fix overload
            return BlockChain.BASE_TARGET * 10;
        return result;

    }

    /**
     * calc WIN_VALUE for ACCOUNT in HEIGHT
     *
     * @param dcSet
     * @param creator                 account of block creator
     * @param height                  current blockchain height
     * @param forgingBalance          current forging Balance on account
     * @param previousForgingPoint_in
     * @return (long) Win Value
     */
    public static long calcWinValue(DCSet dcSet, Account creator, int height, int forgingBalance,
                                    Tuple3<Integer, Integer, Integer> previousForgingPoint_in) {

        if (forgingBalance < MIN_GENERATING_BALANCE && height > ALL_BALANCES_OK_TO) {
            return 0L;
        }

        Tuple3<Integer, Integer, Integer> previousForgingPoint;
        if (previousForgingPoint_in == null) {
            previousForgingPoint = creator.getForgingData(dcSet, height);
            if (previousForgingPoint == null) {
                previousForgingPoint = creator.getLastForgingData(dcSet);
            }
        } else {
            previousForgingPoint = previousForgingPoint_in;
        }

        if (ERA_COMPU_ALL_UP) {
            if (previousForgingPoint == null) {
                // так как неизвестно когда блок первый со счета соберется - задаем постоянный отступ у ДЕВЕЛОП
                previousForgingPoint = new Tuple3<Integer, Integer, Integer>(height - DEVELOP_FORGING_START, forgingBalance, 0);
            }
        } else {
            if (previousForgingPoint == null)
                return 0L;
        }

        int previousForgingHeight = previousForgingPoint.a;

        // OWN + RENT balance - in USE
        if (forgingBalance > previousForgingPoint.b) {
            forgingBalance = previousForgingPoint.b;
        }

        if (forgingBalance < BlockChain.MIN_GENERATING_BALANCE) {
            if (height > ALL_BALANCES_OK_TO)
                return 0L;
            forgingBalance = BlockChain.MIN_GENERATING_BALANCE;
        }

        int difference = height - previousForgingHeight;

        int repeatsMin;

        if (height <= BlockChain.REPEAT_WIN) {
            repeatsMin = height - 2;
        } else {
            repeatsMin = GENESIS_ERA_TOTAL / forgingBalance;
            repeatsMin = (repeatsMin >> 2);

            if (true || repeatsMin < REPEAT_WIN) {
                repeatsMin = REPEAT_WIN;
            }
        }

        if (difference < repeatsMin) {
            return difference - repeatsMin;
        }

        long win_value;


        forgingBalance /= 1000;

        if (difference > 1)
            win_value = (long) forgingBalance * (long) difference;
        else
            win_value = forgingBalance;

        return win_value;

    }

    /**
     * Calculate targeted Win Value and cut by BASE
     * @param dcSet dataChainSet
     * @param height blockchain height
     * @param win_value win value
     * @param target average win value for blockchain by 1024 last blocks
     * @return targeted Win Value and cut by BASE
     */
    public static int calcWinValueTargetedBase(DCSet dcSet, int height, long win_value, long target) {

        if (win_value < 1)
            return (int) win_value;

        int base = BlockChain.getTargetedMin(height);
        int targetedWinValue = calcWinValueTargeted(win_value, target);
        if (height > WIN_VAL_ALL_VALID && height > ALL_VALID_BEFORE
                && !ERA_COMPU_ALL_UP && !BlockChain.TEST_MODE
                && base > targetedWinValue) {
            return -targetedWinValue;
        }

        return targetedWinValue;

    }

    public GenesisBlock getGenesisBlock() {
        return this.genesisBlock;
    }

    public long getGenesisTimestamp() {
        return this.genesisTimestamp;
    }

    public long getTimestamp(int height) {
        if (VERS_30SEC == 0 || height <= VERS_30SEC) {
            return this.genesisTimestamp + (long) height * (long) GENERATING_MIN_BLOCK_TIME_MS(height);
        }

        return this.genesisTimestamp + (!MAIN_MODE ? 0L : 16667L)
                + (long) VERS_30SEC * (long) GENERATING_MIN_BLOCK_TIME_MS(VERS_30SEC)
                + (long) (height - VERS_30SEC) * (long) GENERATING_MIN_BLOCK_TIME_MS(height);

    }

    public long getTimestamp(DCSet dcSet) {
        int height = getHeight(dcSet);
        return getTimestamp(height);
    }

    public int getHeightOnTimestampMS(long timestamp) {
        long diff = timestamp - genesisTimestamp;
        int height = (int) (diff / (long) GENERATING_MIN_BLOCK_TIME_MS(1));
        if (height <= VERS_30SEC)
            return height;

        // новый шаг между блоками
        diff -= (long) GENERATING_MIN_BLOCK_TIME_MS(1) * (long) VERS_30SEC;

        height = (int) (diff / (long) GENERATING_MIN_BLOCK_TIME_MS(VERS_30SEC + 1));

        return VERS_30SEC + height;

    }

    public int compareNewWin(DCSet dcSet, Block block) {
        return this.waitWinBuffer == null ? -1 : this.waitWinBuffer.compareWin(block);
    }

    public boolean isEmptyWaitWinBuffer() {
        return this.waitWinBuffer == null;
    }

    // BUFFER of BLOCK for WIN solving
    public Block getWaitWinBuffer() {
        return this.waitWinBuffer;
    }

    public void clearWaitWinBuffer() {
        if (this.waitWinBuffer != null) {
            synchronized (waitWinBuffer) {
                waitWinBuffer.close();
                this.waitWinBuffer = null;
            }
        }
    }

    public Block popWaitWinBuffer() {
        if (waitWinBuffer == null)
            return null;

        synchronized (waitWinBuffer) {
            Block block = this.waitWinBuffer;
            this.waitWinBuffer = null;
            return block;
        }
    }

    /**
     * SOLVE WON BLOCK
     * 0 - unchanged;
     * 1 - changed, need broadcasting;
     */
    public synchronized boolean setWaitWinBuffer(DCSet dcSet, Block block, Peer peer) {

        LOGGER.info("try set new winBlock: " + block.toString());

        byte[] lastSignature = dcSet.getBlockMap().getLastBlockSignature();
        if (!Arrays.equals(lastSignature, block.getReference())) {
            block.close();
            LOGGER.info("new winBlock from FORK!");
            return false;
        }

        if (this.waitWinBuffer != null) {
            if (block.equals(waitWinBuffer)) {
                block.close();
                LOGGER.info("new winBlock is SAME!");
                return true;
            }
            if (block.compareWin(waitWinBuffer) <= 0) {
                block.close();
                LOGGER.info("new winBlock is POOR!");
                return false;
            }

        }

        // создаем в памяти базу - так как она на 1 блок только нужна - а значит много памяти не возьмет
        int noValid = 99999;
        DCSet fork = dcSet.fork(DCSet.makeDBinMemory(), "setWaitWinBuffer");

        try {
            noValid = block.isValid(fork,
                    // если вторичные индексы нужны то нельзя быстрый просчет - иначе вторичные при сиве из форка не создадутся
                    Controller.getInstance().onlyProtocolIndexing
            );
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            if (BlockChain.CHECK_BUGS > 9) {
                // тут нельзя выходить так как просто битым блоком смогут все ноды убить при атаке
                Controller.getInstance().stopAndExit(1104);
            } else {
                noValid = 999;
                peer.ban(30, "Block ERROR: " + e.getMessage());
            }
        } catch (Throwable e) {
            LOGGER.error(e.getMessage(), e);
            // тут ошибка памяти прилетит - можно выходить
            Controller.getInstance().stopAndExit(1105);
        }

        // FULL VALIDATE because before was only HEAD validating
        if (noValid > 0) {

            if (peer != null) {
                if (noValid > Block.INVALID_REFERENCE) {
                    peer.ban(10, "invalid block");
                } else if (noValid > Block.INVALID_BRANCH) {
                    peer.ban(0, "invalid block reference");
                } else {
                    // вообще не баним - это просто не успел блок встать в цепочку а мы ее уже обновили
                    LOGGER.info("new winBlock is LATE");
                }
            } else {
                LOGGER.error("MY WinBlock is INVALID! ignore...");
            }

            // сперва в блоке транзакции освобождаем и ссылку базу
            block.close();
            // теперь сам фор базы закрываем - освободим память и чистильщиков кеша внутренние у MapDB
            fork.close();
            return false;
        }

        // если вторичные индексы нужны то нельзя быстрый просчет - иначе вторичные при сиве из форка не создадутся
        if (Controller.getInstance().onlyProtocolIndexing) {
            // иначе запоним форкнутую СУБД чтобы потом быстро слить
            block.setValidatedForkDB(fork);
        } else {
            fork.close();
        }

        // set and close OLD
        setWaitWinBufferUnchecked(block);

        LOGGER.info("new winBlock setted!!!" + block.toString());
        return true;

    }

    /**
     * если идет синхронизация то записываем без проверки
     *
     * @param block
     */
    public synchronized void setWaitWinBufferUnchecked(Block block) {

        // тут же мы без проверки должны вносить любой блок
        // иначе просто прилетевший блок в момент синхронизации не будет принят
        if (this.waitWinBuffer != null) {
            synchronized (waitWinBuffer) {
                waitWinBuffer.close();
                waitWinBuffer = null; // поможем сборщику мусора явно
                this.waitWinBuffer = block;
            }
        } else {
            this.waitWinBuffer = block;
        }
    }

    public Tuple2<Integer, Long> getHWeightFull(DCSet dcSet) {
        return new Tuple2<Integer, Long>(dcSet.getBlocksHeadsMap().size(),
                dcSet.getBlocksHeadsMap().getFullWeight());
    }

    public long getFullWeight(DCSet dcSet) {

        return dcSet.getBlocksHeadsMap().getFullWeight();
    }

    public List<byte[]> getSignatures(DCSet dcSet, byte[] parentSignature) {

        List<byte[]> headers = new ArrayList<byte[]>();

        //CHECK IF BLOCK EXISTS
        Integer height = dcSet.getBlockSignsMap().get(parentSignature);
        if (height != null && height > 0) {

            int packet;
            if (Arrays.equals(parentSignature, this.genesisBlock.getSignature())
                    || Arrays.equals(parentSignature, CHECKPOINT.b)) {
                packet = 3;
            } else {
                packet = SYNCHRONIZE_PACKET;
            }
            BlocksHeadsMap map = dcSet.getBlocksHeadsMap();
            int counter = 0;
            do {
                headers.add(parentSignature);
                if (map.contains(++height))
                    parentSignature = map.get(height).signature;
                else
                    break;
            } while (parentSignature != null && counter++ < packet);
        } else if (Arrays.equals(parentSignature, this.CHECKPOINT.b)) {
            headers.add(parentSignature);
        }

        return headers;
    }

    public Block getBlock(DCSet dcSet, byte[] header) {

        return dcSet.getBlockSignsMap().getBlock(header);
    }

    public Block getBlock(DCSet dcSet, int height) {

        return dcSet.getBlockMap().getAndProcess(height);
    }

    /**
     * Среднее время обработки транзакции при прилете блока из сети. Блок считается как одна транзакция
     *
     * @return
     */
    public void updateTXWinnedTimingAverage(long processTiming, int counter) {
        // при переполнении может быть минус
        // в миеросекундах подсчет делаем
        processTiming = processTiming / 1000 / (Controller.BLOCK_AS_TX_COUNT + counter);
        if (transactionWinnedTimingCounter < 1 << 5) {
            transactionWinnedTimingCounter++;
            transactionWinnedTimingAverage = ((transactionWinnedTimingAverage * transactionWinnedTimingCounter)
                    + processTiming - transactionWinnedTimingAverage) / transactionWinnedTimingCounter;
        } else
            transactionWinnedTimingAverage = ((transactionWinnedTimingAverage << 5)
                    + processTiming - transactionWinnedTimingAverage) >> 5;
    }

    private long pointValidateAverage;
    public void updateTXValidateTimingAverage(long processTiming, int counter) {
        // тут всегда Количество больше 0 приходит
        processTiming = processTiming / 1000 / counter;
        if (transactionValidateTimingCounter < 1 << 3) {
            transactionValidateTimingCounter++;
            transactionValidateTimingAverage = ((transactionValidateTimingAverage * transactionValidateTimingCounter)
                    + processTiming - transactionValidateTimingAverage) / transactionValidateTimingCounter;
        } else if (System.currentTimeMillis() - pointValidateAverage > 10000) {
            pointValidateAverage = System.currentTimeMillis();
            transactionValidateTimingAverage = ((transactionValidateTimingAverage << 1)
                    + processTiming - transactionValidateTimingAverage) >> 1;
        } else {
            transactionValidateTimingAverage = ((transactionValidateTimingAverage << 5)
                    + processTiming - transactionValidateTimingAverage) >> 5;
        }
    }

    private long pointProcessAverage;
    public void updateTXProcessTimingAverage(long processTiming, int counter) {
        if (processTiming < 999999999999L) {
            // при переполнении может быть минус
            // в микросекундах подсчет делаем
            processTiming = processTiming / 1000 / (Controller.BLOCK_AS_TX_COUNT + counter);
            if (transactionProcessTimingCounter < 1 << 3) {
                transactionProcessTimingCounter++;
                transactionProcessTimingAverage = ((transactionProcessTimingAverage * transactionProcessTimingCounter)
                        + processTiming - transactionProcessTimingAverage) / transactionProcessTimingCounter;
            } else if (System.currentTimeMillis() - pointProcessAverage > 10000) {
                pointProcessAverage = System.currentTimeMillis();
                transactionProcessTimingAverage = ((transactionProcessTimingAverage << 1)
                        + processTiming - transactionProcessTimingAverage) >> 1;

            } else {
                transactionProcessTimingAverage = ((transactionProcessTimingAverage << 5)
                        + processTiming - transactionProcessTimingAverage) >> 5;
            }
        }
    }

    public Pair<Block, List<Transaction>> scanTransactions(DCSet dcSet, Block block, int blockLimit, int transactionLimit, int type, int service, Account account) {
        //CREATE LIST
        List<Transaction> transactions = new ArrayList<Transaction>();
        int counter = 0;

        //IF NO BLOCK START FROM GENESIS
        if (block == null) {
            block = new GenesisBlock();
        }

        //START FROM BLOCK
        int scannedBlocks = 0;
        do {
            int seqNo = 0;
            //FOR ALL TRANSACTIONS IN BLOCK
            for (Transaction transaction : block.getTransactions()) {

                transaction.setDC(dcSet, Transaction.FOR_NETWORK, block.heightBlock, ++seqNo);

                //CHECK IF ACCOUNT INVOLVED
                if (account != null && !transaction.isInvolved(account)) {
                    continue;
                }

                //CHECK IF TYPE OKE
                if (type != -1 && transaction.getType() != type) {
                    continue;
                }

                //CHECK IF SERVICE OKE
                if (service != -1 && transaction.getType() == Transaction.ARBITRARY_TRANSACTION) {
                    ArbitraryTransaction arbitraryTransaction = (ArbitraryTransaction) transaction;

                    if (arbitraryTransaction.getService() != service) {
                        continue;
                    }
                }

                //ADD TO LIST
                transactions.add(transaction);
                counter++;
            }

            //SET BLOCK TO CHILD
            block = block.getChild(dcSet);
            scannedBlocks++;
        }
        //WHILE BLOCKS EXIST && NOT REACHED TRANSACTIONLIMIT && NOT REACHED BLOCK LIMIT
        while (block != null && (counter < transactionLimit || transactionLimit == -1) && (scannedBlocks < blockLimit || blockLimit == -1));

        //CHECK IF WE REACHED THE END
        if (block == null) {
            block = this.getLastBlock(dcSet);
        } else {
            block = block.getParent(dcSet);
        }

        //RETURN PARENT BLOCK AS WE GET CHILD RIGHT BEFORE END OF WHILE
        return new Pair<Block, List<Transaction>>(block, transactions);
    }

    public Block getLastBlock(DCSet dcSet) {
        return dcSet.getBlockMap().last();
    }


    public byte[] getLastBlockSignature(DCSet dcSet) {
        return dcSet.getBlockMap().getLastBlockSignature();
    }

    // get last blocks for target
    public List<Block> getLastBlocksForTarget_old(DCSet dcSet) {

        Block last = dcSet.getBlockMap().last();

        List<Block> list = new ArrayList<Block>();

        if (last == null || last.getVersion() == 0) {
            return list;
        }

        for (int i = 0; i < TARGET_COUNT && last.getVersion() > 0; i++) {
            list.add(last);
            last = last.getParent(dcSet);
        }

        return list;
    }

    // get Target by last blocks in chain
    public long getTarget(DCSet dcSet) {
        Block block = this.getLastBlock(dcSet);
        return block.getTarget();
    }

    public String blockFromFuture(int height) {
        long blockTimestamp = getTimestamp(height);
        if (blockTimestamp + (BlockChain.WIN_BLOCK_BROADCAST_WAIT_MS >> 2) > NTP.getTime()) {
            return "invalid Timestamp from FUTURE: "
                    + (blockTimestamp + (BlockChain.WIN_BLOCK_BROADCAST_WAIT_MS >> 2) - NTP.getTime());
        }

        return null;
    }
}
