package org.openthinclient.pkgmgr.op;

import org.openthinclient.manager.util.http.DownloadManager;
import org.openthinclient.pkgmgr.PackageManagerConfiguration;
import org.openthinclient.pkgmgr.db.Installation;
import org.openthinclient.pkgmgr.db.PackageManagerDatabase;
import org.openthinclient.pkgmgr.progress.ProgressReceiver;
import org.openthinclient.pkgmgr.progress.ProgressTask;
import org.openthinclient.util.dpkg.LocalPackageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PackageManagerOperationTask implements ProgressTask<PackageManagerOperationReport> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PackageManagerOperationTask.class);

    private final PackageManagerConfiguration configuration;
    private final InstallPlan installPlan;
    private final PackageManagerDatabase packageManagerDatabase;
    private final LocalPackageRepository localPackageRepository;
    private final DownloadManager downloadManager;

    public PackageManagerOperationTask(final PackageManagerConfiguration configuration, InstallPlan installPlan, PackageManagerDatabase packageManagerDatabase, LocalPackageRepository localPackageRepository, DownloadManager downloadManager) {
        this.configuration = configuration;
        this.installPlan = installPlan;
        this.packageManagerDatabase = packageManagerDatabase;
        this.localPackageRepository = localPackageRepository;
        this.downloadManager = downloadManager;
    }

    @Override
    public PackageManagerOperationReport execute(ProgressReceiver progressReceiver) throws Exception {
        LOGGER.info("Package installation started.");

        final Installation installation = new Installation();
        installation.setStart(LocalDateTime.now());


        // persist the installation first to allow on the go persistence of the installationlogentry entities
        packageManagerDatabase.getInstallationRepository().save(installation);

        LOGGER.info("Determining packages to be downloaded");

//        // FIXME we should verify that the test install directory is actually empty at the moment.
//        final Path testInstallDir = configuration.getTestinstallDir().toPath();
        final Path installDir = configuration.getInstallDir().toPath();
        LOGGER.info("Operation destination directory: {}", installDir);

        downloadPackages(installation, installDir);

        executeSteps(installation, installDir, installPlan.getSteps());

        installation.setEnd(LocalDateTime.now());

        packageManagerDatabase.getInstallationRepository().save(installation);

        LOGGER.info("Package installation completed.");

        return new PackageManagerOperationReport();
    }

    private void executeSteps(Installation installation, Path installDir, List<InstallPlanStep> steps) throws IOException {

        final List<PackageOperation> operations = new ArrayList<>(steps.size());

        for (InstallPlanStep step : steps) {
            if (step instanceof InstallPlanStep.PackageInstallStep) {
                operations.add(new PackageOperationInstall(((InstallPlanStep.PackageInstallStep) step).getPackage()));
            } else if (step instanceof InstallPlanStep.PackageUninstallStep) {
                operations.add(new PackageOperationUninstall(((InstallPlanStep.PackageUninstallStep) step).getInstalledPackage()));
            } else if (step instanceof InstallPlanStep.PackageVersionChangeStep) {
                operations.add(new PackageOperationUninstall(((InstallPlanStep.PackageVersionChangeStep) step).getInstalledPackage()));
                operations.add(new PackageOperationInstall(((InstallPlanStep.PackageVersionChangeStep) step).getTargetPackage()));
            } else {
                throw new IllegalArgumentException("Unsupported type of install plan step " + step);
            }
        }

        execute(installation, installDir, operations);
    }

    /**
     * Download all packages that are not available in the {@link #localPackageRepository local
     * package repository}
     */
    private void downloadPackages(Installation installation, Path targetDirectory) throws IOException {

        List<PackageOperationDownload> operations = Stream.concat( //
                installPlan.getPackageInstallSteps()
                        .map(InstallPlanStep.PackageInstallStep::getPackage), //
                installPlan.getPackageVersionChangeSteps()
                        .map(InstallPlanStep.PackageVersionChangeStep::getTargetPackage) //
        )
                // filtering out all packages that are already locally available
                .filter(pkg -> !localPackageRepository.isAvailable(pkg))
                .map(pkg -> new PackageOperationDownload(pkg, downloadManager)) //
                .collect(Collectors.toList());

        execute(installation, targetDirectory, operations);
    }

    private void execute(Installation installation, Path targetDirectory, List<? extends PackageOperation> operations) throws IOException {
        for (PackageOperation operation : operations) {
            execute(installation, targetDirectory, operation);
        }
    }

    private void execute(Installation installation, Path targetDirectory, PackageOperation operation) throws IOException {
        final DefaultPackageOperationContext context = new DefaultPackageOperationContext(localPackageRepository, packageManagerDatabase, installation, targetDirectory,
                operation.getPackage());
        operation.execute(context);

        // save the generated log entries
        packageManagerDatabase.getInstallationLogEntryRepository().save(context.getLog());
    }

    @Override
    public ProgressTaskDescription getDescription(Locale locale) {
        // FIXME
        return null;
    }

}
