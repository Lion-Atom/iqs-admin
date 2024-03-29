package me.zhengjie.repository;

import me.zhengjie.domain.TrainExamDepart;
import me.zhengjie.domain.TrainNewStaff;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-05-09
 */
@Repository
public interface TrainExamDepartRepository extends JpaRepository<TrainExamDepart, Long>, JpaSpecificationExecutor<TrainExamDepart> {

    /**
     * @param departId 部门ID
     * @return 培训考试关联部门数据
     */
    @Query(value = "select * from train_exam_depart where depart_id= ?1 ", nativeQuery = true)
    TrainExamDepart findByDepartId(Long departId);


    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);
}
