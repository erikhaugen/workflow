module.exports = function (grunt) {

    grunt.initConfig({
        'smush-components': {
            options: {
                fileMap: {
                    js: './src/main/webapp/js/bower_components.js',
                    css: './src/main/webapp/css/bower_components.css'
                }
            }
        }
    });

    grunt.loadNpmTasks('grunt-smush-components');
    grunt.registerTask('default', [ 'smush-components' ]);
};