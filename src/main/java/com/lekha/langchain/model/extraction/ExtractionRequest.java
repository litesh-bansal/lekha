package com.lekha.langchain.model.extraction;

import com.lekha.langchain.enums.Model;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
public class ExtractionRequest {

    @NotBlank
    private String referenceId;

    @NotNull
    private MultipartFile documentContent;

    private String mimeType;

    @NotEmpty
    private List<Model> models = List.of(Model.GEMMA);

    /**
     * The browser-reported content type of the upload is more trustworthy than a
     * caller-supplied field; fall back to {@code mimeType} only when it is absent.
     */
    public String resolveMimeType() {
        if (documentContent != null
                && documentContent.getContentType() != null
                && !documentContent.getContentType().isBlank()) {
            return documentContent.getContentType();
        }
        return mimeType;
    }
}
