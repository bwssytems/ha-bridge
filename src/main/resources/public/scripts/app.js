var app = angular.module ('habridge', ['ngRoute', 'ngToast', 'rzModule', 'ngDialog', 'scrollable-table']);

app.config (function ($locationProvider, $routeProvider) {
    $locationProvider.hashPrefix('!');

    $routeProvider.when ('/', {
		templateUrl: 'views/configuration.html',
		controller: 'ViewingController'
	}).when ('/system', {
		templateUrl: 'views/system.html',
		controller: 'SystemController'		
	}).when ('/logs', {
		templateUrl: 'views/logs.html',
		controller: 'LogsController'		
	}).when ('/editdevice', {
		templateUrl: 'views/editdevice.html',
		controller: 'EditController'		
	}).when ('/veradevices', {
		templateUrl: 'views/veradevice.html',
		controller: 'VeraController'		
	}).when ('/verascenes', {
		templateUrl: 'views/verascene.html',
		controller: 'VeraController'		
	}).when ('/harmonydevices', {
		templateUrl: 'views/harmonydevice.html',
		controller: 'HarmonyController'		
	}).when ('/harmonyactivities', {
		templateUrl: 'views/harmonyactivity.html',
		controller: 'HarmonyController'		
	}).when ('/nest', {
		templateUrl: 'views/nestactions.html',
		controller: 'NestController'		
	}).when ('/huedevices', {
		templateUrl: 'views/huedevice.html',
		controller: 'HueController'		
	}).when ('/haldevices', {
		templateUrl: 'views/haldevice.html',
		controller: 'HalController'		
	}).when ('/mqttmessages', {
		templateUrl: 'views/mqttpublish.html',
		controller: 'MQTTController'		
	}).when ('/hassdevices', {
		templateUrl: 'views/hassdevice.html',
		controller: 'HassController'		
	}).otherwise ({
		templateUrl: 'views/configuration.html',
		controller: 'ViewingController'
	})
});

app.run( function (bridgeService) {
	bridgeService.loadBridgeSettings();
	bridgeService.getHABridgeVersion();
	bridgeService.viewMapTypes();
});

String.prototype.replaceAll = function (search, replace)
{
    //if replace is not sent, return original string otherwise it will
    //replace search string with 'undefined'.
    if (replace === undefined) {
        return this.toString();
    }

    return this.replace(new RegExp('[' + search + ']', 'g'), replace);
};


app.service ('bridgeService', function ($http, $window, ngToast) {
	var self = this;
	this.state = {base: window.location.origin + "/api/devices", bridgelocation: window.location.origin, systemsbase: window.location.origin + "/system", huebase: window.location.origin + "/api", configs: [], backups: [], devices: [], device: {}, mapandid: [], type: "", settings: [], myToastMsg: [], logMsgs: [], loggerInfo: [], mapTypes: [], olddevicename: "", logShowAll: false, isInControl: false, showVera: false, showHarmony: false, showNest: false, showHue: false, showHal: false, showMqtt: false, showHass: false, habridgeversion: ""};

	this.displayWarn = function(errorTitle, error) {
		var toastContent = errorTitle;
		if (error !== null && typeof(error) !== 'undefined') {
			if (error.data !== null)
			    toastContent = toastContent + " " + error.data.message + " with status: " + error.statusText + " - "  + error.status;
			else
				toastContent = error;
		}
		ngToast.create({
			className: "warning",
			dismissButton: true,
			dismissOnTimeout: false,
			content: toastContent});
	};
	
	this.displayError = function(errorTitle, error) {
		var toastContent = errorTitle;
		if (error !== null && typeof(error) !== 'undefined')
			toastContent = toastContent + " " + error.data.message + " with status: " + error.statusText + " - "  + error.status;
		ngToast.create({
			className: "danger",
			dismissButton: true,
			dismissOnTimeout: false,
			content: toastContent});
	};
	
	this.displayErrorMessage = function (errorTitle, errorMessage) {
		ngToast.create({
			className: "danger",
			dismissButton: true,
			dismissOnTimeout: false,
			content: errorTitle + errorMessage});
	};
	
	this.displaySuccess = function (theTitle) {
		ngToast.create({
			className: "success",
			content: theTitle});
	};
	
	this.viewDevices = function () {
		return $http.get(this.state.base).then(
				function (response) {
					self.state.devices = response.data;
				},
				function (error) {
					self.displayError("Cannot get devices from habridge: ", error);
				}
		);
	};

	this.renumberDevices = function () {
		return $http.post(this.state.base + "/exec/renumber").then(
				function (response) {
					self.viewDevices();
				},
				function (error) {
					self.displayError("Cannot renumber devices from habridge: ", error);
				}
		);
	};

	this.compareUniqueId = function(r1, r2) {
		 if (r1.id === r2.id)
		      return 0;
		 return parseInt(r1.id) > parseInt(r2.id) ? 1 : -1;
	};

	this.clearDevice = function () {
		self.state.device = {};
		self.state.olddevicename = "";
	};

	this.getHABridgeVersion = function () {
		return $http.get(this.state.systemsbase + "/habridge/version").then(
				function (response) {
					self.state.habridgeversion = response.data.version;
				},
				function (error) {
					self.displayWarn("Cannot get version: ", error);
				}
		);
	};

	this.aContainsB = function (a, b) {
		return a.indexOf(b) >= 0;
	}

	this.compareHarmonyNumber = function(r1, r2) {
		if (r1.device !== undefined) {
		 if (r1.device.id === r2.device.id)
		      return 0;
		 return r1.device.id > r2.device.id ? 1 : -1;
		}
		if (r1.activity !== undefined) {
			 if (r1.activity.id === r2.activity.id)
			      return 0;
			 return r1.activity.id > r2.activity.id ? 1 : -1;
		}
		return 0;
	};

	this.compareHarmonyLabel = function(r1, r2) {
		if (r1.device !== undefined) {
		 if (r1.device.label === r2.device.label)
		      return 0;
		 return r1.device.label > r2.device.label ? 1 : -1;
		}
		if (r1.activity !== undefined) {
			 if (r1.activity.label === r2.activity.label)
			      return 0;
			 return r1.activity.label > r2.activity.label ? 1 : -1;
		}
		return 0;
	};

	this.compareHarmonyHub = function(r1, r2) {
		if (r1.hub !== undefined) {
		 if (r1.hub === r2.hub)
		      return 0;
		 return r1.hub > r2.hub ? 1 : -1;
		}
		return 0;
	};

	this.updateShowVera = function () {
		this.state.showVera = self.state.settings.veraconfigured;
		return;
	}

	this.updateShowNest = function () {
		this.state.showNest = self.state.settings.nestconfigured;
		return;
	}

	this.updateShowHarmony = function () {
		this.state.showHarmony = self.state.settings.harmonyconfigured;
		return;
	}

	this.updateShowHue = function () {
		this.state.showHue = self.state.settings.hueconfigured;
		return;
	}

	this.updateShowHal = function () {
		this.state.showHal = self.state.settings.halconfigured;
		return;
	}

	this.updateShowMqtt = function () {
		this.state.showMqtt = self.state.settings.mqttconfigured;
		return;
	}

	this.updateShowHass = function () {
		this.state.showHass = self.state.settings.hassconfigured;
		return;
	}

	this.loadBridgeSettings = function () {
		return $http.get(this.state.systemsbase + "/settings").then(
				function (response) {
					self.state.settings = response.data;
					self.updateShowVera();
					self.updateShowHarmony();
					self.updateShowNest();
					self.updateShowHue();
					self.updateShowHal();
					self.updateShowMqtt();
					self.updateShowHass();
				},
				function (error) {
					self.displayWarn("Load Bridge Settings Error: ", error);
				}
		);
	};

	this.viewBackups = function () {
		return $http.get(this.state.base + "/backup/available").then(
				function (response) {
					self.state.backups = response.data;
				},
				function (error) {
					self.displayWarn("Get Backups Error: ", error);
				}
		);
	};

	this.viewConfigs = function () {
		return $http.get(this.state.systemsbase + "/backup/available").then(
				function (response) {
					self.state.configs = response.data;
				},
				function (error) {
					self.displayWarn("Get Configs Error: ", error);
				}
		);
	};

	this.viewLogs = function () {
		return $http.get(this.state.systemsbase + "/logmsgs").then(
				function (response) {
					self.state.logMsgs = response.data;
				},
				function (error) {
					self.displayWarn("Get log messages Error: ", error);
				}
		);
	};

	this.viewLoggerInfo = function () {
		return $http.get(this.state.systemsbase + "/logmgmt/loggers/" + self.state.logShowAll).then(
				function (response) {
					self.state.loggerInfo = response.data;
				},
				function (error) {
					self.displayWarn("Get logger info Error: ", error);
				}
		);
	};

	this.viewNestItems = function () {
		if(!this.state.showNest)
			return;
		return $http.get(this.state.base + "/nest/items").then(
				function (response) {
					self.state.nestitems = response.data;
				},
				function (error) {
					self.displayWarn("Get Nest Items Error: ", error);
				}
		);
	};

	this.viewHueDevices = function () {
		if(!this.state.showHue)
			return;
		return $http.get(this.state.base + "/hue/devices").then(
				function (response) {
					self.state.huedevices = response.data;
				},
				function (error) {
					self.displayWarn("Get Hue Items Error: ", error);
				}
		);
	};

	this.viewVeraDevices = function () {
		if(!this.state.showVera)
			return;
		return $http.get(this.state.base + "/vera/devices").then(
				function (response) {
					self.state.veradevices = response.data;
				},
				function (error) {
					self.displayWarn("Get Vera Devices Error: ", error);
				}
		);
	};

	this.viewVeraScenes = function () {
		if(!this.state.showVera)
			return;
		return $http.get(this.state.base + "/vera/scenes").then(
				function (response) {
					self.state.verascenes = response.data;
				},
				function (error) {
					self.displayWarn("Get Vera Scenes Error: ", error);
				}
		);
	};

	this.viewHarmonyActivities = function () {
		if (!this.state.showHarmony)
			return;
		return $http.get(this.state.base + "/harmony/activities").then(
				function (response) {
					self.state.harmonyactivities = response.data;
				},
				function (error) {
					self.displayWarn("Get Harmony Activities Error: ", error);
				}
		);
	};

	this.viewHarmonyDevices = function () {
		if (!this.state.showHarmony)
			return;
		return $http.get(this.state.base + "/harmony/devices").then(
				function (response) {
					self.state.harmonydevices = response.data;
				},
				function (error) {
					self.displayWarn("Get Harmony Devices Error: ", error);
				}
		);
	};

	this.viewHalDevices = function () {
		if (!this.state.showHal)
			return;
		return $http.get(this.state.base + "/hal/devices").then(
				function (response) {
					self.state.haldevices = response.data;
				},
				function (error) {
					self.displayWarn("Get Hal Devices Error: ", error);
				}
		);
	};

	this.viewMQTTDevices = function () {
		if (!this.state.showMqtt)
			return;
		return $http.get(this.state.base + "/mqtt/devices").then(
				function (response) {
					self.state.mqttbrokers = response.data;
				},
				function (error) {
					self.displayWarn("Get MQTT Devices Error: ", error);
				}
		);
	};

	this.viewHassDevices = function () {
		if (!this.state.showHass)
			return;
		return $http.get(this.state.base + "/hass/devices").then(
				function (response) {
					self.state.hassdevices = response.data;
				},
				function (error) {
					self.displayWarn("Get Hass Devices Error: ", error);
				}
		);
	};

	this.getCallObjects = function (deviceString) {
		if (!deviceString.startsWith("[")) {
			if (deviceString.startsWith("{\"item"))
				deviceString = "[" + deviceString + "]"
			else {
				if (deviceString.startsWith("{"))
					deviceString = "[{\"item\":" + deviceString + "}]"
				else
					deviceString = "[{\"item\":\"" + deviceString + "\"}]"
			}
		} else if (!deviceString.startsWith("[{\"item\""))
			deviceString = "[{\"item\":" + deviceString + "}]"
		var newDevices = angular.fromJson(deviceString)
		var i, s, len = newDevices.length
		for (i=0; i<len; ++i) {
				  if (i in newDevices) {
			    s = newDevices[i];
				if (s.type !== null)
					s.type = self.getMapType(s.type)
				if (angular.isObject(s.item))
					s.item = angular.toJson(s.item)
			  }
		}
		return newDevices
	}
	
	this.updateCallObjectsType = function (theDevices) {
		var i, s, type, len = theDevices.length
		for (i=0; i<len; ++i) {
			if (i in theDevices) {
			    s = theDevices[i];
				if (s.type !== undefined && s.type !== null) {
					type = self.getMapType(s.type[0])
					s.type = type[0]
				}
			}
		}
		return theDevices
		
	}
	
	this.viewMapTypes = function () {
		return $http.get(this.state.base + "/map/types").then(
				function (response) {
					self.state.mapTypes = response.data;
				},
				function (error) {
					self.displayWarn("Get mapTypes Error: ", error);
				}
		);
	};

	this.getMapType = function (aMapType) {
		var i, s, len = self.state.mapTypes.length;
		for (i=0; i<len; ++i) {
			  if (i in self.state.mapTypes) {
			    s = self.state.mapTypes[i];
			    if (aMapType === s[0])
			    	return self.state.mapTypes[i];
			  }
		}
		return null;
	}
	this.updateLogLevels = function (logComponents) {
		return $http.put(this.state.systemsbase + "/logmgmt/update", logComponents ).then(
				function (response) {
					self.state.loggerInfo = response.data;
					self.displaySuccess("Updated " + logComponents.length + " loggers for log levels.")
				},
				function (error) {
					self.displayWarn("Update Log components Error: ", error);
				}
		);
			
	};
	
	this.findDeviceByMapId = function (id, target, type) {
		for (var i = 0; i < this.state.devices.length; i++) {
			if (this.state.devices[i].mapId === id && this.state.devices[i].mapType === type && this.state.devices[i].targetDevice === target)
				return true;
		}
		return false;
	};

	this.findNestItemByMapId = function (id, type) {
		for (var i = 0; i < this.state.devices.length; i++) {
			if (this.state.devices[i].mapId === id && this.aContainsB(this.state.devices[i].mapType, type))
				return true;
		}
		return false;
	};

	this.bulkAddDevice = function (devices) {
		return $http.post(this.state.base, devices).then(
				function (response) {
					self.displaySuccess("Bulk device add successful.");
				},
				function (error) {
					self.displayWarn("Bulk Add new Device Error: ", error);
				}
		);
	};

	this.addDevice = function (aDevice) {
		var device = {};
		angular.extend(device, aDevice );
		if (device.deviceType === null || device.deviceType === "")
			device.deviceType = "custom";
		if (device.targetDevice === null || device.targetDevice === "")
			device.targetDevice = "Encapsulated";
		if (device.id) {
			var putUrl = this.state.base + "/" + device.id;
			return $http.put(putUrl, device).then(
					function (response) {
					},
					function (error) {
						self.displayWarn("Edit Device Error: ", error);
					}
			);
		} else {
			if (device.deviceType === null || device.deviceType === "")
				device.deviceType = "custom";
			return $http.post(this.state.base, device).then(
					function (response) {
					},
					function (error) {
						self.displayWarn("Add new Device Error: ", error);
					}
			);
		}
	};

	this.backupDeviceDb = function (afilename) {
		return $http.put(this.state.base + "/backup/create", {
			filename: afilename
		}).then (
				function (response) {
					self.viewBackups();
				},
				function (error) {
					self.displayWarn("Backup Device Db Error: ", error);
				}
		);
	};

	this.restoreBackup = function (afilename) {
		return $http.post(this.state.base + "/backup/restore", {
			filename: afilename
		}).then (
				function (response) {
					self.viewBackups();
					self.viewDevices();
				},
				function (error) {
					self.displayWarn("Backup Db Restore Error: ", error);
				}
		);
	};

	this.deleteBackup = function (afilename) {
		return $http.post(this.state.base + "/backup/delete", {
			filename: afilename
		}).then (
				function (response) {
					self.viewBackups();
				},
				function (error) {
					self.displayWarn("Delete Backup Db File Error:", error);
				}
		);
	};

	this.checkForBridge = function () {
		return $http.get(this.state.bridgelocation + "/description.xml").then(
				function (response) {
					ngToast.dismiss(self.state.myToastMsg);
					self.viewConfigs();
					self.state.myToastMsg = null;
					self.state.isInControl = false;
					window.location.reload();
				},
				function (error) {
					setTimeout(function(){
						self.checkForBridge();
					}, 2000);
				}
		);

	};

	this.stop = function() {
		self.state.isInControl = true;
		return $http.put(this.state.systemsbase + "/control/stop").then(
				function (response) {
					self.displayError("HABridge is now stopped. Restart must occur from the server.", null);
				},
				function (error) {
					self.displayError("HABRidge Stop Error: ", error);
				}
		);
	};

	this.reinit = function() {
		self.state.isInControl = true;
		return $http.put(this.state.systemsbase + "/control/reinit").then(
				function (response) {
					self.state.myToastMsg = ngToast.create({
						className: "warning",
						dismissButton: false,
						dismissOnTimeout: false,
						content:"HABridge is re-initializing, waiting for completion..."});
					setTimeout(function(){
						self.checkForBridge();
					}, 2000);
				},
				function (error) {
					self.displayWarn("HABRidge Reinit Error: ", error);
				}
		);
	};

	this.saveSettings = function () {
		return $http.put(this.state.systemsbase + "/settings", this.state.settings).then(
				function (response) {
					self.reinit();
				},
				function (error) {
					self.displayWarn("Save Settings Error: ", error);
				}
		);

	};

	this.backupSettings = function (afilename) {
		return $http.put(this.state.systemsbase + "/backup/create", {
			filename: afilename
		}).then (
				function (response) {
					self.viewConfigs();
				},
				function (error) {
					self.displayWarn("Backup Settings Error: ", error);
				}
		);
	};

	this.restoreSettings = function (afilename) {
		return $http.post(this.state.systemsbase + "/backup/restore", {
			filename: afilename
		}).then (
				function (response) {
					self.state.settings = response.data;
					self.viewConfigs();
					self.viewDevices();
				},
				function (error) {
					self.displayWarn("Backup Settings Restore Error: ", error);
				}
		);
	};

	this.deleteSettingsBackup = function (afilename) {
		return $http.post(this.state.systemsbase + "/backup/delete", {
			filename: afilename
		}).then (
				function (response) {
					self.viewConfigs();
				},
				function (error) {
					self.displayWarn("Delete Backup Settings File Error: ", error);
				}
		);
	};

	this.deleteDevice = function (id) {
		return $http.delete(this.state.base + "/" + id).then(
				function (response) {
					self.viewDevices();
				},
				function (error) {
					self.displayWarn("Delete Device Error: ", error);
				}
		);
	};

	this.editDevice = function (device) {
		self.state.device = device;
		self.state.olddevicename = device.name;
	};

	this.testUrl = function (device, type, value) {
		var msgDescription = "unknown";
		var testUrl = this.state.huebase + "/test/lights/" + device.id + "/state";
		var testBody = "{\"on\":";
		if (type === "off") {
			testBody = testBody + "false";
		} else {
			testBody = testBody + "true";
		}
		if (value) {
			testBody = testBody + ",\"bri\":" + value;
		}
		testBody = testBody + "}";
		$http.put(testUrl, testBody).then(
				function (response) {
					if (typeof(response.data[0].success) !== 'undefined') {
						msgDescription = "success " + angular.toJson(response.data[0].success);
					}
					if (typeof(response.data[0].error) !== 'undefined') {
						msgDescription = "error " + angular.toJson(response.data[0].error);
						self.displayErrorMessage("Request Error, Pleae look in your habridge log: ", msgDescription);
						return;
					}
						
					self.displaySuccess("Request Executed: " + msgDescription);
				},
				function (error) {
					self.displayWarn("Request Error, Pleae look in your habridge log: ", error);
				}
		);
		return;        		
	};

	this.buildUrls = function (onpayload, dimpayload, offpayload, isObject, anId, deviceName, deviceTarget, deviceType, deviceMapType, count, delay) {
		var currentOn = self.state.device.onUrl;
		var currentDim = self.state.device.dimUrl;
		var currentOff = self.state.device.offUrl;
		if (self.state.device.mapType !== undefined && self.state.device.mapType !== null && self.state.device.mapType !== "") {
			self.state.device.mapId = self.state.device.mapId + "-" + anId;
			if (dimpayload !== null) {
				if (currentDim.indexOf("{\"item") !== 1 ) {
					if (isObject)
						currentDim = "[{\"item\":" + currentDim + ",\"type\":\"" + deviceMapType + "\"}]";
					else
						currentDim = "[{\"item\":\"" + currentDim + "\",\"type\":\"" + deviceMapType + "\"}]";
				}

				self.state.device.dimUrl = currentDim.substr(0, currentDim.indexOf("]")) + ",{\"item\":";
			}

			if (currentOn.indexOf("{\"item") !== 1 ) {
				if (isObject)
					currentOn = "[{\"item\":" + currentOn + ",\"type\":\"" + deviceMapType + "\"}]";
				else
					currentOn = "[{\"item\":\"" + currentOn + "\",\"type\":\"" + deviceMapType + "\"}]";
			}
			self.state.device.onUrl = currentOn.substr(0, currentOn.indexOf("]")) + ",{\"item\":";

			if (currentOff.indexOf("{\"item") !== 1 ) {
				if (isObject)
					currentOff = "[{\"item\":" + currentOff + ",\"type\":\"" + deviceMapType + "\"}]";
				else
					currentOff = "[{\"item\":\"" + currentOff + "\",\"type\":\"" + deviceMapType + "\"}]";
			}
			self.state.device.offUrl = currentOff.substr(0, currentOff.indexOf("]")) + ",{\"item\":";		
		} else if (self.state.device.mapType === undefined || self.state.device.mapType === null || self.state.device.mapType === "") {
			this.clearDevice();
			self.state.device.deviceType = deviceType;
			self.state.device.name = deviceName;
			self.state.device.targetDevice = deviceTarget;
			self.state.device.mapType = deviceMapType;
			self.state.device.mapId = anId;
			if(dimpayload !== null)
				self.state.device.dimUrl = "[{\"item\":";
			self.state.device.onUrl = "[{\"item\":";
			self.state.device.offUrl = "[{\"item\":";
		}
		
		if (isObject) {
			if (dimpayload !== null)
				self.state.device.dimUrl = self.state.device.dimUrl + dimpayload;
			self.state.device.onUrl = self.state.device.onUrl + onpayload;
			self.state.device.offUrl = self.state.device.offUrl + offpayload;
			
		} else {
			if (dimpayload !== null)
				self.state.device.dimUrl = self.state.device.dimUrl + "\"" + dimpayload + "\"";
			self.state.device.onUrl = self.state.device.onUrl + "\"" + onpayload + "\"";
			self.state.device.offUrl = self.state.device.offUrl + "\"" + offpayload + "\"";
		}
		
		if (count !== undefined && count !== null && count !== "") {
			if (dimpayload !== null)
				self.state.device.dimUrl = self.state.device.dimUrl + ",\"count\":\"" + count;
			self.state.device.onUrl = self.state.device.onUrl + ",\"count\":\"" + count;
			self.state.device.offUrl = self.state.device.offUrl + ",\"count\":\"" + count;
		}
		if (delay !== undefined && delay !== null && delay !== "") {
			if (dimpayload !== null)
				self.state.device.dimUrl = self.state.device.dimUrl + ",\"delay\":\"" + delay;
			self.state.device.onUrl = self.state.device.onUrl + ",\"delay\":\"" + delay;
			self.state.device.offUrl = self.state.device.offUrl + ",\"delay\":\"" + delay;
		}
		if (dimpayload !== null)
			self.state.device.dimUrl = self.state.device.dimUrl + ",\"type\":\"" + deviceMapType + "\"}]";
		self.state.device.onUrl = self.state.device.onUrl + ",\"type\":\"" + deviceMapType + "\"}]";
		self.state.device.offUrl = self.state.device.offUrl + ",\"type\":\"" + deviceMapType + "\"}]";
	};
});

app.controller ('SystemController', function ($scope, $location, $http, $window, bridgeService) {
    bridgeService.viewConfigs();
    bridgeService.loadBridgeSettings();
    $scope.bridge = bridgeService.state;
    $scope.optionalbackupname = "";
    $scope.bridge.isInControl = false;
    $scope.visible = false;
    $scope.imgUrl = "glyphicon glyphicon-plus";
    $scope.addVeratoSettings = function (newveraname, newveraip) {
    	if($scope.bridge.settings.veraaddress === null) {
    		$scope.bridge.settings.veraaddress = { devices: [] };
		}
    	var newVera = {name: newveraname, ip: newveraip }
    	$scope.bridge.settings.veraaddress.devices.push(newVera);
    	$scope.newveraname = null;
    	$scope.newveraip = null;
    };
    $scope.removeVeratoSettings = function (veraname, veraip) {
    	for(var i = $scope.bridge.settings.veraaddress.devices.length - 1; i >= 0; i--) {
    	    if($scope.bridge.settings.veraaddress.devices[i].name === veraname && $scope.bridge.settings.veraaddress.devices[i].ip === veraip) {
    	    	$scope.bridge.settings.veraaddress.devices.splice(i, 1);
    	    }
    	}    	
    };
    $scope.addHarmonytoSettings = function (newharmonyname, newharmonyip) {
    	if($scope.bridge.settings.harmonyaddress === null) {
			$scope.bridge.settings.harmonyaddress = { devices: [] };
		}
    	var newharmony = {name: newharmonyname, ip: newharmonyip }
    	$scope.bridge.settings.harmonyaddress.devices.push(newharmony);
    	$scope.newharmonyname = null;
    	$scope.newharmonyip = null;
    };
    $scope.removeHarmonytoSettings = function (harmonyname, harmonyip) {
    	for(var i = $scope.bridge.settings.harmonyaddress.devices.length - 1; i >= 0; i--) {
    	    if($scope.bridge.settings.harmonyaddress.devices[i].name === harmonyname && $scope.bridge.settings.harmonyaddress.devices[i].ip === harmonyip) {
    	    	$scope.bridge.settings.harmonyaddress.devices.splice(i, 1);
    	    }
    	}    	
    };
    $scope.addHuetoSettings = function (newhuename, newhueip) {
    	if($scope.bridge.settings.hueaddress === null) {
			$scope.bridge.settings.hueaddress = { devices: [] };
		}
    	var newhue = {name: newhuename, ip: newhueip }
    	$scope.bridge.settings.hueaddress.devices.push(newhue);
    	$scope.newhuename = null;
    	$scope.newhueip = null;
    };
    $scope.removeHuetoSettings = function (huename, hueip) {
    	for(var i = $scope.bridge.settings.hueaddress.devices.length - 1; i >= 0; i--) {
    	    if($scope.bridge.settings.hueaddress.devices[i].name === huename && $scope.bridge.settings.hueaddress.devices[i].ip === hueip) {
    	    	$scope.bridge.settings.hueaddress.devices.splice(i, 1);
    	    }
    	}    	
    };
    $scope.addHaltoSettings = function (newhalname, newhalip) {
    	if($scope.bridge.settings.haladdress === null) {
			$scope.bridge.settings.haladdress = { devices: [] };
		}
    	var newhal = {name: newhalname, ip: newhalip }
    	$scope.bridge.settings.haladdress.devices.push(newhal);
    	$scope.newhalname = null;
    	$scope.newhalip = null;
    };
    $scope.removeHaltoSettings = function (halname, halip) {
    	for(var i = $scope.bridge.settings.haladdress.devices.length - 1; i >= 0; i--) {
    	    if($scope.bridge.settings.haladdress.devices[i].name === halname && $scope.bridge.settings.haladdress.devices[i].ip === halip) {
    	    	$scope.bridge.settings.haladdress.devices.splice(i, 1);
    	    }
    	}    	
    };
    $scope.addMQTTtoSettings = function (newmqttname, newmqttip, newmqttusername, newmqttpassword) {
    	if($scope.bridge.settings.mqttaddress === null) {
			$scope.bridge.settings.mqttaddress = { devices: [] };
		}
    	var newmqtt = {name: newmqttname, ip: newmqttip, username: newmqttusername, password: newmqttpassword }
    	$scope.bridge.settings.mqttaddress.devices.push(newmqtt);
    	$scope.newmqttname = null;
    	$scope.newmqttip = null;
    	$scope.newmqttusername = null;
    	$scope.newmqttpassword = null;
    };
    $scope.removeMQTTtoSettings = function (mqttname, mqttip) {
    	for(var i = $scope.bridge.settings.mqttaddress.devices.length - 1; i >= 0; i--) {
    	    if($scope.bridge.settings.mqttaddress.devices[i].name === mqttname && $scope.bridge.settings.mqttaddress.devices[i].ip === mqttip) {
    	    	$scope.bridge.settings.mqttaddress.devices.splice(i, 1);
    	    }
    	}    	
    };
    $scope.addHasstoSettings = function (newhassname, newhassip, newhassport, newhasspassword) {
    	if($scope.bridge.settings.hassaddress === null) {
			$scope.bridge.settings.hassaddress = { devices: [] };
		}
    	var newhass = {name: newhassname, ip: newhassip, port: newhassport, password: newhasspassword }
    	$scope.bridge.settings.hassaddress.devices.push(newhass);
    	$scope.newhassname = null;
    	$scope.newhassip = null;
    	$scope.newhassport = null;
    	$scope.newhasspassword = null;
    };
    $scope.removeHasstoSettings = function (hassname, hassip) {
    	for(var i = $scope.bridge.settings.hassaddress.devices.length - 1; i >= 0; i--) {
    	    if($scope.bridge.settings.hassaddress.devices[i].name === hassname && $scope.bridge.settings.hassaddress.devices[i].ip === hassip) {
    	    	$scope.bridge.settings.hassaddress.devices.splice(i, 1);
    	    }
    	}    	
    };
    $scope.bridgeReinit = function () {
    	bridgeService.reinit();
    };
    $scope.bridgeStop = function () {
    	bridgeService.stop();
    };
    $scope.saveSettings = function() {
    	bridgeService.saveSettings();
    };
    $scope.goBridgeUrl = function (url) {
    	window.open(url, "_blank");
    };
    $scope.backupSettings = function (optionalbackupname) {
        bridgeService.backupSettings(optionalbackupname);
    };
    $scope.restoreSettings = function (backupname) {
        bridgeService.restoreSettings(backupname);
    };
    $scope.deleteSettingsBackup = function (backupname) {
        bridgeService.deleteSettingsBackup(backupname);
    };
    $scope.toggle = function () {
        $scope.visible = !$scope.visible;
        if($scope.visible)
            $scope.imgUrl = "glyphicon glyphicon-minus";
        else
            $scope.imgUrl = "glyphicon glyphicon-plus";
    };
});

app.controller('LogsController', function ($scope, $location, $http, $window, bridgeService) {
    bridgeService.viewLogs();
    $scope.bridge = bridgeService.state;
    $scope.levels = ["INFO_INT", "WARN_INT", "DEBUG_INT", "TRACE_INT"];
	$scope.updateComponents = [];
	$scope.visible = false;
	$scope.imgUrl = "glyphicon glyphicon-plus";
	$scope.updateLogs = function() {
		bridgeService.viewLogs();
	};
	$scope.toggle = function () {
		$scope.visible = !$scope.visible;
		if($scope.visible) {
			$scope.imgUrl = "glyphicon glyphicon-minus";
		    bridgeService.viewLoggerInfo();
		}
		else
			$scope.imgUrl = "glyphicon glyphicon-plus";
	};
	$scope.addToUpdate = function (logInfo) {
		var idx = $scope.updateComponents.indexOf(logInfo);

		// is currently selected
		if (idx > -1) {
			$scope.updateComponents.splice(idx, 1);
		}

		// is newly selected
		else {
			$scope.updateComponents.push(logInfo);
		}
	};
	
	$scope.updateLoggers = function () {
		bridgeService.updateLogLevels($scope.updateComponents);
	};
	
	$scope.reloadLoggers = function () {
		bridgeService.viewLoggerInfo();
	};
});

app.controller('ViewingController', function ($scope, $location, $http, $window, bridgeService, ngDialog) {

	bridgeService.viewDevices();
	bridgeService.viewBackups();
	$scope.bridge = bridgeService.state;
	$scope.optionalbackupname = "";
	$scope.visible = false;
	$scope.imgUrl = "glyphicon glyphicon-plus";
	$scope.visibleBk = false;
	$scope.imgBkUrl = "glyphicon glyphicon-plus";
	$scope.comparatorUniqueId = bridgeService.compareUniqueId;
	$scope.testUrl = function (device, type) {
		var dialogNeeded = false;
		if ((type === "on" && device.onUrl !== undefined && bridgeService.aContainsB(device.onUrl, "${intensity")) ||
				(type === "off" && device.offUrl !== undefined && bridgeService.aContainsB(device.offUrl, "${intensity")) ||
				(type === "dim" && device.dimUrl !== undefined)) {
			$scope.bridge.device = device;
			$scope.bridge.type = type;
			ngDialog.open({
				template: 'valueDialog',
				controller: 'ValueDialogCtrl',
				className: 'ngdialog-theme-default'
			});
		}
		else
			bridgeService.testUrl(device, type);
	};
	$scope.deleteDevice = function (device) {
		$scope.bridge.device = device;
		ngDialog.open({
			template: 'deleteDialog',
			controller: 'DeleteDialogCtrl',
			className: 'ngdialog-theme-default'
		});
	};
	$scope.editDevice = function (device) {
		bridgeService.editDevice(device);
		$location.path('/editdevice');
	};
	$scope.renumberDevices = function() {
		bridgeService.renumberDevices();
	};
	$scope.backupDeviceDb = function (optionalbackupname) {
		bridgeService.backupDeviceDb(optionalbackupname);
	};
	$scope.restoreBackup = function (backupname) {
		bridgeService.restoreBackup(backupname);
	};
	$scope.deleteBackup = function (backupname) {
		bridgeService.deleteBackup(backupname);
	};
	$scope.toggle = function () {
		$scope.visible = !$scope.visible;
		if($scope.visible)
			$scope.imgUrl = "glyphicon glyphicon-minus";
		else
			$scope.imgUrl = "glyphicon glyphicon-plus";
	};
	$scope.toggleBk = function () {
		$scope.visibleBk = !$scope.visibleBk;
		if($scope.visibleBk)
			$scope.imgBkUrl = "glyphicon glyphicon-minus";
		else
			$scope.imgBkUrl = "glyphicon glyphicon-plus";
	};
});

app.controller('ValueDialogCtrl', function ($scope, bridgeService, ngDialog) {
	$scope.slider = {
		    value: 100,
		    options: {
		        floor: 0,
		        ceil: 100,
		        showSelectionBar: true
		    }
		};
	$scope.bridge = bridgeService.state;
	$scope.valueType = "percentage";
	$scope.changeScale = function () {
		if($scope.valueType === "raw") {
			$scope.slider.options.ceil = 255; 
			$scope.slider.value = 255;
		}
		else {
			$scope.slider.options.ceil = 100; 
			$scope.slider.value = 100;			
		}
	};
	$scope.setValue = function () {
		ngDialog.close('ngdialog1');
		var theValue = 0;
		if($scope.valueType === "percentage")
			theValue = Math.round(($scope.slider.value * .01) * 255);
		else
			theValue = $scope.slider.value;
		bridgeService.testUrl($scope.bridge.device, $scope.bridge.type, theValue);
		$scope.bridge.device = null;
		$scope.bridge.type = "";
	};
});

app.controller('DeleteDialogCtrl', function ($scope, bridgeService, ngDialog) {
	$scope.bridge = bridgeService.state;
	$scope.device = $scope.bridge.device;
	$scope.deleteDevice = function (device) {
		ngDialog.close('ngdialog1');
		bridgeService.deleteDevice(device.id);
		bridgeService.viewDevices();
		$scope.bridge.device = null;
		$scope.bridge.type = "";
	};
});

app.controller('VeraController', function ($scope, $location, $http, bridgeService, ngDialog) {
	$scope.bridge = bridgeService.state;
	$scope.device = bridgeService.state.device;
	$scope.device_dim_control = "";
	$scope.bulk = { devices: [] };
	$scope.selectAll = false;
	$scope.vera = {base: "http://", port: "3480", id: ""};
	bridgeService.viewVeraDevices();
	bridgeService.viewVeraScenes();
	$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	$scope.buttonsVisible = false;
	$scope.comparatorUniqueId = bridgeService.compareUniqueId;

	$scope.clearDevice = function () {
		bridgeService.clearDevice();
		$scope.device = bridgeService.state.device;
	};

	$scope.buildDeviceUrls = function (veradevice, dim_control) {
		if(dim_control.indexOf("byte") >= 0 || dim_control.indexOf("percent") >= 0 || dim_control.indexOf("math") >= 0) {
				dimpayload = "http://" + veradevice.veraaddress + ":" + $scope.vera.port
				+ "/data_request?id=action&output_format=json&DeviceNum="
				+ veradevice.id
				+ "&serviceId=urn:upnp-org:serviceId:Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget="
				+ dim_control;
			}
			else
				dimpayload = "http://" + veradevice.veraaddress + ":" + $scope.vera.port
				+ "/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum="
				+ veradevice.id;
			onpayload = "http://" + veradevice.veraaddress + ":" + $scope.vera.port
				+ "/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum="
				+ veradevice.id;
			offpayload = "http://" + veradevice.veraaddress + ":" + $scope.vera.port
			+ "/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum="
			+ veradevice.id;

			bridgeService.buildUrls(onpayload, dimpayload, offpayload, false, veradevice.id, veradevice.name, veradevice.veraname, "switch", "veraDevice", null, null);
			$scope.device = bridgeService.state.device;
	};

	$scope.buildSceneUrls = function (verascene) {
		onpayload = "http://" + verascene.veraaddress + ":" + $scope.vera.port
			+ "/data_request?id=action&output_format=json&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene&SceneNum="
			+ verascene.id;
		offpayload = "http://" + verascene.veraaddress + ":" + $scope.vera.port
			+ "/data_request?id=action&output_format=json&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene&SceneNum="
			+ verascene.id;

		bridgeService.buildUrls(onpayload, null, offpayload, false, verascene.id, verascene.name, verascene.veraname, "scene", "veraScene", null, null);
	};

	$scope.addDevice = function () {
		if($scope.device.name === "" && $scope.device.onUrl === "")
			return;
		bridgeService.addDevice($scope.device).then(
				function () {
					$scope.clearDevice();
					bridgeService.viewDevices();
					bridgeService.viewVeraDevices();
					bridgeService.viewVeraScenes();
				},
				function (error) {
					bridgeService.displayWarn("Error adding device: " + $scope.device.name, error)
				}
		);

	};

	$scope.bulkAddDevices = function(dim_control) {
		var devicesList = [];
		for(var i = 0; i < $scope.bulk.devices.length; i++) {
			for(var x = 0; x < bridgeService.state.veradevices.length; x++) {
				if(bridgeService.state.veradevices[x].id === $scope.bulk.devices[i]) {
					$scope.buildDeviceUrls(bridgeService.state.veradevices[x],dim_control);
					devicesList[i] = {
							name: $scope.device.name,
							mapId: $scope.device.mapId,
							mapType: $scope.device.mapType,
							deviceType: $scope.device.deviceType,
							targetDevice: $scope.device.targetDevice,
							onUrl: $scope.device.onUrl,
							dimUrl: $scope.device.dimUrl,
							offUrl: $scope.device.offUrl,
							headers: $scope.device.headers,
							httpVerb: $scope.device.httpVerb,
							contentType: $scope.device.contentType,
							contentBody: $scope.device.contentBody,
							contentBodyDim: $scope.device.contentBodyDim,
							contentBodyOff: $scope.device.contentBodyOff
					};
				}
			}
		}
		bridgeService.bulkAddDevice(devicesList).then(
				function () {
					$scope.clearDevice();
					bridgeService.viewDevices();
					bridgeService.viewVeraDevices();
					bridgeService.viewVeraScenes();
				},
				function (error) {
					bridgeService.displayWarn("Error adding Vera devices in bulk.", error)
				}
			);
		$scope.bulk = { devices: [] };
		$scope.selectAll = false;
	};

	$scope.toggleSelection = function toggleSelection(deviceId) {
		var idx = $scope.bulk.devices.indexOf(deviceId);

		// is currently selected
		if (idx > -1) {
			$scope.bulk.devices.splice(idx, 1);
			if($scope.bulk.devices.length === 0 && $scope.selectAll)
				$scope.selectAll = false;
		}

		// is newly selected
		else {
			$scope.bulk.devices.push(deviceId);
			$scope.selectAll = true;
		}
	};

	$scope.toggleSelectAll = function toggleSelectAll() {
		if($scope.selectAll) {
			$scope.selectAll = false;
			$scope.bulk = { devices: [] };
		}
		else {
			$scope.selectAll = true;
			for(var x = 0; x < bridgeService.state.veradevices.length; x++) {
				if($scope.bulk.devices.indexOf(bridgeService.state.veradevices[x]) < 0 && !bridgeService.findDeviceByMapId(bridgeService.state.veradevices[x].id, bridgeService.state.veradevices[x].veraname, "veraDevice"))
					$scope.bulk.devices.push(bridgeService.state.veradevices[x].id);
			}
		}
	};

	$scope.toggleButtons = function () {
		$scope.buttonsVisible = !$scope.buttonsVisible;
		if($scope.buttonsVisible)
			$scope.imgButtonsUrl = "glyphicon glyphicon-minus";
		else
			$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	};

	$scope.deleteDevice = function (device) {
		$scope.bridge.device = device;
		ngDialog.open({
			template: 'deleteDialog',
			controller: 'DeleteDialogCtrl',
			className: 'ngdialog-theme-default'
		});
	};
	
	$scope.editDevice = function (device) {
		bridgeService.editDevice(device);
		$location.path('/editdevice');
	};
});

app.controller('HarmonyController', function ($scope, $location, $http, bridgeService, ngDialog) {
	$scope.bridge = bridgeService.state;
	$scope.device = bridgeService.state.device;
	bridgeService.viewHarmonyActivities();
	bridgeService.viewHarmonyDevices();
	$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	$scope.buttonsVisible = false;
	$scope.comparatorNumber = bridgeService.compareHarmonyNumber;
	$scope.comparatorLabel = bridgeService.compareHarmonyLabel;
	$scope.comparatorHub = bridgeService.compareHarmonyHub;

	$scope.clearDevice = function () {
		bridgeService.clearDevice();
		$scope.device = bridgeService.state.device;
	};

	$scope.buildActivityUrls = function (harmonyactivity) {
		onpayload = "{\"name\":\"" + harmonyactivity.activity.id + "\",\"hub\":\"" + harmonyactivity.hub + "\"}";
		offpayload = "{\"name\":\"-1\",\"hub\":\"" + harmonyactivity.hub + "\"}";		

		bridgeService.buildUrls(onpayload, null, offpayload, true, harmonyactivity.activity.id,  harmonyactivity.activity.label, harmonyactivity.hub, "activity", "harmonyActivity", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildButtonUrls = function (harmonydevice, onbutton, offbutton) {
		var actionOn = angular.fromJson(onbutton);
		var actionOff = angular.fromJson(offbutton);
		onpayload = "{\"device\":\"" + harmonydevice.device.id + "\",\"button\":\"" + actionOn.command + "\",\"hub\":\"" + harmonydevice.hub + "\"}";
		offpayload = "{\"device\":\"" + harmonydevice.device.id + "\",\"button\":\"" + actionOff.command + "\",\"hub\":\"" + harmonydevice.hub + "\"}";

		bridgeService.buildUrls(onpayload, null, offpayload, true, actionOn.command,  harmonydevice.device.label, harmonydevice.hub, "button", "harmonyButton", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.addDevice = function () {
		if($scope.device.name === "" && $scope.device.onUrl === "")
			return;
		bridgeService.addDevice($scope.device).then(
				function () {
					$scope.clearDevice();
					bridgeService.viewDevices();
					bridgeService.viewHarmonyActivities();
					bridgeService.viewHarmonyDevices();
				},
				function (error) {
				}
		);

	};

	$scope.toggleButtons = function () {
		$scope.buttonsVisible = !$scope.buttonsVisible;
		if($scope.buttonsVisible)
			$scope.imgButtonsUrl = "glyphicon glyphicon-minus";
		else
			$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	};

	$scope.deleteDevice = function (device) {
		$scope.bridge.device = device;
		ngDialog.open({
			template: 'deleteDialog',
			controller: 'DeleteDialogCtrl',
			className: 'ngdialog-theme-default'
		});
	};
	
	$scope.editDevice = function (device) {
		bridgeService.editDevice(device);
		$location.path('/editdevice');
	};
});

app.controller('NestController', function ($scope, $location, $http, bridgeService, ngDialog) {
	$scope.bridge = bridgeService.state;
	$scope.device = bridgeService.state.device;
	bridgeService.viewNestItems();
	$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	$scope.buttonsVisible = false;

	$scope.clearDevice = function () {
		bridgeService.clearDevice();
		$scope.device = bridgeService.state.device;
	};

	$scope.buildNestHomeUrls = function (nestitem) {
		onpayload = "{\"name\":\"" + nestitem.id + "\",\"away\":false,\"control\":\"status\"}";
		offpayload = "{\"name\":\"" + nestitem.id + "\",\"away\":true,\"control\":\"status\"}";
		bridgeService.buildUrls(onpayload, null, offpayload, true, nestitem.id,  nestitem.name, nestitem.name, "home", "nestHomeAway", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildNestTempUrls = function (nestitem) {
		onpayload = "{\"name\":\"" + nestitem.id + "\",\"control\":\"temp\",\"temp\":\"${intensity.percent}\"}";
		dimpayload = "{\"name\":\"" + nestitem.id + "\",\"control\":\"temp\",\"temp\":\"${intensity.percent}\"}";
		offpayload = "{\"name\":\"" + nestitem.id + "\",\"control\":\"off\"}";
		bridgeService.buildUrls(onpayload, dimpayload, offpayload, true, nestitem.id + "-SetTemp",  nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Temperature", nestitem.location, "thermo", "nestThermoSet", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildNestHeatUrls = function (nestitem) {
		onpayload = "{\"name\":\"" + nestitem.id + "\",\"control\":\"heat\"}";
		dimpayload = "{\"name\":\"" + nestitem.id + "\",\"control\":\"temp\",\"temp\":\"${intensity.percent}\"}";
		offpayload = "{\"name\":\"" + nestitem.id + "\",\"control\":\"off\"}";
		bridgeService.buildUrls(onpayload, dimpayload, offpayload, true, nestitem.id + "-SetHeat",  nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Heat", nestitem.location, "thermo", "nestThermoSet", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildNestCoolUrls = function (nestitem) {
		onpayload = "{\"name\":\"" + nestitem.id + "\",\"control\":\"cool\"}";
		dimpayload = "{\"name\":\"" + nestitem.id + "\",\"control\":\"temp\",\"temp\":\"${intensity.percent}\"}";
		offpayload = "{\"name\":\"" + nestitem.id + "\",\"control\":\"off\"}";
		bridgeService.buildUrls(onpayload,dimpayload, offpayload, true, nestitem.id + "-SetCool",  nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Cool", nestitem.location, "thermo", "nestThermoSet", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildNestRangeUrls = function (nestitem) {
		onpayload = "{\"name\":\"" + nestitem.id + "\",\"control\":\"range\"}";
		offpayload = "{\"name\":\"" + nestitem.id + "\",\"control\":\"off\"}";
		bridgeService.buildUrls(onpayload, null, offpayload, true, nestitem.id + "-SetRange",  nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Range", nestitem.location, "thermo", "nestThermoSet", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildNestOffUrls = function (nestitem) {
		onpayload = "{\"name\":\"" + nestitem.id + "\",\"control\":\"range\"}";
		offpayload = "{\"name\":\"" + nestitem.id + "\",\"control\":\"off\"}";
		bridgeService.buildUrls(onpayload, null, offpayload, true, nestitem.id + "-TurnOff",  nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Thermostat", nestitem.location, "thermo", "nestThermoSet", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildNestFanUrls = function (nestitem) {
		onpayload = "{\"name\":\"" + nestitem.id + "\",\"control\":\"fan-on\"}";
		offpayload = "{\"name\":\"" + nestitem.id + "\",\"control\":\"fan-auto\"}";
		bridgeService.buildUrls(onpayload, null, offpayload, true, nestitem.id + "-SetFan",  nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Fan", nestitem.location, "thermo", "nestThermoSet", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.addDevice = function () {
		if($scope.device.name === "" && $scope.device.onUrl === "")
			return;
		bridgeService.addDevice($scope.device).then(
				function () {
					$scope.clearDevice();
					bridgeService.viewDevices();
					bridgeService.viewNestItems();
				},
				function (error) {
				}
		);

	};

	$scope.toggleButtons = function () {
		$scope.buttonsVisible = !$scope.buttonsVisible;
		if($scope.buttonsVisible)
			$scope.imgButtonsUrl = "glyphicon glyphicon-minus";
		else
			$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	};

	$scope.deleteDevice = function (device) {
		$scope.bridge.device = device;
		ngDialog.open({
			template: 'deleteDialog',
			controller: 'DeleteDialogCtrl',
			className: 'ngdialog-theme-default'
		});
	};
	
	$scope.editDevice = function (device) {
		bridgeService.editDevice(device);
		$location.path('/editdevice');
	};
});

app.controller('HueController', function ($scope, $location, $http, bridgeService, ngDialog) {
	$scope.bridge = bridgeService.state;
	$scope.device = bridgeService.state.device;
	$scope.bulk = { devices: [] };
	$scope.selectAll = false;
	bridgeService.viewHueDevices();
	$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	$scope.buttonsVisible = false;

	$scope.clearDevice = function () {
		bridgeService.clearDevice();
		$scope.device = bridgeService.state.device;
	};

	$scope.buildDeviceUrls = function (huedevice) {
		onpayload = "{\"ipAddress\":\"" + huedevice.hueaddress + "\",\"deviceId\":\"" + huedevice.huedeviceid +"\",\"hueName\":\"" + huedevice.huename + "\"}";
		offpayload = "{\"ipAddress\":\"" + huedevice.hueaddress + "\",\"deviceId\":\"" + huedevice.huedeviceid +"\",\"hueName\":\"" + huedevice.huename + "\"}";
		bridgeService.buildUrls(onpayload, null, offpayload, true, huedevice.device.uniqueid,  huedevice.device.name, huedevice.huename, "passthru",  "hueDevice", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.addDevice = function () {
		if($scope.device.name === "" && $scope.device.onUrl === "")
			return;
		bridgeService.addDevice($scope.device).then(
				function () {
					$scope.clearDevice();
					bridgeService.viewDevices();
					bridgeService.viewHueDevices();
				},
				function (error) {
					bridgeService.displayWarn("Error adding device: " + $scope.device.name, error)
				}
		);

	};

	$scope.bulkAddDevices = function() {
		var devicesList = [];
		for(var i = 0; i < $scope.bulk.devices.length; i++) {
			for(var x = 0; x < bridgeService.state.huedevices.length; x++) {
				if(bridgeService.state.huedevices[x].device.uniqueid === $scope.bulk.devices[i]) {
					$scope.buildDeviceUrls(bridgeService.state.huedevices[x]);
					devicesList[i] = {
							name: $scope.device.name,
							mapId: $scope.device.mapId,
							mapType: $scope.device.mapType,
							deviceType: $scope.device.deviceType,
							targetDevice: $scope.device.targetDevice,
							onUrl: $scope.device.onUrl,
							dimUrl: $scope.device.dimUrl,
							offUrl: $scope.device.offUrl,
							headers: $scope.device.headers,
							httpVerb: $scope.device.httpVerb,
							contentType: $scope.device.contentType,
							contentBody: $scope.device.contentBody,
							contentBodyDim: $scope.device.contentBodyDim,
							contentBodyOff: $scope.device.contentBodyOff
					};
				}
			}
		}
		bridgeService.bulkAddDevice(devicesList).then(
				function () {
					$scope.clearDevice();
					bridgeService.viewDevices();
					bridgeService.viewHueDevices();
				},
				function (error) {
					bridgeService.displayWarn("Error adding Hue devices in bulk.", error)
				}
			);

		$scope.bulk = { devices: [] };
		$scope.selectAll = false;
	};

	$scope.toggleSelection = function toggleSelection(deviceId) {
		var idx = $scope.bulk.devices.indexOf(deviceId);

		// is currently selected
		if (idx > -1) {
			$scope.bulk.devices.splice(idx, 1);
			if($scope.bulk.devices.length === 0 && $scope.selectAll)
				$scope.selectAll = false;
		}

		// is newly selected
		else {
			$scope.bulk.devices.push(deviceId);
			$scope.selectAll = true;
		}
	};

	$scope.toggleSelectAll = function toggleSelectAll() {
		if($scope.selectAll) {
			$scope.selectAll = false;
			$scope.bulk = { devices: [] };
		}
		else {
			$scope.selectAll = true;
			for(var x = 0; x < bridgeService.state.huedevices.length; x++) {
				if($scope.bulk.devices.indexOf(bridgeService.state.huedevices[x]) < 0 && !bridgeService.findDeviceByMapId(bridgeService.state.huedevices[x].device.uniqueid, bridgeService.state.huedevices[x].huename, "hueDevice"))
					$scope.bulk.devices.push(bridgeService.state.huedevices[x].device.uniqueid);
			}
		}
	};

	$scope.toggleButtons = function () {
		$scope.buttonsVisible = !$scope.buttonsVisible;
		if($scope.buttonsVisible)
			$scope.imgButtonsUrl = "glyphicon glyphicon-minus";
		else
			$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	};

	$scope.deleteDevice = function (device) {
		$scope.bridge.device = device;
		ngDialog.open({
			template: 'deleteDialog',
			controller: 'DeleteDialogCtrl',
			className: 'ngdialog-theme-default'
		});
	};
	
	$scope.editDevice = function (device) {
		bridgeService.editDevice(device);
		$location.path('/editdevice');
	};
});

app.controller('HalController', function ($scope, $location, $http, bridgeService, ngDialog) {
	$scope.bridge = bridgeService.state;
	$scope.device = bridgeService.state.device;
	$scope.device_dim_control = "";
	$scope.bulk = { devices: [] };
	$scope.selectAll = false;
	bridgeService.viewHalDevices();
	$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	$scope.buttonsVisible = false;

	$scope.clearDevice = function () {
		bridgeService.clearDevice();
		$scope.device = bridgeService.state.device;
	};

	$scope.buildDeviceUrls = function (haldevice, dim_control) {
		var preOnCmd = "";
		var preDimCmd = "";
		var preOffCmd = "";
		var nameCmd = "";
		var aDeviceType;
		var postCmd = "?Token=" + $scope.bridge.settings.haltoken;
		if(haldevice.haldevicetype === "Group") {
			aDeviceType = "group";
			preOnCmd = "/GroupService!GroupCmd=On";
			preOffCmd = "/GroupService!GroupCmd=Off";
			nameCmd = "!GroupName=";
		}
		else if(haldevice.haldevicetype === "Macro") {
			aDeviceType = "macro";
			preOnCmd = "/MacroService!MacroCmd=Set!MacroName=";
			preOffCmd = preOnCmd;
		}
		else if(haldevice.haldevicetype === "Scene") {
			aDeviceType = "scene";
			preOnCmd = "/SceneService!SceneCmd=Set!SceneName=";
			preOffCmd = preOnCmd;
		}
		else {
			aDeviceType = "switch";
			preOnCmd = "/DeviceService!DeviceCmd=SetDevice!DeviceValue=On";
			preDimCmd = "/DeviceService!DeviceCmd=SetDevice!DeviceValue=Dim!DevicePercent=";
			preOffCmd = "/DeviceService!DeviceCmd=SetDevice!DeviceValue=Off";
			nameCmd = "!DeviceName=";
		}
		if((dim_control.indexOf("byte") >= 0 || dim_control.indexOf("percent") >= 0 || dim_control.indexOf("math") >= 0) && aDeviceType === "switch")
			dimpayload = "http://" + haldevice.haladdress
			+ preDimCmd
			+ dim_control
			+ nameCmd
			+ haldevice.haldevicename.replaceAll(" ", "%20")
			+ postCmd;
		else
			dimpayload = "http://" + haldevice.haladdress
			+ preOnCmd
			+ nameCmd
			+ haldevice.haldevicename.replaceAll(" ", "%20")
			+ postCmd;
		onpayload = "http://" + haldevice.haladdress
		+ preOnCmd
		+ nameCmd
		+ haldevice.haldevicename.replaceAll(" ", "%20")
		+ postCmd;
		offpayload = "http://" + haldevice.haladdress 
		+ preOffCmd
		+ nameCmd
		+ haldevice.haldevicename.replaceAll(" ", "%20")
		+ postCmd;
		bridgeService.buildUrls(onpayload, dimpayload, offpayload, false, haldevice.haldevicename + "-" + haldevice.halname,  haldevice.haldevicename, haldevice.halname, aDeviceType,  "halDevice", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildButtonUrls = function (haldevice, onbutton, offbutton) {
		var actionOn = angular.fromJson(onbutton);
		var actionOff = angular.fromJson(offbutton);
		onpayload = "http://" + haldevice.haladdress + "/IrService!IrCmd=Set!IrDevice=" + haldevice.haldevicename.replaceAll(" ", "%20") + "!IrButton=" + actionOn.DeviceName.replaceAll(" ", "%20") + "?Token=" + $scope.bridge.settings.haltoken;
		offpayload = "http://" + haldevice.haladdress + "/IrService!IrCmd=Set!IrDevice=" + haldevice.haldevicename.replaceAll(" ", "%20") + "!IrButton=" + actionOff.DeviceName.replaceAll(" ", "%20") + "?Token=" + $scope.bridge.settings.haltoken;

		bridgeService.buildUrls(onpayload, null, offpayload, false, haldevice.haldevicename + "-" + haldevice.halname + "-" + actionOn.DeviceName,  haldevice.haldevicename, haldevice.halname, "button",  "halButton", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildHALHomeUrls = function (haldevice) {
		onpayload = "http://" + haldevice.haladdress + "/ModeService!ModeCmd=Set!ModeName=Home?Token=" + $scope.bridge.settings.haltoken;
		offpayload = "http://" + haldevice.haladdress	+ "/ModeService!ModeCmd=Set!ModeName=Away?Token=" + $scope.bridge.settings.haltoken;
		bridgeService.buildUrls(onpayload, null, offpayload, false, haldevice.haldevicename + "-" + haldevice.halname + "-HomeAway",  haldevice.haldevicename, haldevice.halname, "home",  "halHome", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildHALHeatUrls = function (haldevice) {
		onpayload = "http://" + haldevice.haladdress 
		+ "/HVACService!HVACCmd=Set!HVACName=" 
		+ haldevice.haldevicename.replaceAll(" ", "%20") 
		+ "!HVACMode=Heat?Token="
		+ $scope.bridge.settings.haltoken;
		dimpayload = "http://" + haldevice.haladdress 
		+ "/HVACService!HVACCmd=Set!HVACName=" 
		+ haldevice.haldevicename.replaceAll(" ", "%20") 
		+ "!HVACMode=Heat!HeatSpValue=${intensity.percent}?Token="
		+ $scope.bridge.settings.haltoken;
		offpayload = "http://" + haldevice.haladdress 
		+ "/HVACService!HVACCmd=Set!HVACName=" 
		+ haldevice.haldevicename.replaceAll(" ", "%20") 
		+ "!HVACMode=Off?Token="
		+ $scope.bridge.settings.haltoken;
		bridgeService.buildUrls(onpayload, dimpayload, offpayload, false, haldevice.haldevicename + "-" + haldevice.halname + "-SetHeat",  haldevice.haldevicename + " Heat", haldevice.halname, "thermo",  "halThermoSet", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildHALCoolUrls = function (haldevice) {
		onpayload = "http://" + haldevice.haladdress 
		+ "/HVACService!HVACCmd=Set!HVACName=" 
		+ haldevice.haldevicename.replaceAll(" ", "%20") 
		+ "!HVACMode=Cool?Token="
		+ $scope.bridge.settings.haltoken;
		dimpayload = "http://" + haldevice.haladdress 
		+ "/HVACService!HVACCmd=Set!HVACName=" 
		+ haldevice.haldevicename.replaceAll(" ", "%20") 
		+ "!HVACMode=Cool!CoolSpValue=${intensity.percent}?Token="
		+ $scope.bridge.settings.haltoken;
		offpayload = "http://" + haldevice.haladdress 
		+ "/HVACService!HVACCmd=Set!HVACName=" 
		+ haldevice.haldevicename.replaceAll(" ", "%20") 
		+ "!HVACMode=Off?Token="
		+ $scope.bridge.settings.haltoken;
		bridgeService.buildUrls(onpayload, dimpayload, offpayload, false, haldevice.haldevicename + "-" + haldevice.halname + "-SetCool",  haldevice.haldevicename + " Cool", haldevice.halname, "thermo",  "halThermoSet", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildHALAutoUrls = function (haldevice) {
		onpayload = "http://" + haldevice.haladdress 
		+ "/HVACService!HVACCmd=Set!HVACName=" 
		+ haldevice.haldevicename.replaceAll(" ", "%20") 
		+ "!HVACMode=Auto?Token="
		+ $scope.bridge.settings.haltoken;
		offpayload = "http://" + haldevice.haladdress 
		+ "/HVACService!HVACCmd=Set!HVACName=" 
		+ haldevice.haldevicename.replaceAll(" ", "%20") 
		+ "!HVACMode=Off?Token="
		bridgeService.buildUrls(onpayload, null, offpayload, false, haldevice.haldevicename + "-" + haldevice.halname + "-SetAuto",  haldevice.haldevicename + " Auto", haldevice.halname, "thermo",  "halThermoSet", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildHALOffUrls = function (haldevice) {
		onpayload = "http://" + haldevice.haladdress 
		+ "/HVACService!HVACCmd=Set!HVACName=" 
		+ haldevice.haldevicename.replaceAll(" ", "%20") 
		+ "!HVACMode=Auto?Token="
		+ $scope.bridge.settings.haltoken;
		offpayload = "http://" + haldevice.haladdress 
		+ "/HVACService!HVACCmd=Set!HVACName=" 
		+ haldevice.haldevicename.replaceAll(" ", "%20") 
		+ "!HVACMode=Off?Token="
		$scope.device.offUrl = "http://" + haldevice.haladdress 
		bridgeService.buildUrls(onpayload, null, offpayload, false, haldevice.haldevicename + "-" + haldevice.halname + "-TurnOff",  haldevice.haldevicename + " Thermostat", haldevice.halname, "thermo",  "halThermoSet", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildHALFanUrls = function (haldevice) {
		onpayload = "http://" + haldevice.haladdress 
			+ "/HVACService!HVACCmd=Set!HVACName=" 
			+ haldevice.haldevicename.replaceAll(" ", "%20") 
			+ "!FanMode=On?Token="
			+ $scope.bridge.settings.haltoken;
		offpayload = "http://" + haldevice.haladdress 
			+ "/HVACService!HVACCmd=Set!HVACName=" 
			+ haldevice.haldevicename.replaceAll(" ", "%20") 
			+ "!FanMode=Auto?Token="
			+ $scope.bridge.settings.haltoken;
		bridgeService.buildUrls(onpayload, null, offpayload, false, haldevice.haldevicename + "-" + haldevice.halname + "-SetFan",  haldevice.haldevicename + " Fan", haldevice.halname, "thermo",  "halThermoSet", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.addDevice = function () {
		if($scope.device.name === "" && $scope.device.onUrl === "")
			return;
		bridgeService.addDevice($scope.device).then(
				function () {
					$scope.clearDevice();
					bridgeService.viewDevices();
					bridgeService.viewHalDevices();
				},
				function (error) {
					bridgeService.displayWarn("Error adding device: " + $scope.device.name, error)
				}
		);

	};

	$scope.bulkAddDevices = function(dim_control) {
		var devicesList = [];
		for(var i = 0; i < $scope.bulk.devices.length; i++) {
			for(var x = 0; x < bridgeService.state.haldevices.length; x++) {
				if(bridgeService.state.haldevices[x].haldevicename === $scope.bulk.devices[i]) {
					if(bridgeService.state.haldevices[x].haldevicetype === "HVAC")
						$scope.buildHALAutoUrls(bridgeService.state.haldevices[x]);
					else if(bridgeService.state.haldevices[x].haldevicetype === "HOME")
						$scope.buildHALHomeUrls(bridgeService.state.haldevices[x]);
					else
						$scope.buildDeviceUrls(bridgeService.state.haldevices[x],dim_control);
					devicesList[i] = {
							name: $scope.device.name,
							mapId: $scope.device.mapId,
							mapType: $scope.device.mapType,
							deviceType: $scope.device.deviceType,
							targetDevice: $scope.device.targetDevice,
							onUrl: $scope.device.onUrl,
							dimUrl: $scope.device.dimUrl,
							offUrl: $scope.device.offUrl,
							headers: $scope.device.headers,
							httpVerb: $scope.device.httpVerb,
							contentType: $scope.device.contentType,
							contentBody: $scope.device.contentBody,
							contentBodyDim: $scope.device.contentBodyDim,
							contentBodyOff: $scope.device.contentBodyOff
					};
				}
			}
		}
		bridgeService.bulkAddDevice(devicesList).then(
				function () {
					$scope.clearDevice();
					bridgeService.viewDevices();
					bridgeService.viewHalDevices();
				},
				function (error) {
					bridgeService.displayWarn("Error adding HAL devices in bulk.", error)
				}
			);
		$scope.bulk = { devices: [] };
		$scope.selectAll = false;
	};

	$scope.toggleSelection = function toggleSelection(deviceId) {
		var idx = $scope.bulk.devices.indexOf(deviceId);

		// is currently selected
		if (idx > -1) {
			$scope.bulk.devices.splice(idx, 1);
			if($scope.bulk.devices.length === 0 && $scope.selectAll)
				$scope.selectAll = false;
		}

		// is newly selected
		else {
			$scope.bulk.devices.push(deviceId);
			$scope.selectAll = true;
		}
	};

	$scope.toggleSelectAll = function toggleSelectAll() {
		if($scope.selectAll) {
			$scope.selectAll = false;
			$scope.bulk = { devices: [] };
		}
		else {
			$scope.selectAll = true;
			for(var x = 0; x < bridgeService.state.haldevices.length; x++) {
				if($scope.bulk.devices.indexOf(bridgeService.state.haldevices[x]) < 0 && !bridgeService.findDeviceByMapId(bridgeService.state.haldevices[x].haldevicename + "-" +  bridgeService.state.haldevices[x].halname, bridgeService.state.haldevices[x].halname, "halDevice"))
					$scope.bulk.devices.push(bridgeService.state.haldevices[x].haldevicename);
			}
		}
	};

	$scope.toggleButtons = function () {
		$scope.buttonsVisible = !$scope.buttonsVisible;
		if($scope.buttonsVisible)
			$scope.imgButtonsUrl = "glyphicon glyphicon-minus";
		else
			$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	};

	$scope.deleteDevice = function (device) {
		$scope.bridge.device = device;
		ngDialog.open({
			template: 'deleteDialog',
			controller: 'DeleteDialogCtrl',
			className: 'ngdialog-theme-default'
		});
	};
	
	$scope.editDevice = function (device) {
		bridgeService.editDevice(device);
		$location.path('/editdevice');
	};
});

app.controller('MQTTController', function ($scope, $location, $http, bridgeService, ngDialog) {
	$scope.bridge = bridgeService.state;
	$scope.device = bridgeService.state.device;
	bridgeService.viewMQTTDevices();
	$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	$scope.buttonsVisible = false;

	$scope.clearDevice = function () {
		bridgeService.clearDevice();
		$scope.device = bridgeService.state.device;
	};

	$scope.buildMQTTPublish = function (mqttbroker, mqtttopic, mqttmessage) {
		onpayload = "{\"clientId\":\"" + mqttbroker.clientId + "\",\"topic\":\"" + mqtttopic + "\",\"message\":\"" + mqttmessage + "\"}";
		offpayload = "{\"clientId\":\"" + mqttbroker.clientId + "\",\"topic\":\"" + mqtttopic + "\",\"message\":\"" + mqttmessage + "\"}";

		bridgeService.buildUrls(onpayload, null, offpayload, true, mqttbroker.clientId + "-" + mqtttopic, mqttbroker.clientId + mqtttopic, mqttbroker.clientId, "mqtt",  "mqttMessage", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.addDevice = function () {
		if($scope.device.name === "" && $scope.device.onUrl === "")
			return;
		bridgeService.addDevice($scope.device).then(
				function () {
					$scope.clearDevice();
					bridgeService.viewDevices();
					bridgeService.viewMQTTDevices();
				},
				function (error) {
				}
		);

	};

	$scope.toggleButtons = function () {
		$scope.buttonsVisible = !$scope.buttonsVisible;
		if($scope.buttonsVisible)
			$scope.imgButtonsUrl = "glyphicon glyphicon-minus";
		else
			$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	};

	$scope.deleteDevice = function (device) {
		$scope.bridge.device = device;
		ngDialog.open({
			template: 'deleteDialog',
			controller: 'DeleteDialogCtrl',
			className: 'ngdialog-theme-default'
		});
	};
	
	$scope.editDevice = function (device) {
		bridgeService.editDevice(device);
		$location.path('/editdevice');
	};
});

app.controller('HassController', function ($scope, $location, $http, bridgeService, ngDialog) {
	$scope.bridge = bridgeService.state;
	$scope.device = bridgeService.state.device;
	$scope.device_dim_control = "";
	$scope.bulk = { devices: [] };
	$scope.selectAll = false;
	bridgeService.viewHassDevices();
	$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	$scope.buttonsVisible = false;

	$scope.clearDevice = function () {
		bridgeService.clearDevice();
		$scope.device = bridgeService.state.device;
	};

	$scope.buildDeviceUrls = function (hassdevice, dim_control) {
		onpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\"}";
		if((dim_control.indexOf("byte") >= 0 || dim_control.indexOf("percent") >= 0 || dim_control.indexOf("math") >= 0))
			dimpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\",\"bri\":\"" + dim_control + "\"}";
		else
			dimpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\"}";
		offpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"off\"}";

		bridgeService.buildUrls(onpayload, dimpayload, offpayload, true, hassdevice.hassname + "-" + hassdevice.deviceState.entity_id, hassdevice.deviceState.entity_id, hassdevice.hassname, hassdevice.domain,  "hassDevice", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildHassHeatUrls = function (hassdevice) {
		onpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\"}";
		if((dim_control.indexOf("byte") >= 0 || dim_control.indexOf("percent") >= 0 || dim_control.indexOf("math") >= 0))
			dimpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\",\"bri\":\"" + dim_control + "\"}";
		else
			dimpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\"}";
		offpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"off\"}";

		bridgeService.buildUrls(onpayload, dimpayload, offpayload, true, hassdevice.hassname + "-" + hassdevice.deviceState.entity_id, hassdevice.deviceState.entity_id, hassdevice.hassname, hassdevice.domain,  "hassDevice", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildHassCoolUrls = function (hassdevice) {
		onpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\"}";
		if((dim_control.indexOf("byte") >= 0 || dim_control.indexOf("percent") >= 0 || dim_control.indexOf("math") >= 0))
			dimpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\",\"bri\":\"" + dim_control + "\"}";
		else
			dimpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\"}";
		offpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"off\"}";

		bridgeService.buildUrls(onpayload, dimpayload, offpayload, true, hassdevice.hassname + "-" + hassdevice.deviceState.entity_id, hassdevice.deviceState.entity_id, hassdevice.hassname, hassdevice.domain,  "hassDevice", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildHassAutoUrls = function (hassdevice) {
		onpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\"}";
		if((dim_control.indexOf("byte") >= 0 || dim_control.indexOf("percent") >= 0 || dim_control.indexOf("math") >= 0))
			dimpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\",\"bri\":\"" + dim_control + "\"}";
		else
			dimpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\"}";
		offpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"off\"}";

		bridgeService.buildUrls(onpayload, dimpayload, offpayload, true, hassdevice.hassname + "-" + hassdevice.deviceState.entity_id, hassdevice.deviceState.entity_id, hassdevice.hassname, hassdevice.domain,  "hassDevice", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildHassOffUrls = function (hassdevice) {
		onpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\"}";
		if((dim_control.indexOf("byte") >= 0 || dim_control.indexOf("percent") >= 0 || dim_control.indexOf("math") >= 0))
			dimpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\",\"bri\":\"" + dim_control + "\"}";
		else
			dimpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\"}";
		offpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"off\"}";

		bridgeService.buildUrls(onpayload, dimpayload, offpayload, true, hassdevice.hassname + "-" + hassdevice.deviceState.entity_id, hassdevice.deviceState.entity_id, hassdevice.hassname, hassdevice.domain,  "hassDevice", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.buildHassFanUrls = function (hassdevice) {
		onpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\"}";
		if((dim_control.indexOf("byte") >= 0 || dim_control.indexOf("percent") >= 0 || dim_control.indexOf("math") >= 0))
			dimpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\",\"bri\":\"" + dim_control + "\"}";
		else
			dimpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"on\"}";
		offpayload = "{\"entityId\":\"" + hassdevice.deviceState.entity_id + "\",\"hassName\":\"" + hassdevice.hassname + "\",\"state\":\"off\"}";

		bridgeService.buildUrls(onpayload, dimpayload, offpayload, true, hassdevice.hassname + "-" + hassdevice.deviceState.entity_id, hassdevice.deviceState.entity_id, hassdevice.hassname, hassdevice.domain,  "hassDevice", null, null);
		$scope.device = bridgeService.state.device;
	};

	$scope.addDevice = function () {
		if($scope.device.name === "" && $scope.device.onUrl === "")
			return;
		bridgeService.addDevice($scope.device).then(
				function () {
					$scope.clearDevice();
					bridgeService.viewDevices();
					bridgeService.viewHassDevices();
				},
				function (error) {
					bridgeService.displayWarn("Error adding device: " + $scope.device.name, error)
				}
		);

	};

	$scope.bulkAddDevices = function(dim_control) {
		var devicesList = [];
		for(var i = 0; i < $scope.bulk.devices.length; i++) {
			for(var x = 0; x < bridgeService.state.hassdevices.length; x++) {
				if(bridgeService.state.hassdevices[x].deviceName === $scope.bulk.devices[i]) {
					if(bridgeService.state.hassdevices[x].domain === "climate")
						$scope.buildHassAutoUrls(bridgeService.state.hassdevices[x]);
					else
						$scope.buildDeviceUrls(bridgeService.state.hassdevices[x],dim_control);
					devicesList[i] = {
							name: $scope.device.name,
							mapId: $scope.device.mapId,
							mapType: $scope.device.mapType,
							deviceType: $scope.device.deviceType,
							targetDevice: $scope.device.targetDevice,
							onUrl: $scope.device.onUrl,
							dimUrl: $scope.device.dimUrl,
							offUrl: $scope.device.offUrl,
							headers: $scope.device.headers,
							httpVerb: $scope.device.httpVerb,
							contentType: $scope.device.contentType,
							contentBody: $scope.device.contentBody,
							contentBodyDim: $scope.device.contentBodyDim,
							contentBodyOff: $scope.device.contentBodyOff
					};
				}
			}
		}
		bridgeService.bulkAddDevice(devicesList).then(
				function () {
					$scope.clearDevice();
					bridgeService.viewDevices();
					bridgeService.viewhassdevices();
				},
				function (error) {
					bridgeService.displayWarn("Error adding Hass devices in bulk.", error)
				}
			);
		$scope.bulk = { devices: [] };
		$scope.selectAll = false;
	};

	$scope.toggleSelection = function toggleSelection(deviceId) {
		var idx = $scope.bulk.devices.indexOf(deviceId);

		// is currently selected
		if (idx > -1) {
			$scope.bulk.devices.splice(idx, 1);
			if($scope.bulk.devices.length === 0 && $scope.selectAll)
				$scope.selectAll = false;
		}

		// is newly selected
		else {
			$scope.bulk.devices.push(deviceId);
			$scope.selectAll = true;
		}
	};

	$scope.toggleSelectAll = function toggleSelectAll() {
		if($scope.selectAll) {
			$scope.selectAll = false;
			$scope.bulk = { devices: [] };
		}
		else {
			$scope.selectAll = true;
			for(var x = 0; x < bridgeService.state.hassdevices.length; x++) {
				if($scope.bulk.devices.indexOf(bridgeService.state.hassdevices[x]) < 0 && !bridgeService.findDeviceByMapId(bridgeService.state.hassdevices[x].hassdevicename + "-" +  bridgeService.state.hassdevices[x].halname, bridgeService.state.hassdevices[x].halname, "hassdevice"))
					$scope.bulk.devices.push(bridgeService.state.hassdevices[x].hassdevicename);
			}
		}
	};

	$scope.toggleButtons = function () {
		$scope.buttonsVisible = !$scope.buttonsVisible;
		if($scope.buttonsVisible)
			$scope.imgButtonsUrl = "glyphicon glyphicon-minus";
		else
			$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	};

	$scope.deleteDevice = function (device) {
		$scope.bridge.device = device;
		ngDialog.open({
			template: 'deleteDialog',
			controller: 'DeleteDialogCtrl',
			className: 'ngdialog-theme-default'
		});
	};
	
	$scope.editDevice = function (device) {
		bridgeService.editDevice(device);
		$location.path('/editdevice');
	};
});

app.controller('EditController', function ($scope, $location, $http, bridgeService) {
	bridgeService.viewMapTypes();
	$scope.bridge = bridgeService.state;
	$scope.device = bridgeService.state.device;
	$scope.onDevices = null;
	$scope.dimDevices = null;
	$scope.offDevices = null;
	if ($scope.device !== undefined && $scope.device.name !== undefined) {
		if($scope.bridge.device.onUrl !== undefined)
			$scope.onDevices = bridgeService.getCallObjects($scope.bridge.device.onUrl);
		if($scope.bridge.device.dimUrl !== undefined)
			$scope.dimDevices = bridgeService.getCallObjects($scope.bridge.device.dimUrl);
		if($scope.bridge.device.offUrl !== undefined)
			$scope.offDevices = bridgeService.getCallObjects($scope.bridge.device.offUrl);
	}
	
	$scope.newOnItem = {};
	$scope.newDimItem = {};
	$scope.newOffItem = {};
	$scope.mapTypeSelected = bridgeService.getMapType($scope.device.mapType); 
	$scope.device_dim_control = "";
	$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	$scope.buttonsVisible = false;

	$scope.clearDevice = function () {
		bridgeService.clearDevice();
		$scope.onDevices = null;
		$scope.dimDevices = null;
		$scope.offDevices = null;
		$scope.newOnItem = {};
		$scope.newDimItem = {};
		$scope.newOffItem = {};
		$scope.device = bridgeService.state.device;
	};

	$scope.editDevice = function (copy) {
		if($scope.device.name === "" && $scope.device.onUrl === "") {
			$scope.clearDevice();
			return;
		}
		if(($scope.device.name === $scope.bridge.olddevicename) && copy) {
			$scope.clearDevice();
			return;
		}
		if (copy)
			$scope.device.id = null;
		$scope.device.mapType = $scope.mapTypeSelected[0];
		if ($scope.onDevices !== null)
			$scope.device.onUrl = angular.toJson(bridgeService.updateCallObjectsType($scope.onDevices));
		if ($scope.dimDevices !== null)
			$scope.device.dimUrl = angular.toJson(bridgeService.updateCallObjectsType($scope.dimDevices));
		if ($scope.offDevices !== null)
			$scope.device.offUrl = angular.toJson(bridgeService.updateCallObjectsType($scope.offDevices));
		bridgeService.addDevice($scope.device).then(
				function () {
					$scope.clearDevice();
				},
				function (error) {
				}
		);

	};

    $scope.addItemOn = function (anItem) {
    	if (anItem.item === undefined || anItem.item === null || anItem.item === "")
    		return;
    	var newitem = { item: anItem.item, type: anItem.type, delay: anItem.delay, count: anItem.count, filterIPs: anItem.filterIPs, httpVerb: anItem.httpVerb, httpBody: anItem.httpBody, httpHeaders: anItem.httpHeaders, contentType: anItem.contentType };
    	if ($scope.onDevices === null)
    		$scope.onDevices = [];
    	$scope.onDevices.push(newitem);
    	$scope.newOnItem = [];
    };
    $scope.removeItemOn = function (anItem) {
    	for(var i = $scope.onDevices.length - 1; i >= 0; i--) {
    	    if($scope.onDevices[i].item === anItem.item && $scope.onDevices[i].type === anItem.type) {
    	    	$scope.onDevices.splice(i, 1);
    	    }
    	}    	
    };

    $scope.addItemDim = function (anItem) {
    	if (anItem.item === undefined || anItem.item === null || anItem.item === "")
    		return;
    	var newitem = { item: anItem.item, type: anItem.type, delay: anItem.delay, count: anItem.count, filterIPs: anItem.filterIPs, httpVerb: anItem.httpVerb, httpBody: anItem.httpBody, httpHeaders: anItem.httpHeaders, contentType: anItem.contentType };
    	if ($scope.dimDevices === null)
    		$scope.dimDevices = [];
    	$scope.dimDevices.push(newitem);
    	$scope.newDimItem = [];
    };
    $scope.removeItemDim = function (anItem) {
    	for(var i = $scope.dimDevices.length - 1; i >= 0; i--) {
    	    if($scope.dimDevices[i].item === anItem.item && $scope.dimDevices[i].type === anItem.type) {
    	    	$scope.dimDevices.splice(i, 1);
    	    }
    	}    	
    };

    $scope.addItemOff = function (anItem) {
    	if (anItem.item === undefined || anItem.item === null || anItem.item === "")
    		return;
    	var newitem = { item: anItem.item, type: anItem.type, delay: anItem.delay, count: anItem.count, filterIPs: anItem.filterIPs, httpVerb: anItem.httpVerb, httpBody: anItem.httpBody, httpHeaders: anItem.httpHeaders, contentType: anItem.contentType };
    	if ($scope.offDevices === null)
    		$scope.offDevices = [];
    	$scope.offDevices.push(newitem);
    	$scope.newOffItem = [];
    };
    $scope.removeItemOff = function (anItem) {
    	for(var i = $scope.offDevices.length - 1; i >= 0; i--) {
    	    if($scope.offDevices[i].item === anItem.item && $scope.offDevices[i].type === anItem.type) {
    	    	$scope.offDevices.splice(i, 1);
    	    }
    	}    	
    };
	$scope.toggleButtons = function () {
		$scope.buttonsVisible = !$scope.buttonsVisible;
		if($scope.buttonsVisible)
			$scope.imgButtonsUrl = "glyphicon glyphicon-minus";
		else
			$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	};

});

app.filter('configuredVeraDevices', function () {
	return function(input) {
		var out = [];
		if(input === undefined || input === null || input.length === undefined)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(input[i].mapType !== undefined && input[i].mapType !== null && input[i].mapType === "veraDevice"){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('configuredVeraScenes', function () {
	return function(input) {
		var out = [];
		if(input === undefined || input === null || input.length === undefined)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(input[i].mapType !== undefined && input[i].mapType !== null && input[i].mapType === "veraScene"){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('configuredNestItems', function (bridgeService) {
	return function(input) {
		var out = [];
		if(input === undefined || input === null || input.length === undefined)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(input[i].mapType !== undefined && input[i].mapType !== null && bridgeService.aContainsB(input[i].mapType, "nest")){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('configuredHueItems', function (bridgeService) {
	return function(input) {
		var out = [];
		if(input === undefined || input === null || input.length === undefined)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(input[i].mapType !== undefined && input[i].mapType !== null && bridgeService.aContainsB(input[i].mapType, "hue")){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('configuredHalItems', function (bridgeService) {
	return function(input) {
		var out = [];
		if(input === undefined || input === null || input.length === undefined)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(input[i].mapType !== undefined && input[i].mapType !== null && bridgeService.aContainsB(input[i].mapType, "hal")){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('configuredHarmonyActivities', function () {
	return function(input) {
		var out = [];
		if(input === undefined || input === null || input.length === undefined)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(input[i].mapType !== undefined && input[i].mapType !== null && input[i].mapType === "harmonyActivity"){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('configuredHarmonyButtons', function () {
	return function(input) {
		var out = [];
		if(input === undefined || input === null || input.length === undefined)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(input[i].mapType !== undefined && input[i].mapType !== null && input[i].mapType === "harmonyButtons"){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('configuredMqttMsgs', function () {
	return function(input) {
		var out = [];
		if(input === undefined || input === null || input.length === undefined)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(input[i].mapType !== undefined && input[i].mapType !== null && input[i].mapType === "mqttMessage"){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('configuredHassItems', function (bridgeService) {
	return function(input) {
		var out = [];
		if(input === undefined || input === null || input.length === undefined)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(input[i].mapType !== undefined && input[i].mapType !== null && bridgeService.aContainsB(input[i].mapType, "hass")){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.controller('VersionController', function ($scope, bridgeService) {
	$scope.bridge = bridgeService.state;
});