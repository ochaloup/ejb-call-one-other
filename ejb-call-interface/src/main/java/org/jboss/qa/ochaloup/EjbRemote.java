package org.jboss.qa.ochaloup;

import javax.ejb.Remote;

@Remote
public interface EjbRemote {
	void callNext();
}
