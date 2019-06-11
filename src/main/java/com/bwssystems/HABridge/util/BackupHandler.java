package com.bwssystems.HABridge.util;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BackupHandler {
    final Logger log = LoggerFactory.getLogger(BackupHandler.class);
    private Path repositoryPath;
    private String fileExtension;
    private String defaultName;

	protected void setupParams(Path aPath, String anExtension, String adefaultName) {
		repositoryPath = aPath;
		if(anExtension.substring(0, 1).equalsIgnoreCase("."))
			fileExtension = anExtension;
		else
			fileExtension = "." + anExtension;
		
		defaultName = adefaultName;
		
		log.debug("setupParams has defaultName: " + defaultName + " and file extension as: " + fileExtension);
	}

	public String backup(String aFilename) {
        if(aFilename == null || aFilename.equalsIgnoreCase("")) {
        	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        	aFilename = defaultName + dateFormat.format(Calendar.getInstance().getTime()) + fileExtension; 
        }
        else
        	aFilename = aFilename + fileExtension;
    	try {
			Files.copy(repositoryPath, FileSystems.getDefault().getPath(repositoryPath.getParent().toString(), aFilename), StandardCopyOption.COPY_ATTRIBUTES);
		} catch (IOException e) {
			log.error("Could not backup to file: " + aFilename + " message: " + e.getMessage(), e);
		}
        log.debug("Backup repository: " + aFilename);
        return aFilename;
    }

	public String deleteBackup(String aFilename) {
        log.debug("Delete backup repository: " + aFilename);
        try {
			Files.delete(FileSystems.getDefault().getPath(repositoryPath.getParent().toString(), aFilename));
		} catch (IOException e) {
			log.error("Could not delete file: " + aFilename + " message: " + e.getMessage(), e);
		}
        return aFilename;
    }

	public String restoreBackup(String aFilename) {
        log.debug("Restore backup repository: " + aFilename);
		try {
			Path target = null;
			if(Files.exists(repositoryPath)) {
	        	DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
				target = FileSystems.getDefault().getPath(repositoryPath.getParent().toString(), defaultName + dateFormat.format(Calendar.getInstance().getTime()) + fileExtension);
				Files.move(repositoryPath, target);
			}
			Files.copy(FileSystems.getDefault().getPath(repositoryPath.getParent().toString(), aFilename), repositoryPath, StandardCopyOption.COPY_ATTRIBUTES);
		} catch (IOException e) {
			log.error("Error restoring the file: " + aFilename + " message: " + e.getMessage(), e);
			return null;
		}
        return aFilename;
    }

	public List<String> getBackups() {
		List<String> theFilenames = new ArrayList<String>();
		Path dir = repositoryPath.getParent();
		try (DirectoryStream<Path> stream =
		     Files.newDirectoryStream(dir, "*.{"+ fileExtension.substring(1) + "}")) {
		    for (Path entry: stream) {
		        theFilenames.add(entry.getFileName().toString());
		    }
		} catch (IOException x) {
		    // IOException can never be thrown by the iteration.
		    // In this snippet, it can // only be thrown by newDirectoryStream.
			log.warn("Issue getting directory listing for backups in directory: " + x.getMessage());
		}
		return theFilenames;
	}
	
	public String downloadBackup(String aFilename) {
		Path filePath = FileSystems.getDefault().getPath(repositoryPath.getParent().toString(), aFilename);

		String content = null;
		if (Files.notExists(filePath) || !Files.isReadable(filePath)) {
			log.warn("Error reading the file: {} - Does not exist or is not readable. continuing...", aFilename);
			return null;
		}

		try {
			content = new String(Files.readAllBytes(filePath));
		} catch (IOException e) {
			log.error("Error reading the file: {} message: {}", aFilename, e.getMessage(), e);
		}

		return content;
	}

	public String uploadBackup(String aFilename, String theContent) {
		String successMessage = null;
		if(aFilename == null || aFilename.isEmpty()) {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
			aFilename = defaultName + "upload-" + dateFormat.format(Calendar.getInstance().getTime()) + fileExtension; 
		} else {
			if(!aFilename.endsWith(fileExtension)) {
				aFilename = aFilename +fileExtension;
			}
		}
		Path filePath = FileSystems.getDefault().getPath(repositoryPath.getParent().toString(), aFilename);

		successMessage = uploadWriter(theContent, filePath);

		return successMessage;
	}

	private String uploadWriter(String content, Path filePath) {
		String aMessage = null;
		if (Files.exists(filePath)) {
			aMessage = "Error: File exists, cannot write: " + filePath;
			log.error(aMessage);
			return aMessage;
		}

		if (Files.notExists(filePath.getParent())) {
			try {
				Files.createDirectories(filePath.getParent());
			} catch (IOException e) {
				aMessage = "Error: creating the directory: " + filePath + " message: " + e.getMessage();
				log.error(aMessage, e);
				return aMessage;
			}
		}

		try {
			Files.write(filePath, content.getBytes(), StandardOpenOption.CREATE);
			aMessage = "Success: creating file: " + filePath;
		} catch (IOException e) {
			aMessage = "Error: writing the file: " + filePath + " message: " + e.getMessage();
			log.error(aMessage, e);
		}

		return aMessage;
	}
}
