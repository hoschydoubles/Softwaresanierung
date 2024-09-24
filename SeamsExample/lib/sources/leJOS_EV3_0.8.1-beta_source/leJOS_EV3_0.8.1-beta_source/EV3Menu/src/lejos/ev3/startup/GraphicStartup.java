package lejos.ev3.startup;

import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarFile;

import lejos.ev3.startup.GraphicListMenu;
import lejos.ev3.startup.Utils;
import lejos.utility.Delay;
import lejos.ev3.startup.Config;
import lejos.hardware.Bluetooth;
import lejos.hardware.BrickFinder;
import lejos.hardware.Button;
import lejos.hardware.LocalBTDevice;
import lejos.hardware.LocalWifiDevice;
import lejos.hardware.RemoteBTDevice;
import lejos.hardware.Sound;
import lejos.hardware.Wifi;
import lejos.hardware.ev3.LocalEV3;
import lejos.hardware.lcd.Font;
import lejos.hardware.lcd.GraphicsLCD;
import lejos.hardware.lcd.Image;
import lejos.hardware.lcd.LCD;
import lejos.hardware.lcd.LCDOutputStream;
import lejos.hardware.lcd.TextLCD;
import lejos.hardware.port.Port;
import lejos.hardware.port.TachoMotorPort;
import lejos.internal.io.Settings;
import lejos.internal.io.SystemSettings;
import lejos.remote.ev3.Menu;
import lejos.remote.ev3.MenuReply;
import lejos.remote.ev3.MenuRequest;
import lejos.remote.ev3.RMIRemoteEV3;

public class GraphicStartup implements Menu {
	private static final int REMOTE_MENU_PORT = 8002;
	
	private static final String JAVA_RUN_CP = "jrun -cp ";
	private static final String JAVA_DEBUG_CP = "jrun -Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=y -cp ";
	
	private static final int TYPE_PROGRAM = 0;
	private static final int TYPE_SAMPLE = 1;
	private static final int TYPE_TOOL = 2;
	
	private static final String defaultProgramProperty = "lejos.default_program";
    private static final String defaultProgramAutoRunProperty = "lejos.default_autoRun";
    private static final String sleepTimeProperty = "lejos.sleep_time";
    private static final String pinProperty = "lejos.bluetooth_pin";
    private static final String ntpProperty = "lejos.ntp_host";
	
    private static final String ICMProgram = "\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0003\u00c0\u0003\u0000\u0003\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u000c\u00c0\u0003\u00c0\u000c\u00c0\u0003\u0030\u000c\u00c0\u0003\u0030\u000c\u00c0\u0003\u0030\u0003\u00c0\u0003\u0030\u0003\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00ff\u00cf\u00c3\u0003\u00ff\u00cf\u00c3\u0003\u0000\u0000\u00c3\u0003\u0000\u0000\u00c3\u0003\u00fc\u00f3\u00c0\u0003\u00fc\u00f3\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u00c3\u00ff\u003f\u00c0\u00c3\u00ff\u003f\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f";
    private static final String ICMSound = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00c0\u0000\u0003\u0000\u00c0\u0000\u0003\u0000\u00f0\u0030\u000c\u0000\u00f0\u0030\u000c\u0000\u00cc\u00c0\u0030\u0000\u00cc\u00c0\u0030\u0000\u00c3\u000c\u0033\u0000\u00c3\u000c\u0033\u00fc\u00c3\u0030\u0033\u00fc\u00c3\u0030\u0033\u000c\u00c3\u0030\u0033\u000c\u00c3\u0030\u0033\u000c\u00c3\u0030\u0033\u000c\u00c3\u0030\u0033\u003c\u00c3\u0030\u0033\u003c\u00c3\u0030\u0033\u00cc\u00cf\u0030\u0033\u00cc\u00cf\u0030\u0033\u00fc\u00f3\u0030\u0033\u00fc\u00f3\u0030\u0033\u0000\u00cf\u000c\u0033\u0000\u00cf\u000c\u0033\u0000\u00fc\u00c0\u0030\u0000\u00fc\u00c0\u0030\u0000\u00f0\u0030\u000c\u0000\u00f0\u0030\u000c\u0000\u00c0\u0000\u0003\u0000\u00c0\u0000\u0003\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    private static final String ICMFile = "\u0000\u00c0\u0000\u0000\u0000\u00c0\u0000\u0000\u0000\u0030\u00ff\u000f\u0000\u0030\u00ff\u000f\u0000\u000c\u000c\u0030\u0000\u000c\u000c\u0030\u00fc\u0003\u0030\u00cc\u00fc\u0003\u0030\u00cc\u00c3\u0000\u00c0\u00f0\u00c3\u0000\u00c0\u00f0\u003f\u0000\u0000\u00c3\u003f\u0000\u0000\u00c3\u00ff\u003f\u0000\u00fc\u00ff\u003f\u0000\u00fc\u0003\u00c0\u0000\u00f0\u0003\u00c0\u0000\u00f0\u0003\u0000\u00ff\u00ff\u0003\u0000\u00ff\u00ff\u0003\u0000\u0000\u00f3\u0003\u0000\u0000\u00f3\u0003\u0000\u00c0\u00cc\u0003\u0000\u00c0\u00cc\u0003\u0000\u0000\u00f3\u0003\u0000\u0000\u00f3\u0003\u0000\u0000\u00cc\u0003\u0000\u0000\u00cc\u0003\u0000\u0000\u00f3\u0003\u0000\u0000\u00f3\u0003\u0000\u00c0\u00cc\u0003\u0000\u00c0\u00cc\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f";

    private static final String ICDefault = "\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0003\u00c0\u0003\u0000\u0003\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u000c\u00c0\u0003\u00c0\u000c\u00c0\u0003\u0030\u000c\u00c0\u0003\u0030\u000c\u00c0\u0003\u0030\u0003\u00c0\u0003\u0030\u0003\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u0000\u00c0\u00f3\u00cf\u00cf\u00c3\u00f3\u00cf\u00cf\u00c3\u00c3\u000f\u0000\u00c3\u00c3\u000f\u0000\u00c3\u00f3\u00cf\u00f3\u00c0\u00f3\u00cf\u00f3\u00c0\u00f3\u000c\u0000\u00c0\u00f3\u000c\u0000\u00c0\u00f3\u00c3\u003f\u00c0\u00f3\u00c3\u003f\u00c0\u00c3\u0003\u0000\u00c0\u00c3\u0003\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f";
    private static final String ICProgram = "\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0003\u00c0\u0003\u0000\u0003\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u000c\u00c0\u0003\u00c0\u000c\u00c0\u0003\u0030\u000c\u00c0\u0003\u0030\u000c\u00c0\u0003\u0030\u0003\u00c0\u0003\u0030\u0003\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00c0\u0000\u00c0\u0003\u00ff\u00cf\u00c3\u0003\u00ff\u00cf\u00c3\u0003\u0000\u0000\u00c3\u0003\u0000\u0000\u00c3\u0003\u00fc\u00f3\u00c0\u0003\u00fc\u00f3\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u00c3\u00ff\u003f\u00c0\u00c3\u00ff\u003f\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u0000\u00c0\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f";
    private static final String ICFiles = "\u0000\u00c0\u0000\u0000\u0000\u00c0\u0000\u0000\u0000\u0030\u00ff\u000f\u0000\u0030\u00ff\u000f\u0000\u000c\u000c\u0030\u0000\u000c\u000c\u0030\u00fc\u0003\u0030\u00cc\u00fc\u0003\u0030\u00cc\u00c3\u0000\u00c0\u00f0\u00c3\u0000\u00c0\u00f0\u003f\u0000\u0000\u00c3\u003f\u0000\u0000\u00c3\u00ff\u003f\u0000\u00fc\u00ff\u003f\u0000\u00fc\u0003\u00c0\u0000\u00f0\u0003\u00c0\u0000\u00f0\u0003\u0000\u00ff\u00ff\u0003\u0000\u00ff\u00ff\u0083\u0007\u0000\u00f3\u0083\u0008\u0000\u00f3\u0083\u0008\u00c0\u00cc\u0083\u0008\u00c0\u00cc\u0083\u0007\u0000\u00f3\u0083\u0000\u0000\u00f3\u0083\u0000\u0000\u00cc\u0083\u0000\u0000\u00cc\u0083\u0000\u0000\u00f3\u0083\u0000\u0000\u00f3\u0003\u0000\u00c0\u00cc\u0003\u0000\u00c0\u00cc\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f";
    private static final String ICSamples = "\u0000\u00c0\u0000\u0000\u0000\u00c0\u0000\u0000\u0000\u0030\u00ff\u000f\u0000\u0030\u00ff\u000f\u0000\u000c\u000c\u0030\u0000\u000c\u000c\u0030\u00fc\u0003\u0030\u00cc\u00fc\u0003\u0030\u00cc\u00c3\u0000\u00c0\u00f0\u00c3\u0000\u00c0\u00f0\u003f\u0000\u0000\u00c3\u003f\u0000\u0000\u00c3\u00ff\u003f\u0000\u00fc\u00ff\u003f\u0000\u00fc\u0003\u00c0\u0000\u00f0\u0003\u00c0\u0000\u00f0\u0003\u0000\u00ff\u00ff\u0003\u001e\u00ff\u00ff\u0003\u0001\u0000\u00f3\u0003\u0001\u0000\u00f3\u0003\u0001\u00c0\u00cc\u0003\u001e\u00c0\u00cc\u0003\u0010\u0000\u00f3\u0003\u0010\u0000\u00f3\u0003\u0010\u0000\u00cc\u0003\u000f\u0000\u00cc\u0003\u0000\u0000\u00f3\u0003\u0000\u0000\u00f3\u0003\u0000\u00c0\u00cc\u0003\u0000\u00c0\u00cc\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f";
    private static final String ICTools = "\u0000\u00c0\u0000\u0000\u0000\u00c0\u0000\u0000\u0000\u0030\u00ff\u000f\u0000\u0030\u00ff\u000f\u0000\u000c\u000c\u0030\u0000\u000c\u000c\u0030\u00fc\u0003\u0030\u00cc\u00fc\u0003\u0030\u00cc\u00c3\u0000\u00c0\u00f0\u00c3\u0000\u00c0\u00f0\u003f\u0000\u0000\u00c3\u003f\u0000\u0000\u00c3\u00ff\u003f\u0000\u00fc\u00ff\u003f\u0000\u00fc\u0003\u00c0\u0000\u00f0\u0003\u00c0\u0000\u00f0\u0003\u0000\u00ff\u00ff\u0003\u0000\u00ff\u00ff\u0003\u0000\u0000\u00f3\u0003\u00fe\u0000\u00f3\u0003\u0010\u00c0\u00cc\u0003\u0010\u00c0\u00cc\u0003\u0010\u0000\u00f3\u0003\u0010\u0000\u00f3\u0003\u0010\u0000\u00cc\u0003\u0010\u0000\u00cc\u0003\u0010\u0000\u00f3\u0003\u0000\u0000\u00f3\u0003\u0000\u00c0\u00cc\u0003\u0000\u00c0\u00cc\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f";
    private static final String ICBlue = "\u0000\u00f0\u000f\u0000\u0000\u00f0\u000f\u0000\u0000\u00ff\u00ff\u0000\u0000\u00ff\u00ff\u0000\u00c0\u003f\u00ff\u0003\u00c0\u003f\u00ff\u0003\u00c0\u003f\u00fc\u0003\u00c0\u003f\u00fc\u0003\u00f0\u003c\u00f0\u000f\u00f0\u003c\u00f0\u000f\u00f0\u0030\u00c3\u000f\u00f0\u0030\u00c3\u000f\u00f0\u0003\u00c3\u000f\u00f0\u0003\u00c3\u000f\u00f0\u000f\u00f0\u000f\u00f0\u000f\u00f0\u000f\u00f0\u000f\u00f0\u000f\u00f0\u000f\u00f0\u000f\u00f0\u0003\u00c3\u000f\u00f0\u0003\u00c3\u000f\u00f0\u0030\u00c3\u000f\u00f0\u0030\u00c3\u000f\u00f0\u003c\u00f0\u000f\u00f0\u003c\u00f0\u000f\u00c0\u003f\u00fc\u0003\u00c0\u003f\u00fc\u0003\u00c0\u003f\u00ff\u0003\u00c0\u003f\u00ff\u0003\u0000\u00ff\u00ff\u0000\u0000\u00ff\u00ff\u0000\u0000\u00f0\u000f\u0000\u0000\u00f0\u000f\u0000";
    private static final String ICWifi = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00f8\u001f\u0000\u0000\u00ff\u00ff\u0000\u00c0\u00ff\u00ff\u0003\u00f0\u00ff\u00ff\u000f\u00f8\u003f\u00fc\u001f\u00fe\u0003\u00c0\u007f\u00ff\u0000\u0000\u00ff\u003f\u0000\u0000\u00fc\u001f\u0000\u0000\u00f8\u000e\u00f8\u001f\u0070\u0000\u00fe\u007f\u0000\u0000\u00ff\u00ff\u0000\u0080\u00ff\u00ff\u0001\u00c0\u003f\u00fc\u0003\u00c0\u0007\u00e0\u0003\u00c0\u0003\u00c0\u0001\u0000\u0000\u0000\u0000\u0000\u00c0\u0003\u0000\u0000\u00e0\u0007\u0000\u0000\u00e0\u0007\u0000\u0000\u00e0\u0007\u0000\u0000\u00e0\u0007\u0000\u0000\u00c0\u0003\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
   
    private static final String ICSound = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00c0\u0000\u0003\u0000\u00c0\u0000\u0003\u0000\u00f0\u0030\u000c\u0000\u00f0\u0030\u000c\u0000\u00cc\u00c0\u0030\u0000\u00cc\u00c0\u0030\u0000\u00c3\u000c\u0033\u0000\u00c3\u000c\u0033\u00fc\u00c3\u0030\u0033\u00fc\u00c3\u0030\u0033\u000c\u00c3\u0030\u0033\u000c\u00c3\u0030\u0033\u000c\u00c3\u0030\u0033\u000c\u00c3\u0030\u0033\u003c\u00c3\u0030\u0033\u003c\u00c3\u0030\u0033\u00cc\u00cf\u0030\u0033\u00cc\u00cf\u0030\u0033\u00fc\u00f3\u0030\u0033\u00fc\u00f3\u0030\u0033\u0000\u00cf\u000c\u0033\u0000\u00cf\u000c\u0033\u0000\u00fc\u00c0\u0030\u0000\u00fc\u00c0\u0030\u0000\u00f0\u0030\u000c\u0000\u00f0\u0030\u000c\u0000\u00c0\u0000\u0003\u0000\u00c0\u0000\u0003\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";

    private static final String ICEV3 = "\u00c0\u00ff\u00ff\u0003\u00c0\u00ff\u00ff\u0003\u00f0\u00ff\u00ff\u000f\u00f0\u00ff\u00ff\u000f\u0030\u0000\u0000\u000c\u0030\u0000\u0000\u000c\u0030\u00ff\u00ff\u000c\u0030\u00ff\u00ff\u000c\u0030\u0003\u00c0\u000c\u0030\u0003\u00c0\u000c\u0030\u000f\u00c0\u000c\u0030\u000f\u00c0\u000c\u0030\u0033\u00c0\u000c\u0030\u0033\u00c0\u000c\u0030\u00cf\u00cc\u000c\u0030\u00cf\u00cc\u000c\u0030\u00ff\u00ff\u000c\u0030\u00ff\u00ff\u000c\u0030\u0000\u0000\u000c\u0030\u0000\u0000\u000c\u0030\u00cf\u00f3\u000c\u0030\u00cf\u00f3\u000c\u0030\u00cc\u0033\u000c\u0030\u00cc\u0033\u000c\u00f0\u00c0\u0003\u000c\u00f0\u00c0\u0003\u000c\u0030\u0033\u0000\u000c\u0030\u0033\u0000\u000c\u00f0\u00ff\u00ff\u000f\u00f0\u00ff\u00ff\u000f\u00c0\u00ff\u00ff\u0003\u00c0\u00ff\u00ff\u0003";
    private static final String ICDebug = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00e0\u0001\u0080\u0007\u00e0\u00e1\u00c7\u0007\u0000\u00f3\u00ee\u0000\u0000\u00ff\u007f\u0000\u0000\u00de\u003f\u0000\u0000\u00fa\u0077\u0000\u0000\u007f\u00ff\u0000\u0000\u00ff\u00ff\u0000\u0008\u00ef\u00fd\u0010\u001c\u00ff\u00df\u0038\u003c\u007e\u007f\u001c\u0078\u00fc\u003f\u001e\u00f0\u00f8\u001f\u000f\u00e0\u00e1\u0087\u0007\u00e0\u0003\u00c0\u0007\u00f0\u000f\u00f0\u000f\u00fc\u00ff\u00ff\u007f\u00ff\u00ff\u00ff\u00ff\u00ff\u00fd\u00bf\u00ff\u00fe\u00f8\u001f\u007f\u00f2\u00f8\u001f\u002f\u00e0\u00fd\u00bf\u0007\u00e0\u007f\u00ff\u0007\u00f0\u003f\u00fe\u000f\u00f8\u003f\u00fe\u001f\u00fc\u007f\u00ff\u003f\u003c\u00ff\u00ff\u003c\u0018\u00fe\u007f\u0018\u0000\u007c\u003e\u0000\u0000\u0060\u0006\u0000";
    private static final String ICLeJOS = "\u0000\u0000\u00fc\u000f\u0000\u0000\u00fc\u000f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00c0\u003f\u0000\u0000\u00c0\u003f\u0000\u0000\u00c0\u003f\u0000\u0000\u00c0\u003f\u0000\u00c0\u00cc\u003f\u0000\u00c0\u00cc\u003f\u0000\u0030\u00c3\u003f\u0000\u0030\u00c3\u003f\u0000\u00c0\u00cc\u003f\u0000\u00c0\u00cc\u003f\u00fc\u0033\u00c3\u003f\u00fc\u0033\u00c3\u003f\u00fc\u0003\u00c0\u003f\u00fc\u0003\u00c0\u003f\u00fc\u0003\u00c0\u003f\u00fc\u0003\u00c0\u003f\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f\u00fc\u00ff\u00ff\u003f\u00f0\u00ff\u00ff\u000f\u00f0\u00ff\u00ff\u000f\u0000\u00ff\u00ff\u0000\u0000\u00ff\u00ff\u0000";

    private static final String ICPower = "\u0000\u00c0\u0003\u0000\u0000\u00c0\u0003\u0000\u00c0\u00cf\u00f3\u0003\u00c0\u00cf\u00f3\u0003\u00f0\u00cf\u00f3\u000f\u00f0\u00cf\u00f3\u000f\u00fc\u00c3\u00c3\u003f\u00fc\u00c3\u00c3\u003f\u00fc\u00c0\u0003\u003f\u00fc\u00c0\u0003\u003f\u00ff\u00c0\u0003\u00ff\u00ff\u00c0\u0003\u00ff\u003f\u00c0\u0003\u00fc\u003f\u00c0\u0003\u00fc\u003f\u00c0\u0003\u00fc\u003f\u00c0\u0003\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u00ff\u0000\u0000\u00ff\u00ff\u0000\u0000\u00ff\u00fc\u0000\u0000\u003f\u00fc\u0000\u0000\u003f\u00fc\u000f\u00f0\u003f\u00fc\u000f\u00f0\u003f\u00f0\u00ff\u00ff\u000f\u00f0\u00ff\u00ff\u000f\u00c0\u00ff\u00ff\u0003\u00c0\u00ff\u00ff\u0003\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000";
    private static final String ICVisibility = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u00c0\u00ff\u00ff\u0003\u00c0\u00ff\u00ff\u0003\u00f0\u0003\u00c0\u000f\u00f0\u0003\u00c0\u000f\u00fc\u00c0\u0003\u003f\u00fc\u00c0\u0003\u003f\u003f\u00f0\u000f\u00fc\u003f\u00f0\u000f\u00fc\u003f\u00f0\u000f\u00fc\u003f\u00f0\u000f\u00fc\u00fc\u00c0\u0003\u003f\u00fc\u00c0\u0003\u003f\u00f0\u0003\u00c0\u000f\u00f0\u0003\u00c0\u000f\u00c0\u00ff\u00ff\u0003\u00c0\u00ff\u00ff\u0003\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    private static final String ICSearch = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00fc\u0003\u0000\u0000\u00fc\u0003\u0000\u00c0\u0003\u003c\u0000\u00c0\u0003\u003c\u0000\u00c0\u003c\u0030\u0000\u00c0\u003c\u0030\u0000\u0030\u000f\u00c0\u0000\u0030\u000f\u00c0\u0000\u0030\u0003\u00c0\u0000\u0030\u0003\u00c0\u0000\u0030\u0003\u00c0\u0000\u0030\u0003\u00c0\u0000\u0030\u0000\u00c0\u0000\u0030\u0000\u00c0\u0000\u00c0\u0000\u0030\u0000\u00c0\u0000\u0030\u0000\u00c0\u0003\u00fc\u0000\u00c0\u0003\u00fc\u0000\u0000\u00fc\u0033\u0003\u0000\u00fc\u0033\u0003\u0000\u0000\u00c0\u000c\u0000\u0000\u00c0\u000c\u0000\u0000\u0000\u0033\u0000\u0000\u0000\u0033\u0000\u0000\u0000\u003c\u0000\u0000\u0000\u003c\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    private static final String ICPIN = "\u0000\u0000\u00ff\u0003\u0000\u0000\u00ff\u0003\u0000\u00c0\u0000\u000c\u0000\u00c0\u0000\u000c\u0000\u0030\u0000\u0030\u0000\u0030\u0000\u0030\u0000\u000c\u00f0\u00c3\u0000\u000c\u00f0\u00c3\u0000\u000c\u0030\u00c3\u0000\u000c\u0030\u00c3\u0000\u000c\u00f0\u00c3\u0000\u000c\u00f0\u00c3\u0000\u000c\u0000\u00f0\u0000\u000c\u0000\u00f0\u0000\u000c\u0000\u00cc\u0000\u000c\u0000\u00cc\u0000\u0033\u0030\u0033\u0000\u0033\u0030\u0033\u00c0\u000c\u00cc\u000c\u00c0\u000c\u00cc\u000c\u0030\u00c3\u00ff\u0003\u0030\u00c3\u00ff\u0003\u00cc\u00c0\u0000\u0000\u00cc\u00c0\u0000\u0000\u0033\u00fc\u0000\u0000\u0033\u00fc\u0000\u0000\u000f\u000c\u0000\u0000\u000f\u000c\u0000\u0000\u0003\u000f\u0000\u0000\u0003\u000f\u0000\u0000\u00ff\u0000\u0000\u0000\u00ff\u0000\u0000\u0000";
    
    private static final String ICDelete = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00c0\u000f\u0000\u0000\u00c0\u000f\u0000\u00c0\u00ff\u00ff\u000f\u00c0\u00ff\u00ff\u000f\u0030\u0000\u0000\u0030\u0030\u0000\u0000\u0030\u00c0\u00ff\u00ff\u000f\u00c0\u00ff\u00ff\u000f\u00c0\u0000\u0000\u000c\u00c0\u0000\u0000\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u000c\u000f\u00c0\u00cc\u000c\u000f\u00c0\u00cc\u00cc\u000c\u00c0\u00cc\u00cc\u000c\u00c0\u000c\u0030\u000f\u00c0\u000c\u0030\u000f\u00c0\u00c0\u00cc\u000c\u00c0\u00c0\u00cc\u000c\u0000\u00ff\u00ff\u0003\u0000\u00ff\u00ff\u0003\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    private static final String ICFormat = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u0003\u00c0\u0000\u0000\u0003\u00c0\u0000\u0000\u00f3\u00cf\u0000\u0000\u00f3\u00cf\u0000\u00c0\u000c\u0030\u0003\u00c0\u000c\u0030\u0003\u00c0\u0000\u0000\u0003\u00c0\u0000\u0000\u0003\u0030\u0003\u00c0\u000c\u0030\u0003\u00c0\u000c\u0030\u00fc\u003f\u000c\u0030\u00fc\u003f\u000c\u00f0\u00ff\u00ff\u000f\u00f0\u00ff\u00ff\u000f\u000c\u0000\u00cc\u0030\u000c\u0000\u00cc\u0030\u00cc\u00ff\u0000\u0030\u00cc\u00ff\u0000\u0030\u00f0\u00ff\u00ff\u000f\u00f0\u00ff\u00ff\u000f\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    private static final String ICSleep = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00fc\u0003\u0000\u0000\u00fc\u0003\u0000\u0000\u00c0\u00f0\u000f\u0000\u00c0\u00f0\u000f\u0000\u0030\u0000\u0033\u0000\u0030\u0000\u0033\u0000\u00fc\u00cf\u00c0\u0003\u00fc\u00cf\u00c0\u0003\u0000\u00f3\u000f\u000c\u0000\u00f3\u000f\u000c\u0000\u0003\u0000\u000c\u0000\u0003\u0000\u000c\u00c0\u000c\u000f\u0033\u00c0\u000c\u000f\u0033\u00c0\u00f0\u00f0\u0030\u00c0\u00f0\u00f0\u0030\u00c0\u0000\u0000\u0030\u00c0\u0000\u0000\u0030\u00c0\u0000\u0000\u0030\u00c0\u0000\u0000\u0030\u0000\u0003\u000f\u000c\u0000\u0003\u000f\u000c\u0000\u0003\u000f\u000c\u0000\u0003\u000f\u000c\u0000\u003c\u00c0\u0003\u0000\u003c\u00c0\u0003\u0000\u00c0\u003f\u0000\u0000\u00c0\u003f\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    private static final String ICAutoRun = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00ff\u00f0\u00ff\u00ff\u00ff\u00f0\u00ff\u00ff\u0000\u000c\u0000\u00c0\u0000\u000c\u0000\u00c0\u003f\u000c\u00c0\u00cc\u003f\u000c\u00c0\u00cc\u0000\u00ff\u00ff\u003f\u0000\u00ff\u00ff\u003f\u000f\u0003\u0000\u0030\u000f\u0003\u0000\u0030\u00c0\u0000\u0000\u000c\u00c0\u0000\u0000\u000c\u00c3\u0000\u0000\u000f\u00c3\u0000\u0000\u000f\u0030\u0000\u00cc\u0003\u0030\u0000\u00cc\u0003\u0033\u0030\u0033\u0003\u0033\u0030\u0033\u0003\u00f0\u00ff\u00ff\u0000\u00f0\u00ff\u00ff\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
 
    private static final String ICYes = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u000f\u0000\u0000\u0000\u000f\u0000\u0000\u00c0\u003f\u0000\u0000\u00c0\u003f\u0000\u0000\u00f0\u00ff\u0000\u0000\u00f0\u00ff\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0030\u0000\u00ff\u000f\u0030\u0000\u00ff\u000f\u00fc\u00c0\u00ff\u0003\u00fc\u00c0\u00ff\u0003\u00ff\u00f3\u00ff\u0000\u00ff\u00f3\u00ff\u0000\u00ff\u00ff\u003f\u0000\u00ff\u00ff\u003f\u0000\u00fc\u00ff\u000f\u0000\u00fc\u00ff\u000f\u0000\u00f0\u00ff\u0003\u0000\u00f0\u00ff\u0003\u0000\u00c0\u00ff\u0000\u0000\u00c0\u00ff\u0000\u0000\u0000\u003f\u0000\u0000\u0000\u003f\u0000\u0000\u0000\u000c\u0000\u0000\u0000\u000c\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    private static final String ICNo = "\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u00f0\u0000\u0000\u000f\u00f0\u0000\u0000\u000f\u00fc\u0003\u00c0\u003f\u00fc\u0003\u00c0\u003f\u00fc\u000f\u00f0\u003f\u00fc\u000f\u00f0\u003f\u00f0\u003f\u00fc\u000f\u00f0\u003f\u00fc\u000f\u00c0\u00ff\u00ff\u0003\u00c0\u00ff\u00ff\u0003\u0000\u00ff\u00ff\u0000\u0000\u00ff\u00ff\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00fc\u003f\u0000\u0000\u00ff\u00ff\u0000\u0000\u00ff\u00ff\u0000\u00c0\u00ff\u00ff\u0003\u00c0\u00ff\u00ff\u0003\u00f0\u003f\u00fc\u000f\u00f0\u003f\u00fc\u000f\u00fc\u000f\u00f0\u003f\u00fc\u000f\u00f0\u003f\u00fc\u0003\u00c0\u003f\u00fc\u0003\u00c0\u003f\u00f0\u0000\u0000\u000f\u00f0\u0000\u0000\u000f\u0000\u0000\u0000\u0000\u0000\u0000\u0000\u0000";
    
    private static final String PROGRAMS_DIRECTORY = "/home/lejos/programs";
    private static final String SAMPLES_DIRECTORY = "/home/root/lejos/samples";
    private static final String TOOLS_DIRECTORY = "/home/root/lejos/tools";
    private static final String MENU_DIRECTORY = "/home/root/lejos/bin/utils";
    private static final String START_BLUETOOTH = "/home/root/lejos/bin/startbt";
    private static final String START_WLAN = "/home/root/lejos/bin/startwlan";
    
    private static final int defaultSleepTime = 2;
    private static final int maxSleepTime = 10;

    // Threads
    private IndicatorThread ind = new IndicatorThread();
    private BatteryIndicator indiBA = new BatteryIndicator();
    private PipeReader pipeReader = new PipeReader();
    private RConsole rcons = new RConsole();
    private BroadcastThread broadcast = new BroadcastThread();
    private RemoteMenuThread remoteMenuThread = new RemoteMenuThread();
    
    //private GraphicMenu curMenu;
    private int timeout = 0;
    private boolean btVisibility;
    private static String version = "Unknown";
    private static String hostname;
    private static List<String> ips = getIPAddresses();
    private static LocalBTDevice bt;
	private static GraphicStartup menu = new GraphicStartup();
	
	private static TextLCD lcd = LocalEV3.get().getTextLCD();
	
	private static Process program; // the running user program, if any
	private static String programName; // The name of the running program
	
	private static boolean suspend = false;
	private static EchoThread echoIn, echoErr;
	
	private static GraphicMenu curMenu;
    
    /**
     * Main method
     */
	public static void main(String[] args) throws Exception {
		Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
		System.out.println("Menu started");
		
        if (args.length > 0) {    	
        	hostname = args[0];
        }
        
        if (args.length > 1) {    	
        	version = args[1];
        }
        
        System.out.println("Host name: " + hostname);
        System.out.println("Version: " + version);
        
        // Check for autorun
    	File file = getDefaultProgram();
    	if (file != null)
    	{
        	String auto = Settings.getProperty(defaultProgramAutoRunProperty, "");        	
    		if (auto.equals("ON") && !Button.LEFT.isDown())
            {
            	System.out.println("Auto executing default program " + file.getPath());
				try {
					JarFile jar = new JarFile(file);
					String mainClass = jar.getManifest().getMainAttributes().getValue("Main-class");
					jar.close();
					exec(file, JAVA_RUN_CP + file.getPath() + " lejos.internal.ev3.EV3Wrapper " + mainClass, PROGRAMS_DIRECTORY);
				} catch (IOException e) {
					System.err.println("Exception running program");
				}
            }
    	}
        
        TuneThread tuneThread = new TuneThread();
        tuneThread.start();
        
        System.out.println("Getting IP addresses");
        ips = getIPAddresses();
        
        // Start the RMI registry 
        InitThread initThread = new InitThread();
        initThread.start();
   
        System.out.println("Starting background threads");
        menu.start();  
        
        System.out.println("Starting the menu");
        menu.mainMenu();
        
        System.out.println("Menu finished");    
        System.exit(0);
	}
	
	
	/**
	 * Start-up thread
	 */
    static class InitThread extends Thread
    {
        /**
         * Create the Bluetooth local device and connect to DBus
         * 
         * Start the RMI server
         * 
         * Broadcast device availability
         * 
         * Get the time from a name server
         */            
        @Override
        public void run()
        {               
        	// Create the Bluetooth local device and connect to DBus
            System.out.println("Creating bluetooth local device");
            bt = Bluetooth.getLocalDevice();
            
        	// Start the RMI server
            System.out.println("Starting RMI");
            
            // Use last IP address, which will be Wifi, it it exists
            String lastIp = null;
            for (String ip: ips) {
            	lastIp = ip;
            }
            
            System.out.println("Setting java.rmi.server.hostname to " + lastIp);
            System.setProperty("java.rmi.server.hostname", lastIp);
            
            try { //special exception handler for registry creation
                LocateRegistry.createRegistry(1099); 
                System.out.println("java RMI registry created.");
            } catch (RemoteException e) {
                //do nothing, error means registry already exists
                System.out.println("java RMI registry already exists.");
            }
            
            try {
    			RMIRemoteEV3 ev3 = new RMIRemoteEV3();
    			Naming.rebind("//localhost/RemoteEV3", ev3);
    			RMIRemoteMenu remoteMenu = new RMIRemoteMenu(menu);
    			Naming.rebind("//localhost/RemoteMenu", remoteMenu);
    		} catch (Exception e) {
    			System.err.println("RMI failed to start: " + e);
    		}
            
            // Broadcast availability of device
            Broadcast.broadcast(hostname);
            
            // Set the date
            try {
				String dt = SntpClient.getDate(Settings.getProperty(ntpProperty, "1.uk.pool.ntp.org"));
				System.out.println("Date and time is " + dt);
				Runtime.getRuntime().exec("date -s " + dt);
			} catch (IOException e) {
				System.err.println("Failed to get time from ntp: " + e);
			}
            
            System.out.println("Initialisation complete");
        }
    }
    
    /**
     *  Start the background threads
     */
    private void start()
    {       
    	ind.start();
    	rcons.start();
    	pipeReader.start();
    	broadcast.start();
    	remoteMenuThread.start();
    }
	
	/**
     * Display the main system menu.
     * Allow the user to select File, Bluetooth, Sound, System operations.
     */
    private void mainMenu()
    {
        GraphicMenu menu = new GraphicMenu(new String[]
                {
                    "Run Default", "Programs", "Samples", "Tools", "Bluetooth", "Wifi", "Sound", "System", "Version"
                },new String[] {ICDefault,ICFiles,ICSamples,ICTools,ICBlue,ICWifi,ICSound,ICEV3,ICLeJOS},3);
        int selection = 0;
        do
        {
            newScreen(hostname);
            int row = 1;
            for(String ip: ips) {
            	lcd.drawString(ip,8 - ip.length()/2,row++);
            }
            selection = getSelection(menu, selection);
            switch (selection)
            {
                case 0:
                    mainRunDefault();
                    break;
                case 1:
                    filesMenu();
                    break;
                case 2:
                    samplesMenu();
                    break;
                case 3:
                    toolsMenu();
                    break;
                case 4:
                    bluetoothMenu();
                    break;
                case 5:
                    wifiMenu();
                    break;
                case 6:
                    soundMenu();
                    break;
                case 7:
                    systemMenu();
                    break;
                case 8:
                    displayVersion();
                    break;
            }
            
            if (selection < 0)  {
            	if (getYesNo("  Shut down EV3 ?", false) == 1)
            		break;
            }
        } while (true);
        
        // Shut down the EV3
        shutdown();
    }
    
    public class RemoteMenuThread extends Thread {
        @Override
        public void run() {
        	
    		ServerSocket ss;
    		Socket conn = null;
    		
        	// Create a server socket
        	try {
    			ss = new ServerSocket(REMOTE_MENU_PORT);
    			System.out.println("Remote menu server socket created");
    		} catch (IOException e) {
    			System.err.println("Error creating server socket: " + e);
    			return;
    		}
        	
        	while(true) {   		
                try {
                	System.out.println("Waiting for a remote menu connection");
            		conn = ss.accept();
            		//conn.setSoTimeout(2000);
            		
            		ObjectOutputStream os = new ObjectOutputStream(conn.getOutputStream());
            		ObjectInputStream is = new ObjectInputStream(conn.getInputStream());
            		            		
            		try {
	            		while(true) { 
	                		MenuRequest request = (MenuRequest) is.readObject();
	                		MenuReply reply = new MenuReply();
	                		
		            		switch (request.request) {
		            		case RUN_PROGRAM:
		            			runProgram(request.name);
		            			break;
							case DEBUG_PROGRAM:
								debugProgram(request.name);
								break;
							case DELETE_ALL_PROGRAMS:
								deleteAllPrograms();
								break;
							case DELETE_FILE:
								reply.result = deleteFile(request.name);
								os.writeObject(reply);
								break;
							case FETCH_FILE:
								reply.contents = fetchFile(request.name);
								os.writeObject(reply);
								break;
							case GET_FILE_SIZE:
								reply.reply = (int) getFileSize(request.name);
								os.writeObject(reply);
								break;
							case GET_MENU_VERSION:
								reply.value = getMenuVersion();
								os.writeObject(reply);
								break;
							case GET_NAME:
								reply.value = menu.getName();
								os.writeObject(reply);
								break;
							case GET_PROGRAM_NAMES:
								reply.names = getProgramNames();
								os.writeObject(reply);
								break;
							case GET_SAMPLE_NAMES:
								reply.names = getSampleNames();
								os.writeObject(reply);
								break;
							case GET_SETTING:
								reply.value = getSetting(request.name);
								os.writeObject(reply);
								break;
							case GET_VERSION:
								reply.value = getVersion();
								os.writeObject(reply);
								break;
							case RUN_SAMPLE:
		            			runSample(request.name);
								break;
							case SET_NAME:
								setName(request.name);
								break;
							case SET_SETTING:
								setSetting(request.name, request.value);
								break;
							case UPLOAD_FILE:
								reply.result = uploadFile(request.name, request.contents);
								os.writeObject(reply);
								break;  
							case STOP_PROGRAM:
								stopProgram();
								break;
							case SHUT_DOWN:
								shutdown();
								break;
							case GET_EXECUTING_PROGRAM_NAME:
								reply.value = programName;
								os.writeObject(reply);
								break;
							case SUSPEND:
								GraphicStartup.this.suspend();
								break;
							case RESUME:
								GraphicStartup.this.resume();
		            		}
	            		}
            		
                    } catch(Exception e) {
                    	System.err.println("Error reading from connection " + e);
						try {
							conn.close();
						} catch (IOException e1) {
							System.err.println("Error closing connection: " + e);
						}
                    }
            		
                } catch(Exception e) {
                	System.err.println("Error accepting connection " + e);
                	break;
                }		
        	}
        	
        	try {
				ss.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }
    
    /**
     * Present the Bluetooth menu to the user.
     */
    private void bluetoothMenu()
    {
    	// Check if BT initialisation is complete
        if (bt == null) {
        	msg("BT not started");
        	return;
        }
        
        int selection = 0;
        GraphicMenu menu = new GraphicMenu(null,null,4);
        boolean visible = false;
        do
        {
            newScreen("Bluetooth");
            visible = bt.getVisibility();
            System.out.println("Visibility is " + visible);

            lcd.drawString("Visibility", 0, 2);
            lcd.drawString(visible ? "on" : "off", 11, 2);
            menu.setItems(new String[]
                    {
                        "Search/Pair", "Devices", "Visibility", "Change PIN"
                    },
                    new String[]{ICSearch,ICEV3,ICVisibility,ICPIN});
            selection = getSelection(menu,selection);
            switch (selection)
            {
                case 0:
                    bluetoothSearch();
                    break;
                case 1:
                    bluetoothDevices();
                    break;
                case 2:
                	visible = !visible;
                    btVisibility = visible;
                    System.out.println("Setting visibility to " + btVisibility);
					try {
						bt.setVisibility(btVisibility);
					} catch (IOException e) {
						System.err.println("Failed to set visibility: " + e);
					}
                    //updateBTIcon();
                    ind.updateNow();
                    break;
                case 3:
                    bluetoothChangePIN();
                    break;
            }
        } while (selection >= 0);
    }
    
    /**
     * Clears the screen, displays a number and allows user to change
     * the digits of the number individually using the NXT buttons.
     * Note the array of bytes represent ASCII characters, not actual numbers.
     * 
     * @param digits Number of digits in the PIN.
     * @param title The text to display above the numbers.
     * @param number Start with a default PIN. Array of bytes up to 8 length.
     * @return
     */
    private boolean enterNumber(String title, byte[] number, int digits)
    {
        // !! Should probably check to make sure defaultNumber is digits in size
        int curDigit = 0;

        while (true)
        {
            newScreen();
            lcd.drawString(title, 0, 2);
            for (int i = 0; i < digits; i++)
            	lcd.drawChar((char)number[i], i * 2 + 1, 4);
            
            if (curDigit >= digits)
            	return true;
            
            Utils.drawRect(curDigit * 20 + 3, 60, 20, 20);

            int ret = getButtonPress();
            switch (ret)
            {
                case Button.ID_ENTER:
                { // ENTER
                    curDigit++;
                    break;
                }
                case Button.ID_LEFT:
                { // LEFT
                    number[curDigit]--;
                    if (number[curDigit] < '0')
                        number[curDigit] = '9';
                    break;
                }
                case Button.ID_RIGHT:
                { // RIGHT
                    number[curDigit]++;
                    if (number[curDigit] > '9')
                        number[curDigit] = '0';
                    break;
                }
                case Button.ID_ESCAPE:
                { // ESCAPE
                    curDigit--;
                    // Return false if user backs out
                    if (curDigit < 0)
                        return false;
                    break;
                }
            }
        }
    }
 
    /**
     * Allow the user to change the Bluetooth PIN.
     */
    private void bluetoothChangePIN()
    {
        // 1. Retrieve PIN from System properties
        String pinStr = SystemSettings.getStringSetting(pinProperty, "1234");
        int len = pinStr.length();
        byte[] pin = new byte[len];
        for (int i = 0; i < len; i++)
            pin[i] = (byte) pinStr.charAt(i);

        // 2. Call enterNumber() method
        if (enterNumber("Enter NXT PIN", pin, 4))
        {
            // 3. Set PIN in system memory.
        	StringBuilder sb = new StringBuilder();
            for (int i = 0; i < pin.length; i++)
                sb.append((char) pin[i]);
            
            try {
				PrintStream out = new PrintStream(new FileOutputStream("/etc/bluetooth/btpin"));
				out.println(sb.toString());
				out.close();
			} catch (IOException e) {
				System.out.println("Failed to write pin to /etc/bluetooth/btpin: " + e);
			}
            
            // 4. Run startbt to restart the agent with the new pin
        	try {
        		lcd.clear();
        		lcd.drawString("Restarting agent", 0, 1);
				Process p = Runtime.getRuntime().exec(START_BLUETOOTH);
				int status = p.waitFor();
				System.out.println("startbt returned " + status);
			} catch (Exception e) {
				System.err.println("Failed to execute startbt: " + e);
			}
        }
    }
    
    /**
     * Perform the Bluetooth search operation
     * Search for Bluetooth devices
     * Present those that are found
     * Allow pairing
     */
    private void bluetoothSearch()
    {
        newScreen("Searching");
        ArrayList<RemoteBTDevice> devList; 
        //indiBT.incCount();
        devList = null;
        try
        {
        	// 0 means "search for all"
	        try {
				devList = (ArrayList<RemoteBTDevice>) bt.search();
			} catch (IOException e) {
				return;
			}
        }
	    finally
	    {
	    }
        if (devList == null || devList.size() <= 0)
        {
            msg("No devices found");
            return;
        }
        
        String[] names = new String[devList.size()];
        String[] icons = new String[devList.size()];
        for (int i = 0; i < devList.size(); i++)
        {
            RemoteBTDevice btrd = devList.get(i);
            names[i] = btrd.getName();
            //icons[i] = getDeviceIcon(btrd.getDeviceClass());
        }
        GraphicListMenu searchMenu = new GraphicListMenu(names, icons);
        GraphicMenu subMenu = new GraphicMenu(new String[]{"Pair"}, new String[]{ICYes},4);
        int selected = 0;
        do
        {
            newScreen("Found");
            selected = getSelection(searchMenu, selected);
            if (selected >= 0)
            {
                RemoteBTDevice btrd = devList.get(selected);
                newScreen();
                //LCD.bitBlt(
                //	Utils.stringToBytes8(getDeviceIcon(btrd.getDeviceClass()))
                //	, 7, 7, 0, 0, 2, 16, 7, 7, LCD.ROP_COPY);
                lcd.drawString(names[selected], 2, 2);
                lcd.drawString(btrd.getAddress(), 0, 3);
                int subSelection = getSelection(subMenu, 0);
                if (subSelection == 0){
                    newScreen("Pairing");
                    //Bluetooth.addDevice(btrd);
                    // !! Assuming 4 length
                    byte[] pin = { '0', '0', '0', '0' };
                    if (!enterNumber("PIN for " + btrd.getName(), pin, pin.length))
                    	break;
                    lcd.drawString("Please wait...", 0, 6);
                    try {
                    	Bluetooth.getLocalDevice().authenticate(btrd.getAddress(), new String(pin));
                    	// Indicate Success or failure:
                    	lcd.drawString("Paired!      ", 0, 6);
                    } catch (Exception e)
                    {
                    	System.err.println("Failed to pair:" + e);
                    	lcd.drawString("UNSUCCESSFUL  ", 0, 6);
                        //Bluetooth.removeDevice(btrd);
                    }
                    lcd.drawString("Press any key", 0, 7);
                    getButtonPress();
                }
            }
        } while (selected >= 0);
    }

    /**
     * Display all currently known Bluetooth devices.
     */
    private void bluetoothDevices()
    {
        List<RemoteBTDevice> devList;
		try {
			devList = (List<RemoteBTDevice>) Bluetooth.getLocalDevice().search();
		} catch (IOException e) {
			return;
		}
        if (devList.size() <= 0)
        {
            msg("No known devices");
            return;
        }
        
        newScreen("Devices");
        String[] names = new String[devList.size()];
        String[] icons = new String[devList.size()];
        int i=0;
        for (RemoteBTDevice btrd: devList)
        {
            names[i] = btrd.getName();
            i++;
        }

        GraphicListMenu deviceMenu = new GraphicListMenu(names, icons);
        GraphicMenu subMenu = new GraphicMenu(new String[] {"Remove"},new String[]{ICDelete},4);
        int selected = 0;
        do
        {
            newScreen();
            selected = getSelection(deviceMenu, selected);
            if (selected >= 0)
            {
                newScreen();
                RemoteBTDevice btrd = devList.get(selected);
                byte[] devclass = btrd.getDeviceClass();
                lcd.drawString(btrd.getName(), 2, 2);
                lcd.drawString(btrd.getAddress(), 0, 3);
                // TODO device class is overwritten by menu
                // LCD.drawString("0x"+Integer.toHexString(devclass), 0, 4);
                int subSelection = getSelection(subMenu, 0);
                if (subSelection == 0)
                {
                    //Bluetooth.removeDevice(btrd);
                    break;
                }
            }
        } while (selected >= 0);
    }
    
    /**
     * Run the default program (if set).
     */
    private void mainRunDefault()
    {
    	File file = getDefaultProgram();
        if (file == null)
        {
       		msg("No default set");
        }
        else
        {
        	System.out.println("Executing default program " + file.getPath());
        	ind.suspend();
			try {
				JarFile jar = new JarFile(file);
				String mainClass = jar.getManifest().getMainAttributes().getValue("Main-class");
				jar.close();
	            exec(file, JAVA_RUN_CP + file.getPath() + " lejos.internal.ev3.EV3Wrapper " + mainClass, PROGRAMS_DIRECTORY);
			} catch (IOException e) {
				System.err.println("Exception running program");
			}
        	ind.resume();
        }
    }
    
    /**
     * Present the system menu.
     * Allow the user to format the filesystem. Change timeouts and control
     * the default program usage.
     */
    private void systemMenu()
    {
        String[] menuData = {"Delete all", "", "Auto Run", "Change name", "NTP host", "Suspend menu", "Unset default"};
        String[] iconData = {ICFormat,ICSleep,ICAutoRun,ICDefault,ICDefault,ICDefault,ICDefault};
        boolean rechargeable = false;
        GraphicMenu menu = new GraphicMenu(menuData,iconData,4);
        int selection = 0;
        do {
            newScreen("System");
            lcd.drawString("RAM", 0, 1);
            lcd.drawInt((int) (Runtime.getRuntime().freeMemory()), 11, 1);
            lcd.drawString("Battery", 0, 2);
            int millis = LocalEV3.ev3.getPower().getVoltageMilliVolt() + 50;
            lcd.drawInt((millis - millis % 1000) / 1000, 11, 2);
            lcd.drawString(".", 12, 2);
            lcd.drawInt((millis % 1000) / 100, 13, 2);
            if (rechargeable)
            	lcd.drawString("R", 15, 2);
            menuData[1] = "Sleep time: " + (timeout == 0 ? "off" : String.valueOf(timeout));
            File f = getDefaultProgram();
            if (f == null){
            	menuData[6] = null;
            	iconData[6] = null;
            }
            menu.setItems(menuData,iconData);
            selection = getSelection(menu, selection);
            switch (selection)
            {
                case 0:              	
                    if (getYesNo("Delete all files?", false) == 1) {
                    	File dir = new File(PROGRAMS_DIRECTORY);
                        for (String fn : dir.list()) {
                            File aFile = new File(dir,fn);
                            System.out.println("Deleting " + aFile.getPath());
                            aFile.delete();
                        }
                    }
                    break;
                case 1:
                	System.out.println("Timeout = " + timeout);
                	System.out.println("Max sleep time = " + maxSleepTime);
                    //timeout++;
                    if (timeout > maxSleepTime) timeout = 0;
                    Settings.setProperty(sleepTimeProperty, String.valueOf(timeout));
                    break;
                case 2:
                    systemAutoRun();
                    break;
                case 3:
                	String newName = new Keyboard().getString();
                	
                	if (newName != null) {
                		setName(newName);
                	}
                	break;
                case 4:
                	String host = new Keyboard().getString();
                	
                	if(host != null) {
                		Settings.setProperty(ntpProperty, host);
                	}
                	break;
                case 5:
                	ind.suspend();
                	lcd.clear();
                	lcd.refresh();
                	lcd.setAutoRefresh(false);
                	System.out.println("Menu suspended");
                    while(true) {
                        int b = Button.getButtons(); 
                        if (b == 6) break;
                        Delay.msDelay(200);
                    }
                    lcd.setAutoRefresh(true);
                    lcd.clear();
                    lcd.refresh();            	
                	ind.resume();
                	System.out.println("Menu resumed");
                	break;
                case 6:
                    Settings.setProperty(defaultProgramProperty, "");
                    Settings.setProperty(defaultProgramAutoRunProperty, "");
                    selection = 0;
                    break;
            }
        } while (selection >= 0);
    }
    
    /**
     * Present details of the default program
     * Allow the user to specify run on system start etc.
     */
    private void systemAutoRun()
    {
    	File f = getDefaultProgram();
    	if (f == null)
    	{
       		msg("No default set");
       		return;
    	}
    	
    	newScreen("Auto Run");
    	lcd.drawString("Default Program:", 0, 2);
    	lcd.drawString(f.getName(), 1, 3);
    	
    	String current = Settings.getProperty(defaultProgramAutoRunProperty, "");
        int selection = getYesNo("Run at power up?", current.equals("ON"));
        if (selection >= 0)
        	Settings.setProperty(defaultProgramAutoRunProperty, selection == 0 ? "OFF" : "ON");
    }
    
    /**
     * Ask the user for confirmation of an action.
     * @param prompt A description of the action about to be performed
     * @return 1=yes 0=no < 0 escape
     */
    private int getYesNo(String prompt, boolean yes)
    {
//    	lcd.bitBlt(null, 178, 64, 0, 0, 0, 64, 178, 64, CommonLCD.ROP_CLEAR);
        GraphicMenu menu = new GraphicMenu(new String[]{"No", "Yes"},new String[]{ICNo,ICYes},4,prompt,3);
        return getSelection(menu, yes ? 1 : 0);
    }
    
    /**
     * Get the default program as a file
     */
    private static File getDefaultProgram()
    {
    	String file = Settings.getProperty(defaultProgramProperty, "");
    	if (file != null && file.length() > 0)
    	{
    		File f = new File(file);
        	if (f.exists())
        		return f;
        	
           	Settings.setProperty(defaultProgramProperty, "");
           	Settings.setProperty(defaultProgramAutoRunProperty, "OFF");
    	}
    	return null;
    }

    /**
     * Display the sound menu.
     * Allow the user to change volume and key click volume.
     */
    private void soundMenu()
    {
        String[] soundMenuData = new String[]{"Volume:    ", "Key volume: ", "Key freq: ", "Key length: "};
        String[] soundMenuData2 = new String[soundMenuData.length];
        GraphicMenu menu = new GraphicMenu(soundMenuData2, new String[] {ICSound,ICSound, ICSound, ICSound},3);
        int[][] Volumes =
        {
            {
                Sound.getVolume() / 10, 784, 250, 0
            },
            {
                Button.getKeyClickVolume() / 10, Button.getKeyClickTone(1) / 200, Button.getKeyClickLength() / 10, 0
            },
            {
                Button.getKeyClickVolume() / 10, Button.getKeyClickTone(1) / 200, Button.getKeyClickLength() / 10, 0
            },
            {
                Button.getKeyClickVolume() / 10, Button.getKeyClickTone(1) / 200, Button.getKeyClickLength() /10, 0
            }
        };
        int selection = 0;
        // Make a note of starting values so we know if they change
        for (int i = 0; i < Volumes.length; i++)
            Volumes[i][3] = Volumes[i][(i == 0 ? 0 : i-1)];
        // remember and Turn off tone for the enter key
        int tone = Button.getKeyClickTone(Button.ID_ENTER);
        Button.setKeyClickTone(Button.ID_ENTER, 0);
        do {
            newScreen("Sound");
            for (int i = 0; i < Volumes.length; i++)
                soundMenuData2[i] = soundMenuData[i] + formatVol(Volumes[i][(i == 0 ? 0 : i -1)]);
            menu.setItems(soundMenuData2);
            selection = getSelection(menu, selection);
            if (selection >= 0)
            {
                Volumes[selection][(selection == 0 ? 0 : selection -1)]++;
                Volumes[selection][(selection == 0 ? 0 : selection -1)] %= 11;
                if (selection > 1 && Volumes[selection][(selection == 0 ? 0 : selection -1)] == 0)
                	Volumes[selection][(selection == 0 ? 0 : selection -1)] = 1;
                if (selection == 0)
                {
                    Sound.setVolume(Volumes[0][0] * 10);
                    Sound.playTone(1000, Volumes[selection][1], Volumes[selection][2]);
                }
                else
                    Sound.playTone(Volumes[selection][1] * 200, Volumes[selection][2] * 10, Volumes[selection][0] * 10);
            }
        } while (selection >= 0);
        // Make sure key click is back on and has new volume
        Button.setKeyClickTone(Button.ID_ENTER, tone);
        Button.setKeyClickVolume(Volumes[1][0] * 10);
        // Save in settings
        if (Volumes[0][0] != Volumes[0][3])
            Settings.setProperty(Sound.VOL_SETTING, String.valueOf(Volumes[0][0] * 10));
        if (Volumes[1][0] != Volumes[1][3])
            Settings.setProperty(Button.VOL_SETTING, String.valueOf(Volumes[1][0] * 10));
        if (Volumes[2][1] != Volumes[2][3])
            Settings.setProperty(Button.FREQ_SETTING, String.valueOf(Volumes[2][1] * 200));
        if (Volumes[3][2] != Volumes[3][3])
            Settings.setProperty(Button.LEN_SETTING, String.valueOf(Volumes[3][2] * 10));
    }
    
    /**
     * Format a string for use when displaying the volume.
     * @param vol Volume setting 0-10
     * @return String version.
     */
    private static String formatVol(int vol)
    {
        if (vol == 0)
            return "mute";
        if (vol == 10)
            return "10";        
        return new StringBuilder().append(' ').append(vol).toString();
    }
    
    /**
     * Display system version information.
     */
    private void displayVersion()
    {
        newScreen("Version");
        lcd.drawString("leJOS:", 0, 2);
        lcd.drawString(version, 6, 2);
        lcd.drawString("Menu:", 0, 3);
        lcd.drawString(Utils.versionToString(Config.VERSION),6, 3);
        getButtonPress();
    }
    
    /**
     * Read a button press.
     * If the read timesout then exit the system.
     * @return The bitcode of the button.
     */
    private int getButtonPress()
    {
        int value = Button.waitForAnyPress(timeout*60000);
        if (value == 0) shutdown();
        return value;
    }
	
    /**
     * Present the menu for a single file.
     * @param file
     */
    private void fileMenu(File file, int type)
    {
        String fileName = file.getName();
        String ext = Utils.getExtension(fileName);
        int selectionAdd;
        String[] items;
        String[] icons;
        if (ext.equals("jar"))
        {
        	selectionAdd = 0;
            items = new String[]{"Execute program", "Debug program", "Set as Default", "Delete file"}; 
            icons = new String[]{ICProgram,ICDebug,ICDefault,ICDelete};
        }
        else if (ext.equals("wav"))
        {
        	selectionAdd = 10;
        	items = new String[]{"Play sample", "Delete file"};
        	icons = new String[]{ICSound,ICDelete};
        }
        else
        {
        	selectionAdd = 20;
        	items = new String[]{"Delete file", "View File"};
        	icons = new String[]{ICDelete, ICEV3};
        }
        newScreen();
        lcd.drawString("Size:", 0, 2);
        lcd.drawString(Long.toString(file.length()), 5, 2);
        GraphicMenu menu = new GraphicMenu(items,icons,3,fileName,1);
        int selection = getSelection(menu, 0);
        if (selection >= 0)
        {
        	String directory = null;
        	
        	switch (type) {
        	case TYPE_PROGRAM:
        		directory =  PROGRAMS_DIRECTORY;
        		break;
        	case TYPE_SAMPLE:
        		directory =  SAMPLES_DIRECTORY;
        		break;
        	case TYPE_TOOL:
        		directory = TOOLS_DIRECTORY;
        		break;
        	}
	        switch(selection + selectionAdd)
	        {
	            case 0:
	            	System.out.println("Running program: " + file.getPath());
	            	ind.suspend();
	            	if (type == TYPE_TOOL) {
	            		execInThisJVM(file);
	            		ind.resume();
	            	} else {
						JarFile jar = null;
						try {
							jar = new JarFile(file);
							String mainClass = jar.getManifest().getMainAttributes().getValue("Main-class");
							jar.close();
				            ind.suspend();
				            exec(file, JAVA_RUN_CP + file.getPath() + " lejos.internal.ev3.EV3Wrapper " + mainClass, directory);
				            ind.resume();
						} catch (IOException e) {
							System.err.println("Exception running program");
						}
	            	}
	            	
	                break;
	            case 1:
	            	System.out.println("Debugging program: " + file.getPath());
					JarFile jar = null;
					try {
						jar = new JarFile(file);
						String mainClass = jar.getManifest().getMainAttributes().getValue("Main-class");
						jar.close();
			            ind.suspend();
			            exec(file, JAVA_DEBUG_CP + file.getPath() + " lejos.internal.ev3.EV3Wrapper " + mainClass, directory);
			            ind.resume();
					} catch (IOException e) {
						System.err.println("Exception running program");
					}
	                break;
	            case 2:
	            	Settings.setProperty(defaultProgramProperty, file.getPath());
	            	break;
	            case 10:
	            	System.out.println("Playing " + file.getPath());
	                Sound.playSample(file);
	                break;
	            case 3:
	            case 11:
	            case 20:
	                file.delete();
	                break;
	            case 21:
					try {
						Viewer.view(file.getPath());
					} catch (IOException e) {
						System.err.println("Exception viewing file");
					}
	            	break;
	        }
        }
    }
    
    /**
     * Present the menu for a menu tool.
     * @param file
     */
    private void toolMenu(File file)
    {
        String fileName = file.getName();
        String ext = Utils.getExtension(fileName);
        if (ext.equals("jar"))
        {
        	ind.suspend();
    		execInThisJVM(file);
    		ind.resume();
        }
    }

    
    /**
     * Execute a program and display its output to System.out and error stream to System.err
     */
    private static void exec(File jar, String command, String directory) {
        try {
        	if (jar != null) {
        		String jarName = jar.getName();
        		programName = jarName.substring(0, jarName.length() - 4); // Remove .jar
        	}
        	lcd.clear();
        	lcd.refresh();
        	lcd.setAutoRefresh(false);

        	drawLaunchScreen();
                    	
            program = new ProcessBuilder(command.split(" ")).directory(new File(directory)).start();
            BufferedReader input = new BufferedReader(new InputStreamReader(program.getInputStream()));
            BufferedReader err= new BufferedReader(new InputStreamReader(program.getErrorStream()));
            
            echoIn = new EchoThread(jar.getPath().replace(".jar", ".out"),input, System.out);
            echoErr = new EchoThread(jar.getPath().replace(".jar", ".err"), err, System.err);
            
            echoIn.start(); 
            echoErr.start();
            
        	System.out.println("Executing " + command + " in " + directory);
            
            while(true) {
              int b = Button.getButtons(); 
              if (b == 6) {
            	  System.out.println("Killing the process");
            	  program.destroy(); 
            	  // reset motors after program is aborted
            	  resetMotors();
                  break;
              }
              if (!echoIn.isAlive() && !echoErr.isAlive()) break;           
              Delay.msDelay(200);
            }
            System.out.println("Waiting for process to die");;
            program.waitFor();
            System.out.println("Program finished");
      	    // Turn the LED off, in case left on
      	    Button.LEDPattern(0);
            lcd.setAutoRefresh(true);
            lcd.clear();
            lcd.refresh();
            program = null;
          }
          catch (Exception e) {
            System.err.println("Failed to execute program: " + e);
          } 	
    }
    
    /**
     * Execute a program and display its output to System.out and error stream to System.err
     */
    private static void startProgram(String command, File jar) {
        try {
        	if (program != null) return;
        	lcd.clear();
        	lcd.refresh();
        	lcd.setAutoRefresh(false);
        	
        	drawLaunchScreen();
        	
        	String[] args = command.split(" ");
        	File directory = jar.getParentFile();
        	
        	programName = jar.getName().replace(".jar", "");
        	
            program = new ProcessBuilder(args).directory(directory).start();
            
            BufferedReader input = new BufferedReader(new InputStreamReader(program.getInputStream()));
            BufferedReader err= new BufferedReader(new InputStreamReader(program.getErrorStream()));
            
            echoIn = new EchoThread(jar.getPath().replace(".jar",".out"), input, System.out);
            echoErr = new EchoThread(jar.getPath().replace(".jar",".err"), err, System.err);
            
            echoIn.start();
            echoErr.start();
            
        	System.out.println("Executing " + command + " in " + directory);
        	
            suspend = true;
            curMenu.quit(); // Quit the current menu and go into the suspend loop
            
        } catch (Exception e) {
        	System.err.println("Failed to start program: " + e);
        } 
    }
    
    public void stopProgram() {           
        try {  
        	if (program == null) return;
        	
        	program.destroy();
        
            System.out.println("Waiting for process to die");;
            program.waitFor();
            System.out.println("Program finished");
            resetMotors();
      	    // Turn the LED off, in case left on
      	    Button.LEDPattern(0);
            lcd.setAutoRefresh(true);
            lcd.clear();
            lcd.refresh();
            program = null;
            suspend = false;
        	ind.resume();
          }
          catch (Exception e) {
            System.err.println("Failed to stop program: " + e);
          } 	
    }
   
    /**
     * Display the files in the file system.
     * Allow the user to choose a file for further operations.
     */
    private void filesMenu()
    {
    	GraphicListMenu menu = new GraphicListMenu(null,null);
    	System.out.println("Finding files ...");
        int selection = 0;
        do {
            File[] files = (new File(PROGRAMS_DIRECTORY)).listFiles();
            int len = 0;
            for (int i = 0; i < files.length && files[i] != null; i++)
                len++;
            if (len == 0)
            {
                msg("No files found");
                return;
            }
            newScreen("Files");
            String fileNames[] = new String[len];
            String[] icons = new String[len];
            for (int i = 0; i < len; i++){
                fileNames[i] = files[i].getName();
                String ext = Utils.getExtension(files[i].getName());
               if (ext.equals("jar"))
                	icons[i] = ICMProgram;
                else if (ext.equals("wav"))
                	icons[i] = ICMSound;
                else
                	icons[i] = ICMFile;
            }
            menu.setItems(fileNames,icons);
            selection = getSelection(menu, selection);
            if (selection >= 0)
                fileMenu(files[selection], TYPE_PROGRAM);
        } while (selection >= 0);
    }
    
    /**
     * Display the tools from the tools directory.
     * Allow the user to choose a file for further operations.
     */
    private void toolsMenu()
    {
    	GraphicListMenu menu = new GraphicListMenu(null,null);
    	//System.out.println("Finding files ...");
        int selection = 0;
        do {
            File[] files = new File(TOOLS_DIRECTORY).listFiles();
            int len = 0;
            for (int i = 0; i < files.length && files[i] != null; i++)
                len++;
            if (len == 0)
            {
                msg("No tools found");
                return;
            }
            newScreen("Tools");
            String fileNames[] = new String[len];
            String[] icons = new String[len];
            for (int i = 0; i < len; i++){
            	fileNames[i] = formatFileName(files[i].getName());
                String ext = Utils.getExtension(files[i].getName());
               if (ext.equals("jar"))
                	icons[i] = ICMProgram;
            }
            
            menu.setItems(fileNames,icons);
            selection = getSelection(menu, selection);
            if (selection >= 0)
                toolMenu(files[selection]);
        } while (selection >= 0);
    }
    
    /**
     * Method to add spaces before capital letters and remove .jar extension.
     * 
     * @param fileName
     * @return
     */
	static private String formatFileName(String fileName) {
    	StringBuffer formattedName = new StringBuffer(""+fileName.charAt(0));
		for(int i=1;i<fileName.length();i++) { //Skip the first letter-can't put space before first word
    		if(fileName.charAt(i)=='.') break;
    		if(Character.isUpperCase(fileName.charAt(i))) {
    			formattedName.append(' ');
    		}
    		formattedName.append(fileName.charAt(i));
    		
    	}
    	return formattedName.toString();
    }
    
    /**
     * Display the samples in the file system.
     * Allow the user to choose a file for further operations.
     */
    private void samplesMenu()
    {
    	GraphicListMenu menu = new GraphicListMenu(null,null);
    	//System.out.println("Finding files ...");
        int selection = 0;
        do {
            File[] files = (new File(SAMPLES_DIRECTORY)).listFiles();
            int len = 0;
            for (int i = 0; i < files.length && files[i] != null; i++)
                len++;
            if (len == 0)
            {
                msg("No samples found");
                return;
            }
            newScreen("Samples");
            String fileNames[] = new String[len];
            String[] icons = new String[len];
            for (int i = 0; i < len; i++){
                fileNames[i] = files[i].getName();
                String ext = Utils.getExtension(files[i].getName());
               if (ext.equals("jar"))
                	icons[i] = ICMProgram;
                else if (ext.equals("wav"))
                	icons[i] = ICMSound;
                else
                	icons[i] = ICMFile;
            }
            menu.setItems(fileNames,icons);
            selection = getSelection(menu, selection);
            if (selection >= 0)
                fileMenu(files[selection], TYPE_SAMPLE);
        } while (selection >= 0);
    }

    
    /**
     * Start a new screen display using the current title.
     */
    private void newScreen()
    {
    	lcd.clear();
        ind.updateNow();
    }
    
    /**
     * Start a new screen display.
     * Clear the screen and set the screen title.
     * @param title
     */
    private void newScreen(String title)
    {
        indiBA.setTitle(title);
        newScreen();
    }
    
    /**
     * Display a status message
     * @param msg
     */
    private void msg(String msg)
    {
        newScreen();
        lcd.drawString(msg, 0, 2);
        long start = System.currentTimeMillis();
        int button;
        int buttons = Button.readButtons();
        do
        {
        	Thread.yield();
        	
        	int buttons2 = Button.readButtons();
        	button = buttons2 & ~buttons;
        } while (button != Button.ID_ESCAPE && System.currentTimeMillis() - start < 2000);
    }
    
    /**
     * Obtain a menu item selection
     * Allow the user to make a selection from the specified menu item. If a
     * power off timeout has been specified and no choice is made within this
     * time power off the NXT.
     * @param menu Menu to display.
     * @param cur Initial item to select.
     * @return Selected item or < 0 for escape etc.
     */
    private int getSelection(GraphicMenu menu, int cur)
    { 	
        int selection;
        
        curMenu = menu;
        
        // If the menu is interrupted by another thread, redisplay
        do {
        	selection = menu.select(cur, timeout*60000);
        	
            while (suspend) {
            	if (program != null && !echoIn.isAlive() && !echoErr.isAlive()) {
            		stopProgram();
            		ind.resume();
            		break;
            	}
                int b = Button.getButtons(); 
                if (b == 6) {
                	if (program != null) stopProgram();
                	ind.resume();
                	break;
                }
                Delay.msDelay(200);
            }
        } while (selection == -2);
        
        if (selection == -3)
            shutdown();
    	
        return selection;
    }
    
    public String getExecutingProgramName() {
    	if (program == null) return null;
    	return programName;
    }
    
    /**
     * Shut down the EV3
     */
    public void shutdown() {
    	System.out.println("Shutting down the EV3");
        ind.suspend();
    	try {
			Runtime.getRuntime().exec("init 0");
		} catch (IOException e) {
			// Ignore
		}
    	lcd.drawString("  Shutting down", 0, 6);
        lcd.refresh();
    }
    
    class PipeReader extends Thread {
    	
    	@Override
		public synchronized void run()
    	{
    		try {
				InputStream is = new FileInputStream(MENU_DIRECTORY + "/menufifo");
				
	    		while(true) {
	    			int c = is.read();
	    			if (c < 0) {
	    				Delay.msDelay(200);
	    			} else {
	    				System.out.println("Read from fifo: " + c + " " + ((char) c));
	    				
	    				if (c == 's') {
	                    	GraphicStartup.this.suspend();
	                    	System.out.println("Menu suspended");
	    				} else if (c == 'r') {
	    					GraphicStartup.this.resume();
	                    	System.out.println("Menu resumed");
	    				}
	    			}
	    		}
	    		
			} catch (IOException e) {
				System.err.println("Failed to read from fifo: " + e);
				return;
			}
    	}	
    }
    
    class BroadcastThread extends Thread {
    	
    	@Override
		public synchronized void run()
    	{
    		while(true) {
    			Broadcast.broadcast(hostname);
    			Delay.msDelay(1000);
    		}
    	}
    }
    
    /**
     * Manage the top line of the display.
     * The top line of the display shows battery state, menu titles, and I/O
     * activity.
     */
	class IndicatorThread extends Thread
    {
		public IndicatorThread()
    	{
    		super();
            setDaemon(true);
    	}
    	
    	@Override
		public synchronized void run()
    	{
    		try
    		{
	    		while (true)
	    		{
	    			long time = System.currentTimeMillis();
	    			
	    			byte[] buf = lcd.getDisplay();
	    			// TODO: Fix this
	    			// clear not necessary, pixels are always overwritten
	    			for (int i=0; i<lcd.getWidth(); i++)
	    				buf[i] = 0;	  
	    			indiBA.setWifi(ips.size() > 1);
	    			indiBA.draw(time, buf);
	    			lcd.refresh();
    			
    				// wait until next tick
    				time = System.currentTimeMillis();
    				this.wait(Config.ANIM_DELAY - (time % Config.ANIM_DELAY));
    			}
    		}
    		catch (InterruptedException e)
    		{
    			//just terminate
    		}
    	}
    	
    	/**
    	 * Update the indicators
    	 */
    	public synchronized void updateNow()
    	{
    		this.notifyAll();
    	}
    }
	
	/**
	 * Get all the IP addresses for the device
	 */
    public static List<String> getIPAddresses()
    {
        List<String> result = new ArrayList<String>();
        Enumeration<NetworkInterface> interfaces;
        try
        {
            interfaces = NetworkInterface.getNetworkInterfaces();
        } catch (SocketException e)
        {
            System.err.println("Failed to get network interfaces: " + e);
            return null;
        }
        while (interfaces.hasMoreElements()){
            NetworkInterface current = interfaces.nextElement();
            try
            {
                if (!current.isUp() || current.isLoopback() || current.isVirtual()) continue;
            } catch (SocketException e)
            {
            	System.err.println("Failed to get network properties: " + e);
            }
            Enumeration<InetAddress> addresses = current.getInetAddresses();
            while (addresses.hasMoreElements()){
                InetAddress current_addr = addresses.nextElement();
                if (current_addr.isLoopbackAddress()) continue;
                result.add(current_addr.getHostAddress());
            }
        }
        return result;
    } 
 
    public static void drawLaunchScreen() {
    	GraphicsLCD g = LocalEV3.get().getGraphicsLCD();
    	g.drawRegion(duke, 0, 0, duke.getWidth(), duke.getHeight(), GraphicsLCD.TRANS_NONE, 50, 65, GraphicsLCD.HCENTER | GraphicsLCD.VCENTER);
    	int x = LCD.SCREEN_WIDTH/2;
    	g.drawString("Wait", x, 30, 0);
    	g.drawString("a", x, 45, 0);
    	g.drawString("second...", x, 60, 0);
    	g.refresh(); // TODO: Needed?
    }
    
	@Override
	public void runProgram(String programName) {
		JarFile jar = null;
		String fullName = PROGRAMS_DIRECTORY + "/" + programName + ".jar";
		try {
			File jarFile = new File(fullName);
			jar = new JarFile(jarFile);
			String mainClass = jar.getManifest().getMainAttributes().getValue("Main-class");
			jar.close();
	    	ind.suspend();
	    	startProgram(JAVA_RUN_CP + fullName + " lejos.internal.ev3.EV3Wrapper " + mainClass, jarFile);
		} catch (IOException e) {
			System.err.println("Failed to run program");
		}
	}

	@Override
	public boolean deleteFile(String fileName) {
		File f = new File(fileName);
		return f.delete();
	}

	@Override
	public String[] getProgramNames() {
		File[] files = (new File(PROGRAMS_DIRECTORY)).listFiles();
		String[] fileNames = new String[files.length];
		for(int i=0;i<files.length;i++) {
			fileNames[i] = files[i].getName();
		}
		return fileNames;
	}

	@Override
	public void runSample(String programName) {
		JarFile jar = null;
		String fullName = SAMPLES_DIRECTORY + "/" + programName + ".jar";
		try {
			File jarFile = new File(fullName);
			jar = new JarFile(jarFile);
			String mainClass = jar.getManifest().getMainAttributes().getValue("Main-class");
			jar.close();
	    	ind.suspend();
	    	startProgram(JAVA_RUN_CP + fullName + " lejos.internal.ev3.EV3Wrapper " + mainClass, jarFile);
		} catch (IOException e) {
			System.err.println("Failed to run program");
		}
	}

	@Override
	public void debugProgram(String programName) {
		JarFile jar = null;
		String fullName = PROGRAMS_DIRECTORY + "/" + programName + ".jar";
		try {
			File jarFile = new File(fullName);
			jar = new JarFile(jarFile);
			String mainClass = jar.getManifest().getMainAttributes().getValue("Main-class");
			jar.close();
	    	ind.suspend();
			startProgram(JAVA_DEBUG_CP + fullName + " lejos.internal.ev3.EV3Wrapper " + mainClass, jarFile);
		} catch (IOException e) {
			System.err.println("Failed to run program");
		}
	}

	@Override
	public String[] getSampleNames() {
		File[] files = (new File(SAMPLES_DIRECTORY)).listFiles();
		String[] fileNames = new String[files.length];
		for(int i=0;i<files.length;i++) {
			fileNames[i] = files[i].getName();
		}
		return fileNames;
	}

	@Override
	public long getFileSize(String filename) {
		return new File(filename).length();
	}

	@Override
	public boolean uploadFile(String fileName, byte[] contents) {
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			out.write(contents);
			out.close();
			return true;
		} catch (IOException e) {
			System.out.println("Failed to upload file: " + e);
			return false;
		}
	
	}
	
	private void wifiMenu() {
    	GraphicListMenu menu = new GraphicListMenu(null,null);
    	System.out.println("Finding access points ...");
    	LocalWifiDevice wifi = Wifi.getLocalDevice("wlan0");
    	String[] names;
    	try {
    		names = wifi.getAccessPointNames();
    	} catch (Exception e) {
    		System.err.println("Exception getting access points" +e);
    		names = new String[0];
    	}
        int selection = 0;
        
        do {           
            int len = names.length;
            if (len == 0)
            {
                msg("No Access Points found");
                return;
            }
            newScreen("Access Pts");

            menu.setItems(names,null);
            selection = getSelection(menu, selection);
            if (selection >= 0) {
            	System.out.println("Access point is " + names[selection]);
            	Keyboard k = new Keyboard();
            	String pwd = k.getString();
            	if (pwd != null) {
                   	System.out.println("Password is " + pwd);
                	WPASupplicant.writeConfiguration("wpa_supplicant.txt",  "wpa_supplicant.conf",  names[selection], pwd);
                	startWlan();
            	}
             	selection = -1;
            }
        } while (selection >= 0);		
	}
	
	/**
	 * Reset all motors to zero power and float state
	 * and reset tacho counts
	 */
	public static void resetMotors() {
		for(String portName: new String[]{"A","B","C","D"}) {
			Port p = LocalEV3.get().getPort(portName);
			TachoMotorPort mp = p.open(TachoMotorPort.class);
			mp.controlMotor(0, TachoMotorPort.FLOAT);
			mp.resetTachoCount();
			mp.close();
		}
	}

	@Override
	public byte[] fetchFile(String fileName) {
		File f = new File(fileName);
		FileInputStream in;
		try {
			in = new FileInputStream(f);
			byte[] data = new byte[(int)f.length()];
		    in.read(data);
		    in.close();
		    return data;
		} catch (IOException e) {
			System.err.println("Failed to fetch file: " + e);
			return null;
		}

	}

	@Override
	public String getSetting(String setting) {
		return Settings.getProperty(setting, null);
	}

	@Override
	public void setSetting(String setting, String value) {
		Settings.setProperty(setting, value);
	}

	@Override
	public void deleteAllPrograms() {
    	File dir = new File(PROGRAMS_DIRECTORY);
        for (String fn : dir.list()) {
            File aFile = new File(dir,fn);
            System.out.println("Deleting " + aFile.getPath());
            aFile.delete();
        }
	}

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public String getMenuVersion() {
		return Utils.versionToString(Config.VERSION);
	}

	@Override
	public String getName() {
		return hostname;
	}

	@Override
	public void setName(String name) {
		hostname = name;
		
		// Write host to /etc/hostname
		try {
			PrintStream out = new PrintStream(new FileOutputStream("/etc/hostname"));
			out.println(name);
			out.close();
		} catch (FileNotFoundException e) {
			System.err.println("Failed to write to /etc/hostname: " + e);
		}
		
    	try {
			Process p = Runtime.getRuntime().exec("hostname " + hostname);
			int status = p.waitFor();
			System.out.println("hostname returned " + status);
		} catch (Exception e) {
			System.err.println("Failed to execute hostname: " + e);
		}
		
		startWlan();
	}
	
	private void startWlan() {
    	try {
			Process p = Runtime.getRuntime().exec(START_WLAN);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader err= new BufferedReader(new InputStreamReader(p.getErrorStream()));
            PrintStream lcdStream = new PrintStream(new LCDOutputStream());
            
            EchoThread echoIn = new EchoThread(null, input, lcdStream);
            EchoThread echoErr = new EchoThread(null, err, lcdStream);
            
            ind.suspend();
            lcd.clear();
            lcdStream.println("Restarting wlan\n");
            
            echoIn.start();
            echoErr.start();
            
			int status = p.waitFor();
			System.out.println("startwlan returned " + status);
			// Get IP addresses again
			ips = getIPAddresses();
            String lastIp = null;
            for (String ip: ips) {
            	lastIp = ip;
            }
			System.setProperty("java.rmi.server.hostname", lastIp);
			
            try {
    			RMIRemoteEV3 ev3 = new RMIRemoteEV3();
    			Naming.rebind("//localhost/RemoteEV3", ev3);
    			RMIRemoteMenu remoteMenu = new RMIRemoteMenu(menu);
    			Naming.rebind("//localhost/RemoteMenu", remoteMenu);
    		} catch (Exception e) {
    			System.err.println("RMI failed to start: " + e);
    		}
            
			lcd.clear();
        	ind.resume();
		} catch (Exception e) {
			System.err.println("Failed to execute startwlan: " + e);
		}
	}
	
	private void execInThisJVM(File jar) {
		try {
			LCD.clearDisplay();
			new JarMain(jar);
		} catch (Exception e) {
			toolException(e);
			System.err.println("Exception in execution of tool: " + e);
			e.printStackTrace();
		}
	}
	
	private void toolException(Throwable t) {
		Sound.buzz();
	    TextLCD lcd = BrickFinder.getDefault().getTextLCD(Font.getSmallFont());
		int offset = 0;
		// Get rid of invocation exception
		if (t.getCause() != null) t = t.getCause();
	    while (true)
	    {
    		lcd.clear();
    		lcd.drawString("Tool exception:", offset, 1);
    		lcd.drawString(t.getClass().getName(), offset, 3);
    		if (t.getMessage() != null) lcd.drawString(t.getMessage(), offset, 4);		
    		
    		if (t.getCause() != null) {
    			lcd.drawString("Caused by:", offset, 5);
    			lcd.drawString(t.getCause().toString(), offset, 6);
    		}
    		
    		StackTraceElement[] trace = t.getStackTrace();
    		for(int i=0;i<7 && i < trace.length ;i++) lcd.drawString(trace[i].toString(), offset, 8+i);
    		
    		lcd.refresh();
    		int id = Button.waitForAnyEvent();
    		if (id == Button.ID_ESCAPE) break;
    		if (id == Button.ID_LEFT) offset += 5;
    		if (id == Button.ID_RIGHT)offset -= 5;
    		if (offset > 0) offset = 0;
	    }
	    lcd.clear();
	}

	@Override
	public void suspend() {
		ind.suspend();
		LCD.clearDisplay();
        suspend = true;
        curMenu.quit();
	}

	@Override
	public void resume() {
		suspend = false;
		ind.resume();
	}
	
	static final Image duke = new Image(100, 64, new byte[] {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x1c, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x1e, (byte) 0x04, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1e, (byte) 0x0f, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x60, (byte) 0x3e, (byte) 0x0f, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xf0, (byte) 0xbe, 
            (byte) 0x0f, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0xf0, (byte) 0xbe, (byte) 0x07, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xf0, 
            (byte) 0xfd, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x01, (byte) 0xe0, (byte) 0xff, (byte) 0x07, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, 
            (byte) 0xe0, (byte) 0xff, (byte) 0x07, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x0f, (byte) 0xc0, (byte) 0xff, 
            (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x1f, (byte) 0xc0, (byte) 0xff, (byte) 0x07, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x3f, (byte) 0x80, 
            (byte) 0xff, (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x7f, (byte) 0x00, (byte) 0xff, (byte) 0x07, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff, 
            (byte) 0x00, (byte) 0xff, (byte) 0x0f, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xe1, (byte) 0xff, 
            (byte) 0x07, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0xff, (byte) 0xf3, (byte) 0xff, (byte) 0x07, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xf7, 
            (byte) 0xff, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0xff, (byte) 0xef, (byte) 0xfe, (byte) 0x01, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff, 
            (byte) 0x0f, (byte) 0xf8, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0x1f, (byte) 0xe0, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0xff, (byte) 0x3f, (byte) 0xc0, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0x7f, 
            (byte) 0xc0, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0xff, (byte) 0x7f, (byte) 0xc0, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff, 
            (byte) 0xff, (byte) 0xc0, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xff, (byte) 0xc1, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0xff, (byte) 0xff, (byte) 0xc0, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xff, (byte) 0xfd, 
            (byte) 0xc3, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x3f, (byte) 0xea, (byte) 0x7f, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x5f, 
            (byte) 0x55, (byte) 0x7f, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x80, (byte) 0x8f, (byte) 0xf8, (byte) 0x3c, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x80, 
            (byte) 0x57, (byte) 0x55, (byte) 0x3c, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0xaf, (byte) 0xea, 
            (byte) 0x38, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x80, (byte) 0x55, (byte) 0x55, (byte) 0x38, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xc0, (byte) 0xad, 
            (byte) 0x7a, (byte) 0x30, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0xc0, (byte) 0x5d, (byte) 0x15, (byte) 0x30, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xc0, 
            (byte) 0xb9, (byte) 0x1e, (byte) 0x60, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0xe0, (byte) 0xf0, (byte) 0x07, 
            (byte) 0x60, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0xf0, (byte) 0x80, (byte) 0x00, (byte) 0xe0, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xf8, (byte) 0x00, 
            (byte) 0x00, (byte) 0xc0, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0xf8, (byte) 0x00, (byte) 0x00, (byte) 0xc0, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0xcc, 
            (byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x01, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x6c, (byte) 0x00, (byte) 0x00, 
            (byte) 0x80, (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x6c, (byte) 0x00, (byte) 0x00, (byte) 0x80, (byte) 0x01, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x6c, (byte) 0x00, 
            (byte) 0x00, (byte) 0x80, (byte) 0x01, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x6c, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x78, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x38, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x38, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x03, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x38, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x03, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x38, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x3c, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x3c, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x1e, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x06, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1f, (byte) 0x00, 
            (byte) 0x0c, (byte) 0x00, (byte) 0x06, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x1f, (byte) 0x80, (byte) 0x7f, (byte) 0x00, 
            (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x1f, 
            (byte) 0xe0, (byte) 0xff, (byte) 0x00, (byte) 0x06, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0xf0, (byte) 0x80, 
            (byte) 0x01, (byte) 0x06, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x18, (byte) 0x38, (byte) 0x00, (byte) 0x03, (byte) 0x06, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x18, (byte) 0x1c, 
            (byte) 0x00, (byte) 0x06, (byte) 0x07, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x18, (byte) 0x07, (byte) 0x00, (byte) 0x0e, 
            (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x98, 
            (byte) 0x03, (byte) 0x00, (byte) 0x0c, (byte) 0x03, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0xf8, (byte) 0x01, (byte) 0x00, 
            (byte) 0x18, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x70, (byte) 0x00, (byte) 0x00, (byte) 0xf0, (byte) 0x01, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0xe0, (byte) 0x00, (byte) 0x00, (byte) 0x00, 
            (byte) 0x00, (byte) 0x00, });
	
}
