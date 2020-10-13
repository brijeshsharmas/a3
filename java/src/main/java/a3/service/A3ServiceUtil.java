/**
 * @author Brijesh Sharma
 * Copyright (c) 2020, Brijesh Sharma 
 * All rights reserved. 
 * This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree. 
 */
package a3.service;
import static a3.model.request.A3Response.Key.CODE;
import static a3.model.request.A3Response.Key.ERROR;
import static a3.model.request.A3Response.Key.ID;
import static a3.model.request.A3Response.Key.MESSAGE;
import static a3.model.request.A3Response.Key.OBJECT;
import static a3.model.request.A3Response.Key.PARAMS;
import static a3.model.request.A3Response.Key.SHORT_MSG;
import static a3.model.request.A3Response.Key.STATUS;
import static a3.model.request.A3Response.Key.SUCCESS;
import static a3.request.accelerator.A3RequestAccelerator.Config.SERVICE_TYPES;
import static a3.service.A3Service.Property.CLOUD_PROVIDER_SUBSITUTOR;
import static a3.service.A3Service.Property.CLOUD_SERVICE_OPERATION;
import static a3.service.A3Service.StatusCode.*;
import static a3.service.irs.IrsService.IrsRecord.EXPIRED_ON;
import static a3.service.irs.IrsService.IrsRecord.TEMPLATE;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import a3.model.RestApiResponse;
import a3.model.db.Operation;
import a3.model.nosql.NoSQLDataType;
import a3.model.nosql.NoSQLItem;
import a3.model.request.A3Request;
import a3.model.request.A3Response;
import a3.request.accelerator.A3ServiceFactory;
import a3.service.irs.IrsService;
import a3.service.irs.IrsTemplate;
import a3.service.irs.TemplateService;
import a3.service.irs.ivr.IvrService;

/**
 * @author Brijesh Sharma<br>
 * This class provides various utility methods to CloudService libraries and accelerator.
 */
public class A3ServiceUtil {

	private static ObjectMapper mapper = new ObjectMapper();
	private static final String secretKey = "aesEncryptionKeydddddddd";
	private static final String initVector = "encryptionIntVec";
	private static final String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\."+ 
            "[a-zA-Z0-9_+&*-]+)*@" + 
            "(?:[a-zA-Z0-9-]+\\.)+[a-z" + 
            "A-Z]{2,7}$"; 
	private static final Pattern pattern = Pattern.compile(emailRegex); 
	
	/***---------------------------------------------STARTED - Encryption/Dycryption-----------------------------***/
	public static String encrypt(String value) {
	    try {
	        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
	        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
	 
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv);
	 
	        byte[] encrypted = cipher.doFinal(value.getBytes());
	        return Base64.getEncoder().encodeToString(encrypted);
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	    return null;
	}
	
	public static String decrypt(String encrypted) {
	    try {
	        IvParameterSpec iv = new IvParameterSpec(initVector.getBytes("UTF-8"));
	        SecretKeySpec skeySpec = new SecretKeySpec(secretKey.getBytes("UTF-8"), "AES");
	 
	        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
	        cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);
	        byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
	 
	        return new String(original);
	    } catch (Exception ex) {
	        ex.printStackTrace();
	    }
	 
	    return null;
	}
	/***---------------------------------------------STARTED - General Utility Method-----------------------------***/
	public static boolean isValidEmail(String email) { return email == null ? false : pattern.matcher(email).matches(); } 
	public static boolean isNumeric(String str) {
		  NumberFormat formatter = NumberFormat.getInstance();
		  ParsePosition pos = new ParsePosition(0);
		  formatter.parse(str, pos);
		  return str.length() == pos.getIndex();
	}
	public static String generateRandonAlphaNumberic(int maxLength, int tokenCounter, String tokenSeperator) {
		int leftLimit = 97; // letter 'a'
	    int rightLimit = 122; // letter 'z'
	    int targetStringLength = maxLength;
	    Random random = new Random();
	    StringBuilder buffer = new StringBuilder(targetStringLength);
	    
	    for (int i = 0; i < targetStringLength; i++) {
	        int randomLimitedInt = leftLimit + (int)(random.nextFloat() * (rightLimit - leftLimit + 1));
	        buffer.append((char) randomLimitedInt);
	        if (tokenCounter > 0 && i % tokenCounter == 0) buffer.append(tokenSeperator);
	    }
	    return buffer.toString();
	}
	
	public static long buildEpochTime_Using_ExpiredOnProperty(A3Request request) {
		long ttl = 24*60; //DefaultTTL time for IVR Records is set to one day
		if (request.getValueByKeyIgnoreCase(EXPIRED_ON.getText()) != null)
			ttl = Long.valueOf(request.getValueByKeyIgnoreCase(EXPIRED_ON.getText()));
		return (System.currentTimeMillis()/1000)+ (ttl*60);
	}
	
	public static String getCurrentDataTime() {
		return new Date(System.currentTimeMillis()).toString();
	}
	
	public static String capitalizeString(String str){  
	    String words[]=str.split("\\s");  
	    String capitalizeWord="";  
	    for(String w:words){  
	        String first=w.substring(0,1);  
	        String afterfirst=w.substring(1);  
	        capitalizeWord+=first.toUpperCase()+afterfirst+" ";  
	    }  
	    return capitalizeWord.trim();  
	}  
	
	/**
	 * This method convert every NOT null Key & Value inside Map<String, String> to a Properties object. 
	 */
	public static Properties convertMapToPropertiesIgnoreNull (Map <String, String> map) {
		
		Properties props = new Properties();
		for (Map.Entry <String, String> entry : map.entrySet()) {
	    	
			if (entry != null && entry.getKey() != null && entry.getValue() != null)
				props.put(entry.getKey().toString(), entry.getValue().toString());
	    }
			
		return props;
		
	}
	
	/** Deserialize given bytes into a java object */
	public static Object deserialize(byte[] byteArray) throws A3ServiceException{
        ByteArrayInputStream bis = new ByteArrayInputStream(byteArray);
        ObjectInput in = null;
        try {
            in = new ObjectInputStream(bis);
            Object o = in.readObject();
            return o;
 
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
            throw new A3ServiceException("Deserialization:" , "Error Deserializing Bytes: Exception " + e);
        } finally {
            try { bis.close(); } catch (IOException ex) { }
            try { if (in != null) in.close(); } catch (IOException ex) { }
        }
    }
	
	/******Serialize given java object into byte array**/
	public static byte[] serialize(Object obj) throws A3ServiceException {
		
		byte[] objBytes = null;
		ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
		ObjectOutput objOut = null;
		try {
			objOut = new ObjectOutputStream(byteOS);   
			objOut.writeObject(obj);
			objOut.flush();
			objBytes =  byteOS.toByteArray();
		} catch (IOException exception) {
			throw new A3ServiceException("Serialization:" , "Error Serializing Object: Exception " + exception);
		} finally {
			try { byteOS.close(); } catch (IOException ex) {}
		}
		
		return objBytes;
	}
	
	public static String getPropertyIgnoreCase(Properties props, String key) {
		if (props == null || key == null) return null;
		
		for(Map.Entry<Object, Object> entry: props.entrySet()) {
			if(entry.getKey().toString().equalsIgnoreCase(key))
				return entry.getValue().toString();
		}
		return null;
	}
	public static String getValueIgnoreCase(Map<String, String> map, String key) {
		if (map == null || key == null) return null;
		
		for(Map.Entry<String, String> entry: map.entrySet()) {
			if(entry.getKey().equalsIgnoreCase(key))
				return entry.getValue();
		}
		return null;
	}

	public static String findPropertyIfExists_ThrowErrorIfMissing(Properties props, String key) {
		if(props == null || key == null) return null;
		
		String value = A3ServiceUtil.getPropertyIgnoreCase(props, key);
		if (value == null)
			throw new A3ServiceException("", "Missing Mandatory Key [" + key + "]");
		return value;
	}
	
	public static void populateCloudRequestWithKeyValueMap(A3Request request, Map<?,?> keyValueMap) {
		if(request == null || keyValueMap == null) return;
		for(Map.Entry<?, ?> entry: keyValueMap.entrySet()) {
			if(entry != null && entry.getKey() != null && entry.getValue() != null)
				request.addKeyValue(entry.getKey().toString(), entry.getValue().toString());
		}
	}
	
	public static void populateCloudRequestWithKeyValueMapForKeyNotExists(A3Request request, Map<?,?> keyValueMap) {
		if(request == null || keyValueMap == null) return;
		for(Map.Entry<?, ?> entry: keyValueMap.entrySet()) {
			if(entry != null && entry.getKey() != null && entry.getValue() != null && 
					request.getValueByKeyIgnoreCase(entry.getKey().toString()) == null)
				request.addKeyValue(entry.getKey().toString(), entry.getValue().toString());
		}
	}
	public static String split(String str, char splitWith) {
		if(str == null) return null;
		if (str.trim().length() == 0) return str;
		
		String returnStr = "";
		for(int i=0; i<str.length()-1; i++)
			returnStr += str.charAt(i) + (splitWith + " ");
		
		return returnStr + str.charAt(str.length()-1);
		
	}
	/***---------------------------------------------COMPLETED - General Utility Method-----------------------------***/
	
	/***---------------------------------------------STARTED - Collection Handling-----------------------------***/
	public static <T> Set<T> convertListToSet(List<T> list) { 
        return list.stream().collect(Collectors.toSet()); 
    } 
	public static <T> List<T> convertSetToList(Set<T> set) { 
        return set.stream().collect(Collectors.toList()); 
    }
	public static <T> List<T> convertCollectionToList(Collection<T> collection) { 
        return collection.stream().collect(Collectors.toList()); 
    }
	public static List<String> convertListtoLowerCase(List<String> list) { 
		List<String> listReturn = new ArrayList<String>();
		for(String str: list)
			listReturn.add(str.toLowerCase());
        return listReturn;
    } 
	/***---------------------------------------------COMPLETED - Collection Handling-----------------------------***/
	
	
	/***---------------------------------------------STARTED - Dynamic Class Loading Method-----------------------------***/
	
	public static <T> T loadClassAndInstantiate (String className, Class<T> classType){
		try {
			Class<?> cls = Class.forName(className);
			final Object object = cls.newInstance();
		    if (classType.isInstance(object)) return (T)object; // safe cast
		    
		    throw new A3ServiceException("loadClassAndInstantiate", "Error Loading And Instantiating Class " + classType + ". Error: Class [" + 
		    		className + "] Is Not Of Type [" + classType + "]");
		    
		}catch (A3ServiceException exception) { throw exception; }
		catch (Exception exception) {
			throw new A3ServiceException("LoadClass", "Error Loading Class " + className + ". Error: - " + exception);
		}

	}

	/***---------------------------------------------COMPLETED - Dynamic Class Loading Method-----------------------------***/
	
	/***---------------------------------------------STARTED - HTTP Elements and JSON Manipulation Method-----------------------------***/
	public static Object getNodeValueFromRestApiResponseBody(RestApiResponse apiResponse, String nodePath) {
		return getNodeValue(apiResponse.getBody(), nodePath);
	}
	
	public static Object getNodeValue(String jsonBody, String nodePath) {
		return getNodeValue(jsonBody, new String[] {nodePath});
	}
	
	public static Object getNodeValue(String  jsonBody, String [] nodePaths) {
		if (jsonBody == null || nodePaths == null) return null;
		
		JsonNode root = transformObjectToJsonNode(jsonBody);
		return getNodeValue(root, nodePaths);
	}
	public static Object getNodeValue(JsonNode  root, String [] nodePaths) {
		if (root == null || nodePaths == null) return null;
		
		JsonNode value = root;
		for(String path: nodePaths) {
			value = value.findValue(path);
			if(value == null) return null;
		}
		
		return value.isInt() ? value.asInt() : (value.isBoolean() ? value.asBoolean() : value.asText()) ;
	}
	
	public static Map<String, String> getParamsFromRestApiResponseBody(RestApiResponse apiResponse, String[] nodePaths) {
		return getParams(apiResponse.getBody(), nodePaths);
	}
	public static Map<String, String> getParams(String jsonBody, String[] nodePaths) {
		Map<String, String> mapParams = new HashMap<String, String>();
		if (nodePaths == null || nodePaths.length == 0 || jsonBody == null) return mapParams;
		
		JsonNode root = transformObjectToJsonNode(jsonBody);
		return getParams(root, nodePaths);
	}
	public static Map<String, String> getParams(JsonNode root, String[] nodePaths) {
		Map<String, String> mapParams = new HashMap<String, String>();
		if (nodePaths == null || nodePaths.length == 0 || root == null) return mapParams;

		JsonNode params = root;
		for(String path: nodePaths) {
			params = params.findValue(path);
			if(params == null) return mapParams;
		}
		
		Iterator<Map.Entry<String, JsonNode>> iterator = params.fields();
		while(iterator.hasNext()) {
			Entry<String, JsonNode> entry = iterator.next();
			if(entry == null || entry.getKey() == null || entry.getValue() == null) continue;
			Object nextValue = entry.getValue();
			mapParams.put(entry.getKey(), nextValue.toString());
		}
		return mapParams;
	}
	
	
	public static String transformIntoEncodedURLString(Map<?, ?> keyValyeMap) {
		if(keyValyeMap == null ) return null;
		
		String str = null;
		for(Map.Entry<?, ?> entry: keyValyeMap.entrySet()) {
			if (entry == null ) continue;
				str += A3ServiceUtil.URLEncode(entry.getKey()) + "=" + A3ServiceUtil.URLEncode(entry.getValue()) + "&";
		}
		if (str != null) str = str.substring(0, str.length()-1);
		return str;
	}
	
	public static String buildEncodedQueryStringFromCloudRequest(A3Request request, String [] keysToEncode ) {
		if(request == null) throw new A3ServiceException("BuildCallBackURL",
				"Could Not Build Call Back URL Because Cloud Request [ " +  request  +   "] Is Null");
		
		if(keysToEncode == null) return "";
		
		//Build Mandatory Fields Like Client ID
		String callBackURL = "";
		String missingKeys = "Missing Mandatory Keys/Overrides [";
		int missingKeysLength = missingKeys.length();
		for(String str: keysToEncode) {
			String keyValue = request.getValueByKeyIgnoreCase(str);
			if(keyValue != null) 
				callBackURL += A3ServiceUtil.URLEncode(str) + "=" + A3ServiceUtil.URLEncode(keyValue) + "&";
			else
				missingKeys += str + ",";
		}
		
		if(missingKeys.length() > missingKeysLength) {
			missingKeys = missingKeys.substring(0, missingKeys.length()-1) + "]";
			throw new A3ServiceException("BuildCallBackURL",
					"Could Not Build Call Back URL Because Of " + missingKeys  +  " In Cloud Request [" +  request + "]");
		}
		
		if(callBackURL.length() == 0) return callBackURL;
		
		return "?" + callBackURL.substring(0, callBackURL.length()-1);
	}
	
	public static String buildEncodedQueryStringFromCloudRequest(A3Request request ) {
		if(request == null) throw new A3ServiceException("BuildCallBackURL",
				"Could Not Build Call Back URL Because Cloud Request [ " +  request  +   "] Is Null");
		
		String callBackURL = "";
		for(Map.Entry<String, String> entry: request.getKeyValueMap().entrySet())
				callBackURL += A3ServiceUtil.URLEncode(entry.getKey()) + "=" + A3ServiceUtil.URLEncode(entry.getValue()) + "&";
		if(callBackURL.length() == 0) return callBackURL;
		
		return "?" + callBackURL.substring(0, callBackURL.length()-1);
	}
	
	public static String URLEncode(Object object) {
		if(object == null) return null;
		return URLEncode(object.toString()); 
	}
	public static String URLEncode(String url) {
		try { if(url == null) return null;
			return URLEncoder.encode( url, "UTF-8" ); 
		}catch(UnsupportedEncodingException exception) {
			throw new A3ServiceException("", "Error Encoding URL [" + url + "]. Error " + exception.getMessage());
		}
	}
	
	public static String URLDecode(String url) {
		try { return URLDecoder.decode( url, "UTF-8" ); 
		}catch(UnsupportedEncodingException exception) {
			throw new A3ServiceException("", "Error Encoding URL [" + url + "]. Error " + exception.getMessage());
		}
	}
	public static String transformObjectToJsonString(Object obj) throws A3ServiceException{
		try { return mapper.writeValueAsString(obj); }catch(JsonProcessingException exception) {
			throw new A3ServiceException("JSON Processing", "Error Parsing Object "  + 
					(obj == null? "null" : obj.getClass()+"") + ". Exception: " + exception.getMessage());
		}
	}
	
	public static boolean writeToFileAsJson(Object obj, String filePath) throws A3ServiceException{
		try {
			File file = new File(filePath);
			file.createNewFile();
			
			mapper.writeValue(file, obj);
			return true;
			
		}catch(JsonProcessingException exception) {
			throw new A3ServiceException("JSON Processing", "Error Parsing Object "  + 
					(obj == null? "null" : obj.getClass()+"") + ". Exception: " + exception.getMessage());
		}catch(IOException exception) {
			throw new A3ServiceException("JSON Processing", "Error Parsing Object "  + 
					(obj == null? "null" : obj.getClass()+"") + ". Exception: " + exception.getMessage());
		}
	}
	public static JsonNode transformObjectToJsonNode(Object obj) throws A3ServiceException{
		try { return mapper.readTree(obj.toString()); }catch(IOException exception) {
			throw new A3ServiceException("JSON Processing", "Error Parsing Object "  + 
					(obj == null? "null" : obj.getClass()+"") + ". Exception: " + exception.getMessage());
		}
	}
	public static <T> T transformJsonStringToObject(byte[] bytes, Class<T> type) {
		try { return mapper.readValue(bytes, type); }catch(IOException exception) {
			throw new A3ServiceException("JSON Processing", "JSON Processing For Class Type[" 
					+ (type == null? "null" : type.getName() +"") + ". Exception: " + exception.getMessage());
		}
	}
	public static <T> T transformJsonStringToObject(byte[] bytes, TypeReference<T> type) {
		try { return mapper.readValue(bytes, type); }catch(IOException exception) {
			throw new A3ServiceException("JSON Processing", "JSON Processing For Class Type[" 
					+ (type == null? "null" : type.toString() +"") + ". Exception: " + exception.getMessage());
		}
	}
	public static <T> T transformJsonFileToObject(String filePath, Class<T> type) {
		File file = new File(filePath);
		if(!file.exists()) throw new A3ServiceException("JSON Processing", "JSON Processing For Class Type[" 
				+ (type == null? "null" : type.getName() +"") + ". Exception: " + filePath + " Does Not Exists");
		
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(file);
			byte[] bytes = new byte[inputStream.available()];
			inputStream.read(bytes);
			return transformJsonStringToObject(bytes, type);
		}catch(FileNotFoundException exception) {
			throw new A3ServiceException("JSON Processing", "JSON Processing For Class Type[" 
					+ (type == null? "null" : type.getName() +"") + ". Exception: " + exception.getMessage());
		}catch(IOException exception) {
			throw new A3ServiceException("JSON Processing", "JSON Processing For Class Type[" 
					+ (type == null? "null" : type.getName() +"") + ". Exception: " + exception.getMessage());
		}finally {try{ if(inputStream != null)  inputStream.close();}catch(Exception e) {}}
		
	}
	
	/***---------------------------------------------COMPLETED - HTTP Elements and JSON Manipulation Method-----------------------------***/
	
	/***---------------------------------------------STARTED - Date Handling Method-----------------------------***/
	public static boolean isDate(String dateToValidate, String dateFromat){
		
		if(dateToValidate == null) return false;
		
		SimpleDateFormat sdf = new SimpleDateFormat(dateFromat);
		sdf.setLenient(false);
		try { sdf.parse(dateToValidate); } catch (Exception e) {return false; }
		
		return true;
	}
	public static int compareWithCurrentDate(String date, String dateFormat) throws ParseException {
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		Date dateTmp = new Date();
		String strCurrentDate = dateFormatter.format(dateTmp);
		
		Date inputDate = dateFormatter.parse(date);
		Date currentDate = dateFormatter.parse(strCurrentDate);
		
		return inputDate.compareTo(currentDate);
	}
	
	/***---------------------------------------------STARTED - Date Handling Method-----------------------------***/
	/***---------------------------------------------STARTED - Supporting Cloud Data Model {@link NoSQLItem}  Method-----------------------------***/
	public static boolean isNoSQLDataType(String dataType) {
		NoSQLDataType [] elements =  NoSQLDataType.values();
		for (NoSQLDataType element: elements) {
			if (element.getText().equalsIgnoreCase(dataType)) return true;
		}
		
		return false;
	}
	
	public static List<String> listNoSQLDataType() {
		NoSQLDataType [] elements =  NoSQLDataType.values();
		List<String> validTypes= new ArrayList<String>();
		for (NoSQLDataType element: elements) 
			validTypes.add(element.getText());
		return validTypes;
	}
	
	public static String getNoSQLDataType_ThrowErrorIfNotSupported(Object value) {
		if (value instanceof String || value instanceof Character) return NoSQLDataType.String.getText();
		
		if (value instanceof BigDecimal || value instanceof Float || value instanceof Double || 
				value instanceof Long || value instanceof Integer)
			return NoSQLDataType.Number.getText();
		
		if (value instanceof Boolean) return NoSQLDataType.Boolean.getText();
		
		throw new A3ServiceException(Operation.CREATE_ATTRIBUTE_ELEMENT.name(),
				value == null ? null : value.getClass() + " Data Type is NOT supported by " + A3NoSQLService.class.getSimpleName());
	}
	
	/***---------------------------------------------COMPLETED - Supporting Cloud Data Model {@link NoSQLItem}  Method-----------------------------***/
	
	/***---------------------------------------------STARTED - Supporting Cloud Data Model {@link A3Request} & {@link A3Response} Method------***/
	
	public static void populateIfNotNull(A3Request request, String key, Object value) {
		if (request == null || value == null) return;
		request.addKeyValue(key, value.toString());
	}
	public static ObjectNode buildJsonBody(A3Request request) {
		if(request == null) return null;
		
		ObjectNode requestNode = JsonNodeFactory.instance.objectNode();
		requestNode.put(ID.getText(), request.getId());
		if (request instanceof A3Response) {
			A3Response response = (A3Response) request;
			requestNode.put(STATUS.getText(), response.processed());

			ObjectNode responseNode = JsonNodeFactory.instance.objectNode();
			if (response.getCode() != null) responseNode.put(CODE.getText(), response.getCode());
			if (response.getShortMessage() != null) responseNode.put(SHORT_MSG.getText(), response.getShortMessage());
			if (response.getMessage() != null) responseNode.put(MESSAGE.getText(), response.getMessage());
			if (response.processed()) requestNode.set(SUCCESS.getText(), responseNode); else requestNode.set(ERROR.getText(), responseNode);
		}
		
		if(request.getObject() != null)
			requestNode.put(OBJECT.getText(), request.getObject().toString());
		
		ObjectNode keyValueNode = JsonNodeFactory.instance.objectNode();
		List<String> suppressFields = request.getSuppressFields();
		for(Map.Entry<String, String> entry: request.getKeyValueMap().entrySet()) {
			if (entry == null || entry.getKey() == null || entry.getValue() == null) continue;
			if(suppressFields.contains(entry.getKey().toString())) 
				keyValueNode.put(entry.getKey().toString(), "************************");
			else keyValueNode.put(entry.getKey().toString(), entry.getValue().toString());
		}
		requestNode.set(PARAMS.getText(), keyValueNode);
		
		return requestNode;
	}
	
	public static A3Response crossReferenceKeys(Map<?, ?> keyMap, List<String> mandatoryKeys) {
		A3Response response = new A3Response().withProcessedStatus(true);
		if (mandatoryKeys == null || mandatoryKeys.size() == 0 || keyMap == null) {
			response.setProcessedStatus(true);
			return response;
		}
		
		A3Request request = new A3Request();
		for(Map.Entry<?, ?> entry: keyMap.entrySet()) {
			if(entry != null && entry.getKey() != null && entry.getValue() !=null)
				request.addKeyValue(entry.getKey().toString(), entry.getValue().toString());
		}
		
		return crossReferenceMandatoryKeysWithCloudRequest(request, mandatoryKeys);
	}
	public static A3Response crossReferenceMandatoryKeysWithCloudRequest(A3Request request, List<String> mandatoryKeys) {
		A3Response response = new A3Response(request).withProcessedStatus(true);
		if (mandatoryKeys == null || mandatoryKeys.size() == 0 || request == null) {
			response.setProcessedStatus(true);
			return response;
		}
		String msg = "Missing Mandatory Keys Are [";
		int msgLength = msg.length();
		for(String str: mandatoryKeys) {
			if (request.getValueByKeyIgnoreCase(str) == null)
				msg += str + ",";
		}
		
		if (msg.length() > msgLength ) {
			msg = msg.substring(0, msg.length()-1) + "]";
			response.setProcessedStatus(false);
			response.setMessage(msg);
			response.setCode(BAD_REQUEST.code());
			response.setShortMessage(BAD_REQUEST.name());
		}
		return response;
	}
	public static A3Response crossReferenceMandatoryKeysWithCloudRequest(A3Request request, String[] mandatoryKeys) {
		return crossReferenceMandatoryKeysWithCloudRequest(request, Arrays.asList(mandatoryKeys));
	}
	
	/**-------------------------------------------------STARTED - Method Supporting Cloud Accelerator & Cloud Service Factory--------------------------------------------*/
	
	/***Method checks if a given service <code> serviceName</code> is supported by enum type <E> <code>enumClassType</code>*/
	public static  <E extends Enum<E>> boolean doSupportCloudService(String serviceName, Class<E> enumClassType) {
		return getEnumTypeIgnoreCase(serviceName, enumClassType) == null ? false : true;
	}
	
	/***Method checks if a given service <code> serviceName</code> is supported by enum type <E> <code>enumClassType</code>*/
	public static <E extends Enum<E>> E getEnumTypeIgnoreCase(String enumName, Class<E> enumClassType) {    
        for (Enum<E> enumVal: EnumSet.allOf(enumClassType)) {
                if (enumVal.toString().equalsIgnoreCase(enumName)) return (E)enumVal;
        }
        return null;
	}
	public static <E extends Enum<E>> E getEnumTypeIgnoreCase_ThrowErrorIfNotExists(Class<E> enumClassType, Properties props, 
			A3ServiceFactory serviceFactory, String cloudProvider) {    
		String opsName = CLOUD_SERVICE_OPERATION.getText().replaceFirst(CLOUD_PROVIDER_SUBSITUTOR.getText(), cloudProvider);
		
		String serviceName = getPropertyIgnoreCase(props, SERVICE_TYPES.name());
		if(serviceName == null ) throw new A3ServiceException(opsName,
				"[" + serviceFactory.getClass().getSimpleName() + "] Could Not Find Key " + SERVICE_TYPES.name() + " Inside Properties " + props);
		
		Enum<E> service = getEnumTypeIgnoreCase(serviceName, enumClassType);
		if (service == null) throw new A3ServiceException(opsName, "Requested Service " + 
				serviceName + " is not supported. Supported Service By [" + serviceFactory.getClass().getSimpleName() + "] Are [ " + convertToString(enumClassType) + "]");
		
		return (E)service;
	}
	
	/**This method convert all enums for a given type into a string*/
	public static <E extends Enum<E>> String convertToString(Class<E> enumClassType) {
		StringBuffer buffer = new StringBuffer("[");
		for(Enum<E> next: EnumSet.allOf(enumClassType)) {
			if (next.ordinal() == 0 ) {
				buffer.append(next.name());
				continue;
			}
			buffer.append(", " + next.name());
		}
		buffer.append("]");
		return buffer.toString();
	}
	/**-------------------------------------------------COMPLETED - Method Supporting Cloud Accelerator & Cloud Service Factory--------------------------------------------*/
	
	/**-------------------------------------------------STARTED - Method Supporting Encryption and Authentication--------------------------------------------*/
	//TODO - authenticate cloud request against client id and password, which is stored in secured environment.
	public static A3Response authenticateCloudRequest(A3Request request) {
		A3Response response = new A3Response(request);
		if(request == null || request.getValueByKeyIgnoreCase(IvrService.AuthKey.IRS_CLIENT_ID.getText()) == null)
			return response.withProcessedStatus(false)
					.withCode(UNAUTHORIZED.code())
					.withShortMessage(UNAUTHORIZED.name())
					.withMessage("Could Not Authenticate Cloud Request Because It Does Not Contains Key [" + 
					IvrService.AuthKey.IRS_CLIENT_ID.getText() + "]");
		
		
		return response;
	}
	/**-------------------------------------------------COMPLETED - Method Supporting Encryption and Authentication--------------------------------------------*/
	
	/**-------------------------------------------------STARTED - Method Supporting Ivr Request and Response--------------------------------------------*/
	public static A3Response validateCloudRequestAgainstsMandatoryFieldsForPostingIrsRequest(A3Request request, 
			IrsService irsService, TemplateService irsTemplateService) {
		A3Response response = validateInputAndTemplateParamsForIrsHandling(request, irsService, irsTemplateService);
		if (!response.processed()) return response;
		
		String templateName = request.getValueByKeyIgnoreCase(TEMPLATE.getText());
		IrsTemplate irsTemplate = irsTemplateService.getTemplate(templateName, irsService.getType());
		String[] mandatoryFieldsForPostingIrsRequest = irsService.listMandatoryFieldsForPostingRequest(irsTemplate);
		return A3ServiceUtil.crossReferenceMandatoryKeysWithCloudRequest(request, mandatoryFieldsForPostingIrsRequest);
	}
	
	public static A3Response buildCallBackEncodeURL(A3Request request, IrsService irsService, TemplateService irsTemplateService) {
		A3Response response = validateInputAndTemplateParamsForIrsHandling(request, irsService, irsTemplateService);
		if (!response.processed()) return response;
		
		String templateName = request.getValueByKeyIgnoreCase(TEMPLATE.getText());
		IrsTemplate irsTemplate = irsTemplateService.getTemplate(templateName, irsService.getType());
		
		String[] mandatoryFieldsForCallBackURL = irsService.listMandatoryFieldsForPreparingCallbackURL(irsTemplate);
		try {
			return new A3Response(request).withProcessedStatus(true)
					.withMessage(A3ServiceUtil.buildEncodedQueryStringFromCloudRequest(request, mandatoryFieldsForCallBackURL));
		}catch(Exception exception) {
			return new A3Response(request).withProcessedStatus(false)
					.withCode(INTERNAL_ERROR.code()).withShortMessage(INTERNAL_ERROR.name())
					.withMessage(exception.getMessage());
		}
	}
	private static A3Response validateInputAndTemplateParamsForIrsHandling(A3Request request, IrsService irsService, TemplateService irsTemplateService) {
		if (request == null || irsTemplateService == null || irsService == null)
			return new A3Response(request).withProcessedStatus(false)
					.withCode(BAD_REQUEST.code()).withShortMessage(BAD_REQUEST.name())
					.withMessage("Not A Valid Request. Either Template Service Template [" + 
					irsTemplateService + "], IrsService [" + irsService + "] OR Cloud Request [" + request + "] Is Null");
		
		String templateName = request.getValueByKeyIgnoreCase(TEMPLATE.getText());
		if (templateName == null || !irsTemplateService.doExists(templateName, irsService.getType()) ) 
			return new A3Response(request).withProcessedStatus(false)
					.withCode(BAD_REQUEST.code()).withShortMessage(BAD_REQUEST.name())
					.withMessage("Template Validation Failed. Template [" 
					+  templateName + "] Not Found In IrsTemplateService [" + irsTemplateService.getCloudSRN()  +  "]");
		
		return new A3Response().withProcessedStatus(true);
	}
	
	
	/**-------------------------------------------------COMPLETED - Method Supporting Ivr Request and Response--------------------------------------------*/
}
