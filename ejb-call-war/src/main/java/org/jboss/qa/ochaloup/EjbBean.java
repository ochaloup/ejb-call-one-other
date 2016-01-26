package org.jboss.qa.ochaloup;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.jboss.qa.ochaloup.xa.TestXAResource;

@Stateless
@LocalBean
public class EjbBean implements EjbRemote {

    @Resource(mappedName = "java:jboss/TransactionManager")
    protected TransactionManager txn;

	@Override
	public void callNext() {
		System.out.println("START");

		TestXAResource xAResource1 = new TestXAResource();
		try {
			txn.getTransaction().enlistResource(xAResource1);
		} catch (IllegalStateException | RollbackException | SystemException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		TestXAResource xAResource2 = new TestXAResource();
		try {
			txn.getTransaction().enlistResource(xAResource2);
		} catch (IllegalStateException | RollbackException | SystemException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		EjbCallUtils.lookup("ejb-call-ear" + EjbCallUtils.PROJECT_VERSION, "ejb-call-jar" + EjbCallUtils.PROJECT_VERSION,
				EjbBean.class, EjbRemote.class);
		System.out.println("GOOD");
	}
}
