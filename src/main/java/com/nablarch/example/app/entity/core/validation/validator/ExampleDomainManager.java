package com.nablarch.example.app.entity.core.validation.validator;

import nablarch.core.util.annotation.Published;
import nablarch.core.validation.ee.DomainManager;

/**
 * {@link DomainManager}の実装クラス。
 * 
 */
@Published(tag = "architect")
public class ExampleDomainManager implements DomainManager<ExampleDomainType> {
    @Override
    public Class<ExampleDomainType> getDomainBean() {
        // ドメインBeanのClassオブジェクトを返す
        return ExampleDomainType.class;
    }
}
