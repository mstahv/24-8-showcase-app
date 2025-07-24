package com.example.application.views.filehandling.legacy;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.dataview.GridListDataView;
import com.vaadin.flow.component.html.Anchor;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.router.Menu;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.streams.DownloadHandler;
import com.vaadin.flow.server.streams.DownloadResponse;
import com.vaadin.flow.server.streams.UploadHandler;
import org.vaadin.lineawesome.LineAwesomeIconUrl;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Set;

@PageTitle("Migrated Legacy Up- & Download Handling")
@Route("file-load-handling/migrated")
@Menu(order = 0, icon = LineAwesomeIconUrl.FILE_CODE)
public class MigratedLegacyFileLoadHandlingView extends VerticalLayout {

    private final Set<FileData> fileDataSet = new HashSet<>();
    private GridListDataView<FileData> fileDataGridListDataView;

    private final UploadHandler uploadHandler = event -> {
        var fileName = event.getFileName();
        var content = event.getInputStream().readAllBytes();

        fileDataSet.add(new FileData(fileName, event.getContentType(), content));

        //Practical Example: pass data-stream

        event.getUI().access(() -> {
            fileDataGridListDataView.refreshAll();
            processWithExternalLibrary(content);
        });
    };

    public MigratedLegacyFileLoadHandlingView() {

        var grid = createFileDataGrid();
        add(grid);

        var upload = createUpload();
        add(upload);
    }

    private Upload createUpload() {
        var upload = new Upload(uploadHandler);

        upload.setMaxFileSize(10 * 1024 * 1024); //max 10MB
        upload.setAcceptedFileTypes("pdf", "application/pdf",
                "image/png", "image/jpg", "image/jpeg", "image/pdf");

//        upload.addSucceededListener(event -> {
//            var content = buffer.getOutputBuffer(event.getFileName()).toByteArray();
//            fileDataSet.add(new FileData(event.getFileName(), content));
//            grid.getDataProvider().refreshAll();
//        });
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
        fileDataGridListDataView = grid.setItems(fileDataSet);
        return grid;
    }

//    private Component createDownloadLink(FileData fileData) {
//        var resource = new StreamResource(fileData.fileName, () -> new ByteArrayInputStream(fileData.content));
//        var downloadLink = new Anchor(resource, fileData.fileName);
//        downloadLink.getElement().setAttribute("download", true);
//        return downloadLink;
//    }

    private Anchor createDownloadLink(FileData fileData) {

        var downloadHelper = DownloadHandler.fromInputStream(event -> new DownloadResponse(
                new ByteArrayInputStream(fileData.content()), fileData.fileName(), fileData.contentType(), -1));

        return new Anchor(downloadHelper, fileData.fileName());
    }
}
