function setCurrentDateToDatePicker() {
    // document.getElementById("dateField").valueAsDate = new Date();
    document.getElementById("dateField").max = new Date(new Date().getTime() - new Date().getTimezoneOffset() * 60000).toISOString().split("T")[0];
}

function increaseDay() {
    let tmp = document.getElementById("dateField").value
    let currentDate = new Date()
    currentDate.setHours(0, 0, 0, 0);
    let newDate = Date.parse(tmp)
    if (currentDate > newDate) {
        newDate += 86400000
        document.getElementById("dateField").valueAsDate = new Date(newDate)
    }

}

function decreaseDay() {
    let tmp = document.getElementById("dateField").value
    let newDate = Date.parse(tmp) - 86400000
    document.getElementById("dateField").valueAsDate = new Date(newDate)
    // document.getElementById("sendDataBtn").style.display = "none"

}

function getDailyTotal() {
    document.forms["form"].method = "get"
    document.forms["form"].submit()
}

function successSubmit() {
    alert("Success!")
}

//
// function setDate() {
//     let date = document.getElementById("dateField").value
//     document.getElementById("hiddenDate").valueAsDate = date
//     console.log(date)
// }