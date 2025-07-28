package com.example.application.views.filehandling.legacy;

import com.example.application.data.SamplePerson;
import com.example.application.services.SamplePersonService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.StreamResource;
import net.coobird.thumbnailator.Thumbnailator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Example of a legacy view that displays a list of persons with their photos,
 * photo data is saved in JPA.
 */
@Route
public class PersonPhotosLegacy extends Grid<SamplePerson> {

    private final SamplePersonService service;

    public PersonPhotosLegacy(SamplePersonService repository) {
        this.service = repository;
        addColumn(SamplePerson::getFirstName).setHeader("First Name");
        addColumn(SamplePerson::getLastName).setHeader("Last Name");
        addComponentColumn(person -> {
            HorizontalLayout layout = new HorizontalLayout();

            if (person.isPhotoAdded()) {

                Image image = new Image();
                // In old API StreamResources (with its "inverted API) one typically created memory
                // consuming StreamResources up front, even if resource was never consumed

                // The backend here loads photo lazily, using service method to load photo contents
                byte[] photo = service.lazyLoadPhoto(person);
                StreamResource streamResource = new StreamResource("photo-" + person.getId() + ".png", () -> new ByteArrayInputStream(person.getPhoto()));
                streamResource.setContentType("image/png");
                image.setSrc(streamResource);
                layout.add(image);
            } else {
                layout.add("No Photo");
            }
            Upload upload = new Upload();
            upload.setDropAllowed(false); // wastes too much space...
            upload.setAcceptedFileTypes("image/*");
            // In old API there is always a receiver. The Receiver API infamously had
            // "inverted" input and output streams, so typically one used one of the
            // ready-made buffers that stored the data temporarily into memory or disk
            MemoryBuffer memoryBuffer = new MemoryBuffer();
            upload.setReceiver(memoryBuffer);
            upload.addSucceededListener(event -> {
                try {
                    // In old API one typically hooked to SucceededEvent and took the buffered content
                    // Note that this input stream is now created against in-memory bytearray
                    InputStream inputStream = memoryBuffer.getInputStream();
                    ByteArrayOutputStream scaled = new ByteArrayOutputStream();

                    // now we have the input stream that we need typically to handle the data
                    // this could be e.g. resizing as implemented here, some other sanitation or e.g. passing xml to JAXP
                    Thumbnailator.createThumbnail(inputStream, scaled, 100, 100);
                    person.setPhoto(scaled.toByteArray());
                    person.setPhotoAdded(true);
                    repository.save(person);
                    list();
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
                p -> service.list(p).toList()
        );
    }
}
