var net = require('net');
var app = require('express')();
var http = require('http').Server(app);
var io = require('socket.io')(http);
var storage = require('node-persist');
var bodyParser = require('body-parser');
var connections = 0;
var device;
var HOST = '127.0.0.1';
var HTTP_PORT = 3000
var SOCKET_PORT = 3001;
var sockets = [];

// init storage
storage.initSync();

// enable body-parser to handle POST
app.use(bodyParser.json());
app.use(bodyParser.urlencoded({ extended: true }));

// check if device has already been named
//device = storage.getItem('device');
//if(device === undefined) {
	storage.setItem('device','Whippy Dip');
	device = storage.getItem('device');
//}

app.get('/', function (req, res) {
	res.sendFile(__dirname + '/index.html');
});

app.route('/device')
	.get(function(req, res) {
		res.send({device: device, connections: connections});
	})
	.post(function(req, res) { // handle request to update device name
		device = req.body.name;
		console.log(device);
		storage.setItem('device',device);
});

// track number of connections
io.on('connection', function(socket) {
	connections++;
	socket.on('disconnect', function() {
		connections--;
	});
});

// listen for HTTP requests
http.listen(HTTP_PORT, function () {
  console.log('Example app listening on port 3000!');
});


// listen for socket requests to keep track of connections
net.createServer(function(sock) {
    
    // We have a connection - a socket object is assigned to the connection automatically
    console.log('CONNECTED: ' + sock.remoteAddress +':'+ sock.remotePort);
 
    // Add a 'data' event handler to this instance of socket
    sock.on('data', function(data) {
        
        console.log('DATA ' + sock.remoteAddress + ': ' + data);
        // Write the data back to the socket, the client will receive it as data from the server
	console.log(data);
	var json = JSON.parse(data);
        
	if (json.eventId === '0') {
		sockets.push(sock);                                                                                                        
    		console.log('Added index: ' + sockets.indexOf(sock));                                                                      
    		var eventObj = {eventId: 1, connections: connections};                                                                     
    		var json = JSON.stringify(eventObj);                                                                                       
    		sock.write(json + '\n'); 
	} else if (json.eventId === '1') {
		connections++;
		
		if (sockets.length === 0) {
 	               return;
		}
  
		var eventObj = {eventId: 1, connections: connections};
		var json = JSON.stringify(eventObj);                                                                                                                    
		sockets.forEach(function(socket, index, array) {
                	socket.write(json + '\n');
		}); 
	} else if (json.eventId === '2' || json.eventId === '3') {
		if (json.eventId === '3')
			connections--;
	
        	sockets.splice(sockets.indexOf(sock), 1);                                                                              
	
		if (sockets.length === 0) {
 	               return;
		}
  
		var eventObj = {eventId: 1, connections: connections};
		var json = JSON.stringify(eventObj);                                                                                                                    
		sockets.forEach(function(socket, index, array) {
                	socket.write(json + '\n');
		}); 
	}
    });

    // Add a 'close' event handler to this instance of socket
    sock.on('close', function(data) {
        // console.log('CLOSED: ' + sock.remoteAddress +' '+ sock.remotePort);

	// console.log('Index of removed: ' + sockets.indexOf(sock));
	// sockets.splice(sockets.indexOf(sock), 1);
        //console.log('Sockets left: ' + sockets.length);	
	
	//if (sockets.length === 0) {
        //        return;
	//}
  
	//var eventObj = {eventId: 1, connections: connections};
	//var json = JSON.stringify(eventObj);                                                                                                                    
	//sockets.forEach(function(socket, index, array) {
	//	if (socket !== 'undefined' && socket !== null)
        //        sock.write(json + '\n');
	//}); 

    });
    
}).listen(SOCKET_PORT);

console.log('Server listning on ' + HOST +':'+ SOCKET_PORT);
