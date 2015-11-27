/*global xtag, workflow_api_ProcessResource, workflow_lib_Common */
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
var processResource = null;
var processId = null;
var activityId = null;
(function () {
    var process, activity;
    document.querySelector("#activityForm").addEventListener("submitted", function (event) {
        var request = event.detail.request;
        //alert(JSON.stringify(event.detail.data, null, 4));
        if (request.status < 400) {
            var location = request.getResponseHeader("Location");
            if (location) { //переход на следующий этап
                document.location = location;
            } else {
                var newstate = event.detail.data.ManualActivityInstance["@State"];
                if (window.location !== window.parent.location) {
                    window.parent.postMessage({
                        eventName: newstate ? "WorkflowActivityFinished" : "WorkflowActivitySaved",
                        args: {}
                    }, "*");
                    window.parent.postMessage({
                        eventName: newstate ? "workflow.activityFinished" : "workflow.activitySaved",
                        args: {}
                    }, "*");

                } else if (newstate) {
                    var webrootpath = document.location.toString().substr(0, document.location.toString().indexOf("/workflow/") + 9);
                    document.location = webrootpath + "/web/processes/workList";
                } else {
                    document.location.reload();
                }
            }
        } else {
            alert(request.responseText);
        }
    }
    );
    xtag.addEvent(document, 'submitted:delegate(form[id=commentsForm])', function (event) {
        document.querySelector('#commentsPanel').src = '../comments?_random=' + Math.random().toString();
    });
    window.addEventListener('load', function () {
        var arr = window.location.toString().match(/\/processes\/(.*)\/activities\/([^?\/]*)/);
        processId = arr[1];
        activityId = arr[2];
        var baseAddress = document.location.toString().substr(0, document.location.toString().indexOf("/workflow/") + 13) + "/processes";
        processResource = new workflow_api_ProcessResource(baseAddress);
    });
    window.addEventListener('unload', function () {

    });
    window.addEventListener('message', function (event) {
        switch (event.data.eventName) {
            case "Form.enableComplete":
            case "workflow.enableComplete":
                enableFormComplete(event.data.args);
                break;
            case "Form.setValue":
            case "workflow.setValue":
                formSetParams(event.data.args);
                break;
            case "workflow.editActivity":
                workflowEditActivity(event.data.args);
                break;
        }
    }, false);
    function enableFormComplete(args) {
        var el = document.querySelector("button[value='closed.completed']");
        var currentEnable = el.getAttribute("disabled") == "disabled" ? false : true;
        if (currentEnable != args.enable) {
            if (args.enable) {
                el.removeAttribute("disabled");
            } else {
                el.setAttribute("disabled", "disabled");
            }
            //сохраняем enableComplete
            if (typeof args.persist != 'undefined') {
                if (args.persist == true) {
                    var req = {
                        "ManualActivityInstance": {
                            "DataInstances": {
                                "BooleanDataInstance": {
                                    "@Id": "enableComplete",
                                    "@Value": args.enable
                                }
                            }
                        }
                    };
                    processResource.editActivity(processId, activityId, req)
                            .then(workflow_lib_Common.checkStatus)
                            .catch(workflow_lib_Common.errorHandler);
                }
            }
        }
    }
    function formSetParams(params) {
        for (var a in params) {
            var elements = window.document.querySelectorAll('input[type="hidden"][value="' + a + '"]');
            if (elements.length == 1) {
                var element = elements.item(0).nextElementSibling;
                if (typeof element !== 'undefined') {
                    element.value = params[a];
                    var event = document.createEvent('HTMLEvents');
                    event.initEvent('change', true, false);
                    element.dispatchEvent(event);
                }
            }
        }
    }
    function workflowEditActivity(params) {
        alert(processResource.processes._.activities._);
        var req = {"ManualActivityInstance": {
                "DataInstances": params.DataInstances
            }
        };
        processResource.processes._.activities._.put(processId, activityId)({"data": JSON.stringify(req), "headers": {"Content-Type": "application/json"}})
                .then(function (value) {
                    console.log('Success!', value);
                }, function (reason) {
                    throw new Error(reason);
                });
    }

}());

