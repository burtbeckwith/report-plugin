package com.letv.plugin.report

import groovy.sql.Sql

import java.util.concurrent.ConcurrentSkipListMap
import java.util.concurrent.TimeUnit

import javax.annotation.PostConstruct

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache

class DictCacheService {

    static transactional = false

    def grailsApplication
    // 最终数据map结构
    private LoadingCache<String, Map> data

    @PostConstruct
    def init() {
        CacheLoader<String, Map> loader =
                new CacheLoader<String, Map>() {
                    Map load(String key) {
                        return loadTable(key)
                    }
                }
        data = CacheBuilder.newBuilder().refreshAfterWrite(30, TimeUnit.MINUTES)
                .build(loader)
    }

    /**
     *
     * @param name 报表名称:项目名称
     * @return
     */
    Map loadTable(String key) {

        def (String reportName, String itemName) = key.split(":")

        def config = grailsApplication.config.report.base
        def item

        //配置项中包含报表名时，根据报表名获取配置
        if (config.get(itemName).containsKey(reportName)) {
            item = config.get(itemName).get(reportName)
        } else {
            item = config.get(itemName)
        }

        //带有tableName属性时
        if (item.tableName == null) {//没有table属性时，无需查询数据
            return item
        }
        def datasource = grailsApplication.mainContext.getBean(config.dataSource)
        def sqlExec = new Sql(datasource)

        def sql = "select id,name from " + item.tableName

        def dataMap = new ConcurrentSkipListMap()
        sqlExec.eachRow(sql) { tempResult ->
            dataMap.put(tempResult.id, tempResult.name)
        }

        return [name: item.name, data: dataMap, tableName: item.tableName]
    }

    Map getData(String name, String reportName) {
        data.getUnchecked(reportName + ":" + name)
    }
}
