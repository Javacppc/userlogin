package com.example.userlogin;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class RegisterActivity extends Activity {
	private static final Integer SUCC = 1;
	private static final Integer FAIL = 0;
	private EditText m_username;
	private EditText m_password;
	private EditText m_contact;
	private Button m_register;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == SUCC) {
				String red = msg.getData().getString("str");
				Toast.makeText(RegisterActivity.this, red, Toast.LENGTH_SHORT).show();
			} else if (msg.what == FAIL) {
				Toast.makeText(RegisterActivity.this, "注册失败！", Toast.LENGTH_SHORT).show();
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);
		m_username = (EditText) super.findViewById(R.id.logintextperson);
		m_password = (EditText) super.findViewById(R.id.loginresult);
		m_contact = (EditText) super.findViewById(R.id.contactWay);
		m_register = (Button) super.findViewById(R.id.button1);
		m_register.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
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
			}
			/**
			 * 将数据传送到服务端
			 * @throws Exception
			 */
			private void sendDataToServer() throws Exception {
				HttpClient client = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost("http://192.168.1.102:8424/partysys/register.action");
				BasicHttpParams params = new BasicHttpParams();
				List<NameValuePair> namevaluePair = new ArrayList<NameValuePair>();
				JSONObject jo = new JSONObject();
				Partymember data = initData();
				jo.put("number", data.getNumber());
				jo.put("pclass", data.getPclass());
				jo.put("contact", data.getPhone());
				
				String str = jo.toString();
				namevaluePair.add(new BasicNameValuePair("register", URLEncoder.encode(str, "utf-8")));
				httpPost.setParams(params);
				httpPost.setEntity(new UrlEncodedFormEntity(namevaluePair));
				HttpResponse response = client.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				Bundle bundle  = new Bundle();
				Message meg = new Message();
				if (statusLine != null) {
					HttpEntity entity = response.getEntity();
					String red = readByteFromResponse(entity.getContent());
					System.out.println(red);
					red = new String(red.getBytes(),"utf-8");
					bundle.putString("str", red);
					meg.what = SUCC;
					meg.setData(bundle);
					handler.sendMessage(meg);
				} else {
					meg.what = FAIL;
					handler.sendMessage(meg);
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
			/**
			 * 封装数据
			 * @return
			 */
			private Partymember initData() {
				Partymember p = new Partymember();
				p.setPhone(m_contact.getText().toString());
				p.setPclass(m_password.getText().toString());
				p.setNumber(m_username.getText().toString());
				return p;
			}
		});
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
}


