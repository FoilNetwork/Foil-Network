package org.erachain.gui.items.persons;

import org.erachain.controller.Controller;
import org.erachain.core.account.PublicKeyAccount;
import org.erachain.core.item.persons.PersonCls;
import org.erachain.core.transaction.RSertifyPubKeys;
import org.erachain.core.transaction.Transaction;
import org.erachain.database.SortableList;
import org.erachain.datachain.DCSet;
import org.erachain.datachain.TransactionFinalMap;
import org.erachain.gui.models.TimerTableModelCls;
import org.erachain.lang.Lang;
import org.erachain.utils.DateTimeFormat;
import org.erachain.utils.ObserverMessage;
import org.erachain.utils.Pair;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.Null;
import java.util.*;

public class PersonVouchFromTableModel extends TimerTableModelCls<RSertifyPubKeys> implements Observer {

    public static final int COLUMN_TIMESTAMP = 0;
    public static final int COLUMN_CREATOR = 1;
    public static final int COLUMN_HEIGHT = 2;

    PersonCls person;

    public PersonVouchFromTableModel(PersonCls person) {

        super(DCSet.getInstance().getTransactionFinalMap(),
                new String[]{"Timestamp", "Persons", "Height"}, new Boolean[]{true, true}, false);

        this.person = person;

        logger = LoggerFactory.getLogger(PersonVouchFromTableModel.class);

        addObservers();
    }

    public PublicKeyAccount getPublicKey(int row) {
        RSertifyPubKeys transaction = this.list.get(row);
        return transaction.getSertifiedPublicKeys().get(0);

    }

    public String getHeightSeq(int row) {

        if (this.list == null || this.list.size() <= row) {
            return null;
        }

        Transaction transaction = this.list.get(row);
        if (transaction == null)
            return null;

        return transaction.viewHeightSeq();

    }


    @Override
    public Object getValueAt(int row, int column) {
        if (this.list == null || this.list.isEmpty()) return null;

        RSertifyPubKeys transaction = this.list.get(row);
        if (transaction == null)
            return null;

        switch (column) {
            case COLUMN_TIMESTAMP:

                return DateTimeFormat.timestamptoString(transaction.getTimestamp()); //.viewTimestamp(); // + " " +

            case COLUMN_CREATOR:

                return transaction.getSertifiedPublicKeys().get(0).getPersonAsString();

            case COLUMN_HEIGHT:

                return transaction.getBlockHeight();


        }

        return null;

    }

    public synchronized void syncUpdate(Observable o, Object arg) {

        ObserverMessage message = (ObserverMessage) arg;

        if (message.getType() == ObserverMessage.LIST_TRANSACTION_TYPE) {

            list = new ArrayList<>();
            TransactionFinalMap mapTransactions = DCSet.getInstance().getTransactionFinalMap();

            //CHECK IF NEW LIST
            TreeMap<String, Stack<Fun.Tuple3<Integer, Integer, Integer>>> pubKeysitems = DCSet.getInstance().getPersonAddressMap().get(person.getKey());
            for (Stack<Fun.Tuple3<Integer, Integer, Integer>> stack: pubKeysitems.values()) {
                Fun.Tuple3<Integer, Integer, Integer> itemTransaction = stack.peek();
                RSertifyPubKeys transaction = (RSertifyPubKeys)mapTransactions.get(itemTransaction.b, itemTransaction.c);
                if (transaction != null) {
                    list.add(transaction);
                }
            }

            this.fireTableDataChanged();

        } else if (message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE) {
            Transaction transaction = (Transaction) message.getValue();
            if (transaction.getType() == Transaction.CERTIFY_PUB_KEYS_TRANSACTION) {
                RSertifyPubKeys rSertify = (RSertifyPubKeys) transaction;
                Tuple2<Integer, PersonCls> personRes = rSertify.getCreator().getPerson();
                if (personRes != null && personRes.b.getKey() == person.getKey()) {
                    if (!this.list.contains(rSertify)) {
                        this.list.add(rSertify);
                        needUpdate = true;
                        return;
                    }
                }
            }
        } else if (message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE) {
            Transaction transaction = (Transaction) message.getValue();
            if (transaction.getType() == Transaction.CERTIFY_PUB_KEYS_TRANSACTION) {
                RSertifyPubKeys rSertify = (RSertifyPubKeys) transaction;
                Tuple2<Integer, PersonCls> personRes = rSertify.getCreator().getPerson();
                if (personRes != null && personRes.b.getKey() == person.getKey()) {
                    if (this.list.contains(rSertify)) {
                        this.list.remove(rSertify);
                        needUpdate = true;
                        return;
                    }
                }
            }
        } else if (message.getType() == ObserverMessage.GUI_REPAINT && needUpdate) {
            needUpdate = false;
            this.fireTableDataChanged();
        }

    }

    @Override
    public void getIntervalThis(long start, long end) {
    }

    public void addObservers() {
        super.addObservers();

        map.addObserver(this);
    }

    public void removeObservers() {
        super.deleteObservers();

        map.deleteObserver(this);
    }

}