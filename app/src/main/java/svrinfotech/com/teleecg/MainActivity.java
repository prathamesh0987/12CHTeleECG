package svrinfotech.com.teleecg;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressLint("HandlerLeak")
public class MainActivity extends AppCompatActivity
{
    public static final String TAG = "TeleECG";

    //boolean create_avg_report=false;
    //boolean create_fine_report=false;
    public static int f_size,no_of_lead_to_display,total_pages,
            lead_page=0,step,selectedPosition,selectedGain,item_id_acq_mode,
            item_id_gain,item_id_filter,mode;
    //public static MenuItem		menu_id;
    public static String mState = "HIDE_MENU";
    protected static final int  START_DATA_ACQ = 1;
    protected static final int  STOP_DATA = 2;
    protected static final int  GENERATE_REPORT = 3;
    protected static final int  LOAD_DATA = 5;
    protected static final int  MESSAGE_COMMUNICATION= 6;
    //protected static final int  RECONNECT			 = 7;
    protected static final int  NEW_PATIENT	= 8;

    public static String settingsFolder = null;

    InputStream instrm;
    OutputStream outstrm;
    public static MenuItem back_to_main_menu,gain_menu,send_menu,filter_menu,add_patient_info,load_menu,start_menu,stop_menu,next_menu,prev_menu,
            mode_menu,gen_report_menu,new_patient,acq_mode,next_lead,prev_lead,repeat_acq;
    //canvas declaration
    public boolean next_and_prev_clicked=false,
            menu_clicked=false;
    public static boolean	report_gen=false,
            filter_change=false,
            gain_change=false,
            load_existing_file=false,
            send_report=false,
            acq_repeat=false,
            acq_mode_change=false;
    int strcounter =0;
    double 	RAM_SIZE=0;
    //if value of report_sequential is 1  then it plots sequential report else plots simultaneous report
    public static int report_sequential,iGain;
    paintView pv;
    Display d;
    Create_report cr;
    Filechooser fc;
    Bluetooth_connect BT;
    RadioGroup gainGroup;
    RadioButton gainBtn;
    public static int iScreenWidth,iScreenHeight;
    Button bt_new,bt_load,bt_lead;
    String password="";
    static Boolean set_pswd=false;
    String comment="";
    static TextView title;
    public static boolean stop=false,demo_start=false,refresh_data=false,menu_press=false, create_image=false,
            create_pdf=false,create_data=false,create_text=false,lead_pressed=false, loggerCreated = false;

    @SuppressWarnings("deprecation")
    private void initialize_GUI() {
        try {
            //to run application full screen
          //  getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
            //to keep the screen on while application is running
            //getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            //to change the text color of title
            int actionBarTitleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
            if (actionBarTitleId > 0)
            {
                title = (TextView) findViewById(actionBarTitleId);
                if (title != null)
                    title.setTextColor(Color.RED);
            }
            //set the color of titlebar
            ActionBar ab=getActionBar();
            ab.setBackgroundDrawable(new ColorDrawable(Color.GRAY));

            //initialize GUI components


            //getting the height and width of tablet/mobile
            d=getWindowManager().getDefaultDisplay();
            iScreenWidth=d.getWidth();
            iScreenHeight=d.getHeight();
            Log.i(TAG, "initialize_GUI() completed");
        }
        catch (Exception e) {
            Log.e(TAG, "Error in initialize_GUI()", e.getCause());
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String size;
        // set the directory to store the settings file.
        settingsFolder = Environment.getExternalStorageDirectory()+"/TELE-ECG/Settings";
        size=find_RAM_SIZE();		//Why to find out RAM size?	//RKJ //To calculate RAM_SIZE //SNB
        Log.i(TAG, "ram_size="+size);
        initialize_GUI();// initialises the
        read_lead_number();//reads the selected lead value saved in file
        Filter_Gain_preference();//reads and sets the selected filter and gain value
        bt_new= findViewById(R.id.newEntry);
        bt_load= findViewById(R.id.loadData);
        bt_lead= findViewById(R.id.leads);
    }

    @Override
    protected void onResume() {
        super.onResume();

        bt_new.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try
                {
                    Log.e(TAG,"new Button Clicked");
                    if(no_of_lead_to_display==12)
                    {
                        paintView.Auto=false;
                        acq_mode.setVisible(false);
                        prev_menu.setVisible(false);
                        next_menu.setVisible(false);
                    }
                    create_data=false;
                    create_image=false;
                    create_pdf=false;
                    create_text=false;
                    load_existing_file=false;

                    init_patient_info();
                    Intent i=new Intent(getApplicationContext(),Bluetooth_connect.class);
                    startActivity(i);
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        bt_load.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    create_data=false;
                    create_image=false;
                    create_pdf=false;
                    create_text=false;
                    onOptionsItemSelected(load_menu);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        bt_lead.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    lead_pressed=true;
                    Select_lead_display();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private String find_RAM_SIZE()
    {
        RandomAccessFile reader = null;
        String load = null;
        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        double totalRam = 0;
        String lastValue = "";
        try
        {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            load = reader.readLine();

            // Get the Number value from the string
            Pattern p = Pattern.compile("(\\d+)");
            Matcher m = p.matcher(load);
            String value = "";
            while (m.find())
                value = m.group(1);
            reader.close();

            totalRam = Double.parseDouble(value);

            double mb = totalRam / 1024.0;
            double gb = totalRam / 1048576.0;
            double tb = totalRam / 1073741824.0;

            if (tb > 1) {
                lastValue = twoDecimalForm.format(tb).concat(" TB");
                RAM_SIZE=Double.parseDouble(twoDecimalForm.format(tb));
                Log.i(TAG,"RAM_SIZE tb="+RAM_SIZE);
            } else if (gb > 1) {
                lastValue = twoDecimalForm.format(gb).concat(" GB");
                RAM_SIZE=Double.parseDouble(twoDecimalForm.format(gb));
                Log.i(TAG,"RAM_SIZE gb="+RAM_SIZE);
            } else if (mb > 1) {
                lastValue = twoDecimalForm.format(mb).concat(" MB");
                RAM_SIZE=Double.parseDouble(twoDecimalForm.format(mb));
                Log.i(TAG,"RAM_SIZE mb="+RAM_SIZE);
            } else {
                lastValue = twoDecimalForm.format(totalRam).concat(" KB");
                RAM_SIZE=Double.parseDouble(twoDecimalForm.format(totalRam));
                Log.i(TAG,"RAM_SIZE total RAM="+RAM_SIZE);
            }
        } catch (IOException ex) {
            Log.e(TAG, "Error in find_RAM_SIZE(): ", ex.getCause());
            ex.printStackTrace();
        } finally {
            // Streams.close(reader);
        }
        return lastValue;
    }

    private void Filter_Gain_preference()
    {
        try
        {
            //store the value of selected Gain
            SharedPreferences gain_settings=getSharedPreferences("GAIN", 0);
            if(gain_change)
                gain_settings.getInt("gain_val", item_id_gain);//user selected gain
            else
                item_id_gain=gain_settings.getInt("gain_val", 4);// default gain value

            //store the value of selected filter
            SharedPreferences filter_settings=getSharedPreferences("FILTER", 0);
            if(filter_change)
                filter_settings.getInt("filter_val", item_id_filter);//User selected filter
            else
                item_id_filter=filter_settings.getInt("filter_val", 2);// default filter

            //store the value of selected acquisition mode
            SharedPreferences acq_mode_settings=getSharedPreferences("ACQ_MODE", 0);
            if(acq_mode_change)
                acq_mode_settings.getInt("acq_mode_val", item_id_acq_mode);// User selected mode
            else
                item_id_acq_mode=acq_mode_settings.getInt("acq_mode_val", 0);//default mode

            Log.i(TAG,"Gain set to: item_id_gain=" + item_id_gain);
            Log.i(TAG,"Filter set to: item_id_filter=" + item_id_filter);
            Log.i(TAG,"Acq Mode set to: item_id_acq_mode=" + item_id_acq_mode);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Error in Filter_Gain_preference(): ", e.getCause());
        }
    }

    //saves the variable value of the selected gain and filter
    @Override
    protected void onStop()
    {
        super.onStop();
        //saves selected gain in menu option
        SharedPreferences settings=getSharedPreferences("GAIN", 0);
        SharedPreferences.Editor editor=settings.edit();
        editor.putInt("gain_val",item_id_gain);
        editor.commit();

        //saves selected filter in menu option
        SharedPreferences filter_settings=getSharedPreferences("FILTER", 0);
        SharedPreferences.Editor edit_filter=filter_settings.edit();
        edit_filter.putInt("filter_val", item_id_filter);
        edit_filter.commit();

        //saves selected filter in menu option
        SharedPreferences acq_mode_settings=getSharedPreferences("ACQ_MODE", 0);
        SharedPreferences.Editor edit_acq_mode=acq_mode_settings.edit();
        edit_acq_mode.putInt("acq_mode_val", item_id_acq_mode);
        edit_acq_mode.commit();

        Log.i(TAG,"set item_id_gain=" + item_id_gain);
        Log.i(TAG,"set item_id_filter=" + item_id_filter);
        Log.i(TAG,"set item_id_acq_mode=" + item_id_acq_mode);
    }

    public static void read_filter_gain()
    {
        try
        {
            int gain_arr[] = {1,2,3,4,6,8,12};
            int filter_arr[] = {1,2,3,4,5,0};
            int item_id_gain_arr[] = {R.id.gain1, R.id.gain2, R.id.gain3, R.id.gain4, R.id.gain6, R.id.gain8, R.id.gain12};

            //get the item position of gain in menu item
            for(int i =0; i<7; i++)
            {
                if(item_id_gain == item_id_gain_arr[i])
                {
                    item_id_gain = i;
                    break;
                }
            }
            iGain = gain_arr[item_id_gain];
            paintView.filter_state = filter_arr[item_id_filter];

            if(item_id_acq_mode == 0)//
            {
                paintView.Auto = true;
                next_menu.setVisible(false);
                prev_menu.setVisible(false);
            }
            else
            {
                paintView.Auto = false;
                next_menu.setVisible(true);
                prev_menu.setVisible(true);
            }

            if(no_of_lead_to_display == 12)
            {
                paintView.Auto = false;
                next_menu.setVisible(false);
                prev_menu.setVisible(false);
            }

            //set selected gain and filter value as checked in the menu option
            gain_menu.getSubMenu().getItem().getSubMenu().getItem(item_id_gain).setChecked(true);
            filter_menu.getSubMenu().getItem().getSubMenu().getItem(item_id_filter).setChecked(true);
            acq_mode.getSubMenu().getItem().getSubMenu().getItem(item_id_acq_mode).setChecked(true);
        } catch (Exception e)
        {
            Log.e(TAG,"Error in read_filter_gain(): ",e);
            e.printStackTrace();
        }
    }

    public static void Store_filter_gain()
    {
        try
        {
            //code to set gain id according to item id
            int item_id_gain_arr[] = {R.id.gain1,R.id.gain2,R.id.gain3,R.id.gain4,R.id.gain6,R.id.gain8,R.id.gain12};
            int item_id_filter_arr[]={R.id.item_0_150,R.id.item_5_40,R.id.item_0_40,R.id.item_5_25,R.id.item_0_25,R.id.item_filter_off};
            int item_id_acq_mode_arr[]={R.id.item_Auto,R.id.item_manual};

            //setting gain
            if(gain_change)
            {
                for(int i = 0; i < 7; i++)
                {
                    if(item_id_gain == item_id_gain_arr[i])
                    {
                        item_id_gain = i;
                        break;
                    }
                }
            }
            else
            {
                item_id_gain=4;
            }

            //setting filter
            if(filter_change)
            {
                for (int i = 0; i < 6; i++)
                {
                    if(item_id_filter==item_id_filter_arr[i])
                    {
                        item_id_filter=i;
                        paintView.filter_state = (item_id_filter==5)?0:i+1;
                        break;
                    }
                }
            }
            else
            {
                item_id_filter=2;
            }

            //setting acquisition mode
            if(acq_mode_change)
            {
                for (int i = 0; i <=1; i++)
                {
                    if(item_id_acq_mode==item_id_acq_mode_arr[i])
                    {
                        item_id_acq_mode=i;
                        break;
                    }
                }
            }
            else
            {
                item_id_acq_mode=0;
            }

        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inf=getMenuInflater();
        inf.inflate(R.menu.main, menu);

        //get the position of menu options
        prev_menu=menu.getItem(0);
        start_menu=menu.getItem(1);
        stop_menu=menu.getItem(2);
        next_menu=menu.getItem(3);
        mode_menu=menu.getItem(4);
        filter_menu=menu.getItem(5);
        gain_menu=menu.getItem(6);
        gen_report_menu=menu.getItem(7);
        acq_mode=menu.getItem(8);
        add_patient_info=menu.getItem(9);
        load_menu=menu.getItem(10);
        send_menu=menu.getItem(11);
        back_to_main_menu=menu.getItem(12);
        new_patient=menu.getItem(13);
        prev_lead=menu.getItem(14);
        next_lead=menu.getItem(15);
        repeat_acq=menu.getItem(16);
        //view_report=menu.getItem(17);

        start_menu.setVisible(false);
        filter_menu.setVisible(false);
        stop_menu.setVisible(false);
        mode_menu.setVisible(false);
        gen_report_menu.setVisible(false);
        send_menu.setVisible(false);
        gain_menu.setVisible(false);
        load_menu.setVisible(false);
        back_to_main_menu.setVisible(false);
        prev_menu.setVisible(false);
        next_menu.setVisible(false);
        new_patient.setVisible(false);
        next_lead.setVisible(false);
        prev_lead.setVisible(false);
        repeat_acq.setVisible(false);

        //hides the menu during connectivity
        if(mState.equals("HIDE_MENU"))
        {
            for(int j=0;j<menu.size();j++)
            {
                menu.getItem(j).setVisible(false);
            }
        }
        return true;
    }

    //used for setting the title of the screen
    public void set_title(int mode, boolean auto)
    {
        String data_str;
        if(mode == 1)
            data_str="Test";
        else
            data_str="ECG";

        setTitle(data_str);
    }

    void set_acq_mode(MenuItem item, boolean mode)	//AUTO:TRUE, MANUAL:FALSE
    {
        item_id_acq_mode=item.getItemId();
        acq_mode_change=true;
        item.setChecked(true);
        menu_clicked=true;
        paintView.Auto=mode;
        next_menu.setVisible(!mode);
        prev_menu.setVisible(!mode);
        paintView.page=0;
        repaint();
    }

    private void handle_item_previous()
    {
        if(load_existing_file)//changes the data
        {
            if(paintView.page>1)
                paintView.page--;
            else if(paintView.page==1)
                paintView.page=4;
            refresh_data=true;
            pv.postInvalidate();
        }
        else
        {
            if(paintView.page>0)
                paintView.page--;
            else if(paintView.page==0)
                paintView.page=(total_pages-1);
            menu_clicked=true;
            next_and_prev_clicked=true;
            refresh_data=true;
            repaint();
        }

    }

    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.item_Auto:
                set_acq_mode(item, true);	//AUTO:TRUE, MANUAL :FALSE

                break;
            case R.id.item_manual:
                set_acq_mode(item, false);	//AUTO:TRUE, MANUAL :FALSE
                break;
            case R.id.item_previous:	//back button
                handle_item_previous();
                break;
            case R.id.item_next:	//next button
                handle_item_next();
                break;
            case R.id.item_next_lead: //used only for 6,3,1 lead data
                handle_item_next_lead();
                break;
            case R.id.item_prev_lead: //used only for 6,3,1 lead data
                handle_item_prev_lead();
                break;
            case R.id.item_Sequence_report:
                report_sequential=1;
                item.setChecked(true);
                Create_report_alert();//code on OK button of alert dialog
                break;
            case R.id.item_Simultaneous_report:
                report_sequential=0;
                item.setChecked(true);
                Create_report_alert();//code on OK button of alert dialog
                break;
            case R.id.item_stop:
                try {
                    outstrm.write('S');	//char 'S' sent to stop acquisition
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stop = true;
                refresh_data = false;
                stop_menu.setVisible(false);
                mode_menu.setVisible(true);
                start_menu.setVisible(true);
                filter_menu.setVisible(true);
                gain_menu.setVisible(true);
                back_to_main_menu.setVisible(true);
                if(mode==1)
                    filter_menu.setVisible(false);
                //pass message to handler
                Message m= mHandler.obtainMessage(STOP_DATA);
                mHandler.sendMessage(m);
                break;
            case R.id.item_start:
                Toast.makeText(getApplicationContext(),"Start Clicked"+demo_start,Toast.LENGTH_LONG).show();
                if(demo_start)
                {
                    load_existing_file=true;
                    repaint();
                    Demo_read();
                    initialization_paintView();
                    pv.invalidate();
                }
                else
                {
                    pv.first_pass=true;
                    refresh_data=true;
                    load_existing_file=false;
                    stop=false;
                    if(acq_repeat)
                        Patient_Info.data_entered=true;
                    else
                        Patient_Info.data_entered=false;
                    stop_menu.setVisible(true);
                    mode_menu.setVisible(false);
                    gain_menu.setVisible(false);
                    start_menu.setVisible(false);
                    gen_report_menu.setVisible(false);
                    send_menu.setVisible(false);
                    filter_menu.setVisible(false);
                    load_menu.setVisible(false);
                    back_to_main_menu.setVisible(false);
                    paintView.batt_count=0;
                    paintView.Battery_level=0;
                    //pass message to handler
                    Message m7= mHandler.obtainMessage(START_DATA_ACQ);
                    mHandler.sendMessage(m7);
                }
                break;
            case R.id.item_0_150://(-50Hz)
                paintView.filter_state=1;
                handle_filter(item);
                break;
            case R.id.item_5_40:
                paintView.filter_state=2;
                handle_filter(item);
                break;
            case R.id.item_0_40:
                paintView.filter_state=3;
                handle_filter(item);
                break;
            case R.id.item_5_25:
                paintView.filter_state=4;
                handle_filter(item);
                break;
            case R.id.item_0_25:
                paintView.filter_state=5;
                handle_filter(item);
                break;
            case R.id.item_filter_off:
                paintView.filter_state=0;
                handle_filter(item);
                break;
            case R.id.item_test:
                mode=1;
                paintView.filter_state=0;
                handle_data_mode(item,false);//test mode filter should be OFF...filter visible false
                break;
            case R.id.item_ecg:
                mode=0;
                Store_filter_gain();
                read_filter_gain();
                handle_data_mode(item,true);//ecg mode filter should be ON...filter visible true
                break;
            case R.id.item_patient_detail:
                handle_patient_detail();
                break;
            case R.id.item_load_data:
                handle_load_data();
                break;
            case R.id.item_new_patient:
                Store_filter_gain();
                read_filter_gain();
                handle_new_patient();
                break;
            case  R.id.item_send:
                send_report();
                break;
            case  R.id.gain1:
                iGain=1;
                handle_gain(item);
                break;
            case  R.id.gain2:
                iGain=2;
                handle_gain(item);
                break;
            case  R.id.gain3:
                iGain=3;
                handle_gain(item);
                break;
            case  R.id.gain4:
                iGain=4;
                handle_gain(item);
                break;
            case  R.id.gain6:
                iGain=6;
                handle_gain(item);
                break;
            case  R.id.gain8:
                iGain=8;
                handle_gain(item);
                break;
            case  R.id.gain12:
                iGain=12;
                handle_gain(item);
                break;
            case  R.id.item_MainPage:
                Store_filter_gain();
                MainActivity.this.finish();
                Intent main=new Intent(getApplicationContext(),MainActivity.class);
                startActivity(main);
                finish();
                break;
            case R.id.item_repeat_acq:
                acq_repeat=true;
                Store_filter_gain();
                read_filter_gain();
                onOptionsItemSelected(new_patient);
                break;
            default:break;
        }
        return true;
    }

    private void handle_gain(MenuItem item) {
        try {
            item_id_gain=item.getItemId();
            gain_change=true;
            item.setChecked(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handle_data_mode(MenuItem item, boolean data_mode) {
        try {
            item.setChecked(true);
            filter_menu.setVisible(data_mode);
            pv.init_done=false;
            paintView.ADS_samples_count=0;
            paintView.check_data_count=0;
            paintView.disp_count=0;
            paintView.chk_disp_count=0;
            paintView.batt_count=0;
            menu_clicked=true;
            repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handle_item_prev_lead() {
        try {
            if(lead_page<=(total_pages-1))
            {
                lead_page--;
                refresh_data=true;
                pv.postInvalidate();
            }
            if(lead_page<0)
                lead_page=(total_pages-1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handle_item_next_lead() {
        try {
            if(lead_page>=0)
            {
                lead_page++;
                refresh_data=true;
                pv.postInvalidate();
            }
            if(lead_page>(total_pages-1))
                lead_page=0;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handle_item_next()
    {
        try
        {
            if(load_existing_file)
            {
                if(paintView.page<4)
                    paintView.page++;
                else if(paintView.page==4)
                    paintView.page=1;
                refresh_data=true;
                pv.postInvalidate();
            }
            else
            {
                if(paintView.page<(total_pages-1))
                    paintView.page++;
                else if(paintView.page==(total_pages-1))
                    paintView.page=0;
                menu_clicked=true;
                next_and_prev_clicked=true;
                repaint();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handle_filter(MenuItem item) {
        try {
            item_id_filter=item.getItemId();
            filter_change=true;
            item.setChecked(true);
            menu_clicked=true;
            if((paintView.filter_state==0 || paintView.filter_state==1)&& paintView.skip_point<2)
                paintView.skip_point=2;
            else if(paintView.skip_point>6)
                paintView.skip_point=6;

            System.out.println("skip_point chnge="+paintView.skip_point);
            repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handle_new_patient()
    {
        try
        {
            create_data=false;
            create_image=false;
            create_pdf=false;
            create_text=false;
            report_gen=false;
            send_menu.setVisible(false);
            set_empty_arry();
            Message new_patient= mHandler.obtainMessage(NEW_PATIENT);
            mHandler.sendMessage(new_patient);
            invalidateOptionsMenu();//SNB REVISIT
            menu_clicked=true;
            repaint();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handle_load_data() {
        try {
            refresh_data=true;
            load_existing_file=true;
            Patient_Info.data_entered=false;
            lead_page=0;
            gen_report_menu.setVisible(true);
            prev_menu.setVisible(true);
            next_menu.setVisible(true);
            if(no_of_lead_to_display!=paintView.CH_NUM)
            {
                next_lead.setVisible(true);
                prev_lead.setVisible(true);
            }
            back_to_main_menu.setVisible(!add_patient_info.isVisible());
            Intent i=new Intent(this,Filechooser.class);
            startActivity(i);
            initialization_paintView();
            pv.postInvalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handle_patient_detail() {
        try {
            load_existing_file=false;
            paintView.START_GUI=false;
            Intent getData=new Intent(getApplicationContext(),Patient_Info.class);
            startActivity(getData);
            set_title(mode, paintView.Auto);
            pv.invalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Demo_read() {
        try {
            //opens the demo file stored in the asset folder of application...file included in the application apk file
            AssetManager manager = getAssets();
            InputStream mInput = manager.open("Demo.dat");
            DataInputStream dinstream=new DataInputStream(mInput);

            //reads the other details
            dinstream.readUTF();
            dinstream.readUTF();
            dinstream.readUTF();
            dinstream.readUTF();
            dinstream.readUTF();
            dinstream.readUTF();
            dinstream.readUTF();
            dinstream.readUTF();
            dinstream.readUTF();
            dinstream.readUTF();
            dinstream.readUTF();
            dinstream.readUTF();
            dinstream.readUTF();
            dinstream.readUTF();
            for(int j=0;j<12;j++)//reads the ecg data and saves in the raw_data array
            {
                for(int k=0;k<paintView.total_samples;k++)
                {
                    paintView.raw_data[j][k]=(short) dinstream.readInt();
                }
            }
            dinstream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //displays options window for sending ECG Report through net
    private void send_report()
    {
        Intent picMessageIntent = new Intent(android.content.Intent.ACTION_SEND);
        picMessageIntent.setType("image/png");
        File root = new File(Environment.getExternalStorageDirectory()+"/TELE-ECG Reports", paintView.Pass_On_name);
        File downloadedPic = new File(root,Create_report.sFileTitle_png+".PNG");
        picMessageIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(downloadedPic));
        startActivity(Intent.createChooser(picMessageIntent, "Send your picture using:"));
    }

    //ask for comment about the ECG report
    private void display_alert()
    {
        try
        {
            final AlertDialog.Builder build=new AlertDialog.Builder(MainActivity.this);
            build.setMessage("Do you want to insert comment? \n(Upto 155 characters)");
            build.setCancelable(false);
            final EditText input = new EditText(this);
            build.setView(input);

            build.setPositiveButton("YES", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface arg0, int arg1)
                {
                    comment=input.getText().toString().trim();
                    int len=comment.length();
                    if(len<=155)
                    {
                        report_gen=true;
                        cr=new Create_report(comment,password,MainActivity.this);
                        Open_view_report();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Length of comment should be upto 155 charcters.", Toast.LENGTH_SHORT).show();
                        display_alert();
                    }
                }
            });

            build.setNegativeButton("NO", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int arg1)
                {
                    report_gen=true;
                    comment="";
                    cr=new Create_report(comment,password,MainActivity.this);
                    Open_view_report();
                }
            });
            AlertDialog alert=build.create();
            alert.show();
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("Alert Alert"+e.getMessage());
        }
    }

    protected void Open_view_report() {
        try {

            final AlertDialog.Builder build=new AlertDialog.Builder(MainActivity.this);
            build.setMessage("View Report?");
            build.setCancelable(false);
            build.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {

                    //File file = new File(Environment.getExternalStorageDirectory()+"/TELE-ECG/TELE-ECG Reports",paintView.Pass_On_name);
                    String path=Environment.getExternalStorageDirectory()+"/TELE-ECG/TELE-ECG Reports";
                    openFile(path);

                }
            });

            build.setNegativeButton("NO",new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface arg0, int arg1) {
                    arg0.cancel();

                }
            });
            AlertDialog alert=build.create();
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //mobile gallery is opened to view report
    public void openFile(String minmeType) {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // special intent for Samsung file manager
        Intent sIntent = new Intent("com.sec.android.app.myfiles.PICK_DATA");

        Intent chooserIntent;
        if(getPackageManager().resolveActivity(sIntent, 0) != null){
            // it is device with samsung file manager
            chooserIntent = Intent.createChooser(sIntent, "Open file");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[] { intent});
        }
        else {
            chooserIntent = Intent.createChooser(intent, "Open file");
        }
        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(chooserIntent);
        try {
            // startActivityForResult(chooserIntent, CHOOSE_FILE_REQUESTCODE);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(getApplicationContext(), "No suitable File Manager was found.", Toast.LENGTH_SHORT).show();
        }
    }

    //while creating pdf file user can set password for the pdf report file
    public  String ask_for_pswd()
    {
        try {
            final AlertDialog.Builder build=new AlertDialog.Builder(MainActivity.this);
            build.setMessage("Do you want to set password to pdf file?");
            build.setCancelable(false);
            final EditText pswd = new EditText(this);
            build.setView(pswd);

            build.setPositiveButton("YES", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface arg0, int arg1)
                {
                    password=pswd.getText().toString().trim();
                    set_pswd=true;
                }
            });

            build.setNegativeButton("NO", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int arg1)
                {
                    set_pswd=true;
                    password="";
                }
            });
            AlertDialog alert=build.create();
            alert.show();

        } catch (Exception e)
        {
            e.printStackTrace();
        }
        return password;
    }

    //ask user for type of report to be generated
    private void Create_report_alert() {

        try {
            final CharSequence[] items_tab = {"IMAGE", "PDF","DATA IN TEXT FILE"}; // Tab allows the user to save file in 3 formats(image, pdf, and dat)
            final CharSequence[] items_small = {"PDF","DATA IN TEXT FILE"};// Small mobile stores only pdf and text file

            send_report=true;
            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("Save Data as...");
            builder.setCancelable(false);

            if(RAM_SIZE<70) //Why 70? SNB
            {
                builder.setMultiChoiceItems(items_tab, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int item, boolean ischecked) {
                        switch(item)
                        {
                            case 0:
                                create_image=true;
                                break;
                            case 1:
                                create_pdf=true;
                                ask_for_pswd();
                                break;
                            case 2:
                                create_text=true;
                                break;
                        }
                    }
                });
            }
            else if(RAM_SIZE>=70 && RAM_SIZE<=700) //SNB: Why 70 to 700?
            {
                builder.setMultiChoiceItems(items_small, null, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int item, boolean ischecked) {
                        switch(item)
                        {
                            case 0:
                                create_pdf=true;
                                ask_for_pswd();
                                break;
                            case 1:
                                create_text=true;
                                break;
                        }
                    }
                });
            }
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id)
                {
                    if(!create_image && !create_pdf && !create_data && !create_text)
                    {
                        Toast.makeText(getApplicationContext(), "please select atleast one report type for report generation.", Toast.LENGTH_SHORT).show();
                        Create_report_alert();
                    }
                    else
                    {
                        if(!Patient_Info.data_entered && !load_existing_file)
                        {
                            Toast.makeText(getApplicationContext(), "Please enter the patient information for generating report.", Toast.LENGTH_SHORT).show();
                            onOptionsItemSelected(add_patient_info);
                        }
                        else if(Patient_Info.data_entered && !load_existing_file )
                        {
                            //demo_menu.setVisible(false);
                            //onOptionsItemSelected(add_patient_info);
                        }
                        Message report= mHandler.obtainMessage(GENERATE_REPORT);
                        mHandler.sendMessage(report);
                    }
                }
            });

            AlertDialog alert = builder.create();
            alert.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //redraw the screen view
    public void repaint()
    {
        if(menu_clicked)
        {
            if (pv.bitmap != null)
                pv.cnvs.drawBitmap(pv.bitmap, 0, 0, pv.paint);

            pv.bitmap.eraseColor(Color.TRANSPARENT);
            pv.cnvs.drawRGB(0,0,0);
            pv.drawGridLines(pv.cnvs);
            pv.print_text_on_canvas(paintView.page);
            menu_clicked=false;
        }

        if(!next_and_prev_clicked)
        {
            paintView.plot_on = false;
            pv.initialise_array();
        }
        else
            paintView.plot_on=true;


        if(paintView.generate_report_opt /*&& stop==true*/)
            gen_report_menu.setVisible(true);
        else
            gen_report_menu.setVisible(false);

        //set_title(mode,false);
        set_title(mode, paintView.Auto);
        //pv.invalidate();
        paintView.disp_count=0;
        paintView.chk_disp_count=0;
        paintView.ADS_samples_count=0;
        paintView.check_data_count=0;
        paintView.batt_count=0;
        next_and_prev_clicked=false;
    }

    //handler receives messages and carries out the specified action
    //@SuppressWarnings("deprecation")
    public final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case START_DATA_ACQ:
                    set_empty_arry();
                    if(strcounter==0)
                    {
                        try
                        {
                            while(instrm.available()>0)
                            {
                                instrm.read();
                            }
                            switch_to_gain();
                            set_filter(paintView.filter_state);
                            if(pv.debug_mode)
                                outstrm.write('U');//for debug mode
                            if(mode == 1)			// Test Channels
                            {
                                outstrm.write('T');
                            }
                            else
                            {
                                outstrm.write('A');
                            }// Take ECG

                            Toast.makeText(getApplicationContext(), "Initialising please wait...", Toast.LENGTH_SHORT).show();
                            pv.init_done=false;
                            pv.array_full=false;
                            pv.report_count=0;
                            pv.echo.start();
                            strcounter++;
                        }
                        catch (IOException e)
                        {
                            System.out.println("START_DATA error="+e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    else
                    {
                        try
                        {
                            switch_to_gain();
                            set_filter(paintView.filter_state);
                            if(mode == 1)			// Test Channels
                            {
                                outstrm.write('T');
                                if(paintView.filter_state!=0)
                                    paintView.filter_state=0;
                            }
                            else 					// Take ECG
                                outstrm.write('A');

                            Toast.makeText(getApplicationContext(), "Acquiring data please wait...", Toast.LENGTH_SHORT).show();
                            pv.init_done=false;
                            pv.plot_done=false;
                            paintView.roll_over=false;
                            paintView.disp_count=0;
                            paintView.chk_disp_count=0;
                            paintView.ADS_samples_count=0;
                            paintView.check_data_count=0;
                            paintView.fill_count=0;
                            paintView.Max_Scaling_factor=0;
                            paintView.Min_Scaling_factor=0;
                            pv.report_count=0;
                            pv.onResume();
                        } catch (Exception e)
                        {
                            System.out.println("START_DATA error="+e.getMessage());
                            e.printStackTrace();
                        }
                    }
                    break;
                case STOP_DATA:
                    try
                    {
                        pv.onPause();
                        if(pv.report_count>0)
                        {
                            paintView.generate_report_opt=true;
                            pv.store_Array(paintView.ADS_samples_count);
                        }
                        else
                            Toast.makeText(getApplicationContext(), "Insufficient data for report generation.", Toast.LENGTH_SHORT).show();

                        if(paintView.generate_report_opt)
                            gen_report_menu.setVisible(true);

                        pv.last_position(paintView.disp_count);
                    }
                    catch (Exception e)
                    {
                        System.out.println("STOP_DATA error="+e.getMessage());
                        e.printStackTrace();
                    }
                    break;
                case NEW_PATIENT:
                    try
                    {
                        start_menu.setVisible(true);
                        stop_menu.setVisible(false);
                        mode_menu.setVisible(true);
                        filter_menu.setVisible(true);
                        gain_menu.setVisible(true);
                        gen_report_menu.setVisible(false);
                        send_menu.setVisible(false);
                        new_patient.setVisible(false);
                        pv.invalidate();
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                        System.out.println("error in NEW_PATIENT"+e.getMessage());
                    }
                    break;
                case GENERATE_REPORT:
                    try
                    {
                        mode_menu.setVisible(false);
                        filter_menu.setVisible(false);
                        start_menu.setVisible(false);
                        stop_menu.setVisible(false);
                        gain_menu.setVisible(false);
                        new_patient.setVisible(true);
                        repeat_acq.setVisible(true);
                        display_alert();
                        if(send_report)
                            send_menu.setVisible(true);
                    }catch(Exception e)
                    {
                        System.out.println("error in GENERATE_REPORT"+e.getMessage());
                    }
                    break;
                case LOAD_DATA:
                    onOptionsItemSelected(load_menu);
                    break;
                case MESSAGE_COMMUNICATION:
                    AlertDialog.Builder build2 = new AlertDialog.Builder(MainActivity.this);
                    build2.setTitle("Communication Error");
                    build2.setMessage("Communication Error! \n Try Again!");
                    build2.setCancelable(false);
                    build2.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            System.exit(0);
                        }
                    });
                    AlertDialog alert2 = build2.create();
                    alert2.show();
                    break;
            }
        }
    };
    //sends the characters according to the selected gain
    private void switch_to_gain()
    {
        try
        {
            switch(iGain)
            {
                case 1:
                    outstrm.write('B'); //System.out.println("igain method="+iGain);
                    break;
                case 2:
                    outstrm.write('C'); //System.out.println("igain method="+iGain);
                    break;
                case 3:
                    outstrm.write('D'); //System.out.println("igain method="+iGain);
                    break;
                case 4:
                    outstrm.write('E');//System.out.println("igain method="+iGain);
                    break;
                case 6:
                    outstrm.write('F');//System.out.println("igain method="+iGain);
                    break;
                case 8:
                    outstrm.write('G');//System.out.println("igain method="+iGain);
                    break;
                case 12:
                    outstrm.write('H');//System.out.println("igain method="+iGain);
                    break;
            }// switch
        }
        catch(Exception e)
        {
            System.out.println("Exception in gain=>"+e.getMessage());
        }
    }

    protected void set_filter(int filter) {
        try {
            switch(filter)
            {
                case 0:		//System.out.println("0 FIR_B_150 raw");
                    break;		// 0 to 150 Hz (RAW)
                case 1:
                    //for (int i = 0; i <=200; i++) {
                        pv.FIR_B=pv.FIR_B_150;
                    //}
                    break;		// 0 to 150 Hz (-50Hz)
                case 2:
                    //for (int i = 0; i <=200; i++) {
                        pv.FIR_B=pv.FIR_B_5_40;
                    //}
                    break;		// 5 to 40 Hz
                case 3:
                    //for (int i = 0; i <= 200; i++) {
                        pv.FIR_B=pv.FIR_B_40;
                    //}
                    break;		// 0 to 40 Hz
                case 4:
                    //for (int i = 0; i <= 200; i++) {
                        pv.FIR_B=pv.FIR_B_5_25;
                    //}
                    break;		// 5 to 25 Hz
                case 5:
                    //for (int i = 0; i <= 200; i++) {
                        pv.FIR_B=pv.FIR_B_25;
                    //}
                    break;		// 0 to 25 Hz
            }// switch
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //to change the context from main Activity to paint view class
    public void initialization_paintView()
    {
        try
        {
            int status_bar_height=getStatusBarHeight();//
            int titlebar_height=(getTitleBarHeight()-status_bar_height);//titlebar_height is the total height of the screen including the titlebar
            //create paintview object......
            pv=new paintView(titlebar_height,status_bar_height,iScreenWidth,instrm,outstrm,MainActivity.this);
            pv.cnvs.drawRGB(0,0,0);
            setTitleColor(Color.BLACK);
            set_title(mode, paintView.Auto);
            //total pages to be displayed for different lead selected
            total_pages=paintView.CH_NUM/no_of_lead_to_display;
            pv.drawGridLines(pv.cnvs);
            pv.print_text_on_canvas(paintView.page);
            setContentView(pv);
            pv.requestFocus();
            System.out.println("");
            switch_to_gain();
            if(!load_existing_file)
            {
                outstrm.write('S');//to stop data ACQ when connection was break
                outstrm.write('R');
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            System.out.println("Error"+e.getMessage());
        }
    }

    @SuppressWarnings("static-access")
    protected void set_empty_arry()
    {
        pv.initialise_array();
        paintView.disp_count=0;
        paintView.chk_disp_count=0;
        paintView.ADS_samples_count=0;
        paintView.check_data_count=0;
        paintView.fill_count=0;
        pv.batt_count=0;
        paintView.generate_report_opt=false;
    }

    //returns the height of the title bar
    public int getTitleBarHeight()
    {
        int viewtop=getWindow().findViewById(Window.ID_ANDROID_CONTENT).getTop();
        f_size = getResources().getDimensionPixelSize(R.dimen.font_size);
        return (viewtop);
    }

    //this method finds the height of status bar
    public int getStatusBarHeight()
    {
        Rect r=new Rect();
        Window w=getWindow();
        w.getDecorView().getWindowVisibleDisplayFrame(r);
        return r.top;
    }

    private void init_patient_info() {
        try {
            paintView.Pass_On_date="";
            paintView.Pass_On_name="";
            paintView.Pass_On_chno="";
            paintView.Pass_On_dob="";
            paintView.Pass_On_age="";
            paintView.Pass_On_gen="";
            paintView.Pass_On_ht="";
            paintView.Pass_On_wt="";
            paintView.Pass_On_medi="";
            paintView.Pass_On_BP="";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void Select_lead_display() {
        try {
            Toast.makeText(getApplicationContext(),"Lead_dispaly() Invoked",Toast.LENGTH_LONG).show();
            final CharSequence[] items = {"1 lead", "3 leads","6 leads","12 leads"};

            AlertDialog.Builder builder=new AlertDialog.Builder(this);
            builder.setTitle("Select number of leads to display.");
            builder.setCancelable(false);
            builder.setSingleChoiceItems(items, selectedPosition, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface arg0, int item) {
                    int chan_menu_selection[] = {1,3,6,12};

                    no_of_lead_to_display = chan_menu_selection[item];
                }
            });
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            selectedPosition = ((AlertDialog)dialog).getListView().getCheckedItemPosition();
                            store_lead_number();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void read_lead_number() {
        try {
            int chan_menu_selection[] = {1,3,6,12};

            File path = new File(settingsFolder, "selected_lead_number.dat" );

            if(!path.exists())
            {
                store_lead_number();
            }
            RandomAccessFile raf=new RandomAccessFile(path,"rw");
            selectedPosition=raf.readInt();
            raf.close();

            no_of_lead_to_display = chan_menu_selection[selectedPosition];
            Log.i(TAG, "number of leads to display:" + no_of_lead_to_display);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void store_lead_number() {
        try {
            File root = new File(settingsFolder);
            if (!root.isDirectory())
                root.mkdirs();

            File ECG_FILE_NAME = new File(root,"selected_lead_number"+".dat");
            RandomAccessFile raf=new RandomAccessFile(ECG_FILE_NAME,"rw");

            if(lead_pressed)
                raf.writeInt(selectedPosition);
            else
                raf.writeInt(3);	//selected lead will be 12 for the first time, array position for 12 lead is 3

            raf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //exit the application on pressing of back button
    @Override
    public void onBackPressed()
    {
        try {
            AlertDialog.Builder build=new AlertDialog.Builder(MainActivity.this);
            build.setMessage("Do you want to exit the application?");
            build.setPositiveButton("YES", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int arg1)
                {
                    MainActivity.this.finish();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
