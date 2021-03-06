package com.apgsga.microservice.patch.core.impl.persistence;

import com.apgsga.microservice.patch.exceptions.ExceptionFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.transaction.file.FileResourceManager;
import org.apache.commons.transaction.util.CommonsLoggingLogger;
import org.apache.commons.transaction.util.LoggerFacade;
import org.springframework.core.io.Resource;

import java.io.OutputStream;

/**
 * "Simple" File Write Manager Attempts to implement a "Atomic" File write:
 * <p>
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
 */
public class AtomicFileWriteManager {

    public static AtomicFileWriteManager create(Resource storagePath, Resource tempStoragePath) {
        return new AtomicFileWriteManager(storagePath, tempStoragePath);
    }

    protected static final Log LOGGER = LogFactory.getLog(AtomicFileWriteManager.class.getName());
    private final LoggerFacade loggerFacade = new CommonsLoggingLogger(LOGGER);

    private final Resource storagePath;
    private final Resource tempStoragePath;

    public AtomicFileWriteManager(Resource storagePath, Resource tempStoragePath) {
        this.storagePath = storagePath;
        this.tempStoragePath = tempStoragePath;
    }

    public void write(String outputString, String fileName) {
        try {
            LOGGER.info("Atomic write of: " + outputString + " to File: " + fileName);
            String targetPath = storagePath.getFile().getAbsolutePath();
            String workDir = tempStoragePath.getFile().getAbsolutePath();
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
            throw ExceptionFactory.create("Exception: <%s> while atomic write to File %s of: %s ", e,
                    e.getMessage(), outputString, fileName);
        }

    }

}
