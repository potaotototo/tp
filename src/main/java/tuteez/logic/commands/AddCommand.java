package tuteez.logic.commands;

import static java.util.Objects.requireNonNull;
import static tuteez.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static tuteez.logic.parser.CliSyntax.PREFIX_EMAIL;
import static tuteez.logic.parser.CliSyntax.PREFIX_LESSON;
import static tuteez.logic.parser.CliSyntax.PREFIX_NAME;
import static tuteez.logic.parser.CliSyntax.PREFIX_PHONE;
import static tuteez.logic.parser.CliSyntax.PREFIX_TAG;
import static tuteez.logic.parser.CliSyntax.PREFIX_TELEGRAM;

import java.util.Set;
import java.util.logging.Logger;

import tuteez.commons.core.LogsCenter;
import tuteez.commons.util.ToStringBuilder;
import tuteez.logic.Messages;
import tuteez.logic.commands.exceptions.CommandException;
import tuteez.model.Model;
import tuteez.model.person.Person;
import tuteez.model.person.lesson.Lesson;

/**
 * Adds a person to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a student to the address book. "
            + "Compulsory Parameters: "
            + PREFIX_NAME + "NAME "
            + PREFIX_PHONE + "PHONE ; "
            + "Optional Parameters:"
            + " [" + PREFIX_EMAIL + "EMAIL]"
            + " [" + PREFIX_ADDRESS + "ADDRESS]"
            + " [" + PREFIX_TELEGRAM + "TELEGRAM USERNAME]"
            + " [" + PREFIX_TAG + "TAG]..."
            + " [" + PREFIX_LESSON + "LESSON]...\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_EMAIL + "johnd@gmail.com "
            + PREFIX_ADDRESS + "Clementi "
            + PREFIX_TELEGRAM + "john_doe "
            + PREFIX_TAG + "Physics "
            + PREFIX_TAG + "Secondary 4 "
            + PREFIX_LESSON + "monday 0900-1100 "
            + PREFIX_LESSON + "saturday 0800-1000\n"
            + "Note: Parameters marked optional can be omitted.";

    public static final String MESSAGE_SUCCESS = "New student added: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON = "This student already exists in the address book";
    public static final String MESSAGE_DUPLICATE_LESSON = "This time slot is already taken by another "
            + " student, please retype command";

    private final Person toAdd;
    private final Logger logger = LogsCenter.getLogger(AddCommand.class);

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasPerson(toAdd)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        Set<Lesson> lessonSet = toAdd.getLessons();
        for (Lesson lesson: lessonSet) {
            assert lesson != null;
            if (model.isClashingWithExistingLesson(lesson)) {
                String logMessage = String.format("Student: %s | Lessons: %s | Conflict: Clashes with "
                        + "another student's lesson", toAdd.getName(), toAdd.getLessons().toString());
                logger.info(logMessage);
                throw new CommandException(MESSAGE_DUPLICATE_LESSON);
            }
        }

        model.addPerson(toAdd);
        logger.info("Student Added - " + toAdd);
        return new CommandResult(String.format(MESSAGE_SUCCESS, Messages.format(toAdd)));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand)) {
            return false;
        }

        AddCommand otherAddCommand = (AddCommand) other;
        return toAdd.equals(otherAddCommand.toAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
