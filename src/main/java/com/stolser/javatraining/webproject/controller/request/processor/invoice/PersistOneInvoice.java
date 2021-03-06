package com.stolser.javatraining.webproject.controller.request.processor.invoice;

import com.stolser.javatraining.webproject.controller.message.FrontMessageFactory;
import com.stolser.javatraining.webproject.controller.message.FrontendMessage;
import com.stolser.javatraining.webproject.controller.request.processor.RequestProcessor;
import com.stolser.javatraining.webproject.controller.utils.HttpUtils;
import com.stolser.javatraining.webproject.model.entity.invoice.Invoice;
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical;
import com.stolser.javatraining.webproject.model.entity.user.User;
import com.stolser.javatraining.webproject.service.InvoiceService;
import com.stolser.javatraining.webproject.service.PeriodicalService;
import com.stolser.javatraining.webproject.service.impl.InvoiceServiceImpl;
import com.stolser.javatraining.webproject.service.impl.PeriodicalServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.stolser.javatraining.webproject.controller.ApplicationResources.*;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

/**
 * Processes a POST request to create a new invoice.
 */
public class PersistOneInvoice implements RequestProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(PersistOneInvoice.class);
    private static final String EXCEPTION_DURING_PERSISTING_INVOICE = "Exception during persisting an invoice: %s.";
    private PeriodicalService periodicalService = PeriodicalServiceImpl.getInstance();
    private InvoiceService invoiceService = InvoiceServiceImpl.getInstance();
    private FrontMessageFactory messageFactory = FrontMessageFactory.getInstance();

    private PersistOneInvoice() {}

    private static class InstanceHolder {
        private static final PersistOneInvoice INSTANCE = new PersistOneInvoice();
    }

    public static PersistOneInvoice getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public String process(HttpServletRequest request, HttpServletResponse response) {
        List<FrontendMessage> generalMessages = new ArrayList<>();
        long periodicalId = Long.parseLong(request.getParameter(PERIODICAL_ID_PARAM_NAME));
        Periodical periodicalInDb = periodicalService.findOneById(periodicalId);

        if (isPeriodicalValid(periodicalInDb, request, generalMessages)) {
            tryToPersistNewInvoice(getNewInvoice(periodicalInDb, request), generalMessages);
        }

        HttpUtils.addGeneralMessagesToSession(request, generalMessages);

        return REDIRECT + getRedirectUri(periodicalId);
    }

    private String getRedirectUri(long periodicalId) {
        return String.format("%s/%d", PERIODICAL_LIST_URI, periodicalId);
    }

    private boolean periodicalExistsInDb(Periodical periodicalInDb, List<FrontendMessage> generalMessages) {
        if (isNull(periodicalInDb)) {
            generalMessages.add(messageFactory.getError(MSG_VALIDATION_PERIODICAL_IS_NULL));
        }

        return nonNull(periodicalInDb);
    }

    private boolean isPeriodicalVisible(Periodical periodicalInDb, List<FrontendMessage> generalMessages) {
        boolean isVisible = Periodical.Status.ACTIVE.equals(periodicalInDb.getStatus());

        if (!isVisible) {
            generalMessages.add(messageFactory.getError(MSG_VALIDATION_PERIODICAL_IS_NOT_VISIBLE));
        }

        return isVisible;
    }

    private boolean isSubscriptionPeriodValid(HttpServletRequest request,
                                              List<FrontendMessage> generalMessages) {
        FrontendMessage message = messageFactory.getError(MSG_VALIDATION_SUBSCRIPTION_PERIOD_IS_NOT_VALID);

        try {
            int subscriptionPeriod = Integer.parseInt(request.getParameter(SUBSCRIPTION_PERIOD_PARAM_NAME));
            if (!isPeriodValid(subscriptionPeriod)) {
                generalMessages.add(message);
            }

            return isPeriodValid(subscriptionPeriod);

        } catch (NumberFormatException e) {
            generalMessages.add(message);
            return false;
        }
    }

    private boolean isPeriodValid(int subscriptionPeriod) {
        return (subscriptionPeriod >= 1) && (subscriptionPeriod <= 12);
    }

    private boolean isPeriodicalValid(Periodical periodicalInDb, HttpServletRequest request,
                                      List<FrontendMessage> generalMessages) {
        return periodicalExistsInDb(periodicalInDb, generalMessages)
                && isPeriodicalVisible(periodicalInDb, generalMessages)
                && isSubscriptionPeriodValid(request, generalMessages);
    }

    private void tryToPersistNewInvoice(Invoice invoiceToPersist, List<FrontendMessage> generalMessages) {
        generalMessages.add(messageFactory.getInfo(MSG_VALIDATION_PASSED_SUCCESS));

        try {
            invoiceService.createNew(invoiceToPersist);

            generalMessages.add(messageFactory.getSuccess(MSG_INVOICE_CREATION_SUCCESS));
        } catch (RuntimeException e) {
            LOGGER.error(String.format(EXCEPTION_DURING_PERSISTING_INVOICE, invoiceToPersist), e);
            generalMessages.add(messageFactory.getError(MSG_INVOICE_PERSISTING_FAILED));
        }
    }

    private Invoice getNewInvoice(Periodical periodicalInDb, HttpServletRequest request) {
        int subscriptionPeriod = Integer.parseInt(request.getParameter(SUBSCRIPTION_PERIOD_PARAM_NAME));

        long totalSum = subscriptionPeriod * periodicalInDb.getOneMonthCost();
        long userIdFromUri = HttpUtils.getFirstIdFromUri(request.getRequestURI());
        User.Builder userBuilder = new User.Builder();
        userBuilder.setId(userIdFromUri);
        User user = userBuilder.build();

        Invoice.Builder invoiceBuilder = new Invoice.Builder();
        invoiceBuilder.setUser(user)
                .setPeriodical(periodicalInDb)
                .setSubscriptionPeriod(subscriptionPeriod)
                .setTotalSum(totalSum)
                .setCreationDate(Instant.now())
                .setStatus(Invoice.Status.NEW);

        return invoiceBuilder.build();
    }
}
