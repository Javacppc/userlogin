package com.example.userlogin;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Button m_button;
	private EditText m_username;
	private EditText m_password;
	private TextView m_return;
	private Button m_LoginButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		m_button = (Button) super.findViewById(R.id.button1);
		m_username = (EditText) super.findViewById(R.id.editText1);
		m_password = (EditText) super.findViewById(R.id.editText2);
		m_return = (TextView) super.findViewById(R.id.returndata);
		m_LoginButton = (Button) super.findViewById(R.id.LoginButton);
		m_button.setOnClickListener(new View.OnClickListener() {
			//422129197406094000
			@Override
			public void onClick(View v) {
				Intent intent=new Intent(MainActivity.this, RegisterActivity.class);
			    startActivity(intent);
			}
		});
		m_LoginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View view) {
				try {
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								sendDataToServer();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
				} catch (Exception e) {
					e.printStackTrace();
					System.out.println(e.getMessage());
				}
			}
			//422129197406094000
			/**
			 * 将数据传送到服务端
			 * @throws Exception
			 */
			private void sendDataToServer() throws Exception {
				
				HttpClient client = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost("http://192.168.1.102:8424/partysys/login.action");
				BasicHttpParams params = new BasicHttpParams();
				List<NameValuePair> namevaluePair = new ArrayList<NameValuePair>();
				JSONObject jo = new JSONObject();
				jo.put("username", m_username.getText().toString());
				jo.put("password", m_password.getText().toString());
				String str = jo.toString();
				
				namevaluePair.add(new BasicNameValuePair("login", URLEncoder.encode(str, "utf-8")));
				httpPost.setParams(params);
				httpPost.setEntity(new UrlEncodedFormEntity(namevaluePair));
				/*Toast.makeText(MainActivity.this, "发送的数据：\n" + str.toString(),  
	                    Toast.LENGTH_SHORT).show(); */ 
				
				HttpResponse response = client.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();
					String redata = readByteFromResponse(content);
					if ("true".equals(redata)) {
						Intent intent=new Intent();
					    intent.setClass(MainActivity.this, ListActivity.class);//从一个activity跳转到另一个activity
					    ArrayList<CharSequence> list = new ArrayList<CharSequence>();
					    list.add(m_username.getText().toString());
					    list.add(m_password.getText().toString());
					    intent.putCharSequenceArrayListExtra("str", list);
					    startActivityForResult(intent, 1);
					} else {
						Toast.makeText(MainActivity.this, "登录错误!", Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(MainActivity.this, "登录错误!", Toast.LENGTH_SHORT).show();
				}
			}
			/**
			 * 读取从服务端传送来的数据
			 * @param inputContent
			 * @return
			 * @throws Exception
			 */
			private String readByteFromResponse(InputStream inputContent) throws Exception {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				byte[] content = new byte[1024];
				int length = 0;
				while ((length = inputContent.read(content)) != -1) {
					out.write(content, 0, length);
				}
				String returnData = out.toString("utf-8");
				out.close();
				return returnData;
			}
		});
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1 && resultCode == 2) {
			String content = data.getStringExtra("key");
			m_return.setText(content);
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
