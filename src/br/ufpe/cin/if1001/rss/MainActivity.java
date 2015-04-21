package br.ufpe.cin.if1001.rss;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		if (savedInstanceState == null) {
			getFragmentManager().beginTransaction()
					.add(R.id.container, new PlaceholderFragment()).commit();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
 			return true;
		}
		return super.onOptionsItemSelected(item);
	} 

	/**
	 * A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		private ListView mRssFeed;

		public PlaceholderFragment() {
		}

		@Override
		public void onStart() {
			super.onStart();
			//pode passar varias URLs, mas so vai pegar a primeira no codigo de doInBackground() abaixo
			new CarregarFeed().execute("http://g1.globo.com/dynamo/rss2.xml","http://rss.cnn.com/rss/edition.rss");

		}

		private class CarregarFeed extends AsyncTask<String, Void, List<ItemRSS>> {

			protected List<ItemRSS> doInBackground(String... params) {
				String result = "";
				List<ItemRSS> items = null;
				InputStream in = null;
				try {
					result = getRssFeed(params[0]);
					items = parseRSS(result);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (XmlPullParserException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
				return items;
			}
			
			private String getRssFeed(String feed) throws IOException {
                InputStream in = null;
                String rssFeed = null;
                try {
                    URL url = new URL(feed);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    in = conn.getInputStream();
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    for (int count; (count = in.read(buffer)) != -1; ) {
                        out.write(buffer, 0, count);
                    }
                    byte[] response = out.toByteArray();
                    rssFeed = new String(response, "UTF-8");
                } finally {
                    if (in != null) {
                        in.close();
                    }
                }
                return rssFeed;
            }
					
			private List<ItemRSS> parseRSS(String rssFeed)
					throws XmlPullParserException, IOException {

				// pegando instancia da XmlPullParserFactory [singleton]
				XmlPullParserFactory factory = XmlPullParserFactory
						.newInstance();
				// criando novo objeto do tipo XmlPullParser
				XmlPullParser parser = factory.newPullParser();
				// Definindo a entrada do nosso parser - argumento passado como
				// parametro
				parser.setInput(new StringReader(rssFeed));
				// Definindo retorno
				List<ItemRSS> items = new ArrayList<ItemRSS>();
				
				ItemRSS item = new ItemRSS();
				String text = "";
				String tagname = "";
				
				int eventType = parser.getEventType();
				
				while (eventType != XmlPullParser.END_DOCUMENT) {
					tagname = parser.getName();
					switch(eventType) {
						case XmlPullParser.START_TAG:
							// se for um novo item, inicialize um objeto
							if (tagname.equalsIgnoreCase("item")) {
								item = new ItemRSS();
							}
							break;
						case XmlPullParser.TEXT:
							// se for texto, salve-o
							text = parser.getText();
							break;
						case XmlPullParser.END_TAG:
							if (tagname.equalsIgnoreCase("item")) {
								// se tiver terminando um item, adicione ele no retorno
								items.add(item);
							} else if (tagname.equalsIgnoreCase("title")) {
								item.title = text;
							} else if (tagname.equalsIgnoreCase("description")) {
								item.description = text;
							} else if (tagname.equalsIgnoreCase("pubDate")) {
								item.pubDate = text;
							} else if (tagname.equalsIgnoreCase("link")) {
								item.link = text;
							}
							break;
						default:
							break;
					}
					eventType = parser.next();
				}
				return items;
			}

			@Override
			protected void onPreExecute() {
				Toast.makeText(getActivity(), "carregando...", Toast.LENGTH_SHORT).show();
			}

			protected void onPostExecute(List<ItemRSS> result) {
				if (result != null) {
					
					ItemRSSAdapter adapter = new ItemRSSAdapter(getActivity(), result);
					mRssFeed.setAdapter(adapter);
					mRssFeed.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
							//cuidado com esse cast, fazer apenas se tiver certeza do tipo
							ItemRSSAdapter adapter = (ItemRSSAdapter) parent.getAdapter();
							String link = adapter.getItem(position).link;
							// chama o intent de abrir o browser
							startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
							//outra forma, pegando o conteudo exibido no item clicado da list view
							//Toast.makeText(getActivity(), ((TextView) view).getText(), Toast.LENGTH_SHORT).show();
						}
						
					});
				}
			}

		}

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View rootView = inflater.inflate(R.layout.fragment_main, container, false);
			mRssFeed = (ListView) rootView.findViewById(R.id.rss_feed);
			return rootView;
		}
	}
}
