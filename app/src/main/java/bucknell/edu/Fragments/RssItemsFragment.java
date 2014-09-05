package bucknell.edu.Fragments;

import android.app.Activity;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.concurrent.CopyOnWriteArrayList;

import bucknell.edu.Data.RssItem;
import bucknell.edu.bucknellreader.R;


/**
 * A fragment representing a list of Items.
 * <p />
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p />
 * Activities containing this fragment MUST implement the {@link bucknell.edu.Fragments.RssItemsFragment.OnRssItemsFragmentInteractionListener}
 * interface.
 */
public class RssItemsFragment extends Fragment implements AbsListView.OnItemClickListener {
    public CopyOnWriteArrayList<RssItem> rssItems;


    private OnRssItemsFragmentInteractionListener mListener;

    /**
     * The fragment's ListView/GridView.
     */
    private AbsListView mListView;

    /**
     * The Adapter which will be used to populate the ListView/GridView with
     * Views.
     */
    private ArrayAdapter<RssItem> mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public RssItemsFragment() {
    }

    public RssItemsFragment(CopyOnWriteArrayList<RssItem> rssItems){
        this.rssItems = rssItems;
    }

    /*
    public static RssItemsFragment newInstance(CopyOnWriteArrayList<RssItem> rssItems){
        RssItemsFragment f = new RssItemsFragment();

        Bundle args = new Bundle();
        args.putParcelableArrayList("RssItems", rssItems);
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // TODO: Change Adapter to display your content
        mAdapter = new ArrayAdapter<RssItem>(getActivity(),
                android.R.layout.simple_list_item_1, android.R.id.text1, rssItems);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rssitem, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);
        ((AdapterView<ListAdapter>) mListView).setAdapter(mAdapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setRetainInstance(true);
        try {
            mListener = (OnRssItemsFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnRssItemsFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
            RssItem rssItem = rssItems.get(position);
            mListener.onRssItemsFragmentInteraction(rssItem.getTitle(), rssItem.getContent());
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(CharSequence emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyText instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    /**
     * Clears the content of the list and updates the view. This will make the whole list view a blank screen.
     */
    public void clearRssItems() {
        mAdapter.clear();
        mAdapter.notifyDataSetChanged();
    }

    /**
     * Resets Rss items to the list of items passed into the method. This method should be called when the activity
     * wants to update the Rss item lists.
     */
    public void ResetRssItems(CopyOnWriteArrayList<RssItem> rssItems){
        mAdapter.clear();
        mAdapter.addAll(rssItems);
        mAdapter.notifyDataSetChanged();
    }

    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnRssItemsFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onRssItemsFragmentInteraction(String title, String content);
    }

}
