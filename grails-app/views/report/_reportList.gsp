<script src="${resource(dir:'js', file:'bootstrap-multiselect.js')}"></script>
<script src="${resource(dir:'js', file:'jquery.dataTables.js')}"></script>
<script src="${resource(dir:'js', file:'prettify.js')}"></script>
<script src="${resource(dir:'js', file:'TableTools.js')}"></script>
<script src="${resource(dir:'js', file:'ZeroClipboard.js')}"></script>

<link rel="stylesheet" href="${resource(dir: 'css', file: 'bootstrap-multiselect.css')}" type="text/css">
<link rel="stylesheet" href="${resource(dir: 'css', file: 'demo_page.css')}" type="text/css">
<link rel="stylesheet" href="${resource(dir: 'css', file: 'demo_table.css')}" type="text/css">
<link rel="stylesheet" href="${resource(dir: 'css', file: 'prettify.css')}" type="text/css">
<link rel="stylesheet" href="${resource(dir: 'css', file: 'TableTools.css')}" type="text/css">
<link rel="stylesheet" href="${resource(dir: 'css', file: 'TableTools_JUI.css')}" type="text/css">

<style type="text/css">
    .bq-table th {
        text-overflow: clip;
        /*width: 150px;*/
        white-space: nowrap;
    }
    .bq-table td {
        text-overflow: ellipsis;
        /*width: 200px;*/
        white-space: normal;
    }
    /**导出按钮的自定义css*/
    .export {
        float:  right;
        margin-left: 30px;
    }
</style>
<g:javascript>
    $(document).ready(function() {
        //统计纬度的多选框
        $('#groupParams').multiselect({
            includeSelectAllOption: true,
            filterPlaceholder: '搜索',
//                enableFiltering: true,
            enableCaseInsensitiveFiltering: true,     //忽略大小写
            filterBehavior: 'text'                     //按照展示值来过滤
        });
        //查询条件的多选框
        <g:each in="${queryParams}" var="data">
            $(${data.get("code")}).multiselect({
//                  includeSelectAllOption: true,
                filterPlaceholder: '搜索',
//                  enableFiltering: true,
                enableCaseInsensitiveFiltering: true,  //忽略大小写
                filterBehavior: 'text'              //按照展示值来过滤
            });
        </g:each>
    });

    function postAndShow(){
        var vals = $("#searchForm").serialize();
        $("#queryBtn").attr({"disabled":"disabled"});

        var oLanguage = {
            "sLengthMenu": "每页显示 _MENU_ 条记录",
            "sZeroRecords": "对不起，查询不到任何相关数据",
            "sInfo": "当前显示 _START_ 到 _END_ 条，共 _TOTAL_ 条记录",
            "sInfoEmpty": "显示第 0 至 0 项结果，共 0 项",
            "sInfoFiltered": "",
            "sProcessing": "正在加载中...",
            "sSearch": "搜索",
            "oPaginate": {
                "sFirst":    "第一页",
                "sPrevious": "上一页 ",
                "sNext":     "下一页 ",
                "sLast":     "最后一页 "
            }
        };
        $.post('<g:createLink controller="report" action="getDataTableHeader" />', vals,
            function(data){//每次点击查询都需要重新生成dataTable。因为表头不一致
                $('#domainTable').dataTable().fnDestroy();
                $("#domainTable").html("");
                $("#domainTable").html(data);
                $('#domainTable').dataTable({
//                        "bScrollInfinite": true,//是否开启无限滚动条，为true的话，分页默认关闭
//                        "sScrollY": 600,
                    "sScrollX": "100%",
                    "iDisplayLength": 2,
//                        "sScrollXInner": "100%",
//                        "oSearch": "100%",
                    "bScrollCollapse": false,
                    "bJQueryUI": false,
                    "bAutoWidth": false,
                    "bProcessing":false,
                    "bPaginate":true,
                    "bSort": false,
                    "bServerSide": true,
                    "oLanguage": oLanguage,
                    "sAjaxSource": '<g:createLink controller="report" action="queryData"/>',
                    "fnServerData": function ( sSource, aoData, fnCallback ) {
                    //alert(aoData);
                        $("#aoData").val(JSON.stringify(aoData));//aoData中包含start limit sEcho参数
                        $.ajax( {
                        "dataType": 'json',
                        "type": "POST",
                        "url": sSource,
                        "data": $("#searchForm").serialize(),
                        "success": fnCallback
                        } );
                    },
                    "sPaginationType": "full_numbers",       //“twobutton”或“fullnumbers”
                    "sDom": 'l<"export"T>frtip',
//                        "sDom": 'T<"clear">lfrtip',
                    "oTableTools":{
                        "sSwfPath": "/${grailsApplication.config.grails.project.groupId}/assets/copy_csv_xls_pdf.swf",
                        "aButtons": [
                            {
                                "sExtends": "xls",
                                "sButtonText": "导出当前页数据"
                            }
                        ]
                    }
                });
                $("#queryBtn").removeAttr("disabled");
            }
         );
    }
</g:javascript>

<div class="sub-nav sub-nav-right">
    <g:form class="form-inline" name="searchForm" controller="report" action="queryData" method="post">
        <input type="hidden" id="aoData" name="aoData" value=""/>
        <div class="input-inline" style="margin-left:20px;">

            <label class="control-label" >统计维度:</label>

            <select id="groupParams" name="groupParams" class="span3" class="multiselect" multiple="multiple">
                <g:each in="${groupParams}">
                    <option value="${it.code}">${it.name}</option>
                </g:each>
            </select>
            <hr/>
            <label class="control-label" >查询条件:</label>
            <input type="hidden"  id="queryParams"  name="queryParams" value="${queryParams.collect({it.code}).join(',')}">
            <g:each in="${queryParams}">
                <label class="control-label" style="margin-left: 20px">${it.name}</label>

                    <select id="${it.code}" name="${it.code}" class="span3" class="multiselect" multiple="multiple">
                        <g:each in="${it.data}" var="item">
                            <option value="${item.key}">${item.value}</option>
                        </g:each>
                    </select>

            </g:each>
            <hr/>
            <g:render template="/report/selectDateRange" />
            <input type="hidden"  id="reportName"  name="reportName" value="${reportName}">
        <br/>
        <br/>
        <hr/>
        <label class="control-label" >可选展示列:</label>
            <g:each in="${displayParams}">
                <input type="checkbox" name="displayParams" value="${it.code}" checked="checked">${it.name}&nbsp;&nbsp;
            </g:each>
        <br/>
        <hr/>
        <button type="button" id="queryBtn" class="btn btn-primary offset1" style="margin-left:10px" onclick="postAndShow()"><i class="icon-search icon-white"></i>查询</button>
        <hr/>
        </div>
    </g:form>
    <div id="centerData">
        <div>
            <table class="table table-hover  table-condensed bq-table" style="margin-bottom:0px" id="domainTable">
                <g:render template="/report/dataTable" />
            </table>
        </div>
    </div>
</div>