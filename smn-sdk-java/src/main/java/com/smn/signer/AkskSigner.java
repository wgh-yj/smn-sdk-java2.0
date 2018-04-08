/*
 * Copyright (C) 2017. Huawei Technologies Co., LTD. All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of Apache License, Version 2.0.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * Apache License, Version 2.0 for more details.
 */
package com.smn.signer;

import com.smn.config.SmnConfiguration;
import com.smn.http.HttpMethod;
import com.smn.request.IHttpRequest;
import org.apache.http.HttpHeaders;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * aksk signature tool
 *
 * @author zhangyx
 * @version 1.0.0
 */
public class AkskSigner {

    /**
     * smn config
     */
    private SmnConfiguration smnConfiguration;

    /**
     * service name
     */
    private String serviceName;

    /**
     * signer
     */
    private DefaultSigner signer;

    /**
     * constructor
     *
     * @param smnConfiguration smn config
     * @param serviceName      service name
     */
    public AkskSigner(SmnConfiguration smnConfiguration, String serviceName) {
        this.smnConfiguration = smnConfiguration;
        this.serviceName = serviceName;
        this.signer = new DefaultSigner();
    }

    /**
     * add signature header for get request
     *
     * @param smnRequest smn request message
     * @param url        request url
     * @throws Exception signature error throw exception
     */
    public void get(IHttpRequest smnRequest, URL url) throws Exception {
        Map<String, String> headers = this.getSignHeader(url, null, null, HttpMethod.GET);

        for (String key : headers.keySet()) {
            if (key.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                continue;
            }
            smnRequest.addHeader(key, headers.get(key));
        }
    }

    /**
     * add signature header for delete request
     *
     * @param smnRequest smn request message
     * @param url        request url
     * @throws Exception signature error throw exception
     */
    public void delete(IHttpRequest smnRequest, URL url) throws Exception {
        Map<String, String> headers = this.getSignHeader(url, null, null, HttpMethod.DELETE);

        for (String key : headers.keySet()) {
            if (key.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                continue;
            }
            smnRequest.addHeader(key, headers.get(key));
        }
    }

    /**
     * add signature header for post request
     *
     * @param smnRequest smn request message
     * @param url        request url
     * @param postbody   request content
     * @throws Exception signature error throw exception
     */
    public void post(IHttpRequest smnRequest, URL url, String postbody) throws Exception {
        InputStream content = new ByteArrayInputStream(postbody.getBytes());
        Map<String, String> headers = this.getSignHeader(url, null, content, HttpMethod.POST);
        for (String key : headers.keySet()) {
            if (key.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH.toString())) {
                continue;
            }
            smnRequest.addHeader(key, headers.get(key));
        }
    }

    /**
     * add signature header for put request
     *
     * @param smnRequest smn request message
     * @param url        request url
     * @param postbody   request content
     * @throws Exception signature error throw exception
     */
    public void put(IHttpRequest smnRequest, URL url, String postbody) throws Exception {
        InputStream content = new ByteArrayInputStream(postbody.getBytes());
        Map<String, String> headers = this.getSignHeader(url, null, content, HttpMethod.PUT);
        for (String key : headers.keySet()) {
            if (key.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                continue;
            }
            smnRequest.addHeader(key, headers.get(key));
        }
    }

    private Map<String, String> getSignHeader(URL url, Map<String, String> headers,
                                              InputStream content, HttpMethod httpMethod)
            throws Exception {

        // Make a request for signing.
        SignerRequest request = new SignerRequest(serviceName);
        try {
            // Set the request address.
            request.setEndpoint(url.toURI());

            String urlString = url.toString();

            String parameters = null;

            if (urlString.contains("?")) {
                parameters = urlString.substring(urlString.indexOf("?") + 1);
                Map parametersmap = new HashMap<String, String>();

                if (null != parameters && !"".equals(parameters)) {
                    String[] parameterarray = parameters.split("&");

                    for (String p : parameterarray) {
                        String key = p.split("=")[0];
                        String value = p.split("=")[1];
                        parametersmap.put(key, value);
                    }
                    request.setParameters(parametersmap);
                }
            }

        } catch (URISyntaxException e) {
            // It is recommended to add logs in this place.
            e.printStackTrace();
        }
        // Set the request method.
        request.setHttpMethod(httpMethod);
        if (headers != null) {
            // Add request header information if required.
            request.setHeaders(headers);
        }
        // Configure the request content.
        request.setContent(content);

        // Select an algorithm for request signing.
        //com.cloud.sdk.auth.signer.Signer signer = SignerFactory.getSigner(serviceName, smnConfiguration.getRegionName());
        // Sign the request, and the request will change after the signing.
        signer.sign(smnConfiguration.getRegionName(), serviceName, request, smnConfiguration.getSecretAccessKey(), smnConfiguration.getAccessKeyId());

        // Make a request that can be sent by the HTTP client.
        Map<String, String> map = new HashMap<String, String>();
        Map<String, String> requestHeaders = request.getHeaders();
        // Put the header of the signed request to the new request.
        for (String key : requestHeaders.keySet()) {
            if (key.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
                continue;
            }
            map.put(key, requestHeaders.get(key));
        }
        return map;
    }
}
