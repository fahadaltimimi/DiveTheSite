<?php

ob_start();
 
/*
 * Following code will list all the dive sites
 */
include('../variables/variables.php'); 
  
// array for JSON response
$response = array();
$response["SUCCESS"] = 0;

// Get inputted params
$DATE_CREATED = 1;
if (isset($_GET["DATE_CREATED"]))
   $DATE_CREATED = $_GET["DATE_CREATED"];
   
$USER_ID = -1;
if (isset($_GET["USER_ID"]))
   $USER_ID = $_GET["USER_ID"];
   
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
   
$MIN_LATITUDE = -90;
if (isset($_GET["MIN_LATITUDE"]) && $_GET["MIN_LATITUDE"] != "") {
	$MIN_LATITUDE = $_GET["MIN_LATITUDE"];
}

$CURRENT_LATITUDE = "";
if (isset($_GET["CURRENT_LATITUDE"])) {
	$CURRENT_LATITUDE = $_GET["CURRENT_LATITUDE"];
}

$CURRENT_LONGITUDE = "";
if (isset($_GET["CURRENT_LONGITUDE"])) {
	$CURRENT_LONGITUDE = $_GET["CURRENT_LONGITUDE"];
}

$MAX_LATITUDE = 90;
if (isset($_GET["MAX_LATITUDE"]) && $_GET["MAX_LATITUDE"] != "") {
	$MAX_LATITUDE = $_GET["MAX_LATITUDE"];
}

$MIN_LONGITUDE = -180;
if (isset($_GET["MIN_LONGITUDE"]) && $_GET["MIN_LONGITUDE"] != "") {
	$MIN_LONGITUDE = $_GET["MIN_LONGITUDE"];
}

$MAX_LONGITUDE = 180;
if (isset($_GET["MAX_LONGITUDE"]) && $_GET["MAX_LONGITUDE"] != "") {
	$MAX_LONGITUDE = $_GET["MAX_LONGITUDE"];
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

$USER_ID = stripslashes($USER_ID);
$USER_ID = mysql_real_escape_string($USER_ID);

$TITLE = stripslashes($TITLE);
$TITLE = mysql_real_escape_string($TITLE);

$COUNTRY = stripslashes($COUNTRY);
$COUNTRY = mysql_real_escape_string($COUNTRY);

$PROVINCE = stripslashes($PROVINCE);
$PROVINCE = mysql_real_escape_string($PROVINCE);

$CITY = stripslashes($CITY);
$CITY = mysql_real_escape_string($CITY);

$CURRENT_LATITUDE = stripslashes($CURRENT_LATITUDE);
$CURRENT_LATITUDE = mysql_real_escape_string($CURRENT_LATITUDE);

$CURRENT_LONGITUDE = stripslashes($CURRENT_LONGITUDE);
$CURRENT_LONGITUDE = mysql_real_escape_string($CURRENT_LONGITUDE);

$MIN_LATITUDE = stripslashes($MIN_LATITUDE);
$MIN_LATITUDE = mysql_real_escape_string($MIN_LATITUDE);

$MAX_LATITUDE = stripslashes($MAX_LATITUDE);
$MAX_LATITUDE = mysql_real_escape_string($MAX_LATITUDE);

$MIN_LONGITUDE = stripslashes($MIN_LONGITUDE);
$MIN_LONGITUDE = mysql_real_escape_string($MIN_LONGITUDE);

$MAX_LONGITUDE = stripslashes($MAX_LONGITUDE);
$MAX_LONGITUDE = mysql_real_escape_string($MAX_LONGITUDE);

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
    $sqlDistanceToSitesResult = "(111.045 * vincenty($CURRENT_LATITUDE, $CURRENT_LONGITUDE, sites.LATITUDE, sites.LONGITUDE))";
	$sqlDistanceToSitesSelect = ", $sqlDistanceToSitesResult as DISTANCE";
}

$condition = " WHERE sites.APPROVED=1 AND sites.DATE_ADDED >= '$DATE_CREATED_DATE' 
			       AND LATITUDE >= $MIN_LATITUDE AND LATITUDE <= $MAX_LATITUDE
				   AND LONGITUDE >= $MIN_LONGITUDE AND LONGITUDE <= $MAX_LONGITUDE";

$sqlCounter = "SELECT COUNT(sites.SITE_ID) as SITE_COUNT
               FROM tbdivesites sites";

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
			   $sqlDistanceToSitesSelect 
	 	FROM tbdivesites sites LEFT JOIN tbusers users ON sites.USER_ID = users.USER_ID";
	
if ($USER_ID != -1) {
	$condition = $condition . " AND sites.USER_ID = '$USER_ID'";
}

if ($TITLE != "") {
	$condition = $condition . " AND sites.NAME LIKE '%$TITLE%'";
}

if ($COUNTRY != "") {
	$condition = $condition . " AND sites.COUNTRY LIKE '%$COUNTRY%'";
}

if ($PROVINCE != "") {
	$condition = $condition . " AND sites.PROVINCE LIKE '%$PROVINCE%'";
}

if ($CITY != "") {
	$condition = $condition . " AND sites.CITY LIKE '%$CITY%'";
}

if ($sqlDistanceToSitesResult != "" && $DISTANCE != "") {
	$condition = $condition . " AND $sqlDistanceToSitesResult <= '$DISTANCE'";
}

$sql = $sql . $condition;
$sqlCounter = $sqlCounter . $condition;

if ($sqlDistanceToSitesSelect != "") {
	$sql = $sql . " ORDER BY CASE WHEN DISTANCE IS NULL THEN 1 ELSE 0 END, DISTANCE $sqlLimit";
} else {
	$sql = $sql . " ORDER BY NAME $sqlLimit";
} 

// Get counter result first
$DIVE_SITE_COUNT = 0;
$resultCounter = mysql_query($sqlCounter, $con) or die(mysql_error());
if (mysql_num_rows($resultCounter) > 0) {
	$row = mysql_fetch_array($resultCounter);
	$DIVE_SITE_COUNT = $row["SITE_COUNT"];
}

$result = mysql_query($sql, $con) or die(mysql_error());
 
// check for empty result
if (mysql_num_rows($result) > 0) {
    // looping through all divesites
   
    while ($row = mysql_fetch_array($result)) {
        // temp user array
		$response["DIVESITES"] = array();
        
		$divesites = array();
		$divesites["SITE_COUNT"] = $DIVE_SITE_COUNT;
        $divesites["SITE_ID"] = $row["SITE_ID"];
        $divesites["NAME"] = $row["NAME"];
        $divesites["TOTAL_RATE"] = $row["TOTAL_RATE"];
        $divesites["NUM_RATES"] = $row["NUM_RATES"];
        $divesites["CITY"] = $row["CITY"];
		$divesites["PROVINCE"] = $row["PROVINCE"];
		$divesites["COUNTRY"] = $row["COUNTRY"];
		$divesites["DIFFICULTY"] = $row["DIFFICULTY"];
		$divesites["ISSALT"] = $row["ISSALT"];
		$divesites["ISFRESH"] = $row["ISFRESH"];
		$divesites["ISSHORE"] = $row["ISSHORE"];
		$divesites["ISBOAT"] = $row["ISBOAT"];
		$divesites["ISWRECK"] = $row["ISWRECK"];
		$divesites["HISTORY"] = $row["HISTORY"];
		$divesites["DESCRIPTION"] = $row["DESCRIPTION"];
		$divesites["DIRECTIONS"] = $row["DIRECTIONS"];
		$divesites["SOURCE"] = $row["SOURCE"];
		$divesites["NOTES"] = $row["NOTES"];
		$divesites["LATITUDE"] = $row["LATITUDE"];
		$divesites["LONGITUDE"] = $row["LONGITUDE"];
		$divesites["ALTITUDE"] = $row["ALTITUDE"];
		$divesites["APPROVED"] = $row["APPROVED"];
		$divesites["USER_ID"] = $row["USER_ID"];
		$divesites["DATE_ADDED"] = strtotime($row["DATE_ADDED"]) * 1000;
		$divesites["LAST_MODIFIED_ONLINE"] = $row["LAST_MODIFIED"] . " UTC";
		$divesites["USERNAME"] = $row["USERNAME"];
		
        array_push($response["DIVESITES"], $divesites);
	
		$SITE_ID = $divesites["SITE_ID"];
		
		// Get Pictures for Dive Site		
		$sql = "SELECT PIC_ID, SITE_ID, PIC_DESC, FILE_NAME FROM tbsitepictures
				WHERE SITE_ID = '$SITE_ID' ORDER BY PIC_ID";
				
		$resultPictures = mysql_query($sql, $con) or die(mysql_error());
		
		// check for empty result
		if (mysql_num_rows($resultPictures) > 0) {		 
			// temp user array
			$response["PICTURE_DIVESITE_" . $SITE_ID] = array();
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
						
				
		        array_push($response["PICTURE_DIVESITE_" . $SITE_ID], $divesitePictures);
			}
		}
		
		// Get Logs for Dive Site
	    $sql = "SELECT logs.LOG_ID,
			   logs.SITE_ID,
			   logs.TIMESTAMP,
			   logs.AIR_TYPE,
			   logs.START_PRESSURE,
			   logs.END_PRESSURE,
			   logs.START_AIR_VALUE,
			   logs.START_AIR_UNITS,
			   logs.END_AIR_VALUE,
			   logs.END_AIR_UNITS,
			   logs.DIVE_TIME,
			   logs.MAX_DEPTH_VALUE,
			   logs.MAX_DEPTH_UNITS,
			   logs.AVERAGE_DEPTH_VALUE,
			   logs.AVERAGE_DEPTH_UNITS,
			   logs.SURFACE_TEMPERATURE_VALUE,
			   logs.SURFACE_TEMPERATURE_UNITS,
			   logs.WATER_TEMPERATURE_VALUE,
			   logs.WATER_TEMPERATURE_UNITS,
			   logs.VISIBILITY_VALUE,
			   logs.VISIBILITY_UNITS,
			   logs.WEIGHTS_REQUIRED_VALUE,
			   logs.WEIGHTS_REQUIRED_UNITS,
			   logs.SURFACE_TIME_VALUE,
			   logs.SURFACE_TIME_UNITS,
			   logs.RATING,
			   logs.COMMENTS,
			   logs.IS_COURSE,
			   logs.IS_PHOTO_VIDEO,
			   logs.IS_ICE,
			   logs.IS_DEEP,
			   logs.IS_INSTRUCTING,
			   logs.IS_NIGHT,
			   logs.LAST_MODIFIED,
			   users.USER_ID,
			   users.USERNAME
	 	FROM tbdivelog logs LEFT JOIN tbusers users ON logs.USER_ID = users.USER_ID 
		WHERE logs.SITE_ID = '$SITE_ID' ORDER BY LOG_ID";
		
		$resultsDiveLogs = mysql_query($sql, $con) or die(mysql_error());
		 
		// check for empty result
		if (mysql_num_rows($resultsDiveLogs) > 0) {
		    // looping through all divelogs
		    $response["DIVELOGS_DIVESITE_" . $SITE_ID] = array();
		 
		    while ($rowDiveLogs = mysql_fetch_array($resultsDiveLogs)) {
		        // temp user array
		        $divelogs = array();
		        $divelogs["LOG_ID"] = $rowDiveLogs["LOG_ID"];
				$divelogs["SITE_ID"] = $rowDiveLogs["SITE_ID"];
				$divelogs["USER_ID"] = $rowDiveLogs["USER_ID"];
				$divelogs["USERNAME"] = $rowDiveLogs["USERNAME"];
				$divelogs["TIMESTAMP"] = $rowDiveLogs["TIMESTAMP"];
				$divelogs["AIR_TYPE"] = $rowDiveLogs["AIR_TYPE"];
				$divelogs["START_PRESSURE"] = $rowDiveLogs["START_PRESSURE"];
				$divelogs["END_PRESSURE"] = $rowDiveLogs["END_PRESSURE"];
				$divelogs["START_AIR_VALUE"] = $rowDiveLogs["START_AIR_VALUE"];
				$divelogs["START_AIR_UNITS"] = $rowDiveLogs["START_AIR_UNITS"];
				$divelogs["END_AIR_VALUE"] = $rowDiveLogs["END_AIR_VALUE"];
				$divelogs["END_AIR_UNITS"] = $rowDiveLogs["END_AIR_UNITS"];
				$divelogs["DIVE_TIME"] = $rowDiveLogs["DIVE_TIME"];
				$divelogs["MAX_DEPTH_VALUE"] = $rowDiveLogs["MAX_DEPTH_VALUE"];
				$divelogs["MAX_DEPTH_UNITS"] = $rowDiveLogs["MAX_DEPTH_UNITS"];
				$divelogs["AVERAGE_DEPTH_VALUE"] = $rowDiveLogs["AVERAGE_DEPTH_VALUE"];
				$divelogs["AVERAGE_DEPTH_UNITS"] = $rowDiveLogs["AVERAGE_DEPTH_UNITS"];
				$divelogs["SURFACE_TEMPERATURE_VALUE"] = $rowDiveLogs["SURFACE_TEMPERATURE_VALUE"];
				$divelogs["SURFACE_TEMPERATURE_UNITS"] = $rowDiveLogs["SURFACE_TEMPERATURE_UNITS"];
				$divelogs["WATER_TEMPERATURE_VALUE"] = $rowDiveLogs["WATER_TEMPERATURE_VALUE"];
				$divelogs["WATER_TEMPERATURE_UNITS"] = $rowDiveLogs["WATER_TEMPERATURE_UNITS"];
				$divelogs["VISIBILITY_VALUE"] = $rowDiveLogs["VISIBILITY_VALUE"];
				$divelogs["VISIBILITY_UNITS"] = $rowDiveLogs["VISIBILITY_UNITS"];
				$divelogs["WEIGHTS_REQUIRED_VALUE"] = $rowDiveLogs["WEIGHTS_REQUIRED_VALUE"];
				$divelogs["WEIGHTS_REQUIRED_UNITS"] = $rowDiveLogs["WEIGHTS_REQUIRED_UNITS"];
				$divelogs["SURFACE_TIME_VALUE"] = $rowDiveLogs["SURFACE_TIME_VALUE"];
				$divelogs["SURFACE_TIME_UNITS"] = $rowDiveLogs["SURFACE_TIME_UNITS"];
				$divelogs["RATING"] = $rowDiveLogs["RATING"];
				$divelogs["COMMENTS"] = $rowDiveLogs["COMMENTS"];
				$divelogs["IS_COURSE"] = $rowDiveLogs["IS_COURSE"];
				$divelogs["IS_PHOTO_VIDEO"] = $rowDiveLogs["IS_PHOTO_VIDEO"];
				$divelogs["IS_ICE"] = $rowDiveLogs["IS_ICE"];
				$divelogs["IS_DEEP"] = $rowDiveLogs["IS_DEEP"];
				$divelogs["IS_INSTRUCTING"] = $rowDiveLogs["IS_INSTRUCTING"];
				$divelogs["IS_NIGHT"] = $rowDiveLogs["IS_NIGHT"];
				$divelogs["LAST_MODIFIED_ONLINE"] = $rowDiveLogs["LAST_MODIFIED"] . " UTC";
				
		        array_push($response["DIVELOGS_DIVESITE_" . $SITE_ID], $divelogs);
				
				// Now get dive log's buddies and sites
				$diveLogID = $divelogs["LOG_ID"];
				
				$sql = "SELECT LOG_BUDDY_ID,
							   LOG_ID,
							   USER_ID,
							   USERNAME
					    FROM tbdivelogbuddies WHERE LOG_ID = '$diveLogID'";
						
				$resultBuddy = mysql_query($sql, $con) or die(mysql_error());
		 
				// check for empty result
				if (mysql_num_rows($resultBuddy) > 0) {
				    // looping through all buddies
				    $response["BUDDY_DIVELOG_".$diveLogID] = array();
					
					while ($rowBuddy = mysql_fetch_array($resultBuddy)) {
				        $diveLogBuddy = array();
					    $diveLogBuddy["LOG_BUDDY_ID"] = $rowBuddy["LOG_BUDDY_ID"];
						$diveLogBuddy["LOG_BUDDY_LOG_ID"] = $rowBuddy["LOG_ID"];
						$diveLogBuddy["LOG_BUDDY_DIVER_ID"] = $rowBuddy["USER_ID"];
						$diveLogBuddy["LOG_BUDDY_DIVER_USERNAME"] = $rowBuddy["USERNAME"];
						
						array_push($response["BUDDY_DIVELOG_".$diveLogID], $diveLogBuddy);
					}
				}
				
				$sql = "SELECT STOP_ID,
							   LOG_ID,
							   TIME,
							   DEPTH_VALUE,
							   DEPTH_UNITS				
					    FROM tbdivelogstops WHERE LOG_ID = '$diveLogID'";
						
				$resultStop = mysql_query($sql, $con) or die(mysql_error());
		 
				// check for empty result
				if (mysql_num_rows($resultStop) > 0) {
				    // looping through all sites
				    $response["STOP_DIVELOG_".$diveLogID] = array();
					
					while ($rowSite = mysql_fetch_array($resultStop)) {
				        $diveLogStop = array();
					    $diveLogStop["LOG_STOP_ID"] = $rowSite["STOP_ID"];
						$diveLogStop["LOG_STOP_LOG_ID"] = $rowSite["LOG_ID"];
						$diveLogStop["LOG_STOP_TIME"] = $rowSite["TIME"];
						$diveLogStop["LOG_STOP_DEPTH_VALUE"] = $rowSite["DEPTH_VALUE"];
						$diveLogStop["LOG_STOP_DEPTH_UNITS"] = $rowSite["DEPTH_UNITS"];
						
						array_push($response["STOP_DIVELOG_".$diveLogID], $diveLogStop);
					}
				}
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
    // no divesites found but no error
	$response["DIVESITES"] = array();
    $response["SUCCESS"] = 1;
 
    ob_end_clean();
	
    // echo no divesites JSON
    echo json_encode($response);
}

mysql_close($con);