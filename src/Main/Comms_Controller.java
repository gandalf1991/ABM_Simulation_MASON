/*
 	Written by Pietro Russo using MASON by Sean Luke and George Mason University
*/

package Main;

import Events.EventArgs.*;
import Events.Handlers.EventHandler;
import Events.Handlers.JSONEventHandler;
import Events.Handlers.StateEventHandler;
import Main.Sim_Controller.SimStateEnum;
import Steppables.StepPublisher;
import Wrappers.GUIState_wrapper;
import javafx.util.Pair;
import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static Utils.Utilities.compressMessage;
import static java.lang.System.currentTimeMillis;

public class Comms_Controller {

	// STATE
	public static CommsStateEnum state = CommsStateEnum.NOT_READY;
	public enum CommsStateEnum {

		NOT_READY(-1),						// sim not init
		READY(0);								// sim init

		private final int code;
		private CommsStateEnum(int code){
			this.code = code;
		}
		public int getCode(){
			return code;
		}
	}

	// MQTT CLIENTS
	private static MqttAsyncClient Sim_client;
	private static MqttAsyncClient Comm_client;

	// MQTT TOPICS
	private static String[] IN_TOPICS = {"all_to_mason"};
	private static String[] OUT_TOPICS = {"mason_to_all"};
	private static ArrayList<String> CLIENTS_TOPICS = new ArrayList<>();
	private static ArrayList<String> STEPS_TOPICS = new ArrayList<String>(){{
		for(int i = 0; i < Sim_Controller.simTopics; i++){
			this.add("Topic"+i);
		}
	}};

	// USERS INFOS
	private HashMap<String, User> USERS = new HashMap<>();
	private long ADMIN_LAST_SEEN;
	private int ADMIN_TIMEOUT = 10000;

	//OPERATIONS LIST
	private static HashMap<String, String> IN_OPS = new HashMap<String, String>() {{
		put("000", "CHECK_STATUS");
		put("001", "CONNECTION");
		put("002", "DISCONNECTION");
		put("003", "SIM_LIST_REQUEST");
		put("004", "SIM_INITIALIZE");
		put("005", "SIM_UPDATE");
		put("006", "SIM_COMMAND");
		put("007", "RESPONSE");
		put("999", "CLIENT_ERROR");
	}};
	private static HashMap<String, String> OUT_OPS = new HashMap<String, String>() {{
		put("000", "CHECK_STATUS");
		put("007", "RESPONSE");
		put("008", "NEW_ADMIN");
		put("998", "SERVER_ERROR");
	}};

	// DISCONNECT ACTION ENUM
	public enum DisconnectAction{

		NONE(-1),								// Do nothing
		PAUSE(0),								// PAUSE when someone disconnects
		STOP(1);								// STOP when someone disconnects

		private final int code;
		private DisconnectAction(int code){
			this.code = code;
		}
		public int getCode(){
			return code;
		}
	}

	// CONSTRUCTOR
	public Comms_Controller() {
		initializeSimClient();
		initializeCommClient();
		state = CommsStateEnum.READY;
	}

	// RESPONSE VARIABLES
	private static ByteBuffer responseBB;
	private static String[] response_topics;
	private static JSONObject responseJson = new JSONObject();
	private static JSONObject responsePayload = new JSONObject();
	private static JSONObject requestJson = new JSONObject();
	private static JSONObject request_payload = new JSONObject();

	// EVENTS
	public StateEventHandler<CheckStatusEventArgs> checkStatusEventArgsEventHandler = new StateEventHandler<>();
	public EventHandler<DisconnectEventArgs> disconnectEventArgsEventHandler = new EventHandler<>();
	public JSONEventHandler<SimListRequestEventArgs> simListRequestEventArgsEventHandler = new JSONEventHandler<>();
	public EventHandler<SimInitializeEventArgs> simInitializeEventHandler = new EventHandler<>();
	public EventHandler<SimUpdateEventArgs> simUpdateEventHandler = new EventHandler<>();
	public EventHandler<SimCommandEventArgs> simCommandEventHandler = new EventHandler<>();

	// METHODS
	public void initializeCommClient() {
		try {
			Comm_client = new MqttAsyncClient("tcp://localhost:1883", "Comm_client");
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
			connOpts.setKeepAliveInterval(300);
			connOpts.setAutomaticReconnect(true);
			connOpts.setCleanSession(true);
			connOpts.setMaxInflight(100);
			Comm_client.connect(connOpts);
			while (!Comm_client.isConnected()) {}
			Comm_client.subscribe(IN_TOPICS[0], 0);
			Comm_client.setCallback(new MqttCallback() {

				@Override
				public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {

					// unwrap json message
					JSONParser parser = new JSONParser();
					JSONObject json = (JSONObject) parser.parse(mqttMessage.toString());
					String sender = (String) json.get("sender");
					String op = (String) json.get("op");
					JSONObject payload = (JSONObject) json.get("payload");

					System.out.println(getClass().getName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + IN_OPS.get(op) + " received from " + sender + ".");

					// verify sender auth
					if (isAdmin(sender)){
						switch (op) {
							default: onWrongCode(op, sender, json); break;						// ANYONE
							case "000" : onCheckStatus(op, sender, json); break;				// ANYONE
							case "001" : onConnection(op, sender, json); break;					// ANYONE
							case "002" : onDisconnection(op, sender, json); break;				// USER
							case "003" : onSimListRequest(op, sender, json); break;				// USER
							case "004" : onSimInitialize(op, sender, json); break;				// ADMIN
							case "005" : onSimUpdate(op, sender, json); break;					// ADMIN
							case "006" : onSimCommand(op, sender, json); break;					// ADMIN
							case "007" : onResponse(op, sender, json); break;					// USER
							case "999" : onClientError(op, sender, json); break;				// USER
						}		// ADMIN OPs
					}
					else if (isUser(sender)){
						switch (op) {
							default: onWrongCode(op, sender, json); break;						// ANYONE
							case "001" : onConnection(op, sender, json); break;					// ANYONE
							case "000" : onCheckStatus(op, sender, json); break;				// ANYONE
							case "002" : onDisconnection(op, sender, json); break;				// USER
							case "003" : onSimListRequest(op, sender, json); break;				// USER
							case "007" : onResponse(op, sender, json); break;					// USER
							case "999" : onClientError(op, sender, json); break;				// USER
						}		// USER OPs
					}
					else {
						switch (op) {
							default: onWrongCode(op, sender, json); break;						// ANYONE
							case "000" : onCheckStatus(op, sender, json); break;				// ANYONE
							case "001" : onConnection(op, sender, json); break;					// ANYONE
						}		// ANYONE OPs
					}
				}

				@Override
				public void connectionLost(Throwable throwable) {}

				@Override
				public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {}
			});


		} catch(MqttException me) {

			System.out.println("reason "+me.getReasonCode());
			System.out.println("msg "+me.getMessage());
			System.out.println("loc "+me.getLocalizedMessage());
			System.out.println("cause "+me.getCause());
			System.out.println("excep "+me);
			me.printStackTrace();
		}
	}
	public void initializeSimClient() {
		try {
			Sim_client = new MqttAsyncClient("tcp://localhost:1883", "Sim_client");
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
			connOpts.setKeepAliveInterval(300);
			connOpts.setAutomaticReconnect(true);
			connOpts.setCleanSession(true);
			connOpts.setMaxInflight(10000);
			Sim_client.connect(connOpts);
			while (!Sim_client.isConnected()) {}

		} catch(MqttException me) {

			System.out.println("reason "+me.getReasonCode());
			System.out.println("msg "+me.getMessage());
			System.out.println("loc "+me.getLocalizedMessage());
			System.out.println("cause "+me.getCause());
			System.out.println("excep "+me);
			me.printStackTrace();
		}
	}

	// Message Callbacks
	public void onWrongCode(String op, String sender, JSONObject request) {
		publishPrivateResponse(sender, op, false, "WRONG_CODE", request);
	}
	public void onCheckStatus(String op, String sender, JSONObject request) {
		Sim_Controller.SimStateEnum state;
		JSONObject response = new JSONObject();
		JSONObject payload = (JSONObject) request.get("payload");

		if(USERS.get(sender).isAdmin()){
			ADMIN_LAST_SEEN = System.currentTimeMillis();
		}

		// trigger init sim event
		CheckStatusEventArgs e = new CheckStatusEventArgs(payload);
		state = checkStatusEventArgsEventHandler.invoke(this, e);

		response.put("state", state.getCode());
		if(!state.equals(SimStateEnum.NOT_READY) && !state.equals(SimStateEnum.BUSY)){
			response.put("adminName", USERS.entrySet().stream().filter((stringUserEntry -> stringUserEntry.getValue().isAdmin())).collect(Collectors.toList()).get(0).getKey());
			response.put("simId", GUIState_wrapper.getPrototype().get("id"));
			response.put("simStepRate", Sim_Controller.simStepRate);
		}

		// response
		publishPrivateResponse(sender, op, true, "", response);
	}
	public void onConnection(String op, String sender, JSONObject request){
		boolean result;
		String error;
		JSONObject response = new JSONObject();

		// connect client
		result = connectClient(sender);
		response.put("result", result);
		response.put("sender", sender);
		response.put("isAdmin", USERS.get(sender).isAdmin());

		//response
		publishPrivateResponse(sender, op, result, result? null : "ALREADY_CONNECTED", response);
		publishPublicResponse(sender, op, result, result? null : "ALREADY_CONNECTED", response);

		if(result) Sim_Controller.completeStep = true;
	}
	public void onDisconnection(String op, String sender, JSONObject request){
		Pair<Boolean, String> res;
		Boolean result;
		String error, new_admin;

		// disconnect client
		res = disconnectClient(sender);

		result = res.getKey();
		new_admin = res.getValue();

		// response
		publishPrivateResponse(sender, op, result, result? null : "NOT_CONNECTED", request);
		publishPublicResponse(sender, op, result, result? null : "NOT_CONNECTED", request);
		if(!new_admin.equals("")) {
			SendNewAdmin(new_admin);
		}
	}
	public void onSimListRequest(String op, String sender, JSONObject request) throws IOException, ParseException {
		boolean result;
		JSONObject sim_list;
		String error;
		JSONObject payload = (JSONObject) request.get("payload");

		// trigger sim list event
		SimListRequestEventArgs e = new SimListRequestEventArgs(payload);
		e.getPayload().put("isAdmin", USERS.get(sender).isAdmin());

		sim_list = simListRequestEventArgsEventHandler.invoke(this, e);
		result = sim_list != null;

		// response
		publishPrivateResponse(sender, op, result, result? null : "INIT_FAILED", sim_list);
	}
	public void onSimInitialize(String op, String sender, JSONObject request){
		boolean result;
		String error;
		JSONObject payload = (JSONObject) request.get("payload");

		// trigger init sim event
		SimInitializeEventArgs e = new SimInitializeEventArgs(payload);
		result = simInitializeEventHandler.invoke(this, e);

		// response
		publishPrivateResponse(sender, op, result, result? null : "INIT_FAILED", request);
		publishPublicResponse(sender, op, result, result? null : "INIT_FAILED", request);
	}
	public void onSimUpdate(String op, String sender, JSONObject request){
		boolean result;
		String error;
		JSONObject payload = (JSONObject) request.get("payload");

		// trigger update sim event
		SimUpdateEventArgs e = new SimUpdateEventArgs(payload);
		result = simUpdateEventHandler.invoke(this, e);

		// response
		publishPrivateResponse(sender, op, result, result? null : "UPDATE_FAILED", request);
		publishPublicResponse(sender, op, result, result? null : "UPDATE_FAILED", request);
	}
	public void onSimCommand(String op, String sender, JSONObject request){
		boolean result;
		String error;
		JSONObject payload = (JSONObject) request.get("payload");

		// trigger sim command event
		SimCommandEventArgs e = new SimCommandEventArgs(payload);
		result = simCommandEventHandler.invoke(this, e);

		// response
		publishPrivateResponse(sender, op, result, result? null : "COMMAND_FAILED", request);
		publishPublicResponse(sender, op, result, result? null : "COMMAND_FAILED", request);
	}
	public void onResponse(String op, String sender, JSONObject request){
		// NOTHING TO DO
	}
	public void onClientError(String op, String sender, JSONObject request){
		Pair<Boolean, String> res;
		Boolean result;
		String error, new_admin;
		JSONObject payload = (JSONObject) request.get("payload");

		// disconnect client
		res = disconnectClient(sender);

		result = res.getKey();
		new_admin = res.getValue();

		// response
		publishPrivateResponse(sender, op, result, result? null : "CANNOT_DISCONNECT");
		publishPublicResponse(sender, op, result, result? null : "CANNOT_DISCONNECT", request);
		if(!new_admin.equals("")) {
			SendNewAdmin(new_admin);
		}
	}

	// Server Operations
	public void SendNewAdmin(String new_admin) {
		// fill NEW_ADMIN payload
		request_payload.put("new_admin", new_admin);
		// init message
		requestJson.put("sender", "MASON");
		requestJson.put("op", "008");
		requestJson.put("payload", request_payload);

		publishMessage(requestJson, CLIENTS_TOPICS.toArray(new String[0]));
		requestJson.clear();
		request_payload.clear();
		System.out.println(getClass().getSimpleName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + " | SEND NEW_ADMIN to all users. Admin: " + new_admin + ".");
	}

	// Support Methods
	private boolean isAdmin(String username){
		return USERS.containsKey(username) && USERS.get(username).isAdmin();
	}
	private boolean isConnected(String username){
		return USERS.containsKey(username);
	}
	private boolean isUser(String username){
		return USERS.containsKey(username) && !USERS.get(username).isAdmin();
	}
	private boolean connectClient(String username) {
		if (USERS.containsKey(username)) {
			System.out.println(getClass().getSimpleName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + username + " already exists.");
			return false;
		}
		else {
			User.Role r = User.Role.ADMIN;
			for (Map.Entry<String, User> u : USERS.entrySet()) {
				if (u.getValue().isAdmin()) {
					r = User.Role.USER;
					break;
				}
			}
			USERS.put(username, new User(username, r, System.currentTimeMillis(), new HashMap<String, Object>()));
			CLIENTS_TOPICS.add(username);
			System.out.println(getClass().getSimpleName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + username + " connected successfully as " + (r==User.Role.ADMIN ? "ADMIN" : "USER") + ".");
			return true;
		}
	}
	private Pair<Boolean, String> disconnectClient(String username){
		if (USERS.containsKey(username)) {
			User old_user = USERS.remove(username);
			CLIENTS_TOPICS.remove(username);
			JSONObject payload = new JSONObject();
			DisconnectEventArgs e = new DisconnectEventArgs(payload);
			System.out.println(getClass().getSimpleName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + (old_user.isAdmin() ? "ADMIN" : "USER") + " " + username + " disconnected successfully.");
			if(old_user.isAdmin()) {
				if(!USERS.isEmpty()) {
					Map.Entry<String, User>[] a = (Map.Entry<String, User>[]) new Map.Entry[0];
					User new_admin = USERS.entrySet().toArray(a)[0].getValue();
					new_admin.setRole(User.Role.ADMIN);
					System.out.println(getClass().getSimpleName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + new_admin.getNickname() + " became ADMIN.");
					if(!Sim_Controller.state.equals(SimStateEnum.NOT_READY)){
						e.getPayload().put("action", DisconnectAction.PAUSE);
						disconnectEventArgsEventHandler.invoke(this, e);
					}
					return new Pair<Boolean, String>(true, new_admin.getNickname());
				}
				else {
					if(!Sim_Controller.state.equals(SimStateEnum.NOT_READY)){
						e.getPayload().put("action", DisconnectAction.STOP);
						disconnectEventArgsEventHandler.invoke(this, e);
					}
				}
			}
			return new Pair<Boolean, String>(true, "");
		}
		System.out.println(getClass().getSimpleName() + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + " | " + username + " doesn't exist.");
		return new Pair<Boolean, String>(false, "");
	}

	public static void publishMessage(JSONObject Json, String[] topics) {
		try {
			responseBB = ByteBuffer.allocate(Json.toString().length() * 2);
			responseBB.order(ByteOrder.LITTLE_ENDIAN);
			for (char c : Json.toString().toCharArray()) {
				responseBB.putChar(c);
			}
			MqttMessage message = new MqttMessage(compressMessage(responseBB.array()));
			responseBB.clear();

			message.setQos(0);

			for (String topic: topics) {
				try {
					Comm_client.publish(topic, message);
				} catch (MqttException e) {
					e.printStackTrace();
				}
			}
		} catch(Exception e) {
			System.out.println("msg "+e.getMessage());
			System.out.println("loc "+e.getLocalizedMessage());
			System.out.println("cause "+e.getCause());
			System.out.println("excep "+e);
			e.printStackTrace();
		}
	}
	public static int publishStep(long step_id, byte[] step) {
		try {
			MqttMessage message = new MqttMessage(compressMessage(step));
			//System.out.println("\nStep send: "+ step_id + " on topic " + (step_id-1)%Sim_Controller.simTopics);
			message.setQos(0);
			try {
				Sim_client.publish("Topic" + (step_id-1)%Math.min((int)Math.floor(Sim_Controller.simStepRate) > 0 ? (int)Math.floor(Sim_Controller.simStepRate) : 60, 60), message);
			} catch (MqttException e) {
				e.printStackTrace();
			}
			return message.getPayload().length;
		} catch(Exception e) {
			System.out.println("msg "+e.getMessage());
			System.out.println("loc "+e.getLocalizedMessage());
			System.out.println("cause "+e.getCause());
			System.out.println("excep "+e);
			e.printStackTrace();
		}
		return 0;
	}
	public static int publishStepOnTopic(byte[] step, int topic, boolean retain) {
		try {
			MqttMessage message = new MqttMessage();
			if(step.length == 0) {
				message.setPayload(step);
			}
			else {
				message = new MqttMessage(compressMessage(step));
			}
			//System.out.println("\nStep send: "+ step_id + " on topic " + (step_id-1)%Sim_Controller.simTopics);
			message.setQos(0);
			message.setRetained(retain);
			try {
				Sim_client.publish("Topic" + topic, message);
			} catch (MqttException e) {
				e.printStackTrace();
			}
			return message.getPayload().length;
		} catch(Exception e) {
			System.out.println("msg "+e.getMessage());
			System.out.println("loc "+e.getLocalizedMessage());
			System.out.println("cause "+e.getCause());
			System.out.println("excep "+e);
			e.printStackTrace();
		}
		return 0;
	}

	public static void publishPrivateResponse(String sender, String op, boolean result, String error, JSONObject payload_data){
		// fill responsePayload
		responsePayload.put("response_to_op", op);
		responsePayload.put("result", result);
		responsePayload.put("error", error);
		responsePayload.put("payload_data", payload_data);
		// init message
		responseJson.put("sender", "MASON");
		responseJson.put("op", "007");
		responseJson.put("payload", responsePayload);
		// init topics array
		response_topics = new String[]{sender};
		publishMessage(responseJson, response_topics);
		responseJson.clear();
		responsePayload.clear();
		//System.out.println(Comms_Controller.class + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + " | PUBLISHED PRIVATE RESPONSE to " + sender + " for " + IN_OPS.get(op) + " operation ended " + (result? "successfully" : "unsuccessfully") + ".");
	}
	public static void publishPrivateResponse(String sender, String op, boolean result, String error){
		publishPrivateResponse(sender, op, result, error, new JSONObject());
	}
	public static void publishPublicResponse(String sender, String op, boolean result, String error, JSONObject request_payload){
		// fill responsePayload
		responsePayload.put("response_to_op", op);
		responsePayload.put("result", result);
		responsePayload.put("error", error);
		responsePayload.put("payload_data", request_payload);
		// init message
		responseJson.put("sender", "MASON");
		responseJson.put("op", "007");
		responseJson.put("payload", responsePayload);

		// init topics array to send to everyone except sender
		response_topics = CLIENTS_TOPICS.stream().filter((String a) -> !a.equals(sender)).toArray(String[]::new);

		publishMessage(responseJson, response_topics);
		responseJson.clear();
		responsePayload.clear();
		System.out.println(Comms_Controller.class + " | " + Thread.currentThread().getStackTrace()[1].getMethodName() + " | PUBLISHED PUBLIC RESPONSE for " + sender + "'s " + IN_OPS.get(op) + " operation ended " + (result? "successfully" : "unsuccessfully") + ".");
	}
}