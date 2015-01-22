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
// Date: Apr 18, 2014 2:29:21 PM EST
//
// Time-stamp: <Apr 18, 2014 2:29:21 PM tjb3>
//
//
// ================================================================

package main.gov.nist.isg.mist.stitching;

import ij.ImageJ;
import ij.Macro;
import ij.plugin.PlugIn;
import ij.plugin.frame.Recorder;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;

import main.gov.nist.isg.mist.stitching.gui.StitchingSwingWorker;
import main.gov.nist.isg.mist.stitching.lib.libraryloader.LibraryUtils;
import main.gov.nist.isg.mist.stitching.lib.log.Log;
import main.gov.nist.isg.mist.stitching.lib.log.Log.LogType;

/**
 * Creates the main NIST image sitching application. Run as a standalone application or a Fiji
 * plugin. Also can be run as a macro.
 * 
 * @author Tim Blattner
 * @version 1.0
 * 
 */
public class MIST implements PlugIn {

  /**
   * The macro recorder commmand
   */
  public static String recorderCommand;

  /**
   * Enumeration of execution types to run
   * 
   * @author Tim Blattner
   * @version 1.0
   */
  public enum ExecutionType {
    /**
     * Executes load parameters
     */
    LoadParams,

    /**
     * Executes save parameters
     */
    SaveParams,

    /**
     * Executes stitching
     */
    RunStitching,

    /**
     * Executes stitching with macro
     */
    RunStitchingMacro,

    /**
     * Composes image from metadata
     */
    RunStitchingFromMeta,

    /**
     * Composes image from metadata with macro
     */
    RunStitchingFromMetaMacro,

    /**
     * Previews full image without overlap
     */
    PreviewNoOverlap;
  }

  static {
    LibraryUtils.initalize();
  }

  private static boolean macro;
  private static String macroOptions;
  private static boolean stitching;


  /**
   * Starts the application. If a macro is running, then it will execute using the macro parameters.
   * Otherwise it will use the GUI.
   * 
   * @param app the stitching gui
   */
  public static void runApp(StitchingGUIFrame app) {
    stitching = false;
    macroOptions = Macro.getOptions();
    macro = macroOptions != null;

    if (macro) {
      executeStitchingWithMacro();
    } else {
      if (app == null) {
        Log.msg(LogType.MANDATORY, "Error: app GUI was not initialized.");
        return;
      }

      app.display();
    }
  }


  /**
   * Starts the application in headless mode. Currently app must be executed using a Fiji macro in
   * order to launch in headless mode.
   */
  public static void runAppHeadless() {
    stitching = false;
    macroOptions = Macro.getOptions();
    macro = macroOptions != null;

    if (macro) {
      executeStitchingWithMacro();
    } else {
      Log.msg(LogType.MANDATORY, "When in headless mode, app must be launched as Macro");
    }
  }

  /**
   * Checks if stitching is being executed or not
   * 
   * @return true if stitching is executing
   */
  public static boolean isStitching() {
    return stitching;
  }

  /**
   * Enables the flag to indicate that stitching is executing
   */
  public static void enableStitching() {
    stitching = true;
  }

  /**
   * Disables the flag to indicate that stitching is executing
   */
  public static void disableStitching() {
    stitching = false;
  }

  /**
   * Checks if the run is a macro or not
   * 
   * @return true if the run is a macro
   */
  public static boolean isMacro() {
    return macro;
  }

  /**
   * Gets the macro options
   * 
   * @return the macro options
   */
  public static String getMacroOptions() {
    return macroOptions;
  }

  private static void executeStitchingWithMacro() {
    Log.msg(LogType.MANDATORY, "Executing stitching using macro");
    ExecutionType type = ExecutionType.RunStitchingMacro;
    StitchingSwingWorker executor = new StitchingSwingWorker(null, type);
    executor.execute();
    try {
      executor.get();
    } catch (InterruptedException e) {
      Log.msg(LogType.MANDATORY, "Macro stitching has been cancelled");
    } catch (ExecutionException e) {
      Log.msg(LogType.MANDATORY, "Macro stitching has been cancelled " + e.getMessage());
    } catch (CancellationException e) {
      Log.msg(LogType.MANDATORY, "Macro stitching has been cancelled " + e.getMessage());
    }
  }

  // ///////////////////////////////////////////////////////////////
  // ///////////////////// MAIN ENTRY POINT ////////////////////////
  // ///////////////////////////////////////////////////////////////

  /**
   * Main entry point for executing
   * 
   * @param args not used
   */
  public static void main(String[] args) {
    // Launch ImageJ window
    ImageJ.main(args);
    if (GraphicsEnvironment.isHeadless()) {
      MIST.runAppHeadless();
    } else {
      StitchingGUIFrame gui = new StitchingGUIFrame();
      MIST.runApp(gui);
    }

    
  }

  @Override
  public void run(String arg) {

    Log.msg(LogType.MANDATORY, "Launching MIST");

    recorderCommand = Recorder.getCommand();
    Recorder.setCommand(null);

    if (GraphicsEnvironment.isHeadless()) {
      MIST.runAppHeadless();
    } else {
      StitchingGUIFrame gui = new StitchingGUIFrame();
      MIST.runApp(gui);
    }
  }

}
