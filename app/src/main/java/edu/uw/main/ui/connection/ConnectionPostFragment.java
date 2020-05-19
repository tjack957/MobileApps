package edu.uw.main.ui.connection;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import edu.uw.main.MainActivity;
import edu.uw.main.R;
import edu.uw.main.databinding.FragmentConnectionPostBinding;


public class ConnectionPostFragment extends Fragment {


    public ConnectionPostFragment() {
        // Required empty public constructor
    }


/**
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
 */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ConnectionPostFragmentArgs args = ConnectionPostFragmentArgs.fromBundle(getArguments());
        FragmentConnectionPostBinding binding = FragmentConnectionPostBinding.bind(getView());

        binding.textName.setText(args.getFriend().getConnection());
        ((MainActivity) getActivity())
                .setActionBarTitle(args.getFriend().getConnection() + " Profile");

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_connection_post, container, false);
        if (view instanceof RecyclerView) {
// //Try out a grid layout to achieve rows AND columns. Adjust the widths of the
// //cards on display
// ((RecyclerView) view).setLayoutManager(new GridLayoutManager(getContext(), 2));
// //Try out horizontal scrolling. Adjust the widths of the card so that it is
// //obvious that there are more cards in either direction. i.e. don't have the cards
// //span the entire witch of the screen. Also, when considering horizontal scroll
// //on recycler view, ensure that there is other content to fill the screen.
// ((LinearLayoutManager)((RecyclerView) view).getLayoutManager())
// .setOrientation(LinearLayoutManager.HORIZONTAL);
            ((RecyclerView) view).setAdapter(
                    new ConnectionRecyclerViewAdapter(ConnectionGenerator.getConnectionsList()));
        }
        return view;
    }
    @Override
    public void onResume() {
        if (MainActivity.changePassword) {
            MainActivity.changePassword = false;
            Navigation.findNavController(getView()).navigate(ConnectionPostFragmentDirections.actionConnectionPostFragmentToChange2());
        }
        super.onResume();
    }
}
