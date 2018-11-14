

<?php
 
/*
* Following will read downloaded NDBC data and update the database with it
*/

include('../variables/variables.php'); 
 
$DATA_LIMIT_COUNT = 300000;
 
// Connect to DB and run sql
$con = mysql_connect($DBHOST,$DBUSER,$DBPASS,$NEWLINK);
mysql_select_db($DBDIVE, $con); 
 
 // Get data count for each table being updated
$meteorologicalCount = 0;
$sql = "SELECT COUNT(DATA_ID) FROM tbNDBCMeteorologicalData";
$result = mysql_query($sql, $con) or die(mysql_error());
$meteorologicalCount = $row["COUNT(DATA_ID)"];
 
// Get folder with data via FTP
$dataFTPFolder = "ftp://divethesitecrons:88iNuzKJ@divethesite.com/www.ndbc.noaa.gov/data/realtime/";

if ($handle = opendir($dataFTPFolder)) {
    while (false !== ($file = readdir($handle))) {
        if ($file != "." && $file != "..") {
			$filename = $dataFTPFolder.$file;
			$fp = fopen($filename, 'r');
			
			// Get file extension first, format depends on it
			$info = pathinfo( $filename );
			$name = $info['filename'];
			$ext  = $info['extension'];

			$name = pathinfo($filename)['filename'];
			$ext = pathinfo($filename)['extension'];
			
			if ($ext == "txt") {
				// Standard meteorological data
					// First row is column heading, second is units
				$line = fgets($fp, 2048);
				$line = fgets($fp, 2048);
				
				while (!feof($fp)){
				    $line = fgets($fp, 2048);

				    $data = preg_split('/\s+/', $line);
				
					// If any values are MM, make blank
					for ($i = 0; $i < count($data); ++$i) {
						if ($data[i] == "MM") {
							$data[i] = "";
						}
					}

					$stationNumber = $name;
					$year = $data[0];
					$month = $data[1];
					$day = $data[2];
					$hour = $data[3];
					$minute = $data[4];
					
					$dateTime = mktime($hour, $minute, 0, $month, $day, $year);
			
					$windDirecction = $data[5];
					$windSpeed = $data[6];
					$windGust = $data[7];
					$significantWaveHeight = $data[8];
					$dominantWavePeriod = $data[9];
					$averageWavePeriod = $data[10];
					$dominantWaveDirection = $data[11];
					$seaLevelPressure = $data[12];
					$airTemperature = $data[13];
					$waterTemperature = $data[14];
					$dewPointTemperature = $data[15];
					$stationVisibility = $data[16];
					$pressureTendency = $data[17];
					$tide = $data[18];
					
					if ($meteorologicalCount == $DATA_LIMIT_COUNT) {
						// Delete earliest entry before inserting
						$sql = "DELETE FROM tbNDBCMeteorologicalData 
								ORDER BY DATA_ID ASC 
								LIMIT 1";
								
						mysql_query($sql, $con) or die(mysql_error());
							
					}
					 
					$sql = "INSERT INTO tbNDBCMeteorologicalData
				            (STATION_ID, STATION_NAME, DATA_TIME, WIND_DIRECTION, WIND_SPEED, WIND_GUST, SIGNIFICANT_WAVE_PERIOD, 
							 DOMINANT_WAVE_PERIOD, AVERAGE_WAVE_PERIOD, DOMINANT_WAVE_DIRECTION, SEA_LEVEL_PRESSURE, 
							 AIR_TEMPERATURE, WATER_TEMPERATURE, DEW_POINT_TEMPERATURE, STATION_VISIBILITY, PRESSURE_TENDENCY, TIDE)
				            VALUES
				            ((SELECT STATION_ID FROM tbNDBCStations WHERE NAME = \"$stationNumber\"),\"$stationNumber\",
							 FROM_UNIXTIME($dateTime),\"$windDirection\",\"$windSpeed\",\"$windGust\",\"$significantWavePeriod\",
							 \"$dominantWavePeriod\",\"$averageWavePeriod\",\"$dominantWaveDirection\",\"$seaLevelPressure\",
							 \"$airTemperature\",\"$waterTemperature\",\"$dewPointTemperature\",\"$stationVisibility\",\"$pressureTendency\",\"$tide\")";
					
					mysql_query($sql, $con) or die(mysql_error());
				}
				   
			}  else if ($ext == "drift"){
				// Meteorological data from drifting buoys
					// First row is column heading, second is units
				$line = fgets($fp, 2048);
				$line = fgets($fp, 2048);
				
				while (!feof($fp)){
				    $line = fgets($fp, 2048);
					
					$data = preg_split('/\s+/', $line);
					
					// If any values are MM, make blank
					for ($i = 0; $i < count($data); ++$i) {
						if ($data[i] == "MM") {
							$data[i] = "";
						}
					}
					
					$stationNumber = $name;
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
					$windDirection = $data[6];
					$windSpeed = $data[7];
					$windGust = $data[8];
					$seaLevelPressure = $data[9];
					$pressureTendency = $data[10];
					$airTemperature = $data[11];
					$waterTemperature = $data[12];
				}
				
			} else if ($ext == "spec") {
				// Spectral wave summaries
					// First row is column heading, second is units
				$line = fgets($fp, 2048);
				$line = fgets($fp, 2048);
				
				while (!feof($fp)){
				    $line = fgets($fp, 2048);

				    $data = preg_split('/\s+/', $line);
					
					// If any values are MM, make blank
					for ($i = 0; $i < count($data); ++$i) {
						if ($data[i] == "MM") {
							$data[i] = "";
						}
					}
					
					$stationNumber = $name;
					$year = $data[0];
					$month = $data[1];
					$day = $data[2];
					$hour = $data[3];
					$minute = $data[4];	
								
					$dateTime = mktime($hour, $minute, 0, $month, $day, $year);
					
					$waveHeight = $data[5];
					$swellHeight = $data[6];
					$swellPeriod = $data[7];
					$windWaveHeight = $data[8];
					$windWavePeriod = $data[9];
					$swellDirection = $data[10];
					$windWaveDirection = $data[11];
					$waveSteepness = $data[12];
					$averageWavePeriod = $data[13];
					$dominantWaveDirection = $data[14];
				}
			} else if ($ext == "ocean") {
				// Oceanographic data
					// First row is column heading, second is units
				$line = fgets($fp, 2048);
				$line = fgets($fp, 2048);
				
				while (!feof($fp)){
				    $line = fgets($fp, 2048);

				    $data = preg_split('/\s+/', $line);
			
					// If any values are MM, make blank
					for ($i = 0; $i < count($data); ++$i) {
						if ($data[i] == "MM") {
							$data[i] = "";
						}
					}
					
					$stationNumber = $name;
					$year = $data[0];
					$month = $data[1];
					$day = $data[2];
					$hour = $data[3];
					$minute = $data[4];	
								
					$dateTime = mktime($hour, $minute, 0, $month, $day, $year);
					
					$depthMeasurements = $data[5];
					$oceanTemperature = $data[6];
					$conductivity = $data[7];
					$salinity = $data[8];
					$oxygenConcentrationPercent = $data[9];
					$oxygenConcentrationPartsMillion = $data[10];
					$chlorophyllConcentration = $data[11];
					$turbidity = $data[12];
					$pH = $data[13];
					$Eh = $data[14];
				}
			}

			fclose($fp);
        }
    }
    closedir($handle);
}

?>
        