package com.SRA;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	Button selectMusic;
	Button selectMusic2;
	Button StartRun;
	TextView tv1;
	TextView tv2;
	EditText et1;
	EditText et2;

	String lovedSongPath = "";
	String hatedSongPath = "";
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectMusic=(Button)this.findViewById(R.id.selectMusic);
        selectMusic2=(Button)this.findViewById(R.id.button1);
        StartRun=(Button)this.findViewById(R.id.button2);
        tv1=(TextView)this.findViewById(R.id.textView6);
        tv2=(TextView)this.findViewById(R.id.textView8);
        et1=(EditText)this.findViewById(R.id.editText1);
        et2=(EditText)this.findViewById(R.id.editText2);
        selectMusic.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent it=new Intent();
				it.setClass(MainActivity.this, MenuAddGridView.class);
				startActivityForResult(it,1);
			}
		});
        selectMusic2.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent it=new Intent();
				it.setClass(MainActivity.this, MenuAddGridView.class);
				startActivityForResult(it,2);
			}
		});
        
       StartRun.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String arg1=et1.getText().toString();
				String arg2=et2.getText().toString();
				double totalmiles;
				double expectedSpeed;
				try{
					totalmiles=Double.parseDouble(arg1);
					if(totalmiles <= 0)
					{
						Toast.makeText(MainActivity.this, "Check Your Input in TOTALMILES",Toast.LENGTH_SHORT).show();
						return;
					}

					try{
						expectedSpeed=Double.parseDouble(arg2);
						if( expectedSpeed <= 0 )
						{
							Toast.makeText(MainActivity.this, "Check Your Input in EXPECTEDSPEED",Toast.LENGTH_SHORT).show();
							return;
						}
						
						// For debug
						/*Intent it=new Intent();
						it.putExtra("totalmiles", totalmiles);
						it.putExtra("expectedspeed", expectedSpeed);
						it.putExtra("lovedSong", lovedSongPath);
						it.putExtra("hatedSong",hatedSongPath);
						it.setClass(MainActivity.this, RunActivity.class);
						startActivity(it);
						*/
						if(!lovedSongPath.equals("")){
							if(!hatedSongPath.equals("")){
								Intent it=new Intent();
								it.putExtra("totalmiles", totalmiles);
								it.putExtra("expectedspeed", expectedSpeed);
								it.putExtra("lovedSong", lovedSongPath);
								it.putExtra("hatedSong",hatedSongPath);
								it.setClass(MainActivity.this, RunActivity.class);
								startActivity(it);
							}else{
								Toast.makeText(MainActivity.this, "Select a Song you hate first", Toast.LENGTH_SHORT).show();
							}
						}else{
							Toast.makeText(MainActivity.this, "Select a Song you love first",Toast.LENGTH_SHORT).show();
						}
					}catch(Exception ex){
						Toast.makeText(MainActivity.this, "Check Your Input in EXPECTEDSPEED",Toast.LENGTH_SHORT).show();
					}
				}catch(Exception ex){
					Toast.makeText(MainActivity.this, "Check Your Input in TOTALMILES",Toast.LENGTH_SHORT).show();
				}
				
			}
		});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }
    
    /**
    * 处理 Activity 收到到请求数据结果
    */
    @Override
    public void  onActivityResult(int requestCode, int resultCode, Intent data) {
            // 用 Toast 打印返回结果
            //Toast.makeText(this, data.getStringExtra("musicResult"), 1).show();
    	if(requestCode==1){
    	    try{
    	    lovedSongPath=data.getStringExtra("musicResult");
    	    String[] arg=lovedSongPath.split("/");
    	    if(arg.length>1){
    	    String lovedSongName=arg[arg.length-1];
    	    tv1.setText(lovedSongName);
    	    }}
    	    catch(Exception ex){
    	    	
    	    }
    	}else{
    		try{
        	    hatedSongPath=data.getStringExtra("musicResult");
        	    String[] arg=hatedSongPath.split("/");
        	    if(arg.length>1){
        	    String hatedSongName=arg[arg.length-1];
        	    tv2.setText(hatedSongName);
        	    }}
        	    catch(Exception ex){
        	    	
        	    }
    	}
            super.onActivityResult(requestCode, resultCode, data);
    }
}
