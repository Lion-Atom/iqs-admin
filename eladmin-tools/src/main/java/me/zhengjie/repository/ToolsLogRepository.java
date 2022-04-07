package me.zhengjie.repository;

import me.zhengjie.domain.ToolsLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/5/31 15:56
 */
@Repository
public interface ToolsLogRepository extends JpaRepository<ToolsLog,Long>, JpaSpecificationExecutor<ToolsLog> {

    /**
     * 根据日志类型删除信息
     *
     * @param bindingId 绑定标识
     */
    @Modifying
    @Query(value = "delete from tools_log where binding_id = ?1", nativeQuery = true)
    void deleteAllByBindingId(Long bindingId);
}
