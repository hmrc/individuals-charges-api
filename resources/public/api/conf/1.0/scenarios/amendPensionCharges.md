<p>Scenario simulations using Gov-Test-Scenario headers is only available in the sandbox environment.</p>
<table>
    <thead>
        <tr>
            <th>Header Value (Gov-Test-Scenario)</th>
            <th>Scenario</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><p>FORMAT_NINO</p></td>
            <td><p>Simulates a scenario where the format of the supplied NINO field is not valid.</p></td>
        </tr>
        <tr>
            <td><p>FORMAT_TAX_YEAR</p></td>
            <td><p>Simulates a scenario where the format of the supplied Tax year field is not valid.</p></td>
        </tr>
        <tr>
            <td><p>RULE_TAX_YEAR_RANGE_INVALID</p></td>
            <td><p>Simulates a scenario where the Tax year range invalid. A tax year range of one year is required.</p></td>
        </tr>
        <tr>
            <td><p>FORMAT_VALUE</p></td>
            <td><p>Simulates a scenario where one or more values have been added with the incorrect format.</p></td>
        </tr>
        <tr>
            <td><p>FORMAT_COUNTRY_CODE</p></td>
            <td><p>Simulates a scenario where the format of the supplied Tax year field is not valid.</p></td>
        </tr>
        <tr>
            <td><p>RULE_COUNTRY_CODE</p></td>
            <td><p>Simulates a scenario where there is not a valid ISO 3166-1 alpha-3 country code.</p></td>
        </tr>  
        <tr>
            <td><p>FORMAT_PROVIDER_NAME</p></td>
            <td><p>Simulates a scenario where the format of the providers name is invalid.</p></td>
        </tr>
        <tr>
            <td><p>FORMAT_PROVIDERS_ADDRESS</p></td>
            <td><p>Simulates a scenario where the format of the providers address is invalid.</p></td>
        </tr>
        <tr>
            <td><p>MATCHING_RESOURCE_NOT_FOUND</p></td>
            <td><p>Simulates a scenario where the supplied income source could not be found.</p></td>
        </tr>
        <tr>
            <td><p>RULE_LUMP_SUM_BENEFIT_TAKEN_IN_EXCESS</p></td>
            <td><p>Simulates a scenario where you can only provide either Lump sum benefit taken in excess of lifetime allowance or Benefit in excess of lifetime allowance.</p></td>
        </tr>
        <tr>
            <td><p>RULE_BENEFIT_IN_EXCESS</p></td>
            <td><p>Simulates a scenario where you can only provide either Benefit in excess of lifetime allowance or Lump sum benefit taken in excess of lifetime allowance.</p></td>
        </tr>
        <tr>
            <td><p>CLIENT_OR_AGENT_NOT_AUTHORISED</p></td>
            <td><p>Simulates a scenario the client or agent is not authorised. This is because: the client is not subscribed to MTD, the agent is not subscribed to Agent Services, or the client has not authorised the agent to act on their behalf.</p></td>
        </tr> 
        <tr>
            <td><p>RULE_INCORRECT_OR_EMPTY_BODY_SUBMITTED</p></td>
            <td><p>Simulates a scenario where an empty or non-matching body was submitted.</p></td>
        </tr>
        <tr>
            <td><p>RULE_TAX_YEAR_NOT_SUPPORTED</p></td>
            <td><p>Simulates a scenario where the specified tax year is not supported. That is, the tax year specified is before the minimum tax year value.</p></td>
        </tr>
        <tr>
            <td><p>FORMAT_QOPS_REF</p></td>
            <td><p>Simulates a scenario where the format of QOPS reference number is not valid.</p></td>
        </tr>
        <tr>
            <td><p>FORMAT_PENSION_SCHEME_TAX_REFERENCE</p></td>
            <td><p>Simulates a scenario where the format of pension scheme tax referencer is not valid.</p></td>
        </tr>   
    </tbody>
</table>
