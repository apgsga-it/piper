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
 * As a temporary solution, we use the dormant Apache commons library:
 * https://commons.apache.org/proper/commons-transaction/ Specifically:
 * https://commons.apache.org/proper/commons-transaction/file/index.html Which though 
 * is "dormant", but which seems to working just fine. Better, then probably ,
 * if i would write my own code and in much less time ;-) ... certainly with much less code.
 * See also : https://commons.apache.org/proper/commons-transaction/apidocs/org/apache/commons/transaction/file/FileResourceManager.html
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
			String targetPath = storagePath.getFile().getAbsolutePath();
			String workDir = tempStoragePath.getFile().getAbsolutePath();
			FileResourceManager frm = new FileResourceManager(targetPath,
					workDir, false, loggerFacade, true);
			frm.start();
			LOGGER.info("Resource Manager started with target Dir: " + targetPath + ", and work Dir: " + workDir);
			Object txId = frm.generatedUniqueTxId();
			frm.startTransaction(txId);
			LOGGER.info("Started File write Transaction with: " + txId.toString());
			OutputStream outputStream = frm.writeResource(txId, fileName, false);
			IOUtils.write(outputString, outputStream);
			frm.commitTransaction(txId);
			LOGGER.info("Commited File write Transaction with: " + txId.toString());

		} catch (Throwable e) {
			LOGGER.error("Transactional Write of : " + outputString + " failed: " + e.getLocalizedMessage());
			LOGGER.error(ExceptionUtils.getFullStackTrace(e));
			throw new RuntimeException(e);
		}

	}

}
