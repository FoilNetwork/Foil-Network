package org.erachain.core.item.polls;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import org.erachain.core.account.Account;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.ItemCls;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.IssueItemMap;
import org.erachain.datachain.ItemMap;
import org.erachain.datachain.VoteOnItemPollMap;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.mapdb.Fun.Tuple3;
import org.erachain.utils.Pair;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NavigableSet;

public abstract class PollCls extends ItemCls {

    public static final int POLL = 1;
    public static final int INITIAL_FAVORITES = 0;
    protected static final int OPTIONS_SIZE_LENGTH = 4;
    protected static final int BASE_LENGTH = OPTIONS_SIZE_LENGTH;
    private List<String> options;

    public PollCls(byte[] typeBytes, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, List<String> options) {
        super(typeBytes, owner, name, icon, image, description);
        this.options = options;

    }

    public PollCls(int type, PublicKeyAccount owner, String name, byte[] icon, byte[] image, String description, List<String> options) {
        this(new byte[TYPE_LENGTH], owner, name, icon, image, description, options);
        this.typeBytes[0] = (byte) type;
    }

    //GETTERS/SETTERS
    public int getItemType() {
        return ItemCls.POLL_TYPE;
    }

    public String getItemTypeName() {
        return "poll";
    }

    public List<String> getOptions() {
        return this.options;
    }

    @Override
    public ItemMap getDBMap(DCSet dc) {
        return dc.getItemPollMap();
    }

    public IssueItemMap getDBIssueMap(DCSet dc) {
        return dc.getIssuePollMap();
    }

    public boolean hasVotes(DCSet dc) {
        return dc.getVoteOnItemPollMap().hasVotes(this.key);
    }

    public BigDecimal getTotalVotes(DCSet dcSet) {
        return getTotalVotes(dcSet, 2);
    }

    public BigDecimal getTotalVotes(DCSet dcSet, long assetKey) {
        BigDecimal votesSum = BigDecimal.ZERO;
        VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
        NavigableSet<Tuple3> optionVoteKeys;
        Account voter;

        optionVoteKeys = map.getVotes(this.key);
        for (Tuple3<Long, Integer, BigInteger> key : optionVoteKeys) {
            voter = Account.makeAccountFromShort(key.c);
            votesSum = votesSum.add(voter.getBalanceUSE(assetKey));
        }

        return votesSum;
    }

    public BigDecimal getTotalVotes(DCSet dcSet, long assetKey, int option) {
        BigDecimal votesSum = BigDecimal.ZERO;
        VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
        NavigableSet<Tuple3> optionVoteKeys;
        Account voter;

        optionVoteKeys = map.getVotes(this.key);
        for (Tuple3<Long, Integer, BigInteger> key : optionVoteKeys) {
            if (option != key.b)
                continue;

            voter = Account.makeAccountFromShort(key.c);
            votesSum = votesSum.add(voter.getBalanceUSE(assetKey));
        }

        return votesSum;
    }


    public List<Pair<Account, Integer>> getVotes(DCSet dcSet) {
        List<Pair<Account, Integer>> votes = new ArrayList<Pair<Account, Integer>>();

        VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
        NavigableSet<Tuple3> optionVoteKeys;
        Pair<Account, Integer> vote;
        Account voter;

        optionVoteKeys = map.getVotes(this.key);
        for (Tuple3<Long, Integer, BigInteger> key : optionVoteKeys) {
            voter = Account.makeAccountFromShort(key.c);
            vote = new Pair<Account, Integer>(voter, key.b);
            votes.add(vote);
        }

        return votes;
    }

    /**
     * список всех персон голосующих
     * @param dcSet
     * @return
     */
    public List<Pair<Account, Integer>> getPersonVotes(DCSet dcSet) {
        List<Pair<Account, Integer>> votes = new ArrayList<Pair<Account, Integer>>();

        VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
        NavigableSet<Tuple3> optionVoteKeys;
        Pair<Account, Integer> vote;
        Account voter;

        optionVoteKeys = map.getVotes(this.key);
        for (Tuple3<Long, Integer, BigInteger> key : optionVoteKeys) {
            voter = Account.makeAccountFromShort(key.c);
            if (voter.isPerson(dcSet, 0)) {
                vote = new Pair<Account, Integer>(voter, key.b);
                votes.add(vote);
            }
        }

        return votes;
    }

    public List<Long> getPersonCountVotes(DCSet dcSet) {


        List<Long> votes = new ArrayList<>(this.options.size());
        for (int i = 0; i < options.size(); i++) {
            votes.add(0L);
        }

        VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
        NavigableSet<Tuple3> optionVoteKeys;
        Pair<Account, Integer> vote;
        Account voter;

        optionVoteKeys = map.getVotes(this.key);
        for (Tuple3<Long, Integer, BigInteger> key : optionVoteKeys) {
            voter = Account.makeAccountFromShort(key.c);
            Integer optionNo = key.b;
            if (voter.isPerson(dcSet, 0)) {
                Long count = votes.get(optionNo);
                votes.add(optionNo, count + 1L);
            }
        }

        return votes;
    }

    public long getPersonCountTotalVotes(DCSet dcSet) {
        long votes = 0L;

        VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
        NavigableSet<Tuple3> optionVoteKeys;
        Pair<Account, Integer> vote;
        Account voter;

        optionVoteKeys = map.getVotes(this.key);
        for (Tuple3<Long, Integer, BigInteger> key : optionVoteKeys) {
            voter = Account.makeAccountFromShort(key.c);
            Integer optionNo = key.b;
            if (voter.isPerson(dcSet, 0)) {
                ++votes;
            }
        }

        return votes;
    }

    public List<Pair<Account, Integer>> getVotes(DCSet dcSet, List<Account> accounts) {
        List<Pair<Account, Integer>> votes = new ArrayList<Pair<Account, Integer>>();

        VoteOnItemPollMap map = dcSet.getVoteOnItemPollMap();
        NavigableSet<Tuple3> optionVoteKeys; // <Long, Integer, BigInteger>
        Pair<Account, Integer> vote;
        Account voter;

        optionVoteKeys = map.getVotes(this.key);
        for (Tuple3<Long, Integer, BigInteger> key : optionVoteKeys) {
            for (Account account : accounts) {
                if (account.equals(key.c)) {
                    vote = new Pair<Account, Integer>(account, key.b);
                    votes.add(vote);
                }
            }
        }

        return votes;
    }

    public int getOption(String option) {

        int i = 0;
        for (String pollOption : this.options) {
            if (pollOption.equals(option)) {
                return i;
            }

            i++;
        }

        return -1;
    }

    public String viewOption(int option) {
        return option + ": " + this.options.get(option);
    }

    // PARSE
    public byte[] toBytes(boolean includeReference, boolean onlyBody) {

        byte[] data = super.toBytes(includeReference, onlyBody);

        //WRITE OPTIONS SIZE
        byte[] optionsLengthBytes = Ints.toByteArray(this.options.size());
        data = Bytes.concat(data, optionsLengthBytes);

        //WRITE OPTIONS
        for (String option : this.options) {

            //WRITE NAME SIZE
            byte[] optionBytes = option.getBytes(StandardCharsets.UTF_8);
            data = Bytes.concat(data, new byte[]{(byte) optionBytes.length});

            //WRITE NAME
            data = Bytes.concat(data, optionBytes);
        }

        return data;
    }


    public int getDataLength(boolean includeReference) {
        int length = super.getDataLength(includeReference) + BASE_LENGTH;

        for (String option : this.options) {
            length += 1 + option.getBytes(StandardCharsets.UTF_8).length;
        }

        return length;

    }


    //OTHER

    @SuppressWarnings("unchecked")
    public JSONObject toJson() {

        JSONObject pollJSON = super.toJson();

        JSONArray jsonOptions = new JSONArray();
        for (String option : this.options) {
            jsonOptions.add(option);
        }

        pollJSON.put("options", jsonOptions);
        pollJSON.put("totalVotes", getTotalVotes(DCSet.getInstance()).toPlainString());

        return pollJSON;
    }

    public JSONObject jsonForExplorerPage(JSONObject langObj) {

        JSONObject json = super.jsonForExplorerPage(langObj);

        json.put("optionsCount", options.size());
        json.put("totalVotes", getTotalVotes(DCSet.getInstance()).toPlainString());

        return json;
    }

}
