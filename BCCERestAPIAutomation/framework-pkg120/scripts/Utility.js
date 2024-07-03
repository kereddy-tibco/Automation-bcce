function firstFn(name){
	print("This Method written in script file : "+name);
	return "return value";
}

// This functions is return current date in dd-mm-yyyy format.
function getCurrentDate(){
    var today = new Date();
    var dd = today.getDate();
    var mm = today.getMonth()+1; //January is 0!
    var yyyy = today.getFullYear();
    if(dd<10){
        dd='0'+dd;
    }
    if(mm<10){
        mm='0'+mm;
    }
    return dd+'/'+mm+'/'+yyyy;
}

function getCurrentDate_New(){

    return '2011-02-20';
}