/*global Calendar */
(function () {
    xtag.register('input-filtered', {
        extends: 'input',
        events: {
            blur: function () {
                if (this.filterChars) {
                    var match = this.filterChars.match(new RegExp('^/(.*?)/([gimy]*)$'));
                    var regex = new RegExp(match[1], match[2]);
                    this.value=this.value.replace(regex, this.replaceTo?this.replaceTo:'');
                }
            }
        },
        accessors: {
            filterChars: {
                attribute: {name: 'filter-chars'}
            },
            replaceTo: {
                attribute: {name: 'replace-to'}
            }

        }
    });
})();