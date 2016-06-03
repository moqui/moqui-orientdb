# Moqui OrientDB Tool Component

[![license](http://img.shields.io/badge/license-CC0%201.0%20Universal-blue.svg)](https://github.com/moqui/moqui-orientdb/blob/master/LICENSE.md)
[![release](http://img.shields.io/github/release/moqui/moqui-orientdb.svg)](https://github.com/moqui/moqui-orientdb/releases)

Moqui Framework tool component for OrientDB, a datasource plugin for the Entity Facade.

To install run (with moqui-framework):

    $ ./gradlew getComponent -Pcomponent=moqui-orientdb

This will add the component to the Moqui runtime/component directory. To use add a entity-facade.datasource element like 
the following to your Moqui Conf XML file:

    <datasource group-name="mygroup" object-factory="org.moqui.impl.entity.orientdb.OrientDatasourceFactory"
            startup-add-missing="true">
        <inline-other uri="plocal:${ORIENTDB_HOME}/databases/MoquiNoSql" username="admin" password="admin"/>
    </datasource>

The OrientDB and dependent JAR files are added to the lib directory when the build is run for this component, which is
designed to be done from the Moqui build (ie from the moqui root directory) along with all other component builds. 
