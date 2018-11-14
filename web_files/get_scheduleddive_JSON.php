

<?php
 
/*
 * Following code will get a single scheduled dive
 */
include('../variables/variables.php'); 
  
// array for JSON response
$response = array();
$response["SUCCESS"] = 0;

$LAST_MODIFIED = 0;
$SCHEDULED_DIVE_ID = -1;

// Get inputted last modified date
if (isset($_GET["LAST_MODIFIED"])) {
   $LAST_MODIFIED = $_GET["LAST_MODIFIED"];
}

if (isset($_GET["SCHEDULED_DIVE_ID"])) {
   $SCHEDULED_DIVE_ID = $_GET["SCHEDULED_DIVE_ID"];
}

// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con);
mysql_query('SET CHARACTER SET utf8');

// To protect MySQL injection (more detail about MySQL injection)

$LAST_MODIFIED = stripslashes($LAST_MODIFIED);
$LAST_MODIFIED = mysql_real_escape_string($LAST_MODIFIED);

$SCHEDULED_DIVE_ID = stripslashes($SCHEDULED_DIVE_ID);
$SCHEDULED_DIVE_ID = mysql_real_escape_string($SCHEDULED_DIVE_ID);

date_default_timezone_set('UTC');
$LAST_MODIFIED_SEC = $LAST_MODIFIED / 1000;
$LAST_MODIFIED_DATE = date("Y-m-d H:i:s T", $LAST_MODIFIED_SEC);

$sql = "SELECT scheduleddives.SCHEDULED_DIVE_ID,
			   scheduleddives.USER_ID,
			   scheduleddives.TITLE,
			   scheduleddives.TIMESTAMP,
			   scheduleddives.COMMENT,
			   scheduleddives.LAST_MODIFIED
	 	FROM tbscheduleddives scheduleddives 
		WHERE SCHEDULED_DIVE_ID = '$SCHEDULED_DIVE_ID' AND scheduleddives.LAST_MODIFIED > '$LAST_MODIFIED_DATE'";	
		
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
	
	// Now get scheduleddives sites and users
	$scheduleddiveID = $scheduleddives["SCHEDULED_DIVE_ID"];
	
	$sql = "SELECT sd_d.SCHEDULED_DIVE_SITE_ID,
				   sd_d.SCHEDULED_DIVE_ID,
				   sd_d.SITE_ID,
				   (SELECT COALESCE(SUM(sd_u.SCHEDULED_DIVE_SITE_ID), 0) 
				    FROM tbscheduleddivesusers sd_u WHERE sd_u.SCHEDULED_DIVE_SITE_ID = sd_d.SCHEDULED_DIVE_SITE_ID) as VOTE_COUNT
		    FROM tbscheduleddivessites sd_d WHERE SCHEDULED_DIVE_ID = '$scheduleddiveID'";
			
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
		    FROM tbscheduleddivesusers WHERE SCHEDULED_DIVE_ID = '$scheduleddiveID'";
			
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
 
    // echoing JSON response  
    echo json_encode($response);
	echo "\r\n";	
} else {
    // no scheduled dives found but no error
	$response["SCHEDULEDDIVES"] = array();
    $response["SUCCESS"] = 1;
 
    // echo no scheduled dives JSON
    echo json_encode($response);
}

mysql_close($con);