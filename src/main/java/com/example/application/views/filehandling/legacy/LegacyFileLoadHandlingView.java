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
import java.io.InputStream;
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
            // Note, getInputStream methods in earlier Vaadin versions is just a convenience method
            // to wrap the buffer with ByteArrayInputStream (not very efficient, temporarily wastes a lot of memory and makes it easy to create memory leaks)
            processWithExternalLibrary(buffer.getInputStream(event.getFileName()));

            grid.getDataProvider().refreshAll();
        });
        upload.addAllFinishedListener(event -> upload.clearFileList());

        return upload;
    }

    private void processWithExternalLibrary(InputStream stream) {
//            externalService.processStream(stream);
//            Notification.show("✓ File processed successfully.", 3000, Notification.Position.TOP_CENTER)
//                    .addThemeVariants(NotificationVariant.LUMO_SUCCESS);
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
        // Old Vaadin versions guided towards using StreamResource for file downloads
        // that often caused people to reserve memory for the whole file content up front, like here
        var resource = new StreamResource(fileData.fileName(), () -> new ByteArrayInputStream(fileData.content()));
        var downloadLink = new Anchor(resource, fileData.fileName());
        downloadLink.getElement().setAttribute("download", true);
        return downloadLink;
    }
}

