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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfservice.ExecutionAdministration;
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
public class CheckDeadlines implements Job {

    int instancesPerTransaction = 1;
    int failuresToIgnore = 20;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CheckDeadlines.class);

    @Override
    public void execute(JobExecutionContext jec) throws JobExecutionException {
        logger.info("Check deadlines!");
        UserTransaction ut = null;
        try {
            javax.naming.Context ctx = new InitialContext();
            ut = (UserTransaction) ctx.lookup("java:comp/env/UserTransaction");
            ut.begin();

            WMSessionHandle shandle = SharkInterfaceWrapper.getDefaultSessionHandle(null);

            //String ver=SharkInterfaceWrapper.getShark().getPackageAdministration().getCurrentPackageVersion(shandle, "offer");
            /*
             WAPI wapi = SharkInterfaceWrapper.getShark().getWAPIConnection();
             ProcessFilterBuilder pfb = SharkInterfaceWrapper.getShark().getProcessFilterBuilder();
             WMFilter filter = pfb.addStateStartsWith(shandle, SharkConstants.STATEPREFIX_OPEN);
             filter=pfb.and(shandle, filter, pfb.addMgrNameEquals(shandle, "offer#8#offer_grace"));
             WMProcessInstance[] procs = wapi.listProcessInstances(shandle, filter, false).getArray();
             if (procs==null || procs.length==0) {*/
            ut.commit();
            ut.begin();
            ExecutionAdministration executionAdministration = SharkInterfaceWrapper.getShark().getExecutionAdministration();
            String[] procIds = executionAdministration.getDeadlineRichedProcessIds(shandle);
            ut.commit();
            checkDeadlines(shandle, new ArrayList(Arrays.asList(procIds)));
            //executionAdministration.checkDeadlinesForProcesses(shandle, procIds);
            //WMActivityInstance[] acitivities = executionAdministration.checkDeadlinesWithFiltering(shandle, null).getArray();
            

        } catch (Exception ex) {
            logger.error("Check deadlines failed!", ex);
        } finally {
            try {
                if (ut != null && ut.getStatus() != Status.STATUS_NO_TRANSACTION) {
                    ut.rollback();
                }
            } catch (SystemException | IllegalStateException | SecurityException ex1) {
            }
        }

    }

    protected void checkDeadlines(WMSessionHandle shandle, List instancesToCheck)
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
                        .checkDeadlinesForProcesses(shandle, pids);
                t.commit();
                t.begin();
                for (String procId : pids) {
                    try {
                        SharkInterfaceWrapper.getShark()
                                .getExecutionAdministration().reevaluateDeadlinesForProcesses(shandle, new String[]{procId});
                    } catch (Exception ex) {
                        if (!ex.getMessage().contains("closed")) {
                            logger.error("reevaluateDeadlinesForProcesses failed", ex);
                        }
                    }
                }
                t.commit();
            } catch (Exception ex) {
                LoggingUtilities.log4jWarn(null, "Problem during execution!", ex, true, true);
                try {
                    if (t!=null && t.getStatus() != Status.STATUS_NO_TRANSACTION) {
                        t.rollback();
                    }
                } catch (Exception _) {
                }
                instancesFailed2check.addAll(currentBatch);
                // may log something
            }
        } while (instancesFailed2check.size() <= failuresToIgnore
                && iterProcesses.hasNext());
        LoggingUtilities.log4jInfo(null, "... deadline check finished: checked:"
                + sizeToCheck + ", failed:"
                + instancesFailed2check.size(), null, true, true);
    }

}
