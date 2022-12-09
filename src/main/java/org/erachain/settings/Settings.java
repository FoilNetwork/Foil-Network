package org.erachain.settings;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.erachain.controller.Controller;
import org.erachain.core.BlockChain;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.lang.Lang;
import org.erachain.network.Peer;
import org.erachain.ntp.NTP;
import org.erachain.utils.FileUtils;
import org.erachain.utils.SaveStrToFile;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

public class Settings {

    public static final long DEFAULT_MAINNET_STAMP = 1620046800000L; // MAIN Net
    public static final long DEFAULT_DEMO_NET_STAMP = 1622206800000L; // DEMO Net

    public static String FORK_APP_Name = "Foil Network";
    public static String FORK_APP_URL_Name = "Foil.Network";
    public static String FORK_APP_URL = "https://foil.network";
    public static String FORK_APP_NAME = FORK_APP_Name.toUpperCase();
    public static String FORK_APP_name = FORK_APP_Name.toLowerCase();

    public static String APP_NAME = FORK_APP_Name;
    public static String APP_FULL_NAME = FORK_APP_NAME;

    public static boolean ERA_COMPU_ALL_UP = false;

    public static boolean EXCHANGE_IN_OUT;

    // FOR TEST by default
    public static long genesisStamp = DEFAULT_MAINNET_STAMP;

    public final static PublicKeyAccount FEE_ASSET_EMITTER = new PublicKeyAccount("DW21hKJ9CPSWruV7gbuJLdS6AfyG7XWgoKtvst4oDimy");

    /*
    Evgenii Evgenii Kuzin, [18.04.21 19:32]


Evgenii Evgenii Kuzin, [18.04.21 19:32]


Evgenii Evgenii Kuzin, [18.04.21 19:33]
7G3XruSQBGsAg4xSs15iFp6bNahRRxurh5 - admin 1 000 000
     */
    public final static List<List<Object>> HOLDERS = Arrays.asList(
            Arrays.asList("7G3XruSQBGsAg4xSs15iFp6bNahRRxurh5", 1000000, 1000000), // ADMIN GENESIS

            Arrays.asList("78cYzVXM69nsBBcf6hJ8Qpyqfkyy88Kv3N", 1000000), // Presale ICO
            Arrays.asList("7M3NizXTkJARCnawqvYUHLE9YVfEeaZddy", 1000000), // Presale ICO
            Arrays.asList("7FHmKh4VsUHQGeGegCLjEJZHCHvYmaZU8H", 1000000), // Presale ICO
            Arrays.asList("78sz6NAP7GQ43qDH4yxk4mQBpEzSj35X2p", 1000000), // Presale ICO
            Arrays.asList("75opvfza3w5VyPTKHtboGz8mkpFaA9No5W", 1000000), // Presale ICO
            Arrays.asList("7QhwTrFctNVqiGHCeSDbvX7cdCSUEAL6dr", 1000000), // Presale ICO
            Arrays.asList("73CgJma5jahiB2PvjZoLCAhQ8oFm4iHCfM", 1000000), // Presale ICO
            Arrays.asList("7LYw7kK7SRAvaALJ6MAMUTYP2zgheQYyjN", 1000000), // Presale ICO
            Arrays.asList("7EZw7aFLpkvHCGjmwiuJHiCiriFpFPhfRv", 1000000), // Presale ICO
            Arrays.asList("7Dn7oga1VmA8iMGJT8HZRvCf8vdPXvkozK", 1000000), // Presale ICO

            Arrays.asList("7Gsyi9v4k94QGhjYocTe8SiVvpfUGgF229", 1000000), // Presale ICO
            Arrays.asList("7LxXuGwDUHjjQFjFFUYTih49S5EVHHBXJD", 1000000), // Presale ICO
            Arrays.asList("7AQmXkyf5VXXCXYcSKNPsRnjirPB6cYEou", 1000000), // Presale ICO
            Arrays.asList("7DkLcPZVziG9LKaGYW3UFZ8pKbzTLyApdC", 1000000), // Presale ICO
            Arrays.asList("7GFXSC71vDPzYnfgoV2MqtBdEXFnHfdoD3", 1000000), // Presale ICO
            Arrays.asList("7D9SQF1GpTs3KRHPEFfGLmtwvSzpmrHy33", 1000000), // Presale ICO
            Arrays.asList("7PfkTgFtPWtWfeHDiBTLfGFjZ8sNp2nZ5F", 1000000), // Presale ICO
            Arrays.asList("7RJDsh6EMnLApRHCTrcRdLtkiv3HnhZjCi", 1000000), // Presale ICO
            Arrays.asList("7N8TqPS91cWDwwjN6iodJivmk2PBbAu4Ht", 1000000), // Presale ICO
            Arrays.asList("7Euk3sjYrVsnTJtUkemV9aiE3TwDBh6fWs", 1000000), // Presale ICO

            Arrays.asList("7RvLUrfq1theCpqQ4KT1iAvduwD77yiTSW", 1000000), // Binance IDO
            Arrays.asList("7LgJphKaC9BNQLnvHL47itzxD6aVZy8dFt", 1000000), // Binance IDO
            Arrays.asList("79eB52TX21TJAW4JkFTrJMZcgEeGnwuEP5", 1000000), // Binance IDO
            Arrays.asList("7AkpW5KpBPbJX1Sxzgg8ChSUF1376SdsH5", 1000000), // Binance IDO
            Arrays.asList("7Dcfnigh9EbzjKMCoAz17dm4pdGH6qjSm5", 1000000), // Binance IDO
            Arrays.asList("7LEP97NL6mAbGgD6wjogPXDKpWPEVyTrH7", 1000000), // Binance IDO
            Arrays.asList("75mKLwRoCy9H2XrZ8Kc76sfbLc377DrfF2", 1000000), // Binance IDO
            Arrays.asList("7BGoK2DLDuRy7jLnGE3h69LCn2Q38Ucbjh", 1000000), // Binance IDO
            Arrays.asList("7QZUmCtnckXcZBviJ5oSq7gCVNCC824w6E", 1000000), // Binance IDO
            Arrays.asList("75sn62qdVXXEw6qResQtcHLgBvuSKWoeXm", 1000000), // Binance IDO

            // Governance and development
            Arrays.asList("7PRTfkQf9uB2rJW1fvsxhUR2TP9iFeVqF9", 14400000, 0,
                    Arrays.asList(
                            Arrays.asList(600000, "7RmbtrpsAWwiRothUj1en6XCtq1K1Nn2nH"),
                            Arrays.asList(600000, "799RTqrmrWieKYcqPBtgt6kgBzKLr1Arji"),
                            Arrays.asList(600000, "79sGZzJAWpfzsHBrES346S68sYS64mHS71"),
                            Arrays.asList(600000, "7DnypMBhK365tdbjgnhvBxkVt2AsQeXVRP"),
                            Arrays.asList(600000, "7KGpLnf53RFDC6CaV6CJWsQi8BbjgGuzkG"),
                            Arrays.asList(600000, "73ziLRA9zkMhWwYQJwh4vLABmf274NuEB8"),
                            Arrays.asList(600000, "7NDX7s25ZNmHYTPEH6fvmhrKDaBYQZDoZC"),
                            Arrays.asList(600000, "7Hss4ngAGs5YrLc6pn8t83m1RgEZWLEEQq"),
                            Arrays.asList(600000, "7RVTMoNxp9YrdQJ493fDY63KCF8tND8H2X"),
                            Arrays.asList(600000, "77mCKgtBgM4p1bdX8Ym5L79fsMkDJV4nGD"),
                            Arrays.asList(600000, "7F7q1raAczKnjupY7oFTdcx37KJ7rJfD9K"),
                            Arrays.asList(600000, "7GyrtYLNMt9t31VqaVuFxeA6Yb6RqnA4JT"),
                            Arrays.asList(600000, "7MuvguyQLBA4XqCiEUhPor4KG2rRJVZx5a"),
                            Arrays.asList(600000, "7DSq7fNPghvUCgaz92XFwV6G2Pxc96Uz91"),
                            Arrays.asList(600000, "73MTRF6R5irAmzGmBdcqnvKUQrtH8dsp9R"),
                            Arrays.asList(600000, "77MWxrB6BuykCrZrsTqmfgfC3vGTgwFQYS"),
                            Arrays.asList(600000, "7EyxQxQwuwtbVuya1ZNjCssetsGMFq9vwU"),
                            Arrays.asList(600000, "7RG5RTbvwvLXsC13vyMHTWurH9uCDsKKkQ"),
                            Arrays.asList(600000, "7Cs26RSdymfSYj4xF8pu5H8QFfJcEbnNcc"),
                            Arrays.asList(600000, "7BnFb1tNbLwJijVbyWGUwiqnzajvMFY78y"),
                            Arrays.asList(600000, "7DVy6Jrep6pqLL6sNfnP2bUVNknb8b3PZG"),
                            Arrays.asList(600000, "73Q9MDPe2nf1hBzA4BrZiojAhZ4oZFfm1T"),
                            Arrays.asList(600000, "73MYmBvDmg2Rpk2LMsBG2PtasYPBKeHomD"),
                            Arrays.asList(600000, "7DSfArGqqD7drmQjpqr1SoXKPojhacLWHh")
                    )),

            Arrays.asList("79RDQvBkoBgo4LkmiwFXLLK96rHKQhDkzS", 1000000),
            Arrays.asList("7AC8XtjivSHiH9oQc7gebv7nSPmRusSANV", 1000000),
            Arrays.asList("7SG8UyJoUvJdp6sxS5UKwyPzVxGc2oEAZd", 1000000),
            Arrays.asList("77qgDxWoP7SC1yrTSbK2Sk3uNPhMGin4V6", 1000000),
            Arrays.asList("7PKFgMwmTvkgc3VZxVuiWr3aTUqNc6wCWJ", 1000000),
            Arrays.asList("7Mwur8LDAWXinQd8BfxmskppRiJAVrzsxa", 1000000),
            Arrays.asList("7NwssCgT4nN5LSmxMWTeDsVHpUK6VKoZt2", 1000000),
            Arrays.asList("7MR9BQfWhqF1XfteJKo9RV4e3yn7nLawN9", 1000000),
            Arrays.asList("7A9SFsHQJsUuKj7YBU4jEim67qpakCYrzz", 1000000),
            Arrays.asList("7QmQfkHiXqb6ag4dDk39jtwzbHwh4LFjhH", 1000000),

            // Staking rewards pool 2021
            Arrays.asList("79ZyZzmNj7sgH59H4jH5KdJonk57CNDxnG", 1000000),
            Arrays.asList("7RyrHxSiUqAQvSR8SqLJ1F9rjw9A3BLazb", 1000000),
            Arrays.asList("7Heoxd4jhMppGiKSt68Aj9AyMpeQ8oUv2q", 1000000),
            Arrays.asList("7HsB9inmcW7TktKxQ4LEsi6VGDMMFKEiiJ", 1000000),
            Arrays.asList("73AdDfXpiE9unN7MGVjF74skzVU6AYTRvk", 1000000),
            Arrays.asList("7GBAzMTp8LK8pJVyVRAEjuSkeTvMVdRihJ", 1000000),
            Arrays.asList("7HTBm78z3cbRtveHg5LRzqe9LVN4foneW6", 1000000),
            Arrays.asList("7KvBpyxHVfkuTbbutBppFJ9gWf2GAQ7DoD", 1000000),
            Arrays.asList("7H7UXrhjDHfa3MwhGGAaD31Ta59BfuQ3Z9", 1000000),
            Arrays.asList("7Ny35cioHpx72AtQ1gSRk6rBqq5joUdmyt", 1000000),

            // founder and Co-faunders
            Arrays.asList("79dSdaY9qFenXUfXzRaiWDuJnoNZw9xjzn", 9600000, 0,
                    Arrays.asList(
                            Arrays.asList(600000, "7RDoBrJbk1sUFFHCP3qLVFjQ8wYYQqni9z"),
                            Arrays.asList(600000, "7HCeSCTsKfzW6fqd8Q1HiY5UDKdMBnyqNw"),
                            Arrays.asList(600000, "7PXZvW7gUR8F8zBdK6Aay2nk3SZMEvcdVd"),
                            Arrays.asList(600000, "7GykmeiME8SAEofGhzMnqDeX4xrQmczrbS"),
                            Arrays.asList(600000, "75hNAQuVmeK5JujJyTkb3zMEQYx21bkaEz"),
                            Arrays.asList(600000, "78671KhRVfsn93HyhEUoyEiFFFn5oekYta"),
                            Arrays.asList(600000, "7QMRMdmz9VF2SmjfPGWesmnsqo8Q5a4HPb"),
                            Arrays.asList(600000, "7Q9P8f7fsMpA4NUZFWyb7on4PC14VG98em"),
                            Arrays.asList(600000, "7LdXnToCaVpuNX8RNgu6gMGPV6EgzKkDjV"),
                            Arrays.asList(600000, "7DQTzYvRogxZod1p2BNq4Q5eHj7AD3asdC"),
                            Arrays.asList(600000, "7EhMaD3TRusQyjDzydjbdbeLMrm4jvnuZR"),
                            Arrays.asList(600000, "7KpNx2xbDXiytQz5DDiHHJS5e3Dy3nVmBp"),
                            Arrays.asList(600000, "73tTksVMX6knZtPqKpbtokADWenYNrtN4b"),
                            Arrays.asList(600000, "7B3JXw7MshpQyy8co4NVB9NdWyRa18Yggq"),
                            Arrays.asList(600000, "7APZjduaFX14CkfbzpU1WeZpk6APGePpJ2"),
                            Arrays.asList(600000, "74a92GxQ2qJK6rtMiBAVsSeVQ2LZSjkz1R")

                    )),
            Arrays.asList("7Rg6NTC7Xfgs9L245y2sCWCJxvnEH7w6Vz", 1000000),
            Arrays.asList("7CMETneDqG1ZvKfNjLztcpzUqY79qzVT3V", 1000000),
            Arrays.asList("7CyvhYymFC4cUq4BxnfQ5Z848fU5xb9kw7", 1000000),
            Arrays.asList("73zje3D3FbvZ34w8YK2LWMFo4UmYWHidnt", 1000000),
            Arrays.asList("7BbKsfhEzffYsfKUyQePazTYNQwnYpLiJD", 1000000),

            // Marketing and Partnership
            Arrays.asList("7LJr2XRqb9iyRYrGQgqgYDd5j1P4ZGphpw", 1000000),
            Arrays.asList("7FeEBwyDCTFyqK9eQcfnmUExtrhirAjSKr", 1000000),
            Arrays.asList("7JpK4fcG2apy1CHyZ7DeGinC5EuMaKYr6f", 1000000),
            Arrays.asList("739VSVdoGdGSgnoRKEkcutfxgGjEZ6VUHk", 1000000),
            Arrays.asList("7S4E3iQra6cNuxjmTBKuzpxLW2pCaZtUoZ", 1000000),
            Arrays.asList("7DX3CDKE1WoDR78gH4BrFQxdMHYLhCLd7R", 1000000),
            Arrays.asList("7L7XVkV9mW2RSiq1FJ1pLvgUKUChC43Eqa", 1000000),
            Arrays.asList("7EkwWUEChUktAsbRjp6srnC17hiJWFP8NR", 1000000),
            Arrays.asList("74hr4koDVieYEW4Q8RHC8eeyZw8WEhnnTN", 1000000),
            Arrays.asList("7B1fEsXNdkqn2gqmbmo2cFJyjAGdN87VWY", 1000000),

            // ERACHAIN freeze
            Arrays.asList("7G1G45RX4td59daBv6PoN84nAJA49NZ47i", 4000000),
            Arrays.asList("7QTDHp15vcHN3F4zP2BTcDXJkeotzQZkG4", 6000000)

    );

    public static String peersURL = "https://gitlab.com/FoilNetwork/foilnetwork-public/-/raw/master/peers.json";
    public static String cloneLicense; // see sidePROTOCOL_example.json79ZyZzmNj7sgH59H4jH5KdJonk57CNDxnG

    public static final String CLONE_OR_SIDE = "Chain"; // cloneChain or SideChain

    //private static final String[] DEFAULT_PEERS = { };
    public static final int DEFAULT_ACCOUNTS = 1;
    //DATA
    public static final String DEFAULT_DATA_CHAIN_DIR = "datachain";

    private static final String DEFAULT_DATA_LOCAL_DIR = "datalocal";
    private static final String DEFAULT_DATA_FPOOR_DIR = "dataFPool";
    private static final String DEFAULT_DATA_TEMP_DIR = "datatemp";
    private static final String DEFAULT_DATA_WALLET_DIR = "dataWallet";
    public static final String DEFAULT_WALLET_KEYS_DIR = "walletKeys";
    private static final String DEFAULT_BACKUP_DIR = "backup";
    private static final String DEFAULT_TEMP_DIR = "temp";
    private static final String DEFAULT_TELEGRAM_DIR = "datatele";
    private static final Logger LOGGER = LoggerFactory.getLogger(Settings.class);
    //NETWORK
    private static final int DEFAULT_MIN_CONNECTIONS = 8; // for OWN connections
    private static final int DEFAULT_MAX_CONNECTIONS = 20;
    private static final boolean DEFAULT_LOCAL_PEER_SCANNER = false;
    // EACH known PEER may send that whit peers to me - not white peer may be white peer for me
    private static final int DEFAULT_MAX_RECEIVE_PEERS = 100;
    private static final int DEFAULT_MAX_SENT_PEERS = DEFAULT_MAX_RECEIVE_PEERS;
    // BLOCK
    //public static final int BLOCK_MAX_SIGNATURES = 100; // blocks load onetime
    private static final int DEFAULT_CONNECTION_TIMEOUT = 20000;
    private static final boolean DEFAULT_TRYING_CONNECT_TO_BAD_PEERS = true;

    // GUI SCHEME
    public static final String DEFAULT_THEME = "System"; //"Metal";
    private static final Integer DEFAULT_FONT_SIZE = 14;
    private static final String DEFAULT_FONT_NAME = "Arial";
    private static final String DEFAULT_FONT_COLOR = "0,137,28"; //"0,120,0";
    private static final String DEFAULT_FONT_COLOR_SELECTED = "154,255,72"; //"120,250,120";


    //RPC
    private static final String DEFAULT_RPC_ALLOWED = "127.0.0.1"; // localhost = error in accessHandler.setWhite(Settings.getInstance().getRpcAllowed());
    private static final boolean DEFAULT_RPC_ENABLED = false; //
    private static final boolean DEFAULT_BACUP_ENABLED = false;
    private static final boolean DEFAULT_BACKUP_ASK_ENABLED = false;
    //GUI CONSOLE
    private static final boolean DEFAULT_GUI_CONSOLE_ENABLED = true;
    //WEB
    public static final String DEFAULT_WEB_ALLOWED = "127.0.0.1";
    private static final boolean DEFAULT_WEB_ENABLED = true;
    private static final String DEFAULT_WEB_KEYSTORE_FILE_PATH = "SSL" + File.separator +"WEBkeystore";
    private String webKeyStorePassword ="";
    private String webStoreSourcePassword ="";
    private String webKeyStorePath ="";
    private boolean webUseSSL = false;

    // 19 03
    //GUI
    private static final boolean DEFAULT_GUI_ENABLED = true;
    private static final boolean DEFAULT_GUI_DYNAMIC = true;
    private static final boolean DEFAULT_GENERATOR_KEY_CACHING = true;
    private static final boolean DEFAULT_CHECKPOINTING = true;
    private static final boolean DEFAULT_SOUND_RECEIVE_COIN = true;
    private static final boolean DEFAULT_SOUND_MESSAGE = true;
    private static final boolean DEFAULT_SOUND_NEW_TRANSACTION = true;
    private static final boolean DEFAULT_SOUND_FORGED_BLOCK = true;
    private static final boolean DEFAULT_TRAY_EVENT = true;
    //private static final int DEFAULT_MAX_BYTE_PER_FEE = 512;
    private static final boolean ALLOW_FEE_LESS_REQUIRED = false;
    //DATE FORMAT
    private static final String DEFAULT_TIME_ZONE = ""; //"GMT+3";

    //private static final BigDecimal DEFAULT_BIG_FEE = new BigDecimal(1000);
    private static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss z";
    private static final String DEFAULT_BIRTH_TIME_FORMAT = "yyyy-MM-dd HH:mm z";
    private static final boolean DEFAULT_NS_UPDATE = false;
    private static final boolean DEFAULT_FORGING_ENABLED = true;

    /**
     * нельзя тут использовать localhost - только 127....
     * ex: http://127.0.0.1/7pay_in/tools/block_proc/ERA
     */
    private static final String NOTIFY_INCOMING_URL = "http://127.0.0.1:8000/exhange/era/income";
    private static final int NOTIFY_INCOMING_CONFIRMATIONS = 0;
    public static String DEFAULT_LANGUAGE = "en";

    /**
     * Если отключить то локально не будут сохраняться телеграммы для своих счетов
     * и соотвественно в кошельке не будет прилетать никаких весточек к моим счетам.
     * !!! Отключать для НОД которые только форжат - надо настройки потом делать
     *
     * @return
     */
    public static final boolean USE_TELEGRAM_STORE = true;
    public static final int TELEGRAM_STORE_PERIOD = 5; // in days

    public final static int NET_MODE_MAIN = 0;
    public final static int NET_MODE_CLONE = 1;
    public final static int NET_MODE_DEMO = 2;
    public final static int NET_MODE_TEST = 3;
    public static int NET_MODE;

    public static int TEST_DB_MODE;
    public static int CHECK_BUGS;

    private static Settings instance;

    List<Peer> cacheInternetPeers;
    long timeLoadInternetPeers;
    private JSONObject settingsJSON;
    private JSONObject peersJSON;
    public static JSONArray genesisJSON;
    public static boolean simpleTestNet;
    private InetAddress localAddress;
    private String[] defaultPeers = {};

    private String userPath = "";

    /**
     * Если задан то включает и конечную папку длля файлов
     */
    public static String dataChainPath = "";
    private String walletKeysPath = "";
    public static File SECURE_WALLET_FILE = new File(DEFAULT_WALLET_KEYS_DIR, "wallet.s.dat");

    private String dataWalletPath = "";
    private String dataTelePath = "";
    private String backUpPath = "";
    private String tempPath;

    private String telegramDefaultSender;
    private String telegramDefaultReciever;
    private String telegramRatioReciever = null;

    private Settings() {
        this.localAddress = this.getCurrentIp();
        settingsJSON = read_setting_JSON();

        EXCHANGE_IN_OUT = isCloneNet();

        //TRY READ PEERS.JSON
        try {
            this.peersJSON = FileUtils.readCommentedJSONObject(this.getPeersPath());
        } catch (Exception e) {
            File file = new File("");
            LOGGER.info("Error while reading PEERS " + file.getAbsolutePath() + ", using default!");
            LOGGER.error(e.getMessage(), e);
            this.peersJSON = new JSONObject();
        }
    }

    public synchronized static Settings getInstance() {
        if (instance == null) {
            instance = new Settings();
        }
        return instance;
    }


    public synchronized static void freeInstance() {
        instance = null;
    }

    public JSONObject Dump() {
        return (JSONObject) settingsJSON.clone();
    }

    public void setDefaultPeers(String[] peers) {
        this.defaultPeers = peers;
    }

    /**
     * Если отключить то локально не будут сохраняться телеграммы для своих счетов
     * и соотвественно в кошельке не будет прилетать никаких весточек к моим счетам.
     * !!! Отключать для НОД которые только форжат - надо настройки потом делать
     *
     * @return
     */
    public boolean getTelegramStoreUse() {
        return USE_TELEGRAM_STORE;
    }

    public int getTelegramStorePeriod() {
        return TELEGRAM_STORE_PERIOD;
    }


    public String getTelegramDefaultSender() {
        return telegramDefaultSender;
    }

    public void setTelegramDefaultSender(String str) {
        telegramDefaultSender = str;
    }

    public String getTelegramDefaultReciever() {
        return telegramDefaultReciever;
    }

    public void setTelegramDefaultReciever(String str) {
        this.telegramDefaultReciever = str;
    }

    /////// DIR

    public String getUserPath() {
        return this.userPath;
    }

    public void setUserPath(String path) {
        if (path == null) {
            path = "";
        } else if (!path.isEmpty()) {
            // correcting single backslash bug
            path.replace("\\", File.separator);
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
        }

        this.userPath = path;

    }

    public String getPeersPath() {
        return this.userPath + (isDemoNet() ? "peers-demo.json" : isTestNet() ? "peers-test.json" :
                isCloneNet() ? Settings.CLONE_OR_SIDE.toLowerCase() + "PEERS.json" : "peers.json");
    }

    public String getDataWalletPath() {
        if (settingsJSON.containsKey("dataWalletPath")) {
            this.dataWalletPath = settingsJSON.get("dataWalletPath").toString();
        }

        if (this.dataWalletPath.isEmpty())
            return this.userPath + DEFAULT_DATA_WALLET_DIR;
        if (this.dataWalletPath.endsWith(DEFAULT_DATA_WALLET_DIR + File.separator))
            return this.dataWalletPath;
        return this.dataWalletPath + DEFAULT_DATA_WALLET_DIR;
    }

    public static String normalizePath(String path) {
        String appPath = new File(".").getAbsolutePath();
        if (appPath.endsWith(".")) {
            appPath = appPath.substring(0, appPath.length() - 1);
        }

        if (path.startsWith(appPath)) {
            path = path.substring(appPath.length());
        }
        return path;

    }

    public void setDataWalletPath(String path) {
        if (path == null) {
            path = "";
        } else if (!path.isEmpty()) {
            // correcting single backslash bug
            path.replace("\\", File.separator);
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
        }

        this.dataWalletPath = path;
        settingsJSON.put("dataWalletPath", path);
    }

    public String getWalletKeysPath() {
        if (settingsJSON.containsKey("walletKeysPath")) {
            this.walletKeysPath = settingsJSON.get("walletKeysPath").toString();
        }

        if (this.walletKeysPath.isEmpty())
            return this.userPath + DEFAULT_WALLET_KEYS_DIR;
        return this.walletKeysPath;
    }

    public void setWalletKeysPath(String path) {
        if (path == null) {
            path = "";
        } else if (!path.isEmpty()) {
            // correcting single backslash bug
            path.replace("\\", File.separator);
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
        }

        this.walletKeysPath = path;
        settingsJSON.put("walletKeysPath", path);
        SECURE_WALLET_FILE = new File(walletKeysPath, "wallet.s.dat");

    }

    public String getTelegramDir() {
        if (this.dataTelePath.isEmpty())
            return this.userPath + DEFAULT_TELEGRAM_DIR;
        if (this.dataTelePath.endsWith(DEFAULT_TELEGRAM_DIR + File.separator))
            return this.dataTelePath;
        return this.dataTelePath + DEFAULT_TELEGRAM_DIR;
    }

    public void setDataTelePath(String path) {

        if (path == null) {
            path = "";
        } else if (!path.isEmpty()) {
            // correcting single backslash bug
            path.replace("\\", File.separator);
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
        }

        this.dataTelePath = path;

    }

    /// Так как в папке все может быть удалено - делаем встроенную папку, иначе по несотарожности все может быть удалено ((
    public String getDataChainPath() {

        if (dataChainPath == null || dataChainPath.isEmpty()) {
            if (settingsJSON.containsKey("dataChainPath")) {
                this.dataChainPath = settingsJSON.get("dataChainPath").toString();
            }
        }

        if (this.dataChainPath.isEmpty()) return this.userPath + DEFAULT_DATA_CHAIN_DIR;
        if (this.dataChainPath.endsWith(DEFAULT_DATA_CHAIN_DIR)
                || this.dataChainPath.endsWith(DEFAULT_DATA_CHAIN_DIR + File.separator)) return this.dataChainPath;
        return this.dataChainPath + DEFAULT_DATA_CHAIN_DIR;
    }

    public void setDataChainPath(String path) {
        if (path == null) {
            path = "";
        } else if (!path.isEmpty()) {
            // correcting single backslash bug
            path.replace("\\", File.separator);
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
        }

        this.dataChainPath = path;
        settingsJSON.put("dataChainPath", path);


    }

    public String getBackUpPath() {
        if (this.backUpPath.isEmpty())
            return this.userPath + DEFAULT_BACKUP_DIR;
        if (this.backUpPath.endsWith(DEFAULT_BACKUP_DIR + File.separator))
            return this.backUpPath;
        return this.backUpPath + DEFAULT_BACKUP_DIR;
    }

    public void setBackUpPath(String path) {
        if (path == null) {
            path = "";
        } else if (!path.isEmpty()) {
            // correcting single backslash bug
            path.replace("\\", File.separator);
            if (!path.endsWith(File.separator)) {
                path += File.separator;
            }
        }

        this.backUpPath = path;
    }

    public String getPatnIcons() {
        return this.getUserPath() + "images" + File.separator + "pageicons" + File.separator;
    }

    public String getLocalDir() {
        return this.getUserPath() + DEFAULT_DATA_LOCAL_DIR;
    }

    public String getFPoolDir() {
        return this.getUserPath() + DEFAULT_DATA_FPOOR_DIR;
    }

    public String getDataTempDir() {
        String path = this.getUserPath() + DEFAULT_DATA_TEMP_DIR;
        File tempDir = new File(path);
        if (!tempDir.exists()) {
            tempDir.mkdir();
        }

        return path;
    }

    public String getLangDir() {
        return this.getUserPath() + "languages";
    }

    public String getSettingsPath() {
        return getUserPath() + "settings.json";
    }

    public String getGuiSettingPath() {
        return getUserPath() + "gui_settings.json";
    }

    ////////////////notify_incoming_confirmations

    // http://127.0.0.1:8000/ipay3_free/tools/block_proc/ERA
    public String getNotifyIncomingURL() {
        if (this.settingsJSON.containsKey("notify_incoming_url")) {
            return (String) this.settingsJSON.get("notify_incoming_url");
        }
        return NOTIFY_INCOMING_URL;
    }

    public int getNotifyIncomingConfirmations() {
        if (this.settingsJSON.containsKey("notify_incoming_confirmations")) {
            return (int) (long) this.settingsJSON.get("notify_incoming_confirmations");
        }

        return NOTIFY_INCOMING_CONFIRMATIONS;
    }

    public JSONArray getPeersJson() {
        if (this.peersJSON != null && this.peersJSON.containsKey("knownpeers")) {
            return (JSONArray) this.peersJSON.get("knownpeers");
        } else {
            return new JSONArray();
        }

    }

    public void updateJson(String key, Object value) {
        settingsJSON.put(key, value);
    }

    @SuppressWarnings("unchecked")
    /**
     * полностью доверныые пиры от которых данные не проверяются - ни блоки ни транзакции
     */
    public List<String> getTrustedPeers() {

        try {
            File file = new File(this.userPath
                    + (BlockChain.TEST_MODE ? "peers-trusted-test.json" : "peers-trusted.json"));
            return new ArrayList<String>(FileUtils.readCommentedJSONArray(file.getPath()));
        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
        }

        return new ArrayList<>();
    }

    public List<Peer> getKnownPeers() {
        try {
            boolean loadPeersFromInternet = (
                    Controller.getInstance().getToOfflineTime() != 0L
                            &&
                            NTP.getTime() - Controller.getInstance().getToOfflineTime() > 5 * 60 * 1000
            );

            List<Peer> knownPeers = new ArrayList<>();
            JSONArray peersArray = new JSONArray();

            if (!BlockChain.TEST_MODE) {
                try {
                    JSONArray peersArraySettings = (JSONArray) this.settingsJSON.get("knownpeers");

                    if (peersArraySettings != null) {
                        for (Object peer : peersArraySettings) {
                            if (!peersArray.contains(peer)) {
                                peersArray.add(peer);
                            }
                        }
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    LOGGER.info("Error with loading knownpeers from settings.json.");
                }
            }

            try {
                JSONArray peersArrayPeers = (JSONArray) this.peersJSON.get("knownpeers");

                if (peersArrayPeers != null) {
                    for (Object peer : peersArrayPeers) {
                        if (!peersArray.contains(peer)) {
                            peersArray.add(peer);
                        }
                    }
                }

            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
                LOGGER.info("Error with loading knownpeers from peers.json.");
            }

            knownPeers.addAll(this.getPeersFromDefault());

            knownPeers.addAll(getKnownPeersFromJSONArray(peersArray));

            if (!BlockChain.TEST_MODE && (knownPeers.isEmpty() || loadPeersFromInternet)) {
                knownPeers.addAll(getKnownPeersFromInternet());
            }

            return knownPeers;

        } catch (Exception e) {
            LOGGER.debug(e.getMessage(), e);
            LOGGER.info("Error in getKnownPeers().");
            return new ArrayList<Peer>();
        }
    }

    public List<Peer> getKnownPeersFromInternet() {
        try {

            if (this.cacheInternetPeers == null) {

                this.cacheInternetPeers = new ArrayList<Peer>();
            }

            if (this.cacheInternetPeers.isEmpty() || NTP.getTime() - this.timeLoadInternetPeers > 24 * 60 * 60 * 1000) {
                this.timeLoadInternetPeers = NTP.getTime();
                URL u = new URL(peersURL);
                InputStream in = u.openStream();
                String stringInternetSettings = IOUtils.toString(in);
                JSONObject internetSettingsJSON = (JSONObject) JSONValue.parse(stringInternetSettings);
                JSONArray peersArray = (JSONArray) internetSettingsJSON.get("knownpeers");
                if (peersArray != null) {
                    this.cacheInternetPeers = getKnownPeersFromJSONArray(peersArray);
                }
            }

            //logger.info(Lang.T("Peers loaded from Internet : ") + this.cacheInternetPeers.size());

            return this.cacheInternetPeers;

        } catch (Exception e) {
            //RETURN EMPTY LIST

            //logger.debug(e.getMessage(), e);
            LOGGER.info(Lang.T("Peers loaded from Internet with errors : ") + this.cacheInternetPeers.size());

            return this.cacheInternetPeers;
        }
    }

    public List<Peer> getPeersFromDefault() {
        List<Peer> peers = new ArrayList<Peer>();
        for (int i = 0; i < this.defaultPeers.length; i++) {
            try {
                InetAddress address = InetAddress.getByName(this.defaultPeers[i]);

                if (!this.isLocalAddress(address)) {
                    //CREATE PEER
                    Peer peer = new Peer(address);

                    //ADD TO LIST
                    peers.add(peer);
                }
            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
                LOGGER.info(this.defaultPeers[i] + " - invalid peer address!");
            }
        }
        return peers;
    }

    public List<Peer> getKnownPeersFromJSONArray(JSONArray peersArray) {
        try {
            //CREATE LIST WITH PEERS
            List<Peer> peers = new ArrayList<>();

            for (int i = 0; i < peersArray.size(); i++) {
                try {
                    InetAddress address = InetAddress.getByName((String) peersArray.get(i));

                    if (!this.isLocalAddress(address)) {
                        //CREATE PEER
                        Peer peer = new Peer(address);

                        //ADD TO LIST
                        peers.add(peer);
                    }
                } catch (Exception e) {
                    LOGGER.debug(e.getMessage(), e);
                    LOGGER.info((String) peersArray.get(i) + " - invalid peer address!");
                }
            }

            //RETURN
            return peers;
        } catch (Exception e) {
            //RETURN EMPTY LIST
            return new ArrayList<Peer>();
        }
    }

    public boolean isMainNet() {
        return NET_MODE == NET_MODE_MAIN;
    }

    public boolean isCloneNet() {
        return NET_MODE == NET_MODE_CLONE;
    }

    public boolean isDemoNet() {
        return NET_MODE == NET_MODE_DEMO;
    }

    public boolean isTestNet() {
        return NET_MODE >= NET_MODE_DEMO;
    }

    public long getGenesisStamp() {
        return this.genesisStamp;
    }

    public int getMaxConnections() {
        if (this.settingsJSON.containsKey("maxconnections")) {
            return ((Long) this.settingsJSON.get("maxconnections")).intValue();
        }

        return DEFAULT_MAX_CONNECTIONS;
    }

    public int getMaxReceivePeers() {
        if (this.settingsJSON.containsKey("maxreceivepeers")) {
            return ((Long) this.settingsJSON.get("maxreceivepeers")).intValue();
        }

        return DEFAULT_MAX_RECEIVE_PEERS;
    }

    public int getMaxSentPeers() {
        if (this.settingsJSON.containsKey("maxsentpeers")) {
            return ((Long) this.settingsJSON.get("maxsentpeers")).intValue();
        }

        return DEFAULT_MAX_SENT_PEERS;
    }

    public int getMinConnections() {
        if (this.settingsJSON.containsKey("minconnections")) {
            return ((Long) this.settingsJSON.get("minconnections")).intValue();
        }

        return DEFAULT_MIN_CONNECTIONS;
    }

    public int getConnectionTimeout() {
        if (this.settingsJSON.containsKey("connectiontimeout")) {
            return ((Long) this.settingsJSON.get("connectiontimeout")).intValue();
        }

        return DEFAULT_CONNECTION_TIMEOUT;
    }

    public boolean isLocalPeersScannerEnabled() {
        if (this.settingsJSON.containsKey("localpeerscanner")) {
            return ((Boolean) this.settingsJSON.get("localpeerscanner")).booleanValue();
        }

        return DEFAULT_LOCAL_PEER_SCANNER;
    }

    public boolean isTryingConnectToBadPeers() {
        if (this.settingsJSON.containsKey("tryingconnecttobadpeers")) {
            return ((Boolean) this.settingsJSON.get("tryingconnecttobadpeers")).booleanValue();
        }

        return DEFAULT_TRYING_CONNECT_TO_BAD_PEERS;
    }

    public int getRpcPort() {
        if (this.settingsJSON.containsKey("rpcport")) {
            return ((Long) this.settingsJSON.get("rpcport")).intValue();
        }

        return BlockChain.DEFAULT_RPC_PORT;
    }

    public String[] getRpcAllowed() {
        try {
            if (this.settingsJSON.containsKey("rpcallowed")) {
                //GET PEERS FROM JSON
                JSONArray allowedArray = (JSONArray) this.settingsJSON.get("rpcallowed");

                //CREATE LIST WITH PEERS
                String[] allowed = new String[allowedArray.size()];
                for (int i = 0; i < allowedArray.size(); i++) {
                    allowed[i] = (String) allowedArray.get(i);
                }

                //RETURN
                return allowed;
            }

            //RETURN
            return DEFAULT_RPC_ALLOWED.split(";");
        } catch (Exception e) {
            //RETURN EMPTY LIST
            return new String[0];
        }
    }

    public boolean isRpcEnabled() {
        if (this.settingsJSON.containsKey("rpcenabled")) {
            return ((Boolean) this.settingsJSON.get("rpcenabled")).booleanValue();
        }

        return DEFAULT_RPC_ENABLED;
    }

    public boolean getbackUpEnabled() {
        if (this.settingsJSON.containsKey("backupenabled")) {
            return ((Boolean) this.settingsJSON.get("backupenabled")).booleanValue();
        }

        return DEFAULT_BACUP_ENABLED;
    }

    public String getCompuRate() {
        if (this.settingsJSON.containsKey("compuRate")) {
            return ((String) this.settingsJSON.get("compuRate")).toString();
        }

        return "100";
    }

    public long getCompuRateAsset() {
        if (this.settingsJSON.containsKey("compuRateAsset")) {
            return Long.valueOf(this.settingsJSON.get("compuRateAsset").toString());
        }

        return AssetCls.USD_KEY;
    }

    public boolean getCompuRateUseDEX() {
        if (this.settingsJSON.containsKey("compuRateUseDEX")) {
            return Boolean.valueOf(this.settingsJSON.get("compuRateUseDEX").toString());
        }

        return true;
    }

    public long getDefaultPairAssetKey() {
        if (this.settingsJSON.containsKey("defaultPairAsset")) {
            return Long.valueOf(this.settingsJSON.get("defaultPairAsset").toString());
        }

        return 2L;
    }

    public AssetCls getDefaultPairAsset() {
        long key = getDefaultPairAssetKey();

        AssetCls asset = Controller.getInstance().getAsset(key);
        if (asset == null)
            asset = Controller.getInstance().getAsset(2L);

        return asset;
    }


    public boolean getbackUpAskToStart() {
        if (this.settingsJSON.containsKey("backupasktostart")) {
            return ((Boolean) this.settingsJSON.get("backupasktostart")).booleanValue();
        }

        return DEFAULT_BACKUP_ASK_ENABLED;
    }

    public int getWebPort() {
        if (this.settingsJSON.containsKey("webport")) {
            return ((Long) this.settingsJSON.get("webport")).intValue();
        }

        return BlockChain.DEFAULT_WEB_PORT;
    }
    // SSL settings
    public String getWebKeyStorePassword() {
        return webKeyStorePassword;
    }

    public void setWebKeyStorePassword(String keyStorePassword) {
        webKeyStorePassword= keyStorePassword;
    }

    public String getWebStoreSourcePassword() {
        return webStoreSourcePassword;
    }
    public void setWebStoreSourcePassword(String storeSourcePassword) {
        webStoreSourcePassword= storeSourcePassword;
    }

    public String getWebKeyStorePath() {
        if (webKeyStorePath.equals("")) return DEFAULT_WEB_KEYSTORE_FILE_PATH;
        return webKeyStorePath;
    }
    public void setWebKeyStorePath(String webKeyStorePath) {
        webStoreSourcePassword= webKeyStorePath;
    }

    public boolean isWebUseSSL() {
        return webUseSSL;
    }

    public void setWebUseSSL(boolean webUseSSL1) {
        webUseSSL= webUseSSL1;
    }


    public String explorerURL;

    public String getBlockexplorerURL() {
        if (explorerURL == null) {
            if (this.settingsJSON.containsKey("explorerURL")) {
                return (explorerURL = this.settingsJSON.get("explorerURL").toString());
            } else {
                return (explorerURL = BlockChain.DEFAULT_EXPLORER);
            }
        }

        return explorerURL;

    }

    public boolean isGuiConsoleEnabled() {
        if (this.settingsJSON.containsKey("guiconsoleenabled")) {
            return ((Boolean) this.settingsJSON.get("guiconsoleenabled")).booleanValue();
        }

        return DEFAULT_GUI_CONSOLE_ENABLED;
    }

    public String[] getWebAllowed() {
        try {
            if (this.settingsJSON.containsKey("weballowed")) {
                //GET PEERS FROM JSON
                JSONArray allowedArray = (JSONArray) this.settingsJSON.get("weballowed");

                //CREATE LIST WITH PEERS
                String[] allowed = new String[allowedArray.size()];
                for (int i = 0; i < allowedArray.size(); i++) {
                    allowed[i] = (String) allowedArray.get(i);
                }

                //RETURN
                return allowed;
            }

            //RETURN
            return (BlockChain.TEST_MODE ? ";" : DEFAULT_WEB_ALLOWED).split(";");

        } catch (Exception e) {
            //RETURN EMPTY LIST
            return new String[0];
        }
    }

    public boolean isWebEnabled() {
        if (this.settingsJSON.containsKey("webenabled")) {
            return ((Boolean) this.settingsJSON.get("webenabled")).booleanValue();
        }

        return DEFAULT_WEB_ENABLED;
    }

    public boolean updateNameStorage() {
        if (this.settingsJSON.containsKey("nsupdate")) {
            return ((Boolean) this.settingsJSON.get("nsupdate")).booleanValue();
        }

        return DEFAULT_NS_UPDATE;
    }

    public boolean isForgingEnabled() {
        try {
            if (this.settingsJSON.containsKey("forging")) {
                return ((Boolean) this.settingsJSON.get("forging")).booleanValue();
            }
        } catch (Exception e) {
            LOGGER.error("Bad Settings.json content for parameter forging " + ExceptionUtils.getStackTrace(e));
        }

        return DEFAULT_FORGING_ENABLED;
    }

    public boolean isGeneratorKeyCachingEnabled() {
        if (this.settingsJSON.containsKey("generatorkeycaching")) {
            return ((Boolean) this.settingsJSON.get("generatorkeycaching")).booleanValue();
        }

        return DEFAULT_GENERATOR_KEY_CACHING;
    }

    public boolean isCheckpointingEnabled() {
        if (this.settingsJSON.containsKey("checkpoint")) {
            return ((Boolean) this.settingsJSON.get("checkpoint")).booleanValue();
        }

        return DEFAULT_CHECKPOINTING;
    }

    public boolean isSoundReceivePaymentEnabled() {
        if (this.settingsJSON.containsKey("soundreceivepayment")) {
            return ((Boolean) this.settingsJSON.get("soundreceivepayment")).booleanValue();
        }

        return DEFAULT_SOUND_RECEIVE_COIN;
    }

    public boolean isSoundReceiveMessageEnabled() {
        if (this.settingsJSON.containsKey("soundreceivemessage")) {
            return ((Boolean) this.settingsJSON.get("soundreceivemessage")).booleanValue();
        }

        return DEFAULT_SOUND_MESSAGE;
    }

    public boolean isSoundNewTransactionEnabled() {
        if (this.settingsJSON.containsKey("soundnewtransaction")) {
            return ((Boolean) this.settingsJSON.get("soundnewtransaction")).booleanValue();
        }

        return DEFAULT_SOUND_NEW_TRANSACTION;
    }

    public boolean isSoundForgedBlockEnabled() {
        if (this.settingsJSON.containsKey("soundforgedblock")) {
            return ((Boolean) this.settingsJSON.get("soundforgedblock")).booleanValue();
        }

        return DEFAULT_SOUND_FORGED_BLOCK;
    }

    public boolean isTrayEventEnabled() {
        if (this.settingsJSON.containsKey("trayeventenabled")) {
            return ((Boolean) this.settingsJSON.get("trayeventenabled")).booleanValue();
        }

        return DEFAULT_TRAY_EVENT;
    }

    public boolean isAllowFeeLessRequired() {
        if (this.settingsJSON.containsKey("allowfeelessrequired")) {
            return ((Boolean) this.settingsJSON.get("allowfeelessrequired")).booleanValue();
        }

        return ALLOW_FEE_LESS_REQUIRED;
    }

    public boolean isGuiEnabled() {

        if (!Controller.getInstance().doesWalletDatabaseExists()) {
            return true;
        }

        if (System.getProperty("nogui") != null) {
            return false;
        }
        if (this.settingsJSON.containsKey("guienabled")) {
            return ((Boolean) this.settingsJSON.get("guienabled")).booleanValue();
        }

        return DEFAULT_GUI_ENABLED;
    }

    public boolean isGuiDynamic() {
        if (this.settingsJSON.containsKey("guidynamic")) {
            return ((Boolean) this.settingsJSON.get("guidynamic")).booleanValue();
        }

        return DEFAULT_GUI_DYNAMIC;
    }

    public String getTimeZone() {
        if (this.settingsJSON.containsKey("timezone")) {
            return (String) this.settingsJSON.get("timezone");
        }

        return DEFAULT_TIME_ZONE;
    }

    public String getTimeFormat() {
        if (this.settingsJSON.containsKey("timeformat")) {
            return (String) this.settingsJSON.get("timeformat");
        }

        return DEFAULT_TIME_FORMAT;
    }

    // birth
    public String getBirthTimeFormat() {
        if (this.settingsJSON.containsKey("birthTimeformat")) {
            return (String) this.settingsJSON.get("birthTimeformat");
        }

        return DEFAULT_BIRTH_TIME_FORMAT;
    }


    public boolean isSysTrayEnabled() {
        if (this.settingsJSON.containsKey("systray")) {
            return ((Boolean) this.settingsJSON.get("systray")).booleanValue();
        }
        return true;
    }

    public boolean isLocalAddress(InetAddress address) {
        try {
            if (this.localAddress == null) {
                return false;
            } else {
                return address.equals(this.localAddress);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public boolean markIncome() {
        if (this.settingsJSON.containsKey("markincome")) {
            return new Boolean(this.settingsJSON.get("markincome").toString());
        }
        return false;
    }

    public String markColor() {
        if (this.settingsJSON.containsKey("markcolor")) {
            return this.settingsJSON.get("markcolor").toString();
        }
        return DEFAULT_FONT_COLOR;
    }

    public Color markColorObj() {
        try {
            String[] rgb = markColor().split(",");
            return new Color(new Integer(rgb[0].trim()), new Integer(rgb[1].trim()), new Integer(rgb[2].trim()));
        } catch (Exception e) {
            String[] rgb = DEFAULT_FONT_COLOR.split(",");
            return new Color(new Integer(rgb[0].trim()), new Integer(rgb[1].trim()), new Integer(rgb[2].trim()));
        }
    }

    public static String colorToHex(Color color) {
        return String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    public String markColorSelected() {
        if (this.settingsJSON.containsKey("markcolorselected")) {
            return this.settingsJSON.get("markcolorselected").toString();
        }
        return DEFAULT_FONT_COLOR_SELECTED;
    }

    public Color markColorSelectedObj() {
        try {
            String[] rgb = markColorSelected().split(",");
            return new Color(new Integer(rgb[0].trim()), new Integer(rgb[1].trim()), new Integer(rgb[2].trim()));
        } catch (Exception e) {
            String[] rgb = DEFAULT_FONT_COLOR_SELECTED.split(",");
            return new Color(new Integer(rgb[0].trim()), new Integer(rgb[1].trim()), new Integer(rgb[2].trim()));
        }
    }

    public InetAddress getCurrentIp() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface
                    .getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) networkInterfaces
                        .nextElement();
                Enumeration<InetAddress> nias = ni.getInetAddresses();
                while (nias.hasMoreElements()) {
                    InetAddress ia = (InetAddress) nias.nextElement();
                    if (!ia.isLinkLocalAddress()
                            && !ia.isLoopbackAddress()
                            && ia instanceof Inet4Address) {
                        return ia;
                    }
                }
            }
        } catch (SocketException e) {
            LOGGER.info("unable to get current IP " + e.getMessage());
        }
        return null;
    }

    public String getLang() {
        if (this.settingsJSON.containsKey("lang")) {
            String langStr = (String) this.settingsJSON.get("lang");

            // delete .json
            String[] tokens = langStr.split("\\.");
            if (tokens.length > 1)
                return tokens[0];
            return langStr;
        }

        return DEFAULT_LANGUAGE;
    }

    public String getLangFileName() {
        return getLang() + ".json";
    }

    public String getFontSize() {
        if (this.settingsJSON.containsKey("font_size")) {
            return this.settingsJSON.get("font_size").toString();
        }

        return DEFAULT_FONT_SIZE.toString();

    }

    public String get_File_Chooser_Paht() {
        if (this.settingsJSON.containsKey("FileChooser_Path")) {
            return this.settingsJSON.get("FileChooser_Path").toString();
        }

        return getUserPath();

    }

    public int get_File_Chooser_Wight() {
        if (this.settingsJSON.containsKey("FileChooser_Wight")) {
            return new Integer(this.settingsJSON.get("FileChooser_Wight").toString());
        }

        return 0;

    }

    public int get_File_Chooser_Height() {
        if (this.settingsJSON.containsKey("FileChooser_Height")) {
            return new Integer(this.settingsJSON.get("FileChooser_Height").toString());
        }

        return 0;

    }

    public String get_Font_Name() {
        if (this.settingsJSON.containsKey("font_name")) {
            return this.settingsJSON.get("font_name").toString();
        }

        return DEFAULT_FONT_NAME;
    }

    public String get_Theme() {

        if (this.settingsJSON.containsKey("theme")) {
            return this.settingsJSON.get("theme").toString();
        }

        return DEFAULT_THEME;
    }

    public String get_LookAndFell() {

        if (this.settingsJSON.containsKey("LookAndFell")) {
            return this.settingsJSON.get("LookAndFell").toString();
        }

        return DEFAULT_THEME;

    }

    public String cutPath(String path) {

        //if (!(this.userPath.endsWith("\\")
        if (path.endsWith("/")) {
            path.substring(0, path.length() - 1);
            path += File.separator;
        }

        return path;


    }

    public JSONObject read_setting_JSON() {
        int alreadyPassed = 0;
        File file = new File(this.userPath + "settings.json");
        if (!file.exists()) {
            try {
                file.createNewFile();
                JSONObject json = new JSONObject();
                json.put("!!!ver", "3.0");
                return json;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        try {
            // тут можно здать путь и чтение 2-й раз будет с того пути использовано как новый Settings по тому пути
            while (alreadyPassed < 2) {
                //OPEN FILE

                settingsJSON = FileUtils.readCommentedJSONObject(file.getPath());
                if (settingsJSON == null)
                    settingsJSON = new JSONObject();

                alreadyPassed++;

                if (this.settingsJSON.containsKey("userPath")) {
                    setUserPath(this.settingsJSON.get("userPath").toString());
                } else {
                    alreadyPassed++;
                }
                // read Telegrams parameters
                if (this.settingsJSON.containsKey("Telegram_Sender")) {
                    telegramDefaultSender = (String) this.settingsJSON.get("Telegram_Sender");
                }

                if (this.settingsJSON.containsKey("Telegram_Reciever")) {
                    telegramDefaultReciever = (String) this.settingsJSON.get("Telegram_Reciever");
                }

                if (this.settingsJSON.containsKey("Telegram_Ratio_Reciever")) {
                    telegramRatioReciever = (String) this.settingsJSON.get("Telegram_Ratio_Reciever");
                }

                // read BackUb Path
                if (this.settingsJSON.containsKey("backUpPath")) {
                    setBackUpPath((String) this.settingsJSON.get("backUpPath"));
                }

                if (this.settingsJSON.containsKey("dataWalletPath")) {
                    setDataWalletPath((String) this.settingsJSON.get("dataWalletPath"));
                }

                // set data dir getDataPath
                if (this.settingsJSON.containsKey("dataChainPath")) {
                    setDataChainPath((String) this.settingsJSON.get("dataChainPath"));
                }

                // set data dir getDataPath
                if (this.settingsJSON.containsKey("dataTelePath")) {
                    setDataTelePath((String) this.settingsJSON.get("dataTelePath"));
                }

                if (this.settingsJSON.containsKey("walletKeysPath")) {
                    setWalletKeysPath((String) this.settingsJSON.get("walletKeysPath"));
                }

                //CREATE FILE IF IT DOESNT EXIST
                if (!file.exists()) {
                    file.createNewFile();
                }
                // read web ssl settings
               try {
                    JSONObject webSSLSettings = (JSONObject)this.settingsJSON.get("WEB_SSL");
                        webKeyStorePassword = (String) webSSLSettings.get("KeyStorePassword");
                        webKeyStorePath = (String) webSSLSettings.get("KeyStorePath");
                        webStoreSourcePassword = (String) webSSLSettings.get("KeyStoreSourcePassword");
                        webUseSSL = (boolean) webSSLSettings.get("Enable");
              } catch (Exception e) {
                    webKeyStorePassword = "";
                    webKeyStorePath="";
                    webStoreSourcePassword = "";
                    webUseSSL = false;
                }

            }

        } catch (Exception e) {
            LOGGER.info("Error while reading/creating settings.json " + file.getAbsolutePath() + " using default!");
            LOGGER.error(e.getMessage(), e);
            settingsJSON = new JSONObject();
        }

        return settingsJSON;

    }

    public JSONObject getJSONObject() {
        return this.settingsJSON;
    }

    public String getTelegramRatioReciever() {
        // TODO Auto-generated method stub
        return this.telegramRatioReciever;
    }

    public void setTelegramRatioReciever(String str) {
        // TODO Auto-generated method stub
        this.telegramRatioReciever = str;
    }

    public void updateSettingsValue() {
        try {
            SaveStrToFile.saveJsonFine(Settings.getInstance().getSettingsPath(), settingsJSON);
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }


}
