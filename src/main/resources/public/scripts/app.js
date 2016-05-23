var app = angular.module('habridge', ['ngRoute','ngToast','rzModule','ngDialog','scrollable-table']);

app.config(function ($routeProvider) {
	$routeProvider.when('/#', {
		templateUrl: 'views/configuration.html',
		controller: 'ViewingController'
	}).when('/system', {
		templateUrl: 'views/system.html',
		controller: 'SystemController'		
	}).when('/logs', {
		templateUrl: 'views/logs.html',
		controller: 'LogsController'		
	}).when('/editor', {
		templateUrl: 'views/editor.html',
		controller: 'EditController'		
	}).when('/editdevice', {
		templateUrl: 'views/editdevice.html',
		controller: 'EditController'		
	}).when('/veradevices', {
		templateUrl: 'views/veradevice.html',
		controller: 'VeraController'		
	}).when('/verascenes', {
		templateUrl: 'views/verascene.html',
		controller: 'VeraController'		
	}).when('/harmonydevices', {
		templateUrl: 'views/harmonydevice.html',
		controller: 'HarmonyController'		
	}).when('/harmonyactivities', {
		templateUrl: 'views/harmonyactivity.html',
		controller: 'HarmonyController'		
	}).when('/nest', {
		templateUrl: 'views/nestactions.html',
		controller: 'NestController'		
	}).when('/huedevices', {
		templateUrl: 'views/huedevice.html',
		controller: 'HueController'		
	}).when('/haldevices', {
		templateUrl: 'views/haldevice.html',
		controller: 'HalController'		
	}).otherwise({
		templateUrl: 'views/configuration.html',
		controller: 'ViewingController'
	})
});

app.run( function (bridgeService) {
	bridgeService.loadBridgeSettings();
	bridgeService.getHABridgeVersion();	
});

String.prototype.replaceAll = function(search, replace)
{
    //if replace is not sent, return original string otherwise it will
    //replace search string with 'undefined'.
    if (replace === undefined) {
        return this.toString();
    }

    return this.replace(new RegExp('[' + search + ']', 'g'), replace);
};


app.service('bridgeService', function ($http, $window, ngToast) {
	var self = this;
	this.state = {base: window.location.origin + "/api/devices", bridgelocation: window.location.origin, systemsbase: window.location.origin + "/system", huebase: window.location.origin + "/api", configs: [], backups: [], devices: [], device: [], mapandid: [], type: "", settings: [], myToastMsg: [], logMsgs: [], loggerInfo: [], olddevicename: "", logShowAll: false, isInControl: false, showVera: false, showHarmony: false, showNest: false, showHue: false, showHal: false, habridgeversion: ""};

	this.displayWarn = function(errorTitle, error) {
		var toastContent = errorTitle;
		if(error != null && typeof(error) != 'undefined') {
			if(error.data != null)
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
		if(error != null && typeof(error) != 'undefined')
			toastContent = toastContent + " " + error.data.message + " with status: " + error.statusText + " - "  + error.status;
		ngToast.create({
			className: "danger",
			dismissButton: true,
			dismissOnTimeout: false,
			content: toastContent});
	};
	
	this.displayErrorMessage = function(errorTitle, errorMessage) {
		ngToast.create({
			className: "danger",
			dismissButton: true,
			dismissOnTimeout: false,
			content: errorTitle + errorMessage});
	};
	
	this.displaySuccess = function(theTitle) {
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

	this.clearDevice = function () {
		if(self.state.device == null)
			self.state.device = [];
		self.state.device.id = "";
		self.state.device.mapType = null;
		self.state.device.mapId = null;
		self.state.device.name = "";
		self.state.device.onUrl = "";
		self.state.device.dimUrl = "";
		self.state.device.deviceType = "custom";
		self.state.device.targetDevice = null;
		self.state.device.offUrl = "";
		self.state.device.headers = null;
		self.state.device.httpVerb = null;
		self.state.device.contentType = null;
		self.state.device.contentBody = null;
		self.state.device.contentBodyOff = null;
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

	this.loadBridgeSettings = function () {
		return $http.get(this.state.systemsbase + "/settings").then(
				function (response) {
					self.state.settings = response.data;
					self.updateShowVera();
					self.updateShowHarmony();
					self.updateShowNest();
					self.updateShowHue();
					self.updateShowHal();
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
		if(!this.state.showHarmony)
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
		if(!this.state.showHarmony)
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
		if(!this.state.showHal)
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

	this.updateLogLevels = function(logComponents) {
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
	
	this.findDeviceByMapId = function(id, target, type) {
		for(var i = 0; i < this.state.devices.length; i++) {
			if(this.state.devices[i].mapId == id && this.state.devices[i].mapType == type && this.state.devices[i].targetDevice == target)
				return true;
		}
		return false;
	};

	this.findNestItemByMapId = function(id, type) {
		for(var i = 0; i < this.state.devices.length; i++) {
			if(this.state.devices[i].mapId == id && this.aContainsB(this.state.devices[i].mapType, type))
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
		if(device.httpVerb != null && device.httpVerb != "")
			device.deviceType = "custom";
		if(device.targetDevice == null || device.targetDevice == "")
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
			if(device.deviceType == null || device.deviceType == "")
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
		}).then(
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
		}).then(
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
		}).then(
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
		}).then(
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
		}).then(
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
		}).then(
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

	this.deleteDeviceByMapId = function (id, type) {
		for(var i = 0; i < this.state.devices.length; i++) {
			if(this.state.devices[i].mapId == id && this.aContainsB(this.state.devices[i].mapType, type))
				return self.deleteDevice(this.state.devices[i].id);
		}
	};

	this.editDevice = function (device) {
		self.state.device = device;
		self.state.olddevicename = device.name;
	};

	this.testUrl = function (device, type, value) {
		var msgDescription = "unknown";
		var testUrl = this.state.huebase + "/test/lights/" + device.id + "/state";
		var testBody = "{\"on\":";
		if(type == "off") {
			testBody = testBody + "false";
		}
		else {
			testBody = testBody + "true";
		}
		if(value) {
			testBody = testBody + ",\"bri\":" + value;
		}
		testBody = testBody + "}";
		$http.put(testUrl, testBody).then(
				function (response) {
					if(typeof(response.data[0].success) != 'undefined') {
						msgDescription = "success " + angular.toJson(response.data[0].success);
					}
					if(typeof(response.data[0].error) != 'undefined') {
						msgDescription = "error " + angular.toJson(response.data[0].error);
						self.displayErrorMessage("Request Error, Pleae look in your habridge log: ", msgDescription);
						return;
					}
						
					self.displaySuccess("Request Exceuted: " + msgDescription);
				},
				function (error) {
					self.displayWarn("Request Error, Pleae look in your habridge log: ", error);
				}
		);
		return;        		
	};
});

app.controller('SystemController', function ($scope, $location, $http, $window, bridgeService) {
    bridgeService.viewConfigs();
    $scope.bridge = bridgeService.state;
    $scope.optionalbackupname = "";
    $scope.isInControl = false;
    $scope.visible = false;
    $scope.imgUrl = "glyphicon glyphicon-plus";
    $scope.addVeratoSettings = function (newveraname, newveraip) {
    	if($scope.bridge.settings.veraaddress == null) {
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
    	if($scope.bridge.settings.harmonyaddress == null) {
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
    	if($scope.bridge.settings.hueaddress == null) {
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
    	if($scope.bridge.settings.haladdress == null) {
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
    $scope.bridgeReinit = function () {
        $scope.isInControl = false;
    	bridgeService.reinit();
    };
    $scope.bridgeStop = function () {
        $scope.isInControl = false;
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
	$scope.testUrl = function (device, type) {
		var dialogNeeded = false;
		if((type == "on" && (bridgeService.aContainsB(device.onUrl, "${intensity.byte}") ||
				bridgeService.aContainsB(device.onUrl, "${intensity.percent}") ||
				bridgeService.aContainsB(device.onUrl, "${intensity.math(")) ||
				(type == "off" && (bridgeService.aContainsB(device.offUrl, "${intensity.byte}") ||
				bridgeService.aContainsB(device.offUrl, "${intensity.percent}") ||
				bridgeService.aContainsB(device.offUrl, "${intensity.math(")))   ||
				(type == "dim" && (bridgeService.aContainsB(device.dimUrl, "${intensity.byte}") ||
						bridgeService.aContainsB(device.dimUrl, "${intensity.percent}") ||
						bridgeService.aContainsB(device.dimUrl, "${intensity.math(") ||
						bridgeService.aContainsB(device.deviceType, "passthru") ||
						bridgeService.aContainsB(device.mapType, "hueDevice"))))) {
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
		if($scope.valueType == "raw") {
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
		if($scope.valueType == "percentage")
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

app.controller('DeleteMapandIdDialogCtrl', function ($scope, bridgeService, ngDialog) {
	$scope.bridge = bridgeService.state;
	$scope.mapandid = $scope.bridge.mapandid;
	$scope.deleteMapandId = function (mapandid) {
		ngDialog.close('ngdialog1');
		bridgeService.deleteDeviceByMapId(mapandid.id, mapandid.mapType);
		bridgeService.viewDevices();
		bridgeService.viewVeraDevices();
		bridgeService.viewVeraScenes();
		bridgeService.viewHarmonyActivities();
		bridgeService.viewHarmonyDevices();
		bridgeService.viewNestItems();
		bridgeService.viewHueDevices();
		$scope.bridge.mapandid = null;
	};
});

app.controller('VeraController', function ($scope, $location, $http, bridgeService, ngDialog) {
	$scope.bridge = bridgeService.state;
	$scope.device = $scope.bridge.device;
	$scope.device_dim_control = "";
	$scope.bulk = { devices: [] };
	var veraList = angular.fromJson($scope.bridge.settings.veraaddress);
	if(veraList != null)
		$scope.vera = {base: "http://" + veraList.devices[0].ip, port: "3480", id: ""};
	else
		$scope.vera = {base: "http://", port: "3480", id: ""};
	bridgeService.viewVeraDevices();
	bridgeService.viewVeraScenes();
	$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	$scope.buttonsVisible = false;

	$scope.clearDevice = function () {
		bridgeService.clearDevice();
	};

	$scope.buildDeviceUrls = function (veradevice, dim_control) {
		bridgeService.clearDevice();
		$scope.device.deviceType = "switch";
		$scope.device.name = veradevice.name;
		$scope.device.targetDevice = veradevice.veraname;
		$scope.device.mapType = "veraDevice";
		$scope.device.mapId = veradevice.id;
		if(dim_control.indexOf("byte") >= 0 || dim_control.indexOf("percent") >= 0 || dim_control.indexOf("math") >= 0)
			$scope.device.dimUrl = "http://" + veradevice.veraaddress + ":" + $scope.vera.port
			+ "/data_request?id=action&output_format=json&DeviceNum="
			+ veradevice.id
			+ "&serviceId=urn:upnp-org:serviceId:Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget="
			+ dim_control;
		else
			$scope.device.dimUrl = "http://" + veradevice.veraaddress + ":" + $scope.vera.port
			+ "/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum="
			+ veradevice.id;
		$scope.device.onUrl = "http://" + veradevice.veraaddress + ":" + $scope.vera.port
		+ "/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum="
		+ veradevice.id;
		$scope.device.offUrl = "http://" + veradevice.veraaddress + ":" + $scope.vera.port
		+ "/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum="
		+ veradevice.id;
	};

	$scope.buildSceneUrls = function (verascene) {
		bridgeService.clearDevice();
		$scope.device.deviceType = "scene";
		$scope.device.name = verascene.name;
		$scope.device.targetDevice = verascene.veraname;
		$scope.device.mapType = "veraScene";
		$scope.device.mapId = verascene.id;
		$scope.device.onUrl = "http://" + verascene.veraaddress + ":" + $scope.vera.port
		+ "/data_request?id=action&output_format=json&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene&SceneNum="
		+ verascene.id;
		$scope.device.offUrl = "http://" + verascene.veraaddress + ":" + $scope.vera.port
		+ "/data_request?id=action&output_format=json&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene&SceneNum="
		+ verascene.id;
	};

	$scope.addDevice = function () {
		if($scope.device.name == "" && $scope.device.onUrl == "")
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
				if(bridgeService.state.veradevices[x].id == $scope.bulk.devices[i]) {
					$scope.buildDeviceUrls(bridgeService.state.veradevices[x],dim_control);
					devicesList[i] = {
							name: $scope.device.name,
							mapId: $scope.device.mapId,
							mapType: $scope.device.mapType,
							deviceType: $scope.device.deviceType,
							targetDevice: $scope.device.targetDevice,
							onUrl: $scope.device.onUrl,
							offUrl: $scope.device.offUrl,
							headers: $scope.device.headers,
							httpVerb: $scope.device.httpVerb,
							contentType: $scope.device.contentType,
							contentBody: $scope.device.contentBody,
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
	};

	$scope.toggleSelection = function toggleSelection(deviceId) {
		var idx = $scope.bulk.devices.indexOf(deviceId);

		// is currently selected
		if (idx > -1) {
			$scope.bulk.devices.splice(idx, 1);
		}

		// is newly selected
		else {
			$scope.bulk.devices.push(deviceId);
		}
	};

	$scope.toggleButtons = function () {
		$scope.buttonsVisible = !$scope.buttonsVisible;
		if($scope.buttonsVisible)
			$scope.imgButtonsUrl = "glyphicon glyphicon-minus";
		else
			$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	};

	$scope.deleteDeviceByMapId = function (id, mapType) {
		$scope.bridge.mapandid = { id, mapType };
		ngDialog.open({
			template: 'deleteMapandIdDialog',
			controller: 'DeleteMapandIdDialogCtrl',
			className: 'ngdialog-theme-default'
		});
	};

});

app.controller('HarmonyController', function ($scope, $location, $http, bridgeService, ngDialog) {
	$scope.bridge = bridgeService.state;
	$scope.device = $scope.bridge.device;
	bridgeService.viewHarmonyActivities();
	bridgeService.viewHarmonyDevices();
	$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	$scope.buttonsVisible = false;

	$scope.clearDevice = function () {
		bridgeService.clearDevice();
	};

	$scope.buildActivityUrls = function (harmonyactivity) {
		bridgeService.clearDevice();
		$scope.device.deviceType = "activity";
		$scope.device.targetDevice = harmonyactivity.hub;
		$scope.device.name = harmonyactivity.activity.label;
		$scope.device.mapType = "harmonyActivity";
		$scope.device.mapId = harmonyactivity.activity.id;
		$scope.device.onUrl = "{\"name\":\"" + harmonyactivity.activity.id + "\"}";
		$scope.device.offUrl = "{\"name\":\"-1\"}";
	};

	$scope.buildButtonUrls = function (harmonydevice, onbutton, offbutton) {
		var currentOn = $scope.device.onUrl;
		var currentOff = $scope.device.offUrl;
		var actionOn = angular.fromJson(onbutton);
		var actionOff = angular.fromJson(offbutton);
		if( $scope.device.mapType == "harmonyButton") {
			$scope.device.mapId = $scope.device.mapId + "-" + actionOn.command;
			$scope.device.onUrl = currentOn.substr(0, currentOn.indexOf("]")) + ",{\"device\":\"" + harmonydevice.device.id + "\",\"button\":\"" + actionOn.command + "\"}]";
			$scope.device.offUrl = currentOff.substr(0, currentOff.indexOf("]")) + ",{\"device\":\"" + harmonydevice.device.id + "\",\"button\":\"" + actionOff.command + "\"}]";        		
		}
		else if ($scope.device.mapType == null || $scope.device.mapType == "") {
			bridgeService.clearDevice();
			$scope.device.deviceType = "button";
			$scope.device.targetDevice = harmonydevice.hub;
			$scope.device.name = harmonydevice.device.label;
			$scope.device.mapType = "harmonyButton";
			$scope.device.mapId = harmonydevice.device.id + "-" + actionOn.command;
			$scope.device.onUrl = "[{\"device\":\"" + harmonydevice.device.id + "\",\"button\":\"" + actionOn.command + "\"}]";
			$scope.device.offUrl = "[{\"device\":\"" + harmonydevice.device.id + "\",\"button\":\"" + actionOff.command + "\"}]";
		}
	};

	$scope.addDevice = function () {
		if($scope.device.name == "" && $scope.device.onUrl == "")
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

	$scope.deleteDeviceByMapId = function (id, mapType) {
		$scope.bridge.mapandid = { id, mapType };
		ngDialog.open({
			template: 'deleteMapandIdDialog',
			controller: 'DeleteMapandIdDialogCtrl',
			className: 'ngdialog-theme-default'
		});
	};

});

app.controller('NestController', function ($scope, $location, $http, bridgeService, ngDialog) {
	$scope.bridge = bridgeService.state;
	$scope.device = $scope.bridge.device;
	bridgeService.viewNestItems();
	$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	$scope.buttonsVisible = false;

	$scope.clearDevice = function () {
		bridgeService.clearDevice();
	};

	$scope.buildNestHomeUrls = function (nestitem) {
		bridgeService.clearDevice();
		$scope.device.deviceType = "home";
		$scope.device.name = nestitem.name;
		$scope.device.targetDevice = nestitem.name;
		$scope.device.mapType = "nestHomeAway";
		$scope.device.mapId = nestitem.id;
		$scope.device.onUrl = "{\"name\":\"" + nestitem.id + "\",\"away\":false,\"control\":\"status\"}";
		$scope.device.offUrl = "{\"name\":\"" + nestitem.id + "\",\"away\":true,\"control\":\"status\"}";
	};

	$scope.buildNestTempUrls = function (nestitem) {
		bridgeService.clearDevice();
		$scope.device.deviceType = "thermo";
		$scope.device.name = nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Temperature";
		$scope.device.targetDevice = nestitem.location;
		$scope.device.mapType = "nestThermoSet";
		$scope.device.mapId = nestitem.id + "-SetTemp";
		$scope.device.dimUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"temp\",\"temp\":\"${intensity.percent}\"}";
		$scope.device.offUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"off\"}";
	};

	$scope.buildNestHeatUrls = function (nestitem) {
		bridgeService.clearDevice();
		$scope.device.deviceType = "thermo";
		$scope.device.name = nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Heat";
		$scope.device.targetDevice = nestitem.location;
		$scope.device.mapType = "nestThermoSet";
		$scope.device.mapId = nestitem.id + "-SetHeat";
		$scope.device.onUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"heat\"}";
		$scope.device.offUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"off\"}";
	};

	$scope.buildNestCoolUrls = function (nestitem) {
		bridgeService.clearDevice();
		$scope.device.deviceType = "thermo";
		$scope.device.name = nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Cool";
		$scope.device.targetDevice = nestitem.location;
		$scope.device.mapType = "nestThermoSet";
		$scope.device.mapId = nestitem.id + "-SetCool";
		$scope.device.onUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"cool\"}";
		$scope.device.offUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"off\"}";
	};

	$scope.buildNestRangeUrls = function (nestitem) {
		bridgeService.clearDevice();
		$scope.device.deviceType = "thermo";
		$scope.device.name = nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Range";
		$scope.device.targetDevice = nestitem.location;
		$scope.device.mapType = "nestThermoSet";
		$scope.device.mapId = nestitem.id + "-SetRange";
		$scope.device.onUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"range\"}";
		$scope.device.offUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"off\"}";
	};

	$scope.buildNestOffUrls = function (nestitem) {
		bridgeService.clearDevice();
		$scope.device.deviceType = "thermo";
		$scope.device.name = nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Thermostat";
		$scope.device.targetDevice = nestitem.location;
		$scope.device.mapType = "nestThermoSet";
		$scope.device.mapId = nestitem.id + "-TurnOff";
		$scope.device.onUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"range\"}";
		$scope.device.offUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"off\"}";
	};

	$scope.buildNestFanUrls = function (nestitem) {
		bridgeService.clearDevice();
		$scope.device.deviceType = "thermo";
		$scope.device.name = nestitem.name.substr(0, nestitem.name.indexOf("(")) + " Fan";
		$scope.device.targetDevice = nestitem.location;
		$scope.device.mapType = "nestThermoSet";
		$scope.device.mapId = nestitem.id + "-SetFan";
		$scope.device.onUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"fan-on\"}";
		$scope.device.offUrl = "{\"name\":\"" + nestitem.id + "\",\"control\":\"fan-auto\"}";
	};

	$scope.addDevice = function () {
		if($scope.device.name == "" && $scope.device.onUrl == "")
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

	$scope.deleteDeviceByMapId = function (id, mapType) {
		$scope.bridge.mapandid = { id, mapType };
		ngDialog.open({
			template: 'deleteMapandIdDialog',
			controller: 'DeleteMapandIdDialogCtrl',
			className: 'ngdialog-theme-default'
		});
	};

});

app.controller('HueController', function ($scope, $location, $http, bridgeService, ngDialog) {
	$scope.bridge = bridgeService.state;
	$scope.device = $scope.bridge.device;
	$scope.bulk = { devices: [] };
	bridgeService.viewHueDevices();
	$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	$scope.buttonsVisible = false;

	$scope.clearDevice = function () {
		bridgeService.clearDevice();
	};

	$scope.buildDeviceUrls = function (huedevice) {
		bridgeService.clearDevice();
		if($scope.device == null)
			$scope.device = $scope.bridge.device;
		$scope.device.deviceType = "passthru";
		$scope.device.name = huedevice.device.name;
		$scope.device.targetDevice = huedevice.huename;
		$scope.device.contentType = "application/json";
		$scope.device.mapType = "hueDevice";
		$scope.device.mapId = huedevice.device.uniqueid;
		$scope.device.onUrl = "{\"ipAddress\":\"" + huedevice.hueaddress + "\",\"deviceId\":\"" + huedevice.huedeviceid +"\"}";
	};

	$scope.addDevice = function () {
		if($scope.device.name == "" && $scope.device.onUrl == "")
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
				if(bridgeService.state.huedevices[x].device.uniqueid == $scope.bulk.devices[i]) {
					$scope.buildDeviceUrls(bridgeService.state.huedevices[x]);
					devicesList[i] = {
							name: $scope.device.name,
							mapId: $scope.device.mapId,
							mapType: $scope.device.mapType,
							deviceType: $scope.device.deviceType,
							targetDevice: $scope.device.targetDevice,
							onUrl: $scope.device.onUrl,
							offUrl: $scope.device.offUrl,
							headers: $scope.device.headers,
							httpVerb: $scope.device.httpVerb,
							contentType: $scope.device.contentType,
							contentBody: $scope.device.contentBody,
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
	};

	$scope.toggleSelection = function toggleSelection(deviceId) {
		var idx = $scope.bulk.devices.indexOf(deviceId);

		// is currently selected
		if (idx > -1) {
			$scope.bulk.devices.splice(idx, 1);
		}

		// is newly selected
		else {
			$scope.bulk.devices.push(deviceId);
		}
	};

	$scope.toggleButtons = function () {
		$scope.buttonsVisible = !$scope.buttonsVisible;
		if($scope.buttonsVisible)
			$scope.imgButtonsUrl = "glyphicon glyphicon-minus";
		else
			$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	};

	$scope.deleteDeviceByMapId = function (id, mapType) {
		$scope.bridge.mapandid = { id, mapType };
		ngDialog.open({
			template: 'deleteMapandIdDialog',
			controller: 'DeleteMapandIdDialogCtrl',
			className: 'ngdialog-theme-default'
		});
	};

});

app.controller('HalController', function ($scope, $location, $http, bridgeService, ngDialog) {
	$scope.bridge = bridgeService.state;
	$scope.device = $scope.bridge.device;
	$scope.device_dim_control = "";
	$scope.bulk = { devices: [] };
	bridgeService.viewHalDevices();
	$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	$scope.buttonsVisible = false;

	$scope.clearDevice = function () {
		bridgeService.clearDevice();
	};

	$scope.buildDeviceUrls = function (haldevice, dim_control) {
		bridgeService.clearDevice();
		$scope.device = $scope.bridge.device;
		$scope.device.deviceType = "switch";
		$scope.device.name = haldevice.haldevicename;
		$scope.device.targetDevice = haldevice.halname;
		$scope.device.mapType = "halDevice";
		$scope.device.mapId = haldevice.haldevicename + "-" + haldevice.halname;
		if(dim_control.indexOf("byte") >= 0 || dim_control.indexOf("percent") >= 0 || dim_control.indexOf("math") >= 0)
			$scope.device.dimUrl = "http://" + haldevice.haladdress
			+ "/DeviceService!DeviceName="
			+ haldevice.haldevicename.replaceAll(" ", "%20")
			+ "!DeviceCmd=SetDevice!DeviceValue=Dim!DevicePercent="
			+ dim_control
			+ "?Token="
			+ $scope.bridge.settings.haltoken;
		else
			$scope.device.dimUrl = "http://" + haldevice.haladdress
			+ "/DeviceService!DeviceName="
			+ haldevice.haldevicename.replaceAll(" ", "%20")
			+ "!DeviceCmd=SetDevice!DeviceValue=On?Token="
			+ $scope.bridge.settings.haltoken;
		$scope.device.onUrl = "http://" + haldevice.haladdress
		+ "/DeviceService!DeviceName="
		+ haldevice.haldevicename.replaceAll(" ", "%20")
		+ "!DeviceCmd=SetDevice!DeviceValue=On?Token="
		+ $scope.bridge.settings.haltoken;
		$scope.device.offUrl = "http://" + haldevice.haladdress 
		+ "/DeviceService!DeviceName="
		+ haldevice.haldevicename.replaceAll(" ", "%20")
		+ "!DeviceCmd=SetDevice!DeviceValue=Off?Token="
		+ $scope.bridge.settings.haltoken;
		
		
	};

	$scope.addDevice = function () {
		if($scope.device.name == "" && $scope.device.onUrl == "")
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
				if(bridgeService.state.haldevices[x].haldevicename == $scope.bulk.devices[i]) {
					$scope.buildDeviceUrls(bridgeService.state.haldevices[x],dim_control);
					devicesList[i] = {
							name: $scope.device.name,
							mapId: $scope.device.mapId,
							mapType: $scope.device.mapType,
							deviceType: $scope.device.deviceType,
							targetDevice: $scope.device.targetDevice,
							onUrl: $scope.device.onUrl,
							offUrl: $scope.device.offUrl,
							headers: $scope.device.headers,
							httpVerb: $scope.device.httpVerb,
							contentType: $scope.device.contentType,
							contentBody: $scope.device.contentBody,
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
	};

	$scope.toggleSelection = function toggleSelection(deviceId) {
		var idx = $scope.bulk.devices.indexOf(deviceId);

		// is currently selected
		if (idx > -1) {
			$scope.bulk.devices.splice(idx, 1);
		}

		// is newly selected
		else {
			$scope.bulk.devices.push(deviceId);
		}
	};

	$scope.toggleButtons = function () {
		$scope.buttonsVisible = !$scope.buttonsVisible;
		if($scope.buttonsVisible)
			$scope.imgButtonsUrl = "glyphicon glyphicon-minus";
		else
			$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	};

	$scope.deleteDeviceByMapId = function (haldevicename, halname, mapType) {
		var id = haldevicename + "-" + halname;
		$scope.bridge.mapandid = { id, mapType };
		ngDialog.open({
			template: 'deleteMapandIdDialog',
			controller: 'DeleteMapandIdDialogCtrl',
			className: 'ngdialog-theme-default'
		});
	};

});

app.controller('EditController', function ($scope, $location, $http, bridgeService) {
	$scope.bridge = bridgeService.state;
	$scope.device = $scope.bridge.device;
	$scope.device_dim_control = "";
	$scope.bulk = { devices: [] };
	var veraList = angular.fromJson($scope.bridge.settings.veraaddress);
	if(veraList != null && veraList.devices.length > 0)
		$scope.vera = {base: "http://" + veraList.devices[0].ip, port: "3480", id: ""};
	else
		$scope.vera = {base: "http://", port: "3480", id: ""};
	$scope.imgButtonsUrl = "glyphicon glyphicon-plus";
	$scope.buttonsVisible = false;

	$scope.clearDevice = function () {
		bridgeService.clearDevice();
	};

	$scope.buildUrlsUsingDevice = function (dim_control) {
		bridgeService.clearDevice();
		$scope.device.deviceType = "switch";
		$scope.device.targetDevice = "Encapsulated";
		$scope.device.mapType = "veraDevice";
		$scope.device.mapId = $scope.vera.id;
		if(dim_control.indexOf("byte") >= 0 || dim_control.indexOf("percent") >= 0 || dim_control.indexOf("math") >= 0)
			$scope.device.dimUrl = $scope.vera.base + ":" + $scope.vera.port
			+ "/data_request?id=action&output_format=json&DeviceNum="
			+ $scope.vera.id
			+ "&serviceId=urn:upnp-org:serviceId:Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget="
			+ dim_control;
		else
			$scope.device.dimUrl = $scope.vera.base + ":" + $scope.vera.port
			+ "/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum="
			+ $scope.vera.id;
		$scope.device.onUrl = $scope.vera.base + ":" + $scope.vera.port
		+ "/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1&DeviceNum="
		+ $scope.vera.id;
		$scope.device.offUrl = $scope.vera.base + ":" + $scope.vera.port
		+ "/data_request?id=action&output_format=json&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0&DeviceNum="
		+ $scope.vera.id;
	};

	$scope.buildUrlsUsingScene = function () {
		bridgeService.clearDevice();
		$scope.device.deviceType = "scene";
		$scope.device.targetDevice = "Encapsulated";
		$scope.device.mapType = "veraScene";
		$scope.device.mapId = $scope.vera.id;
		$scope.device.onUrl = $scope.vera.base + ":" + $scope.vera.port
		+ "/data_request?id=action&output_format=json&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene&SceneNum="
		+ $scope.vera.id;
		$scope.device.offUrl = $scope.vera.base + ":" + $scope.vera.port
		+ "/data_request?id=action&output_format=json&serviceId=urn:micasaverde-com:serviceId:HomeAutomationGateway1&action=RunScene&SceneNum="
		+ $scope.vera.id;
	};

	$scope.addDevice = function () {
		if($scope.device.name == "" && $scope.device.onUrl == "")
			return;
		bridgeService.addDevice($scope.device).then(
				function () {
					$scope.clearDevice();
				},
				function (error) {
				}
		);

	};

	$scope.copyDevice = function () {
		if($scope.device.name == "" && $scope.device.onUrl == "") {
			$scope.clearDevice();
			return;
		}

		if($scope.device.name == $scope.bridge.olddevicename) {
			$scope.clearDevice();
			return;
		}
		$scope.device.id = null;
		bridgeService.addDevice($scope.device).then(
				function () {
					$scope.clearDevice();
				},
				function (error) {
				}
		);

	};

});

app.filter('availableHarmonyActivityId', function(bridgeService) {
	return function(input) {
		var out = [];
		if(input == null)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(!bridgeService.findDeviceByMapId(input[i].activity.id, input[i].hub, "harmonyActivity")){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('unavailableHarmonyActivityId', function(bridgeService) {
	return function(input) {
		var out = [];
		if(input == null)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(bridgeService.findDeviceByMapId(input[i].activity.id, input[i].hub, "harmonyActivity")){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('availableVeraDeviceId', function(bridgeService) {
	return function(input) {
		var out = [];
		if(input == null)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(!bridgeService.findDeviceByMapId(input[i].id, input[i].veraname, "veraDevice")){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('unavailableVeraDeviceId', function(bridgeService) {
	return function(input) {
		var out = [];
		if(input == null)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(bridgeService.findDeviceByMapId(input[i].id, input[i].veraname, "veraDevice")){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('availableVeraSceneId', function(bridgeService) {
	return function(input) {
		var out = [];
		if(input == null)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(!bridgeService.findDeviceByMapId(input[i].id, input[i].veraname, "veraScene")){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('unavailableVeraSceneId', function(bridgeService) {
	return function(input) {
		var out = [];
		if(input == null)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(bridgeService.findDeviceByMapId(input[i].id,input[i].veraname, "veraScene")){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('availableNestItemId', function(bridgeService) {
	return function(input) {
		var out = [];
		if(input == null)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(!bridgeService.findNestItemByMapId(input[i].id, "nestHomeAway")){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('unavailableNestItemId', function(bridgeService) {
	return function(input) {
		var out = [];
		if(input == null)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(input[i].mapType != null && bridgeService.aContainsB(input[i].mapType, "nest")){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('availableHueDeviceId', function(bridgeService) {
	return function(input) {
		var out = [];
		if(input == null)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(!bridgeService.findDeviceByMapId(input[i].device.uniqueid, input[i].huename, "hueDevice")){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('unavailableHueDeviceId', function(bridgeService) {
	return function(input) {
		var out = [];
		if(input == null)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(bridgeService.findDeviceByMapId(input[i].device.uniqueid, input[i].huename, "hueDevice")){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('availableHalDeviceId', function(bridgeService) {
	return function(input) {
		var out = [];
		if(input == null)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(!bridgeService.findDeviceByMapId(input[i].haldevicename + "-" +  input[i].halname, input[i].halname, "halDevice")){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('unavailableHalDeviceId', function(bridgeService) {
	return function(input) {
		var out = [];
		if(input == null)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(bridgeService.findDeviceByMapId(input[i].haldevicename + "-" +  input[i].halname, input[i].halname, "halDevice")){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.filter('configuredButtons', function() {
	return function(input) {
		var out = [];
		if(input == null)
			return out;
		for (var i = 0; i < input.length; i++) {
			if(input[i].mapType == "harmonyButton"){
				out.push(input[i]);
			}
		}
		return out;
	}
});

app.controller('VersionController', function ($scope, bridgeService) {
	$scope.bridge = bridgeService.state;
});