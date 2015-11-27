/**
 * Copyright (C) 2015 Bystrobank, JSC
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see http://www.gnu.org/licenses
 */
var workflow_lib_Common = (function () {
    return {
        errorHandler: function (error) {
            alert(error);
            console.log(error);
            console.log(error.response);
        },
        checkStatus: function (response) {
            //check status
            if (response.status >= 200 && response.status < 300) {
                return response;
            } else {
                var error = new Error(response.status+": "+  response.statusText + " url: "+response.url);
                error.response = response;
                throw error;
            }

        },
        reloadWindow: function (response) {
            window.location.reload();
        },
        responseText: function(response){
            return response.text();
        },
        responseJson: function(response){
            return response.json();
        }
    };
})();

