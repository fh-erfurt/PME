package PME.myfitnessbuddy.view.ui.exercise;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.selection.SelectionPredicates;
import androidx.recyclerview.selection.SelectionTracker;
import androidx.recyclerview.selection.StableIdKeyProvider;
import androidx.recyclerview.selection.StorageStrategy;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.myfitnessbuddy.R;

import java.util.ArrayList;
import java.util.List;

import PME.myfitnessbuddy.model.exercise.Exercise;
import PME.myfitnessbuddy.model.exercise.ExerciseWithMuscleGroup;
import PME.myfitnessbuddy.view.ui.core.BaseFragment;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExerciseFragment} factory method to
 * create an instance of this fragment.
 */
public class ExerciseFragment extends BaseFragment {

    private ExerciseViewModel exerciseViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        this.exerciseViewModel = this.getViewModel(ExerciseViewModel.class);

        View root = inflater.inflate(R.layout.fragment_exercise, container, false);

        RecyclerView exerciseListView = root.findViewById(R.id.exercises);
        final ExerciseAdapter adapter = new ExerciseAdapter(this.requireActivity(),
                exerciseId -> {
                    Bundle args = new Bundle();
                    args.putLong("exerciseId", exerciseId);
                    NavController nc = NavHostFragment.findNavController( this );
                    nc.navigate( R.id.action_fragment_exercise_to_fragment_exercisedetail, args );
                });

        exerciseListView.setAdapter( adapter );

        SelectionTracker<Long> tracker = new SelectionTracker.Builder<>(
                "mySelectionId",
                exerciseListView,
                new StableIdKeyProvider(exerciseListView),
                new ExerciseItemDetailsLookup(exerciseListView),
                StorageStrategy.createLongStorage())
                .withSelectionPredicate(
                        SelectionPredicates.createSelectAnything()
                )
                .build();

        tracker.addObserver( new ExerciseSelectionObserver(tracker, adapter) );
        adapter.setSelectionTracker( tracker );

        exerciseListView.setLayoutManager( new LinearLayoutManager(this.requireActivity()));

        exerciseViewModel.getExercises().observe(this.requireActivity(), exercises -> {

            adapter.submitList( exercises );
        });

        return root;
    }

    private class ExerciseSelectionObserver extends SelectionTracker.SelectionObserver<Long> {

        private final SelectionTracker<Long> tracker;
        private final ExerciseAdapter adapter;
        ActionMode mode;

        public ExerciseSelectionObserver(SelectionTracker<Long> tracker, ExerciseAdapter adapter) {
            this.tracker = tracker;
            this.adapter = adapter;
        }

        @Override
        public void onSelectionChanged() {
            super.onSelectionChanged();

            if (mode != null) return;

            mode = requireActivity().startActionMode(new ActionMode.Callback() {
                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.list_action_mode_menu, menu);
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {

                    if (item.getItemId() == R.id.list_action_delete) {
                        exerciseViewModel.deleteExercises( getSelectedExercises() );
                        tracker.clearSelection();
                        return true;
                    }

                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                    return false;
                }
            });
        }

        @Override
        protected void onSelectionCleared() {
            if (mode != null) mode.finish();
            mode = null;
        }

        private List<ExerciseWithMuscleGroup> getSelectedExercises() {
            List<ExerciseWithMuscleGroup> selectedContacts = new ArrayList<>(tracker.getSelection().size());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                tracker.getSelection().iterator().forEachRemaining(aLong -> {
                    selectedContacts.add(adapter.getExercise(aLong.intValue()));
                });
            }

            return selectedContacts;
        }
    }
}
