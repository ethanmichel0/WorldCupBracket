package com.worldcup.bracket.Service

import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.URI

import org.springframework.stereotype.Component

fun BuildNewRequest(url: String, method: String, requestBody: String? = null, vararg headers: String) : HttpRequest {
    val body = if (requestBody == null) HttpRequest.BodyPublishers.noBody() else HttpRequest.BodyPublishers.ofString(requestBody)
    if (headers.size == 0) {
        return HttpRequest.newBuilder()
        .uri(URI.create(url))
        .method(method, body)
        .build();
    }
    return HttpRequest.newBuilder()
        .headers(*headers)
        .uri(URI.create(url))
        .method(method, body)
        .build();
}