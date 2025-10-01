package com.example.contents.repository;

import com.example.contents.model.Content;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IContentRepository extends JpaRepository<Content, Long> {

    List<Content> findByFileAndTextBlock(String file, String TextBlock);

    //boolean existsByFileIgnoreCaseAndTextBlockIgnoreCase(String file, String textBlock);

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
    boolean existsCaseInsensitive(@Param("file") String file, @Param("textBlock") String textBlock);
    List<DupGroup> findDuplicateGroups();

    public interface DupGroup {
        String getFile();
        String getTextBlock();
        long getCnt();
    }


    List<Content> findTop100ByProcessedFalseOrderByIdAsc();
}
