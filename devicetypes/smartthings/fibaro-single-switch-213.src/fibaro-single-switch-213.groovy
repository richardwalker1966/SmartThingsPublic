/**
 *  Copyright 2016 SmartThings
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
metadata {
	definition (name: "Fibaro Single Switch 213", namespace: "smartthings", author: "Robin Winbourne") {
		capability "Energy Meter"
		capability "Actuator"
		capability "Switch"
		capability "Power Meter"
		capability "Polling"
		capability "Refresh"
		capability "Sensor"
		capability "Configuration"
      
        attribute "scene", "number"

		command "reset"
        command "changeSingleParamAfterSecure"
        command "configureAfterSecure"

        fingerprint deviceId: "0x1001", inClusters: "0x5E, 0x20, 0x86, 0x72, 0x26, 0x5A, 0x59, 0x85, 0x73, 0x98, 0x7A, 0x56, 0x70, 0x31, 0x32, 0x8E, 0x60, 0x75, 0x71, 0x27, 0x22, 0xEF, 0x2B"
	}

	// simulator metadata
	simulator {
    	status "on":  "command: 2003, payload: FF"
		status "off": "command: 2003, payload: 00"
		

		for (int i = 0; i <= 10000; i += 1000) {
			status "power  ${i} W": new physicalgraph.zwave.Zwave().meterV3.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 4, scale: 2, size: 4).incomingMessage()
		}
		for (int i = 0; i <= 100; i += 10) {
			status "energy  ${i} kWh": new physicalgraph.zwave.Zwave().meterV3.meterReport(
				scaledMeterValue: i, precision: 3, meterType: 0, scale: 0, size: 4).incomingMessage()
		}

        ["FF", "00", "09", "0A", "21", "42", "63"].each { val ->
			reply "2001$val,delay 100,2602": "command: 2603, payload: $val"
		}
	}

	// tile definitions

	tiles(scale: 2) {
    	multiAttributeTile(name:"switch", type: "lighting", width: 6, height: 4, canChangeIcon: false, inactiveLabel: true, canChangeBackground: true){
			tileAttribute ("device.switch", key: "PRIMARY_CONTROL") {
				attributeState "on", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "off", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
				attributeState "turningOn", label:'${name}', action:"switch.off", icon:"st.switches.switch.on", backgroundColor:"#79b821", nextState:"turningOff"
				attributeState "turningOff", label:'${name}', action:"switch.on", icon:"st.switches.switch.off", backgroundColor:"#ffffff", nextState:"turningOn"
			}
		}
		valueTile("power", "device.power", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} W'
		}
		valueTile("energy", "device.energy", decoration: "flat", width: 2, height: 2) {
			state "default", label:'${currentValue} kWh'
		}
		standardTile("reset", "device.energy", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'reset kWh', action:"reset"
		}
		standardTile("configureAfterSecure", "device.configure", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "configure", label:'', action:"configureAfterSecure", icon:"st.secondary.configure"
		}
		standardTile("refresh", "device.power", inactiveLabel: false, decoration: "flat", width: 2, height: 2) {
			state "default", label:'', action:"refresh.refresh", icon:"st.secondary.refresh"
		}

		main(["switch","power","energy"])
		details(["switch","power","energy","configureAfterSecure","refresh","reset"])
	}

    preferences {
        def paragraph = "GROUP 0 - Fibaro Sigle Switch behavior - Basic functionalities"
       
        input name: "param9", type: "number", range: "0..1", defaultValue: "1", required: true,
            title: "9. Restore state after power failure " +
                   "This parameter determines if the device will return to state prior to the power failure after power is restored.\n" +
                   "Available settings:\n" +
                   "0 = the device does not save the state before a power failure, it returns to „off” position,\n" +
                   "1 = the device restores its state before power failure.\n" +
                   "Default value: 1."

        input name: "param10", type: "number", range: "0..5", defaultValue: "0", required: true,
            title: "10. First channel - operating mode " +
                   "This parameter allows to choose operating for the 1st channel controlled by the S1 switch.\n" +
                  "Available settings:\n" +
                   "0 = Standard Operation,\n" +
                   "1 = Delay ON,\n" +
                   "2 = Delay OFF,\n" +
                   "3 = Auto ON,\n" +
                   "4 = Auto OFF,\n" +
                   "5 = Flashing Mode.\n" +
                   "Default value: 0."

		input name: "param11", type: "number", range: "0..2", defaultValue: "0", required: true,
            title: "11. First channel - reaction to switch for delay/auto ON/OFF modes " +
                   "This parameter determines how the device in timed mode reacts to pushing the switch connected to the S1 terminal.\n" +
                   "Available settings:\n" +
                   "0 = cancel mode and set target state,\n" +
                   "1 = no reaction to switch - mode runs until it ends,\n" +
                   "2 = reset timer - start counting from the beginning.\n" +
                   "Default value: 0."

		input name: "param12", type: "number", range: "0..32000", defaultValue: "50", required: true,
            title: "12. First channel - time parameter for delay/auto ON/OFF modes " +
            	   "This parameter allows to set time parameter used in timed modes.\n" +
                   "Available settings:\n" +
                   "0 = 0.1s,\n" +
                   "1-32000 = 1-32000s in 1s steps.\n" +
                   "Default value: 50."

		input name: "param13", type: "number", range: "0..32000", defaultValue: "5", required: true,
            title: "13. First channel - pulse time for flashing mode " +
            	   "This parameter allows to set time of switching to opposite state in flashing mode.\n" +
                   "Available settings:\n" +
                   "1-32000 = 0.1-3200.0s in 0.1s steps.\n" +
                   "Default value: 5 (0.5s)."

		input name: "param20", type: "number", range: "0..2", defaultValue: "2", required: true,
            title: "20. Switch type " +
                   "This parameter defines as what type the device should treat the switch connected to the S1 and S2 terminals.\n" +
                   "Available settings:\n" +
                   "0 = momentary switch,\n" +
                   "1 = toggle switch (contact closed - ON, contact opened - OFF),\n" +
                   "2 = toggle switch (device changes status when switch changes status).\n" +
                   "Default value: 2."

		input name: "param21", type: "number", range: "0..1", defaultValue: "0", required: true,
            title: "21. Flashing mode - reports " +
                   "This parameter allows to define if the device sends reports during the flashing mode.\n" +
                   "Available settings:\n" +
                   "0 = the device does not send reports,\n" +
                   "1 = the device sends reports.\n" +
                   "Default value: 0."

		input name: "param27", type: "number", range: "0..15", defaultValue: "15", required: true,
 			title: "27. Associations in Z-Wave network security mode. " +
                   "This parameter defines how commands are sent in specified association groups: as secure or non-secure.\n" +
                   "See 'Note' in User Manual.\n" +
                   "Available settings:\n" +
                   "1 = 2nd group sent as secure,\n" +
                   "2 = 3rd group sent as secure,\n" +
                   "4 = 4th group sent as secure,\n" +
           		   "8 = 5th group sent as secure.\n" +
                   "Default value: 15 (All)."

		input name: "param28", type: "number", range: "0..15", defaultValue: "0", required: true,
 			title: "28. S1 switch - scenes sent. " +
                   "This parameter determines which actions result in sending scene IDs assigned to them.\n" +
                   "See 'Note' in User Manual.\n" +
                   "Available settings:\n" +
                   "1 = Key pressed 1 time,\n" +
                   "2 = Key pressed 2 time,\n" +
                   "4 = Key pressed 3 time,\n" +
           		   "8 = Key Hold Down and Key Released.\n" +
                   "Default value: 0 (scenes not sent)."
       
       	input name: "param29", type: "number", range: "0..15", defaultValue: "0", required: true,
 			title: "29. S2 switch - scenes sent. " +
                   "This parameter determines which actions result in sending scene IDs assigned to them.\n" +
                   "See 'Note' in User Manual.\n" +
                   "Available settings:\n" +
                   "1 = Key pressed 1 time,\n" +
                   "2 = Key pressed 2 time,\n" +
                   "4 = Key pressed 3 time,\n" +
           		   "8 = Key Hold Down and Key Released.\n" +
                   "Default value: 0 (scenes not sent)."
       
       	input name: "param30", type: "number", range: "0..15", defaultValue: "0", required: true,
 			title: "30. S1 switch - associations sent to 2nd and 3rd association groups. " +
                   "This parameter determines which actions are ignored when sending commands to devices associated in 2nd and 3rd association group.\n" +
                   "See 'Notes' in User Manual.\n" +
                   "Available settings:\n" +
                   "1 = ignore turning ON with 1 click of the switch,\n" +
                   "2 = ignore turning OFF with 1 click of the switch,\n" +
                   "4 = ignore holding and releasing the switch*,\n" +
           		   "8 = ignore double click of the switch**.\n" +
                   "Default value: 0 (all actions are active)."
       
		input name: "param31", type: "number", range: "0..255", defaultValue: "255", required: true,
            title: "31. S1 switch - Switch ON value sent to 2nd and 3rd association groups " +
            	   "This parameter defines value sent with Switch ON command to devices associated in 2nd and 3rd association group.\n" +
                   "See 'Note' in User Manual.\n" +
                   "Available settings:\n" +
                   "1-255 = Sent value.\n" +
                   "Default value: 255."
       
		input name: "param32", type: "number", range: "0..255", defaultValue: "0", required: true,
            title: "32. S1 switch - Switch OFF value sent to 2nd and 3rd association groups " +
            	   "This parameter defines value sent with Switch OFF command to devices associated in 2nd and 3rd association group.\n" +
                   "See 'Note' in User Manual.\n" +
                   "Available settings:\n" +
                   "1-255 = Sent value.\n" +
                   "Default value: 0."       
       
       	input name: "param33", type: "number", range: "0..255", defaultValue: "99", required: true,
            title: "33. S1 switch - Double Click value sent to 2nd and 3rd association groups " +
            	   "This parameter defines value sent with Double Click command to devices associated in 2nd and 3rd association group.\n" +
                   "See 'Note' in User Manual.\n" +
                   "Available settings:\n" +
                   "1-255 = Sent value.\n" +
                   "Default value: 99."  
       
              	input name: "param35", type: "number", range: "0..15", defaultValue: "0", required: true,
 			title: "35. S2 switch - associations sent to 2nd and 3rd association groups. " +
                   "This parameter determines which actions are ignored when sending commands to devices associated in 2nd and 3rd association group.\n" +
                   "See 'Notes' in User Manual.\n" +
                   "Available settings:\n" +
                   "1 = ignore turning ON with 1 click of the switch,\n" +
                   "2 = ignore turning OFF with 1 click of the switch,\n" +
                   "4 = ignore holding and releasing the switch*,\n" +
           		   "8 = ignore double click of the switch**.\n" +
                   "Default value: 0 (all actions are active)."
       
		input name: "param36", type: "number", range: "0..255", defaultValue: "255", required: true,
            title: "36. S2 switch - Switch ON value sent to 2nd and 3rd association groups " +
            	   "This parameter defines value sent with Switch ON command to devices associated in 2nd and 3rd association group.\n" +
                   "See 'Note' in User Manual.\n" +
                   "Available settings:\n" +
                   "1-255 = Sent value.\n" +
                   "Default value: 255."
       
		input name: "param37", type: "number", range: "0..255", defaultValue: "0", required: true,
            title: "37. S2 switch - Switch OFF value sent to 2nd and 3rd association groups " +
            	   "This parameter defines value sent with Switch OFF command to devices associated in 2nd and 3rd association group.\n" +
                   "See 'Note' in User Manual.\n" +
                   "Available settings:\n" +
                   "1-255 = Sent value.\n" +
                   "Default value: 0."       
       
       	input name: "param38", type: "number", range: "0..255", defaultValue: "99", required: true,
            title: "38 S2 switch - Double Click value sent to 2nd and 3rd association groups " +
            	   "This parameter defines value sent with Double Click command to devices associated in 2nd and 3rd association group.\n" +
                   "See 'Note' in User Manual.\n" +
                   "Available settings:\n" +
                   "1-255 = Sent value.\n" +
                   "Default value: 99."  
       
        input name: "param40", type: "number", range: "0..3", defaultValue: "3", required: true,
            title: "40. Reaction to General Alarm " +
                   "This parameter determines how the device will react to General Alarm frame.\n" +
                  "Available settings:\n" +
                   "0 = alarm frame is ignored,\n" +
                   "1 = turn ON after receiving the alarm frame,\n" +
                   "2 = turn OFF after receiving the alarm frame,\n" +
                   "3 = flash after receiving the alarm frame.\n" +
                   "Default value: 3."
       
        input name: "param41", type: "number", range: "0..3", defaultValue: "2", required: true,
            title: "41. Reaction to Flood Alarm " +
                   "This parameter determines how the device will react to Flood Alarm frame.\n" +
                  "Available settings:\n" +
                   "0 = alarm frame is ignored,\n" +
                   "1 = turn ON after receiving the alarm frame,\n" +
                   "2 = turn OFF after receiving the alarm frame,\n" +
                   "3 = flash after receiving the alarm frame.\n" +
                   "Default value: 2."
       
        input name: "param42", type: "number", range: "0..3", defaultValue: "3", required: true,
            title: "42. Reaction to CO / Smoke Alarm " +
                   "This parameter determines how the device will react to CO / Smoke Alarm frame.\n" +
                  "Available settings:\n" +
                   "0 = alarm frame is ignored,\n" +
                   "1 = turn ON after receiving the alarm frame,\n" +
                   "2 = turn OFF after receiving the alarm frame,\n" +
                   "3 = flash after receiving the alarm frame.\n" +
                   "Default value: 3."       
       
        input name: "param43", type: "number", range: "0..3", defaultValue: "1", required: true,
            title: "43. Reaction to Heat Alarm " +
                   "This parameter determines how the device will react to Heat Alarm frame.\n" +
                  "Available settings:\n" +
                   "0 = alarm frame is ignored,\n" +
                   "1 = turn ON after receiving the alarm frame,\n" +
                   "2 = turn OFF after receiving the alarm frame,\n" +
                   "3 = flash after receiving the alarm frame.\n" +
                   "Default value: 1."       
       
		input name: "param44", type: "number", range: "0..32000", defaultValue: "600", required: true,
            title: "44. Flashing alarm duration " +
            	   "This parameter allows to set duration of flashing alarm mode.\n" +
                   "Available settings:\n" +
                   "1-32000 = 0.1-3200.0s in 0.1s steps.\n" +
                   "Default value: 600 (10 mins)."       
       
		input name: "param50", type: "number", range: "0..100", defaultValue: "20", required: true,
            title: "50. First channel - power reports " +
            	   "This parameter determines the minimum change in consumed power that will result in sending new power report to the main controller.\n" +
                   "Available settings:\n" +
                   "0 = reports are disabled,\n" +
                   "1-100 = 1-100% - change in power.\n" +
                   "Default value: 20 (20%)."       
       
		input name: "param51", type: "number", range: "0..120", defaultValue: "10", required: true,
            title: "51. First channel - minimal time between power reports " +
            	   "This parameter determines minimum time that has to elapse before ending new power report to the main controller.\n" +
                   "Available settings:\n" +
                   "0 = reports are disabled,\n" +
                   "1-120 = 1-120s - report interval.\n" +
                   "Default value: 10 (10s)."        
       
		input name: "param53",type: "number", range: "0..32000", defaultValue: "100", required: true,
            title: "53. First channel - energy reports " +
            	   "This parameter determines the minimum change in consumed energy that will result in sending new energy report to the main controller.\n" +
                   "Available settings:\n" +
                   "0 = reports are disabled,\n" +
                   "1-32000 = 0.01-320 kWh - change in energy.\n" +
                   "Default value: 100 (1 kWh)."
                   
    	input name: "param58",type: "number", range: "0..32000", defaultValue: "3600", required: true,
            title: "58. Periodic power reports " +
            	   "This parameter determines in what time interval the periodic power reports are sent to the main controller.\n" +
                   "Available settings:\n" +
                   "0 = periodic reports are disabled,\n" +
                   "1-32000 = 1-32000s - report interval.\n" +
                   "Default value: 3600 (1h)."
                   
    	input name: "param59",type: "number", range: "0..32000", defaultValue: "3600", required: true,
            title: "58. Periodic energy reports " +
            	   "This parameter determines in what time interval the periodic energy reports are sent to the main controller.\n" +
                   "Available settings:\n" +
                   "0 = periodic reports are disabled,\n" +
                   "1-32000 = 1-32000s - report interval.\n" +
                   "Default value: 3600 (1h)."                   
       
		input name: "param60", type: "number", range: "0..1", defaultValue: "0", required: true,
            title: "60. Measuring energy consumed by the device itself " +
                   "This parameter determines whether energy metering should include the amount of energy consumed by the device itself. Results are being added to energy reports for first endpoint.\n" +
                   "Available settings:\n" +
                   "0 = function inactive,\n" +
                   "1 = function active.\n" +
                   "Default value: 0."       
      
       
        input name: "paramAssociationGroup1", type: "bool", defaultValue: true, required: true,
             title: "The Fibaro Sigle Switch provides the association of five groups.\n\n" +
                    "1st Association Group „Lifeline”,\n" +
                    "Default value: true"

        input name: "paramAssociationGroup2", type: "bool", defaultValue: true, required: true,
             title: "2nd Association Group „On/Off (S1)”,\n" +
                    "Default value: true"

        input name: "paramAssociationGroup3", type: "bool", defaultValue: false, required: true,
             title: "3rd Association Group „Dimmer (S1)”,\n" +
                    "Default value: false"

        input name: "paramAssociationGroup4", type: "bool", defaultValue: false, required: true,
             title: "4th Association Group „On/Off (S2)”,\n" +
                    "Default value: false"

        input name: "paramAssociationGroup5", type: "bool", defaultValue: false, required: true,
             title: "5th Association Group „Dimmer (S2)”.\n" +
                    "Default value: false"
    }
}

def parse(String description) {
	log.trace(description)
    log.debug("RAW command: $description")
	def result = null

    if (description != "updated") {
		def cmd = zwave.parse(description.replace("98C1", "9881"), [0x20: 1, 0x26: 3, 0x32: 3, 0x25: 1, 0x98: 1, 0x70: 1, 0x85: 2, 0x9B: 1, 0x90: 1, 0x73: 1, 0x30: 1, 0x28: 1, 0x72: 1])
		if (cmd) {
			result = zwaveEvent(cmd)
		}
    }
    log.debug "Parsed '${description}' to ${result.inspect()}"
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.sceneactivationv1.SceneActivationSet cmd) {
	log.debug( "Scene ID: $cmd.sceneId")

    sendEvent(name: "scene", value: "$cmd.sceneId", data: [switchType: "$settings.param20"], descriptionText: "Scene id $cmd.sceneId was activated", isStateChange: true)
    log.debug( "Scene id $cmd.sceneId was activated" )
}


// Devices that support the Security command class can send messages in an encrypted form;
// they arrive wrapped in a SecurityMessageEncapsulation command and must be unencapsulated
def zwaveEvent(physicalgraph.zwave.commands.securityv1.SecurityMessageEncapsulation cmd) {
	log.trace(cmd)
	def encapsulatedCommand = cmd.encapsulatedCommand([0x20: 1, 0x26: 3, 0x32: 3, 0x25: 1, 0x98: 1, 0x70: 1, 0x85: 2, 0x9B: 1, 0x90: 1, 0x73: 1, 0x30: 1, 0x28: 1, 0x72: 1]) // can specify command class versions here like in zwave.parse
	if (encapsulatedCommand) {
		return zwaveEvent(encapsulatedCommand)
	} else {
		log.warn "Unable to extract encapsulated cmd from $cmd"
		createEvent(descriptionText: cmd.toString())
	}
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicReport cmd) {
	log.trace(cmd)
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.basicv1.BasicSet cmd) {
	log.trace(cmd)
	//dimmerEvents(cmd)
}
def zwaveEvent(physicalgraph.zwave.commands.switchmultilevelv3.SwitchMultilevelReport cmd) {
	log.trace(cmd)
	dimmerEvents(cmd)
}

def zwaveEvent(physicalgraph.zwave.commands.switchbinaryv1.SwitchBinaryReport cmd)
{
	log.trace(cmd)
	dimmerEvents(cmd)
}


def dimmerEvents(physicalgraph.zwave.Command cmd) {
	log.trace(cmd)
	def result = []
	def value = (cmd.value ? "on" : "off")
	def switchEvent = createEvent(name: "switch", value: value, descriptionText: "$device.displayName was turned $value")
	result << switchEvent
	if (switchEvent.isStateChange) {
		result << response(["delay 3000", zwave.meterV2.meterGet(scale: 2).format()])
	}
	return result
}

def zwaveEvent(physicalgraph.zwave.commands.meterv3.MeterReport cmd) {
	log.trace(cmd)
	if (cmd.meterType == 1) {
		if (cmd.scale == 0) {
			return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kWh")
		} else if (cmd.scale == 1) {
			return createEvent(name: "energy", value: cmd.scaledMeterValue, unit: "kVAh")
		} else if (cmd.scale == 2) {
			return createEvent(name: "power", value: Math.round(cmd.scaledMeterValue), unit: "W")
		} else {
			return createEvent(name: "electric", value: cmd.scaledMeterValue, unit: ["pulses", "V", "A", "R/Z", ""][cmd.scale - 3])
		}
	}
}

def zwaveEvent(physicalgraph.zwave.Command cmd) {
	log.trace(cmd)
	log.debug "No handler for $cmd"
	// Handles all Z-Wave commands we aren't interested in
	createEvent(descriptionText: cmd.toString(), isStateChange: false)
}


def on() {
	log.trace("on")
	secureSequence([
			zwave.basicV1.basicSet(value: 0xFF),
            zwave.switchMultilevelV1.switchMultilevelGet()
	])
}

def off() {
	log.trace("off")
	secureSequence([
			zwave.basicV1.basicSet(value: 0x00),
            zwave.switchMultilevelV1.switchMultilevelGet()
	])
}

def poll() {
	log.trace("poll")
	secureSequence([
		zwave.meterV2.meterGet(scale: 0),
		zwave.meterV2.meterGet(scale: 2)
	])
}

def refresh() {
	log.trace("trace")
	secureSequence([
		zwave.meterV2.meterGet(scale: 0),
		zwave.meterV2.meterGet(scale: 2)
	])
}

def reset() {
	log.trace("reset")
	return secureSequence([
    	zwave.switchMultilevelV1.switchMultilevelGet(),
		zwave.meterV2.meterReset(),
		zwave.meterV2.meterGet(scale: 0),
        zwave.meterV2.meterGet(scale: 2)
	])
}


def changeSingleParamAfterSecure(paramNum, paramSize, paramValue) {
	log.debug "changeSingleParamAfterSecure(paramNum: $paramNum, paramSize: $paramSize, paramValue: $paramValue)"
    def cmds = secureSequence([
    	zwave.configurationV1.configurationSet(parameterNumber: paramNum, size: paramSize, scaledConfigurationValue: paramValue)
        ])
    cmds
}

def configureAfterSecure() {
    log.debug "configureAfterSecure()"
        def cmds = secureSequence([
            zwave.configurationV1.configurationSet(parameterNumber: 9, size: 1, scaledConfigurationValue: param9.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 10, size: 1, scaledConfigurationValue: param10.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 11, size: 1, scaledConfigurationValue: param11.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 12, size: 2, scaledConfigurationValue: param12.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 13, size: 2, scaledConfigurationValue: param13.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 20, size: 1, scaledConfigurationValue: param20.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 21, size: 1, scaledConfigurationValue: param21.toInteger()),
			zwave.configurationV1.configurationSet(parameterNumber: 27, size: 1, scaledConfigurationValue: param27.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 28, size: 1, scaledConfigurationValue: param28.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 29, size: 1, scaledConfigurationValue: param29.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 30, size: 1, scaledConfigurationValue: param30.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 31, size: 2, scaledConfigurationValue: param31.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 32, size: 2, scaledConfigurationValue: param32.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 33, size: 2, scaledConfigurationValue: param33.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 35, size: 1, scaledConfigurationValue: param35.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 36, size: 2, scaledConfigurationValue: param36.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 37, size: 2, scaledConfigurationValue: param37.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 38, size: 2, scaledConfigurationValue: param38.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 40, size: 1, scaledConfigurationValue: param40.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 41, size: 1, scaledConfigurationValue: param41.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 42, size: 1, scaledConfigurationValue: param42.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 43, size: 1, scaledConfigurationValue: param43.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 44, size: 2, scaledConfigurationValue: param44.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 50, size: 1, scaledConfigurationValue: param50.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 51, size: 1, scaledConfigurationValue: param51.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 53, size: 2, scaledConfigurationValue: param53.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 58, size: 2, scaledConfigurationValue: param58.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 59, size: 2, scaledConfigurationValue: param59.toInteger()),
            zwave.configurationV1.configurationSet(parameterNumber: 60, size: 1, scaledConfigurationValue: param60.toInteger())
        ])

        // Register for Group 1
        if(paramAssociationGroup1) {
        	cmds << secure(zwave.associationV2.associationSet(groupingIdentifier:1, nodeId: [zwaveHubNodeId]))
        }
        else {
        	cmds << secure(zwave.associationV2.associationRemove(groupingIdentifier:1, nodeId: [zwaveHubNodeId]))
        }
        // Register for Group 2
        if(paramAssociationGroup2) {
        	cmds << secure(zwave.associationV2.associationSet(groupingIdentifier:2, nodeId: [zwaveHubNodeId]))
        }
        else {
        	cmds << secure(zwave.associationV2.associationRemove(groupingIdentifier:2, nodeId: [zwaveHubNodeId]))
        }
        // Register for Group 3
        if(paramAssociationGroup3) {
        	cmds << secure(zwave.associationV2.associationSet(groupingIdentifier:3, nodeId: [zwaveHubNodeId]))
        }
        else {
        	cmds << secure(zwave.associationV2.associationRemove(groupingIdentifier:3, nodeId: [zwaveHubNodeId]))
        }
        // Register for Group 4
        if(paramAssociationGroup4) {
        	cmds << secure(zwave.associationV2.associationSet(groupingIdentifier:4, nodeId: [zwaveHubNodeId]))
        }
        else {
        	cmds << secure(zwave.associationV2.associationRemove(groupingIdentifier:4, nodeId: [zwaveHubNodeId]))
        }
        // Register for Group 5
        if(paramAssociationGroups5) {
        	cmds << secure(zwave.associationV2.associationSet(groupingIdentifier:5, nodeId: [zwaveHubNodeId]))
        }
        else {
        	cmds << secure(zwave.associationV2.associationRemove(groupingIdentifier:5, nodeId: [zwaveHubNodeId]))
        }

	cmds
}

def configure() {
	// Wait until after the secure exchange for this
    log.debug "configure()"
}

def updated() {
	log.debug "updated()"
	response(["delay 2000"] + configureAfterSecure() + refresh())
}

private secure(physicalgraph.zwave.Command cmd) {
	log.trace(cmd)
	zwave.securityV1.securityMessageEncapsulation().encapsulate(cmd).format()
}

private secureSequence(commands, delay=200) {
	log.debug "$commands"
	delayBetween(commands.collect{ secure(it) }, delay)
}