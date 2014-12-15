<script src="${resource(dir:'report/js', file:'daterangepicker.js')}"></script>
<script src="${resource(dir:'report/js', file:'moment.js')}"></script>

<link rel="stylesheet" href="${resource(dir: 'report/css', file: 'daterangepicker.css')}" type="text/css">

<input id="startDate" name="startDate" type="hidden"/>
<input id="endDate" name="endDate" type="hidden"/>
<label class="span1 control-label" for="reportrange" style="margin-left: 20px;width:4%;">日期</label>
<div id="daterange" style="float:left;margin-left: 30px">
	<span id="reportrange" class="" style="background: #fff; cursor: pointer; padding: 5px 10px; border: 1px solid #ccc;">
	   <i class="icon-calendar icon-large"></i>
	   <span></span>
	   <b class="caret" style="margin-top: 8px"></b>
	</span>

	<script type="text/javascript">
    $(document).ready(function() {
        $('#reportrange').daterangepicker(
            {
                ranges: {
                    '今天': [new Date(), new Date()],
                    '昨天': [moment().subtract('days', 1), moment().subtract('days', 1)],
                    '过去7天': [moment().subtract('days', 6), new Date()],
                    '过去30天': [moment().subtract('days', 29), new Date()],
                    '本月': [moment().startOf('month'), moment().endOf('month')],
                    '上个月': [moment().subtract('month', 1).startOf('month'), moment().subtract('month', 1).endOf('month')]
                },
                opens: 'left',
                format: 'MM/DD/YYYY',
                separator: ' to ',
                startDate: moment().subtract('days', 6),
                endDate: new Date(),
                minDate: '01/01/2013',
                maxDate: '12/31/2033',
                locale: {
                    applyLabel: '应用',
                    fromLabel: 'From',
                    toLabel: 'To',
                    customRangeLabel: '自定义',
                    daysOfWeek: ['日', '一', '二', '三', '四', '五','六'],
                    monthNames: ['一月', '二月', '三月', '四月', '五月', '六月', '七月', '八月', '九月', '十月', '十一月', '十二月'],
                    firstDay: 1
                },
                showWeekNumbers: true,
                buttonClasses: ['btn-danger'],
                dateLimit: false
            }
        ,
            function(start, end) {
                $('#reportrange span').html(start.format('YYYY-MM-DD') + ' ~ ' + end.format('YYYY-MM-DD'));
                $('#startDate').val(start.format('YYYY-MM-DD'));
                $('#endDate').val(end.format('YYYY-MM-DD'));

            }
        );
        //Set the initial state of the picker label
        $('#reportrange span').html(moment().subtract('days', 6).format('YYYY-MM-DD') + ' ~ ' + moment().format('YYYY-MM-DD'));
        $('#startDate').val(moment().subtract('days', 6).format('YYYY-MM-DD'));
        $('#endDate').val(moment().format('YYYY-MM-DD'));

    });
</script>
</div>