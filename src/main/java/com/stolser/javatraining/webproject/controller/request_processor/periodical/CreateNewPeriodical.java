package com.stolser.javatraining.webproject.controller.request_processor.periodical;

import com.stolser.javatraining.webproject.controller.ApplicationResources;
import com.stolser.javatraining.webproject.controller.request_processor.RequestProcessor;
import com.stolser.javatraining.webproject.controller.validator.FrontendMessage;
import com.stolser.javatraining.webproject.model.entity.periodical.Periodical;
import com.stolser.javatraining.webproject.model.entity.periodical.PeriodicalCategory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

import static com.stolser.javatraining.webproject.controller.ApplicationResources.PERIODICAL_CATEGORIES_ATTR_NAME;
import static com.stolser.javatraining.webproject.controller.ApplicationResources.PERIODICAL_STATUSES_ATTR_NAME;

public class CreateNewPeriodical implements RequestProcessor {

    @Override
    public String getViewName(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession();

        Periodical periodicalIntoRequest;
        Periodical periodicalFromSession = (Periodical) session.getAttribute("periodical");

        if (periodicalFromSession != null) {
            periodicalIntoRequest = periodicalFromSession;
            session.removeAttribute("periodical");
        } else {
            periodicalIntoRequest = new Periodical();
        }

        @SuppressWarnings("unchecked")
        Map<String, FrontendMessage> messages = (Map<String, FrontendMessage>) session
                .getAttribute(ApplicationResources.MESSAGES_ATTR_NAME);

//        session.removeAttribute(ApplicationResources.MESSAGES_ATTR_NAME);

        request.setAttribute(ApplicationResources.MESSAGES_ATTR_NAME, messages);
        request.setAttribute("periodical", periodicalIntoRequest);
        request.setAttribute(PERIODICAL_STATUSES_ATTR_NAME, Periodical.Status.values());
        request.setAttribute(PERIODICAL_CATEGORIES_ATTR_NAME, PeriodicalCategory.values());
        request.setAttribute("entityOperationType", "create");

        return ApplicationResources.CREATE_EDIT_PERIODICAL_VIEW_NAME;
    }
}
