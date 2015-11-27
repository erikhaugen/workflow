/*global xtag, workflow_lib_Common, workflow_api_ProcessResource */
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
(function () {
    var processResource = null;
    var processId = null;
    function formOnSubmit(event) {
        var request = event.detail.request;
        //alert(JSON.stringify(event.detail.data, null, 4));
        if (request.status < 400) {
            document.location.reload();
        } else {
            alert(request.responseText);
        }
    }
    xtag.addEvent(document, 'submitted:delegate(form[is=form-ajax])', formOnSubmit);
    function reevaluateAssignmentsBtnOnClick(event) {
        processResource.reevaluateAssignments(processId)
                .then(workflow_lib_Common.checkStatus)
                .then(workflow_lib_Common.reloadWindow)
                .catch(workflow_lib_Common.errorHandler);
    }
    function migrateProcessBtnOnClick(event) {
        processResource.migrateProcess(processId)
                .then(workflow_lib_Common.checkStatus)
                .then(function(response){return response.text();})
                .then(function(responseText){ alert(responseText)})
                .catch(workflow_lib_Common.errorHandler);
    }
    function checkProceessDeadlinesBtnOnClick(event) {
        processResource.checkProcessDeadlines(processId)
                .then(workflow_lib_Common.checkStatus)
                .then(workflow_lib_Common.reloadWindow)
                .catch(workflow_lib_Common.errorHandler);
    }



    window.addEventListener('load', function () {
        var arr = window.location.toString().match(/\/processes\/([^?]*)/);
        processId = arr[1];
        var baseAddress = document.location.toString().substr(0, document.location.toString().indexOf("/workflow/") + 13) + "/processes";
        processResource = new workflow_api_ProcessResource(baseAddress);

        document.querySelector("#reevaluateAssignmentsBtn").addEventListener("click", reevaluateAssignmentsBtnOnClick);
        document.querySelector("#migrateProcessBtn").addEventListener("click", migrateProcessBtnOnClick);
        document.querySelector("#checkProceessDeadlinesBtn").addEventListener("click", checkProceessDeadlinesBtnOnClick);

    });

}());