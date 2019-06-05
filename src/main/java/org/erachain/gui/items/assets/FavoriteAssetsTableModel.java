package org.erachain.gui.items.assets;

import org.erachain.controller.Controller;
import org.erachain.core.item.assets.AssetCls;
import org.erachain.datachain.DCSet;
import org.erachain.gui.items.FavoriteItemModelTable;
import org.erachain.lang.Lang;
import org.erachain.utils.ObserverMessage;

public class FavoriteAssetsTableModel extends FavoriteItemModelTable {
    public static final int COLUMN_KEY = 0;
    public static final int COLUMN_NAME = 1;
    public static final int COLUMN_ADDRESS = 2;
    private static final int COLUMN_ASSET_TYPE = 3;
    public static final int COLUMN_AMOUNT = 4;
    public static final int COLUMN_FAVORITE = 5;
    private static final int COLUMN_I_OWNER = 6;

    public FavoriteAssetsTableModel() {
        super(DCSet.getInstance().getItemAssetMap(),
                Controller.getInstance().wallet.database.getAssetFavoritesSet(),
                new String[]{"Key", "Name", "Owner", "Type", "Quantity", "Favorite", "I Owner"},
                new Boolean[]{false, true, true, false, false, false, false},
                ObserverMessage.RESET_ASSET_FAVORITES_TYPE,
                ObserverMessage.ADD_ASSET_FAVORITES_TYPE,
                ObserverMessage.DELETE_ASSET_FAVORITES_TYPE,
                ObserverMessage.LIST_ASSET_FAVORITES_TYPE,
                COLUMN_FAVORITE);

        COLUMN_FOR_ICON = COLUMN_NAME;
    }

    @Override
    public Object getValueAt(int row, int column) {
        if (list == null || row >= list.size()) {
            return null;
        }
        AssetCls asset = (AssetCls) list.get(row);
        if (asset == null) {
            return null;
        }
        switch (column) {
            case COLUMN_KEY:
                return asset.getKey();
            case COLUMN_NAME:
                return asset; // use renderer with ICON .viewName();
            case COLUMN_ADDRESS:
                return asset.getOwner().getPersonAsString();
            case COLUMN_ASSET_TYPE:
                return Lang.getInstance().translate(asset.viewAssetType());
            case COLUMN_AMOUNT:
                return asset.getTotalQuantity(DCSet.getInstance());
            case COLUMN_FAVORITE:
                return asset.isFavorite();
            case COLUMN_I_OWNER:
                return Controller.getInstance().isAddressIsMine(asset.getOwner().getAddress());
            //case COLUMN_ITEM_VALUE:
            //    return asset;
        }
        return null;
    }

}
