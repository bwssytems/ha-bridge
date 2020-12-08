# ha-bridge
Emulates Philips Hue API to other home automation gateways such as an Amazon Echo/Dot or other systems that support Philips Hue local network discovery. The ha-bridge is not part of meethue.philips.com so the cloud sign in does not apply to this system.  The Bridge handles basic commands such as "On", "Off", "Brightness" and "Color" commands of the hue protocol.  This bridge can control most devices that have a distinct API.

Here are some diagrams to put this software in perspective.

The Echo path looks like this:
```
                     +------------------------+    +------------------------+
+-------------+      | H A +------------------|    | A +------------------+ |
| Amazon Echo |----->| U P | ha-bridge core   |--->| P | Device to control| |
+-------------+      | E I +------------------|    | I +------------------+ |
                     +------------------------+    +------------------------+
```
THe Harmony Hub path looks like this:
```
                     +------------------------+    +------------------------+
+-------------+      | H A +------------------|    | A +------------------+ |
| Harmony Hub |----->| U P | ha-bridge core   |--->| P | Device to control| |
+-------------+      | E I +------------------|    | I +------------------+ |
                     +------------------------+    +------------------------+
```
A Custom implementation path looks like this:
```
                     +------------------------+    +------------------------+
+-------------+      | H A +------------------|    | A +------------------+ |
|  HA System  |----->| U P | ha-bridge core   |--->| P | Device to control| |
+-------------+      | E I +------------------|    | I +------------------+ |
                     +------------------------+    +------------------------+
```
**SECURITY RISK: If you are unsure on how this software operates and what it exposes to your network, please make sure you understand that it can allow root access to your system. It is best practice to not open this to the Internet through your router as there are no security protocols in place to protect the system. The License agreement states specifically that you use this at your own risk.**

**ATTENTION: This requires a physical Amazon Echo, Echo Plus, Spot, Tap or Show and does not work with prototype devices built using the Alexa Voice Service e.g. Amazon's Alexa AVS Sample App and Sam Machin's AlexaPi. The AVS version does not have any capability for Hue Bridge discovery!**

**NOTE: This software does require the user to have knowledge on how processes run on Linux or Windows with java. Also, an understanding of networking basics will help as well. This system receives upnp udp multicast packets from devices to be found, so that is something to understand. Please make sure you have all your devices use static IP addresses from your router. Most all questions have been answered already. PLEASE USE GOOGLE TO FIND YOUR ANSWERS!**

**NOTE: This software does not control Philips Hue devices directly. A physical Philips Hue Hub is required for that, by which the ha-bridge can then proxy all of your real Hue bridges behind this bridge.**

**ISSUE: Google Home does NOT support local connection to Philips Hue Hubs and requires that it connect to meethue.com. Since the ha-bridge only emulates the local API, and is not associated with Philips, this method will not work. If you have an older Google Home application, this may still work. YMMV.**

**FAQ: Please look here for the current FAQs! https://github.com/bwssytems/ha-bridge/wiki/HA-Bridge-FAQs**

In the cases of systems that require authorization and/or have APIs that cannot be handled in the current method, a module may need to be built. The Harmony Hub is such a module and so is the Nest module. The Bridge has helpers to build devices for the gateway for the Logitech Harmony Hub, Vera, Vera Lite or Vera Edge, Nest, Somfy Tahoma, Home Assistant, Domoticz, MQTT, HAL, Fibaro, HomeWizard, LIFX, OpenHAB, FHEM, Broadlink, Mozilla IOT, HomeGenie and the ability to proxy all of your real Hue bridges behind this bridge.

Alternatively the Bridge supports custom calls and executing programs/scripts as well using http/https/udp and tcp such as the LimitlessLED/MiLight bulbs using the UDP protocol. Binary data is supported with UDP/TCP.

This bridge was built to help put the Internet of Things together.
## Build
To customize and build it yourself, build a new jar with maven:  

```
mvn install
```
Otherwise, downloads are available at https://github.com/bwssytems/ha-bridge/releases.  
## Run
Then locate the jar and start the server with:  

ATTENTION: Due to port 80 being the default, Linux restricts this to super user. Use the instructions below.

```
java -jar ha-bridge-5.3.1RC5.jar
```

## Manual installation of ha-bridge and setup of systemd service
Next gen Linux systems (this includes the Raspberry Pi), use systemd to run and manage services.
Here is a link on how to use systemd: https://www.digitalocean.com/community/tutorials/how-to-use-systemctl-to-manage-systemd-services-and-units

Create the directory and make sure that ha-bridge-5.3.1RC5.jar is in your /home/pi/ha-bridge directory.

```
pi@raspberrypi:~ $ mkdir ha-bridge
pi@raspberrypi:~ $ cd ha-bridge

pi@raspberrypi:~/ha-bridge $ wget https://github.com/bwssytems/ha-bridge/releases/download/v5.3.1RC5/ha-bridge-5.3.1RC5.jar
```

Create the ha-bridge.service unit file:
```
pi@raspberrypi:~ $ cd /etc/systemd/system
pi@raspberrypi:~ $ sudo nano ha-bridge.service
```
Copy the text below into the editor nano.
```
[Unit]
Description=HA Bridge
Wants=network.target
After=network.target

[Service]
Type=simple

WorkingDirectory=/home/pi/ha-bridge
ExecStart=/usr/bin/java -jar -Dconfig.file=/home/pi/ha-bridge/data/habridge.config /home/pi/ha-bridge/ha-bridge-5.3.1RC5.jar

[Install]
WantedBy=multi-user.target
```

Save the file in the editor by hitting CTL-X and then saying Y to update and save.

Reload the system control config:
```
pi@raspberrypi:~ $ sudo systemctl daemon-reload
```

To start the bridge:
```
pi@raspberrypi:~ $ sudo systemctl start ha-bridge.service
```

To start the service at boot, use the `enable` command:
```
pi@raspberrypi:~ $ sudo systemctl enable ha-bridge.service
```

To look at the log, the output goes into the system log at `/var/log/syslog':
```
pi@raspberrypi:~ $ tail -f /var/log/syslog
```

### ha-bridge inside Docker container
Docker start offering official support for Raspbian operating system since autumn 2016.
Running services inside containers became to be a good alternative to normal installation method described before.

Install Docker Community Edition (CE) on Raspberry Pi
```
pi@raspberrypi:~ $ curl -fsSL get.docker.com -o get-docker.sh
pi@raspberrypi:~ $ sudo sh get-docker.sh
```

For every architecture there is a specialized ha-bridge Docker image available.
Please use the suitable image from the following list.

* Generic x86 / x86_64 system: [aptalca/home-automation-bridge](https://hub.docker.com/r/aptalca/home-automation-bridge)
* Raspberry Pi 1 (ARM): [habridge/ha-bridge-raspberry-pi](https://hub.docker.com/r/habridge/ha-bridge-raspberry-pi)
* Raspberry Pi 2 (ARM): [habridge/ha-bridge-raspberry-pi2](https://hub.docker.com/r/habridge/ha-bridge-raspberry-pi2)
* Raspberry Pi 3 (ARM): [habridge/ha-bridge-raspberrypi3](https://hub.docker.com/r/habridge/ha-bridge-raspberrypi3)

The following example explains how to run the latest version of ha-bridge Docker container on Raspberry Pi 3.
```
pi@raspberrypi:~ $ docker pull habridge/ha-bridge-raspberrypi3
pi@raspberrypi:~ $ docker run \
    --name ha-bridge \
    --rm \
    --init \
    --detach \
    --net=host \
    --volume=$PWD:/ha-bridge/data \
    --volume=/etc/localtime:/etc/localtime:ro \
    --volume=/etc/timezone:/etc/timezone:ro \
    habridge/ha-bridge-raspberrypi3
```

To set additional arguments for ha-bridge just write them as arguments for docker run command.
```
pi@raspberrypi:~ $ docker run \
     --name ha-bridge \
     --rm \
     --init \
     --detach \
     --net=host \
     --volume=$PWD:/ha-bridge/data \
     --volume=/etc/localtime:/etc/localtime:ro \
     --volume=/etc/timezone:/etc/timezone:ro \
     habridge/ha-bridge-raspberry-pi3 \
     -Dserver.port=8080 \
     -Dsecurity.key=secret
```

To halt the ha-bridge Docker container use the `stop` command:
```
pi@raspberrypi:~ $ docker stop ha-bridge
```

If you like to automate the deployment just use the Ansible role on https://github.com/escalate/ansible-ha-bridge-docker.

## Run ha-bridge alongside web server already on port 80
These examples will help you proxy your current webserver requests to the ha-bridge running on a different port, such as 8080.

### Apache Example

Reverse proxy with Apache on Ubuntu linux:

Enable the required Apache modules:

`a2enmod proxy proxy_http headers`

Added the following lines to my Apache config file “000-default”

```
<VirtualHost *:80>
	ProxyPass         /api  http://localhost:8080/api nocanon
	ProxyPassReverse  /api  http://localhost:8080/api
	ProxyRequests     Off
	AllowEncodedSlashes NoDecode

	# Local reverse proxy authorization override
	# Most unix distribution deny proxy by default (ie /etc/apache2/mods-enabled/proxy.conf in Ubuntu)
	<Proxy http://localhost:8080/api*>
		  Order deny,allow
		  Allow from all
	</Proxy>

….. (the rest of the VirtualHost config section) …..
</VirtualHost>
```

Restart apache for the changes to take effect.

`service apache2 restart`

### lighthttpd Example
```
server.modules   += ( "mod_proxy" )
proxy.server = (
	"/api" =>
        (
                ( "host" => "127.0.0.1",
                  "port" => "8080"
                )
        )
)
```
### nginx Example
```
location /api {
    proxy_pass http://127.0.0.1:8080/api;
}
```
## Available Arguments
Arguments are now deprecated. The ha-bridge will use the old -D arguments and populate the configuration screen, Bridge Control Tab, which can now be saved to a file and will not be needed. There is only one optional argument that overrides and that is the location of the configuration file. The default is the relative path "data/habridge.config".
### -Djava.net.preferIPv4Stack=true
This will guarantee that the ha-bridge will not use an IPV6 address. This cannot be automatically set inside the code.
```
java -jar -Djava.net.preferIPv4Stack=true ha-bridge-W.X.Y.jar
```
### -Dconfig.file=`<filepath>`
The default location for the configuration file to contain the settings for the bridge is the relative path from where the bridge is started in "data/habridge.config". If you would like a different filename or directory, specify -Dconfig.file=`<directory>/<filename>` explicitly. The command line example:
```
java -jar -Dconfig.file=/home/me/data/myhabridge.config ha-bridge-W.X.Y.jar
```
### -Dserver.port=`<port number>`
The default port number for the bridge is 80. To override what the default or what is in the configuration file for this parameter, specify -Dserver.port=`<port number>` explicitly. This is especially helpful if you are running the ha-bridge for the first time and have another application on port 80. The command line example:
```
java -jar -Dserver.port=80 ha-bridge-W.X.Y.jar
```
Note: if using with a Google Home device, port 80 *must* be used.

### -Dserver.ip=`<ip address>`
The default ip address for the bridge to listen on is all interfaces (0.0.0.0). To override what the default or what is in the configuration file for this parameter, specify -Dserver.ip=`<ip address>` explicitly. This is especially helpful if you are running the ha-bridge for the first time and have another application on that utilizes the default interface. The command line example:
```
java -jar -Dserver.ip=192.168.1.1 ha-bridge-W.X.Y.jar
```
### -Dsecurity.key=`<Your Key To Encrypt Security Data>`
This option is very important to set if you will be using username/passwords to secure the ha-bridge. The ha-bridge needs to encrypt the settings in the config file and to make sure they are secured specifically to you is to provide this key. Otherwise a default key is used and it is available in the code on github for the ha-bridge here, so not very secure in that sense. **It is very important provide this if you are using username/password.** To override the default, specify -Dsecurity.key=`<Your Key To Encrypt Security Data>` explicitly on the command line. This is will prevent any issues if your config file gets hacked. The command line example:
```
java -jar -Dsecurity.key=Xfawer354WertSdf321234asd ha-bridge-W.X.Y.jar
```
### -Dexec.garden=`<The path to your scripts and program directory>`
This sets a directory of your choosing to have a walled area for what can be executed by the Exec Command type. This is a good feature to use if you use the capabilities of executing a script or program from the ha-bridge. The default is not set which allows any program or script to be called and anyone with access to the your system could create an exec command call and execute it from the API. This is will prevent any issues if your system gets hacked.  To override the default, specify -Dexec.garden=`<The path to your scripts and program directory>` explicitly on the command line. The command line example:
```
java -jar -Dexec.garden=C:\Users\John\bin
```
## HA Bridge Usage and Configuration
This section will cover the basics of configuration and where this configuration can be done. This requires that you have started your bridge process and then have pointed your
favorite web interface by going to the `http://<my ip address>:<port>` or `http://localhost:<port>` with port you have assigned. The default quick link is http://localhost for your reference.
### The Bridge Devices Tab
This screen allows you to see your devices you have configured for the ha-bridge to present to a controller, such as an Amazon Echo/Dot. You can test each device from this page as this calls the ha-bridge just as a controller would, i.e. the Echo. This is useful to make sure your configuration for each device is correct and for trouble shooting. You can also manage your devices as well by editing and making a new device copy as well as deleting it.

Each of the columns are sortable by the use of the little arrow in the upper left corner of the heading.

If you click on the ID number on a Device, this will toggle the ID number to lock/unlock that is used during renumbering. When the ID is '<b>Bolded</b>', it is locked. You can also hover your mouse to see the state. This allows you to renumber a part of your devices and keep the ID for others. All IDs will be unique.

There is a new feature for the Devices that can be selected on the 'Startup' column. If you choose to have certain devices executed when the bridge is started or restarted. You can can click this field to bring up the dialog which will allow you to enter what you want the device to do on startup. The default is to do nothing.

At the bottom of the screen is the "Bridge Device DB Backup" which can be accessed with clicking on the `+` to expand this frame. Here you can backup and restore configurations that you have saved. These configs can be named or by clicking the `Backup Device DB' button will create a backup and name it for you. You can manage these backups by restoring them, deleting them, downloading them by clicking on the file name and uploading a saved device DB backup on your local machine.
#### Renumber Devices
This changes the numbering of the added devices to start at the nmuber given in the 'Bridge Control' tab field 'ID Seed' and the default is 100 and goes up from there. It was originally intended for a conversion from the previous system version that used large numbers and was not necessary. This also allows the system to try and number sequentially. If you use this button, you will need to re-discover your devices as their ID's will have changed.
#### Link
If this is present, you have enabled the Hue link button feature for the ha-bridge. If you want a new system to recognize the ha-bridge, you will need to press this button when you are doing a discovery.
#### Manage Links
If this is present, you have enabled the Hue link button feature for the ha-bridge. This button will bring up a dialog which contains all of the registered users created by linking the ha-bridge with a device. This allows you to revoke control for a created user so that it cannot access the ha-bridge by deleting that link user.
#### Show devices visible to
This filter is to sort devices that have an IP filter set. Type in the IP that is used in your device filter field.
#### Filter device type
This filter is for a quick method to find specific devices by their type listed in the dropdown.
### The Bridge Control Tab
This is where all of the configuration occurs for what ports and IP's the bridge runs on. It also contains the configurations for target devices so that Helper Tabs for configuration can be added as well as the connection information to control those devices.
#### Bridge server
This field is used to test the bridge server with the UPNP IP Address and to make sure that the bridge is responding.
#### Bridge Control Buttons
These buttons are for managing the bridge. The Save button is enabled when there is a change to the configuration. The Bridge Reinitialize button will recycle the internal running of the bridge in the java process. The Stop button will stop the java process. The Refresh button will refresh the page and settings.
#### The Security Dialog
This is where you can set the different security settings for the ha-bridge. The settings are the https enablement with the Keyfile path and password and described below, enabling Hue like operation to secure the Hue API with the internally generated user for the calls that are done after the link button. Another is to secure the hue API with a username/password that is created as well. The last one is the path to the exec garden if used for only allowing scripts and programs to be executed from this location only. The other fields are to add and delete users and to set and change passwords for those users. If there are no users in the system, the system will not require a username/password to operate.

The use https selection will require you to generate a java keytool keystore file for the ha-bridge to use. This will require the path to the keyfile and the password that was used to secure the keyfile. There are mulitple ways to add the key file. The basic way is to generate a self signed keystore using keytool as follows:

Step 1. Open the command console

Step 2. Run this command (Where indicate the number of days for which the certificate will be valid)
keytool -genkey -keyalg RSA -alias selfsigned -keystore keystore.jks -storepass ```<password>``` -validity 365 -keysize 2048

Step 3. Enter a password for the keystore. Note this ```<password>``` as you require this for configuring the server

Step 4. When prompted for first name and last name, enter the domain name of the server. For example, myserver or myserver.mycompany.com.

Step 5. Enter the other details, such as Organizational Unit, Organization, City, State, and Country.

Step 6. When prompted with Enter key ```<password>``` for , press Enter to use the same ```<password>``` as the keystore ```<password>```

Step 7. Run this command to verify the contents of the keystore
keytool -list -v -keystore selfsigned.jks

Step 8. When prompted, enter the keystore password note in Step 3. The basic information about the generated certificate is displayed. Verify that the Owner and Issuer are the same. Also, you should see the information you provided in Step 4 and 5.

The second way is to acquire a certified certificate to use in the keyfile. Such way to get one is use letsencrypt. Once you have the certifcate files you can follow this to create the keystore: https://community.letsencrypt.org/t/tutorial-java-keystores-jks-with-lets-encrypt/34754.

The easiest place to keep the keystore files is in your ha bridge data directory.
#### Configuration Path and File
The default location for the configuration file to contain the settings for the bridge is the relative path from where the bridge is started in "data/habridge.config". If you would like a different filename or directory, specify `<directory>/<filename>` explicitly.
#### Device DB Path and File
The default location for the db to contain the devices as they are added is "data/devices.db". If you would like a different filename or directory, specify `<directory>/<filename>`  explicitly.
#### UPNP IP Address
The server defaults to the first available address on the host if this is not given. This default may NOT be the correct IP that is your public IP for your host on the network. It is best to set this parameter to not have discovery issues. Replace this value with the server ipv4 address you would like to use as the address that any upnp device will call after discovery.
#### Use UPNP Address Interface
The server tries to bind to all interfaces to respond to UPNP request. Setting this to `true` will make the binding to the interface that has the `UPNP IP Address`. The default is to be all interfaces which is set as false.
#### Use Rooms for Alexa
This setting controls rooms for Alexa. If it is set to true, any device ID abaove 10000 is treated as a special group. The default is set as false.
#### Web Server IP Address
The server defaults to all interfaces on the machine (0.0.0.0). Replace this value with the server ipv4 address you would like to use as the address that will bind to a specific ip address on an interface if you would like. This is only necessary if you want to isolate how access is handled to the web UI.
#### Web Server Port
The server defaults to running on port 80. To override what the default is, specify a different number. ATTENTION: If you want to use any of the apps made for the Hue to control this bridge, you should keep this port set to 80.
#### UPNP Response Port
The upnp response port that will be used. The default is 50000.  
#### Vera Names and IP Addresses
Provide IP Addresses of your Veras that you want to utilize with the bridge. Also, give a meaningful name to each one so it is easy to decipher in the helper tab. When these names and IP's are given, the bridge will be able to control the devices or scenes by the call it receives and send it to the target Vera device/scene you configure.
#### Fibaro Names and IP Addresses
Provide IP Addresses of your Fibaros that you want to utilize with the bridge. Also, give a meaningful name to each one so it is easy to decipher in the helper tab. When these names and IP's are given, the bridge will be able to control the devices or scenes by the call it receives and send it to the target Fibaro device/scene you configure. There are filter switches available to limit some of the returns for devices and scenes such as use save logs, use user description, use only Lili command and a switch that cleans up format Trash Chars. The default filters are false for everything but Trash Chars.
#### Harmony Names and IP Addresses
Provide IP Addresses of your Harmony Hubs that you want to utilize with the bridge. Also, give a meaningful name to each one so it is easy to decipher in the helper tab. When these names and IP's are given, the bridge will be able to control the activity or buttons by the call it receives and send it to the target Harmony Hub and activity/button you configure. Also, an option of webhook can be called when the activity changes on the harmony hub that will send an HTTP GET call to the the address of your choosing. This can contain the replacement variables of ${activity.id} and/or ${activity.label}. Example : http://192.168.0.1/activity/${activity.id}/${activity.label} OR http://hook?a=${activity.label}
#### Hue Names and IP Addresses
Provide IP Addresses of your Hue Bridges that you want to proxy through the bridge. Also, give a meaningful name to each one so it is easy to decipher in the helper tab. When these names and IP's are given, the bridge will passthru the call it receives to the target Hue and device you configure.

Don't forget - You will need to push the link button when you got to the Hue Tab the first time after the process comes up.  (The user name is not persistent when the process comes up.)
#### HAL Names and IP Addresses
Provide IP Addresses of your HAL Systems that you want to utilize with the bridge. Also, give a meaningful name to each one so it is easy to decipher in the helper tab. When these names and IP's are given, the bridge will be able to control the devices or scenes by the call it receives and send it to the target HAL device you configure.
#### MQTT Client IDs and IP Addresses
Provide Client ID and IP Addresses and ports of your MQTT Brokers that you want to utilize with the bridge. Also, you can provide the username and password if you have secured your MQTT broker which is optional. When these Client ID and IP's are given, the bridge will be able to publish MQTT messages by the call it receives and send it to the target MQTT Broker you configure. The MQTT Messages Tab will become available to help you build messages.
#### Home Assistant Names and IP Addresses
<Provide IP Addresses and ports of your Home Assistant that you want to utilize with the bridge. Also, give a meaningful name to each one so it is easy to decipher in the helper tab. When these names and IP's are given, the bridge will be able to control the devices or scenes by the call it receives and send it to the target Home Assistant device you configure. 
#### HomeWizard Gateways Names and IP Addresses
Provide IP Addresses of your HomeWizard Systems that you want to utilize with the bridge. Also, give a meaningful name to each one so it is easy to decipher in the helper tab. When these names and IP's are given, the bridge will be able to control the devices or scenes by the call it receives and send it to the target HomeWizard device you configure. 
#### Domoticz Names and IP Addresses
Provide IP Addresses of your Domoticz Systems that you want to utilize with the bridge. Also, give a meaningful name to each one so it is easy to decipher in the helper tab. When these names and IP's are given, the bridge will be able to control the devices or scenes by the call it receives and send it to the target Domoticz device you configure.
#### Somfy Tahoma Names and IP Addresses
Provide user name and password used to login to www.tahomalink.com.  This needs to be provided if you're using the Somfy Tahoma features (for connecting to IO Homecontrol used by Velux among others). There is no need to give any IP address or host information as this contacts your cloud account.  *Note:* you have to 'turn on' a window to open it, and 'turn off' to close.
#### OpenHAB Names and IP Addresses
Provide IP Addresses of your OpenHAB Systems that you want to utilize with the bridge. Also, give a meaningful name to each one so it is easy to decipher in the helper tab. When these names and IP's are given, the bridge will be able to control the devices or scenes by the call it receives and send it to the target OpenHAB device you configure. 
#### FHEM Names and IP Addresses
Provide IP Addresses of your FHEM Systems that you want to utilize with the bridge. Also, give a meaningful name to each one so it is easy to decipher in the helper tab. When these names and IP's are given, the bridge will be able to control the devices or scenes by the call it receives and send it to the target FHEM device you configure.
#### Mozilla IOT Names and IP Addresses
Provide IP Addresses of your Mozilla IOT Systems that you want to utilize with the bridge. Also, give a meaningful name to each one so it is easy to decipher in the helper tab. When these names and IP's are given, the bridge will be able to control the devices by the call it receives and send it to the target Mozilla IOT device you configure.
#### HomeGenie Names and IP Addresses	
Provide IP Addresses of your HomeGenie Systems that you want to utilize with the bridge. Also, give a meaningful name to each one so it is easy to decipher in the helper tab. When these names and IP's are given, the bridge will be able to control the devices by the call it receives and send it to the target HomeGenie device you configure. There is an extra Other Types field that you can add types that are not within the defualt of "light", "Switch" and "Dimmer" that you may want to control.
#### Nest Username
Provide the username and password of the home.nest.com account for the Nest user. This needs to be given if you are using the Nest features. There is no need to give any ip address or host information as this contacts your cloud account. Also, you can set Nest Temp Fahrenheit that allows the value being sent into the bridge to be interpreted as Fahrenheit or Celsius. The default is to have Fahrenheit.
#### LIFX Support
This setting will have the ha-bridge look for LIFX devices on your network. Since this is broadcast based, there is no other info needed.
#### Broadlink Support
This setting will have the ha-bridge look for Broadlink devices on your network (i.e. RM2 IRcontrollers, Smart Power Strips and Smart Plugs). Since this is broadcast based, there is no other info needed.
#### Emulate Hue Hub Version
This setting is used to set the version that the ha-bridge will return in the hub version field. The default is 9999999999 which should work to be higher than the versions that are being used.
#### Emulate MAC
This setting is in bridge-id, uuid, etc. in ha-bridge hue config replies. Leave blank unless needed as it is mainly a tool to keep a config to a specific set of devices whtn the ha-bridge is moved to another machine
#### Button Press/Call Item Loop Sleep Interval (ms)
This setting is the time used in between button presses when there is multiple buttons in a button device. It also controls the time between multiple items in a custom device call. This is defaulted to 100ms and the number represents milliseconds (1000 milliseconds = 1 second).
#### Log Messages to Buffer
This controls how many log messages will be kept and displayed on the log tab. This does not affect what is written to the standard output for logging. The default is 512. Changing this will incur more memory usage of the process.
#### Trace UPNP Calls
Turn on tracing for upnp discovery messages to the log. The default is false.
#### Trace State Changes
Turn on tracing for calls to the ha-bridge and send these information messages to the log for debugging. This a quick way to watch the state changes without turning the HueMulator debugging. The default is false.
#### UPNP Send Delay
This setting is for the upnp spec to delay a certain amount between upnp response messages sent back to a client. This is defaulted to 650ms and can be tuned to any value up to 1500ms.
#### ID Seed
The seed that starts numbering from this value and is used in the 'Renumbering' button on the 'Bridge Devices' tab. The defaul for this value is 100.
#### My Echo URL
This sets the URL that is used in the menu bar to ge to your echo. For certain countries, this needs to be set to a different URL.

At the bottom of the screen is the "Bridge Settings Backup" which can be accessed with clicking on the `+` to expand this frame. Here you can backup and restore configurations that you have saved. These configs can be named or by clicking the `Backup Settings' button will create a backup and name it for you. You can manage these backups by restoring them, deleting them, downloading them by clicking on the file name and uploading a saved configuration backup on your local machine.
### The Logs Tab
This screen displays the last 512 or number of rows defined in the config screen of the log so you don't have to go to the output of your process. The `Update Log` button refreshes the log as this screen does not auto refresh. FYI, when the trace upnp setting is turned on in the configuration, the messages will show here.

The bottom part of the Logs Screen has configuration to change the logging levels as it is running. The ROOT is the basic setting and will turn on only top level logging. To set logging at a lower level, select the `Show All Loggers` checkbox and then you can set the explicit level on each of the processes components. The most helpful logger would be setting DEBUG for com.bwssystems.HABridge.hue.HueMulator component. Changing this and then selecting the `Update Log Levels` button applies the new log settings.
### Bridge Device Additions
You must configure devices before you will have anything for the Echo or other controller that is connected to the ha-bridge to receive.
#### Helpers
The easy way to get devices configured is with the use of the helpers for the Vera or Harmony, Nest, Hue and others to create devices that the bridge will present.

For the Helpers, each item being presented from the target system has a button such as `Build Item`, `Build A Button` or specific tasks such as `Temp` for thermostats that is used to create the specific device parameters. The build action buttons will put you into the edit screen. The next thing to check is the name for the bridge device that it is something that makes sense especially if you using the ha-bridge with an Echo or Google Home as this is what the Echo or Google Home will interpret as the device you want. Also, you can go back to any helper tab and click a build action button to add another item for a multi-command. After you are done in the edit tab, click the `Add Bridge Device` to finish that selection setup. OR you can go back to any helper and use `Build Item` or as such to add another item into the current device being shown in the `Add/Edit` tab. This allows you to create custom devices that execute many devices at once.

The helper tabs will also show you what you have already configured for that target type. Click on the `+` and you will see them and be able to delete them.
#### The Add/Edit Tab
This is the main device editing page to modify details or you can add a device through this tab. This allows you to manually enter the name, the on/dim/off/color items and select if there are custom handling with the type of call that can be made. This allows for control of anything that has a distinct request that can be executed so you are not limited to the Vera, Harmony, Nest or other Hue.

There is a new format for the on/dim/off/color URL areas. The new editor handles the intricacies of the components, but is broken down here for explanation.

It is imperative when adding a line by hand that you hit the ```Add``` button at the end of the line before adding or updating the whole entry.

```Update Bridge Device``` Button is for editing a current bridge device entry.

```Add Bridge Device``` Button is to ccreate a new bridge device entry.

```Clear``` Button is used to clear the contents of the entry on the screen.

```Change Edit Mode``` Button is used to swithc back to a pure editor for the on/off/dim/color lines within the device entry.

Here are the fields that can be put into the call item:

Json Type | field name | What | Use
----------|------------|------|-----
String or JsonElement | item | This is the payload that will be called for devices | Required
Integer | count | This is how many times this items will be executed | Optional
Integer | delay | This is how long we will wait until the next call after | Optional
String | type | This is the type of device we are executing | Required
String | filterIPs | This is used filter on the IPs given in the list | Optional
String | httpVerb | This is the http command if given, default is GET | Optional
String | httpBody | Send this Body with a PUT or POST | Optional
String | httpHeaders | Send these headers with the http call | Optional
String | contentType | Define the type of content in the body | Optional

Example from device.db:
```
[{"item":<a String that is quoted or another JSON object>,"type":"<atype>"."count":X."delay":X."filterIPs":"<comma separated list of IP addresses that are valid>"."httpVerb":"<GET,PUT,POST>","httpBody":"<body info>","httpHeaders":[{"name":"header name","value":"header value"},{"name":"another header","value":"another value"}],"contentType":"<http content type i.e application/json>"},{"item":<another item>,"type":"<aType>"}]
```

The format of the example is in JSON where the JSON tags equate to the UI labels of the On Items/Dim Items/Off Items. i.e.: JSON item = UI Target Item, JSON type = UI Type, etc...

The Add/Edit tab will show you the fields to fill in for the above in a form, when you have completed putting in the things you want, make sure to hit the `Add` button at the right.

The format of the item can be the default HTTP request which executes the URLs formatted as `http://<your stuff here>` as a GET. Other options to this are to select the HTTP Verb and add the data type and add a body that is passed with the request. Secure https is supported as well, just use `https://<your secure call here>`. When using POST and PUT, you have the ability to specify the body that will be sent with the request as well as the application type for the http call.

The valid device types are: "custom", "veraDevice", "veraScene", "harmonyActivity", "harmonyButton", "nestHomeAway", "nestThermoSet", "hueDevice", "halDevice",
	"halButton", "halHome", "halThermoSet", "mqttMessage", "cmdDevice", "hassDevice", "homewizardDevice", "tcpDevice", "udpDevice", "httpDevice", "domoticzDevice", "somfyDevice"

Filter Ip example:
```
Turn on Lights in Bedroom 1 (http://api.call.here/1) - Restricted to Echo 1 (10.1.1.1)
Turn on Lights in Bedroom 2 (http://api.call.here/2) - Restricted to Echo 2 (10.2.2.2)
Turn on Lights in Bedroom 3 (http://api.call.here/3) - Restricted to Echo 3 (10.3.3.3)

Device: "Lights"
On URL: [{"item":"http://api.call.here/1", "httpVerb":"POST", "httpBody":"value1=1&value2=2","type":"httpDevice","filterIPs":"10.1.1.1"},{"item":"http://api.call.here/2", "httpVerb":"POST", "httpBody":"value1=1&value2=2","type":"httpDevice","filterIPs":"10.2.2.2"},{"item":"http://api.call.here/3", "httpVerb":"POST", "httpBody":"value1=1&value2=2","type":"httpDevice","filterIPs":"10.3.3.3"}]
```

Headers can be added as well using a Json construct [{"name":"header type name","value":"the header value"}] with the format example:
```
[{"name":"Cache-Control","value":"no-store, no-cache, must-revalidate, post-check=0, pre-check=0"},
{"name":"Pragma","value":"no-cache"}]
```

Another option that is detected by the bridge is to use UDP or TCP direct calls such as `udp://<ip_address>:<port>/<your stuff here>` to send a UDP request. TCP calls are handled the same way as `tcp://<ip_address>:<port>/<your stuff here>`. If your data for the UDP or TCP request is formatted as "0x00F009B9" lexical hex format, the bridge will convert the data into a binary stream to send.

You can also use the value replacement constructs within these statements. Such as using the expressions "${time.format(Java time format string)}" for inserting a date/time stamp, "${time.millis}" for inserting a pure timestamp (milliseconds from 1.1.1970), ${intensity.percent} or ${intensity.percent.hex} for 0-100 or ${intensity.decimal_percent} for 0.00-1.00 or ${intensity.byte} or ${intensity.byte.hex} for 0-255 for straight pass through of the value or items that require special calculated values using ${intensity.math()} i.e. "${intensity.math(X/4)}" or "${intensity.math(X/4).hex}". See Value Passing Controls Below.
Examples:
```

[{"item":"http://192.168.1.1:8180/set/this/value/${intensity.percent}","type":"httpDevice","httpVerb":"GET"}]


[{"item":"http://192.168.1.1:8280/set/this","type":"httpDevice","httpVerb":"PUT","httpBody":{"someValue":"${intensity.byte}"}}]

[{"item":"udp://192.168.1.1:5000/0x45${intensity.percent}55","type":"udpDevice"}]

[{"item":"udp://192.168.2.2:6000/fireoffthismessage\n","type":"udpDevice"}]

[{"item":"tcp://192.168.3.3:9000/sendthismessage","type":"tcpDevice"}]

[{"item":"tcp://192.168.4.4:10000/0x435f12dd${intensity.math((X -4)*50)}438c","type":"tcpDevice"}]

[{"item":"tcp://192.168.5.5:110000/0x","type":"tcpDevice"}]
```

#### Multiple Call Construct
Also available is the ability to specify multiple commands in the On Items, Dim Items, Off Items and Color Items by adding a new line to the item. When doing this manually, make sure to hit the `Add` button to the right of the row. If you do not hit the add button, it will not save when the add or update device buttons are selected. You can also add by going to a helper tab after you have entered the `Add/Edit` tab and using the helpers 'Build' buttons to add a new item line in the respected On/Dim/Off/Color items areas.
#### Script or Command Execution
The release as of v2.0.0 will now support the execution of a local script or program. This will blindly fire off a process to run and is bound by the privileges of the java process.

To configure this type of manual add, you will need to select the Device type of "Execute Script/Program".

In the URL areas, the format of the execution is just providing what command line you would like to run, or using the multiple call item construct described above.

If you are running a script on a system, you will need to provide the shell interface it will use before the script such as `sh` or `cmd.exe`.
#### Value Passing Controls
There are multiple replacement constructs available to be put into any of the calls except Harmony items, Net Items and HAL items. These constructs are: "${time.format(Java time format string)}", "${intensity.percent}", "${intensity.percent.hex}", "${intensity.decimal_percent}", "${intensity.byte}", "${intensity.byte.hex}", "${intensity.math(using X in your calc)}" and "${intensity.math(using X in your calc).hex}".

You can control items that require special calculated values using ${intensity.math(<your expression using "X" as the value to operate on>)} i.e. "${intensity.math(X/4)}".

For the items that want to have a date time put into the message, utilize ${time.format(yyyy-MM-ddTHH:mm:ssXXX)} where "yyyy-MM-ddTHH:mm:ssXXX" can be any format from the Java SimpleDateFormat documented here: https://docs.oracle.com/javase/8/docs/api/java/text/SimpleDateFormat.html. Also, there is a ${time.millis} which will put the millis timestamp where this replacement control is located.

Color has been added as a replacement control and the available values are ${color.r}, ${color.g}, ${color.b} which are representations of each color as 0 - 255. There are hex equivalents as well as ${color.rx}, ${color.gx}, ${color.bx} and ${color.rgbx} as 2 place hex representations except for rgbx which is a six place hex representation.

Color can also be replaced with ${color.hsl} which will output a string in the Hue/Saturation/Brightness comma-delineated format, useful for many devices under OpenHAB. The format will be a 0-360 hue value, a 0-100 saturation value, and a 0-100 brightness value, separated by commas. (Such devices usually accept either a full HSB value in that format, or a single ${intensity.percent} value for the dimming value, sent as raw text in a POST request.)

Special handling for milights is included and is handled by ${color.milight:x}. The usage for that is as follows: udp://ip:port/0x${color.milight:x} where x is a number between 0 and 4 (0 all groups, 1-4 specific group). The group is necessary in case the color turns out to be white. The correct group on must of course be sent before that udp packet.
Note that milight can only use 255 colors and white is handled completely separate for the rgbw strips, so setting temperature via ct with milight does something but not really the desired result

Also, device data can be inserted into your payloads by the use of "${device.name}", "${device.id}", "${device.uniqueid}", "${device.targetDevice}", "${device.mapId}", "${device.mapType}", "${device.deviceType}", "${device.requesterAddress}", "${device.description}" and "${device.comments}". These work just like the dimming value replacements.
e.g.
```
[{"item":"http://192.168.1.201:3480/data_request?id=action&output_format=json&DeviceNum=10&serviceId=urn:upnp-org:serviceId:Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget=${intensity.math(X/4)}","type":"httpDevice"}]

[{"item":"udp://192.168.1.1:5000/0x45${intensity.percent}55","type":"udpDevice"}]

[{"item":"tcp://192.168.1.1:5000/This is the intensity real value ${intensity.byte}","type":"tcpDevice"}]

[{"item":{"clientId":"TestClient","topic":"Yep","message":"This is the time ${time.format(yyyy-MM-ddTHH:mm:ssXXX)}"},"type":"mqttDevice"}]

```

Also, you may want to use the REST APIs listed below to configure your devices.
## Ask Alexa
After this Tell Alexa: "Alexa, discover my devices". If there is an issue you can go to the `Menu / Settings / Connected Home` for the echo on the mobile app or your browser and have Alexa forget all devices and then do the discovery again.

Then you can say "Alexa, Turn on the office light" or whatever name you have given your configured devices.  

Here is the table of items to use to tell Alexa what you want to do, this has changed over time due to Amazon reserving words for skills:

To do this... |	Say this...
--------------|------------
ON Commands | Alexa, turn on `<Device Name>`
OFF Commands | Alexa, turn off `<Device Name>`
DIM Commands | Alexa, brighten `<Device Name>` to `<Position>`
DIM Commands| Alexa, dim `<Device Name>` to `<Position>`
DIM Commands| Alexa, brighten `<Device Name>`
DIM Commands| Alexa, dim `<Device Name>`
DIM Commands| Alexa, set `<Device Name>` to `<Position>`

To see what Alexa thinks you said, you can check in the home page for your Alexa.

To view or remove devices that Alexa knows about, you can use the mobile app `Menu / Settings / Connected Home` or go to http://echo.amazon.com/#cards.

## Google Assistant
Google Home is supported as of v3.2.0 and forward, but only if the bridge is running on port 80.

**ISSUE: Google Home does NOT support local connection to Philips Hue Hubs and requires that it connect to meethue.com. Since the ha-bridge only emulates the local API, and is not associated with Philips, this method will not work. If you have an older Google Home application, this may still work. YMMV.**

Use the Google Home app on a phone to add new "home control" devices by going into `Settings / Home Control / +`
as described [here](https://support.google.com/googlehome/answer/7124115?hl=en&ref_topic=7125624#homecontrol).
Click on `Philips Hue` under the `Add new` section. If ha-bridge is on the same network as the
phone as well as the Home device, then the app should quickly pass through the pairing step and
populate with all of the devices. If instead it takes you to a Philips Hue login page, this means
that the bridge was not properly discovered.

Then you can say "OK Google, Turn on the office light" or whatever name you have given your configured devices.

The Google Assistant can also group lights into rooms as described in the main [help article](https://support.google.com/googlehome/answer/7072090?hl=en&ref_topic=7029100).

Here is the table of items to use to tell Google what you want to do. Note that either "OK Google"
or "Hey Google" can be used as a trigger.

To do this: | Say "Hey Google", then...
------------|--------------------------
To turn on/off a light | "Turn on <light name>"
Dim a light | "Dim the <light name>"
Brighten a light | "Brighten the <light name>"
Set a light brightness to a certain percentage | "Set <light name> to 50%"
Dim/Brighten lights by a certain percentage | "Dim/Brighten <light name> by 50%"
Turn on/off all lights in room | "Turn on/off lights in <room name>"
Turn on/off all lights | "Turn on/off all of the lights"

To see what Home thinks you said, you can ask "Hey Google, What did I say?" or check the history in the app.

New or removed devices are picked up automatically as soon as they are added/removed from ha-bridge.
No re-discovery step is necessary.

## Configuration REST API Usage
This section will describe the REST API available for configuration. The REST body examples are all formatted for easy reading, the actual body usage should be like this:
```
{"var1":"value1","var2":"value2","var3:"value3"}
```
The body should be all in one string and not separated by returns, tabs or spaces. FYI, GET items do not require a body element.  If you would like to see example return of json data for full Harmony Hub configuration if configured, which includes activities and devices, take a look at the resource file config.data. If you are interested in how the json data looks for the HA bridge configuration, after creating a device, look at the data directory for the device.db.
These calls can be accomplished with a REST tool using the following URLs and HTTP Verb types:
### Add a device
Add a new device to the HA Bridge configuration. There is a basic examples and then three alternate examples for the add. Please note that dimming is supported as well as custom value based on the dimming number given from the echo. This is under the Dimming and Value example.
```
POST http://host/api/devices
```
#### Body Arguments
Name |	Type |	Description | Required
-----|-------|--------------|------------
name | string | A name for the device. This is also the utterance value that the Echo will use. | Required
mapId | string | This identifies the id of the source items config and used by the ui to sort what has been configured | Optional
mapType | string | This identifies what type of source item was used from the helper | Optional
deviceType | string | This identifies what type of device entry this is. It is used by the system and should be the values of "switch", "scene", "custom", "activity", "button", "thermo", "passthru", "exec", "UDP", "TCP" or "custom". | Required
targetDevice | string | A name given to the target when there are multiples of a given type in the configuration | Optional
onUrl | string | This is the URL or Data Description that is executed for an "on" request. | Required
dimUrl | string | This is the URL or Data Description that is executed for an "on" request when a intensity value is sent. | Optional
offUrl | string | This is the URL or Data Description that is executed for an "off" request. | Optional
headers | string | This is a header or list of headers that is used for http/https calls when given. | Optional
httpVerb | string | This is used for "custom" calls that the user would like to execute. The values can only be "GET, "PUT", "POST". | Optional
contentType | string | This is an http type string such as "application/text" or "application/xml" or "application/json". | Optional
contentBody | string | This is the content body that you would like to send when executing an "on" request. | Optional
contentBodyOff | string | This is the content body that you would like to send when executing an "off" request. | Optional
#### Basic Example
```
{
"name" : "bedroom light",
"deviceType" : "switch",
  "onUrl" : "[{\"item\":\"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum=41\",\"type\":\"veraDevice\"}]",
  "offUrl" : "[{\"item\":\"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=41\",\"type\":\"veraDevice\"}]"
}
```
#### Dimming Control Example
Dimming is also supported by using the expressions ${intensity.percent} for 0-100 or ${intensity.decimal_percent} for 0.00-1.00 or ${intensity.byte} for 0-255 for straight pass through of the value.
e.g.
```
{
    "name": "entry light",
    "deviceType": "switch",
    "offUrl": "[{\"item\":\"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=31\",\"type\":\"veraDevice\"}]",
    "onUrl": "[{\"item\":\"http://192.168.1.201:3480/data_request?id=action&output_format=json&DeviceNum=31&serviceId=urn:upnp-org:serviceId:Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget=${intensity.percent}\",\"type\":\"veraDevice\"}]"
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
    "offUrl": "[{\"item\":\"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=10\",\"type\":\"veraDevice\"}]",
    "onUrl": "[{\"item\":\"http://192.168.1.201:3480/data_request?id=action&output_format=json&DeviceNum=10&serviceId=urn:upnp-org:serviceId:Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget=${intensity.math(X/4)}\",\"type\":\"veraDevice\"}]"
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
    "offUrl": "[{\"item\":\"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=31\",\"httpVerb\":\"POST\",\"contentType\" : \"application/json\",\"httpBody\" : \"{\"fooBar\":\"baz_off\"}]",
    "onUrl": "[{\"item\":\"http://192.168.1.201:3480/data_request?id=action&output_format=json&DeviceNum=31&serviceId=urn:upnp-org:serviceId:Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget=${intensity.percent}\",\"type\":\"httpDevice\",\"httpVerb\":\"POST\",\"contentType\" : \"application/json\",\"httpBody\" : \"{\"fooBar\":\"baz_on\"}]"
}
```
#### Custom Usage URLs Example
Anything that takes an action as a result of an HTTP request will probably work and you can also use the dimming and value control commands within the URLs as well - like putting Vera in and out of night mode:  
```
{
  "name": "night mode",
  "deviceType": ""custom",
  "offUrl": "[{\"item\":\"http://192.168.1.201:3480/data_request?id=lu_action&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=SetHouseMode&Mode=1\",\"type\":\"httpDevice\"}]",
  "onUrl": "[{\"item\":\"http://192.168.1.201:3480/data_request?id=lu_action&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=SetHouseMode&Mode=3\",\"type\":\"httpDevice\"}]"
}
```
Here is a UDP example that can send binary data.
```
{
  "name": "UDPPacket",
  "deviceType": "custom",
  "offUrl": "[{\"item\":\"udp://192.168.1.1:8899/0x460055\",\"type\":\"udpDevice\"}]",
  "onUrl": "[{\"item\":\"udp://192.168.1.1:8899/0x450055\",\"type\":\"udpDevice\"}]"
}
```
#### Response
Name |	Type |	Description
-----|-------|-------------
id   | number | This is the ID assigned to the device and used for lookup.
name | string | A name for the device. This is also the utterance value that the Echo will use.
mapId | string | This identifies the id of the source items config and used by the ui to sort what has been configured | Optional
mapType | string | This identifies what type of source item was used from the helper | Optional
deviceType | string | This identifies what type of device entry this is. It is used by the system and should be the values of "switch", "scene", "custom", "activity", "button", "thermo", "passthru", "exec", "UDP", "TCP" or "custom". | Required
targetDevice | string | A name given to the target when there are multiples of a given type in the configuration | Optional
onUrl | string | This is the URL or Data Description that is executed for an "on" request. | Required
dimUrl | string | This is the URL or Data Description that is executed for an "on" request when a intensity value is sent. | Optional
offUrl | string | This is the URL or Data Description that is executed for an "off" request. | Optional
headers | string | This is a header or list of headers that is used for http/https calls when given. | Optional
httpVerb | string | This is used for "custom" calls that the user would like to execute. The values can only be "GET, "PUT", "POST".
contentType | string | This is an http type string such as "application/text" or "application/xml" or "application/json".
contentBody | string | This is the content body that you would like to send when executing an "on" request.
contentBodyOff | string | This is the content body that you would like to send when executing an "off" request.
```
{
"id" : "12345",
"name" : "bedroom light",
"deviceType" : "switch",
  "onUrl" : [{"item":"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum=41","type":"veraDevice"}],
  "offUrl" : [{"item":"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=41","type":"veraDevice"}]
}
```
### Update a Device
Update an existing device using its ID that was given when the device was created and the update could contain any of the fields that are used and shown in the previous examples when adding a device.

**Note: You must supply all fields of the device in return as this is a replacement update for the given id.**
```
PUT http://host:port/api/devices/<id>
```
#### Body Arguments
Name |	Type |	Description | Required
-----|-------|--------------|------------
id   | number | This is the ID assigned to the device and used for lookup.
name | string | A name for the device. This is also the utterance value that the Echo will use. | Required
mapId | string | This identifies the id of the source items config and used by the ui to sort what has been configured | Optional
mapType | string | This identifies what type of source item was used from the helper | Optional
deviceType | string | This identifies what type of device entry this is. It is used by the system and should be the values of "switch", "scene", "custom", "activity", "button", "thermo", "passthru", "exec", "UDP", "TCP" or "custom". | Required
targetDevice | string | A name given to the target when there are multiples of a given type in the configuration | Optional
onUrl | string | This is the URL or Data Description that is executed for an "on" request. | Required
dimUrl | string | This is the URL or Data Description that is executed for an "on" request when an intensity value is sent. | Optional
offUrl | string | This is the URL or Data Description that is executed for an "off" request. | Optional
headers | string | This is a header or list of headers that is used for http/https calls when given. | Optional
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
  "onUrl" : "[{\"item\":\"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum=41\",\"type\":\"veraDevice\"}]",
  "offUrl" : "[{\"item\":\"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=41\",\"type\":\"veraDevice\"}]"
}
```
#### Response
```
{
"id" : "6789",
"name" : "table light",
"deviceType" : "switch",
  "onUrl" : [{"item":"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum=41","type":"veraDevice"}],
  "offUrl" : [{"item":"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=41","type":"veraDevice"}]
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
  "onUrl" : [{"item":"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum=41","type":"veraDevice"}],
  "offUrl" : [{"item":"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=41","type":"veraDevice"}]
}
{
"id" : "6789",
"name" : "table light",
"deviceType" : "switch",
  "onUrl" : [{"item":"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum=41","type":"veraDevice"}],
  "offUrl" : [{"item":"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=41","type":"veraDevice"}]
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
  "onUrl" : [{"item":"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum=41","type":"veraDevice"}],
  "offUrl" : [{"item":"http://192.168.1.201:3480/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum=41","type":"veraDevice"}]
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
GET http://host:port/system/habridge/version
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
hub | string | The name of the given target hub.
activity:label | string | The name of the Harmony activity.
activity:id | integer | The id of the Harmony activity.
#### Harmony activities data example
```
[{"hub":"ChicagoanHub","activity":{"label":"Watch TV","suggestedDisplay":"Default","id":15823996,"activityTypeDisplayName":"Default","controlGroup":[{"name":"NumericBasic","function":[{"name":"NumberEnter","label":"Number Enter","action":"{\"command\":\"Enter\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Hyphen","label":"-","action":"{\"command\":\"-\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number0","label":"0","action":"{\"command\":\"0\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number1","label":"1","action":"{\"command\":\"1\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number2","label":"2","action":"{\"command\":\"2\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number3","label":"3","action":"{\"command\":\"3\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number4","label":"4","action":"{\"command\":\"4\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number5","label":"5","action":"{\"command\":\"5\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number6","label":"6","action":"{\"command\":\"6\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number7","label":"7","action":"{\"command\":\"7\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number8","label":"8","action":"{\"command\":\"8\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number9","label":"9","action":"{\"command\":\"9\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"Volume","function":[{"name":"Mute","label":"Mute","action":"{\"command\":\"Mute\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"VolumeDown","label":"Volume Down","action":"{\"command\":\"VolumeDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"VolumeUp","label":"Volume Up","action":"{\"command\":\"VolumeUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"Channel","function":[{"name":"PrevChannel","label":"Prev Channel","action":"{\"command\":\"Last\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"ChannelDown","label":"Channel Down","action":"{\"command\":\"ChannelDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"ChannelUp","label":"Channel Up","action":"{\"command\":\"ChannelUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"NavigationBasic","function":[{"name":"DirectionDown","label":"Direction Down","action":"{\"command\":\"DirectionDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"DirectionLeft","label":"Direction Left","action":"{\"command\":\"DirectionLeft\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"DirectionRight","label":"Direction Right","action":"{\"command\":\"DirectionRight\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"DirectionUp","label":"Direction Up","action":"{\"command\":\"DirectionUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Select","label":"Select","action":"{\"command\":\"Select\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"TransportBasic","function":[{"name":"Play","label":"Play","action":"{\"command\":\"Play\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Rewind","label":"Rewind","action":"{\"command\":\"Rewind\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Pause","label":"Pause","action":"{\"command\":\"Pause\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"FastForward","label":"Fast Forward","action":"{\"command\":\"FastForward\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"TransportRecording","function":[{"name":"Record","label":"Record","action":"{\"command\":\"Record\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"TransportExtended","function":[{"name":"FrameAdvance","label":"Frame Advance","action":"{\"command\":\"Slow\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"SkipBackward","label":"Skip Backward","action":"{\"command\":\"Replay\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"SkipForward","label":"Skip Forward","action":"{\"command\":\"Advance\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"NavigationDVD","function":[{"name":"Back","label":"Back","action":"{\"command\":\"Back\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"NavigationDSTB","function":[{"name":"C","label":"C","action":"{\"command\":\"C\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"B","label":"B","action":"{\"command\":\"B\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"D","label":"D","action":"{\"command\":\"D\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"A","label":"A","action":"{\"command\":\"A\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"List","label":"List","action":"{\"command\":\"List\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Live","label":"Live","action":"{\"command\":\"LiveTv\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"TiVo","function":[{"name":"ThumbsDown","label":"Thumbs Down","action":"{\"command\":\"ThumbsDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"ThumbsUp","label":"Thumbs Up","action":"{\"command\":\"ThumbsUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"TiVo","label":"TiVo","action":"{\"command\":\"TiVo\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"NavigationExtended","function":[{"name":"Guide","label":"Guide","action":"{\"command\":\"Guide\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Info","label":"Info","action":"{\"command\":\"Info\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Clear","label":"Backspace","action":"{\"command\":\"Clear\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"ColoredButtons","function":[{"name":"Green","label":"Green","action":"{\"command\":\"Green\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Red","label":"Red","action":"{\"command\":\"Red\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Blue","label":"Blue","action":"{\"command\":\"Blue\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Yellow","label":"Yellow","action":"{\"command\":\"Yellow\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]}],"activityOrder":0,"tuningDefault":true,"fixit":{"29671749":{"id":"29671749","power":"ON","input":"HDMI 4","alwaysOn":false,"relativePower":true},"29695418":{"id":"29695418","power":"OFF","alwaysOn":false,"relativePower":true},"29671742":{"id":"29671742","power":"ON","input":"","alwaysOn":false,"relativePower":false},"29671764":{"id":"29671764","alwaysOn":false,"relativePower":false},"29695438":{"id":"29695438","power":"OFF","alwaysOn":false,"relativePower":true},"29695467":{"id":"29695467","alwaysOn":false,"relativePower":false},"29695485":{"id":"29695485","alwaysOn":false,"relativePower":false}},"type":"VirtualTelevisionN","icon":"userdata: 0x4454e0","baseImageUri":"https://rcbu-test-ssl-amr.s3.amazonaws.com/"}},{"hub":"ChicagoanHub","activity":{"label":"PowerOff","suggestedDisplay":"Default","id":-1,"activityTypeDisplayName":"Default","controlGroup":[],"tuningDefault":false,"fixit":{"29671749":{"id":"29671749","power":"OFF","alwaysOn":false,"relativePower":true},"29695418":{"id":"29695418","power":"OFF","alwaysOn":false,"relativePower":true},"29671742":{"id":"29671742","power":"OFF","alwaysOn":false,"relativePower":false},"29671764":{"id":"29671764","alwaysOn":false,"relativePower":false},"29695438":{"id":"29695438","power":"OFF","alwaysOn":false,"relativePower":true},"29695467":{"id":"29695467","alwaysOn":false,"relativePower":false},"29695485":{"id":"29695485","alwaysOn":false,"relativePower":false}},"type":"PowerOff","icon":"Default"}},{"hub":"ChicagoanHub","activity":{"label":"Watch a Movie","suggestedDisplay":"Default","id":15839533,"activityTypeDisplayName":"Default","controlGroup":[{"name":"NumericBasic","function":[{"name":"Number0","label":"0","action":"{\"command\":\"0\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number1","label":"1","action":"{\"command\":\"1\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number2","label":"2","action":"{\"command\":\"2\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number3","label":"3","action":"{\"command\":\"3\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number4","label":"4","action":"{\"command\":\"4\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number5","label":"5","action":"{\"command\":\"5\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number6","label":"6","action":"{\"command\":\"6\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number7","label":"7","action":"{\"command\":\"7\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number8","label":"8","action":"{\"command\":\"8\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number9","label":"9","action":"{\"command\":\"9\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Clear","label":"Clear","action":"{\"command\":\"Clear\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"Volume","function":[{"name":"Mute","label":"Mute","action":"{\"command\":\"Mute\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"VolumeDown","label":"Volume Down","action":"{\"command\":\"VolumeDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"VolumeUp","label":"Volume Up","action":"{\"command\":\"VolumeUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"NavigationBasic","function":[{"name":"DirectionDown","label":"Direction Down","action":"{\"command\":\"DirectionDown\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"DirectionLeft","label":"Direction Left","action":"{\"command\":\"DirectionLeft\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"DirectionRight","label":"Direction Right","action":"{\"command\":\"DirectionRight\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"DirectionUp","label":"Direction Up","action":"{\"command\":\"DirectionUp\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Select","label":"Select","action":"{\"command\":\"Ok\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"TransportBasic","function":[{"name":"Stop","label":"Stop","action":"{\"command\":\"Stop\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Play","label":"Play","action":"{\"command\":\"Play\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Rewind","label":"Rewind","action":"{\"command\":\"Rewind\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Eject","label":"Eject","action":"{\"command\":\"Eject\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Pause","label":"Pause","action":"{\"command\":\"Pause\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"FastForward","label":"Fast Forward","action":"{\"command\":\"FastForward\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"TransportExtended","function":[{"name":"FrameAdvance","label":"Frame Advance","action":"{\"command\":\"Step\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"SkipBackward","label":"Skip Backward","action":"{\"command\":\"SkipBack\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"SkipForward","label":"Skip Forward","action":"{\"command\":\"SkipForward\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"NavigationDVD","function":[{"name":"Return","label":"Return","action":"{\"command\":\"Return\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"TopMenu","label":"Top Menu","action":"{\"command\":\"TopMenu\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Menu","label":"Menu","action":"{\"command\":\"Menu\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Subtitle","label":"Subtitle","action":"{\"command\":\"Subtitle\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"PlayMode","function":[{"name":"Repeat","label":"Repeat","action":"{\"command\":\"Repeat\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"Program","function":[{"name":"Program","label":"Program","action":"{\"command\":\"Program\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Bookmark","label":"Bookmark","action":"{\"command\":\"Bookmark\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"GameType3","function":[{"name":"Home","label":"Home","action":"{\"command\":\"Home\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"NavigationExtended","function":[{"name":"Clear","label":"Backspace","action":"{\"command\":\"Clear\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"ColoredButtons","function":[{"name":"Green","label":"Green","action":"{\"command\":\"Green\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Red","label":"Red","action":"{\"command\":\"Red\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Blue","label":"Blue","action":"{\"command\":\"Blue\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Yellow","label":"Yellow","action":"{\"command\":\"Yellow\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]}],"activityOrder":3,"tuningDefault":false,"fixit":{"29671749":{"id":"29671749","power":"ON","input":"HDMI 2","alwaysOn":false,"relativePower":true},"29695418":{"id":"29695418","power":"ON","alwaysOn":false,"relativePower":true},"29671742":{"id":"29671742","power":"ON","input":"","alwaysOn":false,"relativePower":false},"29671764":{"id":"29671764","alwaysOn":false,"relativePower":false},"29695438":{"id":"29695438","power":"OFF","alwaysOn":false,"relativePower":true},"29695467":{"id":"29695467","alwaysOn":false,"relativePower":false},"29695485":{"id":"29695485","alwaysOn":false,"relativePower":false}},"type":"VirtualDvd","icon":"userdata: 0x4454e0","baseImageUri":"https://rcbu-test-ssl-amr.s3.amazonaws.com/"}}]
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
hub | string | The name of the given target hub.
device:id | integer | The id of the Harmony device.
device:label | string | The name of the Harmony device.
device:controlGroup | object | The structure that contains buttons grouped by control areas.

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
[{"hub":"ChicagoanHub","device":{"id":29671749,"label":"Yamaha AV Receiver","type":"StereoReceiver","transport":1,"suggestedDisplay":"DEFAULT","deviceTypeDisplayName":"StereoReceiver","capabilities":[1,5,8],"dongleRFID":0,"controlGroup":[{"name":"Power","function":[{"name":"PowerOff","label":"Power Off","action":"{\"command\":\"PowerOff\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"PowerOn","label":"Power On","action":"{\"command\":\"PowerOn\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"PowerToggle","label":"Power Toggle","action":"{\"command\":\"PowerToggle\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"NumericBasic","function":[{"name":"NumberEnter","label":"Number Enter","action":"{\"command\":\"NumberEnter\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number0","label":"0","action":"{\"command\":\"0\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number1","label":"1","action":"{\"command\":\"1\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number2","label":"2","action":"{\"command\":\"2\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number3","label":"3","action":"{\"command\":\"3\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number4","label":"4","action":"{\"command\":\"4\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number5","label":"5","action":"{\"command\":\"5\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number6","label":"6","action":"{\"command\":\"6\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number7","label":"7","action":"{\"command\":\"7\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number8","label":"8","action":"{\"command\":\"8\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Number9","label":"9","action":"{\"command\":\"9\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"Volume","function":[{"name":"Mute","label":"Mute","action":"{\"command\":\"Mute\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"VolumeDown","label":"Volume Down","action":"{\"command\":\"VolumeDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"VolumeUp","label":"Volume Up","action":"{\"command\":\"VolumeUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"NavigationBasic","function":[{"name":"DirectionDown","label":"Direction Down","action":"{\"command\":\"DirectionDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"DirectionLeft","label":"Direction Left","action":"{\"command\":\"DirectionLeft\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"DirectionRight","label":"Direction Right","action":"{\"command\":\"DirectionRight\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"DirectionUp","label":"Direction Up","action":"{\"command\":\"DirectionUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Select","label":"Select","action":"{\"command\":\"Enter\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"NavigationDVD","function":[{"name":"Return","label":"Return","action":"{\"command\":\"Return\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"RadioTuner","function":[{"name":"PrevPreset","label":"Prev Preset","action":"{\"command\":\"PresetPrev\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"ScanDown","label":"Scan Down","action":"{\"command\":\"TuneDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"ScanUp","label":"Scan Up","action":"{\"command\":\"TuneUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"NextPreset","label":"Next Preset","action":"{\"command\":\"PresetNext\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"Setup","function":[{"name":"Setup","label":"Setup","action":"{\"command\":\"Setup\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Sleep","label":"Sleep","action":"{\"command\":\"Sleep\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"NavigationExtended","function":[{"name":"Info","label":"Info","action":"{\"command\":\"Info\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"DisplayMode","function":[{"name":"Display","label":"Display","action":"{\"command\":\"Display\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"Miscellaneous","function":[{"name":"DSP-Direct","label":"DSP-Direct","action":"{\"command\":\"DSP-Direct\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"DSP-Movie","label":"DSP-Movie","action":"{\"command\":\"DSP-Movie\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"DSP-Music","label":"DSP-Music","action":"{\"command\":\"DSP-Music\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"DSP-Stereo/Enhancer","label":"DSP-Stereo/Enhancer","action":"{\"command\":\"DSP-Stereo\\/Enhancer\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"DSP-Straight","label":"DSP-Straight","action":"{\"command\":\"DSP-Straight\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"DSP-SurrDecode","label":"DSP-SurrDecode","action":"{\"command\":\"DSP-SurrDecode\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputAudio1","label":"InputAudio1","action":"{\"command\":\"InputAudio1\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputAudio2","label":"InputAudio2","action":"{\"command\":\"InputAudio2\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputAv1","label":"InputAv1","action":"{\"command\":\"InputAv1\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputAv2","label":"InputAv2","action":"{\"command\":\"InputAv2\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputAv3","label":"InputAv3","action":"{\"command\":\"InputAv3\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputAv4","label":"InputAv4","action":"{\"command\":\"InputAv4\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputAv5","label":"InputAv5","action":"{\"command\":\"InputAv5\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputAv6","label":"InputAv6","action":"{\"command\":\"InputAv6\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputDock","label":"InputDock","action":"{\"command\":\"InputDock\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputHdmi1","label":"InputHdmi1","action":"{\"command\":\"InputHdmi1\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputHdmi2","label":"InputHdmi2","action":"{\"command\":\"InputHdmi2\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputHdmi3","label":"InputHdmi3","action":"{\"command\":\"InputHdmi3\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputHdmi4","label":"InputHdmi4","action":"{\"command\":\"InputHdmi4\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputTuner","label":"InputTuner","action":"{\"command\":\"InputTuner\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"InputV-Aux","label":"InputV-Aux","action":"{\"command\":\"InputV-Aux\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodDisplay","label":"iPodDisplay","action":"{\"command\":\"iPodDisplay\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodDown","label":"iPodDown","action":"{\"command\":\"iPodDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodEnter","label":"iPodEnter","action":"{\"command\":\"iPodEnter\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodFastForward","label":"iPodFastForward","action":"{\"command\":\"iPodFastForward\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodLeft","label":"iPodLeft","action":"{\"command\":\"iPodLeft\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodNextTrack","label":"iPodNextTrack","action":"{\"command\":\"iPodNextTrack\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodPause","label":"iPodPause","action":"{\"command\":\"iPodPause\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodPlay","label":"iPodPlay","action":"{\"command\":\"iPodPlay\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodPreviousTrack","label":"iPodPreviousTrack","action":"{\"command\":\"iPodPreviousTrack\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodRewind","label":"iPodRewind","action":"{\"command\":\"iPodRewind\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodRight","label":"iPodRight","action":"{\"command\":\"iPodRight\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodStop","label":"iPodStop","action":"{\"command\":\"iPodStop\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"iPodUp","label":"iPodUp","action":"{\"command\":\"iPodUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Memory","label":"Memory","action":"{\"command\":\"Memory\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"Option","label":"Option","action":"{\"command\":\"Option\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"SceneBd/Dvd","label":"SceneBd/Dvd","action":"{\"command\":\"SceneBd\\/Dvd\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"SceneCd","label":"SceneCd","action":"{\"command\":\"SceneCd\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"SceneRadio","label":"SceneRadio","action":"{\"command\":\"SceneRadio\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"SceneTv","label":"SceneTv","action":"{\"command\":\"SceneTv\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"SelectAm","label":"SelectAm","action":"{\"command\":\"SelectAm\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"SelectFm","label":"SelectFm","action":"{\"command\":\"SelectFm\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]}],"controlPort":"7","keyboardAssociated":false,"model":"HTR-5063","deviceProfileUri":"svcs.myharmony.com/res/device/29671749-qVE4lSVuAqkVmhj1KOiyYIRjQrTaPAxfie7iIgbt3kQ\u003d","manufacturer":"Yamaha","icon":"5","manualPower":false}},{"hub":"ChicagoanHub","device":{"id":29695418,"label":"Toshiba DVD","type":"DVD","transport":1,"suggestedDisplay":"DEFAULT","deviceTypeDisplayName":"DVD","capabilities":[1,5,6,9,10],"dongleRFID":0,"controlGroup":[{"name":"Power","function":[{"name":"PowerToggle","label":"Power Toggle","action":"{\"command\":\"PowerToggle\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"NumericBasic","function":[{"name":"Number0","label":"0","action":"{\"command\":\"0\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number1","label":"1","action":"{\"command\":\"1\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number2","label":"2","action":"{\"command\":\"2\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number3","label":"3","action":"{\"command\":\"3\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number4","label":"4","action":"{\"command\":\"4\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number5","label":"5","action":"{\"command\":\"5\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number6","label":"6","action":"{\"command\":\"6\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number7","label":"7","action":"{\"command\":\"7\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number8","label":"8","action":"{\"command\":\"8\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Number9","label":"9","action":"{\"command\":\"9\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Clear","label":"Clear","action":"{\"command\":\"Clear\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"NavigationBasic","function":[{"name":"DirectionDown","label":"Direction Down","action":"{\"command\":\"DirectionDown\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"DirectionLeft","label":"Direction Left","action":"{\"command\":\"DirectionLeft\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"DirectionRight","label":"Direction Right","action":"{\"command\":\"DirectionRight\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"DirectionUp","label":"Direction Up","action":"{\"command\":\"DirectionUp\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Select","label":"Select","action":"{\"command\":\"Ok\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"TransportBasic","function":[{"name":"Stop","label":"Stop","action":"{\"command\":\"Stop\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Play","label":"Play","action":"{\"command\":\"Play\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Rewind","label":"Rewind","action":"{\"command\":\"Rewind\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Eject","label":"Eject","action":"{\"command\":\"Eject\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Pause","label":"Pause","action":"{\"command\":\"Pause\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"FastForward","label":"Fast Forward","action":"{\"command\":\"FastForward\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"TransportExtended","function":[{"name":"FrameAdvance","label":"Frame Advance","action":"{\"command\":\"Step\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"SkipBackward","label":"Skip Backward","action":"{\"command\":\"SkipBack\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"SkipForward","label":"Skip Forward","action":"{\"command\":\"SkipForward\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"NavigationDVD","function":[{"name":"Return","label":"Return","action":"{\"command\":\"Return\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"TopMenu","label":"Top Menu","action":"{\"command\":\"TopMenu\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Menu","label":"Menu","action":"{\"command\":\"Menu\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Subtitle","label":"Subtitle","action":"{\"command\":\"Subtitle\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Audio","label":"Audio","action":"{\"command\":\"Audio\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Angle","label":"Angle","action":"{\"command\":\"Angle\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"PlayMode","function":[{"name":"Repeat","label":"Repeat","action":"{\"command\":\"Repeat\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"RepeatAB","label":"Repeat A-B","action":"{\"command\":\"RepeatA-B\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"Program","function":[{"name":"Program","label":"Program","action":"{\"command\":\"Program\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Bookmark","label":"Bookmark","action":"{\"command\":\"Bookmark\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"PictureInPicture","function":[{"name":"PipToggle","label":"Pip Toggle","action":"{\"command\":\"PIP\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"ColoredButtons","function":[{"name":"Green","label":"Green","action":"{\"command\":\"Green\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Red","label":"Red","action":"{\"command\":\"Red\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Blue","label":"Blue","action":"{\"command\":\"Blue\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Yellow","label":"Yellow","action":"{\"command\":\"Yellow\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"DisplayMode","function":[{"name":"Display","label":"Display","action":"{\"command\":\"Display\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Zoom","label":"Zoom","action":"{\"command\":\"Zoom\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"GoogleTVNavigation","function":[{"name":"Netflix","label":"Netflix","action":"{\"command\":\"Netflix\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]},{"name":"Miscellaneous","function":[{"name":"Connected","label":"Connected","action":"{\"command\":\"Connected\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Digest","label":"Digest","action":"{\"command\":\"Digest\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Goto","label":"Goto","action":"{\"command\":\"Goto\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Home","label":"Home","action":"{\"command\":\"Home\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"OnScreen","label":"OnScreen","action":"{\"command\":\"OnScreen\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"PopUp","label":"PopUp","action":"{\"command\":\"PopUp\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"SecondaryAudio","label":"SecondaryAudio","action":"{\"command\":\"SecondaryAudio\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"},{"name":"Slow","label":"Slow","action":"{\"command\":\"Slow\",\"type\":\"IRCommand\",\"deviceId\":\"29695418\"}"}]}],"controlPort":"7","keyboardAssociated":false,"model":"bdk33","deviceProfileUri":"svcs.myharmony.com/res/device/29695418-D8Q8Amm9TDPWK5OxPaGiCPZ+iJKJJdHmqQL7MGcMHMY\u003d","manufacturer":"Toshiba","icon":"4","manualPower":false}}]
```
### Show Harmony Current Activity
Show the Harmony Hub's current activity.
```
GET http://host:port/api/devices/harmony/show
```
#### Response
Only listing the relevant fields that are needed for identity of an activity. The example below is representative of an activity.

Name |	Type |	Description
-----|-------|-------------
hub | string | The name of the given target hub.
activity:label | string | The name of the Harmony activity.
activity:id | integer | The id of the Harmony activity.
#### Harmony activity data example
```
[{"hub":"ChicagoanHub","activity":{"label":"Watch TV","suggestedDisplay":"Default","id":15823996,"activityTypeDisplayName":"Default","controlGroup":[{"name":"NumericBasic","function":[{"name":"NumberEnter","label":"Number Enter","action":"{\"command\":\"Enter\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Hyphen","label":"-","action":"{\"command\":\"-\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number0","label":"0","action":"{\"command\":\"0\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number1","label":"1","action":"{\"command\":\"1\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number2","label":"2","action":"{\"command\":\"2\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number3","label":"3","action":"{\"command\":\"3\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number4","label":"4","action":"{\"command\":\"4\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number5","label":"5","action":"{\"command\":\"5\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number6","label":"6","action":"{\"command\":\"6\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number7","label":"7","action":"{\"command\":\"7\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number8","label":"8","action":"{\"command\":\"8\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Number9","label":"9","action":"{\"command\":\"9\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"Volume","function":[{"name":"Mute","label":"Mute","action":"{\"command\":\"Mute\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"VolumeDown","label":"Volume Down","action":"{\"command\":\"VolumeDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"},{"name":"VolumeUp","label":"Volume Up","action":"{\"command\":\"VolumeUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671749\"}"}]},{"name":"Channel","function":[{"name":"PrevChannel","label":"Prev Channel","action":"{\"command\":\"Last\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"ChannelDown","label":"Channel Down","action":"{\"command\":\"ChannelDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"ChannelUp","label":"Channel Up","action":"{\"command\":\"ChannelUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"NavigationBasic","function":[{"name":"DirectionDown","label":"Direction Down","action":"{\"command\":\"DirectionDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"DirectionLeft","label":"Direction Left","action":"{\"command\":\"DirectionLeft\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"DirectionRight","label":"Direction Right","action":"{\"command\":\"DirectionRight\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"DirectionUp","label":"Direction Up","action":"{\"command\":\"DirectionUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Select","label":"Select","action":"{\"command\":\"Select\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"TransportBasic","function":[{"name":"Play","label":"Play","action":"{\"command\":\"Play\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Rewind","label":"Rewind","action":"{\"command\":\"Rewind\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Pause","label":"Pause","action":"{\"command\":\"Pause\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"FastForward","label":"Fast Forward","action":"{\"command\":\"FastForward\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"TransportRecording","function":[{"name":"Record","label":"Record","action":"{\"command\":\"Record\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"TransportExtended","function":[{"name":"FrameAdvance","label":"Frame Advance","action":"{\"command\":\"Slow\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"SkipBackward","label":"Skip Backward","action":"{\"command\":\"Replay\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"SkipForward","label":"Skip Forward","action":"{\"command\":\"Advance\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"NavigationDVD","function":[{"name":"Back","label":"Back","action":"{\"command\":\"Back\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"NavigationDSTB","function":[{"name":"C","label":"C","action":"{\"command\":\"C\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"B","label":"B","action":"{\"command\":\"B\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"D","label":"D","action":"{\"command\":\"D\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"A","label":"A","action":"{\"command\":\"A\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"List","label":"List","action":"{\"command\":\"List\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Live","label":"Live","action":"{\"command\":\"LiveTv\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"TiVo","function":[{"name":"ThumbsDown","label":"Thumbs Down","action":"{\"command\":\"ThumbsDown\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"ThumbsUp","label":"Thumbs Up","action":"{\"command\":\"ThumbsUp\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"TiVo","label":"TiVo","action":"{\"command\":\"TiVo\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"NavigationExtended","function":[{"name":"Guide","label":"Guide","action":"{\"command\":\"Guide\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Info","label":"Info","action":"{\"command\":\"Info\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Clear","label":"Backspace","action":"{\"command\":\"Clear\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]},{"name":"ColoredButtons","function":[{"name":"Green","label":"Green","action":"{\"command\":\"Green\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Red","label":"Red","action":"{\"command\":\"Red\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Blue","label":"Blue","action":"{\"command\":\"Blue\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"},{"name":"Yellow","label":"Yellow","action":"{\"command\":\"Yellow\",\"type\":\"IRCommand\",\"deviceId\":\"29671764\"}"}]}],"activityOrder":0,"tuningDefault":true,"fixit":{"29671749":{"id":"29671749","power":"ON","input":"HDMI 4","alwaysOn":false,"relativePower":true},"29695418":{"id":"29695418","power":"OFF","alwaysOn":false,"relativePower":true},"29671742":{"id":"29671742","power":"ON","input":"","alwaysOn":false,"relativePower":false},"29671764":{"id":"29671764","alwaysOn":false,"relativePower":false},"29695438":{"id":"29695438","power":"OFF","alwaysOn":false,"relativePower":true},"29695467":{"id":"29695467","alwaysOn":false,"relativePower":false},"29695485":{"id":"29695485","alwaysOn":false,"relativePower":false}},"type":"VirtualTelevisionN","icon":"userdata: 0x4454e0","baseImageUri":"https://rcbu-test-ssl-amr.s3.amazonaws.com/"},{"label":"PowerOff","suggestedDisplay":"Default","id":-1,"activityTypeDisplayName":"Default","controlGroup":[],"tuningDefault":false,"fixit":{"29671749":{"id":"29671749","power":"OFF","alwaysOn":false,"relativePower":true},"29695418":{"id":"29695418","power":"OFF","alwaysOn":false,"relativePower":true},"29671742":{"id":"29671742","power":"OFF","alwaysOn":false,"relativePower":false},"29671764":{"id":"29671764","alwaysOn":false,"relativePower":false},"29695438":{"id":"29695438","power":"OFF","alwaysOn":false,"relativePower":true},"29695467":{"id":"29695467","alwaysOn":false,"relativePower":false},"29695485":{"id":"29695485","alwaysOn":false,"relativePower":false}},"type":"PowerOff","icon":"Default"}}]
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
### Get Hue Items
Get the list of HUE device descriptors if the HUE pass thru devices are configured.
```
GET http://host:port/api/devices/hue/devices
```
#### Response
The example below is representative of some HUE device responses.


Name |	Type |	Description
-----|-------|-------------
device | HUE lights object | The HUE light detail descriptor, see API for lights response below.
huedeviceid | string | The id of the actual passthru HUE light id.
hueaddress | string | The address of the target HUE bridge.
huename | string | A name given to the target HUE bridge.
#### HUE passthru device data example
```
[{"device":{"state":{"on":true,"bri":254,"hue":4444,"sat":254,"effect":"none","ct":0,"alert":"none","colormode":"hs","reachable":true,"xy":[0.0,0.0]},"type":"Extended color light","name":"Hue Lamp 1","modelid":"LCT001","uniqueid":"00:17:88:01:00:d4:12:08-0a","swversion":"65003148"},"huedeviceid":"1","hueaddress":"192.168.0.118:8000","huename":"HueEmul"},{"device":{"state":{"on":true,"bri":254,"hue":23536,"sat":144,"effect":"none","ct":201,"alert":"none","colormode":"hs","reachable":true,"xy":[0.346,0.3568]},"type":"Extended color light","name":"Hue Lamp 2","modelid":"LCT001","uniqueid":"00:17:88:01:00:d4:12:08-0b","swversion":"65003148"},"huedeviceid":"2","hueaddress":"192.168.0.118:8000","huename":"HueEmul"},{"device":{"state":{"on":true,"bri":254,"hue":65136,"sat":254,"effect":"none","ct":201,"alert":"none","colormode":"hs","reachable":true,"xy":[0.346,0.3568]},"type":"Extended color light","name":"Hue Lamp 3","modelid":"LCT001","uniqueid":"00:17:88:01:00:d4:12:08-0c","swversion":"65003148"},"huedeviceid":"3","hueaddress":"192.168.0.118:8000","huename":"HueEmul"}]
```
## HUE REST API usage
This section will describe the REST API available for controlling the bridge based off of the HUE API. This Bridge does not support the full HUE API, only the calls that are supported with the HA Bridge are shown. The REST body examples are all formatted for easy reading, the actual body usage should be like this:
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
Allows the user to set the internal state of the light on and off, modify the brightness. This is not a HUE API call and is special to the bridge as it keeps track of the state changes to the light from the API. It is intended to allow you to sync the bridge state with your HA system state.
```
PUT	http://host:port/api/<username>/lights/<id>/bridgeupdatestate
```
#### Body arguments
These are examples that can be used in the control of items within the bridge, but for HUE passthru devices, the complete state object is sent.
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
The HA Bridge default UPNP listener is started on port 1900 on the upnp multicast address of 239.255.255.250. All ethernet interfaces that are active are bound to and the response port is set to the one given on the command line above or the default of 50000.

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

If this criteria is met, the following three responses are provided to the calling application:

```
HTTP/1.1 200 OK
HOST: 239.255.255.250:1900
CACHE-CONTROL: max-age=100
EXT:
LOCATION: http://192.168.1.1:80/description.xml
SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/1.15.0
hue-bridgeid: 001E06FFFE123456
ST: upnp:rootdevice
USN: uuid:2f402f80-da50-11e1-9b23-001e06123456::upnp:rootdevice
```
```
HTTP/1.1 200 OK
HOST: 239.255.255.250:1900
CACHE-CONTROL: max-age=100
EXT:
LOCATION: http://192.168.1.1:80/description.xml
SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/1.15.0
hue-bridgeid: 001E06FFFE123456
ST: uuid:2f402f80-da50-11e1-9b23-001e06123456
USN: uuid:2f402f80-da50-11e1-9b23-001e06123456
```
```
HTTP/1.1 200 OK
HOST: 239.255.255.250:1900
CACHE-CONTROL: max-age=100
EXT:
LOCATION: http://192.168.1.1:80/description.xml
SERVER: Linux/3.14.0 UPnP/1.0 IpBridge/1.15.0
hue-bridgeid: 001E06FFFE123456
ST: urn:schemas-upnp-org:device:basic:1
USN: uuid:2f402f80-da50-11e1-9b23-001e06123456
```

Note that `192.168.1.1` and `12345` are replaced with the actual IP address and last 6 digits of the MAC address, respectively.


### UPNP description service
The bridge provides the description service which is used by the calling app to interrogate access details after it has decided the upnp multicast response is the correct device.
#### Get Description
```
GET http://host:80/description.xml
```
#### Response
```
<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n
<root xmlns=\"urn:schemas-upnp-org:device-1-0\">\n
	<specVersion>\n
	<major>1</major>\n
	<minor>0</minor>\n
	</specVersion>\n
	<URLBase>http://192.168.1.1:80/</URLBase>\n
	<device>\n
		<deviceType>urn:schemas-upnp-org:device:Basic:1</deviceType>\n
		<friendlyName>Philips hue (192.168.1.1)</friendlyName>\n
		<manufacturer>Royal Philips Electronics</manufacturer>\n
		<manufacturerURL>http://www.philips.com</manufacturerURL>\n
		<modelDescription>Philips hue Personal Wireless Lighting</modelDescription>\n"
		<modelName>Philips hue bridge 2015</modelName>\n
		<modelNumber>BSB002</modelNumber>\n
		<modelURL>http://www.meethue.com</modelURL>\n
		<serialNumber>0017880ae670</serialNumber>\n
		<UDN>uuid:2f402f80-da50-11e1-9b23-001788102201</UDN>\n
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
## Development Mode
To turn on development mode so that it will not need a Harmony Hub for testing, use the following extra parameter in the command line and the harmony ip and login info will not be needed:
```
java -jar -Ddev.mode=true ha-bridge-0.X.Y.jar
```
