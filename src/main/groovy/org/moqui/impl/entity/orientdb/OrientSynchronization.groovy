/*
 * This software is in the public domain under CC0 1.0 Universal plus a 
 * Grant of Patent License.
 * 
 * To the extent possible under law, the author(s) have dedicated all
 * copyright and related and neighboring rights to this software to the
 * public domain worldwide. This software is distributed without any
 * warranty.
 * 
 * You should have received a copy of the CC0 Public Domain Dedication
 * along with this software (see the LICENSE.md file). If not, see
 * <http://creativecommons.org/publicdomain/zero/1.0/>.
 */
package org.moqui.impl.entity.orientdb

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx
import groovy.transform.CompileStatic
import org.moqui.context.TransactionException
import org.moqui.impl.context.ExecutionContextFactoryImpl
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import javax.transaction.Status
import javax.transaction.Synchronization
import javax.transaction.Transaction
import javax.transaction.TransactionManager
import javax.transaction.xa.XAException

@CompileStatic
class OrientSynchronization implements Synchronization {
    protected final static Logger logger = LoggerFactory.getLogger(OrientSynchronization.class)

    protected ExecutionContextFactoryImpl ecfi
    protected OrientDatasourceFactory odf
    protected ODatabaseDocumentTx database

    protected Transaction tx = null


    OrientSynchronization(ExecutionContextFactoryImpl ecfi, OrientDatasourceFactory odf) {
        this.ecfi = ecfi
        this.odf = odf
    }

    OrientSynchronization enlistOrGet() {
        // logger.warn("========= Enlisting new OrientSynchronization")
        TransactionManager tm = ecfi.transactionFacade.getTransactionManager()
        if (tm == null || tm.getStatus() != Status.STATUS_ACTIVE) throw new XAException("Cannot enlist: no transaction manager or transaction not active")
        Transaction tx = tm.getTransaction()
        if (tx == null) throw new XAException(XAException.XAER_NOTA)
        this.tx = tx

        OrientSynchronization existingOxr = (OrientSynchronization) ecfi.transactionFacade.getActiveSynchronization("OrientSynchronization")
        if (existingOxr != null) {
            logger.warn("Tried to enlist OrientSynchronization in current transaction but one is already in place, not enlisting", new TransactionException("OrientSynchronization already in place"))
            return existingOxr
        }
        // logger.warn("================= putting and enlisting new OrientSynchronization")
        ecfi.transactionFacade.putAndEnlistActiveSynchronization("OrientSynchronization", this)

        this.database = odf.getDatabase()
        this.database.begin()

        return this
    }

    ODatabaseDocumentTx getDatabase() { return database }

    @Override
    void beforeCompletion() { }

    @Override
    void afterCompletion(int status) {
        if (status == Status.STATUS_COMMITTED) {
            try {
                database.commit()
            } catch (Exception e) {
                logger.error("Error in OrientDB commit: ${e.toString()}", e)
                throw new XAException("Error in OrientDB commit: ${e.toString()}")
            } finally {
                database.close()
            }
        } else {
            try {
                database.rollback()
            } catch (Exception e) {
                logger.error("Error in OrientDB rollback: ${e.toString()}", e)
                throw new XAException("Error in OrientDB rollback: ${e.toString()}")
            } finally {
                database.close()
            }
        }
    }
}
