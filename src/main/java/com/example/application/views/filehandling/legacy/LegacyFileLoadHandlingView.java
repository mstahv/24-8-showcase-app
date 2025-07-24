package com.example.application.views.filehandling.legacy;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("removal")
@PageTitle("Simplified Legacy Up- & Download Handling")
@Route("file-load-handling/legacy")
@Menu(order = 0, icon = LineAwesomeIconUrl.FILE_CODE)
public class LegacyFileLoadHandlingView extends VerticalLayout {

    private final Set<FileData> fileDataSet = new HashSet<>();
    private final Grid<FileData> grid;

    public LegacyFileLoadHandlingView() {

        grid = createFileDataGrid();
        add(grid);

        var upload = createUpload();
        add(upload);
    }

    private Upload createUpload() {
        var buffer = new MultiFileMemoryBuffer();
        var upload = new Upload(buffer);

        upload.addSucceededListener(event -> {
            var content = buffer.getOutputBuffer(event.getFileName()).toByteArray();
            fileDataSet.add(new FileData(event.getFileName(), event.getMIMEType(), content));

            //Practical Example: pass data-stream
            processWithExternalLibrary(content);

            grid.getDataProvider().refreshAll();
        });
        upload.addAllFinishedListener(event -> upload.clearFileList());

        return upload;
    }

    private void processWithExternalLibrary(byte[] content) {
//        try (ByteArrayInputStream stream = new ByteArrayInputStream(content)) {
//            externalService.processStream(stream);
//            Notification.show("✓ File processed successfully.", 3000, Notification.Position.TOP_CENTER)
//                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//        } catch (Exception ex) {
//            Notification.show("✗ An error occurred!", 5000, Notification.Position.TOP_CENTER)
//                    .addThemeVariants(NotificationVariant.LUMO_ERROR);
//            ex.printStackTrace();
//        }
    }

    private Grid<FileData> createFileDataGrid() {
        var grid = new Grid<>(FileData.class);
        grid.setColumns("fileName", "contentType");
        grid.addComponentColumn(this::createDownloadLink)
                .setHeader("Content");
        grid.setItems(fileDataSet);
        return grid;
    }

    private Component createDownloadLink(FileData fileData) {
        var resource = new StreamResource(fileData.fileName(), () -> new ByteArrayInputStream(fileData.content()));
        var downloadLink = new Anchor(resource, fileData.fileName());
        downloadLink.getElement().setAttribute("download", true);
        return downloadLink;
    }
}

