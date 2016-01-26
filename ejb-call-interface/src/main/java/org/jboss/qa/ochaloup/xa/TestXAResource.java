package org.jboss.qa.ochaloup.xa;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Implementation of XAResource for use in tx test cases.
 */
public class TestXAResource extends TestXAResourceCommon implements XAResource {
    private HeuristicType heuristicType;

    public TestXAResource() {
        System.out.println(String.format("TestXAResource.created()[id=%s]", id));
    }

    public TestXAResource (HeuristicType heuristicType) {
        System.out.println(String.format("TestXAResource.created(%s)[id=%s]", heuristicType, id));
        this.heuristicType = heuristicType;
    }

    public TestXAResource (boolean isReadonly) {
        System.out.println(String.format("TestXAResource.created(%s)[id=%s]", isReadonly, id));
        if(isReadonly) {
        	setPrepareReturnValue(XAResource.XA_RDONLY);
        }
    }
    
    private void checkHeuristicType(Xid xid, boolean b) throws XAException {
        if (heuristicType != null) {
            switch (heuristicType) {
            case COMMIT:
                throw new XAException(XAException.XA_HEURCOM);
            case MIXED:
                throw new XAException(XAException.XA_HEURMIX);
            case ROLLBACK:
                throw new XAException(XAException.XA_HEURRB);
            default:
                break;
            }
        }
    }

    @Override
    public void commit(Xid xid, boolean b) throws XAException {
        checkHeuristicType(xid, b);
        super.commit(xid, b);
    }

    @Override
    public void end(Xid xid, int i) throws XAException {
        super.end(xid, i);
    }

    @Override
    public void forget(Xid xid) throws XAException {
        super.forget(xid);
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
    public Xid[] recover(int i) throws XAException {
        return super.recover(i);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        super.rollback(xid);
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
        return new String("TestXAResource(" + super.toString() + ")");
    }
}
