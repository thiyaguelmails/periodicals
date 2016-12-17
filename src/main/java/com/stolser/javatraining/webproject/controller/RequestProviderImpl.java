package com.stolser.javatraining.webproject.controller;

import com.stolser.javatraining.webproject.controller.request_processor.BackendMainPage;
import com.stolser.javatraining.webproject.controller.request_processor.SignOut;
import com.stolser.javatraining.webproject.controller.request_processor.RequestProcessor;
import com.stolser.javatraining.webproject.controller.request_processor.admin.DisplayAdminPanel;
import com.stolser.javatraining.webproject.controller.request_processor.invoice.PayOneInvoice;
import com.stolser.javatraining.webproject.controller.request_processor.invoice.PersistOneInvoice;
import com.stolser.javatraining.webproject.controller.request_processor.periodical.*;
import com.stolser.javatraining.webproject.controller.request_processor.user.DisplayAllUsers;
import com.stolser.javatraining.webproject.controller.request_processor.user.DisplayCurrentUser;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Pattern;

final class RequestProviderImpl implements RequestProvider {
    private static final Map<String, RequestProcessor> requestMapping = new HashMap<>();
    private static final String NO_MAPPING_FOR_SUCH_REQUEST = "There no mapping for such a request: '%s'.";

    static {
        requestMapping.put("GET:/backend/?", new BackendMainPage());
        requestMapping.put("GET:/backend/users/?", new DisplayAllUsers());
        requestMapping.put("GET:/backend/users/currentUser/?", new DisplayCurrentUser());
        requestMapping.put("GET:/backend/periodicals/\\d+", new DisplayOnePeriodical());
        requestMapping.put("GET:/backend/periodicals/?", new DisplayAllPeriodicals());
        requestMapping.put("POST:/backend/periodicals/?", new PersistOnePeriodical());
        requestMapping.put("GET:/backend/periodicals/createNew/?", new CreateNewPeriodical());
        requestMapping.put("GET:/backend/periodicals/\\d+/update/?", new UpdatePeriodical());
        requestMapping.put("POST:/backend/periodicals/discarded/?", new DeleteDiscardedPeriodicals());
        requestMapping.put("POST:/backend/users/\\d+/invoices/?", new PersistOneInvoice());
        requestMapping.put("POST:/backend/users/\\d+/invoices/\\d+/pay/?", new PayOneInvoice());
        requestMapping.put("GET:/backend/signOut/?", new SignOut());
        requestMapping.put("GET:/backend/adminPanel/?", new DisplayAdminPanel());

    }

    private RequestProviderImpl() {}

    private static class InstanceHolder {
        private static final RequestProviderImpl INSTANCE = new RequestProviderImpl();
    }

    public static RequestProviderImpl getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public RequestProcessor getRequestProcessor(HttpServletRequest request) {
        String requestMethod = request.getMethod().toUpperCase();
        String requestURI = request.getRequestURI();

        System.out.println("requestMethod = " + requestMethod);
        System.out.println("getRequestProcessor(): requestURI = '" + requestURI + "'");

        Optional<Map.Entry<String, RequestProcessor>> mapping = requestMapping.entrySet()
                .stream()
                .filter(entry -> {
                    String methodPattern = entry.getKey().split(":")[0];
                    String[] methods = methodPattern.split("\\|");

                    return Arrays.asList(methods).contains(requestMethod);
                })
                .filter(entry -> {
                    String urlPattern = entry.getKey().split(":")[1];
//                    System.out.println("urlPattern = '" + urlPattern + "'");

                    return Pattern.matches(urlPattern, requestURI);
                })
                .findFirst();

        System.out.println("mapping = " + mapping);

        if (mapping.isPresent()) {
            return mapping.get().getValue();
        } else {
            throw new NoSuchElementException(
                    String.format(NO_MAPPING_FOR_SUCH_REQUEST, requestURI));
        }
    }
}