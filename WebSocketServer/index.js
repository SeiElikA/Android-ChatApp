const express = require('express')
const SocketServer = require('ws').Server
const fs = require('fs');
const apn = require('apn');
var request = require('request');

const PORT = 3000
const apiPort = 8000

// ios notification certificates
let options = {
    token: {
        key: "AuthKey_Q2F87YU7W3.p8",
        keyId: "Q2F87YU7W3",
        teamId: "72QP2FGS73"
    },
    production: false
};

// initial
const server = express().listen(PORT, () => console.log(`Listening on ${PORT}`))
const app = express()
const wss = new SocketServer({ server })
const apnProvider = new apn.Provider(options)
app.use(express.json());


function sendNotificationForiOS(identity, message) {
    let note = new apn.Notification()
    note.alert = {
        "title": identity,
        "body": message,
        "launch-image": "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885__480.jpg"
    }
    note.sound = "default"
    note.badge = 1
    note.mutableContent = 1
    note.topic = "edu.wschina.WebSocketExample";

    if(fs.existsSync("device_token.json")) {
        fs.readFile('./device_token.json', 'utf8', (err, data) => {
            if (err) {
                console.error(err);
                return;
            }

            let deviceTokenList = JSON.parse(data)
            apnProvider.send(note, deviceTokenList).then((result) => {

            })
        });
    }

}

function sendNotificationForAndroid(identity, message) {
    if(fs.existsSync("device_token_android.json")) {
        fs.readFile('./device_token_android.json', 'utf8', (err, data) => {
            if (err) {
                console.error(err);
                return;
            }

            // read all device token
            let deviceList = JSON.parse(data)
            deviceList.forEach((result) => {
                // send notification for each android device
                let notificationData = {
                    "to": result,
                    "data" : {
                        "body" : message,
                        "title": identity,
                        "icon": "ic_baseline_notifications_24"
                    }
                }

                let options = {
                    url: "https://fcm.googleapis.com/fcm/send",
                    method: "POST",
                    headers: {
                        'content-type': "application/json",
                        'Authorization': 'key=AAAA89zTOQY:APA91bF76sgcnyfXnUFwUTCPlDgLTdNQmkCDrF1zXHTpvkXPF67rv_KYQVgVEYIFswkIOOx6TlcVnodJlqlcuPdir0eWbKAZfe1mKzC_5wHQ9Nm1p1t3ZaKKx0DTVY17qrjiqzgZzZ9z'
                    },
                    json:notificationData
                }


                request(options, (error, response, body) => {
                    if(error != null)
                        console.log("Send Android notification error:" + error)
                });
            })
        });
    }

}

// web socket connection event
wss.on('connection', ws => {
    console.log('Client connected ')

    ws.on('message', data => {
        // check data is correct
        let messageData = null
        try {
            messageData = JSON.parse(String(data))
        } catch (e) {
            ws.send("Data format error:  {\"identity\":\"UUID 8 length\", \"message\": \"example\"}")
            return
        }

        // format message data
        let identity = messageData["identity"] ?? ""
        let message = messageData["message"] ?? ""
        let date = new Date()
        let dateTime = date.getFullYear() + "/" + date.getMonth() + "/" + date.getDay() + " " + date.getHours() + ":" + date.getMinutes()
        let resultObj = {"identity": identity, "message": message, "dateTime": dateTime}

        // read chat history file
        if(!fs.existsSync("chat_history.json")) {
            // if not creat file, create it and write "[]"
            let stream = fs.createWriteStream("chat_history.json");
            stream.once('open', function(fd) {
                stream.write("[]");
                stream.end();
            });
        }

        fs.readFile('./chat_history.json', 'utf8', (err, data) => {
            if(err) {
                console.error(err);
                return;
            }

            let msgList = JSON.parse(data)
            msgList.push(resultObj)

            // save chat content in json file
            let stream = fs.createWriteStream("chat_history.json");
            stream.once('open', function(fd) {
                stream.write(JSON.stringify(msgList, null, 2));
                stream.end();
            });
        });


        // send message to all client
        console.log(JSON.stringify(resultObj))
        sendNotificationForiOS(identity, message)
        sendNotificationForAndroid(identity, message)
        wss.clients.forEach(client => {
            client.send(JSON.stringify(resultObj))
        })
    })

    ws.on('close', () => {
        console.log('Close connected ')
    })
})

// get chat history
app.get('/history', function (req, res) {
    if(!fs.existsSync("chat_history.json")) {
        res.send("[]")
    }
    fs.readFile('./chat_history.json', 'utf8', (err, data) => {
        if (err) {
            console.error(err);
            return;
        }

        res.send(JSON.parse(data))
    });
})

// save device token to json file for ios
app.post("/sendDeviceToken", function (req, res) {
    let token = ""
    try {
        token = req.body["deviceToken"]
    } catch (e) {
        res.status(400).send({"error" : "your body need include 'deviceToken'"})
        return
    }

    if(token === "" || token === undefined) {
        res.status(400).send({"error": "deviceToken can't empty."})
        return
    }

    if(!fs.existsSync("./device_token.json")) {
        // if not creat file, create it and write "[]"
        let stream = fs.createWriteStream("device_token.json");
        stream.once('open', function(fd) {
            stream.write("[]");
            stream.end();

            writeFile()
        });
        return
    }

    function writeFile() {
        let deviceTokenList = []

        fs.readFile('./device_token.json', 'utf8', (err, data) => {
            if (err) {
                console.error(err);
                return;
            }

            deviceTokenList = JSON.parse(data)
            deviceTokenList.push(token)

            let stream = fs.createWriteStream("device_token.json");
            stream.once('open', function(fd) {
                stream.write(JSON.stringify([...new Set(deviceTokenList)], null, 2));
                stream.end();
                res.sendStatus(200)
            });
        });
    }

    writeFile()
})

// save device token to json file for android
app.post("/sendDeviceTokenAndroid", function (req, res) {
    let token = ""
    try {
        token = req.body["deviceToken"]
    } catch (e) {
        res.status(400).send({"error" : "your body need include 'deviceToken'"})
        return
    }

    if(token === "" || token === undefined) {
        res.status(400).send({"error": "deviceToken can't empty."})
        return
    }

    if(!fs.existsSync("./device_token_android.json")) {
        // if not creat file, create it and write "[]"
        let stream = fs.createWriteStream("device_token_android.json");
        stream.once('open', function(fd) {
            stream.write("[]");
            stream.end();

            writeFile()
        });
        return
    }

    function writeFile() {
        let deviceTokenList = []

        fs.readFile('./device_token_android.json', 'utf8', (err, data) => {
            if (err) {
                console.error(err);
                return;
            }

            deviceTokenList = JSON.parse(data)
            deviceTokenList.push(token)

            let stream = fs.createWriteStream("device_token_android.json");
            stream.once('open', function(fd) {
                stream.write(JSON.stringify([...new Set(deviceTokenList)], null, 2));
                stream.end();
                res.sendStatus(200)
            });
        });
    }

    writeFile()
})

// start listen
app.listen(apiPort, () => console.log(`Listening on ${apiPort}`))