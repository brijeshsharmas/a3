/**
 * 
 */
package a3.aws.util;

import static java.lang.System.out;
import static cloud.request.accelerator.CloudRequestAccelerator.Config.SERVICE_ARNS;

import java.util.List;
import java.util.Properties;

import a3.service.aws.AWSDynamoDBService;
import cloud.model.nosql.NoSQLItem;
/**
 * @author Brijesh Sharma
 *
 */
public class CopyDynamoDBTable {

	/**
	 * 
	 */
	public CopyDynamoDBTable() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length < 2) {
			out.println("Aboring As Number of Arguments Provided Are Less Than 2");
			return;
		}

		Properties props = new Properties();
		props.setProperty(SERVICE_ARNS.getText(), args[0]);
		out.println("Connecting To Source Table [" + args[0] + "]");
		AWSDynamoDBService sourceServce = new AWSDynamoDBService(props);
		out.println("Succesfully Connected To Source Table [" + args[0] + "]");
		
		props.setProperty(SERVICE_ARNS.getText(), args[1]);
		out.println("Connecting To Target Table [" + args[1] + "]");
		AWSDynamoDBService targetServce = new AWSDynamoDBService(props);
		out.println("Succesfully Connected To Target Table [" + args[0] + "]");
		
		List<NoSQLItem> sourceList = sourceServce.getAll();
		out.println("Succesfully Retrived Items From Source Table [" + args[0] + "]. Number Of Items Retrieved=" + sourceList.size());
		
		if(args.length > 2) {
			out.println("Checking Third Argument If Target Table [" + args[0] + "] Requiring Truncating Before Copy");
			boolean cleanTarget = Boolean.valueOf(args[2]);
			out.println("Truncating Target Table [" + args[0] + "]");
			if(cleanTarget) {
				targetServce.clearAll();
				out.println("Target Table [" + args[0] + "] Truncated Succesfully");
			}
			
		}
		targetServce.add(sourceList);
		out.println("Succesfully Copied Items To Target Table [" + args[0] + "]. Number Of Items Copied=" + sourceList.size());
		
	}

}
