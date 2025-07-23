package com.example.application.views.filehandling.legacy;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.SucceededEvent;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;


@SuppressWarnings("removal")
@PageTitle("Simplified Legacy Up- & Download Handling")
@Route("lagacy-file-load-handling")
@Menu(order = 0, icon = LineAwesomeIconUrl.FILE_CODE)
public class LegacyFileLoadHandlingView extends VerticalLayout {

    public static final String UPLOAD_PATH_NAME = "uploads";

    public LegacyFileLoadHandlingView() {

        var upload = createUploadComponent();
        add(upload);
    }

    private Upload createUploadComponent() {
        MemoryBuffer buffer = new MemoryBuffer();

        var upload = new Upload(buffer);
        upload.setMaxFiles(1);
        upload.addSucceededListener(event -> {
            handleUpload(event, buffer);

            var downloadLink = createDownloadLink(event, buffer);
            add(downloadLink);
        });

        return upload;
    }

    private Anchor createDownloadLink(SucceededEvent event, MemoryBuffer buffer) {
        var resource = new StreamResource(event.getFileName(), buffer::getInputStream);
        var downloadLink = new Anchor(resource, event.getFileName());
        downloadLink.getElement().setAttribute("download", true);
        return downloadLink;
    }

    private void handleUpload(SucceededEvent event, MemoryBuffer buffer) {
        try {
            createFile(
                    event.getFileName(),
                    event.getContentLength(),
                    event.getMIMEType(),
                    buffer.getInputStream()
            );
        } catch (IOException e) {
            Notification.show("Error during saving: " + e.getMessage(), 5000, Notification.Position.MIDDLE);
        }
    }

    private void createFile(String fileName,
                            long contentLength,
                            String mimeType,
                            InputStream fileData) throws IOException {

        Path target = Paths.get(UPLOAD_PATH_NAME).resolve(fileName);
        Files.createDirectories(target.getParent());      // create a directory, if necessary

        try (InputStream in = fileData;
             OutputStream out = Files.newOutputStream(
                     target,
                     StandardOpenOption.CREATE,
                     StandardOpenOption.TRUNCATE_EXISTING)) {

            in.transferTo(out);                           // Stream-to-file copy
        }

        Notification.show(String.format("Saved %s (%s, %d bytes) to %s%n",
                fileName, mimeType, contentLength, target.toAbsolutePath()));
    }
}

