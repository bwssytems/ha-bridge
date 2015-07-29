# amazon-echo-ha-bridge-compact
Emulates philips hue api to other home automation gateways.  The Amazon echo now supports wemo and philip hue.
Build
-----

To customize and build it yourself, build a new jar with maven:
```
mvn install
```
Then locate the jar and start the server with:
```
java -jar -Dupnp.config.address=192.168.1.Z amazon-echo-bridge-compact-0.X.Y.jar
```
replace the -Dupnp.config.address value with the server ipv4 address.

The server defaults to running on port 8080. If you're already running a server (like openHAB) on 8080, -Dserver.port=XXXX on the command line.

Then configure by going to the url: 
```
http://192.168.1.240:8080
```
or Register a device, via REST by binding some sort of on/off (vera style) url
```
POST http://host:8080/api/devices/
{
"name" : "bedroom light",
"deviceType" : "switch",
  "onUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum=41",
  "offUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=41"
}
```

After this Tell Alexa: "Alexa, discover my devices"

Then you can say "Alexa, Turn on the office light" or whatever name you have given your configured devices.

To view or remove devices that Alexa knows about, you can use the mobile app Menu / Settings / Connected Home
