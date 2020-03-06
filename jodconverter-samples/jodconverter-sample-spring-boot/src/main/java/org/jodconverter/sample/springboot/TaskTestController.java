package org.jodconverter.sample.springboot;

import org.jodconverter.core.office.OfficeException;
import org.jodconverter.core.office.OfficeManager;
import org.jodconverter.local.office.SocketOfficeManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.PreDestroy;

public class TaskTestController {

    private final OfficeManager manager;

    @Autowired
    private ResourceLoader resourceLoader;

    public TaskTestController() throws OfficeException {
        manager = SocketOfficeManager.builder()
                .poolSize(3)
                .host("libreoffice")
                .port(8100)
                .build();
        manager.start();
    }

    @PreDestroy
    public void destroier() throws OfficeException {
        manager.stop();
    }

    @GetMapping(value = "public/test")
    public ResponseEntity test() throws OfficeException {

        manager.execute(new TestTask(resourceLoader));

        return ResponseEntity.ok("ok");
    }

}
