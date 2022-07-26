package com.example.elibrary2.adapters;

import static com.example.elibrary2.Constants.MAX_BYTES_PDF;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.elibrary2.MyApplication;
import com.example.elibrary2.PdfEditActivity;
import com.example.elibrary2.databinding.RowPdfAdminBinding;
import com.example.elibrary2.filters.FilterPdfAdmin;
import com.example.elibrary2.models.ModelPdf;
import com.github.barteksc.pdfviewer.PDFView;
import com.github.barteksc.pdfviewer.listener.OnErrorListener;
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener;
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.PrimitiveIterator;

public class AdapterPdfAdmin extends RecyclerView.Adapter<AdapterPdfAdmin.HolderPdfAdmin> implements Filterable {

    private Context context;
    public ArrayList<ModelPdf> pdfArrayList,filterList;
    private RowPdfAdminBinding binding;

    private FilterPdfAdmin filter;

    private static final String TAG="PDF_ADAPTER_TAG";

    private ProgressDialog progressDialog;

    public AdapterPdfAdmin(Context context, ArrayList<ModelPdf> pdfArrayList) {

        this.context = context;
        this.pdfArrayList = pdfArrayList;
        this.filterList=pdfArrayList;

        progressDialog =new ProgressDialog(context);
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);
    }

    @NonNull
    @Override
    public HolderPdfAdmin onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        binding=RowPdfAdminBinding.inflate(LayoutInflater.from(context),parent,false);
        return new HolderPdfAdmin(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull HolderPdfAdmin holder, int position) {
        ModelPdf model=pdfArrayList.get(position);
        String title=model.getTitle();
        String description=model.getDescription();
        long timestamp=model.getTimestamp();
        String formattedDate= MyApplication.formatTimestamp(timestamp);

        holder.titleTv.setText(title);
        holder.descriptionTv.setText(description);
        holder.dateTv.setText(formattedDate);

        loadCategory(model,holder);
        loadPdfFromUrl(model,holder);
        loadPdfSize(model,holder);
        
        holder.moreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                moreOptionDialog(model,holder);
            }
        });
    }

    private void moreOptionDialog(ModelPdf model, HolderPdfAdmin holder) {
        String bookId =model.getId();
        String bookUrl=model.getUrl();
        String bookTitle=model.getTitle();

        String[] options={"Edit","Delete"};

        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setTitle("Choose Options")
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(i==0){
                            Intent intent=new Intent(context, PdfEditActivity.class);
                            intent.putExtra("bookId",bookId);
                            context.startActivity(intent);
                        }
                        else if (i==1){
                            deleteBook(model,holder);
                        }
                    }
                })
                .show();
    }

    private void deleteBook(ModelPdf model, HolderPdfAdmin holder) {
        String bookId =model.getId();
        String bookUrl=model.getUrl();
        String bookTitle=model.getTitle();

        Log.d(TAG, "deleteBook: Deleting...");
        progressDialog.setMessage("Deleting "+bookTitle+" ...");
        progressDialog.show();

        Log.d(TAG, "deleteBook: Deleting from storage...");
        StorageReference storageReference=FirebaseStorage.getInstance().getReferenceFromUrl(bookUrl);
        storageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d(TAG, "onSuccess: Deleted from storage");
                        Log.d(TAG, "onSuccess: Now deleting info from db");

                        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Books");
                        reference.child(bookId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d(TAG, "onSuccess: Deleted from db too");
                                        progressDialog.dismiss();
                                        Toast.makeText(context,"Book Deleted Successfully...",Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(TAG, "onFailure: Failed to delete from db due to "+e.getMessage());
                                        progressDialog.dismiss();
                                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                                    }
                                });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Fialed to delete from storage due to "+e.getMessage());
                        progressDialog.dismiss();
                        Toast.makeText(context,""+e.getMessage(),Toast.LENGTH_SHORT).show();

                    }
                });
    }

    private void loadPdfSize(ModelPdf model, HolderPdfAdmin holder) {
        String pdfUrl =model.getUrl();

        StorageReference ref= FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);
        ref.getMetadata()
                .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                    @Override
                    public void onSuccess(StorageMetadata storageMetadata) {
                        double bytes=storageMetadata.getSizeBytes();
                        Log.d(TAG, "onSuccess: "+model.getTitle()+" "+bytes);

                        double kb=bytes/1024;
                        double mb=kb/1024;

                        if(mb>=1){
                            holder.sizeTv.setText(String.format("%.2f",mb)+" MB");
                        }
                        else if(kb>=1){
                            holder.sizeTv.setText(String.format("%.2f",kb)+" KB");
                        }
                        else {
                            holder.sizeTv.setText(String.format("%.2f",bytes)+" bytes");
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: "+e.getMessage());
                    }
                });

    }

    private void loadPdfFromUrl(ModelPdf model, HolderPdfAdmin holder) {
        String pdfUrl=model.getUrl();
        StorageReference ref=FirebaseStorage.getInstance().getReferenceFromUrl(pdfUrl);

        ref.getBytes(MAX_BYTES_PDF)
                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        Log.d(TAG, "onSuccess: "+model.getTitle()+" successfully got the file");

                        holder.pdfView.fromBytes(bytes)
                                .pages(0)
                                .spacing(0)
                                .swipeHorizontal(false)
                                .enableSwipe(false)
                                .onError(new OnErrorListener() {
                                    @Override
                                    public void onError(Throwable t) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onError: "+t.getMessage());

                                    }
                                })
                                .onPageError(new OnPageErrorListener() {
                                    @Override
                                    public void onPageError(int page, Throwable t) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "onPageError: "+t.getMessage());
                                    }
                                })
                                .onLoad(new OnLoadCompleteListener() {
                                    @Override
                                    public void loadComplete(int nbPages) {
                                        holder.progressBar.setVisibility(View.INVISIBLE);
                                        Log.d(TAG, "loadComplete: Pdf loaded");
                                    }
                                })
                                .load();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        holder.progressBar.setVisibility(View.INVISIBLE);

                        Log.d(TAG, "onFailure:  failed getting file from url due to "+e.getMessage());
                    }
                });
    }

    private void loadCategory(ModelPdf model, HolderPdfAdmin holder) {
        String categoryId=model.getCategoryId();

        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Categories");
        ref.child(categoryId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String category=""+snapshot.child("category").getValue();

                        holder.categoryTv.setText(category);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public int getItemCount() {
        return pdfArrayList.size();
    }

    @Override
    public Filter getFilter() {
        if(filter==null){
            filter=new FilterPdfAdmin(filterList,this);
        }
        return filter;
    }

    class HolderPdfAdmin extends RecyclerView.ViewHolder{
        PDFView pdfView;
        ProgressBar progressBar;
        TextView titleTv,descriptionTv,categoryTv,sizeTv,dateTv;
        ImageButton moreBtn;

        public HolderPdfAdmin(@NonNull View itemView) {
            super(itemView);

            pdfView=binding.pdfView;
            progressBar=binding.progressBar;
            titleTv =binding.titleTv;
            descriptionTv=binding.descriptionTv;
            categoryTv=binding.categoryTv;
            sizeTv=binding.sizeTv;
            dateTv=binding.dateTv;
            moreBtn=binding.moreBtn;
        }
    }
}
