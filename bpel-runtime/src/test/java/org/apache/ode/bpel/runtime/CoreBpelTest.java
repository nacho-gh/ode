/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.ode.bpel.runtime;

import org.apache.ode.jacob.vpu.FastSoupImpl;
import org.apache.ode.jacob.vpu.JacobVPU;
import org.apache.ode.bpel.common.CorrelationKey;
import org.apache.ode.bpel.common.FaultException;
import org.apache.ode.bpel.evt.ProcessInstanceEvent;
import org.apache.ode.bpel.o.*;
import org.apache.ode.bpel.o.OMessageVarType.Part;
import org.apache.ode.bpel.runtime.channels.FaultData;
import org.apache.ode.bpel.runtime.channels.InvokeResponseChannel;
import org.apache.ode.bpel.runtime.channels.PickResponseChannel;
import org.apache.ode.bpel.runtime.channels.TimerResponseChannel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.Collection;

import javax.wsdl.Operation;
import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Test core BPEL processing capabilities.
 */
public class CoreBpelTest extends TestCase implements BpelRuntimeContext {
  private boolean _completedOk;
  private boolean _terminate;
  private FaultData _fault;
  private FastSoupImpl _soup;
  private JacobVPU _vpu;
  private Long _pid;
  private long _seq;

  protected void setUp() throws Exception {
    _completedOk= false;
    _terminate = false;
    _fault = null;
    _soup = new FastSoupImpl(CoreBpelTest.class.getClassLoader());
    _vpu = new JacobVPU(_soup);
    _vpu.registerExtension(BpelRuntimeContext.class, this);
    _pid = new Long(19355);
  }

  public Long getPid() {
    return _pid;
  }
  
  public boolean isVariableInitialized(VariableInstance variable) {
    return false;
  }

  public Long createScopeInstance(Long parentScopeId, OScope scopeType) {
    return new Long((long)(Math.random()*1000L) + scopeType.getId());
  }

  public Node fetchVariableData(VariableInstance var, boolean forWriting) throws FaultException {
    return null;
  }

  public Node fetchVariableData(VariableInstance var, OMessageVarType.Part partname, boolean forWriting) throws FaultException {
    return null;
  }

  public String readProperty(VariableInstance var, OProcess.OProperty property) throws FaultException {
    return null;
  }

  public Node initializeVariable(VariableInstance var, Node initData) {
    return null;
  }

  public void commitChanges(VariableInstance var, Node changes) {
  }

  public boolean isCorrelationInitialized(CorrelationSetInstance cset) {
    return false;
  }

  public CorrelationKey readCorrelation(CorrelationSetInstance cset) {
    return null;
  }

  public void writeCorrelation(CorrelationSetInstance cset, CorrelationKey correlation) {
  }


  public void cancel(TimerResponseChannel timerResponseChannel) {
  }

  public void completedOk() {
    _completedOk = true;
  }

  public void completedFault(FaultData faultData) {
    _fault = faultData;
  }

  public void select(PickResponseChannel response, Date timeout, boolean createInstnace, Selector[] selectors) throws FaultException {
  }

  public void reply(PartnerLinkInstance plink, String opName, String mexId, Element msg, String fault) throws FaultException {
  }

  public String invoke(PartnerLinkInstance partnerLinkInstance, Operation operation, Element outboundMsg, InvokeResponseChannel invokeResponseChannel) {
    return null;
  }

  public void registerTimer(TimerResponseChannel timerChannel, Date timeToFire) {
  }

  public void terminate() {
    _terminate = true;
  }

  public void sendEvent(ProcessInstanceEvent event) {
  }

  public ExpressionLanguageRuntimeRegistry getExpLangRuntime() {
    return null;
  }

  public void initializePartnerLinks(Long parentScopeId, Collection<OPartnerLink> partnerLinks) {
  }

  public Element fetchEndpointReferenceData(PartnerLinkInstance pLink, boolean isMyEPR) throws FaultException {
    return null;
  }

  public Node writeEndpointReference(PartnerLinkInstance variable, Node data) throws FaultException {
    return null;
  }

  public void testEmptyProcess() {
    OProcess proc = new OProcess("2.0");
    proc.procesScope = new OScope(proc);
    proc.procesScope.activity = new OEmpty(proc);

    run(proc);

    assertTrue(_completedOk);
    assertFalse(_terminate);
    assertNull(_fault);
  }

  public void testThrow() {
    OProcess proc = new OProcess("2.0");
    proc.procesScope = new OScope(proc);
    OThrow othrow = new OThrow(proc);
    othrow.faultName = new QName("foo", "bar");
    proc.procesScope.activity = othrow;

    run(proc);

    assertFalse(_completedOk);
    assertFalse(_terminate);
    assertEquals(_fault.getFaultName(), othrow.faultName);
  }

  public void testFaultHandling() {
    OProcess proc = new OProcess("2.0");
    proc.procesScope = new OScope(proc);
    OThrow othrow = new OThrow(proc);
    othrow.faultName = new QName("foo", "bar");
    proc.procesScope.activity = othrow;
    proc.procesScope.faultHandler = new OFaultHandler(proc);
    OCatch ocatch = new OCatch(proc);
    proc.procesScope.faultHandler.catchBlocks.add(ocatch);
    ocatch.activity = new OEmpty(proc);
    run(proc);

    assertTrue(_completedOk);
    assertFalse(_terminate);
    assertNull(_fault);
  }

 
  public void testOneElementSequence() {
    OProcess proc = new OProcess("2.0");
    proc.procesScope = new OScope(proc);
    OSequence sequence = new OSequence(proc);
    proc.procesScope.activity = sequence;
    sequence.sequence.add(new OEmpty(proc));

    run(proc);

    assertTrue(_completedOk);
    assertFalse(_terminate);
    assertNull(_fault);
  }

  public void testTwoElementSequence() {
    OProcess proc = new OProcess("2.0");
    proc.procesScope = new OScope(proc);
    OSequence sequence = new OSequence(proc);
    proc.procesScope.activity = sequence;
    sequence.sequence.add(new OEmpty(proc));
    sequence.sequence.add(new OEmpty(proc));

    run(proc);

    assertTrue(_completedOk);
    assertFalse(_terminate);
    assertNull(_fault);
  }

  public void testEmptyFlow() {
    OProcess proc = new OProcess("2.0");
    proc.procesScope = new OScope(proc);
    proc.procesScope.activity = new OFlow(proc);

    run(proc);

    assertTrue(_completedOk);
    assertFalse(_terminate);
    assertNull(_fault);
  }

  public void testSingleElementFlow() {
    OProcess proc = new OProcess("2.0");
    proc.procesScope = new OScope(proc);
    OFlow flow = new OFlow(proc);
    proc.procesScope.activity = flow;
    flow.parallelActivities.add(new OEmpty(proc));

    run(proc);

    assertTrue(_completedOk);
    assertFalse(_terminate);
    assertNull(_fault);
  }

  public void testTwoElementFlow() {
    OProcess proc = new OProcess("2.0");
    proc.procesScope = new OScope(proc);
    OFlow flow = new OFlow(proc);
    proc.procesScope.activity = flow;
    flow.parallelActivities.add(new OEmpty(proc));
    flow.parallelActivities.add(new OEmpty(proc));

    run(proc);

    assertTrue(_completedOk);
    assertFalse(_terminate);
    assertNull(_fault);
  }


  public void testFlowTermination() {
    OProcess proc = new OProcess("2.0");
    proc.procesScope = new OScope(proc);
    OFlow flow = new OFlow(proc);
    proc.procesScope.activity = flow;
    OThrow othrow = new OThrow(proc);
    othrow.faultName = new QName("foo","bar");
    flow.parallelActivities.add(othrow);
    flow.parallelActivities.add(new OEmpty(proc));
    flow.parallelActivities.add(new OEmpty(proc));
    flow.parallelActivities.add(new OEmpty(proc));
    flow.parallelActivities.add(new OEmpty(proc));
    flow.parallelActivities.add(new OEmpty(proc));
    flow.parallelActivities.add(new OEmpty(proc));

    run(proc);

    assertFalse(_completedOk);
    assertFalse(_terminate);
    assertEquals(_fault.getFaultName(), othrow.faultName);
  }

  private void run(OProcess proc) {
    _vpu.inject(new PROCESS(proc));
    for (int i = 0; i < 100000 && !_completedOk && _fault == null && !_terminate; ++i ) {
      _vpu.execute();
    }

    assertTrue(_soup.isComplete());

    ByteArrayOutputStream bos = new ByteArrayOutputStream();

    try {
      _soup.write(bos);
      _soup.read(new ByteArrayInputStream(bos.toByteArray()));
      // check empty soup.
    } catch (Exception ex) {

    }
  }

  public long genId() {
    return _seq++;
  }

  public Element getPartnerResponse(String mexId) {
    // TODO Auto-generated method stub
    return null;
  }

  public Element getMyRequest(String mexId) {
    // TODO Auto-generated method stub
    return null;
  }

  public QName getPartnerFault(String mexId) {
    // TODO Auto-generated method stub
    return null;
  }

  public QName getPartnerResponseType(String mexId) {
    // TODO Auto-generated method stub
    return null;
  }

  public boolean isEndpointReferenceInitialized(PartnerLinkInstance pLink, boolean isMyEpr) {
    // TODO Auto-generated method stub
    return false;
  }

  public String fetchEndpointSessionId(PartnerLinkInstance pLink, boolean isMyEPR) throws FaultException {
    // TODO Auto-generated method stub
    return null;
  }

  public Element updatePartnerEndpointReference(PartnerLinkInstance variable, Element data) throws FaultException {
    // TODO Auto-generated method stub
    return null;
  }

  public Node convertEndpointReference(Element epr, Node targetNode) {
    // TODO Auto-generated method stub
    return null;
  }

  public Node getPartData(Element message, Part part) {
    // TODO Auto-generated method stub
    return null;
  }

  public Element getSourceEPR(String mexId) {
    // TODO Auto-generated method stub
    return null;
  }

  public Element writeEndpointReference(PartnerLinkInstance variable, Element data) throws FaultException {
    // TODO Auto-generated method stub
    return null;
  }
}
