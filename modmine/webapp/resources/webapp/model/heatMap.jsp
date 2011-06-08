<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<!-- heatMap.jsp -->

<html:xhtml />

<tiles:importAttribute />

<!--[if IE]><script type="text/javascript" src="model/canvasXpress/js/excanvas.js"></script><![endif]-->
    <script type="text/javascript" src="model/canvasXpress/js/canvasXpress.min.js"></script>

<div class="body" id="expression_div">
    <div id="heatmap_div">
        <p><h2>${ExpressionScoreTitle}</h2></p>
        <p><i>${ExpressionScoreSummary}<a href="/${WEB_PROPERTIES['webapp.path']}/experiment.do?experiment=Drosophila Cell Line and Developmental Stage Gene and Exon Scores">
            the Celniker group</a>.</i></p>
        <br/>
        <div id="heatmapContainer">
            <table>
              <tr>
                <td>
                    <div style="padding: 0px 0px 5px 30px;">
                     <span>Cell Line Clustering - Hierarchical:</span>
                     <select id="cl-hc">
                         <option value="single" selected="selected">Single</option>
                         <option value="complete">Complete</option>
                         <option value="average">Average</option>
                     </select>
                     <span> and K-means:</span>
                     <select id="cl-km">
                         <option value="3" selected="selected">3</option>
                     </select>
                    </div>
                    <canvas id="canvas_cl" width="525" height="550"></canvas>
                </td>
                <td>
                     <div style="padding: 0px 0px 5px 25px;">
                     <span>Developmental Stage Clustering - Hierarchical:</span>
                     <select id="ds-hc">
                         <option value="single" selected="selected">Single</option>
                         <option value="complete">Complete</option>
                         <option value="average">Average</option>
                     </select>
                     <span> and K-means:</span>
                     <select id="ds-km">
                         <option value="3" selected="selected">3</option>
                     </select>
                    </div>
                     <canvas id="canvas_ds" width="550" height="550"></canvas>
                </td>
              </tr>
            </table>
        </div>
        <div id="description_div">
            <table border="0">
                <tr>
                    <td ><h3 style="font-weight: bold; background: black; color: white;">More Information</h3></td>
                    <td ><h3 style="background: white;"><img src="images/disclosed.gif" id="co"></h3></td>
                </tr>
            </table>
        </div>
        <div id="description" style="padding: 5px">
            <i>${ExpressionScoreDescription}</i>To see <a href="/${WEB_PROPERTIES['webapp.path']}/portal.do?class=Submission&externalids=${expressionScoreDCCid}">
            further information about the submission</a> and <a href="http://www.modencode.org/docs/flyscores/" target="_blank">original score tables</a>.
        </div>
    </div>
</div>

<script type="text/javascript">

    if ('${expressionScoreDCCid}'=='') {
        jQuery('#heatmap_div').remove();
        jQuery('#expression_div').html('<i>Expression scores are not available</i>');
     } else {
         jQuery("#description").hide();

         jQuery("#description_div").click(function () {
               if(jQuery("#description").is(":hidden")) {
                 jQuery("#co").attr("src", "images/disclosed.gif");
               } else {
                 jQuery("#co").attr("src", "images/undisclosed.gif");
               }
               jQuery("#description").toggle("slow");
            });

           var feature_count = parseInt(${FeatureCount});

            // hm - heatmap; cl - cellline; ds - developmentalstage; hc - hierarchical clustering; km - kmeans
            var hm_cl = new CanvasXpress('canvas_cl',
                                         ${expressionScoreJSONCellLine},
                                         {graphType: 'Heatmap',
                                          title: 'Cell Line',
                                          dendrogramSpace: 6,
                                          smpDendrogramPosition: 'right',
                                          varDendrogramPosition: 'bottom',
                                          setMin: ${minExpressionScore},
                                          setMax: ${maxExpressionScore},
                                          varLabelRotate: 45,
                                          centerData: false,
                                          autoExtend: false},
                                          {click: function(o) {/*basiclly disable click function*/}}
                                         );

            if (feature_count > 3) {
                hm_cl.clusterSamples();
                hm_cl.kmeansSamples();

                for (var i=4; i < feature_count; ++i) {
                    jQuery('#cl-km').
                              append(jQuery("<option></option>").
                              attr("value",i).
                              text(i));
                }

            } else {
                jQuery("#cl-km").attr('disabled', 'disabled');
            }

            hm_cl.clusterVariables();
            // cx_cellline.kmeansVariables();
            hm_cl.draw();

            var hm_ds = new CanvasXpress('canvas_ds',
                                         ${expressionScoreJSONDevelopmentalStage},
                                         {graphType: 'Heatmap',
                                          title: 'Developmental Stage',
                                          dendrogramSpace: 6,
                                          smpDendrogramPosition: 'right',
                                          setMin: ${minExpressionScore},
                                          setMax: ${maxExpressionScore},
                                          varLabelRotate: 45,
                                          centerData: false,
                                          autoExtend: false},
                                          {click: function(o) {}}
                                         );

            if (feature_count > 3) {
                hm_ds.clusterSamples();
                hm_ds.kmeansSamples();

                for (var i=4; i < feature_count; ++i) {
                    jQuery('#ds-km').
                              append(jQuery("<option></option>").
                              attr("value",i).
                              text(i));
                }

            } else {
                jQuery("#ds-hc").attr('disabled', 'disabled');
                jQuery("#ds-km").attr('disabled', 'disabled');
            }

            hm_ds.draw();

           jQuery('#cl-hc').change(function() {
                hm_cl.linkage = this.value;
                if (feature_count >= 3) { hm_cl.clusterSamples(); }
                hm_cl.clusterVariables();
                hm_cl.draw();
           });

           jQuery('#cl-km').change(function() {
                hm_cl.kmeansClusters = parseInt(this.value);
                hm_cl.kmeansSamples();
                // hm_cl.kmeansVariables();
                hm_cl.draw();
           });

           jQuery('#ds-hc').change(function() {
                hm_ds.linkage = this.value;
                hm_ds.clusterSamples();
                hm_ds.draw();
            });

            jQuery('#ds-km').change(function() {
                hm_ds.kmeansClusters = parseInt(this.value);
                hm_ds.kmeansSamples();
                hm_ds.draw();
           });

     }

    </script>

<!-- /heatMap.jsp -->