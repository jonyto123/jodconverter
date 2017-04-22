/*
 * Copyright 2004 - 2012 Mirko Nasato and contributors
 *           2016 - 2017 Simon Braconnier and contributors
 *
 * This file is part of JODConverter - Java OpenDocument Converter.
 *
 * JODConverter is an Open Source software: you can redistribute it and/or
 * modify it under the terms of either (at your option) of the following
 * licenses:
 *
 * 1. The GNU Lesser General Public License v3 (or later)
 *    http://www.gnu.org/licenses/lgpl-3.0.txt
 * 2. The Apache License, Version 2.0
 *    http://www.apache.org/licenses/LICENSE-2.0.txt
 */

package org.artofsolving.jodconverter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.artofsolving.jodconverter.document.DocumentFormat;
import org.artofsolving.jodconverter.filter.DefaultFilterChain;
import org.artofsolving.jodconverter.filter.RefreshFilter;

public class OfficeDocumentConverterFunctionalTest extends BaseOfficeTest {

  private static final Logger logger =
      LoggerFactory.getLogger(OfficeDocumentConverterFunctionalTest.class);

  private static final String OUTPUT_DIR =
      TEST_OUTPUT_DIR + OfficeDocumentConverterFunctionalTest.class.getSimpleName();

  private void convertFileToAllSupportedFormats(
      final File inputFile, final File outputDir, final DefaultFilterChain chain) throws Exception {

    // Detect input format
    final String inputExtension = FilenameUtils.getExtension(inputFile.getName());

    final DocumentFormat inputFormat = formatRegistry.getFormatByExtension(inputExtension);

    assumeTrue(inputFormat != null);

    assertNotNull("unknown input format: " + inputExtension, inputFormat);

    // Get all supported output formats
    final Set<DocumentFormat> outputFormats =
        formatRegistry.getOutputFormats(inputFormat.getInputFamily());

    // For each supported output format, convert the input file
    for (final DocumentFormat outputFormat : outputFormats) {

      // LibreOffice 4 fails natively on those one
      if (inputFormat.getExtension().equals("odg") && outputFormat.getExtension().equals("svg")) {
        logger.info("-- skipping odg to svg test... ");
        continue;
      }
      if (StringUtils.equalsAny(outputFormat.getExtension(), "sxc", "sxw", "sxi")) {
        logger.info("-- skipping {} to {} test... ", inputExtension, outputFormat.getExtension());
        continue;
      }

      // Generate an output filename
      File outputFile = null;
      if (outputDir == null) {
        outputFile = File.createTempFile("test", "." + outputFormat.getExtension());
        outputFile.deleteOnExit();
      } else {
        outputFile =
            new File(
                outputDir,
                FilenameUtils.getBaseName(inputFile.getName()) + "." + outputFormat.getExtension());

        // Delete existing file
        FileUtils.deleteQuietly(outputFile);
      }

      // Convert the file
      logger.info(
          "-- converting {} to {}... ", inputFormat.getExtension(), outputFormat.getExtension());
      converter.convert(chain, inputFile, outputFile, outputFormat);
      logger.info("done.\n");
      assertTrue(outputFile.isFile() && outputFile.length() > 0);

      //TODO use file detection to make sure outputFile is in the expected format

      // Reset the chain in order to reuse it.
      chain.reset();
    }
  }

  /**
   * Test the conversion of an HTML file that contains an image.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void htmlWithImageConversion() throws Exception {

    final File inputFile = new File(DOCUMENTS_DIR + "index.html");
    final File outputDir = new File(OUTPUT_DIR);

    // Create the filter chain to use
    final DefaultFilterChain chain = new DefaultFilterChain(RefreshFilter.INSTANCE);

    // Convert the file to all supported formats
    convertFileToAllSupportedFormats(inputFile, outputDir, chain);
  }

  /**
   * Test the conversion of all the supported documents format.
   *
   * @throws Exception if an error occurs.
   */
  @Test
  public void runAllPossibleConversions() throws Exception {

    final File dir = new File("src/test/resources/documents");
    final File[] files =
        dir.listFiles(
            new FilenameFilter() {
              public boolean accept(final File dir, final String name) {
                return name.charAt(0) != '.';
              }
            });

    // Here we can reuse a unique FilterChain
    final DefaultFilterChain chain = new DefaultFilterChain(RefreshFilter.INSTANCE);

    for (final File inputFile : files) {

      // Convert the file to all supported formats
      convertFileToAllSupportedFormats(inputFile, null, chain);
    }
  }
}
