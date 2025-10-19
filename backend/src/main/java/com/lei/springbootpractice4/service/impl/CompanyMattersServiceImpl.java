package com.lei.springbootpractice4.service.impl;

import com.lei.springbootpractice4.mapper.CompanyMattersMapper;
import com.lei.springbootpractice4.pojo.CompanyMattersPojo;
import com.lei.springbootpractice4.service.CompanyMattersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompanyMattersServiceImpl implements CompanyMattersService {

    @Autowired
    private CompanyMattersMapper mattersMapper;

    @Override
    public List<CompanyMattersPojo> getAllMatters() {
        // TODO: 可以根据前端需求添加排序逻辑，例如按 importance_level 或 created_at 排序
        return mattersMapper.selectList(null);
    }

    @Override
    public CompanyMattersPojo createMatter(CompanyMattersPojo matter) {
        // TODO: 设置创建者ID (creatorId)，可以从当前登录用户获取
        // matter.setCreatorId(currentUserId);
        mattersMapper.insert(matter);
        return matter; // 返回包含新生成ID的对象
    }

    @Override
    public CompanyMattersPojo updateMatter(Integer id, CompanyMattersPojo matter) {
        matter.setMatterId(id); // 确保ID设置正确
        mattersMapper.updateById(matter);
        return matter;
    }

    @Override
    public boolean deleteMatter(Integer id) {
        int deletedRows = mattersMapper.deleteById(id);
        return deletedRows > 0;
    }
}