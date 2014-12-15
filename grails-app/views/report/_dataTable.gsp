<thead id="domainHead">
<tr class="theader">
    <g:if test="${headerData}">
    <g:each in="${headerData}" >
        <th>${it}</th>
    </g:each>
    </g:if>
    <g:else>
        <th></th>
    </g:else>
</tr>
</thead>
<tbody id="tbodyId">
</tbody>

