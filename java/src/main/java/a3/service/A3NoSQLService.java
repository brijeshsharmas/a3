/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service;

import java.util.List;

import a3.model.nosql.NoSQLItem;
import a3.model.request.A3Response;
import a3.model.request.A3Responses;

/**
 * @author <b>Brijesh Sharma</b><br>
 * This interface extends {@link A3DBService} and add more contract definition relevant to cloud storage service of type 
 * No-SQL databases. A few example of No-SQL databases by different cloud providers are<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;1. DynamoDB by AWS<br> 
 * &nbsp;&nbsp;&nbsp;&nbsp;2. DocumentDB by Mircrosoft Azure for document based data storage service
 *
 */
public interface A3NoSQLService extends A3DBService {
	
	public static final String ABORTED = "ABORTED";
	public static final String OUTAGE = "OUTAGE";
	public static final String DATA_ISSUE = "DATA_ISSUE";
	
	/**
	 * The caller can send multiple commit requests in a List. The method must implement below requirements<br> 
	 * <b>Requirement 1</b> - Ensure best possible effort made to commit all items in the list.<br>
	 * <b>Requirement 2</b> - If commits are exceeding provisioned throughput capacity, then improvise all commits with lowest 
	 * possible latency<br> 
	 * <b>Requirement 3</b> - Abort outstanding items commit for below scenarios<br>
	 * &nbsp;&nbsp;&nbsp;&nbsp;3.1 Cloud No-SQL service is down, incorrect configuration provided, network outage, etc.
	 * 		The implementation must try at-least 5 attempts before determining if service is down or there is a network outage. Abort entire item set and return 
	 * 		status = "ABORTED", reason = "OUTAGE", description = "Outage Description"<br> 
	 * &nbsp;&nbsp;&nbsp;&nbsp;3.2 Data Issues or Incorrect Commit Query. For 10 consecutive commit failures, abort outstanding items commit and return 
	 * response object with status = "ABORTED", reason = "DATA", description = <<Data Issue Description>>
	 * entries
	 * @param listItems
	 * @return {@link A3Responses} containing commit status of each {@link NoSQLItem} item.
	 */
	public A3Responses add(List<NoSQLItem> listItems);
	public A3Responses update(List<NoSQLItem> listItems);
	
	/**
	 * The implementation method to ensure best possible effort made to commit request item and return commit status in {@link A3Response}
	 * @param item
	 */
	public A3Response add(NoSQLItem item);
	public A3Response update(NoSQLItem item);
	public A3Response delete(NoSQLItem item);
	
	/**
	 * For a given CloudNOSQLDBRequestEntry, return CloudNoSQLDBResponse filled with zero or more CloudNoSQLDBResponseEntry instances
	 * @param requestItem
	 * @return
	 */
	public List<NoSQLItem> get(NoSQLItem requestItem);
	public List<NoSQLItem> getByIndex(String name, Object value);
	
	/**This method return single {@link NoSQLItem} item represented by given primary keys wherein sort key is optional value*/
	public NoSQLItem getByPrimaryKeysValue(Object partitionKeyValue, Object sortKeyValue);
	
	/**Return all items from NOSQL Cloud Service*/
	public List<NoSQLItem> getAll();
	public NoSQLItem getAllAttributes();

}
