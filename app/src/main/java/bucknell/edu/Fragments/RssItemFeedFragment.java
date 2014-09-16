package bucknell.edu.Fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import bucknell.edu.bucknellreader.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link bucknell.edu.Fragments.RssItemFeedFragment.OnRssItemFeedFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link RssItemFeedFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class RssItemFeedFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_RSS_ITEM_TITLE = "ARG_RSS_ITEM_TITLE";
    private static final String ARG_RSS_ITEM_CONTENT = "ARG_RSS_ITEM_CONTENT";

    // TODO: Rename and change types of parameters
    private String title;
    private String content;

    private OnRssItemFeedFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param title The title of the Rss item.
     * @param content The content of the Rss item.
     * @return A new instance of fragment RssItemFeedFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static RssItemFeedFragment newInstance(String title, String content) {
        RssItemFeedFragment fragment = new RssItemFeedFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RSS_ITEM_TITLE, title);
        args.putString(ARG_RSS_ITEM_CONTENT, content);
        fragment.setArguments(args);
        return fragment;
    }
    public RssItemFeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            title = getArguments().getString(ARG_RSS_ITEM_TITLE);
            content = getArguments().getString(ARG_RSS_ITEM_CONTENT);
        }


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rss_item_feed, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedState){
        super.onActivityCreated(savedState);
        TextView titleView = (TextView) getActivity().findViewById(R.id.rss_item_title);
        TextView contentView = (TextView) getActivity().findViewById(R.id.rss_item_content);
        titleView.setText(title);
        contentView.setText(content);
        contentView.setMovementMethod(new ScrollingMovementMethod());
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onRssItemFeedFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnRssItemFeedFragmentInteractionListener) activity;
            mListener.onRssItemFeedFragmentStart();
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnRssItemFeedFragmentInteractionListener");
        }
        setRetainInstance(true);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onStop() {
        super.onStop();
        mListener.onRssItemFeedFragmentStop();
        Log.i("RssItemFeedFragment OnStop", "RssItemFeedFragment OnStop");
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
    public interface OnRssItemFeedFragmentInteractionListener {
        public void onRssItemFeedFragmentInteraction(Uri uri);
        public void onRssItemFeedFragmentStart();
        public void onRssItemFeedFragmentStop();
    }

}
