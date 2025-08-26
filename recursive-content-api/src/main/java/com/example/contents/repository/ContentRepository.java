package com.example.contents.repository;

import com.example.contents.model.Content;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ContentRepository extends JpaRepository<Content, Long> {
    @EntityGraph(attributePaths = {"children"})
    List<Content> findByParentIsNull();
    interface DupGroup {
        String getFile();
        String getTextBlock();
        long getCnt();
    }

//    @Query("""
//           select c.image as file, c.textBlock as textBlock, count(c) as cnt
//           from Content c
//           group by c.image, c.textBlock
//           having count(c) > 1
//           """)


//    @Query("""
//           select lower(trim(c.image)) as file,
//                  lower(trim(c.textBlock)) as textBlock,
//                  count(c) as cnt
//           from Content c
//           group by lower(trim(c.image)), lower(trim(c.textBlock))
//           having count(c) > 1
//           """)
    List<Content> findByFileAndTextBlock(String file, String textBlock);

    @Query(value = """
        SELECT
          btrim(file)      AS file,
          btrim(textblock) AS textblock,
          COUNT(*)         AS cnt
        FROM contents
        GROUP BY btrim(file), btrim(textblock)
        HAVING COUNT(*) > 1
        """,
            nativeQuery = true)
    List<DupGroup> findDuplicateGroups();
    interface DuplicateGroup {
        String getFile();
        String gettextBlock();
        long getCnt();
    }
}