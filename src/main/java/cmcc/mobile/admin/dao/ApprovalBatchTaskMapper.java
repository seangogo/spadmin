package cmcc.mobile.admin.dao;

import java.util.List;

import cmcc.mobile.admin.entity.ApprovalBatchTask;

public interface ApprovalBatchTaskMapper {
    int deleteByPrimaryKey(Long id);

    int insert(ApprovalBatchTask record);

    int insertSelective(ApprovalBatchTask record);

    ApprovalBatchTask selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ApprovalBatchTask record);

    int updateByPrimaryKey(ApprovalBatchTask record);
    
    /**
     * 条件查询
     * @param record
     * @return
     */
    List<ApprovalBatchTask> findByCondition(ApprovalBatchTask record);
    
    /**
     * 名字唯一性校验
     * @param record
     * @return
     */
    ApprovalBatchTask checkName(ApprovalBatchTask record);
}