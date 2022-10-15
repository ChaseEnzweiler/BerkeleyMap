import java.util.HashMap;
import java.util.Map;

/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {

    /**
     * The root upper left/lower right longitudes and latitudes represent the bounding box of
     * the root tile, as the images in the img/ folder are scraped.
     * Longitude == x-axis; latitude == y-axis.
     */
    private static final double ROOT_ULLAT = 37.892195547244356, ROOT_ULLON = -122.2998046875,
            ROOT_LRLAT = 37.82280243352756, ROOT_LRLON = -122.2119140625;


    /**
     * Takes a user query and finds the grid of images that best matches the query. These
     * images will be combined into one big image (rastered) by the front end. <br>
     *
     *     The grid of images must obey the following properties, where image in the
     *     grid is referred to as a "tile".
     *     <ul>
     *         <li>The tiles collected must cover the most longitudinal distance per pixel
     *         (LonDPP) possible, while still covering less than or equal to the amount of
     *         longitudinal distance per pixel in the query box for the user viewport size. </li>
     *         <li>Contains all tiles that intersect the query bounding box that fulfill the
     *         above condition.</li>
     *         <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     *     </ul>
     *
     * @param params Map of the HTTP GET request's query parameters - the query box and
     *               the user viewport width and height.
     *
     * @return A map of results for the front end as specified: <br>
     * "render_grid"   : String[][], the files to display. <br>
     * "raster_ul_lon" : Number, the bounding upper left longitude of the rastered image. <br>
     * "raster_ul_lat" : Number, the bounding upper left latitude of the rastered image. <br>
     * "raster_lr_lon" : Number, the bounding lower right longitude of the rastered image. <br>
     * "raster_lr_lat" : Number, the bounding lower right latitude of the rastered image. <br>
     * "depth"         : Number, the depth of the nodes of the rastered image <br>
     * "query_success" : Boolean, whether the query was able to successfully complete; don't
     *                    forget to set this to true on success! <br>
     */
    public Map<String, Object> getMapRaster(Map<String, Double> params) {
        // System.out.println(params);
        Map<String, Object> results = new HashMap<>();

        double queryLRLon = params.get("lrlon") ;
        double queryLRLat = params.get("lrlat");
        double queryULLon = params.get("ullon");
        double queryULLat = params.get("ullat");
        double queryWidth = params.get("w");
        double queryHeight = params.get("h");

        /*
        check if it is a reasonable query
         */

        boolean querySuccess = calculateQuerySuccess(queryULLon, queryULLat, queryLRLon, queryLRLat);

        if(querySuccess == false){

            /*
            populate and return hashmap with arbitrary values
             */

            results.put("render_grid",  new String[1][1]);

            results.put("raster_ul_lon", 1.1);

            results.put("raster_ul_lat", 1.1);

            results.put("raster_lr_lon", 1.1);

            results.put("raster_lr_lat", 1.1);

            results.put("depth", 0);

            results.put("query_success", querySuccess);

            return results;

        }

        /*
        find the depth of the images
         */
        int depth = findDepth(calculateLondpp(queryLRLon, queryULLon, (int) queryWidth));

        /*
        find starting and ending tile coordinates that will make up raster box
         */
        int[] rasterStartTile = findStartingTile(queryULLon, queryULLat, depth);
        int[]  rasterEndTile = findEndingTile(queryLRLon, queryLRLat, depth);

        /*
        get the string names of image files
         */

        String[][] renderGrid = getImages(rasterStartTile, rasterEndTile, depth);

        /*
        calculate raster boundary coordinates
         */

        double[] startRasterBoundary = upperLeftRasterBox(rasterStartTile, depth);
        double[] endRasterBoundary = lowerRightRasterBox(rasterEndTile, depth);

        double raster_ul_lon = startRasterBoundary[0];
        double raster_ul_lat = startRasterBoundary[1];
        double raster_lr_lon = endRasterBoundary[0];
        double raster_lr_lat = endRasterBoundary[1];

        results.put("render_grid",  renderGrid);

        results.put("raster_ul_lon", raster_ul_lon);

        results.put("raster_ul_lat", raster_ul_lat);

        results.put("raster_lr_lon", raster_lr_lon);

        results.put("raster_lr_lat", raster_lr_lat);

        results.put("depth", depth);

        results.put("query_success", querySuccess);


        //System.out.println("Since you haven't implemented getMapRaster, nothing is displayed in "
        //                    + "your browser.");
        return results;
    }



    /**
     * takes coordinates and width of a query and returns the calculation for the LonDpp
     *
     * @param lrLon Lower right longitude of query box
     * @param ulLon Upper left longitude of the query box
     * @param width resolution of the query box
     * @return a Double representing the LonDpp of the query box aka feet per pixel
     */

    public static double calculateLondpp(double lrLon, double ulLon, int width){

        return (lrLon - ulLon) / width;

    }

    /**
     * Returns the required depth level given the londpp of the query box
     *
     * @param Londpp londpp of the user requested query box
     * @return the depth of the images needed for rastering
     */

    public static int findDepth(double Londpp){

        double mapLondpp = calculateLondpp(-122.21191406, -122.29980468, 256);
        int depth = 0;
        while(Londpp < mapLondpp){
            mapLondpp = mapLondpp / 2;
            depth = depth + 1;
            if (depth >= 7){
                return depth;
            }
        }
        return depth;
    }

    /**NEEDS DEBUGGING getting too many
     * finds the upper left tile of the rastered image, returns coordinates of tile as an array
     *
     * @param queryUlLon upper left longitude of the query box
     * @param queryUlLat upper left longitude of the query box
     * @param depth depth of the requested rastered images
     * @return array of the x and y coordinates of the upper left tile of rastered image
     */

    //lon width = 0.005493164063
    //lat width = 0.004337069607

    public static int[] findStartingTile(double queryUlLon, double queryUlLat, int depth){

        double widthPerTileLon = (ROOT_LRLON - ROOT_ULLON) / Math.pow(2, depth);
        double widthPerTileLat = (ROOT_ULLAT - ROOT_LRLAT) / Math.pow(2, depth);
        int startTileLon;
        int startTileLat;
        if(queryUlLon <= ROOT_ULLON){
            startTileLon = 0;
        }else {
            startTileLon = (int) Math.floor((queryUlLon - ROOT_ULLON) / widthPerTileLon);
        }
        if(queryUlLat >= ROOT_ULLAT){
            startTileLat = 0;
        }else {
            startTileLat = (int) Math.floor((ROOT_ULLAT - queryUlLat) / widthPerTileLat);
        }

        int[] startArray = new int[] {startTileLon, startTileLat};
        return startArray;

    }

    /** NEEDS DEBUGGING getting too many
     * finds the lower tight tile of the image to be rastered
     *
     * @param queryLrLon double, lower right longitude of the query box
     * @param queryLrLat double, lower right latitude of the query box
     * @param depth int, depth of the images
     * @return int[], of the coordinates of the lower right tile of the rastered image
     */

    public static int[] findEndingTile(double queryLrLon, double queryLrLat, int depth){

        double widthPerTileLon = (ROOT_LRLON - ROOT_ULLON) / Math.pow(2, depth);
        double widthPerTileLat = (ROOT_ULLAT - ROOT_LRLAT) / Math.pow(2, depth);
        int endTileLon;
        int endTileLat;
        if(queryLrLon >= ROOT_LRLON){
            endTileLon = (int) Math.pow(2, depth) - 1;
        }else {
            endTileLon = (int) Math.floor((queryLrLon - ROOT_ULLON) / widthPerTileLon);
        }
        if(queryLrLat <= ROOT_LRLAT){
            endTileLat = (int) Math.pow(2, depth) - 1;
        } else{
            endTileLat = (int) Math.floor((ROOT_ULLAT - queryLrLat) / widthPerTileLat);
        }
        int[] endArray = new int[] {endTileLon, endTileLat};
        return endArray;
    }


    /**
     * Creates a 2 dimensional String array containing the file names of the images to be rastered. Images returned in
     * order of upper left to lower right by row.
     *
     * @param startArray int[], coordinates of the upper left tile of the rastered image
     * @param endArray int[], coordinates of the lower right tile of the rastered image
     * @param depth int, image depth required for rastered image
     * @return String[][], strings of names of image files to raster
     */

    public static String[][] getImages(int[] startArray, int[] endArray, int depth){

        int tileX1 = startArray[0];
        int tileX2 = endArray[0];
        int tileY1 = startArray[1];
        int tileY2 = endArray[1];
        String[][] imageGrid = new String[tileY2 - tileY1 + 1][tileX2 - tileX1 + 1];
        tileY1 = tileY1 - 1;
        for(int i = 0; i < imageGrid.length; i++){
            tileY1 = tileY1 + 1;
            tileX1 = startArray[0];
            for(int j = 0; j < imageGrid[0].length; j++){
                imageGrid[i][j] = "d" + depth + "_x" + tileX1 + "_y" + tileY1 + ".png";
                tileX1 = tileX1 + 1;
            }
        }
        return imageGrid;
    }

    /**
     *
     * calculates the latitude and longitude of the upper left boundary of the rastered complete image
     *
     * @param startTile int[],  [lon, lat] of upper left tile of the images to be rastered
     * @param depth int, depth of the images to be rastered
     * @return double[], returns an array of the longitude and latitude of the rastered image boundary
     */

    public static double[] upperLeftRasterBox(int[] startTile, int depth){

        double widthPerTile = (ROOT_LRLON - ROOT_ULLON) / Math.pow(2, depth);
        double rasterImageULLon =  ROOT_ULLON + (widthPerTile * (startTile[0]));
        double heightPerTile = (ROOT_ULLAT - ROOT_LRLAT) / Math.pow(2, depth);
        double rasterImageULLat = ROOT_ULLAT - (heightPerTile * startTile[1]);
        return new double[]{rasterImageULLon, rasterImageULLat};

    }

    /**
     *
     * calculates the latitude and longitude of the lower right boundary of the rastered complete image
     *
     * @param endTile int[], longitude and latitude of lower right tile of raster images
     * @param depth int, depth of the images to be rastered
     * @return double[], returns array with lon and lat of lower right boundary of raster box
     */

    public static double[] lowerRightRasterBox(int[] endTile, int depth){

        double widthPerTile = (ROOT_LRLON - ROOT_ULLON) / Math.pow(2, depth);
        double rasterImageLRLon = ROOT_ULLON + (widthPerTile * (endTile[0] + 1));
        double heightPerTile = (ROOT_ULLAT - ROOT_LRLAT) / Math.pow(2, depth);
        double rasterImageLRLat = ROOT_ULLAT - (heightPerTile * (endTile[1] + 1));
        return new double[]{rasterImageLRLon, rasterImageLRLat};


    }

    /**
     *
     * @param ULLon double, upper left longitude of query box
     * @param ULLat double, upper left latitude of query box
     * @param LRLon double, lower right longitude of query box
     * @param LRLat double, lower right latitude of query box
     * @return boolean, whether its a successful query
     */

    public static boolean calculateQuerySuccess(double ULLon, double ULLat, double LRLon, double LRLat){

        /*
        bad query, aka numbers that make no sense
         */
        if(LRLon <= ULLon || ULLat <= LRLat){
            return false;
        }

        /*
        bad query, query box intersects no images
         */
        if(LRLat >= ROOT_ULLAT || ULLat <= ROOT_LRLAT){
            return false;
        } else if(ULLon >= ROOT_LRLON || LRLon <= ROOT_ULLON){
            return false;
        }
        return true;
    }
}
