package org.jodconverter.sample.springboot;

import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XStorable;
import com.sun.star.io.XInputStream;
import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.XComponent;
import com.sun.star.lib.uno.adapter.ByteArrayToXInputStreamAdapter;
import com.sun.star.lib.uno.adapter.XOutputStreamToByteArrayAdapter;
import com.sun.star.uno.UnoRuntime;
import org.apache.commons.io.IOUtils;
import org.jodconverter.core.office.OfficeContext;
import org.jodconverter.core.task.OfficeTask;
import org.jodconverter.local.office.OfficeConnection;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.*;
import java.util.UUID;

public class TestTask implements OfficeTask {


    private ResourceLoader resourceLoader;

    public TestTask(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void execute(OfficeContext context) {
        OfficeConnection officeConnection = (OfficeConnection) context;

        String fileUrl = "testFile.odt";
        Resource resource = resourceLoader.getResource("classpath:" + fileUrl);

        PropertyValue[] loadProps = new PropertyValue[1];
        try {
            InputStream inputStream = resource.getInputStream();
            XInputStream xInputStream = new ByteArrayToXInputStreamAdapter(IOUtils.toByteArray(inputStream));
            loadProps[0] = new PropertyValue();
            loadProps[0].Name = "InputStream";
            loadProps[0].Value = xInputStream;
        } catch (IOException e) {
            e.printStackTrace();
        }

        XComponent document = null;
        try {
            document = officeConnection.getComponentLoader()
                    .loadComponentFromURL("private:stream", "_blank", 0, loadProps);
        } catch (com.sun.star.io.IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        XOutputStreamToByteArrayAdapter outputStream = new XOutputStreamToByteArrayAdapter();

        PropertyValue[] exportProperties = new PropertyValue[4];
        exportProperties[0] = new PropertyValue();
        exportProperties[0].Name = "FilterName";
        exportProperties[0].Value = "writer_pdf_Export";

        exportProperties[1] = new PropertyValue();
        exportProperties[1].Name = "OutputStream";
        exportProperties[1].Value = outputStream;

        XStorable xStorable = (XStorable) UnoRuntime.queryInterface(XStorable.class, document);
        try {
            xStorable.storeToURL("private:stream", exportProperties);
        } catch (com.sun.star.io.IOException e) {
            e.printStackTrace();
        }

        try {
            outputStream.closeOutput();
        } catch (com.sun.star.io.IOException e) {
            e.printStackTrace();
        }

        byte[] result = outputStream.getBuffer();

        OutputStream out = null;
        try {
            out = new FileOutputStream(UUID.randomUUID().toString() + ".pdf");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            out.write(result);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        document.dispose();
    }
}
