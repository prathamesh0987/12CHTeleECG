package svrinfotech.com.teleecg;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Patient_Info extends MainActivity implements android.view.View.OnClickListener
{
    Button save,clear,cancel;
    public EditText 	name,age,chno,medi,BP,dob;
    public Spinner gender,ht,wt;
    public static ArrayAdapter<String> dataAdapter_gen;
    public static ArrayAdapter<Number> dataAdapter_ht,dataAdapter_wt;
    public TextView date;
    public	static String sname,sage,sgen,schno,smedi,sBP,sht,swt,sdob;
    public static String sdf,sdf1,acq_date;
    public	static boolean data_entered=false;
    //public static int ht_position,wt_position;
    paintView paintV;
    String pos;

    @SuppressLint("SimpleDateFormat") @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        //setting background and text color for title
        int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        if (actionBarTitleId > 0)
        {
            TextView title =  findViewById(actionBarTitleId);
            if (title != null)
                title.setTextColor(Color.RED);
        }
        ActionBar ab=getActionBar();
        if(ab!=null) {
            ab.setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.white)));
        }

        setContentView(R.layout.activity_generate_report);
        setTitleColor(Color.BLACK);
        setTitle("Patient Details");
        //initialization of GUI of patient details
        save= findViewById(R.id.btnSave);
        save.setOnClickListener(this);
        clear= findViewById(R.id.btnClear);
        clear.setOnClickListener(this);
        cancel= findViewById(R.id.btnCancel);
        cancel.setOnClickListener(this);
        chno= findViewById(R.id.chss_no);
        dob= findViewById(R.id.DOB);
        name= findViewById(R.id.txtName);
        age= findViewById(R.id.patientAge);
        date= findViewById(R.id.date);
        medi= findViewById(R.id.txtMedication);
        BP= findViewById(R.id.txtBP);
        gender= findViewById(R.id.spinner_gender);//drop down for gender
        ht= findViewById(R.id.spinner_height);//drop down for height
        wt= findViewById(R.id.spinner_weight);//drop down for weight


        //adding gender to list
        List<String> list = new ArrayList<>();
        list.add("Male");
        list.add("Female");
        dataAdapter_gen=new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,list);
        dataAdapter_gen.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(dataAdapter_gen);

        //adding height to dropdown list
        List<Number> list_ht=new ArrayList<>();
        for(int i=1;i<=255;i++)
            list_ht.add(i);

        dataAdapter_ht=new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,list_ht);
        dataAdapter_ht.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ht.setAdapter(dataAdapter_ht);
        ht.setSelection(149);

        //adding weight to dropdown list
        List<Number> list_wt=new ArrayList<>();
        for(int i=1;i<=255;i++)
            list_wt.add(i);

        dataAdapter_wt=new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,list_wt);
        dataAdapter_wt.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        wt.setAdapter(dataAdapter_wt);
        wt.setSelection(74);

        //date
        Date now=new Date();
        sdf = new SimpleDateFormat("yyyy_MM_dd HH-mm").format(now);
        acq_date=new SimpleDateFormat("dd/MM/yyyy  HH-mm").format(now);
        date.setText(sdf);

        if(MainActivity.load_existing_file)
        {
            set_data();
            cancel.setEnabled(false);//disables cancel button
        }
    }


    private void set_data()
    {
        try
        {
            //display text on text box
            date.setText(sdf1.trim());
            name.setText(sname.trim());
            chno.setText(schno.trim());
            dob.setText(sdob.trim());
            age.setText(sage.trim());

            //adding gender
            if(sgen.contains("Female"))
                gender.setSelection(1);
            else if(sgen.contains("Male"))
                gender.setSelection(0);

            //set the previous selected height
            for(int i=1;i<=255;i++)
            {
                pos=String.valueOf(i);
                if(sht.equals(pos))
                {
                    ht.setSelection(i-1);
                }
            }

            //set the previous selected weight
            for(int i=1;i<=255;i++)
            {
                pos=String.valueOf(i);
                if(swt.equals(pos))
                {
                    wt.setSelection(i-1);
                }
            }
            medi.setText(smedi.trim());
            BP.setText(sBP.trim());
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("set data.."+e.getMessage());
        }
    }

    public void onClick(View v)
    {
        if(v.getId()==R.id.btnSave)
        {
            data_entered=true;
            sdf=date.getText().toString();
            sname=name.getText().toString().trim();
            schno=chno.getText().toString().trim();
            sdob=dob.getText().toString().trim();
            sage=age.getText().toString().trim();
            sgen=gender.getSelectedItem().toString().trim();
            sht=ht.getSelectedItem().toString().trim();
            swt=wt.getSelectedItem().toString().trim();
            smedi=medi.getText().toString().trim();
            sBP=BP.getText().toString().trim();

            //validation for GUI ?? SNB: Its commented.. CAn I delete?
			/*if(schno.isEmpty())
					chno.setError("Enter Id number");
			else if(sname.isEmpty())
					name.setError("Enter name");
			else if( sage.isEmpty())
					age.setError("Enter age");
			else if(Integer.parseInt(sage)>100)
					age.setError("Age should be less than 100..");
			else if(smedi.isEmpty())
					medi.setError("Enter medication");
			else if(sBP.isEmpty())
					BP.setError("Enter Blood Pressure");
			else if(sdob.isEmpty())
					dob.setError("enter date of birth");
			else
			{*/
            //calls the constructor of paintView

            MainActivity.gen_report_menu.setVisible(true);

            paintV=new paintView(sdf,sname,schno,sdob,sage,sgen,sht,swt,smedi,sBP,Patient_Info.this);
            Patient_Info.this.finish();
            //}
            Toast.makeText(getApplicationContext(), "Data saved",Toast.LENGTH_SHORT).show();
        }
        if(v.getId()==R.id.btnClear)
            Clear_alert();
        if(v.getId()==R.id.btnCancel)
            Patient_Info.this.finish();
    }

    //clear data and sets default data
    public void Clear_alert()
    {
        AlertDialog.Builder build=new AlertDialog.Builder(Patient_Info.this);
        build.setMessage("Do you want to clear the entered details?");
        build.setPositiveButton("YES", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int arg1)
            {
                name.setText("");
                age.setText("");
                gender.setSelection(0);
                medi.setText("");
                BP.setText("");
                chno.setText("");
                dob.setText("");
                ht.setSelection(149);
                wt.setSelection(74);
            }
        });

        build.setNegativeButton("NO",new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface arg0, int arg1)
            {
                arg0.cancel();
            }
        });
        AlertDialog alert=build.create();
        alert.show();
    }
}

