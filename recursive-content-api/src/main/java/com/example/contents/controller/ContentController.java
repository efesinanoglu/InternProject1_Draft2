package com.example.contents.controller;

import com.example.contents.dto.BulkJobStatus;
import com.example.contents.dto.ContentDto;
import com.example.contents.service.BulkJobRegistry;
import com.example.contents.service.ContentBulkService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/contents")
public class ContentController {

    @Autowired
    private com.example.contents.service.ContentService service;

    @Autowired
    private ContentBulkService bulkService;

    @Autowired
    private BulkJobRegistry jobs;

    @Value("${contents.files.base-dir:}")
    private String baseDirProp;

    @Operation(summary = "Start async bulk insert; returns jobId to poll")
    @PostMapping(value = "/bulk/async", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)

    // TODO tamamlandı: Bu servis bir tane dosya alır ve dosya üzerindeki kayıtları tek tek kaydeder.

    public ResponseEntity<BulkJobStatus> bulkInsertAsync(@RequestParam("file") MultipartFile file) throws IOException {
        // 1 Dosyayı CSV (header: file,textBlock) olarak parse et
        List<ContentDto> dtos = parseCsvToDtos(file);

        // 2 İş kaydı oluştur ve async bulk insert başlat
        BulkJobStatus job = jobs.create(dtos.size());
        int defaultChunkSize = 1000;
        boolean dedupePayload = false;
        boolean dedupeDb = false;
        bulkService.bulkInsertAsync(job.getJobId(), dtos, defaultChunkSize, dedupePayload, dedupeDb);

        // 3 Accepted + Location: job status
        URI statusUri = URI.create("/api/contents/bulk/jobs/" + job.getJobId());
        return ResponseEntity.accepted()
                .location(statusUri)
                .body(job);
    }

    @Operation(summary = "Async bulk job return")
    @GetMapping("/bulk/jobs/{jobId}")
    public ResponseEntity<BulkJobStatus> getBulkJob(@PathVariable UUID jobId) {
        BulkJobStatus st = jobs.get(jobId);
        return (st == null) ? ResponseEntity.notFound().build() : ResponseEntity.ok(st);
    }



    private Path getBaseDir() {
        if (baseDirProp != null && !baseDirProp.isBlank()) {
            return Paths.get(baseDirProp).toAbsolutePath().normalize();
        }
        return Paths.get(System.getProperty("user.home"),
                        "Desktop", "Internproject", "images")
                .toAbsolutePath().normalize();
    }


    private List<ContentDto> parseCsvToDtos(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("The file is not found or empty!");
        }

        List<ContentDto> out = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String header = br.readLine();
            if (header == null) {
                throw new IllegalArgumentException("File is empty!");
            }

            header = stripBom(header);

            // Ayraç tespiti
            char sep = header.contains(";") ? ';' : ',';

            String[] headerCols = splitCsvLine(header, sep);
            int fileIdx = indexOfIgnoreCase(headerCols, "file");
            int textIdx = indexOfIgnoreCase(headerCols, "textBlock");

            if (fileIdx < 0 || textIdx < 0) {
                throw new IllegalArgumentException("CSV header must contain 'file' and 'textBlock' columns.");
            }

            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                String[] cols = splitCsvLine(line, sep);

                // Sütun sayısı yetersizse atla
                if (cols.length <= Math.max(fileIdx, textIdx)) continue;

                String fileVal = cols[fileIdx].trim();
                String textVal = cols[textIdx].trim();

                if (fileVal.isEmpty() && textVal.isEmpty()) continue;

                // Base dizine göre çözer ve güvenlik kontrolü yapar
                String resolvedPath = resolveAgainstBaseDir(fileVal);

                ContentDto dto = ContentDto.builder()
                        .File(resolvedPath)
                        .textBlock(textVal)
                        .build();

                out.add(dto);
            }
        }

        if (out.isEmpty()) {
            throw new IllegalArgumentException("No valid record found. Please check the csv content.");
        }

        return out;
    }

    private String resolveAgainstBaseDir(String fileVal) {
        Path base = getBaseDir();

        Path candidate = Paths.get(fileVal);
        // Relative ise base'e göre çöz
        if (!candidate.isAbsolute()) {
            candidate = base.resolve(candidate);
        }
        candidate = candidate.normalize().toAbsolutePath();

        // Güvenlik: base dışına çıkan path'leri reddet
        if (!candidate.startsWith(base)) {
            throw new IllegalArgumentException("Geçersiz dosya yolu: base dizin dışına çıkılamaz -> " + fileVal);
        }
        return candidate.toString();
    }

    private static String stripBom(String s) {
        if (s != null && s.startsWith("\uFEFF")) {
            return s.substring(1);
        }
        return s;
    }

    // CSV ayırma.
    private static String[] splitCsvLine(String line, char sep) {
        return line.split("\\" + sep, -1);
    }

    private static int indexOfIgnoreCase(String[] arr, @NotBlank String target) {
        for (int i = 0; i < arr.length; i++) {
            if (target.equalsIgnoreCase(arr[i].trim())) return i;
        }
        return -1;
    }

    // (Opsiyonel) Eski payload sınıfı; farklı yerlerde referans varsa bozulmasın
    @Data
    public static class UpsertRequest {
        public Long id; // ignored in bulk
        @NotBlank public String File;
        @NotBlank public String textBlock;
    }
}
