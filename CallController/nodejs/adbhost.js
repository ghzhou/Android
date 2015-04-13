var net = require('net');
var exec = require('child_process').exec;

net.createServer(function(sock) {
    
    console.log('CONNECTED: ' + sock.remoteAddress +':'+ sock.remotePort);
    
    // Add a 'data' event handler to this instance of socket
    sock.on('data', function(data) {
        
        console.log('DATA ' + sock.remoteAddress + ': ' + data);
        if (data.toString()==='A'){
            console.log("Answering...");
            exec("D:\\android-sdk\\platform-tools\\adb.exe shell input keyevent KEYCODE_CALL");
            setTimeout(function() {
                console.log("then rejected");
                exec("D:\\android-sdk\\platform-tools\\adb.exe shell input keyevent KEYCODE_ENDCALL");
            }, 2010);

        }
        else if(data.toString()==='R'){
            console.log("Rejecting...");
            exec("D:\\android-sdk\\platform-tools\\adb.exe shell input keyevent KEYCODE_ENDCALL");
        }
        
    });
    
    sock.on('close', function(data) {
        console.log('CLOSED: ' + sock.remoteAddress +' '+ sock.remotePort);
    });
}).listen(62001);
