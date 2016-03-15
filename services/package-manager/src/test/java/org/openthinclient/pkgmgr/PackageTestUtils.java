package org.openthinclient.pkgmgr;

import org.openthinclient.pkgmgr.db.Package;
import org.openthinclient.pkgmgr.db.Source;
import org.openthinclient.pkgmgr.db.Version;

public class PackageTestUtils {
    public static Package createPackage(String name, String version) {
        final Package pkg = new Package();
        pkg.setName(name);
        pkg.setVersion(version);
        return pkg;
    }

    public static Version createVersion(String upstreamVersions, String debianRevision) {
        final Version version = new Version();
        version.setUpstreamVersion(upstreamVersions);
        version.setDebianRevision(debianRevision);
        return version;
    }

    public static void configureSources(DebianTestRepositoryServer server, PackageManager packageManager) {

        final Source source = new Source();
        source.setEnabled(true);
        source.setUrl(server.getServerUrl());
        packageManager.getSourceRepository().saveAndFlush(source);

    }
}
