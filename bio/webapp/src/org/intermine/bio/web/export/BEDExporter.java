package org.intermine.bio.web.export;

/*
 * Copyright (C) 2002-2011 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;
import org.intermine.api.results.ResultElement;
import org.intermine.bio.io.bed.BEDRecord;
import org.intermine.model.bio.SequenceFeature;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.util.IntPresentSet;
import org.intermine.util.PropertiesUtil;
import org.intermine.web.logic.export.ExportException;
import org.intermine.web.logic.export.ExportHelper;
import org.intermine.web.logic.export.Exporter;

/**
 * Exports LocatedSequenceFeature objects in UCSC BED format.
 *
 * @author Fengyuan Hu
 */
public class BEDExporter implements Exporter
{
    @SuppressWarnings("unused")
    private static final Logger LOG = Logger.getLogger(BEDExporter.class);

    PrintWriter out;
    private boolean makeUcscCompatible = true;
    private int writtenResultsCount = 0;
    private boolean headerPrinted = false;
    private List<Integer> featureIndexes;
    private IntPresentSet exportedIds = new IntPresentSet();
    private Set<Integer> organisms;

     /* Header */
    private static final String FORMAT = "# UCSC BED format";
    private static final String SOURCE_PRE = "\n# Source: ";
    private String sourceName;
    private static final String GENOME_BUILD_PRE = "\n# Genome Build: ";
    private String genomeBuild = "";
    private static final String TRACK_NAME_PRE = "\ntrack name=";
    private String trackName;
    private static final String TRACK_DESCRIPTION_PRE = " description=\"";
    private String trackDescription;
    private static final String TRACK_DESCRIPTION_END =  "\"";
    private static final String TRACK_USE_SCORE = " useScore=0";

    /* State for the exportRow method, to allow several rows to be merged. */
    private Integer lastLsfId = null;
    private SequenceFeature lastLsf = null;

    /**
     * Constructor.
     * @param out output stream
     * @param featureIndexes index of column with exported sequence
     * @param sourceName name of Mine to put in GFF source column
     * @param organisms taxon id of the organisms
     * @param makeUcscCompatible true if chromosome ids should be prefixed by 'chr'
     * @param trackDescription track description in the header
     */
    public BEDExporter(PrintWriter out, List<Integer> featureIndexes, String sourceName,
            Set<Integer> organisms, boolean makeUcscCompatible, String trackDescription) {

        this.out = out;
        this.featureIndexes = featureIndexes;
        this.sourceName = sourceName;
        this.organisms = organisms;
        this.makeUcscCompatible = makeUcscCompatible;
        this.trackDescription = trackDescription;

        if ("".equals(trackDescription) || trackDescription == null) {
            this.trackName = "track";
            this.trackDescription = "A Custom Track";
        } else {
            this.trackName = trackDescription.replaceAll(" ", "_");
        }
    }

    @Override
    public void export(Iterator<? extends List<ResultElement>> resultIt) {
        if (featureIndexes.size() == 0) {
            throw new ExportException("No columns with sequence");
        }
        try {
            // LOG.info("SOO:" + cNames.toString());
            while (resultIt.hasNext()) {
                List<ResultElement> row = resultIt.next();
                exportRow(row);
            }
            finishLastRow();
            out.flush();
        } catch (Exception ex) {
            throw new ExportException("Export failed", ex);
        }
    }

    private void exportRow(List<ResultElement> row)
        throws ObjectStoreException, IllegalAccessException {

        List<ResultElement> elWithObject = getResultElements(row);
        if (elWithObject == null) {
            return;
        }

        // loop through all the objects in a row
        for (ResultElement re : elWithObject) {
            SequenceFeature lsf = (SequenceFeature) re.getObject();

            if (exportedIds.contains(lsf.getId()) && !(lsf.getId().equals(lastLsfId))) {
                continue;
            }

            if ((lastLsfId != null) && !(lsf.getId().equals(lastLsfId))) {
                makeRecord();
            }

            lastLsfId = lsf.getId();
            lastLsf = lsf;
        }
    }

    /**
     * to read genome and project versions
     * @return header further info about versions
     */
    private String getHeader() {
        StringBuffer header = new StringBuffer();

        Properties props = PropertiesUtil.getProperties();
        // TODO the way to store genome build information should be changed ...
        if (organisms != null) {
            for (Integer taxId : organisms) {
                if (taxId == 7227) {
                    String fV = props.getProperty("genomeVersion.fly");
                    if (fV != null && fV.length() > 0) {
                        genomeBuild = fV;
                    }
                }
            }
            for (Integer taxId : organisms) {
                if (taxId == 6239) {
                    String wV = props.getProperty("genomeVersion.worm");
                    if (wV != null && wV.length() > 0) {
                        genomeBuild = wV;
                    }
                }
            }
        }

        if ("".equals(genomeBuild)) {
            genomeBuild = "not available";
        }

        header.append(FORMAT);
        if ("".equals(sourceName) || sourceName == null) {

        } else {
            header.append(SOURCE_PRE)
                .append(sourceName);
        }

        header.append(GENOME_BUILD_PRE)
            .append(genomeBuild)
            .append(TRACK_NAME_PRE)
            .append(trackName)
            .append(TRACK_DESCRIPTION_PRE)
            .append(trackDescription)
            .append(TRACK_DESCRIPTION_END)
            .append(TRACK_USE_SCORE);

        return header.toString();
    }

    private List<ResultElement> getResultElements(List<ResultElement> row) {
        List<ResultElement> els = new ArrayList<ResultElement>();
        for (Integer index : featureIndexes) {
            if (row.get(index) != null) {
                els.add(row.get(index));
            }
        }
        return els;
    }

    private void makeRecord() {

        BEDRecord bedRecord = BEDUtil.makeBEDRecord(lastLsf, makeUcscCompatible);

        if (bedRecord != null) {
            // have a chromosome ref and chromosomeLocation ref
            if (!headerPrinted) {
                out.println(getHeader());
                headerPrinted = true;
            }

            out.println(bedRecord.toBED());
            exportedIds.add(lastLsf.getId());
            writtenResultsCount++;
        }
        lastLsfId = null;
    }

    @Override
    public int getWrittenResultsCount() {
        return writtenResultsCount;
    }

    private void finishLastRow() {
        BEDRecord bedRecord = BEDUtil.makeBEDRecord(lastLsf, makeUcscCompatible);

        if (bedRecord != null) {
            // have a chromsome ref and chromosomeLocation ref
            if (!headerPrinted) {
                out.println(getHeader());
                headerPrinted = true;
            }

            out.println(bedRecord.toBED());
            writtenResultsCount++;
        }
        lastLsfId = null;
    }

    @Override
    public boolean canExport(List<Class<?>> clazzes) {
        return canExportStatic(clazzes);
    }

    /**
     * @param clazzes classes of result row
     * @return true if this exporter can export result composed of specified classes
     */
    public static boolean canExportStatic(List<Class<?>> clazzes) {
        return ExportHelper.getClassIndex(clazzes, SequenceFeature.class) >= 0;
    }
}
