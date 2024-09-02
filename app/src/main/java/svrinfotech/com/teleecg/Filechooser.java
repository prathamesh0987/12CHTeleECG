package svrinfotech.com.teleecg;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ListAdapter;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class Filechooser extends Activity {

    // Stores names of traversed directories
    ArrayList<String> str = new ArrayList<>();
    // Check if the first level of the directory structure is the one showing
    private Item[] fileList;
    private File path = new File(Environment.getExternalStorageDirectory()+"/TELE-ECG","TELE-ECG Reports" );
    private String chosenFile;
    private static final int DIALOG_LOAD_FILE = 1000;
    public static File sel ;
    public AlertDialog.Builder builder;
    Dialog dialog = null;
    int iBackPress=0;
    ListAdapter adapter;
    public boolean BACK=false;
    private String[] mFileList;

    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setTitle("Patient Info...");
        loadFileList();
        showDialog(DIALOG_LOAD_FILE);
    }

    private void loadFileList()
    {
        try {
            path.mkdirs();
        }
        catch(SecurityException e) {
            e.printStackTrace();
        }
        try
        {
            int counter=0;
            if(path.exists())
            {
                FilenameFilter filter = new FilenameFilter()
                {
                    @Override
                    public boolean accept(File dir, String filename)
                    {
                        File sel = new File(dir, filename);
                        return sel.isFile() || sel.isDirectory();
                    }
                };
                mFileList = path.list(filter);
                fileList=new Item[counter];
                for (int j = 0; j < mFileList.length; j++)
                {
                    if(mFileList[j].endsWith(".dat"))
                    {
                        fileList[counter] = new Item(mFileList[j]);
                        counter++;
                    }
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "path does not exist..\nplease create new file first..", Toast.LENGTH_SHORT).show();
                mFileList= new String[0];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private class Item
    {
        private String file;
        private Item(String file)
        {
            this.file = file;
        }
        @Override
        public String toString()
        {
            return file;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        AlertDialog.Builder builder = new Builder(this);
        switch(id)
        {
            case DIALOG_LOAD_FILE:
                builder.setTitle("Choose .dat file to load data.");
                if(mFileList == null)
                {
                    dialog = builder.create();
                    return dialog;
                }
                builder.setItems(mFileList, new DialogInterface.OnClickListener() {
                    @SuppressWarnings("deprecation")
                    public void onClick(DialogInterface dialog, int which)
                    {
                        chosenFile = mFileList[which];
                        sel = new File(path + "/" + chosenFile);
                        if (sel.isDirectory())
                        {
                            // Adds chosen directory to list
                            str.add(chosenFile);
                            fileList = null;
                            path = new File(sel + "");
                            loadFileList();
                            removeDialog(DIALOG_LOAD_FILE);
                            showDialog(DIALOG_LOAD_FILE);
                        }
                        else
                        {
                            if(!sel.getName().endsWith(".dat"))
                            {
                                Toast.makeText(getApplicationContext(), "Select .dat file", Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }
                            else
                            {
                                // Perform action with file picked
                                iBackPress=1;
                                read_data(sel);
                            }
                        }
                    }
                });
                break;
        }
        dialog = builder.show();
        return dialog;
    }

    //Read data from selected file and display on screen
    private void read_data(File sel)
    {
        try
        {
            RandomAccessFile raf=new RandomAccessFile(sel,"rw");
            raf.readUTF();//title
            Patient_Info.sdf1=raf.readUTF();//date
            Patient_Info.schno=raf.readUTF();//chssno
            Patient_Info.sname=raf.readUTF();//name
            Patient_Info.sdob=raf.readUTF();//dob//
            Patient_Info.sage=raf.readUTF();//age//
            Patient_Info.sgen=raf.readUTF();//gende
            Patient_Info.sht=raf.readUTF();//height
            Patient_Info.swt=raf.readUTF();//weight
            Patient_Info.smedi=raf.readUTF();//medi
            Patient_Info.sBP=raf.readUTF();//bp
            MainActivity.iGain=raf.readInt();//gain
            paintView.filter_state=raf.readInt();//filter
            raf.seek(1023);

            for(int j=0;j<12;j++)
            {
                for(int k=0;k<paintView.total_samples;k++)
                {
                    paintView.raw_data[j][k]=(short) raf.readInt();
                    paintView.Gen_report[j][k]=paintView.raw_data[j][k];
                }
            }
            raf.close();//SNB: CONFIRM
            paintView.START_GUI=true;
            MainActivity.refresh_data=true;
            //calls patient info class if any changes in the patient details to be done
            Intent i=new Intent(getApplicationContext(),Patient_Info.class);
            startActivity(i);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.out.println("Error in Noo of text lines: "+e.getMessage());
        }
    }
    //exit the application on pressing of back button
    @Override
    public void onBackPressed()
    {
        BACK=true;
        Filechooser.this.finish();
        Intent i=new Intent(this,MainActivity.class);
        startActivity(i);
    }
}