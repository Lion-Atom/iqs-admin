package me.zhengjie.repository;

import me.zhengjie.domain.Instrument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * @author TongMin Jie
 * @version V1
 * @date 2022/5/27 14:00
 */
@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long>, JpaSpecificationExecutor<Instrument> {

    /**
     * @param innerId 内部ID
     * @return 仪器信息
     */
    @Query(value = "SELECT * FROM tools_instrument where inner_id = ?1", nativeQuery = true)
    Instrument findByInnerID(String innerId);

    /**
     * @param assetNum 资产号
     * @return 仪器信息
     */
    @Query(value = "SELECT * FROM tools_instrument where asset_num = ?1", nativeQuery = true)
    Instrument findByAssetNum(String assetNum);

    /**
     * 根据Id删除
     *
     * @param ids /
     */
    void deleteAllByIdIn(Set<Long> ids);

    /**
     * @param instruIds 仪器Ids
     */
    @Modifying
    @Query(value = "delete FROM tools_instrument where instru_id in ?1", nativeQuery = true)
    void deleteAllByInstruIdIn(Set<Long> instruIds);
}
