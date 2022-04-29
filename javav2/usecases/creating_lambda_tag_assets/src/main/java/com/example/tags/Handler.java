/*
   Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
   SPDX-License-Identifier: Apache-2.0
*/

package com.example.tags;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Handler implements RequestHandler<Map<String, String>, String>
{
    @Override
    public String handleRequest(Map<String, String> event, Context context)
    {
	LambdaLogger logger = context.getLogger();
	long startTime = System.currentTimeMillis();
	String delFag = event.get("flag");
	String s3BucketName = event.get("s3bucketname");
	// String region = event.get("s3bucketname");
	logger.log("FLAG IS: " + delFag);
	S3Service s3Service = new S3Service();
	AnalyzePhotos photos = new AnalyzePhotos();

	String bucketName = s3BucketName;
	List<String> myKeys = s3Service.listBucketObjects(bucketName);
	logger.log(bucketName +" has "+myKeys.size()+" objects.");
	if (delFag.compareTo("true") == 0)
	{

	    // Create a List to store the data.
	    List<ArrayList<WorkItem>> myList = new ArrayList<>();

	    // loop through each element in the List and tag the assets.
	    for (String key : myKeys)
	    {

		byte[] keyData = s3Service.getObjectBytes(bucketName, key);
		logger.log("Rekognizing "+key);

		// Analyze the photo and return a list where each element is a WorkItem.
		ArrayList<WorkItem> item = photos.detectLabels(keyData, key);
		myList.add(item);
	    }
	    logger.log("Collated "+myList.size()+" items to add tags. Elapsed time:"+(System.currentTimeMillis()- startTime)+"ms.");
	    s3Service.tagAssets(context, myList, bucketName, startTime);
	    logger.log("All Assets in the bucket are tagged!");

	} else
	{

	    // Delete all object tags.
	    for (String key : myKeys)
	    {
		s3Service.deleteTagFromObject(bucketName, key);
		logger.log("All Assets in the bucket are deleted!");
	    }
	}
	return delFag;
    }
}
