# Kodi volume control using dim commands & JSON-RPC

You can use HA Bridge to adjust the Kodi software volume output. This allows you to use dim commands and set the volume level with percentage values.
### What is JSON-RPC?

The short answer is JSON-RPC an interface to communicate with Kodi. [See the official Kodi Wiki for more info](http://kodi.wiki/view/JSON-RPC_API) 
### Setup Kodi to allow control through JSON-RPC

In Kodi navigate to Settings/Services/Control ([screenshot](http://kodi.wiki/view/Settings/Services/Control))

Turn **ON** the following:
- Allow control of Kodi via HTTP
- Allow remote control from applications on this system
- Allow remote control from applications on other systems

Change the **username** to something unique and set a strong **password**. 

Make a note of the **PORT**
### Adding the device to HA Bridge

Access the HA Bridge Configuration in your browser and open the  **Manual Add** tab.
#### Name

Give the device a unique name that doesn’t include **“volume”** as it will cause conflicts with the Echo’s built in volume controls. A device name of **“cody sound”** works well.
#### Device type

Select **TCP** in the dropdown
### URLs

This section might seem a little long winded and if you know what you are doing then feel free to jump ahead. 
#### Building the URL

We need to log into the Kodi web server without having to fill in the popup each time. You can do this by putting the username and password in the URL. It is not a good idea to do this on other websites as it does put your password in clear view, but for your local network it is fine. 

Use the example below replacing the relevant sections with the details that you defined in the Kodi settings screen in the first step. Replacing the IP with the address of the machine that Kodi is running on.

```
http://KODI_USERNAME:KODI_PASSWORD@192.168.1.123:8080/jsonrpc
```
##### Testing the URL in a browser

Before you continue, open your custom URL in a browser (making sure Kodi is running). If all is working as it should you will see a big page of JSON that starts with:

``` json
{
    "description": "JSON-RPC API of XBMC",
    "id": "http://xbmc.org/jsonrpc/ServiceDescription.json",
```

If you don’t see something that looks like the code above, then go back and double check your settings.
#### The JSON request

The URL is what connects you to Kodi, JSON is what is used to communicate with it. The JSON that is used to set the volume level is:

``` json
{"jsonrpc":"2.0","method":"Application.SetVolume","params":{"volume":100},"id":1}
```
##### Joining the URL and JSON

Join the two together by adding `?request=` to the end of the URL and then add the JSON to the end of the request. You will end up with something like:

```
http://KODI_USERNAME:KODI_PASSWORD@192.168.1.123:8080/jsonrpc?request={"jsonrpc":"2.0","method":"Application.SetVolume","params":{"volume":100},"id":1}
```
##### Testing the request in a browser

Go ahead and test the combined URL/JSON in a browser changing **100** to whatever level you want to set. Kodi should adjust the volume accordingly, try a few different levels to be sure it is working correctly. 

The browser will reformat the URL each time you press return so don’t build the URL in the browser bar without making a copy first. 

Ideally build the URL in a text document which you can easily edit and then copy/paste each time. 
#### Prepare the three URLs

You want to end up with three full URLs in your text file, one for each of the commands.

**ON**

```
http://KODI_USERNAME:KODI_PASSWORD@192.168.1.123:8080/jsonrpc?request={"jsonrpc":"2.0","method":"Application.SetVolume","params":{"volume":100},"id":1}
```

**DIM**

```
http://KODI_USERNAME:KODI_PASSWORD@192.168.1.123:8080/jsonrpc?request={"jsonrpc":"2.0","method":"Application.SetVolume","params":{"volume":45},"id":1}
```

**OFF**

```
http://KODI_USERNAME:KODI_PASSWORD@192.168.1.123:8080/jsonrpc?request={"jsonrpc":"2.0","method":"Application.SetVolume","params":{"volume":0},"id":1}
```
#### Encoding the JSON part

You should now be able to control the volume of Kodi using the structured URL you built above in a browser. If you can’t get it to work in a browser then you won’t be able to get it to work in HA Bridge.

In order for HA Bridge to send the JSON request you need to encode the URL. This means we take this:

```
http://KODI_USERNAME:KODI_PASSWORD@192.168.1.123:8080/jsonrpc?request={"jsonrpc":"2.0","method":"Application.SetVolume","params":{"volume":100},"id":1}
```

And turn it into this:

```
http://KODI_USERNAME:KODI_PASSWORD@192.168.1.123:8080/jsonrpc?request=%7B%22jsonrpc%22%3A%222.0%22%2C%22method%22%3A%22Application.SetVolume%22%2C%22params%22%3A%7B%22volume%22%3A100%7D%2C%22id%22%3A1%7D
```

Note that we are only encoding the JSON, the URL part we leave as is.

Using the online tool [www.url-encode-decode.com](http://www.url-encode-decode.com/), copy the JSON part (shown in bold below) and paste it into the left hand field.

http://KODI_USERNAME:KODI_PASSWORD@192.168.1.123:8080/jsonrpc?request=**{"jsonrpc":"2.0","method":"Application.SetVolume","params":{"volume":100},"id":1}**

Click the **Encode url** button.

Copy the output from the right hand box and paste it on a new line in your text file.

```
%7B%22jsonrpc%22%3A%222.0%22%2C%22method%22%3A%22Application.SetVolume%22%2C%22params%22%3A%7B%22volume%22%3A100%7D%2C%22id%22%3A1%7D
```
#### Join the URL and JSON

Finally you want to insert the URL part before the request (shown in bold below)

**http://KODI_USERNAME:KODI_PASSWORD@192.168.1.123:8080/jsonrpc?request=**%7B%22jsonrpc%22%3A%222.0%22%2C%22method%22%3A%22Application.SetVolume%22%2C%22params%22%3A%7B%22volume%22%3A100%7D%2C%22id%22%3A1%7D
### HA Bridge

Paste the final URL in **On URL** field. Just do the one for now, add the device and in the Bridge Control tab click Save.

Test the button in the Bridge Devices tab and hopefully it should turn the volume up in Kodi.

Go ahead and repeat the steps for the **Off URL**. All the same steps but change 100% to 0%. If you want to use the previous URL you can, just find the 100% in the encoded URL.

`…%22%3A%7B%22volume%22%3A`**100**`%7D%2C%22id%22%3A1%7D`

and change it to 0

`…%22%3A%7B%22volume%22%3A`**0**`%7D%2C%22id%22%3A1%7D`
#### Dim JSON

The Dim URL uses the `${intensity.percent}` to take the given number from your voice command and pass it to Kodi.

Here is the JSON to “Dim” the volume 

``` json
{"jsonrpc":"2.0","method":"Application.SetVolume","params":{"volume":"${intensity.percent}""},"id":1}
```

You don’t need to encode the `${intensity.percent}` part. This means you can simply replace the number value (100/0) with `${intensity.percent}` as shown below.

`…%22%3A%7B%22volume%22%3A`**${intensity.percent}**`%7D%2C%22id%22%3A1%7D`
### Controlling the Device

You can use the commands as listed in the [README](https://github.com/bwssytems/ha-bridge#ask-alexa)

“Set Cody Sound to 50 percent”
“Cody Sound to 70 percent”

Remembering that “Turn on Cody Sound” will set the volume to 100%, and “Turn off Cody Sound” will mute.
