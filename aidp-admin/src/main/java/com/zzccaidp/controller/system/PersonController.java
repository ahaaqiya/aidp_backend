package com.zzccaidp.controller.system;

import com.zzccaidp.service.system.PersonService;
import com.zzccaidp.vo.system.PersonPageQueryRequest;
import com.zzccaidp.vo.system.PersonPageQueryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description: 用户
 * @Author: WB211928
 * @Createtime: 2026-04-29 09:24
 * @Version: 1.0
 */
@RestController
@RequestMapping("/person")
public class PersonController {
    @Autowired
    private PersonService personService;

    @PostMapping("/selectPersonInfoAll")
    public PersonPageQueryResponse selectPersonInfoAll(@RequestBody PersonPageQueryRequest personPageQueryRequest) {
        return personService.selectPersonInfoAll(personPageQueryRequest);
    }

    @PostMapping("/selectOrgInfoAll")
    public PersonPageQueryResponse selectOrgInfoAll(@RequestBody PersonPageQueryRequest personPageQueryRequest) {
        return personService.selectOrgInfoAll(personPageQueryRequest);
    }

}
