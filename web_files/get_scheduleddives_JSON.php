

<?php

ob_start(); 

/*
 * Following code will list all the scheduled dives
 */
include('../variables/variables.php'); 
  
// array for JSON response
$response = array();
$response["SUCCESS"] = 0;

$DATE_CREATED = 0;
$SUBMITTER_ID = -1;
$USER_ATTENDING_ID = -1;
$SITE_ID = -1;

// Get inputted last modified date
if (isset($_GET["DATE_CREATED"])) {
   $DATE_CREATED = $_GET["DATE_CREATED"];
}

if (isset($_GET["SUBMITTER_ID"])) {
   $SUBMITTER_ID = $_GET["SUBMITTER_ID"];
}

if (isset($_GET["USER_ID"])) {
   $USER_ATTENDING_ID = $_GET["USER_ID"];
}

if (isset($_GET["SITE_ID"])) {
   $SITE_ID = $_GET["SITE_ID"];
}

$TITLE = "";   
if (isset($_GET["TITLE"]))
   $TITLE = $_GET["TITLE"];
   
$COUNTRY = "";   
if (isset($_GET["COUNTRY"]))
   $COUNTRY = $_GET["COUNTRY"];
   
$PROVINCE = "";   
if (isset($_GET["PROVINCE"]))
   $PROVINCE = $_GET["PROVINCE"];
   
$CITY = "";   
if (isset($_GET["CITY"]))
   $CITY = $_GET["CITY"];

$TIMESTAMP_START = "";   
if (isset($_GET["TIMESTAMP_START"]))
   $TIMESTAMP_START = $_GET["TIMESTAMP_START"];

$TIMESTAMP_END = "";
if (isset($_GET["TIMESTAMP_END"])) {
	$TIMESTAMP_END = $_GET["TIMESTAMP_END"];
}

$IGNORE_TIMESTAMP_START = "";   
if (isset($_GET["IGNORE_TIMESTAMP_START"]))
   $IGNORE_TIMESTAMP_START = $_GET["IGNORE_TIMESTAMP_START"];

$IGNORE_TIMESTAMP_END = "";
if (isset($_GET["IGNORE_TIMESTAMP_END"])) {
	$IGNORE_TIMESTAMP_END = $_GET["IGNORE_TIMESTAMP_END"];
}

$CURRENT_LATITUDE = "";
if (isset($_GET["CURRENT_LATITUDE"])) {
	$CURRENT_LATITUDE = $_GET["CURRENT_LATITUDE"];
}

$CURRENT_LONGITUDE = "";
if (isset($_GET["CURRENT_LONGITUDE"])) {
	$CURRENT_LONGITUDE = $_GET["CURRENT_LONGITUDE"];
}

$DISTANCE = "";
if (isset($_GET["DISTANCE"])) {
	$DISTANCE = $_GET["DISTANCE"];
}

$START_INDEX_LOAD = "";
if (isset($_GET["START_INDEX_LOAD"])) {
	$START_INDEX_LOAD = $_GET["START_INDEX_LOAD"];
}

$COUNT_LOAD = "";
if (isset($_GET["COUNT_LOAD"])) {
	$COUNT_LOAD = $_GET["COUNT_LOAD"];
}

// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con);
mysql_query('SET CHARACTER SET utf8');

// To protect MySQL injection (more detail about MySQL injection)
$DATE_CREATED = stripslashes($DATE_CREATED);
$DATE_CREATED = mysql_real_escape_string($DATE_CREATED);

$SUBMITTER_ID = stripslashes($SUBMITTER_ID);
$SUBMITTER_ID = mysql_real_escape_string($SUBMITTER_ID);

$USER_ATTENDING_ID = stripslashes($USER_ATTENDING_ID);
$USER_ATTENDING_ID = mysql_real_escape_string($USER_ATTENDING_ID);

$SITE_ID = stripslashes($SITE_ID);
$SITE_ID = mysql_real_escape_string($SITE_ID);

$TITLE = stripslashes($TITLE);
$TITLE = mysql_real_escape_string($TITLE);

$COUNTRY = stripslashes($COUNTRY);
$COUNTRY = mysql_real_escape_string($COUNTRY);

$PROVINCE = stripslashes($PROVINCE);
$PROVINCE = mysql_real_escape_string($PROVINCE);

$CITY = stripslashes($CITY);
$CITY = mysql_real_escape_string($CITY);

$TIMESTAMP_START = stripslashes($TIMESTAMP_START);
$TIMESTAMP_START = mysql_real_escape_string($TIMESTAMP_START);

$TIMESTAMP_END = stripslashes($TIMESTAMP_END);
$TIMESTAMP_END = mysql_real_escape_string($TIMESTAMP_END);

$IGNORE_TIMESTAMP_START = stripslashes($IGNORE_TIMESTAMP_START);
$IGNORE_TIMESTAMP_START = mysql_real_escape_string($IGNORE_TIMESTAMP_START);

$IGNORE_TIMESTAMP_END = stripslashes($IGNORE_TIMESTAMP_END);
$IGNORE_TIMESTAMP_END = mysql_real_escape_string($IGNORE_TIMESTAMP_END);

$CURRENT_LATITUDE = stripslashes($CURRENT_LATITUDE);
$CURRENT_LATITUDE = mysql_real_escape_string($CURRENT_LATITUDE);

$CURRENT_LONGITUDE = stripslashes($CURRENT_LONGITUDE);
$CURRENT_LONGITUDE = mysql_real_escape_string($CURRENT_LONGITUDE);

$DISTANCE = stripslashes($DISTANCE);
$DISTANCE = mysql_real_escape_string($DISTANCE);

$START_INDEX_LOAD = stripslashes($START_INDEX_LOAD);
$START_INDEX_LOAD = mysql_real_escape_string($START_INDEX_LOAD);

$COUNT_LOAD = stripslashes($COUNT_LOAD);
$COUNT_LOAD = mysql_real_escape_string($COUNT_LOAD);

date_default_timezone_set('UTC');
$DATE_CREATED_SEC = $DATE_CREATED / 1000;
$DATE_CREATED_DATE = date("Y-m-d H:i:s", $DATE_CREATED_SEC);

$sqlLimit = "";
if ($START_INDEX_LOAD != "" && $COUNT_LOAD != "") {
	$sqlLimit = " LIMIT $START_INDEX_LOAD, $COUNT_LOAD ";
}

$sqlDistanceToSitesResult = "";
$sqlDistanceToSitesSelect = "";
if ($CURRENT_LONGITUDE != "" && $CURRENT_LATITUDE != "") {
	$sqlDistanceToSitesResult = "(SELECT MIN(111.045 * vincenty($CURRENT_LATITUDE, $CURRENT_LONGITUDE, d.LATITUDE, d.LONGITUDE)) 
		                          FROM tbscheduleddivessites sd_d LEFT JOIN tbdivesites d ON sd_d.SITE_ID = d.SITE_ID
								  WHERE sd_d.SCHEDULED_DIVE_ID = scheduleddives.SCHEDULED_DIVE_ID)";
	$sqlDistanceToSitesSelect = ", $sqlDistanceToSitesResult as DISTANCE";
}

$condition = " WHERE scheduleddives.TITLE LIKE '%$TITLE%' AND 
		      	     scheduleddives.DATE_ADDED >= '$DATE_CREATED_DATE'";
					 
$sqlCounter = "SELECT COUNT(scheduleddives.SCHEDULED_DIVE_ID) as SCHEDULED_DIVE_COUNT
               FROM tbscheduleddives scheduleddives";

$sql = "SELECT scheduleddives.SCHEDULED_DIVE_ID,
			   scheduleddives.TITLE,
			   scheduleddives.USER_ID,
			   scheduleddives.TIMESTAMP,
			   scheduleddives.COMMENT,
			   scheduleddives.LAST_MODIFIED
			   $sqlDistanceToSites
	 	FROM tbscheduleddives scheduleddives";
		
if ($TITLE != "" || $COUNTRY != "" || $PROVINCE != "" || $CITY != "") {
	$condition = $condition . " AND
		      scheduleddives.SCHEDULED_DIVE_ID IN 
			    (SELECT sd_d.SCHEDULED_DIVE_ID
				 FROM tbscheduleddivessites sd_d LEFT JOIN tbdivesites d ON sd_d.SITE_ID = d.SITE_ID
				 WHERE d.NAME LIKE '%$TITLE%' AND
				       d.COUNTRY LIKE '%$COUNTRY%' AND
					   d.PROVINCE LIKE '%$PROVINCE%' AND
					   d.CITY LIKE '%$CITY%')";
}
					   
if ($TIMESTAMP_START != '') {
	$condition = $condition . " AND scheduleddives.TIMESTAMP >= '$TIMESTAMP_START'";
}

if ($TIMESTAMP_END != '') {
	$condition = $condition . " AND scheduleddives.TIMESTAMP <= '$TIMESTAMP_END'";
}

if ($IGNORE_TIMESTAMP_START != '' && $IGNORE_TIMESTAMP_END != '') {
	$condition = $condition . " AND (scheduleddives.TIMESTAMP < '$IGNORE_TIMESTAMP_START' OR scheduleddives.TIMESTAMP > '$IGNORE_TIMESTAMP_START')";
}	
											
if ($SUBMITTER_ID != -1 && $USER_ATTENDING_ID != -1) {
	$condition = $condition . " AND (scheduleddives.USER_ID = '$SUBMITTER_ID' OR 
			                     scheduleddives.SCHEDULED_DIVE_ID IN 
								 	(SELECT sd_u.SCHEDULED_DIVE_ID 
									 FROM tbscheduleddivesusers sd_u 
									 WHERE sd_u.USER_ID = '$USER_ATTENDING_ID'))";
} else if ($SUBMITTER_ID != -1) {
	$condition = $condition . " AND scheduleddives.USER_ID = '$SUBMITTER_ID'";
} else if ($USER_ATTENDING_ID != -1) {
	$condition = $condition . " AND scheduleddives.SCHEDULED_DIVE_ID IN 
						 	(SELECT sd_u.SCHEDULED_DIVE_ID 
							 FROM tbscheduleddivesusers sd_u 
							 WHERE sd_u.USER_ID = '$USER_ATTENDING_ID')";
}

if ($SITE_ID != -1) {
	$condition = $condition . " AND scheduleddives.SCHEDULED_DIVE_ID IN 
						 	(SELECT sd_d.SCHEDULED_DIVE_ID 
							 FROM tbscheduleddivessites sd_d 
							 WHERE sd_d.SITE_ID = '$SITE_ID')";
}

$sql = $sql . $condition;
$sqlCounter = $sqlCounter . $condition;

if ($sqlDistanceToSitesResult != "" && $DISTANCE != "") {
	$sql = $sql . " AND $sqlDistanceToSitesResult <= '$DISTANCE'";
}

if ($sqlDistanceToSites != "") {
	$sql = $sql . " ORDER BY CASE WHEN DISTANCE IS NULL THEN 1 ELSE 0 END, DISTANCE, TIMESTAMP $sqlLimit";
} else {
	$sql = $sql . " ORDER BY scheduleddives.TIMESTAMP $sqlLimit";
}

// Get counter result first
$SCHEDULED_DIVE_COUNT = 0;
$resultCounter = mysql_query($sqlCounter, $con) or die(mysql_error());
if (mysql_num_rows($resultCounter) > 0) {
	$row = mysql_fetch_array($resultCounter);
	$SCHEDULED_DIVE_COUNT = $row["SCHEDULED_DIVE_COUNT"];
}

$result = mysql_query($sql, $con) or die(mysql_error());

// check for empty result
if (mysql_num_rows($result) > 0) {
    // looping through all scheduled dives
 
    while ($row = mysql_fetch_array($result)) {
        $response["SCHEDULEDDIVES"] = array();
	
	    $scheduleddives = array();
		$scheduleddives["SCHEDULED_DIVE_COUNT"] = $SCHEDULED_DIVE_COUNT;
	    $scheduleddives["SCHEDULED_DIVE_ID"] = $row["SCHEDULED_DIVE_ID"];
		$scheduleddives["TITLE"] = $row["TITLE"];
		$scheduleddives["SUBMITTER_ID"] = $row["USER_ID"];
		$scheduleddives["TIMESTAMP"] = $row["TIMESTAMP"];
		$scheduleddives["COMMENT"] = $row["COMMENT"];
		$scheduleddives["LAST_MODIFIED_ONLINE"] = $row["LAST_MODIFIED"] . " UTC";
		
	    array_push($response["SCHEDULEDDIVES"], $scheduleddives);
		
		// Now get scheduleddives sites and users
		$scheduleddiveID = $scheduleddives["SCHEDULED_DIVE_ID"];
		
		$sql = "SELECT sd_d.SCHEDULED_DIVE_SITE_ID,
					   sd_d.SCHEDULED_DIVE_ID,
					   sd_d.SITE_ID,
					   (SELECT COALESCE(SUM(sd_u.SCHEDULED_DIVE_SITE_ID), 0) 
					    FROM tbscheduleddivesusers sd_u WHERE sd_u.SCHEDULED_DIVE_SITE_ID = sd_d.SCHEDULED_DIVE_SITE_ID) as VOTE_COUNT
			    FROM tbscheduleddivessites sd_d WHERE SCHEDULED_DIVE_ID = '$scheduleddiveID'
				ORDER BY sd_d.SCHEDULED_DIVE_SITE_ID";
				
		$resultScheduledSite = mysql_query($sql, $con) or die(mysql_error());

		// check for empty result
		if (mysql_num_rows($resultScheduledSite) > 0) {
		    // looping through all scheduled dive dive sites
		    $response["SCHEDULEDDIVEDIVESITE_SCHEDULEDDIVE_".$scheduleddiveID] = array();
			
			while ($rowScheduledSite = mysql_fetch_array($resultScheduledSite)) {
		        $scheduleddivesSite = array();
			    $scheduleddivesSite["SCHEDULED_DIVE_SITE_ID"] = $rowScheduledSite["SCHEDULED_DIVE_SITE_ID"];
				$scheduleddivesSite["SCHEDULED_DIVE_ID"] = $rowScheduledSite["SCHEDULED_DIVE_ID"];
				$scheduleddivesSite["SITE_ID"] = $rowScheduledSite["SITE_ID"];
				$scheduleddivesSite["VOTE_COUNT"] = $rowScheduledSite["VOTE_COUNT"];
				
				array_push($response["SCHEDULEDDIVEDIVESITE_SCHEDULEDDIVE_".$scheduleddiveID], $scheduleddivesSite);
				
				// Get dive site for scheduled dive dive site
				$scheduleddivesSiteID = $scheduleddivesSite["SCHEDULED_DIVE_SITE_ID"];
				$scheduleddivesSiteSiteID = $scheduleddivesSite["SITE_ID"];
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
				WHERE sites.SITE_ID = '$scheduleddivesSiteSiteID'";
						
				$resultSite = mysql_query($sql, $con) or die(mysql_error());

				// check for empty result
				if (mysql_num_rows($resultSite) > 0) {
				    // looping through all sites
				    $response["DIVESITES_SCHEDULEDDIVEDIVESITE_".$scheduleddivesSiteID] = array();
					
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
						
						array_push($response["DIVESITES_SCHEDULEDDIVEDIVESITE_".$scheduleddivesSiteID], $scheduleddivesSiteSite);
						
						// Get Pictures for Dive Site		
						$sql = "SELECT PIC_ID, SITE_ID, PIC_DESC, FILE_NAME FROM tbsitepictures
								WHERE SITE_ID = '$scheduleddivesSiteSiteID' ORDER BY PIC_ID";
								
						$resultPictures = mysql_query($sql, $con) or die(mysql_error());
						
						// check for empty result
						if (mysql_num_rows($resultPictures) > 0) {		 
							// temp user array
							$response["PICTURE_DIVESITE_" . $scheduleddivesSiteSiteID] = array();
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
										
						        array_push($response["PICTURE_DIVESITE_" . $scheduleddivesSiteSiteID], $divesitePictures);
							}
						}
					}
				}
			}
		}
		
		$sql = "SELECT SCHEDULED_DIVE_USER_ID,
					   SCHEDULED_DIVE_ID,
					   SCHEDULED_DIVE_SITE_ID,
					   USER_ID,
					   ATTEND_STATE				
			    FROM tbscheduleddivesusers WHERE SCHEDULED_DIVE_ID = '$scheduleddiveID'
				ORDER BY SCHEDULED_DIVE_USER_ID";
				
		$resultScheduledUser = mysql_query($sql, $con) or die(mysql_error());

		// check for empty result
		if (mysql_num_rows($resultScheduledUser) > 0) {
		    // looping through all scheduled dive users
		    $response["SCHEDULEDDIVEUSER_SCHEDULEDDIVE_".$scheduleddiveID] = array();
			
			while ($rowScheduledUser = mysql_fetch_array($resultScheduledUser)) {
		        $scheduleddivesUser = array();
			    $scheduleddivesUser["SCHEDULED_DIVE_USER_ID"] = $rowScheduledUser["SCHEDULED_DIVE_USER_ID"];
				$scheduleddivesUser["SCHEDULED_DIVE_ID"] = $rowScheduledUser["SCHEDULED_DIVE_ID"];
				$scheduleddivesUser["VOTED_SCHEDULED_DIVE_SITE_ID"] = $rowScheduledUser["SCHEDULED_DIVE_SITE_ID"];
				$scheduleddivesUser["USER_ID"] = $rowScheduledUser["USER_ID"];
				$scheduleddivesUser["ATTEND_STATE"] = $rowScheduledUser["ATTEND_STATE"];
				
				array_push($response["SCHEDULEDDIVEUSER_SCHEDULEDDIVE_".$scheduleddiveID], $scheduleddivesUser);
			}
		}
		
		// success
	    $response["SUCCESS"] = 1;
	 
	 	ob_end_clean();
		
	    // echoing JSON response  
	    echo json_encode($response);
		echo "\r\n";	
    }
} else {
    // no scheduled dives found but no error
	$response["SCHEDULEDDIVES"] = array();
    $response["SUCCESS"] = 1;
 
 	ob_end_clean();
 
    // echo no scheduled dives JSON
    echo json_encode($response);
}

mysql_close($con);