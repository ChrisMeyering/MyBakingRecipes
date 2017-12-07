package com.baking.chris.mybakingrecipes.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.baking.chris.mybakingrecipes.R;
import com.baking.chris.mybakingrecipes.data.Step;

import java.util.List;

/**
 * Created by chris on 11/19/17.
 */

public class StepListAdapter extends RecyclerView.Adapter<StepListAdapter.StepViewHolder> {

    public static final String TAG = IngredientListAdapter.class.getSimpleName();
    private Context context;
    private OnStepSelectedListener callback;

    public interface OnStepSelectedListener {
        void onStepSelected(int stepNumber);
    }
    private List<Step> steps;

    public StepListAdapter(Context context, List<Step> steps){
        this.context = context;
        this.steps = steps;
        try {
            callback = (OnStepSelectedListener) context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString() + " must implement OnStepSelectedListener");
        }
    }

    @Override
    public StepViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.step_list_item, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StepViewHolder holder, int position) {
        holder.bind(steps.get(position));
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (steps == null)
            return 0;
        return steps.size();
    }


    class StepViewHolder extends RecyclerView.ViewHolder {
        private TextView tvStepShortDescription;
        public StepViewHolder(View itemView) {
            super(itemView);
            tvStepShortDescription = itemView.findViewById(R.id.tv_step_short_description);
            tvStepShortDescription.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callback.onStepSelected(getAdapterPosition() + 1);
                }
            });
        }

        public void bind(Step step) {
                    tvStepShortDescription.setText("Step " + String.valueOf(step.getId()) + " - " + step.getShortDescription());
        }
    }
}
