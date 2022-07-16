package org.erachain.core.item.assets;


import com.google.common.primitives.Bytes;
import org.erachain.controller.PairsController;
import org.erachain.core.BlockChain;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.block.Block;
import org.erachain.core.exdata.exLink.ExLinkAddress;
import org.erachain.core.item.ItemCls;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.Transaction;
import org.erachain.core.transaction.TransactionAmount;
import org.erachain.dapp.epoch.memeTheGathering.MemeTheGatheringDAPP;
import org.erachain.dapp.epoch.shibaverse.ShibaVerseDAPP;
import org.erachain.database.PairMap;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.ItemMap;
import org.erachain.lang.Lang;
import org.erachain.utils.NumberAsString;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


/**
 * flag[0] - profitFeeMin[int] + profitFeeMax[int]
 * flag[1] - profitTax[int] + loanInterest[int] //  use "/apiasset/image/1048664" "/apiasset/icon/1048664"
 */
public abstract class AssetCls extends ItemCls {

    public static final int TYPE_KEY = ItemCls.ASSET_TYPE;

    protected static final int ASSET_TYPE_LENGTH = 1;

    protected static final long APP_DATA_DEX_AWARDS_MASK = 1L;
    // untransferable
    protected static final long APP_DATA_UNTRANSFERABLE_MASK = 2L;
    // anonimouse protection
    protected static final long APP_DATA_ANONIM_PROTECT_MASK = 4L;

    //
    protected int assetType;
    protected ExLinkAddress[] dexAwards;

    // CORE KEY
    public static final long ERA_KEY = 1L;
    public static final String ERA_ABBREV = "FOIL"; // ERA (main rights units)
    public static final String ERA_NAME = "FOIL";
    public static final String ERA_DESCR = "";

    public static final long FEE_KEY = 2L;
    public static final String FEE_ABBREV = "MVolt"; // COMP (main rights units)
    public static final String FEE_NAME = "MVolt";
    public static final String FEE_DESCR = "";

    public static final long AS_KEY = 3L;
    public static final String AS_ABBREV = "AS";
    public static final String AS_NAME = "AS";
    public static final String AS_DESCR = "";

    public static final long LIA_KEY = 5L;
    public static final String LIA_ABBREV = "LIA"; // Live In Asset
    public static final String LIA_NAME = "LIA";
    public static final String LIA_DESCR = "";

    public static final long BAL_KEY = 7L; // see in chainPROTOCOL.json
    public static final String BAL_ABBREV = "BAL";
    public static final String BAL_NAME = "BAL";
    public static final String BAL_DESCR = "";

    public static final long BTC_KEY = 12L;
    public static final long USD_KEY = 1840L;
    public static final long EUR_KEY = 1978L;

    public static final int UNIQUE = 1;
    public static final int VENTURE = 2;
    public static final int NAME = 3;
    public static final int UNIQUE_COPY = 4;
    public static final int INITIAL_FAVORITES = 100;

    ///////////////////////////////////////////////////
    /**
     * GOODS
     * передача в собственность, взять на хранение
     * 0 : движимая вещь вовне - может быть доставлена и передана на хранение (товары)
     */
    public static final int AS_OUTSIDE_GOODS = 0; // movable

    /**
     * ASSETS
     * передача имущества не требует действий во вне - все исполняется тут же. Их можно дать в долг и забрать самостоятельно
     * Требования не предъявляются.
     * 3 : цифровое имущество - не требует действий вовне и исполняется внутри платформы (токены, цифровые валюты, цифровые билеты, цифровые права и т.д.)
     */
    public static final int AS_INSIDE_ASSETS = 1;

    /**
     * IMMOVABLE
     * передача в сосбтвенность, дать в аренду (по графику времени), взять на охрану
     * 1 : недвижимая вещь вовне - может быть передана в аренду (недвижимость)
     */

    public static final int AS_OUTSIDE_IMMOVABLE = 2;

    /**
     * outside CURRENCY
     * +++ деньги вовне - можно истребовать вернуть и подтвердить получение денег
     * === полный аналог OUTSIDE_CLAIM по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_OUTSIDE_CURRENCY = 11;

    /**
     * outside SERVICE
     * +++ услуги во вне
     * === полный аналог OUTSIDE_CLAIM по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_OUTSIDE_SERVICE = 12; // UTILITY

    /**
     * outside SHARE
     * +++ акция предприятия вовне
     * === полный аналог OUTSIDE_CLAIM по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_OUTSIDE_SHARE = 13;

    /**
     * outside BILL - вексель - promissory note
     * +++ вексель на оплату во вне
     * === полный аналог OUTSIDE_CLAIM по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_OUTSIDE_BILL = 14;

    /**
     * outside BILL - вексель переводной (тратта) - bill of exchange
     * +++ вексель на оплату во вне
     * === полный аналог OUTSIDE_CLAIM по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_OUTSIDE_BILL_EX = 15;

    /**
     * my debt
     * +++ мой долг перед другим лицом - это обязательство
     * === полный аналог OUTSIDE_CLAIM по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_MY_DEBT = 26;

    /**
     * 🕐🕜🕑🕝🕒🕞🕓🕟🕔🕠🕕🕡🕖🕢🕗🕣🕘🕤🕙🕥🕚🕦🕛🕧
     * outside WORK TIME - рабочее время, которое можно купить и потребовать потратить и учесть как затрата
     */
    public static final int AS_OUTSIDE_WORK_TIME_MINUTES = 34;
    public static final int AS_OUTSIDE_WORK_TIME_HOURS = 35;

    /**
     * outside CLAIMS
     * +++ требования и обязательства вовне - можно истребовать право и подтвердить его исполнение (ссуда, займ, услуга, право, требование, деньги, билеты и т.д.)
     * <p>
     * учет обязательств прав и требований на услуги и действия во внешнем мире - в том числе займы, ссуды, кредиты, фьючерсы и т.д.
     * нельзя вернуть эмитенту - но можно потребовать исполнение прав и можно подтвердить исполнение (погасить требование)
     * это делается теми же трнзакциями что выдать и забрать долг у внутренних активов
     * И в момент погашения одновременно передается как имущество эмитенту
     */
    public static final int AS_OUTSIDE_OTHER_CLAIM = 49;

    ///////////////
    /**
     * inside CURRENCY
     * +++ деньги
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INSIDE_CURRENCY = 51;

    /**
     * inside CLAIMS
     * +++ требования и обязательства
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INSIDE_UTILITY = 52; // SERVICE

    /**
     * inside CLAIMS
     * +++ требования и обязательства
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INSIDE_SHARE = 53;

    /**
     * inside BONUS
     * +++ бонусы - для анонимов так же платежи возможны
     * === ASSET - без обмена на бирже и можно анонимам переводить
     */
    public static final int AS_INSIDE_BONUS = 54;

    /**
     * inside RIGHTS
     * +++ права и доступы
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     * можно вернуть право себе создателю и справо дается не в долг а как на харанение - и потом любой может забрать с хранения
     * 2 баланса - имущечтыо и хранение - при передаче? короче каждый может кто имеет право выдавать или назначать право
     * потом забирать назначение с баланса Хранить - получается как с движимым товарос
     */
    public static final int AS_INSIDE_ACCESS = 55;

    /**
     * inside VOTE
     * +++ права и доступы
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INSIDE_VOTE = 56;

    /**
     * bank guarantee - банковская гарантия
     * === полный аналог AS_INSIDE_ASSETS по действиям в протоколе - чисто для наименования другого - так как не требует действий 2-й стороны - скорее бухгалтерская единица?
     */

    public static final int AS_BANK_GUARANTEE = 60;
    /**
     * bank guarantee total - банковская гарантия общая сумма - так как не требует действий 2-й стороны - скорее бухгалтерская единица?
     * === полный аналог AS_INSIDE_ASSETS по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_BANK_GUARANTEE_TOTAL = 61;

    /**
     * NFT - Non Fungible Token. невзаимозаменяемый токен
     * === полный аналог AS_INSIDE_ASSETS по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_NON_FUNGIBLE = 65;
    public static final int AS_RELEASED_FUNGIBLE = 67;

    /**
     * INDEXES (FOREX etc.)
     * +++ требования и обязательства
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INDEX = 100;

    /**
     * inside CLAIMS
     * +++ требования и обязательства
     * === полный аналог ASSET по действиям в протоколе - чисто для наименования другого
     */
    public static final int AS_INSIDE_OTHER_CLAIM = 119;

    /**
     * ACCOUNTING
     * учетные единицы - нельзя на бирже торговать - они ничего не стоят, можно делать любые действия от своего имени
     * 4 : учетные единицы - не имеет стоимости и не может быть продано (бухгалтерский учет)
     */
    public static final int AS_ACCOUNTING = 123;

    /**
     * self-managed
     * === Не может управляться ни кем кроме обладателя актива
     * === доступны 4-ре баланса и у каждого работает Возврат - backward
     */
    public static final int AS_SELF_MANAGED_ACCOUNTING = 124;

    /**
     * accounting loan
     * +++ мой займ другому лицу - учетный, бухгалтерский учет
     * === подобно AS_SELF_MANAGED_ACCOUNTING - но долговой баланс - отражает требование к оплате
     */
    public static final int AS_SELF_ACCOUNTING_LOAN = 125;

    /**
     * mutual aid fund
     * +++ фонд взаимопомощи - учетный, бухгалтерский учет
     * === подобно AS_SELF_MANAGED_ACCOUNTING - по-идее тут без требований к оплате
     */
    public static final int AS_SELF_ACCOUNTING_MUTUAL_AID_FUND = 126;

    /**
     * cash fund
     * +++ денежный фонд - для учета взносов ТСЖ например - учетный, бухгалтерский учет
     * === подобно AS_SELF_MANAGED_ACCOUNTING - c требованиями к оплате и с автоматическим снятием требования (DEBT) при погашении
     */
    public static final int AS_SELF_ACCOUNTING_CASH_FUND = 127;

    /**
     * self-managed - direct OWN balances
     * === Не может управляться ни кем кроме обладателя актива
     * === доступны 4-ре баланса и у каждого работает Возврат - backward
     */
    public static final int AS_SELF_MANAGED_DIRECT_SEND = 128;
    /**
     * self-managed - direct OWN balances
     * === Не может управляться ни кем кроме обладателя актива
     * === доступны 4-ре баланса и у каждого работает Возврат - backward
     */
    public static final int AS_SELF_MANAGED_SHARE = 129;

    protected AssetCls(byte[] typeBytes, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description, int assetType) {
        super(typeBytes, appData, maker, name, icon, image, description);
        this.assetType = assetType;
    }

    public AssetCls(byte[] typeBytes) {
        super(typeBytes);
    }

    public AssetCls(int type, byte pars, byte[] appData, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description, int assetType) {
        this(new byte[TYPE_LENGTH], appData, maker, name, icon, image, description, assetType);
        this.typeBytes[0] = (byte) type;
        this.typeBytes[1] = pars;
    }

    protected AssetCls(byte[] typeBytes, byte[] appDataIn, PublicKeyAccount maker, String name, byte[] icon, byte[] image, String description, int assetType,
                       ExLinkAddress[] dexAwards) {
        this(typeBytes, appDataIn, maker, name, icon, image, description, assetType);
        this.dexAwards = dexAwards;
    }

    @Override
    protected int parseAppData() {
        int pos = super.parseAppData();
        if ((flags & APP_DATA_DEX_AWARDS_MASK) != 0) {
            int dexAwardsLen = Byte.toUnsignedInt(appData[pos++]) + 1;
            dexAwards = new ExLinkAddress[dexAwardsLen];
            for (int i = 0; i < dexAwardsLen; i++) {

                if (pos >= appData.length) {
                    // старая версия с 255 числом
                    ExLinkAddress[] dexAwardsTMP = new ExLinkAddress[dexAwardsLen - 1];
                    for (int k = 0; k < dexAwardsTMP.length; k++) {
                        dexAwardsTMP[k] = dexAwards[k];
                    }
                    dexAwards = dexAwardsTMP;
                    break;
                }

                dexAwards[i] = new ExLinkAddress(appData, pos);
                pos += dexAwards[i].length();


            }
        }
        return pos;
    }

    public static byte[] makeAppData(boolean iconAsURL, int iconType, boolean imageAsURL, int imageType,
                                     Long startDate, Long stopDate, String tags, ExLinkAddress[] dexAwards,
                                     boolean isUnTransferable, boolean isAnonimDenied) {
        long flags = dexAwards == null ? 0 : APP_DATA_DEX_AWARDS_MASK;
        if (isUnTransferable)
            flags |= APP_DATA_UNTRANSFERABLE_MASK;
        if (isAnonimDenied)
            flags |= APP_DATA_ANONIM_PROTECT_MASK;

        byte[] appData = ItemCls.makeAppData(flags,
                iconAsURL, iconType, imageAsURL, imageType, startDate, stopDate, tags);

        if (dexAwards == null)
            return appData;

        appData = Bytes.concat(appData, new byte[]{(byte) (dexAwards.length - 1)});
        for (ExLinkAddress exAddress : dexAwards) {
            appData = Bytes.concat(appData, exAddress.toBytes());
        }

        return appData;
    }

    //GETTERS/SETTERS

    public static int[] assetTypes;

    public static int[] assetTypes() {

        if (assetTypes != null)
            return assetTypes;

        int[] array = new int[]{

                AS_OUTSIDE_GOODS,
                AS_OUTSIDE_IMMOVABLE,
                AS_OUTSIDE_CURRENCY,
                AS_OUTSIDE_SERVICE,
                AS_OUTSIDE_BILL,
                AS_OUTSIDE_BILL_EX,
                AS_OUTSIDE_WORK_TIME_HOURS,
                AS_OUTSIDE_WORK_TIME_MINUTES,
                AS_OUTSIDE_SHARE,

                AS_MY_DEBT,

                AS_OUTSIDE_OTHER_CLAIM,

                AS_INSIDE_ASSETS,
                AS_INSIDE_CURRENCY,
                AS_INSIDE_UTILITY,
                AS_INSIDE_SHARE,
                AS_INSIDE_BONUS,
                AS_INSIDE_ACCESS,
                AS_INSIDE_VOTE,
                AS_BANK_GUARANTEE,
                AS_BANK_GUARANTEE_TOTAL,
                AS_NON_FUNGIBLE,
                AS_INDEX,
                AS_INSIDE_OTHER_CLAIM,

                AS_ACCOUNTING,
                AS_SELF_MANAGED_ACCOUNTING,
                AS_SELF_ACCOUNTING_LOAN,
                AS_SELF_ACCOUNTING_MUTUAL_AID_FUND,
                AS_SELF_ACCOUNTING_CASH_FUND,
                AS_SELF_MANAGED_DIRECT_SEND,
                AS_SELF_MANAGED_SHARE
        };

        if (BlockChain.TEST_MODE) {
            // AS_SELF_ACCOUNTING_CASH_FUND,
        }

        Arrays.sort(array);

        return array;
    }

    @Override
    public int getItemType() {
        return TYPE_KEY;
    }

    @Override
    public long START_KEY() {
        if (Transaction.parseHeightDBRef(dbRef) > BlockChain.START_KEY_UP)
            return BlockChain.START_KEY_UP_ITEMS;

        return START_KEY_OLD;
    }

    @Override
    public long MIN_START_KEY() {
        if (Transaction.parseHeightDBRef(dbRef) > BlockChain.START_KEY_UP)
            return BlockChain.START_KEY_UP_ITEMS;

        return MIN_START_KEY_OLD;
    }

    @Override
    public String getItemTypeName() {
        return "asset";
    }

    // DB
    @Override
    public ItemMap getDBMap(DCSet db) {
        return db.getItemAssetMap();
    }

    public abstract BigDecimal getReleased();
    public abstract BigDecimal getReleased(DCSet dc);

    public int getAssetType() {
        return this.assetType;
    }

    // https://unicode-table.com/ru/#23FC
    public static String charAssetType(long key, int assetType) {

        if (key < 10000) {
            return "";
        }

        switch (assetType) {
            case AS_OUTSIDE_GOODS:
                return "▲";
            case AS_OUTSIDE_IMMOVABLE:
                return "▼";
            case AS_ACCOUNTING:
                if (key == 555L || key == 666L || key == 777L)
                    return "♥";

                return "±";
            case AS_NON_FUNGIBLE:
                //return "\uD83C\uDFFA"; // амфора
                //return "💎"; // U+1F48E драгоценный камень
                return "\uD83C\uDFA8"; // палитра художника
                //return "\uD83C\uDFAC"; // кино-хлопушка
                //return "\uD83D\uDC18"; // слон
            case AS_INDEX:
                return "⤴";
            case AS_INSIDE_VOTE:
                return "✋";
            case AS_OUTSIDE_BILL:
                return "⬖"; // ⬒
            case AS_OUTSIDE_BILL_EX:
                return "⬖"; // ⬒
            case AS_OUTSIDE_SERVICE:
                return "⬔";
            case AS_INSIDE_BONUS:
                return "⮌";
            case AS_INSIDE_ACCESS:
                return "⛨";
            case AS_INSIDE_SHARE:
                return "◒";
            case AS_SELF_MANAGED_ACCOUNTING:
            case AS_SELF_ACCOUNTING_LOAN:
            case AS_SELF_ACCOUNTING_MUTUAL_AID_FUND:
            case AS_SELF_ACCOUNTING_CASH_FUND:
            case AS_SELF_MANAGED_DIRECT_SEND:
            case AS_SELF_MANAGED_SHARE:
                return "±";
            case AS_MY_DEBT:
                return "◆";
            case AS_OUTSIDE_WORK_TIME_HOURS:
                // 🕐🕜🕑🕝🕒🕞🕓🕟🕔🕠🕕🕡🕖🕢🕗🕣🕘🕤🕙🕥🕚🕦🕛🕧
                return "◕";
            case AS_OUTSIDE_WORK_TIME_MINUTES:
                return "◔";

        }

        if (assetType >= AS_OUTSIDE_CURRENCY
                && assetType <= AS_OUTSIDE_OTHER_CLAIM)
            return "◄";

        if (assetType == AS_INSIDE_ASSETS
                || assetType >= AS_INSIDE_CURRENCY
                && assetType <= AS_INSIDE_OTHER_CLAIM)
            return "►";

        // ● ⚫ ◆ █ ▇ ■ ◢ ◤ ◔ ◑ ◕ ⬛ ⬜ ⬤ ⛃
        return "⚫";

    }

    public String charAssetType() {
        return charAssetType(this.key, this.assetType);
    }

    @Override
    public String getName() {
		/*
		if (this.key == 1)
			return "ERA";
		 */

        return this.name;
    }

    @Override
    public int getMinNameLen() {
        return 1;
    }

    @Override
    public String viewName() {

        if (this.key < 100) {
            return this.name;
        } else if (key < getStartKey()) {
            return charAssetType() + this.name;
        }

        return charAssetType() + viewAssetTypeAbbrev() + ":" + this.name;

    }

    public PublicKeyAccount getMaker() {
        if (this.key > 10 && this.key < 100 && BlockChain.ASSET_OWNERS.containsKey(this.key)) {
            return BlockChain.ASSET_OWNERS.get(this.key);
        }

        return this.maker;
    }

    @Override
    public String getDescription() {
		/*
		if (this.key == 1)
			return "'Управляющая единица' (единица доли собственности) - подобна акции предприятия. Дает право собирать блоки тем чаще, чем больше Вы имеете их в обладании. Так же дает право удостоверять других персон и создавать новые статусы.";
		else if (this.key == 2)
			return "'Рабочая единица' (единица оплаты) - применяется для оплаты транзакций и как награда за сборку блоков.";
		else if (this.key == 3)
			return "'Доверяющая единица' (единица доверия) - применяется для оценки доверия и чести личности.";
		else if (this.key == 4)
			return "'Полезная единица' (единица пользы) - применяется для оценки пользы личности.";
		else if (this.key == 5)
			return "'Деловая единица' (единица деловитости) - применяется для оценки деловитости и активности личности.";
		 */

        return this.description;
    }

    @Override
    public String viewDescription() {
        switch ((int) this.key) {
            case 1:
                return "<p><img src=\"data:image/png;base64,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\" alt=\"\" width=\"216\" height=\"100\" /></p>\n" +
                        "<p><strong>FOIL</strong> is the official native digital accounting unit. In the Blockchain FOIL Network,&nbsp;&nbsp; Foil gives its holder additional rights to manage and govern the ecosystem.</p>\n" +
                        "<p>Native token FOIL has a set of features:</p>\n" +
                        "<p>&bull; Earn MVolt<br />&bull; Perform on chain creator transactions<br />&bull; FOIL governance and voting<br />&bull; Participate in FOIL launchpad IDOs<br />&bull; Participate in staking and liquidity mining<br />&bull; Total Supply 100 000 000</p>\n" +
                        "<p style=\"text-align: left;\"><em>Source: </em><a href=\"https://foil.network/\">FOIL Network</a></p>\n" +
                        "<p style=\"text-align: left;\">&nbsp;</p>\n" +
                        "<p style=\"text-align: left;\"><a href=\"https://scan.foil.network/\">FOIL DataVision</a> (block explorer)</p>\n" +
                        "<p style=\"text-align: left;\"><a href=\"https://oldscan.foil.network/index/blockexplorer.html?blocks\">FOIL old block explorer</a></p>\n" +
                        "<p style=\"text-align: left;\"><em>Source: <a href=\"https://foil.network/\">FOIL Network</a></em></p>\n" +
                        "<p>&nbsp;</p>";
            case 2:
                return "<p><img src=\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAA+gAAAFWCAYAAAD6/LfIAAAACXBIWXMAAAT+AAAE/gG8hC4xAAAgAElEQVR4nOy9eZsmRZnvH1GX/3f9XkH30XOccXCmC8edpQtEBVm62AQUrWoW2aFwR7bCBUHArmZREKGrQFlEpRv3DbrUmXFB6FaY7YzY9QpO1xuo+F2ZT+bzREbGcmdkRD6Z+Xw/19VdVZkZS0Zuccf3jju4EIIBEItLbhczjLNpwdk0Y2wmvdsGf88kvwqe/r2NMbZV5H8Pjhn+rvtZOI5Jx3G2zhg7LB1zkDF2JPs7/Z1xduR7l/KDuOgAAAAAAACANgEDHdTiss+LxLjexjibyYzw2cwI38FGRjPLf8/2jf42G9rlY5S/C8cR0hnyXssOOSB4asgnhvvh/bv4YdwZAAAAAAAAgCaBgQ5IXHWLSFVvwdlsZpBvE8xihDNJBbftq2Foq8a9TX3XpnEb8WuCs8OCscOZAX/whx/lR3DHAAAAAAAAAGIAAx1oueYmMZsZ44lbeuKmvtVqUDPFMFb+1u1jAQztvC5U9b2QhmrEF/NOXOgPJsa64OzAjz/MD+AOAgAAAAAAAIQABjpg190gBi7qLDXIE8N8exWDmumMXiWNTUUP7ubO9Aa4xc29kLfpfIx5c3YoVdgHKvuBn14AlR0AAAAAAABQHRjoE8j1nxHTqTHOiwY5M7mY501UxS1dk8amsPsa2nWMe6eKrjkfY95F9f8Q44mxLhJ1/cDPzpuCwQ4AAAAAAABwAgN9Qvj4p9Jo6nOZQb7DaEA7XNZZFbd0JU1tVT5usDht3i4V3T1AIFgWiG5foq7/4twpRI8HAAAAAAAAaIGB3mM++UmRGORzYmCUb03P1DVPPPs1iFu6kqbDweK0KjpN/RdqmetZwLl9vzxnat8E3IYAAAAAAAAAIjDQe8anrxdzgrP0H2Nsi9OAruJirv7tkaYtweK8jHhX3lojvmSgy2VtJIY642zfr86CsQ4AAAAAAMCkAwO9B3xmUeQGeWqUkxXp/NQ9A7/5pOl5sDhDWwvKIEBqrCf/XpiDsQ4AAAAAAMAkAgO9o3z2WrGNMbaYGuaZ+7pVAa4wT5xpjUwEi3Oq6HQ3d1cd1hNVnTG2/MLOqcMMAAAAAAAAMBHAQO8Qn7taTGcG+SJjbLvJgGU6BZgV/2YuA5po0Fdxcy/Up44qX0VFJyrkpmNM51Nyf3cOEJRVdGcdBnklS7gtJ8r62hmIBg8AAAAAAECfgYHeAW68SszkarngbEtaY4vRXfpJNKBJKjQrpvdJg2Bx9joU8h4dN5ivztjyr09HJHgAAAAAAAD6CAz0FnPTlWJBcLbAGNvBKhrdzGC0ltK3NVgc0c1dTkM6V183d+mYCMHi9Hlp2iDbnizbtvLr06ZWGAAAAAAAAKA3wEBvGTdfLqYztXyhztxyZjL+Krq5s4KROcoDweLsyre+rUnB4qxtoByXLNm2Ijhb/u0H4P4OAAAAAABA14GB3hJuuUxsy+aWLwgpErvv3HL5eJLrN+t/sDiqcWzKewzB4mwquvz3RmKoJ+7vv/0AgsoBAAAAAADQVWCgj5lbP5ZGY18SnM2nNalidDsMWEYwWplicJbmiRMNep80CBZHrINORTfXdTW5n/7lFBjqAAAAAAAAdA0Y6GNi6VLFMCeqxoxgwDKX8ecwoH0M+lqDAL6qvMHQJnkMOAxwkhFfxc19eAzdzV3Oy+LmbsprVXC29G/vh6EOAAAAAABAV4CB3jC3XZy6si8xxuZNhqBNNWYmgzGUil7DoHelobq5F+rsqoPmPEnnSjWOLce0IFic83xSRT0x1N8HQx0AAAAAAIC2AwO9IT5/0cAwF0xyZTcYgiHnljOTUUdRuJnJyBzlUdktXUlTW5U3G6Zec9iprvCCkrexrR0qOqFNqYq/dFyqqP/uvTDUAQAAAAAAaCsw0CPzhV1pVPbElf062WCzGoJEVVw+flKDxREM00kKFqfNS6nbnsRQ//1JiPoOAAAAAABA25jCFYnHFxfEEhcsUSyvSwuRx0Kk37k6RmLYVzpOgst/EMdcjHnb0stpDHmFSGOqj5rGdA7cUKatXFtbG+vD9Ntt9eaSJU09H2obELYn9+Lhd/xyc0mfCgAAAAAAADAuoKBH4EvzYi5Zm5qx4jrmqhLqcnNnWvW1eBzFDdxWB4qKPXHB4ohu7nKdqyjfDQeLs+WXrKO++Pv3TO1jAAAAAAAAgLEDAz0gt39UzAjGlhlnOxjBxdxlCDYdLI7s5i7lTTaoHWkQLK5YljYvi5u763wcdVtLDPU/vGfqIAMAAAAAAACMDRjoAfjyR0bzzLXGp8VgQ7A4d/l1DO0qxj3V6DWdD1X5HnOwOFvd0vnpL56A+ekAAAAAAACMA8xBr8mXLxRzTJpnLtlH9vnFluMKaeQ/iHONKfOyrfOlCXW25eczt9w2791YhwBz2EOmsc5hN9SbPHefeO+Q5uSb63YdF+zgW1/YnLPUCgAAAAAAABAJKOie3PHhdNm0FcbYjpKCrPyd41LRqW7uTFVyA7q5F85BTu9SitU28EhTW5WvoqITFXLTMabzMSraFVR0olt6ZcW/oiq/n3G2+OIslmUDAAAAAACgKWCge3Dnh8Ri4grMGNuiNbSVv3NMBqPLzZ0ZjD/5uBgGfcRgcWtSmgPSIEDiWn3Q1IZ2d3E2q7TJrNQ2O3zmsFc24h1u7vryWxEsrlS3bPtGcp//acfUMgMAAAAAAABEBwZ6Bb5ygdjGGFsRchA4k6HNKqjoDkPQphozk8FoMNCNdSCo2DYjVzGok6BjRwRnB4dGN2NHHryVjzUI2QX3p0H8kngBM4KzacbZDGNsOrmeZOPYckwPgsWZjksGVBZeOh5qOgAAAAAAADGBgU7krvMl1dxgLDkDrVkMNpeK3tJgceuMp4p3YngfEIwdfuALvJNG3LkPpoMvyb9EiZ/JjPitIdzcmfM6ti5YnO64VE1/6Xio6QAAAAAAAMQCBrqDu85LI7TvYy6VlUm/W1R0k3FsVdEdxp8u31AGvVS3xEA7kBrkiVs6YwfvvZ33Otr32d9Ir/0M40OjPfk5HKBhmva1qujG62hW0W0GtZpXyQOiiopOV+UTNX3u5eMQ6R0AAAAAAIDQwEC3cPd5Yi51ac/nmhNU6JKxRjSOh79XcHMv1aOiQe/IOzHI9+XG+PKd43VPbwtnPiJSQz0z1ocGu1ZFJyjfqoruvMfobul2Fb2GKi8422CMLbx83NS+rl9PAAAAAAAA2gQMdA33nCumM3f263TzxHOchrbyt5yOaYxpl5s7q290uwz6RB1NjK4DX70LBjmFuUfFTBqQjrO5NKJ/P4PFlQ38Aem66QePhZoOAAAAAABACGCgK3z13DSQWLJ82nbVCPIytJnDuDep7xYV3Wag6+qjM9Cz3zcygzxx4T9w9939dlmPzc6VdGBnVrDUWJ9L1XWiit6RYHE6w/9QEkDu4LFTGNABAAAAAACgJjDQJb56jlhgnC0LKRCcyYDOcRra+YHtCRa3kRrkjO37ym4OF+WInPZYOkViLlHXC3PXux0sTpdvMtCzeOiYqZVWXggAAAAAAAA6Agz0jN1ni2T5tHmjCzKTfq9qaLNmg8Vp6jNQyjnbd+cyjPJxcNrjIjHS56z3WIeCxRkM/9VDx0wtdO/qAAAAAAAA0A4m3kDffXa6vNa+oUs7syicTG8Qm47Lt48xWNxaFuRu3x33wn29DXzg22lk+ERVXxBMs55+t4LFFfLN/j6UeAwcejfWTAcAAAAAAKAqE22gL58lkmjc+4aRuF1KNetEsLhELU+8AZa/fF831ySfFD7whNgmGEvW119gw5UCOhksTj0umUYxe+jdmJcOAAAAAABAFSbWQF8+UyRG0V6T0awN5MWkfRZDm+rmzmRDh+rmzsqKaVbOoeS0vvQAxzzgDnLyk2n8g0XBxXbmNoDJBrUpr1CqvPVe5mzXn9+FeekAAAAAAABQmUgDfc+ZIjEa5m1Gs8vNXZfG6ebOogSLW2WcrXzxa/xAnTYB7eDkp8Ss4CJR1Xc677H2BYvTHbf653dhXjoAAAAAAAAUJspAv3enmGaDZcV2uIzmDgSLWxWMLX3hQbix95H3P725LVljPA0ql1HFLZ3q5i6n91HlbSq/dFwSC2HuL+/EeukAAAAAAADYmBgD/b6dbJtgIg0GpzOaqW7urjROFd1hLDmCxSXzy5cTV/bPP4Sgb5PAe5/ZTIIYJgr0orpUGzPcO1Q398L2Gqo8QUVPp2AkUexfeQeCxwEAAAAAAGBiIgz0+85gM4yzxAV8i0hMDqLR3KJgcUPD/LZvwDCfRN77zOZ0ZqQPDfWmg8VR3dwL9SkelwaPe+UdCB4HAAAAAACAjt4b6PefzmbFwK19ECVbZ6Azt4pOdXNnGmOpZrC4PYmr89LDMMwBYyd9NzXUE9f36zoULE4+biNZhu2Vt08hZgIAAAAAAAAKvTbQ7z+dDSK1F4zm7HyJRvMYg8WtJobYrY9gjjko857vbW5jnC2lwQ67ESxO3b7rlbcjwjsAAAAAAAAyvTXQHziNLQg+MM5ZybiuoKLb3NwNaVzHOeawryXLbd3yCIcbMHDynu9vzmTTH3ZUcUvP0anoPqq8c8qG3vDf9SqMdAAAAAAAAIb00kB/4FS2yDjbbXZZN6voNjd3pjFaAgaLW0/mF9+8l+8L0ARgwjjx2c25zFDf2vJgceqzASMdAAAAAACAjN4Z6F87la0Ixubdgd9aEywuCZy1fNMKXwrVBmByOWHf5lLigSHYKOK7TUWnurmzsMHiRukHv62++naslQ4AAAAAAECvDPSvfYCtMM7maYHfWhEsLlkfeuHGVcwzB+E4YX+6NFsyULWDqqI7jwsfLE41/Ff//W0w0gEAAAAAwGTTGwP966ewFcEl5Zy5Ar+NNVjchkgM88fgzg7iMbt/cy411DnbYnJLZ+MPFicb/jDSAQAAAADARDPVh5N/8BSWzGGdT/+QxxssYw+cjSwJTk5jOM42xlE+bg9jbBuMcxCbAzunknssUdNX1aLke970eykNIb1vOdnf8296cRPz0QEAAAAAwMTSeQX9wZOLbu3UIG5jCBa3nqjmN3yLY/1n0Dg7ntucFTx9VraydgaLk/et/sdboaQDAAAAAIDJo9MK+kPvHynnRnXbpuKZVHRrmurlMMH2M8ZmYJyDcbF2xlRy781k92KKTflmlPtfwZSfRzlQ0gEAAAAAwETSWQX9ofezRcakpdQ8grg1ECwuidC+8Jkn4M4O2sPxP9TMTW9PsDh5+67/+GcswQYAAAAAACaHTiro33gfS9xfdzOX8m3aro5JeKQhlLPGBZuBcQ7axq9PS+emzyT3qFo1cjwGw3E+qrylnL1v+tMmXN0BAAAAAMDE0DkFPTPO9+qirDPTWuXMoIgP01RQ0Wnl3PbpJ7GuOWg/x/1oc0lwdmteUZ26TVS7C+kd8Rj05WjqkJW/6z/fAiUdAAAAAAD0n04Z6A+/lyWBrl5gumXQmF8Qt8DB4jYYZ3OfegpzzUF3OPbHm7OMsURV3yJUA5oaLI7q5s5Gz1VFw/+E/3zLFJ4rAAAAAADQazrj4v7wSalL7tBdPGQQN15MRUxT4lCypBWMc9A1fvuB1PDdlt3DRaj3P8HN3TZNhOD+vu/vXt6cwc0FAAAAAAD6TCcU9IdPSo2Hg0wT1Kr0+3iCxe355Hf4YrATBmBMHPvjzSR43HzLgsXlf28Izmb+6+ipw7g/AAAAAABAH2m9gv7Nk9g0z9xvmU3RHl+wuF0wzkFf+O0H0vXHd7UsWFx+3JZMSZ/GDQcAAAAAAPpI+13cRWqcb89+J2F0c7cZAKY10c1pNjhjR3/iGY7gVaBX/MspaUC2o9N7PMTAF+G4Cu7v2+WpLgAAAAAAAPSJVhvoj5zIEkNhB7MYAyXoc8ZHmIwBc4pDTLCZjz/DD9LOBIBu8S+nTB3MlmIbzkv3UcuNRrh1sMyZfscbD25iYAwAAAAAAPSO1hroj5yYLqc2n/5h6dhXCOJGSEMKFpesbz778e9yzIMFveZfT07nes+m66W3J1hcvm/+jQexRjoAAAAAAOgXrQwS9+gJo+XUmGadZGPgtvjB4lav/x6HUQAmjnf9bHOF8cGAWZ1gcdQ10ZkULE5dnk1Jc/R/z0zBkwUAAAAAAPSC1inoj56QRmzXzjGlzHmNGCwOxjmYWP7t/VMLTLBV0/lT3d891XLb7wfeeAhB4wAAAAAAQD9oo4v7MGJ7AeL8VZmAweJ2LX4fxjmYbFIjnbFdrB3B4nKSd8WBSb82AAAAAACgH7TKQN87mwaFSyO2U+aWNxQsbtfis4jUDkDCv71vaiVZWnDYGOMNFpfnsf2NhxA0DgAAAAAAdJ/WGOh7d0hB4Uz4KHKW7AjB4nZdB+McgAKJka6ulT7GYHE58288hKBxAAAAAACg27QiSNzKDjYjEjdVPnBttwaAay5Y3K5r98M4B8DEO3+xuSA426sGdWPjCRaXbN8QjM3+3+0IGgcAAAAAALrJ2BX0leNZEuBpJZ1L6jG33GvOq2G7BIxzABz87r0DJZ2idjcQLC5hC2ds5f8gaBwAAAAAAOgobXBxX0rmnfvMLSe7vBPTZ2l2XbOfwTgHgMDvT8qM9MDB4mzHOdznt2fvFAAAAAAAADrHWF3cV49jc4KzZ9M/qriuS66vzOA6ywxu7br0UppdV/8AxjkAVXnHLzcXGGd75efM5eaubs+fRZube2k71+Qx+O3M/7t9SrtcIwAAAAAAAG1lbAb66nGpa/vhxC21ztxy1UDX/q528vUGPoxzAGrwjl+lQdr2kueMa4xuw9xyVjL8mdOg3xCcbfuff5o6gmsKAAAAAAC6wjhd3EvrnVNcZDVLLLmxpcnc2mGcA1CP379n5O7OqjzDzH0cdTUG6dnewgWDgg4AAAAAADrFWAz0x45li1ywHYWNxLnlMoGCxe256ocwzgEIQWakr445WFz+bO/433/eXMSFBQAAAAAAXaFxF/fHjmXbGGMHh67tGXXnlleZty6lX73yRwxrJwMQmLf/anNFcDbPXO7r1eeWu93ni8/6BmNs5n/+aeowrjEAAAAAAGg741DQV1TX9iE+qpnhGFve2XFrMM4BiMMf3jO1wAU7JGdOcnm3PfeEvNQ8uEjfNfCQAQAAAAAAnaBRA/3xY4qu7VS3dCP+c1kTw2HOo0QAAJ3Z7FlzLY2mpcLSatp9kkEPV3cAAAAAANAJGnNxf/wYi2u7/LvFzd10fMXo7xuCsZkrfsLg8gpAZN72/Gb63AvOttiWXLO5uZe2U6PEF93ik6juM3/9R7i6AwAAAACA9tKYgs7FyLWd6ooeKVjcLIxzAJrhjyemBvFsWtgYgsVJJO+eZVx2AAAAAADQZhox0L/1rtSdfIduX8i55QSX912X/yRV8QEADfHHE6cOcjFafs2Gz2oMVsO/mGbnG/6yiaktAAAAAACgtUR3cf/2u9i0YOww4wP13BqVPUCEdsuxq5f9DEHhABgXb31hc4VJkd1N7wLVdd3m5l7a7o7+vp5Edf/rP04dwY0AAAAAAADaRhMK+pIuantF91Q7btfXQzDOARgvL54wtcCkyO5jCBaXbN+avZMAAAAAAABoHVEN9G+/k80wxq5L//AwvClzy21LLGVsDOfAAgDGTfIsbtieYcqzzpkCYd66tP26N7ySBq8DAAAAAACgVcRW0NOgTBWDuA0IFyxu7rKfMbizAtACXjxh6ggToyUOvYLFKXh54wisjQ4AAAAAANpHNAP9iXdmgeGqBXEr/244poS+nNs+9nN2wKf+AIA4vHjCVPJM3mbLnOol4xksLmHHG15BwDgAAAAAANAuYiroy4VOsU8O9Qz6tY/9HHNNAWgjL85OLXHB1vKqGQ1ym/u7aTvdoMeyawAAAAAAoFVEMdCfeAdbYoNgTAN85pi655aXGR2TzDtHUDgA2s2COh/dRuBgcQlb3/DKJgbxAAAAAABAawi+zNqTb2fTjLHDIllWTV3+LD/ItWya/LuSh+l4ZVmlMy/9JdvXnmYGAOh464HNOcHZs+pzbFtCrbTdsDybmkaz5FqyLRnM2/bXN2PZNQAAAAAAMH5iKOjaZdVY5GBxUh6rMM4B6AYvzk4lz+p+tbINBotL3lWLuF0AAAAAAEAbCGqgP/U2tm24rBqzGNfxgsWtM4HONgAdY4ELtq6rcgPB4hJuxbJrAAAAAACgDYRW0IfzOY3GtU+udIN+4ZJfYUk1ALrEn3ak7uVpzIiQweJK2PPGXHQAAAAAADB2gs1Bz9Tzv2nnk6vzz5l9Prn2d/cc9j0XPw/1HICu8s9rmyuCs3n12facW146TjdvXd4uOPtfrx01dRg3EAAAAAAAGBchFfRUgaK4qKd4jAtY5rBvQAEDoPMs6qK6k1Z9UM/cx/1d4B0CAAAAAADGSxAD/em3pur5PPX4CMHiFi5+Hq7tAHQZ2dU9p8FgcQnzr38Vc9EBAAAAAMD4CKWgL5HXMA8fLG7/RS8gajsAfeCl49Oo7mvqqTQULA4qOgAAAAAAGCu1DXSXeh45WNwGlkgCoHcsZNNWUshquWF7CbtBDxUdAAAAAACMjdoGOllxokZkprqnDv5evugFhqBOAPSIl45PA7Uta8/Ib265/nfDMYhnAQAAAAAAxkWtKO7f+WdL5HZKJHb59wp5sEF/fH3XGoPSBUAPecuvN6cZYwcFZ1uZJuJ6/nspEjsr73NFfzfkjYjuAAAAAACgceoq6JXdy01u7h7B4uDaDkBPeen4NGDc8BkPMLfcnEafN94vAAAAAACgcbwV9GfewqYFT93LtzDCeubWNdFZZRV9bWGNzbbpdvnRG9mc4GxRXod5c7S+sv5fNjyS/BTqT1M+VY4d/hPl/Zp8EjanRulveJy3qo1D8MGvi8TwmnO1o/GaDdtRlNvReCy9za3p1X9KPtpzqcbya0dNtSrg4tG/2TzAGNvBHCq6dU10xvQqumVN9CS+heBs22tHTWF1CAAAAAAA0Bivq1HQYm6cU0jsmbwTzCWhirPSlHQ3opXq1gEm2ArjbEt+rvI5y+TnPNwv5I2j7aV8xOAfTwyM7KfceKVjeSHbwk9XmT7r1HcBLtKBlB2k86/Qjmk6NfihdJ1Kx1LKzPcbLgc1PZGN9B5uGVykg14vM2Y/H+P7xdYG0gUrHDfYviVb8k0/Fx4AAAAAAIAI1HFxXyC7pcvUDxa3uvBrdtCzztE49b/ZEc7Y8rCuyk9u+Dk8L/UnMR+1Pbn0k+cG/VAeVPJSypTz6StcjOIWuNrceM0qtuPoOhSvj+5ZcJal/lSKLJ0LneU2qsUvHT+VPOurpR3NBIuDmzsAAAAAAGgULwP9mbekytJW035yJHYZukHR5gjLy7IhlRvJVY2u0sCF8pNs/MtGnFwfxWjU5uXfBm1n6/CaSD8LbUFqc65ta1M7qvnI14o7jHfyQIG6n85Gy5XiwjNf9/1iMuhVuGBbX//q5oJ3rQEAAAAAAKiIn4IuWO1Oq2ewuD3zv2nvsmqJip6ofRQDSjXeVcOMZHRb1N9yPhYVXTbaJeO9b1xwv5hxDX5wndEuG9BEo9t6TdT0lnyG5Wv2k8qiXcdWquc5Lx+XRlNPVfQxBIuDgQ4AAAAAABqj8hz07x7NZvKgTUyd+2n4vYSQetCm3xW4SIM2tX59Ys7YkmBsXvq7MN+cMu98OHdZOjafd26dL62WSZxvXpq3Xm3ucncQbJo0B9zSjsX4CZwJJgZJuHJsjuuaqfvVMvOyNHUeXlrl3hjWhXYd1zsyzzoN7pfHvbC+a6TrqJlbrsU0b50LtuP1r25uw5JrAAAAmiAVExhLlhrNv0tJ32UmL9oURDnjcBbAefi9e+Yy3rr4MgAAOz5B4qovrWYy4gvGjlO0XZ7/DWt9ROVT/5sd/uEb2SrjbL5klCn4BIvLjXfZaM+DxVmNbkeQM+1AQf+Yrdrmpf22dlS2D1GNd+JAicl4L91HmvTDY90DLktdiFT+8nFTR47+zWYykHCr7XyM7xcljclwN2xfgpIOAACgLh/eIxLDeyZZJYSx9F9ujG8TXJo6qn7jLAPMRrI05z4k8m9hMp3toOBpX/ogGxjyh793KQx4ANpGJQP9uzPpKN5c+oehU0tWXy3KuSa/ts+RLZCo6Eyw+UoR2jNKSSoYkjrFvWi883TJNZfR2VfyU2f5uXJam5MNaVM76ox3NcK7fGwVo91gvJfORc/6a0dNrXToci+XVo8gqOU245xo0M+9/tXNaSy5ZuaJd6SdzDn5PT68Hw1qj+6+lJfFU/MpHE8tZ7TtwEXPt2+VgibZfbZYyIyCcO3L9Nts11GXT7Lvxsd46z3kAKjKhcsiWa52NjPEZxg3x2/SoeuXufrZuv1ZPlskD9id+Xfy7IdTA/5QtpLLAcbZge9fwvG96wHv/MVmshLOtO5MCsvL5kjvZHUbU97ZpTSO70KdfIz1DVyO6zuobStHfX2pqqCnLqZUQ662+/uI5Y/+tv3qeY6qog/fsBWNrqCu67rl2bi5Tn1V0JlivDKNUW0z3sttPhj0qOy6Lu/XKe4a472y4u4eLOtUh1hW0VmA9wvZ/V2wLWIwMNmlwYymmUuvi64dayg/Ms772V32xBrou89OjYS9ww2h2tcF8X7gjK1VPys9t10skud0Ps+YVRlQMOzX5ZO/m7/0NV6zG9Y/rr5ZHEiXM63avrr97uuUCDjbHr6xHQblR3aLbdn7cE5uAxWtEe3xDIZKwxnbLhjbzhi7Lvn7rG+KxGBfEZzte/Zi3vkpXu/7zmYyuL/bOnjI7M96xUHKPWtnTLVhJZjdNQZ0zGmIA0fOskMNQHlMzfWqb6CyCWxUDRK3WHyD1q+BYbBCDebUxbWIl4bnoPzk0k+e/yQcK/+UVdnCT2c+vLxftzxbP5kutY/aflI7ciG1j/SzdCwr5um6ZqZrN0ROJ4p14Uz/3DnLKqfpmnqek9a5cLgyD0EAACAASURBVD7Fd0UByzvFnEafN1zc7cyqe3XfK9e7Rbdf+92rnk+pfhOGcTAuUPt6fTekNCGXTi0ZExHqO+TGK8U20/GTyNU3p4NBO7zaV7fRfS8uj9s4/+g9YttHviqWPvJVcZgJ9rfEKJJjNXnh8Qy6ILfvYNv27Dz+duYj4sCZj4iufwNnCn/5tG+1Nh/7ctDv/Plm9e+ez3lT2yVUPh7leNnPTdVXz0Gygf697WwbF+kDq4Xa+S1gMvaLaVY/8i/dUc9zTvtvdlhev1k1oG2GFJd+sgrGu8vorGr8943h/Vvx/HXtWDTeudF4Z1Iaa1nqfkM+hcEdi/FeqIv+enbSnVSO6M4qvF9MBr2KxaBPg8XVqXuf4ULp/Kj4GEjENM4Pb38HHEnsPmtgMLGQAyQOPPIJ1pnlHp4SPga8dD54LxQpfVu82peWZqzTH+fvEQsfvUck99vfuEg9uwru68HO21GPYANQ5ryT98feuUfF4blHO2uop9+oqO+44raxG+jyoETNdxw5jTafeM+/X5oWP4MSB6oo6AVXjbo3dOF3e77dnZcmBnUvGWUKdPW7bLzrDDNtmQbDz2W894ULl4sqR5U21+63taOquJuMd+pAgcF4V9GlHw4kFK/poY6q5zmF5yqH9H6xpCEY9G1wV2sdT749NVCKcQEsBHu3VPvw1lOzus1SMFXE59rStsVR0GPeayMm3TtjSK6eV07ocy8OWP7GTc2q5/N3i+n5u8XC/D3icDZtpHy+xPvOZ5DS654ON0C6VTLU5zxqMhbe9/TmdOYR4DzHQOrrxtoZU60y0F143Q+aw0Ll0+r6NmMf0RX0bE7NAEqnlopNzRKpet7ZuS+Jip6cQ/qHwwA0ueZWNd5VxV1vvGvc3C2qbR/gIvUAKSjOFDd30jWjtqPOeJevk3psFaPdYLwbVPROG5qZir6/sNFtXJegGPQKnemQNIy3MkEddXbmQ8j70RP1gXL6zO6zUrWrYEDEal/KflM+n3uMB+vM3vIIP0ytbyBFBgr6iIKgElktblw9X7grfZ4OZoZ5qpZHMS50aTzyIbdv9XySc3927lGxb+5R0YX36ozzHHXb/Acp22CcM9J5xxyIdRCsbB8CPYMR60sz0L//T2nHdKtamO3FZOz82gz6cse6+0GZJBVdnW9eWb3N8HFd1ynu3HCsrsweUOpEyddDNpopxnu5zTntmhryMSruGuO9suJePJe1146a6kOwrGHHjPp+qaiW6/Lb+vpXPeZ09Z/yKH2gD3go5SfbRlYT+gJ3eaCFal8fRvkECxAnMcqTWN8aigwMdMbYNTfZ1fMISlVj6vnCXWJ24a6hYj5wY6cOLoYZBCoSMU3FQdWdiSEx96ho+7u18N1uQH1tSx+r4DUQZcCMmk+oARKPcmI8g17PLZ2N146aOkxV0MvKEVGloqJxcz904b92P+ouVUUfZ7A4k/HeK8SgE1Vl8EOnuA9/17U5K+YZRHHXGO+qZ0Sh/m6jvRdLGb18XDrIcIhkdKvvl3oGPYLFKXCHi2+LlJ+JUtCXB+r5aE6sRwfJRx3wUEViqE1az7tQnSolzSRPn5DRfltCqcXKPdSIer5wl5heuCtdFeAFdX65jM+zQ8bjGQxWDi3vpF0O7Nzbapf3wQBCxIFjhbEr6O+SA8SFOseG7umY5XQkWFx6/5ANdKvyzfT7qGkMxn4XI7ebGH64OhUsrl/MGAc3PNpRp7jnweK0ba60PVn9NuRTGNxxGO/SPbf21zf3Qj3P0aroJUzvIdt7TP69mB5u7mWsgWgKxDGQBttciVyB7HrEnjNTt9MlU7vEUFJq5BPFQI8U+Km8H5Hch+p5g0pVdPV811fELBfpvTnvqo9rf7Dztiehl+3Kh5i3xBYu2LM797Y2gFx5lRFiu3he7za4uGsHzqOq2809/2HSjPEZdJD2050GeubevsW0P1KwuI0L/60/aw6f9l+DiO4uA7husDjTsbo8qcHi+gIXevUsRrA4neIue0c0GSxOGUjohXousS95V9iUb8P7haqW67Zvef2rmzDSM57KAsRFnZvlYfQb0kySEbWoVfw8Oik+acidocG2GJ3Z4kBkqO+ZOZ/JdnMP9W2h3YvR1fNdXxFLLtVcqk9tfAY2xzllyJJm78697XJ3f//Tm9u4xYZJIbYv0d5Zf2Fuqg2xs2Y6F3zNkURHKA+dpsQFIkQF3aYYUTq1VIppemOc5wxdi6mGdJ5O/Wkx2qor7vZgcX2CJ26I6hxvw2BEzGBxRuNdc6ytTOd9pGTDGVv7n3/qlXqeuLkfKb0r7Ma1FopBrwADfUSpM9Zi5WcijKhMPV+MotAR83Eh53PD4+ECxEkcrlLfAIrMxMamuObG4tzzBtTi5YdujqOeX3SnmL7oTrEvWzLNCLm+oQYpHeUEa98w9U3c3ds0naja+ue6bdUGSNoTIE4m1KBqzTRRy9bhM/jloIFnnaagy51Rj46sOY3NoBe9cm9PkVX0cQSL0ynu3HAs8/xwtB3TS8AYLM6rzWsEi6Muz8aUvGj3UV/nTo8jWBwM9ByP9c/HqPxMhsopUvW8oBgR3P9LeKl6Ghz5xAgQl0ZyL20MU1+T8jPJCnol9bymUhVNPb/oDjHNRNox3mkoWw91cLH+IFAZnzTUsn3SDLZtybzb2sKM6XwiDZCM3UB/98/SZeWMqwzEGLwl5xNqgMSjnBjPoNdza2f9taNS8cluoD/7j3b39hSiSkUlUfo+/LvuLq1mg6KijyNYXCEwnGy894QkAqvX4IfBeKe4uVe5ZjbFfbhf5y5vuU5KWav/vb0VLlfByZZcCxosTkWTN9zcRwyVw3Gor0NoH1G7u2oP2DM3UM+t7evRQfJRB4hlx+zMVjL+axo2E2mgX/s5ST33uEdcaO7FKOp5apwPVKth1Oum3k1kQrWvz8Bb9XPYsXOvaMtyrmXvFuo5+g1StsFTUT9wHmhwPJR7egx1m5pPi+s7/Ca6FPQ5tVCfTi01TVZO79zbc3IVnWkMaJshPa5gcT1imtLmTkNaaj/5ujCz8b7GGbueMXY0Z2y9kAnVaHcY71rFvWy8923uuUrQYHGF95Vpu5hcl1YF7TqrY1WdLMc/ekLvDall06B6S4PFxTTQK7m5u/Y70kxqJPelBpWqKOr5xXeIaa4Y55S6BVOqIzyDZEXRlQ8xb8P+pZ0r43d154Jbn01qu1DP+4W5VkwlnI3yDBLzCTag21SaMT6DGsgG+iz1YxwoWNxGy1xjYjD8oJkuVquCxfWDcgT3Gm2u3T/4sc4Z28MFP/Pn503xX5w7NfuLc6aWkyXekjW0SW7ulIECQz6lcxrcD6v/dXQ/1XOJ9J1RUstNg4SGY0p/2w36iVfQn3rbIECcvK0DweJ6a6DfO5dGEh9EnA7TVvXTuPOJZ6ALjSdeqO+aJp+brmzVvNvoFNTznIjtm6rnt4RVzy/+clk5b2wKjgafgc0xThlypdmSTbcZGyc/VQxY5zPVp6L6emic5ytR7bw1eN0PEfOpXV9dGp98Aj3rFoYDPEYD/dk3p8oIbQ1VSqeWxr4P/Y5FXTpj3Jz+n2mnYY1sSGeMI1hcX+DZGuhMaZOqHgcaxT0ZUNrPBdvFGPtfP/kQ3/aTC/jiT8/n+5R8FvN8CmUKRX3XGe05VY32UX59V8/zYHGrhY1241oLxaCX2Pr6VzcnZtkuHdwx/7ylyk+fr9lSUyp5qOBrN3wrSoC4nGFHp6FgcZP2PrB+WwKrxcHVc9U470DwNWc5ofIJWN/FMavog/nnQnOWoQZQmhpwrMYMsb4lfNJoidO+tDQ+9WnPszO8h15nKXhOjH4vlJkUkN/vhd8tdTOmKf7ed/U8J1HRX2DZstl5GwzbQkiNyS37M9QktvTydpEbiPnv6rE9ITHQha4dWbFNuXIDG9o8mdd4gDO2b/+8u3P5vu9szrKpQQR57TWRsxaj4tXLrB6rex4198nqf7y19+p5zr5cPaS8a0p/S41t2q7Jb7ZFH+RxMDJIlHYqodvvSkPMR72ujjS9VDnvnUuVosJ6zdq2cvUf6revEU0+UQLESejfff71deUz05L5p9FR1XNnW2mo2L7LD94afO75stGtPdBzoEvjyscnTdSyHe8MRzlbsgC14wr8XF7/XHM+Xuetz2f8AeJ+ujnNeDneCvW8XfuD5eNKE7GcKPX1eW7LDAPEMYeLe2n+uZX6Bt3GBX+YDAP99P9MP+JrJfVUUcHlbbVd1wkq+lBdZkGuZ5vYRm1H1RsknTsu2B7O2JlcsP9v30V8dv8uvrR/gaz8LFV2XWfFn8Nj8wEV6ScX5WMzNuRgUX3n5eOmymui29RytW01v5fSlPOedDd37Ty3UOqrC8/R636qnI6VT7yUCX059jRU1XSwLWpn9pZH+eFgKg4tzSQFijOr5+HV4g0e2MC75PY0gBlpOkgwsaKhfKIqin5K6vhWkAm1ygi9rVox/zyGWhzA/d+/PhGfnZbVt/BN1Cro+45iyQhMeWTRoDKVRgkIx2lGFiZFPc9JPnAvDFVRjUo+HEHSqd+K+htDce8NyVQN3fnr2ykx8g4Izg4kHh3fuVyzXA+R9z+9OVAZLOq385pZFHch5aG5zMuvvn2q19NFNGhV9NLfPmq51N7S9h1veGVz+q9vnrh2zimvs9p+5ad3Cvq9OxU100/5Ca6sEfJpQm1ay9umAeVnIlzcr7tBzDJeDooXUXVa/npA9fyS29PnZXcEtTh4Pk2VE/mdsX3nipipIGqERIrKz5mwjF6EUIufP3OqDR51xfnn41S3Nd/32mWHUarD1Tfss+420HO3kEKnlPgxrvHRnigDPVHRf/D3bE1wtsN0sVRDUrXMeO76rDHeXUa7mqfGJuwFF39ZzOSu6yY39yywR3L/HXjqah5sBJQLvjQoSHomKEY38ZqZ8om5VmzLSQ10m3FOMLrLf5sM+gGzEzi4yJ5+azlAnIz2O+Dx8dPiekHp9o+29THa9mj5zoodjsbS6MtuojObqOg7gtRXRzGfSZmDvuQz8OaFYOuMh/uWXXJ7Oh+6/L4Od09XH6TU4NW+TdXXL03j08FOflLMZmVXn+qjw51P7Ck7VIKet9f94DGgE7O+wQYcXPWtL24W7A+Ti3vRRcL0u4rhOILLzcS4tysM3Z+HRrfhZw5Xfyrp05+5+zOT3OV9gsX1AC4yxax4/km09VUmBm7r376OzzxxLV968ppwxvkpT6ZrrxfWh5WvR+E6C801YcWfQ0z3R/Hn8ivvmDxVN3dzL2w0vYcs97jJ5d1wLSZ1ubWhMeJ6v/u4wYfKpy8DjSbuO4PN6ZYR0p13jOtELUe37bPfbkRRK3hA1akvIc2WvkdyX/ysJnK7g5rtuxRSPc+W8TUOLLLYz45HPi586qstO259xzEdrDRgRg4W53PeLZh/nqE5b81RfudYHZ97MVR9dbiOa+jZ0fHaUcUl+kwK+vBhsqnoFGWqdCL6NJNonKcq+nNvStfK3iEr5U6ltKbreklxF/pje8Jsdk77c9f11U820DEUbGnQjgO3Kor6Le8rPXMuN/fR/ZO46E+iep7TdLC4iTfQCwRSbCOpOGmavbNsdteB3gTzcj/rPspEoGuiI8unKbUpuc631smg4nn3PVDccO55VFfTAetfX+IrnvUscemXxBzjbGel+kZUxDXbNjKVOfl3JAk4JnhxVSMlzTbp30zqzk2sr5cbcT1X3sY9lzhjs9b6ElVT53mP9o/9uT/mp5vbOGNbQpy3a3+wfCKo29RyotTXv+zSEn0lA33/UenDPogASPzgFvBJM6EGOhtcpCUhzUVnivv10BgjGu1O13VLPrLPvOiPgb7yyA280aXGTv32YI6eacCE7LouP07ysVk5BuN9+dC7JnZONMs+kkU3d8s7qTAAaTHiC2mKeW+f0HnohYGJmh0baz4utOX4fYc6xf1npMGXitF6HeetbV9XGo/2JXRImlpdYlAOcWBIxtMQ662BXlk9r9++wb7bl34p9WywG/uh7odqg5T7s/tl37cW/ePdJJx/f3qOc9m/nZHqS0ujYedekQTYbfLZCL/KiD2fNijoxUCVPuftgjrI7iMix6gvMZ8W1Ld0/+gUdLcyYlCZSi8EwnHZ7xOxNIkOm4rOVANPMqSpRncdxb0PfPNz9T56XojBesQuo7vwMnBdM43irhrvmev+JKvnLBvs25v/EVgtNxn0kzgP3TzfNqIiHmikf7YnRpSkZpYDIMUYIPEZiDHk00hnNonk/vmL3O0SUEnpcyT39H5rSHUKqp5ndde6tkdQi1355DFiVuoa5TJPXZ1OBUjabOX8B8S27JznTcc3Nagq7W9s8OqUJ9PBiq36+tqDxekgnPfG82eNf0nbfzk5dZFude/96N9sJgNIz8rbIqvbh159+1Qn44Po5qDPqufHDb/bqHCHrJ3/RzbJil+qomc/9ftHxw2Ql2eTl9qSl2djo2Pln9zwc7hfTQ8qcdpjA5UhN5x59kHgrHjNtNfEcO1c10w6bungMROtnifz0I+YgrXIz1fhd0t+hWfSkH7S3Ny/88/2AHEy2rYN5Z3jyke3f7Ct8/OE7z8j/WZsJbevf1uFTTOiSbVp8D6oV187o3x6GSjOpJ477Rz/9g2mnn/siyK5Jtcx3/r6nIMmTVb2bckgzuPX86XHr48nHjx1FT/81FU88bA5obT8qA5zfUOmaXLwqvxN1tVXk9DrvPs9rSUozqVOidfJhXQd2xIboDJaA73wl6GDan1p0Tq1ORPr3p6Trosuip0Io1GWoRpzNuM9Nw51x+rLhHlegyWnIc2K12V4rM54Z8WfQ8r3x/rLx02FVBy6zD6mPjOm95D6XJmMeJtBLyZuHvqM7l3u6tho0xCP88nHsr/TRtT9p6cDDIuUY8fUvuZ8sm2feaJRV1eSIRTonu5rJPeCwVxRgDSmMeSz/rXbgqrny1711W3zzyeZX3r04x/nS49/PGjQOytPX5k+Z7Ol4Knu+hbxeGdoaO7ZcBiBEYLFddYIHAPk/pL2vqp+L/bIQBea9c8dKnpNZQojTwOWVAPabUgr+/P0ys9Ceo3iLhvvalmAzhmrIvE+2aFrx/yD4LymqvEuXzO74t7oPPuWU3inUIzu0t/ugUV5n/ad2WPcHa1ASlUk5afrCvoiE2UPBufAqo8yEeiaKJSC4cSEi7KBXlORsZ03ybOkS1z/Gfvcc5+BDUc+IdXzUt296ktMY8hnNTFKHvvEWNYBT4z0g6lnbMRBVWeawbYm37tDIzDY4KI9G9gxdPwG+HXbaO+Zfhjoz/2D4ab2MdZoaTbOexEjTwln/MdARZcNMRm6+q0/zqa4D38WFHeo6JUR0nrEujbP8yNeM10+OsU9Uc9fOh7qec7Lx00dzBUDm1ouUxiAtBnnhn1v+MvmJKnoxnONqb66qKD8dHZA5YHTyup5MMXLo4Pk0zdoav65xKjzHEaRsXLzFaJv74J6BnO19g2unldOEep+GBy3uvoJvvDYJ5pTzXVkRvpt1oMCDapaaPK9OxN12k6PVNomecuvN2nT4+oPAqckz+qrb5/q7OCJqqCPFrg3QVCZqJ1aLuDerlBQ0eV/qorKqEZ3HcUdkEkilCbqudZ1XXdN1DZX2p7sRTH4AfW8jFFFL/1tendZ0mgM+kky0FMFPZK6TUpTV/nZO9vZYF7Lww6Otl3KZnSMAZKa0xWa7swejjq9orytN4HicvU82DPoThNSPV+QjcKG1WKWG+eEqjbC01emq9msBzlv3bbAA12+nPJEGnOgYATq61tdhDKc9/qvzprs2D9knFMPaNsqpGnUWys0qoE+Y3VLN/xuA24hdFIVnWUqugbVeNfNN88JFSwO0OAiU7VUjwQ2+lkIFme7JoZrZ7hmh16chXqukqx5b9mn/92e3wh9+r7OPS3wnbewbTr3ahce6iv9w+qninTOiHrgtLTOxqjMJWIqSPXSNGqg37yXEIwrlPEwyKdPkdxJBnOAaQ8J6w98Pqh6bozT4FXfavfI/tVPtsc4z+HC4VEQcVC1KUIFIatw3rBj6Ayvjdd9Vb28Tns2FA105cY2urlbVCYlP+3v0oWBW4iK6iZtMqQzdCq6yXivqrgDGmc+ki5psrOS67oy/7+G4k4KFjWBDD+atdVymkE/EQa6ep6R5jUmATOPRFG8Rr920YgyrvYRUPFat+0PUc6nn2w0QFzOaGWHOMqvTC+8aa7/tJhlwr7uuddUFHP7BlPPL/tCqvyXXKpDTZ1x5JMMLrbOOM8gD4DECBaXeBpSy69Bcf1za32DBIuDHUNn1nNaVBlaPv0w0J97Uzq3betwj0PFVX9n1ZWpjQ/+CTe2SqKi86wzQXZzVvdn6JRYneKuM97h5l6J0XrEys/yNSMEi9MZ70J77NofT+zu/JqYvHT8aB56jnUajuE4o0FfTrP1DX/Z7PzyXQSqDUT4KVUHXVMUKPk40nTKQH/g1NRzoayea8/bO1icdnlCczn2YgyMy+WQ7ObugnAv9kVBrxS53WdgQyK0er4Upb60NAurnxzvnHMTT1/Jk4HP0jM4Bvf/mOiDkLnq63/esGPo1B/g120z59MbBX3keiBtjBgsDoaFCZF9XCwvB6rRbnJd1+VjcpcHZs56WGzjWceZqn7XCRanKO6Ye25BdnMnq+Wm7erzoN83CSq6O05JffU1cUk+YsrHBVH56ZoRVTBeyO1b7TotpoP0Hh2kCqrTuDpMxXI9FJkK9+JWwjGt5uOflqKfh+oL2PMJp55/Pp2DbFX+SfjdD7etfGo80dorYI/95Deo2iZ2RKmvIZ9fng2RhMI/rxEDxMn43IsSr7yj29dGNtDLHS/byRNUJkenFqNOBjIVPXU15OMMFgec8KRjIZSBjopGt7bN3enXfn8SPgwOSu+Y0hQRilpON+h7b6DrzjGCun2Qcu0oZVsM2c4Y6F87lVmXudLhESxulWuWQQqlrEnHtcNAl4gRLK4Hkdy1BnOwZ7C4bf3+L8SZex5pCo6J9ZVP8S4MmmufhchTZxrh1G/bnztysDj6IKXZ6wioVJh6QNvm2N/pAHFMMdALHZYGgsXBuLBjdNFqMlgcMHPOQ2LodqprvzxAHNMEi9O2tXotDNcO6jmZA1RXdsKUnNJxGoO+1wb6M0ez4gh4PHX7IBOsuototYGCLl2r0bPuowbR0iwV5k7HK2esBrrPYJIXHXZzz9XzSO7/OkKq59NVAil61ddsPLR13rmKM76H15QWnzThCR6EjDCYDGhog5CHuk59vDZDA53LHxSDslraV09tx41t4Yx/T10a152GdEaMYHHADs+DNhna0Tb4oRrvvFqwuLXfvQ/quQvVS6e2Wu426PsUvVmH0agNqFRtfPh37MiHfze6djEUL17V1W5M5Op5jfmRzv1csNXF76eRzrXqUw1FsZTPmALEJZHcj6gxKSKrOJ19F2iXOvNoK0I5LIJ6PjCSY9TXns/ayqfGc29X5ekrq9UzRrC4iOhXonLW1ztYHOwYOsXvi/87o4g5n/4Y6EYXOoeKq93n7siun/uSh0IyYXAlam8w1/UqweKAlg9+PY3cPl+lzYvXzDNY3OCYrozUj5U/7UjXJrWu+0pV2I0GfZH68x7bDW39cx30UfKD0jbSmr2GfJxp9u7ohIpeNl7Iqh4pWNyGpGBal1k1l6PZpk89bpdDo4oeQS3upIv7Jz4lCtMpYrj/KwT1BOOaVU0C13eA3gOlUzTl/t+wsV547mI+61k+EErohB3gd+fTDwP9B3+vH+2lBosj3fA9a7gmSFR0rumkMoL67XJdJwWLAzaWyAMlpjZn9vTafARb/deTp9zr+oKcg0aD3KaWm7arz4Wy73//ub+R3LnO6AjfoZO/DcP7PJTipdS31dfqax9gC0xUDzhWUUFavu5ZfnjPmWlgrS3ENNXK0Q2+jIdy+dUUmcF50+7FriroZkMzVJ9glE9Q9fzy29J72LkSUWXc98Pa3k93Qz2vhMdgKKHNowlzp35LTFsDNFIHNl0FjdJs/PIc9MUovPXAKECcl/jncS/+5Z3d9zLNFfT0Y2LsvNoap7rKxBgCxJGRg5BxXbA4idDB4oCe8x8Yqeeyws2EZiDE03gfUk6PuecVoAaj9FDLTQZ9n+ehW+f3BVK3Dxp+t5bjqfy0XeUsLnOlOcB93tZgcYl6vpz9rl3FhV6OZlt501i/+1wwa2c6sFrcuUjun/jkSD2PoTpr9of+lpXUc0vZIdXiZf3mbhJw6kyJfRdFjXBvfYcV6qbbVj1YHOwYOtql72JMM8rofIA4JhnoZtcDS2JqsDhNRxZuIUScKropwrvDeCcr7kDHKICfpR3lwHDyfPNKbu7Fa7f62w9gxLYiWrdW4zQcg4qeQlDi+xrJ/btqgDiZsOq2fH8f8VLBfJSflvH1U7Ilz0z19VGvyvsT9TxXtPSDFX4qmSlNaxR0n8Gkqtx8uejau2BJO4jmkRGhfdfv+2LQuecJc6SyNXjdD4Nt649+htuXLWsnw1WCqp53lDRhmA1WX81hmnxgx9Aprn+uSRb4OvVi8GRgoIuRq5/VBZQZ9lVX22FkVGOpZLQZDGnZENcq7pLx7lTcQYkL7h9Fbte1ua4dddfMZLw7FHeo59U5aDPOKUa3us/q8t5yt+kaDOafO9LXVao+9LtCp8cc5I9atj1JKxX0r5+S3kP6Za502/wUrw1F+dMrHD7lGPL51FNjdwMuvQtS4qnFnXFz/+QnRGkpv1BtZSDot+yK28ScNvBjuPqO0hT/7KR6zuNPncnLaRKnERhswLdHRmBDmL+1oQbUitt6ZKBTOiqGRvQJFnfuyzDQq7Azi+judJM27c9QjXfVaLeqhyBvU23gPkqb669ZWUUvquzDbau/ORXqeVX+tMPcZg5DW7/Pkia7xv10cfdZ/1yfjw3VU+iIKU0gFaetgymJer4lhH4d4AAAIABJREFUVPsagsUtXbdvoJ7vmUvnbm4vpPEqR7Nt9OvYXQ5vWilGcvcZ2NBhuU69WcovsPv/+n1fiqOeE8o24jltJ/R5NE5k9385jdYTNCBGO4ZaX1dgTSUfKOh0SAFmve5FfT69MtCtHafAweKwsL8HprnHMYLFDY12UOBD94ptXLB503xz50CJqc2ZPT0XaafSOL8OODnElBc9WS03bVefj9HffVXQh52fUEqKJp/CYMqHf1fuAAVQvAYM8tmu2zVOHjyFTXPds+6jZprPe/3afbygng/ThC1Hpi0dJn094qjFnTDQdeq5lfp9gxieYHPGPaH6MuV89j/6Wd7/1Yg8BkMNaaIJDKc9LkZTsALV12HPrP/inCmsREXgrS9sJl6nJe+WKMHiMv78rn4sQ5wa6Nzm2mTpyBYgqkwxozj2mVRFFwMVveC6blbyageLA0q75oMkmvaLHCxuee10fAxqcIRq2FHVclMa3t8gcdrz8lSdTGl0H9UNzTbvstX9KztaN6CyJM/1pys/9kwVZUg1kPTrn3uUY8mnNQa6pyJjT6O/F7vi4l7wCpOJoHgFV8+vWBJz1AjRgdXiTqvnUVRyj3ICoZ2iEzFYHNzb6RSnHgQaDLXk04sAcUxZB91KwGBxuLE90aroilFYcF1nPi7XxZ9gwIXLqRvocO55lcGPmsHiNrjoV5TYpuFiZPiVBrMMRrh1uoddidcHUusw353JAsT5KBMOlLbVKSwHffMm1rc1AyoPnpy283XGA6gdG4eBdO2+koE0E0x1MufTlu9+4R7zGUyqQOs8NFSM6rmHsaND075x1HOfgUINFe6HjUc/28ngcOz8+0Vp4CjQlCFXmpjvgOIgY6j7QbfNPJgM9FQKQu51Lxb/7I2NOfXDv5Maz97xHO1jhn00tR1KoCe5is5simyGbBRyh/FuVNyBzKLL4yBSsLjlF+agntckbT+bce6llht+/z+HNnulousi08dQqnQfVq4x2gMrP21S0IfGS6i50Jp8dFNlCp3bYO0rHfepp1uzTrR2VYeU8Goxu+Wy1kdyLy7lV02pMmJol/V7bw8+95yV5p/rjvCrr41OGucZQwM98JQhc5rB/pj9mGorUXnc030MQtYQg+9LjAF+/XXsj4Ged1DIHQJDI1YIFocbux5LRkPRZLQ7jHemifA+lhO7VMzd+jHRug/fR+8R01wMOraVXdczPIPFbTCo5yGwvnNMhnYKYdBSMzjWt3no2kHcHK8BPU2aD/1ee51GBnoYFUdN0woDKlPP56nn6BfEja9du7+o+t07Z5+7GShYXJtcDsuDQPHUYtbmSO6f+njFueeeipe0P7h6fuWt6TkYvZYiuu132UDXEtj9X0fMILf+6/drj7O/AX5xbj/mODeBbtpfhKkzMv0z0IfY1CPD71aFvQzUwBrMvTqI6K7mEDJYHBeenW4PPn+RmPn8xWL5tktEcl88mzgKNFNyJRbzIBda9bvqQInJc4GV0i8/fxbU8wAU5qAbDXKLwl4xWFzfDHTyHGUvFXLww2TEaTt1AZWfthhQJWUxlPIj5aMzkPTX1kdh0jFI05oOkxrJvUQ4tTinzQq63WAOq3jFV88jKHSGfDYe6ah7e4Z7SosOn/YtbotioJ/2mNCfT/36pmjew72Z4xybtyUB4uQBtECDobZ8/vzu/gyevC5xXzQNFiUfnuE+IbWAsLSq4bg8r7MPQUGvjWBLnLG9STZJm+ZtW7heWdML+We+XxR3ltLbrm8AvvRRMc04WxCcLXDOtgulLm1i/m4xzTlblNtPbSd5u9yuQtiP1bZ5fhxj66Kja6y2jRdnpw6+9cBmWiv1GTFhevfZ0kv7ZnqmsMy42k2339nWxfeMqfN2mDv6Wj5lS/vHbqA/9H42mwRqrdy+mnaxnPfaNc9p503OONuXWI4ln7Z985P67HDeI9Xa17S/lQZ6rp5Tn50Az3+MuecJcxXvRV3djNsM+7tuABQGkL2urd87I5aCXpqiU7W+OhIVXagd0sE3CzYMHfPUA91957A9CM9trwZPCkHiKDaZ1ZW9Yl7AD1lFV1yih4pufZfr8BfnjgvF3Jc/IlY4Y/+PMbZ7GESnWGbstTIrkbm2G9ckruK5oJtvXjy24Oa+9MuzoZ6HxKZ8UxT20juNqMR3me9tZ9tKLqRxlCpTp6e4Pbzy0wYFvWi8hDrH4jaTgeQMrlSxnBTl+WqjgV7AZ6oEkba6uNMM5jCKVxT1/Mpb0+kZWwsb67nGUtN0ffB1YDT5tFWNNN+/hMcy0M2DYMT6OmOqFP+EezudwfrnjsN9ps4Y3k29Gjx5ndYd06QYKaMbhVEpmsKONdBDkUR052yvUZFlmTGYK7nKEKJWCVaE3BDcdb7YJhIFmrM5xgcfU1WkF7xQZsx5SpXY9ZVU6V+s6nHg8lywKu6Dn8kam51ewqV1CHaIcX1U5Zpque73Prm4F9bIjqVUmT6sH/odO/LEO4Opmbp8ih38hknUc3n+JKl9q6sM+w3qOcsHSSO2L/tkewLE5Qy/MZFUHJnWRXL/9PXC6bGho0ZbxVLPtdMzhmWHeXZ09EpBZ/TzLqap1r4xlc3RgENFjxhtGpcHKeJo0RFs1qN9K48CSvdvr67NlLp+ILldHIqi7ri2uS93mURF52Iwl67yHGimKO75MZbl2arw1XPF9D3nioW7zxMHmGB/S5YO4mJknDPlPsnLZg3Ofacgq+eqUq62k7rd33Mh3ROrQzPJDL0RjN4QtncVQS2Xtvcpirv+XAIpVVI+toG54sBuYOVn5fixXi/9QFwg5SfLRxe5nd27U+gNnDCqaV7fNrocajtxXu1LyKeFkdz9IrfL+3Xb9GnW93w5ytxzJs8/d9bX5xz1aQ49ckM0JbgRuCMwYLC2Kv4Zrc24ZhAsQH2z48pbf37eFAx0OqMBfk2SCM9t7xR0PZZRDHkkyqiiB1ZigZZkjvKtumtTUncN6q9WRc8V94osnyVmk3nlbKCWb3Eqzur+Ft0vF98xUM/lOg/rp1HBq7R5aX9x0/rPz4N6HguH8j1AfY/Jx8nvPg/VoYNYVSq1TYZUVKrO/4PfhzWQ8jMWj4eH3s8WKAp+TeVn9ernjJ3jWS9FsZrq1MYOk7tOHsqa5V5sjUdNop7bPDa01FO8Yg42mweYAit0Ep1Wzy9Q10D3aQuf9o2kOp/+mChP0amq2OrQpMnew/ACJvK25zfL0+NyzO1bDSWfQz0KEMfkOei2UQnj/Epba+qPw9yNkAyW4NowqrcZqmpLVtwJ3HcG23bvTrG4Z04kncAXmGDzqeqslDmkouI8RoaR20vtqKuzMu+fSz9152c5f6jncTjg481DUMtLf/fMU6ig/sVQqghTn9LvhquPVUP5GYvCyTXPus+941AmbO+T4rWtV47puNYZ6Gok94BqsWm/c5CrQbT3A/XZqXje68t3xFHPr7w19Uoofp89PCA8zrHr889TAz2YmukoTNrfSIC4QtnU+jrPu5AKNgwdY6wDGe195+ndEutExsWULogJZbDJdgwnHgfqMfdqunxUGulbNgCHrusBgsWZeOBUtnD/6WwfZ6kL+27O2FbuWFt9WBfTvdESw+aS28U0U9Y9dw1eWAc/NMY7E9o2P/TTC6K5A040JgOaamgXflcbsnxcL+agf/+fNAHiZFzPK/V5dkX3FZqlOX3eFeY0jV+vb7yPoJ77DHYU9++5+gfWtp01leN1bfX1bavLIdnNvYDfN6oVgeJk9bwyHsZZTPWcC8egh99AoTPNNz/XungKVSm3W/33DCVN1ABxxKk+BTwHHODeTiRZIUw9MtQgcIHR/t5dm9dZOwmS+4DNHcro5l7PlQhQSAz0gSv2ltDB4lQePDldkm8hc2PfMiorW45CSDeD4rrN5Hqp943i5t4CFnMvAMHK7eN0XVfOz5SP3GTZQdq5oiAglneSyc3d+u7Tu8m3LjCUJ1plWdceLvc0bZrRNlfnbfjh1ZXjctUl1LdRBf3h97Jk+oxxCUVy+9q/rxs6hT4n8XxKpyGpZQdu308801qD5qBsrJJd+SUc97RMKwz05H6ovBSiPh9KW63FUs8zyF4JIabgZHTfvVmkz72RQFOGSu37/UvjvAe0y0S7rm29KUMw0OmMnlF6+9L3l+mhgW7oiFo7XAQjvJQexnoUEhV935vZsuDsVi9DUmO8y7sfPolNi6mhUb5dzd5VltYotRvvY33IPvbFwbrnTLp/OSs/F87zq2i8M8HWfnxh50fnW43a+TB1RgxG9wCT4a5s7wnO9c+1c8mqd/Jc973egPcpW5+maQV9MfdMCNW+mu/18lU/1HgejJjRrvPrKruaiNxml0PjoJDPAJQjHz/VOiCfWXSr5xUGHEb7zQM6sadqzXrVd7LXP2fyYFHNQVV6mkjvgTNWi8vs+Qwu6tDnk74r13923lSnAwQ2jH6AP9QgcHlb7wz0KecRlieYG363HAcDJA7L8pw6JrW51uVa2m5ygU/Sf/M9LF2znAu2mwu2veC6rhyr++kq07B/3Gt/L8puvXI7llz3FWOsapsr+zH3PC6Fdw/V5d2YxrC9Z1SaO6v9BtDaxtrp+dDv2WFXPl7uq6NfGzOgUvWcDYJPVoXcvoPVPYwKfUal9c+19XVva3OHiVY36nlL6NrqlsvEuFV0q8cGCddxo/2Jeh6tr3fVLeX55476+O0v04f+6w6vKS066O3bivXPA7jyQz0n8vZfDQLEeU2V8CkwUfaO6VeAODY00D06qMz0u21ffzuyY2XuldSoXXbNly4Z0m5Dcr6wX05fmm+ehTJ3GO9DXAMFY+Cyz4tC57nUPob248p8+8J2R5tnf6/9COp5VEzvLuv9ZjrOkr5nxvpgfp9Hx4aaJtl2/h9JHbihCuP6gPvUt0EWScGt6hnwLvWcja5tuTWd7avbpu9otbZDq5sb77yn9fm4yskZm4H+mevEwnC9e5/7qvr9GXWw2TX/3HAvuvKkbOu0gXbBfeXl/mre09Z8JGK1m/H7JEOurzMfDgOdiG7+ebBBYH2a3gWIS5giKt9ex1AVdlAfrkR0Z1IwMmZTb5XrQ1W/bSo6rxssbnwscDFyPSWdv4LOECcY71DPG4KqnPso7H0agHz2Hx0B4mTqKVXUeZ1HYio/q8fFj7T98ElsGxPFZTFJVDOQ1gnqOdN6Dfi0rz1Nazu0N64WI7mr+Cg/DsYZyX2JWt8Aitfa7jujDzYP29Krvn6DFIe++Tk+bu++WmiNJpn6CrMpTZz7QTNQ4+xLUgeO9akhotAZrX/uM2Cm22a/F3s5eOJ2cWcVFHZDGijn8bGp6DqjnTOL0WkwoN2KO9fvl/IplKUa8GNWurghSJu367qaXpePYGs/+CjU8yYwegApkNVypt/XExV9JoKSUkeVKhwXQflpYh56YSAukvKzdNWP7Or5/Wew2QgdpBIfb2+AuByaiu5zncrbxqKgZ+p5IRBwMI8NfVs1MdjsfDfp8FNNh7/24Rs9MtB91EwHlvaNqqAz3+dWt7GjA44tRD8gGWoQuEzPDXSTYmRLTejwQjlvDp2KLv/UGZJU4z244m4x3sfBFUtiIVkqjknL06mu69rzd52fux0XxnfWk4NtzrjRG8JmdJv29WcwstL65zWUKur8RLPRGUb5iRrJ/ZsnpQaadsoQC9e+61f9iFEiZyvX1vGVJg4UKPVpvcuhawk4nwEoSz5jMdB9vLN8DPisXdZ2fyXuoMzVNxcDg5nq5jX41X+FTjuwEWPKkLR//XuXhvc8OGPFHIcgxOCiJp9DPz2/2x4UDeMe4Ndt839u+2ug1zWiPYLFgQjkKrquzalGu6Lulox2LgzH6pRiVtzvVJzHq54vMcsLonT+ivKvHstobb66f4EjKugY8XFlNw1g9kQ5z0lHwBtQqqgf1gOOfAZl+9c3toKudTsPds8M8qEaY4Nra87HCFmpau/65zL0d299Zb3xSO6fVdXzsPeajkbU88opwih0/QgQJ+OjZuoYz8DG4D7wubZ+A6RQz4m845ebpelxAabOWDl4bP8CxLGSi3tdxciSBm7ujZG4uadz60qGcIZHsLjifmZQ3PNgcbq8lDLlfJSyGn/Qrrol68hUOX+l/XSKu86QU9occ88bhuq+XoDw7uuZcc50HeFIShWp48NFWUEPrPxEU9C/eVJqEO8MVV9DPoeupKnnTH9tgweL60KH9mANtbhaGjGWSO6DQWdDfSjbKqSJrp5nkOefB1SLNx6+sdsD6R+6txwgTiZisLgo7wFO/D659lcYpISBTqfe1IPqaXoZII7JBnpTweJAXM78S3Euet350pVd15miuBeMd0Ne479Phoayt+u6gmq8s7LxvrrvIqjnTeIzZ9xHYe/6YGSlAHEy1UfJN857kbas4gV/kDpIcZSfeAq6bl5uIKVKug+18TNU7j89DVS31XpQmPbthIHuOsBH+bHQmIH+2WvLc89TiPX1OO+mBpv1ayv7KHT0a9cL9/b8lyhtZU4Ta9BmNMfZR32tPhiKOEF0yM9ooFgovR08oQWJy6F2ag1poKI3RkFFl92zUxwR3p2u607j36Ki6+a4i/KxTXHNTSKJ3L61ZEhrCBwsDup5kxBUcHUfVS3voZt7wZCIqFRV/bCWom4HVH62V6wLiW++J+1IDl1LIylVa1f+mNyBLM4/dxxMrq+y7ePfbX/gS1sk91DKj7ItapwDBWfkdh8V3ZDP2lebUc+Z6qbdwBQc1hPjjB60S6Ju+37vY9HuC+372ue51aKk+ekFWGKtAu4VK3yuk3l/jw109aRNipEtF4IRDhW9Oaqo6EOjmWi8B1fcJeN9WJ9mKRnKcpvoXNe15+86v+LP1e9fAvV8HDQWLK7bGD+wgdWXSh9WV1CvusrP6nFRFE7zkmfhDKQqg33GuZsBg8V1yeVQ6+Yu4zMAZcinEQX9hms0kds98qlgwDcy2HzNTQ43bY/niZimXxHc6w2qOrdJRHkP7FwR5hgaEuSpHe58qEuBggGVAswGGLSeDAW9qWBxoBGWZXXAJ1jc0FCvuzwbK+6nGv+xufZzYiF396ziuq4a7UPjXXOs5vw2mGE5NxAZy/3l48puGsDsgaFeeX6fDoJSVfXDOhrUiqP8BDWgHjkxXaFhoPSEqm/5uLUrflLJgKCtHexT39G2LnWYqtW1nrLelII+MphDKYo6Bvms3XNXY+p5vfabUIXuw3vEtElxTomkOkdc/7x8H/hcW/qAA9RzIu/8RTlAnIzXAL8jycvH9TNAHDO6uPsoRjYVvT/KUmdIVPRs2TWj0VknWJxOcS8a7/WCxTVEIXJ7bdd1WrC45e99DMt1NI1VLaeq4IR3Xx9UdK7rABX3k7YR0lT1IjkcWfkJrXAuxaxvlk+lwT4u7NHEfYLFaeicge51T+u22dNEN9B16nlOpGBxTU7V0i8TFsEDQkqz/o2bOv+9Jt93gafgxHoPjAIFEuvj2u8YpISBToceI6LeIHBObwPEsaGBblCCVEIq7KARxhMsTrc8m01xLxrvjbh+L3520JGxKeWMUmeC4p7vT+MCCIvLK2iExoLFdZB9b06DpdmDiLkgjpJ/8E+VFZZiRym88hPMQM/U82I7hq/v6hU/oXceHzhN6TyFUp3Km7rUoSV9b3yUHw1bbv2YiL2cn3W50CHE+86RT5PqOaMYmoGn4LCm+iORmfUZ2Kg7ZSji1AD9feAxEKOjp1McmsK9/rnPgJk5n14PnlQLEpdj6aAa3dyhojdOpqKvMsVIruLmXtjPlJ/OfDQqusNd/obHG5ubvSTXv6R+E43uPL380zL4sfzM5VDPxwpBBVf3UdXyngxApp2fBpSq9epVM0d8D6SkBFE4Hz2RTXPb3HPP+mqoql4a17YPGSzu+u+1P0Bczo2r9rqGUn6acHP/3NWDgKelHR59L+K92HSgU2PbRZqCw3odIE7GR820s/7dy8L35XbuFdOFJXGr1NdngFSwjZ9cgHhBFXDfazJ+10RmQgx0tVFMipEtN0uHN6PJKKZgwJLNkCwEZ6tgvAdV3PO/GxrEuf7TRfXc9DI3BotjlvM3n9+GNWAUiM0s9T1mHJix3Z/9UdVJH9gA6kvlTs8Ffxh0liMqP6HUzUV5Hl6o+irbVi//SeU2JM3drBksrosuh+lgUQzlR0PMPlDBYKbW1+u8BVu75+7mBmKuuVEM57Z61tc3TR8MAOO0lpru/7ZtUe4N3fSryMHi4N5eDf8Bft02d19q8hT0iMHiYrt3AYUz/5J24lbV60ExxFXjXTZa1WN1eXoHi4vPkrHOlDop3gj54IJDcV/+zhVQz8eFj1puc2U3HtfJ1pHwmH/uwqBUhenAhVV+ahtPj56QfuPSeeHBgq/p86muXgr74ItPfTV0r8NUPRZCnekKUSK5J+q5dWqKf31NNK2ebws2gF9twKzT6umFy2I0X9tjoLDGFJyxrH8ebNrOaBvc24m88+f2AHEyXoPWmsP6HCCOZQa62dXQpgqZ9tlU9G4rS50lX4bHZHT6BIujL89WMVhcZD7xKbHATUF0TF4CjoGIQjvql2dbx9zzdmAztMkquPvd1+XAJUMjNbJS5WvIrRnLdiQk1JfUuXCwqMsncH1vu/yn1QyHB04txhZwXydasDhNPl1UNIadvJjKT7YtloI+NJgD11fH2t33ND6NoTC4FEwldxT6jZs6v/61dv65iwCeFvENdLlsd31IaPKBgU5nJtjUA1qaXgeIS5jihqVrYgaLA82SqOj5XPQmXNe9l2drYgBHsEXZnd6l3gcKFrf01NVQz9tGxGBxnbzW+45ipPl9ZOz5+CpT5bYNqPw8dpy/AfXoCak6uhhKxTHgO1Vm1rtsquo0+NFFg4Z8L3qpkEWCG+g29TzUN1XJp2n1nFX1PAg0raQP61/XHtjwmDK0/kyE+ecZziBkVPWVOOAAF3c61dY/9xm0Lv7Z+2vjFyQux9KpNbi5VwsgAIIhq+jjDBbnMt5j8slPiGQ0eXted935cU2QOKrxbjj/9Sev4Su4k8dOUUlwq+ClfVS1vcMDmKT1z0N8eD/4kt/HlTs+ygGUH+9pWNk7tqCeBwu+NmL58p96DQDRrq0jE1d9r/9+dwLESRxuSvnhIkok95LBTK5v9XMYh3rOhga6z3VyYJna0elB9QuX0/vMuqxiiTDtG+X+mHtUlF2oQz23+nzWf/IhCCsVMA8Cu/AbtJ4MA90abd2kGNlytXR4wXhwqegtCRYX1WUlH6SgzIHXue7rjHfj+Y+OG4faAFQ83mPGgRnbO63D7zvumKNsSGNHb6TUec6HykwE5Yf5DiLvnU0NiPlaZbvTeAea5K7z0pZdeaipky6HNz5WNDhjKD8KwVT0G68yRG6X6xZWURzL90y3fr/XdaqWpvPu7fkvUdRMc/tGd2+PMbioOQ7u7dUIM8Cv26Y34CfAQLecZMRgcWA8LKnXg2KI64LFmY7V5VkhWFy00cpPX58GSxl+5Knu9hTjnZuMd8bWv30d1POWkKpWPmq5zf3dsK+rH47RBzauUlXH/fFw5OBrvhgNl4D1XbrsZ97vyJGB49FBIiocXe4wrTeo/IQMFFfNYParb85Y1PNrPyeK7RVqENQ9YNYbA13Ga5pGtTYf+/rngZ5luLcTedfPN7dxjxgudaYrvLSj3wHiWKaguz/4lg4qyVV09Hs1dxsQlLP+PIjobnLdruK6Xl1xH3uwuFLkdlOZRpXcYdTnv0ttAvW8PWy3uqUb3l1UtVxJ31W3OO38vghKVZ2OT8G4D6z8MB8Ffe+ONM28KW9n2bRi1i/7mZ96/rVTs6VviPUp7q8ULK7LHVr3fRVI+eEijIF+45WjueeR65szru+Zsb1iTcHJ6Lp785z8Ryj3f0c+h565PN78c9cBFdRXSj4w0OkUr43PAHS1NL0PEMfkOegmN3cEi+sXZDdvh/FeVXGvsjxbaD57XTr3fIe2LItSrj1/uuJ+6PHroZ63BVunImKwuM6w/x+KUb5T4ilV3h2f8/9gUd/DKCk+84OXvMompgngWkxa/zxAfbvcoa2kxnipkCNCxeIh3xMB3ktrd311bPEFjGsru6g5raSz9/NHdosZ27J7PgMbxGk7Me+RgsBXR32l5PPjD3cynsa4qDEIrNnmPomJGDyZSk+07svbpkwpv+87KtoyI4CArKKPI1icHBhOZ7xHYkl3TtwQXZ7i5i5jOP9F3I/t4JifbJqji5pV8AJGI1y/vXMfD+5QJwIrVXXb51CE4Gs526tUJFPPnR3HmvVN1HP/wT7X+uf+qn6BxW4GiMsZDPxEUH401FbQh+p5qPq68xmnN1h50MznvB2o9/xDt3Q6QFi9QSD/9o0iSpz5iKgcQ0PG4zmYCIU2IOXr49Onpw5aT5CBPnwJUQO/FTqrxDTS76EjmIKK2FT0cQaLi+GRccM1YpYp6jnlnHTH2rwAlPNfe+wTGH1tEaN3jsd7zDgwY/4AdbFjRw4opKOKmnHuy7XmoDO1fQMpP8Ntjx1L/0Zxl4oZRqmqaxyN1rZ3HamtL8nNvdMdWq6JixBK+dEcZw3qRkF330Ws79pXdo/1eza6f0MNFLrTdN1AW4js/q9Ls/GdK6KtG68dQA41uNiwJ0Dv4CLCAL89nwkz0NXGohrhBBQVHQr6mMlVdFZJ/W4uWFxIhsvLGfJ01rmC8S5tx9zzdlFykfRRy22u7Mq+LhroM1GDr40IsbZw2nmKWF/SN2plB1uIEnytyNrHfu6vSn3tA+lgw3ZtOaHqO9jW6Q7T5+RI7hGVn5ylSx2KoIWbrlTWPY9f33F/z/QDZqH6C/p8Oquef2R3GlTP6QnkNU3Dnmafq8waeK1/TnVz16TB/HMi7/7ZZmH5u0ADJEWU/X+agABxCVOn/LXCjWhrRGLnFwp6aygFTcsZY7C4oA/djVcNIrc7jW5D+iqKe76dC7aN7+LdAAAgAElEQVS28imo5y1j2vFOGkJyf1dRjvvLO6e6+HE3TwOwbPNIEyWAUE3lR91G/UYZB/8CqpnB1HNHOVYIKnofOrTr6oYIyk9OHTd384oBPh4b9nzGrZ4zSls1pRZ3AlEMDle1XbRpaPlENdCrHBwgWBwMdDrmGBE+A+bu+3Niph9MqRuoweLIbu7l/KCgt4CzD2UqOsHolGkkWFwocvVcKruy0a1ACBYH9bxlyOt7W5Vv03aiQZ/9vtG19nnuTZoAcTJhlar6BrpwDOT5ffRlnN+olePZgrbNfMo2p1m79Bf1Bi2da9vXb6s8Tec7tDo3d2can2s7wMtAv/kKKXK7R3oPo7QN37PCcxZ7Ck5GlwfZF/JfYrj/G6btJO7tUQz0s74ppnOPgGD11Rwmu+r/6MPRXPX7SKBBYM02/XETc21yA33gdli3I2bp1EoNDQW9PSxxQVPKdT+NrusEFd0YLC4QN19RVM9tKjoPFyxu7dHPQD1vIeU10D3VcoKbfKfV85pLlFHShHg+aHFTPDsKLuNp5fj0ftJHbneUU7G+IYyjkYEeqr6a467b14v3HtnN3VP5kfF1cV/yKZucpritDeq5HZ/zdtBlFf0jX02jt+untPhAb992qOd+g4sqMM6r4DMI7ML+bpo4A71A5GBxWAu9Jcgqeq4yM536XUVxrqG4B2apdE+66mww3tVjLV4Aw5Fr0CrK8/Go7zGC4a5s72aAOB9lQgMhTW0F/fw/6lce8VR+dNusBjoX6QoNRVXPUbRHfffXVc8z9Gvbu1Jp62t0c++Ly+HhyMqPTGUFXVbPK5RD2h9pekVtrrtBP1c/4hScrlPqgzTk/h/TQJ+N9Aya9kNkqYY1iKNMoClDE2egm0eOqUY4gTz9/qPqLzMCglGYR6k1WhVDVTXeayvuaj41ueWyUeT2Up0MWTvr7DbeVx/5LI8yvxb4c+yPN60B4kwqOjWNRpXv/vxz3RFhlKqNc+pHcM8ZzheOUF/j92n1uFQ9Hy2h6KNm6jaW09RepvHrp6TXdUthY5z69uW9VzyP8MqPTPVI7i6DOaxqunbncjvU8xrTCKrRA2Odi+oigVf7FrdtPH1lHPf2DGd8lALEZ9Biz0BBJ/Lun2YB4nwGgTVQru2Ls5MRII5JBno11cfWiLTOLwz0lpCo6ElgM6YzNtSfFuM9VLC4QCx5ua4byiYq7ph73k4K7xqbQU45zvoBGezrZAR3047ASlXIjo/WKPRSi8vYjKfFktFrKcdT+Vm99Jf1jV7n2vbE+hT3a1X0XnRoC5HcFQIpP4U0VSK533x5UT0PplSZ8+nk96whtbiVfPSe9B4ZvZvCDKo624WLqOo5y6eDBKxvmeI2GOh0yvPPfQaB6Wkman363EAv3ZCRg8V5LzECorBkNqSL29Vr7RssznBs7U7p0qUiceksqOeFcuIEi1v95uegnrcReX1Oh/I9+t20nWbQd+rj/oO/lwLExVeqQraN1s29AFVJ0Wx7/Jhyx+Ox49LBnkVTGp+yDftDGUf0te196juiT4rGehTlR0+VeDyDeyKiUiXl0xr13BQ/xLTNRWNq/HgYBcVViOL+P9rvvQyki7MeFtt0A6LB7ofypvUfXYi+HJU4g8DWfCZq8CQ30Ou7dhHTZw2NSO4t4uxDaQdrrclgcTxTY5RgcSFejAXX0Cqu65UV9/AdahAe57vGRy23uMl37QPiNUfZs4MUsuNT8FSIECxOZzwlcS3K6nlYhW71kgDqeUawte0d+fSp01SpL0RWfvT5kPpBt1xWnnteqezqadr0PaP1FSOoxV1i/p7UG4O2qoQP5nzWn7oq6mDOTOSpJup+qOfVIAchDXQdJ89AP+WvBndBW0pDB9WmsEtpYKC3jDQgjCFYHCMa3dr9anqm7Hco1VW47eJ0tHXe6bruqrPBeDccu/qNmzDi2mJKBijFIPcNFnfomKmuubgXvZkiKlWB5+cP8qqn/NjSFL5RmXo+7yqnZrC4jRBzzyUKwRHD1beQav3afbyL0zpMpMZGQ8HiqP0gfeR2ejlkkukVd+5pT+T2GMpvKPW1ZZSD4gY4b8I9HU09z6g0/9zjGVSBgV4N7SBwlOd28GMiFXQ2XGotR20sW+e1Ipyxrfv/AcuttQmbij402jXB4hjB6CYr7vXbY8lUVqlOhgwqKe6MbWQRnUELOf6Hm9NcmNfQtajg2nvCmmbAWmlL+zHPP9dtrKdUhfy4loxCn/paKH6ffAJ0VTeqli/5ZZgYBg+eHH79c0P79q3DVB5sjafgOWPxaNXzQEqV4VltrTdYY+7pg3w6IyLN3z1YUlbe1pj7v4huoM+ayg41bUd5ryGCO5Fj8gBxMj6DwBpM1/bFEyYnQByTDXSv+b+2B8Td+YWK3jLyZVVKiqP602K8V1+eTQkW58kXdoltPFG4COq303XdUAVN+uUHb+2VetQ3aMHPLPce1f09+7uLnhSlKPc6AqgvG2cfChdA7/w/2jtSAZSU4b3z2LFpJ3GonsdYe52LVD1fdmRdhdH88wj1leiVge7qB4VSX7M05eUfyywpaSqVTU4z2LZ6x572eYMF9OCpkqZLAtLQSI64AkcxzeDH/qeujn6/lJZlDuXtoMvnhx9p+br/bUI45p/7DALb00xUgDimKOhuN3eLim51D9WDQHEtI1XRs4juQVzXPZdn82TJVufS2uX1g8WF7lCD0Ah9kCybCm56jzmU85zuBoiTiaNUxWibjfT/UEqKuYNuDtAVqOxUPf9V0BUAtHM3g6lOIzf3XnVob3h81EGPEHytRBLU1LTPNPc8JY5S1fpYKoEHSMw4jI+2MH+3WDTdIw24/0ft/5z9sNDH0JCg1pc4SDlxBmBNRgF4HfkEcoOfuOkHsoFe/tDWHamyKVNQ0NvKEl391h9XfXk2XjCgq/LFBbGNi4HCFcR1nRYsbvnrS1DPWw5JHc6hquWmNB1cA905vy/gvMbghpyuvQN2FFLl5vFjWMl91FiOT+d38CPGYF9hADxisLjedZq4GK2xP6Se8mPbZnNzJxnMPkqVhlaq55UJpxZvueI20erlgBfuSutnHDwsEEh1lvJZf+rq6Gpz0UbwOYdqzwbmn1djtkYwvmoM0ky0gX7Q9PKKFCwOCnoLOefgQEUfuq5nNBQszusBLLnmmwYS1ISuOhuMdzaYew71vP3M2gxtikFeMVhc1z4g5ndweKUqRud/NEAWSC3WpHEH6PJRM4tpFi9+Ppx6/tD7mXZpopwA9R2kEXz92v29HKQc3qsNBIvTChW3fkwsqPEzSGVT66O5z+05j5cGpuDotrW9j7rChWYJMkeiQG3VxP0yWP+cVh/nfkI+CPZbjeIAvyZp4Od2cg30k19LOwjukWNb57UaW577B6joLaWgog9dwnOjXeiNmLrB4q7ziAZ8+0fFNiak6MpVjW5Dvo46Lz/weajnbWbHc5szcufF6srup5arv6+/fFznIriT3r+B1NcYnR/tBztUsLjHj0kH4YrquY86YE+zfvHzwQMt1Vv6ht5Wfe0w6ZXB8CokszyDbgMokFLFBVu9497uqOde0zR8EGwufO3DsHCXWKK8myK5/68/eTWPHRyOaZ+NQAOxhikiUNCJHPtjTYA4GZ9BYA3ytf3jhAWIY4qCzmwquhVbGlPnFyp6a0lVdJap6BI6Fd1kvFdX3P2Ge2T1XHZP181rr+LmzswvlHWo5+2HC9q7xaSiq38T1PbOfdy5Zp5lLKUqWyUiNIcjKSk511XNx6OcGEqU39r2um32fPraofW7rzwGO7gou7gvXWqZex5HLW6zej7yZghkbFZoq51tdHNfuCu9P261HRM5WFwz94uwB1H0slXs+UB0oTPqO/jcV9XTTGR8gJKBbjowUrA4GOhtRRTnNvm4rusUd53x7jvifceF2brn6g5NnYcDCer+qoq7YEv3fRHqeQfQqohU5dz0HrOk6ZSx8sO/MwSIkwmnVJU9s8JQVP1CKSke+XiqOOsXhVfPWZC17Wlt1VdFo3BfRQ4WpzNCrJHbC9RXqla/fF+r1XN98OKm1OKWDV7s+koaVLAgEDTs/p+8y/dR6lqHs78h3DE0aPUtbrNnA49eOlEHgTX7J9K7QTXQ47h2mdPDQG8piYrOs3WdCUYr0x1XdXk2D5aMdTLkpauzTnHn+uXZ1u+9vRHXLlCfWaa8/KkdOIJarkvTNWOFtgRdvs2RmaODFMsAOGgs25Gw5tJi9nLond+F6iWSMCpPgYPF9bLTlEdyD6T8OAc7li4dqbRLlw6icgczNjtmgAYjlFos2PwVt5kj7TfJrq+kRuuBqq7Flfa7WXrymkYECuP65xGDxcEeoeMcBA58L8JAP/k1Zv4wuTo9JmVKzaPY+cU89DYjsgBJmusZLVgckTs/NJp7Xtt13ZJeMd772ZnpGSfs30zeKVuohjblOFewuJeO79z8qNIAhpYwSlWUtjnvRY1LYkRlLZQ7fcbaRS+Eb5eH3q9fWlCHz8CGxPo1z/XaJbTg9RFzQCeZMpUY6UuXijmdwex1L+rLUWm7es5K3gzNqsU5K1fcJsa6LvpFd6Zu7S+YjPMIg6o61p+8pjGBojz9SnNQqHdcdtzO0x9rx2BMBzAP8Ou21X8GYaBn0Hz9bZ3XamDUqqWoKvrQTVxRv3PqBourAs8GD3IDunaEdkPZsnq+fAfU844wa3JLV6G6vMtojuve/CiPdX5rqK8xP65rxrJ1G2MqMtXKiTXYp7+u9eurbut7h8lsuPp44tjz2ckY+xtj7FmrOupbH/P+1g84L99hHkDwmqbhw2Au9IFxGOkX3SGmL7pTJC7le/NtDQ2q6tIsVs/FD11shgKBzlE3bef0xwRsEgvH/mgUIK6BKTgpfzxx8gLEMYOBrlfRKTe/7RhT57fFkTJBSmmZoUjB4owdbZW7zh/MPVfSj8rUBIsr1N9PcYd63hWkd4pVLTdtt7z7DMd18eMxNOQiqU4y0VW6wEpK7Xwc+6Oo5xnFuZth6qvLp+8GutWbMIf8HIxH+bWxevv9nYncXhgAbcojRiE30htTWC+6I53ucDgbwBnHFByZtSeu5dHnnkuMpun41bcMLZ/E8HzhtMfEvtMeEwunQVHXMeMz0F3jvTiRAeISXqfZdkCOXqvC5TYVozcAV9o6aVhh2Kew47l/YNNn/DsiKLaRc15mB545mq0xwXYkFzK/rsPrK4pfAXlTes1F8fqnv2fpkz+E+/4oI9hioS5qmTnZRvXFkG5OypbrLh2rOb9Du++Eet4FTnx2c5rxwfIzpvuBqe8n6XcV03tMSdMpA/2HbyQEiJMRpp5ehmP/WX+Oaswd0C43VLW+mm22+6JmmpiDfeYOZaBzzLb1W9EQ7HCprTxEWV37hsqngO7aasqR8unSgPNhU1wFbfu62so/n6QOL19+m1hN2u/BW8MPcFx8Ryo+LGT/Bu9o4nPrOu+aaRpTz895aOCp4PPsUM/RmQ9jO0U2MHLa46Mjh/lI+RXG7G375W1Zp7RQh8J+aj7G/WvPnzkVyxOguP65X/va3k0qE7v8nclA1+Pq+Lgwp59tIjIk8IMztiQYe6FklBuMWvW4UhJpf9XOyt0fFIkBtmAcKFDrZMhfTj/aODLaFQOusY8TqI3WI8fLIJePMxj32d/dDBDneJ8H6iDFHv0eDuw2Zgzp0ujK0bfv6q4Dce6Xb7yPbWOcNvBSob4m+t5pGp6f9n7w6QsRB4ZkQhmbSjldUs9Zdi12eqUM1b7FfBLvvfnLbxP7xWCgat9Dt/i35yW3i1nB03fyXDrYGL6+1fcXue2Ja3mTz7t+/fMxPYMx0zi/T/5lx7xeZsM/VFsVgYGec/Jr7MhPX592qrabXgbWm8qkUlkU9swlFQZ6S8lU9HUhdf5sRrfNeLcp7hT4QD3fYlLKrQMF6n6L4i4Z72t3380ncv5LRxlFf61uaDuP09wzh148YapT3j+csdkKo9cDfNTXAbENAf3Hm6o6BVJkKpQTVT2PUF/dcetX9ztAHLvhW/zglz9SvjOoyo+zzaspSKHL6dR0rWQAVPDiut+R1WL7tlGb7sz+7b7s82IjM4qS5+Kgrpx8m+CpB1NihE7nngExBxdrvuPW1WXdYhNYfQ2aTzi1mA9V9CGuwaRq9Y35fqYN8Id7x8FAVzhgW6qlgNyR9XihZGAeestJVXTB9qr2i87tRrXTyYq7g6+ek6rni3mhhSSZ+s252+iuqLhj7nm3mCO6pRcwKeeENJ2ef+6jQlRUX2N/XJ0DAOT6hlJkzPtXd61FHbBwuzSGUYsmo8Mk2CHGLf0g4v3iNY3AB1o+q196oFPqOdt9Jz+w+Flzz3Kc7SuVvUWaamNV+2tMK6mdj4chu/DEtY0sq6Yl1DmGmiLiU7ZPOQHyidIvOe5Hm9s4UwSyWO2b8fuTJjNAHDMEiWPyxVUHeUgtajtG2iflveW5N8FIbzPnvMxWkkjmchW59JNLwdmYEixOPZbJ1179aWeR59Ej1cOElG+4YHFrd98D9bwrvOf7m4lxPoqCrH/XaP/Wbre8+6Tjum2gS+jaxNROtv3KtqjG3HkvBliGiXicTz7K/tiDfWHXtjfnMymKxvDeCvAcDAiVj3/ZXR1w3q9ucLWLDp/21eaj2+hxnXzKabC+t337uvH0f6K2r08+rjQe5WgJ9M5IY2jEYUYpJ1R9TUxsgDhGMdBNFG5IywWRL4ajQwADvf0sFQxhnaGdoRrvqtGuuS+sL5TdZ4npfC64xpCW87Ea76WBBLXuxTyhnncJS/T2AgbD3ZbG9B7r2vzzH2UB4rwEJVeHQ7+/CWNuXbs1UAfJ2fGjpbltIa56znIFL1B9bUzKoOVBbVt5ZOQzQELNp4C9nM6p5zlc2KdABjO8Yw6Q1ExDfW4Dlb32rUXeiv6P1+Aita1c+RDz9imHO1wZ6tT3+bOmmjHQ5bLjtO/Eurczk4GezEO3rS9rNcgpperTw0BvOee+zJJI5usm9dupSDPlZ1Fxt75Q+CCK6BZXmcb9pnx1xvsg/dpXdkM97xjpO6Sklsu/+xjkBlU98bD444kdm3+uW/88hlqcpTnzL/GXWFMH98j1bUjxYoJtxJ7H+fB72WxUZW0yO02F8/RR1rSEUp0caNJ0ecC5XoyihtRX3zmeY8tHv3+jtf3xMT6DMdM47ab2qM7pNKrA9bXth4FuYPhCNLqD2nI2qVRqHqN9cHPvAGqAGarrOlVx17FnLlPPDWurD6EOFLiM98H2ha5eo0nkpO9uzhUGcHJohnYB6nEdDWw5nKfs5T5ZTbExD/KGxfwRD6Wk1FOqlhfWogdVKy19Y6mPFmK7rF/9g4lZEtU4uER9dpxtTsynbjmpev61bqrnbDAP/QgX1d3cY6rbkd8Z1nKiTMEZpEmM89lvLY5v3vkzlw2EkRjqdqh8wqnFmq313xnRnnOvAX59Pq5ycmCgG6imHlqM8ArAQG85qYouBip6wXWd2d3cC/tNBrSBXD2X81Hd5bmn8W6o0+qde7rbmZlQSu8Oi1u6EZNyblDlezP/3EeFIKivTT1DpM6kp1pMw5wmunqeUW3NW3+1aGI6TEkk9/SXQKqpz+CXF3rjoQ/TtazP0VjbN4yHg7OciFNwcha/tdjokmomivGOAp1jzSkitcp2EWzAPNI7+vgfbm7LgiHm5ZTLDty+kxwgjtkM9JNfSy/y8CEpNTzlBrQdo+/8zj/3pnSOJGgxWhVdWILFSdfbqbgr3HdGej8slgxpJZ/CfaWUrxrvKhrFHXPPO8R7n9lM7pF5olt6AaN3kCG99Pf6H94z1UVjZaZB1akRA10XB6BFSsrywq8bUZxnTPWR8VF+FCZN0Sh4gQQzxJpVfvd8scPqeU4SzV3nlRPqmkSdIhKhnAj13fWtRb5SPdcoHCTUV3+O1HbxycdDLW7KnV6qb+Pzz4NN2ylum+gAccyhoDOKCyeCxU0euYrObIp0hmq8q+o3gcQ436Iry+i6zoo/h+iMdjGqX8bql++Det4x5iq4pTuPIyrsnXNv//H/HgSIk7dFDhbX1Oi33QCOrzqZ0qwv/Dr+YN/DJ7FpJorXlYVUnYp/TpqiYVTRm3KNpeZTYLR/o0/BTl2D58EM77ADJEHTRCp71+PXt8Y4Z7rva8xnp2fB4qIa6M25/0+2ezsjGOj2j7GtI0spXZ8ec3+7wZLDTdxsSLPRz4KhrHD/6Ww6c2+v7i7vMN5VpMEDqOfdYzGvMcEt3XkcoxzXsejtGcMR8GCqnj1JIwNdH/yT/kNOrm88xaupd0nBvT2qEjh5naaS0eLz7GgJpTrZWfri18Y3nzg0mYq+6p1tQ+prTHf6wFNwWAuN86S+9gFwYruEmkbQVJpAwdfivKNFeRpV5GBxMNBtO09+LX1INvK/fT78VDVL2rfjuTexbR4lgQb54EuDiO7D66f+JBrvtocxCwy3JR1hVNV3l1JeRXEf5dfZZWgmlfd9J50XtZ15quVUrx8l/cbvT5rqdIA4mUjB4jbmXmlsDnp6TawH+KnFtDT6tlqf/03ZuItEtaVv/Adi1v//9u78S46rPPj4c3Xy+8x/oAnwvoDlRGMg75ucLBoSEmSTWPKCbXnRzBhs2RjweF+xRjZeIIBHEPAmW9NeAFs2nrGx5RCDZ1iCYyCMkgO/JDjSf6D5B7g591ZVT1V1Lbeqq7qrur+fc3Smp7s2dVf39FPPc5973Wtj0yDO8sehJ0/jV+C9k/ucO26n4H7W73tEDaL/wUD5060WGqNca6a6zDqVZV/73s78Mzc2LDg3jeKusReV7IWYUWsWl7ydyprFbf7w/NpmlunvAn/SfdnrEKA7LFO6zD3rRM05iReyH0YjaC+L7lTmnp/9jnyofOsTtmwzMXueVi4fOZa08yw9aN/kvGul5NcsJSDPahaXli1PuN3G4FzsH9gaMj8p2ddB/3Htb3/VZ6oGWYkzE9u3u2LrjOsXJi+Aqei9UyqrV9ym6NGsRjQd3bOGQlb1/NYyDKai/VSQLTbfd85qYnDepR2rV6Lr9Ch1caai7Pagyunrzjr/1avRBnFhdTWLe/tvx7tBnJQJ0HueeJcPpOKZLcrcWyCSRd+aPzyxdD2rzF2Szyuvc3t3fRVZPyuLHg/a04L3mKVRKgUcBx9/3jaHm3MNtPtuFrelvQF6SB1ZJ1XzF4UMJ12PdwDjDdf3Dy57bo53V87x9K7j+rxElxvLAF153cMjFRqVBWL1vQfn7nt0dKvBlh6ylQ3z4fsqy4hXd5Gy8L7LKLGdE0pk6umbGtGtPdWxa7aGM5R5fp2VeZ1KZIsHVE5fb4O46o837fkd+wZx4hKgx8vc01TcLM7MiU6Q3gb+mO3MLHpC8C4JHd5jFlzHm6eWrkv0p4T2HWsWt6n0QKZCQrX2xq/qugbkEQ6Z89D2Nt/+2/aVtx9/b6hBXJnsQJLhfFEov7+qMin56wwse37kY7Hy9hr/j2r8GsRZtz9rL9x6fx8c3zuDK41NvPvQfY+qtl5EdLb0kM3+zictX2tJe70XNrPvy96My3YOdW5S052bWpOMWHC5ODbcucyr2U9FzeJq+burEoZR1fz8jn15uzhm0CU3Y5QVkLtsPSVAc1kVw2Wy6EpvZdHDP3MDaYkF7b5Hd9us6ETPsklBd84+HZvFLd37GNnzFlpwyXz3ZMsdl0vZ3khkzwOVZfV672pMiXvJbLHTdhKs7//pQAPZ1PnPa8gEjvOXpiWncc9lsnpVZZ08nXsfU2PT6DQrSM80oOxrg5rFmYzkWZ2b2nVuHDtgv5fNpCYJHZ+XqoYR9L2O43b6aL5W19+erb8z1R5vmrEP0KVAgB4p1ytVmlP8i/DOVz6Y/uUDjdL90K+gWZzYDvGSlHHfahYX2VZsn11uFwrInrfQ7u/qGaXVzuDIXQPtCprFNXfMXraZ3CveSfeVz74O+g/saSkblFabqRr0F2Bv6puchSrIKJ76zJg1iAu7w8uip1b1NaRZnAnOx67y0A/SP+r/Lc/knKmuIztY434ytmMC2xs6N6vpzs3NLmlPc+yAPe69KqeStxXN4nK303ezuNobxEX2Xebiott2xj5AF9cAffe79qpMaifTAM3ixtNFv/Ky6DlBd27223js4zZ7vj1pOz3Ts0WCd4kE71ll7pFtallafILseevo5KnVeqQE5CWbxZ36+cdb27gktxS60OMJQs/p5t7fDDiYc21KV29WZNDZc0ma+qayLE70vrH/wnTHs3ZM7HxVWdFSWb10h8cxOA8sPWRfG9PIaj24r6rnt5ZhMGX2U2zfJpg9ZJ6T5Vva38n/2AH7+nYz6YMartC2ZnFvXrCt8s/pXX6DuJqb20W28/bf0SBOCmTQpZJmcVmSvxTveeUMplxrCS9zVLJZ3FVvel9su/OQp2bc85vFRfYVD+Cjwfup7thCtMbZ39bmM2FP5HwoEGg7Z8t772/zuE4v01pVFid7nYEHcxf9+9YFgcrGcybdl/3/HmiAdORvQn0F6s/8jH2ALl6QbrK1NwS/VzVEpI9mcSZgmb/3MTX2yYylh9TppYeUqRQ6zyWhFFZBhYm3TtKdZc6H8tsJB+aLy7eMTvLBZNKVdxFmNfJAfxdVndepKltcYzl9PY3VdEL2vIYL3aHnlwZxviIBulMgk9ksziGb1WOADXdQnsmih0vMErPoOc3inviY/YLb84WzymZxseB98eARsuctFL2IkyAtIM8K4h2axbXyYk6kQVxYmexAkt7tDKvKYN1hmboaqXWu+NnAG+Olzn9ew/9x7Ke8CdzxrFoS3TvueQjN4sz5Pn3o8QZPlTUESw+plcMPKhPIzXf744QMKvta5zoJ58gJfyz+1NFbRyswDzNj0l88oExz2I8q3ft53+Rmce7bKd0srv75zx2PJ+/xnOeXi8E+5wB997v2y0f2lY2sL8wuO0lef5Ysejt0x3IXDWl5e7AAAB9KSURBVKSl+7M7r3o8eK+hWdype57ii03bnONlz2czA+2U+3vWcVzOt/6vu7e1ddqi9EDOV1lWz/sxnOdJF/+CUmHGaxgXkp3mP6/oiyNfmkL8TPpZJlNb5r2TyH07JuicX3xCzRx6fHSnUuvX4QfVsh+on9eTdQ2UeZ3KbKee/Zjv44dFyx8+dZuafuo2tXz01vFIOLx4QK29eEDN+O/BjuiE8ekVXaSsKltcQzn9CfEuUqz6VRN1/Q1K7gVW5uKi23PF3xrfHxRc3gRgR4NfzAmXc7Gnl956FbPWjzymbWaVTHrzLfl9A+x4leCl1uHXM3Ln1v1P/rXMyTbZHj4/JFjUBOr+Ot7DSnQ4Yg+2Fdt8V2xf/k/Op3bqKeU0V5y1/9cv/LmR+fmU9jmkU//KtPliTuQPbN7nds/7x+WzPvq8DSto2Nga+tB7vLn/76R1cr6f+esMI3tu9j1T8nhz74s59ZnXx7dBXJo7nrHNq6YevEIvBn/3khYtdS4mvwdPmb9bi09wYbmIrz9gp5xb+fydetKfmnOv0rLH5TWp8TOj0H58m34li/m38tTtXJx56Wr7HrRDiy54Qs/4f+tm/H4EW9WYJWKVMn8Hnc8H7+/lZkIwaj5nN0LfaU53l/HW2fjBRdsG/Vk8nfG9yEp8XvLWSX9+CdB9Smv3S3tvvMeWSp4M/yHqeVFiQ4TD93XvD//u9timKJk697d8SWi65z9iA9+Deps//Dv8U/mvqRL5vf9Tb/0zXz62dx/3azuStuOtr+Pr9/7zt/H7bb37uqujqMpomXOe05OivM8fHf58sK+p94nTPcd8kd9Dt4PzTII2BbH7I797X4ymfnrOwP8wVuL4e+3Y+T3hbUU+t1XvfUmf34kNZhO2c+5vy1XK9+uFD9kvakeLHq8u8HjCtje1kulhBOhP/rWc1ioUFLodb5nnZfXa4zawQYoH9tvgzwTpczoIDAo85+HPqO593o9N2/tCyfLBI4phBhX63F3dgG5alK0y2u78OqU8nvmZUvx8WPe/b5vXfePIne3swj5M5x/R3YvTWkn4do+M92Dkfq22hvtoSVin93zYeO2ydlY2zKz+fkor+R/7S9LzkhXLpT2e8/z+68e3DeX7QxMVyqDvfldOv/Ee+2VvNnWhnKsmuZIzWxP+Hz+yng1nyty1kgXRMmGy3pHsd28WO5z93l4k4969whh/PHwKhfflP23+45xH7WTPq6TMdziLHpeaVc/4rIpdEV5pa3DuW4lfqa8sW1Smiqo+a36pX6Hjzc14ZWdSNoYUnE+m9USoIfNDYJjjzqftF3Dzd2Xx/v162mZqTYd9JbvS1kzJOplkxEaQKT34JEF5Xb5xv1oLn9ufu9teZJn2s6/df/7rlPo6JsnImoYFY6j9rKm94GZe+5NH7iQ7XoXvfTry/uG9VI73NzXp+1JevFd8Hc77kEIZdPGy6OYD7Nfh+4pk0Z2uvJBFb7VSWfTiGXcbkPU8npRxj2bRT9zxjModk4tm+cSzduz5hs0YJmS+JXw+JGQjsrLoOn+5P/zpOa0dfw5giO6ftZ9dJhM1Ge8HodVWCasJzu55iqalTfeZgzrpdfSkZAfN44/dw8UWAO4KB+jiBenmg6Z7RTHxyrxKDtClfJm7uf/wub9lbvSme+HDMqmVnDTBVDigditdTwjE+yhzl97tfNSfzxYt8vfP6GWtZDYSQPs3t17n9DL38HKRZRKC/dhy6z/5xLbkJikAAABAxYpMsxYWaVJSpktsOHrvWT/tMS3X09G9+S76lc0K2PLL+IXljK7qyY/7t1Xo51aHd5W8rdg+Q9tZJzhvn79/2uvcLjmfNWlzomd+PqV/1gRoyAQAAICBKRWg737XfmntmV8yLhIghb8Iu66TjPHD7bCk/KkvEoPupEA6J2hPnHItFLRHg/fEbXHutJN93bKmScu84OewXKRQZ+v+Uz/5xDYCdAAAAAxM2Qy65GaWymTVU9aPfeE286IzhrjhLv6ln0UPZb/FNegulHHPyKKHg3aR9dufI3veNv/QsV1Ye5tSpgXaGZ2wUrPq6Z9ViU24AAAAgLr0E6Av+VOAWGlZq7QsV/z3tGxWD93dN5ovkkXvBs2hYDoreK844072vJ28183lsyJ+vjgE5D1Z+a3fN0VT3g4AAIDBKh2gmynX+smi9zkzz65XzmBe1qYLZ9GtjEC6G6gnjTcvknGX6OP+z/Vbv0P2vG3O7WjzHt9VIvOdfsHQ8TPJTBf4439o9dRqAAAAaKF+MugSz2QPsFlcz77RWLbSop9mcUkZ92jwntssbo7To132LNupbJZcx5mnlblnNYvLyaqTPQcAAMDA9RWg737XTirfyVuupmZx21fPoGy56UwW3WQjpa7S9YSgPZZ579z8vGIO6/ZZUFq2B0edGWiHf6mmWVxn/VzmPQcAAMDg9ZtBl9yxvfU1izNfphdWmXatDUyZu9evYJDN4rxgnYs4LbPnqJ4SLQeDo3boSeEtV12zOM4ZAAAADEXfAXo8iz7gZnETlKI2X5BFD2e4k6ZCq6FZXOfGY2TP20aFmrO5DnupsFlcZ20P2XMAAAAMRxUZdOkni95nsziz/q5VGsa1QWYWPR689z09m/eDTGjLnPek1xgu6agH1CyOcwYAAABDU0mA7ppFz1S+WZyxvHqGTPb3v0Cdgo7uZZrFuU/PFilz79zwEtnzNjnvSdsYbqsixiFDXnGzuM5bZM8BAAAwRFVl0MUl81RTszjxS93p6t5wdTSLS5mebVNpWRjLJ7ndlv33cmpAXXOzOLLnAAAAGKrKAvTcju71NoszZld3yEyfe0GNbBZd++dIvc3ilq5/WTGHdYucf8SWtu+RrHHiWf+d/pvFdX50HtlzAAAADFeVGXQJZ9EH3CwuuL28uoNS9yYzWcqam8Vtiqaaok0ueCJW2u7LypbX0CyO7DkAAACGrtIA3c+iH05doN5mccZ2uro328W/lJNZWfSkZnFpy0pC8K5Elj6/Sva8ZWxpe2ZALsmPVdQs7tAPzyd7DgAAgOGrOoMufibKduseQrM4c3vP6g66ujfcYpHS9QIZ9016EbTLBY97pe3O48TT7i/fLI5zBgAAAI1ReYC++12vW3fWMjU2iwssr+yQqWKrYFAu+YXXr6CKZnGx4H3ps68I2fOWuPAxPWWz5yWy5ZkZ9vAv+ess/fCCbZwzAAAAaIQ6MugmSDdZ9FOJD9bfLE5suSyl7s0WdMyurlncKTKhrbOstNe1PeBSvh6/WJe6XH6zuFNvXriNsecAAABojFoCdJ+d5sqpWVxc/83ijF0rO2j81FQmi678LHpFzeIWr3uN7HlbfPJRvai07Aofbs448eRlSnxWhJZjKj4AAAA0itK635R2ujfeI2smUE5MXqlYnBVaRsd/d3wsuF9Hb39072/scaBhvvsnMqWV/I95De3rqER+718y0tv8HnHhn2rrX2zZU9ccZ0hDW3zyUW2mQ3xLJPR6iv+ZELxvY+/x4DXv/h56LHJ/eFsS37YOL7f+L5/cxrSMAAAAaJQ6M+iSl0XP5No0Kr+L88oKU681UpBFl/6bxVEp0RIXPWLHna+Ej9Z1bHnFzeLmRuuZBQAAwCioNUDf/a5sZE275toszrnMPXl7ZowrGfTmWuyzWdypa96g30CLrITHnZeZJq2CZnGHfnAR06oBAACgeerOoIs/7Vpvw7jBNIsLbu9c2UEQ10SXvON1dO+jWRzZ85a4+FvaNIXbmXS02Znv0AP9N4ujmSAAAAAaq/YA3Z92bWGIzeICsys7KGttIuVn0V3K3GOPn7j6B1x4aYOLv6nnRMtscKhZWXCXZnE93D8rFv75YqZVAwAAQDMNIoNugnQz5nS154Gsjsx97jMhi24cXTlTpvvcNCoWyaLraCAeGXcuPUE7Xbhb4JJ/0tNKy9HgSEsF2o4Z9sySeS2rb1yiVgQAAABoqIEE6D6TRd8svFZ1zeKCx9ZWzqTjd9MobyhEdhZdR4L29avepLdA0+0zwXm4B0SJceY9yjWL2+SCDgAAAJpuYAH67ndtljRxvPCAmsUFJmxn9zPp7N4kJouutNfR3bFZHGPPG27fN/SkaDsEYSLpSJ0z3w7rODSLW3xjn6IxHAAAABptkBl0E6Sb5kzrkTsrbBYXl1LmbtbZGZ/qCY2w6Ngsbv1TPyJ73nRK29eo2xTOJaCuqVnc+vFLFY3hAAAA0HgDDdBla/7hnlL3KprFpWbhkoOBXS+fSYOxJtnnj0V3aBZH9rzhLv26Xo4E5+HDdc18V9MsznzW0BwSAAAArTDwAD2x1H3wzeKC32cJ0ptFaVlKaxYn3mu2Pr9G9rzJLjtsp1Prdmwv0/itwmZxi69fSmk7AAAA2mHgAbpx9u8SSt3zVN8sLmCCdDJsDbHvHdkw50ZSFl15gTuvVYNdvqQXVNp0auHDHkyzuNXXL6O0HQAAAO0xlADdtzde6j7gZnHh9Y++/EcEfk3RLWHvbRbXmfuxkA1tqMuXtHkPPRwcXb+N3/psFkdpOwAAAFpnaAH62b+T05Ev0MNpFhdGkN4Q+96RNdFehUWsSRxjzxvqiof1XNpc586N31KWyVwn/WLd3GuXq9NtfT4BAAAwnoaZQTdBuumkfjh83xCaxYUfO/o9gvSmWIyVuXf2/4TseRPt/1o0OM8yoGZxh79/hWKWBgAAALTOUAN0n8mKnrA3B9QsLusxRZDeCJf+m59F12TPm2z/V/WcpGTOe34fTLO4E9/frxba/8wCAABgHA09QA+VuvdMvZaogmZxqWXuW7cJ0pshyKJ3rvgZ2fOmmf1Kell7/PcyfSNKNIvbVIw7BwAAQIs1IYNugnTTubub9Rpis7jw7aPf+2O+7A+TzaKLrIoWMqINM2eCc3Esa88aZuKSIXdvFrfwyqzaaMPzBwAAACRRWvfbna06x99r5ySfNRFy5KhC0bOO/x5/LPS4Toi6dfx26PGex7zb8+f/B3OlA4G5f7Td2o8G75nu+0xt3dax92jk/tD7O/g9vlx4G911srd9eHWe0nYAAAC0W6MCdPGC9A1RslMSAu7U+xwD+Ejgn3ARIPExgnSga/7LekErfyq1AoF2PNgWyQ/uE9dJDuJPrFyppnmVAAAA0HaNKHGP6ZkffUjN4sJMuTsBOsbalV/S5j3wcOrQkvoav2WtYz4rZsb9tQEAAMBoaFyAfvbv5KRohy/cg2kWF749+9JOgnSMpysfssH5rEvQHFdzs7iZlSuZ7xwAAACjoXEl7oHj77MN2o6WHotessw9vH5KOb2ZEm7mghNCUICR9+kH9aRWsqbFG3bSU4oeL1d3GE8eKWsvOIY9WMb/ff7lTykumgEAAGBkNLHE3Tr7v222ulN6A1kZvZTbmRn2LSZQWXtpp0yVPjagBT79gDbjuk0n/Z2R90yfXdldp2PLorR0CM4BAAAwahqbQQ8cf5/N3u2yvw6/WVx4W5taycyFG8K0Thg5V92vp03mXJRMSJDFDv6TKjkTLg5Z9Iqaxa2+dJXay1kHAACAUdPYDHqI+SJ+wv46/GZxYRNKy69fnGaudIyWq75op1H7tTnHs95zQ2oWZz4LeM8BAABgJDU+g268/j5bTr5hAwbHec/zMuyJ857HH0u43bMf73bnwg2CBrTf1V/Uy1pkVmJZax1+n2SMJ09cp0gWPXsMu6lamXrpaprCAQAAYDS1IkAXL0gPxsNO9ATVw2kWF73tN4+78Nc0j0P7HLhPm4tgK1rJzngpeyRojj82uGZxdkjJiwcUQ0oAAAAwstpQ4m6d8982gx4Zd+pU5l5fs7j4fkzzuJPHzmJOZrTLgXv1jGj7/rKd2nvO95RS9syGbo7l62F506kRnAMAAGDUtSZAFy9INxn0+Z4HHOdZTu0+7RhoOMz7bBpqvXXsLFnM2CLQGNcc0otKy1v+udsjNWgu0b09c9vZ25o/dg3BOQAAAEZfa0rcw143c6QrORrc1c+c6JJUyp7wWO5+VM9y66aZ1Sf/XU72+d8FKnftop4SJSsma55Ylh56HySWnsd/L9mV3WEM+/wL1zKdGgAAAMZDqzLogXO8OdKjmfQS5baZpez9Z9vN1HAbxz4kTAeFRvnMQb3Xb7q4M3JcJUrZXbqyZ62TU/FCcA4AAICx0soMeuD1/2MD9dmGNYtL2ueqVjJ30a9oIIfhue4LelKULGsle5zmMJfQMoNvFnf4+evUAqcLAAAAxkkrM+iBc/7LTm3WaVizuCR7TMbyhQ/TQA7D8dkv2Kz5Sf9cdBr/3WNwzeI6BOcAAAAYR63OoAdMJt3O3SzJ2W3nOdHjt7MeS8kGOizXEZEFsukYhM/drSe1spUme0rNYR5bJyuL7pSVz58TvfOdz6o5Tg4AAACMo1Zn0AMmk668wDeRS+Y7a5nUDHt8uYzxtCHmQsLJ5z/C2HTU63N3eVlzpb2suVXhjAdZ67hm5WP7ITgHAADAWBuJDHrgNX9MusQz2kWy6PGsu6Rk0bPGoSesk7Kc7fR+8S/p9I7qXH+nntJix5rvshvNyIg7dV+X3ix65vYkOYveU2kSXafz7c8TnAMAAGC8jVSALkGQrvxy9+Y1i0sK0jdFZEkrWbrkF5S9o7zr79CTZviEKDno2vitIc3iDj93PWPOAQAAgJEL0I3X/q9tHmfnSc/KomfNiR5ZXlIy7K5Z9LzlvNunTHB1yS/s3NRAIQu323J2c6Fne1ZG3Dmgzlmn76z81rk//+wCU6kBAAAAMqoBuqQE6Q1rFpe8jsi6VrKw7x07TzWQ6Ybb9IwWWRSRXT0BdPgc67csPbZORc3iCM4BAACAkJEN0CUUpBcpcw8vk1XmnretgmXuSbdN07vFfe8wPh29brxFT4mSRe0P50gNoF1K1sMBuSRsIyeLXiorrwjOAQAAgLiRDtDFC9JntLJl4xOSl0V3LXMP366mWVzW7Y7JqF/6b4xPh8hNN9tx5ks2MHcZJ+7Y+G2AzeI2tZKZZ25QVIgAAAAAMSMfoIsXpE9rJWuRIL1kmXt4/YqbxaXe1sprJCdKli59m0B9HN18k54UJaaR2oIWmcgKoMs0fquzWVxoe5uiZObpGwnOAQAAgCRjEaAb33+/TInYTPrO0s3iBl/mHt+W1/FdZPmytyl9Hwe33qCnTAWFVna4xkSpceLNaBZ3wmTOn75JcYEJAAAASDE2Abp4QbopD17RsjU/dKCBzeLySuY7ZgzyZT8nUB9FJjAXr/nbbGYA7TJOXKLrD6FZ3KoWmevcTHAOAAAAZBmrAD3w/ffLspatMbzS7GZxPduKXSBYNVn1y39uS/jRcrddr2f8UvY98dc+HkA7jxNPOhcH1yyus3yLmuO8BAAAAPKNZYBuvPp+fxq2lCy1NK9ZXGqw798+4TcPW7niZ4xTb5M7Pm8bv+3VYkvZd9pDV72vfV/jxIfTLG7+6K10agcAAABcjW2ALl6QPi3h5nEFytxlOM3iso9NdRvKmaBo6YqfUf7eZHd+1htfLiJzomQiLeju+ekYQNfaLC67zN02g3vqNprBAQAAAEWMdYBuvPoBOy59zTaPyyhz797OemyAZe5p+48ta7PqZtz9/p+SVW+Cu67zsuUSypbnjS0P9D1OfDDN4k5okZmnbme8OQAAAFDU2AfogVc/YLPOs/EgvQXN4nrK7FPWNfOpr8z+xHayx4Ddfa3eK8oG5rN5QXdSVrzn3MgI6IfYLO7wkTvUAucWAAAAUA4BesirH5A5rWzG2Za8t6xZ3Nbt7G1s2k72SlbmfkywXqd7rtF7/bHle7XaOqfKjC3vWb7MOPGUbVfQLM6cUwtH7mS8OQAAANAPAvSYVz8g01rZbPrOljaLS103MVhXdto5U+K/Mr9OGXw/Dl7dLV+f0V62PHHeckl4TVyCbgkH6FIw6C4Y0BdYxwyjmHviLsabAwAAAP0iQE/wygftuHQzB/X1TWkW51zmLslBnePxrJvMuhmTf+VbQsDl4NCntGk0GATku7Ke336D7p6fGdt2LXOXcEDvWOYeWuewmYv/ibsYbw4AAABUgQA9wysfFDNu2GTTJ1reLC77dvIYdlO2vKa9Lvdrn/oRAbtx37w2FRZmrvIZmyn3s+QSD2wTnt9+x5aHl+9jnHgVzeI2tZK5x+9WDJEAAAAAKkSAnsNm070y8F12yfY3i8s9npT1Nm2wrmygvqZFNq56c7RL4u/fr81r7wXkYv+Z2xN5z1tWv4AiQXe/AX3VzeL839dNs7vHvkDWHAAAAKgaAbqj1TPsfNWL4fmqrQZk0Ssuc09dPiHIO2UCdu0F7fbf1T9o59zrD16up0Vkyg/Ip20wLrLdtcQ8M4tecZl7ZPmcbVfYLM5UVCw+elAtFXxqAQAAADgiQC9g9QwbwC3bscbh1drfLK5YcJ+xH/+naRx22i+PP22DeJHT1x4fbpn8l/fZIHzSz4RP+s3cTJbcm49cssdlB9KC6W65eM7zmxR0O5SVlwroK2oWt24awT1yULXy4gsAAADQFgToJazukAXtNZHbmjor2EyBZnGuZe5SNLgO3y4Z6BcurXfcdjeAV3La39dabJ216DraJWietFnv6H5m/GOb1OGO/EUC7Zz/X9bzVkcW3bnMPe//4N4szgxrWHxkkaw5AAAAMAgE6CWt7pApLXbO9D1NKHNP23+h7RUZw95HQJ90gSJ9nYQAPXedYhcSso4nN4uelXl2OTbXoLvABYZ4QF+yWdyqVrLwrUNkzQEAAIBBIUDv08oOO73WkijZXnWzuL6Ca0kP6lyPp/BFgZzlE7O2WesHAbq4B81l9pMZaGc8H3nPm/Ox5Wy334A+K4uesO1TWmThm/fRoR0AAAAYtG084/3Z+xs7b7gZ23w4FDuF4+0eSpdYzuG2pVNulzietH2W3k+J9VVoLeW8TvH9pC1X5nXsea5cjs11P1mvScq2s44ttr1D5lwmOAcAAACGgwx6hVbOtGXvtolcPKMtDqXfrhnvxNsZZe5p67ocT9ksumuZe3j9zDL32DL56xTbT24WvUCJebdcvMixJf3MKEXPO4bcLHp027YJ3Dfup5wdAAAAGCYC9Bq8fKbsFSVLdpouyR+LPsbN4vLHvXfXcWoWV3o/mYF2zv8v6XXMKnOPHFuB4+nux7XMPe//oLxy9q8/QMYcAAAAaAIC9Bq9/EeyaAIgM3d6sBeXLHTici63M7LohbY3gGZxhQJ8msVV3SzOdGdfOvygWhQAAAAAjcEY9Bqd9592KrYp8cb2Jksb+1zmuknW2Oc+xzG7ct1Pv+PeXdcps5+05UqNYXc9tj5fk8xx79FjM+fiFME5AAAA0Dxk0Afke38sU+LNnT5bdxa96jL3tOUrL3NPWz+rzD13Hff95C1Xegy767EVyIonPbc52+6Y8+/hLzHOHAAAAGgqAvQBM4G6Vl6gLhnBo6QExbm3M8rcJR5QZm2PZnGj0iyuI0oWv/ZlAnMAAACg6QjQh+SlnbGMOs3imtkszjVjn1bB4HpsBQL/+NjypGMQkY4WWfzqVwjMAQAAgLYgQB8yE6jbjLryM+pSTZl7fFuFm8VlZMXTli90IYBmcXU1i7Ol7F/5KoE5AAAA0DYE6A3x4rRMiZIFLTInIhNJQXqhYD2eARbHQLuGMve05bPKz9PXSS9zr2o/uWXuKc9H3vOW9fzmZtHTqgG825sisqSVLP/j1wjMAQAAgLYiQG+YF8+SSRukK1kQfx71sll0msWV209uoJ3x/yuURc8OujOPx1//VBCYf/lhdVoAAAAAtBoBeoMd+5DNps9pJbuCo6yizF2Sguu8TLwUC7qd9kOzuNwsesrxrJt5zL90WK0IAAAAgJFBgN4Cxz7U7fy+15S/Z2axs4Lr8O2SgX5dAX1qaX5OFt21zD2yH4d1so5nSM3iTBn7iunI/uDXKWMHAAAARhEBeou88GGZNEG6VjazbrPqhYLr4EGaxbWpWZzJli+b4PyBf6KMHQAAABhlBOgt9fxH7DRtC1rZrHr6WHWaxRXej3OZe8rzkfe8ZT2//j7N2PIVU8Z+/zfJlgMAAADjggB9BHz3T2RGvO7vJrs+EfyPEjPA4dsVZNFrLXNPWz9W5i4OQXMLmsVtai8oX/7iI2pNAAAAAIwdAvQR853/ZzPqe7vBelKQKI6Bdg1Z9LxtO497H41mcZtaiWn0tnLvYzR8AwAAAMYdAfoI+/b/t4H6jCjZq/0y+FY0i3MsWZdQFt21zL3oflKHCpQfwx6Ur68depygHAAAAMAWAvQx8e0/lWm9Fax707bRLG5QzeLWtdeBfe3gEbUhAAAAAJCAAH0MPfdnMukH62bs+oxWsjN4FlIDVGl3szjXMvekdXLL3HufjxMmQ27+icjaPU/RfR0AAABAPgJ0yDN/bqdvC4J189MG7DSLc24Wd0JCAfndywTkAAAAAIojQEeip/+iG6xPm39alRjD3kdA3+Bmcae0yIYo2TAB+Z1P03EdAAAAQDUI0OGk85cyqZVMi7Jj2U3QPiVKdjk3i3Msc5dmNYtbF5GT2gvGzdjxjdufIzsOAAAAoB4E6OjL0Rk/cPeC9uD2pJ91n2h4s7hNP/A+bTPi5qefHb/luwTiAAAAAAaLAB21OvIxP3A3QbsXvJsgedIP6IOAebLbqK5EE7dYmbtp0HY6tI4NwP1lN/zHTt/4It3UAQAAADSIiPwvUUOuuZkk/GIAAAAASUVORK5CYII=\" alt=\"\" width=\"275\" height=\"100\" /></p>\n" +
                        "<p><strong>MVolt</strong> is a stable token and the energy of the platform. FOIL Network charges MVolt for the operations, preventing the abuse of node resources. It is used to pay for transactions, and it is given as a reward for building blocks.</p>\n" +
                        "<p>Native token FOIL has a set of features:</p>\n" +
                        "<p>&bull; Genesis supply 1 000 000 = 1Tb of Data<br />&bull; Additional supply will be created through<br />public key verification</p>\n" +
                        "<p style=\"text-align: left;\"><em>Source: </em><a href=\"https://foil.network/\">FOIL Network</a></p>\n" +
                        "<p style=\"text-align: left;\">&nbsp;</p>\n" +
                        "<p style=\"text-align: left;\"><a href=\"https://scan.foil.network/\">FOIL DataVision</a> (block explorer)</p>\n" +
                        "<p style=\"text-align: left;\"><a href=\"https://oldscan.foil.network/index/blockexplorer.html?blocks\">FOIL old block explorer</a></p>\n" +
                        "<p style=\"text-align: left;\"><em>Source: <a href=\"https://foil.network/\">FOIL Network</a></em></p>\n" +
                        "<p>&nbsp;</p>\n" +
                        "<div class=\"notranslate\" style=\"all: initial;\">&nbsp;</div>";
            case 18: // DOGE
                try {
                    return new String(Files.readAllBytes(Paths.get("images/icons/assets/DOGE.txt"))) ;
                } catch (Exception e) {
                }
                return "";
            case 20: // LTC
                try {
                    return new String(Files.readAllBytes(Paths.get("images/icons/assets/LTC.txt"))) ;
                } catch (Exception e) {
                }
                return "";
            case 22: // LTC
                try {
                    return new String(Files.readAllBytes(Paths.get("images/icons/assets/DASH.txt"))) ;
                } catch (Exception e) {
                }
                return "";

        }

        if (maker.equals(ShibaVerseDAPP.MAKER))
            return ShibaVerseDAPP.viewDescription(this, description);
        else if (maker.equals(MemeTheGatheringDAPP.MAKER))
            return MemeTheGatheringDAPP.viewDescription(this, description);

        return this.description;
    }

    @Override
    public String[] getTags() {
        String tagType = ":" + viewAssetTypeAbbrev().toLowerCase();

        String[] tagsArray = super.getTags();
        if (tagsArray == null)
            return new String[]{tagType};

        String[] tagsArrayNew = new String[tagsArray.length + 1];
        System.arraycopy(tagsArray, 0, tagsArrayNew, 0, tagsArray.length);
        tagsArrayNew[tagsArray.length] = tagType;

        return tagsArrayNew;
    }

    @Override
    public byte[] getIcon() {
        switch ((int) key) {
            case 1:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/FOIL.png"));
                } catch (Exception e) {
                }
                return icon;
            case 2:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/MVolt.png"));
                } catch (Exception e) {
                }
                return icon;
            case (int) BTC_KEY:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/BTC.gif"));
                } catch (Exception e) {
                }
                return icon;
            case 14:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/ETH.png"));
                } catch (Exception e) {
                }
                return icon;
            case 16:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/BNB.png"));
                } catch (Exception e) {
                }
                return icon;
            case 18:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/DOGE.png"));
                } catch (Exception e) {
                }
                return icon;
            case 20:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/LTC.png"));
                } catch (Exception e) {
                }
                return icon;
            case 22:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/DASH.png"));
                } catch (Exception e) {
                }
                return icon;
            case 24:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/ZEN.png"));
                } catch (Exception e) {
                }
                return icon;
            case 1643:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/RUB.png"));
                } catch (Exception e) {
                }
                return icon;
            case 1156:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/CNY.png"));
                } catch (Exception e) {
                }
                return icon;
            case (int) EUR_KEY:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/EUR.png"));
                } catch (Exception e) {
                }
                return icon;
            case (int) USD_KEY:
                try {
                    icon = Files.readAllBytes(Paths.get("images/icons/assets/USD.png"));
                } catch (Exception e) {
                }
                return icon;
        }
        return icon;
    }

    public abstract long getQuantity();

    public abstract int getScale();

    public static ExLinkAddress[] getDefaultDEXAwards(int type, Account owner) {
        if (type == AS_NON_FUNGIBLE) {
            return new ExLinkAddress[]{new ExLinkAddress(owner, 10000, "Author royalty")};
        }
        return null;
    }

    public ExLinkAddress[] getDEXAwards() {
        if ((flags & APP_DATA_DEX_AWARDS_MASK) == 0) {
            return getDefaultDEXAwards(assetType, maker);
        }
        return dexAwards;
    }

    @Override
    public HashMap getNovaItems() {
        return BlockChain.NOVA_ASSETS;
    }

    public boolean hasDEXAwards() {
        return (flags & APP_DATA_DEX_AWARDS_MASK) != 0;
    }

    public boolean isMovable() {
        if (false && this.key < BlockChain.AMOUNT_SCALE_FROM) {
            return (this.typeBytes[1] & (byte) 1) > 0;
        }
        return this.assetType == AS_OUTSIDE_GOODS;
    }

    public boolean isImMovable() {
        if (false && this.key < BlockChain.AMOUNT_SCALE_FROM) {
            return (this.typeBytes[1] & (byte) 1) <= 0;
        }
        return this.assetType == AS_OUTSIDE_IMMOVABLE;
    }

    public boolean isInsideType() {
        return this.assetType == AS_INSIDE_ASSETS
                || this.assetType >= AS_INSIDE_CURRENCY
                && this.assetType <= AS_INSIDE_OTHER_CLAIM;
    }

    public boolean isInsideCurrency() {
        return this.assetType == AS_INSIDE_CURRENCY;
    }

    public boolean isInsideUtility() {
        return this.assetType == AS_INSIDE_UTILITY;
    }

    public boolean isInsideShare() {
        return this.assetType == AS_INSIDE_SHARE;
    }

    public boolean isInsideBonus() {
        return this.assetType == AS_INSIDE_BONUS;
    }

    public boolean isInsideAccess() {
        return this.assetType == AS_INSIDE_ACCESS;
    }

    public boolean isInsideVote() {
        return this.assetType == AS_INSIDE_VOTE;
    }

    public boolean isIndex() {
        return this.assetType == AS_INDEX;
    }

    public boolean isInsideOtherClaim() {
        return this.assetType == AS_INSIDE_OTHER_CLAIM;
    }

    /**
     * Их нельзя вернуть из долга самостоятельно
     *
     * @return
     */
    public boolean isNotReDebted() {
        return isOutsideType();
    }

    public boolean isOutsideType() {
        return isOutsideType(this.assetType);
    }

    public static boolean isOutsideType(int assetType) {
        return // ?? this.assetType == AS_OUTSIDE_GOODS ||
                assetType >= AS_OUTSIDE_CURRENCY
                        && assetType <= AS_OUTSIDE_OTHER_CLAIM;
    }

    public boolean isOutsideCurrency() {
        return this.assetType == AS_OUTSIDE_CURRENCY;
    }

    public boolean isOutsideService() {
        return this.assetType == AS_OUTSIDE_SERVICE;
    }

    public boolean isOutsideShare() {
        return this.assetType == AS_OUTSIDE_SHARE;
    }

    public boolean isOutsideBill() {
        return this.assetType == AS_OUTSIDE_BILL || this.assetType == AS_OUTSIDE_BILL_EX;
    }

    public boolean isOutsideBillEx() {
        return this.assetType == AS_OUTSIDE_BILL_EX;
    }

    public boolean isOutsideOtherClaim() {
        return this.assetType == AS_OUTSIDE_OTHER_CLAIM;
    }

    public static boolean isUnHoldable(long key, int assetType) {
        if (key < getStartKey(ItemCls.ASSET_TYPE, START_KEY_OLD, MIN_START_KEY_OLD)
                || assetType == AS_INSIDE_ASSETS
                || assetType > AS_OUTSIDE_OTHER_CLAIM
                && assetType <= AS_INSIDE_OTHER_CLAIM
        ) {
            return true;
        }
        return false;
    }

    public boolean isUnHoldable() {
        return isUnHoldable(key, assetType);
    }

    public static boolean isUnSpendable(long key, int assetType) {
        return key < 100
                || assetType == AssetCls.AS_INDEX
                || assetType == AssetCls.AS_INSIDE_ACCESS
                || assetType == AssetCls.AS_INSIDE_BONUS;
    }

    public boolean isUnSpendable() {
        return isUnSpendable(key, assetType);
    }

    /**
     * нельзя передавать никак кроме со счета создателя - но торговать можно
     *
     * @return
     */
    public boolean isUnTransferable() {
        return (flags & APP_DATA_UNTRANSFERABLE_MASK) != 0;
    }

    public boolean isUnTransferable(boolean senderIsAssetMaker) {
        return isUnTransferable() && !senderIsAssetMaker;
    }

    public boolean validPair(long pairAssetKey) {
        if (assetType == AssetCls.AS_NON_FUNGIBLE) {
            if (pairAssetKey != ERA_KEY
                && pairAssetKey != FEE_KEY // instead USD
                && pairAssetKey != BTC_KEY
                && pairAssetKey != 18
                //&& pairAssetKey != USD_KEY - FEE_KEY already use!
            ) {
                return false;
            }
        }

        return true;
    }

    public static boolean isUnDebtable(long key, int assetType) {
        return assetType == AssetCls.AS_INDEX
                || assetType == AssetCls.AS_INSIDE_BONUS;
    }

    public boolean isUnDebtable() {
        return isUnDebtable(key, assetType);
    }

    public static boolean isTypeUnique(int assetType, long quantity) {
        if (quantity == 1L
                || assetType == AS_OUTSIDE_BILL
                || assetType == AS_OUTSIDE_BILL_EX
                || assetType == AS_BANK_GUARANTEE
                || assetType == AS_NON_FUNGIBLE
        ) {
            return true;
        }
        return false;
    }

    public abstract boolean isUnique();

    public abstract boolean isUnlimited(Account address, boolean notAccounting);

    /**
     * Управлять может только сам обладатель
     *
     * @return
     */
    public boolean isSelfManaged() {
        return assetType >= AS_SELF_MANAGED_ACCOUNTING;
    }

    /**
     * Активы у которых есть только 4-ре баланса и каждый из них имеет возможность забрать - backward
     *
     * @return
     */
    public boolean isDirectBalances() {
        return assetType >= AS_SELF_MANAGED_ACCOUNTING;
    }

    public static boolean isAccounting(int assetType) {
        return assetType >= AS_ACCOUNTING;
    }

    public boolean isAccounting() {
        return isAccounting(assetType);
    }

    public boolean isSendPersonProtected() {
        return (key <= AssetCls.ERA_KEY || key > getStartKey()) // GATE Assets
                && assetType != AssetCls.AS_NON_FUNGIBLE
                && !isAccounting()
                && assetType != AssetCls.AS_INSIDE_BONUS
                && assetType != AssetCls.AS_INSIDE_VOTE;
    }

    /**
     * anonymous ownership is denied
     *
     * @return
     */
    public boolean isAnonimDenied() {
        return (flags & APP_DATA_ANONIM_PROTECT_MASK) != 0;
    }

    /**
     * Actions on OWN balance will update DEBT balance too
     *
     * @return
     */
    public boolean isChangeDebtBySendActions() {
        return this.assetType == AS_SELF_ACCOUNTING_CASH_FUND;
    }

    public static boolean isChangeDebtBySpendActions(int assetType) {
        return isOutsideType(assetType);
    }

    public boolean isChangeDebtBySpendActions() {
        return isChangeDebtBySpendActions(this.assetType);
    }

    /**
     * Если обратный Послать то в меню местами меняем
     *
     * @return
     */
    public static boolean isReverseSend(int assetType) {
        return assetType == AS_SELF_MANAGED_ACCOUNTING
                || assetType == AS_SELF_ACCOUNTING_MUTUAL_AID_FUND
                || assetType == AS_SELF_ACCOUNTING_CASH_FUND;
    }

    public boolean isReverseSend() {
        return isReverseSend(this.assetType);
    }


    /**
     * в обычном состоянии тут отрицательные балансы или нет?
     *
     * @param balPos
     * @return
     */
    public static boolean isReverseBalancePos(int assetType, int balPos) {

        switch (balPos) {
            case Account.BALANCE_POS_OWN:
                return isReverseSend(assetType);
            //case Account.BALANCE_POS_SPEND:
            //    return true;
        }
        return false;
    }

    /**
     * в обычном сотоянии тут отрицательные балансы или нет?
     *
     * @param balPos
     * @return
     */
    public boolean isReverseBalancePos(int balPos) {
        return isReverseBalancePos(this.assetType, balPos);
    }

    public BigDecimal defaultAmountAssetType() {
        switch (assetType) {
            case AS_BANK_GUARANTEE:
            case AS_NON_FUNGIBLE:
                return BigDecimal.ONE;
        }
        return isUnique() ? BigDecimal.ONE : null;
    }

    public PublicKeyAccount defaultRecipient(int actionType, boolean backward) {

        if (isOutsideType()) {
            if (actionType == Account.BALANCE_POS_SPEND
                    || actionType == Account.BALANCE_POS_DEBT) {
                return getMaker();
            }
        }

        return null;
    }

    public static String viewAssetTypeCls(int assetType) {
        switch (assetType) {
            case AS_OUTSIDE_GOODS:
                return "Movable";
            case AS_OUTSIDE_IMMOVABLE:
                return "Immovable";

            case AS_OUTSIDE_CURRENCY:
                return "Outside Currency";
            case AS_OUTSIDE_WORK_TIME_HOURS:
                return "Work Time [hours]";
            case AS_OUTSIDE_WORK_TIME_MINUTES:
                return "Work Time [minutes]";
            case AS_OUTSIDE_SERVICE:
                return "Outside Service";
            case AS_OUTSIDE_SHARE:
                return "Outside Share";
            case AS_OUTSIDE_BILL:
                return "AS_OUTSIDE_BILL_N";
            case AS_OUTSIDE_BILL_EX:
                return "AS_OUTSIDE_BILL_EX_N";
            case AS_MY_DEBT:
                return "AS_MY_DEBT_N";
            case AS_OUTSIDE_OTHER_CLAIM:
                return "Outside Other Claim";

            case AS_INSIDE_ASSETS:
                return "Digital Asset";
            case AS_INSIDE_CURRENCY:
                return "Digital Currency";
            case AS_INSIDE_UTILITY:
                return "Digital Utility";
            case AS_INSIDE_SHARE:
                return "Digital Share";
            case AS_INSIDE_BONUS:
                return "Digital Bonus";
            case AS_INSIDE_ACCESS:
                return "Digital Access";
            case AS_INSIDE_VOTE:
                return "Digital Vote";
            case AS_BANK_GUARANTEE:
                return "Bank Guarantee";
            case AS_BANK_GUARANTEE_TOTAL:
                return "Accounting Bank Guarantee";
            case AS_NON_FUNGIBLE:
                return "Non Fungible Token";
            case AS_INDEX:
                return "Index";
            case AS_INSIDE_OTHER_CLAIM:
                return "Other Digital Claim";

            case AS_ACCOUNTING:
                return "Accounting";
            case AS_SELF_MANAGED_ACCOUNTING:
                return "Self Managed";
            case AS_SELF_ACCOUNTING_LOAN:
                return "AS_SELF_ACCOUNTING_LOAN_N";
            case AS_SELF_ACCOUNTING_MUTUAL_AID_FUND:
                return "AS_SELF_ACCOUNTING_MUTUAL_AID_FUND_N";
            case AS_SELF_ACCOUNTING_CASH_FUND:
                return "AS_SELF_ACCOUNTING_CASH_FUND_N";
            case AS_SELF_MANAGED_DIRECT_SEND:
                return "AS_SELF_MANAGED_DIRECT_SEND_N";
            case AS_SELF_MANAGED_SHARE:
                return "AS_SELF_MANAGED_SHARE_N";

        }
        return null;
    }

    public String viewAssetType() {
        return viewAssetTypeCls(this.assetType);
    }

    public static String viewAssetTypeFullCls(int assetType) {
        switch (assetType) {
            case AS_OUTSIDE_GOODS:
                return "Movable Goods";
            case AS_OUTSIDE_IMMOVABLE:
                return "Immovable Goods, Real Estate";
            case AS_OUTSIDE_CURRENCY:
                return "Outside Currency";
            case AS_OUTSIDE_WORK_TIME_HOURS:
                return "Work Time [hours]";
            case AS_OUTSIDE_WORK_TIME_MINUTES:
                return "Work Time [minutes]";
            case AS_OUTSIDE_SERVICE:
                return "Outside Service";
            case AS_OUTSIDE_SHARE:
                return "Outside Share Rights";
            case AS_OUTSIDE_BILL:
                return "AS_OUTSIDE_BILL_NF";
            case AS_OUTSIDE_BILL_EX:
                return "AS_OUTSIDE_BILL_EX_NF";
            case AS_MY_DEBT:
                return "AS_MY_DEBT_NF";
            case AS_OUTSIDE_OTHER_CLAIM:
                return "Other Outside Right of Claim";

            case AS_INSIDE_ASSETS:
                return "Digital Asset";
            case AS_INSIDE_CURRENCY:
                return "Digital Currency";
            case AS_INSIDE_UTILITY:
                return "Digital Utility";
            case AS_INSIDE_SHARE:
                return "Digital Share";
            case AS_INSIDE_BONUS:
                return "Bonuses, Loyalty Points";
            case AS_INSIDE_ACCESS:
                return "Digital Access Rights";
            case AS_INSIDE_VOTE:
                return "Digital Vote";
            case AS_BANK_GUARANTEE:
                return "Bank Guarantee";
            case AS_BANK_GUARANTEE_TOTAL:
                return "Accounting Bank Guarantee";
            case AS_NON_FUNGIBLE:
                return "Non Fungible Token";
            case AS_INDEX:
                return "Digital Index";
            case AS_INSIDE_OTHER_CLAIM:
                return "Other Digital Right of Claim";

            case AS_ACCOUNTING:
                return "Accounting";
            case AS_SELF_MANAGED_ACCOUNTING:
                return "Self Managed for Accounting";
            case AS_SELF_ACCOUNTING_LOAN:
                return "AS_SELF_ACCOUNTING_LOAN_NF";
            case AS_SELF_ACCOUNTING_MUTUAL_AID_FUND:
                return "AS_SELF_ACCOUNTING_MUTUAL_AID_FUND_NF";
            case AS_SELF_ACCOUNTING_CASH_FUND:
                return "AS_SELF_ACCOUNTING_CASH_FUND_NF";
            case AS_SELF_MANAGED_DIRECT_SEND:
                return "AS_SELF_MANAGED_DIRECT_SEND_NF";
            case AS_SELF_MANAGED_SHARE:
                return "AS_SELF_MANAGED_SHARE_NF";

        }
        return null;
    }

    public static String viewAssetTypeFullClsAndChars(int assetType) {
        return charAssetType(Long.MAX_VALUE, assetType) + viewAssetTypeAbbrev(assetType) + ":" + viewAssetTypeFullCls(assetType);
    }

    public String viewAssetTypeFullClsAndChars() {
        return charAssetType(Long.MAX_VALUE, assetType) + viewAssetTypeAbbrev(assetType) + ":" + viewAssetTypeFullCls(assetType);
    }

    public static String viewAssetTypeAbbrev(int asset_type) {
        switch (asset_type) {
            case AS_OUTSIDE_GOODS:
                return "OGd";
            case AS_OUTSIDE_IMMOVABLE:
                return "UIm";
            case AS_OUTSIDE_CURRENCY:
                return "OCr";
            case AS_OUTSIDE_WORK_TIME_HOURS:
                return "WH";
            case AS_OUTSIDE_WORK_TIME_MINUTES:
                return "WM";
            case AS_OUTSIDE_SERVICE:
                return "OSv";
            case AS_OUTSIDE_SHARE:
                return "OSh";
            case AS_OUTSIDE_BILL:
                return "PNo"; // Promissory Note";
            case AS_OUTSIDE_BILL_EX:
                return "BEx"; //Bill of Exchange";
            case AS_MY_DEBT:
                return "Dbt"; // Debt to Loaner
            case AS_OUTSIDE_OTHER_CLAIM:
                return "OCl";

            case AS_INSIDE_ASSETS:
                return "Ast";
            case AS_INSIDE_CURRENCY:
                return "Cur";
            case AS_INSIDE_UTILITY:
                return "Utl";
            case AS_INSIDE_SHARE:
                return "Shr";
            case AS_INSIDE_BONUS:
                return "Bon";
            case AS_INSIDE_ACCESS:
                return "Rit";
            case AS_INSIDE_VOTE:
                return "Vte";
            case AS_BANK_GUARANTEE:
                return "BGu";
            case AS_BANK_GUARANTEE_TOTAL:
                return "BGuT";
            case AS_NON_FUNGIBLE:
                return "NFT";
            case AS_INDEX:
                return "Idx";
            case AS_INSIDE_OTHER_CLAIM:
                return "CLM";

            case AS_ACCOUNTING:
                return "Acc";
            case AS_SELF_MANAGED_ACCOUNTING:
                return "SAcc";
            case AS_SELF_ACCOUNTING_LOAN:
                return "AccL";
            case AS_SELF_ACCOUNTING_MUTUAL_AID_FUND:
                return "AccAF";
            case AS_SELF_ACCOUNTING_CASH_FUND:
                return "AccCF";
            case AS_SELF_MANAGED_DIRECT_SEND:
                return "AccDS";
            case AS_SELF_MANAGED_SHARE:
                return "AccSh";
        }
        return "?";
    }

    public String viewAssetTypeAbbrev() {
        return viewAssetTypeAbbrev(assetType);
    }

    public String viewAssetTypeFull() {
        return viewAssetTypeFullCls(this.assetType);
    }

    public static String viewAssetTypeDescriptionCls(int assetType) {
        switch (assetType) {
            case AS_OUTSIDE_GOODS:
                return "Movable things and goods. These goods can be taken for storage by the storekeeper or for confirmation of delivery. In this case you can see the balances on the accounts of storekeepers and delivery agents";
            case AS_OUTSIDE_IMMOVABLE:
                return "Real estate and other goods and things not subject to delivery. Such things can be taken and given for rent and handed over to the guard";
            case AS_OUTSIDE_CURRENCY:
                return "AS_OUTSIDE_CURRENCY_D";
            case AS_OUTSIDE_WORK_TIME_HOURS:
                return "AS_OUTSIDE_WORK_TIME_HOURS_D";
            case AS_OUTSIDE_WORK_TIME_MINUTES:
                return "AS_OUTSIDE_WORK_TIME_MINUTES_D";
            case AS_OUTSIDE_SERVICE:
                return "An external service that needs to be provided outside. To notify your wish to provide services you must make demands and then confirm the fulfillment";
            case AS_OUTSIDE_SHARE:
                return "External shares which have to be transferred to an external depository. The depositary can be notified by presenting the claim and then confirm the shares transfer";
            case AS_OUTSIDE_BILL:
                return "AS_OUTSIDE_BILL_D";
            case AS_OUTSIDE_BILL_EX:
                return "AS_OUTSIDE_BILL_EX_D";
            case AS_MY_DEBT:
                return "AS_MY_DEBT_D";
            case AS_OUTSIDE_OTHER_CLAIM:
                return "Other external rights, requirements and obligations. Any obligation (as well as other external assets), which can be claimed by the record \"summon\" and discharged by the record \"confirmation of fulfillment\" of this obligation. You can take it into your hands";
            case AS_INSIDE_ASSETS:
                return "AS_INSIDE_ASSETS_D";
            case AS_INSIDE_CURRENCY:
                return "Digital money";
            case AS_INSIDE_UTILITY:
                return "AS_INSIDE_UTILITY_D";
            case AS_INSIDE_SHARE:
                return "Digital share. The share of ownership of an external or internal enterpris, the possession of which establishes the right to own the corresponding share of the enterprise without the need to take any external actions";
            case AS_INSIDE_BONUS:
                return "AS_INSIDE_BONUS_D";
            case AS_INSIDE_ACCESS:
                return "Digital rights of access and control, membership, pass";
            case AS_INSIDE_VOTE:
                return "A digital voice for voting";
            case AS_BANK_GUARANTEE:
                return "A digital bank guarantee";
            case AS_BANK_GUARANTEE_TOTAL:
                return "A digital accounting bank guarantee";
            case AS_NON_FUNGIBLE:
                return "AS_NON_FUNGIBLE_D";
            case AS_INDEX:
                return "Index on foreign and domestic assets, for example currencies on FOREX";
            case AS_INSIDE_OTHER_CLAIM:
                return "Other digital rights, requirements and obligations. These assets (as well as other digital assets) can be given in debt and seized by the lender";
            case AS_ACCOUNTING:
                return "AS_ACCOUNTING_D";
            case AS_SELF_MANAGED_ACCOUNTING:
                return "AS_SELF_MANAGED_ACCOUNTING_D";
            case AS_SELF_ACCOUNTING_LOAN:
                return "AS_SELF_ACCOUNTING_LOAN_D";
            case AS_SELF_ACCOUNTING_MUTUAL_AID_FUND:
                return "AS_SELF_ACCOUNTING_MUTUAL_AID_FUND_D";
            case AS_SELF_ACCOUNTING_CASH_FUND:
                return "AS_SELF_ACCOUNTING_CASH_FUND_D";
            case AS_SELF_MANAGED_DIRECT_SEND:
                return "AS_SELF_MANAGED_DIRECT_SEND_D";
            case AS_SELF_MANAGED_SHARE:
                return "AS_SELF_MANAGED_SHARE_D";

        }
        return "";
    }

    public static String viewAssetTypeDescriptionDEX(int assetType, long key) {
        if (key < 100)
            return "AS_CURRENCY_100_DEX";

        switch (assetType) {
            case AS_NON_FUNGIBLE:
                return "AS_NON_FUNGIBLE_DEX";
        }
        return "Not used by default";
    }

    public static String viewAssetTypeAction(long assetKey, int assetType, boolean backward, int actionType, boolean isCreatorMaker) {
        switch (assetType) {
            case AS_OUTSIDE_IMMOVABLE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : "Transfer to the ownership ";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "Confiscate from rent" : "Transfer to rent";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return backward ? null : "Return from rent";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "The employment security/received from security" : null;
                    default:
                        return null;
                }
            case AS_OUTSIDE_CURRENCY:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null // для формирования списка действия надо выдать НУЛЬ
                                : isCreatorMaker ? "AS_OUTSIDE_CURRENCY_Issue" : "AS_OUTSIDE_CURRENCY_1";
                    case Account.BALANCE_POS_DEBT:
                        return isCreatorMaker ? null
                                : backward ? "AS_OUTSIDE_CURRENCY_2B" // Отозвать требование об исполнении денежного требования
                                : "AS_OUTSIDE_CURRENCY_2"; // Потребовать исполнения денежного требования
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null
                                : isCreatorMaker ? null
                                : "AS_OUTSIDE_CURRENCY_4"; // Подтвердить исполнение денежного требования
                    default:
                        return null;
                }
            case AS_OUTSIDE_WORK_TIME_HOURS:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : "AS_OUTSIDE_WORK_TIME_HOURS_1"; // Передать в собственность рабочие часы
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_OUTSIDE_WORK_TIME_HOURS_2B" // Отозвать требование траты рабочих часов
                                : "AS_OUTSIDE_WORK_TIME_HOURS_2"; // Потребовать потратить рабочие часы
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_OUTSIDE_WORK_TIME_HOURS_4"; // Подтвердить затраты рабочих часов
                    default:
                        return null;
                }
            case AS_OUTSIDE_WORK_TIME_MINUTES:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : "AS_OUTSIDE_WORK_TIME_MINUTES_1"; // Передать в собственность рабочие минуты
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_OUTSIDE_WORK_TIME_MINUTES_2B" // Отозвать требование траты рабочих минут
                                : "AS_OUTSIDE_WORK_TIME_MINUTES_2"; // Потребовать потратить рабочие минуты
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_OUTSIDE_WORK_TIME_MINUTES_4"; // Подтвердить затраты рабочих минут
                    default:
                        return null;
                }
            case AS_OUTSIDE_SERVICE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : "Transfer Service Requirement";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "To reduce the provision of services" // Отозвать требование в предоставлении услуг
                                : "To require the provision of services";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "Confirm the provision of services";
                    default:
                        return null;
                }
            case AS_OUTSIDE_SHARE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : "To transfer shares in the property";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "To reduce the transfer of shares"
                                : "To require the transfer of shares";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return backward ? null : "Return debt";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "Confirm receipt of shares";
                    default:
                        return null;
                }
            case AS_OUTSIDE_BILL:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : "AS_OUTSIDE_BILL_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_OUTSIDE_BILL_2B"
                                : "AS_OUTSIDE_BILL_2";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_OUTSIDE_BILL_4";
                    default:
                        return null;
                }
            case AS_OUTSIDE_BILL_EX:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : "AS_OUTSIDE_BILL_EX_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_OUTSIDE_BILL_EX_2B"
                                : "AS_OUTSIDE_BILL_EX_2";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_OUTSIDE_BILL_EX_4";
                    default:
                        return null;
                }
            case AS_MY_DEBT:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null
                                : isCreatorMaker ? "AS_MY_DEBT_Issue" : "AS_MY_DEBT_1";
                    case Account.BALANCE_POS_DEBT:
                        return isCreatorMaker ? null // эмитент долга не может делать требования
                                : backward ? "AS_MY_DEBT_2B"
                                : "AS_MY_DEBT_2";
                    case Account.BALANCE_POS_SPEND:
                        return isCreatorMaker ? null // эмитент долга не может делать погашения
                                : backward ? null : "AS_MY_DEBT_4";
                    default:
                        return null;
                }
            case AS_OUTSIDE_OTHER_CLAIM:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_OUTSIDE_OTHER_CLAIM_Issue"
                                : "AS_OUTSIDE_OTHER_CLAIM_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_OUTSIDE_OTHER_CLAIM_2B"
                                : "AS_OUTSIDE_OTHER_CLAIM_2";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_OUTSIDE_OTHER_CLAIM_4";
                    default:
                        return null;
                }
            case AS_INSIDE_CURRENCY:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_INSIDE_CURRENCY_Issue"
                                : "AS_INSIDE_CURRENCY_1";
                    default:
                        return null;
                }
            case AS_INSIDE_UTILITY:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_INSIDE_UTILITY_Issue"
                                : "AS_INSIDE_UTILITY_1";
                    default:
                        return null;
                }
            case AS_INSIDE_SHARE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_INSIDE_SHARE_Issue"
                                : "AS_INSIDE_SHARE_1";
                    default:
                        return null;
                }
            case AS_INSIDE_BONUS:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_INSIDE_BONUS_Issue"
                                : "AS_INSIDE_BONUS_1";
                    default:
                        return null;
                }
            case AS_INSIDE_ACCESS:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_INSIDE_ACCESS_Issue" : "AS_INSIDE_ACCESS_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_INSIDE_ACCESS_2B"
                                : "AS_INSIDE_ACCESS_2";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return backward ? null : "AS_INSIDE_ACCESS_2R";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_INSIDE_ACCESS_4";
                    default:
                        return null;
                }
            case AS_INSIDE_VOTE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_INSIDE_VOTE_Issue" : "AS_INSIDE_VOTE_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_INSIDE_VOTE_2B"
                                : "AS_INSIDE_VOTE_2";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return backward ? null : "AS_INSIDE_VOTE_2R";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_INSIDE_VOTE_4";
                    default:
                        return null;
                }
            case AS_BANK_GUARANTEE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_BANK_GUARANTEE_Issue" : "AS_BANK_GUARANTEE_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_BANK_GUARANTEE_2B" : "AS_BANK_GUARANTEE_2";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return backward ? null : "AS_BANK_GUARANTEE_2R";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "AS_BANK_GUARANTEE_3" : null;
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_BANK_GUARANTEE_4";
                    default:
                        return null;
                }
            case AS_BANK_GUARANTEE_TOTAL:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_BANK_GUARANTEE_TOTAL_Issue" : "AS_BANK_GUARANTEE_TOTAL_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_BANK_GUARANTEE_TOTAL_2B" : "AS_BANK_GUARANTEE_TOTAL_2";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return backward ? null : "AS_BANK_GUARANTEE_TOTAL_2R";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_BANK_GUARANTEE_TOTAL_4";
                    default:
                        return null;
                }
            case AS_INSIDE_OTHER_CLAIM:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? null : isCreatorMaker ? "AS_INSIDE_OTHER_CLAIM_Issue" : "AS_INSIDE_OTHER_CLAIM_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_INSIDE_OTHER_CLAIM_2B"
                                : "AS_INSIDE_OTHER_CLAIM_2";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? null : "AS_INSIDE_OTHER_CLAIM_4";
                    default:
                        return null;
                }
            case AS_SELF_MANAGED_ACCOUNTING:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? "AS_SELF_MANAGED_ACCOUNTING_1B" : "AS_SELF_MANAGED_ACCOUNTING_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_SELF_MANAGED_ACCOUNTING_2B" : "AS_SELF_MANAGED_ACCOUNTING_2";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "AS_SELF_MANAGED_ACCOUNTING_3B" : "AS_SELF_MANAGED_ACCOUNTING_3";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? "AS_SELF_MANAGED_ACCOUNTING_4B" : "AS_SELF_MANAGED_ACCOUNTING_4";
                    default:
                        return null;
                }
            case AS_SELF_ACCOUNTING_LOAN:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? "AS_SELF_ACCOUNTING_LOAN_1B" : "AS_SELF_ACCOUNTING_LOAN_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_SELF_ACCOUNTING_LOAN_2B" : "AS_SELF_ACCOUNTING_LOAN_2";
                    case Account.BALANCE_POS_HOLD:
                        // SPEND нельзя брать так как он Баланс Мой изменит у меня
                        return backward ? "AS_SELF_ACCOUNTING_LOAN_3B" : "AS_SELF_ACCOUNTING_LOAN_3";
                    default:
                        return null;
                }
            case AS_SELF_ACCOUNTING_MUTUAL_AID_FUND:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? "AS_SELF_ACCOUNTING_MUTUAL_AID_FUND_1B" : "AS_SELF_ACCOUNTING_MUTUAL_AID_FUND_1";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? "AS_SELF_ACCOUNTING_MUTUAL_AID_FUND_4B" : "AS_SELF_ACCOUNTING_MUTUAL_AID_FUND_4";
                    default:
                        return null;
                }
            case AS_SELF_ACCOUNTING_CASH_FUND:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? "AS_SELF_ACCOUNTING_CASH_FUND_1B" : "AS_SELF_ACCOUNTING_CASH_FUND_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_SELF_ACCOUNTING_CASH_FUND_2B" : "AS_SELF_ACCOUNTING_CASH_FUND_2";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? "AS_SELF_ACCOUNTING_CASH_FUND_4B" : "AS_SELF_ACCOUNTING_CASH_FUND_4";
                    default:
                        return null;
                }
            case AS_SELF_MANAGED_DIRECT_SEND:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? "AS_SELF_MANAGED_DIRECT_SEND_1B" : "AS_SELF_MANAGED_DIRECT_SEND_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_SELF_MANAGED_DIRECT_SEND_2B" : "AS_SELF_MANAGED_DIRECT_SEND_2";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "AS_SELF_MANAGED_DIRECT_SEND_3B" : "AS_SELF_MANAGED_DIRECT_SEND_3";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? "AS_SELF_MANAGED_DIRECT_SEND_4B" : "AS_SELF_MANAGED_DIRECT_SEND_4";
                    default:
                        return null;
                }
            case AS_SELF_MANAGED_SHARE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return backward ? "AS_SELF_MANAGED_SHARE_1B" : "AS_SELF_MANAGED_SHARE_1";
                    case Account.BALANCE_POS_DEBT:
                        return backward ? "AS_SELF_MANAGED_SHARE_2B" : "AS_SELF_MANAGED_SHARE_2";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "AS_SELF_MANAGED_SHARE_3B" : "AS_SELF_MANAGED_SHARE_3";
                    case Account.BALANCE_POS_SPEND:
                        return backward ? "AS_SELF_MANAGED_SHARE_4B" : "AS_SELF_MANAGED_SHARE_4";
                    default:
                        return null;
                }

            case AS_NON_FUNGIBLE:
            case AS_INDEX:
            case AS_ACCOUNTING:
                // as default below

        }

        switch (actionType) {
            case Account.BALANCE_POS_OWN:
                return backward ? null : "Transfer to the ownership";
            case Account.BALANCE_POS_DEBT:
                return backward ? "To confiscate a debt"
                        : "Transfer to debt";
            case TransactionAmount.ACTION_REPAY_DEBT:
                return backward ? null : "Return debt";
            case Account.BALANCE_POS_HOLD:
                return isUnHoldable(assetKey, assetType) ? null
                        : backward ? "Confirm acceptance \"in hand\"" : null;
            case Account.BALANCE_POS_SPEND:
                return backward ? null : "Spend";
            case Account.BALANCE_POS_PLEDGE:
                return backward ? null //"Re-pledge"
                        : null; //"Pledge";
            case TransactionAmount.ACTION_RESERVED_6:
                // for CALCULATED TX
                return null; // backward ? "Reserved 6-" : "Reserved 6+";
        }

        return null;
    }

    public String viewAssetTypeAction(boolean backward, int actionType, boolean isCreatorMaker) {
        return viewAssetTypeAction(key, assetType, backward, actionType, isCreatorMaker);
    }

    public static String viewAssetTypeAdditionAction(long assetKey, int assetType, boolean backward, int actionType, boolean isCreatorMaker) {
        switch (assetType) {
            case AS_SELF_ACCOUNTING_CASH_FUND:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return "AS_SELF_SEND_ADDITIONAL_ACT_DEBT";
                }
        }

        if (actionType == Account.BALANCE_POS_SPEND && isChangeDebtBySpendActions(assetType)) {
            return "AdditionAction_on_isChangeDebtBySpendActions";
        }

        return null;
    }

    /**
     * isMirrorDebtBySend - same
     *
     * @param backward
     * @param actionType
     * @param isCreatorMaker
     * @return
     */
    public String viewAssetTypeAdditionAction(boolean backward, int actionType, boolean isCreatorMaker) {
        return viewAssetTypeAdditionAction(key, assetType, backward, actionType, isCreatorMaker);
    }

    /**
     * Balance Position + Backward + Action Name
     *
     * @param assetKey
     * @param assetType
     * @param isCreatorMaker
     * @param useAddedActions
     * @return
     */
    public static List<Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String>> viewAssetTypeActionsList(long assetKey,
                                                                                                  int assetType, Boolean isCreatorMaker, boolean useAddedActions) {

        List<Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String>> list = new ArrayList<>();

        String actionStr;
        String addActionStr;
        Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String> item;
        for (int balPos : TransactionAmount.ACTIONS_LIST) {

            boolean backward = !AssetCls.isReverseSend(assetType) || balPos != Account.BALANCE_POS_OWN;

            actionStr = viewAssetTypeAction(assetKey, assetType, !backward, balPos,
                    isCreatorMaker != null ? isCreatorMaker : true);
            if (actionStr != null) {
                item = new Fun.Tuple2<>(new Fun.Tuple2<>(balPos, !backward), actionStr);
                if (!list.contains(item)) {
                    list.add(item);
                    if (useAddedActions) {
                        addActionStr = viewAssetTypeAdditionAction(assetKey, assetType, !backward, balPos,
                                isCreatorMaker != null ? isCreatorMaker : true);
                        if (addActionStr != null) {
                            item = new Fun.Tuple2<>(new Fun.Tuple2<>(balPos, !backward), addActionStr);
                            list.add(item);
                        }
                    }
                }
            }

            if (isCreatorMaker == null) {
                actionStr = viewAssetTypeAction(assetKey, assetType, !backward, balPos,
                        false);
                if (actionStr != null) {
                    item = new Fun.Tuple2<>(new Fun.Tuple2<>(balPos, !backward), actionStr);
                    if (!list.contains(item)) {
                        list.add(item);
                        if (useAddedActions) {
                            addActionStr = viewAssetTypeAdditionAction(assetKey, assetType, !backward, balPos,
                                    false);
                            if (addActionStr != null) {
                                item = new Fun.Tuple2<>(new Fun.Tuple2<>(balPos, !backward), addActionStr);
                                list.add(item);
                            }
                        }
                    }
                }
            }

            actionStr = viewAssetTypeAction(assetKey, assetType, backward, balPos,
                    isCreatorMaker != null ? isCreatorMaker : true);
            if (actionStr != null) {
                item = new Fun.Tuple2<>(new Fun.Tuple2<>(balPos, backward), actionStr);
                if (!list.contains(item)) {
                    list.add(item);
                    if (useAddedActions) {
                        addActionStr = viewAssetTypeAdditionAction(assetKey, assetType, backward, balPos,
                                isCreatorMaker != null ? isCreatorMaker : true);
                        if (addActionStr != null) {
                            item = new Fun.Tuple2<>(new Fun.Tuple2<>(balPos, backward), addActionStr);
                            list.add(item);
                        }
                    }
                }
            }

            if (isCreatorMaker == null) {
                actionStr = viewAssetTypeAction(assetKey, assetType, backward, balPos,
                        false);
                if (actionStr != null) {
                    item = new Fun.Tuple2<>(new Fun.Tuple2<>(balPos, backward), actionStr);
                    if (!list.contains(item)) {
                        list.add(item);
                        if (useAddedActions) {
                            addActionStr = viewAssetTypeAdditionAction(assetKey, assetType, backward, balPos,
                                    false);
                            if (addActionStr != null) {
                                item = new Fun.Tuple2<>(new Fun.Tuple2<>(balPos, backward), addActionStr);
                                list.add(item);
                            }
                        }
                    }
                }
            }
        }

        return list;
    }

    public List<Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String>> viewAssetTypeActionsList(Boolean isCreatorMaker, boolean useAddedActions) {
        return viewAssetTypeActionsList(key, assetType, isCreatorMaker, useAddedActions);
    }

    public String viewAssetTypeActionTitle(boolean backward, int actionType, boolean isCreatorMaker) {
        return viewAssetTypeAction(backward, actionType, isCreatorMaker);
    }

    public static String viewAssetTypeCreator(int assetType, boolean backward, int actionType, boolean isCreatorMaker) {
        switch (assetType) {
            case AS_MY_DEBT:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return isCreatorMaker ? "Debtor" : "Lender";
                    case Account.BALANCE_POS_DEBT:
                    case Account.BALANCE_POS_SPEND:
                        return isCreatorMaker ? null // эмитент долга не может делать требования
                                : "Debtor";
                    default:
                        return null;
                }
            case AS_BANK_GUARANTEE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                    case Account.BALANCE_POS_DEBT:
                    case Account.BALANCE_POS_SPEND:
                        return "Guarantee";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Beneficiary";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "Beneficiary" : null;
                }
            case AS_BANK_GUARANTEE_TOTAL:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                    case Account.BALANCE_POS_DEBT:
                        return "Guarantee";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Beneficiary";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "Beneficiary" : null;
                    case Account.BALANCE_POS_SPEND:
                        return "Spender";
                }
            case AS_NON_FUNGIBLE:
                if (actionType == Account.BALANCE_POS_OWN && !backward && isCreatorMaker) {
                    return "Author";
                }
            case AS_SELF_MANAGED_ACCOUNTING:
            case AS_SELF_MANAGED_DIRECT_SEND:
            case AS_SELF_MANAGED_SHARE:
                return "Accountant";
            case AS_SELF_ACCOUNTING_LOAN:
                return "Lender";
            case AS_SELF_ACCOUNTING_MUTUAL_AID_FUND:
            case AS_SELF_ACCOUNTING_CASH_FUND:
                return "Cashier";
            case AS_OUTSIDE_IMMOVABLE:
            case AS_OUTSIDE_CURRENCY:
            case AS_OUTSIDE_SERVICE:
            case AS_OUTSIDE_SHARE:
            case AS_OUTSIDE_BILL:
            case AS_OUTSIDE_BILL_EX:
            case AS_OUTSIDE_OTHER_CLAIM:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return isCreatorMaker ? "Issuer" : "Sender";
                    case Account.BALANCE_POS_DEBT:
                    case Account.BALANCE_POS_SPEND:
                        return isCreatorMaker ? null : "Issuer";
                    default:
                        return null;
                }
            case AS_INSIDE_ASSETS:
            case AS_INSIDE_CURRENCY:
            case AS_INSIDE_UTILITY:
            case AS_INSIDE_SHARE:
            case AS_INSIDE_BONUS:
            case AS_INSIDE_ACCESS:
            case AS_INSIDE_VOTE:
            case AS_INDEX:
            case AS_INSIDE_OTHER_CLAIM:
            case AS_ACCOUNTING:
        }

        switch (actionType) {
            case Account.BALANCE_POS_OWN:
                return backward ? null : "Sender";
            case Account.BALANCE_POS_DEBT:
                return "Creditor";
            case TransactionAmount.ACTION_REPAY_DEBT:
                return "Debtor";
            case Account.BALANCE_POS_HOLD:
                return backward ? "Taker" : null;
            case Account.BALANCE_POS_SPEND:
                return backward ? null : "Spender";
        }

        return null;
    }

    public String viewAssetTypeCreator(boolean backward, int actionType, boolean isCreatorMaker) {
        return viewAssetTypeCreator(assetType, backward, actionType, isCreatorMaker);
    }

    public static String viewAssetTypeTarget(int assetType, boolean backward, int actionType, boolean isRecipientMaker) {
        switch (assetType) {
            case AS_MY_DEBT:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return isRecipientMaker ? null : "Lender"; // Тут может быть начальная эмиссия к Кредитору и переуступка - тоже кредитору по сути
                    case Account.BALANCE_POS_DEBT:
                    case Account.BALANCE_POS_SPEND:
                        return isRecipientMaker ?
                                "Debtor"
                                : null; // реципиент только эмитент долга;
                    default:
                        return null;
                }
            case AS_BANK_GUARANTEE:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return "Recipient";
                    case Account.BALANCE_POS_DEBT:
                        return "Beneficiary";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Guarantee";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "Guarantee" : null;
                    case Account.BALANCE_POS_SPEND:
                        return "Spender";
                }
            case AS_BANK_GUARANTEE_TOTAL:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return "Recipient";
                    case Account.BALANCE_POS_DEBT:
                        return "Principal";
                    case TransactionAmount.ACTION_REPAY_DEBT:
                        return "Guarantee";
                    case Account.BALANCE_POS_HOLD:
                        return backward ? "Guarantee" : null;
                    case Account.BALANCE_POS_SPEND:
                        return "Spender";
                }
            case AS_SELF_MANAGED_ACCOUNTING:
            case AS_SELF_MANAGED_DIRECT_SEND:
            case AS_SELF_MANAGED_SHARE:
                return "Ledger";
            case AS_SELF_ACCOUNTING_LOAN:
                return "Debtor";
            case AS_SELF_ACCOUNTING_MUTUAL_AID_FUND:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return "Benefactor";
                    case Account.BALANCE_POS_SPEND:
                        return "Recipient";
                }
            case AS_SELF_ACCOUNTING_CASH_FUND:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                    case Account.BALANCE_POS_DEBT:
                        return "Participant";
                    case Account.BALANCE_POS_SPEND:
                        return "Recipient";
                }
            case AS_OUTSIDE_IMMOVABLE:
            case AS_OUTSIDE_CURRENCY:
            case AS_OUTSIDE_SERVICE:
            case AS_OUTSIDE_SHARE:
            case AS_OUTSIDE_BILL:
            case AS_OUTSIDE_BILL_EX:
            case AS_OUTSIDE_OTHER_CLAIM:
                switch (actionType) {
                    case Account.BALANCE_POS_OWN:
                        return isRecipientMaker ? "Issuer" : "Recipient";
                    case Account.BALANCE_POS_DEBT:
                    case Account.BALANCE_POS_SPEND:
                        return isRecipientMaker ? "Issuer" : null;
                    default:
                        return null;
                }
            case AS_INSIDE_ASSETS:
            case AS_INSIDE_CURRENCY:
            case AS_INSIDE_UTILITY:
            case AS_INSIDE_SHARE:
            case AS_INSIDE_BONUS:
            case AS_INSIDE_ACCESS:
            case AS_INSIDE_VOTE:
            case AS_NON_FUNGIBLE:
            case AS_INDEX:
            case AS_INSIDE_OTHER_CLAIM:
            case AS_ACCOUNTING:
                /// by default below
        }

        switch (actionType) {
            case Account.BALANCE_POS_OWN:
                return "Recipient";
            case Account.BALANCE_POS_DEBT:
                return "Debtor";
            case TransactionAmount.ACTION_REPAY_DEBT:
                return "Creditor";
            case Account.BALANCE_POS_HOLD:
                return "Supplier";
            case Account.BALANCE_POS_SPEND:
                return "Spender";
        }

        return null;
    }

    public String viewAssetTypeTarget(boolean backward, int actionType, boolean isRecipientMaker) {
        return viewAssetTypeTarget(assetType, backward, actionType, isRecipientMaker);

    }

    public String viewAssetTypeActionOK(boolean backward, int actionType, boolean isCreatorMaker) {
        switch (assetType) {
            case AS_OUTSIDE_IMMOVABLE:
            case AS_OUTSIDE_CURRENCY:
            case AS_OUTSIDE_SERVICE:
            case AS_OUTSIDE_SHARE:
            case AS_OUTSIDE_BILL:
            case AS_OUTSIDE_BILL_EX:
            case AS_OUTSIDE_OTHER_CLAIM:
            case AS_INSIDE_ASSETS:
            case AS_INSIDE_CURRENCY:
            case AS_INSIDE_UTILITY:
            case AS_INSIDE_SHARE:
            case AS_INSIDE_BONUS:
            case AS_INSIDE_ACCESS:
            case AS_INSIDE_VOTE:
            case AS_BANK_GUARANTEE:
            case AS_BANK_GUARANTEE_TOTAL:
            case AS_NON_FUNGIBLE:
            case AS_INDEX:
            case AS_INSIDE_OTHER_CLAIM:
            case AS_ACCOUNTING:
        }

        return viewAssetTypeAction(backward, actionType, isCreatorMaker) + " # to";

    }

    public int getOperations(DCSet dcSet) {
        return dcSet.getOrderMap().getCountOrders(key);
    }

    //OTHER
    public static JSONObject AssetTypeJson(int assetType, JSONObject langObj) {

        JSONObject assetTypeJson = new JSONObject();
        assetTypeJson.put("id", assetType);
        assetTypeJson.put("name", Lang.T(AssetCls.viewAssetTypeCls(assetType), langObj));
        assetTypeJson.put("nameFull", Lang.T(AssetCls.viewAssetTypeFullCls(assetType), langObj));

        long startKey = ItemCls.getStartKey(
                AssetCls.ASSET_TYPE, START_KEY_OLD, MIN_START_KEY_OLD);
        List<Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String>> actions = AssetCls.viewAssetTypeActionsList(startKey,
                assetType, null, true);
        StringJoiner joiner = new StringJoiner(", ");
        JSONArray actionsArray = new JSONArray();
        for (Fun.Tuple2<Fun.Tuple2<Integer, Boolean>, String> actionItem : actions) {
            int action = actionItem.a.a;
            boolean backward = actionItem.a.b;

            joiner.add(Lang.T(actionItem.b, langObj));
            JSONObject actionJson = new JSONObject();
            actionJson.put("position", action);
            actionJson.put("backward", backward);
            actionJson.put("name", Lang.T(actionItem.b, langObj));

            String name;
            //// CREATOR
            name = viewAssetTypeCreator(assetType, backward, action, false);
            if (name != null) actionJson.put("creator", Lang.T(name, langObj));

            name = viewAssetTypeCreator(assetType, backward, action, true);
            if (name != null) actionJson.put("creator_owner", Lang.T(name, langObj));

            //////// TARGET
            name = viewAssetTypeTarget(assetType, backward, action, false);
            if (name != null) actionJson.put("target", Lang.T(name, langObj));

            name = viewAssetTypeTarget(assetType, backward, action, true);
            if (name != null) actionJson.put("target_owner", Lang.T(name, langObj));

            actionsArray.add(actionJson);
        }

        assetTypeJson.put("actions", actionsArray);

        String description = Lang.T(AssetCls.viewAssetTypeDescriptionCls(assetType), langObj) + ".<br>";
        if (AssetCls.isReverseSend(assetType)) {
            description += Lang.T("Actions for OWN balance is reversed", langObj) + ".<br>";
        }
        description += "<b>" + Lang.T("Acceptable actions", langObj) + ":</b><br>" + joiner.toString();

        String dexDesc = AssetCls.viewAssetTypeDescriptionDEX(assetType, startKey);
        if (dexDesc != null) {
            description += "<br><b>" + Lang.T("DEX rules and royalties", langObj) + ":</b><br>" + Lang.T(dexDesc, langObj);
        }

        assetTypeJson.put("description", description);

        return assetTypeJson;
    }

    public static JSONObject assetTypesJson;

    public static JSONObject AssetTypesActionsJson() {

        if (assetTypesJson != null)
            return assetTypesJson;

        assetTypesJson = new JSONObject();
        for (String iso : Lang.getInstance().getLangListAvailable().keySet()) {
            JSONObject langObj = Lang.getInstance().getLangJson(iso);
            JSONObject langJson = new JSONObject();
            for (int type : assetTypes()) {
                langJson.put(type, AssetTypeJson(type, langObj));
            }
            assetTypesJson.put(iso, langJson);
        }
        return assetTypesJson;
    }

    public static JSONObject typeJson(int type) {

        String assetTypeName;

        assetTypeName = viewAssetTypeCls(type);
        if (assetTypeName == null)
            return null;

        JSONObject typeJson = new JSONObject();

        JSONObject langObj = Lang.getInstance().getLangJson("en");

        long startKey = getStartKey(ItemCls.ASSET_TYPE, START_KEY_OLD, MIN_START_KEY_OLD);
        typeJson.put("key", type);
        typeJson.put("char", charAssetType(startKey, type));
        typeJson.put("abbrev", viewAssetTypeAbbrev(type));
        typeJson.put("name", Lang.T(assetTypeName, langObj));
        typeJson.put("name_full", Lang.T(viewAssetTypeFullCls(type), langObj));
        typeJson.put("desc", Lang.T(viewAssetTypeDescriptionCls(type), langObj));
        String dexDesc = AssetCls.viewAssetTypeDescriptionDEX(type, startKey);
        if (dexDesc != null) {
            typeJson.put("desc_DEX", Lang.T(dexDesc, langObj));
        }

        return typeJson;
    }

    public static JSONArray typesJson() {

        JSONArray types = new JSONArray();

        for (int i = 0; i < 256; i++) {
            JSONObject json = typeJson(i);
            if (json == null)
                continue;

            types.add(json);
        }
        return types;
    }

    public String viewProperties(JSONObject langObj) {

        StringJoiner joiner = new StringJoiner(", ");

        if (isImMovable())
            joiner.add(Lang.T("ImMovable", langObj));
        if (isUnlimited(maker, false))
            joiner.add(Lang.T("Unlimited", langObj));
        if (isAccounting())
            joiner.add(Lang.T("Accounting", langObj));
        if (isUnique())
            joiner.add(Lang.T("Unique", langObj));
        if (isAnonimDenied())
            joiner.add(Lang.T("ANONIM_OWN_DENIED", langObj));
        if (isUnHoldable())
            joiner.add(Lang.T("Not holdable", langObj));
        if (isOutsideType())
            joiner.add(Lang.T("Outside Claim", langObj));
        if (isSelfManaged())
            joiner.add(Lang.T("Self Managed", langObj));
        if (isChangeDebtBySendActions())
            joiner.add(Lang.T("isChangeDebtBySendActions", langObj));
        if (isChangeDebtBySpendActions())
            joiner.add(Lang.T("isChangeDebtBySpendActions", langObj));
        if (isDirectBalances())
            joiner.add(Lang.T("isDirectBalances", langObj));
        if (isNotReDebted())
            joiner.add(Lang.T("isNotReDebted", langObj));
        if (isOutsideOtherClaim())
            joiner.add(Lang.T("isOutsideOtherClaim", langObj));
        if (isReverseSend())
            joiner.add(Lang.T("isReverseSend", langObj));

        return joiner.toString();
    }

    public int isValid() {
        if (hasDEXAwards()) {

            if (isAccounting()) {
                errorValue = "Award is denied for Accounting Asset";
                return Transaction.INVALID_AWARD;
            }

            if (dexAwards.length > 256) {
                return Transaction.INVALID_MAX_AWARD_COUNT;
            }

            // нельзя делать ссылку на иконку у Персон
            int total = 0;
            for (int i = 0; i < dexAwards.length; ++i) {
                ExLinkAddress exAddress = dexAwards[i];
                if (exAddress == null) {
                    errorValue = "Award[" + i + "] = Null";
                    return Transaction.INVALID_AWARD;
                } else if (exAddress.getValue1() <= 0) {
                    errorValue = "Award[" + i + "] percent is so small (<=0%)";
                    return Transaction.INVALID_AWARD;
                } else if (exAddress.getValue1() > 25000) {
                    errorValue = "Award[" + i + "] percent is so big (>25%)";
                    return Transaction.INVALID_AWARD;
                }

                total += exAddress.getValue1();
                if (total > 25000) {
                    errorValue = "Total Award percent is so big (>25%)";
                    return Transaction.INVALID_AWARD;
                }
            }
        }

        return super.isValid();
    }

    @Override
    @SuppressWarnings("unchecked")
    public JSONObject toJson() {

        JSONObject assetJSON = super.toJson();

        JSONObject landObj = Lang.getInstance().getLangJson("en");

        // ADD DATA
        assetJSON.put("assetTypeKey", this.assetType);
        assetJSON.put("assetTypeName", Lang.T(viewAssetType(), landObj));
        assetJSON.put("assetTypeDesc", Lang.T(viewAssetTypeDescriptionCls(assetType), landObj));

        String dexDesc = AssetCls.viewAssetTypeDescriptionDEX(assetType, START_KEY());
        if (dexDesc != null) {
            assetJSON.put("type_desc_DEX", Lang.T(dexDesc, landObj));
        }

        assetJSON.put("released", this.getReleased());
        assetJSON.put("type_key", this.assetType);
        assetJSON.put("type_char", charAssetType());
        assetJSON.put("type_abbrev", viewAssetTypeAbbrev());
        assetJSON.put("type_name", Lang.T(viewAssetType(), landObj));
        assetJSON.put("type_name_full", Lang.T(viewAssetTypeFull(), landObj));
        assetJSON.put("type_desc", Lang.T(viewAssetTypeDescriptionCls(assetType), landObj));

        assetJSON.put("scale", this.getScale());
        assetJSON.put("quantity", this.getQuantity());

        assetJSON.put("isImMovable", this.isImMovable());
        assetJSON.put("isUnlimited", this.isUnlimited(maker, false));
        assetJSON.put("isAccounting", this.isAccounting());
        assetJSON.put("isUnique", this.isUnique());
        assetJSON.put("isAnonimDenied", this.isAnonimDenied());
        assetJSON.put("isUnHoldable", this.isUnHoldable());
        assetJSON.put("isOutsideType", this.isOutsideType());
        assetJSON.put("isSelfManaged", this.isSelfManaged());
        assetJSON.put("isChangeDebtBySendActions", this.isChangeDebtBySendActions());
        assetJSON.put("isChangeDebtBySpendActions", this.isChangeDebtBySpendActions());
        assetJSON.put("isDirectBalances", this.isDirectBalances());
        assetJSON.put("isNotReDebted", this.isNotReDebted());
        assetJSON.put("isOutsideOtherClaim", this.isOutsideOtherClaim());
        assetJSON.put("isReverseSend", this.isReverseSend());

        JSONObject revPos = new JSONObject();
        for (int pos = Account.BALANCE_POS_OWN; pos <= Account.BALANCE_POS_6; pos++) {
            revPos.put("" + pos, isReverseBalancePos(pos));
        }
        assetJSON.put("reversedBalPos", revPos);

        ExLinkAddress[] listDEXAwards = getDEXAwards();
        if (listDEXAwards != null) {
            JSONArray array = new JSONArray();
            for (ExLinkAddress award : listDEXAwards) {
                array.add(award.toJson());
            }
            assetJSON.put("DEXAwards", array);
        }

        return assetJSON;
    }

    public JSONObject jsonForExplorerPage(JSONObject langObj, Object[] args) {

        JSONObject assetJSON = super.jsonForExplorerPage(langObj, args);
        assetJSON.put("assetTypeNameFull", charAssetType() + viewAssetTypeAbbrev() + ":" + Lang.T(viewAssetTypeFull(), langObj));

        assetJSON.put("quantity", this.getQuantity());

        BigDecimal released = getReleased();
        assetJSON.put("released", released);

        if (args != null) {
            // параметры для показа Объемов торгов
            AssetCls quoteAsset = (AssetCls) args[0];
            TradePair tradePair = PairsController.reCalcAndUpdate(this, quoteAsset, (PairMap) args[1], 10);

            BigDecimal price = tradePair.getLastPrice();
            if (price.signum() == 0) {
                price = tradePair.getLower_askPrice();
                if (price.signum() == 0) {
                    price = tradePair.getHighest_bidPrice();
                }
            }
            BigDecimal marketCap = released.multiply(price);
            assetJSON.put("marketCap", marketCap);
            assetJSON.put("price", price);

            assetJSON.put("changePrice", tradePair.getFirstPrice().signum() > 0 ?
                    price.subtract(tradePair.getFirstPrice())
                            .movePointRight(2).divide(tradePair.getFirstPrice(), 3, RoundingMode.DOWN)
                    : 0.0);

        }

        return assetJSON;
    }

    public JSONObject jsonForExplorerInfo(DCSet dcSet, JSONObject langObj, boolean forPrint) {

        JSONObject itemJson = super.jsonForExplorerInfo(dcSet, langObj, forPrint);
        itemJson.put("Label_Asset", Lang.T("Asset", langObj));
        itemJson.put("Label_Scale", Lang.T("Accuracy", langObj));
        itemJson.put("Label_AssetType", Lang.T("Type # вид", langObj));
        itemJson.put("Label_AssetType_Desc", Lang.T("Type Description", langObj));
        itemJson.put("Label_Quantity", Lang.T("Quantity", langObj));
        itemJson.put("Label_Released", Lang.T("Released", langObj));

        itemJson.put("Label_ImMovable", Lang.T("ImMovable", langObj));
        itemJson.put("Label_Unlimited", Lang.T("Unlimited", langObj));
        itemJson.put("Label_Accounting", Lang.T("Accounting", langObj));
        itemJson.put("Label_Unique", Lang.T("Unique", langObj));
        itemJson.put("Label_UnHoldable", Lang.T("Un holdable", langObj));
        itemJson.put("Label_OutsideType", Lang.T("Outside Type", langObj));
        itemJson.put("Label_SelfManaged", Lang.T("Self Managed", langObj));
        itemJson.put("Label_ChangeDebtBySendActions", Lang.T("isChangeDebtBySendActions", langObj));
        itemJson.put("Label_ChangeDebtBySpendActions", Lang.T("isChangeDebtBySpendActions", langObj));
        itemJson.put("Label_DirectBalances", Lang.T("isDirectBalances", langObj));
        itemJson.put("Label_isNotReDebted", Lang.T("isNotReDebted", langObj));
        itemJson.put("Label_isOutsideOtherClaim", Lang.T("isOutsideOtherClaim", langObj));
        itemJson.put("Label_isReverseSend", Lang.T("isReverseSend", langObj));
        itemJson.put("Label_Properties", Lang.T("Properties", langObj));
        itemJson.put("Label_DEX_Awards", Lang.T("DEX royalties", langObj));

        itemJson.put("assetTypeNameFull", charAssetType() + viewAssetTypeAbbrev() + ":" + Lang.T(viewAssetTypeFull(), langObj));
        itemJson.put("released", getReleased());

        if (!forPrint) {
            itemJson.put("Label_Holders", Lang.T("Holders", langObj));
            itemJson.put("Label_Available_pairs", Lang.T("Available pairs", langObj));
            itemJson.put("Label_Pair", Lang.T("Pair", langObj));
            itemJson.put("Label_Orders_Count", Lang.T("Orders Count", langObj));
            itemJson.put("Label_Open_Orders_Volume", Lang.T("Open Orders Volume", langObj));
            itemJson.put("Label_Trades_Count", Lang.T("Trades Count", langObj));
            itemJson.put("Label_Trades_Volume", Lang.T("Trades Volume", langObj));

            itemJson.put("orders", getOperations(DCSet.getInstance()));
        }

        itemJson.put("quantity", NumberAsString.formatAsString(getQuantity()));
        itemJson.put("released", NumberAsString.formatAsString(getReleased(dcSet)));

        itemJson.put("scale", getScale());

        itemJson.put("assetType", Lang.T(viewAssetType(), langObj));
        itemJson.put("assetTypeChar", charAssetType() + viewAssetTypeAbbrev());

        itemJson.put("assetTypeFull", Lang.T(viewAssetTypeFull(), langObj));
        StringJoiner joiner = new StringJoiner(", ");
        for (Fun.Tuple2<?, String> item : viewAssetTypeActionsList(null, true)) {
            joiner.add(Lang.T(item.b, langObj));
        }

        String desc = Lang.T(viewAssetTypeDescriptionCls(getAssetType()), langObj)
                + ".<br><b>" + Lang.T("Acceptable actions", langObj) + "</b>: " + joiner.toString();
        String dexDesc = AssetCls.viewAssetTypeDescriptionDEX(assetType, START_KEY());
        if (dexDesc != null) {
            desc += "<br><b>" + Lang.T("DEX rules and royalties", langObj) + ":</b><br>" + Lang.T(dexDesc, langObj);
        }

        itemJson.put("assetTypeDesc", desc);


        itemJson.put("properties", viewProperties(langObj));

        return itemJson;
    }

    public String makeHTMLHeadView() {

        String text = super.makeHTMLHeadView();
        text += Lang.T("Asset Class") + ":&nbsp;"
                + Lang.T(getItemSubType() + "") + "<br>"
                + Lang.T("Asset Type") + ":&nbsp;"
                + "<b>" + charAssetType() + viewAssetTypeAbbrev() + "</b>:" + Lang.T(viewAssetTypeFull() + "") + "<br>";

        return text;

    }

    public int getDataLength(boolean includeReference) {
        return super.getDataLength(includeReference) + ASSET_TYPE_LENGTH;
    }

    public static void processTrade(DCSet dcSet, Block block, Account receiver,
                                    boolean isInitiator, AssetCls assetHave, AssetCls assetWant,
                                    boolean asOrphan, BigDecimal tradeAmountForWant, long timestamp, Long orderID) {
        //TRANSFER FUNDS
        BigDecimal tradeAmount = tradeAmountForWant.setScale(assetWant.getScale());
        BigDecimal assetRoyaltyTotal = BigDecimal.ZERO;
        BigDecimal inviterRoyalty;
        BigDecimal forgerFee;
        int scale = assetWant.getScale();
        Long assetWantKey = assetWant.getKey();

        PublicKeyAccount haveAssetMaker = assetHave.getMaker();
        PublicKeyAccount inviter = null;


        //////// ACCOUNTING assets is Denied for Awards //////

        ExLinkAddress[] dexAwards = assetHave.getDEXAwards();
        if (dexAwards != null) {
            for (ExLinkAddress dexAward : dexAwards) {
                if (receiver.equals(dexAward.getAccount())) {
                    // to mySelf not pay
                    continue;
                }

                BigDecimal assetRoyalty = tradeAmount.multiply(new BigDecimal(dexAward.getValue1()))
                        .movePointLeft(5) // in ExLinkAddress is x1000 and x100 as percent
                        .setScale(scale, RoundingMode.DOWN);
                if (assetRoyalty.signum() > 0) {
                    assetRoyaltyTotal = assetRoyaltyTotal.add(assetRoyalty);
                    dexAward.getAccount().changeBalance(dcSet, asOrphan, false, assetWantKey,
                            assetRoyalty, false, false, false);
                    if (!asOrphan && block != null)
                        block.addCalculated(dexAward.getAccount(), assetWantKey, assetRoyalty,
                                "Royalty by Order @" + Transaction.viewDBRef(orderID), orderID);
                }
            }
        }

        if (assetHave.getAssetType() == AS_NON_FUNGIBLE) {

            // всегда 1% форжеру
            forgerFee = tradeAmount.movePointLeft(2).setScale(scale, RoundingMode.DOWN);

            Fun.Tuple4<Long, Integer, Integer, Integer> issuerPersonDuration = haveAssetMaker.getPersonDuration(dcSet);
            if (issuerPersonDuration != null) {
                inviter = PersonCls.getIssuer(dcSet, issuerPersonDuration.a);
            }

            if (inviter == null) {
                inviterRoyalty = BigDecimal.ZERO;
            } else {
                inviterRoyalty = forgerFee;
            }

        } else if (assetWant.getKey() < assetWant.getStartKey()
                && !isInitiator) {
            // это системные активы - берем комиссию за них
            forgerFee = tradeAmount.movePointLeft(3).setScale(scale, RoundingMode.DOWN);

            // за рефералку тут тоже
            Fun.Tuple4<Long, Integer, Integer, Integer> issuerPersonDuration = receiver.getPersonDuration(dcSet);
            if (issuerPersonDuration != null) {
                inviter = PersonCls.getIssuer(dcSet, issuerPersonDuration.a);
                if (inviter == null)
                    inviterRoyalty = BigDecimal.ZERO;
                else
                    inviterRoyalty = forgerFee;
            } else {
                inviter = null;
                inviterRoyalty = BigDecimal.ZERO;
            }

        } else {

            if (assetRoyaltyTotal.signum() > 0) {

                forgerFee = assetRoyaltyTotal.movePointLeft(2).setScale(scale, RoundingMode.DOWN);

                Fun.Tuple4<Long, Integer, Integer, Integer> issuerPersonDuration = haveAssetMaker.getPersonDuration(dcSet);
                if (issuerPersonDuration != null) {
                    inviter = PersonCls.getIssuer(dcSet, issuerPersonDuration.a);
                }

                if (inviter == null) {
                    inviterRoyalty = BigDecimal.ZERO;
                } else {
                    inviterRoyalty = forgerFee;
                }

            } else {
                inviterRoyalty = BigDecimal.ZERO;
                inviter = null;
                forgerFee = BigDecimal.ZERO;
            }
        }

        if (assetRoyaltyTotal.signum() > 0) {
            tradeAmount = tradeAmount.subtract(assetRoyaltyTotal);
        }

        if (inviterRoyalty.signum() > 0) {
            tradeAmount = tradeAmount.subtract(inviterRoyalty);

            long inviterRoyaltyLong = inviterRoyalty.setScale(assetWant.getScale()).unscaledValue().longValue();
            Transaction.process_gifts(dcSet, BlockChain.FEE_INVITED_DEEP, inviterRoyaltyLong, inviter, asOrphan,
                    assetWant, block,
                    "Royalty referral bonus " + "@" + Transaction.viewDBRef(orderID),
                    orderID, timestamp);
        }

        if (forgerFee.signum() > 0) {
            tradeAmount = tradeAmount.subtract(forgerFee);

            if (block != null) {
                block.addAssetFee(assetWant, forgerFee, null);
            }
        }

        receiver.changeBalance(dcSet, asOrphan, false, assetWantKey,
                tradeAmount, false, false, false);
        if (!asOrphan && block != null)
            block.addCalculated(receiver, assetWantKey, tradeAmount,
                    "Trade Order @" + Transaction.viewDBRef(orderID), orderID);

    }

}
