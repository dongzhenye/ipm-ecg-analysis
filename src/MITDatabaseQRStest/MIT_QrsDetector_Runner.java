//    This files allows the user to call to run the beat detectors on ecg stored in txt files 
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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;



public class MIT_QrsDetector_Runner {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub

		//Change root_dir to your project path, for example:"/home/joe/workspace/PhysioNet2011_Challenge_Java/";
		String root_dir="/Users/julian/Documents/Work/EclipseWorkspace/QrsDetector/";
		String data_dir= "/Users/julian/Documents/Matlab/";
		
		double elapsed_time=0,result;
		
		long start_time=0, end_time=0, trial_time=0;
		short i=0;

		//QrsDetector code = new QrsDetector();
		wQRS code = new wQRS();
		
		InputStream HiFile = new FileInputStream(data_dir + "ecg_header.txt");
		InputStreamReader inread= new InputStreamReader(HiFile);
		BufferedReader buf = new BufferedReader(inread,500);
		String strLine=null;
		BufferedWriter out_log =null;

		try {
			strLine = buf.readLine();
		} catch (IOException e1) {
			System.err.print("Could not read ecg header file.");
		}

		//First line in the header file is the header file name to be used in the log
		String file_name="ECG_LOG_" + strLine;
		File log_file = new File(root_dir, file_name);
		try {
			//FileWriter out_log_writer=new FileWriter(log_file);
			out_log= new BufferedWriter(new FileWriter(log_file));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.err.print("Could not generate log file:" + log_file);
		}

		try {
			strLine = buf.readLine();
		} catch (IOException e4) {
			// TODO Auto-generated catch block
			e4.printStackTrace();
		} //Move on to the data part

		while (strLine != null){


				InputStream FiFile = new FileInputStream(data_dir + strLine + ".txt");

				start_time=System.currentTimeMillis();
				code.Initialise(strLine);
				result = code.get_result(FiFile,strLine) ;
				end_time=System.currentTimeMillis();

			trial_time=end_time-start_time;
			elapsed_time +=  ((double) trial_time/1000);
			String strLog= strLine + " " + result + " beats detected. Time (ms): " + trial_time + " Total Time (s)= " + elapsed_time + "\n";

			try {
				out_log.write(strLog);
				System.out.print(strLog);
				out_log.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			i++;
			if(i>48){
				break;
			}
			try {
				strLine = buf.readLine();
			} catch (IOException e1) {
				System.err.print("Could not read ecg header file");
			}
		} //end of while loop
		
		try {
			out_log.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.print("Could not close log file.");
			e.printStackTrace();
		}
	}
	
}
