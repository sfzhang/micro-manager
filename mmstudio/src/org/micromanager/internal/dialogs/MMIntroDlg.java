///////////////////////////////////////////////////////////////////////////////
//FILE:          MMIntroDlg.java
//PROJECT:       Micro-Manager
//SUBSYSTEM:     mmstudio
//-----------------------------------------------------------------------------
//
// AUTHOR:       Nenad Amodaj, nenad@amodaj.com, Dec 1, 2005
//
// COPYRIGHT:    University of California, San Francisco, 2006
// COPYRIGHT:    University of California, San Francisco, 2006
//
// LICENSE:      This file is distributed under the BSD license.
//               License text is included with the source distribution.
//
//               This file is distributed in the hope that it will be useful,
//               but WITHOUT ANY WARRANTY; without even the implied warranty
//               of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
//
//               IN NO EVENT SHALL THE COPYRIGHT OWNER OR
//               CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
//               INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES.
//
// CVS:          $Id$

package org.micromanager.internal.dialogs;

import com.swtdesigner.SwingResourceManager;

import ij.IJ;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Arrays;
import java.util.Set;

import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.border.EtchedBorder;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;

import org.micromanager.internal.MMStudio;
import org.micromanager.internal.utils.DefaultUserProfile;
import org.micromanager.internal.utils.FileDialogs;
import org.micromanager.internal.utils.JavaUtils;
import org.micromanager.internal.utils.ReportingUtils;

/**
 * Splash screen and introduction dialog. 
 * Opens up at startup and allows selection of the configuration file.
 */
public class MMIntroDlg extends JDialog {
   private static final long serialVersionUID = 1L;
   private static final String USERNAME_NEW = "Create new profile";
   private static final String RECENTLY_USED_CONFIGS = "recently-used config files";
   private static final String GLOBAL_CONFIGS = "config files supplied from a central authority";
   private static final String SHOULD_ASK_FOR_CONFIG = "whether or not the intro dialog should include a prompt for the config file";
   private static final String DEFAULT_CONFIG_FILE_NAME = "MMConfig_demo.cfg";
   private final JTextArea welcomeTextArea_;
   private boolean okFlag_ = true;
   
   ArrayList<String> mruCFGFileList_;

   private JComboBox cfgFileDropperDown_;
   private JComboBox profileSelect_;
   
   public static String DISCLAIMER_TEXT = 
      
      "This software is distributed free of charge in the hope that it will be useful, " +
      "but WITHOUT ANY WARRANTY; without even the implied warranty " +
      "of merchantability or fitness for a particular purpose. In no event shall the copyright owner or contributors " +
      "be liable for any direct, indirect, incidental, special, examplary, or consequential damages.\n\n" +
      
      "Copyright University of California San Francisco, 2007, 2008, 2009, 2010. All rights reserved.";

   public static String SUPPORT_TEXT =
      "Micro-Manager was initially funded by grants from the Sandler Foundation and is now supported by a grant from the NIH.";

   public static String CITATION_TEXT =
      "If you have found this software useful, please cite Micro-Manager in your publications.";

   public MMIntroDlg(String ver) {
      super();
      setFont(new Font("Arial", Font.PLAIN, 10));
      setTitle("Micro-Manager Startup");
      getContentPane().setLayout(null);
      setName("Intro");
      setResizable(false);
      setModal(true);
      setUndecorated(true);
      if (! IJ.isMacOSX())
        ((JPanel) getContentPane()).setBorder(BorderFactory.createLineBorder(Color.GRAY));
      setSize(new Dimension(392, 573));
      Dimension winSize = getSize();
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      setLocation(screenSize.width/2 - (winSize.width/2), screenSize.height/2 - (winSize.height/2));

      JLabel introImage = new JLabel();
      introImage.setIcon(SwingResourceManager.getIcon(MMIntroDlg.class, "/org/micromanager/internal/icons/splash.gif"));
      introImage.setLayout(null);
      introImage.setBounds(0, 0, 392, 197);
      introImage.setFocusable(false);
      introImage.setBorder(new LineBorder(Color.black, 1, false));
      introImage.setText("New JLabel");
      getContentPane().add(introImage);

      final JButton okButton = new JButton();
      okButton.setFont(new Font("Arial", Font.PLAIN, 10));
      okButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            okFlag_ = true;
            setVisible(false);
         }
      });
      okButton.setText("OK");
      okButton.setBounds(JavaUtils.isMac() ? 200 : 100, 537, 81, 24);
      getContentPane().add(okButton);
      getRootPane().setDefaultButton(okButton);
      okButton.requestFocusInWindow();
      
      final JButton cancelButton = new JButton();
      cancelButton.setFont(new Font("Arial", Font.PLAIN, 10));
      cancelButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            okFlag_ = false;
            setVisible(false);
         }
      });
      cancelButton.setText("Cancel");
      cancelButton.setBounds(JavaUtils.isMac() ? 100 : 200, 537, 81, 24);
      getContentPane().add(cancelButton);     

      final JLabel microscopeManagerLabel = new JLabel();
      microscopeManagerLabel.setFont(new Font("", Font.BOLD, 12));
      microscopeManagerLabel.setText("Micro-Manager startup configuration");
      microscopeManagerLabel.setBounds(5, 198, 259, 22);
      getContentPane().add(microscopeManagerLabel);

      final JLabel version10betaLabel = new JLabel();
      version10betaLabel.setFont(new Font("Arial", Font.PLAIN, 10));
      version10betaLabel.setText("MMStudio Version " + ver);
      version10betaLabel.setBounds(5, 216, 193, 13);
      getContentPane().add(version10betaLabel);

      if (!DefaultUserProfile.getShouldAlwaysUseDefaultProfile()) {
         addProfileDropdown();
      }

      if (getShouldAskForConfigFile()) {
         addConfigFileSelect();
      }

      welcomeTextArea_ = new JTextArea() {
         @Override
         public Insets getInsets() {
            return new Insets(10,10,10,10);
         }
      };
      welcomeTextArea_.setBorder(new EtchedBorder());
      welcomeTextArea_.setWrapStyleWord(true);
      welcomeTextArea_.setText(DISCLAIMER_TEXT + "\n\n" + SUPPORT_TEXT + "\n\n" + CITATION_TEXT);

      welcomeTextArea_.setLineWrap(true);
      welcomeTextArea_.setFont(new Font("Arial", Font.PLAIN, 10));
      welcomeTextArea_.setFocusable(false);
      welcomeTextArea_.setEditable(false);
      welcomeTextArea_.setBackground(Color.WHITE);
      welcomeTextArea_.setBounds(10, 324, 356, 205);
      getContentPane().add(welcomeTextArea_);

   }

   private void addConfigFileSelect() {
      final JLabel loadConfigurationLabel = new JLabel();
      loadConfigurationLabel.setFont(new Font("Arial", Font.PLAIN, 10));
      loadConfigurationLabel.setText("Configuration file:");
      loadConfigurationLabel.setBounds(5, 270, 319, 19);
      getContentPane().add(loadConfigurationLabel);

      final JButton browseButton = new JButton();
      browseButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            loadConfigFile();
         }
      });
      browseButton.setText("...");
      browseButton.setBounds(350, 287, 36, 26);
      getContentPane().add(browseButton);

      cfgFileDropperDown_ = new JComboBox();
      cfgFileDropperDown_.setFont(new Font("Arial", Font.PLAIN, 10));
      cfgFileDropperDown_.setBounds(5, 287, 342, 26);
      getContentPane().add(cfgFileDropperDown_);
   }

   private void addProfileDropdown() {
      JLabel userProfileLabel = new JLabel("User profile:");
      Font stdFont = new Font("Arial", Font.PLAIN, 10);
      userProfileLabel.setFont(stdFont);
      userProfileLabel.setBounds(5, 228, 319, 19);
      getContentPane().add(userProfileLabel);

      final DefaultUserProfile profile = DefaultUserProfile.getInstance();
      Set<String> profiles = profile.getProfileNames();
      final ArrayList<String> profilesAsList = new ArrayList<String>(profiles);
      // HACK: put the "new" and "default" options first in the list.
      profilesAsList.remove(DefaultUserProfile.DEFAULT_USER);
      profilesAsList.add(0, DefaultUserProfile.DEFAULT_USER);
      profilesAsList.add(0, USERNAME_NEW);
      profileSelect_ = new JComboBox();
      profileSelect_.setFont(stdFont);
      for (String profileName : profilesAsList) {
         profileSelect_.addItem(profileName);
      }
      profileSelect_.setSelectedItem(DefaultUserProfile.DEFAULT_USER);
      profileSelect_.setBounds(5, 244, 342, 26);
      profileSelect_.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            String profileName = (String) profileSelect_.getSelectedItem();
            if (profileName.contentEquals(USERNAME_NEW)) {
               // Prompt the user for the new profile name.
               profileName = JOptionPane.showInputDialog("Please input the new profile name:");
               if (profilesAsList.contains(profileName)) {
                  ReportingUtils.showError("That profile name is already in use.");
               }
               else {
                  profilesAsList.add(profileName);
                  profileSelect_.addItem(profileName);
                  profile.addProfile(profileName);
               }
               // TODO: will this re-invoke our listener, causing us to call
               // setConfigFile twice?
               profileSelect_.setSelectedItem(profileName);
            }
            // Set the current active profile.
            profile.setCurrentProfile(profileName);
            // Update the list of hardware config files.
            setConfigFile(null);
         }
      });
      getContentPane().add(profileSelect_);
   }

   public boolean okChosen() {
      return okFlag_;
   }

   // Add a new config file to the dropdown menu.
   public void setConfigFile(String path) {
      if (cfgFileDropperDown_ == null) {
         // Prompting for config files is disabled.
         return;
      }
      cfgFileDropperDown_.removeAllItems();
      DefaultUserProfile profile = DefaultUserProfile.getInstance();
      ArrayList<String> configs = new ArrayList<String>(
            Arrays.asList(profile.getStringArray(MMIntroDlg.class,
               RECENTLY_USED_CONFIGS, new String[0])));
      Boolean doesExist = false;
      if (path != null) {
         doesExist = new File(path).exists();
      }
      if (doesExist) {
         // Update the user's preferences for recently-used config files.
         configs.add(path);
         // Don't remember more than six profile-specific config files.
         if (configs.size() > 6) {
            configs.remove(configs.get(0));
         }
         String[] tmp = new String[configs.size()];
         tmp = configs.toArray(tmp);
         profile.setStringArray(MMIntroDlg.class,
               RECENTLY_USED_CONFIGS, tmp);
      }

      // Add on global default configs.
      configs.addAll(Arrays.asList(profile.getStringArray(MMIntroDlg.class,
              GLOBAL_CONFIGS,
              new String[] {new File(DEFAULT_CONFIG_FILE_NAME).getAbsolutePath()})));
      for (String config : configs) {
         cfgFileDropperDown_.addItem(config);
      }
      cfgFileDropperDown_.addItem("(none)");

      if (doesExist) {
         cfgFileDropperDown_.setSelectedItem(path);
      }
      else {
         cfgFileDropperDown_.setSelectedItem(configs.size() - 1);
      }
   }
      
   public String getConfigFile() {
      if (cfgFileDropperDown_ == null) {
         // Prompting for config files is disabled.
         return "";
      }
       String tvalue = cfgFileDropperDown_.getSelectedItem().toString();
       String nvalue = "(none)";
       if( nvalue.equals(tvalue))
           tvalue = "";

      return tvalue;
   }

   public String getUserName() {
      return (String) profileSelect_.getSelectedItem();
   }
   
   public String getScriptFile() {
      return "";
   }
   
   // User wants to use a file browser to select a hardware config file.
   protected void loadConfigFile() {
      File f = FileDialogs.openFile(this, "Choose a config file", MMStudio.MM_CONFIG_FILE);
      if (f != null) {
         setConfigFile(f.getAbsolutePath());
      }
   }

   public static boolean getShouldAskForConfigFile() {
      return DefaultUserProfile.getInstance().getBoolean(MMIntroDlg.class,
            SHOULD_ASK_FOR_CONFIG, true);
   }

   public static void setShouldAskForConfigFile(boolean shouldAsk) {
      DefaultUserProfile.getInstance().setBoolean(MMIntroDlg.class,
            SHOULD_ASK_FOR_CONFIG, shouldAsk);
   }
}