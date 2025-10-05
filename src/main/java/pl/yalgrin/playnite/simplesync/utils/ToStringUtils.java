package pl.yalgrin.playnite.simplesync.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serial;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ToStringUtils {
    public static final ToStringStyle CUSTOM_STYLE = new CustomStyle();

    public static ToStringBuilder createBuilder(Object object) {
        return new ToStringBuilder(object, CUSTOM_STYLE) {
            @Override
            public ToStringBuilder append(String fieldName, Object obj) {
                if (obj != null) {
                    return super.append(fieldName, obj);
                }
                return this;
            }
        };
    }

    private static final class CustomStyle extends ToStringStyle {
        @Serial
        private static final long serialVersionUID = -3638644891843475385L;

        CustomStyle() {
            setUseShortClassName(true);
            setUseIdentityHashCode(false);
        }

        @Serial
        private Object readResolve() {
            return CUSTOM_STYLE;
        }
    }
}
