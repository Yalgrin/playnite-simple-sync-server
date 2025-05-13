package pl.yalgrin.playnite.simplesync.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import pl.yalgrin.playnite.simplesync.dto.AbstractObjectDTO;
import pl.yalgrin.playnite.simplesync.dto.LinkDTO;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapperUtil {

    public static boolean hasChanged(Long obj1, Long obj2) {
        return !Objects.equals(Optional.ofNullable(obj1).orElse(0L), Optional.ofNullable(obj2).orElse(0L));
    }

    public static boolean hasChanged(String obj1, String obj2) {
        return !StringUtils.equals(obj1, obj2);
    }

    public static boolean hasChanged(Object obj1, Object obj2) {
        return !Objects.equals(obj1, obj2);
    }

    public static boolean hasChanged(List<? extends AbstractObjectDTO> oldList,
                                     List<? extends AbstractObjectDTO> newList) {
        Set<String> oldIds = Optional.ofNullable(oldList)
                .map(e -> e.stream().map(AbstractObjectDTO::getId).filter(Objects::nonNull).collect(Collectors.toSet()))
                .orElseGet(Collections::emptySet);
        Set<String> newIds = Optional.ofNullable(newList)
                .map(e -> e.stream().map(AbstractObjectDTO::getId).filter(Objects::nonNull).collect(Collectors.toSet()))
                .orElseGet(Collections::emptySet);
        return oldIds.size() != newIds.size() || oldIds.stream().anyMatch(id -> !newIds.contains(id));
    }

    public static boolean haveLinksChanged(List<LinkDTO> oldList, List<LinkDTO> newList) {
        oldList = Optional.ofNullable(oldList).orElseGet(Collections::emptyList);
        newList = Optional.ofNullable(newList).orElseGet(Collections::emptyList);
        return oldList.size() != newList.size() || anyLinkChanged(oldList, newList);
    }

    private static boolean anyLinkChanged(List<LinkDTO> oldList, List<LinkDTO> newList) {
        return IntStream.range(0, oldList.size()).anyMatch(i -> {
            LinkDTO oldLink = oldList.get(i);
            LinkDTO newLink = newList.get(i);
            return !StringUtils.equals(oldLink.getUrl(), newLink.getUrl()) || !StringUtils.equals(oldLink.getName(),
                    newLink.getName());
        });
    }

}
