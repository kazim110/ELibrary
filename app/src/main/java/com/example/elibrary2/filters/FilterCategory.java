package com.example.elibrary2.filters;

import android.widget.Filter;

import com.example.elibrary2.adapters.AdapterCategory;
import com.example.elibrary2.models.ModelCategory;

import java.util.ArrayList;

public class FilterCategory extends Filter {

    ArrayList<ModelCategory> filterList;
    AdapterCategory adapterCategory;

    public FilterCategory(ArrayList<ModelCategory> filterList,AdapterCategory adapterCategory){
        this.filterList=filterList;
        this.adapterCategory=adapterCategory;
    }
    @Override
    protected FilterResults performFiltering(CharSequence contraint) {
        FilterResults results=new FilterResults();
        if(contraint!=null&& contraint.length()>0){
            contraint=contraint.toString().toUpperCase();
            ArrayList<ModelCategory> filteredModels=new ArrayList<>();
            for (int i=0;i<filterList.size();i++){
                if(filterList.get(i).getCategory().toUpperCase().contains(contraint)){
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count=filteredModels.size();
            results.values=filteredModels;
        }
        else {
            results.count=filterList.size();
            results.values=filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        adapterCategory.categoryArrayList=(ArrayList<ModelCategory>)results.values;
        adapterCategory.notifyDataSetChanged();
    }
}
