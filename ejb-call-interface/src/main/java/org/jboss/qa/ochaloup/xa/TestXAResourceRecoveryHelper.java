package org.jboss.qa.ochaloup.xa;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.arjuna.recovery.RecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/**
 * Implementation of XAResourceRecoveryHelper for use in txbridge recovery tests.
 * Provides persistence for TestXAResource via a file in the ObjectStore.
 *
 * This behaves as Recovery Manager for TestXAResource and offers methods for XAResource (Resource Adapter)
 * to persistently save prepared state of the resource.
 */
@Singleton
@Startup
public class TestXAResourceRecoveryHelper implements XAResourceRecoveryHelper {
    private static final TestXAResourceRecoveryHelper instance = new TestXAResourceRecoveryHelper();
    private static final TestXAResourceRecovered xaResourceInstance = new TestXAResourceRecovered();

    private final Set<Xid> preparedXids = new HashSet<Xid>();

    private static final String TEST_XA_RESOURCE_SUB_DIR = "TestXAResourceStateStore";
    private static final String TEST_XA_RESOURCE_FILE = "TestXAResource.ser";

    public static TestXAResourceRecoveryHelper getInstance() {
        return instance;
    }

    /**
     * register the recovery module with the transaction manager.
     */
    @PostConstruct
    public void postConstruct() {
        System.out.println("TestXAResourceRecoveryHelper starting");
        getRecoveryModule().addXAResourceRecoveryHelper(getInstance());
        getInstance().recoverFromDisk();
    }

    /**
     * unregister the recovery module from the transaction manager.
     */
    @PreDestroy
    public void preDestroy() {
        System.out.println("TestXAResourceRecoveryHelper stopping");
        getRecoveryModule().removeXAResourceRecoveryHelper(getInstance());
    }

    private XARecoveryModule getRecoveryModule() {
        for (RecoveryModule recoveryModule : ((Vector<RecoveryModule>) RecoveryManager.manager().getModules())) {
            if (recoveryModule instanceof XARecoveryModule) {
                return (XARecoveryModule) recoveryModule;
            }
        }
        return null;
    }

    public boolean initialise(String param) throws Exception {
        System.out.println(String.format("initialise(param=%s) - in fact doing nothing", param));
        return true;
    }

    public XAResource[] getXAResources() throws Exception {
        System.out.println("getXAResources() instance: " + xaResourceInstance);
        XAResource values[] = new XAResource[1];
        values[0] = xaResourceInstance;

        return values;
    }

    ///////////////////////////

    public void logPrepared(Xid xid) throws XAException {
        System.out.println("logPrepared(xid=" + xid + ")");

        synchronized (preparedXids) {
            if (preparedXids.add(xid)) {
                writeToDisk();
            } else {
                throw new XAException(XAException.XAER_PROTO);
            }
        }
    }

    public void removeLog(Xid xid) throws XAException {
        System.out.println("removeLog(xid=" + xid);

        synchronized (preparedXids) {
            if (preparedXids.remove(xid)) {
                writeToDisk();
            } else {
                System.out.println("no log present for " + xid);
            }
        }
    }

    public Xid[] recover() {
        System.out.println("recover()");
        System.out.println("returning " + preparedXids.size() + " Xids");
        for(Xid xid: preparedXids) {
            System.out.println("returning xid: " + xid);
        }
        return preparedXids.toArray(new Xid[preparedXids.size()]);
    }


    private void writeToDisk() {
        File logFile = getLogFile();
        System.out.println(String.format("logging xids: %s[number: %s] records to %s",
                preparedXids, preparedXids.size(), logFile.getAbsolutePath()));

        try {
            FileOutputStream fos = new FileOutputStream(logFile);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(preparedXids);
            oos.close();
            fos.close();
        } catch (IOException e) {
        	System.out.println(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void recoverFromDisk() {
        File logFile = getLogFile();

        if (!logFile.exists()) {
            System.out.println(String.format("file %s does not exists - no data for recovery",
                    logFile.getAbsolutePath()));
            return;
        }

        System.out.println(String.format("file %s found - expecting data for recovery", logFile.getAbsolutePath()));

        try {
            FileInputStream fis = new FileInputStream(logFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Set<Xid> xids = (Set<Xid>) ois.readObject();
            preparedXids.addAll(xids);
            System.out.println(String.format("Number of xids for recovery is %d.\nContent: %s", xids.size(), xids));
            ois.close();
            fis.close();
        } catch (Exception e) {
            System.out.println(e);
        }

    }

    /**
     * There is needed to get location of tx object store. But this thing seems not being easily accesible.
     * The service class does not provide any getter method for this
     * (check transactions/src/main/java/org/jboss/as/txn/service/ArjunaObjectStoreEnvironmentService.java)
     * and there are three types of ObjectStoreEnvironmentBean with names "default", "stateStore" and "communicationStore)
     *
     * The default one does not contains the correct values when HornetQ object store is used.
     * We use "stateStore" bean. We are using private JTS api and the behavior or bean name could change in future.
     */
    private File getLogFile() {
        String dataDir = System.getProperty("jboss.server.data.dir");
        String txObjectStoreDir = BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "stateStore").getObjectStoreDir();

        if(txObjectStoreDir == null || txObjectStoreDir.isEmpty()) {
            throw new RuntimeException("Not possible to get path to object store dir");
        }
        if(!txObjectStoreDir.contains(dataDir)) {
        	System.out.println("The object store dir is not placed under JBoss data dir. This could be intentional but it could be an error.");
        }

        String subDirectory = TEST_XA_RESOURCE_SUB_DIR;

        File logDir = new File(txObjectStoreDir, subDirectory);
        logDir.mkdirs();
        File logFile = new File(logDir, TEST_XA_RESOURCE_FILE);

        System.out.println(String.format("Using file %s for saving state of the %s XA resource", logFile, TestXAResource.class.getName()));
        return logFile;
    }

}
