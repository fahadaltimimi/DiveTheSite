

<?php
 
/*
* Following will read downloaded .drift files from NDBC data and update the database with new coordinates
*/

$DBHOST = "mysql.divethesite.com";
$DBUSER = "divesiteuser";
$DBPASS = "divesite5329";
$NEWLINK = TRUE;
$DBDIVE = "divesite";
  
// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con); 
 
// Get folder with data via FTP
$dataFTPFolder = "ftp://divethesitecrons:88iNuzKJ@divethesite.com/www.ndbc.noaa.gov/data/realtime2/";
$destinationAppFolder = "ftp://divethesitecrons:88iNuzKJ@divethesite.com/www.ndbc.noaa.gov/data/realtime2app/";

// Process files
if ($handle = opendir($dataFTPFolder)) {
    while (false !== ($file = readdir($handle))) {
        if ($file != "." && $file != "..") {
			$filename = $dataFTPFolder.$file;
			$fp = fopen($filename, 'r');
			
			if ($fp != false) {
				// Get file extension first
				$info = pathinfo( $filename );
				$name = $info['filename'];
				$ext  = $info['extension'];

				$name = pathinfo($filename)['filename'];
				$ext = pathinfo($filename)['extension'];
								
				// Update timestamp of NDBC Stations
				$sql = "UPDATE tbNDBCStations SET LAST_UPDATE = UTC_TIMESTAMP() WHERE NAME = \"$name\"";
				mysql_query($sql, $con) or die(mysql_error());
				
				if ($ext == "drift"){
					// Update location of drifting buoys
						// First row is column heading, second is units
					$line = fgets($fp, 2048);
					$line = fgets($fp, 2048);
					
					// Update latitude and longitude of station with first line, latest coordinates
				    $line = fgets($fp, 2048);
					
					$data = preg_split('/\s+/', $line);
					
					// If any values are MM, make blank
					for ($i = 0; $i < count($data); ++$i) {
						if ($data[i] == "MM") {
							$data[i] = "";
						}
					}
					
					$year = $data[0];
					$month = $data[1];
					$day = $data[2];
					
					$hourMinute = $data[3];
					$hour = 0;
					$minute = 0;

					if (strlen($hourMinute) == 4) {
						$hour = $hourMinute[0] + $hourMinute[1];
						$minute = $hourMinute[2] + $hourMinute[3];
					} 
					
					$dateTime = mktime($hour, $minute, 0, $month, $day, $year);
					
					$latitude = $data[4];
					$longitude = $data[5];
					
					// Add drifitng buoy coordinates to history table, and set new coordinates in station table
					$sql = "UPDATE tbNDBCStations SET LATITUDE=$latitude, LONGITUDE=$longitude
					        WHERE NAME=\"$name\"";			
					mysql_query($sql, $con) or die(mysql_error());
					
					$sql = "INSERT INTO tbNDBCBuoyHistory (STATION_ID, UPDATE_TIME, LATITUDE, LONGITUDE)
					        VALUES ((SELECT STATION_ID FROM tbNDBCStations WHERE NAME=\"$name\"),
							        FROM_UNIXTIME($dateTime), $latitude, $longitude)";
					mysql_query($sql, $con) or die(mysql_error());
				}
				
				// Copy file to app location
				$destinationFilename = $destinationAppFolder.$file;
				rename($filename, $destinationFilename);
			}
			
			fclose($fp);
        }
    }
    closedir($handle);
}

// Now delete data in folder so next update updates them

// set up basic connection
$ftp_server = "divethesite.com";
$conn_id = ftp_connect($ftp_server);

// login with username and password
$ftp_user_name = "divethesitecrons";
$ftp_user_pass = "88iNuzKJ";
$login_result = ftp_login($conn_id, $ftp_user_name, $ftp_user_pass);

// check connection
if ((!$conn_id) || (!$login_result)) {
    die("FTP connection has failed !");
}

// browse to directory
$logs_dir = "www.ndbc.noaa.gov/data/realtime2";
ftp_chdir($conn_id, $logs_dir);

// delete all files in directory
$files = ftp_nlist($conn_id, ".");
foreach ($files as $file)
{
	ftp_delete($conn_id, $file);
}

// close the connection
ftp_close($conn_id);    

?>
        