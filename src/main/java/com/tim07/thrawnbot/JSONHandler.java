package com.tim07.thrawnbot;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * The JSONHandler file parses a URLRequest into a JSONObject and throws an exception, if the action is not possible
 * @author u/tim07
 * @see JsonObject, which is used to parse f.ex. api requests
 */

public abstract class JSONHandler {
    /**
     * getJSON returns a JSONObject after parsing a url-request
     * @param urlString the url being fetched from another method, f.ex. an api request
     * @return JSONObject which can be used to access the api values
     * @throws Exception if the urlconnection or the jsonparsing have failed. MUST be caught by the requesting method.
     */
    public static JsonObject getJSON(String urlString) throws Exception{
        URL url = new URL(urlString);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.connect();

        if (connection.getResponseCode() != 200){
            throw new Exception("API: Die API hat zur Zeit Probleme deine Eingabe zu verstehen.");
        }

        //String parsing
        StringBuilder inline = new StringBuilder();
        Scanner sc = new Scanner(url.openStream(), StandardCharsets.UTF_8);
        while(sc.hasNext()) {
            inline.append(sc.nextLine());
        }
        sc.close();

        // Returns casted JSONObject
        return (JsonObject) JsonParser.parseString(inline.toString());
    }
}
