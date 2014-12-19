package com.letv.plugin.report

import groovy.sql.Sql

import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat

class ReportService {

    static transactional = false

    def dictCacheService
    def grailsApplication

    /**
     * 初始化页面时需要的参数
     * @param reportName
     * @return
     */
    def initParams(String reportName) {
        def config = grailsApplication.config.report.get(reportName)

        def groupParams = config.groupParams.collect { code ->
            return [code: code, name: dictCacheService.getData(code, reportName).name]
        }
        def queryParams = config.queryParams.collect { code ->
            return [
                code: code,
                name: dictCacheService.getData(code, reportName).name,
                data: dictCacheService.getData(code, reportName).data]
        }
        def displayParams = config.displayParams.collect { code ->
            return [code: code, name: dictCacheService.getData(code, reportName).name]
        }
        return [reportName : reportName, groupParams : groupParams, queryParams : queryParams, displayParams : displayParams]
    }

    /**
     * 获取dataTable的表头列
     * @param groupByCol
     * @param displayParams
     * @param reportName
     * @return
     */
    def getTableHeader(List groupByCol, List displayParams, String reportName) {
        def selectPart = ["日期"]
        def all = groupByCol + displayParams//注意顺序--

        all.each { code ->
            Map item = dictCacheService.getData(code, reportName)
            if (item.get("tableName")) {//会有两个列
                selectPart << (item.name + " ID")
                selectPart << (item.name + " 名称")
            } else {
                selectPart << item.name
            }
        }

        return selectPart
    }

    private handerSelect( List displayParams, String reportName){
        def selectPart = ["t.data_time"] //数据时间 字段名写死
        //处理显示列
        displayParams.each {code->
            Map item = dictCacheService.getData(code, reportName)
            if(item.get("eval")){
                selectPart << "${item.eval} as $code"
            }else{
                selectPart << "sum(t.${code}) as $code"
            }
        }
        return selectPart
    }

    private handerWhere(Map queryMap, String startDate, String endDate){
        //处理各个查询条件
        def wherePart = ["t.data_time between '$startDate' and '$endDate'"]
        if(queryMap){
            queryMap.each {key, value ->
                wherePart << "t.$key in ($value)"
            }
        }
        return wherePart
    }

    private handerGroup(List grougByCol, List selectPart){
        def groupPart = []
        //处理分组 --group的字段要放在select的字段前，数据格式才可读
        def seleTmp = []
        groupPart << "t.data_time"//此处写死 TODO   data_time
        if(grougByCol){
            grougByCol.each {code->
                seleTmp << code
                groupPart << code
            }
        }
        selectPart.addAll(1, seleTmp)//第一位是日期
        return groupPart
    }

    /**
     * 获取数据的sql
     * @param reportName
     * @param queryMap
     * @param groupByCol
     * @param displayParams
     * @param startDate
     * @param endDate
     * @param start
     * @param limit
     * @return
     */
    private String getDataSQL(String reportName, Map queryMap, List groupByCol, List displayParams,
                              String startDate, String endDate, Integer start, Integer limit){
        def selectPart = handerSelect(displayParams, reportName)
        def wherePart = handerWhere(queryMap, startDate, endDate)
        def groupPart = handerGroup(groupByCol, selectPart);

        String selectStr = selectPart.join(", ")
        String whereStr = wherePart.join(" and ")
        String groupStr = groupPart.join(", ")

        def config = grailsApplication.config.report.get(reportName)
        String tableName = config.get("tableName")
        String sqlStr = """
                select $selectStr
                from $tableName as t
                where $whereStr
                group by $groupStr
                limit $start, $limit
                """
        return sqlStr
    }

    /**
     * 获取数据总数的sql
     * @param reportName
     * @param queryMap
     * @param groupByCol
     * @param displayParams
     * @param startDate
     * @param endDate
     * @return
     */
    private String getDataCountSQL(String reportName, Map queryMap, List groupByCol, List displayParams, String startDate, String endDate){
        def selectPart = handerSelect(displayParams, reportName)
        def wherePart = handerWhere(queryMap, startDate, endDate)
        def groupPart = handerGroup(groupByCol, selectPart);

        String selectStr = selectPart.join(", ")
        String whereStr = wherePart.join(" and ")
        String groupStr = groupPart.join(", ")

        def config = grailsApplication.config.report.get(reportName)
        String tableName = config.get("tableName")
        String sqlStr = """
                select count(1) as cnt
                from ( select $selectStr
                from $tableName as t
                where $whereStr
                group by $groupStr ) as s
                """
        return sqlStr
    }

    /**
     * 查询数据，返回json格式数据
     * @param reportName
     * @param queryMap
     * @param groupByCol
     * @param displayParams
     * @param startDate
     * @param endDate
     * @param start
     * @param limit
     * @param sEcho
     * @return
     */
    public queryData(String reportName, Map queryMap, List groupByCol, List displayParams,
                         String startDate, String endDate, Integer start, Integer limit, Integer sEcho) {

        String sqlStr = getDataSQL(reportName, queryMap, groupByCol, displayParams, startDate, endDate, start, limit)

        def config = grailsApplication.config.report.get(reportName)
        def dataSource = grailsApplication.mainContext.getBean(config.dataSource)
        def sql = new Sql(dataSource)

        def data = [];
        sql.rows(sqlStr.replaceAll("\n", "")).each { row -> //不能使用eachrow--
            Map data_row = [:];
            row.each { colName, value ->
                if (colName == "data_time") {
                    String finalValue = new SimpleDateFormat("yyyy-MM-dd").format(value)
                    data_row.put(colName, finalValue)
                } else {
                    Map item = dictCacheService.getData(colName, reportName)
                    String finalName = item.name;
                    String finalValue = value;

                    if (item.get("tableName")) {//将id转化为name -1.显示名字+ID 2 多放一个列 放ID
                        finalName += " 名称"
                        finalValue = item.data.get(value);
                        data_row.put(item.name + " ID", value)
                    }
                    if (item.get("format")) {//将数据格式化
                        NumberFormat df = new DecimalFormat(item.format);
                        finalValue = df.format(value)
                    }
                    data_row.put(finalName, finalValue)
                }
            }
//            println data_row
            data << data_row.values();
        }
        //获取总数
        String countSql = getDataCountSQL(reportName, queryMap, groupByCol, displayParams, startDate, endDate)
        Integer count = 0;
        sql.rows(countSql.replaceAll("\n", "")).each { count = it.cnt }

        return [aaData: data, iTotalDisplayRecords: count, iTotalRecords: count, sEcho: sEcho]
    }
}
