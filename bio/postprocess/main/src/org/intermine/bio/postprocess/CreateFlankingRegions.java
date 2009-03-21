package org.intermine.bio.postprocess;

/*
 * Copyright (C) 2002-2009 FlyMine
 *
 * This code may be freely distributed and modified under the
 * terms of the GNU Lesser General Public Licence.  This should
 * be distributed with the code.  See the LICENSE file for more
 * information or http://www.gnu.org/copyleft/lesser.html.
 *
 */
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.intermine.bio.util.BioQueries;
import org.intermine.model.bio.Chromosome;
import org.intermine.model.bio.DataSet;
import org.intermine.model.bio.DataSource;
import org.intermine.model.bio.Gene;
import org.intermine.model.bio.GeneFlankingRegion;
import org.intermine.model.bio.Location;
import org.intermine.objectstore.ObjectStore;
import org.intermine.objectstore.ObjectStoreException;
import org.intermine.objectstore.ObjectStoreWriter;
import org.intermine.objectstore.query.Results;
import org.intermine.objectstore.query.ResultsRow;
import org.intermine.util.DynamicUtil;

import org.apache.log4j.Logger;

/**
 * Create features to represent flanking regions of configurable distance either side of gene
 * featues.  These will be used in overlap queries.
 * @author rns
 *
 */
public class CreateFlankingRegions 
{
    private ObjectStoreWriter osw = null;
    private ObjectStore os;
    private DataSet dataSet;
    private DataSource dataSource;
    private Map<Integer, Chromosome> chrs = new HashMap<Integer, Chromosome>();

    private static final Logger LOG = Logger.getLogger(CreateFlankingRegions.class);
    
    /**
     * Create a new CreateFlankingRegions object that will operate on the given
     * ObjectStoreWriter.
     *
     * @param osw
     *            the ObjectStoreWriter to use when creating/changing objects
     */
    public CreateFlankingRegions(ObjectStoreWriter osw) {
        this.osw = osw;
        this.os = osw.getObjectStore();
        dataSource = (DataSource) DynamicUtil.createObject(Collections
                .singleton(DataSource.class));
        dataSource.setName("modMine");
        try {
            dataSource = (DataSource) os.getObjectByExample(dataSource,
                    Collections.singleton("name"));
        } catch (ObjectStoreException e) {
            throw new RuntimeException(
                    "unable to fetch modMine DataSource object", e);
        }
    }

    /**
     * Iterate over genes in database and create flanking regions.
     *
     * @throws ObjectStoreException
     *             if there is an ObjectStore problem
     */
    public void createFlankingFeatures() throws ObjectStoreException {
        Results results = BioQueries.findLocationAndObjects(os,
                Chromosome.class, Gene.class, false, false, 500);

        dataSet = (DataSet) DynamicUtil.createObject(Collections
                .singleton(DataSet.class));
        dataSet.setTitle("modMine gene flanking regions");
        dataSet.setDescription("Gene flanking regions generated by modMine");
        dataSet.setVersion("" + new Date()); // current time and date
        dataSet.setUrl("http://intermine.modencode.org");
        dataSet.setDataSource(dataSource);

        Iterator resIter = results.iterator();

        int count = 0;

        osw.beginTransaction();
        while (resIter.hasNext()) {
            ResultsRow rr = (ResultsRow) resIter.next();
            Integer chrId = (Integer) rr.get(0);
            Gene gene = (Gene) rr.get(1);
            Location loc = (Location) rr.get(2);
            createAndStoreFlankingRegion(getChromosome(chrId), loc, gene);
            if ((count % 1000) == 0) {
                LOG.info("Created flankning regions for " + count + " genes.");
            }
            count++;
        }
        osw.store(dataSet);

        osw.commitTransaction();
    }

    
    private void createAndStoreFlankingRegion(Chromosome chr, Location geneLoc, Gene gene) 
    throws ObjectStoreException {
        
        int[] distances = new int[] {1, 2, 5, 10};
        String[] directions = new String[] {"upstream", "downstream"};
        
        
        for (int distance : distances) {
            for (String direction : directions) {
                String strand = geneLoc.getStrand();
                
                // TODO what do we do if strand not set?
                int geneStart = geneLoc.getStart().intValue();
                int geneEnd = geneLoc.getEnd().intValue();
                
                GeneFlankingRegion region = (GeneFlankingRegion) DynamicUtil
                .createObject(Collections.singleton(GeneFlankingRegion.class));
                Location location = (Location) DynamicUtil
                .createObject(Collections.singleton(Location.class));

                region.setDistance("" + distance + "kb");
                region.setDirection(direction);
                region.setGene(gene);
                region.setChromosome(chr);
                region.setChromosomeLocation(location);
                region.setOrganism(gene.getOrganism());
                region.setPrimaryIdentifier(gene.getPrimaryIdentifier() + " " + distance + " " 
                        + direction);  

                // this should be some clever algorithm
                int start, end;
                
                if (direction.equals("upstream") && strand.equals("1")) {
                    start = geneStart - (distance * 1000);
                    end = geneStart - 1;
                } else if (direction.equals("upstream") && strand.equals("-1")) {
                    start = geneEnd + 1;
                    end = geneEnd + (distance * 1000);
                } else if (direction.equals("downstream") && strand.equals("1")) {
                    start = geneEnd + 1;
                    end = geneEnd + (distance * 1000);
                } else {  // direction.equals("downstream") && strand.equals("-1")
                    start = geneStart - (distance * 1000);
                    end = geneStart - 1;
                }
                
                // if the region hangs off the start or end of a chromosome set it to finish
                // at the end of the chromosome
                location.setStart(Math.max(start, 1));
                location.setEnd(Math.min(end, chr.getLength()));
                
                location.setStrand(strand);
                location.setObject(chr);
                location.setSubject(region);

                region.setLength(new Integer((location.getEnd() - location.getStart()) + 1));
                
                osw.store(location);
                osw.store(region);
            }
        }
    }
    
    private Chromosome getChromosome(Integer chrId) throws ObjectStoreException {
        Chromosome chr = chrs.get(chrId);
        if (chr == null) {
            chr = (Chromosome) os.getObjectById(chrId, Chromosome.class);
            chrs.put(chrId, chr);
        }
        return chr;
    }
}
