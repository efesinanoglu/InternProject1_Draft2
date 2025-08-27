package com.example.contents.repository;

import com.example.contents.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {

    // exact match helper
    List<Content> findByFileAndTextBlock(String file, String TextBlock);

    // projection for the native duplicate query
    @Query(value = """
    SELECT
      lower(btrim(coalesce(file::text, '')))      AS file,
      lower(btrim(coalesce(text_block::text, ''))) AS textblock,
      COUNT(*)                                    AS cnt
    FROM contents
    GROUP BY lower(btrim(coalesce(file::text, ''))),
             lower(btrim(coalesce(text_block::text, '')))
    HAVING COUNT(*) > 1
    """,
            nativeQuery = true)
    List<DupGroup> findDuplicateGroups();

    public interface DupGroup {
        String getFile();       // alias: file
        String getTextBlock();  // alias: textblock
        long getCnt();          // alias: cnt
    }


    // batch for normal processing
    List<Content> findTop100ByProcessedFalseOrderByIdAsc();
}
