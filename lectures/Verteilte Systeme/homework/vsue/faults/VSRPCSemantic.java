package vsue.faults;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface VSRPCSemantic {
    VSRPCSemanticType value(); // default VSRPCSemanticType.LAST_OF_MANY;
}
