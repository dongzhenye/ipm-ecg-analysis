
//    The algorithm posted here was originally written from scratch in C by G. D. Clifford and includes updated from the citations above. which are described in: Clifford G.D., Signal Processing Methods for Heart Rate Variability, DPhil. Thesis, Oxford University, Michaelmas 2002. Please cite this when using the code. 
//    Copyright (C) 2013, University of Oxford
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 2 of the License, or
//    any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.


//Authors: 	Gari D Clifford - 
//			Julien Oster	- 

package MITDatabaseQRStest;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.*;



public class QrsDetector {
	
	private boolean DEBUG  = false;
	
	//for writng and reading files (most of them are for debugging purpose)
	private String root_dir="/Users/julian/Documents/Work/EclipseWorkspace/QrsDetector/";
	
	private String bpf_filename = "bpf_output.txt";
	private BufferedWriter bpf_out_log =null;
	private File bpf_output_file = null;
	
	private String tav_filename = "tav_output.txt";
	private BufferedWriter tav_out_log =null;
	private File tav_output_file = null;
	
	private String med_filename = "med_output.txt";
	private BufferedWriter med_out_log =null;
	private File med_output_file = null;	
	
	//Create an output file to store QRS locations, RR interval 
	private String MitFileName;
	private File log_file = null;
	private BufferedWriter QrsOutput = null;
	private String strQrsOutput = null;
	
	private int samplingFrequency = 256;
	private int WIND = 1000  ;
	
	private int WINDOW = 100 ;// window on data must be at least 100 because delays are 
			  					//72 samples (so far) and peak is about 80ms (10 samps) wide */
	private int TAVWINDOW = 80;

	private double THRESHOLD = 0.9;
	private double LOW_THRESHOLD =  0.161 ;//0.4 // 0.161
	
	
	private double  REFACTORY = 0.2; // no. of seconds after a fid that blanking kicks in.
	private long sampleNo = 0;
	private long t_refact = 0	;
	
	//private double fixed_low_threshold = 0.161; 
	private double low_threshold = LOW_THRESHOLD;
	
	//private double Raw_OFFSET = 0.0; /* The (variable) amount to be added to the baseline of the signal according to database */
	
	//***********************************************************************/
	//MIT 256Hz data
	private int NO_LP_COEFF = 38;
	private int NO_HP_COEFF = 312;
	
	private double l_p_filt_coeff[]= { 
			  -0.00009346235941,
			  -0.00040034167381,
			  -0.00111158024526,
			  -0.00243326225590,
			  -0.00450640152123,
			  -0.00729365122836,
			  -0.01046324024778,
			  -0.01331254752728,
			  -0.01477621421105,
			  -0.01355002231955,
			  -0.00833248504761,
			   0.00185207850745,
			   0.01732068871587,
			   0.03748060637556,
			   0.06074599598689,
			   0.08467505972049,
			   0.10632605270596,
			   0.12276996113233,
			   0.13164895219470,
			   0.13164895219470,
			   0.12276996113233,
			   0.10632605270596,
			   0.08467505972049,
			   0.06074599598689,
			   0.03748060637556,
			   0.01732068871587,
			   0.00185207850745,
			  -0.00833248504761,
			  -0.01355002231955,
			  -0.01477621421105,
			  -0.01331254752728,
			  -0.01046324024778,
			  -0.00729365122836,
			  -0.00450640152123,
			  -0.00243326225590,
			  -0.00111158024526,
			  -0.00040034167381,
			  -0.00009346235941};
	
	private double h_p_filt_coeff[] = {
			   0.00329398890514,
			   -0.00028439114267,
			   -0.00027900767416,
			   -0.00027821248664,
			   -0.00028095530856,
			   -0.00028727959281,
			   -0.00029610732188,
			   -0.00030743716597,
			   -0.00032012741679,
			   -0.00033409688125,
			   -0.00034816121452,
			   -0.00036231845336,
			   -0.00037542569706,
			   -0.00038742259197,
			   -0.00039710281868,
			   -0.00040454224477,
			   -0.00040860846756,
			   -0.00040937214382,
			   -0.00040568775819,
			   -0.00039799975327,
			   -0.00038499276946,
			   -0.00036735004266,
			   -0.00034380162937,
			   -0.00031534711747,
			   -0.00028061710510,
			   -0.00024097275304,
			   -0.00019502307100,
			   -0.00014466724385,
			   -0.00008815768937,
			   -0.00002818561032,
			    0.00003778137254,
			    0.00010508534223,
			    0.00017874793479,
			    0.00025562229835,
			    0.00032460518936,
			    0.00040513655233,
			    0.00048306988452,
			    0.00056103453189,
			    0.00063481489377,
			    0.00070631466823,
			    0.00077269630528,
			    0.00083498376038,
			    0.00089068215672,
			    0.00094020255813,
			    0.00098117981153,
			    0.00101379580497,
			    0.00103609682806,
			    0.00104822948731,
			    0.00104840235022,
			    0.00103682499562,
			    0.00101225243592,
			    0.00097488722045,
			    0.00092374034938,
			    0.00085948442595,
			    0.00078130672239,
			    0.00069020311185,
			    0.00058563383852,
			    0.00046903179604,
			    0.00034010001721,
			    0.00020063055966,
			    0.00005057944525,
			   -0.00010769767645,
			   -0.00027419618199,
			   -0.00044587722659,
			   -0.00062330137332,
			   -0.00080164262228,
			   -0.00098236045617,
			   -0.00116264276831,
			   -0.00133737610903,
			   -0.00150803957428,
			   -0.00167135430060,
			   -0.00182564086747,
			   -0.00196774835135,
			   -0.00209592909364,
			   -0.00220767423805,
			   -0.00230140994360,
			   -0.00237503145684,
			   -0.00242706963862,
			   -0.00245555648141,
			   -0.00245906979924,
			   -0.00243611677704,
			   -0.00238583751270,
			   -0.00230710918363,
			   -0.00219932736644,
			   -0.00206235594730,
			   -0.00189595603396,
			   -0.00170051413018,
			   -0.00147659772987,
			   -0.00122514375256,
			   -0.00094741201395,
			   -0.00064488628873,
			   -0.00031957582626,
			    0.00002641609610,
			    0.00039043639778,
			    0.00076985614809,
			    0.00116125302498,
			    0.00156158742029,
			    0.00196649819842,
			    0.00237341023967,
			    0.00277629813580,
			    0.00317244055478,
			    0.00355754264775,
			    0.00392575388895,
			    0.00427312882178,
			    0.00459491577104,
			    0.00488692966214,
			    0.00514416988773,
			    0.00536231492610,
			    0.00553659307785,
			    0.00566302072149,
			    0.00573736583063,
			    0.00575629365108,
			    0.00571612526400,
			    0.00561385859726,
			    0.00544627623023,
			    0.00521152654179,
			    0.00490725730326,
			    0.00453199267692,
			    0.00408487648568,
			    0.00356524980028,
			    0.00297319953436,
			    0.00230922276973,
			    0.00157444401596,
			    0.00077049840301,
			   -0.00010050086507,
			   -0.00103581044612,
			   -0.00203221516402,
			   -0.00308592023264,
			   -0.00419275109899,
			   -0.00534786615004,
			   -0.00654625738906,
			   -0.00778170658540,
			   -0.00904933366708,
			   -0.01034139380427,
			   -0.01165204179707,
			   -0.01297445451386,
			   -0.01430133695905,
			   -0.01562510885414,
			   -0.01693846937362,
			   -0.01823399649977,
			   -0.01950435726868,
			   -0.02074211574665,
			   -0.02193997693588,
			   -0.02309067176107,
			   -0.02418717538846,
			   -0.02522291117292,
			   -0.02619175143339,
			   -0.02708783951403,
			   -0.02790503237395,
			   -0.02863884069299,
			   -0.02928443258279,
			   -0.02983758031215,
			   -0.03029512740559,
			   -0.03065389292862,
			   -0.03106724037372,
			    0.96888079336632,
			   -0.03106724037372,
			   -0.03091190298954,
			   -0.03065389292862,
			   -0.03029512740559,
			   -0.02983758031215,
			   -0.02928443258279,
			   -0.02863884069299,
			   -0.02790503237395,
			   -0.02708783951403,
			   -0.02619175143339,
			   -0.02522291117292,
			   -0.02418717538846,
			   -0.02309067176107,
			   -0.02193997693588,
			   -0.02074211574665,
			   -0.01950435726868,
			   -0.01823399649977,
			   -0.01693846937362,
			   -0.01562510885414,
			   -0.01430133695905,
			   -0.01297445451386,
			   -0.01165204179707,
			   -0.01034139380427,
			   -0.00904933366708,
			   -0.00778170658540,
			   -0.00654625738906,
			   -0.00534786615004,
			   -0.00419275109899,
			   -0.00308592023264,
			   -0.00203221516402,
			   -0.00103581044612,
			   -0.00010050086507,
			    0.00077049840301,
			    0.00157444401596,
			    0.00230922276973,
			    0.00297319953436,
			    0.00356524980028,
			    0.00408487648568,
			    0.00453199267692,
			    0.00490725730326,
			    0.00521152654179,
			    0.00544627623023,
			    0.00561385859726,
			    0.00571612526400,
			    0.00575629365108,
			    0.00573736583063,
			    0.00566302072149,
			    0.00553659307785,
			    0.00536231492610,
			    0.00514416988773,
			    0.00488692966214,
			    0.00459491577104,
			    0.00427312882178,
			    0.00392575388895,
			    0.00355754264775,
			    0.00317244055478,
			    0.00277629813580,
			    0.00237341023967,
			    0.00196649819842,
			    0.00156158742029,
			    0.00116125302498,
			    0.00076985614809,
			    0.00039043639778,
			    0.00002641609610,
			   -0.00031957582626,
			   -0.00064488628873,
			   -0.00094741201395,
			   -0.00122514375256,
			   -0.00147659772987,
			   -0.00170051413018,
			   -0.00189595603396,
			   -0.00206235594730,
			   -0.00219932736644,
			   -0.00230710918363,
			   -0.00238583751270,
			   -0.00243611677704,
			   -0.00245906979924,
			   -0.00245555648141,
			   -0.00242706963862,
			   -0.00237503145684,
			   -0.00230140994360,
			   -0.00220767423805,
			   -0.00209592909364,
			   -0.00196774835135,
			   -0.00182564086747,
			   -0.00167135430060,
			   -0.00150803957428,
			   -0.00133737610903,
			   -0.00116264276831,
			   -0.00098236045617,
			   -0.00080164262228,
			   -0.00062330137332,
			   -0.00044587722659,
			   -0.00027419618199,
			   -0.00010769767645,
			    0.00005057944525,
			    0.00020063055966,
			    0.00034010001721,
			    0.00046903179604,
			    0.00058563383852,
			    0.00069020311185,
			    0.00078130672239,
			    0.00085948442595,
			    0.00092374034938,
			    0.00097488722045,
			    0.00101225243592,
			    0.00103682499562,
			    0.00104840235022,
			    0.00104822948731,
			    0.00103609682806,
			    0.00101379580497,
			    0.00098117981153,
			    0.00094020255813,
			    0.00089068215672,
			    0.00083498376038,
			    0.00077269630528,
			    0.00070631466823,
			    0.00063481489377,
			    0.00056103453189,
			    0.00048306988452,
			    0.00040513655233,
			    0.00032460518936,
			    0.00025562229835,
			    0.00017874793479,
			    0.00010508534223,
			    0.00003778137254,
			   -0.00002818561032,
			   -0.00008815768937,
			   -0.00014466724385,
			   -0.00019502307100,
			   -0.00024097275304,
			   -0.00028061710510,
			   -0.00031534711747,
			   -0.00034380162937,
			   -0.00036735004266,
			   -0.00038499276946,
			   -0.00039799975327,
			   -0.00040568775819,
			   -0.00040937214382,
			   -0.00040860846756,
			   -0.00040454224477,
			   -0.00039710281868,
			   -0.00038742259197,
			   -0.00037542569706,
			   -0.00036231845336,
			   -0.00034816121452,
			   -0.00033409688125,
			   -0.00032012741679,
			   -0.00030743716597,
			   -0.00029610732188,
			   -0.00028727959281,
			   -0.00028095530856,
			   -0.00027821248664,
			   -0.00027900767416,
			   -0.00028439114267,
			    0.00329398890514};
	
	
	private int bp_delays = 172;
	private int total_delays = 208;
	private int tav_delay=32;
	private int slp_delay=4;
	
	private double qrswin[];
	private double data[];
	private double time_buff[];		
	private double new_l_p_filt[];
	private double new_h_p_filt[];
	private double slope[];
	private double square[];
	private double time_av[];
	
	private double five_pt_median_array[];
	private double five_pt_median_value=0.2;
	private double window_max = 0.2;
	
	private long no_of_fids=0;
	
	
	//variables for control of number of detection and RR computation
	private int mult = 4;
	private int check1= mult*samplingFrequency, check2;
    

    private double fid_pt_pandt_time=0;
    //float fid_pt_pandt_height;
    //float fid_pt_pandt_time_sub_samp, fid_pt_pandt_rr_sub_samp;
    private double last_fid_time=0;// last_fid_time_sub_samp;
    private long last_fid=0;
    private double rr_int,heart_rate;
	
	public void Initialise(String fileName){
		qrswin= new double[WIND] ;
		data =new double[WINDOW];
		time_buff =new double[WIND];
		new_l_p_filt =new double[WIND];
		new_h_p_filt =new double[WIND];
		slope =new double[WINDOW];
		square =new double[WINDOW];

		time_av =new double[TAVWINDOW];
		
		five_pt_median_array = new double[5];
		
		MitFileName = fileName;
		
		//variables to reinitialise 
		sampleNo=0;
		t_refact=0;
		low_threshold = LOW_THRESHOLD;
		five_pt_median_value=0.2;
		window_max=0.2;
		no_of_fids=0;
		check2=0;
		fid_pt_pandt_time=0;
		last_fid_time=0;
		last_fid=0;
		rr_int=0;
		heart_rate=0;
		
		
		int indexInitialise=0;
		
		
		for (indexInitialise=0;indexInitialise<WIND;indexInitialise++){
			qrswin[indexInitialise]=0;
			time_buff[indexInitialise]=0;
			new_l_p_filt[indexInitialise]=0;
			new_h_p_filt[indexInitialise]=0;
		}
		for (indexInitialise=0;indexInitialise<WINDOW;indexInitialise++){
			data[indexInitialise]=0;
			slope[indexInitialise]=0;
			square[indexInitialise]=0;
		}
		
		for (indexInitialise=0;indexInitialise<TAVWINDOW;indexInitialise++){
			time_av[indexInitialise]=0;
		}
		
		for (indexInitialise=0;indexInitialise<5;indexInitialise++){
			five_pt_median_array[indexInitialise]=1;
		}
		

		String file_name = "QRS_Ouput_" + MitFileName + ".txt";
		log_file = new File(root_dir, file_name);
		try {
			QrsOutput= new BufferedWriter(new FileWriter(log_file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.print("Could not generate QRS output file:" + log_file);
		}
		
		//initialisation of the output files for debug
		if (DEBUG){

			bpf_output_file = new File(root_dir, bpf_filename);
			
			try{
			bpf_out_log= new BufferedWriter(new FileWriter(bpf_output_file));
			} catch (IOException e) {
				System.err.print("Could not open band-pass debug file.");
			}
			

			tav_output_file = new File(root_dir, tav_filename);
			
			try{
			tav_out_log= new BufferedWriter(new FileWriter(tav_output_file));
			} catch (IOException e) {
				System.err.print("Could not open time averag debug file.");
			}			

			med_output_file = new File(root_dir, med_filename);
			
			try{
			med_out_log= new BufferedWriter(new FileWriter(med_output_file));
			} catch (IOException e) {
				System.err.print("Could not open median debug file.");
			}
			
			
		}
		
	}
	
	private long scan_back(long right_window, long left_window)
		{
		    long  fiducial_index, S_index;
		    double fiducial_time=0, fiducial_height, S_height;
		    double hi_check = -1.0;
		    double lo_check = 100000000.0;
		    // check when reading in real data ...  what the minimum value is
		    // initialise check to this 
		    
		    // Remove the filter differences 
		    right_window = right_window - (tav_delay+slp_delay) -bp_delays;
		    left_window  = left_window  - (tav_delay+slp_delay) -bp_delays;
		    
		    fiducial_index = right_window;


		    
		    // right window is closing back to the left one through each loop 
		    while ((right_window - left_window)  >= 0)
		    {
		        //      printf("%i, %i\n",right_window,left_window);
		        //if(scanflag == 1)printf("%f %d\n", data[right_window%WINDOW], right_window);
		        // later set the test to be for a peak as well ... to pick out
		        // peaks on a massive gradient line caused by a drop out */
		        

		        if(qrswin[(int)((right_window)%WIND)] >= hi_check)
		        {
		            hi_check = qrswin[(int)((right_window)%WIND)];
		            fiducial_index = right_window;
		            fiducial_height = hi_check;
		            fiducial_time   = time_buff[(int)((fiducial_index)%WIND)];
		        }
		        
		        //  also ... If the previous value is higher than any of the data in
		        // this window set the fiducial point to be that datum ...
		        // avoids local maxima
		        // if (data[(right_window-1)%WINDOW] > data[(fiducial)%WINDOW]) 
		        
		        if(qrswin[(int)((right_window)%WIND)] <= lo_check)
		        {

		            lo_check = qrswin[(int)((right_window)%WIND)];
		            S_index = right_window;
		            S_height = lo_check;
		        }
		        
		        // back track through the window to test each data point 
		        right_window--;
		    }
		    
		    //test JOs
		    if (fiducial_index + tav_delay + slp_delay + bp_delays-t_refact<REFACTORY){
		        fiducial_index=-1;
		        
		    }
		    else 
		    {
		        
		    	//TODO save output in txt file
//		    	fprintf(fptr_fid,"%f %f %f %f\n",fiducial_time,hi_check-lo_check,hi_check,lo_check);
//		    	fflush(fptr_fid);
				//test
				strQrsOutput=fiducial_time + " " + (hi_check-lo_check) + " " + hi_check + " " + lo_check;
				try {
					QrsOutput.write(strQrsOutput);
					QrsOutput.newLine();
					QrsOutput.flush();
				} catch (IOException e3) {
					System.err.print("Could not write QRS output file.");
				}
		    
				t_refact = fiducial_index + (int)(REFACTORY*samplingFrequency) + tav_delay + slp_delay + bp_delays;
		    
		    
		        
		    }
		    return(fiducial_index);
	}
	
	private long locate_R()
	{
	    long left_margin=0, right_margin=0, x;
	    long fiducial_pt = 0;
	    
	    //fiducial_pt = 0;
	    
//	     if we have not started processing data yet then initialise the five
//	     point mean to be a specified value
//	     LATER *** make this hardware independent by taking an average (MODE)
//	     over the first TRANSIENT points to give a quasi hardware independent
//	     measure 

	    
	    /* check to see we are out of the refactory blanking period */
	    if (sampleNo<=t_refact)
	    {
	        fiducial_pt=-1;
	        return(fiducial_pt);
	    }
//	    if (t<200)
//	    {
//	        fiducial_pt=0;
//	        return(fiducial_pt);
//	    }
	    
//	     then check for lower neighbouring points,  highest point in the window
//	     and a value greater than 30% of the 5pt median. 
	    if ((time_av[(int)((sampleNo-2)%TAVWINDOW)] < time_av[(int)((sampleNo-1)%TAVWINDOW)])
	        && (time_av[(int)((sampleNo-1)%TAVWINDOW)] > time_av[(int)(sampleNo%TAVWINDOW)])
	        && (time_av[(int)((sampleNo-1)%TAVWINDOW)]>(low_threshold*five_pt_median_value)))
	    {
	        // if this is true, set the WINDOW sized window's maximum to be this 
	        window_max = time_av[(int)((sampleNo-1)%TAVWINDOW)];
	        
	    }
	    
//	    if the data falls below the 90% threshold (the last data point is
//	     above it and the next below) whilst in this region 
	    if ( (time_av[(int)((sampleNo-1)%TAVWINDOW)] >= (0.9*window_max))
	        && (time_av[(int)(sampleNo%TAVWINDOW)] < (0.9*window_max))
	        && (time_av[(int)((sampleNo-1)%TAVWINDOW)] > (low_threshold*five_pt_median_value)) )
	    {
	        right_margin = (sampleNo-1);
	        x = right_margin;
	        
	        
	        while (left_margin == 0)
	        {
//	             starting from the peak, if the current value is greater
//	             than the threshold AND the previous value then this is the
//	             left margin of the window, otherwise scan back 
	            if((time_av[(int)(x%TAVWINDOW)] >= (0.9*window_max)) && (time_av[(int)((x-1)%TAVWINDOW)] < (0.9*window_max)))
	            {
	                left_margin = x;
//	                fidicial point is half way between the two points
//	                (minus the delays =40 plus rounding error) 
//	                fiducial_pt = ((right_margin+left_margin)/2)-39; 
	                
	                // look back through original data to find the true peak 
	                // removing delays (was 40 for 128Hz now 56  .. 193 new_filt?
	                
	                
	                // look back through original data to find the true peak 
                    fiducial_pt = scan_back(right_margin, left_margin);

	                //Test JOs in scan_back one can return -1 if detection within refactory period
	                if (fiducial_pt>0)
	                {
	                
	                // pass the number fiducial points encountered so far,
	                // and the largest value in this region into the five
	                // point median filter to reset the threshold */
	                	five_pt_median_value=five_pt_median_funct(window_max,no_of_fids);
	                	
	                    
	                    if(DEBUG)
	                    {
	                		String strMedOutput = sampleNo + " " + five_pt_median_value;
	                		try {
	                			med_out_log.write(strMedOutput);
	                			med_out_log.newLine();
	                			med_out_log.flush();
	                		} catch (IOException e3) {
	                			System.err.print("Could not write median value debug output file.");
	                		}
	                		
	                    } 
	                	
	                	
	                	no_of_fids++;
	                }
	            }
	            else x--;
	        }
	        //******** call original data scan back function here ************/
	        left_margin = 0;
	    }
	    return(fiducial_pt);
	}
	
	private double five_pt_median_funct(double new_value, long index_5_pt)
	{
	    short i,j;
	    short noswap=5;
	    double median=0, temp_median[], dummy;
	    temp_median = new double[5];
	    
	    // use the index through the number of samples as a wrap around for
	    // the 5 values in the array - hence we always write over the very
	    // oldest value 
	    j = (short) (index_5_pt%5);

	    
	    // pass the latest value into the five point median array overwriting
	    // the oldest value 
	    five_pt_median_array[j] = new_value;
	    temp_median[j] = five_pt_median_array[j];

	    
	    // put each value of the five point median array into a temporary array
	    // for sorting, so we do not keep very old values 
	    for (i=0; i<=4; i++)
	    {
	        temp_median[i] = (double) five_pt_median_array[i];
	    }

	    
	    // while the order is still being swapped, continue. 
	    while (noswap > 0)
	    {
	        
	        // scan up the array once swapping adjacent values if the one
	        // above is larger 
	        
	        noswap = 0; // initialise the number of no exchanges 
	        
	        for (i=0; i<=3; i++)
	        {
	            // if the one above is greater, then shuffle it up one -
	            // repeat the process for each neighbouring pair 
	            if (temp_median[i+1]<temp_median[i])
	            {
	                dummy = temp_median[i];
	                temp_median[i] = temp_median[i+1];
	                temp_median[i+1] = dummy;
	                noswap++;
	            }
	        }
	        median = temp_median[2];
	        
	    }
        

	    return(median);
	}
	
	private void seed_5pt_median_filt(double seed){
	    int i;
	    
	    for(i=0;i<5;i++)
	    {
	        five_pt_median_array[i] = seed;
	    }
	}
	
	private double hpsort(int n, double[] ra){
		double highest, lowest;
		double med_filt;
	    double threshold, thresh;
		
		Arrays.sort(ra);
	    
		/* work out the median value of the 80-85 percentile */
		highest=ra[(int)(TAVWINDOW*0.85)];
		lowest=ra[(int)(TAVWINDOW*0.8)];
		med_filt=(highest+lowest)/2.0;
	    

		threshold=ra[(int)(TAVWINDOW*0.1)]; // noise floor
		
	    
	    thresh=(med_filt+threshold)/2.0;    //
	    
		low_threshold = thresh/med_filt;

	    //test JOs
	    if (low_threshold>LOW_THRESHOLD)
	    {
	        low_threshold=LOW_THRESHOLD;
	    }
		
		return(med_filt);
	}
	
	private void reset_5pt_median_filt(){
	    double median_seed, dummy[];
	    dummy = new double[TAVWINDOW]; 
	    

	    // make a copy of the array 
	    System.arraycopy(time_av, 0, dummy, 0, time_av.length);

	    // and sort it in ascending ranked order
	    median_seed = hpsort(TAVWINDOW, dummy);
	    
	    // Note - this also finds largest 80-85% values */
	    // pick middle value to seed 5 point median filter .. look - */
	    
	    seed_5pt_median_filt(median_seed);
	    
	    
	    
	}
	
	private void time_average(){
	    
		double accum=0;
	    int i, BUFF_SZ;
	    
	    BUFF_SZ= (int) (samplingFrequency/4); // 32 for 128Hz,
	    // double size of window - 64 for 256 rather than 32 for 128Hz 
	    
	    for(i=0; i<=BUFF_SZ-1; i++)
	    {
	    	if ((sampleNo-i)>0){
		        accum += square[(int)((sampleNo-i)%WINDOW)];
	    	}
	    }
	    
	    // 64 point window causing further 32 point delay 
	    time_av[(int)(sampleNo%TAVWINDOW)]  = accum/((double)(BUFF_SZ));
	}
	
	private void squaring_func(){
		square[(int)(sampleNo%WINDOW)] = slope[(int)(sampleNo%WINDOW)]*slope[(int)(sampleNo%WINDOW)]*100;
	}

	private void slope_func(){
	    // differentiate signal to aquire QRS slope information 
	    // the multiplicative const is a normalising constant to cause this signal to be the smae size as the
	    //   Pan & Tompkins filter (*28 for new_filter)
		if ((sampleNo-4)>0){
			slope[(int)(sampleNo%WINDOW)]=((2*new_h_p_filt[(int)(sampleNo%WIND)])+new_h_p_filt[(int)((sampleNo-1)%WIND)]-new_h_p_filt[(int)((sampleNo-3)%WIND)]-(2*new_h_p_filt[(int)((sampleNo-4)%WIND)]))/8;
		}
	}
	
	private void new_high_pass_filter(){

		    int i;

		    new_h_p_filt[(int)(sampleNo%WIND)] = 0.0;
		    for (i=0; i<=NO_HP_COEFF-1; i++) //
		    {
		        if ((sampleNo-i)<0){
		        	new_h_p_filt[(int)(sampleNo%WIND)] = 0;
		        }
		        else{
		        	new_h_p_filt[(int)(sampleNo%WIND)] += new_l_p_filt[(int)((sampleNo-i)%WIND)]*h_p_filt_coeff[i];
		        }
		        	
		    }

		
	}
	
	private void new_low_pass_filter()
	{
		
	    int i;
		    
	    // FIR filter difference calculation from MATLAB coefficients
		    
	    new_l_p_filt[(int) (sampleNo%WIND)] =0.0;
		    
		for (i=0; i<=NO_LP_COEFF-1; i++)
		{        
        	
        	if ((sampleNo-i)<0) {
	        	new_l_p_filt[(int)(sampleNo%WIND)] = 0;
	        }
        	else
        	{
        		new_l_p_filt[(int)(sampleNo%WIND)] += qrswin[(int)((sampleNo-i)%WIND)]*l_p_filt_coeff[i];
        	}
	    }	    
		
	}

	private long filter_algorithms(int window_index, int window_indextav)
	{
		long fid_pt_index = 0 ;
		int dataIndexcnv,i;
    
		double rel_time=time_buff[(int) (sampleNo%WIND)];
		double rel_time_bp=-1;
		double rel_time_tav=-1;
		if ((sampleNo-bp_delays)>=0){
			rel_time_bp=time_buff[(int) ((sampleNo-bp_delays)%WIND)];
		}
		double filter_delays = (double) 209.0/256.0;
    
		fid_pt_index=0;
    
		dataIndexcnv = (int) (sampleNo%WIND);
    
		// 37 point window & therefore a delay of 18 
		new_low_pass_filter();
        
		// 313 point window & therefore a delay of 156
		new_high_pass_filter();

    
    if(DEBUG)
    {
		String strBpfOutput = rel_time + " " + qrswin[dataIndexcnv] + " " + rel_time_bp + " " + new_h_p_filt[dataIndexcnv];
		try {
			bpf_out_log.write(strBpfOutput);
			bpf_out_log.newLine();
			bpf_out_log.flush();
		} catch (IOException e3) {
			System.err.print("Could not write band pass debug output file.");
		}
		
    } 
    
    /* 4 point window & therefore a delay of 2 */
    slope_func();

    
    //  instantaneous therefore no delay 
    squaring_func();


    /* 64 point window & therefore a delay of 32 */
    time_average();
    
    if(DEBUG)
    {
    	if ((sampleNo-bp_delays-34)>0){
    		rel_time_tav = time_buff[(int) ((sampleNo-bp_delays-34)%WIND)];
    	}
		String strTavOutput =  rel_time_tav + " " +  time_av[window_indextav];
		try {
			tav_out_log.write(strTavOutput);
			tav_out_log.newLine();
			tav_out_log.flush();
		} catch (IOException e3) {
			System.err.print("Could not write time average debug output file.");
		}
		
    }
    /* when we have 1000+ non-zero entries in the integrated filter */
    if(sampleNo==(total_delays+WIND))
    {
        reset_5pt_median_filt();
    }
    
    if(sampleNo > WIND+total_delays){
        fid_pt_index = locate_R();
    }

		return fid_pt_index; // fid_pt as found in bandpassed data	
	}

	
	
	private long DetectQRS(double x, double y){
		long fid =0;
	    int dataIndex, dataIndextav, dataIndexcnv; /* indexes into the windows */
	   

	    dataIndextav = (int) (sampleNo % (long) TAVWINDOW);
	    dataIndex = (int) (sampleNo % (long) WINDOW);
	    dataIndexcnv = (int) (sampleNo % (long) WIND);
	    data[dataIndex] = y; 
	    qrswin[dataIndexcnv] = y;

        time_buff[(int) (sampleNo%WIND)] = x;

	    fid = filter_algorithms(dataIndex, dataIndextav);
	    
		
		return fid;
	}
	
	synchronized public double get_result(InputStream iFile, String fileName) throws IOException {
		
		
		double time=0, startTime =0;
		long fid_pandt=0;
		
		//Read the txt file as a stream
		InputStreamReader inreadFile= new InputStreamReader(iFile);
		BufferedReader bufFile = new BufferedReader(inreadFile,500);
		String strLineFile = null;
		double sample=-1;
		
		
		try {
			strLineFile	 = bufFile.readLine();		
		} catch (IOException e1) {
			System.err.print("Could not read ecg file.");
		}
		
		while (strLineFile	!= null){
			//only read the first column of the txt file
			StringTokenizer tok = new StringTokenizer (strLineFile, " ");			
			sample = Double.parseDouble(tok.nextToken());
			
			fid_pandt = DetectQRS(time,sample);
			
	        //check1 = (int) (samplingFrequency*mult);
	        check2 = (int) (sampleNo-last_fid)%(check1);
	        if ( (check2 == check1-1) && (((double)(sampleNo)/samplingFrequency)>8) ) // make sure it runs for at least 8 seconds first
	        {
	            //printf("Hmmm more than a n * %f seconds since the last R peak\n",mult);
	            //printf("%i %i %i -- ",check1,check2,sample_no-last_fid);
	            low_threshold=low_threshold*0.9;
	            //fprintf(stdout,"reducing low threshold to %f at time %f\n",low_threshold,y);
	        }
	        
	        if (fid_pandt > 0) /* (-1 indicates a refactory period) */
	        {

                fid_pt_pandt_time = time_buff[(int)((fid_pandt)%WIND)];

	            rr_int = fid_pt_pandt_time-last_fid_time;
	            heart_rate = 60.0/rr_int;

	            
	            //fid_pt_pandt_height = h_p_filt[(fid_pt_pandt)%WIND];
	 
	            if(rr_int > REFACTORY)
	            {
	                if(heart_rate<30.0){
	                	heart_rate=30.0;
	                }
	                if(heart_rate>200.0)
	                {
	                    heart_rate=200.0;
	                    reset_5pt_median_filt();
	                }	                
	            }
	            
	            //actual_fid_rel_time=time_buff[(fid_pt_pandt)%WIND];
	            //actual_last_fid_rel_time=actual_fid_rel_time;
	            
	            last_fid=fid_pandt;
	            last_fid_time = fid_pt_pandt_time;
	            
	            //last_fid_time_sub_samp=fid_pt_pandt_time_sub_samp;
	            //last_heart_rate=heart_rate;

	        }
			
			
			//read new sample
			try {
				strLineFile	 = bufFile.readLine();		
			} catch (IOException e2) {
				System.err.print("Could not read ecg file.");
			}
			//increment sampleIndex and time
			time += 1/ ((double )samplingFrequency);
			sampleNo++ ;
			
		}
		
		QrsOutput.close();
		
		return (double) no_of_fids;
		 
	}

}
