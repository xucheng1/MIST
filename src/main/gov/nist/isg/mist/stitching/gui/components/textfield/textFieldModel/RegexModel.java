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
// Date: Apr 18, 2014 12:37:30 PM EST
//
// Time-stamp: <Apr 18, 2014 12:37:30 PM tjb3>
//
//
// ================================================================

package main.gov.nist.isg.mist.stitching.gui.components.textfield.textFieldModel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Validator that checks values based on a regex
 * 
 * @author Tim Blattner
 * @version 1.0
 * 
 */
public class RegexModel implements TextFieldModel<String> {

  private Pattern pattern;
  private String errorText;

  /**
   * Creates a regex validator based on a given regex
   * 
   * @param regex the regex to check
   * @param errorText the error text associated with this validator
   */
  public RegexModel(String regex, String errorText) {
    this.pattern = Pattern.compile(regex);
    this.errorText = errorText;
  }

  @Override
  public boolean validateText(String val) {
    Matcher matcher = this.pattern.matcher(val);
    if (!matcher.find() || matcher.groupCount() != 3) {
      return false;
    }
    return true;
  }

  @Override
  public String getErrorMessage() {
    return this.errorText;
  }

  @Override
  public String getModelValue(String val) {
    return val;
  }

  @Override
  public String setModelValue(String val) {
    return val;
  }

  @Override
  public void updateTextFields() {    
  }


}
