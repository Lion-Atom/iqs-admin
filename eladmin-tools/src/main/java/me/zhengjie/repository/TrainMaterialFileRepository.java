package me.zhengjie.repository;

import me.zhengjie.domain.TrainMaterialDepart;
import me.zhengjie.domain.TrainMaterialFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/**
 * @author Tong Minjie
 * @date 2022-05-12
 */
@Repository
public interface TrainMaterialFileRepository extends JpaRepository<TrainMaterialFile, Long>, JpaSpecificationExecutor<TrainMaterialFile> {

    /**
     * @param departId 部门ID
     * @return 培训考试关联部门数据
     */
    @Query(value = "select * from train_material_file where depart_id= ?1 ", nativeQuery = true)
    List<TrainMaterialFile> findByDepartId(Long departId);


    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);
}
