// ================================================================
//
// Disclaimer: IMPORTANT: This software was developed at the National
// Institute of Standards and Technology by employees of the Federal
// Government in the course of their official duties. Pursuant to
// title 17 Section 105 of the United States Code this software is not
// subject to copyright protection and is in the public domain. This
// is an experimental system. NIST assumes no responsibility
// whatsoever for its use by other parties, and makes no guarantees,
// expressed or implied, about its quality, reliability, or any other
// characteristic. We would appreciate acknowledgement if the software
// is used. This software can be redistributed and/or modified freely
// provided that any derivative works bear some notice that they are
// derived from it, and any modified versions bear some notice that
// they have been modified.
//
// ================================================================

// ================================================================
//
// Author: tjb3
// Date: Apr 18, 2014 1:34:00 PM EST
//
// Time-stamp: <Apr 18, 2014 1:34:00 PM tjb3>
//
//
// ================================================================

package main.gov.nist.isg.mist.stitching.gui;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.SwingWorker;

import main.gov.nist.isg.mist.stitching.MIST;
import main.gov.nist.isg.mist.stitching.StitchingGUIFrame;
import main.gov.nist.isg.mist.stitching.MIST.ExecutionType;
import main.gov.nist.isg.mist.stitching.gui.executor.StitchingExecutor;
import main.gov.nist.isg.mist.stitching.gui.params.StitchingAppParams;
import main.gov.nist.isg.mist.stitching.lib.log.Log;
import main.gov.nist.isg.mist.stitching.lib.log.Log.LogType;

/**
 * Creates a separate thread for managing stitching execution. This thread is separate from the main
 * thread to enable continued interaction with GUI elements
 * 
 * @author Tim Blattner
 * @version 1.0
 * 
 */
public class StitchingSwingWorker extends SwingWorker<Void, Void> {

  private StitchingExecutor executor;
  private Thread executorThread;
  private ExecutionType executionType;
  private StitchingGUIFrame stitchingGUI;
  private StitchingAppParams params;

  /**
   * Initializes the stitching execution
   * 
   * @param stitchingGUI the stitching application GUI
   * @param type the type of execution to be done
   */
  public StitchingSwingWorker(StitchingGUIFrame stitchingGUI, ExecutionType type) {
    this.params = new StitchingAppParams();
    

    this.executor = new StitchingExecutor(stitchingGUI, type, this.params);
    this.executionType = type;
    this.stitchingGUI = stitchingGUI;

  }

  @Override
  protected void done() {
    super.done();
    if (this.executorThread != null && this.executorThread.isAlive()) {
      this.executor.cancelExecution();
    }
  }

  @Override
  protected Void doInBackground() throws Exception {

    switch (this.executionType) {
      case LoadParams:
        runLoadParams();
        break;
      case SaveParams:
        runSaveParams();
        break;
      case PreviewNoOverlap:
        this.executorThread = new Thread(this.executor);
        this.executorThread.start();
        this.executorThread.join();
        break;
      case RunStitching:
      case RunStitchingMacro:
      case RunStitchingFromMeta:
        MIST.enableStitching();
        this.executorThread = new Thread(this.executor);
        this.executorThread.start();
        this.executorThread.join();

        break;
      case RunStitchingFromMetaMacro:
      default:
        break;

    }

    return null;
  }

  private void runSaveParams() {
    Log.msg(LogType.MANDATORY, "Checking Parameters for save");


    if (this.stitchingGUI.checkAndParseGUI(this.params)) {

      JFileChooser chooser = new JFileChooser(System.getProperty("user.home"));
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setMultiSelectionEnabled(false);

      int val = chooser.showSaveDialog(this.stitchingGUI);
      if (val == JFileChooser.APPROVE_OPTION) {
        File file = chooser.getSelectedFile();
        if (!file.exists()) {
          try {
            file.createNewFile();
          } catch (IOException e1) {
            Log.msg(LogType.MANDATORY, e1.getMessage());
          }
        }

        Log.msg(LogType.MANDATORY, "Saving Parameters");
        this.params.saveParams(file);
      }
    } else {
      Log.msg(LogType.MANDATORY, "Stitching parameter check"
          + " failed. Check the console for information. "
          + "(increase logging level for more details)");
    }

  }

  private void runLoadParams() {
    Log.msg(LogType.MANDATORY, "Loading Parameters from file");

    JFileChooser chooser = new JFileChooser(this.stitchingGUI.getInputPanel().getImageDirectory());
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setMultiSelectionEnabled(false);

    int val = chooser.showOpenDialog(this.stitchingGUI);
    if (val == JFileChooser.APPROVE_OPTION) {
      File file = chooser.getSelectedFile();

      Log.msg(LogType.MANDATORY, "Loading Parameters");
      if (this.params.loadParams(file))      
      {
        this.params.printParams();
              
        this.stitchingGUI.loadParamsIntoGUI(StitchingSwingWorker.this.params);
      }
      

    }
  }
}
