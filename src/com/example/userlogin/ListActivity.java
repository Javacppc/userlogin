package com.example.userlogin;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;


public class ListActivity extends Activity {
	private TextView secondTxt;
	private Button m_returnBtn;
	private Button m_search;
	private ListView m_lv;
	private EditText m_searPa;
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if (msg.what == 0) {
				ArrayList<String> str = msg.getData().getStringArrayList("str");
				m_lv.setAdapter(new ArrayAdapter<String>(ListActivity.this,android.R.layout.simple_expandable_list_item_1,str));
				ListActivity.this.setContentView(m_lv);
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);
	    Intent intent=getIntent();//getIntent将该项目中包含的原始intent检索出来，将检索出来的intent赋值给一个Intent类型的变量intent
	    Bundle bundle=intent.getExtras();//.getExtras()得到intent所附带的额外数据
	    ArrayList<String> str=bundle.getStringArrayList("str");//getString()返回指定key的值
	    secondTxt=(TextView)findViewById(R.id.transform);//用TextView显示值
	    m_lv = new ListView(this);
	    secondTxt.setText(str.get(0) + " " + str.get(1));
	    m_returnBtn = (Button) findViewById(R.id.returnbtn);
	    m_searPa = (EditText) super.findViewById(R.id.queryParameter);
	    m_search = (Button) super.findViewById(R.id.searchButton);
	    m_search.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							ArrayList<HashMap<String,Object>> list = sendDataToServer();
							ArrayList<String> str = new ArrayList<String>();
							for (HashMap<String, Object> l : list) {
								String a = (String) l.get("number");
								String b = (String) l.get("pclass");
								String c = (String) l.get("contact");
								str.add(a + " " + b + " " + c);
							}
							
							Bundle bundle = new Bundle();
							bundle.putStringArrayList("str", str);
							Message msg = new Message();
							msg.what = 0;
							msg.setData(bundle);
							handler.sendMessage(msg);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
			private ArrayList<HashMap<String,Object>> sendDataToServer() throws Exception {
				HttpClient client = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost("http://192.168.1.102:8424/partysys/list.action");
				BasicHttpParams params = new BasicHttpParams();
				List<NameValuePair> namevaluePair = new ArrayList<NameValuePair>();
				JSONObject obj = new JSONObject();
				obj.put("query", m_searPa.getText().toString());
				String str = obj.toString();
				namevaluePair.add(new BasicNameValuePair("list", URLEncoder.encode(str, "utf-8")));
				httpPost.setParams(params);
				httpPost.setEntity(new UrlEncodedFormEntity(namevaluePair));
				HttpResponse response = client.execute(httpPost);
				StatusLine statusLine = response.getStatusLine();
				//if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity entity = response.getEntity();
					String re = readByteFromResponse(entity.getContent());
					System.out.println(re);
					ArrayList<HashMap<String,Object>> list = analyzeJson(re);
					System.out.println(list == null);
					return list;
			}
			private ArrayList<HashMap<String, Object>> analyzeJson(String readByteFromResponse) throws JSONException {
				JSONArray jsonArray = null;
				// 初始化list数组对象
				ArrayList<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
				jsonArray = new JSONArray(readByteFromResponse);
				for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
			         // 初始化map数组对象
	             HashMap<String, Object> map = new HashMap<String, Object>();
	             map.put("number", jsonObject.getString("number"));
	             map.put("pclass", jsonObject.getString("pclass"));
	             map.put("contact", jsonObject.getString("contact"));
	             list.add(map);
				}
				
				return list;
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
	    m_returnBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent intent = new Intent();
				intent.putExtra("key", "重新注册！！");
				setResult(2, intent);
				finish();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.second, menu);
		return true;
	}
}
