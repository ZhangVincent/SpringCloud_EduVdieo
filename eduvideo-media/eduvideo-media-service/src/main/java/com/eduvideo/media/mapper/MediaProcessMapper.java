package com.eduvideo.media.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.eduvideo.media.model.po.MediaProcess;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author itcast
 */
public interface MediaProcessMapper extends BaseMapper<MediaProcess> {

    /***
     * @description 保证查询到的记录不重复
     * @param shardTotal
     * @param shardindex
     * @param count
     * @return java.util.List<com.eduvideo.media.model.po.MediaProcess>
     * @author zkp15
     * @date 2023/6/19 20:51
     */
    @Select("SELECT t.* FROM media_process t WHERE t.id % #{shardTotal} = #{shardindex} and t.status='1' limit #{count}")
    List<MediaProcess> selectListByShardIndex(@Param("shardindex") int shardindex, @Param("shardTotal") int shardTotal, @Param("count") int count);

}
