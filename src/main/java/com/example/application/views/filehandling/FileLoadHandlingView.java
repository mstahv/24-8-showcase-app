package com.example.application.views.filehandling;

import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.UploadHandler;
import com.vaadin.flow.server.streams.UploadMetadata;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.File;

@PageTitle("Simplified Up- & Download Handling")
@Route("file-load-handling")
@Menu(order = 0, icon = LineAwesomeIconUrl.FILE)
public class FileLoadHandlingView extends VerticalLayout {

    public FileLoadHandlingView() {

        var upload = new Upload(
                UploadHandler.toFile((metadata, file) -> {
                            createAndAddDownloadLink(file);
                            SuccessNotification.show("File uploaded successfully");
                        },
                        this::createFile)
        );

        upload.setMaxFileSize(10 * 1024 * 1024); //set 10MB
        upload.setAcceptedFileTypes("zip", "application/zip");
        upload.addFileRejectedListener(event ->
                ErrorNotification.show("Filetype not accepted"));

        add(upload);
    }

    private void createAndAddDownloadLink(File file) {
        var download = new Anchor(DownloadHandler.forFile(file), "download " + file.getName());
        add(download);

    }

    private File createFile(UploadMetadata metadata) {
        File uploadDir = new File("uploads");
        if (!uploadDir.exists() && !uploadDir.mkdir()) {
            throw new RuntimeException("Failed to create upload directory");
        }

        return new File(uploadDir, metadata.fileName());
    }

    public static class SuccessNotification extends Notification {

        public static Notification show(String text) {
            Notification notification = new Notification(text, 3000);
            notification.setPosition(Position.BOTTOM_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
            notification.open();

            return notification;
        }
    }

    public static class ErrorNotification extends Notification {
        public static Notification show(String text) {
            Notification notification = new Notification(text, 3000);
            notification.setPosition(Position.BOTTOM_CENTER);
            notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
            notification.open();

            return notification;
        }
    }
}
