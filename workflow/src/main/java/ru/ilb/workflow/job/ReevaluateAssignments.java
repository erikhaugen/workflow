/**
 * Copyright (C) 2015 Bystrobank, JSC
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see http://www.gnu.org/licenses
 */
package ru.ilb.workflow.job;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.enhydra.shark.api.client.wfmc.wapi.WAPI;
import org.enhydra.shark.api.client.wfmc.wapi.WMFilter;
import org.enhydra.shark.api.client.wfmc.wapi.WMProcessInstance;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.common.ProcessFilterBuilder;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.utilities.interfacewrapper.SharkInterfaceWrapper;
import org.enhydra.shark.utilities.logging.LoggingUtilities;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.LoggerFactory;

/**
 *
 * @author slavb
 */
@DisallowConcurrentExecution
public class ReevaluateAssignments implements Job {

    int instancesPerTransaction = 50;
    int failuresToIgnore = 500;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ReevaluateAssignments.class);

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        logger.info("Reevaluate assignments!");
        UserTransaction ut = null;
        try {
            ProcessFilterBuilder pfb = SharkInterfaceWrapper.getShark().getProcessFilterBuilder();
            WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();

            javax.naming.Context ctx = new InitialContext();
            ut = (UserTransaction) ctx.lookup("java:comp/env/UserTransaction");
            ut.begin();
            WMSessionHandle shandle = SharkInterfaceWrapper.getDefaultSessionHandle(null);
            WMFilter processfilter = pfb.addStateStartsWith(shandle, SharkConstants.STATEPREFIX_OPEN);
            processfilter=pfb.and(shandle, processfilter, pfb.not(shandle, pfb.addPackageIdEquals(shandle, "offer")));
            //pfb.setLimit(shandle, processfilter, 50);
            WMProcessInstance[] procsInst = wapi.listProcessInstances(shandle, processfilter, false).getArray();
            ut.commit();
            logger.info("Reevaluate assignments: {} processes",procsInst.length);
            List<String> procs=new ArrayList<>();
            for (WMProcessInstance proc : procsInst){
                procs.add(proc.getId());
            }
            reevaluateAssignments(shandle,procs);
            
            /*SharkInterfaceWrapper.getShark()
                    .getExecutionAdministration().reevaluateAssignmentsWithFiltering(shandle, null, true);*/
            
        } catch (Exception ex) {
            logger.error("Reevaluate assignments failed!", ex);
        } finally {
            try {
                if (ut != null && ut.getStatus() != Status.STATUS_NO_TRANSACTION) {
                    ut.rollback();
                }
            } catch (SystemException | IllegalStateException | SecurityException ex1) {
            }
        }

    }

    protected void reevaluateAssignments(WMSessionHandle shandle, List instancesToCheck)
            throws Exception {
        int sizeToCheck = instancesToCheck.size();
        Iterator iterProcesses = instancesToCheck.iterator();
        List instancesFailed2check = new ArrayList();
        List currentBatch = null;
        do {
            UserTransaction t = null;
            currentBatch = new ArrayList();
            try {
                for (int n = 0; n < instancesPerTransaction; ++n) {
                    if (!iterProcesses.hasNext()) {
                        break;
                    }
                    String procId = (String) iterProcesses.next();
                    iterProcesses.remove();
                    currentBatch.add(procId);
                }
                String[] pids = new String[currentBatch.size()];
                currentBatch.toArray(pids);
                t = SharkInterfaceWrapper.getUserTransaction();
                t.begin();
                SharkInterfaceWrapper.getShark()
                        .getExecutionAdministration()
                        .reevaluateAssignmentsForProcesses(shandle, pids, true);
                t.commit();
            } catch (Exception ex) {
                LoggingUtilities.log4jWarn(null, "Problem during execution!", ex, true, true);
                try {
                    if (t != null && t.getStatus() != Status.STATUS_NO_TRANSACTION) {
                        t.rollback();
                    }
                } catch (Exception _) {
                }
                instancesFailed2check.addAll(currentBatch);
                // may log something
            }
        } while (instancesFailed2check.size() <= failuresToIgnore
                && iterProcesses.hasNext());
        LoggingUtilities.log4jInfo(null, "... reevaluateAssignments: checked:"
                + sizeToCheck + ", failed:"
                + instancesFailed2check.size(), null, true, true);
    }

}
