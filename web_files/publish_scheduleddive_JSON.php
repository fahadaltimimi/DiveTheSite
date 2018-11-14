<?php
 
/*
 * Following code will publish a single scheduled dive
 */
include('../variables/variables.php'); 

ob_start();
    
// array for JSON response
$response = array();
$response["SUCCESS"] = 0;

// Get parameters
$ScheduledDiveID = $_POST['SCHEDULED_DIVE_ID'];
$Title = $_POST['TITLE'];
$SubmitterID = $_POST['SUBMITTER_ID'];
$Timestamp = $_POST['TIMESTAMP'];
$Comment = $_POST['COMMENT'];

// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con);
mysql_query('SET CHARACTER SET utf8');

// To protect MySQL injection (more detail about MySQL injection)
$ScheduledDiveID = stripslashes($ScheduledDiveID);
$ScheduledDiveID = mysql_real_escape_string($ScheduledDiveID);

$Title = stripslashes($Title);
$Title = mysql_real_escape_string($Title);

$SubmitterID = stripslashes($SubmitterID);
$SubmitterID = mysql_real_escape_string($SubmitterID);

$Timestamp = stripslashes($Timestamp);
$Timestamp = mysql_real_escape_string($Timestamp);

$Comment = stripslashes($Comment);
$Comment = mysql_real_escape_string($Comment);

// If scheduled dive id was given, make sure it still exists before trying to update
if ($ScheduledDiveID != -1) {
	$sql = "SELECT SCHEDULED_DIVE_ID FROM tbscheduleddives WHERE SCHEDULED_DIVE_ID ='$ScheduledDiveID'";
	$result = mysql_query($sql, $con) or die(mysql_error());
	
	if (mysql_num_rows($result) == 0) {
		$ScheduledDiveID = -1;
	}
}

$newScheduledDive = 0;
if ($ScheduledDiveID == -1) {
	// New Scheduled Dive, insert new one		
	$sql = "INSERT INTO tbscheduleddives (TITLE, USER_ID, TIMESTAMP, COMMENT, LAST_MODIFIED, DATE_ADDED)
			VALUES (\"$Title\", \"$SubmitterID\", \"$Timestamp\", \"$Comment\", UTC_TIMESTAMP(), UTC_TIMESTAMP())";
								   
	mysql_query($sql, $con) or die(mysql_error());
	$ScheduledDiveID = mysql_insert_id(); 
	$newScheduledDive = 1;
} else {
	// ID already exists, update existing Scheduled Dive
	$sql = "UPDATE tbscheduleddives
			SET USER_ID = \"$SubmitterID\",
			    TITLE = \"$Title\",
				TIMESTAMP = \"$Timestamp\",
				COMMENT = \"$Comment\",
				LAST_MODIFIED = UTC_TIMESTAMP()
			WHERE SCHEDULED_DIVE_ID = '$ScheduledDiveID'";
			
	mysql_query($sql, $con) or die(mysql_error()); 
	
	// Delete scheduled dives existing sites, will be added
	$sql = "DELETE FROM tbscheduleddivessites WHERE SCHEDULED_DIVE_ID ='$ScheduledDiveID'"; 
	mysql_query($sql, $con) or die(mysql_error()); 
}

// Now get and save Dive Site fields for Scheduled Dive
$SheduledDiveDiveSiteCount = $_POST['SCHEDULEDDIVE_SITE_COUNT'];

$SheduledDiveDiveSiteCount = stripslashes($SheduledDiveDiveSiteCount);
$SheduledDiveDiveSiteCount = mysql_real_escape_string($SheduledDiveDiveSiteCount);

$response["SCHEDULEDDIVEDIVESITE_SCHEDULEDDIVE_".$ScheduledDiveID] = array();
for ($i = 0; $i < $SheduledDiveDiveSiteCount; $i++) {
	$ScheduledDiveDiveSiteID = $_POST['SCHEDULED_DIVE_SITE_ID_' . $i];
	$ScheduledDiveDiveSiteLocalID = $_POST['SCHEDULED_DIVE_SITE_LOCAL_ID_' . $i];
	$ScheduledDiveDiveSiteSiteID = $_POST['SITE_ID_' . $i];
		
	if ($ScheduledDiveDiveSiteID != -1) {
		$sql = "INSERT INTO tbscheduleddivessites (SCHEDULED_DIVE_SITE_ID, SCHEDULED_DIVE_ID, SITE_ID)
				VALUES (\"$ScheduledDiveDiveSiteID\", \"$ScheduledDiveID\", \"$ScheduledDiveDiveSiteSiteID\")";
		mysql_query($sql, $con) or die(mysql_error());
	} else {
		$sql = "INSERT INTO tbscheduleddivessites (SCHEDULED_DIVE_ID, SITE_ID)
				VALUES (\"$ScheduledDiveID\", \"$ScheduledDiveDiveSiteSiteID\")";
		mysql_query($sql, $con) or die(mysql_error());
		$ScheduledDiveDiveSiteID = mysql_insert_id();
	}
	
	// Get Vote Count for published scheduled dive dive site
	$ScheduledDiveDiveSiteVoteCount = 0;
	$sql = "SELECT COALESCE(SUM(sd_u.SCHEDULED_DIVE_SITE_ID), 0) as VOTE_COUNT
			FROM tbscheduleddivesusers sd_u WHERE sd_u.SCHEDULED_DIVE_SITE_ID = $ScheduledDiveDiveSiteID";
			
	$resultVoteCount = mysql_query($sql, $con) or die(mysql_error());
	if (mysql_num_rows($resultVoteCount) > 0) {
		$rowVoteCount = mysql_fetch_array($resultVoteCount);
	    $ScheduledDiveDiveSiteVoteCount = $resultVoteCount["VOTE_COUNT"];
	}
	
	// Need to store scheduled dive's dive sites here to retain the local ids passed in
	$scheduleddivesSite = array();
    $scheduleddivesSite["SCHEDULED_DIVE_SITE_ID"] = $ScheduledDiveDiveSiteID;
	$scheduleddivesSite["SCHEDULED_DIVE_SITE_LOCAL_ID"] = $ScheduledDiveDiveSiteLocalID;
	$scheduleddivesSite["SCHEDULED_DIVE_ID"] = $ScheduledDiveID;
	$scheduleddivesSite["SITE_ID"] = $ScheduledDiveDiveSiteSiteID;
	$scheduleddivesSite["VOTE_COUNT"] = $ScheduledDiveDiveSiteVoteCount;
	
	array_push($response["SCHEDULEDDIVEDIVESITE_SCHEDULEDDIVE_".$ScheduledDiveID], $scheduleddivesSite);
	
	// Get dive site for scheduled dive dive site
	$sql = "SELECT sites.SITE_ID,
		   sites.NAME,
		   sites.TOTAL_RATE,
		   sites.NUM_RATES,
		   sites.CITY,
		   sites.PROVINCE,
		   sites.COUNTRY,
		   sites.DIFFICULTY,
		   sites.ISSALT,
		   sites.ISFRESH,
		   sites.ISSHORE,
		   sites.ISBOAT,
		   sites.ISWRECK,
		   sites.HISTORY,
		   sites.DESCRIPTION,
		   sites.DIRECTIONS,
		   sites.SOURCE,
		   sites.NOTES,
		   sites.LATITUDE,
		   sites.LONGITUDE,
		   sites.ALTITUDE,
		   sites.APPROVED,
		   sites.USER_ID,
		   sites.DATE_ADDED,
		   sites.LAST_MODIFIED,
		   users.USER_ID,
		   users.USERNAME
 	FROM tbdivesites sites LEFT JOIN tbusers users ON sites.USER_ID = users.USER_ID 
	WHERE sites.SITE_ID = '$ScheduledDiveDiveSiteSiteID'";
			
	$resultSite = mysql_query($sql, $con) or die(mysql_error());

	// check for empty result
	if (mysql_num_rows($resultSite) > 0) {
	    // looping through all sites
	    $response["DIVESITES_SCHEDULEDDIVEDIVESITE_".$ScheduledDiveDiveSiteID] = array();
		
		while ($rowSite = mysql_fetch_array($resultSite)) {
	        $scheduleddivesSiteSite = array();
	        $scheduleddivesSiteSite["SITE_ID"] = $rowSite["SITE_ID"];
	        $scheduleddivesSiteSite["NAME"] = $rowSite["NAME"];
	        $scheduleddivesSiteSite["TOTAL_RATE"] = $rowSite["TOTAL_RATE"];
	        $scheduleddivesSiteSite["NUM_RATES"] = $rowSite["NUM_RATES"];
	        $scheduleddivesSiteSite["CITY"] = $rowSite["CITY"];
			$scheduleddivesSiteSite["PROVINCE"] = $rowSite["PROVINCE"];
			$scheduleddivesSiteSite["COUNTRY"] = $rowSite["COUNTRY"];
			$scheduleddivesSiteSite["DIFFICULTY"] = $rowSite["DIFFICULTY"];
			$scheduleddivesSiteSite["ISSALT"] = $rowSite["ISSALT"];
			$scheduleddivesSiteSite["ISFRESH"] = $rowSite["ISFRESH"];
			$scheduleddivesSiteSite["ISSHORE"] = $rowSite["ISSHORE"];
			$scheduleddivesSiteSite["ISBOAT"] = $rowSite["ISBOAT"];
			$scheduleddivesSiteSite["ISWRECK"] = $rowSite["ISWRECK"];
			$scheduleddivesSiteSite["HISTORY"] = $rowSite["HISTORY"];
			$scheduleddivesSiteSite["DESCRIPTION"] = $rowSite["DESCRIPTION"];
			$scheduleddivesSiteSite["DIRECTIONS"] = $rowSite["DIRECTIONS"];
			$scheduleddivesSiteSite["SOURCE"] = $rowSite["SOURCE"];
			$scheduleddivesSiteSite["NOTES"] = $rowSite["NOTES"];
			$scheduleddivesSiteSite["LATITUDE"] = $rowSite["LATITUDE"];
			$scheduleddivesSiteSite["LONGITUDE"] = $rowSite["LONGITUDE"];
			$scheduleddivesSiteSite["ALTITUDE"] = $rowSite["ALTITUDE"];
			$scheduleddivesSiteSite["APPROVED"] = $rowSite["APPROVED"];
			$scheduleddivesSiteSite["USER_ID"] = $rowSite["USER_ID"];
			$scheduleddivesSiteSite["DATE_ADDED"] = strtotime($rowSite["DATE_ADDED"]) * 1000;
			$scheduleddivesSiteSite["LAST_MODIFIED_ONLINE"] = $rowSite["LAST_MODIFIED"] . " UTC";
			$scheduleddivesSiteSite["USERNAME"] = $rowSite["USERNAME"];
			
			array_push($response["DIVESITES_SCHEDULEDDIVEDIVESITE_".$ScheduledDiveDiveSiteID], $scheduleddivesSiteSite);
			
			// Get Pictures for Dive Site		
			$sql = "SELECT PIC_ID, SITE_ID, PIC_DESC, FILE_NAME FROM tbsitepictures
					WHERE SITE_ID = '$ScheduledDiveDiveSiteSiteID' ORDER BY PIC_ID";
					
			$resultPictures = mysql_query($sql, $con) or die(mysql_error());
			
			// check for empty result
			if (mysql_num_rows($resultPictures) > 0) {		 
				// temp user array
				$response["PICTURE_DIVESITE_" . $ScheduledDiveDiveSiteSiteID] = array();
			    while ($rowPictures = mysql_fetch_array($resultPictures)) {
					$divesitePictures = array();
			        $divesitePictures["PIC_ID"] = $rowPictures["PIC_ID"];
			        $divesitePictures["SITE_ID"] = $rowPictures["SITE_ID"];
			        $divesitePictures["PIC_DESC"] = $rowPictures["PIC_DESC"];	
					
					if (trim($rowPictures["FILE_NAME"]) == "") {
						$divesitePictures["PIC_URL"] = "";
					} else {
						$divesitePictures["PIC_URL"] = "https://www.divethesite.com/siteImages/".$rowPictures["FILE_NAME"];	
					}
							
			        array_push($response["PICTURE_DIVESITE_" . $ScheduledDiveDiveSiteSiteID], $divesitePictures);
				}
			}
		}
	}
}

// Now get and save User fields for Scheduled Dive, if scheduled dive is new
if ($newScheduledDive == 1) {
	$SheduledDiveUserCount = $_POST['SCHEDULEDDIVE_USER_COUNT'];

	$SheduledDiveUserCount = stripslashes($SheduledDiveUserCount);
	$SheduledDiveUserCount = mysql_real_escape_string($SheduledDiveUserCount);

	$response["SCHEDULEDDIVEUSER_SCHEDULEDDIVE_".$ScheduledDiveID] = array();
	for ($i = 0; $i < $SheduledDiveUserCount; $i++) {
	    $ScheduledDiveUserID = -1;
		$ScheduledDiveUserLocalID = $_POST['SCHEDULED_DIVE_USER_LOCAL_ID_' . $i];
		$ScheduledDiveUserVotedSiteID = $_POST['VOTED_SCHEDULED_DIVE_SITE_ID_' . $i];
		$ScheduledDiveUserUserID = $_POST['USER_ID_' . $i];
		$ScheduledDiveUserAttendStateID = $_POST['ATTEND_STATE_' . $i];
		
		$sql = "INSERT INTO tbscheduleddivesusers (SCHEDULED_DIVE_ID, SCHEDULED_DIVE_SITE_ID, USER_ID, ATTEND_STATE)
				VALUES (\"$ScheduledDiveID\", \"$ScheduledDiveUserVotedSiteID\", \"$ScheduledDiveUserUserID\", \"$ScheduledDiveUserAttendStateID\")"; 
		mysql_query($sql, $con) or die(mysql_error());
		$ScheduledDiveUserID = mysql_insert_id(); 							
		
		// Need to store scheduled dive's user here to retain the local id passed in
		$scheduleddivesUser = array();
	    $scheduleddivesUser["SCHEDULED_DIVE_USER_ID"] = $ScheduledDiveUserID;
		$scheduleddivesUser["SCHEDULED_DIVE_USER_LOCAL_ID"] = $ScheduledDiveUserLocalID;
		$scheduleddivesUser["SCHEDULED_DIVE_ID"] = $ScheduledDiveID;
		$scheduleddivesUser["VOTED_SCHEDULED_DIVE_SITE_ID"] = $ScheduledDiveUserVotedSiteID;
		$scheduleddivesUser["USER_ID"] = $ScheduledDiveUserUserID;
		$scheduleddivesUser["ATTEND_STATE"] = $ScheduledDiveUserAttendStateID;
		
		array_push($response["SCHEDULEDDIVEUSER_SCHEDULEDDIVE_".$ScheduledDiveID], $scheduleddivesUser);
	}
}

// Now retrieve the updated scheduled dive to get the new modified date
$sql = "SELECT scheduleddives.SCHEDULED_DIVE_ID,
			   scheduleddives.TITLE,
			   scheduleddives.USER_ID,
			   scheduleddives.TIMESTAMP,
			   scheduleddives.COMMENT,
			   scheduleddives.LAST_MODIFIED
	 	FROM tbscheduleddives scheduleddives 
		WHERE SCHEDULED_DIVE_ID = '$ScheduledDiveID'";	
		
$result = mysql_query($sql, $con) or die(mysql_error());
 
// check for empty result
if (mysql_num_rows($result) > 0) {
 
    $row = mysql_fetch_array($result);
	$response["SCHEDULEDDIVES"] = array();
	
    $scheduleddives = array();
    $scheduleddives["SCHEDULED_DIVE_ID"] = $row["SCHEDULED_DIVE_ID"];
	$scheduleddives["TITLE"] = $row["TITLE"];
	$scheduleddives["SUBMITTER_ID"] = $row["USER_ID"];
	$scheduleddives["TIMESTAMP"] = $row["TIMESTAMP"];
	$scheduleddives["COMMENT"] = $row["COMMENT"];
	$scheduleddives["LAST_MODIFIED_ONLINE"] = $row["LAST_MODIFIED"] . " UTC";
	
    array_push($response["SCHEDULEDDIVES"], $scheduleddives);
	
	// Now get scheduleddives users if scheduled dive is not new
	if ($newScheduledDive == 0) {
		$sql = "SELECT SCHEDULED_DIVE_USER_ID,
					   SCHEDULED_DIVE_ID,
					   SCHEDULED_DIVE_SITE_ID,
					   USER_ID,
					   ATTEND_STATE				
			    FROM tbscheduleddivesusers WHERE SCHEDULED_DIVE_ID = '$ScheduledDiveID'";
				
		$resultScheduledUser = mysql_query($sql, $con) or die(mysql_error());

		// check for empty result
		if (mysql_num_rows($resultScheduledUser) > 0) {
		    // looping through all scheduled dive users
		    $response["SCHEDULEDDIVEUSER_SCHEDULEDDIVE_".$ScheduledDiveID] = array();
			
			while ($rowScheduledUser = mysql_fetch_array($resultScheduledUser)) {
		        $scheduleddivesUser = array();
			    $scheduleddivesUser["SCHEDULED_DIVE_USER_ID"] = $rowScheduledUser["SCHEDULED_DIVE_USER_ID"];
				$scheduleddivesUser["SCHEDULED_DIVE_ID"] = $rowScheduledUser["SCHEDULED_DIVE_ID"];
				$scheduleddivesUser["VOTED_SCHEDULED_DIVE_SITE_ID"] = $rowScheduledUser["SCHEDULED_DIVE_SITE_ID"];
				$scheduleddivesUser["USER_ID"] = $rowScheduledUser["USER_ID"];
				$scheduleddivesUser["ATTEND_STATE"] = $rowScheduledUser["ATTEND_STATE"];
				
				array_push($response["SCHEDULEDDIVEUSER_SCHEDULEDDIVE_".$ScheduledDiveID], $scheduleddivesUser);
			}
		}
	}
	
	// success
    $response["SUCCESS"] = 1;	
} else {
    // no schedule dive found, error must have occured with saving
    $response["SUCCESS"] = 0;  
}

ob_end_clean();

echo json_encode($response);

mysql_close($con);

?>