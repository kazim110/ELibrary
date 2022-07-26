package com.example.elibrary2.filters;

import android.widget.Filter;

import com.example.elibrary2.adapters.AdapterCategory;
import com.example.elibrary2.adapters.AdapterPdfAdmin;
import com.example.elibrary2.models.ModelCategory;
import com.example.elibrary2.models.ModelPdf;

import java.util.ArrayList;

public class FilterPdfAdmin extends Filter {

    ArrayList<ModelPdf> filterList;
    AdapterPdfAdmin adapterPdfAdmin;

    public FilterPdfAdmin(ArrayList<ModelPdf> filterList, AdapterPdfAdmin adapterPdfAdmin){
        this.filterList=filterList;
        this.adapterPdfAdmin=adapterPdfAdmin;
    }
    @Override
    protected FilterResults performFiltering(CharSequence contraint) {
        FilterResults results=new FilterResults();
        if(contraint!=null&& contraint.length()>0){
            contraint=contraint.toString().toUpperCase();
            ArrayList<ModelPdf> filteredModels=new ArrayList<>();
            for (int i=0;i<filterList.size();i++){
                if(filterList.get(i).getTitle().toUpperCase().contains(contraint)){
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
        adapterPdfAdmin.pdfArrayList=(ArrayList<ModelPdf>)results.values;
        adapterPdfAdmin.notifyDataSetChanged();
    }
}
