package gui;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import org.apache.log4j.Logger;

import controller.Controller;
import gui.items.persons.AllPersonsFrame;
import gui.items.persons.IssuePersonFrame;
import gui.items.persons.PersonsPanel;
import gui.items.persons.SearchPersons;
import gui.settings.SettingsFrame;
import lang.Lang;
import settings.Settings;
import utils.URLViewer;
import gui.MainFrame;

public class Menu extends JMenuBar 
{
	private static final long serialVersionUID = 5237335232850181080L;
	public static JMenuItem webServerItem;
	public static JMenuItem blockExplorerItem;
	public static JMenuItem lockItem;
	private ImageIcon lockedIcon;
	private ImageIcon unlockedIcon;
	private static final Logger LOGGER = Logger.getLogger(Menu.class);

	public Menu()
	{
		super();
		
		//FILE MENU
        JMenu fileMenu = new JMenu(Lang.getInstance().translate("File"));
        fileMenu.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("File menu"));
        this.add(fileMenu);
        
      

        //LOCK

        //LOAD IMAGES
		try {
			BufferedImage lockedImage = ImageIO.read(new File("images/wallet/locked.png"));
			this.lockedIcon = new ImageIcon(lockedImage.getScaledInstance(20, 16, Image.SCALE_SMOOTH));
	
			BufferedImage unlockedImage = ImageIO.read(new File("images/wallet/unlocked.png"));
			this.unlockedIcon = new ImageIcon(unlockedImage.getScaledInstance(20, 16, Image.SCALE_SMOOTH));
		} catch (IOException e2) {
			LOGGER.error(e2.getMessage(),e2);
		}
		
        lockItem = new JMenuItem("lock");
        lockItem.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Lock/Unlock Wallet"));
        lockItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.ALT_MASK));
        
        lockItem.addActionListener(new ActionListener()
        {
        	
        	public void actionPerformed(ActionEvent e)
        	{
				PasswordPane.switchLockDialog();
        	}
        });
        fileMenu.add(lockItem);
        
        //SEPARATOR
        fileMenu.addSeparator();
        
        //CONSOLE
        JMenuItem consoleItem = new JMenuItem(Lang.getInstance().translate("Debug"));
        consoleItem.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Debug information"));
        consoleItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.ALT_MASK));
        consoleItem.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
                new DebugFrame();
        	}
        });
        fileMenu.add(consoleItem);
        
        //SETTINGS
        JMenuItem settingsItem = new JMenuItem(Lang.getInstance().translate("Settings"));
        settingsItem.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Settings of program"));
        settingsItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        settingsItem.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
                new SettingsFrame();
        	}
        });
        fileMenu.add(settingsItem);        

        //WEB SERVER
        webServerItem = new JMenuItem(Lang.getInstance().translate("Decentralized Web server"));
        webServerItem.getAccessibleContext().setAccessibleDescription("http://127.0.0.1:"+Settings.getInstance().getWebPort());
        webServerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.ALT_MASK));
        webServerItem.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		try {
        			URLViewer.openWebpage(new URL("http://127.0.0.1:"+Settings.getInstance().getWebPort()));
				} catch (MalformedURLException e1) {
					LOGGER.error(e1.getMessage(),e1);
				}
        	}
        });
        fileMenu.add(webServerItem);   
        
        webServerItem.setVisible(Settings.getInstance().isWebEnabled());
        
        //WEB SERVER
        blockExplorerItem = new JMenuItem(Lang.getInstance().translate("Built-in BlockExplorer"));
        blockExplorerItem.getAccessibleContext().setAccessibleDescription("http://127.0.0.1:"+Settings.getInstance().getWebPort()+"/index/blockexplorer.html");
        blockExplorerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, ActionEvent.ALT_MASK));
        blockExplorerItem.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		try {
        			URLViewer.openWebpage(new URL("http://127.0.0.1:"+Settings.getInstance().getWebPort()+"/index/blockexplorer.html"));
				} catch (MalformedURLException e1) {
					LOGGER.error(e1.getMessage(),e1);
				}
        	}
        });
        fileMenu.add(blockExplorerItem);   
        
        blockExplorerItem.setVisible(Settings.getInstance().isWebEnabled());
        
        //ABOUT
        JMenuItem aboutItem = new JMenuItem(Lang.getInstance().translate("About"));
        aboutItem.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Information about the application"));
        aboutItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.ALT_MASK));
        aboutItem.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
                new AboutFrame();
        	}
        });
        fileMenu.add(aboutItem);
        
        //SEPARATOR
        fileMenu.addSeparator();
        
        //QUIT
        JMenuItem quitItem = new JMenuItem(Lang.getInstance().translate("Quit"));
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
        quitItem.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Quit the application"));
        quitItem.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		new ClosingDialog();
        	}
        });
       
        fileMenu.add(quitItem);    
        
        fileMenu.addMenuListener(new MenuListener()
        {
			@Override
			public void menuSelected(MenuEvent arg0) {
        		if(Controller.getInstance().isWalletUnlocked()) {
        			lockItem.setText(Lang.getInstance().translate("Lock Wallet"));
        			lockItem.setIcon(lockedIcon);
        		} else {
        			lockItem.setText(Lang.getInstance().translate("Unlock Wallet"));
        			lockItem.setIcon(unlockedIcon);
        		}
			}

			@Override
			public void menuCanceled(MenuEvent e) {
				
			}

			@Override
			public void menuDeselected(MenuEvent e) {
				
			}
        });
        
        /*//HELP MENU
        JMenu helpMenu = new JMenu("Help");
        helpMenu.getAccessibleContext().setAccessibleDescription("Help menu");
        this.add(helpMenu);
        
        //ABOUT
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.getAccessibleContext().setAccessibleDescription("Information about the application");
        helpMenu.add(aboutItem);  */ 
       
        // work menu
        
        JMenu PersonMenu = new JMenu(Lang.getInstance().translate("Persons"));
        PersonMenu.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Persons"));
        this.add(PersonMenu);
 /*        
        //Search person
        JMenuItem searchPerson = new JMenuItem(Lang.getInstance().translate("Search"));
        searchPerson.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Search Persons"));
   //     searchPerson.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        searchPerson.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
             //   new SettingsFrame();
        		
        		new SearchPersons();
        		
        		
        	}
        });
        workMenu.add(searchPerson);   
        
*/
        
        // меню Accounts
        JMenuItem accountsmenu = new JMenuItem(Lang.getInstance().translate("Accounts"));
        accountsmenu.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Accounts"));
   //     searchPerson.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        accountsmenu.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
             
        		// выводим окно если оно не отображено
        		selectOrAdd(new AccountsPanel(), MainFrame.desktopPane.getAllFrames());
        		
        	
        		
        	}
        });
        PersonMenu.add(accountsmenu);     

        // меню Persons
        JMenuItem Allpersonsmenu = new JMenuItem(Lang.getInstance().translate("All Persons"));
        Allpersonsmenu.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("All Persons"));
   //     searchPerson.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        Allpersonsmenu.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
             
        // выводим окно или делаем фокус если уже открыто
        		selectOrAdd( new AllPersonsFrame(), MainFrame.desktopPane.getAllFrames());
        		
        	}
        });
        PersonMenu.add(Allpersonsmenu);  
        
        // issue Person menu
        JMenuItem Issuepersonmenu = new JMenuItem(Lang.getInstance().translate("Issue Person"));
        Issuepersonmenu.getAccessibleContext().setAccessibleDescription(Lang.getInstance().translate("Issue Person"));
   //     searchPerson.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.ALT_MASK));
        Issuepersonmenu.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
             
        // выводим окно или делаем фокус если уже открыто
        		selectOrAdd( new IssuePersonFrame(), MainFrame.desktopPane.getAllFrames());
        		
        	}
        });
        PersonMenu.add(Issuepersonmenu);  
        
        
	}
	
	
	// подпрограмма выводит в панели окно или передает фокус если окно уже открыто
	// item открываемое окно
	// массив всех открытых окон в панели
	void selectOrAdd(JInternalFrame item, JInternalFrame[] a ){
		    		
		//проверка если уже открыто такое окно то передаем только фокус на него
		int k= -1;
		for (int i=0 ; i < a.length; i=i+1) {
//			String s = a[i].getClass().getName();
			if (a[i].getClass().getName() == item.getClass().getName()){
				k=i;
			}
			
		};
		if (k==-1){
		MainFrame.desktopPane.add(item);
		 try {
			 item.setSelected(true);
	        } catch (java.beans.PropertyVetoException e1) {}
		}
		else {
			try {
				a[k].setSelected(true);
			} catch (PropertyVetoException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}	
		
		
	
	}
}
