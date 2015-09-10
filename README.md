# ha-bridge
Emulates philips hue api to other home automation gateways.  The Amazon echo now supports wemo and philips hue.  
## Build
To customize and build it yourself, build a new jar with maven:  
```
mvn install
```
Otherwise go to http://www.bwssystems.com/apps.html to download the latest jar file.  
## Run
Then locate the jar and start the server with:  
```
java -jar -Dvera.address=192.168.X.Y ha-bridge-0.X.Y.jar
```
## Available Arguments
### -Dvera.address=`<ip address>`
The argument for the vera address should be given as it the system does not have a way to find the address. Supply -Dvera.address=X.Y.Z.A on the command line to provide it.
### -Dupnp.config.address=`<ip address>`
The server defaults to the first available address on the host. Replace the -Dupnp.config.address=`<ip address>` value with the server ipv4 address you would like to use. 
### -Dserver.port=`<port>`
The server defaults to running on port 8080. If you're already running a server (like openHAB) on 8080, -Dserver.port=`<port>` on the command line.
### -Dupnp.device.db=`<filepath>`
The default location for the db to contain the devices as they are added is "data/devices.db". If you would like a different filename or directory, specify -Dupnp.devices.db=`<directory>/<filename> or <filename>` if it is the same directory.
### -Dupnp.resonse.port=`<port>`
The upnp response port that will be used. The default is 50000.  
### -Dupnp.strict=`<true|false>`
Upnp has been very closed on this platform to try and respond as a hue and there is now a setting to control if it is more open or strict, Add -Dupnp.strict=`<true|false>` to your command line to have the emulator respond to what it thinks is an echo to a hue or any other device. The default is upnp.strict=false.
### -Dtrace.upnp=`<true|false>`
Turn on tracing for upnp discovery messages. The default is false.
### -Dvtwo.compatibility=`<true|false>`
Turns on compatibility for upnp detection and response as it was in the original version of amazon-echo-ha-bridge. The default is true. 
## Web Config
Configure by going to the url for the host you are running on or localhost with port you have assigned: 
```
http://<ip address>:<port>
```
## Command line configure
Register a device via REST by  by using a tool and the url, for example:  
```
POST http://host:8080/api/devices
{
"name" : "bedroom light",
"deviceType" : "switch",
  "onUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum=41",
  "offUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=41"
}
```
## Dimming
Dimming is also supported by using the expessions ${intensity.percent} or ${intensity.byte} for 0-100 and 0-255 respectively.    
e.g.
```
{
    "name": "entry light",
    "deviceType": "switch",
    "offUrl": "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=31",
    "onUrl": "http://192.168.1.201:3480/data_request?id=action&output_format=json&DeviceNum=31&serviceId=urn:upnp-org:serviceId:Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget=${intensity.percent}"
}
```
See the echo's documentation for the dimming phrase.

## POST/PUT support
added optional fields
 * contentType (currently un-validated)
 * httpVerb (POST/PUT/GET only supported)
 * contentBody your post/put body here

This will allow control of any other application that may need more then GET.  
e.g: 
```
{
    "name": "test device",
    "deviceType": "switch",
    "offUrl": "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=31",
    "onUrl": "http://192.168.1.201:3480/data_request?id=action&output_format=json&DeviceNum=31&serviceId=urn:upnp-org:serviceId:Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget=${intensity.percent}",
  "contentType" : "application/json",
  "httpVerb":"POST",
  "contentBody" : "{\"fooBar\":\"baz\"}"
}
```
Anything that takes an action as a result of an HTTP request will probably work - like putting Vera in and out of night mode:  
```
{
  "name": "night mode",
  "deviceType": "switch",
  "offUrl": "http://192.168.1.201:3480/data_request?id=lu_action&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=SetHouseMode&Mode=1",
  "onUrl": "http://192.168.1.201:3480/data_request?id=lu_action&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=SetHouseMode&Mode=3"
}
```
## Ask Alexa
After this Tell Alexa: "Alexa, discover my devices"  

Then you can say "Alexa, Turn on the office light" or whatever name you have given your configured devices.  

To view or remove devices that Alexa knows about, you can use the mobile app Menu / Settings / Connected Home  
## Debugging
To turn on debugging for the bridge, use the following extra parm in the command line:
```
-Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG
```