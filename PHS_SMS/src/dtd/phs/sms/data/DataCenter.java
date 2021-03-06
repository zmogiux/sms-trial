package dtd.phs.sms.data;

import android.content.Context;
import dtd.phs.sms.global.ThreadPools;


public class DataCenter {

	/**
	 * NOTE: in the release version, summaries will be cached 3 layers:
	 * 	1.Base layer: Raw data, just all messages from content provider
	 * 	2.Middle Layer: 
	 * 	- all summaries must be "cached" in a local database of the message app
	 * 	- So, create a content observer such that everytime, when the raw data changed, check whether it affects the local database
	 * 	3.Top layer:
	 * 	- Middle layer, but on Memory
	 * @param context 
	 * @param dateGetter
	 */
	public static void requestSummaries(IDataGetter dataGetter, Context context) {
		/**
		 * 1. Get all messages from sms-content provider
		 * 2. Group them up:
		 * 	#Pass 1: all mess with the same thread-id is in the same group (test: null thread-id ???)
		 * 	#Pass 2: merge "same" groups: { 2 group a,b are "same" means | they have 2 messages m in a, n in b such that:  m.person = n.person } 
		 */
		Request request = new GetSummariesRequest(dataGetter, context);
		ThreadPools.getInstance().add( request );

	}

	public static void requestMessagesForThread(int threadId, IDataGetter delegate, Context context) {
		Request request = new GetMessagesForThreadIdRequest(threadId,delegate,context);
		ThreadPools.getInstance().add(request);
		
	}

}
 