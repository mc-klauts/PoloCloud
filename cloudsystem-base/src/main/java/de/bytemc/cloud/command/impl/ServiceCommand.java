package de.bytemc.cloud.command.impl;

import de.bytemc.cloud.Base;
import de.bytemc.cloud.api.CloudAPI;
import de.bytemc.cloud.api.command.CloudCommand;
import de.bytemc.cloud.api.logger.LogType;
import de.bytemc.cloud.api.services.IService;
import de.bytemc.cloud.api.services.utils.ServiceState;

import java.util.Arrays;
import java.util.List;

public final class ServiceCommand extends CloudCommand {

    public ServiceCommand() {
        super("service", "Manage services", "ser");
    }

    @Override
    public void execute(CloudAPI cloudAPI, String[] args) {
        final var logger = cloudAPI.getLogger();

        if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
            for (final IService service : cloudAPI.getServiceManager().getAllCachedServices()) {
                logger.logMessage("Name of service '§b" + service.getName()
                    + "§7' (§7State of service '§b" + service.getServiceState().getName()
                    + "§7' | Node: '" + service.getGroup().getNode() + "')");
            }
            return;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("stop")) {
            cloudAPI.getServiceManager().getService(args[1]).ifPresentOrElse(service -> {
                if (service.getServiceState() == ServiceState.PREPARED || service.getServiceState() == ServiceState.STOPPING) {
                    logger.logMessage("This service ist not started or already in stopping state.", LogType.WARNING);
                    return;
                }

                service.stop();
                logger.logMessage("The service '§b" + service.getName() + "§7' is now stopped.");
            }, () -> logger.logMessage("This service does not exists.", LogType.WARNING));
            return;
        } else if (args.length == 4) {
            // TODO
            return;
        } else if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            cloudAPI.getServiceManager().getService(args[1]).ifPresentOrElse(service -> {
                logger.logMessage("Service information:");
                logger.logMessage("Name: §b" + service.getName());
                logger.logMessage("ID: §b" + service.getServiceId());
                logger.logMessage("Group: §b" + service.getGroup().getName());
                logger.logMessage("Host: §b" + service.getHostName());
                logger.logMessage("Port: §b" + service.getPort());
            }, () -> logger.logMessage("The service does not exists.", LogType.WARNING));
            return;
        } else if (args.length > 1 && args[0].equalsIgnoreCase("command")) {
            cloudAPI.getServiceManager().getService(args[1]).ifPresentOrElse(service -> {
                final var stringBuilder = new StringBuilder();
                for (int i = 2; i < args.length; i++) stringBuilder.append(args[i]).append(" ");
                final var command = stringBuilder.toString();
                service.executeCommand(command);
                logger.logMessage("Executed command '" + command + "' on service " + service.getName());
            }, () -> logger.logMessage("The service does not exists.", LogType.WARNING));
            return;
        }

        logger.logMessage("§7Use following command: §bservice list §7- List all available services.");
        logger.logMessage("§7Use following command: §bservice start (name) §7- Starting a specific service that not exists.");
        logger.logMessage("§7Use following command: §bservice stop (name) §7- Stopping a specific service that exists.");
        logger.logMessage("§7Use following command: §bservice info (name) §7- Prints information about the specific service.");
        logger.logMessage("§7Use following command: §bservice command (name) (command) §7- Executes a command on a server.");
    }

    @Override
    public List<String> tabComplete(String[] arguments) {
        if (arguments.length == 1) {
            return Arrays.asList("list", "start", "stop", "info", "command");
        } else if (arguments.length == 2) {
            if (!arguments[0].equalsIgnoreCase("list")) {
                return Base.getInstance().getServiceManager().getAllCachedServices().stream().map(IService::getName).toList();
            }
        }
        return super.tabComplete(arguments);
    }
}