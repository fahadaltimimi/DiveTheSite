

<?php
 
/*
* Following will read downloaded NDBC data echo it for JSON
*/

include('../variables/variables.php'); 

ob_start();
 
// Get given station ID
$STATION_ID = "";
if (isset($_GET["STATION_ID"])) {
	$STATION_ID = $_GET["STATION_ID"];
}

$MAX_DATA_RECORDS = "";
if (isset($_GET["MAX_DATA_RECORDS"])) {
	$MAX_DATA_RECORDS = $_GET["MAX_DATA_RECORDS"];
}

// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con);

// To protect MySQL injection (more detail about MySQL injection)
$STATION_ID = stripslashes($STATION_ID);
$STATION_ID = mysql_real_escape_string($STATION_ID);

$MAX_DATA_RECORDS = stripslashes($MAX_DATA_RECORDS);
$MAX_DATA_RECORDS = mysql_real_escape_string($MAX_DATA_RECORDS);

$response = array();
$response["METEOROLOGICAL_DATA_SUCCESS"] = 0;
$response["SPECTRAL_WAVE_DATA_SUCCESS"] = 0;
$response["DRIFTING_BUOY_DATA_SUCCESS"] = 0;
$response["OCEANIC_DATA_SUCCESS"] = 0;

$response["MESSAGE"] = "";

$stationName = "";

if ($STATION_ID != "") {
 	// Get station name
	$sql = "SELECT NAME FROM tbNDBCStations WHERE STATION_ID = $STATION_ID";
	
	$result = mysql_query($sql, $con) or die(mysql_error()); 
	
	// Proceed if station name found for given Station ID
	if (mysql_num_rows($result) > 0) {
		$row = mysql_fetch_array($result);
		$stationName = $row["NAME"];
		
		// Get folder with data via FTP
		$dataFTPFolder = "ftp://divethesitecrons:88iNuzKJ@divethesite.com/www.ndbc.noaa.gov/data/realtime2app/";

		// .txt file, if it exists
		$response["METEOROLOGICAL_DATA"] = array();		
		$filename = $dataFTPFolder.$stationName.".txt";
		
		if (file_exists($filename)) {
			$fp = fopen($filename, 'r');
	
			// Standard meteorological data
				// First row is column heading, second is units
			$line = fgets($fp, 2048);
			$line = fgets($fp, 2048);
			
			$record_count = 0;
			while (($record_count < $MAX_DATA_RECORDS) && !feof($fp)){
				$record_count = $record_count + 1;
				
				$meterologicalData = array();
			    $meterologicalData["STATION_ID"] = $STATION_ID;
				$meterologicalData["STATION_NAME"] = $stationName;
				
				$line = fgets($fp, 2048);

			    $data = preg_split('/\s+/', $line);
				
				// If any values are MM, make blank
				for ($i = 0; $i < count($data); ++$i) {
					if ($data[$i] == "MM") {
						$data[$i] = "N/A";
					}
				}

				$year = $data[0];
				$month = $data[1];
				$day = $data[2];
				$hour = $data[3];
				$minute = $data[4];
				
				$unixDateTime = mktime($hour, $minute, 0, $month, $day, $year);
				$dateTime = date('Y-m-d H:i:s', $unixDateTime);
				$meterologicalData["DATA_TIME"] = $dateTime;	
					
				$meterologicalData["WIND_DIRECTION"] = $data[5];
				$meterologicalData["WIND_SPEED"] = $data[6];
				$meterologicalData["WIND_GUST"] = $data[7];
				$meterologicalData["SIGNIFICANT_WAVE_HEIGHT"] = $data[8];
				$meterologicalData["DOMINANT_WAVE_PERIOD"] = $data[9];
				$meterologicalData["AVERAGE_WAVE_PERIOD"] = $data[10];
				$meterologicalData["DOMINANT_WAVE_DIRECTION"] = $data[11];
				$meterologicalData["SEA_LEVEL_PRESSURE"] = $data[12];
				$meterologicalData["AIR_TEMPERATURE"] = $data[13];
				$meterologicalData["WATER_TEMPERATURE"] = $data[14];
				$meterologicalData["DEW_POINT_TEMPERATURE"] = $data[15];
				$meterologicalData["STATION_VISIBILITY"] = $data[16];
				$meterologicalData["PRESSURE_TENDENCY"] = $data[17];
				$meterologicalData["TIDE"] = $data[18];
				
				// Add result
				$response["METEOROLOGICAL_DATA_SUCCESS"] = 1;
				array_push($response["METEOROLOGICAL_DATA"], $meterologicalData);
			}
			
			fclose($fp);
		}
			
		// .drift file, if it exists
		$response["DRIFTING_BUOY_DATA"] = array();		
		$filename = $dataFTPFolder.$stationName.".drift";
		
		if (file_exists($filename)) {
			$fp = fopen($filename, 'r');
			
			// Meteorological data from drifting buoys
				// First row is column heading, second is units
			$line = fgets($fp, 2048);
			$line = fgets($fp, 2048);
			
			$record_count = 0;
			while (($record_count < $MAX_DATA_RECORDS) && !feof($fp)){
				$record_count = $record_count + 1;
				
				$driftingBuoyData = array();
			    $driftingBuoyData["STATION_ID"] = $STATION_ID;
				$driftingBuoyData["STATION_NAME"] = $stationName;
			
			    $line = fgets($fp, 2048);
				
				$data = preg_split('/\s+/', $line);				
				
				// If any values are MM, make blank
				for ($i = 0; $i < count($data); ++$i) {
					if ($data[$i] == "MM") {
						$data[$i] = "N/A";
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
				
				$unixDateTime = mktime($hour, $minute, 0, $month, $day, $year);
				$dateTime = date('Y-m-d H:i:sNDBC', $unixDateTime);
				$driftingBuoyData["DATA_TIME"] = $dateTime;
				
				$driftingBuoyData["LATITUDE"] = $data[4];
				$driftingBuoyData["LONGITUDE"] = $data[5];
				$driftingBuoyData["WIND_DIRECTION"] = $data[6];
				$driftingBuoyData["WIND_SPEED"] = $data[7];
				$driftingBuoyData["WIND_GUST"] = $data[8];
				$driftingBuoyData["SEA_LEVEL_PRESSURE"] = $data[9];
				$driftingBuoyData["PRESSURE_TENDENCY"] = $data[10];
				$driftingBuoyData["AIR_TEMPERATURE"] = $data[11];
				$driftingBuoyData["WATER_TEMPERATURE"] = $data[12];
				
				// Add result
				$response["DRIFTING_BUOY_DATA_SUCCESS"] = 1;
				array_push($response["DRIFTING_BUOY_DATA"], $driftingBuoyData);
			}
			
			fclose($fp);
		}	
			
		// .spec file, if it exists
		$response["SPECTRAL_WAVE_DATA"] = array();		
		$filename = $dataFTPFolder.$stationName.".spec";
		
		if (file_exists($filename)) {
			$fp = fopen($filename, 'r');
			
			// Spectral wave summaries
				// First row is column heading, second is units
			$line = fgets($fp, 2048);
			$line = fgets($fp, 2048);
			
			$record_count = 0;
			while (($record_count < $MAX_DATA_RECORDS) && !feof($fp)){
				$record_count = $record_count + 1;
				$spectralWaveData = array();
			    $spectralWaveData["STATION_ID"] = $STATION_ID;
				$spectralWaveData["STATION_NAME"] = $stationName;
			
			    $line = fgets($fp, 2048);

			    $data = preg_split('/\s+/', $line);
			
				// If any values are MM, make blank
				for ($i = 0; $i < count($data); ++$i) {
					if ($data[$i] == "MM") {
						$data[$i] = "N/A";
					}
				}
				
				$year = $data[0];
				$month = $data[1];
				$day = $data[2];
				$hour = $data[3];
				$minute = $data[4];	
							
				$unixDateTime = mktime($hour, $minute, 0, $month, $day, $year);
				$dateTime = date('Y-m-d H:i:s', $unixDateTime);
				$spectralWaveData["DATA_TIME"] = $dateTime;
				
				$spectralWaveData["WAVE_HEIGHT"] = $data[5];
				$spectralWaveData["SWELL_HEIGHT"] = $data[6];
				$spectralWaveData["SWELL_PERIOD"] = $data[7];
				$spectralWaveData["WIND_WAVE_HEIGHT"] = $data[8];
				$spectralWaveData["WIND_WAVE_PERIOD"] = $data[9];
				$spectralWaveData["SWELL_DIRECTION"] = $data[10];
				$spectralWaveData["WIND_WAVE_DIRECTION"] = $data[11];
				$spectralWaveData["WAVE_STEEPNESS"] = $data[12];
				$spectralWaveData["AVERAGE_WAVE_PERIOD"] = $data[13];
				$spectralWaveData["DOMINANT_WAVE_DIRECTION"] = $data[14];
				
				// Add result
				$response["SPECTRAL_WAVE_DATA_SUCESS"] = 1;
				array_push($response["SPECTRAL_WAVE_DATA"], $spectralWaveData);
			}
			
			fclose($fp);
		}
			
		// .ocean file, if it exists
		$response["OCEANIC_DATA"] = array();		
		$filename = $dataFTPFolder.$stationName.".ocean";
		
		if (file_exists($filename)) {		
			$fp = fopen($filename, 'r');
		    
			// Oceanographic data
				// First row is column heading, second is units
			$line = fgets($fp, 2048);
			$line = fgets($fp, 2048);
			
			$record_count = 0;
			while (($record_count < $MAX_DATA_RECORDS) && !feof($fp)){
				$record_count = $record_count + 1;
				
				$oceanicData = array();
			    $oceanicData["STATION_ID"] = $STATION_ID;
				$oceanicData["STATION_NAME"] = $stationName;
			
			    $line = fgets($fp, 2048);

			    $data = preg_split('/\s+/', $line);
				
				// If any values are MM, make blank
				for ($i = 0; $i < count($data); ++$i) {
					if ($data[$i] == "MM") {
						$data[$i] = "N/A";
					}
				}
				
				$year = $data[0];
				$month = $data[1];
				$day = $data[2];
				$hour = $data[3];
				$minute = $data[4];	
							
				$unixDateTime = mktime($hour, $minute, 0, $month, $day, $year);
				$dateTime = date('Y-m-d H:i:s', $unixDateTime);
				$oceanicData["DATA_TIME"] = $dateTime;
				
				$oceanicData["DEPTH_MEASUREMENT"] = $data[5];
				$oceanicData["OCEAN_TEMPERATURE"] = $data[6];
				$oceanicData["CONDUCTIVITY"] = $data[7];
				$oceanicData["SALINITY"] = $data[8];
				$oceanicData["OXYGEN_CONCENTRATION_PERCENT"] = $data[9];
				$oceanicData["OXYGEN_CONCENTRATION_PARTS_MILLION"] = $data[10];
				$oceanicData["CHOLOROPHYLL_CONCENTRATION"] = $data[11];
				$oceanicData["TURBIDITY"] = $data[12];
				$oceanicData["PH"] = $data[13];
				$oceanicData["EH"] = $data[14];
				
				// Add result
				$response["OCEANIC_DATA_SUCCESS"] = 1;
				array_push($response["OCEANIC_DATA"], $oceanicData);
			}
			
			fclose($fp);
		}
	} else {
		$response["MESSAGE"] = "Station not found.";
	}
} else {
	$response["MESSAGE"] = "Station not found.";
}

if ($response["MESSAGE"] == "" && 
	$response["SPECTRAL_WAVE_DATA_SUCCESS"] == 0 &&
	$response["DRIFTING_BUOY_DATA_SUCCESS"] == 0 &&
	$response["OCEANIC_DATA_SUCCESS"] == 0 &&
	$response["METEOROLOGICAL_DATA_SUCCESS"] == 0) {
	
	$response["MESSAGE"] = "No data found for station " . $stationName;
}

ob_end_clean();

echo json_encode($response);

?>
        