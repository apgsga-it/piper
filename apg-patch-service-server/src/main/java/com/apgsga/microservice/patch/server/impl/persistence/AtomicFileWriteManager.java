package com.apgsga.microservice.patch.server.impl.persistence;

import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.transaction.file.FileResourceManager;
import org.apache.commons.transaction.util.CommonsLoggingLogger;
import org.apache.commons.transaction.util.LoggerFacade;
import org.springframework.core.io.Resource;

/**
 * "Simple" File Write Manager Attempts to implement a "Atomic" File write:
 * 
 * 1. If the File exists is the Target Directory, it is copied to a Backup
 * Directory , assumed to be on the same Filesystem as the Target File
 * 
 * 2. If 1. fails, the orginal State of the Target File is maintained and we
 * throw a Runtime Exception
 * 
 * 3. The Target File to be written, is written to a temporary Directory ,
 * assumed to be on the same Filesystem as the Target file
 * 
 * 4. If 3. Fails a Runtime Exceptions is thrown: the orginal state of the
 * Target File is maintained
 * 
 * 5. The temporary File in the Temporary directory is moved to target Directory
 * with a temporary Name
 * 
 * 6. If 5 fails the original state of the File is maintained
 * 
 * 7. If 6 ok, we delete the current file and rename the temporary File created
 * in 5.
 * 
 * 8. 7 is the critical path: if 7 fails , we may have a corrupt state of the
 * Target File, but we have a Backup in the Backup Directory
 * 
 * 
 * As a temporary solution, we use the dormant Apache commons library:
 * https://commons.apache.org/proper/commons-transaction/ Specifically:
 * https://commons.apache.org/proper/commons-transaction/file/index.html Which
 * is "dormant", but which seems to working just fine. Better, then probably ,
 * if i would write my own code and in much less time ;-)
 * 
 * @author che
 *
 */
public class AtomicFileWriteManager {

	public static AtomicFileWriteManager create(Resource storagePath, Resource tempStoragePath) {
		return new AtomicFileWriteManager(storagePath, tempStoragePath);
	}

	protected final Log LOGGER = LogFactory.getLog(getClass());
	private LoggerFacade loggerFacade = new CommonsLoggingLogger(LOGGER);

	private Resource storagePath;

	private Resource tempStoragePath;

	private AtomicFileWriteManager(Resource storagePath, Resource tempStoragePath) {
		super();
		this.storagePath = storagePath;
		this.tempStoragePath = tempStoragePath;
	}

	public void write(String outputString, String fileName) {
		try {
			FileResourceManager frm = new FileResourceManager(storagePath.getFile().getAbsolutePath(),
					tempStoragePath.getFile().getAbsolutePath(), false, loggerFacade);
			frm.start();
			Object txId = frm.generatedUniqueTxId();
			frm.startTransaction(txId);
			OutputStream outputStream = frm.writeResource(txId, fileName, false);
			IOUtils.write(outputString, outputStream);
			frm.commitTransaction(txId);

		} catch (Throwable e) {
			LOGGER.error("Transactional Write of : " + outputString + " failed: " + e.getLocalizedMessage());
			LOGGER.error(ExceptionUtils.getFullStackTrace(e));
			throw new RuntimeException(e);
		}

	}

}
