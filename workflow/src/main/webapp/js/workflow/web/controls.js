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
    window.addEventListener('load', function () {
        var buttons = document.querySelectorAll("button.remove-element");
        for (var i = 0; i < buttons.length; i++) {
            var button = buttons[i];
            button.onclick = function () {
                onClickRemoveButton(this);
            };
        }
        if (document.querySelector(".add-element") !== null) {
            document.querySelector(".add-element").addEventListener('click', function () {
                var elementLi = document.createElement("li");
                var input = document.createElement("input");
                input.tyle = "text";
                input.className = "pure-input-1-2";
                input.name = "ManualActivityInstance[DataInstances][StringArrayDataInstance][0][StringValue][" + (this.parentNode.querySelector("ol").getElementsByTagName("li").length) + "][@Value]";
                elementLi.appendChild(input);
                elementLi.appendChild(document.createTextNode("\n"));
                var button = document.createElement("button");
                button.type = "button";
                button.className = "pure-button remove-element";
                button.innerHTML = " - ";
                button.onclick = function () {
                    onClickRemoveButton(this);
                };
                elementLi.appendChild(button);
                this.parentNode.querySelector("ol").appendChild(elementLi);
            });
        }
    });


    function onClickRemoveButton(button) {
        if (button !== null) {
            var inputs = button.closest("ol").getElementsByTagName("input");
            button.closest("ol").removeChild(button.closest("li"));
            for (var j = 0; j < inputs.length; j++) {
                var input = inputs[j];
                input.name = "ManualActivityInstance[DataInstances][StringArrayDataInstance][0][StringValue][" + j + "][@Value]";
            }
        }
    }

}());