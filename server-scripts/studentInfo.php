<?php
	require_once('dbConn.php');
	if($_SERVER['REQUEST_METHOD']=='GET'){
		 $sql = "SELECT STUDENT_ID, STUDENT_NAME, BATCH, TAGKEY FROM ".$DB.".STUDENT;";
		 
		 $r=$conn->query($sql);
		 
		 $result = array();
		 if ($r->num_rows > 0) {
			// output data of each row
			while($row = $r->fetch_assoc()) {
				array_push($result, $row['COURSE_NAME']);
			}
		 } else {
		 array_push($result, array("errorMessage"=>"Could find any rows"));
		 }
		
		 echo json_encode(array("list"=>$result));
	 }else{
	 	echo "Something went wrong";
	 }
	 //to close the connection
	 mysqli_close($conn);	
?>