/**
 * Together Workflow Server
 * Copyright (C) 2011 Together Teamsolutions Co., Ltd. 
 * 
 * This program is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU General Public License for more details. 
 *
 * You should have received a copy of the GNU General Public License 
 * along with this program. If not, see http://www.gnu.org/licenses
 */

package org.enhydra.shark.assignment.historyrelated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.enhydra.jxpdl.XMLUtil;
import org.enhydra.jxpdl.XPDLConstants;
import org.enhydra.shark.Shark;
import org.enhydra.shark.api.RootError;
import org.enhydra.shark.api.client.wfmc.wapi.WMSessionHandle;
import org.enhydra.shark.api.client.wfmodel.WfActivity;
import org.enhydra.shark.api.client.wfmodel.WfActivityIterator;
import org.enhydra.shark.api.client.wfmodel.WfProcess;
import org.enhydra.shark.api.client.wfservice.SharkConnection;
import org.enhydra.shark.api.client.wfservice.WMEntity;
import org.enhydra.shark.api.common.SharkConstants;
import org.enhydra.shark.api.internal.assignment.AssignmentManager;
import org.enhydra.shark.api.internal.assignment.PerformerData;
import org.enhydra.shark.api.internal.partmappersistence.ParticipantMap;
import org.enhydra.shark.api.internal.partmappersistence.ParticipantMappingManager;
import org.enhydra.shark.api.internal.usergroup.UserGroupManager;
import org.enhydra.shark.api.internal.working.CallbackUtilities;
import org.enhydra.shark.utilities.misc.MiscUtilities;
import org.enhydra.shark.utilities.wmentity.WMEntityUtilities;

/**
 * This class provides an extended Assignment Manager implementation via the use of XPDL
 * activity extended attributes. The following extended attributes can be associated with
 * an activity to affect assignments: ReassignToOriginalPerformer - If an activity is
 * executed more than once in a particular process, then it will only be assigned to the
 * original performer during subsequent executions. The value of this extended attribute
 * is ignored. AssignToPerformerOfActivity - This extended attribute can be used to force
 * an activity to be assigned to the performer of a previously-executed activity. The
 * value of this extended attribute should be the activity definition id in question.
 * DoNotAssignToPerformerOfActivity - This extended attribute can be used to force an
 * activity NOT to be assigned to the performer of a previously- executed activity. The
 * value of this extended attribute should be the activity definition id in question. Note
 * that only one of each extended attribute should be associated with any single activity
 * definition. Note that the above names are just the default names of these extended
 * attributes, and that they can be overriden in the configuration file (Shark.conf) using
 * the following properties: -
 * HistoryRelatedAssignmentManager.extAttrReassignToOriginalPerformer -
 * HistoryRelatedAssignmentManager.extAttrAssignToPerformerOfActivity -
 * HistoryRelatedAssignmentManager.extAttrDoNotAssignToPerformerOfActivity Finally, note
 * that this class needs to make a connection to the workflow engine. If anybody wishes to
 * extend/modify this class in any way, one obvious improvment would be to allow multiple
 * copies of each extended attribute to be assigned to a single activity.
 * 
 * @author Rich Robinson
 */
public class HistoryRelatedAssignmentManager implements AssignmentManager {

   public static final String USER_GROUP_CLASS_NAME_PROPERTY = "UserGroupManagerClassName";

   public static final String PARTICIPANT_MAPPING_CLASS_NAME_PROPERTY = "ParticipantMapPersistenceManagerClassName";

   public static final String DEFAULT_ASSIGNEES_PROPERTY = "defaultAssignees";

   public static final String SHARK_PROCESS_REQUESTER = "SHARK_PROCESS_REQUESTER";

   public static final String TRY_STRAIGHTFORWARD_MAPPING_PROPERTY = "tryStraightforwardMapping";

   public static final String EA_TRY_STRAIGHTFORWARD_MAPPING = "ASSIGNMENT_MANAGER_TRY_STRAIGHTFORWARD_MAPPING";

   protected UserGroupManager userGroupManager;

   protected ParticipantMappingManager participantMappings;

   protected CallbackUtilities cus;

   protected String extAttrReassignToOriginalPerformer = "AssignToOriginalPerformer";

   protected String extAttrAssignToPerformerOfActivity = "AssignToPerformerOfActivity";

   protected String extAttrDoNotAssignToPerformerOfActivity = "DoNotAssignToPerformerOfActivity";

   protected String shortClassName = MiscUtilities.getShortClassName(getClass().getName());

   protected String defaultAssignees;

   protected boolean caseInsensitiveOutput;

   protected boolean tryStraightforwardMapping;

   public void configure(CallbackUtilities cus) throws Exception {
      this.cus = cus;

      String ugClassName = cus.getProperty(shortClassName
                                           + "." + USER_GROUP_CLASS_NAME_PROPERTY);
      String pmmClassName = cus.getProperty(shortClassName
                                            + "."
                                            + PARTICIPANT_MAPPING_CLASS_NAME_PROPERTY);

      defaultAssignees = cus.getProperty(shortClassName + "." + DEFAULT_ASSIGNEES_PROPERTY, SHARK_PROCESS_REQUESTER);
      caseInsensitiveOutput = new Boolean(cus.getProperty(shortClassName
                                                          + "."
                                                          + SharkConstants.PROPERTY_NAME_CASE_INSENSITIVE_OUTPUT,
                                                    "false")).booleanValue();
      tryStraightforwardMapping = new Boolean(cus.getProperty(shortClassName + "." + TRY_STRAIGHTFORWARD_MAPPING_PROPERTY, "true")).booleanValue();
      ClassLoader cl = getClass().getClassLoader();

      try {
         userGroupManager = (UserGroupManager) cl.loadClass(ugClassName).newInstance();
         userGroupManager.configure(cus);
         cus.info(null, shortClassName
                        + " -> Working with '" + ugClassName
                        + "' implementation of UserGroup API");
      } catch (Exception ex) {
         boolean throwError = true;
         String msg = shortClassName + " -> Can't work - ";
         if (ugClassName == null || ugClassName.trim().equals("")) {
            msg = shortClassName + " -> Working without UserGroup API implementation - ";
            msg += "UserGroupManager is not specified.";
            throwError = false;
         } else if (userGroupManager == null) {
            msg += "Can't find UserGroupManager class '"
                   + ugClassName + "' in classpath!";
         } else {
            msg += "Problems while configuring UserGroupManager!";
         }
         cus.info(null, msg);
         if (throwError) {
            throw new RootError(msg, ex);
         }
      }

      try {
         participantMappings = (ParticipantMappingManager) cl.loadClass(pmmClassName)
            .newInstance();
         participantMappings.configure(cus);
         cus.info(null, shortClassName
                        + " -> Working with '" + pmmClassName
                        + "' implementation of ParticipantMapping API");
      } catch (Exception ex) {
         boolean throwError = true;
         String msg = shortClassName + " -> Can't work - ";
         if (pmmClassName == null || pmmClassName.trim().equals("")) {
            msg = shortClassName
                  + ". -> Working without ParticipantMapping API implementation - ";
            msg += "ParticipantMappingManager is not specified.";
            throwError = false;
         } else if (participantMappings == null) {
            msg += "Can't find ParticipantMappingManager class '"
                   + pmmClassName + "' in classpath!";
         } else {
            msg += "Problems while configuring ParticipantMappingManager!";
         }
         cus.info(null, msg);
         if (throwError) {
            throw new RootError(msg, ex);
         }
      }

   }

   public List getAssignments(WMSessionHandle shandle,
                              String procId,
                              String actId,
                              String processRequesterId,
                              PerformerData xpdlParticipant,
                              List xpdlResponsibleParticipants) throws Exception {
      List result = new ArrayList();

      String[][] actExtAttribs = null;

      WMEntity ent = Shark.getInstance()
         .getAdminMisc()
         .getActivityDefinitionInfo(shandle, procId, actId);
      actExtAttribs = WMEntityUtilities.getExtAttribNVPairs(shandle, Shark.getInstance()
         .getXPDLBrowser(), ent);

      if (actExtAttribs != null) {
         if (XMLUtil.getExtendedAttributeValue(actExtAttribs,
                                               extAttrReassignToOriginalPerformer) != null) {
            result = doReassignToOriginalPerformer(shandle,
                                                   procId,
                                                   actId,
                                                   processRequesterId,
                                                   xpdlParticipant,
                                                   xpdlResponsibleParticipants,
                                                   ent.getId());
         } else if (XMLUtil.getExtendedAttributeValue(actExtAttribs,
                                                      extAttrAssignToPerformerOfActivity) != null) {
            result = doAssignToPerformerOfActivity(shandle,
                                                   procId,
                                                   actId,
                                                   processRequesterId,
                                                   xpdlParticipant,
                                                   xpdlResponsibleParticipants,
                                                   actExtAttribs);
         } else if (XMLUtil.getExtendedAttributeValue(actExtAttribs,
                                                      extAttrDoNotAssignToPerformerOfActivity) != null) {
            result = doDoNotAssignToPerformerOfActivity(shandle,
                                                        procId,
                                                        actId,
                                                        processRequesterId,
                                                        xpdlParticipant,
                                                        xpdlResponsibleParticipants,
                                                        actExtAttribs);
         } else {
            // If this is not a special case then use the parent implementation.
            result = getDefaultAssignments(shandle,
                                           procId,
                                           actId,
                                           processRequesterId,
                                           xpdlParticipant,
                                           xpdlResponsibleParticipants);
         }
      } else {
         // If there were some problems then use the parent implementation.
         result = getDefaultAssignments(shandle,
                                        procId,
                                        actId,
                                        processRequesterId,
                                        xpdlParticipant,
                                        xpdlResponsibleParticipants);
      }
      if (result.size()==0) {
         if (defaultAssignees != null && !defaultAssignees.equals("")) {
            String[] das = MiscUtilities.tokenize(defaultAssignees,
                                                  MiscUtilities.COMMA_SEPARATOR_STR);
            for (int i = 0; i < das.length; i++) {
               if (das[i].equals(SHARK_PROCESS_REQUESTER)) {
                  result.add(processRequesterId);
               } else {
                  result.add(das[i]);
               }
            }
         }
      }
      if (caseInsensitiveOutput) {
         result = MiscUtilities.makeCaseInsensitive(result, true);
      }
      
      return result;
   }

   protected List doReassignToOriginalPerformer(WMSessionHandle shandle,
                                                String procId,
                                                String actId,
                                                String processRequesterId,
                                                PerformerData xpdlParticipant,
                                                List xpdlResponsibleParticipants,
                                                String actDefId) throws Exception {

      return getAssignmentsForActDefId(shandle,
                                       procId,
                                       actId,
                                       processRequesterId,
                                       xpdlParticipant,
                                       xpdlResponsibleParticipants,
                                       actDefId,
                                       true);
   }

   protected List doAssignToPerformerOfActivity(WMSessionHandle shandle,
                                                String procId,
                                                String actId,
                                                String processRequesterId,
                                                PerformerData xpdlParticipant,
                                                List xpdlResponsibleParticipants,
                                                String[][] actExtAttribs)
      throws Exception {
      String actDefId = XMLUtil.getExtendedAttributeValue(actExtAttribs,
                                                          extAttrAssignToPerformerOfActivity);

      return getAssignmentsForActDefId(shandle,
                                       procId,
                                       actId,
                                       processRequesterId,
                                       xpdlParticipant,
                                       xpdlResponsibleParticipants,
                                       actDefId,
                                       true);
   }

   protected List doDoNotAssignToPerformerOfActivity(WMSessionHandle shandle,
                                                     String procId,
                                                     String actId,
                                                     String processRequesterId,
                                                     PerformerData xpdlParticipant,
                                                     List xpdlResponsibleParticipants,
                                                     String[][] actExtAttribs)
      throws Exception {
      String actDefId = XMLUtil.getExtendedAttributeValue(actExtAttribs,
                                                          extAttrDoNotAssignToPerformerOfActivity);

      List doNotAssignTo = getAssignmentsForActDefId(shandle,
                                                     procId,
                                                     actId,
                                                     processRequesterId,
                                                     xpdlParticipant,
                                                     xpdlResponsibleParticipants,
                                                     actDefId,
                                                     false);

      List allAssignments = getDefaultAssignments(shandle,
                                                  procId,
                                                  actId,
                                                  processRequesterId,
                                                  xpdlParticipant,
                                                  xpdlResponsibleParticipants);

      /*
       * If we only have one assignment then we CANNOT remove the "do not assign to"
       * assignment (either it won't be the same assignment, in which case there's no need
       * to remove it, or it WILL be the same assignment, and we will be left with an
       * empty assignments list - this should probably never happen).
       */
      if (allAssignments.size() > 1) {
         allAssignments.removeAll(doNotAssignTo);
      }

      return allAssignments;
   }

   /*
    * Given an activity definition id, this method returns assignments based on the last
    * resource to execute that activity definition id in this process.
    */
   protected List getAssignmentsForActDefId(WMSessionHandle shandle,
                                            String procId,
                                            String actId,
                                            String processRequesterId,
                                            PerformerData xpdlParticipant,
                                            List xpdlResponsibleParticipants,
                                            String actDefId,
                                            boolean fallbackToDefault) throws Exception {
      List result = new ArrayList();

      String prevPerformer = getPrevPerformerOfActDefId(shandle, procId, actDefId);

      if (prevPerformer != null) {
         result.add(prevPerformer);
      } else if (fallbackToDefault) {
         /*
          * If we are falling back to the parent implementation in the case that there is
          * no resource to assign to, and if our resource list is empty then we call the
          * super implementation.
          */
         result = getDefaultAssignments(shandle,
                                        procId,
                                        actId,
                                        processRequesterId,
                                        xpdlParticipant,
                                        xpdlResponsibleParticipants);
      }

      return result;
   }

   protected String getPrevPerformerOfActDefId(WMSessionHandle shandle,
                                               String procId,
                                               String actDefId) throws Exception {
      String result = null;

      if (actDefId != null) {
         SharkConnection sc = Shark.getInstance().getSharkConnection();

         try {
            sc.attachToHandle(shandle);
            WfProcess proc = sc.getProcess(procId);

            WfActivityIterator wai = proc.get_iterator_step();

            String query = "state.equals(\"closed.completed\") && definitionId.equals(\""
                           + actDefId + "\")";

            wai.set_query_expression(query);

            WfActivity[] acts = wai.get_next_n_sequence(0);
            if (acts != null && acts.length > 0) {
               // find the last one - maybe there was some reassignments
               long maxTime = -1;
               WfActivity lastAct = null;
               for (int i = 0; i < acts.length; i++) {
                  long time = acts[i].last_state_time().getTime();
                  if (time > maxTime) {
                     maxTime = time;
                     lastAct = acts[i];
                  }
               }
               String prevActId = lastAct.key();

               /*
                * Get the resource that finished the previous instance of the specified
                * activity.
                */
               result = Shark.getInstance()
                  .getAdminMisc()
                  .getActivityResourceUsername(shandle, procId, prevActId);
            }
         } finally {
            sc.disconnect();
         }

      }

      return result;
   }

   public List getDefaultAssignments(WMSessionHandle shandle,
                                     String procId,
                                     String actId,
                                     String processRequesterId,
                                     PerformerData xpdlParticipant,
                                     List xpdlResponsibleParticipants) throws Exception {

      List result = new ArrayList();
      Set performers = findResources(shandle, procId, actId, xpdlParticipant);
      if (performers != null && performers.size() > 0) {
         result = new ArrayList(performers);
      } 
      if (result.size()==0) {
         // collect all responsibles of the activity
         Set responsibles = new HashSet();
         for (int i = 0; i < xpdlResponsibleParticipants.size(); i++) {
            PerformerData pd = (PerformerData) xpdlResponsibleParticipants.get(i);
            responsibles.addAll(findResources(shandle,procId, actId, pd));
         }
         result = new ArrayList(responsibles);
      }
      
      
      return result;
   }

   public ParticipantMappingManager getParticipantMapPersistenceManager() {
      return participantMappings;
   }

   public UserGroupManager getUserGroupManager() {
      return userGroupManager;
   }

   /**
    * Return a resource Ids for the specified participant.
    * 
    * @return A set of resource mapping for given participant.
    */
   protected Set findResources(WMSessionHandle shandle, String procId, String actId, PerformerData p) throws Exception {
      Set ress = new HashSet();
      if (p.participantIdOrExpression == null)
         return ress;
      Set usernames = new HashSet();

      // We can live without mapping manager
      List pMappings = new ArrayList();
      ParticipantMappingManager pms = getParticipantMapPersistenceManager();
      if (null != pms) {
         try {
            pMappings = Arrays.asList(pms.getParticipantMappings(shandle,
                                                                 p.pkgId,
                                                                 p.pkgVer,
                                                                 p.pDefId,
                                                                 p.participantIdOrExpression));
         } catch (Exception ex) {
            cus.error(shandle, shortClassName + ". -> Error in findResources()", ex);
            throw ex;
         }
      }

      // getting mapped users/groups
      Iterator it = pMappings.iterator();
      Set groupnames = new HashSet();
      while (it.hasNext()) {
         ParticipantMap pm = (ParticipantMap) it.next();
         String uname = pm.getUsername();
         if (pm.getIsGroupUser()) {
            groupnames.add(uname);
         } else {
            usernames.add(uname);
         }
      }

      // expanding user groups
      UserGroupManager ugm = getUserGroupManager();
      boolean tsfm = tryStraightforwardMapping;
      if (ugm != null) {
         tsfm = WMEntityUtilities.getAndCacheXPDLExtendedAttributeAsBoolean(shandle,
                                                                            procId,
                                                                            actId,
                                                                            EA_TRY_STRAIGHTFORWARD_MAPPING,
                                                                            new Boolean(tryStraightforwardMapping),
                                                                            true);
      }
      if ((p.isUnresolvedExpressionParticipant || pms == null || (pms != null && groupnames.size() == 0 && usernames.size() == 0)) && tsfm && ugm != null) {
         String defaultUGDomain = cus.getProperty("DefaultUserGroupDomain", "");
         if (!defaultUGDomain.equals("")) {
            defaultUGDomain += "\\";
         }
         if (p.isUnresolvedExpressionParticipant) {
            if (ugm.doesGroupExist(shandle, p.participantIdOrExpression)) {
               groupnames.add(p.participantIdOrExpression);
            } else if (ugm.doesUserExist(shandle, p.participantIdOrExpression)) {
               usernames.add(p.participantIdOrExpression);
            }
         } else if (p.participantType.equals(XPDLConstants.PARTICIPANT_TYPE_HUMAN)) {
            if (ugm.doesUserExist(shandle, defaultUGDomain + p.participantIdOrExpression)) {
               usernames.add(defaultUGDomain + p.participantIdOrExpression);
            }
         } else {
            if (ugm.doesGroupExist(shandle, defaultUGDomain + p.participantIdOrExpression)) {
               groupnames.add(defaultUGDomain + p.participantIdOrExpression);
            }
         }
      } else if (p.isUnresolvedExpressionParticipant) {
         usernames.add(p.participantIdOrExpression);
      }

      // We can live without usergroup manager
      if (ugm != null) {
         try {
            String[] gnstrarr = new String[groupnames.size()];
            groupnames.toArray(gnstrarr);
            usernames.addAll(Arrays.asList(ugm.getAllUsersForGroups(shandle, gnstrarr)));
         } catch (Exception ex) {
            cus.error(shandle, shortClassName + " -> Error in findResources() : ", ex);
            throw ex;
         }
      } else {
         usernames.addAll(groupnames);
      }
      return usernames;
   }

}
