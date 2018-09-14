/*
 * Copyright 2018 Alexandre Roman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function init() {
    checkConnection();
}

function checkConnection() {
    var xhttp = new XMLHttpRequest();
    xhttp.onreadystatechange = function() {
        if (this.readyState == 4) {
            if(this.status == 200 || this.status == 503) {
                var jsonResult = JSON.parse(this.responseText);
                if(jsonResult.state == "SUCCESS") {
                    showResult(0, jsonResult.message);
                } else if(jsonResult.state == "BAD_CONFIG") {
                    showResult(1, jsonResult.message);
                } else {
                    showResult(2, jsonResult.message);
                }
            } else {
                showResult(2, null);
            }
        }
    };
    xhttp.open("GET", "/status", true);
    xhttp.setRequestHeader("Content-type", "application/json");
    xhttp.send();
}

function showResult(result, message) {
    var emoji = "";
    var status = "";
    switch (result) {
        case 0:
            emoji = "😁";
            status = "Success!";
            break;
        case 1:
            emoji = "😞";
            status = "There's a problem with your configuration";
            break;
        case 2:
            emoji = "😭";
            status = "Oops: no connection was made";
            break;
    }
    if(message != null && message != "") {
        status = message;
    }
    document.getElementById("result-emoji").innerText = emoji;
    document.getElementById("result-text").innerText = status;
}
