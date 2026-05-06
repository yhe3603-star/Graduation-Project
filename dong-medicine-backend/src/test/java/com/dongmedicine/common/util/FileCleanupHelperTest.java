package com.dongmedicine.common.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.dongmedicine.service.FileUploadService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileCleanupHelperTest {

    @Mock
    private FileUploadService fileUploadService;

    private FileCleanupHelper fileCleanupHelper;

    @BeforeEach
    void setUp() {
        fileCleanupHelper = new FileCleanupHelper();
        java.lang.reflect.Field field;
        try {
            field = FileCleanupHelper.class.getDeclaredField("fileUploadService");
            field.setAccessible(true);
            field.set(fileCleanupHelper, fileUploadService);
        } catch (Exception e) {
            fail("Failed to inject mock");
        }
    }

    @Test
    @DisplayName("deleteFilesFromJson - 空字符串不执行删除")
    void deleteFilesFromJson_emptyString_noDeletion() {
        fileCleanupHelper.deleteFilesFromJson("");
        fileCleanupHelper.deleteFilesFromJson(null);
        fileCleanupHelper.deleteFilesFromJson("   ");
        
        verifyNoInteractions(fileUploadService);
    }

    @Test
    @DisplayName("deleteFilesFromJson - 有效JSON数组执行删除")
    void deleteFilesFromJson_validJsonArray_deletesFiles() throws Exception {
        when(fileUploadService.deleteFile(anyString())).thenReturn(true);
        
        fileCleanupHelper.deleteFilesFromJson("[\"file1.pdf\", \"file2.docx\"]");
        
        verify(fileUploadService, times(2)).deleteFile(anyString());
    }

    @Test
    @DisplayName("deleteSingleFile - 空路径不执行删除")
    void deleteSingleFile_emptyPath_noDeletion() {
        fileCleanupHelper.deleteSingleFile("");
        fileCleanupHelper.deleteSingleFile(null);
        
        verifyNoInteractions(fileUploadService);
    }

    @Test
    @DisplayName("deleteSingleFile - 正常路径执行删除")
    void deleteSingleFile_validPath_deletesFile() throws Exception {
        when(fileUploadService.deleteFile("uploads/test.pdf")).thenReturn(true);
        
        fileCleanupHelper.deleteSingleFile("/uploads/test.pdf");
        
        verify(fileUploadService).deleteFile("uploads/test.pdf");
    }

    @Test
    @DisplayName("deleteSingleFile - 删除失败不抛异常")
    void deleteSingleFile_deleteFails_noException() throws Exception {
        when(fileUploadService.deleteFile(anyString())).thenThrow(new RuntimeException("Delete failed"));
        
        assertDoesNotThrow(() -> fileCleanupHelper.deleteSingleFile("test.pdf"));
    }
}
