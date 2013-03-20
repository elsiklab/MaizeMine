package org.intermine.webservice.server.template;

/*
 * Copyright (C) 2002-2013 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import org.intermine.webservice.server.WebService;
import org.intermine.webservice.server.core.NoServiceException;
import org.intermine.webservice.server.core.WebServiceServlet;

/**
 * Runs the Template Upload Service to handle user template uploads.
 * @author Alexis Kalderimis
 *
 */
public class TemplateUploadServlet extends WebServiceServlet
{
    private static final long serialVersionUID = 1L;

    @Override
    protected WebService getService(Method method) throws NoServiceException {
        switch (method) {
            case GET: return new TemplateUploadService(api);
            case POST: return new TemplateUploadService(api);
            case PUT: return new TemplateUploadService(api);
            default: throw new NoServiceException();
        }
    }
}
