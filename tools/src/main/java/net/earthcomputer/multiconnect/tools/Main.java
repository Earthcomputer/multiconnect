package net.earthcomputer.multiconnect.tools;

import joptsimple.BuiltinHelpFormatter;
import joptsimple.HelpFormatter;
import joptsimple.OptionDescriptor;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import net.earthcomputer.multiconnect.tools.command.CommandBase;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class Main {
    private static final OptionParser parser = new OptionParser();
    private static final HelpFormatter helpFormatter = new BuiltinHelpFormatter(80, 2) {
        @Override
        protected void addNonOptionsDescription(Collection<? extends OptionDescriptor> options) {
            OptionDescriptor nonOptions = findAndRemoveNonOptionsSpec(options);
            if (nonOptions == null) {
                return;
            }
            if (shouldShowNonOptionArgumentDisplay(nonOptions)) {
                addNonOptionRow("Usage:");
                addNonOptionRow(createNonOptionArgumentsDisplay( nonOptions ));
            }
        }

        @Override
        protected String createNonOptionArgumentsDisplay(OptionDescriptor nonOptionDescriptor) {
            return getScriptName() + " " + nonOptionDescriptor.description();
        }

        @Override
        protected OptionDescriptor findAndRemoveNonOptionsSpec(Collection<? extends OptionDescriptor> options) {
            try {
                return super.findAndRemoveNonOptionsSpec(options);
            } catch (AssertionError e) {
                // This is fine because of our custom implementation of addNonOptionDescription
                return null;
            }
        }
    };

    static {
        parser.formatHelpWith(helpFormatter);
    }

    public static final OptionSpec<Void> HELP = parser.accepts("help", "Displays the help menu").forHelp();
    public static final OptionSpec<Void> KEEP_OLD_NAME;
    public static final OptionSpec<String> KEEP_OLD_NAME_UNTIL;
    public static final OptionSpec<String> KEEP_OLD_NAME_SINCE;
    public static final OptionSpec<String> MIN_VERSION = parser.accepts("min-version", "The minimum version to apply an operation to").withRequiredArg();
    public static final OptionSpec<String> MAX_VERSION = parser.accepts("max-version", "The maximum version to apply an operation to").withRequiredArg();

    static {
        OptionSpecBuilder keepOldNameBuilder = parser.accepts("keep-old-name", "Move the existing name to oldName");
        OptionSpecBuilder keepOldNameUntilBuilder = parser.accepts("keep-old-name-until", "Move the existing name to oldName on all versions up to and including this version");
        OptionSpecBuilder keepOldNameSinceBuilder = parser.accepts("keep-old-name-since", "Move the existing name to oldName on all versions down to and including this version");
        parser.mutuallyExclusive(keepOldNameBuilder, keepOldNameUntilBuilder, keepOldNameSinceBuilder);
        KEEP_OLD_NAME = keepOldNameBuilder;
        KEEP_OLD_NAME_UNTIL = keepOldNameUntilBuilder.withRequiredArg();
        KEEP_OLD_NAME_SINCE = keepOldNameSinceBuilder.withRequiredArg();
    }

    public static final Pattern VERSION_REGEX = Pattern.compile("1\\.\\d+(?:\\.\\d)?");

    public static void main(String[] args) {
        try {
            OptionSet options = parser.parse(args);
            parser.nonOptions("<" + String.join("|", CommandBase.COMMANDS.keySet()) + "> ...");

            if (options.has(HELP)) {
                printHelp(options);
                return;
            }

            @SuppressWarnings("unchecked")
            List<String> nonOptions = (List<String>) options.nonOptionArguments();
            CommandBase command;
            if (nonOptions.isEmpty() || (command = CommandBase.COMMANDS.get(nonOptions.get(0))) == null) {
                printHelp(options);
                return;
            }

            if (!command.run(nonOptions.subList(1, nonOptions.size()), options)) {
                printHelp(options);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static String getScriptName() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        String scriptName;
        if (osName.contains("win") && !osName.contains("darwin")) {
            scriptName = "tools.bat";
        } else {
            scriptName = "./tools.sh";
        }
        return scriptName;
    }

    private static void printHelp(OptionSet options) throws IOException {
        @SuppressWarnings("unchecked")
        List<String> nonOptions = (List<String>) options.nonOptionArguments();
        CommandBase command;
        if (!nonOptions.isEmpty() && (command = CommandBase.COMMANDS.get(nonOptions.get(0))) != null) {
            System.out.print(getScriptName() + " " + command.getName() + " ");
            command.printHelp(nonOptions.subList(1, nonOptions.size()), options);
            printFlags("Required Flags:", command.getRequiredFlags());
            printFlags("Optional Flags:", command.getOptionalFlags());
            return;
        }

        parser.printHelpOn(System.out);
    }

    private static void printFlags(String title, List<OptionSpec<?>> flags) {
        if (flags.isEmpty()) {
            return;
        }

        System.out.println();
        System.out.println(title);

        Map<String, OptionDescriptor> options = new LinkedHashMap<>();
        for (OptionSpec<?> flag : flags) {
            for (String option : flag.options()) {
                options.put(option, (OptionDescriptor) flag);
            }
        }
        System.out.print(helpFormatter.format(options));
        System.out.flush();
    }
}
