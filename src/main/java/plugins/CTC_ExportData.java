// Define package name as "plugins" as show here
package plugins;

// Import needed classes here 
import java.io.File;
import java.io.IOException;
import java.util.TreeMap;

import jex.statics.JEXStatics;
import logs.Logs;

import org.apache.commons.io.FileUtils;
import org.scijava.plugin.Plugin;

import tables.DimensionMap;
import weka.core.converters.JEXTableWriter;
import Database.DBObjects.JEXData;
import Database.DBObjects.JEXEntry;
import Database.DataReader.FileReader;
import Database.DataReader.ImageReader;
import function.plugin.mechanism.InputMarker;
import function.plugin.mechanism.JEXPlugin;
import function.plugin.mechanism.MarkerConstants;
import function.plugin.mechanism.ParameterMarker;

// Specify plugin characteristics here
@Plugin(
		type = JEXPlugin.class,
		name="CTC - Export Data",
		menuPath="CTC Toolbox",
		visible=true,
		description="An R Shiny app for analyzing single-cell CTC data."
		)
public class CTC_ExportData extends JEXPlugin {
	
	// Define a constructor that takes no arguments.
	public CTC_ExportData()
	{}
	
	/////////// Define Inputs here ///////////
	
	@InputMarker(uiOrder=1, name="Cell Crops", type=MarkerConstants.TYPE_IMAGE, description="Image crops for each cell and image channel ", optional=false)
	JEXData imageData;
	
	@InputMarker(uiOrder=2, name="Colocalization Data", type=MarkerConstants.TYPE_FILE, description="Table of colocalization data", optional=false)
	JEXData tableData;
	
	/////////// Define Parameters here ///////////
	
	@ParameterMarker(uiOrder=3, name="Where to save...", description="Opens a file chooser dialog box and returns the path String of the user choice (file or folder).", ui=MarkerConstants.UI_FILECHOOSER, defaultText="~/")
	String path;
	
	/////////// Define Outputs here ///////////
	
	// Define threading capability here (set to 1 if using non-final static variables shared between function instances).
	@Override
	public int getMaxThreads()
	{
		return 1;
	}
	
	// Code the actions of the plugin here using comments for significant sections of code to enhance readability as shown here
	@Override
	public boolean run(JEXEntry optionalEntry)
	{
		// Validate the input data
		if(imageData == null || !imageData.getTypeName().getType().equals(JEXData.IMAGE))
		{
			return false;
		}
		if(tableData == null || !tableData.getTypeName().getType().equals(JEXData.FILE))
		{
			return false;
		}
		
		// Run the function
		// Read in the input with one of the many "reader" classes (see package "Database.DataReader")
		TreeMap<DimensionMap,String> imageMap = ImageReader.readObjectToImagePathTable(imageData);
		TreeMap<DimensionMap,String> tableMap = FileReader.readObjectToFilePathTable(tableData);
		int count = 0, percentage = 0;
		
		// Cancel the function as soon as possible if the user has hit the cancel button
		// Perform this inside loops to check as often as possible.
		if(this.isCanceled())
		{
			return false;
		}
		
		JEXStatics.statusBar.setProgressPercentage(0);
		
		File pathFile = new File(path);
		if(!pathFile.isDirectory())
		{
			pathFile = pathFile.getParentFile();
		}
		
		String out = JEXTableWriter.writeTable("CellCrops", imageMap);
		File newOutFile = new File(pathFile.getAbsolutePath() + File.separator + "CellCrops.arff");
		try
		{
			FileUtils.copyFile(new File(out), newOutFile);
		}
		catch (IOException e)
		{
			Logs.log("Couldn't copy the cell crops information.", this);
			e.printStackTrace();
		}
		
		JEXStatics.statusBar.setProgressPercentage(50);
		
		String tablePath = tableMap.firstEntry().getValue();
		File tableFile = new File(tablePath);
		File newTableFile = new File(pathFile.getAbsolutePath() + File.separator + "ColocalizationData.arff");
		try
		{
			FileUtils.copyFile(tableFile, newTableFile);
		}
		catch (IOException e)
		{
			Logs.log("Couldn't copy the data table file.", this);
			e.printStackTrace();
		}
		
		JEXStatics.statusBar.setProgressPercentage(100);
		
		// Return status
		return true;
	}
}
