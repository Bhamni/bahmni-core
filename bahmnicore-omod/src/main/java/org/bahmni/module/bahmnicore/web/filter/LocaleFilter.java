package org.bahmni.module.bahmnicore.web.filter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsConstants;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Map;

public class LocaleFilter implements Filter {

    protected final Log log = LogFactory.getLog(getClass());

    @Override
    public void init(FilterConfig arg0) throws ServletException {
        log.debug("Initializing LocaleFilter");
    }

    @Override
    public void destroy() {
        log.debug("Destroying LocaleFilter");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        String locale = request.getParameter("locale");
        User user = Context.getAuthenticatedUser();
        if(!StringUtils.isEmpty(locale) && user != null) {
            user.setUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE, locale);
        }

        chain.doFilter(request, response);
    }
}
