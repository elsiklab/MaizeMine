package org.flymine.web;

/*
 * Copyright (C) 2002-2003 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.Iterator;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletContext;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import org.flymine.metadata.Model;
import org.flymine.metadata.presentation.DisplayModel;
import org.flymine.objectstore.query.Query;

/**
 * @author Richard Smith
 * @author Mark Woodbridge
 */
public class QueryBuildAction extends Action
{
    /**
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward execute(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {
        QueryBuildForm qbf = (QueryBuildForm) form;
        
        ActionForward forward = null;
        Map buttons = qbf.getButtons();
        String button = (String) buttons.keySet().iterator().next();

        if ("editFql".equals(button)) {
            forward = editFql(mapping, form, request, response);
        } else if ("addConstraint".equals(button)) {
            forward = addConstraint(mapping, form, request, response);
        } else if ("updateClass".equals(button)) {
            forward = updateClass(mapping, form, request, response);
        } else if ("removeConstraints".equals(button)) {
            forward = removeConstraints(mapping, form, request, response);
        } else if (button.startsWith("removeClass")) {
            forward = removeClass(mapping, form, request, response);
        } else if (button.startsWith("editClass")) {
            forward = editClass(mapping, form, request, response);
        } else if ("runQuery".equals(button)) {
            forward = runQuery(mapping, form, request, response);
        }
        
        return forward;
    }

    /**
     * Save the Query on the and go to the FQL edit page.
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward editFql(ActionMapping mapping,
                                 ActionForm form,
                                 HttpServletRequest request,
                                 HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();

        Map queryClasses = (Map) session.getAttribute(Constants.QUERY_CLASSES);
        Map savedBags = (Map) session.getAttribute(Constants.SAVED_BAGS);
        ServletContext servletContext = session.getServletContext();
        Model model = ((DisplayModel) servletContext.getAttribute(Constants.MODEL)).getModel();

        if (queryClasses.size() == 0) {
            throw new Exception("There are no classes present in the query");
        }
        
        Query q = QueryBuildHelper.createQuery(queryClasses, model, savedBags);
        session.setAttribute(Constants.QUERY, q);
        session.setAttribute(Constants.QUERY_CLASSES, null);
        session.setAttribute(Constants.EDITING_ALIAS, null);

        return mapping.findForward("buildfqlquery");
    }

    /**
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward addConstraint(ActionMapping mapping,
                                       ActionForm form,
                                       HttpServletRequest request,
                                       HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();

        Map queryClasses = (Map) session.getAttribute(Constants.QUERY_CLASSES);
        String editingAlias = (String) session.getAttribute(Constants.EDITING_ALIAS);

        if (queryClasses == null || editingAlias == null) {
            return mapping.findForward("buildquery");
        }

        QueryBuildForm qbf = (QueryBuildForm) form;

        String fieldName = qbf.getNewFieldName();
        DisplayQueryClass d = (DisplayQueryClass) queryClasses.get(editingAlias);
        if (d == null) {
            return mapping.findForward("buildquery");
        }

        List constraintNames = d.getConstraintNames();
        
        int maxNum = -1;
        for (Iterator j = new TreeSet(constraintNames).iterator(); j.hasNext();) {
            String constraintName = (String) j.next();
            if (constraintName.startsWith(fieldName)) {
                maxNum = Integer.parseInt(constraintName.substring(constraintName.lastIndexOf("_")
                                                                   + 1));
            }
        }

        String constraintName = fieldName + "_" + (maxNum + 1);
        constraintNames.add(constraintName);
        d.setFieldName(constraintName, fieldName);

        d.setFieldOps(qbf.getParsedFieldOps());
        d.setFieldValues(qbf.getParsedFieldValues());

        return mapping.findForward("buildquery");
    }

    /**
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward updateClass(ActionMapping mapping,
                                     ActionForm form,
                                     HttpServletRequest request,
                                     HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();

        Map queryClasses = (Map) session.getAttribute(Constants.QUERY_CLASSES);
        String editingAlias = (String) session.getAttribute(Constants.EDITING_ALIAS);

        QueryBuildForm qbf = (QueryBuildForm) form;

        DisplayQueryClass d = (DisplayQueryClass) queryClasses.get(editingAlias);
        d.setFieldOps(qbf.getParsedFieldOps());
        d.setFieldValues(qbf.getParsedFieldValues());
        
        session.removeAttribute(Constants.EDITING_ALIAS);

        return mapping.findForward("buildquery");
    }

    /**
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward removeConstraints(ActionMapping mapping,
                                           ActionForm form,
                                           HttpServletRequest request,
                                           HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();
        
        Map queryClasses = (Map) session.getAttribute(Constants.QUERY_CLASSES);
        String editingAlias = (String) session.getAttribute(Constants.EDITING_ALIAS);

        QueryBuildForm qbf = (QueryBuildForm) form;
        String[] selectedConstraints = qbf.getSelectedConstraints();

        DisplayQueryClass d = (DisplayQueryClass) queryClasses.get(editingAlias);
        for (int i = 0; i < selectedConstraints.length; i++) {
            String constraintName = selectedConstraints[i];
            d.getConstraintNames().remove(constraintName);
            d.getFieldNames().remove(constraintName);
            d.getFieldOps().remove(constraintName);
            d.getFieldValues().remove(constraintName);
        }

        return mapping.findForward("buildquery");
    }

    /**
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward removeClass(ActionMapping mapping,
                                     ActionForm form,
                                     HttpServletRequest request,
                                     HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();

        Map queryClasses = (Map) session.getAttribute(Constants.QUERY_CLASSES);

        QueryBuildForm qbf = (QueryBuildForm) form;
        String button = (String) qbf.getButtons().keySet().iterator().next();
        String alias = button.substring("removeClass".length());

        queryClasses.remove(alias);
        QueryBuildHelper.removeContainsConstraints(queryClasses, alias);

        return mapping.findForward("buildquery");
    }

    /**
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward editClass(ActionMapping mapping,
                                   ActionForm form,
                                   HttpServletRequest request,
                                   HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();

        QueryBuildForm qbf = (QueryBuildForm) form;
        String button = (String) qbf.getButtons().keySet().iterator().next();
        String alias = button.substring("editClass".length());

        session.setAttribute(Constants.EDITING_ALIAS, alias);

        return mapping.findForward("buildquery");
    }

    /**
     * @param mapping The ActionMapping used to select this instance
     * @param form The optional ActionForm bean for this request (if any)
     * @param request The HTTP request we are processing
     * @param response The HTTP response we are creating
     * @return an ActionForward object defining where control goes next
     * @exception Exception if the application business logic throws
     *  an exception
     */
    public ActionForward runQuery(ActionMapping mapping,
                                  ActionForm form,
                                  HttpServletRequest request,
                                  HttpServletResponse response)
        throws Exception {
        HttpSession session = request.getSession();

        Map queryClasses = (Map) session.getAttribute(Constants.QUERY_CLASSES);
        Map savedBags = (Map) session.getAttribute(Constants.SAVED_BAGS);
        ServletContext servletContext = session.getServletContext();
        Model model = ((DisplayModel) servletContext.getAttribute(Constants.MODEL)).getModel();

        session.setAttribute(Constants.QUERY, QueryBuildHelper.createQuery(queryClasses, model,
                                                                           savedBags));

        return mapping.findForward("runquery");
    }
}
