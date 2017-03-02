<?php
	require_once('dbConn.php');
	if($_SERVER['REQUEST_METHOD']=='GET'){
		 //no parameter has been passed in the url
		 //$id  = $_GET['id'];
		 
		 //this is the query
		 $sql = "SELECT COURSE_NAME FROM ".$DB.".COURSE;";
		 
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
