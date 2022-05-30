package me.zhengjie.repository;

import me.zhengjie.domain.InstrumentFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;


/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/27 14:00
 */
@Repository
public interface InstruFileRepository extends JpaRepository<InstrumentFile, Long>, JpaSpecificationExecutor<InstrumentFile> {


    /**
     * @param instruId 仪器id
     * @return 仪器报告（废弃时候使用）
     */
    @Query(value = "select * from instrument_file where instru_id = ?1", nativeQuery = true)
    List<InstrumentFile> findByInstruId(Long instruId);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);
}
