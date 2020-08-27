package files;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;
import java.util.ResourceBundle;

public class FileProcessor
{
    private static final boolean SAVE_FILE = true;
    private static FileProcessor processor;
    
    private final Properties properties = new Properties();
    private final String FILE_NAME;
    private final String name;
    
    private FileProcessor(String name) {
        this.name = name;
        FILE_NAME = name + ".properties";
    }
    
    public static FileProcessor createFile(String name) {
        return processor = new FileProcessor(name);
    }
      
    public static FileProcessor getFile() {
        return processor;
    }
    
    public Properties getProperties() {
        return properties;
    }
    
    public void loadFile() 
    {
        Reader read = null;
        
        try {
            read = new FileReader(FILE_NAME);
            properties.load(read);
        }
        catch(Exception fileErr) {
            System.out.format("%s%n", fileErr);
        }
        finally 
        {
            try 
            {
                if (read != null) {
                    read.close();
                }
            } 
            catch (IOException ioErr) {
                System.out.format("%s%n", ioErr);
            }
        }
    }
    
    public void saveFile()
    {
        if (SAVE_FILE)
        {
            try (FileWriter fileWriter = new FileWriter(FILE_NAME)) {
                properties.store(fileWriter, name + "system preference");
            }
            catch (Exception fileErr) {
                System.out.format("%s%n", fileErr);
            } 
        }
    }
    
    public static ResourceBundle getResourceBundle(FileProcessor fileProcessor) {
        fileProcessor.loadFile();
        return ResourceBundle.getBundle(fileProcessor.getProperties().get("file").toString());
    }
}