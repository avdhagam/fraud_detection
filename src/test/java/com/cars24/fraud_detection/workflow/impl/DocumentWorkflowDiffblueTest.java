package com.cars24.fraud_detection.workflow.impl;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.cars24.fraud_detection.data.request.AudioRequest;
import com.cars24.fraud_detection.data.request.DocumentRequest;
import com.cars24.fraud_detection.exception.DocumentProcessingException;
import com.cars24.fraud_detection.utils.PythonExecutor;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {DocumentWorkflow.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class DocumentWorkflowDiffblueTest {
  @Autowired
  private DocumentWorkflow documentWorkflow;

  @MockBean
  private PythonExecutor pythonExecutor;

  /**
   * Test {@link DocumentWorkflow#processDocument(DocumentRequest)}.
   * <ul>
   *   <li>Given {@link DocumentProcessingException#DocumentProcessingException(String)} with message is {@code An error occurred}.</li>
   * </ul>
   * <p>
   * Method under test: {@link DocumentWorkflow#processDocument(DocumentRequest)}
   */
  @Test
  @DisplayName("Test processDocument(DocumentRequest); given DocumentProcessingException(String) with message is 'An error occurred'")
  @Tag("MaintainedByDiffblue")
  void testProcessDocument_givenDocumentProcessingExceptionWithMessageIsAnErrorOccurred()
      throws UnsupportedEncodingException {
    // Arrange
    DocumentRequest request = mock(DocumentRequest.class);
    when(request.getFileName()).thenThrow(new DocumentProcessingException("An error occurred"));
    when(request.getDocumentData()).thenReturn("AXAXAXAX".getBytes("UTF-8"));
    doNothing().when(request).setDocumentData(Mockito.<byte[]>any());
    doNothing().when(request).setFileName(Mockito.<String>any());
    doNothing().when(request).setUserId(Mockito.<String>any());
    request.setDocumentData("AXAXAXAX".getBytes("UTF-8"));
    request.setFileName("foo.txt");
    request.setUserId("42");

    // Act and Assert
    assertThrows(DocumentProcessingException.class, () -> documentWorkflow.processDocument(request));
    verify(request, atLeast(1)).getDocumentData();
    verify(request).getFileName();
    verify(request).setDocumentData(isA(byte[].class));
    verify(request).setFileName(eq("foo.txt"));
    verify(request).setUserId(eq("42"));
  }

  /**
   * Test {@link DocumentWorkflow#processDocument(DocumentRequest)}.
   * <ul>
   *   <li>Given empty array of {@code byte}.</li>
   * </ul>
   * <p>
   * Method under test: {@link DocumentWorkflow#processDocument(DocumentRequest)}
   */
  @Test
  @DisplayName("Test processDocument(DocumentRequest); given empty array of byte")
  @Tag("MaintainedByDiffblue")
  void testProcessDocument_givenEmptyArrayOfByte() throws UnsupportedEncodingException {
    // Arrange
    DocumentRequest request = mock(DocumentRequest.class);
    when(request.getDocumentData()).thenReturn(new byte[]{});
    doNothing().when(request).setDocumentData(Mockito.<byte[]>any());
    doNothing().when(request).setFileName(Mockito.<String>any());
    doNothing().when(request).setUserId(Mockito.<String>any());
    request.setDocumentData("AXAXAXAX".getBytes("UTF-8"));
    request.setFileName("foo.txt");
    request.setUserId("42");

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> documentWorkflow.processDocument(request));
    verify(request, atLeast(1)).getDocumentData();
    verify(request).setDocumentData(isA(byte[].class));
    verify(request).setFileName(eq("foo.txt"));
    verify(request).setUserId(eq("42"));
  }

  /**
   * Test {@link DocumentWorkflow#processDocument(DocumentRequest)}.
   * <ul>
   *   <li>Given empty string.</li>
   * </ul>
   * <p>
   * Method under test: {@link DocumentWorkflow#processDocument(DocumentRequest)}
   */
  @Test
  @DisplayName("Test processDocument(DocumentRequest); given empty string")
  @Tag("MaintainedByDiffblue")
  void testProcessDocument_givenEmptyString() throws UnsupportedEncodingException {
    // Arrange
    DocumentRequest request = mock(DocumentRequest.class);
    when(request.getFileName()).thenReturn("");
    when(request.getDocumentData()).thenReturn("AXAXAXAX".getBytes("UTF-8"));
    doNothing().when(request).setDocumentData(Mockito.<byte[]>any());
    doNothing().when(request).setFileName(Mockito.<String>any());
    doNothing().when(request).setUserId(Mockito.<String>any());
    request.setDocumentData("AXAXAXAX".getBytes("UTF-8"));
    request.setFileName("foo.txt");
    request.setUserId("42");

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> documentWorkflow.processDocument(request));
    verify(request, atLeast(1)).getDocumentData();
    verify(request).getFileName();
    verify(request).setDocumentData(isA(byte[].class));
    verify(request).setFileName(eq("foo.txt"));
    verify(request).setUserId(eq("42"));
  }

  /**
   * Test {@link DocumentWorkflow#processDocument(DocumentRequest)}.
   * <ul>
   *   <li>Given {@code null}.</li>
   *   <li>When {@link DocumentRequest} {@link DocumentRequest#getDocumentData()} return {@code null}.</li>
   * </ul>
   * <p>
   * Method under test: {@link DocumentWorkflow#processDocument(DocumentRequest)}
   */
  @Test
  @DisplayName("Test processDocument(DocumentRequest); given 'null'; when DocumentRequest getDocumentData() return 'null'")
  @Tag("MaintainedByDiffblue")
  void testProcessDocument_givenNull_whenDocumentRequestGetDocumentDataReturnNull()
      throws UnsupportedEncodingException {
    // Arrange
    DocumentRequest request = mock(DocumentRequest.class);
    when(request.getDocumentData()).thenReturn(null);
    doNothing().when(request).setDocumentData(Mockito.<byte[]>any());
    doNothing().when(request).setFileName(Mockito.<String>any());
    doNothing().when(request).setUserId(Mockito.<String>any());
    request.setDocumentData("AXAXAXAX".getBytes("UTF-8"));
    request.setFileName("foo.txt");
    request.setUserId("42");

    // Act and Assert
    assertThrows(IllegalArgumentException.class, () -> documentWorkflow.processDocument(request));
    verify(request).getDocumentData();
    verify(request).setDocumentData(isA(byte[].class));
    verify(request).setFileName(eq("foo.txt"));
    verify(request).setUserId(eq("42"));
  }

  /**
   * Test {@link DocumentWorkflow#processDocument(DocumentRequest)}.
   * <ul>
   *   <li>When {@link DocumentRequest} (default constructor) DocumentData is {@code AXAXAXAX} Bytes is {@code UTF-8}.</li>
   * </ul>
   * <p>
   * Method under test: {@link DocumentWorkflow#processDocument(DocumentRequest)}
   */
  @Test
  @DisplayName("Test processDocument(DocumentRequest); when DocumentRequest (default constructor) DocumentData is 'AXAXAXAX' Bytes is 'UTF-8'")
  @Tag("MaintainedByDiffblue")
  void testProcessDocument_whenDocumentRequestDocumentDataIsAxaxaxaxBytesIsUtf8() throws UnsupportedEncodingException {
    // Arrange
    DocumentRequest request = new DocumentRequest();
    request.setDocumentData("AXAXAXAX".getBytes("UTF-8"));
    request.setFileName("foo.txt");
    request.setUserId("42");

    // Act and Assert
    assertThrows(DocumentProcessingException.class, () -> documentWorkflow.processDocument(request));
  }

  /**
   * Test {@link DocumentWorkflow#processAudio(AudioRequest)}.
   * <p>
   * Method under test: {@link DocumentWorkflow#processAudio(AudioRequest)}
   */
  @Test
  @DisplayName("Test processAudio(AudioRequest)")
  @Tag("MaintainedByDiffblue")
  void testProcessAudio() throws IOException {
    // Arrange
    AudioRequest request = new AudioRequest();
    request.setAudioFile(new MockMultipartFile("Name", new ByteArrayInputStream("AXAXAXAX".getBytes("UTF-8"))));
    request.setFilepath("/directory/foo.txt");
    request.setUuid("01234567-89AB-CDEF-FEDC-BA9876543210");

    // Act and Assert
    assertNull(documentWorkflow.processAudio(request));
  }
}
