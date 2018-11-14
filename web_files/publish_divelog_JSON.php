

<?php
 
/*
 * Following code will publish a single dive log
 */
include('../variables/variables.php'); 

ob_start();
    
// array for JSON response
$response = array();
$response["SUCCESS"] = 0;

// Get parameters
$DiveLogID = $_POST['LOG_ID'];
$DiveSiteID = $_POST['SITE_ID'];
$UserID = $_POST['USER_ID'];
$Timestamp = $_POST['TIMESTAMP'];
$AirType = $_POST['AIR_TYPE'];
$StartPressure = $_POST['START_PRESSURE'];
$EndPressure = $_POST['END_PRESSURE'];
$StartAirValue = $_POST['START_AIR_VALUE'];
$StartAirUnits = $_POST['START_AIR_UNITS'];
$EndAirValue = $_POST['END_AIR_VALUE'];
$EndAirUnits = $_POST['END_AIR_UNITS'];
$DiveTime = $_POST['DIVE_TIME'];
$MaxDepthValue = $_POST['MAX_DEPTH_VALUE'];
$MaxDepthUnits = $_POST['MAX_DEPTH_UNITS'];
$AverageDepthValue = $_POST['AVERAGE_DEPTH_VALUE'];
$AverageDepthUnits = $_POST['AVERAGE_DEPTH_UNITS'];
$SurfaceTemperatureValue = $_POST['SURFACE_TEMPERATURE_VALUE'];
$SurfaceTemperatureUnits = $_POST['SURFACE_TEMPERATURE_UNITS'];
$WaterTemperatureValue = $_POST['WATER_TEMPERATURE_VALUE'];
$WaterTemperatureUnits = $_POST['WATER_TEMPERATURE_UNITS'];
$VisibilityValue = $_POST['VISIBILITY_VALUE'];
$VisibilityUnits = $_POST['VISIBILITY_UNITS'];
$WeightsRequiredValue = $_POST['WEIGHTS_REQUIRED_VALUE'];
$WeightsRequiredUnits = $_POST['WEIGHTS_REQUIRED_UNITS'];
$SurfaceTimeValue = $_POST['SURFACE_TIME_VALUE'];
$Rating = $_POST['RATING'];
$Comments = $_POST['COMMENTS'];
$IsCourse = $_POST['IS_COURSE'];
$IsPhotoVideo = $_POST['IS_PHOTO_VIDEO'];
$IsIce = $_POST['IS_ICE'];
$IsDeep = $_POST['IS_DEEP'];
$IsInstructing = $_POST['IS_INSTRUCTING'];
$IsNight = $_POST['IS_NIGHT'];

// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con);
mysql_query('SET CHARACTER SET utf8');

// To protect MySQL injection (more detail about MySQL injection)
$DiveLogID = stripslashes($DiveLogID);
$DiveLogID = mysql_real_escape_string($DiveLogID);

$DiveSiteID = stripslashes($DiveSiteID);
$DiveSiteID = mysql_real_escape_string($DiveSiteID);

$UserID = stripslashes($UserID);
$UserID = mysql_real_escape_string($UserID);

$Timestamp = stripslashes($Timestamp);
$Timestamp = mysql_real_escape_string($Timestamp);

$AirType = stripslashes($AirType);
$AirType = mysql_real_escape_string($AirType);

$StartPressure = stripslashes($StartPressure);
$StartPressure = mysql_real_escape_string($StartPressure);

$EndPressure = stripslashes($EndPressure);
$EndPressure = mysql_real_escape_string($EndPressure);

$StartAirValue = stripslashes($StartAirValue);
$StartAirValue = mysql_real_escape_string($StartAirValue);

$StartAirUnits = stripslashes($StartAirUnits);
$StartAirUnits = mysql_real_escape_string($StartAirUnits);

$EndAirValue = stripslashes($EndAirValue);
$EndAirValue = mysql_real_escape_string($EndAirValue);

$EndAirUnits = stripslashes($EndAirUnits);
$EndAirUnits = mysql_real_escape_string($EndAirUnits);

$DiveTime = stripslashes($DiveTime);
$DiveTime = mysql_real_escape_string($DiveTime);

$MaxDepthValue = stripslashes($MaxDepthValue);
$MaxDepthValue = mysql_real_escape_string($MaxDepthValue);

$MaxDepthUnits = stripslashes($MaxDepthUnits);
$MaxDepthUnits = mysql_real_escape_string($MaxDepthUnits);

$AverageDepthValue = stripslashes($AverageDepthValue);
$AverageDepthValue = mysql_real_escape_string($AverageDepthValue);

$AverageDepthUnits = stripslashes($AverageDepthUnits);
$AverageDepthUnits = mysql_real_escape_string($AverageDepthUnits);

$SurfaceTemperatureValue = stripslashes($SurfaceTemperatureValue);
$SurfaceTemperatureValue = mysql_real_escape_string($SurfaceTemperatureValue);

$SurfaceTemperatureUnits = stripslashes($SurfaceTemperatureUnits);
$SurfaceTemperatureUnits = mysql_real_escape_string($SurfaceTemperatureUnits);

$WaterTemperatureValue = stripslashes($WaterTemperatureValue);
$WaterTemperatureValue = mysql_real_escape_string($WaterTemperatureValue);

$WaterTemperatureUnits = stripslashes($WaterTemperatureUnits);
$WaterTemperatureUnits = mysql_real_escape_string($WaterTemperatureUnits);

$VisibilityValue = stripslashes($VisibilityValue);
$VisibilityValue = mysql_real_escape_string($VisibilityValue);

$VisibilityUnits = stripslashes($VisibilityUnits);
$VisibilityUnits = mysql_real_escape_string($VisibilityUnits);

$WeightsRequiredValue = stripslashes($WeightsRequiredValue);
$WeightsRequiredValue = mysql_real_escape_string($WeightsRequiredValue);

$WeightsRequiredUnits = stripslashes($WeightsRequiredUnits);
$WeightsRequiredUnits = mysql_real_escape_string($WeightsRequiredUnits);

$SurfaceTimeValue = stripslashes($SurfaceTimeValue);
$SurfaceTimeValue = mysql_real_escape_string($SurfaceTimeValue);

$Rating = stripslashes($Rating);
$Rating = mysql_real_escape_string($Rating);

$Comments = stripslashes($Comments);
$Comments = mysql_real_escape_string($Comments);

$IsCourse = stripslashes($IsCourse);
$IsCourse = mysql_real_escape_string($IsCourse);

$IsPhotoVideo = stripslashes($IsPhotoVideo);
$IsPhotoVideo = mysql_real_escape_string($IsPhotoVideo);

$IsIce = stripslashes($IsIce);
$IsIce = mysql_real_escape_string($IsIce);

$IsDeep = stripslashes($IsDeep);
$IsDeep = mysql_real_escape_string($IsDeep);

$IsInstructing = stripslashes($IsInstructing);
$IsInstructing = mysql_real_escape_string($IsInstructing);

$IsNight = stripslashes($IsNight);
$IsNight = mysql_real_escape_string($IsNight);

// If dive log id was given, make sure it still exists before trying to update
if ($DiveLogID != -1) {
	$sql = "SELECT LOG_ID FROM tbdivelog WHERE LOG_ID ='$DiveLogID'";
	$result = mysql_query($sql, $con) or die(mysql_error());
	
	if (mysql_num_rows($result) == 0) {
		$DiveLogID = -1;
	}
}

if ($DiveLogID == -1) {
	// New Dive Log, insert new log		
	$sql = "INSERT INTO tbdivelog (SITE_ID, USER_ID, TIMESTAMP, AIR_TYPE, START_PRESSURE, END_PRESSURE,
								   START_AIR_VALUE, START_AIR_UNITS, END_AIR_VALUE, END_AIR_UNITS,
	                               DIVE_TIME, MAX_DEPTH_VALUE, MAX_DEPTH_UNITS, AVERAGE_DEPTH_VALUE, AVERAGE_DEPTH_UNITS,
								   SURFACE_TEMPERATURE_VALUE, SURFACE_TEMPERATURE_UNITS, 
								   WATER_TEMPERATURE_VALUE, WATER_TEMPERATURE_UNITS, VISIBILITY_VALUE, VISIBILITY_UNITS,
								   WEIGHTS_REQUIRED_VALUE, WEIGHTS_REQUIRED_UNITS, SURFACE_TIME_VALUE,
								   RATING, COMMENTS, IS_COURSE, IS_PHOTO_VIDEO, IS_ICE, IS_DEEP, IS_INSTRUCTING, 
								   IS_NIGHT, LAST_MODIFIED, DATE_ADDED)
			VALUES (\"$DiveSiteID\", \"$UserID\", \"$Timestamp\", \"$AirType\", 
			        \"$StartPressure\", \"$EndPressure\", \"$StartAirValue\", \"$StartAirUnits\", \"$EndAirValue\", \"$EndAirUnits\", 
					\"$DiveTime\", \"$MaxDepthValue\", \"$MaxDepthUnits\", 
					\"$AverageDepthValue\", \"$AverageDepthUnits\", \"$SurfaceTemperatureValue\", \"$SurfaceTemperatureUnits\", 
					\"$WaterTemperatureValue\", \"$WaterTemperatureUnits\", \"$VisibilityValue\", \"$VisibilityUnits\", 
					\"$WeightsRequiredValue\", \"$WeightsRequiredUnits\", \"$SurfaceTimeValue\", \"$Rating\", \"$Comments\", 
					\"$IsCourse\", \"$IsPhotoVideo\", \"$IsIce\", \"$IsDeep\", \"$IsInstructing\", \"$IsNight\", UTC_TIMESTAMP(), UTC_TIMESTAMP())";
								   
	mysql_query($sql, $con) or die(mysql_error());
	$DiveLogID = mysql_insert_id(); 
} else {
	// ID already exists, update existing log
	$sql = "UPDATE tbdivelog
			SET SITE_ID = \"$DiveSiteID\",
				USER_ID = \"$UserID\",
				TIMESTAMP = \"$Timestamp\",
				AIR_TYPE = \"$AirType\",
				START_PRESSURE = \"$StartPressure\",
				END_PRESSURE = \"$EndPressure\",
				START_AIR_VALUE = \"$StartAirValue\",
				START_AIR_UNITS = \"$StartAirUnits\",
				END_AIR_VALUE = \"$EndAirValue\",
				END_AIR_UNITS = \"$EndAirUnits\",
				DIVE_TIME = \"$DiveTime\",
				MAX_DEPTH_VALUE = \"$MaxDepthValue\",
				MAX_DEPTH_UNITS = \"$MaxDepthUnits\",
				AVERAGE_DEPTH_VALUE = \"$AverageDepthValue\",
				AVERAGE_DEPTH_UNITS = \"$AverageDepthUnits\",
				SURFACE_TEMPERATURE_VALUE = \"$SurfaceTemperatureValue\",
				SURFACE_TEMPERATURE_UNITS = \"$SurfaceTemperatureUnits\",
				WATER_TEMPERATURE_VALUE = \"$WaterTemperatureValue\",
				WATER_TEMPERATURE_UNITS = \"$WaterTemperatureUnits\",
				VISIBILITY_VALUE = \"$VisibilityValue\",
				VISIBILITY_UNITS = \"$VisibilityUnits\",
				WEIGHTS_REQUIRED_VALUE = \"$WeightsRequiredValue\",
				WEIGHTS_REQUIRED_UNITS = \"$WeightsRequiredUnits\",
				SURFACE_TIME_VALUE = \"$SurfaceTimeValue\",
				RATING = \"$Rating\",
				COMMENTS = \"$Comments\",
				IS_COURSE = \"$IsCourse\",
				IS_PHOTO_VIDEO = \"$IsPhotoVideo\",
				IS_ICE = \"$IsIce\",
				IS_DEEP = \"$IsDeep\",			
				IS_INSTRUCTING = \"$IsInstructing\",
				IS_NIGHT = \"$IsNight\",
				LAST_MODIFIED = UTC_TIMESTAMP()
			WHERE LOG_ID = '$DiveLogID'";
			
	mysql_query($sql, $con) or die(mysql_error()); 
	
	// Delete logs existing buddies and stops, will be added
	$sql = "DELETE FROM tbdivelogbuddies WHERE LOG_ID ='$DiveLogID'"; 
	mysql_query($sql, $con) or die(mysql_error()); 
	
	$sql = "DELETE FROM tbdivelogstops WHERE LOG_ID ='$DiveLogID'"; 
	mysql_query($sql, $con) or die(mysql_error()); 
}

// Now get and save Buddy and Stop fields for the dive log
$BuddyCount = $_POST['BUDDY_COUNT'];
$StopCount = $_POST['STOP_COUNT'];

$BuddyCount = stripslashes($BuddyCount);
$BuddyCount = mysql_real_escape_string($BuddyCount);

$StopCount = stripslashes($StopCount);
$StopCount = mysql_real_escape_string($StopCount);

$response["BUDDY_DIVELOG_".$DiveLogID] = array();
for ($i = 0; $i < $BuddyCount; $i++) {
	$BuddyID = $_POST['LOG_BUDDY_ID_' . $i] ;
	$BuddyLocalID = $_POST['LOG_BUDDY_LOCAL_ID_' . $i] ;
	$BuddyLogID = $_POST['LOG_ID_' . $i]; 
	$BuddyUserID = $_POST['USER_ID_' . $i];
	$BuddyUsername = $_POST['USERNAME_' . $i];
	
	// If buddy id was given, make sure it still exists before trying to update
	/*if ($BuddyID != -1) {
		$sql = "SELECT LOG_BUDDY_ID FROM tbdivelogbuddies WHERE LOG_BUDDY_ID ='$BuddyID'";
		$result = mysql_query($sql, $con) or die(mysql_error());
		
		if (mysql_num_rows($result) == 0) {
			$BuddyID = -1;
		}
	}*/
	$BuddyID = -1;
	
	if ($BuddyID == -1) {
		// New Dive Log buddy, insert new buddy		
		$sql = "INSERT INTO tbdivelogbuddies (LOG_ID, USER_ID, USERNAME)
				VALUES (\"$DiveLogID\", \"$BuddyUserID\", \"$BuddyUsername\")";
									   
		mysql_query($sql, $con) or die(mysql_error());
		$BuddyID = mysql_insert_id();
	} else {
		// ID already exists, update existing buddy
		$sql = "UPDATE tbdivelogbuddies
				SET LOG_ID = \"$DiveLogID\",
					USER_ID = \"$BuddyUserID\",
					USERNAME = \"$BuddyUsername\"
				WHERE LOG_BUDDY_ID = '$BuddyID'";
				
		mysql_query($sql, $con) or die(mysql_error());
	}
	
	// Save buddy
	$diveLogBuddy = array();
    $diveLogBuddy["LOG_BUDDY_ID"] = $BuddyID;
	$diveLogBuddy["LOG_BUDDY_LOCAL_ID"] = $BuddyLocalID;
	$diveLogBuddy["LOG_BUDDY_LOG_ID"] = $DiveLogID;
	$diveLogBuddy["LOG_BUDDY_DIVER_ID"] = $BuddyUserID;
	$diveLogBuddy["LOG_BUDDY_DIVER_USERNAME"] = $BuddyUsername;
	
	array_push($response["BUDDY_DIVELOG_".$DiveLogID], $diveLogBuddy);
}

$response["STOP_DIVELOG_".$DiveLogID] = array();
for ($i = 0; $i < $StopCount; $i++) {
	$StopID = $_POST['LOG_STOP_ID_' . $i] ;
	$StopLocalID = $_POST['LOG_STOP_LOCAL_ID_' . $i] ;
	$StopLogID = $_POST['LOG_ID_' . $i]; 
	$StopTime = $_POST['TIME_' . $i]; 
	$StopDepthValue = $_POST['DEPTH_VALUE_' . $i];
	$StopDepthUnits = $_POST['DEPTH_UNITS_' . $i];
	
	// If buddy id was given, make sure it still exists before trying to update
	/*if ($StopID != -1) {
		$sql = "SELECT STOP_ID FROM tbdivelogstops WHERE STOP_ID ='$StopID'";
		$result = mysql_query($sql, $con) or die(mysql_error());
		
		if (mysql_num_rows($result) == 0) {
			$StopID = -1;
		}
	}*/
	$StopID = -1;
	
	if ($StopID == -1) {
	// New Dive Log stop, insert new stop		
	$sql = "INSERT INTO tbdivelogstops (LOG_ID, TIME, DEPTH_VALUE, DEPTH_UNITS)
			VALUES (\"$DiveLogID\", \"$StopTime\", \"$StopDepthValue\", \"$StopDepthUnits\")";
								   
	mysql_query($sql, $con) or die(mysql_error());
	$StopID = mysql_insert_id();
	} else {
		// ID already exists, update existing stop
		$sql = "UPDATE tbdivelogstops
				SET LOG_ID = \"$DiveLogID\",
					TIME = \"$StopTime\",
					DEPTH_VALUE = \"$StopDepthValue\",
					DEPTH_UNITS = \"$StopDepthUnits\"
				WHERE STOP_ID = '$StopID'";
				
		mysql_query($sql, $con) or die(mysql_error());
	}
	
	// Save stop
	$diveLogStop = array();
    $diveLogStop["LOG_STOP_ID"] = $StopID;
	$diveLogStop["LOG_STOP_LOCAL_ID"] = $StopLocalID;
	$diveLogStop["LOG_STOP_LOG_ID"] = $DiveLogID;
	$diveLogStop["LOG_STOP_TIME"] = $StopTime;
	$diveLogStop["LOG_STOP_DEPTH_VALUE"] = $StopDepthValue;
	$diveLogStop["LOG_STOP_DEPTH_UNITS"] = $StopDepthUnits;
	
	array_push($response["STOP_DIVELOG_".$DiveLogID], $diveLogStop);
}

// Now retrieve the updated dive log to get the new modified date
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
		WHERE logs.LOG_ID = '$DiveLogID'";

$result = mysql_query($sql, $con) or die(mysql_error());
 
// check for empty result
if (mysql_num_rows($result) > 0) {
    // Should return one dive site if successful
    $response["DIVELOGS"] = array();
 
    $row = mysql_fetch_array($result);
	
	$divelogs = array();
    $divelogs["LOG_ID"] = $row["LOG_ID"];
	$divelogs["SITE_ID"] = $row["SITE_ID"];
	$divelogs["USER_ID"] = $row["USER_ID"];
	$divelogs["USERNAME"] = $row["USERNAME"];
	$divelogs["TIMESTAMP"] = $row["TIMESTAMP"];
	$divelogs["AIR_TYPE"] = $row["AIR_TYPE"];
	$divelogs["START_PRESSURE"] = $row["START_PRESSURE"];
	$divelogs["END_PRESSURE"] = $row["END_PRESSURE"];
	$divelogs["START_AIR_VALUE"] = $row["START_AIR_VALUE"];
	$divelogs["START_AIR_UNITS"] = $row["START_AIR_UNITS"];
	$divelogs["END_AIR_VALUE"] = $row["END_AIR_VALUE"];
	$divelogs["END_AIR_UNITS"] = $row["END_AIR_UNITS"];
	$divelogs["DIVE_TIME"] = $row["DIVE_TIME"];
	$divelogs["MAX_DEPTH_VALUE"] = $row["MAX_DEPTH_VALUE"];
	$divelogs["MAX_DEPTH_UNITS"] = $row["MAX_DEPTH_UNITS"];
	$divelogs["AVERAGE_DEPTH_VALUE"] = $row["AVERAGE_DEPTH_VALUE"];
	$divelogs["AVERAGE_DEPTH_UNITS"] = $row["AVERAGE_DEPTH_UNITS"];
	$divelogs["SURFACE_TEMPERATURE_VALUE"] = $row["SURFACE_TEMPERATURE_VALUE"];
	$divelogs["SURFACE_TEMPERATURE_UNITS"] = $row["SURFACE_TEMPERATURE_UNITS"];
	$divelogs["WATER_TEMPERATURE_VALUE"] = $row["WATER_TEMPERATURE_VALUE"];
	$divelogs["WATER_TEMPERATURE_UNITS"] = $row["WATER_TEMPERATURE_UNITS"];
	$divelogs["VISIBILITY_VALUE"] = $row["VISIBILITY_VALUE"];
	$divelogs["VISIBILITY_UNITS"] = $row["VISIBILITY_UNITS"];
	$divelogs["WEIGHTS_REQUIRED_VALUE"] = $row["WEIGHTS_REQUIRED_VALUE"];
	$divelogs["WEIGHTS_REQUIRED_UNITS"] = $row["WEIGHTS_REQUIRED_UNITS"];
	$divelogs["SURFACE_TIME_VALUE"] = $row["SURFACE_TIME_VALUE"];
	$divelogs["SURFACE_TIME_UNITS"] = $row["SURFACE_TIME_UNITS"];
	$divelogs["RATING"] = $row["RATING"];
	$divelogs["COMMENTS"] = $row["COMMENTS"];
	$divelogs["IS_COURSE"] = $row["IS_COURSE"];
	$divelogs["IS_PHOTO_VIDEO"] = $row["IS_PHOTO_VIDEO"];
	$divelogs["IS_ICE"] = $row["IS_ICE"];
	$divelogs["IS_DEEP"] = $row["IS_DEEP"];
	$divelogs["IS_INSTRUCTING"] = $row["IS_INSTRUCTING"];
	$divelogs["IS_NIGHT"] = $row["IS_NIGHT"];
	$divelogs["LAST_MODIFIED_ONLINE"] = $row["LAST_MODIFIED"] . " UTC";
	
    array_push($response["DIVELOGS"], $divelogs);		
	
	// Now get dive log's site and pictures
	$DIVELOG_SITEID = $divelogs["SITE_ID"];
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
	WHERE sites.SITE_ID = '$DIVELOG_SITEID'";
			
	$resultSite = mysql_query($sql, $con) or die(mysql_error());

	// check for empty result
	if (mysql_num_rows($resultSite) > 0) {
	    // looping through all sites
	    $response["DIVESITES_DIVELOG_".$DiveLogID] = array();
		
		while ($rowSite = mysql_fetch_array($resultSite)) {
	        $diveLogSite = array();
	        $diveLogSite["SITE_ID"] = $rowSite["SITE_ID"];
	        $diveLogSite["NAME"] = $rowSite["NAME"];
	        $diveLogSite["TOTAL_RATE"] = $rowSite["TOTAL_RATE"];
	        $diveLogSite["NUM_RATES"] = $rowSite["NUM_RATES"];
	        $diveLogSite["CITY"] = $rowSite["CITY"];
			$diveLogSite["PROVINCE"] = $rowSite["PROVINCE"];
			$diveLogSite["COUNTRY"] = $rowSite["COUNTRY"];
			$diveLogSite["DIFFICULTY"] = $rowSite["DIFFICULTY"];
			$diveLogSite["ISSALT"] = $rowSite["ISSALT"];
			$diveLogSite["ISFRESH"] = $rowSite["ISFRESH"];
			$diveLogSite["ISSHORE"] = $rowSite["ISSHORE"];
			$diveLogSite["ISBOAT"] = $rowSite["ISBOAT"];
			$diveLogSite["ISWRECK"] = $rowSite["ISWRECK"];
			$diveLogSite["HISTORY"] = $rowSite["HISTORY"];
			$diveLogSite["DESCRIPTION"] = $rowSite["DESCRIPTION"];
			$diveLogSite["DIRECTIONS"] = $rowSite["DIRECTIONS"];
			$diveLogSite["SOURCE"] = $rowSite["SOURCE"];
			$diveLogSite["NOTES"] = $rowSite["NOTES"];
			$diveLogSite["LATITUDE"] = $rowSite["LATITUDE"];
			$diveLogSite["LONGITUDE"] = $rowSite["LONGITUDE"];
			$diveLogSite["ALTITUDE"] = $rowSite["ALTITUDE"];
			$diveLogSite["APPROVED"] = $rowSite["APPROVED"];
			$diveLogSite["USER_ID"] = $rowSite["USER_ID"];
			$diveLogSite["DATE_ADDED"] = strtotime($rowSite["DATE_ADDED"]) * 1000;
			$diveLogSite["LAST_MODIFIED_ONLINE"] = $rowSite["LAST_MODIFIED"] . " UTC";
			$diveLogSite["USERNAME"] = $rowSite["USERNAME"];
			
			array_push($response["DIVESITES_DIVELOG_".$DiveLogID], $diveLogSite);
			
			// Get Pictures for Dive Site		
			$sql = "SELECT PIC_ID, SITE_ID, PIC_DESC, FILE_NAME FROM tbsitepictures
					WHERE SITE_ID = '$DIVELOG_SITEID' ORDER BY PIC_ID";
					
			$resultPictures = mysql_query($sql, $con) or die(mysql_error());
			
			// check for empty result
			if (mysql_num_rows($resultPictures) > 0) {		 
				// temp user array
				$response["PICTURE_DIVESITE_" . $DIVELOG_SITEID] = array();
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
							
			        array_push($response["PICTURE_DIVESITE_" . $DIVELOG_SITEID], $divesitePictures);
				}
			}
		}
	} 
		
    // success
    $response["SUCCESS"] = 1;
} else {
    // no divelog found, error must have occured with saving
    $response["SUCCESS"] = 0;    
}

ob_end_clean();

echo json_encode($response);

mysql_close($con);

?>