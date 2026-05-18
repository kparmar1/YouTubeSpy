package org.self.youtube.spy.processor;

import org.apache.commons.cli.*;
import org.apache.commons.cli.help.HelpFormatter;
import org.self.youtube.spy.exception.ConfigurationException;
import org.self.youtube.spy.model.Config;

import java.util.*;

public class ApacheCommonsArgumentsProcessor implements ArgumentsProcessor {

    private final Options options;
    private final CommandLineParser parser;
    private static final HelpFormatter formatter = org.apache.commons.cli.help.HelpFormatter.builder().get();
    private CommandLine cmd;

    public ApacheCommonsArgumentsProcessor() throws Exception {
        this.parser = new DefaultParser();
        this.options = new Options();

        for (Map.Entry<String, List<ARGUMENT>> entry : ARGUMENT.getGroups().entrySet()) {
            OptionGroup optionGroup =  new OptionGroup();
            optionGroup.setRequired(true);

            for  (ARGUMENT argument : entry.getValue()) {
                Option option = Option.builder(argument.getShortName())
                        .longOpt(argument.getLongName())
                        .hasArg(argument.isHasValue())
                        .argName(argument.getLongName())
                        .desc(argument.getDescription()).get();
                optionGroup.addOption(option);
            }
            options.addOptionGroup(optionGroup);
        }

        for (ARGUMENT argument : ARGUMENT.getNoneGroups()) {
            Option option = Option.builder(argument.getShortName())
                    .longOpt(argument.getLongName())
                    .hasArg(argument.isHasValue())
                    .argName(argument.getLongName())
                    .required(argument.isRequired())
                    .desc(argument.getDescription()).get();
            options.addOption(option);
        }
    }

    public Config getConfig(String[] arguments) throws ConfigurationException {
        try {
            cmd = parser.parse(options, arguments);
            Map<Config.Configuration, String> argumentsHashMap = new HashMap<>();
            for (ARGUMENT argument : ARGUMENT.values()) {
                if (cmd.hasOption(argument.getShortName())) {
                    Config.Configuration configuration = Config.Configuration.fromName(argument.getInternalKey());
                    String configurationValue = cmd.getOptionValue(argument.getShortName());
                    argumentsHashMap.put(configuration, configurationValue);
                }
            }
            return new Config(argumentsHashMap);
        } catch (Exception e) {
            try {
                formatter.printHelp("YouTubeSpy",
                        "Video Subscription Spy",
                        options,
                        "",
                        true
                );
            } catch (Exception ignored) {}
            throw new ConfigurationException();
        }
    }

    @Override
    public boolean hasArgument(ARGUMENT argument) {
        return cmd.hasOption(argument.getShortName());
    }

    @Override
    public String getArgumentValue(ARGUMENT argument) {
        return cmd.getOptionValue(argument.getShortName());
    }

    public static void main(String[] args) throws Exception {
        ArgumentsProcessor argumentsProcessor = new ApacheCommonsArgumentsProcessor();
        Config config = argumentsProcessor.getConfig(new String[]{"-c", "dfdf", "-k", "fdsf"});
        System.out.println(config.toString());
    }
}
