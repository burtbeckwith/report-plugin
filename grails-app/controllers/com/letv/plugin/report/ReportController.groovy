package com.letv.plugin.report

import grails.converters.JSON

class ReportController {

    def reportService

    /**
     * 点击查询时，生成dataTable的表头
     * @return
     */
    def getDataTableHeader() {
        //处理统计纬度的选择值
        def groupByColumns = []
        if(params.groupParams){
            if(params.groupParams instanceof  String){
                groupByColumns <<  params.groupParams
            }else{
                groupByColumns = (params.groupParams as String[]).toList()
            }
        }
        //处理显示列的选择值
        def displayParams = []
        if(params.displayParams){
            if(params.displayParams instanceof  String){
                displayParams <<  params.displayParams
            }else{
                displayParams = (params.displayParams as String[]).toList()
            }
        }

        def data = reportService.getTableHeader(groupByColumns, displayParams, params.reportName)
        render template: "/report/dataTable", model: [headerData : data]
    }

    /**
     * ajax查询数据
     * @return
     */
    def queryData() {
        //处理查询条件选择的值
        def queryMap = [:]
        if(params.queryParams){
            params.queryParams.split(",").each {code ->
                def value =   params."${code}"
                if(value){
                    def ids = value instanceof String ? value : (value as String[]).join(",")
                    queryMap.put(code, ids)
                }
            }
        }
        //处理统计纬度的选择值
        def groupByColumns = []
        if(params.groupParams){
            if(params.groupParams instanceof  String){
                groupByColumns <<  params.groupParams
            }else{
                groupByColumns = (params.groupParams as String[]).toList()
            }
        }
        //处理显示列的选择值
        def displayParams = []
        if(params.displayParams){
            if(params.displayParams instanceof  String){
                displayParams <<  params.displayParams
            }else{
                displayParams = (params.displayParams as String[]).toList()
            }
        }

        def aoData = JSON.parse(params.aoData)//dataTable使用的三个参数名称。
        Integer start = aoData.find {it.name == "iDisplayStart"}.value as Integer
        Integer limit = aoData.find {it.name == "iDisplayLength"}.value as Integer
        Integer sEcho = aoData.find {it.name == "sEcho"}.value as Integer//可能表示是次数，必须传

        def data = reportService.queryData(params.reportName, queryMap, groupByColumns, displayParams, params.startDate, params.endDate, start, limit, sEcho)
        render data as JSON
    }
}
