package vn.plusplus.lms.repository.rsql;

import cz.jirutka.rsql.parser.ast.ComparisonNode;
import cz.jirutka.rsql.parser.ast.ComparisonOperator;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.jpa.domain.Specification;
import vn.plusplus.lms.repository.entities.enumerates.*;

import javax.persistence.criteria.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static vn.plusplus.lms.repository.rsql.RSQLOperators.*;


@Log4j2
public class GenericRSQLSpecification<T> implements Specification<T> {

    private final static List<SimpleDateFormat> validDateFormats = Arrays.asList(
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ"),
            new SimpleDateFormat("yyyy-MM-dd")
    );

    private String property;
    private ComparisonOperator operator;
    private List<String> arguments;

    public GenericRSQLSpecification(ComparisonNode comparisonNode) {
        super();
        this.property = comparisonNode.getSelector();
        this.operator = comparisonNode.getOperator();
        this.arguments = comparisonNode.getArguments();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public Predicate toPredicate(final Root<T> root, final CriteriaQuery<?> query, final CriteriaBuilder builder) {
        final List<?> args = castArguments(root);
        Expression path = root.get(property);
        if (args.size() == 1) {
            final Object argument = args.get(0);
            boolean isString = argument instanceof String;
            if (EQUAL.equals(operator)) {
                if (isString) {
                    Predicate likePredicate = builder.like(path, argument.toString().replace('*', '%'));
                    if (argument.toString().isEmpty()) {
                        Predicate isNullPredicate = builder.isNull(path);
                        return builder.or(likePredicate, isNullPredicate);
                    }
                    return likePredicate;
                }
                if (argument == null) {
                    return builder.isNull(path);
                }
                return builder.equal(path, argument);
            }
            if (NOT_EQUAL.equals(operator)) {
                if (isString) {
                    return builder.notLike(path, argument.toString().replace('*', '%'));
                }
                if (argument == null) {
                    return builder.isNotNull(path);
                }
                return builder.notEqual(path, argument);
            }

            if (IS_NULL.equals(operator)) {
                return builder.isNull(path);
            }
            if (NOT_NULL.equals(operator)) {
                return builder.isNotNull(path);
            }

            if (LIKE.equals(operator)) {
                return builder.like(path, "%" + argument.toString() + "%");
            }
            if (NOT_LIKE.equals(operator)) {
                return builder.like(path, "%" + argument.toString() + "%").not();
            }

            if (IN.equals(operator)) {
                return builder.equal(path, argument);
            }

            if (NOT_IN.equals(operator)) {
                return builder.notEqual(path, argument);
            }

            Comparable comparable = (Comparable) argument;
            if (GREATER_THAN.equals(operator)) {
                return builder.greaterThan(path, comparable);
            }
            if (GREATER_THAN_OR_EQUAL.equals(operator)) {
                return builder.greaterThanOrEqualTo(path, comparable);
            }
            if (LESS_THAN.equals(operator)) {
                return builder.lessThan(path, comparable);
            }
            if (LESS_THAN_OR_EQUAL.equals(operator)) {
                return builder.lessThanOrEqualTo(path, comparable);
            }

            log.error("Unknown operator: {}", operator);
            throw new IllegalArgumentException("Invalid operator");
        }

        if (args.size() > 1) {
            if (IN.equals(operator)) {
                return path.in(args);
            }
            if (NOT_IN.equals(operator)) {
                return builder.not(path.in(args));
            }
            if (BETWEEN.equals(operator) && args.size() == 2 && args.get(0) instanceof Comparable && args.get(1) instanceof Comparable) {
                return builder.between(path, (Comparable) args.get(0), (Comparable) args.get(1));
            }
            if (NOT_BETWEEN.equals(operator) && args.size() == 2 && args.get(0) instanceof Comparable && args.get(1) instanceof Comparable) {
                return builder.between(path, (Comparable) args.get(0), (Comparable) args.get(1)).not();
            }
            throw new IllegalArgumentException("Invalid operator");
        }
        log.error("Invalid param: {}", args);
        throw new IllegalArgumentException("Invalid param");
    }

    private List<?> castArguments(final Root<T> root) {

        final Class<?> type = root.get(property).getJavaType();

        List<Object> typedArgs = new ArrayList<>();
        for (String arg : arguments) {
            if (type.equals(LessonType.class)) {
                typedArgs.add(LessonType.valueOf(arg));
            } else if (type.equals(Status.class)) {
                typedArgs.add(Status.valueOf(arg));
            } else if (type.equals(AssignmentType.class)) {
                typedArgs.add(AssignmentType.valueOf(arg));
            } else if (type.equals(ExamType.class)) {
                typedArgs.add(ExamType.valueOf(arg));
            } else if (type.equals((MaterialType.class))) {
                typedArgs.add(MaterialType.valueOf(arg));
            } else if (type.equals(QuestionType.class)) {
                typedArgs.add(QuestionType.valueOf(arg));
            } else if (type.equals(QuizzType.class)) {
                typedArgs.add(QuizzType.valueOf(arg));
            } else if (type.equals(SessionType.class)) {
                typedArgs.add(SessionType.valueOf(arg));
            } else if (type.equals(UserCourseState.class)) {
                typedArgs.add(UserCourseState.valueOf(arg));
            } else {
                typedArgs.add(castToType(type, arg));
            }
        }
        return typedArgs;
    }

    private Object castToType(Class<?> type, String arg) {
        if (arg.equals("null") && type != String.class) {
            return null;
        }
        if (type == String.class) {
            return arg;
        }
        if (type == Integer.class || type == int.class) {
            return Integer.parseInt(arg);
        }
        if (type == Long.class || type == long.class) {
            return Long.parseLong(arg);
        }
        if (type == Float.class || type == float.class) {
            return Float.parseFloat(arg);
        }
        if (type == Double.class || type == double.class) {
            return Double.parseDouble(arg);
        }
        if (type == Boolean.class || type == boolean.class) {
            return Boolean.valueOf(arg);
        }
        if (type == Timestamp.class) {
            for (SimpleDateFormat validDateFormat : validDateFormats) {
                try {
                    return new Timestamp(validDateFormat.parse(arg).getTime());
                } catch (ParseException e) {
                    log.debug("Parsing [{}] with [{}] using format [{}] causing [{}], skip", arg, type.getName(), validDateFormat, e.getMessage());
                }
            }
            log.error("Can't parse [{}] with [{}]", arg, type.getName());
            throw new IllegalArgumentException("Can't parse param");
        }

        log.error("Can't parse [{}] with unsupported type [{}]", arg, type.getName());
        throw new IllegalArgumentException("Can't parse param");
    }
}
