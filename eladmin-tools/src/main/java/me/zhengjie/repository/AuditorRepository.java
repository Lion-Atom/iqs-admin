package me.zhengjie.repository;

import me.zhengjie.domain.Auditor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Set;

/**
 * @author tmj
 * @version 1.0
 * @date 2021/9/7 10:28
 */
public interface AuditorRepository extends JpaRepository<Auditor, Long>, JpaSpecificationExecutor<Auditor> {

    /**
     * 根据员工ID查询审核人员信息
     *
     * @param userId 员工ID
     * @param system 审核体系
     * @return 审核人员信息
     */
    @Query(value = "SELECT * FROM tools_auditor where user_id = ?1 and system = ?2 ", nativeQuery = true)
    List<Auditor> findByUserIdAndSystem(Long userId, String system);

    /**
     * 根据员工ID查询审核人员信息
     *
     * @param userId 员工ID
     * @return 审核人员信息
     */
    @Query(value = "SELECT * FROM tools_auditor where user_id = ?1", nativeQuery = true)
    List<Auditor> findByUserId(Long userId);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * 根据状态查询对应的审核人员数量
     *
     * @param status 审核人员有效期状态
     * @return 审核人员数量
     */
    @Query(value = "SELECT count(auditor_id) FROM tools_auditor where status = ?1 ", nativeQuery = true)
    int getCountByStatus(String status);

    @Query(value = "SELECT count(auditor_id) FROM tools_auditor ", nativeQuery = true)
    int getAuditorCount();

    @Query(value = "SELECT count(auditor_id) FROM tools_auditor where system = ?1 ", nativeQuery = true)
    int findBySystem(String system);

    @Query(value = "SELECT * FROM tools_auditor where auditor_id in ?1 ", nativeQuery = true)
    List<Auditor> findByIdIn(Set<Long> auditorIds);
}
