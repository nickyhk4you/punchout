package com.waters.punchout.gateway.logging;

import com.waters.punchout.gateway.repository.NetworkRequestRepository;
import com.waters.punchout.gateway.entity.NetworkRequestDocument;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
@Slf4j
public class NetworkRequestLogger {

    private final NetworkRequestRepository networkRequestRepository;

    public NetworkRequestLogger(NetworkRequestRepository networkRequestRepository) {
        this.networkRequestRepository = networkRequestRepository;
    }

    public NetworkRequestDocument logInboundRequest(
            String sessionKey,
            String source,
            String destination,
            String method,
            String url,
            Map<String, String> headers,
            String requestBody,
            String requestType
    ) {
        log.debug("Logging inbound request for sessionKey={}", sessionKey);

        NetworkRequestDocument document = new NetworkRequestDocument();
        document.setRequestId(generateRequestId());
        document.setSessionKey(sessionKey);
        document.setTimestamp(LocalDateTime.now());
        document.setDirection("INBOUND");
        document.setSource(source);
        document.setDestination(destination);
        document.setMethod(method);
        document.setUrl(url);
        document.setHeaders(headers);
        document.setRequestBody(requestBody);
        document.setRequestType(requestType);

        NetworkRequestDocument saved = networkRequestRepository.save(document);
        log.info("Logged inbound request: requestId={}, sessionKey={}", saved.getRequestId(), sessionKey);
        return saved;
    }

    public NetworkRequestDocument logOutboundRequest(
            String sessionKey,
            String source,
            String destination,
            String method,
            String url,
            Map<String, String> headers,
            String requestBody,
            Integer statusCode,
            Map<String, String> responseHeaders,
            String responseBody,
            Long duration,
            String requestType,
            Boolean success,
            String errorMessage
    ) {
        log.debug("Logging outbound request for sessionKey={}", sessionKey);

        NetworkRequestDocument document = new NetworkRequestDocument();
        document.setRequestId(generateRequestId());
        document.setSessionKey(sessionKey);
        document.setTimestamp(LocalDateTime.now());
        document.setDirection("OUTBOUND");
        document.setSource(source);
        document.setDestination(destination);
        document.setMethod(method);
        document.setUrl(url);
        document.setHeaders(headers);
        document.setRequestBody(requestBody);
        document.setStatusCode(statusCode);
        document.setResponseHeaders(responseHeaders);
        document.setResponseBody(responseBody);
        document.setDuration(duration);
        document.setRequestType(requestType);
        document.setSuccess(success);
        document.setErrorMessage(errorMessage);

        NetworkRequestDocument saved = networkRequestRepository.save(document);
        log.info("Logged outbound request: requestId={}, sessionKey={}, success={}", 
                saved.getRequestId(), sessionKey, success);
        return saved;
    }

    public void updateRequestResponse(
            String requestId,
            Integer statusCode,
            Map<String, String> responseHeaders,
            String responseBody,
            Long duration,
            Boolean success,
            String errorMessage
    ) {
        networkRequestRepository.findById(requestId).ifPresent(document -> {
            document.setStatusCode(statusCode);
            document.setResponseHeaders(responseHeaders);
            document.setResponseBody(responseBody);
            document.setDuration(duration);
            document.setSuccess(success);
            document.setErrorMessage(errorMessage);
            networkRequestRepository.save(document);
            log.debug("Updated request response: requestId={}", requestId);
        });
    }

    private String generateRequestId() {
        return "REQ_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase();
    }
}
