package com.example.application.views.filehandling.legacy;

import com.example.application.data.SamplePerson;
import com.example.application.data.SamplePersonRepository;
import com.example.application.services.SamplePersonService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import net.coobird.thumbnailator.Thumbnailator;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Example of a legacy view that displays a list of persons with their photos,
 * photo data is saved in JPA.
 */
@Route
public class PersonPhotosUpdated extends Grid<SamplePerson> {

    private final SamplePersonService service;

    public PersonPhotosUpdated(SamplePersonService service) {
        this.service = service;
        addColumn(SamplePerson::getFirstName).setHeader("First Name");
        addColumn(SamplePerson::getLastName).setHeader("Last Name");
        addComponentColumn(person -> {
            HorizontalLayout layout = new HorizontalLayout();

            if (person.isPhotoAdded()) {
                Image image = new Image();
                image.setSrc(downloadEvent -> {
                    downloadEvent.setFileName(person.getId() + ".png");
                    downloadEvent.setContentType("image/png");
                    // In new callback based default API one writes the content
                    // of the file only once really requested
                    // Note, in optimal situation the file contents would be streamed from DB driver directly,
                    // but with JPA this would require raw JDBC or custom Hibernate APIs
                    downloadEvent.getOutputStream().write(service.lazyLoadPhoto(person));
                });
                layout.add(image);
            } else {
                layout.add("No Photo");
            }
            Upload upload = new Upload();
            upload.setDropAllowed(false); // wastes too much space...
            upload.setAcceptedFileTypes("image/*");
            upload.setUploadHandler(uploadEvent -> {
                try {
                    // With new API we can directly access the file contents pushed by the browser
                    // The whole original image is never stored in obsolete byte[] buffers
                    // NOTE the known issue with Spring Boot, it gets buffered on a file though, before passed here...
                    ByteArrayOutputStream scaled = new ByteArrayOutputStream();
                    Thumbnailator.createThumbnail(uploadEvent.getInputStream(), scaled, 100, 100);
                    person.setPhoto(scaled.toByteArray());
                    person.setPhotoAdded(true);
                    service.save(person);
                    // File handling as potentially long process doesn't lock UI
                    // When we actually want to modify UI, it needs to happen with UI.access block
                    uploadEvent.getUI().access(() -> {
                        list();
                    });
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            layout.add(upload);

            return layout;
        }).setHeader("Photo");
        list();
    }

    private void list() {
        setItemsPageable(
                p -> service.list(p).stream().toList()
        );
    }
}
