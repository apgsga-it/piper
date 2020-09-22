package com.apgsga.microservice.patch.core.impl.persistence;

import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.transaction.file.FileResourceManager;
import org.apache.commons.transaction.util.CommonsLoggingLogger;
import org.apache.commons.transaction.util.LoggerFacade;

import com.apgsga.microservice.patch.exceptions.ExceptionFactory;

/**
 * "Simple" File Write Manager Attempts to implement a "Atomic" File write:
 * 
 * As a temporary solution, we use the dormant Apache commons library:
 * https://commons.apache.org/proper/commons-transaction/ Specifically:
 * https://commons.apache.org/proper/commons-transaction/file/index.html Which
 * though is "dormant", but which seems to working just fine. Better, then
 * probably , if i would write my own code and in much less time ;-) ...
 * certainly with much less code. See also :
 * https://commons.apache.org/proper/commons-transaction/apidocs/org/apache/
 * commons/transaction/file/FileResourceManager.html
 * 
 * @author che
 *
 */
public class AtomicFileWriteManager {

	public static AtomicFileWriteManager create(AbstractFilebasedPersistence fileBasedPersistance) {
		return new AtomicFileWriteManager(fileBasedPersistance);
	}

	protected static final Log LOGGER = LogFactory.getLog(AtomicFileWriteManager.class.getName());
	private LoggerFacade loggerFacade = new CommonsLoggingLogger(LOGGER);

	private AbstractFilebasedPersistence fileBasedPersistenance;

	private AtomicFileWriteManager(AbstractFilebasedPersistence fileBasedPersistenance) {
		super();
		this.fileBasedPersistenance = fileBasedPersistenance;
	}

	public void write(String outputString, String fileName) {
		try {
			LOGGER.info("Atomic write of: " + outputString + " to File: " + fileName);
			String targetPath = fileBasedPersistenance.getStoragePath().getFile().getAbsolutePath();
			String workDir = fileBasedPersistenance.getTempStoragePath().getFile().getAbsolutePath();
			FileResourceManager frm = new FileResourceManager(targetPath, workDir, false, loggerFacade, true);
			frm.start();
			LOGGER.info("Resource Manager started with target Dir: " + targetPath + ", and work Dir: " + workDir);
			Object txId = frm.generatedUniqueTxId();
			frm.startTransaction(txId);
			LOGGER.info("Started File write Transaction with: " + txId.toString());
			OutputStream outputStream = frm.writeResource(txId, fileName, false);
			IOUtils.write(outputString, outputStream);
			frm.commitTransaction(txId);
			LOGGER.info("Commited File write Transaction with: " + txId.toString());

		} catch (Exception e) {
			throw ExceptionFactory.createPatchServiceRuntimeException("AtomicFileWriteManager.write.exception",
					new Object[] { e.getMessage(), outputString, fileName }, e);
		}

	}

}
