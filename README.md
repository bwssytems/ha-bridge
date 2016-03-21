# ha-bridge
Emulates Philips Hue api to other home automation gateways such as an Amazon Echo.  The Bridge handles basic commands such as "On", "Off" and "brightness" commands of the hue protocol.  This bridge can control most devices that have a distinct API. In the cases of systems that require authorization and/or have API's that cannot be handled in the current method, a module may need to be built. The Harmony Hub is such a module and so is the Nest module. The Bridge has helpers to build devices for the gateway for the Logitech Harmony Hub, Vera, Vera Lite or Vera Edge and the Nest. Alternatively the Bridge supports custom calls as well.
## Build
To customize and build it yourself, build a new jar with maven:  
```
mvn install
```
Otherwise, downloads are available at https://github.com/bwssytems/ha-bridge/releases.  
## Run
Then locate the jar and start the server with:  
```
java -jar ha-bridge-W.X.Y.jar
```
## Available Arguments
Arguments are now depecated. The ha-bridge will use the old -D arguments and populate the config screen which can now be saved to a file and will not be needed. There is only one optional argument that overides and that is the location of the config file. The default is the relative path "data/habridge.config".
### -Dconfig.file=`<filepath>`
The default location for the config fileto contain the settings for the bridge is the relative path from where the bridge is started in "data/habridge.config". If you would like a different filename or directory, specify -Dconfig.file=`<directory>/<filename>` explicitly.
## HA Bridge Device Configuration
You must configure devices before you will have any thing for the Echo to receive. The easy way to get devices configures is with the web interface by going to the url for the host you are running on or localhost with port you have assigned: and use the helpers for the Vera or Harmony Hub to create devices that the Echo will find.

Another way to add a device is through the Manual Add Tab. This allows you to manually enter the name, the on and off URLs and select if there are custom handling with the type of call that can be made. This allows for control of anything that has a distinct request that can be executed so you are not limited to the Vera or the Harmony.

The format of these can be the default HTTP request which excecutes the URLs formtted as http://<your stuff here> as a GET. Other options to this are to select the HTTP Verb and add the data type and add a body that is passed with the request. Another option that is detected by the bridge is to use the udp://<ip_address>:<port>/<your stuff here to send a UDP request. If your data for the UDP request is formatted as "0x00F009B9" lexical hex format, the bridge will convert the UDP data into a binary stream to send. Sed the examples inthe Configuration REST API Usage Section to get an idea.

Also, you may want to use the REST API's listed below to configure your devices.

Web Configuration Access
```
http://<ip address>:<port>
```
## Ask Alexa
After this Tell Alexa: "Alexa, discover my devices". If there is an issue you can go to the `Menu / Settings / Connected Home` for the echo on the mobile app or your browser and have Alexa forget all devices and then do the discovery again.

Then you can say "Alexa, Turn on the office light" or whatever name you have given your configured devices.  

Here is the table of items to use to tell Alexa what you want to do:

To do this... |	Say this...
--------------|------------
Discover | “Alexa, discover my devices.”
Turn On | “Alexa, turn on the kitchen light.”
Turn Off | “Alexa, turn off the kitchen light.”
Brighten | “Alexa, brighten the kitchen light.”
Dim | “Alexa, dim the kitchen light.”
Dim | "Alexa, Dim the lights to 40%."
Set | “Alexa, set the kitchen light to 50%.”
Set | "Alexa, Set brightness to 60%."
To view or remove devices that Alexa knows about, you can use the mobile app `Menu / Settings / Connected Home` or go to http://echo.amazon.com/#cards.
## Configuration REST API Usage
This section will describe the REST api available for configuration. The REST body examples are all formatted for easy reading, the actual body usage should be like this:
```
{"var1":"value1","var2":"value2","var3:"value3"}
```
The body should be all in one string and not separated by returns, tabs or spaces. FYI, GET items do not require a body element.  If you would like to see example return of json data for full Harmony Hub configuration if configured, which includes activities and devices, take a look at the resource file config.data. If you are interested in how the json data looks for the HA bridge configuration, after creating a device, look at the data directory for the device.db.
These calls can be accomplished with a REST tool using the following URLs and HTTP Verb types:
### Add a device 
Add a new device to the HA Bridge configuration. There is a basic examples and then three alternate examples for the add. Please note that dimming is supported as well as custom value based on the dimming number given from the echo. This is under the Dimming and Value example.
```
POST http://host:8080/api/devices
```
#### Body Arguments
Name |	Type |	Description | Required
-----|-------|--------------|------------
name | string | A name for the device. This is also the utterance value that the Echo will use. | Required
deviceType | string | This identifies what type of device entry this is. It is used by the system and should be the values of "switch", "scene", "custom", "activity" or "button". | Required
onUrl | string | This is the URL that is executed for an "on" request and is used for the device types of "switch", "scene" and "custom". For "activity" or "button" it is the Json target generated by the Harmony Hub helpers. | Required
offUrl | string | This is the URL that is executed for an "off" request and is used for the device types of "switch", "scene" and "custom". For "activity" or "button" it is the Json target generated by the Harmony Hub helpers. | Required
httpVerb | string | This is used for "custom" calls that the user would like to execute. The values can only be "GET, "PUT", "POST". | Optional
contentType | string | This is an http type string such as "application/text" or "application/xml" or "application/json". | Optional
contentBody | string | This is the content body that you would like to send when executing an "on" request. | Optional
contentBodyOff | string | This is the content body that you would like to send when executing an "off" request. | Optional
#### Basic Example
```
{
"name" : "bedroom light",
"deviceType" : "switch",
  "onUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum=41",
  "offUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=41"
}
```
#### Dimming Control Example
Dimming is also supported by using the expressions ${intensity.percent} for 0-100 or ${intensity.byte} for 0-255 for straight pass trhough of the value.
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

#### Value Passing Control Example
You can control items that require special calculated values using ${intensity.math(<your expression using "X" as the value to operate on>)} i.e. "${intensity.math(X/4)}".    
e.g.
```
{
    "name": "Thermostat,
    "deviceType": "custom",
    "offUrl": "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=10",
    "onUrl": "http://192.168.1.201:3480/data_request?id=action&output_format=json&DeviceNum=10&serviceId=urn:upnp-org:serviceId:Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget=${intensity.math(X/4)}"
}
```
See the echo's documentation for the dimming phrase.

#### POST/PUT Support Example
```
This will allow control of any other application that may need more then GET.  You can also use the dimming and value control commands within the URLs as well.
e.g: 
{
    "name": "test device",
    "deviceType": "custom",
    "offUrl": "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=31",
    "onUrl": "http://192.168.1.201:3480/data_request?id=action&output_format=json&DeviceNum=31&serviceId=urn:upnp-org:serviceId:Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget=${intensity.percent}",
  "httpVerb":"POST",
  "contentType" : "application/json",
  "contentBody" : "{\"fooBar\":\"baz_on\"}"
  "contentBodyOff" : "{\"fooBar\":\"baz_off\"}"
}
```
#### Custom Usage URLs Example
Anything that takes an action as a result of an HTTP request will probably work and you can also use the dimming and value control commands within the URLs as well - like putting Vera in and out of night mode:  
```
{
  "name": "night mode",
  "deviceType": ""custom",
  "offUrl": "http://192.168.1.201:3480/data_request?id=lu_action&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=SetHouseMode&Mode=1",
  "onUrl": "http://192.168.1.201:3480/data_request?id=lu_action&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=SetHouseMode&Mode=3"
}
```
Here is a UDP example that can send binary data.
```
{
  "name": "UDPPacket",
  "deviceType": "custom",
  "offUrl": "udp://192.168.1.1:8899/0x460055",
  "onUrl": "udp://192.168.1.1:8899/0x450055"
}
```
#### Response
Name |	Type |	Description
-----|-------|-------------
id   | number | This is the ID assigned to the device and used for lookup.
name | string | A name for the device. This is also the utterance value that the Echo will use.
deviceType | string | This identifies what type of device entry this is. It is used by the system and should be the values of "switch", "scene", "custom", "activity" or "button".
onUrl | string | This is the URL that is executed for an "on" request and is used for the device types of "switch", "scene" and "custom". For "activity" or "button" it is the Json target generated by the Harmony Hub helpers.
offUrl | string | This is the URL that is executed for an "off" request and is used for the device types of "switch", "scene" and "custom". For "activity" or "button" it is the Json target generated by the Harmony Hub helpers.
httpVerb | string | This is used for "custom" calls that the user would like to execute. The values can only be "GET, "PUT", "POST".
contentType | string | This is an http type string such as "application/text" or "application/xml" or "application/json".
contentBody | string | This is the content body that you would like to send when executing an "on" request.
contentBodyOff | string | This is the content body that you would like to send when executing an "off" request.
```
{
"id" : "12345",
"name" : "bedroom light",
"deviceType" : "switch",
  "onUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum=41",
  "offUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=41"
}
```
### Update a Device 
Update an existing device using it's ID that was given when the device was created and the update could contain any of the fields that are used and shown in the previous examples when adding a device. 
```
POST http://host:port/api/devices/<id>
```
#### Body Arguments
Name |	Type |	Description | Required
-----|-------|--------------|------------
id   | number | This is the ID assigned to the device and used for lookup.
name | string | A name for the device. This is also the utterance value that the Echo will use. | Required
deviceType | string | This identifies what type of device entry this is. It is used by the system and should be the values of "switch", "scene", "custom", "activity" or "button". | Required
onUrl | string | This is the URL that is executed for an "on" request and is used for the device types of "switch", "scene" and "custom". For "activity" or "button" it is the Json target generated by the Harmony Hub helpers. | Required
offUrl | string | This is the URL that is executed for an "off" request and is used for the device types of "switch", "scene" and "custom". For "activity" or "button" it is the Json target generated by the Harmony Hub helpers. | Required
httpVerb | string | This is used for "custom" calls that the user would like to execute. The values can only be "GET, "PUT", "POST". | Optional
contentType | string | This is an http type string such as "application/text" or "application/xml" or "application/json". | Optional
contentBody | string | This is the content body that you would like to send when executing an "on" request. | Optional
contentBodyOff | string | This is the content body that you would like to send when executing an "off" request. | Optional
#### Basic Example
```
{
"id" : "6789",
"name" : "table light",
"deviceType" : "switch",
  "onUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum=41",
  "offUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=41"
}
```
#### Response
```
{
"id" : "6789",
"name" : "table light",
"deviceType" : "switch",
  "onUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum=41",
  "offUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=41"
}
```
### Get All Devices 
Get all devices saved in the HA bridge configuration. 
```
GET http://host:port/api/devices
```
#### Response
Individual entries are the same as a single device but in json list format.
```
[{
"id" : "12345",
"name" : "bedroom light",
"deviceType" : "switch",
  "onUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum=41",
  "offUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=41"
}
{
"id" : "6789",
"name" : "table light",
"deviceType" : "switch",
  "onUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum=41",
  "offUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=41"
}]
```
### Get a Specific Device 
Get a device by ID assigned from creation and saved in the HA bridge configuration. 
```
GET http://host:port/api/devices/<id>
```
#### Response
The response is the same layout as defined in the add device response.
```
{
"id" : "6789",
"name" : "table light",
"deviceType" : "switch",
  "onUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum=41",
  "offUrl" : "http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=41"
}
```
### Delete a Specific Device 
Delete a device by ID assigned from creation and saved in the HA bridge configuration. 
```
DELETE http://host:port/api/devices/<id>
```
#### Response
This call returns a null json "{}".
### Get HA Bridge Version 
Get current version of the HA bridge software. 
```
GET http://host:port/api/devices/habridge/version
```
#### Response
Name |	Type |	Description
-----|-------|-------------
version | string | The version returned by the software.
```
{"version":"1.0.7"}
```
### Get Vera Devices 
Get the list of devices available from the Vera, VeraLite or VeraEdge if configured. Please refer to the <a href="http://wiki.micasaverde.com/index.php/Luup_Sdata">Luup Sdata Structure</a> for the explanation of the devices list returned.
```
GET http://host:port/api/devices/vera/devices
```
#### Response
Name |	Type |	Description
-----|-------|-------------
name | string | The name of the Vera device.
altid | string | Vera internal alternate id
id | string | Vera id for accessing device. This is used in calls to the vera url for control.
category | string | Vera category name.
subcategory | string | Vera subcategory identifier.
room | string | Room name assigned to device.
parent | string | Vera id of the parent device.
status | string | Vera status for identifying on = 1 and off = 2.
level | string | Vera device dim percentage.
state | string | Vera additional state attribute. -1 means nothing is happening. 0, 1, 5, 6 mean that the engine is working. 2 or 3 indicates an error. 4 indicates a successful operation.
comment | string | Comment configured with device. Not always present.
```
[{
	"name":"Couch Left Lamp",
	"altid":"4",
	"id":"6",
	"category":"Dimmable Light",
	"subcategory":"0",
	"room":"Family Room",
	"parent":"1",
	"status":"0",
	"level":"0",
	"state":"-1",
	"comment":"",
	"veraname":"default",
	"veraddress":"192.168.1.2"
},
{
	"name":"Couch Right Lamp",
	"altid":"7",
	"id":"9",
	"category":"Dimmable Light",
	"subcategory":"0",
	"room":"Family Room",
	"parent":"1",
	"status":"0",
	"level":"0",
	"state":"-1",
	"comment":""
	"veraname":"default",
	"veraddress":"192.168.1.2"
}]
```
### Get Vera Scenes 
Get the list of scenes available from the Vera, VeraLite or VeraEdge if configured.  Please refer to the <a href="http://wiki.micasaverde.com/index.php/Luup_Sdata">Luup Sdata Structure</a> for the explanation of the scenes list returned.
```
GET http://host:port/api/devices/vera/scenes
```
#### Response
Name |	Type |	Description
-----|-------|-------------
active | string | 1 if the scene is active, 0 otherwise. 
name | string | The name of the Vera scene.
id | string | Vera id for accessing scene. This is used in calls to the vera url for control.
room | string | Room name assigned to scene.
```
[{
	"active":"1",
	"name":"AccentLightsOff",
	"id":"27",
	"room":"no room"
	"veraname":"default",
	"veraddress":"192.168.1.2"
},
{
	"active":"0",
	"name":"AccentLightsOn",
	"id":"26",
	"room":"no room"
	"veraname":"default",
	"veraddress":"192.168.1.2"
}]
```
### Get Harmony Activities
Get the list of activities available from the Harmony Hub if configured. 
```
GET http://host:port/api/devices/harmony/activities
```
#### Response
Only listing the relevant fields that are needed for control of an activity. The example below is representative of some activities.

One caveat is that the "PowerOff" activity as id "-1" is always present and is the power off activity no matter what activity is active.

Name |	Type |	Description
-----|-------|-------------
label | string | The name of the Harmony activity.
id | integer | The id of the Harmony activity. 
#### Harmony activities data example
```
[{"label":"Watch TV","suggestedDisplay":"Default","id":15823996,"activityTypeDisplayName":"Default","controlGroup":[{"name":"NumericBasic","function":[{"name":"NumberEnter","label":"Number Enter","action":"{\"command\":\"Enter\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Hyphen","label":"-","action":"{\"command\":\"-\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number0","label":"0","action":"{\"command\":\"0\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number1","label":"1","action":"{\"command\":\"1\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number2","label":"2","action":"{\"command\":\"2\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number3","label":"3","action":"{\"command\":\"3\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number4","label":"4","action":"{\"command\":\"4\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number5","label":"5","action":"{\"command\":\"5\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number6","label":"6","action":"{\"command\":\"6\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number7","label":"7","action":"{\"command\":\"7\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number8","label":"8","action":"{\"command\":\"8\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number9","label":"9","action":"{\"command\":\"9\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"Volume","function":[{"name":"Mute","label":"Mute","action":"{\"command\":\"Mute\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"VolumeDown","label":"Volume Down","action":"{\"command\":\"VolumeDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"VolumeUp","label":"Volume Up","action":"{\"command\":\"VolumeUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"Channel","function":[{"name":"PrevChannel","label":"Prev Channel","action":"{\"command\":\"Last\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"ChannelDown","label":"Channel Down","action":"{\"command\":\"ChannelDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"ChannelUp","label":"Channel Up","action":"{\"command\":\"ChannelUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"NavigationBasic","function":[{"name":"DirectionDown","label":"Direction Down","action":"{\"command\":\"DirectionDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"DirectionLeft","label":"Direction Left","action":"{\"command\":\"DirectionLeft\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"DirectionRight","label":"Direction Right","action":"{\"command\":\"DirectionRight\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"DirectionUp","label":"Direction Up","action":"{\"command\":\"DirectionUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Select","label":"Select","action":"{\"command\":\"Select\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"TransportBasic","function":[{"name":"Play","label":"Play","action":"{\"command\":\"Play\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Rewind","label":"Rewind","action":"{\"command\":\"Rewind\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Pause","label":"Pause","action":"{\"command\":\"Pause\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"FastForward","label":"Fast Forward","action":"{\"command\":\"FastForward\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"TransportRecording","function":[{"name":"Record","label":"Record","action":"{\"command\":\"Record\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"TransportExtended","function":[{"name":"FrameAdvance","label":"Frame Advance","action":"{\"command\":\"Slow\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"SkipBackward","label":"Skip Backward","action":"{\"command\":\"Replay\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"SkipForward","label":"Skip Forward","action":"{\"command\":\"Advance\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"NavigationDVD","function":[{"name":"Back","label":"Back","action":"{\"command\":\"Back\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"NavigationDSTB","function":[{"name":"C","label":"C","action":"{\"command\":\"C\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"B","label":"B","action":"{\"command\":\"B\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"D","label":"D","action":"{\"command\":\"D\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"A","label":"A","action":"{\"command\":\"A\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"List","label":"List","action":"{\"command\":\"List\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Live","label":"Live","action":"{\"command\":\"LiveTv\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"TiVo","function":[{"name":"ThumbsDown","label":"Thumbs Down","action":"{\"command\":\"ThumbsDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"ThumbsUp","label":"Thumbs Up","action":"{\"command\":\"ThumbsUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"TiVo","label":"TiVo","action":"{\"command\":\"TiVo\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"NavigationExtended","function":[{"name":"Guide","label":"Guide","action":"{\"command\":\"Guide\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Info","label":"Info","action":"{\"command\":\"Info\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Clear","label":"Backspace","action":"{\"command\":\"Clear\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"ColoredButtons","function":[{"name":"Green","label":"Green","action":"{\"command\":\"Green\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Red","label":"Red","action":"{\"command\":\"Red\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Blue","label":"Blue","action":"{\"command\":\"Blue\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Yellow","label":"Yellow","action":"{\"command\":\"Yellow\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]}],"activityOrder":0,"tuningDefault":true,"fixit":{"29671749":{"id":"29671749","power":"ON","input":"HDMI 4","alwaysOn":false,"relativePower":true},"29695418":{"id":"29695418","power":"OFF","alwaysOn":false,"relativePower":true},"29671742":{"id":"29671742","power":"ON","input":"","alwaysOn":false,"relativePower":false},"29671764":{"id":"29671764","alwaysOn":false,"relativePower":false},"29695438":{"id":"29695438","power":"OFF","alwaysOn":false,"relativePower":true},"29695467":{"id":"29695467","alwaysOn":false,"relativePower":false},"29695485":{"id":"29695485","alwaysOn":false,"relativePower":false}},"type":"VirtualTelevisionN","icon":"userdata: 0x4454e0","baseImageUri":"https://rcbu-test-ssl-amr.s3.amazonaws.com/"},{"label":"PowerOff","suggestedDisplay":"Default","id":-1,"activityTypeDisplayName":"Default","controlGroup":[],"tuningDefault":false,"fixit":{"29671749":{"id":"29671749","power":"OFF","alwaysOn":false,"relativePower":true},"29695418":{"id":"29695418","power":"OFF","alwaysOn":false,"relativePower":true},"29671742":{"id":"29671742","power":"OFF","alwaysOn":false,"relativePower":false},"29671764":{"id":"29671764","alwaysOn":false,"relativePower":false},"29695438":{"id":"29695438","power":"OFF","alwaysOn":false,"relativePower":true},"29695467":{"id":"29695467","alwaysOn":false,"relativePower":false},"29695485":{"id":"29695485","alwaysOn":false,"relativePower":false}},"type":"PowerOff","icon":"Default"},{"label":"Watch a Movie","suggestedDisplay":"Default","id":15839533,"activityTypeDisplayName":"Default","controlGroup":[{"name":"NumericBasic","function":[{"name":"Number0","label":"0","action":"{\"command\":\"0\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number1","label":"1","action":"{\"command\":\"1\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number2","label":"2","action":"{\"command\":\"2\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number3","label":"3","action":"{\"command\":\"3\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number4","label":"4","action":"{\"command\":\"4\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number5","label":"5","action":"{\"command\":\"5\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number6","label":"6","action":"{\"command\":\"6\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number7","label":"7","action":"{\"command\":\"7\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number8","label":"8","action":"{\"command\":\"8\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number9","label":"9","action":"{\"command\":\"9\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Clear","label":"Clear","action":"{\"command\":\"Clear\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"Volume","function":[{"name":"Mute","label":"Mute","action":"{\"command\":\"Mute\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"VolumeDown","label":"Volume Down","action":"{\"command\":\"VolumeDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"VolumeUp","label":"Volume Up","action":"{\"command\":\"VolumeUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"NavigationBasic","function":[{"name":"DirectionDown","label":"Direction Down","action":"{\"command\":\"DirectionDown\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"DirectionLeft","label":"Direction Left","action":"{\"command\":\"DirectionLeft\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"DirectionRight","label":"Direction Right","action":"{\"command\":\"DirectionRight\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"DirectionUp","label":"Direction Up","action":"{\"command\":\"DirectionUp\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Select","label":"Select","action":"{\"command\":\"Ok\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"TransportBasic","function":[{"name":"Stop","label":"Stop","action":"{\"command\":\"Stop\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Play","label":"Play","action":"{\"command\":\"Play\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Rewind","label":"Rewind","action":"{\"command\":\"Rewind\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Eject","label":"Eject","action":"{\"command\":\"Eject\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Pause","label":"Pause","action":"{\"command\":\"Pause\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"FastForward","label":"Fast Forward","action":"{\"command\":\"FastForward\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"TransportExtended","function":[{"name":"FrameAdvance","label":"Frame Advance","action":"{\"command\":\"Step\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"SkipBackward","label":"Skip Backward","action":"{\"command\":\"SkipBack\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"SkipForward","label":"Skip Forward","action":"{\"command\":\"SkipForward\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"NavigationDVD","function":[{"name":"Return","label":"Return","action":"{\"command\":\"Return\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"TopMenu","label":"Top Menu","action":"{\"command\":\"TopMenu\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Menu","label":"Menu","action":"{\"command\":\"Menu\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Subtitle","label":"Subtitle","action":"{\"command\":\"Subtitle\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"PlayMode","function":[{"name":"Repeat","label":"Repeat","action":"{\"command\":\"Repeat\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"Program","function":[{"name":"Program","label":"Program","action":"{\"command\":\"Program\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Bookmark","label":"Bookmark","action":"{\"command\":\"Bookmark\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"GameType3","function":[{"name":"Home","label":"Home","action":"{\"command\":\"Home\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"NavigationExtended","function":[{"name":"Clear","label":"Backspace","action":"{\"command\":\"Clear\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"ColoredButtons","function":[{"name":"Green","label":"Green","action":"{\"command\":\"Green\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Red","label":"Red","action":"{\"command\":\"Red\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Blue","label":"Blue","action":"{\"command\":\"Blue\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Yellow","label":"Yellow","action":"{\"command\":\"Yellow\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]}],"activityOrder":3,"tuningDefault":false,"fixit":{"29671749":{"id":"29671749","power":"ON","input":"HDMI 2","alwaysOn":false,"relativePower":true},"29695418":{"id":"29695418","power":"ON","alwaysOn":false,"relativePower":true},"29671742":{"id":"29671742","power":"ON","input":"","alwaysOn":false,"relativePower":false},"29671764":{"id":"29671764","alwaysOn":false,"relativePower":false},"29695438":{"id":"29695438","power":"OFF","alwaysOn":false,"relativePower":true},"29695467":{"id":"29695467","alwaysOn":false,"relativePower":false},"29695485":{"id":"29695485","alwaysOn":false,"relativePower":false}},"type":"VirtualDvd","icon":"userdata: 0x4454e0","baseImageUri":"https://rcbu-test-ssl-amr.s3.amazonaws.com/"}]
```
### Get Harmony Devices
Get the list of devices available from the Harmony Hub if configured. 
```
GET http://host:port/api/devices/harmony/devices
```
#### Response
Only listing the relevant fields that are needed for control of a device. The example below is representative of some devices.

Main device object

Name |	Type |	Description
-----|-------|-------------
id | integer | The id of the Harmony device. 
label | string | The name of the Harmony device.
controlGroup | object | The structure that contains buttons grouped by control areas. 

Control Group object

Name |	Type |	Description
-----|-------|-------------
name | string | The name of a device control area.
function | object | The structure that contains buttons grouped by function. 

Function object

Name |	Type |	Description
-----|-------|-------------
name | string | The name of a button in the device control area.
Label | string | The Title of a button for display purposes.
action | string | The embedded json object which describes the button press. The command value is the name of the button to be called.
#### Harmony devices data example
```
[{"id":29671749,"label":"Yamaha AV Receiver","type":"StereoReceiver","transport":1,"suggestedDisplay":"DEFAULT","deviceTypeDisplayName":"StereoReceiver","capabilities":[1,5,8],"dongleRFID":0,"controlGroup":[{"name":"Power","function":[{"name":"PowerOff","label":"Power Off","action":"{\"command\":\"PowerOff\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"PowerOn","label":"Power On","action":"{\"command\":\"PowerOn\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"PowerToggle","label":"Power Toggle","action":"{\"command\":\"PowerToggle\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"NumericBasic","function":[{"name":"NumberEnter","label":"Number Enter","action":"{\"command\":\"NumberEnter\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number0","label":"0","action":"{\"command\":\"0\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number1","label":"1","action":"{\"command\":\"1\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number2","label":"2","action":"{\"command\":\"2\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number3","label":"3","action":"{\"command\":\"3\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number4","label":"4","action":"{\"command\":\"4\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number5","label":"5","action":"{\"command\":\"5\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number6","label":"6","action":"{\"command\":\"6\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number7","label":"7","action":"{\"command\":\"7\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number8","label":"8","action":"{\"command\":\"8\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number9","label":"9","action":"{\"command\":\"9\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"Volume","function":[{"name":"Mute","label":"Mute","action":"{\"command\":\"Mute\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"VolumeDown","label":"Volume Down","action":"{\"command\":\"VolumeDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"VolumeUp","label":"Volume Up","action":"{\"command\":\"VolumeUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"NavigationBasic","function":[{"name":"DirectionDown","label":"Direction Down","action":"{\"command\":\"DirectionDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"DirectionLeft","label":"Direction Left","action":"{\"command\":\"DirectionLeft\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"DirectionRight","label":"Direction Right","action":"{\"command\":\"DirectionRight\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"DirectionUp","label":"Direction Up","action":"{\"command\":\"DirectionUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Select","label":"Select","action":"{\"command\":\"Enter\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"NavigationDVD","function":[{"name":"Return","label":"Return","action":"{\"command\":\"Return\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"RadioTuner","function":[{"name":"PrevPreset","label":"Prev Preset","action":"{\"command\":\"PresetPrev\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"ScanDown","label":"Scan Down","action":"{\"command\":\"TuneDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"ScanUp","label":"Scan Up","action":"{\"command\":\"TuneUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"NextPreset","label":"Next Preset","action":"{\"command\":\"PresetNext\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"Setup","function":[{"name":"Setup","label":"Setup","action":"{\"command\":\"Setup\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Sleep","label":"Sleep","action":"{\"command\":\"Sleep\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"NavigationExtended","function":[{"name":"Info","label":"Info","action":"{\"command\":\"Info\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"DisplayMode","function":[{"name":"Display","label":"Display","action":"{\"command\":\"Display\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"Miscellaneous","function":[{"name":"DSP-Direct","label":"DSP-Direct","action":"{\"command\":\"DSP-Direct\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"DSP-Movie","label":"DSP-Movie","action":"{\"command\":\"DSP-Movie\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"DSP-Music","label":"DSP-Music","action":"{\"command\":\"DSP-Music\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"DSP-Stereo/Enhancer","label":"DSP-Stereo/Enhancer","action":"{\"command\":\"DSP-Stereo\\/Enhancer\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"DSP-Straight","label":"DSP-Straight","action":"{\"command\":\"DSP-Straight\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"DSP-SurrDecode","label":"DSP-SurrDecode","action":"{\"command\":\"DSP-SurrDecode\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputAudio1","label":"InputAudio1","action":"{\"command\":\"InputAudio1\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputAudio2","label":"InputAudio2","action":"{\"command\":\"InputAudio2\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputAv1","label":"InputAv1","action":"{\"command\":\"InputAv1\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputAv2","label":"InputAv2","action":"{\"command\":\"InputAv2\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputAv3","label":"InputAv3","action":"{\"command\":\"InputAv3\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputAv4","label":"InputAv4","action":"{\"command\":\"InputAv4\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputAv5","label":"InputAv5","action":"{\"command\":\"InputAv5\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputAv6","label":"InputAv6","action":"{\"command\":\"InputAv6\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputDock","label":"InputDock","action":"{\"command\":\"InputDock\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputHdmi1","label":"InputHdmi1","action":"{\"command\":\"InputHdmi1\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputHdmi2","label":"InputHdmi2","action":"{\"command\":\"InputHdmi2\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputHdmi3","label":"InputHdmi3","action":"{\"command\":\"InputHdmi3\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputHdmi4","label":"InputHdmi4","action":"{\"command\":\"InputHdmi4\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputTuner","label":"InputTuner","action":"{\"command\":\"InputTuner\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputV-Aux","label":"InputV-Aux","action":"{\"command\":\"InputV-Aux\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodDisplay","label":"iPodDisplay","action":"{\"command\":\"iPodDisplay\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodDown","label":"iPodDown","action":"{\"command\":\"iPodDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodEnter","label":"iPodEnter","action":"{\"command\":\"iPodEnter\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodFastForward","label":"iPodFastForward","action":"{\"command\":\"iPodFastForward\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodLeft","label":"iPodLeft","action":"{\"command\":\"iPodLeft\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodNextTrack","label":"iPodNextTrack","action":"{\"command\":\"iPodNextTrack\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodPause","label":"iPodPause","action":"{\"command\":\"iPodPause\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodPlay","label":"iPodPlay","action":"{\"command\":\"iPodPlay\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodPreviousTrack","label":"iPodPreviousTrack","action":"{\"command\":\"iPodPreviousTrack\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodRewind","label":"iPodRewind","action":"{\"command\":\"iPodRewind\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodRight","label":"iPodRight","action":"{\"command\":\"iPodRight\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodStop","label":"iPodStop","action":"{\"command\":\"iPodStop\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodUp","label":"iPodUp","action":"{\"command\":\"iPodUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Memory","label":"Memory","action":"{\"command\":\"Memory\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Option","label":"Option","action":"{\"command\":\"Option\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"SceneBd/Dvd","label":"SceneBd/Dvd","action":"{\"command\":\"SceneBd\\/Dvd\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"SceneCd","label":"SceneCd","action":"{\"command\":\"SceneCd\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"SceneRadio","label":"SceneRadio","action":"{\"command\":\"SceneRadio\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"SceneTv","label":"SceneTv","action":"{\"command\":\"SceneTv\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"SelectAm","label":"SelectAm","action":"{\"command\":\"SelectAm\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"SelectFm","label":"SelectFm","action":"{\"command\":\"SelectFm\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]}],"controlPort":"7","keyboardAssociated":false,"model":"HTR-5063","deviceProfileUri":"svcs.myharmony.com/res/device/29671749-qVE4lSVuAqkVmhj1KOiyYIRjQrTaPAxfie7iIgbt3kQ\u003d","manufacturer":"Yamaha","icon":"5","manualPower":false},{"id":29695418,"label":"Toshiba DVD","type":"DVD","transport":1,"suggestedDisplay":"DEFAULT","deviceTypeDisplayName":"DVD","capabilities":[1,5,6,9,10],"dongleRFID":0,"controlGroup":[{"name":"Power","function":[{"name":"PowerToggle","label":"Power Toggle","action":"{\"command\":\"PowerToggle\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"NumericBasic","function":[{"name":"Number0","label":"0","action":"{\"command\":\"0\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number1","label":"1","action":"{\"command\":\"1\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number2","label":"2","action":"{\"command\":\"2\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number3","label":"3","action":"{\"command\":\"3\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number4","label":"4","action":"{\"command\":\"4\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number5","label":"5","action":"{\"command\":\"5\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number6","label":"6","action":"{\"command\":\"6\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number7","label":"7","action":"{\"command\":\"7\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number8","label":"8","action":"{\"command\":\"8\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number9","label":"9","action":"{\"command\":\"9\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Clear","label":"Clear","action":"{\"command\":\"Clear\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"NavigationBasic","function":[{"name":"DirectionDown","label":"Direction Down","action":"{\"command\":\"DirectionDown\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"DirectionLeft","label":"Direction Left","action":"{\"command\":\"DirectionLeft\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"DirectionRight","label":"Direction Right","action":"{\"command\":\"DirectionRight\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"DirectionUp","label":"Direction Up","action":"{\"command\":\"DirectionUp\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Select","label":"Select","action":"{\"command\":\"Ok\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"TransportBasic","function":[{"name":"Stop","label":"Stop","action":"{\"command\":\"Stop\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Play","label":"Play","action":"{\"command\":\"Play\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Rewind","label":"Rewind","action":"{\"command\":\"Rewind\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Eject","label":"Eject","action":"{\"command\":\"Eject\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Pause","label":"Pause","action":"{\"command\":\"Pause\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"FastForward","label":"Fast Forward","action":"{\"command\":\"FastForward\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"TransportExtended","function":[{"name":"FrameAdvance","label":"Frame Advance","action":"{\"command\":\"Step\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"SkipBackward","label":"Skip Backward","action":"{\"command\":\"SkipBack\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"SkipForward","label":"Skip Forward","action":"{\"command\":\"SkipForward\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"NavigationDVD","function":[{"name":"Return","label":"Return","action":"{\"command\":\"Return\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"TopMenu","label":"Top Menu","action":"{\"command\":\"TopMenu\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Menu","label":"Menu","action":"{\"command\":\"Menu\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Subtitle","label":"Subtitle","action":"{\"command\":\"Subtitle\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Audio","label":"Audio","action":"{\"command\":\"Audio\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Angle","label":"Angle","action":"{\"command\":\"Angle\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"PlayMode","function":[{"name":"Repeat","label":"Repeat","action":"{\"command\":\"Repeat\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"RepeatAB","label":"Repeat A-B","action":"{\"command\":\"RepeatA-B\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"Program","function":[{"name":"Program","label":"Program","action":"{\"command\":\"Program\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Bookmark","label":"Bookmark","action":"{\"command\":\"Bookmark\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"PictureInPicture","function":[{"name":"PipToggle","label":"Pip Toggle","action":"{\"command\":\"PIP\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"ColoredButtons","function":[{"name":"Green","label":"Green","action":"{\"command\":\"Green\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Red","label":"Red","action":"{\"command\":\"Red\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Blue","label":"Blue","action":"{\"command\":\"Blue\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Yellow","label":"Yellow","action":"{\"command\":\"Yellow\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"DisplayMode","function":[{"name":"Display","label":"Display","action":"{\"command\":\"Display\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Zoom","label":"Zoom","action":"{\"command\":\"Zoom\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"GoogleTVNavigation","function":[{"name":"Netflix","label":"Netflix","action":"{\"command\":\"Netflix\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"Miscellaneous","function":[{"name":"Connected","label":"Connected","action":"{\"command\":\"Connected\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Digest","label":"Digest","action":"{\"command\":\"Digest\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Goto","label":"Goto","action":"{\"command\":\"Goto\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Home","label":"Home","action":"{\"command\":\"Home\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"OnScreen","label":"OnScreen","action":"{\"command\":\"OnScreen\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"PopUp","label":"PopUp","action":"{\"command\":\"PopUp\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"SecondaryAudio","label":"SecondaryAudio","action":"{\"command\":\"SecondaryAudio\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Slow","label":"Slow","action":"{\"command\":\"Slow\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]}],"controlPort":"7","keyboardAssociated":false,"model":"bdk33","deviceProfileUri":"svcs.myharmony.com/res/device/29695418-D8Q8Amm9TDPWK5OxPaGiCPZ+iJKJJdHmqQL7MGcMHMY\u003d","manufacturer":"Toshiba","icon":"4","manualPower":false}]
```
### Show Harmony Current Activity
Show the Harmony Hub's current activity.
```
GET http://host:port/api/devices/harmony/show
```
#### Response
Only listing the relevant fields that are needed for identity of an activity. TThe example below is representative of an activity.

Name |	Type |	Description
-----|-------|-------------
label | string | The name of the Harmony activity.
id | integer | The id of the Harmony activity. 
#### Harmony activity data example
```
{"label":"Watch TV","suggestedDisplay":"Default","id":15823996,"activityTypeDisplayName":"Default","controlGroup":[{"name":"NumericBasic","function":[{"name":"NumberEnter","label":"Number Enter","action":"{\"command\":\"Enter\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Hyphen","label":"-","action":"{\"command\":\"-\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number0","label":"0","action":"{\"command\":\"0\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number1","label":"1","action":"{\"command\":\"1\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number2","label":"2","action":"{\"command\":\"2\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number3","label":"3","action":"{\"command\":\"3\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number4","label":"4","action":"{\"command\":\"4\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number5","label":"5","action":"{\"command\":\"5\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number6","label":"6","action":"{\"command\":\"6\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number7","label":"7","action":"{\"command\":\"7\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number8","label":"8","action":"{\"command\":\"8\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number9","label":"9","action":"{\"command\":\"9\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"Volume","function":[{"name":"Mute","label":"Mute","action":"{\"command\":\"Mute\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"VolumeDown","label":"Volume Down","action":"{\"command\":\"VolumeDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"VolumeUp","label":"Volume Up","action":"{\"command\":\"VolumeUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"Channel","function":[{"name":"PrevChannel","label":"Prev Channel","action":"{\"command\":\"Last\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"ChannelDown","label":"Channel Down","action":"{\"command\":\"ChannelDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"ChannelUp","label":"Channel Up","action":"{\"command\":\"ChannelUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"NavigationBasic","function":[{"name":"DirectionDown","label":"Direction Down","action":"{\"command\":\"DirectionDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"DirectionLeft","label":"Direction Left","action":"{\"command\":\"DirectionLeft\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"DirectionRight","label":"Direction Right","action":"{\"command\":\"DirectionRight\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"DirectionUp","label":"Direction Up","action":"{\"command\":\"DirectionUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Select","label":"Select","action":"{\"command\":\"Select\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"TransportBasic","function":[{"name":"Play","label":"Play","action":"{\"command\":\"Play\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Rewind","label":"Rewind","action":"{\"command\":\"Rewind\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Pause","label":"Pause","action":"{\"command\":\"Pause\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"FastForward","label":"Fast Forward","action":"{\"command\":\"FastForward\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"TransportRecording","function":[{"name":"Record","label":"Record","action":"{\"command\":\"Record\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"TransportExtended","function":[{"name":"FrameAdvance","label":"Frame Advance","action":"{\"command\":\"Slow\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"SkipBackward","label":"Skip Backward","action":"{\"command\":\"Replay\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"SkipForward","label":"Skip Forward","action":"{\"command\":\"Advance\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"NavigationDVD","function":[{"name":"Back","label":"Back","action":"{\"command\":\"Back\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"NavigationDSTB","function":[{"name":"C","label":"C","action":"{\"command\":\"C\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"B","label":"B","action":"{\"command\":\"B\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"D","label":"D","action":"{\"command\":\"D\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"A","label":"A","action":"{\"command\":\"A\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"List","label":"List","action":"{\"command\":\"List\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Live","label":"Live","action":"{\"command\":\"LiveTv\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"TiVo","function":[{"name":"ThumbsDown","label":"Thumbs Down","action":"{\"command\":\"ThumbsDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"ThumbsUp","label":"Thumbs Up","action":"{\"command\":\"ThumbsUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"TiVo","label":"TiVo","action":"{\"command\":\"TiVo\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"NavigationExtended","function":[{"name":"Guide","label":"Guide","action":"{\"command\":\"Guide\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Info","label":"Info","action":"{\"command\":\"Info\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Clear","label":"Backspace","action":"{\"command\":\"Clear\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"ColoredButtons","function":[{"name":"Green","label":"Green","action":"{\"command\":\"Green\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Red","label":"Red","action":"{\"command\":\"Red\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Blue","label":"Blue","action":"{\"command\":\"Blue\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Yellow","label":"Yellow","action":"{\"command\":\"Yellow\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]}],"activityOrder":0,"tuningDefault":true,"fixit":{"29671749":{"id":"29671749","power":"ON","input":"HDMI 4","alwaysOn":false,"relativePower":true},"29695418":{"id":"29695418","power":"OFF","alwaysOn":false,"relativePower":true},"29671742":{"id":"29671742","power":"ON","input":"","alwaysOn":false,"relativePower":false},"29671764":{"id":"29671764","alwaysOn":false,"relativePower":false},"29695438":{"id":"29695438","power":"OFF","alwaysOn":false,"relativePower":true},"29695467":{"id":"29695467","alwaysOn":false,"relativePower":false},"29695485":{"id":"29695485","alwaysOn":false,"relativePower":false}},"type":"VirtualTelevisionN","icon":"userdata: 0x4454e0","baseImageUri":"https://rcbu-test-ssl-amr.s3.amazonaws.com/"},{"label":"PowerOff","suggestedDisplay":"Default","id":-1,"activityTypeDisplayName":"Default","controlGroup":[],"tuningDefault":false,"fixit":{"29671749":{"id":"29671749","power":"OFF","alwaysOn":false,"relativePower":true},"29695418":{"id":"29695418","power":"OFF","alwaysOn":false,"relativePower":true},"29671742":{"id":"29671742","power":"OFF","alwaysOn":false,"relativePower":false},"29671764":{"id":"29671764","alwaysOn":false,"relativePower":false},"29695438":{"id":"29695438","power":"OFF","alwaysOn":false,"relativePower":true},"29695467":{"id":"29695467","alwaysOn":false,"relativePower":false},"29695485":{"id":"29695485","alwaysOn":false,"relativePower":false}},"type":"PowerOff","icon":"Default"}
```
### Get Nest Items
Get the list of Nest Home Structures and Thermostats available for a Nest account if configured. 
```
GET http://host:port/api/devices/nest/items
```
#### Response
The example below is representative of some nest accounts.


Name |	Type |	Description
-----|-------|-------------
name | string | The name of the Nest item.
id | string | The id of the Nest item.
type | string | The type of nest item returned. i.e.: Home or Thermostat 
location | string | Location of the device. For Home type it is the physical location. For the thermostat it is the Room Location and Home name.
#### Nest items data example
```
[{"name":"TestHouse","id":"b999fcb0-9dbb-11e5-acf5-22000aba84ca","type":"Home","location":"Scranton, OH"},{"name":"Bedroom(3658)","id":"fake7890F3EC3658","type":"Thermostat","location":"Bedroom - TestHouse"},{"name":"Basement(A594)","id":"fake7890E4ABA594","type":"Thermostat","location":"Basement - TestHouse"}]
```
## HUE REST API usage
This section will describe the REST api available for controlling the bridge based off of the HUE API. This Bridge does not support the full HUE API, only the calls that are supported with the HA Bridge are shown. The REST body examples are all formatted for easy reading, the actual body usage should be like this:
```
{"var1":"value1","var2":"value2","var3:"value3"}
```
### Get all lights
Gets a list of all lights that have been discovered by the bridge.
```
GET	http://host:port/api/<username>/lights
```
#### Response
Returns a list of all lights in the system. 

If there are no lights in the system then the bridge will return an empty object, {}.
```
{
    "1": {
        "state": {
            "on": true,
            "bri": 144,
            "alert": "none",
            "effect": "none",
            "reachable": true
        },
        "type": "Dimmable light",
        "name": "Table Lamp 1",
        "modelid": "LWB004",
        "swversion": "66012040",
    },
    "2": {
        "state": {
            "on": false,
            "bri": 0,
            "alert": "none",
            "effect": "none",
            "reachable": true
        },
        "type": "Dimmable light",
        "name": "Table Lamp 2",
        "modelid": "LWB004",
        "swversion": "66012040",
    }
}
```
### Get light attributes and state
Gets the attributes and state of a given light.
```
GET	http://host:port/api/<username>/lights/<id>
```
####  Response
Name |	Type |	Description
-----|-------|-------------
state |	state object |	Details the state of the light, see the state table below for more details.
type |	string |	A fixed name describing the type of light which will be "Dimmable light".
name |	string 0, 32 |	A unique, editable name given to the light.
modelid |	string 6, 6 |	The hardware model of the light which will be "LWB004".
uniqueid |	string 6, 32 |	Unique id of the device.
manufacturername |	string 6, 32 |	The manufacturer name will be "Philips".
luminaireuniqueid |	string 6, 32 |	This will be empty.
swversion |	string 8, 8 |	An identifier for the software version running on the light which will be "65003148".

The state object contains the following fields

Name |	Type |	Description
-----|-------|-------------
on |	bool |	On/Off state of the light. On=true, Off=false
bri |	uint8 |	Brightness of the light. This will be set to 254 as a default.
alert |	string |	This will be set to "none".
effect |	string |	This will be set to "none".
reachable |	bool |	Indicates if a light can be reached by the bridge and will be set to true.
```
{
	"state": {
		"on": true,
		"effect": "none",
		"alert": "none",
		"bri": 254,
		"reachable": true,
	},
	"type": "Dimmable light",
	"name": "Kitchen Ceiling",
	"modelid": "LWB004",
	"swversion": "66012040", 	
}
```
### Set light state
Allows the user to turn the light on and off, modify the brightness.
```
PUT	http://host:port/api/<username>/lights/<id>/state
```
#### Body arguments
Name |	Type |	Description	 
-----|-------|-------------
on |	bool |	On/Off state of the light. On=true, Off=false. Optional
bri |	uint8 |	The brightness value to set the light to. Brightness is a scale from 1 (the minimum the light is capable of) to 254 (the maximum). Note: a brightness of 1 is not off. e.g. "brightness": 60 will set the light to a specific brightness. Optional
```
{
	"on": true,
	"bri": 200
}
```
#### Response
A response to a successful PUT request contains confirmation of the arguments passed in. Note: If the new value is too large to return in the response due to internal memory constraints then a value of "Updated." is returned.
```
[
	{"success":{"/lights/1/state/bri":200}},
	{"success":{"/lights/1/state/on":true}},
]
```
### Update bridge internal light state
Allows the user to set the internal state of the light on and off, modify the brightness. This is not a HUE API call and is special to the bridge as it keeps track of the state changes to the light from the api. It is intended to allow you to sync the bridge state with your HA system state.
```
PUT	http://host:port/api/<username>/lights/<id>/bridgeupdatestate
```
#### Body arguments
Name |	Type |	Description	 
-----|-------|-------------
on |	bool |	On/Off state of the light. On=true, Off=false. Optional
bri |	uint8 |	The brightness value to set the light to. Brightness is a scale from 1 (the minimum the light is capable of) to 254 (the maximum). Note: a brightness of 1 is not off. e.g. "brightness": 60 will set the light to a specific brightness. Optional
```
{
	"on": true,
	"bri": 200
}
```
#### Response
A response to a successful PUT request contains confirmation of the arguments passed in. Note: If the new value is too large to return in the response due to internal memory constraints then a value of "Updated." is returned.
```
[
	{"success":{"/lights/1/state/bri":200}},
	{"success":{"/lights/1/state/on":true}},
]
```
### Create user
Emulates creating a new user. The link button state on the HA Bridge is always on for the purpose of responding to this request. No actual user is saved as this is for compatibility.
```
POST	http://host:port/api
```
#### Body arguments
Name |	Type |	Description |	Required
-----|-------|--------------|-----------
devicetype |	string 0..40 | <application_name>#<devicename> application_name string 0..20, devicename string 0..19 (Example: my_hue_app#iphone peter ) | Required
username |	string 10..40 |	A username. If this is not provided, a random key will be generated and returned in the response. |	Optional
```
{"devicetype": "my_hue_app#iphone peter"}
```
#### Response
Contains a list with a single item that details whether the user was added successfully along with the username parameter.
```
[{"success":{"username": "83b7780291a6ceffbe0bd049104df"}}]
```
### Get full state (datastore)
This command is used to fetch the entire datastore from the device, including settings and state information for lights, groups, schedules and configuration.
```
GET	http://host:port/api/<username>
```
#### Response
Name |	Type |	Description
-----|-------|-------------
lights |	object |	A collection of all lights and their attributes.
groups |	object |	A collection of all groups and their attributes. This is empty.
config |	object |	All configuration settings.
schedules |	object |	A collection of all schedules and their attributes. This is not given.
scenes |	object |	A collection of all scenes and their attributes. This is empty.
sensors |	object |	A collection of all sensors and their attributes. This is not given.
rules |	object |	A collection of all rules and their attributes. This is not given.
```
{
	"lights": {
		"1": {
			"state": {
				"on": false,
				"bri": 0,
				"alert": "none",
				"effect": "none",
				"reachable": true
			},
			"type": "Dimmable light",
			"name": "Table Lamp 1",
			"modelid": "LWB004",
			"swversion": "66012040", 	
		},
		"2": {
			"state": {
				"on": true,
				"bri": 254,
				"alert": "none",
				"effect": "none",
				"reachable": true
			},
			"type": "Dimmable light",
			"name": "Table Lamp 2",
			"modelid": "LWB004",
			"swversion": "66012040", 	
		}
	},
	"scenes":{
	},
	"groups":{
	},
	"config": {
		"name": "Philips hue",
		"mac": "00:00:88:00:bb:ee",
		"dhcp": true,
		"ipaddress": "192.168.1.74",
		"netmask": "255.255.255.0",
		"gateway": "192.168.1.254",
		"proxyaddress": "",
		"proxyport": 0,
		"UTC": "2012-10-29T12:00:00",
		"whitelist": {
			"1028d66426293e821ecfd9ef1a0731df": {
				"last use date": "2012-10-29T12:00:00",
				"create date": "2012-10-29T12:00:00",
				"name": "test user"
			}
		},
		"swversion": "01003372",
		"swupdate": {
			"updatestate": 0,
			"url": "",
			"text": "",
			"notify": false
		},
		"linkbutton": false,
		"portalservices": false
	},
}
```
## UPNP Emulation of HUE
This section will discuss the UPNP implementation of this bridge based as much as can be for the HUE.
### UPNP listening
The HA Bridge default UPNP listner is started on port 1900 on the upnp multicast address of 239.255.255.250. All ethernet interfaces that are active are bound to and the repsonse port is set to the one given on the command line above or the default of 50000.

The listener will respond to the following body packet that contain the following minimal information:

```
M-SEARCH * HTTP/1.1\r\n
MAN: "ssdp:discover"\r\n
ST: urn:schemas-upnp-org:device:basic:1\r\n
	OR
ST: upnp:rootdevice\r\n
	OR
ST: ssdp:all\r\n
```

If this criteria is met, the following response is provided to the calling application:

```
HTTP/1.1 200 OK\r\n
CACHE-CONTROL: max-age=86400\r\n
EXT:\r\n
LOCATION: http://192.168.1.1:8080/description.xml\r\n
SERVER: FreeRTOS/6.0.5, UPnP/1.0, IpBridge/0.1\r\n 
ST: urn:schemas-upnp-org:device:basic:1\r\n
"USN: uuid:Socket-1_0-221438K0100073::urn:schemas-upnp-org:device:basic:1\r\n\r\n
```
### UPNP description service
The bridge provides the description service which is used by the calling app to interogate access details after it has decided the upnp multicast repsonse is the correct device.
#### Get Description
```
GET http://host:8080/description.xml
```
#### Response
```
<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n
<root xmlns=\"urn:schemas-upnp-org:device-1-0\">\n
	<specVersion>\n
	<major>1</major>\n
	<minor>0</minor>\n
	</specVersion>\n
	<URLBase>http://192.168.1.1:8080/</URLBase>\n
	<device>\n
		<deviceType>urn:schemas-upnp-org:device:Basic:1</deviceType>\n
		<friendlyName>HA-Bridge (192.168.1.1)</friendlyName>\n
		<manufacturer>Royal Philips Electronics</manufacturer>\n
		<manufacturerURL>http://www.bwssystems.com</manufacturerURL>\n
		<modelDescription>Hue Emulator for HA bridge</modelDescription>\n
		<modelName>Philips hue bridge 2012</modelName>\n
		<modelNumber>929000226503</modelNumber>\n
		<modelURL>http://www.bwssystems.com/apps.html</modelURL>\n
		<serialNumber>0017880ae670</serialNumber>\n
		<UDN>uuid:88f6698f-2c83-4393-bd03-cd54a9f8595</UDN>\n
		<serviceList>\n
			<service>\n
				<serviceType>(null)</serviceType>\n
				<serviceId>(null)</serviceId>\n
				<controlURL>(null)</controlURL>\n
				<eventSubURL>(null)</eventSubURL>\n
				<SCPDURL>(null)</SCPDURL>\n
			</service>\n
		</serviceList>\n
		<presentationURL>index.html</presentationURL>\n
		<iconList>\n
			<icon>\n
				<mimetype>image/png</mimetype>\n
				<height>48</height>\n
				<width>48</width>\n
				<depth>24</depth>\n
				<url>hue_logo_0.png</url>\n
			</icon>\n
			<icon>\n
				<mimetype>image/png</mimetype>\n
				<height>120</height>\n
				<width>120</width>\n
				<depth>24</depth>\n
				<url>hue_logo_3.png</url>\n
			</icon>\n
		</iconList>\n
	</device>\n
</root>\n
```
## Debugging
To turn on debugging for the bridge, use the following extra parm in the command line:
```
-Dorg.slf4j.simpleLogger.defaultLogLevel=DEBUG
```
## Development Mode
To turn on development mode so that it will not need an Harmony Hub for testing, use the following extra parm in the command line and the harmony ip and login info will not be needed:
```
java -jar -Ddev.mode=true ha-bridge-0.X.Y.jar
```