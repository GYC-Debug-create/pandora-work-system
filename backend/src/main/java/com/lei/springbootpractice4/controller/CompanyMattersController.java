package com.lei.springbootpractice4.controller;

import com.lei.springbootpractice4.pojo.CompanyMattersPojo;
import com.lei.springbootpractice4.service.CompanyMattersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/company-matters") // API路径与前端对应
public class CompanyMattersController {

    @Autowired
    private CompanyMattersService mattersService;

    @GetMapping
    public ResponseEntity<List<CompanyMattersPojo>> getAllMatters() {
        List<CompanyMattersPojo> matters = mattersService.getAllMatters();
        return ResponseEntity.ok(matters);
    }

    @PostMapping
    public ResponseEntity<CompanyMattersPojo> createMatter(@RequestBody CompanyMattersPojo matter) {
        CompanyMattersPojo createdMatter = mattersService.createMatter(matter);
        return ResponseEntity.ok(createdMatter);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyMattersPojo> updateMatter(@PathVariable Integer id, @RequestBody CompanyMattersPojo matter) {
        CompanyMattersPojo updatedMatter = mattersService.updateMatter(id, matter);
        return ResponseEntity.ok(updatedMatter);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteMatter(@PathVariable Integer id) {
        boolean success = mattersService.deleteMatter(id);
        if (success) {
            return ResponseEntity.ok("事项删除成功");
        } else {
            return ResponseEntity.badRequest().body("删除失败，事项可能不存在");
        }
    }
}