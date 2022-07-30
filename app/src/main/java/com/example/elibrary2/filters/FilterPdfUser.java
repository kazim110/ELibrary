package com.example.elibrary2.filters;

import android.widget.Filter;

import com.example.elibrary2.adapters.AdapterPdfUser;
import com.example.elibrary2.models.ModelPdf;

import java.util.ArrayList;
import java.util.Locale;

public class FilterPdfUser extends Filter {
    ArrayList<ModelPdf> filterList;
    AdapterPdfUser adapterPdfUser;

    public FilterPdfUser(ArrayList<ModelPdf> filterList,AdapterPdfUser adapterPdfUser){
        this.filterList=filterList;
        this.adapterPdfUser=adapterPdfUser;

    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {

        FilterResults results=new FilterResults();
        if(constraint!=null||constraint.length()>0){
            constraint=constraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels=new ArrayList<>();

            for (int i=0;i<filterList.size();i++){
                if (filterList.get(i).getTitle().toUpperCase().contains(constraint)){
                    filteredModels.add(filterList.get(i));
                }
            }
            results.count=filterList.size();
            results.values=filterList;

        }
        else {
            results.count=filterList.size();
            results.values=filterList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults) {

        adapterPdfUser.pdfArrayList=(ArrayList<ModelPdf>)filterResults.values;

        adapterPdfUser.notifyDataSetChanged();
    }
}
