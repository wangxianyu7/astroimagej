package Astronomy;// Set_Aperture.java

import astroj.Photometer;
import ij.I18n;
import ij.IJ;
import ij.Prefs;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import java.awt.*;

/**
 * Setup plug-in for Aperture_ which sets the following characteristics:
 *	- aperture radii (object=AP_PREFS_RADIUS and background=AP_PREFS_RBACK1,2)
 *	- output to image-specific table or not (AP_PREFS_ONETABLE)
 *	- show centroid position (AP_PREFS_SHOWPOSITION)
 *	- show photometry (AP_PREFS_SHOWPHOTOMETRY)
 *	- show x- and y-widths (AP_PREFS_SHOWWIDTHS)
 *	- show mean width (AP_PREFS_SHOWMEANWIDTH)
 *	- show aperture radii (AP_PREFS_SHOWRADII)
 *	- show JD times (AP_PREFS_SHOWTIMES)
 *	- show object orientation angle (AP_PREFS_SHOWANGLE)
 *	- show object roundedness (AP_PREFS_SHOWROUNDNESS)
 *	- show object roundedness (AP_PREFS_SHOWVARIANCE)
 *	- show raw photometry (AP_PREFS_SHOWRAW)
 *	- use non-constant background (AP_PREFS_CONST_BACK)
 *	- display aperture radii in overlay (AP_PREFS_SHOWSKYANNULAE)
 *	- set CCD parameters gain [e-/ADU] and RON [e-]
 *	- show photometric errors (AP_PREFS_SHOWERRORS), do/don't reposition aperture (AP_PREFS_REPOSITION)
 *	- show RA, DEC (AP_PREFS_SHOWRADEC)
 *
 * @author F.V. Hessman, Georg-August-Universitaet Goettingen
 * @version 1.0
 * @date 2006-Mar-07
 *
 * @version 1.1
 * @date 2006-May-02
 * @changes Added support or planar background.
 *
 * @version 1.2
 * @date 2006-Dec-12
 * @changes Added AP_PREFS_STAROVERLAY & SKYOVERLAY support.
 *
 * @version 1.3
 * @date 2006-Dec-20
 * @changes Added orientation angle and roundness support from Centroid
 *
 * @version 1.4
 * @date 2007-Jan-29
 * @changes Added CCD parameters, showing errors, do/don't  reposition, FITS keyword display.
 *
 * @version 1.5
 * @date 2007-Aug-21
 * @changes Added possibility of making ROIs disappear after use (to prevent future
 *	manipulations from occuring only within the ROI).
 *
 * @ version 1.6
 * @date 2008-Feb-07
 * @changes Added showRADEC.
 *
 * @ version 1.7
 * @date 2008-Jul-03
 * @changes Panel was too long for small screens so packed little-used features into a separate configuration panel.
 *
 * @ version 1.8
 * @date 2009-01-10
 * @changes Support retry option.
 *
 * @version 1.9
 * @date 2010-03-18
 * @author Karen Collins (Univ. Louisville)
 * @changes 
 * 1) Reversed the order of display of the "Aperture Photometry Parameters" * and "Other Photometry
 * 	Parameters" panels. "The Aperture Photometry * Parameters" panel now displays first.
 * 2) Added a check box option to list an aperture peak value column in the measurements table.
 * 3) Added a check box option to list a saturation warning column in the measurements table.
 *	This option displays the peak value that exceeds a defined limit. Otherwise a "0" is listed
 *	in the column. This helps in quickly identifying overly saturated images.
 * 4) Added a numeric entry box for the "Saturation warning level".
 * 5) Added an option to list the error in the "ratio" for multi-aperture differential photometry.
 *	This column is only added to the table if both ratio calculation is selected in multi-aperture
 *	and the "error in the ratio" option is selected here.
 * 6) Added an option to list the multi-aperture ratio SNR. Again, this column only gets added to the
 *	table if both ratio calculation is selected in multi-aperture and the "ratio SNR" option is selected here.
 * 7) Added a "CCD dark current" numeric entry box that is used in the enhanced source error calculation.
 *
 * @version 1.10
 * @dave 2010-03-18
 * @author FVH
 * @changes Slight modification: put some specialized entries in the "Other Photometry Parameters" panel.
 *
 * @version 1.11
 * @date 2010-Nov-24
 * @author Karen Collins (Univ. Louisvill/KY)
 * @changes Added support for removal of stars from background region (>3 sigma from mean)
 */
public class Set_Aperture implements PlugIn
	{
	double radius=25.0;
	double rBack1=40.0;
	double rBack2 = 60.0;
    double oldradius;
    double oldrBack1;
    double oldrBack2;
    boolean oldUseVarSizeAp;
    double oldapFWHMFactor = 1.4;
	double saturationWarningLevel = 55000.0;
    double linearityWarningLevel = 30000.0;
	double gain = 1.0;
	double noise = 0.0;
	double dark = 0.0;
    double apFWHMFactor = 1.4;
    double autoModeFluxCutOff = 0.010;
    double oldAutoModeFluxCutOff = 0.010;
	String darkKeyword = new String("");
    int nAperturesMax = 1000;

	boolean finished = false;

//	boolean oneTable=true;
//	boolean wideTable = true;

	boolean backPlane  = false;
	boolean reposition = true;
	boolean forgiving  = true;			// STOP IF ERROR
//	boolean retry      = false;
	boolean removeBackStars = true;			// REMOVE STARS > 2 SIGMA FROM MEAN FROM BACKGROUND CALCULATION
    boolean showRemovedPixels = false;      // MARK PIXELS > 2 SIGMA FROM MEAN WITH AN OVERLAY DOT
    boolean exact = true;                // USE EXACT PARTIAL PIXEL ACCOUNTING IN SOURCE APERTURE

    boolean showFileName = true;        //LIST THE FILENAME AS THE ROW LABEL
    boolean showSliceNumber = true;     // LIST THE SLICE NUMBER
	boolean showPosition=true;			// LIST MEASURED CENTROID POSITION in IJ COORDINATES
    boolean showPositionFITS=true;			// LIST MEASURED CENTROID POSITION in FITS COORDINATES
	boolean showPhotometry=true;			// LIST MEASURED APERTURE BRIGHTNESS MINUS BACKGROUND
    boolean showNAperPixels= true;          // LIST THE NUMBER OF PIXELS COUNTED IN THE SOURCE APERTURE
    boolean showBack=true;                  // LIST MEASURED SKY BRIGHTNESS PER PIXEL
    boolean showNBackPixels=true;           // LIST THE NUMBER OF PIXELS COUNTED IN THE SKY BACKGROUND ANNULUS
	boolean showSaturationWarning=true;		// LIST and SHOW WARNING IF PEAK IS OVER SATURATION LIMIT
    boolean showLinearityWarning=true;		// SHOW WARNING IF PEAK IS OVER LINEARITY LIMIT
	boolean showPeak=true;				// LIST THE PEAK VALUE WITHIN THE SOURCE APERTURE
    boolean showMean=true;				// LIST THE MEAN VALUE WITHIN THE SOURCE APERTURE
	boolean showWidths=true;			// LIST MEASURED MOMENT WIDTHS
	boolean showMeanWidth=true;			// LIST MEAN OF MEASURE MOMENT WIDTHS
	boolean showRadii=true;			// LIST APERTURE RADII USED
	boolean showTimes=true;			// LIST JD IF AVAILABLE
	boolean showRaw = false;			// LIST RAW PHOTOMETRY NUMBERS
	boolean showAngle = true;			// LIST ORIENTATION ANGLE
	boolean showRoundness = true;			// LIST ROUNDEDNESS
	boolean showVariance = false;			// LIST VARIANCE
	boolean showErrors = true;			// LIST SOURCE STAR COUNT ERROR
	boolean showSNR = true;			// LIST SOURCE STAR SIGNAL-TO-NOISE
    boolean useVarSizeAp = false;       // VARY APERTURE SIZE IN MULTI-APERTURE DEPENDING ON FWHM
    boolean useHowellCentroidMethod = true;   //USE CENTROID METHOD FROM Howell, CCD Astronomy, 2nd Ed., p. 105, instead of the original
    boolean enableDoubleClicks = true;   //Allow left/right double click for fast zoom-in/out in Multi-Aperture (adds slight delay to aperture placement)
    boolean alwaysstartatfirstSlice = true;   //Always start Multi-Aperture at first slice in stack 
    boolean unused = true;
    
	boolean showFits = true;			// LIST FITS VALUE READ FROM HEADER (e.g. FOCUS)
    boolean getMags = false;
    boolean oldGetMags = false;
	String fitsKeywords = "JD_SOBS,JD_UTC,HJD_UTC,BJD_TDB,AIRMASS,ALT_OBJ,CCD-TEMP,EXPTIME,RAOBJ2K,DECOBJ2K";		// default FITS keywords for which data is added to measurements table

	boolean showRADEC = true;			// LIST RA, DEC IF PRESENT IN WCS

	boolean starOverlay = true;			// SHOW STAR ANNULUS IN OVERLAY
	boolean skyOverlay = true;			// SHOW SKY ANNULAE IN OVERLAY
    boolean oldSkyOverlay;
    boolean nameOverlay = true;	    // SHOW SOURCE NUMBER AS LABEL IN OVERLAY
	boolean valueOverlay = true;		// SHOW VALUE AS LABEL IN OVERLAY
	boolean tempOverlay = true;			// OVERLAY IS TEMPORARY
	boolean clearOverlay = false;		// CLEAR OVERLAY BEFORE MEASUREMENT

	// MultiAperture_ PREFERENCES

	// boolean follow = false;				// TRY TO GUESS THE TRAJECTORY
	boolean showRatio = true;			// LIST THE RATIO OF FIRST TO OTHER STARS
    boolean showCompTot = true;            //LIST TOTAL COMPARISON STAR COUNTS
	boolean showRatioError = true;			// LIST THE RATIO ERROR IN MULTI-APERTURE MODE
	boolean showRatioSNR = true;			// LIST THE RATIO SIGNAL TO NOISE RATIO
//	boolean autoMode =false;			// SETS MULTI-APERTURE AUTO MODE FOR USE WITH DIRECTORY WATCHER

	// Set_Aperture PREFERENCES

//	boolean showOtherPanel = true;
    boolean apertureChanged = false;    // LETS OTHER PLUGINS KNOW THE APERTURE SIZE HAS CHANGED
    boolean startedFromMA = false;

	/**
	 * Standard ImageJ PluginFilter setup routine which also determines the default aperture radius.
	 */
	public void run (String arg)
		{
        if (arg.equals("from_MA")) startedFromMA = true;
        
        getPrefs();
		
		if (mainPanel())
            {
            savePrefs();
            }
        if (startedFromMA) 
            {
            EventQueue.invokeLater(() -> IJ.runPlugIn("Astronomy.MultiAperture_", ""));
            }
		}

	public boolean mainPanel ()
		{
		GenericDialog gd = new GenericDialog (I18n.t("apset.title"));

        gd.addFloatSlider(I18n.t("apset.radius_obj"), 1, radius>100?radius:100, radius, 3, 1);
        gd.addFloatSlider(I18n.t("apset.radius_inner_bg"), 1, rBack1>100?rBack1:100, rBack1, 3, 1);
        gd.addFloatSlider(I18n.t("apset.radius_outer_bg"), 1, rBack2>100?rBack2:100, rBack2, 3, 1);
        gd.addCheckbox (I18n.t("apset.use_variable"), useVarSizeAp);
        gd.addFloatSlider(I18n.t("apset.fwhm_factor"), 0.0, 5.1, apFWHMFactor, 3, 0.1);
        gd.addNumericField(I18n.t("apset.radial_cutoff"),  autoModeFluxCutOff, 3, 6, I18n.t("apset.cutoff_hint"));
        gd.addCheckboxGroup(1, 5, new String[]{I18n.t("apset.cb_centroid"),I18n.t("apset.cb_howell"),I18n.t("apset.cb_bg_plane"),I18n.t("apset.cb_remove_stars"),I18n.t("apset.cb_mark_removed")},
                                  new boolean[]{reposition,useHowellCentroidMethod,backPlane,removeBackStars,showRemovedPixels});
        gd.addCheckbox (I18n.t("apset.exact_partial"), exact);
        gd.addCheckbox(I18n.t("apset.parallel"), Photometer.USE_PARALLEL_PIXEL_PROCESS.get());
        gd.addCheckbox (I18n.t("apset.prompt_refstar"), getMags);
        gd.addCheckbox (I18n.t("apset.list_fits_kw"), showFits);
		gd.addStringField (I18n.t("apset.keywords"),fitsKeywords,80);
		gd.addNumericField (I18n.t("apset.ccd_gain"), gain, 6, 10, I18n.t("apset.unit_e_count"));
		gd.addNumericField (I18n.t("apset.ccd_ron"), noise, 6, 10, I18n.t("apset.unit_e"));
		gd.addNumericField (I18n.t("apset.ccd_dark"), dark, 6, 10, I18n.t("apset.unit_e_pix_sec"));
		gd.addStringField (I18n.t("apset.dark_kw"), darkKeyword);
		gd.addCheckbox (I18n.t("apset.sat_warning"), showSaturationWarning);
		gd.addNumericField (I18n.t("apset.warning_above"), saturationWarningLevel,0);
		gd.addCheckbox (I18n.t("apset.lin_warning"), showLinearityWarning);
		gd.addNumericField (I18n.t("apset.warning_above"), linearityWarningLevel,0);

        gd.enableYesNoCancel(I18n.t("apset.btn_ok"), I18n.t("apset.btn_more"));

		gd.showDialog();
		if (gd.wasCanceled())
            {
                gd.dispose();
            return false;
            }

		radius = gd.getNextNumber();
        if (radius <= 0) radius = oldradius;
        if (oldradius != radius)
            apertureChanged = true;
        oldradius = radius;
		rBack1 = gd.getNextNumber();
        if (rBack1 <= 0) rBack1 = oldrBack1;
        if (oldrBack1 != rBack1)
            apertureChanged = true;
        oldrBack1 = rBack1;
		rBack2 = gd.getNextNumber();
        if (rBack2 <= 0) rBack2 = oldrBack2;
        if (oldrBack2 != rBack2)
            apertureChanged = true;
        oldrBack2 = rBack2;
        useVarSizeAp = gd.getNextBoolean();
        if (oldUseVarSizeAp != useVarSizeAp)
            apertureChanged = true;
        oldUseVarSizeAp = useVarSizeAp;
		apFWHMFactor = gd.getNextNumber();
        if (apFWHMFactor < 0) apFWHMFactor = oldapFWHMFactor;
        if (oldapFWHMFactor != apFWHMFactor)
            apertureChanged = true; 
        oldapFWHMFactor = apFWHMFactor;
        autoModeFluxCutOff = gd.getNextNumber();
        if (autoModeFluxCutOff <= 0 || autoModeFluxCutOff >=1.0) autoModeFluxCutOff = oldAutoModeFluxCutOff;
        if (oldAutoModeFluxCutOff != autoModeFluxCutOff)
            apertureChanged = true; 
        oldAutoModeFluxCutOff = autoModeFluxCutOff;
        reposition = gd.getNextBoolean();
        useHowellCentroidMethod = gd.getNextBoolean();
		backPlane = gd.getNextBoolean();
        removeBackStars = gd.getNextBoolean();
        showRemovedPixels = gd.getNextBoolean();
        exact = gd.getNextBoolean();
        Photometer.USE_PARALLEL_PIXEL_PROCESS.set(gd.getNextBoolean());
        getMags = gd.getNextBoolean();
        if (oldGetMags != getMags)
            apertureChanged = true;        
		showFits = gd.getNextBoolean();
		fitsKeywords = gd.getNextString();
		gain = gd.getNextNumber();
		noise = gd.getNextNumber();
		dark = gd.getNextNumber();
		darkKeyword = gd.getNextString(); 
        
		showSaturationWarning = gd.getNextBoolean();
		saturationWarningLevel = gd.getNextNumber();
		showLinearityWarning = gd.getNextBoolean();
		linearityWarningLevel = gd.getNextNumber();         

        if (!gd.wasOKed()) return otherPanel();

        gd.dispose();
        return true;
		}
    
	public boolean otherPanel ()
		{
		GenericDialog gd = new GenericDialog (I18n.t("apset.more_title"));

		gd.addMessage (I18n.t("apset.select_single"));
        gd.addCheckboxGroup(5, 4, new String[]{I18n.t("apset.col_filename"), I18n.t("apset.col_slice"), I18n.t("apset.col_times"), I18n.t("apset.col_radec"),
                                               I18n.t("apset.col_fits_xy"), I18n.t("apset.col_ij_xy"), I18n.t("apset.col_radii"), I18n.t("apset.col_variance"),
                                               I18n.t("apset.col_source"), I18n.t("apset.col_peak"), I18n.t("apset.col_mean"), I18n.t("apset.col_sky"),
                                               I18n.t("apset.col_fwhm"), I18n.t("apset.col_widths"), I18n.t("apset.col_angle"), I18n.t("apset.col_round"),
                                               I18n.t("apset.col_src_err"), I18n.t("apset.col_src_snr"), I18n.t("apset.col_n_src"), I18n.t("apset.col_n_sky")},
                                  new boolean[]{showFileName, showSliceNumber, showTimes, showRADEC,
                                                showPositionFITS, showPosition, showRadii, showVariance,
                                                showPhotometry,showPeak,showMean,showBack,
                                                showMeanWidth,showWidths,showAngle,showRoundness,
                                                showErrors, showSNR, showNAperPixels, showNBackPixels});

        gd.addMessage (I18n.t("apset.select_multi"));
        gd.addCheckboxGroup(1, 4, new String[]{I18n.t("apset.col_rel_flux"), I18n.t("apset.col_rel_err"), I18n.t("apset.col_rel_snr"), I18n.t("apset.col_tot_c")},
                                  new boolean[]{showRatio, showRatioError, showRatioSNR, showCompTot});
		gd.addMessage (I18n.t("apset.note_warnings"));

        gd.addMessage (I18n.t("apset.ma_settings"));
        gd.addCheckbox (I18n.t("apset.allow_dblclick"), enableDoubleClicks);
        gd.addCheckbox (I18n.t("apset.always_first_slice"), alwaysstartatfirstSlice);
        gd.addNumericField (I18n.t("apset.max_apertures"), nAperturesMax,0,6,"");

		gd.addMessage (I18n.t("apset.select_overlay"));
        gd.addCheckboxGroup(1, 4, new String[]{I18n.t("apset.ovl_obj"), I18n.t("apset.ovl_sky"), I18n.t("apset.ovl_num"), I18n.t("apset.ovl_val")},
                                  new boolean[]{starOverlay, skyOverlay, nameOverlay, valueOverlay});
        gd.addCheckboxGroup(1, 2, new String[]{I18n.t("apset.ovl_clear_after"), I18n.t("apset.ovl_clear_before")},
                                  new boolean[]{tempOverlay, clearOverlay});

        gd.enableYesNoCancel(I18n.t("apset.btn_ok"), I18n.t("apset.btn_main"));

		gd.showDialog();
        
		if (gd.wasCanceled())
            {
                gd.dispose();
            return false;
            }
        
        showFileName = gd.getNextBoolean();
        showSliceNumber = gd.getNextBoolean();
        showTimes = gd.getNextBoolean();
        showRADEC = gd.getNextBoolean();
        showPositionFITS = gd.getNextBoolean();
		showPosition = gd.getNextBoolean();
        showRadii = gd.getNextBoolean();
        showVariance = gd.getNextBoolean();
		showPhotometry = gd.getNextBoolean();
        showPeak = gd.getNextBoolean();
        showMean = gd.getNextBoolean();
        showBack = gd.getNextBoolean();
        showMeanWidth = gd.getNextBoolean();
        showWidths = gd.getNextBoolean();
        showAngle = gd.getNextBoolean();
        showRoundness = gd.getNextBoolean();
		showErrors = gd.getNextBoolean();
        showSNR = gd.getNextBoolean();
        showNAperPixels = gd.getNextBoolean();
        showNBackPixels = gd.getNextBoolean();
        
        showRatio = gd.getNextBoolean();
		showRatioError = gd.getNextBoolean();
		showRatioSNR = gd.getNextBoolean();
        showCompTot = gd.getNextBoolean();
        
        enableDoubleClicks = gd.getNextBoolean();
        alwaysstartatfirstSlice = gd.getNextBoolean();
        nAperturesMax = (int)gd.getNextNumber();
        
		starOverlay = gd.getNextBoolean();
		skyOverlay = gd.getNextBoolean();
        if (oldSkyOverlay != skyOverlay)
            apertureChanged = true;
        nameOverlay = gd.getNextBoolean();
		valueOverlay = gd.getNextBoolean();
		tempOverlay = gd.getNextBoolean();
		clearOverlay = gd.getNextBoolean();

        if (!gd.wasOKed()) return mainPanel();

        gd.dispose();
        return true;
		}
    
    public void getPrefs()
        {
		radius     = Prefs.get (Aperture_.AP_PREFS_RADIUS, radius);
        oldradius = radius;
		rBack1     = Prefs.get (Aperture_.AP_PREFS_RBACK1, rBack1);
        oldrBack1 = rBack1;
		rBack2     = Prefs.get (Aperture_.AP_PREFS_RBACK2, rBack2);
        oldrBack2 = rBack2;
        useVarSizeAp  = Prefs.get (MultiAperture_.PREFS_USEVARSIZEAP, useVarSizeAp);
        oldUseVarSizeAp = useVarSizeAp;
        exact      = Prefs.get (Aperture_.AP_PREFS_EXACT, exact);
		gain       = Prefs.get (Aperture_.AP_PREFS_CCDGAIN, gain);
		noise      = Prefs.get (Aperture_.AP_PREFS_CCDNOISE, noise);
		dark       = Prefs.get (Aperture_.AP_PREFS_CCDDARK, dark);
		darkKeyword= Prefs.get (Aperture_.AP_PREFS_DARKKEYWORD, darkKeyword);
		showPeak   = Prefs.get (Aperture_.AP_PREFS_SHOWPEAK, showPeak);
        showMean   = Prefs.get (Aperture_.AP_PREFS_SHOWMEAN, showMean);

//		oneTable   = Prefs.get (Aperture_.AP_PREFS_ONETABLE, oneTable);

		backPlane  = Prefs.get (Aperture_.AP_PREFS_BACKPLANE, backPlane);
		reposition = Prefs.get (Aperture_.AP_PREFS_REPOSITION, reposition);
//		forgiving  = Prefs.get (Aperture_.AP_PREFS_FORGIVING, forgiving);
//		retry      = Prefs.get (Aperture_.AP_PREFS_RETRY, retry);
        removeBackStars = Prefs.get (Aperture_.AP_PREFS_REMOVEBACKSTARS, removeBackStars);
        showRemovedPixels = Prefs.get (Aperture_.AP_PREFS_SHOWREMOVEDPIXELS, showRemovedPixels);
        
		showFileName   = Prefs.get (Aperture_.AP_PREFS_SHOWFILENAME, showFileName);
        showSliceNumber= Prefs.get (Aperture_.AP_PREFS_SHOWSLICENUMBER, showSliceNumber);
        showPosition   = Prefs.get (Aperture_.AP_PREFS_SHOWPOSITION, showPosition);
        showPositionFITS   = Prefs.get (Aperture_.AP_PREFS_SHOWPOSITION_FITS, showPositionFITS);
		showPhotometry = Prefs.get (Aperture_.AP_PREFS_SHOWPHOTOMETRY, showPhotometry);
        showNAperPixels = Prefs.get (Aperture_.AP_PREFS_SHOWNAPERPIXELS, showNAperPixels);
        showNBackPixels = Prefs.get (Aperture_.AP_PREFS_SHOWNBACKPIXELS, showNBackPixels);
        showBack       = Prefs.get (Aperture_.AP_PREFS_SHOWBACK, showBack);
		showWidths     = Prefs.get (Aperture_.AP_PREFS_SHOWWIDTHS, showWidths);
		showMeanWidth  = Prefs.get (Aperture_.AP_PREFS_SHOWMEANWIDTH, showMeanWidth);
		showRadii      = Prefs.get (Aperture_.AP_PREFS_SHOWRADII, showRadii);
		showTimes      = Prefs.get (Aperture_.AP_PREFS_SHOWTIMES, showTimes);
		showRaw        = Prefs.get (Aperture_.AP_PREFS_SHOWRAW, showRaw);
		showAngle      = Prefs.get (Aperture_.AP_PREFS_SHOWANGLE, showAngle);
		showRoundness  = Prefs.get (Aperture_.AP_PREFS_SHOWROUNDNESS, showRoundness);
		showVariance   = Prefs.get (Aperture_.AP_PREFS_SHOWVARIANCE, showVariance);
		showErrors     = Prefs.get (Aperture_.AP_PREFS_SHOWERRORS, showErrors);
        showSNR        = Prefs.get (Aperture_.AP_PREFS_SHOWSNR, showSNR);
		showSaturationWarning  = Prefs.get (Aperture_.AP_PREFS_SHOWSATWARNING, showSaturationWarning);
		saturationWarningLevel = Prefs.get (Aperture_.AP_PREFS_SATWARNLEVEL, saturationWarningLevel);
		showLinearityWarning  = Prefs.get (Aperture_.AP_PREFS_SHOWLINWARNING, showLinearityWarning);
		linearityWarningLevel = Prefs.get (Aperture_.AP_PREFS_LINWARNLEVEL, linearityWarningLevel);        
        useHowellCentroidMethod = Prefs.get (Aperture_.AP_PREFS_USEHOWELL, useHowellCentroidMethod);
        
		showFits = Prefs.get (Aperture_.AP_PREFS_SHOWFITS, showFits);
        getMags = Prefs.get (MultiAperture_.PREFS_GETMAGS, getMags);
        oldGetMags = getMags;
		fitsKeywords = Prefs.get (Aperture_.AP_PREFS_FITSKEYWORDS, fitsKeywords);

		showRADEC = Prefs.get (Aperture_.AP_PREFS_SHOWRADEC, showRADEC);

		starOverlay = Prefs.get (Aperture_.AP_PREFS_STAROVERLAY, starOverlay);
		skyOverlay = Prefs.get (Aperture_.AP_PREFS_SKYOVERLAY, skyOverlay);
        oldSkyOverlay = skyOverlay;
        nameOverlay = Prefs.get (Aperture_.AP_PREFS_NAMEOVERLAY, nameOverlay);
		valueOverlay = Prefs.get (Aperture_.AP_PREFS_VALUEOVERLAY, valueOverlay);
		tempOverlay = Prefs.get (Aperture_.AP_PREFS_TEMPOVERLAY, tempOverlay);
		clearOverlay = Prefs.get (Aperture_.AP_PREFS_CLEAROVERLAY, clearOverlay);

//		showOtherPanel = Prefs.get("setaperture.showother", showOtherPanel);
        apertureChanged = Prefs.get("setaperture.aperturechanged", apertureChanged);

		// MultiAperture_ PREFERENCES

		// follow         = Prefs.get (MultiAperture_.PREFS_FOLLOW, follow);
//		wideTable      = Prefs.get (MultiAperture_.PREFS_WIDETABLE, wideTable);
		showRatio      = Prefs.get (MultiAperture_.PREFS_SHOWRATIO, showRatio);
        showCompTot    = Prefs.get (MultiAperture_.PREFS_SHOWCOMPTOT, showCompTot);
		showRatioError = Prefs.get (MultiAperture_.PREFS_SHOWRATIO_ERROR, showRatioError);
		showRatioSNR   = Prefs.get (MultiAperture_.PREFS_SHOWRATIO_SNR, showRatioSNR);
        apFWHMFactor   = Prefs.get (MultiAperture_.PREFS_APFWHMFACTOR, apFWHMFactor);
        oldapFWHMFactor = apFWHMFactor;
        autoModeFluxCutOff = Prefs.get (MultiAperture_.PREFS_AUTOMODEFLUXCUTOFF, autoModeFluxCutOff);
        oldAutoModeFluxCutOff = autoModeFluxCutOff; 
        nAperturesMax  = (int) Prefs.get (MultiAperture_.PREFS_NAPERTURESMAX, nAperturesMax);
        enableDoubleClicks = Prefs.get (MultiAperture_.PREFS_ENABLEDOUBLECLICKS, enableDoubleClicks);
        alwaysstartatfirstSlice = Prefs.get (MultiAperture_.PREFS_ALWAYSFIRSTSLICE, alwaysstartatfirstSlice);        
//        autoMode       = Prefs.get (MultiAperture_.PREFS_AUTOMODE, autoMode);        
        }
    
    public void savePrefs()
        {
		Prefs.set (Aperture_.AP_PREFS_RADIUS, radius);
		Prefs.set (Aperture_.AP_PREFS_RBACK1, rBack1);
		Prefs.set (Aperture_.AP_PREFS_RBACK2, rBack2);
        if (showSaturationWarning || showLinearityWarning) showPeak = true;
		Prefs.set (Aperture_.AP_PREFS_SHOWPEAK, showPeak);
        Prefs.set (Aperture_.AP_PREFS_SHOWMEAN, showMean);
        Prefs.set (Aperture_.AP_PREFS_EXACT, exact);
		Prefs.set (Aperture_.AP_PREFS_SATWARNLEVEL, saturationWarningLevel);
		Prefs.set (Aperture_.AP_PREFS_SHOWSATWARNING, showSaturationWarning);
		Prefs.set (Aperture_.AP_PREFS_SHOWLINWARNING, showLinearityWarning);
		Prefs.set (Aperture_.AP_PREFS_LINWARNLEVEL, linearityWarningLevel);        
        Prefs.set (Aperture_.AP_PREFS_USEHOWELL, useHowellCentroidMethod);
//		Prefs.set (Aperture_.AP_PREFS_ONETABLE, oneTable);
		Prefs.set (Aperture_.AP_PREFS_BACKPLANE, backPlane);
		Prefs.set (Aperture_.AP_PREFS_REPOSITION, reposition);
//		Prefs.set (Aperture_.AP_PREFS_FORGIVING, forgiving);
//		Prefs.set (Aperture_.AP_PREFS_RETRY, retry);
        Prefs.set (Aperture_.AP_PREFS_REMOVEBACKSTARS, removeBackStars);
        Prefs.set (Aperture_.AP_PREFS_SHOWREMOVEDPIXELS, showRemovedPixels);

		Prefs.set (Aperture_.AP_PREFS_SHOWFILENAME, showFileName);
        Prefs.set (Aperture_.AP_PREFS_SHOWSLICENUMBER, showSliceNumber);
        Prefs.set (Aperture_.AP_PREFS_SHOWPOSITION, showPosition);
        Prefs.set (Aperture_.AP_PREFS_SHOWPOSITION_FITS, showPositionFITS);
		Prefs.set (Aperture_.AP_PREFS_SHOWPHOTOMETRY, showPhotometry);
        Prefs.set (Aperture_.AP_PREFS_SHOWNAPERPIXELS, showNAperPixels);
        Prefs.set (Aperture_.AP_PREFS_SHOWNBACKPIXELS, showNBackPixels);
        Prefs.set (Aperture_.AP_PREFS_SHOWBACK, showBack);
		Prefs.set (Aperture_.AP_PREFS_SHOWWIDTHS, showWidths);
		Prefs.set (Aperture_.AP_PREFS_SHOWMEANWIDTH, showMeanWidth);
		Prefs.set (Aperture_.AP_PREFS_SHOWRADII, showRadii);
		Prefs.set (Aperture_.AP_PREFS_SHOWTIMES, showTimes);
		Prefs.set (Aperture_.AP_PREFS_SHOWRAW, showRaw);
		Prefs.set (Aperture_.AP_PREFS_SHOWANGLE, showAngle);
		Prefs.set (Aperture_.AP_PREFS_SHOWROUNDNESS, showRoundness);
		Prefs.set (Aperture_.AP_PREFS_SHOWVARIANCE, showVariance);
        Prefs.set (MultiAperture_.PREFS_USEVARSIZEAP, useVarSizeAp);

		Prefs.set (Aperture_.AP_PREFS_SHOWFITS, showFits);
        Prefs.set (MultiAperture_.PREFS_GETMAGS, getMags);
		Prefs.set (Aperture_.AP_PREFS_FITSKEYWORDS, fitsKeywords);

		Prefs.set (Aperture_.AP_PREFS_SHOWRADEC, showRADEC);

		Prefs.set (Aperture_.AP_PREFS_STAROVERLAY, starOverlay);
		Prefs.set (Aperture_.AP_PREFS_SKYOVERLAY, skyOverlay);
        Prefs.set (Aperture_.AP_PREFS_NAMEOVERLAY, nameOverlay);
		Prefs.set (Aperture_.AP_PREFS_VALUEOVERLAY, valueOverlay);
		Prefs.set (Aperture_.AP_PREFS_TEMPOVERLAY, tempOverlay);
		Prefs.set (Aperture_.AP_PREFS_CLEAROVERLAY, clearOverlay);

		Prefs.set (Aperture_.AP_PREFS_SHOWERRORS, showErrors);
        Prefs.set (Aperture_.AP_PREFS_SHOWSNR, showSNR);
		Prefs.set (Aperture_.AP_PREFS_CCDGAIN, gain);
		Prefs.set (Aperture_.AP_PREFS_CCDNOISE, noise);
		Prefs.set (Aperture_.AP_PREFS_CCDDARK, dark);
		Prefs.set (Aperture_.AP_PREFS_DARKKEYWORD, darkKeyword);

		// SAVE MultiAperture_ PREFERENCES

		// Prefs.set (MultiAperture_.PREFS_FOLLOW, follow);
//		Prefs.set (MultiAperture_.PREFS_WIDETABLE, wideTable);
		Prefs.set (MultiAperture_.PREFS_SHOWRATIO, showRatio);
        Prefs.set (MultiAperture_.PREFS_SHOWCOMPTOT, showCompTot);
		Prefs.set (MultiAperture_.PREFS_SHOWRATIO_ERROR, showRatioError);
		Prefs.set (MultiAperture_.PREFS_SHOWRATIO_SNR, showRatioSNR);
        Prefs.set (MultiAperture_.PREFS_APFWHMFACTOR, apFWHMFactor);
        Prefs.set (MultiAperture_.PREFS_AUTOMODEFLUXCUTOFF, autoModeFluxCutOff);
        Prefs.set (MultiAperture_.PREFS_ENABLEDOUBLECLICKS, enableDoubleClicks);
        Prefs.set (MultiAperture_.PREFS_ALWAYSFIRSTSLICE, alwaysstartatfirstSlice); 
        Prefs.set (MultiAperture_.PREFS_NAPERTURESMAX, nAperturesMax);
//        Prefs.set (MultiAperture_.PREFS_AUTOMODE, autoMode);

		// SAVE Set_Aperture PREFERNCES

//		Prefs.set("setaperture.showother", showOtherPanel);
        Prefs.set("setaperture.aperturechanged",apertureChanged);        
        }
    
    
	}


