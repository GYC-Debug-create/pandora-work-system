package com.lei.springbootpractice4.service;

import com.lei.springbootpractice4.pojo.CompanyMattersPojo;
import java.util.List;

public interface CompanyMattersService {
    List<CompanyMattersPojo> getAllMatters();
    CompanyMattersPojo createMatter(CompanyMattersPojo matter);
    CompanyMattersPojo updateMatter(Integer id, CompanyMattersPojo matter);
    boolean deleteMatter(Integer id);
}