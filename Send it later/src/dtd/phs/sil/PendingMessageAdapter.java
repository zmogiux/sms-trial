package dtd.phs.sil;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import dtd.phs.sil.entities.PendingMessageItem;
import dtd.phs.sil.entities.PendingMessagesList;
import dtd.phs.sil.utils.Helpers;

public class PendingMessageAdapter extends BaseAdapter {


	private static final int STUB_AVATAR = R.drawable.contact;
	private Context context;
	private PendingMessagesList messages;

	public PendingMessageAdapter(Context context, PendingMessagesList pendingMessagesList) {
		super();
		this.context = context;
		this.messages = pendingMessagesList;
	}

	@Override
	public int getCount() {
		return messages.size();
	}

	@Override
	public Object getItem(int position) {
		return messages.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}


	public class ViewHolder {
		public ImageView avatar;
		public TextView contact;
		public TextView content;
		public TextView status;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		ViewHolder holder;
		if ( view == null ) {
			view = Helpers.inflate(context,R.layout.pending_item);
			holder = new ViewHolder();
			createHolder(view, holder);
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		updateView(view,holder,messages.get(position));

		return view;
	}


	private void updateView(View view, ViewHolder holder, PendingMessageItem message) {

		holder.avatar.setImageResource(STUB_AVATAR);
		holder.contact.setText(message.getContact());
		holder.content.setText(message.getContent());

		updateNext(holder, message);
	}

	private void updateNext(ViewHolder holder, PendingMessageItem message) {

		String next = context.getResources().getString(R.string.next);
		String nextTime = message.getNextTime();
		if ( nextTime == null) nextTime = "TODO: remove this one !";
		holder.status.setText(next+": " + nextTime);
	}

	private void createHolder(View view, ViewHolder holder) {
		holder.avatar = (ImageView)view.findViewById(R.id.ivAvatar);
		holder.contact = (TextView) view.findViewById(R.id.tvContact);
		holder.content = (TextView) view.findViewById(R.id.tvContent);
		holder.status = (TextView) view.findViewById(R.id.tvStatus);
	}

	public void setMessages(PendingMessagesList list) {
		this.messages = list;
	}

	public PendingMessageItem getMessage(int position) {
		return messages.get(position);
	}

	public long getMessageRowId(int position) {
		return messages.get(position).getId();
	}

}