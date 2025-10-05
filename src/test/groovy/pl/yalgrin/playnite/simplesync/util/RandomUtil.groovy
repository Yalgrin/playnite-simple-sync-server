package pl.yalgrin.playnite.simplesync.util

import org.apache.commons.compress.utils.Lists

import java.util.concurrent.ThreadLocalRandom
import java.util.function.Function
import java.util.function.Supplier

class RandomUtil {
    static <T> T generateRandomObject(Supplier<T> supplier, int chanceOfNull = 5) {
        def random = ThreadLocalRandom.current()
        if (random.nextInt(100) < chanceOfNull) {
            return null
        }
        return supplier.get()
    }

    static <T> List<T> generateRandomList(Function<Integer, T> supplier, int min = 0, int max = 10, int chanceOfNull = 5) {
        def random = ThreadLocalRandom.current()
        if (random.nextInt(100) < chanceOfNull) {
            return null
        }
        def number = min + random.nextInt(max - min)
        List<T> result = Lists.newArrayList()
        for (i in 0..<number) {
            result.add(supplier.apply(i))
        }
        return result
    }
}
