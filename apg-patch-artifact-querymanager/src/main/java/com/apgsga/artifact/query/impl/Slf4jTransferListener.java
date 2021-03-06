package com.apgsga.artifact.query.impl;

import org.eclipse.aether.transfer.AbstractTransferListener;
import org.eclipse.aether.transfer.MetadataNotFoundException;
import org.eclipse.aether.transfer.TransferEvent;
import org.eclipse.aether.transfer.TransferResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Slf4jTransferListener extends AbstractTransferListener {

	protected static final Logger LOGGER = LoggerFactory.getLogger("Aether Transfer to Local Repo");

	private final Map<TransferResource, Long> downloads = new ConcurrentHashMap<>();

	private int lastLength;

	@Override
	public void transferInitiated(TransferEvent event) {
		String message = event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploading" : "Downloading";

		LOGGER.debug("{} : {} , {}", message, event.getResource().getRepositoryUrl(),
				event.getResource().getResourceName());
	}

	@Override
	public void transferProgressed(TransferEvent event) {
		TransferResource resource = event.getResource();
		downloads.put(resource, event.getTransferredBytes());

		StringBuilder buffer = new StringBuilder(64);

		for (Map.Entry<TransferResource, Long> entry : downloads.entrySet()) {
			long total = entry.getKey().getContentLength();
			long complete = entry.getValue();

			buffer.append(getStatus(complete, total)).append("  ");
		}

		int pad = lastLength - buffer.length();
		lastLength = buffer.length();
		pad(buffer, pad);
		String output = buffer.toString();
		LOGGER.debug(output);
	}

	private String getStatus(long complete, long total) {
		if (total >= 1024) {
			return toKB(complete) + "/" + toKB(total) + " KB ";
		} else if (total >= 0) {
			return complete + "/" + total + " B ";
		} else if (complete >= 1024) {
			return toKB(complete) + " KB ";
		} else {
			return complete + " B ";
		}
	}

	private void pad(StringBuilder buffer, int spaces) {
		String block = "                                        ";
		while (spaces > 0) {
			int n = Math.min(spaces, block.length());
			buffer.append(block, 0, n);
			spaces -= n;
		}
	}

	@Override
	public void transferSucceeded(TransferEvent event) {
		transferCompleted(event);

		TransferResource resource = event.getResource();
		long contentLength = event.getTransferredBytes();
		if (contentLength >= 0) {
			String type = (event.getRequestType() == TransferEvent.RequestType.PUT ? "Uploaded" : "Downloaded");
			String len = contentLength >= 1024 ? toKB(contentLength) + " KB" : contentLength + " B";

			String throughput = "";
			long duration = System.currentTimeMillis() - resource.getTransferStartTime();
			if (duration > 0) {
				long bytes = contentLength - resource.getResumeOffset();
				DecimalFormat format = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.ENGLISH));
				double kbPerSec = (bytes / 1024.0) / (duration / 1000.0);
				throughput = " at " + format.format(kbPerSec) + " KB/sec";
			}

			LOGGER.info("{}: {} {} ( {} {})", type, resource.getRepositoryUrl(), resource.getResourceName(), len,
					throughput);
		}
	}

	@Override
	public void transferFailed(TransferEvent event) {
		transferCompleted(event);

		if (!(event.getException() instanceof MetadataNotFoundException)) {
			LOGGER.error("Aether Transfer Exception", event.getException());
		}
	}

	private void transferCompleted(TransferEvent event) {
		downloads.remove(event.getResource());

		StringBuilder buffer = new StringBuilder(64);
		pad(buffer, lastLength);
		String output = buffer.toString();
		LOGGER.info(output);
	}

	@Override
	public void transferCorrupted(TransferEvent event) {
		LOGGER.error("Aether Transfer Exception", event.getException());
	}

	protected long toKB(long bytes) {
		return (bytes + 1023) / 1024;
	}

}
