package com.fahadaltimimi.controller;

import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Helper class for providing helper methods for list views
 */

public class ListViewHelper {

    private static int DEFAULT_LIST_ITEMS_BUFFER_MULTIPLIER = 2;
    private static int DEFAULT_LIST_ITEMS_MINIMUM_ADDITIONAL_LOAD = 20;

    private ListViewHelper() {
        //
    }

    /**
     * Returns true if given list view requires a refresh to load additional items so that they are visible when scrolled to
     * @param listView List View to check if additional items should be loaded
     * @param listItemsBufferMultiplier Multiplier to apply to last items position such that if the
     *                                  list views current count is less than that a refresh is determined to be required
     * @return True if the given list view should be refreshed to show additional items
     */
    public static boolean shouldRefreshAdditionalListViewItems(ListView listView, int listItemsBufferMultiplier) {
        int lastItemPosition = listView.getLastVisiblePosition();
        return (lastItemPosition <= 0 || listView.getChildCount() == 0 || listView.getParent() == null ||
                listView.getChildAt(listView.getChildCount() - 1).getBottom() < ((ViewGroup) listView.getParent()).getHeight() ||
                listView.getCount() < lastItemPosition * listItemsBufferMultiplier);
    }

    /**
     * Calls @shouldRefreshAdditionalListViewItems with a default lisItemsBufferMultiplier
     * @param listView List View to check if additional items should be loaded
     * @return
     */
    public static boolean shouldRefreshAdditionalListViewItems(ListView listView) {
        return shouldRefreshAdditionalListViewItems(listView, DEFAULT_LIST_ITEMS_BUFFER_MULTIPLIER);
    }

    /**
     * Return how many items should be loaded in list based on list view count
     * @param listView List View to check
     * @return Count of items to load
     */
    public static int additionalItemCountToLoad(ListView listView) {
        if (listView.getCount() < DEFAULT_LIST_ITEMS_MINIMUM_ADDITIONAL_LOAD) {
            return DEFAULT_LIST_ITEMS_MINIMUM_ADDITIONAL_LOAD;
        } else {
            return listView.getCount();
        }
    }
}
