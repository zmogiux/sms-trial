package dtd.phs.sms;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import dtd.phs.sms.data.DataCenter;
import dtd.phs.sms.data.DataWrapper;
import dtd.phs.sms.data.IDataGetter;
import dtd.phs.sms.data.IListFactory;
import dtd.phs.sms.data.MessagesFactory;
import dtd.phs.sms.data.entities.MessageItem;
import dtd.phs.sms.data.entities.SMSItem;
import dtd.phs.sms.message_center.NormalSMSReceiver;
import dtd.phs.sms.message_center.Postman;
import dtd.phs.sms.message_center.SendMessageListener;
import dtd.phs.sms.util.Helpers;
import dtd.phs.sms.util.Logger;


public class ShowConversation
extends PHS_SMSActivity
implements IDataGetter, SendMessageListener
{


	
	private final class BeingSentMessageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			listview.post(new Runnable() {
				@Override
				public void run() {
					requestMessages();
				}
			});
		}
	}

	public class IncomingMessageReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Helpers.runAfterWaiting(new Runnable() {
				@Override
				public void run() {
					listview.post(new Runnable() {
						@Override
						public void run() {
							requestMessages();							
						}
					});
				}
			},NormalSMSReceiver.WAIT_BEFORE_REFRESH);					
		}

	}


	public static final String INPUT_BUNDLE = "input_bundle";
	private static final int DATA_FRAME = 0;
	private static final int WAITING_FRAME = 1;

	private int threadId;
	private ImageView ivAvatar;
	private TextView tvContactName;
	private TextView tvNumber;
	private ListView listview;
	private EditText etMessage;
	private TextView tvCount;
	private Button btSend;
	private Button btAttach;

	private IListFactory adapterFactory;
	private BaseAdapter adapter;
	private String passedContactNumber;
	private IncomingMessageReceiver incomingMessageReceiver;
	private Postman postman;
	private BeingSentMessageReceiver beingSentMessageReceiver;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_conversation);
		adapterFactory = new MessagesFactory();
		extractInputData();
		bindViews();
		postman = Postman.getInstance(getApplicationContext());
	}

	@Override
	protected void onResume() {	
		super.onResume();
		requestMessages();
		
		incomingMessageReceiver = new IncomingMessageReceiver();
		registerReceiver(incomingMessageReceiver, new IntentFilter(NormalSMSReceiver.GENERAL_MESSAGE_RECEIVED));
		
		beingSentMessageReceiver = new BeingSentMessageReceiver();
		registerReceiver(beingSentMessageReceiver, new IntentFilter(Postman.GENERAL_BEING_SENT_EVENT));
		

		postman.startInternetPostman();
	}

	@Override
	protected void onPause() {
		unregisterReceiver(incomingMessageReceiver);
		unregisterReceiver(beingSentMessageReceiver);
		super.onPause();
	}
	
	private void extractInputData() {
		Intent srcIntent = getIntent();
		Bundle bundleExtra = srcIntent.getBundleExtra(INPUT_BUNDLE);
		if ( bundleExtra == null ) {
			showInbox();
		} else {
			threadId = bundleExtra.getInt(SMSItem.THREAD_ID);
			passedContactNumber = bundleExtra.getString(SMSItem.ADDRESS);
		}
	}

	private void showInbox() {
		Intent i = new Intent( this,ShowInbox.class);
		startActivity(i);
		finish();
	}

	private void bindViews() {
		//Top bar
		ivAvatar = (ImageView) findViewById(R.id.ivAvatar);
		tvContactName = (TextView) findViewById(R.id.tvContactName);	
		tvNumber = (TextView) findViewById(R.id.tvNumber);		
		tvNumber.setText(passedContactNumber);


		//listview
		listview = (ListView) findViewById(R.id.list);

		//Bottom
		etMessage = (EditText) findViewById(R.id.etMessage);
		tvCount = (TextView) findViewById(R.id.tvCount);
		btSend = (Button) findViewById(R.id.btSend);
		btAttach = (Button) findViewById(R.id.btAttach);
		
		btSend.setOnClickListener( new OnButtonSendClickListener(etMessage) );
		
	}



	private void requestMessages() {
		showOnlyView(WAITING_FRAME);
		DataCenter.requestMessagesForThread(threadId, this, getApplicationContext());
	}

	private void showOnlyView(int id) {
		FrameLayout mainFrames = (FrameLayout) findViewById(R.id.main_frames);
		for(int i = 0 ; i < mainFrames.getChildCount() ; i++) {
			View v = mainFrames.getChildAt(i);
			if ( i == id ) 
				v.setVisibility(View.VISIBLE);
			else v.setVisibility(View.GONE);
		}
	}

	public void onGetDataFailed(Exception exception) {
		Logger.logException(exception);
	}

	public void onGetDataSuccess(DataWrapper wrapper) {
		updateListView(wrapper);
		updateContactsInfo(wrapper.getData());

	}



	private void updateListView(DataWrapper wrapper) {
		adapter = adapterFactory.createAdapter(wrapper.getData(), getApplicationContext());
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(adapterFactory.createOnItemClickListener(this, adapter));
		listview.setSelection(adapter.getCount());
		showOnlyView(DATA_FRAME);
	}


	private void updateContactsInfo(final Object data) {
		//TODO: this shouldn't be here ! UI should have no knowledge about inside of database
		//		ThreadPools.getInstance().add(new Runnable() {
		//			
		//			public void run() {
		//				SMSList messages = (SMSList) data;
		//				loadContactsFromMessages( messages );
		//			}
		//
		//			private void loadContactsFromMessages(SMSList messages) {
		//				List<Long> personIds = getAllPersonIds(messages);				
		//				ArrayList<String> contactIds = getContactIds(personIds);
		//				if ( contactIds.size() == 0 ) {
		//					Logger.logInfo("No contacts !");
		//				} else if ( contactIds.size() == 1 ) {
		//					
		//				} else {
		//					
		//				}
		//				
		//				
		//			}
		//
		//			private List<Long> getAllPersonIds(SMSList messages) {
		//				List<Long> ids = new ArrayList<Long>();
		//				//TODO: what happens with thread contains only sent sms (no contact id)
		//				//Solution: take the telephone number and search for the contact, dont take the person id / contact id
		//				for(SMSItem item : messages) {
		//					long personId = item.getPerson();
		//					if ( personId > 0 ) {
		//						ids.add(personId);
		//					}
		//				}
		//				return ids;
		//			}
		//
		//			private ArrayList<String> getContactIds(List<Long> personIds) {
		//				ArrayList<String> contactIds = new ArrayList<String>();
		//				for(long personId : personIds) {
		//					String contactId = SMSItem.getContactID(personId);
		//					boolean dup = false;
		//					for(int i = 0 ; i < contactIds.size(); i++)
		//						if ( contactIds.get(i).equals(contactId) ) {
		//							dup = true;
		//							contactIds.set(i, contactId);
		//							break;
		//						} 
		//					if ( ! dup ) contactIds.add(contactId);
		//				}
		//				return contactIds;
		//			}
		//		});

	}


	public class OnButtonSendClickListener implements OnClickListener {
		private EditText etMessage;
		public OnButtonSendClickListener(EditText etMessage) {
			this.etMessage = etMessage;
		}

		@Override
		public void onClick(View v) {
			String message = etMessage.getText().toString();
			if ( message != null && message.length() > 0 ) {
				MessageItem mess = new MessageItem();
				//TODO: multiple numbers
				String number = tvNumber.getText().toString();
				mess.setNumber(number);
				mess.setContent(message);
				mess.setId(""+System.currentTimeMillis());
				etMessage.setText("");
				postman.sendMessage(mess, ShowConversation.this, false);
				
			}
		}
	}


	@Override
	public void onSendFailed(Object data) {
		//TODO: later
		Helpers.showToast(getApplicationContext(),"Send failed !");
	}

	@Override
	public void onSendSuccces(Object data) {
		// TODO Auto-generated method stub
		// This one is published multiple times for just one success message
		// why ? because there are alot receiver for "DELIVERED" - which is not nice ! 
		// In fact: wastful & wrong
		Helpers.showToast(getApplicationContext(),"Send success !");
		
	}

}
