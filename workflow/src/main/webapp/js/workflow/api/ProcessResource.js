/*global fetch */
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
function workflow_api_ProcessResource(baseAddress) {
    this.baseAddress=baseAddress;

    this.editActivity = function (processId, activityId, activityinstance) {
        var url = this.baseAddress + "/" + processId + "/activities/" + activityId;
        var opts = {
            method: 'put',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(activityinstance)
        };
        return fetch(url, opts);
    };
    this.startActivity = function (processId, activityId) {
        var url = this.baseAddress + "/" + processId + "/activities/" + activityId+"/start";
        var opts = {
            method: 'post'
        };
        return fetch(url, opts);
    };

    this.reevaluateAssignments = function (processId) {
        var url = this.baseAddress + "/" + processId + "/reevaluateAssignments";
        var opts = {
            method: 'post'
        };
        return fetch(url, opts);
    };
    this.checkProcessDeadlines = function (processId) {
        var url = this.baseAddress + "/" + processId + "/checkDeadlines";
        var opts = {
            method: 'post'
        };
        return fetch(url, opts);
    };

    this.migrateProcess = function (processId) {
        var url = this.baseAddress + "/" + processId + "/migrate";
        var opts = {
            method: 'post'
        };
        return fetch(url, opts);
    };

};