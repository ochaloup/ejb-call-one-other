package org.jboss.qa.ochaloup.xa;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.jboss.tm.XAResourceWrapper;

/**
 * Basic implementation of XAResource for use in tx test cases.
 */
public abstract class TestXAResourceCommon implements XAResource, XAResourceWrapper {
    protected int id = (int )(Math.random() * 1000 + 1);
    private int txTimeout;
    private Xid currentXid;
    private int prepareReturnValue = XAResource.XA_OK;
    
    // no-arg constructor for logging
    public TestXAResourceCommon() {
        System.out.println(String.format("TestXAResourceCommon.created()[id=%s]", id));
    }

    public void commit(Xid xid, boolean b) throws XAException {
        System.out.println(String.format("TestXAResourceCommon.commit(Xid=%s, b=%s)[id=%s]", xid, b, id));
        if (!xid.equals(currentXid)) {
            System.out.println("TestXAResourceCommon.commit - wrong Xid!");
        }

        currentXid = null;
        TestXAResourceRecoveryHelper.getInstance().removeLog(xid);
    }

    public void end(Xid xid, int i) throws XAException {
        System.out.println(String.format("TestXAResourceCommon.end(Xid=%s, i=%s)[id=%s]", xid, i, id));
    }

    public void forget(Xid xid) throws XAException {
        System.out.println(String.format("TestXAResourceCommon.forget(Xid=%s)[id=%s]", xid, id));
        if (!xid.equals(currentXid)) {
            System.out.println("TestXAResourceCommon.forget - wrong Xid!");
        }
        currentXid = null;
    }

    public int getTransactionTimeout() throws XAException {
        System.out.println(String.format("TestXAResourceCommon.getTransactionTimeout() [return %s][id=%s]", txTimeout, id));
        return txTimeout;
    }

    public boolean isSameRM(XAResource xaResource) throws XAException {
        System.out.println(String.format("TestXAResourceCommon.isSameRM(xaResource=%s)[return 'false'][id=%s]", xaResource, id));
        return false;
    }

    public int prepare(Xid xid) throws XAException {
        System.out.println(String.format("TestXAResourceCommon.prepare(Xid=%s)[return %s][id=%s]", xid, prepareReturnValue, id));
        if (prepareReturnValue == XA_OK) {
            TestXAResourceRecoveryHelper.getInstance().logPrepared(xid);
        }
        return prepareReturnValue;
    }

    public Xid[] recover(int i) throws XAException {
        System.out.println(String.format("TestXAResourceCommon.recover(i=%s)[id=%s]", i, id));
        return new Xid[0];
    }

    public void rollback(Xid xid) throws XAException {
        System.out.println(String.format("TestXAResourceCommon.rollback(Xid=%s)[id=%s]", xid, id));
        if (!xid.equals(currentXid)) {
            System.out.println(String.format("TestXAResourceCommon.rollback - wrong Xid! Wanted to rollback '%s' but current is '%s'",
                    xid, currentXid));
        }
        currentXid = null;
        TestXAResourceRecoveryHelper.getInstance().removeLog(xid);
    }

    public boolean setTransactionTimeout(int i) throws XAException {
        System.out.println(String.format("TestXAResourceCommon.setTransactionTimeout(i=%s)[id=%s]", i, id));
        txTimeout = i;
        return true;
    }

    public void start(Xid xid, int i) throws XAException {
        System.out.println(String.format("TestXAResourceCommon.start(Xid=%s, i=%s)[id=%s]", xid, i, id));
        if (currentXid != null) {
            System.out.println(String.format("TestXAResourceCommon.start - wrong Xid! Wanted to start '%s' but current is '%s'",
                    xid, currentXid));
        }
        currentXid = xid;
    }

    public String toString() {
        return String.format("TestXAResourceCommon(id:%s, xid:%s, timeout:%s, prepareReturn:%s)", 
                id, currentXid, txTimeout, prepareReturnValue);
    }

    // ---------------- METHODS of XAResourceWrapper --------------------
    public XAResource getResource() {
        throw new UnsupportedOperationException("getResource() method from "
                + XAResourceWrapper.class.getName() + " is not implemented yet");
    }

    public String getProductName() {
        return "Crash Recovery Test";
    }

    public String getProductVersion() {
        return "EAP Test";
    }

    public String getJndiName() {
        String currentXidGlobalId = currentXid == null ? "" :
            "-" + currentXid.getGlobalTransactionId().toString();
        String jndi = "java:/TestXAResource" + currentXidGlobalId;
        System.out.println(String.format("getJndiName()[return %s][id=%s]", jndi, id));
        return jndi;
    }
    
    protected void setPrepareReturnValue(final int prepareReturnValue) {
    	this.prepareReturnValue = prepareReturnValue;
    }
}
