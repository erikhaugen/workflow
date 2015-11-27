/*global Calendar */
(function () {
    xtag.register('dynarch-calendar', {
        extends: 'input',
        lifecycle: {
            inserted: function () {
                if (!this.id) {
                    this.id = "calendar_" + Math.random().toString();
                }

                Calendar.setup({
                    inputField: this.id,
                    ifFormat: this.ifFormat?this.ifFormat:(this.showsTime?"%Y-%m-%d %H:%M:%S":"%Y-%m-%d"),
                    showsTime: this.showsTime,
                    button: this.id,
                    singleClick: true,
                    firstDay: 1
                });


            }
        },
        accessors: {
            showsTime: {
                attribute: {name:'shows-time',boolean: true} // creates a boolean attribute.  enabled=""
            },
            ifFormat: {
                attribute: {name: 'if-format'} // use a different attribute name
            }
        }
    });
})();