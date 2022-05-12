package me.zhengjie.repository;

import me.zhengjie.domain.TrainMaterialDepart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-05-12
 */
@Repository
public interface TrainMaterialDepartRepository extends JpaRepository<TrainMaterialDepart, Long>, JpaSpecificationExecutor<TrainMaterialDepart> {

    /**
     * @param departId 部门ID
     * @return 培训考试关联部门数据
     */
    @Query(value = "select * from train_material_depart where depart_id= ?1 ", nativeQuery = true)
    TrainMaterialDepart findByDepartId(Long departId);


    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);
}
