package test.gov.nist.isg.mist.stitchingvalidation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import main.gov.nist.isg.mist.stitching.gui.executor.StitchingExecutor;
import main.gov.nist.isg.mist.stitching.gui.executor.StitchingExecutor.StitchingType;
import main.gov.nist.isg.mist.stitching.gui.panels.advancedTab.parallelPanels.CUDAPanel;
import main.gov.nist.isg.mist.stitching.gui.params.StitchingAppParams;
import main.gov.nist.isg.mist.stitching.lib.libraryloader.LibraryUtils;
import main.gov.nist.isg.mist.stitching.lib.log.Log;
import main.gov.nist.isg.mist.stitching.lib.log.Log.LogType;

public class ImageStitchingValidationDatasets {

  static {
    LibraryUtils.initalize();
  }  

  private static final String STITCHING_PARAMS_FILE = "stitching-params.txt";


  private static String validationRootFolder = "F:\\StitchingData\\Image_Stitching_Validation_Datasets";
  private static String fftwPlanPath = "C:\\Users\\tjb3\\Desktop\\Fiji.app\\fftPlans";
  private static String fftwLibraryPath = "C:\\Users\\tjb3\\Desktop\\Fiji.app\\lib\\fftw";


  public static void main(String [] args)
  {
    if (args.length > 0)
    {
      validationRootFolder = args[0];
    }


    // get all folders in root folder
    File rootFolder = new File(validationRootFolder);
    if (!rootFolder.exists() && !rootFolder.isDirectory())
    {
      System.out.println("Error: Unable to find root folder: " + validationRootFolder);
      System.exit(1);
    }    

    File[] roots = rootFolder.listFiles();

    CUDAPanel cudaPanel = new CUDAPanel();

    JFrame frame = new JFrame("Select CUDA Devices");    
    JOptionPane.showMessageDialog(frame, cudaPanel);    

    Log.setLogLevel(LogType.NONE);

    StitchingAppParams params;

    long startTime = System.currentTimeMillis();

    for (File r : roots)
    {      
      
      System.out.println("Running: " + r.getAbsolutePath());
      if (!r.isDirectory())
        continue;
      
      params = new StitchingAppParams();

      File paramFile = new File(r, STITCHING_PARAMS_FILE);

      params.loadParams(paramFile);
      
      params.getInputParams().setImageDir(r.getAbsolutePath());
      params.getAdvancedParams().setNumCPUThreads(Runtime.getRuntime().availableProcessors());
      params.getAdvancedParams().setPlanPath(fftwPlanPath);
      params.getAdvancedParams().setFftwLibraryPath(fftwLibraryPath);
      params.getAdvancedParams().setCudaDevices(cudaPanel.getSelectedDevices());
      params.getOutputParams().setOutputFullImage(false);
      for (StitchingType t : StitchingType.values())
      {
        if (t == StitchingType.AUTO)
          continue;

        if (t == StitchingType.CUDA)
        {
          if (!cudaPanel.isCudaAvailable())
            continue;
        }

        System.out.println("Stitching Type: " + t);

        File metaDataPath = new File(r, t.name().toLowerCase());
        params.getOutputParams().setMetadataPath(metaDataPath.getAbsolutePath());  
        params.getOutputParams().setOutputPath(metaDataPath.getAbsolutePath());
        params.getAdvancedParams().setProgramType(t);


        StitchingExecutor executor = new StitchingExecutor(params);

        executor.runStitching(false, false);      
      }     
    }

    long endTime = System.currentTimeMillis();

    System.out.println("Total time: " + (endTime-startTime));
    
    File results = new File("validationDataSetResults.txt");

    FileWriter writer = null;
    try {
      writer = new FileWriter(results);
    } catch (IOException e) {
      e.printStackTrace();
    }

    try
    {
      if (writer != null)
        writer.write("Runtime for " + roots.length + " experiements: " + (endTime-startTime));

    } catch (IOException e) {
      e.printStackTrace();
    }

    try
    {
      if (writer != null)
        writer.close();
    } catch (IOException e) {
      e.printStackTrace();
    }

    System.exit(1);
  }
}
