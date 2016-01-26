package org.jboss.qa.ochaloup.xa;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Implementation of XAResource for use in txbridge recovery tests.
 *
 * Note: TestXAResourceRecovered cannot directly extend TestXAResource, otherwise
 * TestXAResource will be every time instrumented together with TestXAResourceRecovered
 * which will deny the distinction between them and will increase the number of
 * known instrumented instances of TestXAResource, for example in InboundCrashRecoveryTests#testCrashOneLog
 * if the recovery process is run on the server side before the following assert:
 *   ...
 *   execute(baseURL + TestClient.URL_PATTERN, false);
 *   // if recovery process is run before the next assert and if TestXAResourceRecovered
 *   // extends TestXAResource then the assert will fail because of 2 known instances
 *   instrumentedTestXAResource.assertKnownInstances(1);
 *   ...
 */
public class TestXAResourceRecovered extends TestXAResourceCommon implements XAResource {
    public TestXAResourceRecovered() {
        System.out.println(String.format("TestXAResourceRecovered.created()[id=%s]", id));
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        System.out.println(String.format("TestXAResourceRecovered.rollback(Xid=%s)[id=%s]", xid, id));
        TestXAResourceRecoveryHelper.getInstance().removeLog(xid);
    }

    @Override
    public void commit(Xid xid, boolean b) throws XAException {
        System.out.println(String.format("TestXAResourceRecovered.commit(Xid=%s, b=%s)[id=%s]", xid, b, id));
        TestXAResourceRecoveryHelper.getInstance().removeLog(xid);
    }

    @Override
    public Xid[] recover(int i) throws XAException {
        System.out.println(String.format("TestXAResourceRecovered.recover(i=%s)[id=%s]", i, id));
        return TestXAResourceRecoveryHelper.getInstance().recover();
    }

    @Override
    public void forget(Xid xid) throws XAException {
        System.out.println(String.format("TestXAResource.forget(Xid=%s)[id=%s]", xid, id));
        TestXAResourceRecoveryHelper.getInstance().removeLog(xid);
    }

    @Override
    public void end(Xid xid, int i) throws XAException {
        super.end(xid, i);
    }

    @Override
    public int getTransactionTimeout() throws XAException {
        return super.getTransactionTimeout();
    }

    @Override
    public boolean isSameRM(XAResource xaResource) throws XAException {
        return super.isSameRM(xaResource);
    }

    @Override
    public int prepare(Xid xid) throws XAException {
        return super.prepare(xid);
    }

    @Override
    public boolean setTransactionTimeout(int i) throws XAException {
        return super.setTransactionTimeout(i);
    }

    @Override
    public void start(Xid xid, int i) throws XAException {
        super.start(xid, i);
    }

    @Override
    public String toString() {
        return new String("TestXAResourceRecovered(" + super.toString() + ")");
    }

}
