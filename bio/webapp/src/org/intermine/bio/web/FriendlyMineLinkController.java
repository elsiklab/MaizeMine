package org.intermine.bio.web;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.tiles.ComponentContext;
import org.apache.struts.tiles.actions.TilesAction;
import org.intermine.api.InterMineAPI;
import org.intermine.api.profile.InterMineBag;
import org.intermine.bio.web.logic.BioUtil;
import org.intermine.util.PropertiesUtil;
import org.intermine.util.StringUtil;
import org.intermine.util.TypeUtil;
import org.intermine.web.logic.bag.BagHelper;
import org.intermine.web.logic.session.SessionMethods;


/**
 * Gets list of friendly intermines to show on list analysis page.
 *
 * @author Julie Sullivan
 */
public class FriendlyMineLinkController  extends TilesAction
{
    private static final String IDENTIFIER = "primaryIdentifier";
    private static final String ALTERNATIVE_IDENTIFIER = "ensemblIdentifier";

    /**
     * {@inheritDoc}
     */
    @Override
    public ActionForward execute(@SuppressWarnings("unused") ComponentContext context,
            @SuppressWarnings("unused") ActionMapping mapping,
            @SuppressWarnings("unused") ActionForm form,
            HttpServletRequest request, @SuppressWarnings("unused") HttpServletResponse response) {
        InterMineBag bag = (InterMineBag) request.getAttribute("bag");
        final InterMineAPI im = SessionMethods.getInterMineAPI(request.getSession());
        Collection<String> organismsInBag = BioUtil.getOrganisms(im.getObjectStore(), bag, false,
                "shortName");
        String organisms = null;
        if (!organismsInBag.isEmpty()) {
            organisms = StringUtil.join(organismsInBag, ",");
        }
        Properties webProperties = SessionMethods.getWebProperties(request.getSession()
                .getServletContext());
        String localMineName = webProperties.getProperty("project.title");
        Properties props = PropertiesUtil.stripStart("intermines",
                PropertiesUtil.getPropertiesStartingWith("intermines", webProperties));
        Enumeration<?> propNames = props.propertyNames();
        HashMap<String, LinkedHashMap<String, String>> minePortals =
                new HashMap<String, LinkedHashMap<String, String>>();
        while (propNames.hasMoreElements()) {
            String mineId = (String) propNames.nextElement();
            mineId = mineId.substring(0, mineId.indexOf("."));
            Properties mineProps = PropertiesUtil.stripStart(mineId,
                    PropertiesUtil.getPropertiesStartingWith(mineId, props));
            
            // get name and url
            String mineName = mineProps.getProperty("name");
            String mineURL = mineProps.getProperty("url");
            if (StringUtils.isNotEmpty(mineName) && StringUtils.isNotEmpty(mineURL)
                    && !mineName.equals(localMineName)) {
                LinkedHashMap<String, String> mineDetails = new LinkedHashMap<String, String>();
                // colors for the mines
                String mineBgColor = mineProps.getProperty("bgcolor");
                String mineFrontColor = mineProps.getProperty("frontcolor");
                if (StringUtils.isNotEmpty(mineBgColor)
                        && StringUtils.isNotEmpty(mineFrontColor)) {
                    mineDetails.put("bgcolor", mineBgColor);
                    mineDetails.put("frontcolor", mineFrontColor);
                }
                mineDetails.put("url", mineURL);
                minePortals.put(mineName, mineDetails);
            }
        }

        Class c = null;
        try {
            c = Class.forName(bag.getQualifiedType());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
        String identifierField = IDENTIFIER;
        // hack so we can use ensembl for ratmine
        if (TypeUtil.getFieldInfo(c, ALTERNATIVE_IDENTIFIER) != null) {
            identifierField = ALTERNATIVE_IDENTIFIER;
        }
        String identifierList = BagHelper.getIdList(bag, im.getObjectStore(), "", identifierField);
        request.setAttribute("identifierList", identifierList);

        if (!minePortals.isEmpty() && StringUtils.isNotEmpty(organisms)) {
            request.setAttribute("mines", minePortals);
            request.setAttribute("organisms", organisms);
        }
        return null;
    }
}
