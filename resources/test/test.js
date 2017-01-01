phantom.onError = function(msg, trace) {
    var msgStack = ['PHANTOM ERROR: ' + msg];
    if (trace && trace.length) {
        msgStack.push('TRACE:');
        trace.forEach(function(t) {
            msgStack.push(' -> ' + (t.file || t.sourceURL) + ': ' + t.line + (t.function ? ' (in function ' + t.function +')' : ''));
        });
    }
    console.log(msgStack.join('\n'));
    phantom.exit(1);
};

var page = require("webpage").create();

function exit(code) {
    if (page) {
        page.close();
    }
    setTimeout(function() {
        phantom.exit(code);
    }, 0);
}

page.onError = function(msg, trace) {
    var msgStack = ['ERROR: ' + msg];
    if (trace && trace.length) {
        msgStack.push('TRACE:');
        trace.forEach(function(t) {
            msgStack.push(' -> ' + t.file + ': ' + t.line + (t.function ? ' (in function "' + t.function +'")' : ''));
        });
    }
    console.log(msgStack.join('\n'));
    exit(1);
};

page.onConsoleMessage = function(msg) {
    console.log(msg);
};

page.onCallback = function(successful) {
    if (successful) {
        exit(0);
    } else {
        exit(1);
    }
};

page.open(require("system").args[1], function(status) {
    page.evaluate(function() {
        tenforty.test.run(function(successful) {
            window.callPhantom(successful);
        });
    });
});
